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
 * SetSpectrumAmplitude.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer;

import adams.core.Index;
import adams.core.QuickInfoHelper;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spectrum.SpectrumUtils;
import adams.flow.core.Token;

/**
 <!-- globalinfo-start -->
 * Sets the amplitude for the specified wave number.<br>
 * Can either be used to replace an existing spectrum point or simply to insert another one.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;adams.data.spectrum.Spectrum<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;adams.data.spectrum.Spectrum<br>
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: SetSpectrumAmplitude
 * </pre>
 *
 * <pre>-annotation &lt;adams.core.base.BaseAnnotation&gt; (property: annotations)
 * &nbsp;&nbsp;&nbsp;The annotations to attach to this actor.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-skip &lt;boolean&gt; (property: skip)
 * &nbsp;&nbsp;&nbsp;If set to true, transformation is skipped and the input token is just forwarded
 * &nbsp;&nbsp;&nbsp;as it is.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-stop-flow-on-error &lt;boolean&gt; (property: stopFlowOnError)
 * &nbsp;&nbsp;&nbsp;If set to true, the flow execution at this level gets stopped in case this
 * &nbsp;&nbsp;&nbsp;actor encounters an error; the error gets propagated; useful for critical
 * &nbsp;&nbsp;&nbsp;actors.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-silent &lt;boolean&gt; (property: silent)
 * &nbsp;&nbsp;&nbsp;If enabled, then no errors are output in the console; Note: the enclosing
 * &nbsp;&nbsp;&nbsp;actor handler must have this enabled as well.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-no-copy &lt;boolean&gt; (property: noCopy)
 * &nbsp;&nbsp;&nbsp;If enabled, no copy of the spectrum is created before processing it.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-type &lt;INDEX|WAVE_NUMBER|WAVE_NUMBER_CLOSEST|INSERT&gt; (property: type)
 * &nbsp;&nbsp;&nbsp;How to set the amplitude.
 * &nbsp;&nbsp;&nbsp;default: INDEX
 * </pre>
 *
 * <pre>-index &lt;adams.core.Index&gt; (property: index)
 * &nbsp;&nbsp;&nbsp;The index of the amplitude to set.
 * &nbsp;&nbsp;&nbsp;default: first
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 *
 * <pre>-wave-number &lt;float&gt; (property: waveNumber)
 * &nbsp;&nbsp;&nbsp;The wave number of the amplitude to set.
 * &nbsp;&nbsp;&nbsp;default: 0.0
 * </pre>
 *
 * <pre>-amplitude &lt;float&gt; (property: amplitude)
 * &nbsp;&nbsp;&nbsp;The amplitude to set.
 * &nbsp;&nbsp;&nbsp;default: 0.0
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SetSpectrumAmplitude
  extends AbstractInPlaceSpectrumTransformer {

  private static final long serialVersionUID = 5652640626092283192L;

  /**
   * Defines how to set the amplitude.
   */
  public enum UpdateType {
    INDEX,
    WAVE_NUMBER,
    WAVE_NUMBER_CLOSEST,
    INSERT,
  }

  /** how to retrieve the amplitude. */
  protected UpdateType m_Type;

  /** the index. */
  protected Index m_Index;

  /** the wave number. */
  protected float m_WaveNumber;

  /** the amplitude. */
  protected float m_Amplitude;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Sets the amplitude for the specified wave number.\n"
      + "Can either be used to replace an existing spectrum point or simply to insert another one.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "type", "type",
      UpdateType.INDEX);

    m_OptionManager.add(
      "index", "index",
      new Index(Index.FIRST));

    m_OptionManager.add(
      "wave-number", "waveNumber",
      0.0f);

    m_OptionManager.add(
      "amplitude", "amplitude",
      0.0f);
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result = QuickInfoHelper.toString(this, "type", m_Type, "type: ");
    result += QuickInfoHelper.toString(this, "index", m_Index, ", index: ");
    result += QuickInfoHelper.toString(this, "waveNumber", m_WaveNumber, ", wave: ");
    result += QuickInfoHelper.toString(this, "amplitude", m_Amplitude, ", ampl: ");
    result += QuickInfoHelper.toString(this, "noCopy", m_NoCopy, "no copy", ", ");

    return result;
  }

  /**
   * Sets how to retrieve the amplitude.
   *
   * @param value	the type
   */
  public void setType(UpdateType value) {
    m_Type = value;
    reset();
  }

  /**
   * Returns how to retrieve the amplitude.
   *
   * @return		the type
   */
  public UpdateType getType() {
    return m_Type;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String typeTipText() {
    return "How to set the amplitude.";
  }

  /**
   * Sets the index of the amplitude to set.
   *
   * @param value	the index
   */
  public void setIndex(Index value) {
    m_Index = value;
    reset();
  }

  /**
   * Returns the index of the amplitude to set.
   *
   * @return		the index
   */
  public Index getIndex() {
    return m_Index;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String indexTipText() {
    return "The index of the amplitude to set.";
  }

  /**
   * Sets the wave number of the amplitude to set.
   *
   * @param value	the wave number
   */
  public void setWaveNumber(float value) {
    m_WaveNumber = value;
    reset();
  }

  /**
   * Returns the wave number of the amplitude to set.
   *
   * @return		the wave number
   */
  public float getWaveNumber() {
    return m_WaveNumber;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String waveNumberTipText() {
    return "The wave number of the amplitude to set.";
  }

  /**
   * Sets the amplitude to set.
   *
   * @param value	the amplitude
   */
  public void setAmplitude(float value) {
    m_Amplitude = value;
    reset();
  }

  /**
   * Returns the amplitude to set.
   *
   * @return		the amplitude
   */
  public float getAmplitude() {
    return m_Amplitude;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String amplitudeTipText() {
    return "The amplitude to set.";
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String	result;
    Spectrum	sp;
    int		index;

    result = null;
    sp     = m_InputToken.getPayload(Spectrum.class);
    if (!m_NoCopy)
      sp = (Spectrum) sp.getClone();

    switch (m_Type) {
      case INDEX:
        m_Index.setMax(sp.size());
        index = m_Index.getIntIndex();
        if (index == -1) {
          result = "Invalid amplitude index: " + m_Index.getIndex();
        }
        else {
          sp.toList().get(index).setAmplitude(m_Amplitude);
          m_OutputToken = new Token(sp);
        }
        break;

      case WAVE_NUMBER:
	index = SpectrumUtils.findWaveNumber(sp.toList(), m_WaveNumber);
        if (index == -1) {
          result = "Wave number not found: " + m_WaveNumber;
        }
        else {
          sp.toList().get(index).setAmplitude(m_Amplitude);
          m_OutputToken = new Token(sp);
        }
        break;

      case WAVE_NUMBER_CLOSEST:
	index = SpectrumUtils.findClosestWaveNumber(sp.toList(), m_WaveNumber);
        if (index == -1) {
	  result = "Wave number (closest) not found: " + m_WaveNumber;
	}
        else {
          sp.toList().get(index).setAmplitude(m_Amplitude);
	  m_OutputToken = new Token(sp);
	}
        break;

      case INSERT:
        sp.add(new SpectrumPoint(m_WaveNumber, m_Amplitude));
	m_OutputToken = new Token(sp);
        break;

      default:
        result = "Unhandled retrieval type: " + m_Type;
    }

    return result;
  }
}
