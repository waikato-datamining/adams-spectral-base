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
 * AbstractOutlierRemoval.java
 * Copyright (C) 2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.multispectrumoperation.outlierremoval;

import adams.core.MessageCollection;
import adams.core.QuickInfoSupporter;
import adams.core.option.AbstractOptionHandler;
import adams.data.spectrum.MultiSpectrum;

/**
 * Ancestor for outlier removal schemes that work on multiple spectra.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractOutlierRemoval
  extends AbstractOptionHandler
  implements QuickInfoSupporter {

  private static final long serialVersionUID = 5872864478259429702L;

  /**
   * Returns a quick info about the object, which can be displayed in the GUI.
   * <br>
   * Default implementation returns null.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    return null;
  }

  /**
   * Hook method for checking the data before performing outlier removal.
   *
   * @param multi 	the data to check
   * @return		null if checks passed, otherwise error message
   */
  protected String check(MultiSpectrum multi) {
    if (multi == null)
      return "No multi-spectrum provided!";
    return null;
  }

  /**
   * Performs the actual outlier removal.
   *
   * @param multi	the data to process
   * @param errors	for collecting errors
   * @return		the clean data, null if failed to process
   */
  protected abstract MultiSpectrum doRemoveOutliers(MultiSpectrum multi, MessageCollection errors);

  /**
   * Performs the outlier removal.
   *
   * @param multi	the data to process
   * @param errors	for collecting errors
   * @return		the clean data, null if failed to process
   */
  public MultiSpectrum removeOutliers(MultiSpectrum multi, MessageCollection errors) {
    String	msg;

    msg = check(multi);
    if (msg != null) {
      errors.add(msg);
      return null;
    }

    return doRemoveOutliers(multi, errors);
  }
}
