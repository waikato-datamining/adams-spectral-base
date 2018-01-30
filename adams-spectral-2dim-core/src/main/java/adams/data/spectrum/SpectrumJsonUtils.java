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
 * SpectrumJsonUtils.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrum;

import adams.data.report.Report;
import adams.data.report.ReportJsonUtils;
import adams.data.sampledata.SampleData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * For converting spectra to JSON and vice versa.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumJsonUtils {

  public static final String KEY_DATA = "data";

  public static final String KEY_WAVE = "wave";

  public static final String KEY_AMPL = "ampl";

  public static final String KEY_REPORT = "report";

  /**
   * Returns an example spectrum in JSON.
   *
   * @return		the example string
   */
  public static String example() {
    return "{\n" +
      "  \"id\": \"someid\",\n" +
      "  \"format\": \"NIR\",\n" +
      "  \"data\": [\n" +
      "    {\"wave\": 1.0, \"ampl\": 1.1},\n" +
      "    {\"wave\": 2.0, \"ampl\": 2.1}\n" +
      "  ],\n" +
      "  \"report\": {\n" +
      "    \"Sample ID\": \"someid\",\n" +
      "    \"GLV2\": 1.123,\n" +
      "    \"valid\": true\n" +
      "  }\n" +
      "}\n";
  }

  /**
   * Creates a spectrum from the JSON object (spectral + report).
   *
   * @param jobj	the object to get the data from
   * @return		the spectrum, null if failed to create or find data
   */
  public static Spectrum fromJson(JsonObject jobj) {
    Spectrum		result;
    Report 		report;
    JsonArray 		array;
    JsonObject		jo;

    result = null;

    if (jobj.has(KEY_DATA)) {
      result = new Spectrum();

      // data
      array = jobj.getAsJsonArray(KEY_DATA);
      for (JsonElement je: array) {
	jo = je.getAsJsonObject();
	if (jo.has(KEY_WAVE) && jo.has(KEY_AMPL))
	  result.add(new SpectrumPoint(jo.get(KEY_WAVE).getAsFloat(), jo.get(KEY_AMPL).getAsFloat()));
      }

      // report
      if (jobj.has(KEY_REPORT)) {
	report = ReportJsonUtils.fromJson(jobj.getAsJsonObject(KEY_REPORT));
	result.getReport().mergeWith(report);
	if (report.hasValue(SampleData.SAMPLE_ID))
	  result.setID(report.getStringValue(SampleData.SAMPLE_ID));
	if (report.hasValue(SampleData.FORMAT))
	  result.setFormat(report.getStringValue(SampleData.FORMAT));
      }
    }

    return result;
  }

  /**
   * Turns the spectrum into a json structure (spectral + report).
   *
   * @param spec	the spectrum to convert
   * @return		the json data structure
   */
  public static JsonObject toJson(Spectrum spec) {
    JsonObject		result;
    JsonArray		array;
    JsonObject		data;

    result = new JsonObject();

    // data
    array = new JsonArray();
    for (SpectrumPoint p: spec) {
      data = new JsonObject();
      data.addProperty(KEY_WAVE, p.getWaveNumber());
      data.addProperty(KEY_AMPL, p.getAmplitude());
      array.add(data);
    }
    result.add(KEY_DATA, array);

    // report
    data = new JsonObject();
    if (spec.hasReport())
      data = ReportJsonUtils.toJson(spec.getReport());
    result.add(KEY_REPORT, data);

    return result;
  }
}
