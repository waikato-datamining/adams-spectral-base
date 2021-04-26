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
 * Flatliner.java
 * Copyright (C) 2021 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumoutlier;

import adams.data.outlier.AbstractOutlierDetector;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.statistics.StatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Checks for spectra that consist only of the same value (aka flat-liners).
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  FracPete (fracpete at waikato dot ac dot nz)
 */
public class Flatliner
  extends AbstractOutlierDetector<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -1701525249098341015L;
  
  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Checks for spectra that consist only of the same value (aka flat-liners).";
  }

  /**
   * Performs the actual detection.
   *
   * @param data	the data to process
   * @return		the detections
   */
  @Override
  protected List<String> processData(Spectrum data) {
    List<String>	result;
    List<SpectrumPoint>	points;
    String		msg;
    int			i;
    float[]		ampl;
    float[]		unique;

    result = new ArrayList<>();

    points = data.toList();
    ampl   = new float[points.size()];;
    for (i = 0; i < points.size(); i++)
      ampl[i] = points.get(i).getAmplitude();

    unique = StatUtils.uniqueValues(ampl);

    if (unique.length == 1) {
      msg = "All amplitudes only have one value: " + unique[0];
      result.add(msg);
      if (isLoggingEnabled())
	getLogger().info(data.getDatabaseID() + " - " + getClass().getName() + ": " + msg);
    }

    return result;
  }
}
