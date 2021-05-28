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
 * TransformSpectrum.java
 * Copyright (C) 2018-2021 University of Waikato, Hamilton, NZ
 */

package adams.flow.rest;

import adams.data.conversion.JsonToSpectrum;
import adams.data.conversion.SpectrumToJson;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumJsonUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * REST plugin for filtering spectra.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class TransformSpectrum
  extends AbstractRESTPluginWithDatabaseConnection {

  private static final long serialVersionUID = -826056354423201513L;

  /** the filter to apply. */
  protected adams.data.filter.Filter m_Filter;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Filters spectra with the specified filter.\n"
      + "Format:\n"
      + "- complete report stored:\n"
      + SpectrumJsonUtils.example(false) + "\n"
      + "- specific reference and meta-data values stored:\n"
      + SpectrumJsonUtils.example(true);
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "filter", "filter",
      new adams.data.filter.PassThrough());
  }

  /**
   * Sets the filter to use.
   *
   * @param value	the filter
   */
  public void setFilter(adams.data.filter.Filter value) {
    m_Filter = value;
    reset();
  }

  /**
   * Returns the filter in use.
   *
   * @return		the filter
   */
  public adams.data.filter.Filter getFilter() {
    return m_Filter;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String filterTipText() {
    return "The filter to use for filtering the data.";
  }

  /**
   * Filters the upload spectrum (in JSON format) with the filter and returns
   * the output of the filter again (in JSON format).
   *
   * @param id		the sample ID
   * @param format	the format, eg NIR
   * @param content	the spectrum in JSON format
   * @return		the spectrum or error message
   */
  @POST
  @Path("/spectrum/transform/{id}/{format}")
  @Consumes("text/json")
  @Produces("text/json")
  public String put(@PathParam("id") String id, @PathParam("format") String format, String content) {
    Spectrum 		sp;
    Spectrum		filtered;
    JsonToSpectrum 	j2s;
    SpectrumToJson	s2j;
    String		msg;

    initDatabase();
    j2s = new JsonToSpectrum();
    j2s.setInput(content);
    msg = j2s.convert();
    if (msg == null) {
      sp = (Spectrum) j2s.getOutput();
      sp.setID(id);
      sp.setFormat(format);
      filtered = (Spectrum) m_Filter.filter(sp);
      if (filtered == null)
        return "Failed to filter spectrum with: " + m_Filter.toCommandLine();
      s2j = new SpectrumToJson();
      s2j.setInput(filtered);
      msg = s2j.convert();
      if (msg == null)
	return (String) s2j.getOutput();
      else
        return msg;
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
          + "- complete report stored:\n"
          + SpectrumJsonUtils.example(false) + "\n"
          + "- specific reference and meta-data values stored:\n"
          + SpectrumJsonUtils.example(true);
    }
  }
}
