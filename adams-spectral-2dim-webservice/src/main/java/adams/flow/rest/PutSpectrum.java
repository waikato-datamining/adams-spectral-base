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
 * PutSpectrum.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.flow.rest;

import adams.data.conversion.JsonToSpectrum;
import adams.data.spectrum.JsonUtils;
import adams.data.spectrum.Spectrum;
import adams.db.SpectrumT;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * REST plugin for uploading spectra.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class PutSpectrum
  extends AbstractRESTPluginWithDatabaseConnection {

  private static final long serialVersionUID = -826056354423201513L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Stores spectra in the database.\n"
      + "Delete any spectrum that already exists with this ID/format beforehand.\n"
      + "Format:\n"
      + JsonUtils.example();
  }

  /**
   * Stores the upload spectrum (in JSON format) in the database.
   *
   * @param id		the sample ID
   * @param format	the format, eg NIR
   * @param content	the spectrum in JSON format
   * @return		the database ID or error message
   */
  @POST
  @Path("/spectrum/put/{id}/{format}")
  public String put(@PathParam("id") String id, @PathParam("format") String format, String content) {
    Spectrum 		sp;
    JsonToSpectrum 	conv;
    String		msg;
    SpectrumT		spt;

    initDatabase();
    conv = new JsonToSpectrum();
    conv.setInput(content);
    msg = conv.convert();
    if (msg == null) {
      sp = (Spectrum) conv.getOutput();
      sp.setID(id);
      sp.setFormat(format);
      spt = SpectrumT.getSingleton(m_DatabaseConnection);
      if (spt.exists(id, format))
        spt.remove(id, format);
      return "" + spt.add(sp);
    }
    else {
      return
	"Failed to parse JSON string:\n"
	  + content
	  + "\n"
	  + "Error message:\n"
	  + msg
	  + "\n"
	  + "Expected format:\n"
	  + JsonUtils.example();
    }
  }
}
