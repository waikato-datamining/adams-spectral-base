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
 * Median.java
 * Copyright (C) 2019-2025 University of Waikato, Hamilton, New Zealand
 */
package adams.data.multifilter;

import adams.core.base.BaseString;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.statistics.StatUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Averages spectra into a single one using the median. Either uses all spectra, if no format is specified, or only the ones that match the specified formats.<br>
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-no-id-update &lt;boolean&gt; (property: dontUpdateID)
 * &nbsp;&nbsp;&nbsp;If enabled, suppresses updating the ID of adams.data.id.IDHandler data containers.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-format &lt;adams.core.base.BaseString&gt; [-format ...] (property: formats)
 * &nbsp;&nbsp;&nbsp;The formats of the spectra to average; use an empty array to average all.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-new-format &lt;java.lang.String&gt; (property: newFormat)
 * &nbsp;&nbsp;&nbsp;The new format to use for the averaged spectrum.
 * &nbsp;&nbsp;&nbsp;default: AVG
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class Median
  extends AbstractFormatsBasedMultiSpectrumFilter {

  /** for serialization. */
  private static final long serialVersionUID = 396771111601239664L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return 
	"Averages spectra into a single one using the median. Either uses all spectra, if no "
	+ "format is specified, or only the ones that match the specified "
	+ "formats.";
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String formatsTipText() {
    return "The formats of the spectra to average; use an empty array to average all.";
  }

  /**
   * Returns the default format for the generated spectrum.
   *
   * @return		the default format
   */
  @Override
  protected String getDefaultNewFormat() {
    return "MEDIAN";
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String newFormatTipText() {
    return "The new format to use for the averaged spectrum.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data, null if failed to generate output
   */
  @Override
  protected Spectrum processData(MultiSpectrum data) {
    Spectrum		result;
    HashSet<String>	formats;
    List<Spectrum>	spectra;
    int			i;
    int			n;
    double[]		values;
    List<SpectrumPoint>	points;

    result = null;

    // get spectra to average
    spectra = new ArrayList<>();
    if (m_Formats.length > 0) {
      formats = new HashSet<>();
      for (BaseString format: m_Formats)
	formats.add(format.getValue());
      for (Spectrum sp: data) {
	if (formats.contains(sp.getFormat()))
	  spectra.add(sp);
      }
    }
    else {
      spectra.addAll(data);
    }

    // average
    if (spectra.size() > 1) {
      result = spectra.get(0).getHeader();
      result.setID(data.getID());
      result.setFormat(m_NewFormat);
      values = new double[spectra.size()];
      points = new ArrayList<>();
      for (i = 0; i < spectra.get(0).size(); i++) {
	for (n = 0; n < spectra.size(); n++)
	  values[n] = spectra.get(n).toList().get(i).getAmplitude();
	points.add(
	    new SpectrumPoint(
		spectra.get(0).toList().get(i).getWaveNumber(), 
		(float) StatUtils.median(values)));
      }
      result.replaceAll(points, true);
    }
    else if (spectra.size() == 1) {
      result = spectra.get(0);
    }
    
    return result;
  }
}
