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
 * JsonSpectrumReader.java
 * Copyright (C) 2016-2022 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumJsonUtils;
import adams.env.Environment;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Reads spectra in JSON format.<br>
 * Input format (single spectrum):<br>
 * - complete report stored:<br>
 * {<br>
 *   "id": "someid",<br>
 *   "format": "NIR",<br>
 *   "waves": [1.0, 2.0],<br>
 *   "amplitudes": [1.1, 2.1],<br>
 *   "report": {<br>
 *     "Sample ID": "someid",<br>
 *     "GLV2": 1.123,<br>
 *     "valid": true<br>
 *   }<br>
 * }<br>
 * <br>
 * - specific reference and meta-data values stored:<br>
 * {<br>
 *   "id": "someid",<br>
 *   "format": "NIR",<br>
 *   "waves": [1.0, 2.0],<br>
 *   "amplitudes": [1.1, 2.1],<br>
 *   "reference": {<br>
 *     "GLV2": 1.123<br>
 *   },<br>
 *   "meta-data": {<br>
 *     "valid": true<br>
 *   }<br>
 * }<br>
 * <br>
 * Multiple spectra are wrapped in an array called 'spectra'.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-input &lt;adams.core.io.PlaceholderFile&gt; (property: input)
 * &nbsp;&nbsp;&nbsp;The file to read and turn into a container.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 * 
 * <pre>-create-dummy-report &lt;boolean&gt; (property: createDummyReport)
 * &nbsp;&nbsp;&nbsp;If true, then a dummy report is created if none present.
 * &nbsp;&nbsp;&nbsp;default: false
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
 * <pre>-keep-format &lt;boolean&gt; (property: keepFormat)
 * &nbsp;&nbsp;&nbsp;If enabled the format obtained from the file is not replaced by the format 
 * &nbsp;&nbsp;&nbsp;defined here.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-use-absolute-source &lt;boolean&gt; (property: useAbsoluteSource)
 * &nbsp;&nbsp;&nbsp;If enabled the source report field stores the absolute file name rather 
 * &nbsp;&nbsp;&nbsp;than just the name.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class JsonSpectrumReader
  extends AbstractTextBasedSpectrumReader {

  /** for serialization. */
  private static final long serialVersionUID = -27209265703137172L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads spectra in JSON format.\n"
      + "Input format (single spectrum):\n"
      + "- complete report stored:\n"
      + SpectrumJsonUtils.example(false) + "\n"
      + "- specific reference and meta-data values stored:\n"
      + SpectrumJsonUtils.example(true) + "\n"
      + "Multiple spectra are wrapped in an array called 'spectra'.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "JSON Format";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"json"};
  }

  /**
   * Performs the actual reading.
   *
   * @param content 	the content to read from
   */
  @Override
  protected void readData(List<String> content) {
    Spectrum		spec;
    BufferedReader	breader;
    JsonElement		je;
    JsonObject		jobj;
    JsonArray		array;

    breader = null;

    try {
      breader = new BufferedReader(new StringReader(Utils.flatten(content, "\n")));
      je = JsonParser.parseReader(breader);

      jobj = je.getAsJsonObject();
      if (jobj.has("spectra")) {
	array = jobj.getAsJsonArray("spectra");
	for (JsonElement jo: array) {
	  spec = SpectrumJsonUtils.fromJson(jo.getAsJsonObject());
	  if (spec != null)
	    m_ReadData.add(spec);
	}
      }
      else {
	getLogger().severe("Failed to find 'spectra' property - malformed JSON?");
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to read file: " + m_Input, e);
    }
    finally {
      FileUtils.closeQuietly(breader);
    }
  }

  /**
   * Runs the reader from the command-line.
   *
   * If the option {@link #OPTION_OUTPUTDIR} is specified then the read spectra
   * get output as .spec files in that directory.
   *
   * @param args	the command-line options to use
   */
  public static void main(String[] args) {
    runReader(Environment.class, JsonSpectrumReader.class, args);
  }
}
