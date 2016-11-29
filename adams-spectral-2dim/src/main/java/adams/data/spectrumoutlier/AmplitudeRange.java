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
 * AmplitudeRange.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumoutlier;

import adams.data.outlier.AbstractOutlierDetector;
import adams.data.outlier.TrainableOutlierDetector;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import gnu.trove.map.hash.TFloatFloatHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * For each wave number in the training data the range (min&#47;max) is calculated.<br>
 * After training, if a spectrum falls outside these ranges, it gets flagged as an outlier.<br>
 * NB: Assumes the spectra (training and subsequent) have the same wave numbers.
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
 * @version $Revision$
 */
public class AmplitudeRange
  extends AbstractOutlierDetector<Spectrum>
  implements TrainableOutlierDetector<Spectrum> {

  private static final long serialVersionUID = 7717702676725599967L;

  /** the minimum. */
  protected TFloatFloatHashMap m_Min;

  /** the maximum. */
  protected TFloatFloatHashMap m_Max;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "For each wave number in the training data the range (min/max) is calculated.\n"
	+ "After training, if a spectrum falls outside these ranges, it gets flagged as "
	+ "an outlier.\n"
	+ "NB: Assumes the spectra (training and subsequent) have the same wave numbers.";
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_Min = new TFloatFloatHashMap();
    m_Max = new TFloatFloatHashMap();
  }

  /**
   * Resets the detector, i.e., flags it as "not trained".
   *
   * @see		#isTrained()
   */
  @Override
  public void resetDetector() {
    m_Min.clear();
    m_Max.clear();
  }

  /**
   * Returns whether the detector has been trained already and is ready to use.
   *
   * @return		true if already trained
   */
  @Override
  public boolean isTrained() {
    return (m_Min.size() > 0) && (m_Max.size() > 0);
  }

  /**
   * Trains the detector with the specified data.
   */
  @Override
  public void trainDetector(Spectrum[] data) {
    m_Min.clear();
    m_Max.clear();
    for (Spectrum spec: data) {
      for (SpectrumPoint point: spec) {
	if (    !m_Min.containsKey(point.getWaveNumber())
	  || (m_Min.get(point.getWaveNumber()) > point.getAmplitude()))
	  m_Min.put(point.getWaveNumber(), point.getAmplitude());
	if (    !m_Max.containsKey(point.getWaveNumber())
	  || (m_Max.get(point.getWaveNumber()) < point.getAmplitude()))
	  m_Max.put(point.getWaveNumber(), point.getAmplitude());
      }
    }
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

    result = new ArrayList<>();

    for (SpectrumPoint point: data) {
      if (    m_Min.containsKey(point.getWaveNumber())
	&& (m_Min.get(point.getWaveNumber()) > point.getAmplitude()))
	result.add("Amplitude at " + point.getWaveNumber() + " below minimum of " + m_Min.get(point.getWaveNumber()) + ": " + point.getAmplitude());
      if (    m_Max.containsKey(point.getWaveNumber())
	&& (m_Max.get(point.getWaveNumber()) < point.getAmplitude()))
	result.add("Amplitude at " + point.getWaveNumber() + " above maximum of " + m_Max.get(point.getWaveNumber()) + ": " + point.getAmplitude());
      if (result.size() > 0)
	break;
    }

    return result;
  }
}
