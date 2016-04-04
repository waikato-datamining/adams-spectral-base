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
 * SimpleSavitzkyGolay.java
 * Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.container.DataPoint;
import adams.data.filter.AbstractSimpleSavitzkyGolay;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

/**
 <!-- globalinfo-start -->
 * A filter that applies a simplified Savitzky-Golay smoothing.<br>
 * <br>
 * For more information on Savitzky-Golay see:<br>
 * <br>
 * A. Savitzky, Marcel J.E. Golay (1964). Smoothing and Differentiation of Data by Simplified Least Squares Procedures. Analytical Chemistry. 36:1627-1639.
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
 * <pre>-windowSize &lt;int&gt; (property: windowSize)
 *         The window size to use for smoothing.
 *         default: 7
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  dale (dale at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SimpleSavitzkyGolay
  extends AbstractSimpleSavitzkyGolay<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 343582700139292935L;

  /**
   * Returns the Y-value of the point.
   *
   * @param point	the DataPoint to get the Y-value from
   * @return		the Y-value
   */
  @Override
  protected double getValue(DataPoint point) {
    return ((SpectrumPoint) point).getAmplitude();
  }

  /**
   * Creates a new DataPoint based on the old one and the new Y-value.
   *
   * @param old		the old DataPoint
   * @param y		the new Y-value
   * @return		the new DataPoint
   */
  @Override
  protected DataPoint newDataPoint(DataPoint old, double y) {
    return new SpectrumPoint(((SpectrumPoint) old).getWaveNumber(), (float) y);
  }
}
