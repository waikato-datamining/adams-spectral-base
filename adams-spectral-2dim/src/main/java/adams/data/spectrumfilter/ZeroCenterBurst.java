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
 * ZeroCenterBurst.java
 * Copyright (C) 2015 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Locates the largest burst (negative or positive) and splits the spectrum into the following parts:<br>
 * left, burst, right<br>
 * <br>
 * These parts then get reassembled as:<br>
 * right, left, burst
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
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class ZeroCenterBurst
  extends AbstractFilter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 2319957467336388607L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return
      "Locates the largest burst (negative or positive) and splits the spectrum "
	+ "into the following parts:\n"
	+ "left, burst, right\n"
	+ "\n"
	+ "These parts then get reassembled as:\n"
	+ "right, left, burst";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  protected Spectrum processData(Spectrum data) {
    Spectrum			result;
    List<SpectrumPoint> 	points;
    int				i;
    int				burstIndex;
    float			burstValue;
    int				n;
    float			wave;
    float			ampl;

    result = data.getHeader();
    if (data.size() == 0)
      return result;

    points = data.toList();

    // find burst
    burstIndex = 0;
    burstValue = Math.abs(points.get(0).getAmplitude());
    for (i = 1; i < points.size(); i++) {
      ampl = Math.abs(points.get(i).getAmplitude());
      if (burstValue < ampl) {
	burstIndex = i;
	burstValue = ampl;
      }
    }

    // split and swap
    for (i = 0; i < points.size(); i++) {
      n    = (i <= burstIndex) ? i + (points.size() - burstIndex - 1) : i - burstIndex;
      wave = points.get(i).getWaveNumber();
      ampl = points.get(n).getAmplitude();
      result.add(new SpectrumPoint(wave, ampl));
    }

    return result;
  }
}
