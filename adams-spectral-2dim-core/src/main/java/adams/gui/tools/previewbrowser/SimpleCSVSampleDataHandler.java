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
 * SimpleCSVSampleDataHandler.java
 * Copyright (C) 2011-2017 University of Waikato, Hamilton, New Zealand
 */
package adams.gui.tools.previewbrowser;

import adams.data.io.input.AbstractReportReader;
import adams.data.io.input.SimpleCSVSampleDataReader;

import java.io.File;

/**
 <!-- globalinfo-start -->
 * Displays the following report types: csv
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D &lt;int&gt; (property: debugLevel)
 * &nbsp;&nbsp;&nbsp;The greater the number the more additional info the scheme may output to
 * &nbsp;&nbsp;&nbsp;the console (0 = off).
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SimpleCSVSampleDataHandler
  extends AbstractReportHandler {

  /** for serialization. */
  private static final long serialVersionUID = 5400842321068719101L;

  /**
   * Returns the list of extensions (without dot) that this handler can
   * take care of.
   *
   * @return		the list of extensions (no dot)
   */
  public String[] getExtensions() {
    return new String[]{"csv"};
  }

  /**
   * Returns the reader to use.
   *
   * @param file	the file to read from
   * @return		the reader
   */
  protected AbstractReportReader getReader(File file) {
    return new SimpleCSVSampleDataReader();
  }
}
