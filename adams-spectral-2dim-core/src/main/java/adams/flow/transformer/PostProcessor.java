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
 * PostProcessor.java
 * Copyright (C) 2011-2024 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.MessageCollection;
import adams.core.QuickInfoHelper;
import adams.core.StoppableUtils;
import adams.core.io.FileUtils;
import adams.core.io.PlaceholderFile;
import adams.core.logging.LoggingLevel;
import adams.data.instance.WekaInstanceContainer;
import adams.data.postprocessor.PostProcessorDetails;
import adams.data.postprocessor.instances.AbstractPostProcessor;
import adams.data.report.Report;
import adams.data.textrenderer.AbstractTextRenderer;
import adams.flow.container.PostProcessingContainer;
import adams.flow.control.StorageName;
import adams.flow.core.AbstractModelLoader.ModelLoadingType;
import adams.flow.core.CallableActorReference;
import adams.flow.core.ModelLoaderSupporter;
import adams.flow.core.PostProcessorModelLoader;
import adams.flow.core.Token;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Hashtable;

/**
 <!-- globalinfo-start -->
 * Post-processes the input Instances or, after the post-processor has been initialized with Instances, also input Instance objects.<br>
 * The actual post-processor in use gets output when container output enabled.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;weka.core.Instances<br>
 * &nbsp;&nbsp;&nbsp;weka.core.Instance<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;weka.core.Instances<br>
 * &nbsp;&nbsp;&nbsp;weka.core.Instance<br>
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
 * &nbsp;&nbsp;&nbsp;default: PostProcessor
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
 * <pre>-post-processor &lt;adams.data.postprocessor.instances.AbstractPostProcessor&gt; (property: postProcessor)
 * &nbsp;&nbsp;&nbsp;The PostProcessor to use to process the Instances.
 * &nbsp;&nbsp;&nbsp;default: adams.data.postprocessor.instances.WekaFilter -filter weka.filters.AllFilter
 * </pre>
 *
 * <pre>-output-container &lt;boolean&gt; (property: outputContainer)
 * &nbsp;&nbsp;&nbsp;If enabled, a container gets output.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class PostProcessor
  extends AbstractTransformer
  implements ModelLoaderSupporter {

  /** for serialization. */
  private static final long serialVersionUID = 5924370393684251310L;

  /** the key for storing the current postprocessor in the backup. */
  public final static String BACKUP_ACTUALPOSTPROCESSOR = "postprocessor";

  /** the postprocessor. */
  protected AbstractPostProcessor m_PostProcessor;

  /** the postprocessor used for doing the actual work. */
  protected AbstractPostProcessor m_ActualPostProcessor;

  /** whether to output a container. */
  protected boolean m_OutputContainer;

  /** the model loader. */
  protected PostProcessorModelLoader m_ModelLoader;

  /** the file to save the cleaner details to. */
  protected PlaceholderFile m_PostProcessorDetailsOutput;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Post-processes the input Instances or, after the post-processor "
	+ "has been initialized with Instances, also input Instance objects.\n"
	+ "The actual post-processor in use gets output when container output enabled.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "post-processor", "postProcessor",
      new adams.data.postprocessor.instances.WekaFilter());

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
      "output-container", "outputContainer",
      false);

    m_OptionManager.add(
      "post-processor-details-output", "postProcessorDetailsOutput",
      new PlaceholderFile());
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_ModelLoader = new PostProcessorModelLoader();
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
   * Sets the post-processor to use.
   *
   * @param value	the post-processor
   */
  public void setPostProcessor(AbstractPostProcessor value) {
    m_PostProcessor = value;
    reset();
  }

  /**
   * Returns the post-processor in use.
   *
   * @return		the post-processor
   */
  public AbstractPostProcessor getPostProcessor() {
    return m_PostProcessor;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String postProcessorTipText() {
    return "The PostProcessor to use to process the Instances.";
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
   * Sets whether to output a container.
   *
   * @param value	true if to output a container
   */
  public void setOutputContainer(boolean value) {
    m_OutputContainer = value;
    reset();
  }

  /**
   * Returns whether to output a container.
   *
   * @return		true if to output a container
   */
  public boolean getOutputContainer() {
    return m_OutputContainer;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String outputContainerTipText() {
    return "If enabled, a container gets output.";
  }

  /**
   * Sets the file for the post-processor details.
   *
   * @param value	the file
   */
  public void setPostProcessorDetailsOutput(PlaceholderFile value) {
    m_PostProcessorDetailsOutput = value;
    reset();
  }

  /**
   * Returns the file for the post-processor details.
   *
   * @return		the file
   */
  public PlaceholderFile getPostProcessorDetailsOutput() {
    return m_PostProcessorDetailsOutput;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String postProcessorDetailsOutputTipText() {
    return
      "The file to save the post-processor details to after training, in case "
	+ "the post-processor implements the " + PostProcessorDetails.class.getName()
	+ " interface; ignored if pointing to a directory.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result  = QuickInfoHelper.toString(this, "postProcessor", m_PostProcessor);
    result += QuickInfoHelper.toString(this, "modelLoadingType", getModelLoadingType(), ", type: ");
    result += QuickInfoHelper.toString(this, "modelFile", getModelFile(), ", model: ");
    result += QuickInfoHelper.toString(this, "modelSource", getModelActor(), ", source: ");
    result += QuickInfoHelper.toString(this, "modelStorage", getModelStorage(), ", storage: ");
    result += QuickInfoHelper.toString(this, "outputContainer", (m_OutputContainer ? "output container" : "output processed data"), ", ");

    return result;
  }

  /**
   * Removes entries from the backup.
   */
  @Override
  protected void pruneBackup() {
    super.pruneBackup();

    pruneBackup(BACKUP_ACTUALPOSTPROCESSOR);
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

    if (m_ActualPostProcessor != null)
      result.put(BACKUP_ACTUALPOSTPROCESSOR, m_ActualPostProcessor);

    return result;
  }

  /**
   * Restores the state of the actor before the variables got updated.
   *
   * @param state	the backup of the state to restore from
   */
  @Override
  protected void restoreState(Hashtable<String,Object> state) {
    if (state.containsKey(BACKUP_ACTUALPOSTPROCESSOR)) {
      m_ActualPostProcessor = (AbstractPostProcessor) state.get(BACKUP_ACTUALPOSTPROCESSOR);
      state.remove(BACKUP_ACTUALPOSTPROCESSOR);
    }

    super.restoreState(state);
  }

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();

    m_ActualPostProcessor = null;
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the accepted classes
   */
  public Class[] accepts() {
    return new Class[]{Instances.class, Instance.class, PostProcessingContainer.class, WekaInstanceContainer.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the generated classes
   */
  public Class[] generates() {
    if (m_OutputContainer)
      return new Class[]{PostProcessingContainer.class};
    else
      return new Class[]{Instances.class, Instance.class};
  }

  /**
   * Configures the postprocessor.
   *
   * @return		null if successful, otherwise error message
   */
  protected String setUpPostProcessor() {
    String		result;
    MessageCollection errors;

    result = null;
    errors = new MessageCollection();
    m_ActualPostProcessor = m_ModelLoader.getModel(errors);
    if ((m_ActualPostProcessor == null) && (getModelLoadingType() == ModelLoadingType.AUTO)) {
      m_ActualPostProcessor = m_PostProcessor.shallowCopy();
      m_ActualPostProcessor.setFlowContext(this);
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
    String			result;
    Instances			data;
    Instances 			processedData;
    Instance			inst;
    Instance			processedInst;
    Report			report;
    PostProcessingContainer 	cont;

    result = null;

    if (m_ActualPostProcessor == null)
      result = setUpPostProcessor();

    if (result == null) {
      try {
	data   = null;
	inst   = null;
	report = null;
	if (m_InputToken.getPayload() instanceof Instances) {
	  data = m_InputToken.getPayload(Instances.class);
	}
	else if (m_InputToken.getPayload() instanceof Instance) {
	  inst = m_InputToken.getPayload(Instance.class);
	}
	else if (m_InputToken.getPayload() instanceof WekaInstanceContainer) {
	  inst   = m_InputToken.getPayload(WekaInstanceContainer.class).getContent();
	  report = m_InputToken.getPayload(WekaInstanceContainer.class).getReport();
	}
	else if (m_InputToken.getPayload() instanceof PostProcessingContainer) {
	  cont = m_InputToken.getPayload(PostProcessingContainer.class);
	  if (cont.hasValue(PostProcessingContainer.VALUE_OUTPUT_INSTANCES))
	    data = cont.getValue(PostProcessingContainer.VALUE_OUTPUT_INSTANCES, Instances.class);
	  else if (cont.hasValue(PostProcessingContainer.VALUE_OUTPUT_INSTANCE))
	    inst = cont.getValue(PostProcessingContainer.VALUE_OUTPUT_INSTANCE, Instance.class);
	  if (cont.hasValue(PostProcessingContainer.VALUE_REPORT))
	    report = cont.getValue(PostProcessingContainer.VALUE_REPORT, Report.class);
	}
	else {
	  result = m_InputToken.unhandledData();
	}
	if (result == null) {
	  if (data != null) {
	    processedData = m_ActualPostProcessor.postProcess(data);
	    if (m_OutputContainer)
	      m_OutputToken = new Token(new PostProcessingContainer(data, processedData, m_ActualPostProcessor));
	    else
	      m_OutputToken = new Token(processedData);

	    if ((m_PostProcessor instanceof PostProcessorDetails) && !m_PostProcessorDetailsOutput.isDirectory()) {
	      if (!FileUtils.writeToFile(
		m_PostProcessorDetailsOutput.getAbsolutePath(),
		((PostProcessorDetails<Object>) m_ActualPostProcessor).getDetails(),
		false))
		getLogger().severe("Failed to save post-processor details to '" + m_PostProcessorDetailsOutput + "'!");
	    }
	  }
	  else if (inst != null) {
	    processedInst = m_ActualPostProcessor.postProcess(inst);
	    if (m_OutputContainer)
	      m_OutputToken = new Token(new PostProcessingContainer(inst, processedInst, m_ActualPostProcessor));
	    else
	      m_OutputToken = new Token(processedInst);
	  }
	  else {
	    result = "Failed to obtain any data from input token!";
	  }
	  // store report, if available
	  if (m_OutputContainer && (result == null) && (report != null))
	    m_OutputToken.getPayload(PostProcessingContainer.class).setValue(PostProcessingContainer.VALUE_REPORT, report.getClone());
	}
      }
      catch (Exception e) {
	m_OutputToken = null;
	result = handleException("Failed to post-process: " + AbstractTextRenderer.renderObject(m_InputToken.getPayload()), e);
      }
    }

    return result;
  }

  /**
   * Stops the execution. No message set.
   */
  @Override
  public void stopExecution() {
    StoppableUtils.stopAnyExecution(m_ActualPostProcessor);
    super.stopExecution();
  }

  /**
   * Cleans up after the execution has finished.
   */
  @Override
  public void wrapUp() {
    if (m_ActualPostProcessor != null) {
      m_ActualPostProcessor.destroy();
      m_ActualPostProcessor = null;
    }

    super.wrapUp();
  }
}
