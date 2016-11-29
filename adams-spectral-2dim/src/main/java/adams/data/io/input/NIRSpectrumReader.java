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
 * NIRSpectrumReader.java
 * Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

/**
<!-- globalinfo-start -->
* Reads spectra in FOSS NIR format.
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
* <pre>-input &lt;adams.core.io.PlaceholderFile&gt; (property: input)
* &nbsp;&nbsp;&nbsp;The file to read and turn into a container.
* &nbsp;&nbsp;&nbsp;default: .
* </pre>
*
* <pre>-create-dummy-report (property: createDummyReport)
* &nbsp;&nbsp;&nbsp;If true, then a dummy report is created if none present.
* </pre>
*
* <pre>-instrument &lt;java.lang.String&gt; (property: instrument)
* &nbsp;&nbsp;&nbsp;The name of the instrument that generated the spectra (if not already present
* &nbsp;&nbsp;&nbsp;in data).
* &nbsp;&nbsp;&nbsp;default: unknown
* </pre>
*
* <pre>-format &lt;java.lang.String&gt; (property: format)
* &nbsp;&nbsp;&nbsp;The data format string.
* &nbsp;&nbsp;&nbsp;default: NIR
* </pre>
*
* <pre>-typefield &lt;java.lang.String&gt; (property: typefield)
* &nbsp;&nbsp;&nbsp;Code|Field1|Field2|Field3|ID|[sample_type]
* &nbsp;&nbsp;&nbsp;default: Code
* </pre>
*
* <pre>-idfield &lt;java.lang.String&gt; (property: idfield)
* &nbsp;&nbsp;&nbsp;ID|Field1|Field2|Field3|[prefix]
* &nbsp;&nbsp;&nbsp;default: ID
* </pre>
*
* <pre>-start &lt;int&gt; (property: start)
* &nbsp;&nbsp;&nbsp;Spectrum number to start loading from.
* &nbsp;&nbsp;&nbsp;default: 1
* </pre>
*
* <pre>-max &lt;int&gt; (property: max)
* &nbsp;&nbsp;&nbsp;Maximum spectra to load.
* &nbsp;&nbsp;&nbsp;default: -1
* </pre>
*
<!-- options-end -->
*
* @author  dale (dale at waikato dot ac dot nz)
* @version $Revision: 2242 $
*/
public class NIRSpectrumReader extends CALSpectrumReader {

  /**suid.  */
  private static final long serialVersionUID = 7690015355854851867L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return "Reads spectra in FOSS NIR format.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  public String getFormatDescription() {
    return "FOSS NIR Format";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension(s) (without the dot!)
   */
  public String[] getFormatExtensions() {
    return new String[]{"nir"};
  }
}
