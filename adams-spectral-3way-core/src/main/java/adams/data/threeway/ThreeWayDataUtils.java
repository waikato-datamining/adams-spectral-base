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
 * ThreeWayDataUtils.java
 * Copyright (C) 2017 University of Waikato, Hamilton, New Zealand
 */

package adams.data.threeway;

import adams.data.container.DataContainerUtils;
import adams.data.threewayreport.ThreeWayReport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for ThreeWayData objects.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4198 $
 */
public class ThreeWayDataUtils
  extends DataContainerUtils {

  /** comparator for finding timestamps. */
  protected static LevelOnePointComparator m_Comparator;
  static {
    m_Comparator = new LevelOnePointComparator(false, true);
  }

  /**
   * Returns the comparator used for finding timestamps.
   *
   * @return		the comparator
   */
  public static LevelOnePointComparator getComparator() {
    return m_Comparator;
  }

  /**
   * Creates a header based on the given LevelOnePoint points.
   *
   * @param points	the LevelOnePoint points to create the header for
   * @return		the generated header
   */
  protected static ThreeWayData getHeader(List<LevelOnePoint> points) {
    ThreeWayData	result;

    if ((points.size() > 0) && (points.get(0).getParent() != null))
      result = (ThreeWayData) (points.get(0).getParent()).getHeader();
    else
      result = new ThreeWayData("unknown");

    return result;
  }

  /**
   * Returns the index in points of the given LevelOnePoint point, -1 if not found.
   *
   * @param points	the vector of LevelOnePoint points to search in
   * @param p		the point to get the index for
   * @return		the index or -1 if not found
   */
  public static int findX(List<LevelOnePoint> points, LevelOnePoint p) {
    int		result;

    result = Collections.binarySearch(points, p, m_Comparator);
    if (result < 0)
      result = -1;

    return result;
  }

  /**
   * Returns the index in points of the given timestamp.
   *
   * @param points	the vector of LevelOnePoint points to search in
   * @param x		the X to get the index for
   * @return		the index
   */
  public static int findX(List<LevelOnePoint> points, double x) {
    return findX(points, new LevelOnePoint(x, 0));
  }

  /**
   * Returns the index in points closest to the given timestamp.
   *
   * @param points	the list of LevelOnePoint points to search in
   * @param x		the X to get the closest index for
   * @return		the index
   */
  public static int findClosestX(List<LevelOnePoint> points, double x) {
    int			result;
    int			index;
    LevelOnePoint	currPoint;
    double		currDist;
    double		dist;
    int			i;

    result = -1;

    if (points.size() == 0)
      return result;

    index = Collections.binarySearch(points, new LevelOnePoint(x, 0), m_Comparator);

    // no exact match -> find closest
    if (index < 0) {
      index = -index;
      if (index >= points.size())
	index = points.size() - 1;
      result = index;
      dist   = Math.abs(x - points.get(index).getX());

      for (i = index - 2; i <= index + 2; i++) {
	if ((i >= 0) && (i < points.size())) {
	  currPoint = points.get(i);
	  currDist  = Math.abs(x - currPoint.getX());

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
   * Returns the indices of points in points that enclose the given X.
   * If the given X happens to be an exact point, then this point will
   * be stored at index 0. If no index could be determined, then -1 will be
   * stored.
   *
   * @param points	the list of LevelOnePoint points to search in
   * @param x		the X to get the enclosing indices for
   * @return		the indices
   */
  public static int[] findEnclosingXs(List<LevelOnePoint> points, double x) {
    int[]	result;
    int		index;

    result = new int[]{-1, -1};

    index = findClosestX(points, x);
    if (index > -1) {
      // found exact X (or left of X) -> store at position 0
      if (points.get(index).getX() == x) {
	result[0] = index;
      }
      else if (points.get(index).getX() < x) {
	result[0] = index;
	if (index < points.size() - 1)
	  result[1] = index + 1;
      }
      // right of the X
      else if (points.get(index).getX() > x) {
	result[1] = index;
	if (index > 0)
	  result[0] = index - 1;
      }
    }

    return result;
  }

  /**
   * Locates the X for the given X.
   *
   * @param points	the LevelOnePoint points to search
   * @param x		the X to locate
   * @param less	if no exact match possible, whether to return the next
   * 			smaller (= true) or next larger (= false) timestamp
   * @return		the index of the LevelOnePoint point, -1 if not found
   */
  public static int findX(List<LevelOnePoint> points, double x, boolean less) {
    int		result;

    result = findClosestX(points, x);
    if (result != -1) {
      if (less) {
	if (points.get(result).getX() > x)
	  result--;
	if (result < 0)
	  result = 0;
      }
      else {
	if (points.get(result).getX() < x)
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
   * @param windowStart	the start of the window, use -1 for left-most point
   * @param windowEnd	the end of the window, use -1 for right-most point
   * @return		the generated window
   */
  public static List<LevelOnePoint> createWindow(List<LevelOnePoint> points, double windowStart, double windowEnd) {
    List<LevelOnePoint>	result;
    double		start;
    double		end;
    int			startLevelOnePoint;
    int			endLevelOnePoint;
    int			i;

    start = windowStart;
    end   = windowEnd;

    if (start == -1.0)
      start = points.get(0).getX();
    if (end == -1.0)
      end = points.get(points.size() - 1).getX();

    if (start >= end)
      throw new IllegalStateException(
	  "The start of the window must come before the end: "
	  + "start=" + start + ", end=" + end);

    // determine start/end point of window
    startLevelOnePoint = findX(points, start, true);
    endLevelOnePoint   = findX(points, end, false);
    if (startLevelOnePoint == -1)
      throw new IllegalStateException("Failed to determine start of window!");
    if (endLevelOnePoint == -1)
      throw new IllegalStateException("Failed to determine end of window!");

    // create window
    result = new ArrayList<>();
    for (i = startLevelOnePoint; i <= endLevelOnePoint; i++)
      result.add((LevelOnePoint) points.get(i).getClone());

    return result;
  }

  /**
   * Returns a region for the given range, starting just after the
   * x of "lastEnd" and ending (including) at "end".
   *
   * @param points	the LevelOnePoint points to work on
   * @param lastEnd	the last end point, if null then the first LevelOnePoint point
   * 			will be the first point included.
   * @param end		the last point to include in the region, if null then
   * 			the last point in the data is used.
   * @return		the generated region
   */
  public static ThreeWayData getConsecutiveRegion(List<LevelOnePoint> points, LevelOnePoint lastEnd, LevelOnePoint end) {
    ThreeWayData	result;
    int			indexStart;
    int			indexEnd;
    int			i;

    result = getHeader(points);

    if (lastEnd == null)
      indexStart = 0;
    else
      indexStart = findX(points, lastEnd) + 1;

    if (end == null)
      indexEnd = points.size() - 1;
    else
      indexEnd = findX(points, end);

    for (i = indexStart; i <= indexEnd; i++)
      result.add((LevelOnePoint) points.get(i).getClone());

    return result;
  }

  /**
   * Returns a region for the given range, including both, start and end point.
   *
   * @param points	the LevelOnePoint points to work on
   * @param start	the starting point, if null the first point in the
   * 			data is used.
   * @param end		the last point to include in the region, if null then
   * 			the last point in the data is used.
   * @return		the generated region
   */
  public static ThreeWayData getRegion(List<LevelOnePoint> points, LevelOnePoint start, LevelOnePoint end) {
    ThreeWayData	result;
    int			indexStart;
    int			indexEnd;
    int			i;

    result = getHeader(points);

    if (start == null)
      indexStart = 0;
    else
      indexStart = findX(points, start);

    if (end == null)
      indexEnd = points.size() - 1;
    else
      indexEnd = findX(points, end);

    for (i = indexStart; i <= indexEnd; i++)
      result.add((LevelOnePoint) points.get(i).getClone());

    return result;
  }

  /**
   * Counts the sign changes in the given data between the given points (incl.
   * the borders).
   *
   * @param points	the data to check for changes in sign
   * @param start	the X to start with
   * @param end		the last X
   * @return		the number of changes in sign
   */
  public static int countSignChanges(List<LevelOnePoint> points, double start, double end) {
    int			result;
    int			startIndex;
    int			endIndex;
    int			i;
    double		abund;
    LevelOnePoint	point;

    result     = 0;
    startIndex = findX(points, start);
    endIndex   = findX(points, end);
    abund      = points.get(startIndex).getY();

    for (i = startIndex + 1; i <= endIndex; i++) {
      point = points.get(i);
      if (Math.signum(point.getY()) != Math.signum(abund)) {
	result++;
	abund = point.getY();
      }
    }

    return result;
  }

  /**
   * Counts the positive or negative regions between the given points (incl.
   * the borders).
   *
   * @param points	the data to check for regions
   * @param start	the X to start with
   * @param end		the last X
   * @param positive	if true then positive regions are counted otherwise
   * 			negative ones
   * @return		the number of positive/negative regions
   */
  public static int countRegions(List<LevelOnePoint> points, double start, double end, boolean positive) {
    int			result;
    int			startIndex;
    int			endIndex;
    int			i;
    double		abund;
    LevelOnePoint	point;

    result     = 0;
    startIndex = findX(points, start);
    endIndex   = findX(points, end);
    abund      = points.get(startIndex).getY();
    if (positive && (abund >= 0))
      result++;
    else if (!positive && (abund < 0))
      result++;

    for (i = startIndex + 1; i <= endIndex; i++) {
      point = points.get(i);
      if (Math.signum(point.getY()) != Math.signum(abund)) {
	abund = point.getY();
	if (positive && (abund >= 0))
	  result++;
	else if (!positive && (abund < 0))
	  result++;
      }
    }

    return result;
  }

  /**
   * Returns the union of the two objects "a" and "b". All LevelOnePoint points
   * from "a" are used, plus the ones from "b" that are not in "a".
   *
   * @param a		the first object
   * @param b		the second object
   * @return		the union
   */
  public static ThreeWayData union(ThreeWayData a, ThreeWayData b) {
    ThreeWayData	result;
    List<LevelOnePoint>	points;
    int			i;

    result = (ThreeWayData) a.getClone();

    points = b.toList();
    for (i = 0; i < points.size(); i++) {
      if (findX(a.toList(), points.get(i).getX()) == -1)
	result.add((LevelOnePoint) points.get(i).getClone());
    }

    return result;
  }

  /**
   * Returns an object that contains all the points that are in "a"
   * but not in "b". It is assumed that "b" is a subset of "a"
   * and does not contain other Xs.
   *
   * @param a		the "full" object
   * @param b		the "subset" object
   * @return		the points missing in "b"
   */
  public static ThreeWayData minus(ThreeWayData a, ThreeWayData b) {
    ThreeWayData	result;
    List<LevelOnePoint>	points;
    int			i;

    result = (ThreeWayData) a.getHeader();

    points = a.toList();
    for (i = 0; i < points.size(); i++) {
      if (findX(b.toList(), points.get(i).getX()) == -1)
	result.add((LevelOnePoint) points.get(i).getClone());
    }

    return result;
  }

  /**
   * Returns an object that contains all the points that are in "a"
   * and in "b". The LevelOnePoint points in the result are taken from "a".
   *
   * @param a		the first object
   * @param b		the second object
   * @return		the points in "a" and "b"
   */
  public static ThreeWayData intersect(ThreeWayData a, ThreeWayData b) {
    ThreeWayData	result;
    List<LevelOnePoint>	points;
    int			i;

    result = (ThreeWayData) a.getHeader();

    points = a.toList();
    for (i = 0; i < points.size(); i++) {
      if (findX(b.toList(), points.get(i).getX()) != -1)
	result.add((LevelOnePoint) points.get(i).getClone());
    }

    return result;
  }

  /**
   * Returns all the regions that are in "a", but not in "b". It is assumed
   * that "b" is a subset of "a" and does not contain other timestamps.
   *
   * @param a		the "full" object
   * @param b		the "subset" object
   * @return		the missing regions
   */
  public static List<ThreeWayData> getMissingRegions(ThreeWayData a, ThreeWayData b) {
    List<ThreeWayData>	result;
    ThreeWayData		region;
    List<LevelOnePoint>		points;
    int				i;

    result = new ArrayList<>();

    region = null;
    points = a.toList();
    for (i = 0; i < points.size(); i++) {
      if (findX(b.toList(), points.get(i).getX()) == -1) {
	if (region == null) {
	  region = (ThreeWayData) a.getHeader();
	  result.add(region);
	}
	region.add((LevelOnePoint) points.get(i).getClone());
      }
      else {
	region = null;
      }
    }

    return result;
  }

  /**
   * Fills the gaps in the "gaps" object with the data from
   * the "reference" object and the type of gap-filling.
   *
   * @param gaps	the object with gaps to fill
   * @param reference	the reference object to get the missing data from
   * @param type	the type of gap-filling to perform
   * @return		the processed object (a copy of it)
   */
  public static ThreeWayData fillGaps(ThreeWayData gaps, ThreeWayData reference, GapFilling type) {
    ThreeWayData	result;
    ThreeWayData	tmpData;
    List<LevelOnePoint>	points;
    LevelOnePoint	point;
    int			i;
    int			first;
    int			second;
    double		delta;
    double		base;

    result = (ThreeWayData) gaps.getClone();

    // do nothing
    if (type == GapFilling.NOTHING) {
      // ignored
    }

    // add points with zero abundance
    else if (type == GapFilling.ZERO) {
      tmpData = ThreeWayDataUtils.minus(reference, gaps);
      points  = tmpData.toList();
      for (i = 0; i < points.size(); i++) {
	point = (LevelOnePoint) points.get(i).getClone();
	point.setY(0);
	result.add(point);
      }
    }

    // add points with the original abundance
    else if (type == GapFilling.ORIGINAL) {
      tmpData = ThreeWayDataUtils.minus(reference, gaps);
      points  = tmpData.toList();
      for (i = 0; i < points.size(); i++) {
	point = (LevelOnePoint) points.get(i).getClone();
	result.add(point);
      }
    }

    // connect regions with points with slowly increasing/decreasing abundances
    else if (type == GapFilling.CONNECT) {
      points = reference.toList();
      i      = 0;
      while (i < points.size()) {
	// end of a non-noise region?
	if (gaps.find(points.get(i).getX()) == null) {
	  first = i - 1;

	  // find start of next region
	  while ((i < points.size()) && (gaps.find(points.get(i).getX()) == null)) {
	    i++;
	  }

	  // found another region or end of points?
	  if (i < points.size()) {
	    second = i;
	    delta  = points.get(second).getY();
	  }
	  else {
	    second = points.size();
	    delta  = 0;
	  }
	  base = 0;
	  if (first > -1) {
	    delta -= points.get(first).getY();
	    base   = points.get(first).getY();
	  }
	  delta /= (double) (second - first + 1);

	  // fill in points
	  for (i = first + 1; i < second; i++) {
	    point = (LevelOnePoint) points.get(i).getClone();
	    point.setY(((i - first) * delta) + base);
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
   * Merges the given object with the current data pool.
   *
   * @param pool	the current data pool
   * @param c		the objet to merge with the pool
   */
  protected static void add(HashMap<Double,LevelOnePoint> pool, ThreeWayData c) {
    Iterator<LevelOnePoint> 	iterLevelOnePoint;
    LevelOnePoint		pointLevelOnePoint;
    LevelOnePoint		poolLevelOnePoint;

    iterLevelOnePoint = c.toList().iterator();
    while (iterLevelOnePoint.hasNext()) {
      pointLevelOnePoint = iterLevelOnePoint.next();
      poolLevelOnePoint  = pool.get(pointLevelOnePoint.getX());
      if (poolLevelOnePoint == null)
	poolLevelOnePoint = new LevelOnePoint(pointLevelOnePoint.getX(), 0);
      poolLevelOnePoint = LevelOnePointUtils.merge(poolLevelOnePoint, pointLevelOnePoint);
      pool.put(poolLevelOnePoint.getX(), poolLevelOnePoint);
    }
  }

  /**
   * Merges the two objects. The header of the first one is used for the
   * output.
   *
   * @param c1		the first object
   * @param c2		the second object
   * @return		the merged object
   */
  public static ThreeWayData merge(ThreeWayData c1, ThreeWayData c2) {
    List<ThreeWayData>	list;

    list = new ArrayList<>();
    list.add(c1);
    list.add(c2);

    return merge(list);
  }

  /**
   * Merges the given objects. THe header of the first one is used for
   * the output.
   *
   * @param list	the objects to merge
   * @return		the merged object
   */
  public static ThreeWayData merge(List<ThreeWayData> list) {
    ThreeWayData			result;
    int					i;
    HashMap<Double,LevelOnePoint>	pool;

    if (list.size() == 0)
      return null;
    else if (list.size() == 1)
      return list.get(0);

    result = (ThreeWayData) list.get(0).getHeader();
    pool   = new HashMap<>();
    for (i = 0; i < list.size(); i++) {
      add(pool, list.get(i));
      // merge quantitation reports
      if (list.get(i).hasReport()) {
	if (!result.hasReport())
	  result.setReport((ThreeWayReport) list.get(i).getReport().getClone());
	else
	  result.getReport().mergeWith(list.get(i).getReport());
      }
      // merge notes
      result.getNotes().mergeWith(list.get(i).getNotes());
    }

    // create output data
    for (LevelOnePoint l1: pool.values())
      result.add(l1);

    return result;
  }

  /**
   * Returns the abundance as double array.
   *
   * @param c		the object to turn into a double array
   * @return		the abundances as double array
   */
  public static double[] toDoubleArray(ThreeWayData c) {
    return toDoubleArray(c.toList());
  }

  /**
   * Returns the abundance as double array.
   *
   * @param data	the LevelOnePoint points to turn into a double array
   * @return		the abundances as double array
   */
  public static double[] toDoubleArray(List<LevelOnePoint> data) {
    double[] 	result;
    int 	i;

    result = new double[data.size()];
    i      = 0;
    for (LevelOnePoint l1 :data)
      result[i++] = l1.getY();

    return result;
  }
}
