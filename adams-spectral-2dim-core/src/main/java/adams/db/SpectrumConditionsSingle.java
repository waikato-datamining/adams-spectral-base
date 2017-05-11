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
 * SpectrumConditionsSimple.java
 * Copyright (C) 2010-2015 University of Waikato, Hamilton, New Zealand
 */

package adams.db;

import adams.core.base.BaseDouble;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;

/**
 <!-- globalinfo-start -->
 * Conditions for the retrieval of spectra.<br>
 * Only a single field can be specified.
 * <br><br>
 <!-- globalinfo-end -->
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
 * <pre>-limit &lt;int&gt; (property: limit)
 * &nbsp;&nbsp;&nbsp;The maximum number of records to retrieve.
 * &nbsp;&nbsp;&nbsp;default: 10000
 * &nbsp;&nbsp;&nbsp;minimum: -1
 * </pre>
 * 
 * <pre>-instrument &lt;adams.core.base.BaseRegExp&gt; (property: instrument)
 * &nbsp;&nbsp;&nbsp;The regular expression on the instrument.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-sampleid &lt;adams.core.base.BaseRegExp&gt; (property: sampleIDRegExp)
 * &nbsp;&nbsp;&nbsp;The regular expression on the spectrum ID.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-format &lt;adams.core.base.BaseRegExp&gt; (property: format)
 * &nbsp;&nbsp;&nbsp;The regular expression on the data format.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-type &lt;adams.core.base.BaseRegExp&gt; (property: sampleTypeRegExp)
 * &nbsp;&nbsp;&nbsp;The regular expression on the sample type.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-start &lt;adams.core.base.BaseDateTime&gt; (property: startDate)
 * &nbsp;&nbsp;&nbsp;The start date for the spectra (yyyy-MM-dd HH:mm:ss).
 * &nbsp;&nbsp;&nbsp;default: -INF
 * </pre>
 * 
 * <pre>-end &lt;adams.core.base.BaseDateTime&gt; (property: endDate)
 * &nbsp;&nbsp;&nbsp;The end date for the spectra (yyyy-MM-dd HH:mm:ss).
 * &nbsp;&nbsp;&nbsp;default: +INF
 * </pre>
 * 
 * <pre>-no-dummies (property: excludeDummies)
 * &nbsp;&nbsp;&nbsp;If set to true, then spectra with sample data flagged as dummies will be 
 * &nbsp;&nbsp;&nbsp;excluded.
 * </pre>
 * 
 * <pre>-only-dummies (property: onlyDummies)
 * &nbsp;&nbsp;&nbsp;If set to true, then only spectra with sample data flagged as dummies will 
 * &nbsp;&nbsp;&nbsp;be included.
 * </pre>
 * 
 * <pre>-latest (property: latest)
 * &nbsp;&nbsp;&nbsp;If set to true, order is reversed and only latest ones are returned.
 * </pre>
 * 
 * <pre>-field &lt;knir.data.sampledata.Field&gt; (property: field)
 * &nbsp;&nbsp;&nbsp;The required field in the sample data.
 * &nbsp;&nbsp;&nbsp;default: [U]
 * </pre>
 * 
 * <pre>-min &lt;adams.core.base.BaseDouble&gt; (property: minimumValue)
 * &nbsp;&nbsp;&nbsp;The required minimum value for the field in the sample data (-1 is unbounded
 * &nbsp;&nbsp;&nbsp;).
 * &nbsp;&nbsp;&nbsp;default: -1.0
 * </pre>
 * 
 * <pre>-max &lt;adams.core.base.BaseDouble&gt; (property: maximumValue)
 * &nbsp;&nbsp;&nbsp;The required maximum value for the field in the sample data (-1 is unbounded
 * &nbsp;&nbsp;&nbsp;).
 * &nbsp;&nbsp;&nbsp;default: -1.0
 * </pre>
 * 
 * <pre>-required &lt;knir.data.sampledata.Field&gt; (property: requiredField)
 * &nbsp;&nbsp;&nbsp;The field that is required to be present in the report, regardless of values 
 * &nbsp;&nbsp;&nbsp;it has.
 * &nbsp;&nbsp;&nbsp;default: [U]
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 11831 $
 */
public class SpectrumConditionsSingle
  extends AbstractSpectrumConditions {

  /** for serialization. */
  private static final long serialVersionUID = 8972337431072505207L;

  /** the reference fields. */
  protected Field m_Field;

  /** the minimum value for the field. */
  protected BaseDouble m_MinimumValue;

  /** the maximum value for the field. */
  protected BaseDouble m_MaximumValue;

  /** the field that us required to be present in report. */
  protected Field m_RequiredField;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "Conditions for the retrieval of spectra.\n"
      + "Only a single field can be specified.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "field", "field",
	    new Field("", DataType.UNKNOWN));

    m_OptionManager.add(
	    "min", "minimumValue",
	    new BaseDouble("-1.0"));

    m_OptionManager.add(
	    "max", "maximumValue",
	    new BaseDouble("-1.0"));

    m_OptionManager.add(
	    "required", "requiredField",
	    new Field("", DataType.UNKNOWN));
  }

  /**
   * Sets the field to require.
   *
   * @param value 	the field
   */
  public void setField(Field value) {
    m_Field = value;
    reset();
  }

  /**
   * Returns the required field.
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
    return "The required field in the sample data.";
  }

  /**
   * Sets the minimum value for the field.
   *
   * @param value 	the minimum value
   */
  public void setMinimumValue(BaseDouble value) {
    m_MinimumValue = value;
    reset();
  }

  /**
   * Returns the minimum value for the field.
   *
   * @return 		the minimum value
   */
  public BaseDouble getMinimumValue() {
    return m_MinimumValue;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String minimumValueTipText() {
    return "The required minimum value for the field in the sample data (-1 is unbounded).";
  }

  /**
   * Sets the maximum value to the fields.
   *
   * @param value 	the maximum value
   */
  public void setMaximumValue(BaseDouble value) {
    m_MaximumValue = value;
    reset();
  }

  /**
   * Returns the maximum values for the fields.
   *
   * @return 		the maximum values
   */
  public BaseDouble getMaximumValue() {
    return m_MaximumValue;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String maximumValueTipText() {
    return "The required maximum value for the field in the sample data (-1 is unbounded).";
  }

  /**
   * Sets the required field.
   *
   * @param value 	the field
   */
  public void setRequiredField(Field value) {
    m_RequiredField = value;
    reset();
  }

  /**
   * Returns the required field.
   *
   * @return 		the field
   */
  public Field getRequiredField() {
    return m_RequiredField;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String requiredFieldTipText() {
    return "The field that is required to be present in the report, regardless of values it has.";
  }

  /**
   * Automatically corrects values.
   */
  @Override
  protected void update() {
    super.update();

    if (m_Field == null)
      m_Field = new Field("", DataType.UNKNOWN);

    if (m_MinimumValue == null)
      m_MinimumValue = new BaseDouble("-1.0");

    if (m_MaximumValue == null)
      m_MaximumValue = new BaseDouble("-1.0");

    if (m_RequiredField == null)
      m_RequiredField = new Field(SampleData.FIELD_DUMMYREPORT, DataType.BOOLEAN);
  }
}
