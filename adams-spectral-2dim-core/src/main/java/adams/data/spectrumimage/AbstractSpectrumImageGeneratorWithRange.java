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
 * AbstractSpectrumImageGeneratorWithRange.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumimage;

import adams.data.spectrum.Spectrum;

/**
 * Ancestor for spectrum image generators that limit the amplitude ranges.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractSpectrumImageGeneratorWithRange
  extends AbstractSpectrumImageGenerator {

  private static final long serialVersionUID = -2796398928812431488L;

  /** the minimum amplitude to use. */
  protected float m_MinAmplitude;

  /** the maximum amplitude to use. */
  protected float m_MaxAmplitude;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "min-amplitude", "minAmplitude",
      0.0f);

    m_OptionManager.add(
      "max-amplitude", "maxAmplitude",
      1000.0f);
  }

  /**
   * Sets the minimum amplitude to assume.
   *
   * @param value	the minimum
   */
  public void setMinAmplitude(float value) {
    if (getOptionManager().isValid("minAmplitude", value)) {
      m_MinAmplitude = value;
      reset();
    }
  }

  /**
   * Returns the minimum amplitude to assume.
   *
   * @return		the minimum
   */
  public float getMinAmplitude() {
    return m_MinAmplitude;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String minAmplitudeTipText() {
    return "The minimum amplitude to assume; amplitudes below get set to this value.";
  }

  /**
   * Sets the maximum amplitude to assume.
   *
   * @param value	the maximum
   */
  public void setMaxAmplitude(float value) {
    if (getOptionManager().isValid("maxAmplitude", value)) {
      m_MaxAmplitude = value;
      reset();
    }
  }

  /**
   * Returns the maximum amplitude to assume.
   *
   * @return		the maximum
   */
  public float getMaxAmplitude() {
    return m_MaxAmplitude;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String maxAmplitudeTipText() {
    return "The maximum amplitude to assume; amplitudes below get set to this value.";
  }

  /**
   * Hook method for checks before generating the image.
   *
   * @param spectrum	the spectrum to check
   * @return		null if successful, otherwise the error message
   */
  @Override
  protected String check(Spectrum spectrum) {
    String	result;

    result = super.check(spectrum);
    if (result != null)
      return result;

    if (m_MaxAmplitude <= m_MinAmplitude)
      return "max amplitude must be greater than min amplitude: min=" + m_MinAmplitude + ", max=" + m_MaxAmplitude;

    return null;
  }

  /**
   * Makes sure that the amplitude is within the defined ranges.
   *
   * @param ampl	the amplitude to process
   * @return		the (potentially) updated amplitude
   */
  protected float rangeCheck(float ampl) {
    ampl = Math.max(ampl, m_MinAmplitude);
    ampl = Math.min(ampl, m_MaxAmplitude);
    return ampl;
  }
}
