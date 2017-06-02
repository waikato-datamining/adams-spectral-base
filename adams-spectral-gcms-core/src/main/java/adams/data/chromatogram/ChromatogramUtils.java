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
 * ChromatogramUtils.java
 * Copyright (C) 2008-2013 University of Waikato, Hamilton, New Zealand
 */

package adams.data.chromatogram;

import adams.data.container.DataContainerUtils;
import adams.data.quantitation.QuantitationReport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for chromatograms.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4198 $
 */
public class ChromatogramUtils
  extends DataContainerUtils {

  /** comparator for finding timestamps. */
  protected static GCComparator m_Comparator;
  static {
    m_Comparator = new GCComparator(false, true);
  }

  /**
   * Returns the comparator used for finding timestamps.
   *
   * @return		the comparator
   */
  public static GCComparator getComparator() {
    return m_Comparator;
  }

  /**
   * Creates a header based on the given GC points.
   *
   * @param points	the GC points to create the header for
   * @return		the generated header
   */
  protected static Chromatogram getHeader(List<GCPoint> points) {
    Chromatogram	result;

    if ((points.size() > 0) && (points.get(0).getParent() != null))
      result = (Chromatogram) ((Chromatogram) points.get(0).getParent()).getHeader();
    else
      result = new Chromatogram("unknown");

    return result;
  }

  /**
   * Returns the index in points of the given GC point, -1 if not found.
   *
   * @param points	the vector of GC points to search in
   * @param p		the point to get the index for
   * @return		the index or -1 if not found
   */
  public static int findTimestamp(List<GCPoint> points, GCPoint p) {
    int		result;

    result = Collections.binarySearch(points, p, m_Comparator);
    if (result < 0)
      result = -1;

    return result;
  }

  /**
   * Returns the index in points of the given timestamp.
   *
   * @param points	the vector of GC points to search in
   * @param timestamp	the timestamp to get the index for
   * @return		the index
   */
  public static int findTimestamp(List<GCPoint> points, long timestamp) {
    return findTimestamp(points, new GCPoint(timestamp, 0));
  }

  /**
   * Returns the index in points closest to the given timestamp.
   *
   * @param points	the vector of GC points to search in
   * @param timestamp	the timestamp to get the closest index for
   * @return		the index
   */
  public static int findClosestTimestamp(List<GCPoint> points, long timestamp) {
    int		result;
    int		index;
    GCPoint	currPoint;
    long	currDist;
    long	dist;
    int		i;

    result = -1;

    if (points.size() == 0)
      return result;

    index = Collections.binarySearch(points, new GCPoint(timestamp, 0), m_Comparator);

    // no exact match -> find closest
    if (index < 0) {
      index = -index;
      if (index >= points.size())
	index = points.size() - 1;
      result = index;
      dist   = Math.abs(timestamp - points.get(index).getTimestamp());

      for (i = index - 2; i <= index + 2; i++) {
	if ((i >= 0) && (i < points.size())) {
	  currPoint = points.get(i);
	  currDist  = Math.abs(timestamp - currPoint.getTimestamp());

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
   * Returns the indices of points in m_Points that enclose the given timestamp.
   * If the given timestamp happens to be an exact point, then this points will
   * be stored at index 0. If no index could be determined, then -1 will be
   * stored.
   *
   * @param points	the vector of GC points to search in
   * @param timestamp	the timestamp to get the enclosing indices for
   * @return		the indices
   */
  public static int[] findEnclosingTimestamps(List<GCPoint> points, long timestamp) {
    int[]	result;
    int		index;

    result = new int[]{-1, -1};

    index = findClosestTimestamp(points, timestamp);
    if (index > -1) {
      // found exact timestamp (or left of timestamp) -> store at position 0
      if (points.get(index).getTimestamp() == timestamp) {
	result[0] = index;
      }
      else if (points.get(index).getTimestamp() < timestamp) {
	result[0] = index;
	if (index < points.size() - 1)
	  result[1] = index + 1;
      }
      // right of the timestamp
      else if (points.get(index).getTimestamp() > timestamp) {
	result[1] = index;
	if (index > 0)
	  result[0] = index - 1;
      }
    }

    return result;
  }

  /**
   * Locates a timestamp for the given time (in minutes).
   *
   * @param points	the GC points to search
   * @param type	the of GC selection
   * @param timeOrScan	the time in minutes to locate
   * @param less	if no exact match possible, whether to return the next
   * 			smaller (= true) or next larger (= false) timestamp
   * @return		the index of the GC point, -1 if not found
   */
  public static int findTimestamp(List<GCPoint> points, GCPointSelection type, double timeOrScan, boolean less) {
    int		result;
    long	stamp;

    switch (type) {
      case TIME:
	stamp = (long) (timeOrScan * 60000.0);
	break;

      case SCAN:
	stamp = (long) timeOrScan;
	break;

      default:
	throw new IllegalStateException("Unhandled selection type: " + type);
    }

    result = findClosestTimestamp(points, stamp);
    if (result != -1) {
      if (less) {
	if (points.get(result).getTimestamp() > stamp)
	  result--;
	if (result < 0)
	  result = 0;
      }
      else {
	if (points.get(result).getTimestamp() < stamp)
	  result++;
	if (result >= points.size())
	  result = points.size() - 1;
      }
    }

    return result;
  }

  /**
   * Generates a sub-window of the data.
   *
   * @param points	the data to create the sub-window from
   * @param type	the type of GC point selection
   * @param windowStart	the start of the window, use -1 for left-most point
   * @param windowEnd	the end of the window, use -1 for right-most point
   * @return		the generated window
   */
  public static List<GCPoint> createWindow(List<GCPoint> points, GCPointSelection type, double windowStart, double windowEnd) {
    List<GCPoint>	result;
    double		start;
    double		end;
    int			startGC;
    int			endGC;
    int			i;

    start = windowStart;
    end   = windowEnd;

    if (start == -1.0) {
      switch (type) {
	case TIME:
	  start = (double) points.get(0).getTimestamp() / 60000;
	  break;
	case SCAN:
	  start = points.get(0).getTimestamp();
	  break;
	default:
	  throw new IllegalStateException("Unhandled type: " + type);
      }
    }
    if (end == -1.0) {
      switch (type) {
	case TIME:
	  end = (double) points.get(points.size() - 1).getTimestamp() / 60000;
	  break;
	case SCAN:
	  end = points.get(points.size() - 1).getTimestamp();
	  break;
	default:
	  throw new IllegalStateException("Unhandled type: " + type);
      }
    }

    if (start >= end)
      throw new IllegalStateException(
	  "The start of the window must come before the end: "
	  + "start=" + start + ", end=" + end);

    // determine start/end point of window
    startGC = findTimestamp(points, type, start, true);
    endGC   = findTimestamp(points, type, end,   false);
    if (startGC == -1)
      throw new IllegalStateException("Failed to determine start of window!");
    if (endGC == -1)
      throw new IllegalStateException("Failed to determine end of window!");

    // create window
    result = new ArrayList<GCPoint>();
    for (i = startGC; i <= endGC; i++)
      result.add((GCPoint) points.get(i).getClone());

    return result;
  }

  /**
   * Returns a region for the given range, starting just after the
   * timestamp of "lastEnd" and ending (including) at "end".
   *
   * @param points	the GC points to work on
   * @param lastEnd	the last end point, if null then the first GC point
   * 			will be the first point included.
   * @param end		the last point to include in the region, if null then
   * 			the last point in the data is used.
   * @return		the generated region
   */
  public static Chromatogram getConsecutiveRegion(List<GCPoint> points, GCPoint lastEnd, GCPoint end) {
    Chromatogram	result;
    int			indexStart;
    int			indexEnd;
    int			i;

    result = getHeader(points);

    if (lastEnd == null)
      indexStart = 0;
    else
      indexStart = findTimestamp(points, lastEnd) + 1;

    if (end == null)
      indexEnd = points.size() - 1;
    else
      indexEnd = findTimestamp(points, end);

    for (i = indexStart; i <= indexEnd; i++)
      result.add((GCPoint) points.get(i).getClone());

    return result;
  }

  /**
   * Returns a region for the given range, including both, start and end point.
   *
   * @param points	the GC points to work on
   * @param start	the starting point, if null the first point in the
   * 			data is used.
   * @param end		the last point to include in the region, if null then
   * 			the last point in the data is used.
   * @return		the generated region
   */
  public static Chromatogram getRegion(List<GCPoint> points, GCPoint start, GCPoint end) {
    Chromatogram	result;
    int			indexStart;
    int			indexEnd;
    int			i;

    result = getHeader(points);

    if (start == null)
      indexStart = 0;
    else
      indexStart = findTimestamp(points, start);

    if (end == null)
      indexEnd = points.size() - 1;
    else
      indexEnd = findTimestamp(points, end);

    for (i = indexStart; i <= indexEnd; i++)
      result.add((GCPoint) points.get(i).getClone());

    return result;
  }

  /**
   * Counts the sign changes in the given data between the given points (incl.
   * the borders).
   *
   * @param points	the data to check for changes in sign
   * @param start	the timestamp to start with
   * @param end		the last timestamp
   * @return		the number of changes in sign
   */
  public static int countSignChanges(List<GCPoint> points, long start, long end) {
    int			result;
    int			startIndex;
    int			endIndex;
    int			i;
    long		abund;
    GCPoint		point;

    result     = 0;
    startIndex = findTimestamp(points, start);
    endIndex   = findTimestamp(points, end);
    abund      = points.get(startIndex).getAbundance();

    for (i = startIndex + 1; i <= endIndex; i++) {
      point = points.get(i);
      if (Math.signum(point.getAbundance()) != Math.signum(abund)) {
	result++;
	abund = point.getAbundance();
      }
    }

    return result;
  }

  /**
   * Counts the positive or negative regions between the given points (incl.
   * the borders).
   *
   * @param points	the data to check for regions
   * @param start	the timestamp to start with
   * @param end		the last timestamp
   * @param positive	if true then positive regions are counted otherwise
   * 			negative ones
   * @return		the number of positive/negative regions
   */
  public static int countRegions(List<GCPoint> points, long start, long end, boolean positive) {
    int			result;
    int			startIndex;
    int			endIndex;
    int			i;
    long		abund;
    GCPoint		point;

    result     = 0;
    startIndex = findTimestamp(points, start);
    endIndex   = findTimestamp(points, end);
    abund      = points.get(startIndex).getAbundance();
    if (positive && (abund >= 0))
      result++;
    else if (!positive && (abund < 0))
      result++;

    for (i = startIndex + 1; i <= endIndex; i++) {
      point = points.get(i);
      if (Math.signum(point.getAbundance()) != Math.signum(abund)) {
	abund = point.getAbundance();
	if (positive && (abund >= 0))
	  result++;
	else if (!positive && (abund < 0))
	  result++;
      }
    }

    return result;
  }

  /**
   * Returns the union of the two chromatograms "a" and "b". All GC points
   * from "a" are used, plus the ones from "b" that are not in "a".
   *
   * @param a		the first chromatogram
   * @param b		the second chromatogram
   * @return		the union
   */
  public static Chromatogram union(Chromatogram a, Chromatogram b) {
    Chromatogram	result;
    List<GCPoint>	points;
    int			i;

    result = (Chromatogram) a.getClone();

    points = b.toList();
    for (i = 0; i < points.size(); i++) {
      if (a.find(points.get(i).getTimestamp()) == null)
	result.add((GCPoint) points.get(i).getClone());
    }

    return result;
  }

  /**
   * Returns a chromatogram that contains all the points that are in "a"
   * but not in "b". It is assumed that "b" is a subset of "a"
   * and does not contain other timestamps.
   *
   * @param a		the "full" chromatogram
   * @param b		the "subset" chromatogram
   * @return		the points missing in "b"
   */
  public static Chromatogram minus(Chromatogram a, Chromatogram b) {
    Chromatogram	result;
    List<GCPoint>	points;
    int			i;

    result = (Chromatogram) a.getHeader();

    points = a.toList();
    for (i = 0; i < points.size(); i++) {
      if (b.find(points.get(i).getTimestamp()) == null)
	result.add((GCPoint) points.get(i).getClone());
    }

    return result;
  }

  /**
   * Returns a chromatogram that contains all the points that are in "a"
   * and in "b". The GC points in the result are taken from "a".
   *
   * @param a		the first chromatogram
   * @param b		the second chromatogram
   * @return		the points in "a" and "b"
   */
  public static Chromatogram intersect(Chromatogram a, Chromatogram b) {
    Chromatogram	result;
    List<GCPoint>	points;
    int			i;

    result = (Chromatogram) a.getHeader();

    points = a.toList();
    for (i = 0; i < points.size(); i++) {
      if (b.find(points.get(i).getTimestamp()) != null)
	result.add((GCPoint) points.get(i).getClone());
    }

    return result;
  }

  /**
   * Returns all the regions that are in "a", but not in "b". It is assumed
   * that "b" is a subset of "a" and does not contain other timestamps.
   *
   * @param a		the "full" chromatogram
   * @param b		the "subset" chromatogram
   * @return		the missing regions
   */
  public static List<Chromatogram> getMissingRegions(Chromatogram a, Chromatogram b) {
    List<Chromatogram>	result;
    Chromatogram		region;
    List<GCPoint>		points;
    int				i;

    result = new ArrayList<Chromatogram>();

    region = null;
    points = a.toList();
    for (i = 0; i < points.size(); i++) {
      if (b.find(points.get(i).getTimestamp()) == null) {
	if (region == null) {
	  region = (Chromatogram) a.getHeader();
	  result.add(region);
	}
	region.add((GCPoint) points.get(i).getClone());
      }
      else {
	region = null;
      }
    }

    return result;
  }

  /**
   * Fills the gaps in the "gaps" chromatogram with the data from
   * the "reference" chromatogram and the type of gap-filling.
   *
   * @param gaps	the chromatogram with gaps to fill
   * @param reference	the reference chromatogram to get the missing data from
   * @param type	the type of gap-filling to perform
   * @return		the processed chromatogram (a copy of it)
   */
  public static Chromatogram fillGaps(Chromatogram gaps, Chromatogram reference, GapFilling type) {
    Chromatogram	result;
    Chromatogram	tmpData;
    List<GCPoint>	points;
    GCPoint		point;
    int			i;
    int			first;
    int			second;
    double		delta;
    long		base;

    result = (Chromatogram) gaps.getClone();

    // do nothing
    if (type == GapFilling.NOTHING) {
      // ignored
    }

    // add points with zero abundance
    else if (type == GapFilling.ZERO) {
      tmpData = ChromatogramUtils.minus(reference, gaps);
      points  = tmpData.toList();
      for (i = 0; i < points.size(); i++) {
	point = (GCPoint) points.get(i).getClone();
	point.setAbundance(0);
	result.add(point);
      }
    }

    // add points with the original abundance
    else if (type == GapFilling.ORIGINAL) {
      tmpData = ChromatogramUtils.minus(reference, gaps);
      points  = tmpData.toList();
      for (i = 0; i < points.size(); i++) {
	point = (GCPoint) points.get(i).getClone();
	result.add(point);
      }
    }

    // connect regions with points with slowly increasing/decreasing abundances
    else if (type == GapFilling.CONNECT) {
      points = reference.toList();
      i      = 0;
      while (i < points.size()) {
	// end of a non-noise region?
	if (gaps.find(points.get(i).getTimestamp()) == null) {
	  first = i - 1;

	  // find start of next region
	  while ((i < points.size()) && (gaps.find(points.get(i).getTimestamp()) == null)) {
	    i++;
	  }

	  // found another region or end of points?
	  if (i < points.size()) {
	    second = i;
	    delta  = (double) points.get(second).getAbundance();
	  }
	  else {
	    second = points.size();
	    delta  = 0;
	  }
	  base = 0;
	  if (first > -1) {
	    delta -= (double) points.get(first).getAbundance();
	    base   = points.get(first).getAbundance();
	  }
	  delta /= (double) (second - first + 1);

	  // fill in points
	  for (i = first + 1; i < second; i++) {
	    point = (GCPoint) points.get(i).getClone();
	    point.setAbundance((long) ((i - first)*delta) + base);
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
   * Merges the given chromatogram with the current data pool.
   *
   * @param pool	the current data pool
   * @param c		the chromatogram to merge with the pool
   */
  protected static void add(Hashtable<Long,GCPoint> pool, Chromatogram c) {
    Iterator<GCPoint>	iterGC;
    GCPoint		pointGC;
    GCPoint		poolGC;

    iterGC = c.toList().iterator();
    while (iterGC.hasNext()) {
      pointGC = iterGC.next();
      poolGC  = pool.get(pointGC.getTimestamp());
      if (poolGC == null)
	poolGC = new GCPoint(pointGC.getTimestamp(), 0);
      poolGC = GCPointUtils.merge(poolGC, pointGC);
      pool.put(poolGC.getTimestamp(), poolGC);
    }
  }

  /**
   * Merges the two chromatograms. The header of the first one is used for the
   * output.
   *
   * @param c1		the first chromatogram
   * @param c2		the second chromatogram
   * @return		the merged chromatogram
   */
  public static Chromatogram merge(Chromatogram c1, Chromatogram c2) {
    List<Chromatogram>	list;

    list = new ArrayList<Chromatogram>();
    list.add(c1);
    list.add(c2);

    return merge(list);
  }

  /**
   * Merges the given chromatograms. THe header of the first one is used for
   * the output.
   *
   * @param list	the chromatograms to merge
   * @return		the merged chromatogram
   */
  public static Chromatogram merge(List<Chromatogram> list) {
    Chromatogram		result;
    int				i;
    Hashtable<Long,GCPoint>	pool;
    Enumeration<GCPoint>	elements;

    if (list.size() == 0)
      return null;
    else if (list.size() == 1)
      return list.get(0);

    result = (Chromatogram) list.get(0).getHeader();
    pool   = new Hashtable<Long,GCPoint>();
    for (i = 0; i < list.size(); i++) {
      add(pool, list.get(i));
      // merge quantitation reports
      if (list.get(i).hasReport()) {
	if (!result.hasReport())
	  result.setReport((QuantitationReport) list.get(i).getReport().getClone());
	else
	  result.getReport().mergeWith(list.get(i).getReport());
      }
      // merge notes
      result.getNotes().mergeWith(list.get(i).getNotes());
    }

    // create output data
    elements = pool.elements();
    while (elements.hasMoreElements())
      result.add(elements.nextElement());

    return result;
  }

  /**
   * Returns the abundance as double array.
   *
   * @param c		the chromatogram to turn into a double array
   * @return		the abundances as double array
   */
  public static double[] toDoubleArray(Chromatogram c) {
    return toDoubleArray(c.toList());
  }

  /**
   * Returns the abundance as double array.
   *
   * @param data	the GC points to turn into a double array
   * @return		the abundances as double array
   */
  public static double[] toDoubleArray(List<GCPoint> data) {
    double[] 	result;
    int 	i;

    result = new double[data.size()];
    i      = 0;
    for (GCPoint gcp:data)
      result[i++] = new Double(gcp.getAbundance());

    return result;
  }
}
