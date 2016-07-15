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
 * Copyright (C) 2016 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.output;

import adams.core.io.FileUtils;
import adams.data.report.AbstractField;
import adams.data.report.Report;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
<!-- globalinfo-start -->
* Writes spectra in JSON format.
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
<!-- options-end -->
*
* @author  dale (dale at waikato dot ac dot nz)
* @version $Revision: 2242 $
*/
public class JsonSpectrumWriter
  extends AbstractSpectrumWriter {

  /** for serialization. */
  private static final long serialVersionUID = 208155740775061862L;

  @Override
  public String globalInfo() {
    return "Writes spectra in JSON format.";
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
   * Turns the spectrum into a json structure (spectral + report).
   *
   * @param spec	the spectrum to convert
   * @return		the json data structure
   */
  protected JsonObject toJson(Spectrum spec) {
    JsonObject		result;
    JsonArray		array;
    JsonObject		data;
    Report		report;

    result = new JsonObject();

    // data
    array = new JsonArray();
    for (SpectrumPoint p: spec) {
      data = new JsonObject();
      data.addProperty("wave", p.getWaveNumber());
      data.addProperty("ampl", p.getAmplitude());
      array.add(data);
    }
    result.add("data", array);

    // report
    data = new JsonObject();
    if (spec.hasReport()) {
      report = spec.getReport();
      for (AbstractField field : report.getFields()) {
	switch (field.getDataType()) {
	  case NUMERIC:
	    data.addProperty(field.getName(), report.getDoubleValue(field));
	    break;
	  case BOOLEAN:
	    data.addProperty(field.getName(), report.getBooleanValue(field));
	    break;
	  case STRING:
	  case UNKNOWN:
	    data.addProperty(field.getName(), report.getStringValue(field));
	    break;
	  default:
	    throw new IllegalStateException("Unhandled data type: " + field.getDataType());
	}
      }
    }
    result.add("report", data);

    return result;
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
    Gson 		gson;
    String		content;
    String		msg;

    jcont  = new JsonObject();
    jspecs = new JsonArray();
    for (Spectrum spec: data) {
      jspec = toJson(spec);
      jspecs.add(jspec);
    }
    jcont.add("spectra", jspecs);
    gson    = new GsonBuilder().setPrettyPrinting().create();
    content = gson.toJson(jcont);

    msg = FileUtils.writeToFileMsg(getOutput().getAbsolutePath(), content, false, null);
    if (msg != null)
      getLogger().severe(msg);

    return (msg == null);
  }
}
