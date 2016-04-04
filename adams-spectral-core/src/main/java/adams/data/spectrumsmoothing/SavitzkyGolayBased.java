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
 * SavitzkyGolayBased.java
 * Copyright (C) 2009-2010 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumsmoothing;

import adams.data.filter.AbstractSavitzkyGolay;
import adams.data.smoothing.AbstractSavitzkyGolayBased;
import adams.data.spectrumfilter.SavitzkyGolay;
import adams.data.spectrum.Spectrum;

/**
 <!-- globalinfo-start -->
 * A Savitzky-Golay based smoothing algorithm.<br>
 * It uses a Savitzky-Golay filter with derivative order 0 and adding of mass-spec data turned on.<br>
 * <br>
 * For more information on Savitzky-Golay see:<br>
 * <br>
 * A. Savitzky, Marcel J.E. Golay (1964). Smoothing and Differentiation of Data by Simplified Least Squares Procedures. Analytical Chemistry. 36:1627-1639.<br>
 * <br>
 * William H. Press, Saul A. Teukolsky, William T. Vetterling, Brian P. Flannery (1992). Savitzky-Golay Smoothing Filters.
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
 * <pre>-polynomial &lt;int&gt; (property: polynomialOrder)
 *         The polynomial order to use, must be at least 2.
 *         default: 2
 * </pre>
 *
 * <pre>-left &lt;int&gt; (property: numPointsLeft)
 *         The number of points left of a data point, &gt;= 0.
 *         default: 3
 * </pre>
 *
 * <pre>-right &lt;int&gt; (property: numPointsRight)
 *         The number of points right of a data point, &gt;= 0.
 *         default: 3
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SavitzkyGolayBased
  extends AbstractSavitzkyGolayBased<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -4052647569528377770L;

  /**
   * Returns the default Savitzky-Golay filter.
   *
   * @return		the default filter
   */
  protected AbstractSavitzkyGolay getDefault() {
    SavitzkyGolay	result;

    result = new SavitzkyGolay();
    result.setDerivativeOrder(0);

    return result;
  }
}
