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
 * Mean.java
 * Copyright (C) 2021 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumfilter.detrend;

import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.statistics.StatUtils;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

/**
 <!-- globalinfo-start -->
 * Performs the correction using simply the mean.
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
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Mean
  extends AbstractDetrend {

  private static final long serialVersionUID = -6754404982002787538L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Performs the correction using simply the mean.";
  }

  /**
   * Corrects the spectrum.
   *
   * @param data 	the spectrum to process
   * @return		the processed spectrum
   */
  @Override
  public Spectrum correct(Spectrum data) {
    Spectrum		result;
    TDoubleList		y;
    int			i;
    double		mean;
    SpectrumPoint 	point;

    // create copy of spectrum
    result = (Spectrum) data.getClone();

    // iterate ranges
    y = new TDoubleArrayList();
    for (i = 0; i < data.size(); i++)
      y.add(data.toList().get(i).getAmplitude());

    // calculate mean
    mean = StatUtils.mean(y.toArray());

    // store in report
    result.getReport().setNumericValue("Mean", mean);

    if (isLoggingEnabled())
      getLogger().info(data.getID() + ": mean=" + mean);

    // correct spectrum
    for (i = 0; i < result.size(); i++) {
      point = result.toList().get(i);
      point.setAmplitude((float) (point.getAmplitude() - mean));
    }

    return result;
  }
}
