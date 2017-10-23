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
 * Copyright (C) 2011-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.io.FileUtils;
import adams.core.io.PlaceholderFile;
import adams.data.cleaner.CleanerDetails;
import adams.data.cleaner.instance.AbstractCleaner;
import adams.data.cleaner.instance.IQRCleaner;
import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.flow.container.CleaningContainer;
import adams.flow.core.Token;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Hashtable;

/**
 <!-- globalinfo-start -->
 * In case of Instances objects, 'unclean' Instance objects get removed. When receiving an Instance object, a note is attached
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;knir.flow.container.CleaningContainer<br>
 * &nbsp;&nbsp;&nbsp;weka.core.Instance<br>
 * &nbsp;&nbsp;&nbsp;weka.core.Instances<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;knir.flow.container.CleaningContainer<br>
 * <br><br>
 * Container information:<br>
 * - knir.flow.container.CleaningContainer: Instance, Instances, Checks<br>
 * - knir.flow.container.CleaningContainer: Instance, Instances, Checks
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
 * &nbsp;&nbsp;&nbsp;default: Cleaner
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
 * <pre>-cleaner &lt;knir.data.cleaner.instance.AbstractCleaner&gt; (property: cleaner)
 * &nbsp;&nbsp;&nbsp;The Cleaner to use to clean Instances.
 * &nbsp;&nbsp;&nbsp;default: knir.data.cleaner.instance.IQRCleaner -pre-filter weka.filters.AllFilter
 * </pre>
 * 
 * <pre>-cleaner-details-output &lt;adams.core.io.PlaceholderFile&gt; (property: cleanerDetailsOutput)
 * &nbsp;&nbsp;&nbsp;The file to save the cleaner details to after training, in case the cleaner 
 * &nbsp;&nbsp;&nbsp;implements the knir.data.cleaner.CleanerDetails interface; ignored if pointing 
 * &nbsp;&nbsp;&nbsp;to a directory.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  dale (dale at waikato dot ac dot nz)
 * @version $Revision: 2355 $
 */
public class Cleaner
  extends AbstractTransformer {

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

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "In case of Instances objects, 'unclean' Instance objects get removed. "
      + "When receiving an Instance object, a note is attached";
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
	"cleaner-details-output", "cleanerDetailsOutput",
	new PlaceholderFile());
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
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;
    String	variable;

    variable = getOptionManager().getVariableForProperty("cleaner");
    if (variable != null)
      result = variable;
    else
      result = m_Cleaner.getClass().getName().replaceAll("knir\\.data\\.", "");
    
    variable = getOptionManager().getVariableForProperty("cleanerDetailsOutput");
    if (variable != null)
      result += ", details: " + variable;
    else if (!m_CleanerDetailsOutput.isDirectory())
      result = ", details: " + m_CleanerDetailsOutput;
    
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
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		<!-- flow-accepts-start -->knir.flow.container.CleaningContainer.class, weka.core.Instance.class, weka.core.Instances.class<!-- flow-accepts-end -->
   */
  public Class[] accepts() {
    return new Class[]{CleaningContainer.class, Instance.class, Instances.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		<!-- flow-generates-start -->knir.flow.container.CleaningContainer.class<!-- flow-generates-end -->
   */
  public Class[] generates() {
    return new Class[]{CleaningContainer.class};
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

    result = null;

    if (m_ActualCleaner == null) {
      m_ActualCleaner = m_Cleaner.shallowCopy();
      m_ActualCleaner.setFlowContent(this);
    }

    try {
      data    = null;
      inst    = null;
      cleaned = null;
      check   = null;
      checks  = new DefaultSpreadSheet();
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
	cleaned = m_ActualCleaner.clean(data);
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

    return result;
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

    super.wrapUp();
  }
}
