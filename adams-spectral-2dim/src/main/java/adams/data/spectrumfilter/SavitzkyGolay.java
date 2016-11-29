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
 * SavitzkyGolay.java
 * Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.container.DataPoint;
import adams.data.filter.AbstractSavitzkyGolay;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

/**
 <!-- globalinfo-start -->
 * A filter that applies Savitzky-Golay smoothing.<br>
 * <br>
 * For more information see:<br>
 * <br>
 * A. Savitzky, Marcel J.E. Golay (1964). Smoothing and Differentiation of Data by Simplified Least Squares Procedures. Analytical Chemistry. 36:1627-1639.<br>
 * <br>
 * William H. Press, Saul A. Teukolsky, William T. Vetterling, Brian P. Flannery (1992). Savitzky-Golay Smoothing Filters.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;article{Savitzky1964,
 *    author = {A. Savitzky and Marcel J.E. Golay},
 *    journal = {Analytical Chemistry},
 *    pages = {1627-1639},
 *    title = {Smoothing and Differentiation of Data by Simplified Least Squares Procedures},
 *    volume = {36},
 *    year = {1964},
 *    HTTP = {http://dx.doi.org/10.1021/ac60214a047}
 * }
 *
 * &#64;inbook{Press1992,
 *    author = {William H. Press and Saul A. Teukolsky and William T. Vetterling and Brian P. Flannery},
 *    chapter = {14.8},
 *    edition = {Second},
 *    pages = {650-655},
 *    publisher = {Cambridge University Press},
 *    series = {Numerical Recipes in C},
 *    title = {Savitzky-Golay Smoothing Filters},
 *    year = {1992},
 *    PDF = {http://www.nrbook.com/a/bookcpdf/c14-8.pdf}
 * }
 * </pre>
 * <br><br>
 <!-- technical-bibtex-end -->
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
 * <pre>-derivative &lt;int&gt; (property: derivativeOrder)
 *         The order of the derivative to use, &gt;= 0.
 *         default: 1
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
public class SavitzkyGolay
  extends AbstractSavitzkyGolay<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -8446122688895546559L;

  /**
   * Returns the X-value of the DataPoint.
   *
   * @param point	the point to get the X-Value from
   * @return		the X-value
   */
  protected double getValue(DataPoint point) {
    return ((SpectrumPoint) point).getAmplitude();
  }

  /**
   * Creates a new DataPoint based on the old one and the new X value.
   *
   * @param oldPoint	the old DataPoint
   * @param x		the new X value
   * @return		the new DataPoint
   */
  protected DataPoint newDataPoint(DataPoint oldPoint, double x) {
    return new SpectrumPoint(((SpectrumPoint) oldPoint).getWaveNumber(), (float) x);
  }
}
