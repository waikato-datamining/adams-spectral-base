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
import adams.data.threeway.LevelOnePoint;
import adams.data.threeway.LevelTwoPoint;
import adams.data.threeway.ThreeWayData;

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
 * @version $Revision$
 */
public class ThreeWayDataToHeatmap
  extends AbstractConversion {

  private static final long serialVersionUID = -8371135112409803967L;

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
    int			cols;
    int			n;
    int			i;

    input  = (ThreeWayData) m_Input;
    cols   = 0;
    for (LevelOnePoint l1: input.toList())
      cols = Math.max(cols, l1.size());
    result = new Heatmap(input.size(), cols);

    n = 0;
    for (LevelOnePoint l1: input.toList()) {
      i = 0;
      for (LevelTwoPoint l2: l1.toList()) {
	result.set(n, i, l2.getY());
	i++;
      }
      n++;
    }

    return result;
  }
}
