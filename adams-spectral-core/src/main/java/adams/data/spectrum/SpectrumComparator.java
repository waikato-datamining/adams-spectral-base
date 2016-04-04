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
 * SpectrumComparator.java
 * Copyright (C) 2014-2015 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrum;

import adams.data.container.DataPointComparator;

/**
 * A comparator for Spectrum points.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 11831 $
 */
public class SpectrumComparator
  extends DataPointComparator<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -8837304326357509992L;

  /**
   * The default constructor uses comparison in ascending manner.
   */
  public SpectrumComparator() {
    this(true);
  }

  /**
   * This constructor initializes the comparator either in ascending 
   * manner or descending.
   *
   * @param ascending		if true then the ordering is done in ascending
   * 				manner, otherwise descending
   */
  public SpectrumComparator(boolean ascending) {
    super(ascending);
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
  @Override
  public int compare(Spectrum o1, Spectrum o2) {
    int		result;

    // header
    result = o1.compareToHeader(o2);
    
    // data points
    if (result == 0)
      result = o1.compareTo(o2);

    // flip ordering?
    if (!m_Ascending)
      result *= -1;

    return result;
  }
}
