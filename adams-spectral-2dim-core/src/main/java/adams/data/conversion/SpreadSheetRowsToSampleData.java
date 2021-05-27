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
 * SpreadSheetRowsToSampleData.java
 * Copyright (C) 2021 University of Waikato, Hamilton, New Zealand
 */
package adams.data.conversion;

import adams.core.QuickInfoHelper;
import adams.data.sampledata.SampleData;
import adams.data.spreadsheet.Cell;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.spreadsheet.SpreadSheetColumnIndex;
import adams.data.spreadsheet.SpreadSheetColumnRange;
import adams.data.spreadsheet.SpreadSheetRowIndex;
import adams.data.spreadsheet.SpreadSheetRowRange;

/**
 <!-- globalinfo-start -->
 * Turns spreadsheet rows into sample data.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-row-sampledata-names &lt;adams.data.spreadsheet.SpreadSheetRowIndex&gt; (property: rowSampleDataNames)
 * &nbsp;&nbsp;&nbsp;The (optional) row that contains the sample data names.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 *
 * <pre>-sampledata-names-in-header &lt;boolean&gt; (property: sampleDataNamesInHeader)
 * &nbsp;&nbsp;&nbsp;Whether the sample data names are stored in the header.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-rows-sampledata-values &lt;adams.data.spreadsheet.SpreadSheetRowRange&gt; (property: rowsSampleDataValues)
 * &nbsp;&nbsp;&nbsp;The rows in the spreadsheet that contain the sample data values.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 *
 * <pre>-cols-sampledata &lt;adams.data.spreadsheet.SpreadSheetColumnRange&gt; (property: columnsSampleData)
 * &nbsp;&nbsp;&nbsp;The columns that contain sampledata.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-col-id &lt;adams.data.spreadsheet.SpreadSheetColumnIndex&gt; (property: columnID)
 * &nbsp;&nbsp;&nbsp;The (optional) column that contains the sample ID.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpreadSheetRowsToSampleData
  extends AbstractConversion {

  /** for serialization. */
  private static final long serialVersionUID = -258589003642261978L;

  /** the column with the sample data names. */
  protected SpreadSheetRowIndex m_RowSampleDataNames;

  /** whether the sample data names are in the header row. */
  protected boolean m_SampleDataNamesInHeader;

  /** the rows with sample data values. */
  protected SpreadSheetRowRange m_RowsSampleDataValues;

  /** the rows to get the sample data from. */
  protected SpreadSheetColumnRange m_ColumnsSampleData;

  /** the (optional) row with the sample ID. */
  protected SpreadSheetColumnIndex m_ColumnID;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Turns spreadsheet rows into sample data.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "row-sampledata-names", "rowSampleDataNames",
      new SpreadSheetRowIndex());

    m_OptionManager.add(
      "sampledata-names-in-header", "sampleDataNamesInHeader",
      false);

    m_OptionManager.add(
      "rows-sampledata-values", "rowsSampleDataValues",
      new SpreadSheetRowRange());

    m_OptionManager.add(
      "cols-sampledata", "columnsSampleData",
      new SpreadSheetColumnRange());

    m_OptionManager.add(
      "col-id", "columnID",
      new SpreadSheetColumnIndex());
  }

  /**
   * Sets the (optional) column that contains the sample ID.
   *
   * @param value	the column
   */
  public void setColumnID(SpreadSheetColumnIndex value) {
    m_ColumnID = value;
    reset();
  }

  /**
   * Returns the (optional) column that contains the sample ID.
   *
   * @return 		the column
   */
  public SpreadSheetColumnIndex getColumnID() {
    return m_ColumnID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnIDTipText() {
    return "The (optional) column that contains the sample ID.";
  }

  /**
   * Sets the row that contains the sample data names.
   *
   * @param value	the row
   */
  public void setRowSampleDataNames(SpreadSheetRowIndex value) {
    m_RowSampleDataNames = value;
    reset();
  }

  /**
   * Returns the row that contains the sample data names.
   *
   * @return 		the row
   */
  public SpreadSheetRowIndex getRowSampleDataNames() {
    return m_RowSampleDataNames;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rowSampleDataNamesTipText() {
    return "The (optional) row that contains the sample data names.";
  }

  /**
   * Sets whether the wave numbers are in the header.
   *
   * @param value	true if in header
   */
  public void setSampleDataNamesInHeader(boolean value) {
    m_SampleDataNamesInHeader = value;
    reset();
  }

  /**
   * Returns whether the sample data names are in the header.
   *
   * @return 		true if in header
   */
  public boolean getSampleDataNamesInHeader() {
    return m_SampleDataNamesInHeader;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sampleDataNamesInHeaderTipText() {
    return "Whether the sample data names are stored in the header.";
  }

  /**
   * Sets the columns with sampledata.
   *
   * @param value	the columns
   */
  public void setColumnsSampleData(SpreadSheetColumnRange value) {
    m_ColumnsSampleData = value;
    reset();
  }

  /**
   * Returns the columns with sampledata.
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
    return "The columns that contain sampledata.";
  }

  /**
   * Sets the rows with the sample data values.
   *
   * @param value	the rows
   */
  public void setRowsSampleDataValues(SpreadSheetRowRange value) {
    m_RowsSampleDataValues = value;
    reset();
  }

  /**
   * Returns the rows with the sample data values.
   *
   * @return 		the rows
   */
  public SpreadSheetRowRange getRowsSampleDataValues() {
    return m_RowsSampleDataValues;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rowsSampleDataValuesTipText() {
    return "The rows in the spreadsheet that contain the sample data values.";
  }

  /**
   * Returns the class that is accepted as input.
   *
   * @return		the class
   */
  @Override
  public Class accepts() {
    return SpreadSheet.class;
  }

  /**
   * Returns the class that is generated as output.
   *
   * @return		the class
   */
  @Override
  public Class generates() {
    return SampleData[].class;
  }

  /**
   * Returns a quick info about the object, which can be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result = QuickInfoHelper.toString(this, "columnID", (m_ColumnID.isEmpty() ? "-none-" : m_ColumnID.getIndex()), "ID: ");
    result += QuickInfoHelper.toString(this, "columnsSampleData", (m_ColumnsSampleData.isEmpty() ? "-none-" : m_ColumnsSampleData.getRange()), ", cols: ");
    result += QuickInfoHelper.toString(this, "sampleDataNamesInHeader", m_SampleDataNamesInHeader, "SD names in header", ", ");
    result += QuickInfoHelper.toString(this, "rowsSampleDataValues", (m_RowsSampleDataValues.isEmpty() ? "-none-" : m_RowsSampleDataValues.getRange()), ", rows: ");

    return result;
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  @Override
  protected Object doConvert() throws Exception {
    SampleData[]	result;
    SpreadSheet		sheet;
    int[] 		rowsValues;
    int			i;
    int			n;
    int 		rowMeta;
    int[] 		colsMeta;
    int 		colID;
    Row			row;
    Row			rowMetaObj;
    Cell 		cell;

    sheet = (SpreadSheet) m_Input;

    m_RowsSampleDataValues.setSpreadSheet(sheet);
    rowsValues = m_RowsSampleDataValues.getIntIndices();
    if (rowsValues.length == 0)
      throw new IllegalStateException("Failed to locate rows with sample data values: " + m_RowsSampleDataValues);

    if (m_SampleDataNamesInHeader) {
      rowMetaObj = sheet.getHeaderRow();
    }
    else {
      m_RowSampleDataNames.setSpreadSheet(sheet);
      rowMeta = m_RowSampleDataNames.getIntIndex();
      if (rowMeta == -1)
        throw new IllegalStateException("Failed to locate row with sample data names: " + m_RowSampleDataNames.getIndex());
      rowMetaObj = sheet.getRow(rowMeta);
    }

    m_ColumnsSampleData.setSpreadSheet(sheet);
    colsMeta = m_ColumnsSampleData.getIntIndices();
    if (colsMeta.length == 0)
      throw new IllegalStateException("Failed to locate columns with sample data: " + m_ColumnsSampleData.getRange());

    m_ColumnID.setSpreadSheet(sheet);
    colID = m_ColumnID.getIntIndex();
    
    result = new SampleData[rowsValues.length];
    for (i = 0; i < rowsValues.length; i++) {
      row       = sheet.getRow(rowsValues[i]);
      result[i] = new SampleData();
      if ((colID > -1) && row.hasCell(colID) && !row.getCell(colID).isMissing())
	result[i].setID(row.getCell(colID).getContent());
      else
        result[i].setID("" + (i+1));

      // sample data
      if (rowMetaObj != null) {
	for (n = 0; n < colsMeta.length; n++) {
	  if (colsMeta[n] == colID)
	    continue;
	  if (row.hasCell(colsMeta[n]) && !row.getCell(colsMeta[n]).isMissing()) {
	    cell = row.getCell(colsMeta[n]);
	    if (cell.isNumeric())
	      result[i].setNumericValue(rowMetaObj.getCell(colsMeta[n]).getContent(), cell.toDouble());
	    else if (cell.isBoolean())
	      result[i].setBooleanValue(rowMetaObj.getCell(colsMeta[n]).getContent(), cell.toBoolean());
	    else
	      result[i].setStringValue(rowMetaObj.getCell(colsMeta[n]).getContent(), cell.getContent());
	  }
	}
      }
    }
    
    return result;
  }
}
