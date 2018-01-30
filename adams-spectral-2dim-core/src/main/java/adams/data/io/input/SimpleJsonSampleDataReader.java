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
 * SimpleJsonSampleDataReader.java
 * Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.data.sampledata.SampleData;

/**
 <!-- globalinfo-start -->
 * Reads a report file in Json file format.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 * &nbsp;&nbsp;&nbsp;If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-input &lt;adams.core.io.PlaceholderFile&gt; (property: input)
 * &nbsp;&nbsp;&nbsp;The file to read and turn into a report.
 * &nbsp;&nbsp;&nbsp;default: .
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SimpleJsonSampleDataReader
  extends AbstractSimpleJsonReportReader<SampleData>
  implements SampleDataReader {

  /** for serialization. */
  private static final long serialVersionUID = 5951148282369493129L;

  /**
   * Returns a new instance of the report class in use.
   *
   * @return		the new (empty) report
   */
  public SampleData newInstance() {
    return new SampleData();
  }
}
