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
 * SpectrumMinMax.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.QuickInfoHelper;
import adams.flow.core.Token;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.Hashtable;

/**
 <!-- globalinfo-start -->
 * Keeps track of min&#47;max per wave number and outputs these min&#47;max spectra at the specified interval of spectra processed.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;knir.data.spectrum.Spectrum<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;knir.data.spectrum.Spectrum[]<br>
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
 * &nbsp;&nbsp;&nbsp;default: SpectrumMinMax
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
 * &nbsp;&nbsp;&nbsp;If set to true, the flow gets stopped in case this actor encounters an error;
 * &nbsp;&nbsp;&nbsp; useful for critical actors.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-interval &lt;int&gt; (property: interval)
 * &nbsp;&nbsp;&nbsp;The interval (ie number of spectra processed) when to output the min&#47;max
 * &nbsp;&nbsp;&nbsp;spectra.
 * &nbsp;&nbsp;&nbsp;default: 100
 * &nbsp;&nbsp;&nbsp;minimum: 1
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SpectrumMinMax
  extends AbstractTransformer {

  /** for serialization. */
  private static final long serialVersionUID = -3019442578354930841L;

  /** the key for storing the min spectrum in the backup. */
  public final static String BACKUP_MIN = "minimum";

  /** the key for storing the max spectrum in the backup. */
  public final static String BACKUP_MAX = "maximum";

  /** the key for storing the current counter in the backup. */
  public final static String BACKUP_CURRENT = "current";

  /** the interval when to output the min/max spectra. */
  protected int m_Interval;

  /** the current input token counter. */
  protected int m_Current;

  /** the minimum spectrum. */
  protected Spectrum m_Min;

  /** the maximum spectrum. */
  protected Spectrum m_Max;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
	"Keeps track of min/max per wave number and outputs these min/max "
	+ "spectra at the specified interval of spectra processed.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "interval", "interval",
	    100, 1, null);
  }

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();

    m_Min     = null;
    m_Max     = null;
    m_Current = 0;
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    return QuickInfoHelper.toString(this, "interval", m_Interval);
  }

  /**
   * Sets the interval (ie number of spectra processed) when to output
   * the min/max spectra.
   *
   * @param value	the interval
   */
  public void setInterval(int value) {
    m_Interval = value;
    reset();
  }

  /**
   * Returns the interval (ie number of spectra processed) when to output
   * the min/max spectra.
   *
   * @return		the interval
   */
  public int getInterval() {
    return m_Interval;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String intervalTipText() {
    return "The interval (ie number of spectra processed) when to output the min/max spectra.";
  }

  /**
   * Removes entries from the backup.
   */
  @Override
  protected void pruneBackup() {
    super.pruneBackup();
    pruneBackup(BACKUP_MIN);
    pruneBackup(BACKUP_MAX);
    pruneBackup(BACKUP_CURRENT);
  }

  /**
   * Backs up the current state of the actor before update the variables.
   *
   * @return		the backup
   */
  @Override
  protected Hashtable<String,Object> backupState() {
    Hashtable<String,Object>	result;

    result = super.backupState();

    if (m_Min != null)
      result.put(BACKUP_MIN, m_Min);
    if (m_Max != null)
      result.put(BACKUP_MAX, m_Max);
    result.put(BACKUP_CURRENT, m_Current);

    return result;
  }

  /**
   * Restores the state of the actor before the variables got updated.
   *
   * @param state	the backup of the state to restore from
   */
  @Override
  protected void restoreState(Hashtable<String,Object> state) {
    if (state.containsKey(BACKUP_MIN)) {
      m_Min = (Spectrum) state.get(BACKUP_MIN);
      state.remove(BACKUP_MIN);
    }

    if (state.containsKey(BACKUP_MAX)) {
      m_Max = (Spectrum) state.get(BACKUP_MAX);
      state.remove(BACKUP_MAX);
    }

    if (state.containsKey(BACKUP_CURRENT)) {
      m_Current = (Integer) state.get(BACKUP_CURRENT);
      state.remove(BACKUP_CURRENT);
    }

    super.restoreState(state);
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		<!-- flow-accepts-start -->knir.data.spectrum.Spectrum.class<!-- flow-accepts-end -->
   */
  @Override
  public Class[] accepts() {
    return new Class[]{Spectrum.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		<!-- flow-generates-start -->knir.data.spectrum.Spectrum[].class<!-- flow-generates-end -->
   */
  @Override
  public Class[] generates() {
    return new Class[]{Spectrum[].class};
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String		result;
    Spectrum		sp;
    SpectrumPoint	pointCur;
    SpectrumPoint	pointMin;
    SpectrumPoint	pointMax;
    int			i;

    result = null;

    sp = (Spectrum) m_InputToken.getPayload();
    m_Current++;

    // update min/max
    if (m_Min == null) {
      m_Min = (Spectrum) sp.getClone();
      m_Min.setID(sp.getID() + "-min");
      m_Max = (Spectrum) sp.getClone();
      m_Max.setID(sp.getID() + "-max");
    }
    else {
      if (sp.size() != m_Min.size()) {
	result = "New spectra has different size to previous ones: " + sp.size() + " != " + m_Min.size();
      }
      else {
	for (i = 0; i < sp.size(); i++) {
	  pointCur = sp.toList().get(i);
	  pointMin = m_Min.toList().get(i);
	  pointMax = m_Max.toList().get(i);
	  if (pointCur.getWaveNumber() != pointMin.getWaveNumber()) {
	    result = "Wave numbers differ at #" + (i+1) + ": " + pointCur.getWaveNumber() + " != " + pointMin.getWaveNumber();
	    break;
	  }
	  pointMin.setAmplitude(Math.min(pointMin.getAmplitude(), pointCur.getAmplitude()));
	  pointMax.setAmplitude(Math.max(pointMax.getAmplitude(), pointCur.getAmplitude()));
	}
      }
    }

    if (result == null) {
      if (m_Current % m_Interval == 0)
	m_OutputToken = new Token(new Spectrum[]{m_Min, m_Max});
    }

    return result;
  }
}
