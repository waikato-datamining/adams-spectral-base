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
 * PassThrough.java
 * Copyright (C) 2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.multispectrumoperation;

import adams.core.MessageCollection;
import adams.data.spectrum.MultiSpectrum;

/**
 * Does nothing, passes through the data.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class PassThrough
  extends AbstractMultiSpectrumOperation {

  private static final long serialVersionUID = -3740751515860984156L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Does nothing, passes through the data.";
  }

  /**
   * Performs the actual operation.
   *
   * @param multi	the data to process
   * @param errors	for collecting errors
   * @return		the clean data, null if failed to process
   */
  @Override
  protected MultiSpectrum doApply(MultiSpectrum multi, MessageCollection errors) {
    return multi;
  }
}
