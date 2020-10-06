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
 * ConditionalReportField.java
 * Copyright (C) 2018-2020 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.Report;
import adams.data.report.ReportHandler;
import adams.data.spectrum.Spectrum;
import adams.flow.condition.bool.BooleanCondition;
import adams.flow.condition.bool.Expression;
import adams.flow.core.Token;

/**
 * Sets the 'success' value for the specified field if the condition
 * evaluates to 'true', otherwise the 'failure' value.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ConditionalReportField
  extends AbstractFilter<Spectrum> {

  private static final long serialVersionUID = 7769069544817788117L;

  /** the condition to apply. */
  protected BooleanCondition m_Condition;

  /** the target field. */
  protected Field m_Target;

  /** the success value. */
  protected String m_Success;

  /** the failure value. */
  protected String m_Failure;


  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Sets the 'success' value for the specified field if the condition "
      + "evaluates to 'true', otherwise the 'failure' value.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "condition", "condition",
      new Expression());

    m_OptionManager.add(
      "target", "target",
      new Field("Test", DataType.BOOLEAN));

    m_OptionManager.add(
      "success", "success",
      "true");

    m_OptionManager.add(
      "failure", "failure",
      "false");
  }

  /**
   * Sets the condition to evaluate.
   *
   * @param value	the condition
   */
  public void setCondition(BooleanCondition value) {
    m_Condition = value;
    reset();
  }

  /**
   * Returns the condition to evaluate.
   *
   * @return		the condition
   */
  public BooleanCondition getCondition() {
    return m_Condition;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String conditionTipText() {
    return "The condition to evaluate.";
  }

  /**
   * Sets the target field in the report to store the result in.
   *
   * @param value	the field
   */
  public void setTarget(Field value) {
    m_Target = value;
    reset();
  }

  /**
   * Returns the target field in the report to store the result in.
   *
   * @return		the field
   */
  public Field getTarget() {
    return m_Target;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String targetTipText() {
    return "The target field in the report to store the result in.";
  }

  /**
   * Sets the success value, ie the value to use when the condition 
   * evaluates to true.
   *
   * @param value	the success value
   */
  public void setSuccess(String value) {
    m_Success = value;
    reset();
  }

  /**
   * Returns the success value, ie the value to use when the condition 
   * evaluates to true.
   *
   * @return		the success value
   */
  public String getSuccess() {
    return m_Success;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String successTipText() {
    return "The value to use in case the condition evaluates to 'true' (= success).";
  }

  /**
   * Sets the failure value, ie the value to use when the condition 
   * evaluates to true.
   *
   * @param value	the failure value
   */
  public void setFailure(String value) {
    m_Failure = value;
    reset();
  }

  /**
   * Returns the failure value, ie the value to use when the condition 
   * evaluates to true.
   *
   * @return		the failure value
   */
  public String getFailure() {
    return m_Failure;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String failureTipText() {
    return "The value to use in case the condition evaluates to 'true' (= failure).";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  protected Spectrum processData(Spectrum data) {
    Token 	token;
    boolean	success;
    Report	report;

    token   = new Token(data);
    success = m_Condition.evaluate(null, token);
    if (isLoggingEnabled())
      getLogger().info("Success? " + success);

    report = ((ReportHandler) data).getReport();

    report.addField(m_Target);
    report.setValue(m_Target, success ? m_Success : m_Failure);

    return data;
  }
}
