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
 * Copyright (C) 2018-2021 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrum;

import adams.data.report.Field;
import adams.data.report.Report;
import adams.data.report.ReportJsonUtils;
import adams.data.sampledata.SampleData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * For converting spectra to JSON and vice versa.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumJsonUtils {

  public static final String KEY_ID = "id";

  public static final String KEY_WAVES = "waves";

  public static final String KEY_AMPLITUDES = "amplitudes";

  public static final String KEY_REPORT = "report";

  public static final String KEY_REFERENCE = "reference";

  public static final String KEY_METADATA = "meta-data";

  /**
   * Returns an example spectrum in JSON.
   *
   * @param useRefAndMeta 	whether to output example for ref/meta-data or complete report
   * @return			the example string
   */
  public static String example(boolean useRefAndMeta) {
    if (useRefAndMeta)
      return "{\n" +
	"  \"id\": \"someid\",\n" +
	"  \"format\": \"NIR\",\n" +
	"  \"waves\": [1.0, 2.0],\n" +
	"  \"amplitudes\": [1.1, 2.1],\n" +
	"  \"reference\": {\n" +
	"    \"GLV2\": 1.123\n" +
	"  },\n" +
	"  \"meta-data\": {\n" +
	"    \"valid\": true\n" +
	"  }\n" +
	"}\n";
    else
      return "{\n" +
	"  \"id\": \"someid\",\n" +
	"  \"format\": \"NIR\",\n" +
	"  \"waves\": [1.0, 2.0],\n" +
	"  \"amplitudes\": [1.1, 2.1],\n" +
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
    Report report;
    JsonArray		waves;
    JsonArray 		ampls;
    int			i;

    result = null;

    if (jobj.has(KEY_WAVES) && jobj.has(KEY_AMPLITUDES)) {
      result = new Spectrum();
      result.setID("");
      if (jobj.has(KEY_ID))
	result.setID(jobj.get(KEY_ID).getAsString());

      // spectrum
      waves = jobj.getAsJsonArray(KEY_WAVES);
      ampls = jobj.getAsJsonArray(KEY_AMPLITUDES);
      if (waves.size() != ampls.size())
        throw new IllegalStateException("Wave number and amplitude arrays differ in length: " + waves.size() + " != " + ampls.size());
      for (i = 0; i < waves.size(); i++)
	result.add(new SpectrumPoint(waves.get(i).getAsFloat(), ampls.get(i).getAsFloat()));

      // report
      if (jobj.has(KEY_REPORT)) {
	report = ReportJsonUtils.fromJson(jobj.getAsJsonObject(KEY_REPORT));
	result.getReport().mergeWith(report);
	if (result.getID().isEmpty() && report.hasValue(SampleData.SAMPLE_ID))
	  result.setID(report.getStringValue(SampleData.SAMPLE_ID));
	if (report.hasValue(SampleData.FORMAT))
	  result.setFormat(report.getStringValue(SampleData.FORMAT));
      }

      // reference values
      if (jobj.has(KEY_REFERENCE)) {
	report = ReportJsonUtils.fromJson(jobj.getAsJsonObject(KEY_REFERENCE));
	result.getReport().mergeWith(report);
      }

      // meta-data values
      if (jobj.has(KEY_METADATA)) {
	report = ReportJsonUtils.fromJson(jobj.getAsJsonObject(KEY_METADATA));
	result.getReport().mergeWith(report);
	if (result.getID().isEmpty() && report.hasValue(SampleData.SAMPLE_ID))
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
   * @see		#KEY_AMPLITUDES
   * @see		#KEY_WAVES
   * @see		#KEY_REPORT
   */
  public static JsonObject toJson(Spectrum spec) {
    JsonObject		result;
    JsonArray		waves;
    JsonArray		ampls;
    JsonObject		data;

    result = new JsonObject();
    result.addProperty(KEY_ID, spec.getID());

    // spectrum
    waves = new JsonArray();
    ampls = new JsonArray();
    for (SpectrumPoint p: spec) {
      waves.add(p.getWaveNumber());
      ampls.add(p.getAmplitude());
    }
    result.add(KEY_WAVES, waves);
    result.add(KEY_AMPLITUDES, ampls);

    // report
    data = new JsonObject();
    if (spec.hasReport())
      data = ReportJsonUtils.toJson(spec.getReport());
    result.add(KEY_REPORT, data);

    return result;
  }

  /**
   * Turns the spectrum into a json structure (spectral + ref + meta).
   *
   * @param spec	the spectrum to convert
   * @param ref 	the reference values to output
   * @param meta	the meta-data values to output
   * @return		the json data structure
   * @see		#KEY_AMPLITUDES
   * @see		#KEY_WAVES
   * @see		#KEY_REFERENCE
   * @see		#KEY_METADATA
   */
  public static JsonObject toJson(Spectrum spec, Field[] ref, Field[] meta) {
    JsonObject		result;
    JsonArray		waves;
    JsonArray		ampls;
    JsonObject		data;

    result = new JsonObject();
    result.addProperty(KEY_ID, spec.getID());

    // spectrum
    waves = new JsonArray();
    ampls = new JsonArray();
    for (SpectrumPoint p: spec) {
      waves.add(p.getWaveNumber());
      ampls.add(p.getAmplitude());
    }
    result.add(KEY_WAVES, waves);
    result.add(KEY_AMPLITUDES, ampls);

    // reference values
    data = new JsonObject();
    if (spec.hasReport())
      data = ReportJsonUtils.toJson(spec.getReport(), ref);
    result.add(KEY_REFERENCE, data);

    // meta-data values
    data = new JsonObject();
    if (spec.hasReport())
      data = ReportJsonUtils.toJson(spec.getReport(), meta);
    result.add(KEY_METADATA, data);

    return result;
  }
}
