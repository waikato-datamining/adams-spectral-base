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
 * ArrayToSpectrum.java
 * Copyright (C) 2020 University of Waikato, Hamilton, NZ
 */

package adams.data.conversion;

import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

/**
 <!-- globalinfo-start -->
 * Converts a float array representing amplitudes into a spectrum.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-id &lt;java.lang.String&gt; (property: ID)
 * &nbsp;&nbsp;&nbsp;The sample ID to use.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-format &lt;java.lang.String&gt; (property: format)
 * &nbsp;&nbsp;&nbsp;The format to use.
 * &nbsp;&nbsp;&nbsp;default: NIR
 * </pre>
 *
 * <pre>-waveno-start &lt;float&gt; (property: waveNoStart)
 * &nbsp;&nbsp;&nbsp;The first wave number to use.
 * &nbsp;&nbsp;&nbsp;default: 1550.0
 * </pre>
 *
 * <pre>-waveno-inc &lt;float&gt; (property: waveNoInc)
 * &nbsp;&nbsp;&nbsp;The increment to use between wave numbers.
 * &nbsp;&nbsp;&nbsp;default: 2.0
 * &nbsp;&nbsp;&nbsp;minimum: 0.0
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ArrayToSpectrum
  extends AbstractConversion {

  private static final long serialVersionUID = 6699402711369780235L;

  /** the sample id. */
  protected String m_ID;

  /** the format. */
  protected String m_Format;

  /** the starting wave number. */
  protected float m_WaveNoStart;

  /** the wave number increment. */
  protected float m_WaveNoInc;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Converts a float array representing amplitudes into a spectrum.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "id", "ID",
      "");

    m_OptionManager.add(
      "format", "format",
      SampleData.DEFAULT_FORMAT);

    m_OptionManager.add(
      "waveno-start", "waveNoStart",
      1550.0f);

    m_OptionManager.add(
      "waveno-inc", "waveNoInc",
      2.0f, 0.0f, null);
  }

  /**
   * Sets the sample ID.
   *
   * @param value	the sample ID
   */
  public void setID(String value) {
    m_ID = value;
    reset();
  }

  /**
   * Returns the sample ID.
   *
   * @return 		the sample ID
   */
  public String getID() {
    return m_ID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String IDTipText() {
    return "The sample ID to use.";
  }

  /**
   * Sets the format.
   *
   * @param value	the format
   */
  public void setFormat(String value) {
    m_Format = value;
    reset();
  }

  /**
   * Returns the format.
   *
   * @return 		the format
   */
  public String getFormat() {
    return m_Format;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String formatTipText() {
    return "The format to use.";
  }

  /**
   * Sets the first wave number to use.
   *
   * @param value	the wave no
   */
  public void setWaveNoStart(float value) {
    if (getOptionManager().isValid("waveNoStart", value)) {
      m_WaveNoStart = value;
      reset();
    }
  }

  /**
   * Returns the first wave number to use.
   *
   * @return 		the wave no
   */
  public float getWaveNoStart() {
    return m_WaveNoStart;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String waveNoStartTipText() {
    return "The first wave number to use.";
  }

  /**
   * Sets the increment to use between wave numbers.
   *
   * @param value	the increment
   */
  public void setWaveNoInc(float value) {
    if (getOptionManager().isValid("waveNoInc", value)) {
      m_WaveNoInc = value;
      reset();
    }
  }

  /**
   * Returns the increment to use between wave numbers.
   *
   * @return 		the increment
   */
  public float getWaveNoInc() {
    return m_WaveNoInc;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String waveNoIncTipText() {
    return "The increment to use between wave numbers.";
  }

  /**
   * Returns the class that is accepted as input.
   *
   * @return		the class
   */
  @Override
  public Class accepts() {
    return float[].class;
  }

  /**
   * Returns the class that is generated as output.
   *
   * @return		the class
   */
  @Override
  public Class generates() {
    return Spectrum.class;
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  @Override
  protected Object doConvert() throws Exception {
    float[]	input;
    Spectrum	result;
    float	waveno;
    int		i;

    input = (float[]) m_Input;
    result = new Spectrum();
    result.setID(m_ID);
    result.setFormat(m_Format);
    waveno = m_WaveNoStart;
    for (i = 0; i < input.length; i++) {
      result.add(new SpectrumPoint(waveno, input[i]));
      waveno += m_WaveNoInc;
    }

    return result;
  }
}
