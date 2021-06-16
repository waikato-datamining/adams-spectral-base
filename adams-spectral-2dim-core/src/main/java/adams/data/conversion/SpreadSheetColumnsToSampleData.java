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
 * SpreadSheetColumnsToSampleData.java
 * Copyright (C) 2021 University of Waikato, Hamilton, New Zealand
 */
package adams.data.conversion;

import adams.core.LenientModeSupporter;
import adams.core.QuickInfoHelper;
import adams.core.logging.LoggingHelper;
import adams.data.sampledata.SampleData;
import adams.data.spreadsheet.Cell;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.spreadsheet.SpreadSheetColumnIndex;
import adams.data.spreadsheet.SpreadSheetColumnRange;
import adams.data.spreadsheet.SpreadSheetRowIndex;
import adams.data.spreadsheet.SpreadSheetRowRange;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Turns spreadsheet columns into sample data.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-col-sampledata-names &lt;adams.data.spreadsheet.SpreadSheetColumnIndex&gt; (property: colSampleDataNames)
 * &nbsp;&nbsp;&nbsp;The (optional) column that contains the sample data names.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-cols-sampledata-values &lt;adams.data.spreadsheet.SpreadSheetColumnRange&gt; (property: colsSampleDataValues)
 * &nbsp;&nbsp;&nbsp;The columns to get the sample data values from.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-rows-sampledata &lt;adams.data.spreadsheet.SpreadSheetRowRange&gt; (property: rowsSampleData)
 * &nbsp;&nbsp;&nbsp;The rows that contain sampledata.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 *
 * <pre>-row-id &lt;adams.data.spreadsheet.SpreadSheetRowIndex&gt; (property: rowID)
 * &nbsp;&nbsp;&nbsp;The (optional) row that contains the sample ID.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 *
 * <pre>-lenient &lt;boolean&gt; (property: lenient)
 * &nbsp;&nbsp;&nbsp;If enabled, then errors (e.g., due to corrupt data) will not cause exceptions.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpreadSheetColumnsToSampleData
  extends AbstractConversion
  implements LenientModeSupporter {

  /** for serialization. */
  private static final long serialVersionUID = -258589003642261978L;

  /** the column with the sample data names. */
  protected SpreadSheetColumnIndex m_ColSampleDataNames;

  /** the columns with the sample data values. */
  protected SpreadSheetColumnRange m_ColsSampleDataValues;

  /** the rows to get the sample data from. */
  protected SpreadSheetRowRange m_RowsSampleData;

  /** the (optional) row with the sample ID. */
  protected SpreadSheetRowIndex m_RowID;

  /** whether to skip over errors. */
  protected boolean m_Lenient;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Turns spreadsheet columns into sample data.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "col-sampledata-names", "colSampleDataNames",
      new SpreadSheetColumnIndex());

    m_OptionManager.add(
      "cols-sampledata-values", "colsSampleDataValues",
      new SpreadSheetColumnRange());

    m_OptionManager.add(
      "rows-sampledata", "rowsSampleData",
      new SpreadSheetRowRange());

    m_OptionManager.add(
      "row-id", "rowID",
      new SpreadSheetRowIndex());

    m_OptionManager.add(
      "lenient", "lenient",
      false);
  }

  /**
   * Sets the (optional) row that contains the sample ID.
   *
   * @param value	the row
   */
  public void setRowID(SpreadSheetRowIndex value) {
    m_RowID = value;
    reset();
  }

  /**
   * Returns the (optional) row that contains the sample ID.
   *
   * @return 		the row
   */
  public SpreadSheetRowIndex getRowID() {
    return m_RowID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rowIDTipText() {
    return "The (optional) row that contains the sample ID.";
  }

  /**
   * Sets the column that contains the sample data names.
   *
   * @param value	the column
   */
  public void setColSampleDataNames(SpreadSheetColumnIndex value) {
    m_ColSampleDataNames = value;
    reset();
  }

  /**
   * Returns the column that contains the sample data names.
   *
   * @return 		the column
   */
  public SpreadSheetColumnIndex getColSampleDataNames() {
    return m_ColSampleDataNames;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String colSampleDataNamesTipText() {
    return "The (optional) column that contains the sample data names.";
  }

  /**
   * Sets the rows with sampledata.
   *
   * @param value	the rows
   */
  public void setRowsSampleData(SpreadSheetRowRange value) {
    m_RowsSampleData = value;
    reset();
  }

  /**
   * Returns the rows with sampledata.
   *
   * @return 		the rows
   */
  public SpreadSheetRowRange getRowsSampleData() {
    return m_RowsSampleData;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rowsSampleDataTipText() {
    return "The rows that contain sampledata.";
  }

  /**
   * Sets the columns to get the sample data values from.
   *
   * @param value	the columns
   */
  public void setColsSampleDataValues(SpreadSheetColumnRange value) {
    m_ColsSampleDataValues = value;
    reset();
  }

  /**
   * Returns the columns to get the sample data values from.
   *
   * @return 		the columns
   */
  public SpreadSheetColumnRange getColsSampleDataValues() {
    return m_ColsSampleDataValues;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String colsSampleDataValuesTipText() {
    return "The columns to get the sample data values from.";
  }

  /**
   * Sets whether to skip over errors.
   *
   * @param value	true if to skip
   */
  @Override
  public void setLenient(boolean value) {
    m_Lenient = value;
    reset();
  }

  /**
   * Returns whether whether to skip over errors.
   *
   * @return		true if to skip
   */
  @Override
  public boolean getLenient() {
    return m_Lenient;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  @Override
  public String lenientTipText() {
    return "If enabled, then errors (e.g., due to corrupt data) will not cause exceptions.";
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

    result = QuickInfoHelper.toString(this, "rowID", (m_RowID.isEmpty() ? "-none-" : m_RowID.getIndex()), "ID: ");
    result += QuickInfoHelper.toString(this, "rowsSampleData", (m_RowsSampleData.isEmpty() ? "-none-" : m_RowsSampleData.getRange()), ", rows: ");
    result += QuickInfoHelper.toString(this, "colSampleDataNames", (m_ColSampleDataNames.isEmpty() ? "-none-" : m_ColSampleDataNames.getIndex()), ", names: ");
    result += QuickInfoHelper.toString(this, "colsSampleDataValues", (m_ColsSampleDataValues.isEmpty() ? "-none-" : m_ColsSampleDataValues.getRange()), ", value cols: ");
    result += QuickInfoHelper.toString(this, "lenient", m_Lenient, "lenient", ", ");

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
    List<SampleData> 	result;
    SampleData		sd;
    SpreadSheet		sheet;
    int			i;
    int			n;
    int			colMeta;
    int[]		rowsMeta;
    int[]		colsValues;
    int			rowID;
    Row			row;
    Cell 		cell;

    sheet = (SpreadSheet) m_Input;

    m_ColSampleDataNames.setSpreadSheet(sheet);
    colMeta = m_ColSampleDataNames.getIntIndex();
    if (colMeta == -1)
      throw new IllegalStateException("No column for sample data names: " + m_ColSampleDataNames.getIndex());

    m_ColsSampleDataValues.setSpreadSheet(sheet);
    colsValues = m_ColsSampleDataValues.getIntIndices();
    if (colsValues.length == 0)
      throw new IllegalStateException("No columns for sample data values: " + m_ColsSampleDataValues.getRange());

    m_RowsSampleData.setSpreadSheet(sheet);
    rowsMeta = m_RowsSampleData.getIntIndices();
    if (rowsMeta.length == 0)
      throw new IllegalStateException("No rows for sample data: " + m_RowsSampleData.getRange());

    m_RowID.setSpreadSheet(sheet);
    rowID = m_RowID.getIntIndex();

    result = new ArrayList<>();
    for (i = 0; i < colsValues.length; i++) {
      try {
	sd = new SampleData();
	if ((rowID > -1) && sheet.hasCell(rowID, colsValues[i]) && !sheet.getCell(rowID, colsValues[i]).isMissing())
	  sd.setID(sheet.getCell(rowID, colsValues[i]).getContent());
	else
	  sd.setID("" + (i+1));

	// sample data
	if (colMeta > -1) {
	  for (n = 0; n < rowsMeta.length; n++) {
	    if (rowsMeta[n] == rowID)
	      continue;
	    row = sheet.getRow(rowsMeta[n]);
	    if (row.hasCell(colsValues[i]) && !row.getCell(colsValues[i]).isMissing()) {
	      cell = row.getCell(colsValues[i]);
	      if (cell.isNumeric())
		sd.setNumericValue(row.getCell(colMeta).getContent(), cell.toDouble());
	      else if (cell.isBoolean())
		sd.setBooleanValue(row.getCell(colMeta).getContent(), cell.toBoolean());
	      else
		sd.setStringValue(row.getCell(colMeta).getContent(), cell.getContent());
	    }
	  }
	}

	result.add(sd);
      }
      catch (Exception e) {
	if (m_Lenient)
	  getLogger().warning("Failed to process column " + (colsValues[i] + 1) + ":\n" + LoggingHelper.throwableToString(e));
	else
	  throw e;
      }
    }

    return result;
  }
}
