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
 * GCComparator.java
 * Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 */

package adams.data.chromatogram;

import adams.data.container.DataPointComparator;

/**
 * A comparator for GC points.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 3800 $
 */
public class GCComparator
  extends DataPointComparator<GCPoint> {

  /** for serialization. */
  private static final long serialVersionUID = -5536677097973106152L;

  /** whether to compare abundance or timestamp. */
  protected boolean m_UseAbundance;

  /**
   * The default constructor uses comparison by timestamp in ascending manner.
   */
  public GCComparator() {
    this(false, true);
  }

  /**
   * This constructor initializes the comparator either with comparison by
   * timestamp or by abundance. Either in ascending manner or descending.
   *
   * @param useAbundance	if true then abundance is used for comparison
   * 				otherwise the timestamp
   * @param ascending		if true then the ordering is done in ascending
   * 				manner, otherwise descending
   */
  public GCComparator(boolean useAbundance, boolean ascending) {
    super(ascending);

    m_UseAbundance = useAbundance;
  }

  /**
   * Returns whether the abundance or the timestamp is used for ordering.
   *
   * @return		true if abundance is used for ordering
   */
  public boolean isUsingAbundance() {
    return m_UseAbundance;
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
  public int compare(GCPoint o1, GCPoint o2) {
    int		result;

    if (m_UseAbundance)
      result = (((Long) o1.getAbundance()).compareTo((Long) o2.getAbundance()));
    else
      result = (((Long) o1.getTimestamp()).compareTo((Long) o2.getTimestamp()));

    // flip ordering?
    if (!m_Ascending)
      result *= -1;

    return result;
  }
}
