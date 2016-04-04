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
 * Evaluator.java
 * Copyright (C) 2011-2015 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.flow.core.Token;
import adams.data.evaluator.instance.AbstractEvaluator;
import adams.data.evaluator.instance.NullEvaluator;
import adams.flow.container.EvaluationContainer;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.Hashtable;

/**
 <!-- globalinfo-start -->
 * If input is Instances, build this Evaluator. If Instance, use the built Evaluator.<br>
 * The name of this evaluator is used for storing the evaluation result.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;weka.core.Instances<br>
 * &nbsp;&nbsp;&nbsp;weka.core.Instance<br>
 * &nbsp;&nbsp;&nbsp;knir.flow.container.EvaluationContainer<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;knir.flow.container.EvaluationContainer<br>
 * <br><br>
 * Container information:<br>
 * - knir.flow.container.EvaluationContainer: Instance, Instances, Evaluations, Evaluator, Abstention classification, Component<br>
 * - knir.flow.container.EvaluationContainer: Instance, Instances, Evaluations, Evaluator, Abstention classification, Component
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
 * &nbsp;&nbsp;&nbsp;default: Evaluator
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
 * <pre>-silent &lt;boolean&gt; (property: silent)
 * &nbsp;&nbsp;&nbsp;If enabled, then no errors are output in the console.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-evaluator &lt;knir.data.evaluator.instance.AbstractEvaluator&gt; (property: evaluator)
 * &nbsp;&nbsp;&nbsp;The Evaluator to train on the input data.
 * &nbsp;&nbsp;&nbsp;default: knir.data.evaluator.instance.NullEvaluator -missing-evaluation NaN
 * </pre>
 * 
 * <pre>-component &lt;java.lang.String&gt; (property: component)
 * &nbsp;&nbsp;&nbsp;The component identifier.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  dale (dale at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class Evaluator
  extends AbstractTransformer {

  /** for serialization. */
  private static final long serialVersionUID = 4523798891781897832L;

  /** the key for storing the current evaluator in the backup. */
  public final static String BACKUP_ACTUALEVALUATOR = "evaluator";

  /** the evaluator. */
  protected AbstractEvaluator m_Evaluator;

  /** the evaluator used for training/evaluating. */
  protected AbstractEvaluator m_ActualEvaluator;

  /** the name of the component. */
  protected String m_Component;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "If input is Instances, build this Evaluator. If Instance, use the "
      + "built Evaluator.\n"
      + "The name of this evaluator is used for storing the evaluation result.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "evaluator", "evaluator",
      new NullEvaluator());

    m_OptionManager.add(
      "component", "component",
      "");
  }

  /**
   * Sets the evaluator to use.
   *
   * @param value	the evaluator
   */
  public void setEvaluator(AbstractEvaluator value) {
    m_Evaluator = value;
    reset();
  }

  /**
   * Returns the evaluator in use.
   *
   * @return		the evaluator
   */
  public AbstractEvaluator getEvaluator() {
    return m_Evaluator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String evaluatorTipText() {
    return "The Evaluator to train on the input data.";
  }

  /**
   * Sets the component identifier to use.
   *
   * @param value	the component identifier
   */
  public void setComponent(String value) {
    m_Component = value;
    reset();
  }

  /**
   * Returns the component identifier.
   *
   * @return		the component identifier
   */
  public String getComponent() {
    return m_Component;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String componentTipText() {
    return "The component identifier.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;
    String	variable;

    result   = "evaluator: ";
    variable = getOptionManager().getVariableForProperty("evaluator");
    if (variable != null)
      result += variable;
    else
      result += m_Evaluator.getClass().getSimpleName();

    return result;
  }

  /**
   * Removes entries from the backup.
   */
  @Override
  protected void pruneBackup() {
    super.pruneBackup();

    pruneBackup(BACKUP_ACTUALEVALUATOR);
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

    if (m_ActualEvaluator != null)
      result.put(BACKUP_ACTUALEVALUATOR, m_ActualEvaluator);

    return result;
  }

  /**
   * Restores the state of the actor before the variables got updated.
   *
   * @param state	the backup of the state to restore from
   */
  @Override
  protected void restoreState(Hashtable<String,Object> state) {
    if (state.containsKey(BACKUP_ACTUALEVALUATOR)) {
      m_ActualEvaluator = (AbstractEvaluator) state.get(BACKUP_ACTUALEVALUATOR);
      state.remove(BACKUP_ACTUALEVALUATOR);
    }

    super.restoreState(state);
  }

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();

    m_ActualEvaluator = null;
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		<!-- flow-accepts-start -->weka.core.Instances.class, weka.core.Instance.class, knir.flow.container.EvaluationContainer.class<!-- flow-accepts-end -->
   */
  @Override
  public Class[] accepts() {
    return new Class[]{Instances.class, Instance.class, EvaluationContainer.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		<!-- flow-generates-start -->knir.flow.container.EvaluationContainer.class<!-- flow-generates-end -->
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
    Instances			data;
    Instance			inst;
    EvaluationContainer		cont;
    EvaluationContainer		newCont;
    Hashtable<String,Object>	evals;
    HashMap<String,Float>       eval;

    result = null;

    if (m_ActualEvaluator == null)
      m_ActualEvaluator = m_Evaluator.shallowCopy();

    try {
      // get data
      data  = null;
      inst  = null;
      cont  = null;
      evals = new Hashtable<String,Object>();
      if (m_InputToken.getPayload() instanceof EvaluationContainer) {
	cont = (EvaluationContainer) m_InputToken.getPayload();
	if (cont.hasValue(EvaluationContainer.VALUE_INSTANCES))
	  data = (Instances) cont.getValue(EvaluationContainer.VALUE_INSTANCES);
	else if (cont.hasValue(EvaluationContainer.VALUE_INSTANCE))
	  inst = (Instance) cont.getValue(EvaluationContainer.VALUE_INSTANCE);
	evals = (Hashtable<String,Object>) cont.getValue(EvaluationContainer.VALUE_EVALUATIONS);
      }
      else if (m_InputToken.getPayload() instanceof Instances) {
	data = (Instances) m_InputToken.getPayload();
      }
      else if (m_InputToken.getPayload() instanceof Instance) {
	inst = (Instance) m_InputToken.getPayload();
      }
      else {
	throw new IllegalArgumentException(
	    "Unhandled input: " + m_InputToken.getPayload().getClass().getName());
      }

      // process data
      eval = null;
      if (data != null) {
	m_ActualEvaluator.build(data);
      }
      else if (inst != null) {
	eval = m_ActualEvaluator.evaluate(inst);
      }

      // generate output
      if (cont != null)
	newCont = (EvaluationContainer) cont.getClone();
      else
	newCont = new EvaluationContainer();
      newCont.setValue(EvaluationContainer.VALUE_EVALUATOR, m_ActualEvaluator);
      if (data != null)
	newCont.setValue(EvaluationContainer.VALUE_INSTANCES, data);
      if (inst != null)
	newCont.setValue(EvaluationContainer.VALUE_INSTANCE, inst);
      if (eval != null) {
        if (eval.size() == 1) {
          evals.put(getName(), eval.get(AbstractEvaluator.DEFAULT_METRIC));
        }
        else {
          for (String name: eval.keySet())
            evals.put(getName() + "." + name, eval.get(name));
        }
      }
      newCont.setValue(EvaluationContainer.VALUE_EVALUATIONS, evals);
      newCont.setValue(EvaluationContainer.VALUE_COMPONENT, m_Component);
      m_OutputToken = new Token(newCont);
    }
    catch (Exception e) {
      m_OutputToken = null;
      result = handleException("Failed to evaluate: " + m_InputToken.getPayload(), e);
    }

    return result;
  }

  /**
   * Cleans up after the execution has finished.
   */
  @Override
  public void wrapUp() {
    if (m_ActualEvaluator != null) {
      m_ActualEvaluator.destroy();
      m_ActualEvaluator = null;
    }

    super.wrapUp();
  }
}
