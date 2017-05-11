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
 * Copyright (C) 2016 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.io.FileUtils;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.Report;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Map.Entry;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Reads spectra in JSON format.
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
 * @version $Revision: 2242 $
 */
public class JsonSpectrumReader
  extends AbstractSpectrumReader {

  /** for serialization. */
  private static final long serialVersionUID = -27209265703137172L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads spectra in JSON format.";
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
   * Creates a spectrum from the JSON object (spectral + report).
   *
   * @param jobj	the object to get the data from
   * @return		the spectrum, null if failed to create or find data
   */
  protected Spectrum fromJson(JsonObject jobj) {
    Spectrum		result;
    Report		report;
    JsonArray		array;
    JsonObject		jreport;
    JsonObject		jo;
    Field		field;
    JsonPrimitive	prim;

    result = null;

    if (jobj.has("data")) {
      result = new Spectrum();

      // data
      array = jobj.getAsJsonArray("data");
      for (JsonElement je: array) {
	jo = je.getAsJsonObject();
	if (jo.has("wave") && jo.has("ampl"))
	  result.add(new SpectrumPoint(jo.get("wave").getAsFloat(), jo.get("ampl").getAsFloat()));
      }

      // report
      if (jobj.has("report")) {
	report  = result.getReport();
	jreport = jobj.getAsJsonObject("report");
	for (Entry<String, JsonElement> entry: jreport.entrySet()) {
	  prim = entry.getValue().getAsJsonPrimitive();
	  if (prim.isBoolean()) {
	    field = new Field(entry.getKey(), DataType.BOOLEAN);
	    report.addField(field);
	    report.setBooleanValue(field.getName(), prim.getAsBoolean());
	  }
	  else if (prim.isNumber()) {
	    field = new Field(entry.getKey(), DataType.NUMERIC);
	    report.addField(field);
	    report.setNumericValue(field.getName(), prim.getAsNumber().doubleValue());
	  }
	  else {
	    field = new Field(entry.getKey(), DataType.STRING);
	    if (field.getName().equals(SampleData.SAMPLE_ID)) {
	      result.setID(prim.getAsString());
	    }
	    else if (field.getName().equals(SampleData.FORMAT)) {
	      result.setFormat(prim.getAsString());
	    }
	    else {
	      report.addField(field);
	      report.setStringValue(field.getName(), prim.getAsString());
	    }
	  }
	}
      }
    }

    return result;
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    Spectrum		spec;
    FileReader		freader;
    BufferedReader	breader;
    JsonParser 		jp;
    JsonElement		je;
    JsonObject		jobj;
    JsonArray		array;

    freader = null;
    breader = null;

    try {
      freader = new FileReader(m_Input.getAbsolutePath());
      breader = new BufferedReader(freader);
      jp = new JsonParser();
      je = jp.parse(breader);

      jobj = je.getAsJsonObject();
      if (jobj.has("spectra")) {
	array = jobj.getAsJsonArray("spectra");
	for (JsonElement jo: array) {
	  spec = fromJson(jo.getAsJsonObject());
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
      FileUtils.closeQuietly(freader);
    }
  }
}
