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
 * Copyright (C) 2011-2012 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.flow.core.Token;
import adams.data.postprocessor.instances.AbstractPostProcessor;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Hashtable;

/**
 <!-- globalinfo-start -->
 * Post-processes the input Instances or, after the post-processor has been initialized with Instances, also input Instance objects.
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
 * Valid options are: <br><br>
 *
 * <pre>-D &lt;int&gt; (property: debugLevel)
 * &nbsp;&nbsp;&nbsp;The greater the number the more additional info the scheme may output to
 * &nbsp;&nbsp;&nbsp;the console (0 = off).
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 *
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: PostProcessor
 * </pre>
 *
 * <pre>-annotation &lt;adams.core.base.BaseText&gt; (property: annotations)
 * &nbsp;&nbsp;&nbsp;The annotations to attach to this actor.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-skip (property: skip)
 * &nbsp;&nbsp;&nbsp;If set to true, transformation is skipped and the input token is just forwarded
 * &nbsp;&nbsp;&nbsp;as it is.
 * </pre>
 *
 * <pre>-stop-flow-on-error (property: stopFlowOnError)
 * &nbsp;&nbsp;&nbsp;If set to true, the flow gets stopped in case this actor encounters an error;
 * &nbsp;&nbsp;&nbsp; useful for critical actors.
 * </pre>
 *
 * <pre>-post-processor &lt;knir.data.postprocessor.instances.AbstractPostProcessor&gt; (property: postProcessor)
 * &nbsp;&nbsp;&nbsp;The PostProcessor to use to process the Instances.
 * &nbsp;&nbsp;&nbsp;default: knir.data.postprocessor.instances.WekaFilter -filter weka.filters.AllFilter
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

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "Post-processes the input Instances or, after the post-processor "
      + "has been initialized with Instances, also input Instance objects.";
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
  }

  /**
   * Sets the cleaner to use.
   *
   * @param value	the cleanerr
   */
  public void setPostProcessor(AbstractPostProcessor value) {
    m_PostProcessor = value;
    reset();
  }

  /**
   * Returns the cleaner in use.
   *
   * @return		the cleaner
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
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	variable;

    variable = getOptionManager().getVariableForProperty("postProcessor");

    if (variable != null)
      return variable;
    else
      return m_PostProcessor.getClass().getName().replaceAll("knir\\.data\\.", "");
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
	m_OutputToken = new Token(processedData);
      }
      else if (m_InputToken.getPayload() instanceof Instance) {
	inst          = (Instance) m_InputToken.getPayload();
	processedInst = m_ActualPostProcessor.postProcess(inst);
	m_OutputToken = new Token(processedInst);
      }
    }
    catch (Exception e) {
      m_OutputToken = null;
      result = handleException("Failed to postprocess: " + m_InputToken.getPayload(), e);
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
