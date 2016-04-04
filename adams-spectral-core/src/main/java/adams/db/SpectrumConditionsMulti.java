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
 * SpectrumConditionsMulti.java
 * Copyright (C) 2009-2015 University of Waikato, Hamilton, New Zealand
 */

package adams.db;

import adams.core.Utils;
import adams.core.base.BaseDouble;
import adams.data.report.Field;

/**
 <!-- globalinfo-start -->
 * Conditions for the retrieval of spectra.<br>
 * Several fields can be specified.
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
 * <pre>-field &lt;knir.data.sampledata.Field&gt; [-field ...] (property: fields)
 * &nbsp;&nbsp;&nbsp;The required fields in the sample data.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-min &lt;adams.core.base.BaseDouble&gt; [-min ...] (property: minimumValues)
 * &nbsp;&nbsp;&nbsp;The required minimum values for the fields in the sample data (-1 is unbounded
 * &nbsp;&nbsp;&nbsp;).
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-max &lt;adams.core.base.BaseDouble&gt; [-max ...] (property: maximumValues)
 * &nbsp;&nbsp;&nbsp;The required maximum values for the fields in the sample data (-1 is unbounded
 * &nbsp;&nbsp;&nbsp;).
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-required &lt;knir.data.sampledata.Field&gt; [-required ...] (property: requiredFields)
 * &nbsp;&nbsp;&nbsp;The fields that are required to be present in the report, regardless of 
 * &nbsp;&nbsp;&nbsp;values they have.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 11831 $
 */
public class SpectrumConditionsMulti
  extends AbstractSpectrumConditions {

  /** for serialization. */
  private static final long serialVersionUID = 8972337431072505207L;

  /** the reference fields. */
  protected Field[] m_Fields;

  /** the minimum value for the field(s). */
  protected BaseDouble[] m_MinimumValues;

  /** the maximum value for the field(s). */
  protected BaseDouble[] m_MaximumValues;

  /** the fields that are required to be present in report. */
  protected Field[] m_RequiredFields;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "Conditions for the retrieval of spectra.\n"
      + "Several fields can be specified.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "field", "fields",
	    new Field[0]);

    m_OptionManager.add(
	    "min", "minimumValues",
	    new BaseDouble[0]);

    m_OptionManager.add(
	    "max", "maximumValues",
	    new BaseDouble[0]);

    m_OptionManager.add(
	    "required", "requiredFields",
	    new Field[0]);
  }

  /**
   * Sets the fields to require.
   *
   * @param value 	the fields
   */
  public void setFields(Field[] value) {
    m_Fields = value;
    reset();
  }

  /**
   * Returns the required fields.
   *
   * @return 		the fields
   */
  public Field[] getFields() {
    return m_Fields;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String fieldsTipText() {
    return "The required fields in the sample data.";
  }

  /**
   * Sets the minimum values to the fields.
   *
   * @param value 	the minimum values
   */
  public void setMinimumValues(BaseDouble[] value) {
    m_MinimumValues = value;
    reset();
  }

  /**
   * Returns the minimum values for the fields.
   *
   * @return 		the minimum values
   */
  public BaseDouble[] getMinimumValues() {
    return m_MinimumValues;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String minimumValuesTipText() {
    return "The required minimum values for the fields in the sample data (-1 is unbounded).";
  }

  /**
   * Sets the maximum values to the fields.
   *
   * @param value 	the maximum values
   */
  public void setMaximumValues(BaseDouble[] value) {
    m_MaximumValues = value;
    reset();
  }

  /**
   * Returns the maximum values for the fields.
   *
   * @return 		the maximum values
   */
  public BaseDouble[] getMaximumValues() {
    return m_MaximumValues;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String maximumValuesTipText() {
    return "The required maximum values for the fields in the sample data (-1 is unbounded).";
  }

  /**
   * Sets the required fields.
   *
   * @param value 	the fields
   */
  public void setRequiredFields(Field[] value) {
    m_RequiredFields = value;
    reset();
  }

  /**
   * Returns the required fields.
   *
   * @return 		the fields
   */
  public Field[] getRequiredFields() {
    return m_RequiredFields;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String requiredFieldsTipText() {
    return "The fields that are required to be present in the report, regardless of values they have.";
  }

  /**
   * Automatically corrects values.
   */
  @Override
  protected void update() {
    super.update();
    
    if (m_Fields == null)
      m_Fields = new Field[0];

    m_MinimumValues = (BaseDouble[]) Utils.adjustArray(m_MinimumValues, m_Fields.length, new BaseDouble("-1.0"));
    m_MaximumValues = (BaseDouble[]) Utils.adjustArray(m_MaximumValues, m_Fields.length, new BaseDouble("-1.0"));

    if (m_RequiredFields == null)
      m_RequiredFields = new Field[0];
  }
}
