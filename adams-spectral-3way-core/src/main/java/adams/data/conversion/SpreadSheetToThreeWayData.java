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
 * SpreadSheetToThreeWayData.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.conversion;

import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.spreadsheet.SpreadSheetColumnIndex;
import adams.data.threeway.L1Point;
import adams.data.threeway.L2Point;
import adams.data.threeway.ThreeWayData;

import java.util.HashMap;
import java.util.Map;

/**
 <!-- globalinfo-start -->
 * Converts a spreadsheet into a 3-way data structure.<br>
 * The sheet requires four columns: X, Y, Z and data.<br>
 * The Z column is optional, which, if not provided, is simply assumed to be 0.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-column-x &lt;adams.data.spreadsheet.SpreadSheetColumnIndex&gt; (property: columnX)
 * &nbsp;&nbsp;&nbsp;The column in the spreadsheet representing the X axis.
 * &nbsp;&nbsp;&nbsp;default: 1
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-column-y &lt;adams.data.spreadsheet.SpreadSheetColumnIndex&gt; (property: columnY)
 * &nbsp;&nbsp;&nbsp;The column in the spreadsheet representing the Y axis.
 * &nbsp;&nbsp;&nbsp;default: 2
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-column-z &lt;adams.data.spreadsheet.SpreadSheetColumnIndex&gt; (property: columnZ)
 * &nbsp;&nbsp;&nbsp;The column in the spreadsheet representing the (optional) Z axis.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-column-data &lt;adams.data.spreadsheet.SpreadSheetColumnIndex&gt; (property: columnData)
 * &nbsp;&nbsp;&nbsp;The column in the spreadsheet representing the data column.
 * &nbsp;&nbsp;&nbsp;default: 3
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpreadSheetToThreeWayData
  extends AbstractConversion {

  private static final long serialVersionUID = 1785721720488380977L;

  /** the X column. */
  protected SpreadSheetColumnIndex m_ColumnX;

  /** the Y column. */
  protected SpreadSheetColumnIndex m_ColumnY;

  /** the Z column. */
  protected SpreadSheetColumnIndex m_ColumnZ;

  /** the data column. */
  protected SpreadSheetColumnIndex m_ColumnData;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Converts a spreadsheet into a 3-way data structure.\n"
      + "The sheet requires four columns: X, Y, Z and data.\n"
      + "The Z column is optional, which, if not provided, is simply assumed to be 0.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "column-x", "columnX",
      new SpreadSheetColumnIndex("1"));

    m_OptionManager.add(
      "column-y", "columnY",
      new SpreadSheetColumnIndex("2"));

    m_OptionManager.add(
      "column-z", "columnZ",
      new SpreadSheetColumnIndex(""));

    m_OptionManager.add(
      "column-data", "columnData",
      new SpreadSheetColumnIndex("3"));
  }

  /**
   * Sets the X column.
   *
   * @param value	the column
   */
  public void setColumnX(SpreadSheetColumnIndex value) {
    m_ColumnX = value;
    reset();
  }

  /**
   * Returns the X column.
   *
   * @return		the column
   */
  public SpreadSheetColumnIndex getColumnX() {
    return m_ColumnX;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnXTipText() {
    return "The column in the spreadsheet representing the X axis.";
  }

  /**
   * Sets the X column.
   *
   * @param value	the column
   */
  public void setColumnY(SpreadSheetColumnIndex value) {
    m_ColumnY = value;
    reset();
  }

  /**
   * Returns the Y column.
   *
   * @return		the column
   */
  public SpreadSheetColumnIndex getColumnY() {
    return m_ColumnY;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnYTipText() {
    return "The column in the spreadsheet representing the Y axis.";
  }

  /**
   * Sets the Z column.
   *
   * @param value	the column
   */
  public void setColumnZ(SpreadSheetColumnIndex value) {
    m_ColumnZ = value;
    reset();
  }

  /**
   * Returns the Z column.
   *
   * @return		the column
   */
  public SpreadSheetColumnIndex getColumnZ() {
    return m_ColumnZ;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnZTipText() {
    return "The column in the spreadsheet representing the (optional) Z axis.";
  }

  /**
   * Sets the data column.
   *
   * @param value	the column
   */
  public void setColumnData(SpreadSheetColumnIndex value) {
    m_ColumnData = value;
    reset();
  }

  /**
   * Returns the data column.
   *
   * @return		the column
   */
  public SpreadSheetColumnIndex getColumnData() {
    return m_ColumnData;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnDataTipText() {
    return "The column in the spreadsheet representing the data column.";
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
    return ThreeWayData.class;
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  @Override
  protected Object doConvert() throws Exception {
    ThreeWayData		result;
    SpreadSheet			sheet;
    int				colX;
    int				colY;
    int				colZ;
    int				colD;
    L1Point 			l1;
    L2Point 			l2;
    Map<Double,Map<Double,L1Point>> cache;
    double			x;
    double			y;
    double			z;
    double			d;

    result = new ThreeWayData();
    sheet  = (SpreadSheet) m_Input;
    m_ColumnX.setSpreadSheet(sheet);
    m_ColumnY.setSpreadSheet(sheet);
    m_ColumnZ.setSpreadSheet(sheet);
    m_ColumnData.setSpreadSheet(sheet);
    colX = m_ColumnX.getIntIndex();
    colY = m_ColumnY.getIntIndex();
    colZ = m_ColumnZ.getIntIndex();
    colD = m_ColumnData.getIntIndex();
    if (colX == -1)
      throw new IllegalStateException("Column for X not found: " + m_ColumnX);
    if (colY == -1)
      throw new IllegalStateException("Column for Y not found: " + m_ColumnY);
    if (colD == -1)
      throw new IllegalStateException("Column for Data not found: " + m_ColumnData);
    cache = new HashMap<>();
    for (Row row: sheet.rows()) {
      x = row.getCell(colX).toDouble();
      y = row.getCell(colY).toDouble();
      z = 0.0;
      if (colZ > -1)
	z = row.getCell(colZ).toDouble();
      d = row.getCell(colD).toDouble();

      if (cache.containsKey(x) && cache.get(x).containsKey(y)) {
        l1 = cache.get(x).get(y);
      }
      else {
	l1 = new L1Point(x, y);
	if (!cache.containsKey(x))
	  cache.put(x, new HashMap<>());
	cache.get(x).put(y, l1);
	result.add(l1);
      }
      l2 = new L2Point(z, d);
      l1.add(l2);
    }

    return result;
  }
}
