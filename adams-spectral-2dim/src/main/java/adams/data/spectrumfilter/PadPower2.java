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
 * PadPower2.java
 * Copyright (C) 2015 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.padding.PaddingHelper;
import adams.data.padding.PaddingType;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.statistics.SpectrumStatistic;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Pads the spectrum to a power of 2 number of wave numbers.
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
 * <pre>-padding &lt;ZERO|LAST&gt; (property: paddingType)
 * &nbsp;&nbsp;&nbsp;The type of padding to use.
 * &nbsp;&nbsp;&nbsp;default: ZERO
 * </pre>
 * 
 * <pre>-num-additional &lt;int&gt; (property: numAdditional)
 * &nbsp;&nbsp;&nbsp;The number of additional wave numbers to add.
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 * 
 * <pre>-wave-number-step-size &lt;float&gt; (property: waveNumberStepSize)
 * &nbsp;&nbsp;&nbsp;The step size for the new wave numbers; using average if &lt;= 0.
 * &nbsp;&nbsp;&nbsp;default: -1.0
 * </pre>
 * 
 * <pre>-pad-left &lt;boolean&gt; (property: padLeft)
 * &nbsp;&nbsp;&nbsp;If enabled the spectrum gets padded on the left rather than on the right.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class PadPower2
  extends AbstractFilter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -5581114911009545192L;

  /** the type of padding to use. */
  protected PaddingType m_PaddingType;

  /** the number of additional data points to add. */
  protected int m_NumAdditional;

  /** the step size between wave numbers (<= 0 to use average). */
  protected float m_WaveNumberStepSize;

  /** whether to padd on the left. */
  protected boolean m_PadLeft;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "Pads the spectrum to a power of 2 number of wave numbers.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "padding", "paddingType",
      PaddingType.ZERO);

    m_OptionManager.add(
      "num-additional", "numAdditional",
      0, 0, null);

    m_OptionManager.add(
      "wave-number-step-size", "waveNumberStepSize",
      -1.0f);

    m_OptionManager.add(
      "pad-left", "padLeft",
      false);
  }

  /**
   * Sets the type of padding.
   *
   * @param value 	the type
   */
  public void setPaddingType(PaddingType value) {
    m_PaddingType = value;
    reset();
  }

  /**
   * Returns the type of padding.
   *
   * @return 		the type
   */
  public PaddingType getPaddingType() {
    return m_PaddingType;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String paddingTypeTipText() {
    return "The type of padding to use.";
  }

  /**
   * Sets the number of additional wave numbers to add (beyond the power of 2).
   *
   * @param value 	the number
   */
  public void setNumAdditional(int value) {
    if (value >= 0) {
      m_NumAdditional = value;
      reset();
    }
    else {
      getLogger().warning("Number of additional wave numbers must be >=0, provided: " + value);
    }
  }

  /**
   * Returns the number of additional wave numbers to add (beyond the power of 2).
   *
   * @return 		the number
   */
  public int getNumAdditional() {
    return m_NumAdditional;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String numAdditionalTipText() {
    return "The number of additional wave numbers to add.";
  }

  /**
   * Sets the step size for the additional wave numbers. Uses average if
   * <= 0.
   *
   * @param value 	the step size
   */
  public void setWaveNumberStepSize(float value) {
    m_WaveNumberStepSize = value;
    reset();
  }

  /**
   * Returns the step size for the additional wave numbers. Uses average if
   * <= 0.
   *
   * @return 		the step size
   */
  public float getWaveNumberStepSize() {
    return m_WaveNumberStepSize;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String waveNumberStepSizeTipText() {
    return "The step size for the new wave numbers; using average if <= 0.";
  }

  /**
   * Sets whether to pad on the left rather than on the right.
   *
   * @param value 	true if to pad on the left
   */
  public void setPadLeft(boolean value) {
    m_PadLeft = value;
    reset();
  }

  /**
   * Returns whether to pad on the left rather than on the right.
   *
   * @return 		true if to pad on the left
   */
  public boolean getPadLeft() {
    return m_PadLeft;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String padLeftTipText() {
    return "If enabled the spectrum gets padded on the left rather than on the right.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  @Override
  protected Spectrum processData(Spectrum data) {
    Spectrum 			result;
    List<SpectrumPoint> 	points;
    SpectrumPoint 		newPoint;
    float[]			waves;
    float[]			values;
    int				i;
    float			inc;

    result = data.getHeader();

    // get wave numbers/amplitudes
    points = data.toList();
    waves  = new float[points.size()];
    values = new float[points.size()];
    for (i = 0; i < points.size(); i++) {
      waves[i]  = points.get(i).getWaveNumber();
      values[i] = points.get(i).getAmplitude();
    }

    // pad amplitudes
    if (m_NumAdditional > 0)
      values = PaddingHelper.pad(values, PaddingHelper.nextPowerOf2(values.length) + m_NumAdditional, m_PaddingType, m_PadLeft);
    else
      values = PaddingHelper.padPow2(values, m_PaddingType, m_PadLeft);

    // pad wave numbers
    if (m_WaveNumberStepSize <= 0)
      inc = (float) data.toStatistic().getStatistic(SpectrumStatistic.MEAN_DELTA_WAVE_NUMBER);
    else
      inc = m_WaveNumberStepSize;
    waves = PaddingHelper.pad(waves, values.length, PaddingType.ZERO);
    for (i = data.size(); i < waves.length; i++) {
      if (i > 0)
	waves[i] = waves[i - 1] + inc;
      else
	waves[i] = 0.0f;
    }

    // create new spectrum
    for (i = 0; i < waves.length; i++) {
      newPoint = new SpectrumPoint(waves[i], values[i]);
      result.add(newPoint);
    }

    return result;
  }
}
