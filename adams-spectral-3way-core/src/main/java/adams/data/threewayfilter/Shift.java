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
 * Shift.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.threewayfilter;

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
public class Shift
  extends AbstractFilter<ThreeWayData> {

  private static final long serialVersionUID = 5240367647182267129L;

  /** the offset for X. */
  protected int m_OffsetX;

  /** the offset for Y. */
  protected int m_OffsetY;

  /** the offset for Z. */
  protected int m_OffsetZ;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Apples the specified offsets to X, Y and Z.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "offset-x", "offsetX",
      0);

    m_OptionManager.add(
      "offset-y", "offsetY",
      0);

    m_OptionManager.add(
      "offset-z", "offsetZ",
      0);
  }

  /**
   * Sets the offset for X.
   *
   * @param value 	the offset
   */
  public void setOffsetX(int value) {
    m_OffsetX = value;
    reset();
  }

  /**
   * Returns the offset for X.
   *
   * @return 		the offset
   */
  public int getOffsetX() {
    return m_OffsetX;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String offsetXTipText() {
    return "The offset to use for X.";
  }

  /**
   * Sets the offset for Y.
   *
   * @param value 	the offset
   */
  public void setOffsetY(int value) {
    m_OffsetY = value;
    reset();
  }

  /**
   * Returns the offset for Y.
   *
   * @return 		the offset
   */
  public int getOffsetY() {
    return m_OffsetY;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String offsetYTipText() {
    return "The offset to use for Y.";
  }

  /**
   * Sets the offset for Z.
   *
   * @param value 	the offset
   */
  public void setOffsetZ(int value) {
    m_OffsetZ = value;
    reset();
  }

  /**
   * Returns the offset for Z.
   *
   * @return 		the offset
   */
  public int getOffsetZ() {
    return m_OffsetZ;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String offsetZTipText() {
    return "The offset to use for Z.";
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
    L2Point		l2new;

    result = (ThreeWayData) data.getHeader();
    for (L1Point l1: data) {
      l1new = new L1Point(l1.getX() + m_OffsetX, l1.getY() + m_OffsetY);
      result.add(l1new);

      for (L2Point l2: l1) {
        l2new = new L2Point(l2.getZ() + m_OffsetZ, l2.getData());
	l1new.add(l2new);
      }
    }

    return result;
  }
}
