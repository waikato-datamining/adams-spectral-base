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
 * Cleaner.java
 * Copyright (C) 2011-2024 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.MessageCollection;
import adams.core.QuickInfoHelper;
import adams.core.Stoppable;
import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.core.io.PlaceholderFile;
import adams.core.logging.LoggingLevel;
import adams.core.option.OptionUtils;
import adams.data.cleaner.CleanerDetails;
import adams.data.cleaner.instance.AbstractCleaner;
import adams.data.cleaner.instance.IQRCleaner;
import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.flow.container.CleaningContainer;
import adams.flow.control.StorageName;
import adams.flow.core.AbstractModelLoader.ModelLoadingType;
import adams.flow.core.CallableActorReference;
import adams.flow.core.CleanerModelLoader;
import adams.flow.core.ModelLoaderSupporter;
import adams.flow.core.Token;
import adams.flow.standalone.JobRunnerInstance;
import adams.multiprocess.AbstractJob;
import adams.multiprocess.JobRunnerSupporter;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Hashtable;

/**
 <!-- globalinfo-start -->
 * In case of Instances objects, 'unclean' Instance objects get removed. When receiving an Instance object, a note is attached.<br>
 * The following order is used to obtain the model (when using AUTO):<br>
 * 1. model file present?<br>
 * 2. source actor present?<br>
 * 3. storage item present?<br>
 * 4. The cleaner is instantiated from the provided definition.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;adams.flow.container.CleaningContainer<br>
 * &nbsp;&nbsp;&nbsp;weka.core.Instance<br>
 * &nbsp;&nbsp;&nbsp;weka.core.Instances<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;adams.flow.container.CleaningContainer<br>
 * <br><br>
 * Container information:<br>
 * - adams.flow.container.CleaningContainer: Instance, Instances, Checks, Cleaner
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
 * &nbsp;&nbsp;&nbsp;default: Cleaner
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
 * <pre>-cleaner &lt;adams.data.cleaner.instance.AbstractCleaner&gt; (property: cleaner)
 * &nbsp;&nbsp;&nbsp;The Cleaner to use to clean Instances.
 * &nbsp;&nbsp;&nbsp;default: adams.data.cleaner.instance.IQRCleaner -pre-filter weka.filters.AllFilter -filter \"weka.filters.unsupervised.attribute.InterquartileRange -R first-last -O 3.0 -E 6.0\"
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
 * <pre>-cleaner-details-output &lt;adams.core.io.PlaceholderFile&gt; (property: cleanerDetailsOutput)
 * &nbsp;&nbsp;&nbsp;The file to save the cleaner details to after training, in case the cleaner
 * &nbsp;&nbsp;&nbsp;implements the adams.data.cleaner.CleanerDetails interface; ignored if pointing
 * &nbsp;&nbsp;&nbsp;to a directory.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
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
public class Cleaner
  extends AbstractTransformer
  implements ModelLoaderSupporter, JobRunnerSupporter {

  public static class CleanJob
    extends AbstractJob {

    private static final long serialVersionUID = 6406892820872772446L;

    /** the cleaner to apply. */
    protected AbstractCleaner m_Cleaner;

    /** the data to clean. */
    protected Instances m_Data;

    /** the cleaned data. */
    protected Instances m_Cleaned;

    /**
     * Initializes the job.
     *
     * @param cleaner  	the cleaner to apply
     * @param data 	the data to clean
     */
    public CleanJob(AbstractCleaner cleaner, Instances data) {
      super();
      m_Cleaner = cleaner;
      m_Data      = data;
      m_Cleaned   = null;
    }

    /**
     * Returns the cleaned data.
     *
     * @return		the cleaned data, null if not available
     */
    public Instances getCleaned() {
      return m_Cleaned;
    }

    /**
     * Checks whether all pre-conditions have been met.
     *
     * @return null if everything is OK, otherwise an error message
     */
    @Override
    protected String preProcessCheck() {
      if (m_Cleaner == null)
	return "No cleaner to train!";
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
      m_Cleaned = m_Cleaner.clean(m_Data);
    }

    /**
     * Stops the execution.
     */
    @Override
    public void stopExecution() {
      if (m_Cleaner instanceof Stoppable)
	((Stoppable) m_Cleaner).stopExecution();
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
      return OptionUtils.getCommandLine(m_Cleaner) + "\n" + m_Data.relationName();
    }

    /**
     * Cleans up data structures, frees up memory.
     * Removes dependencies and job parameters.
     */
    @Override
    public void cleanUp() {
      m_Cleaner = null;
      m_Data      = null;
      m_Cleaned   = null;
      super.cleanUp();
    }
  }

  /** for serialization. */
  private static final long serialVersionUID = -7274476322706230890L;

  /** the key for storing the current evaluator in the backup. */
  public final static String BACKUP_ACTUALCLEANER = "cleaner";

  /** the evaluator. */
  protected AbstractCleaner m_Cleaner;

  /** the cleaner used for training/evaluating. */
  protected AbstractCleaner m_ActualCleaner;

  /** the file to save the cleaner details to. */
  protected PlaceholderFile m_CleanerDetailsOutput;

  /** the model loader. */
  protected CleanerModelLoader m_ModelLoader;

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
      "In case of Instances objects, 'unclean' Instance objects get removed. "
	+ "When receiving an Instance object, a note is attached.\n"
	+ m_ModelLoader.automaticOrderInfo() + "\n"
	+ "4. The cleaner is instantiated from the provided definition.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "cleaner", "cleaner",
      new IQRCleaner());

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
      "cleaner-details-output", "cleanerDetailsOutput",
      new PlaceholderFile());

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

    m_ModelLoader = new CleanerModelLoader();
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
   * Sets the cleaner to use.
   *
   * @param value	the cleaner
   */
  public void setCleaner(AbstractCleaner value) {
    m_Cleaner = value;
    reset();
  }

  /**
   * Returns the cleaner in use.
   *
   * @return		the cleaner
   */
  public AbstractCleaner getCleaner() {
    return m_Cleaner;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String cleanerTipText() {
    return "The Cleaner to use to clean Instances.";
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
   * Sets the file for the cleaner details.
   *
   * @param value	the file
   */
  public void setCleanerDetailsOutput(PlaceholderFile value) {
    m_CleanerDetailsOutput = value;
    reset();
  }

  /**
   * Returns the file for the cleaner details.
   *
   * @return		the file
   */
  public PlaceholderFile getCleanerDetailsOutput() {
    return m_CleanerDetailsOutput;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String cleanerDetailsOutputTipText() {
    return
      "The file to save the cleaner details to after training, in case "
	+ "the cleaner implements the " + CleanerDetails.class.getName()
	+ " interface; ignored if pointing to a directory.";
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

    result  = QuickInfoHelper.toString(this, "cleaner", m_Cleaner, "cleaner: ");
    result += QuickInfoHelper.toString(this, "modelLoadingType", getModelLoadingType(), ", type: ");
    result += QuickInfoHelper.toString(this, "modelFile", getModelFile(), ", model: ");
    result += QuickInfoHelper.toString(this, "modelSource", getModelActor(), ", source: ");
    result += QuickInfoHelper.toString(this, "modelStorage", getModelStorage(), ", storage: ");
    result += QuickInfoHelper.toString(this, "cleanerDetailsOutput", m_CleanerDetailsOutput, ", details: ");
    result += QuickInfoHelper.toString(this, "preferJobRunner", m_PreferJobRunner, ", jobrunner");

    return result;
  }

  /**
   * Removes entries from the backup.
   */
  @Override
  protected void pruneBackup() {
    super.pruneBackup();

    pruneBackup(BACKUP_ACTUALCLEANER);
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

    if (m_ActualCleaner != null)
      result.put(BACKUP_ACTUALCLEANER, m_ActualCleaner);

    return result;
  }

  /**
   * Restores the state of the actor before the variables got updated.
   *
   * @param state	the backup of the state to restore from
   */
  @Override
  protected void restoreState(Hashtable<String,Object> state) {
    if (state.containsKey(BACKUP_ACTUALCLEANER)) {
      m_ActualCleaner = (AbstractCleaner) state.get(BACKUP_ACTUALCLEANER);
      state.remove(BACKUP_ACTUALCLEANER);
    }

    super.restoreState(state);
  }

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();

    m_ActualCleaner = null;
    m_ModelLoader.reset();
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		<!-- flow-accepts-start -->adams.flow.container.CleaningContainer.class, weka.core.Instance.class, weka.core.Instances.class<!-- flow-accepts-end -->
   */
  public Class[] accepts() {
    return new Class[]{CleaningContainer.class, Instance.class, Instances.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		<!-- flow-generates-start -->adams.flow.container.CleaningContainer.class<!-- flow-generates-end -->
   */
  public Class[] generates() {
    return new Class[]{CleaningContainer.class};
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
   * Configures the cleaner.
   *
   * @return		null if successful, otherwise error message
   */
  protected String setUpCleaner() {
    String		result;
    MessageCollection	errors;

    result = null;
    errors = new MessageCollection();
    m_ActualCleaner = m_ModelLoader.getModel(errors);
    if ((m_ActualCleaner == null) && (getModelLoadingType() == ModelLoadingType.AUTO)) {
      m_ActualCleaner = m_Cleaner.shallowCopy();
      m_ActualCleaner.setFlowContext(this);
    }
    else {
      if (!errors.isEmpty())
	result = errors.toString();
    }

    return result;
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String		result;
    Instances		data;
    Instance		inst;
    Instances 		cleaned;
    String		check;
    SpreadSheet		checks;
    Row			row;
    CleaningContainer	cont;
    CleaningContainer	newCont;
    CleanJob		job;

    result = null;

    if (m_ActualCleaner == null)
      result = setUpCleaner();

    if (result == null) {
      try {
	data = null;
	inst = null;
	cleaned = null;
	check = null;
	checks = new DefaultSpreadSheet();
	checks.getHeaderRow().addCell("Cleaner").setContent("Cleaner");
	checks.getHeaderRow().addCell("Check").setContent("Check");

	// get data
	if (m_InputToken.getPayload() instanceof CleaningContainer) {
	  cont = (CleaningContainer) m_InputToken.getPayload();
	  if (cont.hasValue(CleaningContainer.VALUE_INSTANCES))
	    data = (Instances) cont.getValue(CleaningContainer.VALUE_INSTANCES);
	  if (cont.hasValue(CleaningContainer.VALUE_INSTANCE))
	    inst = (Instance) cont.getValue(CleaningContainer.VALUE_INSTANCE);
	  if (cont.hasValue(CleaningContainer.VALUE_CHECKS))
	    checks = (SpreadSheet) cont.getValue(CleaningContainer.VALUE_CHECKS);
	}
	else if (m_InputToken.getPayload() instanceof Instances) {
	  data = (Instances) m_InputToken.getPayload();
	}
	else if (m_InputToken.getPayload() instanceof Instance) {
	  inst = (Instance) m_InputToken.getPayload();
	}

	// apply cleaner
	if (data != null) {
	  if (m_JobRunnerInstance != null) {
	    job     = new CleanJob(m_ActualCleaner, data);
	    result  = m_JobRunnerInstance.executeJob(job);
	    cleaned = job.getCleaned();
	    job.cleanUp();
	    if (result != null)
	      throw new Exception(result);
	  }
	  else {
	    cleaned = m_ActualCleaner.clean(data);
	  }
	  if (cleaned == null) {
	    if (m_ActualCleaner.hasCleanInstancesError())
	      throw new IllegalStateException(
		"Cleaner '" + m_ActualCleaner + "' returned empty dataset:\n"
		  + m_ActualCleaner.getCleanInstancesError());
	    else
	      throw new IllegalStateException(
		"Cleaner '" + m_ActualCleaner + "' returned empty dataset!");
	  }
	  else if ((m_Cleaner instanceof CleanerDetails) && !m_CleanerDetailsOutput.isDirectory()) {
	    if (!FileUtils.writeToFile(
	      m_CleanerDetailsOutput.getAbsolutePath(),
	      ((CleanerDetails) m_ActualCleaner).getDetails(),
	      false))
	      getLogger().severe("Failed to save cleaner details to '" + m_CleanerDetailsOutput + "'!");
	  }
	}
	else if (inst != null) {
	  check = m_ActualCleaner.check(inst);
	  if (check != null) {
	    row = checks.addRow("" + checks.getRowCount());
	    row.addCell("Cleaner").setContent(getName());
	    row.addCell("Check").setContent(check);
	  }
	}

	// generate output
	newCont = new CleaningContainer();
	if (inst != null)
	  newCont.setValue(CleaningContainer.VALUE_INSTANCE, inst);
	if (cleaned != null)
	  newCont.setValue(CleaningContainer.VALUE_INSTANCES, cleaned);
	newCont.setValue(CleaningContainer.VALUE_CHECKS, checks);
	newCont.setValue(CleaningContainer.VALUE_CLEANER, m_ActualCleaner);
	m_OutputToken = new Token(newCont);
      }
      catch (Exception e) {
	m_OutputToken = null;
	result = handleException("Failed to clean!", e);
      }
    }

    return result;
  }

  /**
   * Stops the execution. No message set.
   */
  @Override
  public void stopExecution() {
    if (m_ActualCleaner instanceof Stoppable)
      ((Stoppable) m_ActualCleaner).stopExecution();
    super.stopExecution();
  }

  /**
   * Cleans up after the execution has finished.
   */
  @Override
  public void wrapUp() {
    if (m_ActualCleaner != null) {
      m_ActualCleaner.destroy();
      m_ActualCleaner = null;
    }

    m_JobRunnerInstance = null;

    super.wrapUp();
  }
}
