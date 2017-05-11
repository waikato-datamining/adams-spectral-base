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
 * Derivative.java
 * Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.container.DataPoint;
import adams.data.filter.AbstractDerivative;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

/**
 <!-- globalinfo-start -->
 * A filter for generating derivatives of spectra.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-order &lt;int&gt; (property: order)
 *         The order of the derivative to calculate.
 *         default: 1
 * </pre>
 *
 * <pre>-scaling &lt;float&gt; (property: scalingRange)
 *         The range to scale the abundances to after each derivation step; use 0 to
 *          turn off and -1 to set it to the input range.
 *         default: 0.0
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class Derivative
  extends AbstractDerivative<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 530300053103127948L;

  /**
   * Returns a string describing the object.
   *
   * @return 		a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return "A filter for generating derivatives of spectra.";
  }

  /**
   * Turns the DataPoint into the intermediate format.
   *
   * @param point	the DataPoint to convert
   * @return		the generated intermediate format point
   */
  protected Point toPoint(DataPoint point) {
    Point		result;
    SpectrumPoint	sp;

    sp     = (SpectrumPoint) point;
    result = new Point(sp.getWaveNumber(), sp.getAmplitude());

    return result;
  }

  /**
   * Turns the intermediate format point back into a DataPoint.
   *
   * @param point	the intermediate format point to convert
   * @return		the generated DataPoint
   */
  protected DataPoint toDataPoint(Point point) {
    return new SpectrumPoint((float) point.getX(), (float) point.getY());
  }
}
