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
 * Copyright (C) 2011-2025 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.MessageCollection;
import adams.core.QuickInfoHelper;
import adams.core.StoppableUtils;
import adams.core.Utils;
import adams.core.VariableName;
import adams.core.io.PlaceholderFile;
import adams.core.logging.LoggingLevel;
import adams.core.option.OptionUtils;
import adams.data.InPlaceProcessing;
import adams.data.evaluator.instance.AbstractEvaluator;
import adams.data.evaluator.instance.NullEvaluator;
import adams.data.instance.WekaInstanceContainer;
import adams.data.report.Report;
import adams.event.VariableChangeEvent;
import adams.flow.container.EvaluationContainer;
import adams.flow.control.StorageName;
import adams.flow.core.AbstractModelLoader.ModelLoadingType;
import adams.flow.core.CallableActorReference;
import adams.flow.core.EvaluatorModelLoader;
import adams.flow.core.ModelLoaderSupporter;
import adams.flow.core.Token;
import adams.flow.standalone.JobRunnerInstance;
import adams.multiprocess.AbstractJob;
import adams.multiprocess.JobRunnerSupporter;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

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
 * &nbsp;&nbsp;&nbsp;adams.data.instance.WekaInstanceContainer<br>
 * &nbsp;&nbsp;&nbsp;adams.flow.container.EvaluationContainer<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;adams.flow.container.EvaluationContainer<br>
 * <br><br>
 * Container information:<br>
 * - adams.flow.container.EvaluationContainer: Instance, Instances, Evaluations, Evaluator, Classification, Abstention classification, Distribution, Component, Version, ID, Report
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * &nbsp;&nbsp;&nbsp;min-user-mode: Expert
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
 * &nbsp;&nbsp;&nbsp;If set to true, the flow execution at this level gets stopped in case this
 * &nbsp;&nbsp;&nbsp;actor encounters an error; the error gets propagated; useful for critical
 * &nbsp;&nbsp;&nbsp;actors.
 * &nbsp;&nbsp;&nbsp;default: false
 * &nbsp;&nbsp;&nbsp;min-user-mode: Expert
 * </pre>
 *
 * <pre>-silent &lt;boolean&gt; (property: silent)
 * &nbsp;&nbsp;&nbsp;If enabled, then no errors are output in the console; Note: the enclosing
 * &nbsp;&nbsp;&nbsp;actor handler must have this enabled as well.
 * &nbsp;&nbsp;&nbsp;default: false
 * &nbsp;&nbsp;&nbsp;min-user-mode: Expert
 * </pre>
 *
 * <pre>-no-copy &lt;boolean&gt; (property: noCopy)
 * &nbsp;&nbsp;&nbsp;If enabled, no copy of the evaluation container is generated before adding
 * &nbsp;&nbsp;&nbsp;the additional evaluations.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-evaluator &lt;adams.data.evaluator.instance.AbstractEvaluator&gt; (property: evaluator)
 * &nbsp;&nbsp;&nbsp;The Evaluator to train on the input data.
 * &nbsp;&nbsp;&nbsp;default: adams.data.evaluator.instance.NullEvaluator -missing-evaluation NaN
 * </pre>
 *
 * <pre>-model-loading-type &lt;AUTO|FILE|SOURCE_ACTOR|STORAGE&gt; (property: modelLoadingType)
 * &nbsp;&nbsp;&nbsp;Determines how to load the model, in case of AUTO, first the model file
 * &nbsp;&nbsp;&nbsp;is checked, then the callable actor and then the storage.
 * &nbsp;&nbsp;&nbsp;default: AUTO
 * </pre>
 *
 * <pre>-model &lt;adams.core.io.PlaceholderFile&gt; (property: modelFile)
 * &nbsp;&nbsp;&nbsp;The file to load the model from, ignored if pointing to a directory.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 *
 * <pre>-model-actor &lt;adams.flow.core.CallableActorReference&gt; (property: modelActor)
 * &nbsp;&nbsp;&nbsp;The callable actor (source) to obtain the model from, ignored if not present.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-model-storage &lt;adams.flow.control.StorageName&gt; (property: modelStorage)
 * &nbsp;&nbsp;&nbsp;The storage item to obtain the model from, ignored if not present.
 * &nbsp;&nbsp;&nbsp;default: storage
 * </pre>
 *
 * <pre>-use-evaluator-reset-variable &lt;boolean&gt; (property: useEvaluatorResetVariable)
 * &nbsp;&nbsp;&nbsp;If enabled, chnages to the specified variable are monitored in order to
 * &nbsp;&nbsp;&nbsp;reset the evaluator, eg when a storage evaluator changed.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-evaluator-reset-variable &lt;adams.core.VariableName&gt; (property: evaluatorResetVariable)
 * &nbsp;&nbsp;&nbsp;The variable to monitor for changes in order to reset the evaluator, eg
 * &nbsp;&nbsp;&nbsp;when a storage evaluator changed.
 * &nbsp;&nbsp;&nbsp;default: variable
 * </pre>
 *
 * <pre>-component &lt;java.lang.String&gt; (property: component)
 * &nbsp;&nbsp;&nbsp;The component identifier.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-version &lt;java.lang.String&gt; (property: version)
 * &nbsp;&nbsp;&nbsp;The version.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-clean-after-build &lt;boolean&gt; (property: cleanAfterBuild)
 * &nbsp;&nbsp;&nbsp;If enabled, the internally built evaluator gets discarded again.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-prefer-jobrunner &lt;boolean&gt; (property: preferJobRunner)
 * &nbsp;&nbsp;&nbsp;If enabled, tries to offload the processing onto a adams.flow.standalone.JobRunnerInstance;
 * &nbsp;&nbsp;&nbsp; applies only to training.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  dale (dale at waikato dot ac dot nz)
 */
public class Evaluator
  extends AbstractTransformer
  implements ModelLoaderSupporter, InPlaceProcessing, JobRunnerSupporter {

  public static class EvaluateJob
    extends AbstractJob {

    private static final long serialVersionUID = 6406892820872772446L;

    /** the evaluator to train. */
    protected AbstractEvaluator m_Evaluator;

    /** the data to use for training. */
    protected Instances m_Data;

    /**
     * Initializes the job.
     *
     * @param evaluator  the evaluator to train
     * @param data the training data
     */
    public EvaluateJob(AbstractEvaluator evaluator, Instances data) {
      super();
      m_Evaluator = evaluator;
      m_Data      = data;
    }

    /**
     * Checks whether all pre-conditions have been met.
     *
     * @return null if everything is OK, otherwise an error message
     */
    @Override
    protected String preProcessCheck() {
      if (m_Evaluator == null)
	return "No evaluator to train!";
      if (m_Data == null)
	return "No training data!";
      return null;
    }

    /**
     * Does the actual execution of the job.
     *
     * @throws Exception if fails to execute job
     */
    @Override
    protected void process() throws Exception {
      m_Evaluator.build(m_Data);
    }

    /**
     * Stops the execution.
     */
    @Override
    public void stopExecution() {
      StoppableUtils.stopAnyExecution(m_Evaluator);
      super.stopExecution();
    }

    /**
     * Checks whether all post-conditions have been met.
     *
     * @return null if everything is OK, otherwise an error message
     */
    @Override
    protected String postProcessCheck() {
      return null;
    }

    /**
     * Returns a string representation of this job.
     *
     * @return the job as string
     */
    @Override
    public String toString() {
      return OptionUtils.getCommandLine(m_Evaluator) + "\n" + m_Data.relationName();
    }

    /**
     * Cleans up data structures, frees up memory.
     * Removes dependencies and job parameters.
     */
    @Override
    public void cleanUp() {
      m_Evaluator = null;
      m_Data      = null;
      super.cleanUp();
    }
  }

  /** for serialization. */
  private static final long serialVersionUID = 4523798891781897832L;

  /** the key for storing the current evaluator in the backup. */
  public final static String BACKUP_ACTUALEVALUATOR = "evaluator";

  /** whether to only update the container or work on a copy. */
  protected boolean m_NoCopy;

  /** the evaluator. */
  protected AbstractEvaluator m_Evaluator;

  /** the evaluator used for training/evaluating. */
  protected AbstractEvaluator m_ActualEvaluator;

  /** the name of the component. */
  protected String m_Component;

  /** the version of the component. */
  protected String m_Version;

  /** whether to use a variable to monitor for changes, triggering resets of the evaluator. */
  protected boolean m_UseEvaluatorResetVariable;

  /** the variable to monitor for changes, triggering resets of the evaluator. */
  protected VariableName m_EvaluatorResetVariable;

  /** whether we need to reset the evaluator. */
  protected boolean m_ResetEvaluator;

  /** the model loader. */
  protected EvaluatorModelLoader m_ModelLoader;

  /** whether to clean up after build. */
  protected boolean m_CleanAfterBuild;

  /** whether to offload training into a JobRunnerInstance. */
  protected boolean m_PreferJobRunner;

  /** the JobRunnerInstance to use. */
  protected transient JobRunnerInstance m_JobRunnerInstance;

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
      "no-copy", "noCopy",
      false);

    m_OptionManager.add(
      "evaluator", "evaluator",
      new NullEvaluator());

    m_OptionManager.add(
      "model-loading-type", "modelLoadingType",
      ModelLoadingType.AUTO);

    m_OptionManager.add(
      "model", "modelFile",
      new PlaceholderFile("."));

    m_OptionManager.add(
      "model-actor", "modelActor",
      new CallableActorReference());

    m_OptionManager.add(
      "model-storage", "modelStorage",
      new StorageName());

    m_OptionManager.add(
      "use-evaluator-reset-variable", "useEvaluatorResetVariable",
      false);

    m_OptionManager.add(
      "evaluator-reset-variable", "evaluatorResetVariable",
      new VariableName());

    m_OptionManager.add(
      "component", "component",
      "");

    m_OptionManager.add(
      "version", "version",
      "");

    m_OptionManager.add(
      "clean-after-build", "cleanAfterBuild",
      false);

    m_OptionManager.add(
      "prefer-jobrunner", "preferJobRunner",
      false);
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_ModelLoader = new EvaluatorModelLoader();
    m_ModelLoader.setFlowContext(this);
  }

  /**
   * Sets the logging level.
   *
   * @param value 	the level
   */
  @Override
  public synchronized void setLoggingLevel(LoggingLevel value) {
    super.setLoggingLevel(value);
    m_ModelLoader.setLoggingLevel(value);
  }

  /**
   * Sets whether to skip creating a copy of the container before updating it.
   *
   * @param value	true if to skip creating copy
   */
  public void setNoCopy(boolean value) {
    m_NoCopy = value;
    reset();
  }

  /**
   * Returns whether to skip creating a copy of the container before updating it.
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
	+ "adding the additional evaluations.";
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
   * Sets the loading type. In case of {@link ModelLoadingType#AUTO}, first
   * file, then callable actor, then storage.
   *
   * @param value	the type
   */
  public void setModelLoadingType(ModelLoadingType value) {
    m_ModelLoader.setModelLoadingType(value);
    reset();
  }

  /**
   * Returns the loading type. In case of {@link ModelLoadingType#AUTO}, first
   * file, then callable actor, then storage.
   *
   * @return		the type
   */
  public ModelLoadingType getModelLoadingType() {
    return m_ModelLoader.getModelLoadingType();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String modelLoadingTypeTipText() {
    return m_ModelLoader.modelLoadingTypeTipText();
  }

  /**
   * Sets the file to load the model from.
   *
   * @param value	the model file
   */
  public void setModelFile(PlaceholderFile value) {
    m_ModelLoader.setModelFile(value);
    reset();
  }

  /**
   * Returns the file to load the model from.
   *
   * @return		the model file
   */
  public PlaceholderFile getModelFile() {
    return m_ModelLoader.getModelFile();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String modelFileTipText() {
    return m_ModelLoader.modelFileTipText();
  }

  /**
   * Sets the filter source actor.
   *
   * @param value	the source
   */
  public void setModelActor(CallableActorReference value) {
    m_ModelLoader.setModelActor(value);
    reset();
  }

  /**
   * Returns the filter source actor.
   *
   * @return		the source
   */
  public CallableActorReference getModelActor() {
    return m_ModelLoader.getModelActor();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String modelActorTipText() {
    return m_ModelLoader.modelActorTipText();
  }

  /**
   * Sets the filter storage item.
   *
   * @param value	the storage item
   */
  public void setModelStorage(StorageName value) {
    m_ModelLoader.setModelStorage(value);
    reset();
  }

  /**
   * Returns the filter storage item.
   *
   * @return		the storage item
   */
  public StorageName getModelStorage() {
    return m_ModelLoader.getModelStorage();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String modelStorageTipText() {
    return m_ModelLoader.modelStorageTipText();
  }

  /**
   * Sets whether to use a variable to monitor for changes in order
   * to reset the evaluator.
   *
   * @param value	true if to use monitor variable
   */
  public void setUseEvaluatorResetVariable(boolean value) {
    m_UseEvaluatorResetVariable = value;
    reset();
  }

  /**
   * Returns whether to use a variable to monitor for changes in order
   * to reset the evaluator.
   *
   * @return		true if to use monitor variable
   */
  public boolean getUseEvaluatorResetVariable() {
    return m_UseEvaluatorResetVariable;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String useEvaluatorResetVariableTipText() {
    return
      "If enabled, chnages to the specified variable are monitored in order "
	+ "to reset the evaluator, eg when a storage evaluator changed.";
  }

  /**
   * Sets the variable to monitor for changes in order to reset the evaluator.
   *
   * @param value	the variable
   */
  public void setEvaluatorResetVariable(VariableName value) {
    m_EvaluatorResetVariable = value;
    reset();
  }

  /**
   * Returns the variable to monitor for changes in order to reset the evaluator.
   *
   * @return		the variable
   */
  public VariableName getEvaluatorResetVariable() {
    return m_EvaluatorResetVariable;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String evaluatorResetVariableTipText() {
    return
      "The variable to monitor for changes in order to reset the evaluator, eg "
	+ "when a storage evaluator changed.";
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
   * Sets the version to use.
   *
   * @param value	the version
   */
  public void setVersion(String value) {
    m_Version = value;
    reset();
  }

  /**
   * Returns the version.
   *
   * @return		the version
   */
  public String getVersion() {
    return m_Version;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String versionTipText() {
    return "The version.";
  }

  /**
   * Sets whether to discard the internal evaluator again after building it.
   *
   * @param value	true if to discard
   */
  public void setCleanAfterBuild(boolean value) {
    m_CleanAfterBuild = value;
    reset();
  }

  /**
   * Returns whether to discard the internal evaluator again after building it.
   *
   * @return		true if to discard
   */
  public boolean getCleanAfterBuild() {
    return m_CleanAfterBuild;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String cleanAfterBuildTipText() {
    return "If enabled, the internally built evaluator gets discarded again.";
  }

  /**
   * Sets whether to offload processing to a JobRunner instance if available.
   *
   * @param value	if true try to find/use a JobRunner instance
   */
  public void setPreferJobRunner(boolean value) {
    m_PreferJobRunner = value;
    reset();
  }

  /**
   * Returns whether to offload processing to a JobRunner instance if available.
   *
   * @return		if true try to find/use a JobRunner instance
   */
  public boolean getPreferJobRunner() {
    return m_PreferJobRunner;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  @Override
  public String preferJobRunnerTipText() {
    return "If enabled, tries to offload the processing onto a " + Utils.classToString(JobRunnerInstance.class) + "; applies only to training.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result  = QuickInfoHelper.toString(this, "evaluator", m_Evaluator, "evaluator: ");
    result += m_ModelLoader.getQuickInfo(this, result);
    result += QuickInfoHelper.toString(this, "evaluatorResetVariable", m_EvaluatorResetVariable, ", reset: ");
    result += QuickInfoHelper.toString(this, "noCopy", (m_NoCopy ? "no copy" : "copy"), ", ");
    result += QuickInfoHelper.toString(this, "cleanAfterBuild", (m_CleanAfterBuild ? "clean" : "keep"), ", ");
    result += QuickInfoHelper.toString(this, "preferJobRunner", m_PreferJobRunner, ", jobrunner");

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
    m_ModelLoader.reset();
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the accepted classes
   */
  @Override
  public Class[] accepts() {
    return new Class[]{Instances.class, Instance.class, WekaInstanceContainer.class, EvaluationContainer.class};
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
   * Initializes the item for flow execution.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  public String setUp() {
    String	result;

    result = super.setUp();

    if (result == null) {
      if (m_PreferJobRunner)
	m_JobRunnerInstance = JobRunnerInstance.locate(this, true);
    }

    return result;
  }

  /**
   * Loads the evaluator from the evaluator file.
   *
   * @return		null if everything worked, otherwise an error message
   */
  protected String setUpEvaluator() {
    String		result;
    MessageCollection	errors;

    result = null;
    errors = new MessageCollection();
    m_ActualEvaluator = m_ModelLoader.getModel(errors);
    if ((m_ActualEvaluator == null) && (getModelLoadingType() == ModelLoadingType.AUTO)) {
      m_ActualEvaluator = m_Evaluator.shallowCopy();
    }
    else {
      if (!errors.isEmpty())
	result = errors.toString();
    }

    m_ResetEvaluator = false;

    return result;
  }

  /**
   * Gets triggered when a variable changed (added, modified, removed).
   *
   * @param e		the event
   */
  @Override
  public void variableChanged(VariableChangeEvent e) {
    super.variableChanged(e);
    if (e.getName().equals(m_EvaluatorResetVariable.getValue()))
      m_ResetEvaluator = true;
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
    Report			report;
    EvaluationContainer		cont;
    EvaluationContainer		newCont;
    Map<String,Object> 		evals;
    HashMap<String,Float>       eval;
    EvaluateJob job;

    result = null;

    if ((m_ActualEvaluator == null) || m_ResetEvaluator)
      result = setUpEvaluator();

    if (result == null) {
      try {
	// get data
	data   = null;
	inst   = null;
	cont   = null;
	report = null;
	evals  = new HashMap<>();
	if (m_InputToken.hasPayload(EvaluationContainer.class)) {
	  cont = m_InputToken.getPayload(EvaluationContainer.class);
	  if (cont.hasValue(EvaluationContainer.VALUE_INSTANCES))
	    data = cont.getValue(EvaluationContainer.VALUE_INSTANCES, Instances.class);
	  else if (cont.hasValue(EvaluationContainer.VALUE_INSTANCE))
	    inst = cont.getValue(EvaluationContainer.VALUE_INSTANCE, Instance.class);
	  evals = (Map<String, Object>) cont.getValue(EvaluationContainer.VALUE_EVALUATIONS);
	  if (cont.hasValue(EvaluationContainer.VALUE_REPORT))
	    report = cont.getValue(EvaluationContainer.VALUE_REPORT, Report.class);
	}
	else if (m_InputToken.hasPayload(Instances.class)) {
	  data = m_InputToken.getPayload(Instances.class);
	}
	else if (m_InputToken.hasPayload(Instance.class)) {
	  inst = m_InputToken.getPayload(Instance.class);
	}
	else if (m_InputToken.getPayload() instanceof WekaInstanceContainer) {
	  inst   = m_InputToken.getPayload(WekaInstanceContainer.class).getContent();
	  report = m_InputToken.getPayload(WekaInstanceContainer.class).getReport();
	}
	else {
	  throw new IllegalArgumentException(
	    "Unhandled input: " + m_InputToken.getPayload().getClass().getName());
	}

	// process data
	synchronized(m_ActualEvaluator) {
	  eval = null;
	  if (data != null) {
	    if (m_JobRunnerInstance != null) {
	      job    = new EvaluateJob(m_ActualEvaluator, data);
	      result = m_JobRunnerInstance.executeJob(job);
	      job.cleanUp();
	      if (result != null)
		throw new Exception(result);
	    }
	    else {
	      m_ActualEvaluator.build(data);
	    }
	  }
	  else if (inst != null) {
	    eval = m_ActualEvaluator.evaluate(inst);
	  }
	}

	// generate output
	if (cont != null) {
	  if (m_NoCopy)
	    newCont = cont;
	  else
	    newCont = (EvaluationContainer) cont.getClone();
	}
	else {
	  newCont = new EvaluationContainer();
	}
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
	    for (String name : eval.keySet())
	      evals.put(getName() + "." + name, eval.get(name));
	  }
	}
	newCont.setValue(EvaluationContainer.VALUE_EVALUATIONS, evals);
	if (report != null)
	  newCont.setValue(EvaluationContainer.VALUE_REPORT, report.getClone());
	if (!m_Component.isEmpty())
	  newCont.setValue(EvaluationContainer.VALUE_COMPONENT, m_Component);
	if (!m_Version.isEmpty())
	  newCont.setValue(EvaluationContainer.VALUE_VERSION, m_Version);
	m_OutputToken = new Token(newCont);
      }
      catch (Exception e) {
	m_OutputToken = null;
	result = handleException("Failed to evaluate: " + m_InputToken.getPayload(), e);
      }
    }

    if (m_CleanAfterBuild) {
      if (m_ActualEvaluator != null) {
	m_ActualEvaluator.destroy();
	m_ActualEvaluator = null;
      }
    }

    return result;
  }

  /**
   * Stops the execution. No message set.
   */
  @Override
  public void stopExecution() {
    StoppableUtils.stopAnyExecution(m_ActualEvaluator);
    super.stopExecution();
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

    m_JobRunnerInstance = null;

    super.wrapUp();
  }
}
