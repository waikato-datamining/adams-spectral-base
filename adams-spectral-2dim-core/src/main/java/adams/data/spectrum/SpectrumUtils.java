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
 * SpectrumUtils.java
 * Copyright (C) 2008-2024 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrum;

import adams.data.container.DataContainerUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for spectra.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumUtils
  extends DataContainerUtils {

  /** comparator for finding wavenumbers. */
  protected static SpectrumPointComparator m_Comparator;
  static {
    m_Comparator = new SpectrumPointComparator(false, true);
  }

  /**
   * Returns the comparator used for finding wave numbers.
   *
   * @return		the comparator
   */
  public static SpectrumPointComparator getComparator() {
    return m_Comparator;
  }

  /**
   * Creates a header based on the given spectrum points.
   *
   * @param points	the spectrum points to create the header for
   * @return		the generated header
   */
  protected static Spectrum getHeader(List<SpectrumPoint> points) {
    Spectrum	result;

    if (!points.isEmpty() && (points.get(0).getParent() != null)) {
      result = ((Spectrum) points.get(0).getParent()).getHeader();
    }
    else {
      result = new Spectrum();
      result.setID("unknown");
    }

    return result;
  }

  /**
   * Returns the index in points of the given spectrum point, -1 if not found.
   *
   * @param points	the vector of spectrum points to search in
   * @param p		the point to get the index for
   * @return		the index or -1 if not found
   */
  public static int findWaveNumber(List<SpectrumPoint> points, SpectrumPoint p) {
    int		result;

    result = Collections.binarySearch(points, p, m_Comparator);
    if (result < 0)
      result = -1;

    return result;
  }

  /**
   * Returns the index in points of the given wave number.
   *
   * @param points	the vector of spectrum points to search in
   * @param waveno	the wave number to get the index for
   * @return		the index
   */
  public static int findWaveNumber(List<SpectrumPoint> points, float waveno) {
    return findWaveNumber(points, new SpectrumPoint(waveno, 0));
  }

  /**
   * Returns the index in points closest to the given wave number.
   *
   * @param points	the vector of spectrum points to search in
   * @param waveno	the wave number to get the closest index for
   * @return		the index
   */
  public static int findClosestWaveNumber(List<SpectrumPoint> points, float waveno) {
    int			result;
    int			index;
    SpectrumPoint	currPoint;
    double		currDist;
    double		dist;
    int			i;

    result = -1;

    if (points.isEmpty())
      return result;

    index = Collections.binarySearch(points, new SpectrumPoint(waveno, 0), m_Comparator);

    // no exact match -> find closest
    if (index < 0) {
      index = -index;
      if (index >= points.size())
	index = points.size() - 1;
      result = index;
      dist   = Math.abs(waveno - points.get(index).getWaveNumber());

      for (i = index - 2; i <= index + 2; i++) {
	if ((i >= 0) && (i < points.size())) {
	  currPoint = points.get(i);
	  currDist  = Math.abs(waveno - currPoint.getWaveNumber());

	  if (currDist < dist) {
	    dist   = currDist;
	    result = i;
	  }
	}
      }
    }
    else {
      result = index;
    }

    return result;
  }

  /**
   * Returns the indices of points in m_Points that enclose the given wave number.
   * If the given wave number happens to be an exact point, then this points will
   * be stored at index 0. If no index could be determined, then -1 will be
   * stored.
   *
   * @param points	the vector of spectrum points to search in
   * @param waveno	the wave number to get the enclosing indices for
   * @return		the indices
   */
  public static int[] findEnclosingWaveNumbers(List<SpectrumPoint> points, float waveno) {
    int[]	result;
    int		index;

    result = new int[]{-1, -1};

    index = findClosestWaveNumber(points, waveno);
    if (index > -1) {
      // found exact wave number (or left of wave number) -> store at position 0
      if (points.get(index).getWaveNumber() <= waveno) {
	result[0] = index;
	if (index < points.size() - 1)
	  result[1] = index + 1;
      }
      // right of the wave number
      else if (points.get(index).getWaveNumber() > waveno) {
	result[1] = index;
	if (index > 0)
	  result[0] = index - 1;
      }
    }

    return result;
  }

  /**
   * Returns a region for the given range, starting just after the
   * wave number of "lastEnd" and ending (including) at "end".
   *
   * @param points	the spectrum points to work on
   * @param lastEnd	the last end point, if null then the first spectrum point
   * 			will be the first point included.
   * @param end		the last point to include in the region, if null then
   * 			the last point in the data is used.
   * @return		the generated region
   */
  public static Spectrum getConsecutiveRegion(List<SpectrumPoint> points, SpectrumPoint lastEnd, SpectrumPoint end) {
    Spectrum	result;
    int			indexStart;
    int			indexEnd;
    int			i;

    result = getHeader(points);

    if (lastEnd == null)
      indexStart = 0;
    else
      indexStart = findWaveNumber(points, lastEnd) + 1;

    if (end == null)
      indexEnd = points.size() - 1;
    else
      indexEnd = findWaveNumber(points, end);

    for (i = indexStart; i <= indexEnd; i++)
      result.add((SpectrumPoint) points.get(i).getClone());

    return result;
  }

  /**
   * Returns a region for the given range, including both, start and end point.
   *
   * @param points	the spectrum points to work on
   * @param start	the starting point, if null the first point in the
   * 			data is used.
   * @param end		the last point to include in the region, if null then
   * 			the last point in the data is used.
   * @return		the generated region
   */
  public static Spectrum getRegion(List<SpectrumPoint> points, SpectrumPoint start, SpectrumPoint end) {
    Spectrum	result;
    int			indexStart;
    int			indexEnd;
    int			i;

    result = getHeader(points);

    if (start == null)
      indexStart = 0;
    else
      indexStart = findWaveNumber(points, start);

    if (end == null)
      indexEnd = points.size() - 1;
    else
      indexEnd = findWaveNumber(points, end);

    for (i = indexStart; i <= indexEnd; i++)
      result.add((SpectrumPoint) points.get(i).getClone());

    return result;
  }

  /**
   * Counts the sign changes in the given data between the given points (incl.
   * the borders).
   *
   * @param points	the data to check for changes in sign
   * @param start	the wave number to start with
   * @param end		the last wave number
   * @return		the number of changes in sign
   */
  public static int countSignChanges(List<SpectrumPoint> points, long start, long end) {
    int			result;
    int			startIndex;
    int			endIndex;
    int			i;
    double		ampl;
    SpectrumPoint	point;

    result     = 0;
    startIndex = findWaveNumber(points, start);
    endIndex   = findWaveNumber(points, end);
    ampl       = points.get(startIndex).getAmplitude();

    for (i = startIndex + 1; i <= endIndex; i++) {
      point = points.get(i);
      if (Math.signum(point.getAmplitude()) != Math.signum(ampl)) {
	result++;
	ampl = point.getAmplitude();
      }
    }

    return result;
  }

  /**
   * Counts the positive or negative regions between the given points (incl.
   * the borders).
   *
   * @param points	the data to check for regions
   * @param start	the wave number to start with
   * @param end		the last wave number
   * @param positive	if true then positive regions are counted otherwise
   * 			negative ones
   * @return		the number of positive/negative regions
   */
  public static int countRegions(List<SpectrumPoint> points, long start, long end, boolean positive) {
    int			result;
    int			startIndex;
    int			endIndex;
    int			i;
    double		ampl;
    SpectrumPoint	point;

    result     = 0;
    startIndex = findWaveNumber(points, start);
    endIndex   = findWaveNumber(points, end);
    ampl       = points.get(startIndex).getAmplitude();
    if (positive && (ampl >= 0))
      result++;
    else if (!positive && (ampl < 0))
      result++;

    for (i = startIndex + 1; i <= endIndex; i++) {
      point = points.get(i);
      if (Math.signum(point.getAmplitude()) != Math.signum(ampl)) {
	ampl = point.getAmplitude();
	if (positive && (ampl >= 0))
	  result++;
	else if (!positive && (ampl < 0))
	  result++;
      }
    }

    return result;
  }

  /**
   * Returns the union of the two spectra "a" and "b". All spectrum points
   * from "a" are used, plus the ones from "b" that are not in "a".
   *
   * @param a		the first spectrum
   * @param b		the second spectrum
   * @return		the union
   */
  public static Spectrum union(Spectrum a, Spectrum b) {
    Spectrum		result;
    List<SpectrumPoint>	points;
    int			i;

    result = (Spectrum) a.getClone();

    points = b.toList();
    for (i = 0; i < points.size(); i++) {
      if (a.find(points.get(i).getWaveNumber()) == null)
	result.add((SpectrumPoint) points.get(i).getClone());
    }

    return result;
  }

  /**
   * Returns a spectrum that contains all the points that are in "a"
   * but not in "b". It is assumed that "b" is a subset of "a"
   * and does not contain other wave numbers.
   *
   * @param a		the "full" spectrum
   * @param b		the "subset" spectrum
   * @return		the points missing in "b"
   */
  public static Spectrum minus(Spectrum a, Spectrum b) {
    Spectrum		result;
    List<SpectrumPoint>	points;
    int			i;

    result = a.getHeader();

    points = a.toList();
    for (i = 0; i < points.size(); i++) {
      if (b.find(points.get(i).getWaveNumber()) == null)
	result.add((SpectrumPoint) points.get(i).getClone());
    }

    return result;
  }

  /**
   * Returns a spectrum that contains all the points that are in "a"
   * and in "b". The spectrum points in the result are taken from "a".
   *
   * @param a		the first spectrum
   * @param b		the second spectrum
   * @return		the points in "a" and "b"
   */
  public static Spectrum intersect(Spectrum a, Spectrum b) {
    Spectrum		result;
    List<SpectrumPoint>	points;
    int			i;

    result = a.getHeader();

    points = a.toList();
    for (i = 0; i < points.size(); i++) {
      if (b.find(points.get(i).getWaveNumber()) != null)
	result.add((SpectrumPoint) points.get(i).getClone());
    }

    return result;
  }

  /**
   * Returns all the regions that are in "a", but not in "b". It is assumed
   * that "b" is a subset of "a" and does not contain other wave numbers.
   *
   * @param a		the "full" spectrum
   * @param b		the "subset" spectrum
   * @return		the missing regions
   */
  public static List<Spectrum> getMissingRegions(Spectrum a, Spectrum b) {
    List<Spectrum>	result;
    Spectrum		region;
    List<SpectrumPoint>	points;
    int			i;

    result = new ArrayList<Spectrum>();

    region = null;
    points = a.toList();
    for (i = 0; i < points.size(); i++) {
      if (b.find(points.get(i).getWaveNumber()) == null) {
	if (region == null) {
	  region = a.getHeader();
	  result.add(region);
	}
	region.add((SpectrumPoint) points.get(i).getClone());
      }
      else {
	region = null;
      }
    }

    return result;
  }

  /**
   * Fills the gaps in the "gaps" spectrum with the data from
   * the "reference" spectrum and the type of gap-filling.
   *
   * @param gaps	the spectrum with gaps to fill
   * @param reference	the reference spectrum to get the missing data from
   * @param type	the type of gap-filling to perform
   * @return		the processed spectrum (a copy of it)
   */
  public static Spectrum fillGaps(Spectrum gaps, Spectrum reference, GapFilling type) {
    Spectrum		result;
    Spectrum		tmpData;
    List<SpectrumPoint>	points;
    SpectrumPoint	point;
    int			i;
    int			first;
    int			second;
    double		delta;
    double		base;

    result = (Spectrum) gaps.getClone();

    // do nothing
    if (type == GapFilling.NOTHING) {
      // ignored
    }

    // add points with zero abundance
    else if (type == GapFilling.ZERO) {
      tmpData = SpectrumUtils.minus(reference, gaps);
      points  = tmpData.toList();
      for (i = 0; i < points.size(); i++) {
	point = (SpectrumPoint) points.get(i).getClone();
	point.setAmplitude(0);
	result.add(point);
      }
    }

    // add points with the original abundance
    else if (type == GapFilling.ORIGINAL) {
      tmpData = SpectrumUtils.minus(reference, gaps);
      points  = tmpData.toList();
      for (i = 0; i < points.size(); i++) {
	point = (SpectrumPoint) points.get(i).getClone();
	result.add(point);
      }
    }

    // connect regions with points with slowly increasing/decreasing abundances
    else if (type == GapFilling.CONNECT) {
      points = reference.toList();
      i      = 0;
      while (i < points.size()) {
	// end of a non-noise region?
	if (gaps.find(points.get(i).getWaveNumber()) == null) {
	  first = i - 1;

	  // find start of next region
	  while ((i < points.size()) && (gaps.find(points.get(i).getWaveNumber()) == null)) {
	    i++;
	  }

	  // found another region or end of points?
	  if (i < points.size()) {
	    second = i;
	    delta  = (double) points.get(second).getAmplitude();
	  }
	  else {
	    second = points.size();
	    delta  = 0;
	  }
	  base = 0;
	  if (first > -1) {
	    delta -= (double) points.get(first).getAmplitude();
	    base   = points.get(first).getAmplitude();
	  }
	  delta /= (double) (second - first + 1);

	  // fill in points
	  for (i = first + 1; i < second; i++) {
	    point = (SpectrumPoint) points.get(i).getClone();
	    point.setAmplitude((float) ((long) ((i - first)*delta) + base));
	    result.add(point);
	  }
	}

	i++;
      }
    }

    // unhandled type!
    else {
      throw new IllegalArgumentException("Unhandled gap-filling type: " + type);
    }

    return result;
  }

  /**
   * Merges the given spectrum with the current data pool. New spectrum
   * points don't override ones already in the pool, they only get added.
   *
   * @param pool	the current data pool
   * @param s		the spectrum to merge with the pool
   */
  protected static void add(Hashtable<Float,SpectrumPoint> pool, Spectrum s) {
    Iterator<SpectrumPoint>	iter;
    SpectrumPoint		pointSP;
    SpectrumPoint		poolSP;

    iter = s.toList().iterator();
    while (iter.hasNext()) {
      pointSP = iter.next();
      poolSP  = pool.get(pointSP.getWaveNumber());
      if (poolSP == null)
	poolSP = pointSP;
      pool.put(poolSP.getWaveNumber(), poolSP);
    }
  }

  /**
   * Merges the two spectra. The header of the first one is used for the
   * output.
   *
   * @param s1		the first spectrum
   * @param s2		the second spectrum
   * @return		the merged spectrum
   */
  public static Spectrum merge(Spectrum s1, Spectrum s2) {
    List<Spectrum>	list;

    list = new ArrayList<Spectrum>();
    list.add(s1);
    list.add(s2);

    return merge(list);
  }

  /**
   * Merges the given spectra. THe header of the first one is used for
   * the output.
   *
   * @param list	the spectra to merge
   * @return		the merged spectrum
   */
  public static Spectrum merge(List<Spectrum> list) {
    Spectrum				result;
    int					i;
    Hashtable<Float,SpectrumPoint>	pool;
    Enumeration<SpectrumPoint>		elements;

    if (list.isEmpty())
      return null;
    else if (list.size() == 1)
      return list.get(0);

    result = list.get(0).getHeader();
    pool   = new Hashtable<>();
    for (i = 0; i < list.size(); i++) {
      add(pool, list.get(i));
    }

    // create output data
    elements = pool.elements();
    while (elements.hasMoreElements())
      result.add(elements.nextElement());

    return result;
  }

  /**
   * Returns the amplitudes as double array.
   *
   * @param c		the spectrum to turn into a double array
   * @return		the amplitudes as double array
   */
  public static double[] toDoubleArray(Spectrum c) {
    return toDoubleArray(c.toList());
  }

  /**
   * Returns the amplitudes as double array.
   *
   * @param data	the spectrum points to turn into a double array
   * @return		the amplitudes as double array
   */
  public static double[] toDoubleArray(List<SpectrumPoint> data) {
    double[] 	result;
    int 	i;

    result = new double[data.size()];
    i      = 0;
    for (SpectrumPoint gcp:data)
      result[i++] = gcp.getAmplitude();

    return result;
  }
  
  /**
   * Computes the difference between two spectra.
   * 
   * @param s1		the first spectrum
   * @param s2		the second spectrum
   * @param diff	the spectrum to store the difference
   * @param absolute	whether to compute the absolute difference
   * @return		null if successful, otherwise error message
   */
  public static String diff(Spectrum s1, Spectrum s2, Spectrum diff, boolean absolute) {
    String		result;
    SpectrumPoint	pointDiff;
    SpectrumPoint	point0;
    SpectrumPoint	point1;
    int			i;
    float		ampl;

    result = null;

    if (s1.size() != s2.size()) {
      result = "Spectra differ in size: " + s1.size() + " != " + s2.size();
    }
    else {
      diff.setID(s1.getID() + " - " + s2.getID());
      for (i = 0; i < s1.size(); i++) {
	point0 = s1.toList().get(i);
	point1 = s2.toList().get(i);
	if (point0.getWaveNumber() != point1.getWaveNumber()) {
	  result = "Wave numbers differ at #" + (i+1) + ": " + point0.getWaveNumber() + " != " + point1.getWaveNumber();
	  break;
	}
	else {
	  if (absolute)
	    ampl = Math.abs(point0.getAmplitude() - point1.getAmplitude());
	  else
	    ampl = point0.getAmplitude() - point1.getAmplitude();
	  pointDiff = new SpectrumPoint(point0.getWaveNumber(), ampl);
	  diff.add(pointDiff);
	}
      }
    }

    return result;
  }

  /**
   * Pads the spectrum either on left or right to have at least the specified
   * number of points.
   *
   * @param data		the spectrum to pad
   * @param numPoints		the number of points to have at least
   * @param padLeft		whether to pad on the left or right
   * @param waveStepSize	the increment for the wave numbers
   * @param amplitude		the amplitude to use for the padded data points
   * @return			the updated copy of the spectrum
   */
  public static Spectrum pad(Spectrum data, int numPoints, boolean padLeft, float waveStepSize, float amplitude) {
    Spectrum			result;
    List<SpectrumPoint>		points;
    SpectrumPoint		newPoint;
    float			wave;

    result = (Spectrum) data.getClone();

    while (result.size() < numPoints) {
      points = result.toList();
      if (points.isEmpty()) {
	wave = 0.0f;
      }
      else {
	if (padLeft)
	  wave = points.get(0).getWaveNumber();
	else
	  wave = points.get(points.size() - 1).getWaveNumber();
      }
      if (padLeft)
	newPoint = new SpectrumPoint(wave - waveStepSize, amplitude);
      else
	newPoint = new SpectrumPoint(wave + waveStepSize, amplitude);
      result.add(newPoint);
    }

    return result;
  }
}
