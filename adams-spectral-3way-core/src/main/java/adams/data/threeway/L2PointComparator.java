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
 * L2PointComparator.java
 * Copyright (C) 2017 University of Waikato, Hamilton, New Zealand
 */

package adams.data.threeway;

import adams.data.container.DataPointComparator;

/**
 * A comparator for level 2 points.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class L2PointComparator
  extends DataPointComparator<L2Point> {

  /** for serialization. */
  private static final long serialVersionUID = -2616612006241389909L;

  /** whether to compare data as well as z. */
  protected boolean m_UseData;

  /**
   * The default constructor uses comparison by x in ascending manner.
   */
  public L2PointComparator() {
    this(false, true);
  }

  /**
   * This constructor initializes the comparator either with comparison by
   * x or by y as well. Either in ascending manner or descending.
   *
   * @param useData		if true then y is used for comparison
   * 				as well as x
   * @param ascending		if true then the ordering is done in ascending
   * 				manner, otherwise descending
   */
  public L2PointComparator(boolean useData, boolean ascending) {
    super(ascending);

    m_UseData = useData;
  }

  /**
   * Returns whether the data is used for ordering as well.
   *
   * @return		true if data is used for ordering as well
   */
  public boolean isUsingData() {
    return m_UseData;
  }

  /**
   * Compares its two arguments for order. Returns a negative integer, zero,
   * or a positive integer as the first argument is less than, equal to, or
   * greater than the second.
   *
   * @param o1		the first object
   * @param o2		the second object
   * @return		a negative integer, zero, or a positive integer as
   * 			the first argument is less than, equal to, or greater
   * 			than the second.
   */
  public int compare(L2Point o1, L2Point o2) {
    int		result;

    result = Double.compare(o1.getZ(), o2.getZ());

    if ((result == 0) && m_UseData)
      result = Double.compare(o1.getData(), o2.getData());

    // flip ordering?
    if (!m_Ascending)
      result *= -1;

    return result;
  }
}
