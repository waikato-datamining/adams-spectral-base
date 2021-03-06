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
 * DeleteSpectrum.java
 * Copyright (C) 2018-2019 University of Waikato, Hamilton, NZ
 */

package adams.flow.rest;

import adams.db.SpectrumF;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * REST plugin for deleting spectra.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class DeleteSpectrum
  extends AbstractRESTPluginWithDatabaseConnection {

  private static final long serialVersionUID = -826056354423201513L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Deletes spectra via their ID and format.";
  }

  /**
   * Deletes the specified spectrum.
   *
   * @param id		the sample ID
   * @param format	the format, eg NIR
   * @return		true/false if deletion was successful, or message if spectrum did not exist
   */
  @GET
  @Path("/spectrum/delete/{id}/{format}")
  @Produces("text/plain")
  public String delete(@PathParam("id") String id, @PathParam("format") String format) {
    SpectrumF spt;

    initDatabase();
    spt = SpectrumF.getSingleton(m_DatabaseConnection);
    if (spt.exists(id, format))
      return "" + spt.remove(id, format, false);
    else
      return "Spectrum " + id + "/" + format + " does not exist!";
  }
}
