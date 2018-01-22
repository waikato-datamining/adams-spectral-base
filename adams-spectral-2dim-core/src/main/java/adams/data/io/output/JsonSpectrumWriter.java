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
 * JsonSpectrumWriter.java
 * Copyright (C) 2016-2018 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.output;

import adams.core.io.FileUtils;
import adams.core.io.PrettyPrintingSupporter;
import adams.data.spectrum.JsonUtils;
import adams.data.spectrum.Spectrum;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
<!-- globalinfo-start -->
* Writes spectra in JSON format.<br>
* Output format for single spectrum:<br>
* {<br>
*   "id": "someid",<br>
*   "format": "NIR",<br>
*   "data": [<br>
*     {"wave": 1.0, "ampl": 1.1},<br>
*     {"wave": 2.0, "ampl": 2.1}<br>
*   ],<br>
*   "report": {<br>
*     "Sample ID": "someid",<br>
*     "GLV2": 1.123,<br>
*     "valid": true<br>
*   }<br>
* }<br>
* <br>
* Multiple spectra get wrapped in an array called 'spectra'.
* <br><br>
<!-- globalinfo-end -->
*
<!-- options-start -->
* <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
* &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
* &nbsp;&nbsp;&nbsp;default: WARNING
* </pre>
*
* <pre>-output &lt;adams.core.io.PlaceholderFile&gt; (property: output)
* &nbsp;&nbsp;&nbsp;The file to write the container to.
* &nbsp;&nbsp;&nbsp;default: ${TMP}&#47;out.tmp
* </pre>
*
* <pre>-pretty-printing &lt;boolean&gt; (property: prettyPrinting)
* &nbsp;&nbsp;&nbsp;If enabled, the output is printed in a 'pretty' format.
* &nbsp;&nbsp;&nbsp;default: false
* </pre>
* 
<!-- options-end -->
*
* @author FracPete (fracpete at waikato dot ac dot nz)
*/
public class JsonSpectrumWriter
  extends AbstractSpectrumWriter
  implements PrettyPrintingSupporter {

  /** for serialization. */
  private static final long serialVersionUID = 208155740775061862L;

  /** whether to use pretty-printing. */
  protected boolean m_PrettyPrinting;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Writes spectra in JSON format.\n"
      + "Output format for single spectrum:\n"
      + JsonUtils.example() + "\n"
      + "Multiple spectra get wrapped in an array called 'spectra'.";
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "pretty-printing", "prettyPrinting",
      false);
  }

  /**
   * Sets whether to use pretty-printing or not.
   *
   * @param value	true if to use pretty-printing
   */
  public void setPrettyPrinting(boolean value) {
    m_PrettyPrinting = value;
    reset();
  }

  /**
   * Returns whether pretty-printing is used or not.
   *
   * @return		true if to use pretty-printing
   */
  public boolean getPrettyPrinting() {
    return m_PrettyPrinting;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String prettyPrintingTipText() {
    return "If enabled, the output is printed in a 'pretty' format.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the file chooser
   */
  @Override
  public String getFormatDescription() {
    return "JSON format";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 		the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"json"};
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();
    m_OutputIsFile = true;
  }

  /**
   * Returns whether writing of multiple containers is supported.
   *
   * @return 		true if multiple containers are supported
   */
  @Override
  public boolean canWriteMultiple() {
    return true;
  }

  /**
   * Performs the actual writing.
   *
   * @param data	the data to write
   * @return		true if successfully written
   */
  @Override
  protected boolean writeData(List<Spectrum> data) {
    JsonObject		jspec;
    JsonArray		jspecs;
    JsonObject		jcont;
    GsonBuilder		builder;
    Gson 		gson;
    String		content;
    String		msg;

    jcont  = new JsonObject();
    jspecs = new JsonArray();
    for (Spectrum spec: data) {
      jspec = JsonUtils.toJson(spec);
      jspecs.add(jspec);
    }
    jcont.add("spectra", jspecs);
    builder = new GsonBuilder();
    if (m_PrettyPrinting)
      builder.setPrettyPrinting();
    gson    = builder.create();
    content = gson.toJson(jcont);

    msg = FileUtils.writeToFileMsg(getOutput().getAbsolutePath(), content, false, null);
    if (msg != null)
      getLogger().severe(msg);

    return (msg == null);
  }
}
