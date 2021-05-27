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
 * SampleDataOnlyRowGenerator.java
 * Copyright (C) 2021 University of Waikato, Hamilton, NZ
 */

package adams.data.spreadsheetrowgenerator;

import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;

/**
 * Outputs only sample data values.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SampleDataOnlyRowGenerator
  extends AbstractSpreadSheetRowGenerator {

  private static final long serialVersionUID = -565276918178382521L;

  /** fields to add to the output data. */
  protected Field[] m_Fields;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Outputs only sample data values.";
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "field", "fields",
      new Field[0]);
  }

  /**
   * Sets the fields to add.
   *
   * @param value	the fields
   */
  public void setFields(Field[] value) {
    m_Fields = value;
    reset();
  }

  /**
   * Returns the fields to add.
   *
   * @return		the fields
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
    return "The fields to add to the output.";
  }

  /**
   * Checks whether the number of waves are the same.
   *
   * @param data	the input data
   */
  protected void checkHeader(Spectrum data) {
    int		size;

    size = m_OutputHeader.getColumnCount();
    if (m_AddDatabaseID)
      size--;
    if (m_AddSampleID)
      size--;

    if (size != m_Fields.length)
      throw new IllegalStateException(
	  "Number of fields and output columns differ "
            + "(#" + data.getDatabaseID() + "/" + data.getID() + "/" + data.getFormat() + "): "
            + m_Fields.length + " != " + size);
  }

  /**
   * Generates the header of the output data.
   *
   * @param data the input data
   */
  @Override
  protected void generateHeader(Spectrum data) {
    SpreadSheet  	header;
    Row			row;
    int			i;

    header = new DefaultSpreadSheet();
    row    = header.getHeaderRow();

    for (i = 0; i < m_Fields.length; i++)
      row.addCell("" + i + "-" + m_Fields[i].getName()).setContentAsString(m_Fields[i].getName());

    m_OutputHeader = header;
  }

  /**
   * Generates the actual data.
   *
   * @param data the input data to transform
   * @return the generated data
   */
  @Override
  protected Row generateOutput(Spectrum data) {
    Row		result;
    int		i;
    SampleData	sd;
    String	index;

    result = m_OutputHeader.addRow();
    sd     = data.getReport();
    for (i = 0; i < m_Fields.length; i++) {
      index = "" + i + "-" + m_Fields[i].getName();
      if (sd.hasValue(m_Fields[i])) {
        switch (m_Fields[i].getDataType()) {
	  case NUMERIC:
	    result.addCell(index).setContent(sd.getDoubleValue(m_Fields[i]));
	    break;
	  case BOOLEAN:
	    result.addCell(index).setContent(sd.getBooleanValue(m_Fields[i]));
	    break;
	  default:
	    result.addCell(index).setContentAsString(sd.getStringValue(m_Fields[i]));
	    break;
	}
      }
    }

    m_OutputHeader.removeRow(m_OutputHeader.getRowCount() - 1);

    return result;
  }
}
