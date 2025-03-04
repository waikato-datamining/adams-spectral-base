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
 * PredictionCheck.java
 * Copyright (C) 2017-2025 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.QuickInfoHelper;
import adams.data.InPlaceProcessing;
import adams.flow.container.EvaluationContainer;
import adams.flow.core.FlowContextUtils;
import adams.flow.core.Token;
import adams.flow.transformer.predictioncheck.AbstractPredictionCheck;
import adams.flow.transformer.predictioncheck.PassThrough;

/**
 <!-- globalinfo-start -->
 * Applies the specified check scheme to the incoming evaluation container.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;adams.flow.container.EvaluationContainer<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;adams.flow.container.EvaluationContainer<br>
 * <br><br>
 * Container information:<br>
 * - adams.flow.container.EvaluationContainer: Instance, Instances, Evaluations, Evaluator, Classification, Abstention classification, Distribution, Component, Version, ID
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
 * &nbsp;&nbsp;&nbsp;default: PredictionCheck
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
 * <pre>-check &lt;adams.flow.transformer.predictioncheck.AbstractPredictionCheck&gt; (property: check)
 * &nbsp;&nbsp;&nbsp;The prediction check scheme to use.
 * &nbsp;&nbsp;&nbsp;default: adams.flow.transformer.predictioncheck.PassThrough
 * </pre>
 *
 * <pre>-no-copy &lt;boolean&gt; (property: noCopy)
 * &nbsp;&nbsp;&nbsp;If enabled, no copy of the evaluation container is generated before passing
 * &nbsp;&nbsp;&nbsp;it to the check scheme.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class PredictionCheck
  extends AbstractTransformer
  implements InPlaceProcessing {

  /** for serialization. */
  private static final long serialVersionUID = 4523798891781897832L;

  /** the check scheme to use. */
  protected AbstractPredictionCheck m_Check;

  /** whether to only update the container or work on a copy. */
  protected boolean m_NoCopy;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Applies the specified check scheme to the incoming evaluation container.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "check", "check",
      new PassThrough());

    m_OptionManager.add(
      "no-copy", "noCopy",
      false);
  }

  /**
   * Sets the check to use.
   *
   * @param value	the check
   */
  public void setCheck(AbstractPredictionCheck value) {
    m_Check = value;
    reset();
  }

  /**
   * Returns the check in use.
   *
   * @return		the check
   */
  public AbstractPredictionCheck getCheck() {
    return m_Check;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String checkTipText() {
    return "The prediction check scheme to use.";
  }

  /**
   * Sets whether to skip creating a copy of the data before processing it.
   *
   * @param value	true if to skip creating copy
   */
  public void setNoCopy(boolean value) {
    m_NoCopy = value;
    reset();
  }

  /**
   * Returns whether to skip creating a copy of the data before processing t.
   *
   * @return		true if copying is skipped
   */
  public boolean getNoCopy() {
    return m_NoCopy;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String noCopyTipText() {
    return
      "If enabled, no copy of the evaluation container is generated before "
	+ "passing it to the check scheme.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result = QuickInfoHelper.toString(this, "check", m_Check, "check: ");
    result += QuickInfoHelper.toString(this, "noCopy", m_NoCopy, "no copy", ", ");

    return result;
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the accepted classes
   */
  @Override
  public Class[] accepts() {
    return new Class[]{EvaluationContainer.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the generated classes
   */
  @Override
  public Class[] generates() {
    return new Class[]{EvaluationContainer.class};
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String			result;
    EvaluationContainer		cont;
    EvaluationContainer		newCont;

    result = null;

    cont = m_InputToken.getPayload(EvaluationContainer.class);
    try {
      if (!m_NoCopy)
        cont = (EvaluationContainer) cont.getClone();
      if (FlowContextUtils.isHandler(m_Check))
	FlowContextUtils.update(m_Check, this);
      newCont = m_Check.checkPrediction(cont);
      m_OutputToken = new Token(newCont);
    }
    catch (Exception e) {
      m_OutputToken = null;
      result = handleException("Failed to check: " + m_InputToken.getPayload(), e);
    }

    return result;
  }
}
