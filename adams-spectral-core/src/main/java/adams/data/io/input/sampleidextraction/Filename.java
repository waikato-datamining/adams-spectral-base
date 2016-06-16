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
 * SimpleExtraction.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input.sampleidextraction;

import adams.core.io.FileUtils;
import adams.data.spectrum.Spectrum;

import java.io.File;

/**
 <!-- globalinfo-start -->
 * Just uses the filename (without path) as sample ID.
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
public class Filename
  extends AbstractSampleIDExtraction {

  private static final long serialVersionUID = 8066127884918088949L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Just uses the filename (without path and extensions) as sample ID.";
  }

  /**
   * Performs the actual extraction.
   *
   * @param file	the current file
   * @param spec	the current spectrum
   * @return		the extracted sample ID
   */
  @Override
  protected String doExtract(File file, Spectrum spec) {
    return FileUtils.replaceExtension(file, "").getName();
  }
}
