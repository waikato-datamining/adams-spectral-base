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
 * ThreeWayDataToHeatmap.java
 * Copyright (C) 2017-2018 University of Waikato, Hamilton, NZ
 */

package adams.data.conversion;

import adams.data.heatmap.Heatmap;
import adams.data.threeway.L1Point;
import adams.data.threeway.L2Point;
import adams.data.threeway.ThreeWayData;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.set.TDoubleSet;
import gnu.trove.set.hash.TDoubleHashSet;

/**
 <!-- globalinfo-start -->
 * Turns a adams.data.threeway.ThreeWayData data structure into a heatmap.<br>
 * Sums up the data values of the Z layers that fall into the specified min&#47;max.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-min-z &lt;double&gt; (property: minZ)
 * &nbsp;&nbsp;&nbsp;The minimum Z layer to include.
 * &nbsp;&nbsp;&nbsp;default: 0.0
 * </pre>
 *
 * <pre>-max-z &lt;double&gt; (property: maxZ)
 * &nbsp;&nbsp;&nbsp;The maximum Z layer to include.
 * &nbsp;&nbsp;&nbsp;default: 0.0
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ThreeWayDataToHeatmap
  extends AbstractConversion {

  private static final long serialVersionUID = -8371135112409803967L;

  /** the minimum x to use. */
  protected double m_MinX;

  /** the maximum x to use. */
  protected double m_MaxX;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Turns a " + ThreeWayData.class.getName() + " data structure into a heatmap.\n"
      + "Sums up the data values of the X layers that fall into the specified min/max.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "min-x", "minX",
      0.0);

    m_OptionManager.add(
      "max-X", "maxX",
      0.0);
  }

  /**
   * Sets the minimum X layer to use.
   *
   * @param value 	the minimum X
   */
  public void setMinX(double value) {
    m_MinX = value;
    reset();
  }

  /**
   * Returns the minimum X layer to use.
   *
   * @return 		the minimum X
   */
  public double getMinX() {
    return m_MinX;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String minXTipText() {
    return "The minimum X layer to include.";
  }

  /**
   * Sets the maximum X layer to use.
   *
   * @param value 	the maximum X
   */
  public void setMaxX(double value) {
    m_MaxX = value;
    reset();
  }

  /**
   * Returns the maximum X layer to use.
   *
   * @return 		the maximum X
   */
  public double getMaxX() {
    return m_MaxX;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String maxXTipText() {
    return "The maximum X layer to include.";
  }

  /**
   * Returns the class that is accepted as input.
   *
   * @return		the class
   */
  @Override
  public Class accepts() {
    return ThreeWayData.class;
  }

  /**
   * Returns the class that is generated as output.
   *
   * @return		the class
   */
  @Override
  public Class generates() {
    return Heatmap.class;
  }

  /**
   * Checks whether the data can be processed.
   *
   * @return		null if checks passed, otherwise error message
   */
  @Override
  protected String checkData() {
    String	result;

    result = super.checkData();

    if (result == null) {
      if (m_MaxX < m_MinX)
        result = "MaxX must be smaller than MinX: MinX=" + m_MinX + ", MaxX=" + m_MaxX;
    }

    return result;
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  @Override
  protected Object doConvert() throws Exception {
    Heatmap		result;
    ThreeWayData	input;
    int			x;
    int			y;
    TDoubleSet 		setY;
    TDoubleSet 		setZ;
    TDoubleList 	listY;
    TDoubleList 	listZ;

    input = (ThreeWayData) m_Input;
    setY  = new TDoubleHashSet();
    setZ  = new TDoubleHashSet();
    for (L1Point l1: input.toList()) {
      setY.add(l1.getY());
      for (L2Point l2: l1)
	setZ.add(l2.getZ());
    }
    result = new Heatmap(setZ.size(), setY.size());
    result.setReport(input.getReport().getClone());

    listY = new TDoubleArrayList(setY);
    listY.sort();
    listZ = new TDoubleArrayList(setZ);
    listZ.sort();
    for (L1Point l1: input.toList()) {
      if ((l1.getX() >= m_MinX) && (l1.getX() <= m_MaxX)) {
	x = listY.indexOf(l1.getY());
	for (L2Point l2: l1.toList()) {
	  y = listZ.indexOf(l2.getZ());
	  result.set(y, x, result.get(y, x) + l2.getData());
	}
      }
    }

    return result;
  }
}
