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
 * WekaPredictionContainerToEvaluationContainer.java
 * Copyright (C) 2012-2023 University of Waikato, Hamilton, New Zealand
 */
package adams.data.conversion;

import adams.core.QuickInfoHelper;
import adams.flow.container.EvaluationContainer;
import adams.flow.container.WekaPredictionContainer;
import weka.core.Instance;

/**
 <!-- globalinfo-start -->
 * Turns a adams.flow.container.WekaPredictionContainer into a knir.flow.container.EvaluationContainer.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-id &lt;java.lang.String&gt; (property: ID)
 * &nbsp;&nbsp;&nbsp;The (optional) ID for the evaluation (to be used instead of component).
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class WekaPredictionContainerToEvaluationContainer
  extends AbstractConversion {

  /** for serialization. */
  private static final long serialVersionUID = 8828591710515484463L;

  /** the optional component to store. */
  protected String m_Component;

  /** the optional version to store. */
  protected String m_Version;

  /** the optional ID to store. */
  protected String m_ID;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Turns a " + WekaPredictionContainer.class.getName()
	+ " into a " + EvaluationContainer.class.getName() + ".";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "component", "component",
      "");

    m_OptionManager.add(
      "version", "version",
      "");

    m_OptionManager.add(
      "id", "ID",
      "");
  }

  /**
   * Sets the component to use for the evaluation.
   *
   * @param value	the component
   */
  public void setComponent(String value) {
    m_Component = value;
    reset();
  }

  /**
   * Returns the component to use for the evaluation.
   *
   * @return 		the component
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
    return "The (optional) component for the evaluation.";
  }

  /**
   * Sets the version to use for the evaluation.
   *
   * @param value	the version
   */
  public void setVersion(String value) {
    m_Version = value;
    reset();
  }

  /**
   * Returns the version to use for the evaluation.
   *
   * @return 		the version
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
    return "The (optional) version of the evaluation.";
  }

  /**
   * Sets the ID to use for the evaluation (to be used instead of component).
   *
   * @param value	the ID
   */
  public void setID(String value) {
    m_ID = value;
    reset();
  }

  /**
   * Returns the ID to use for the evaluation (to be used instead of component).
   *
   * @return 		the ID
   */
  public String getID() {
    return m_ID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String IDTipText() {
    return "The (optional) ID for the evaluation (to be used instead of component).";
  }

  /**
   * Returns a quick info about the object, which can be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result = QuickInfoHelper.toString(this, "component", m_Component, "component: ");
    result += QuickInfoHelper.toString(this, "version", m_Version, ", version: ");
    result += QuickInfoHelper.toString(this, "ID", m_ID, ", ID: ");

    return result;
  }

  /**
   * Returns the class that is accepted as input.
   *
   * @return		the class
   */
  @Override
  public Class accepts() {
    return WekaPredictionContainer.class;
  }

  /**
   * Returns the class that is generated as output.
   *
   * @return		the class
   */
  @Override
  public Class generates() {
    return EvaluationContainer.class;
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  @Override
  protected Object doConvert() throws Exception {
    EvaluationContainer		result;
    WekaPredictionContainer	input;
    Instance			inst;

    result = new EvaluationContainer();
    input  = (WekaPredictionContainer) m_Input;
    inst   = (Instance) input.getValue(WekaPredictionContainer.VALUE_INSTANCE);
    // update the class value for the evaluators
    if (input.hasValue(WekaPredictionContainer.VALUE_CLASSIFICATION)) {
      inst = (Instance) inst.copy();
      inst.setClassValue((Double) input.getValue(WekaPredictionContainer.VALUE_CLASSIFICATION));
    }

    // classification (or label)
    if (input.hasValue(WekaPredictionContainer.VALUE_CLASSIFICATION_LABEL))
      result.setValue(EvaluationContainer.VALUE_CLASSIFICATION, input.getValue(WekaPredictionContainer.VALUE_CLASSIFICATION_LABEL));
    else if (input.hasValue(WekaPredictionContainer.VALUE_CLASSIFICATION))
      result.setValue(EvaluationContainer.VALUE_CLASSIFICATION, input.getValue(WekaPredictionContainer.VALUE_CLASSIFICATION));

    // class distribution
    if (input.hasValue(WekaPredictionContainer.VALUE_DISTRIBUTION))
      result.setValue(EvaluationContainer.VALUE_DISTRIBUTION, input.getValue(WekaPredictionContainer.VALUE_DISTRIBUTION));

    result.setValue(EvaluationContainer.VALUE_INSTANCE, inst);

    // abstention (or label)
    if (input.hasValue(WekaPredictionContainer.VALUE_ABSTENTION_CLASSIFICATION))
      result.setValue(EvaluationContainer.VALUE_ABSTENTION_CLASSIFICATION, input.getValue(WekaPredictionContainer.VALUE_ABSTENTION_CLASSIFICATION));
    else if (input.hasValue(WekaPredictionContainer.VALUE_ABSTENTION_CLASSIFICATION_LABEL))
      result.setValue(EvaluationContainer.VALUE_ABSTENTION_CLASSIFICATION, input.getValue(WekaPredictionContainer.VALUE_ABSTENTION_CLASSIFICATION_LABEL));

    // report
    if (input.hasValue(WekaPredictionContainer.VALUE_REPORT))
      result.setValue(EvaluationContainer.VALUE_REPORT, input.getValue(WekaPredictionContainer.VALUE_REPORT));

    if (!m_Component.isEmpty())
      result.setValue(EvaluationContainer.VALUE_COMPONENT, m_Component);
    if (!m_Version.isEmpty())
      result.setValue(EvaluationContainer.VALUE_VERSION, m_Version);
    if (!m_ID.isEmpty())
      result.setValue(EvaluationContainer.VALUE_ID, m_ID);

    return result;
  }
}
