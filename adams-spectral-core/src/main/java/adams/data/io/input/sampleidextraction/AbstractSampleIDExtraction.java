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
 * AbstractSampleIDExtraction.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input.sampleidextraction;

import adams.core.option.AbstractOptionHandler;
import adams.data.spectrum.Spectrum;

import java.io.File;

/**
 * Ancestor for schemes that extract the sample ID from the filename.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractSampleIDExtraction
  extends AbstractOptionHandler
  implements SampleIDExtraction {

  private static final long serialVersionUID = 8856396830843848348L;

  /**
   * Performs checks before the actual extraction.
   *
   * @param file	the current file
   * @param spec	the current spectrum
   */
  protected void check(File file, Spectrum spec) {
    if (file == null)
      throw new IllegalArgumentException("No file provided!");
    if (spec == null)
      throw new IllegalArgumentException("No spectrum provided!");
  }

  /**
   * Performs the actual extraction.
   *
   * @param file	the current file
   * @param spec	the current spectrum
   * @return		the extracted sample ID
   */
  protected abstract String doExtract(File file, Spectrum spec);

  /**
   * Performs the extraction.
   *
   * @param file	the current file
   * @param spec	the current spectrum
   * @return		the extracted sample ID
   */
  public String extract(File file, Spectrum spec) {
    check(file, spec);
    return doExtract(file, spec);
  }
}
