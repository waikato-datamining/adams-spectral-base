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
 * TrinamixSpectrumJsonReader.java
 * Copyright (C) 2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.UniqueIDs;
import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Level;

/**
 * Reader for the trinamiX (<a href="https://trinamixsensing.com/">https://trinamixsensing.com/</a>)
 * JSON format.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class TrinamixSpectrumJsonReader
  extends AbstractTextBasedSpectrumReader {

  private static final long serialVersionUID = -4783711375310420549L;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reader for the trinamiX (https://trinamixsensing.com/) JSON format.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the
   * file chooser
   */
  @Override
  public String getFormatDescription() {
    return "TrinamiX JSON";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return the extension (without the dot!)
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
    BufferedReader	breader;
    JsonObject 		obj;
    Spectrum		spec;
    String		sampleID;
    String		configID;
    String		useCase;
    String		device;
    int			s;
    int			i;
    JsonArray		spectraList;
    JsonObject		spObj;
    JsonArray		x;
    String		xLabel;
    JsonArray		y;
    String		yLabel;

    breader = null;
    try {
      breader  = new BufferedReader(new StringReader(Utils.flatten(content, "\n")));
      obj      = (JsonObject) JsonParser.parseReader(breader);
      configID = null;
      if (obj.has("deviceConfigIdentity") && !obj.get("deviceConfigIdentity").isJsonNull())
	configID = obj.get("deviceConfigIdentity").getAsString();
      if (obj.has("sampleIdentifier") && !obj.get("sampleIdentifier").isJsonNull()) {
	sampleID = obj.get("sampleIdentifier").getAsString();
      }
      else {
	sampleID = "NA-" + UniqueIDs.nextLong();
	getLogger().warning("Sample ID is null, using dummy one: " + sampleID);
      }
      useCase = null;
      if (obj.has("useCaseIdentity") && !obj.get("useCaseIdentity").isJsonNull())
	useCase = obj.get("useCaseIdentity").getAsString();
      spectraList = obj.getAsJsonArray("spectraList");
      for (s = 0; s < spectraList.size(); s++) {
	spObj  = spectraList.get(s).getAsJsonObject();
	device = null;
	if (spObj.has("device") && !spObj.get("device").isJsonNull())
	  device = spObj.get("device").getAsString();
	x      = spObj.getAsJsonArray("x");
	xLabel = null;
	if (spObj.has("xlabel") && !spObj.get("xlabel").isJsonNull())
	  xLabel = spObj.get("xlabel").getAsString();
	y      = spObj.getAsJsonArray("y");
	yLabel = null;
	if (spObj.has("ylabel") && !spObj.get("ylabel").isJsonNull())
	  yLabel = spObj.get("ylabel").getAsString();
	spec = new Spectrum();
	spec.setID(sampleID);
	if (configID != null)
	  spec.getReport().setStringValue("DeviceConfigIdentity", configID);
	if (useCase != null)
	  spec.getReport().setStringValue("UseCaseIdentity", useCase);
	if (device != null)
	  spec.getReport().setStringValue("Device", device);
	if (xLabel != null)
	  spec.getReport().setStringValue("x-label", xLabel);
	if (yLabel != null)
	  spec.getReport().setStringValue("y-label", yLabel);
	for (i = 0; i < x.size(); i++)
	  spec.add(new SpectrumPoint(x.get(i).getAsFloat(), y.get(i).getAsFloat()));
	m_ReadData.add(spec);
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to read: " + m_Input, e);
    }
    finally {
      FileUtils.closeQuietly(breader);
    }
  }
}
