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

/**
 * AbstractBinaryFormatsBasedMultiSpectrumFilter.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package adams.data.multifilter;

import adams.core.Utils;
import adams.core.base.BaseString;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Ancestor for formats-based filters that require exactly two spectra.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractBinaryFormatsBasedMultiSpectrumFilter
  extends AbstractFormatsBasedMultiSpectrumFilter{

  private static final long serialVersionUID = -5574206537717377027L;

  /**
   * The default implementation only checks whether there is any data set.
   *
   * @param data	the data to filter
   */
  @Override
  protected void checkData(MultiSpectrum data) {
    super.checkData(data);

    if ((m_Formats.length > 0) && (m_Formats.length != 2))
      throw new IllegalStateException("Two formats required if specified, found: " + m_Formats.length);
  }

  /**
   * Performs the actual filtering of the selected spectra.
   *
   * @param data	the original data to filter
   * @param spectra	the spectra to filter
   * @return		the filtered data, null if failed to generate output
   */
  protected abstract Spectrum processData(MultiSpectrum data, List<Spectrum> spectra);

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data, null if failed to generate output
   */
  @Override
  protected Spectrum processData(MultiSpectrum data) {
    HashSet<String> 	formats;
    HashSet<String>	found;
    List<Spectrum> 	spectra;

    // get spectra to compute atan2 for
    spectra = new ArrayList<Spectrum>();
    formats = new HashSet<String>();
    found   = new HashSet<String>();
    if (m_Formats.length > 0) {
      for (BaseString format : m_Formats)
	formats.add(format.getValue());
      for (Spectrum sp : data) {
	if (formats.contains(sp.getFormat())) {
	  spectra.add(sp);
	  found.add(sp.getFormat());
	}
      }
    }
    else {
      spectra.addAll(data);
    }

    // spectra found?
    if (m_Formats.length > 0) {
      if (found.size() != m_Formats.length)
	throw new IllegalStateException(
	  "Failed to find all spectra: "
	    + "required=" + Utils.flatten(m_Formats, ",") + ", "
	    + "found=" + Utils.flatten(found.toArray(), ","));
    }
    else {
      if (spectra.size() != 2)
	throw new IllegalStateException(
	  "Incorrect number of spectra: "
	    + "required=2" + ", "
	    + "found=" + spectra.size());
    }

    return processData(data, spectra);
  }
}
