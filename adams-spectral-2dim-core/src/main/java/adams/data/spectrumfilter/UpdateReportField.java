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
 * UpdateReportField.java
 * Copyright (C) 2026 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;

/**
 <!-- globalinfo-start -->
 * Updates the specified report field with the provided value.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * &nbsp;&nbsp;&nbsp;min-user-mode: Expert
 * </pre>
 *
 * <pre>-no-id-update &lt;boolean&gt; (property: dontUpdateID)
 * &nbsp;&nbsp;&nbsp;If enabled, suppresses updating the ID of adams.data.id.IDHandler data containers.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-no-processing-info-update &lt;boolean&gt; (property: dontUpdateProcessingInfo)
 * &nbsp;&nbsp;&nbsp;If enabled, suppresses updating the processing information of adams.data.NotesHandler
 * &nbsp;&nbsp;&nbsp;data containers.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-field &lt;adams.data.report.Field&gt; (property: field)
 * &nbsp;&nbsp;&nbsp;The field to update in the report.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-value &lt;java.lang.String&gt; (property: value)
 * &nbsp;&nbsp;&nbsp;The value to use for the report field.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 <!-- options-end -->
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class UpdateReportField
  extends AbstractFilter<Spectrum> {

  private static final long serialVersionUID = 1798831616767361030L;

  /** the report field to update. */
  protected Field m_Field;

  /** the value to set. */
  protected String m_Value;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Updates the specified report field with the provided value.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "field", "field",
      new Field());

    m_OptionManager.add(
      "value", "value",
      "");
  }

  /**
   * Sets field to update.
   *
   * @param value	the field
   */
  public void setField(Field value) {
    m_Field = value;
    reset();
  }

  /**
   * Returns the field to update.
   *
   * @return		the field
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
    return "The field to update in the report.";
  }

  /**
   * Sets the value to set in the report.
   *
   * @param value	the value
   */
  public void setValue(String value) {
    m_Value = value;
    reset();
  }

  /**
   * Returns the value to set in the report.
   *
   * @return		the value
   */
  public String getValue() {
    return m_Value;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String valueTipText() {
    return "The value to use for the report field.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data the data to filter
   * @return the filtered data
   */
  @Override
  protected Spectrum processData(Spectrum data) {
    Spectrum	result;

    result = (Spectrum) data.getClone();

    if (m_Field.getName() == null) {
      getLogger().warning("No field specified!");
      return result;
    }

    if (!result.hasReport())
      result.setReport(new SampleData());
    result.getReport().addField(m_Field);
    result.getReport().setValue(m_Field, m_Value);
    return result;
  }
}
