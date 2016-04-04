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
 * PassThrough.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumfilter.amplitudetransform;

import adams.data.spectrum.SpectrumPoint;

/**
 <!-- globalinfo-start -->
 * Dummy transformer, which only returns a copy of the data point.
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
public class PassThrough
  extends AbstractAmplitudeTransformer {

  private static final long serialVersionUID = 1342682133382785357L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Dummy transformer, which only returns a copy of the data point.";
  }

  /**
   * Transform the spectrum point and returns a new object.
   *
   * @param index	the 0-based index of the current point
   * @return		the new point
   */
  @Override
  protected SpectrumPoint transform(int index, SpectrumPoint point) {
    return (SpectrumPoint) point.getClone();
  }
}
