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
 * Copyright (C) 2011-2017 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.QuickInfoHelper;
import adams.data.postprocessor.instances.AbstractPostProcessor;
import adams.flow.container.PostProcessingContainer;
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
 * @version $Revision: 2242 $
 */
public class PostProcessor
  extends AbstractTransformer {

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
	"output-container", "outputContainer",
	false);
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
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result  = QuickInfoHelper.toString(this, "postProcessor", m_PostProcessor);
    result += QuickInfoHelper.toString(this, "outputContainer", (m_OutputContainer ? ", container" : ""));

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
   * @return		<!-- flow-accepts-start -->weka.core.Instances.class, weka.core.Instance.class<!-- flow-accepts-end -->
   */
  public Class[] accepts() {
    return new Class[]{Instances.class, Instance.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		<!-- flow-generates-start -->weka.core.Instances.class, weka.core.Instance.class<!-- flow-generates-end -->
   */
  public Class[] generates() {
    if (m_OutputContainer)
      return new Class[]{PostProcessingContainer.class};
    else
      return new Class[]{Instances.class, Instance.class};
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String	result;
    Instances	data;
    Instances 	processedData;
    Instance	inst;
    Instance	processedInst;

    result = null;

    if (m_ActualPostProcessor == null)
      m_ActualPostProcessor = m_PostProcessor.shallowCopy();

    try {
      if (m_InputToken.getPayload() instanceof Instances) {
	data          = (Instances) m_InputToken.getPayload();
	processedData = m_ActualPostProcessor.postProcess(data);
	if (m_OutputContainer)
	  m_OutputToken = new Token(new PostProcessingContainer(data, processedData, m_ActualPostProcessor));
	else
	  m_OutputToken = new Token(processedData);
      }
      else if (m_InputToken.getPayload() instanceof Instance) {
	inst          = (Instance) m_InputToken.getPayload();
	processedInst = m_ActualPostProcessor.postProcess(inst);
	if (m_OutputContainer)
	  m_OutputToken = new Token(new PostProcessingContainer(inst, processedInst, m_ActualPostProcessor));
	else
	  m_OutputToken = new Token(processedInst);
      }
    }
    catch (Exception e) {
      m_OutputToken = null;
      result = handleException("Failed to post-process: " + m_InputToken.getPayload(), e);
    }

    return result;
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
