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
 * MultiColumnSpreadSheetSampleDataReader.java
 * Copyright (C) 2024 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input;

import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spreadsheet.Cell;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.spreadsheet.SpreadSheetColumnIndex;
import adams.data.spreadsheet.SpreadSheetColumnRange;
import adams.data.spreadsheet.rowfinder.AllFinder;
import adams.data.spreadsheet.rowfinder.RowFinder;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Reads sample data from a spreadsheet (format depends on reader), reference values stored in the specified range of columns and the column headers representing the reference value names.
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
 * <pre>-col-id &lt;adams.data.spreadsheet.SpreadSheetColumnIndex&gt; (property: columnSampleID)
 * &nbsp;&nbsp;&nbsp;The column containing the sample ID.
 * &nbsp;&nbsp;&nbsp;default: ID
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-cols-sampledata &lt;adams.data.spreadsheet.SpreadSheetColumnRange&gt; (property: columnsSampleData)
 * &nbsp;&nbsp;&nbsp;The columns with the reference values; header represents the reference name.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-cols-numeric &lt;adams.data.spreadsheet.SpreadSheetColumnRange&gt; (property: columnsNumeric)
 * &nbsp;&nbsp;&nbsp;The columns with numeric reference values.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-cols-bool &lt;adams.data.spreadsheet.SpreadSheetColumnRange&gt; (property: columnsBoolean)
 * &nbsp;&nbsp;&nbsp;The columns with boolean reference values.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-row-finder &lt;adams.data.spreadsheet.rowfinder.RowFinder&gt; (property: rowFinder)
 * &nbsp;&nbsp;&nbsp;The row finder to use for locating the rows to import.
 * &nbsp;&nbsp;&nbsp;default: adams.data.spreadsheet.rowfinder.AllFinder
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class MultiColumnSpreadSheetSampleDataReader
  extends AbstractSampleDataReader {

  /** for serialization. */
  private static final long serialVersionUID = -3534989186717843663L;

  /** the spreadsheet reader. */
  protected SpreadSheetReader m_Reader;

  /** the column name to get the sample ID from. */
  protected SpreadSheetColumnIndex m_ColumnSampleID;

  /** the range of columns with the sample data. */
  protected SpreadSheetColumnRange m_ColumnsSampleData;

  /** the range of columns with numeric sample data. */
  protected SpreadSheetColumnRange m_ColumnsNumeric;

  /** the range of columns with boolean sample data. */
  protected SpreadSheetColumnRange m_ColumnsBoolean;

  /** for locating the rows to import. */
  protected RowFinder m_RowFinder;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads sample data from a spreadsheet (format depends on reader), reference values stored in the "
	     + "specified range of columns and the column headers representing the reference value names.";
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
      new SpreadSheetColumnIndex("ID"));

    m_OptionManager.add(
      "cols-sampledata", "columnsSampleData",
      new SpreadSheetColumnRange(""));

    m_OptionManager.add(
      "cols-numeric", "columnsNumeric",
      new SpreadSheetColumnRange(""));

    m_OptionManager.add(
      "cols-bool", "columnsBoolean",
      new SpreadSheetColumnRange(""));

    m_OptionManager.add(
      "row-finder", "rowFinder",
      new AllFinder());
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Sample data in spreadsheet format (row-wise, one ref per col)";
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
   * Sets the column with the sample ID.
   *
   * @param value 	the column
   */
  public void setColumnSampleID(SpreadSheetColumnIndex value) {
    m_ColumnSampleID = value;
    reset();
  }

  /**
   * Returns the column with the sample ID.
   *
   * @return 		the column
   */
  public SpreadSheetColumnIndex getColumnSampleID() {
    return m_ColumnSampleID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnSampleIDTipText() {
    return "The column containing the sample ID.";
  }

  /**
   * Sets the range of columns with the reference values.
   *
   * @param value 	the columns
   */
  public void setColumnsSampleData(SpreadSheetColumnRange value) {
    m_ColumnsSampleData = value;
    reset();
  }

  /**
   * Returns the columns with the reference values.
   *
   * @return 		the columns
   */
  public SpreadSheetColumnRange getColumnsSampleData() {
    return m_ColumnsSampleData;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnsSampleDataTipText() {
    return "The columns with the reference values; header represents the reference name.";
  }

  /**
   * Sets the range of columns with numeric reference values.
   *
   * @param value 	the columns
   */
  public void setColumnsNumeric(SpreadSheetColumnRange value) {
    m_ColumnsNumeric = value;
    reset();
  }

  /**
   * Returns the columns with numeric reference values.
   *
   * @return 		the columns
   */
  public SpreadSheetColumnRange getColumnsNumeric() {
    return m_ColumnsNumeric;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnsNumericTipText() {
    return "The columns with numeric reference values.";
  }

  /**
   * Sets the range of columns with boolean reference values.
   *
   * @param value 	the columns
   */
  public void setColumnsBoolean(SpreadSheetColumnRange value) {
    m_ColumnsBoolean = value;
    reset();
  }

  /**
   * Returns the columns with boolean reference values.
   *
   * @return 		the columns
   */
  public SpreadSheetColumnRange getColumnsBoolean() {
    return m_ColumnsBoolean;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnsBooleanTipText() {
    return "The columns with boolean reference values.";
  }

  /**
   * Sets the row finder to use for locating the rows to import.
   *
   * @param value 	the finder
   */
  public void setRowFinder(RowFinder value) {
    m_RowFinder = value;
    reset();
  }

  /**
   * Returns the row finder to use for locating the rows to import.
   *
   * @return 		the finder
   */
  public RowFinder getRowFinder() {
    return m_RowFinder;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rowFinderTipText() {
    return "The row finder to use for locating the rows to import.";
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
   * Performs the actual reading.
   *
   * @return		the reports that were read
   */
  @Override
  protected List<SampleData> readData() {
    List<SampleData> 		result;
    List<Row>			rows;
    SpreadSheet			sheet;
    int				i;
    int				colID;
    int[]			colsSD;
    char[]			colsType;
    SampleData			sd;
    Field[]			fields;
    Cell			cell;
    boolean			any;

    result = new ArrayList<>();

    // load file
    sheet = m_Reader.read(m_Input.getAbsolutePath());

    // locate columns
    m_ColumnSampleID.setSpreadSheet(sheet);
    colID = m_ColumnSampleID.getIntIndex();
    if (colID == -1)
      throw new IllegalStateException("Failed to locate sample ID column: " + m_ColumnSampleID.getIndex());
    m_ColumnsSampleData.setSpreadSheet(sheet);
    colsSD = m_ColumnsSampleData.getIntIndices();
    if (colsSD.length == 0)
      throw new IllegalStateException("No sample data columns found: " + m_ColumnsSampleData.getRange());

    // column types
    colsType = new char[sheet.getColumnCount()];
    for (i = 0; i < colsType.length; i++)
      colsType[i] = 'S';
    m_ColumnsNumeric.setSpreadSheet(sheet);
    for (int index: m_ColumnsNumeric.getIntIndices())
      colsType[index] = 'N';
    m_ColumnsBoolean.setSpreadSheet(sheet);
    for (int index: m_ColumnsBoolean.getIntIndices())
      colsType[index] = 'B';

    // fields
    fields = new Field[colsType.length];
    for (i = 0; i < colsType.length; i++) {
      if (colsType[i] == 'N')
	fields[i] = new Field(sheet.getColumnName(i), DataType.NUMERIC);
      else if (colsType[i] == 'B')
	fields[i] = new Field(sheet.getColumnName(i), DataType.BOOLEAN);
      else
	fields[i] = new Field(sheet.getColumnName(i), DataType.STRING);
    }

    // rows to process
    rows = new ArrayList<>();
    if (m_RowFinder instanceof AllFinder) {
      rows.addAll(sheet.rows());
    }
    else {
      for (int index: m_RowFinder.findRows(sheet))
	rows.add(sheet.getRow(index));
    }

    // read data
    for (Row row: rows) {
      if (m_Stopped)
	break;

      sd = newInstance();
      sd.setID(row.getCell(colID).getContent().trim());
      any = false;
      for (int index: colsSD) {
	if (index == colID)
	  continue;
	if (!row.hasCell(index))
	  continue;
	cell = row.getCell(index);
	if (cell.isMissing())
	  continue;
	sd.addField(fields[index]);
	sd.setValue(fields[index], cell.getContent());
	any = true;
      }
      if (any)
	result.add(sd);
    }

    return result;
  }
}
