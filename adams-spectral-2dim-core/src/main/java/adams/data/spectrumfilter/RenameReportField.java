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
 * RenameReportField.java
 * Copyright (C) 2026 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumfilter;

import adams.core.Utils;
import adams.data.filter.AbstractFilter;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;

/**
 <!-- globalinfo-start -->
 * Replaces the old field names with the new ones.
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
 * <pre>-field-old &lt;adams.data.report.Field&gt; [-field-old ...] (property: fieldsOld)
 * &nbsp;&nbsp;&nbsp;The olf fields to rename.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-field-new &lt;adams.data.report.Field&gt; [-field-new ...] (property: fieldsNew)
 * &nbsp;&nbsp;&nbsp;The new fields to use.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class RenameReportField
  extends AbstractFilter<Spectrum> {

  private static final long serialVersionUID = -1463998138621419940L;

  /** the old field(s). */
  protected Field[] m_FieldsOld;

  /** the new field(s). */
  protected Field[] m_FieldsNew;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Replaces the old field names with the new ones.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "field-old", "fieldsOld",
      new Field[0]);

    m_OptionManager.add(
      "field-new", "fieldsNew",
      new Field[0]);
  }

  /**
   * Sets the old fields to rename.
   *
   * @param value	the old fields
   */
  public void setFieldsOld(Field[] value) {
    m_FieldsOld = value;
    m_FieldsNew = (Field[]) Utils.adjustArray(m_FieldsNew, m_FieldsOld.length, new Field());
    reset();
  }

  /**
   * Returns the old fields to rename.
   *
   * @return		the old fields
   */
  public Field[] getFieldsOld() {
    return m_FieldsOld;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String fieldsOldTipText() {
    return "The olf fields to rename.";
  }

  /**
   * Sets the new fields to use.
   *
   * @param value	the new fields
   */
  public void setFieldsNew(Field[] value) {
    m_FieldsNew = value;
    m_FieldsOld = (Field[]) Utils.adjustArray(m_FieldsOld, m_FieldsNew.length, new Field());
    reset();
  }

  /**
   * Returns the new fields to use.
   *
   * @return		the new fields
   */
  public Field[] getFieldsNew() {
    return m_FieldsNew;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String fieldsNewTipText() {
    return "The new fields to use.";
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
    SampleData 	sd;
    int		i;
    Object	value;

    result   = (Spectrum) data.getClone();
    sd       = result.getReport();

    for (i = 0; i < m_FieldsOld.length; i++) {
      if (sd.hasValue(m_FieldsOld[i])) {
	if (isLoggingEnabled())
	  getLogger().info("Renaming " + m_FieldsOld[i] + " to " + m_FieldsNew[i]);
	value = sd.removeValue(m_FieldsOld[i]);
	sd.setValue(m_FieldsNew[i], value);
      }
    }

    return result;
  }
}
