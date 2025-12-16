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
 * Subtract.java
 * Copyright (C) 2018-2025 University of Waikato, Hamilton, New Zealand
 */
package adams.data.multifilter;

import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Subtracts the amplitudes of first spectrum with the amplitudes of the second.
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
 * &nbsp;&nbsp;&nbsp;The formats of the two spectra to divide.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-new-format &lt;java.lang.String&gt; (property: newFormat)
 * &nbsp;&nbsp;&nbsp;The new format to use for the divided spectrum.
 * &nbsp;&nbsp;&nbsp;default: DIV
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class Subtract
  extends AbstractBinaryFormatsBasedMultiSpectrumFilter {

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
	"Subtracts the amplitudes of first spectrum with the amplitudes of the second.";
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String formatsTipText() {
    return "The formats of the two spectra to subtract.";
  }
  /**
   * Returns the default format for the generated spectrum.
   *
   * @return		the default format
   */
  @Override
  protected String getDefaultNewFormat() {
    return "SUB";
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String newFormatTipText() {
    return "The new format to use for the subtracted spectrum.";
  }

  /**
   * Performs the actual filtering of the selected spectra.
   *
   * @param data	the original data to filter
   * @param spectra	the spectra to filter
   * @return		the filtered data, null if failed to generate output
   */
  protected Spectrum processData(MultiSpectrum data, List<Spectrum> spectra) {
    Spectrum		result;
    int			i;
    SpectrumPoint	point0;
    SpectrumPoint	point1;
    List<SpectrumPoint>	points;

    // same size?
    if (spectra.get(0).size() != spectra.get(1).size())
      throw new IllegalStateException(
	"Spectra differ in size: " + spectra.get(0).size() + " != " + spectra.get(1).size());

    // add
    result = spectra.get(0).getHeader();
    result.setID(data.getID());
    result.setFormat(m_NewFormat);
    points = new ArrayList<>();
    for (i = 0; i < spectra.get(0).size(); i++) {
      point0 = spectra.get(0).toList().get(i);
      point1 = spectra.get(1).toList().get(i);
      points.add(
	new SpectrumPoint(
	  point0.getWaveNumber(), point0.getAmplitude() - point1.getAmplitude()));
    }
    result.replaceAll(points, true);

    return result;
  }
}
