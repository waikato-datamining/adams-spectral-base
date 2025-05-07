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
 * ThreeWayDataModelFilter.java
 * Copyright (C) 2018-2025 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer;

import adams.core.MessageCollection;
import adams.core.QuickInfoHelper;
import adams.core.Utils;
import adams.core.VariableName;
import adams.core.io.PlaceholderFile;
import adams.core.logging.LoggingLevel;
import adams.data.container.TensorContainer;
import adams.event.VariableChangeEvent;
import adams.flow.control.StorageName;
import adams.flow.core.AbstractModelLoader.ModelLoadingType;
import adams.flow.core.CallableActorReference;
import adams.flow.core.ModelLoaderSupporter;
import adams.flow.core.ThreeWayDataModelFilterLoader;
import adams.flow.core.Token;
import nz.ac.waikato.cms.adams.multiway.algorithm.api.AbstractAlgorithm;
import nz.ac.waikato.cms.adams.multiway.algorithm.api.Filter;
import nz.ac.waikato.cms.adams.multiway.data.tensor.Tensor;

import java.util.Hashtable;

/**
 <!-- globalinfo-start -->
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ThreeWayDataModelFilter
  extends AbstractTransformer
  implements ModelLoaderSupporter {

  private static final long serialVersionUID = -2363530100534716943L;

  /** the key for storing the current model in the backup. */
  public final static String BACKUP_ACTUALMODEL = "model";

  /** whether to use a variable to monitor for changes, triggering resets of the model. */
  protected boolean m_UseModelResetVariable;

  /** the variable to monitor for changes, triggering resets of the model. */
  protected VariableName m_ModelResetVariable;

  /** whether we need to reset the model. */
  protected boolean m_ResetModel;

  /** the model loader. */
  protected ThreeWayDataModelFilterLoader m_ModelLoader;

  /** the model used for training/evaluating. */
  protected AbstractAlgorithm m_ActualModel;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Applies a built multi-way algorithm to the incoming data, acting as a filter.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

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
      "use-model-reset-variable", "useModelResetVariable",
      false);

    m_OptionManager.add(
      "model-reset-variable", "modelResetVariable",
      new VariableName());
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_ModelLoader = new ThreeWayDataModelFilterLoader();
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
   * Sets the whether to use a variable to monitor for changes in order
   * to reset the model.
   *
   * @param value	true if to use monitor variable
   */
  public void setUseModelResetVariable(boolean value) {
    m_UseModelResetVariable = value;
    reset();
  }

  /**
   * Returns the whether to use a variable to monitor for changes in order
   * to reset the model.
   *
   * @return		true if to use monitor variable
   */
  public boolean getUseModelResetVariable() {
    return m_UseModelResetVariable;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String useModelResetVariableTipText() {
    return
        "If enabled, chnages to the specified variable are monitored in order "
	  + "to reset the model, eg when a storage model changed.";
  }

  /**
   * Sets the variable to monitor for changes in order to reset the model.
   *
   * @param value	the variable
   */
  public void setModelResetVariable(VariableName value) {
    m_ModelResetVariable = value;
    reset();
  }

  /**
   * Returns the variable to monitor for changes in order to reset the model.
   *
   * @return		the variable
   */
  public VariableName getModelResetVariable() {
    return m_ModelResetVariable;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String modelResetVariableTipText() {
    return
        "The variable to monitor for changes in order to reset the model, eg "
	  + "when a storage model changed.";
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the Class of objects that can be processed
   */
  @Override
  public Class[] accepts() {
    return new Class[]{TensorContainer.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the Class of the generated tokens
   */
  @Override
  public Class[] generates() {
    return new Class[]{TensorContainer.class};
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result  = m_ModelLoader.getQuickInfo(this);
    result += QuickInfoHelper.toString(this, "modelResetVariable", m_ModelResetVariable, ", reset: ");

    return result;
  }

  /**
   * Removes entries from the backup.
   */
  @Override
  protected void pruneBackup() {
    super.pruneBackup();

    pruneBackup(BACKUP_ACTUALMODEL);
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

    if (m_ActualModel != null)
      result.put(BACKUP_ACTUALMODEL, m_ActualModel);

    return result;
  }

  /**
   * Restores the state of the actor before the variables got updated.
   *
   * @param state	the backup of the state to restore from
   */
  @Override
  protected void restoreState(Hashtable<String,Object> state) {
    if (state.containsKey(BACKUP_ACTUALMODEL)) {
      m_ActualModel = (AbstractAlgorithm) state.get(BACKUP_ACTUALMODEL);
      state.remove(BACKUP_ACTUALMODEL);
    }

    super.restoreState(state);
  }

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();

    m_ActualModel = null;
    m_ModelLoader.reset();
  }

  /**
   * Tries to obtain the algorithm model.
   *
   * @return		null if everything worked, otherwise an error message
   */
  protected String setUpModel() {
    String		result;
    MessageCollection errors;

    if (m_ResetModel) {
      m_ModelLoader.reset();
      m_ResetModel = false;
    }

    result = null;
    errors = new MessageCollection();
    m_ActualModel = m_ModelLoader.getModel(errors);
    if (m_ActualModel == null) {
      result = "Failed to obtain model!";
    }
    else {
      if (!errors.isEmpty())
        result = errors.toString();
      else if (!(m_ActualModel instanceof Filter))
        result = "Model is not an instance of " + Utils.classToString(Filter.class);
    }

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
    if (e.getName().equals(m_ModelResetVariable.getValue())) {
      m_ResetModel = true;
      if (isLoggingEnabled())
        getLogger().info("Reset 'model'");
    }
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String		result;
    TensorContainer	data;
    Filter		filter;
    Tensor		filtered;
    TensorContainer	cont;

    result = null;

    if ((m_ActualModel == null) || m_ResetModel)
      result = setUpModel();

    data = null;
    if (m_InputToken.hasPayload(TensorContainer.class))
      data = m_InputToken.getPayload(TensorContainer.class);
    else
      result = m_InputToken.unhandledData();

    if (result == null) {
      try {
        filter   = (Filter) m_ActualModel;
        filtered = filter.filter(data.getContent());
        if (filtered != null) {
          cont = (TensorContainer) data.getHeader();
          cont.setContent(filtered);
          cont.getNotes().addProcessInformation(this);
	  m_OutputToken = new Token(cont);
	}
      }
      catch (Exception e) {
        result = handleException("Failed to filter data!", e);
      }
    }

    return result;
  }
}
