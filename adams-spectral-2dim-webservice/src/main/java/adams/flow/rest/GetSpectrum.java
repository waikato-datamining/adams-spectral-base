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
 * GetSpectrum.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.flow.rest;

import adams.data.conversion.SpectrumToJson;
import adams.data.spectrum.JsonUtils;
import adams.data.spectrum.Spectrum;
import adams.db.SpectrumT;
import com.google.gson.JsonObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * REST plugin for retrieving spectra.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class GetSpectrum
  extends AbstractRESTPluginWithDatabaseConnection {

  private static final long serialVersionUID = -826056354423201513L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Retrieves spectra via ID and format.\n"
      + "Format:\n"
      + JsonUtils.example();
  }

  /**
   * Retrieves a spectrum and returns it as JSON.
   *
   * @param id		the sample ID
   * @param format	the format, eg NIR
   * @return		the spectrum in JSON
   */
  @GET
  @Path("/spectrum/get/{id}/{format}")
  @Produces("text/json")
  public String get(@PathParam("id") String id, @PathParam("format") String format) {
    Spectrum 		sp;
    JsonObject 		json;
    SpectrumToJson	conv;
    String		msg;

    initDatabase();
    sp = SpectrumT.getSingleton(m_DatabaseConnection).load(id, format);
    if (sp == null) {
      json = new JsonObject();
      json.addProperty("message", "not found");
      return json.toString();
    }
    else {
      conv = new SpectrumToJson();
      conv.setInput(sp);
      msg = conv.convert();
      if (msg == null) {
        return (String) conv.getOutput();
      }
      else {
	json = new JsonObject();
	json.addProperty("message", msg);
	return json.toString();
      }
    }
  }
}
