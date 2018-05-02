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
 * DownSample.java
 * Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */

package adams.data.threewayfilter;

import adams.data.container.DataPoint;
import adams.data.threeway.L1Point;
import adams.data.threeway.L2Point;
import adams.data.threeway.ThreeWayData;

import java.util.List;

/**
 <!-- globalinfo-start -->
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class DownSample
  extends adams.data.filter.DownSample<ThreeWayData> {

  /** for serialization. */
  private static final long serialVersionUID = -7633117391523711914L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "A filter that returns only every n-th 1st level and 2nd level point.";
  }

  /**
   * Creates a copy of the data point.
   *
   * @param index	the index in the data point list to copy
   * @param points	the list of data points
   * @return		the copy of the specified data point
   */
  @Override
  protected DataPoint copy(int index, List<DataPoint> points) {
    L1Point result;
    L1Point l1;
    int			i;

    l1     = (L1Point) points.get(index);
    result = new L1Point(l1.getX(), l1.getY());
    for (i = 0; i < l1.size(); i++) {
      if ((i+1) % m_NthPoint == 0)
	result.add((L2Point) l1.toList().get(i).getClone());
    }

    return result;
  }
}
