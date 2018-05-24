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

/**
 * ThreeWayDataToHeatmap.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
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
 * Does not take the X of the level 2 points into account.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ThreeWayDataToHeatmap
  extends AbstractConversion {

  private static final long serialVersionUID = -8371135112409803967L;

  /** the z layer to use. */
  protected double m_Z;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Turns a " + ThreeWayData.class.getName() + " data structure into a heatmap.\n"
      + "Does not take the X of the level 2 points into account.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "z", "Z",
      0.0);
  }

  /**
   * Sets the Z layer to use.
   *
   * @param value 	the Z
   */
  public void setZ(double value) {
    m_Z = value;
    reset();
  }

  /**
   * Returns the Z layer to use.
   *
   * @return 		the Z
   */
  public double getZ() {
    return m_Z;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String ZTipText() {
    return "The Z layer to use.";
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
    TDoubleSet 		setX;
    TDoubleSet 		setY;
    TDoubleList		listX;
    TDoubleList		listY;

    input = (ThreeWayData) m_Input;
    setX  = new TDoubleHashSet();
    setY  = new TDoubleHashSet();
    for (L1Point l1: input.toList()) {
      setX.add(l1.getX());
      setY.add(l1.getY());
    }
    result = new Heatmap(setY.size(), setX.size());

    listX = new TDoubleArrayList(setX);
    listX.sort();
    listY = new TDoubleArrayList(setY);
    listY.sort();
    for (L1Point l1: input.toList()) {
      x = listX.indexOf(l1.getX());
      y = listY.indexOf(l1.getY());
      for (L2Point l2: l1.toList()) {
        if (l2.getZ() == m_Z)
	  result.set(y, x, l2.getData());
      }
    }

    return result;
  }
}
