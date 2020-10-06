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
 * ReportFieldFromStringValueWithLookup.java
 * Copyright (C) 2018-2020 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumfilter;

import adams.core.base.BaseKeyValuePair;
import adams.core.base.BaseRegExp;
import adams.data.filter.AbstractFilter;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.Report;
import adams.data.report.ReportHandler;
import adams.data.spectrum.Spectrum;

import java.util.HashMap;
import java.util.Map;

/**
 * Extracts the first group from the regular expression matched
 * against the report field value and uses this value as key in the
 * provided lookup table generated from the specified key-value pairs.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ReportFieldFromStringValueWithLookup
  extends AbstractFilter<Spectrum> {

  private static final long serialVersionUID = 752950660636409501L;

  /** the report field to read from. */
  protected Field m_Source;

  /** the regexp to extract the lookup key (uses 1st group). */
  protected BaseRegExp m_Extract;

  /** the key-value pairs. */
  protected BaseKeyValuePair[] m_Lookup;

  /** the report field to store the result in. */
  protected Field m_Target;

  /** the lookup table. */
  protected transient Map<String,String> m_Table;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Extracts the first group from the regular expression matched "
      + "against the report field value and uses this value as key in the "
      + "provided lookup table generated from the specified key-value pairs.";
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
      "extract", "extract",
      new BaseRegExp("^([A-Z])_.*"));

    m_OptionManager.add(
      "lookup", "lookup",
      new BaseKeyValuePair[0]);

    m_OptionManager.add(
      "target", "target",
      new Field("Sample Type", DataType.STRING));
  }

  /**
   * Resets the scheme.
   */
  @Override
  public void reset() {
    super.reset();

    m_Table = null;
  }

  /**
   * Sets the source field to use.
   *
   * @param value	the field
   */
  public void setSource(Field value) {
    m_Source = value;
    reset();
  }

  /**
   * Returns the source field to use.
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
    return "The field in the report to use as source.";
  }

  /**
   * Sets the regexp for extracting the substring to use in the lookup.
   *
   * @param value	the expression
   */
  public void setExtract(BaseRegExp value) {
    m_Extract = value;
    reset();
  }

  /**
   * Returns the regexp for extracting the substring to use in the lookup.
   *
   * @return		the expression
   */
  public BaseRegExp getExtract() {
    return m_Extract;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String extractTipText() {
    return "The regexp for extracting the substring to use in the lookup (uses the 1st group).";
  }

  /**
   * Sets the key-value pairs that make up the lookup table.
   *
   * @param value	the pairs
   */
  public void setLookup(BaseKeyValuePair[] value) {
    m_Lookup = value;
    reset();
  }

  /**
   * Returns the key-value pairs that make up the lookup table.
   *
   * @return		the pairs
   */
  public BaseKeyValuePair[] getLookup() {
    return m_Lookup;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String lookupTipText() {
    return "The key-value pairs that make up the lookup table.";
  }

  /**
   * Sets the target field to use for the result.
   *
   * @param value	the field
   */
  public void setTarget(Field value) {
    m_Target = value;
    reset();
  }

  /**
   * Returns the target field to use for the result.
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
    return "The field in the report to store the result in.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  protected Spectrum processData(Spectrum data) {
    Report	report;
    String 	key;
    String	value;

    // construct lookup table
    if (m_Table == null) {
      m_Table = new HashMap<>();
      for (BaseKeyValuePair pair: m_Lookup)
        m_Table.put(pair.getPairKey(), pair.getPairValue());
    }

    report = ((ReportHandler) data).getReport();

    if (report != null) {
      if (report.hasValue(m_Source)) {
	key = "" + report.getValue(m_Source);
	// extract 1st group
	key = key.replaceAll(m_Extract.getValue(), "$1");
	if (isLoggingEnabled())
	  getLogger().info("Extracted key: " + key);
	// determine value
	if (m_Table.containsKey(key)) {
	  value = m_Table.get(key);
	  if (isLoggingEnabled())
	    getLogger().info("Match found for '" + key + "': " + value);
	  report.addField(m_Target);
	  report.setValue(m_Target, value);
	}
	else {
	  getLogger().warning("No match found for: " + key);
	}
      }
      else {
	getLogger().warning("Failed to locate field: " + m_Source);
      }
    }

    return data;
  }
}
