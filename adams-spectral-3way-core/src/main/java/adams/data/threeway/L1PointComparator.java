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
 * L1PointComparator.java
 * Copyright (C) 2017-2018 University of Waikato, Hamilton, New Zealand
 */

package adams.data.threeway;

import adams.data.container.DataPointComparator;

/**
 * A comparator for level one points.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class L1PointComparator
  extends DataPointComparator<L1Point> {

  /** for serialization. */
  private static final long serialVersionUID = -5536677097973106152L;

  /** whether to compare Y as well. */
  protected boolean m_UseY;

  /**
   * The default constructor uses comparison by X in ascending manner.
   */
  public L1PointComparator() {
    this(false, true);
  }

  /**
   * This constructor initializes the comparator either with comparison by
   * X or Y as well. Either in ascending manner or descending.
   *
   * @param useY		if true then Y is used for comparison
   * 				as well as X
   * @param ascending		if true then the ordering is done in ascending
   * 				manner, otherwise descending
   */
  public L1PointComparator(boolean useY, boolean ascending) {
    super(ascending);

    m_UseY = useY;
  }

  /**
   * Returns whether the Y used for ordering as well.
   *
   * @return		true if Y is used as well
   */
  public boolean isUsingY() {
    return m_UseY;
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
  public int compare(L1Point o1, L1Point o2) {
    int		result;

    result = Double.compare(o1.getX(), o2.getX());

    if ((result == 0) && m_UseY)
      result = Double.compare(o1.getY(), o2.getY());

    // flip ordering?
    if (!m_Ascending)
      result *= -1;

    return result;
  }
}
