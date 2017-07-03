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

/**
 * SimpleEEMReader.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input;

import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.threeway.LevelOnePoint;
import adams.data.threeway.LevelTwoPoint;
import adams.data.threeway.ThreeWayData;

/**
 * Reads EEM data in spreadsheet format (tab-separated columns).
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SimpleEEMReader
  extends AbstractThreeWayDataReader {

  private static final long serialVersionUID = 3881844348241663649L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads EEM data in spreadsheet format (tab-separated columns).";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Tab-separated EEM";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"dat"};
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    CsvSpreadSheetReader	reader;
    SpreadSheet			sheet;
    ThreeWayData		data;
    LevelOnePoint		l1;
    LevelTwoPoint		l2;
    Row				header;
    double			sum;
    int				i;

    data   = new ThreeWayData();
    reader = new CsvSpreadSheetReader();
    reader.setSeparator("\\t");
    sheet  = reader.read(m_Input);

    // remove "nm" and "Normalized by ..." -- TODO in report?
    i = 0;
    while (!sheet.getCell(0, 0).isNumeric()) {
      i++;
      data.getReport().setStringValue("Info-" + i, sheet.getCell(0, 0).getContent());
      sheet.removeRow(0);
    }

    header = sheet.getHeaderRow();
    for (Row row: sheet.rows()) {
      l1 = new LevelOnePoint();
      l1.setX(row.getCell(0).toDouble());
      sum = 0.0;
      for (i = 1; i < sheet.getColumnCount(); i++) {
	l2 = new LevelTwoPoint(header.getCell(i).toDouble(), row.getCell(i).toDouble());
	l1.add(l2);
	sum += l2.getY();
      }
      l1.setY(sum);
      data.add(l1);
    }

    m_ReadData.add(data);
  }
}
