/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * GCPointUtils.java
 * Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 */

package adams.data.chromatogram;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * A helper class for the MS points of a given GC point.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 3800 $
 */
public class GCPointUtils {

  /** the comparator in use. */
  protected static MSComparator m_Comparator;
  static {
    m_Comparator = new MSComparator(false, true);
  }

  /**
   * Returns the index in points of the given MS point, -1 if not found.
   *
   * @param points	the vector of MS points to search in
   * @param p		the point to get the index for
   * @return		the index or -1 if not found
   */
  public static int findMassCharge(List<MSPoint> points, MSPoint p) {
    int		result;

    result = Collections.binarySearch(points, p, m_Comparator);
    if (result < 0)
      result = -1;

    return result;
  }

  /**
   * Returns the index in points of the given timestamp.
   *
   * @param points	the vector of MS points to search in
   * @param mz		the m/z ratio to get the index for
   * @return		the index
   */
  public static int findMassCharge(List<MSPoint> points, float mz) {
    return findMassCharge(points, new MSPoint(mz, 0));
  }

  /**
   * Merges the two GC points. It sums up the abundance of each point and also
   * merges the mass-spec data (mass-spec points with the same m/z ratio will
   * get summed up as well).
   *
   * @param point1	the first point
   * @param point2	the second point
   * @return		the merged data
   */
  public static GCPoint merge(GCPoint point1, GCPoint point2) {
    GCPoint			result;
    Vector<MSPoint>		points1;
    Vector<MSPoint>		points2;
    Hashtable<Float,MSPoint>	pool;
    int				i;
    MSPoint			ms;
    MSPoint			msPool;
    Enumeration<MSPoint>	elements;

    result = new GCPoint(point1.getTimestamp(), point1.getAbundance() + point2.getAbundance());

    points1 = point1.toVector(new MSComparator(false, true));
    points2 = point2.toVector(new MSComparator(false, true));

    // init pool
    pool = new Hashtable<Float,MSPoint>();
    for (i = 0; i < points1.size(); i++) {
      ms = points1.get(i);
      pool.put(ms.getMassCharge(), ms);
    }

    // add to pool
    for (i = 0; i < points2.size(); i++) {
      ms = points2.get(i);
      if (pool.containsKey(ms.getMassCharge())) {
	msPool = pool.get(ms.getMassCharge());
	msPool.setAbundance(msPool.getAbundance() + ms.getAbundance());
      }
      else {
	pool.put(ms.getMassCharge(), ms);
      }
    }

    // create new point
    elements = pool.elements();
    while (elements.hasMoreElements())
      result.add((MSPoint) elements.nextElement().getClone());

    return result;
  }

  /**
   * Generates data for a histogram display. It counts how many MS points have
   * the same abundance in the GC point.
   *
   * @param c		the GC point to generate the histogram for
   * @param numBins	the number of bins to generate
   * @return		the histogram data
   */
  public static double[] getHistogram(GCPoint c, int numBins) {
    Vector<MSPoint>	points;
    double[]		result;
    long		min;
    long		max;
    int			i;
    double		scale;

    result = new double[numBins];

    points = new Vector<MSPoint>(c.toTreeSet(new MSComparator(true, true)));
    min    = points.firstElement().getAbundance();
    max    = points.lastElement().getAbundance();
    scale  = 1.0 / ((double) (max - min + 1) / ((double) numBins));
    for (i = 0; i < points.size(); i++)
      result[(int) ((points.get(i).getAbundance() - min)*scale)]++;

    return result;
  }

  /**
   * Returns the abundance as double array.
   *
   * @param c		the GC point to turn into a double array
   * @return		the abundances as double array
   */
  public static double[] toDoubleArray(GCPoint c) {
    return toDoubleArray(c.toList());
  }

  /**
   * Returns the abundance as double array.
   *
   * @param data	the MS points to turn into a double array
   * @return		the abundances as double array
   */
  public static double[] toDoubleArray(List<MSPoint> data) {
    double[] 	result;
    int 	i;

    result = new double[data.size()];
    i      = 0;
    for (MSPoint gcp:data)
      result[i++] = new Double(gcp.getAbundance());

    return result;
  }

  /**
   * Interpolates the two GCPoints and the corresponding MS data.
   *
   * @param timestamp	the timestamp we have to interpolate for
   * @param left	the "earlier" GCPoint
   * @param right	the "later" GCPoint
   * @return		the interpolated GC point
   */
  public static GCPoint interpolate(long timestamp, GCPoint left, GCPoint right) {
    GCPoint		result;
    double		timediff;
    double		percLeft;
    double		percRight;
    Iterator<MSPoint>	iterLeft;
    Iterator<MSPoint>	iterRight;
    MSPoint		msLeft;
    MSPoint		msRight;
    MSPoint		msNew;
    double		msLeftMZ;
    double		msRightMZ;

    // interpolate GCpoint
    timediff  = right.getTimestamp() - left.getTimestamp();
    percLeft  = 1 - ((double) (timestamp - left.getTimestamp()) / timediff);
    percRight = 1 - ((double) (right.getTimestamp() - timestamp) / timediff);
    result    = new GCPoint(
			timestamp,
			Math.round(
			      (double) left.getAbundance()*percLeft
			    + (double) right.getAbundance()*percRight));

    // interpolate MS data
    iterLeft  = left.toList().iterator();
    iterRight = right.toList().iterator();
    msLeft    = null;
    msRight   = null;
    msLeftMZ  = Double.MIN_VALUE;
    msRightMZ = Double.MIN_VALUE;
    while (iterLeft.hasNext() || iterRight.hasNext()) {
      // get points, if necessary/possible
      if ((msLeft == null) && (iterLeft.hasNext())) {
	msLeft   = iterLeft.next();
	msLeftMZ = msLeft.getMassCharge();
      }
      if ((msRight == null) && (iterRight.hasNext())) {
	msRight   = iterRight.next();
	msRightMZ = msRight.getMassCharge();
      }

      // interpolate MS points
      if (    (msLeft != null)
	   && (msRight != null)
	   && (msLeftMZ == msRightMZ) ) {
	msNew = new MSPoint(
	    msLeft.getMassCharge(),
	      (int)
	      (Math.round(msLeft.getAbundance() * percLeft)
	    + Math.round(msRight.getAbundance() * percRight)));
	msLeft  = null;
	msRight = null;
	result.add(msNew);
      }
      else {
	if ((msLeft != null) && (msLeftMZ < msRightMZ)) {
	  msNew = new MSPoint(
	      msLeft.getMassCharge(),
	      (int) Math.round(msLeft.getAbundance() * percLeft));
	  msLeft = null;
	  result.add(msNew);
	}
	else if ((msRight != null) && (msRightMZ < msLeftMZ)) {
	  msNew = new MSPoint(
	      msRight.getMassCharge(),
	      (int) Math.round(msRight.getAbundance() * percRight));
	  msRight = null;
	  result.add(msNew);
	}
      }

      if (!iterLeft.hasNext() && (msLeft == null))
	msLeftMZ = Double.MAX_VALUE;
      if (!iterRight.hasNext() && (msRight == null))
	msRightMZ = Double.MAX_VALUE;
    }

    return result;
  }

  /**
   * Returns a GC point with the MS spectrum of the closest of the two GCPoints.
   *
   * @param timestamp	the timestamp we have to interpolate for
   * @param left	the "earlier" GCPoint
   * @param right	the "later" GCPoint
   * @return		the interpolated GC point
   */
  public static GCPoint closest(long timestamp, GCPoint left, GCPoint right) {
    GCPoint		result;
    double		timediff;
    double		percLeft;
    double		percRight;
    Iterator<MSPoint>	msIter;

    timediff  = right.getTimestamp() - left.getTimestamp();
    percLeft  = 1 - ((double) (timestamp - left.getTimestamp()) / timediff);
    percRight = 1 - ((double) (right.getTimestamp() - timestamp) / timediff);
    result    = new GCPoint(
			timestamp,
			Math.round(
			      (double) left.getAbundance()*percLeft
			    + (double) right.getAbundance()*percRight));

    // add MS spectrum
    if (percLeft > percRight)
      msIter = left.toList().iterator();
    else
      msIter = right.toList().iterator();

    while (msIter.hasNext())
      result.add((MSPoint) msIter.next().getClone());

    return result;
  }
}
