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
 * ReportFieldFromStringValue.java
 * Copyright (C) 2018-2020 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumfilter;

import adams.core.base.BaseRegExp;
import adams.data.conversion.Conversion;
import adams.data.conversion.ObjectToObject;
import adams.data.filter.AbstractFilter;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.Report;
import adams.data.report.ReportHandler;
import adams.data.spectrum.Spectrum;

/**
 * Sets a report field with the value extracted from another string field via a
 * regular expression.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ReportFieldFromStringValue
  extends AbstractFilter<Spectrum> {

  private static final long serialVersionUID = -1463998138621419940L;

  /** the source field. */
  protected Field m_Source;

  /** the regular expression to apply. */
  protected BaseRegExp m_RegExp;

  /** the group to use as value for the target field. */
  protected String m_Group;

  /** the conversion to apply. */
  protected Conversion m_Conversion;

  /** the target field. */
  protected Field m_Target;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Sets a report field with the value extracted from another string "
      + "field via a regular expression. The specified conversion gets applied "
      + "to the extracted value before setting the field in the report.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "source", "source",
      new Field("Source", DataType.STRING));

    m_OptionManager.add(
      "regexp", "regExp",
      new BaseRegExp("(.*)"));

    m_OptionManager.add(
      "group", "group",
      "$1");

    m_OptionManager.add(
      "conversion", "conversion",
      new ObjectToObject());

    m_OptionManager.add(
      "target", "target",
      new Field("Target", DataType.STRING));
  }

  /**
   * Sets the source field in the report to get the string to apply the
   * regular expression to.
   *
   * @param value	the field
   */
  public void setSource(Field value) {
    m_Source = value;
    reset();
  }

  /**
   * Returns the source field in the report to get the string to apply the
   * regular expression to.
   *
   * @return		the field
   */
  public Field getSource() {
    return m_Source;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sourceTipText() {
    return "The source field in the report to get the string to apply the regular expression to.";
  }

  /**
   * Sets the regular expression to apply for identifying the group(s) to
   * extract from the string.
   *
   * @param value	the expression
   */
  public void setRegExp(BaseRegExp value) {
    m_RegExp = value;
    reset();
  }

  /**
   * Returns the regular expression to apply for identifying the group(s) to
   * extract from the string.
   *
   * @return		the expression
   */
  public BaseRegExp getRegExp() {
    return m_RegExp;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String regExpTipText() {
    return "The regular expression to apply for identifying the group(s) to extract from the string.";
  }

  /**
   * Sets the group expression for generating the value to be stored.
   *
   * @param value	the group expression
   */
  public void setGroup(String value) {
    m_Group = value;
    reset();
  }

  /**
   * Returns group expression for generating the value to be stored.
   *
   * @return		the group expression
   */
  public String getGroup() {
    return m_Group;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String groupTipText() {
    return "The group expression for generating the value to be stored (eg '$1' or '$1-$3.$2').";
  }

  /**
   * Sets the conversion to apply to the extracted string value.
   *
   * @param value	the conversion
   */
  public void setConversion(Conversion value) {
    m_Conversion = value;
    reset();
  }

  /**
   * Returns the conversion to apply to the extracted string value.
   *
   * @return		the conversion
   */
  public Conversion getConversion() {
    return m_Conversion;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String conversionTipText() {
    return "The conversion to apply to the extracted string value.";
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
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  protected Spectrum processData(Spectrum data) {
    Report	report;
    String	str;
    String 	extracted;
    Object	value;
    String	msg;

    report = ((ReportHandler) data).getReport();

    if (!report.hasField(m_Source)) {
      getLogger().warning("Source field '" + m_Source + "' not found: " + data);
      return data;
    }

    str = "" + report.getValue(m_Source);
    if (m_RegExp.isMatch(str)) {
      extracted = str.replaceAll(m_RegExp.getValue(), m_Group);
      if (isLoggingEnabled())
        getLogger().info("Extraction: '" + str + "' -> '" + extracted + "'");

      m_Conversion.setInput(extracted);
      msg = m_Conversion.convert();
      if (msg != null) {
        getLogger().severe(msg);
      }
      else {
        value = m_Conversion.getOutput();
        if (isLoggingEnabled())
          getLogger().info("Conversion: '" + extracted + "' -> " + value);
        report.setValue(m_Target, value);
      }
      m_Conversion.cleanUp();
    }
    else {
      if (isLoggingEnabled())
        getLogger().info("regexp '" + m_RegExp + "' does not match '" + str + "'");
    }

    return data;
  }
}
