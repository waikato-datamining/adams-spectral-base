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

/**
 * PickByReportValue.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package adams.data.multifilter;

import adams.core.base.BaseRegExp;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;

/**
 <!-- globalinfo-start -->
 * Extracts the first sub-spectrum that matches the condition for the field.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-no-id-update &lt;boolean&gt; (property: dontUpdateID)
 * &nbsp;&nbsp;&nbsp;If enabled, suppresses updating the ID of adams.data.id.IDHandler data containers.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-field &lt;knir.data.sampledata.Field&gt; (property: field)
 * &nbsp;&nbsp;&nbsp;The field to inspect.
 * &nbsp;&nbsp;&nbsp;default: Instrument[S]
 * </pre>
 * 
 * <pre>-condition-string &lt;adams.core.base.BaseRegExp&gt; (property: conditionString)
 * &nbsp;&nbsp;&nbsp;The regular expression to apply to string fields.
 * &nbsp;&nbsp;&nbsp;default: .*
 * </pre>
 * 
 * <pre>-condition-numeric-min &lt;double&gt; (property: conditionNumericMin)
 * &nbsp;&nbsp;&nbsp;The lower bound for numeric fields.
 * &nbsp;&nbsp;&nbsp;default: 4.9E-324
 * </pre>
 * 
 * <pre>-condition-numeric-max &lt;double&gt; (property: conditionNumericMax)
 * &nbsp;&nbsp;&nbsp;The upper bound for numeric fields.
 * &nbsp;&nbsp;&nbsp;default: 1.7976931348623157E308
 * </pre>
 * 
 * <pre>-condition-boolean &lt;boolean&gt; (property: conditionBoolean)
 * &nbsp;&nbsp;&nbsp;Whether the boolean field must be true or false.
 * &nbsp;&nbsp;&nbsp;default: true
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class PickByReportValue
  extends AbstractMultiSpectrumFilter {

  /** for serialization. */
  private static final long serialVersionUID = 396771111601239664L;
  
  /** the field to use.*/
  protected Field m_Field;

  /** the condition for string fields. */
  protected BaseRegExp m_ConditionString;

  /** the condition for numeric (lower bound). */
  protected double m_ConditionNumericMin;

  /** the condition for numeric (upper bound). */
  protected double m_ConditionNumericMax;

  /** the condition for boolean fields. */
  protected boolean m_ConditionBoolean;
  
  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Extracts the first sub-spectrum that matches the condition for the field.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "field", "field",
	    new Field(SampleData.INSTRUMENT, DataType.STRING));

    m_OptionManager.add(
	    "condition-string", "conditionString",
	    new BaseRegExp());

    m_OptionManager.add(
	    "condition-numeric-min", "conditionNumericMin",
	    Double.MIN_VALUE);

    m_OptionManager.add(
	    "condition-numeric-max", "conditionNumericMax",
	    Double.MAX_VALUE);

    m_OptionManager.add(
	    "condition-boolean", "conditionBoolean",
	    true);
  }

  /**
   * Sets the field to inspect.
   *
   * @param value 	the field
   */
  public void setField(Field value) {
    m_Field = value;
    reset();
  }

  /**
   * Returns the field to inspect.
   *
   * @return 		the field
   */
  public Field getField() {
    return m_Field;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String fieldTipText() {
    return "The field to inspect.";
  }

  /**
   * Sets the regular expression to apply to string fields.
   *
   * @param value 	the regexp
   */
  public void setConditionString(BaseRegExp value) {
    m_ConditionString = value;
    reset();
  }

  /**
   * Returns the regular expression to apply to string fields.
   *
   * @return 		the field
   */
  public BaseRegExp getConditionString() {
    return m_ConditionString;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String conditionStringTipText() {
    return "The regular expression to apply to string fields.";
  }

  /**
   * Sets the lower bound that numeric fields must match.
   *
   * @param value 	the lower bound
   */
  public void setConditionNumericMin(double value) {
    m_ConditionNumericMin = value;
    reset();
  }

  /**
   * Returns the lower bound that numeric fields must match.
   *
   * @return 		the lower bound
   */
  public double getConditionNumericMin() {
    return m_ConditionNumericMin;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String conditionNumericMinTipText() {
    return "The lower bound for numeric fields.";
  }

  /**
   * Sets the upper bound that numeric fields must match.
   *
   * @param value 	the upper bound
   */
  public void setConditionNumericMax(double value) {
    m_ConditionNumericMax = value;
    reset();
  }

  /**
   * Returns the upper bound that numeric fields must match.
   *
   * @return 		the upper bound
   */
  public double getConditionNumericMax() {
    return m_ConditionNumericMax;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String conditionNumericMaxTipText() {
    return "The upper bound for numeric fields.";
  }

  /**
   * Sets whether the boolean field must be true or false.
   *
   * @param value 	the condition
   */
  public void setConditionBoolean(boolean value) {
    m_ConditionBoolean = value;
    reset();
  }

  /**
   * Returns whether the boolean field must be true or false.
   *
   * @return 		the condition
   */
  public boolean getConditionBoolean() {
    return m_ConditionBoolean;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String conditionBooleanTipText() {
    return "Whether the boolean field must be true or false.";
  }
  
  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data, null if failed to generate output
   */
  @Override
  protected Spectrum processData(MultiSpectrum data) {
    Spectrum	result;
    Spectrum	spec;
    SampleData	sd;
    int		i;

    result = null;
    
    for (i = 0; i < data.size(); i++) {
      spec = data.toList().get(i);
      if (spec.hasReport()) {
	sd = spec.getReport();
	if (sd.hasValue(m_Field)) {
	  switch (m_Field.getDataType()) {
	    case BOOLEAN:
	      if (sd.getBooleanValue(m_Field) == m_ConditionBoolean)
		result = (Spectrum) spec.getClone();
	      break;
	    case NUMERIC:
	      if ((sd.getDoubleValue(m_Field) >= m_ConditionNumericMin) && (sd.getDoubleValue(m_Field) <= m_ConditionNumericMax))
		result = (Spectrum) spec.getClone();
	      break;
	    case STRING:
	      if (m_ConditionString.isMatch(sd.getStringValue(m_Field)))
		result = (Spectrum) spec.getClone();
	      break;
	    case UNKNOWN:
	      if (m_ConditionString.isMatch("" + sd.getValue(m_Field)))
		result = (Spectrum) spec.getClone();
	      break;
	    default:
	      throw new IllegalStateException("Unhandled data type: " + m_Field.getDataType());
	  }
	}
      }
      if (result != null)
	break;
    }
    
    return result;
  }
}
