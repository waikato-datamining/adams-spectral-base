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
 * LevelOnePointUtils.java
 * Copyright (C) 2017 University of Waikato, Hamilton, New Zealand
 */

package adams.data.threeway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * A helper class for the Level 2 points of a given Level 1 point.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 3800 $
 */
public class LevelOnePointUtils {

  /** the comparator in use. */
  protected static LevelTwoPointComparator m_Comparator;
  static {
    m_Comparator = new LevelTwoPointComparator(false, true);
  }

  /**
   * Returns the index in points of the given level 2 point, -1 if not found.
   *
   * @param points	the list of level 2 points to search in
   * @param p		the point to get the index for
   * @return		the index or -1 if not found
   */
  public static int findX(List<LevelTwoPoint> points, LevelTwoPoint p) {
    int		result;

    result = Collections.binarySearch(points, p, m_Comparator);
    if (result < 0)
      result = -1;

    return result;
  }

  /**
   * Returns the index in points of the given timestamp.
   *
   * @param points	the list of level points to search in
   * @param x		the X to get the index for
   * @return		the index
   */
  public static int findMassCharge(List<LevelTwoPoint> points, double x) {
    return findX(points, new LevelTwoPoint(x, 0));
  }

  /**
   * Merges the two level 1 points. It sums up the Y of each point and also
   * merges the level 2 data (level 2 points with the same X will
   * get summed up as well).
   *
   * @param point1	the first point
   * @param point2	the second point
   * @return		the merged data
   */
  public static LevelOnePoint merge(LevelOnePoint point1, LevelOnePoint point2) {
    LevelOnePoint 			result;
    List<LevelTwoPoint>			points1;
    List<LevelTwoPoint>			points2;
    Hashtable<Double,LevelTwoPoint>	pool;
    int					i;
    LevelTwoPoint 			l2;
    LevelTwoPoint 			l2Pool;
    Enumeration<LevelTwoPoint>		elements;

    result = new LevelOnePoint(point1.getX(), point1.getY() + point2.getY());

    points1 = point1.toList(new LevelTwoPointComparator(false, true));
    points2 = point2.toList(new LevelTwoPointComparator(false, true));

    // init pool
    pool = new Hashtable<>();
    for (i = 0; i < points1.size(); i++) {
      l2 = points1.get(i);
      pool.put(l2.getX(), l2);
    }

    // add to pool
    for (i = 0; i < points2.size(); i++) {
      l2 = points2.get(i);
      if (pool.containsKey(l2.getX())) {
	l2Pool = pool.get(l2.getX());
	l2Pool.setY(l2Pool.getY() + l2.getY());
      }
      else {
	pool.put(l2.getX(), l2);
      }
    }

    // create new point
    elements = pool.elements();
    while (elements.hasMoreElements())
      result.add((LevelTwoPoint) elements.nextElement().getClone());

    return result;
  }

  /**
   * Generates data for a histogram display. It counts how many level 2 points have
   * the same Y in the level 1 point.
   *
   * @param l1		the level 1 point to generate the histogram for
   * @param numBins	the number of bins to generate
   * @return		the histogram data
   */
  public static double[] getHistogram(LevelOnePoint l1, int numBins) {
    List<LevelTwoPoint>	points;
    double[]		result;
    double		min;
    double		max;
    int			i;
    double		scale;

    result = new double[numBins];

    points = new ArrayList<>(l1.toTreeSet(new LevelTwoPointComparator(true, true)));
    min    = points.get(0).getY();
    max    = points.get(points.size() - 1).getY();
    scale  = 1.0 / (max - min) / ((double) numBins);
    for (i = 0; i < points.size(); i++)
      result[(int) ((points.get(i).getY() - min)*scale)]++;

    return result;
  }

  /**
   * Returns the abundance as double array.
   *
   * @param c		the GC point to turn into a double array
   * @return		the abundances as double array
   */
  public static double[] toDoubleArray(LevelOnePoint c) {
    return toDoubleArray(c.toList());
  }

  /**
   * Returns the abundance as double array.
   *
   * @param data	the MS points to turn into a double array
   * @return		the abundances as double array
   */
  public static double[] toDoubleArray(List<LevelTwoPoint> data) {
    double[] 	result;
    int 	i;

    result = new double[data.size()];
    i      = 0;
    for (LevelTwoPoint l2 : data)
      result[i++] = l2.getY();

    return result;
  }

  /**
   * Interpolates the two level 1 points and the corresponding level 2 data.
   *
   * @param x		the timestamp we have to interpolate for
   * @param left	the left level 1 point
   * @param right	the right level 1 point
   * @return		the interpolated level 1 point
   */
  public static LevelOnePoint interpolate(double x, LevelOnePoint left, LevelOnePoint right) {
    LevelOnePoint 		result;
    double 			xdiff;
    double			percLeft;
    double			percRight;
    Iterator<LevelTwoPoint>	iterLeft;
    Iterator<LevelTwoPoint>	iterRight;
    LevelTwoPoint 		l2Left;
    LevelTwoPoint 		l2Right;
    LevelTwoPoint 		l2New;
    double 			l2LeftX;
    double 			l2RightX;

    // interpolate GCpoint
    xdiff = right.getX() - left.getX();
    percLeft  = 1 - (x - left.getX()) / xdiff;
    percRight = 1 - (right.getX() - x) / xdiff;
    result    = new LevelOnePoint(x, Math.round(left.getY()*percLeft + right.getY()*percRight));

    // interpolate MS data
    iterLeft  = left.toList().iterator();
    iterRight = right.toList().iterator();
    l2Left    = null;
    l2Right   = null;
    l2LeftX   = Double.MIN_VALUE;
    l2RightX  = Double.MIN_VALUE;
    while (iterLeft.hasNext() || iterRight.hasNext()) {
      // get points, if necessary/possible
      if ((l2Left == null) && (iterLeft.hasNext())) {
	l2Left  = iterLeft.next();
	l2LeftX = l2Left.getX();
      }
      if ((l2Right == null) && (iterRight.hasNext())) {
	l2Right  = iterRight.next();
	l2RightX = l2Right.getX();
      }

      // interpolate MS points
      if (    (l2Left != null)
	   && (l2Right != null)
	   && (l2LeftX == l2RightX) ) {
	l2New = new LevelTwoPoint(
	  l2Left.getX(),
	  (Math.round(l2Left.getY() * percLeft) + Math.round(l2Right.getY() * percRight)));
	l2Left = null;
	l2Right = null;
	result.add(l2New);
      }
      else {
	if ((l2Left != null) && (l2LeftX < l2RightX)) {
	  l2New = new LevelTwoPoint(
	    l2Left.getX(),
	    Math.round(l2Left.getY() * percLeft));
	  l2Left = null;
	  result.add(l2New);
	}
	else if ((l2Right != null) && (l2RightX < l2LeftX)) {
	  l2New = new LevelTwoPoint(
	      l2Right.getX(),
	      Math.round(l2Right.getY() * percRight));
	  l2Right = null;
	  result.add(l2New);
	}
      }

      if (!iterLeft.hasNext() && (l2Left == null))
	l2LeftX = Double.MAX_VALUE;
      if (!iterRight.hasNext() && (l2Right == null))
	l2RightX = Double.MAX_VALUE;
    }

    return result;
  }

  /**
   * Returns a level 1 point with the level 2 data of the closest of the two level 1 points.
   *
   * @param x		the X we have to interpolate for
   * @param left	the left level 1 point
   * @param right	the right level 1 point
   * @return		the interpolated level 1point
   */
  public static LevelOnePoint closest(long x, LevelOnePoint left, LevelOnePoint right) {
    LevelOnePoint 		result;
    double 			xdiff;
    double			percLeft;
    double			percRight;
    Iterator<LevelTwoPoint> 	l2Iter;

    xdiff = right.getX() - left.getX();
    percLeft  = 1 - (x - left.getX()) / xdiff;
    percRight = 1 - (right.getX() - x) / xdiff;
    result    = new LevelOnePoint(x, Math.round(left.getY()*percLeft + right.getY()*percRight));

    // add level 2 dta
    if (percLeft > percRight)
      l2Iter = left.toList().iterator();
    else
      l2Iter = right.toList().iterator();

    while (l2Iter.hasNext())
      result.add((LevelTwoPoint) l2Iter.next().getClone());

    return result;
  }
}
