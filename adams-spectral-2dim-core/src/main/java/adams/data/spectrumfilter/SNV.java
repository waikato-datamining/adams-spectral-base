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
 * SNV.java
 * Copyright (C) 2026 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spectrum.SpectrumUtils;
import adams.data.statistics.StatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Standard Normal Variate (SNV) filter.<br>
 * Centers and scales each instance (row) individually by subtracting the row mean and dividing by the row standard deviation.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * &nbsp;&nbsp;&nbsp;min-user-mode: Expert
 * </pre>
 *
 * <pre>-no-id-update &lt;boolean&gt; (property: dontUpdateID)
 * &nbsp;&nbsp;&nbsp;If enabled, suppresses updating the ID of adams.data.id.IDHandler data containers.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-no-processing-info-update &lt;boolean&gt; (property: dontUpdateProcessingInfo)
 * &nbsp;&nbsp;&nbsp;If enabled, suppresses updating the processing information of adams.data.NotesHandler
 * &nbsp;&nbsp;&nbsp;data containers.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SNV
  extends AbstractFilter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 1836858988505886282L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Standard Normal Variate (SNV) filter.\n"
	     + "Centers and scales each instance (row) individually by subtracting the row mean "
	     + "and dividing by the row standard deviation.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  @Override
  protected Spectrum processData(Spectrum data) {
    Spectrum		result;
    List<SpectrumPoint>	points;
    List<SpectrumPoint>	pointsNew;
    double[]		ampls;
    int			i;
    double		sum;
    double		mean;
    double 		sumSqDiff;
    double 		diff;
    double 		stdDev;

    if (data.size() <= 1) {
      getLogger().warning("More than one data points required!");
      return data;
    }

    result = data.getHeader();
    points = data.toList();
    ampls  = SpectrumUtils.toDoubleArray(points);

    // Pass 1: Compute row mean
    sum   = StatUtils.sum(ampls);
    mean  = sum / ampls.length;

    // Pass 2: Compute sample variance and standard deviation
    sumSqDiff = 0.0;
    for (i = 0; i < ampls.length; i++) {
      diff       = ampls[i] - mean;
      sumSqDiff += diff * diff;
    }
    stdDev = Math.sqrt(sumSqDiff / (ampls.length - 1));

    // Pass 3: Center and scale (x - mean) / stdDev
    pointsNew = new ArrayList<>();
    if (stdDev != 0.0) {
      for (i = 0; i < ampls.length; i++) {
	pointsNew.add(
	  new SpectrumPoint(
	    points.get(i).getWaveNumber(),
	    (float) ((ampls[i] - mean) / stdDev)));
      }
    }
    result.replaceAll(pointsNew, true);

    return result;
  }
}
