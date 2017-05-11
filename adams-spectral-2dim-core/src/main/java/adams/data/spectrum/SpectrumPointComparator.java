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
 * SpectrumPointComparator.java
 * Copyright (C) 2008-2015 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrum;

import adams.data.container.DataPointComparator;

/**
 * A comparator for SpectrumPoint points.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 11831 $
 */
public class SpectrumPointComparator
  extends DataPointComparator<SpectrumPoint> {

  /** for serialization. */
  private static final long serialVersionUID = -5536677097973106152L;

  /** whether to compare amplitude or wave number. */
  protected boolean m_UseAmplitude;

  /**
   * The default constructor uses comparison by wave number in ascending manner.
   */
  public SpectrumPointComparator() {
    this(false, true);
  }

  /**
   * This constructor initializes the comparator either with comparison by
   * wave number or by amplitude. Either in ascending manner or descending.
   *
   * @param useAmplitude	if true then amplitude is used for comparison
   * 				otherwise the wave number
   * @param ascending		if true then the ordering is done in ascending
   * 				manner, otherwise descending
   */
  public SpectrumPointComparator(boolean useAmplitude, boolean ascending) {
    super(ascending);

    m_UseAmplitude = useAmplitude;
  }

  /**
   * Returns whether the amplitude or the wave number is used for ordering.
   *
   * @return		true if amplitude is used for ordering
   */
  public boolean isUsingAmplitude() {
    return m_UseAmplitude;
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
  public int compare(SpectrumPoint o1, SpectrumPoint o2) {
    int		result;

    if (m_UseAmplitude)
      result = (((Float) o1.getAmplitude()).compareTo((Float) o2.getAmplitude()));
    else
      result = (((Float) o1.getWaveNumber()).compareTo((Float) o2.getWaveNumber()));

    // flip ordering?
    if (!m_Ascending)
      result *= -1;

    return result;
  }
}
