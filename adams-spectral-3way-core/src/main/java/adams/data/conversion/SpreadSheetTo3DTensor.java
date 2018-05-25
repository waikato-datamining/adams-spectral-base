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
 * SpreadSheetTo3DTensor.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.conversion;

import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.spreadsheet.SpreadSheetColumnIndex;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.set.TDoubleSet;
import gnu.trove.set.hash.TDoubleHashSet;
import nz.ac.waikato.cms.adams.multiway.data.tensor.Tensor;

/**
 <!-- globalinfo-start -->
 * Converts a spreadsheet into a 3-way Tensor data structure.<br>
 * The sheet requires four columns: X, Y, Z and data.
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
 * &nbsp;&nbsp;&nbsp;The column in the spreadsheet representing the Z axis.
 * &nbsp;&nbsp;&nbsp;default: 3
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-column-data &lt;adams.data.spreadsheet.SpreadSheetColumnIndex&gt; (property: columnData)
 * &nbsp;&nbsp;&nbsp;The column in the spreadsheet representing the data column.
 * &nbsp;&nbsp;&nbsp;default: 4
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpreadSheetTo3DTensor
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
    return "Converts a spreadsheet into a 3-way Tensor data structure.\n"
      + "The sheet requires four columns: X, Y, Z and data.";
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
      new SpreadSheetColumnIndex("3"));

    m_OptionManager.add(
      "column-data", "columnData",
      new SpreadSheetColumnIndex("4"));
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
    return "The column in the spreadsheet representing the Z axis.";
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
    return Tensor.class;
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  @Override
  protected Object doConvert() throws Exception {
    Tensor		result;
    SpreadSheet		sheet;
    int			colX;
    int			colY;
    int			colZ;
    int			colD;
    TDoubleSet 		setX;
    TDoubleSet 		setY;
    TDoubleSet 		setZ;
    TDoubleList 	listX;
    TDoubleList		listY;
    TDoubleList		listZ;
    double[][][]	tdata;
    int			x;
    int			y;
    int			z;

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
    if (colZ == -1)
      throw new IllegalStateException("Column for Z not found: " + m_ColumnZ);
    if (colD == -1)
      throw new IllegalStateException("Column for Data not found: " + m_ColumnData);
    setX = new TDoubleHashSet();
    setY = new TDoubleHashSet();
    setZ = new TDoubleHashSet();
    for (Row row: sheet.rows()) {
      setX.add(row.getCell(colX).toDouble());
      setY.add(row.getCell(colY).toDouble());
      setZ.add(row.getCell(colZ).toDouble());
    }

    listX = new TDoubleArrayList(setX);
    listX.sort();
    listY = new TDoubleArrayList(setY);
    listY.sort();
    listZ = new TDoubleArrayList(setZ);
    listZ.sort();

    tdata = new double[listX.size()][listY.size()][listZ.size()];
    for (Row row: sheet.rows()) {
      x = listX.indexOf(row.getCell(colX).toDouble());
      y = listY.indexOf(row.getCell(colY).toDouble());
      z = listY.indexOf(row.getCell(colZ).toDouble());
      tdata[x][y][z] = row.getCell(colD).toDouble();
    }
    result = Tensor.create(tdata);

    return result;
  }
}
