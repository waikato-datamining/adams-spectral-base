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
 * JsonUtils.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrum;

import adams.data.report.AbstractField;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.Report;
import adams.data.sampledata.SampleData;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Map.Entry;

/**
 * For converting spectra to JSON and vice versa.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class JsonUtils {

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
    JsonObject		jreport;
    JsonObject		jo;
    Field 		field;
    JsonPrimitive 	prim;

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
   * Turns the spectrum into a json structure (spectral + report).
   *
   * @param spec	the spectrum to convert
   * @return		the json data structure
   */
  public static JsonObject toJson(Spectrum spec) {
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
}
