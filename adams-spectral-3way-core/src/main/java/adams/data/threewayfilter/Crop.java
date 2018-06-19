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
 * Crop.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.threewayfilter;

import adams.core.base.BaseInterval;
import adams.data.filter.AbstractFilter;
import adams.data.threeway.L1Point;
import adams.data.threeway.L2Point;
import adams.data.threeway.ThreeWayData;

/**
 <!-- globalinfo-start -->
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Crop
  extends AbstractFilter<ThreeWayData> {

  private static final long serialVersionUID = 5240367647182267129L;

  /** the range for X. */
  protected BaseInterval m_RangeX;

  /** the range for Y. */
  protected BaseInterval m_RangeY;

  /** the range for Z. */
  protected BaseInterval m_RangeZ;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Crops the data to the specified ranges for X, Y and Z.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "range-x", "rangeX",
      new BaseInterval(BaseInterval.ALL));

    m_OptionManager.add(
      "range-y", "rangeY",
      new BaseInterval(BaseInterval.ALL));

    m_OptionManager.add(
      "range-z", "rangeZ",
      new BaseInterval(BaseInterval.ALL));
  }

  /**
   * Sets the range for X.
   *
   * @param value 	the range
   */
  public void setRangeX(BaseInterval value) {
    m_RangeX = value;
    reset();
  }

  /**
   * Returns the range for X.
   *
   * @return 		the range
   */
  public BaseInterval getRangeX() {
    return m_RangeX;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rangeXTipText() {
    return "The range to use for X.";
  }

  /**
   * Sets the range for Y.
   *
   * @param value 	the range
   */
  public void setRangeY(BaseInterval value) {
    m_RangeY = value;
    reset();
  }

  /**
   * Returns the range for Y.
   *
   * @return 		the range
   */
  public BaseInterval getRangeY() {
    return m_RangeY;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rangeYTipText() {
    return "The range to use for Y.";
  }

  /**
   * Sets the range for Z.
   *
   * @param value 	the range
   */
  public void setRangeZ(BaseInterval value) {
    m_RangeZ = value;
    reset();
  }

  /**
   * Returns the range for Z.
   *
   * @return 		the range
   */
  public BaseInterval getRangeZ() {
    return m_RangeZ;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rangeZTipText() {
    return "The range to use for Z.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  @Override
  protected ThreeWayData processData(ThreeWayData data) {
    ThreeWayData  	result;
    L1Point		l1new;

    result = (ThreeWayData) data.getHeader();
    for (L1Point l1: data) {
      if (!m_RangeX.isInfinite() && !m_RangeX.isInside(l1.getX()))
        continue;
      if (!m_RangeY.isInfinite() && !m_RangeY.isInside(l1.getY()))
        continue;
      l1new = new L1Point(l1.getX(), l1.getY());
      result.add(l1new);

      for (L2Point l2: l1) {
	if (!m_RangeZ.isInfinite() && !m_RangeZ.isInside(l2.getZ()))
	  continue;
	l1new.add((L2Point) l2.getClone());
      }
    }

    return result;
  }
}
