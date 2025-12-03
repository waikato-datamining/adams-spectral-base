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
 * Rebase.java
 * Copyright (C) 2012-2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Shifts all wave numbers (left or right), so that the first wave number is at the specified starting point.
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
 * <pre>-start &lt;float&gt; (property: start)
 * &nbsp;&nbsp;&nbsp;The new starting point for the wave numbers.
 * &nbsp;&nbsp;&nbsp;default: 0.0
 * </pre>
 *
 * <pre>-update-wave-numbers &lt;boolean&gt; (property: updateWaveNumbers)
 * &nbsp;&nbsp;&nbsp;If enabled, the wave numbers get updated using the specified step size.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-wave-step &lt;float&gt; (property: waveStep)
 * &nbsp;&nbsp;&nbsp;The difference between two wave numbers when updating the wave numbers.
 * &nbsp;&nbsp;&nbsp;default: 1.0
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class Rebase
  extends AbstractFilter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 2319957467336388607L;

  /** the new starting point for wave numbers. */
  protected float m_Start;

  /** whether to introduce new step size between points. */
  protected boolean m_UpdateWaveNumbers;

  /** the difference between two wave numbers. */
  protected float m_WaveStep;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "Shifts all wave numbers (left or right), so that the first wave "
	+ "number is at the specified starting point.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "start", "start",
      0.0f);

    m_OptionManager.add(
      "update-wave-numbers", "updateWaveNumbers",
      false);

    m_OptionManager.add(
      "wave-step", "waveStep",
      1.0f);
  }

  /**
   * Sets the new starting point for the wave numbers.
   *
   * @param value 	the start
   */
  public void setStart(float value) {
    m_Start = value;
    reset();
  }

  /**
   * Returns the new starting point for the wave numbers.
   *
   * @return 		the start
   */
  public float getStart() {
    return m_Start;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String startTipText() {
    return "The new starting point for the wave numbers.";
  }

  /**
   * Sets whether to update the wave numbers using the specified step size.
   *
   * @param value 	true if to update
   */
  public void setUpdateWaveNumbers(boolean value) {
    m_UpdateWaveNumbers = value;
    reset();
  }

  /**
   * Returns whether to update the wave numbers using the specified step size.
   *
   * @return 		true if to update
   */
  public boolean getUpdateWaveNumbers() {
    return m_UpdateWaveNumbers;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String updateWaveNumbersTipText() {
    return "If enabled, the wave numbers get updated using the specified step size.";
  }

  /**
   * Sets the difference between two wave numbers when updating them.
   *
   * @param value 	the difference
   */
  public void setWaveStep(float value) {
    m_WaveStep = value;
    reset();
  }

  /**
   * Returns the difference between two wave numbers when updating them.
   *
   * @return 		the difference
   */
  public float getWaveStep() {
    return m_WaveStep;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String waveStepTipText() {
    return "The difference between two wave numbers when updating the wave numbers.";
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
    List<SpectrumPoint> pointsNew;
    int			i;
    float		diff;
    SpectrumPoint	point;

    result    = data.getHeader();
    points    = data.toList();
    pointsNew = new ArrayList<>();
    if (!points.isEmpty()) {
      if (m_UpdateWaveNumbers) {
	for (i = 0; i < points.size(); i++) {
	  point = points.get(i);
	  pointsNew.add(
	    new SpectrumPoint(
	      m_Start + i * m_WaveStep,
	      point.getAmplitude()));
	}
      }
      else {
	diff = m_Start - points.get(0).getWaveNumber();
	if (isLoggingEnabled())
	  getLogger().info("Difference: " + diff + " (= shifting " + ((diff < 0) ? "left" : "right") + ")");
	for (i = 0; i < points.size(); i++) {
	  point = points.get(i);
	  pointsNew.add(
	    new SpectrumPoint(
	      point.getWaveNumber() + diff,
	      point.getAmplitude()));
	}
      }
    }
    result.addAll(pointsNew);

    return result;
  }
}
