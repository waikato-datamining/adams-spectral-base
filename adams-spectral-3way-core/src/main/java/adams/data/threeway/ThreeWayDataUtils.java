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
 * Copyright (C) 2017-2018 University of Waikato, Hamilton, New Zealand
 */

package adams.data.threeway;

import adams.data.container.DataContainerUtils;

import java.util.Collections;
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
  protected static L1PointComparator m_Comparator;
  static {
    m_Comparator = new L1PointComparator(false, true);
  }

  /**
   * Returns the comparator used for finding timestamps.
   *
   * @return		the comparator
   */
  public static L1PointComparator getComparator() {
    return m_Comparator;
  }

  /**
   * Creates a header based on the given LevelOnePoint points.
   *
   * @param points	the LevelOnePoint points to create the header for
   * @return		the generated header
   */
  protected static ThreeWayData getHeader(List<L1Point> points) {
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
  public static int findXY(List<L1Point> points, L1Point p) {
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
  public static int findXY(List<L1Point> points, double x, double y) {
    return findXY(points, new L1Point(x, y));
  }

  /**
   * Returns the index in points closest to the given timestamp.
   *
   * @param points	the list of LevelOnePoint points to search in
   * @param x		the X to get the closest index for
   * @param y		the Y to get the closest index for
   * @return		the index
   */
  public static int findClosestXY(List<L1Point> points, double x, double y) {
    int			result;
    int			index;
    L1Point 		currPoint;
    double		currDist;
    double		dist;
    int			i;

    result = -1;

    if (points.size() == 0)
      return result;

    index = Collections.binarySearch(points, new L1Point(x, y), m_Comparator);

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
   * Locates the X for the given X.
   *
   * @param points	the LevelOnePoint points to search
   * @param x		the X to locate
   * @param less	if no exact match possible, whether to return the next
   * 			smaller (= true) or next larger (= false) timestamp
   * @return		the index of the LevelOnePoint point, -1 if not found
   */
  public static int findXY(List<L1Point> points, double x, double y, boolean less) {
    int		result;

    result = findClosestXY(points, x, y);
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
}
