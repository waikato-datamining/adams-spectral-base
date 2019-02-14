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
 * Copyright (C) 2014-2019 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrum;

/**
 * A comparator for Spectrum objects.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumComparator
  extends AbstractSpectrumComparator {

  /** for serialization. */
  private static final long serialVersionUID = -8837304326357509992L;

  @Override
  public String globalInfo() {
    return "Default comparator, comparison order:\n"
      + "- header (ID, format, report)\n"
      + "- data points";
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
