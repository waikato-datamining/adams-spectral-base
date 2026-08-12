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
 * CenteredMovingAverageBased.java
 * Copyright (C) 2026 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumsmoothing;

import adams.data.filter.AbstractCenteredMovingAverage;
import adams.data.smoothing.AbstractCenteredMovingAverageBased;
import adams.data.spectrum.Spectrum;
import adams.data.spectrumfilter.CenteredMovingAverage;

/**
 <!-- globalinfo-start -->
 * A CenteredMovingAverage based smoothing algorithm.<br>
 * For more information on CenteredMovingAverage see:<br>
 * <br>
 * WikiPedia. Moving average. URL https:&#47;&#47;en.wikipedia.org&#47;wiki&#47;Moving_average
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * &nbsp;&nbsp;&nbsp;min-user-mode: Expert
 * </pre>
 *
 * <pre>-window-size &lt;int&gt; (property: windowSize)
 * &nbsp;&nbsp;&nbsp;The window size to use (uneven number).
 * &nbsp;&nbsp;&nbsp;default: 15
 * &nbsp;&nbsp;&nbsp;minimum: 1
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class CenteredMovingAverageBased
  extends AbstractCenteredMovingAverageBased<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -4052647569528377770L;

  /**
   * Returns the default CenteredMovingAverage filter.
   *
   * @return		the default filter
   */
  @Override
  protected AbstractCenteredMovingAverage getDefault() {
    return new CenteredMovingAverage();
  }
}
