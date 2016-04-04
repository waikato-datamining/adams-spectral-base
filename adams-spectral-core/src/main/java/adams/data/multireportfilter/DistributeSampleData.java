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
 * DistributeSampleData.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package adams.data.multireportfilter;

import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;

/**
 <!-- globalinfo-start -->
 * Distributes the sample data among all the sub-spectra, never overwrites already existing values.
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
public class DistributeSampleData
  extends AbstractMultiSpectrumReportFilter {

  private static final long serialVersionUID = 9130818615270130876L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Distributes the sample data among all the sub-spectra, never overwrites already existing values.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data, null if failed to generate output
   */
  @Override
  protected MultiSpectrum processData(MultiSpectrum data) {
    MultiSpectrum     result;

    result = (MultiSpectrum) data.getClone();
    for (Spectrum spOutter: result) {
      for (Spectrum spInner: result) {
        if (spOutter != spInner) {
          if (spOutter.hasReport() && spInner.hasReport())
            spOutter.getReport().mergeWith(spInner.getReport());
        }
      }
    }

    return result;
  }
}
