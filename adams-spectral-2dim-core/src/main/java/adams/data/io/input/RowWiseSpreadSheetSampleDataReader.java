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
 * RowWiseSpreadSheetSampleDataReader.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input;

import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spreadsheet.Cell;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Reads a sample data file from a spreadsheet (format depends on reader), one reference value per row.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-input &lt;adams.core.io.PlaceholderFile&gt; (property: input)
 * &nbsp;&nbsp;&nbsp;The file to read and turn into a report.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 *
 * <pre>-offline &lt;boolean&gt; (property: offline)
 * &nbsp;&nbsp;&nbsp;If set to true, the database won't get queried, e.g., for obtaining the
 * &nbsp;&nbsp;&nbsp;parent ID.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-format &lt;java.lang.String&gt; (property: format)
 * &nbsp;&nbsp;&nbsp;The type of spectrum to use (used internally to determine the database ID
 * &nbsp;&nbsp;&nbsp;of the spectrum).
 * &nbsp;&nbsp;&nbsp;default: NIR
 * </pre>
 *
 * <pre>-reader &lt;adams.data.io.input.SpreadSheetReader&gt; (property: reader)
 * &nbsp;&nbsp;&nbsp;The spreadsheet reader to use.
 * &nbsp;&nbsp;&nbsp;default: adams.data.io.input.CsvSpreadSheetReader -data-row-type adams.data.spreadsheet.DenseDataRow -spreadsheet-type adams.data.spreadsheet.DefaultSpreadSheet
 * </pre>
 *
 * <pre>-col-id &lt;java.lang.String&gt; (property: columnSampleID)
 * &nbsp;&nbsp;&nbsp;The name of the column containing the sample ID.
 * &nbsp;&nbsp;&nbsp;default: SampID
 * </pre>
 *
 * <pre>-col-type &lt;java.lang.String&gt; (property: columnSampleType)
 * &nbsp;&nbsp;&nbsp;The name of the column containing the sample type.
 * &nbsp;&nbsp;&nbsp;default: TYPE
 * </pre>
 *
 * <pre>-col-measurement &lt;java.lang.String&gt; (property: columnMeasurementName)
 * &nbsp;&nbsp;&nbsp;The name of the column containing the reference value name.
 * &nbsp;&nbsp;&nbsp;default: MEASUREMENT
 * </pre>
 *
 * <pre>-col-value &lt;java.lang.String&gt; (property: columnMeasurementValue)
 * &nbsp;&nbsp;&nbsp;The name of the column containing the reference value.
 * &nbsp;&nbsp;&nbsp;default: VALUE
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class RowWiseSpreadSheetSampleDataReader
  extends AbstractSampleDataReader {

  /** for serialization. */
  private static final long serialVersionUID = -3534989186717843663L;

  /** the spreadsheet reader. */
  protected SpreadSheetReader m_Reader;

  /** the column name to get the sample ID from. */
  protected String m_ColumnSampleID;

  /** the column name that stores the sample type. */
  protected String m_ColumnSampleType;

  /** the column name that stores the reference value name. */
  protected String m_ColumnMeasurementName;

  /** the column name that stores the reference value. */
  protected String m_ColumnMeasurementValue;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads a sample data file from a spreadsheet (format depends on reader), one reference value per row.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "reader", "reader",
      new CsvSpreadSheetReader());

    m_OptionManager.add(
      "col-id", "columnSampleID",
      "SampID");

    m_OptionManager.add(
      "col-type", "columnSampleType",
      "TYPE");

    m_OptionManager.add(
      "col-measurement", "columnMeasurementName",
      "MEASUREMENT");

    m_OptionManager.add(
      "col-value", "columnMeasurementValue",
      "VALUE");
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Sample data spreadsheet file format (row-wise)";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return m_Reader.getFormatExtensions();
  }

  /**
   * Sets the spreadsheet reader to use.
   *
   * @param value 	the reader
   */
  public void setReader(SpreadSheetReader value) {
    m_Reader = value;
    reset();
  }

  /**
   * Returns the spreadsheet reader in use.
   *
   * @return 		the reader
   */
  public SpreadSheetReader getReader() {
    return m_Reader;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String readerTipText() {
    return "The spreadsheet reader to use.";
  }

  /**
   * Sets the name of the column with the sample ID.
   *
   * @param value 	the column name
   */
  public void setColumnSampleID(String value) {
    m_ColumnSampleID = value;
    reset();
  }

  /**
   * Returns the name of the column with the sample ID.
   *
   * @return 		the column name
   */
  public String getColumnSampleID() {
    return m_ColumnSampleID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnSampleIDTipText() {
    return "The name of the column containing the sample ID.";
  }

  /**
   * Sets the name of the column with the sample type.
   *
   * @param value 	the column name
   */
  public void setColumnSampleType(String value) {
    m_ColumnSampleType = value;
    reset();
  }

  /**
   * Returns the name of the column with the sample type.
   *
   * @return 		the column name
   */
  public String getColumnSampleType() {
    return m_ColumnSampleType;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnSampleTypeTipText() {
    return "The name of the column containing the sample type.";
  }

  /**
   * Sets the name of the column with the reference value name.
   *
   * @param value 	the column name
   */
  public void setColumnMeasurementName(String value) {
    m_ColumnMeasurementName = value;
    reset();
  }

  /**
   * Returns the name of the column with the reference value name.
   *
   * @return 		the column name
   */
  public String getColumnMeasurementName() {
    return m_ColumnMeasurementName;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnMeasurementNameTipText() {
    return "The name of the column containing the reference value name.";
  }

  /**
   * Sets the name of the column with the reference value.
   *
   * @param value 	the column name
   */
  public void setColumnMeasurementValue(String value) {
    m_ColumnMeasurementValue = value;
    reset();
  }

  /**
   * Returns the name of the column with the reference value.
   *
   * @return 		the column name
   */
  public String getColumnMeasurementValue() {
    return m_ColumnMeasurementValue;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnMeasurementValueTipText() {
    return "The name of the column containing the reference value.";
  }

  /**
   * Returns a new instance of the report class in use.
   *
   * @return		the new (empty) report
   */
  @Override
  public SampleData newInstance() {
    return new SampleData();
  }

  /**
   * Locates the column and returns the key for the column.
   *
   * @param sheet	the spread sheet to work on
   * @param column	the column to locate
   * @param fail	if true then a IllegalStateException execption is
   * 			thrown in case the column cannot be located
   * @return		the column key or null if not found
   */
  protected String locateColumn(SpreadSheet sheet, String column, boolean fail) {
    String		result;
    Row			row;
    Cell		cell;
    String		msg;

    result = null;
    if ((sheet.getColumnCount() == 0) || (column.length() == 0))
      return result;

    row = sheet.getHeaderRow();
    for (String key: row.cellKeys()) {
      cell = row.getCell(key);
      if (cell.getContent().trim().equals(column)) {
	result = key;
	break;
      }
    }

    if (result == null) {
      msg = "Failed to locate column: " + column;
      if (fail)
	throw new IllegalStateException(msg);
      else
	getLogger().severe(msg);
    }

    return result;
  }

  /**
   * Performs the actual reading.
   *
   * @return		the reports that were read
   */
  @Override
  protected List<SampleData> readData() {
    List<SampleData> 		result;
    SpreadSheet			sheet;
    Row				row;
    int				i;
    String			keySampleID;
    String			keySampleType;
    String			keyMeasurementName;
    String			keyMeasurementValue;
    SampleData			sd;
    Field			field;
    String			sampleKey;
    String			sampleKeyNew;

    result = new ArrayList<>();

    // load file
    sheet = m_Reader.read(m_Input.getAbsolutePath());

    // locate columns
    keySampleID         = locateColumn(sheet, m_ColumnSampleID, true);
    keySampleType       = locateColumn(sheet, m_ColumnSampleType, true);
    keyMeasurementName  = locateColumn(sheet, m_ColumnMeasurementName, true);
    keyMeasurementValue = locateColumn(sheet, m_ColumnMeasurementValue, true);

    // read data
    sampleKey = null;
    sd        = null;
    for (i = 0; i < sheet.getRowCount(); i++) {
      if (m_Stopped)
	break;
      
      row = sheet.getRow(i);

      if (row.getCell(0).getContent().trim().equals("="))
	continue;

      sampleKeyNew = row.getCell(keySampleID).getContent().trim() + "\t" + row.getCell(keySampleType).getContent().trim();
      if ((sampleKey == null) || !sampleKey.equals(sampleKeyNew)) {
	sampleKey = sampleKeyNew;
	sd        = newInstance();
	sd.addParameter(SampleData.SAMPLE_ID, row.getCell(keySampleID).getContent().trim());
	sd.addParameter(SampleData.SAMPLE_TYPE, row.getCell(keySampleType).getContent().trim());
	result.add(sd);
      }

      field = new Field(row.getCell(keyMeasurementName).getContent().trim(), DataType.NUMERIC);
      sd.addField(field);
      try {
	sd.setValue(field, Double.parseDouble(row.getCell(keyMeasurementValue).getContent().trim()));
      }
      catch (Exception e) {
	getLogger().log(Level.SEVERE, "Failed to convert measurement value in row #" + (i + 1) + ": " + row.getCell(keyMeasurementValue).getContent().trim(), e);
      }
    }

    return result;
  }
}
