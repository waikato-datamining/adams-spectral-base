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
 * SpectrumToJson.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.conversion;

import adams.data.report.AbstractField;
import adams.data.report.Report;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 <!-- globalinfo-start -->
 * Turns a spectrum into a JSON object.
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
 */
public class SpectrumToJson
  extends AbstractConversion {

  private static final long serialVersionUID = 2957342595369694174L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Turns a spectrum into a JSON object.";
  }

  /**
   * Returns the class that is accepted as input.
   *
   * @return		the class
   */
  @Override
  public Class accepts() {
    return Spectrum.class;
  }

  /**
   * Returns the class that is generated as output.
   *
   * @return		the class
   */
  @Override
  public Class generates() {
    return JSONObject.class;
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  @Override
  protected Object doConvert() throws Exception {
    JSONObject	result;
    JSONObject	data;
    JSONArray	array;
    Spectrum	input;
    Report	report;

    result = new JSONObject();
    input  = (Spectrum) m_Input;

    // basic
    result.put("id", input.getID());
    result.put("format", input.getFormat());

    // spectrum
    array = new JSONArray();
    for (SpectrumPoint p: input) {
      data = new JSONObject();
      data.put("wave", p.getWaveNumber());
      data.put("ampl", p.getAmplitude());
      array.add(data);
    }
    result.put("spectrum", array);

    // sample data
    array = new JSONArray();
    if (input.hasReport()) {
      report = input.getReport();
      for (AbstractField field : report.getFields()) {
	data = new JSONObject();
	data.put("name", field.getName());
	data.put("type", field.getDataType().toString());
	switch (field.getDataType()) {
	  case NUMERIC:
	    data.put("value", report.getDoubleValue(field));
	    break;
	  case BOOLEAN:
	    data.put("value", report.getBooleanValue(field));
	    break;
	  case STRING:
	  case UNKNOWN:
	    data.put("value", report.getStringValue(field));
	    break;
	  default:
	    throw new IllegalStateException("Unhandled data type: " + field.getDataType());
	}
	array.add(data);
      }
    }
    result.put("sampledata", array);

    return result;
  }
}
