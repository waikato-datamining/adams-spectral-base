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
 * SlidingWindow.java
 * Copyright (C) 2008-2010 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumsmoothing;

import adams.data.container.DataPoint;
import adams.data.smoothing.AbstractSlidingWindow;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

/**
 <!-- globalinfo-start -->
 * Uses a sliding window for determining the median/average inside the window. This measure is then used as new abundance for the spectrum point in the center of the window. The left and the right ends of the chromatogram are filled with dummy spectrum points to return a chromatogram with the same number of spectrum points.
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
 * <pre>-window &lt;int&gt; (property: windowSize)
 *         The window size for determining the 'smoothed' abundances.
 *         default: 20
 * </pre>
 *
 * <pre>-measure &lt;MEDIAN|MEAN&gt; (property: measure)
 *         The measure to use for calculating the 'smoothed' abundances.
 *         default: MEDIAN
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SlidingWindow
  extends AbstractSlidingWindow<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 5542490162825298823L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return
        "Uses a sliding window for determining the median/average inside the window. "
      + "This measure is then used as new abundance for the spectrum point in the "
      + "center of the window. The left and the right ends of the chromatogram "
      + "are filled with dummy spectrum points to return a chromatogram with the same "
      + "number of spectrum points.";
  }

  /**
   * Returns the X-value of the data point.
   *
   * @param point	the point to get the X-value from
   * @return		the X-value
   */
  protected Double getValue(DataPoint point) {
    return new Double(((SpectrumPoint) point).getAmplitude());
  }

  /**
   * Updates the X-value of the data point.
   *
   * @param point	the point to update
   * @param value	the value to update the point with
   */
  protected void updatePoint(DataPoint point, double value) {
    ((SpectrumPoint) point).setAmplitude((float) value);
  }
}
