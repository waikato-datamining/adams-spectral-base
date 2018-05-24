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
 * SimpleEEMReader.java
 * Copyright (C) 2017-2018 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input;

import adams.core.io.FileUtils;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.threeway.L1Point;
import adams.data.threeway.L2Point;
import adams.data.threeway.ThreeWayData;

/**
 <!-- globalinfo-start -->
 * Reads EEM data in spreadsheet format (tab-separated columns).
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
 * &nbsp;&nbsp;&nbsp;The file to read and turn into a container.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 * 
 * <pre>-create-dummy-report &lt;boolean&gt; (property: createDummyReport)
 * &nbsp;&nbsp;&nbsp;If true, then a dummy report is created if none present.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-instrument &lt;java.lang.String&gt; (property: instrument)
 * &nbsp;&nbsp;&nbsp;The name of the instrument that generated the spectra (if not already present 
 * &nbsp;&nbsp;&nbsp;in data).
 * &nbsp;&nbsp;&nbsp;default: unknown
 * </pre>
 * 
 * <pre>-format &lt;java.lang.String&gt; (property: format)
 * &nbsp;&nbsp;&nbsp;The data format string.
 * &nbsp;&nbsp;&nbsp;default: EEM
 * </pre>
 * 
 * <pre>-keep-format &lt;boolean&gt; (property: keepFormat)
 * &nbsp;&nbsp;&nbsp;If enabled the format obtained from the file is not replaced by the format 
 * &nbsp;&nbsp;&nbsp;defined here.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-use-absolute-source &lt;boolean&gt; (property: useAbsoluteSource)
 * &nbsp;&nbsp;&nbsp;If enabled the source report field stores the absolute file name rather 
 * &nbsp;&nbsp;&nbsp;than just the name.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SimpleEEMReader
  extends AbstractThreeWayDataReader {

  private static final long serialVersionUID = 3881844348241663649L;

  public static final String PREFIX_INFO = "Info-";

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
    L1Point 			l1;
    L2Point 			l2;
    Row				header;
    int				i;

    data   = new ThreeWayData();
    data.setID(FileUtils.replaceExtension(m_Input.getName(), ""));
    reader = new CsvSpreadSheetReader();
    reader.setSeparator("\\t");
    sheet  = reader.read(m_Input);

    // remove "nm" and "Normalized by ..." -- TODO in report?
    i = 0;
    while (!sheet.getCell(0, 0).isNumeric()) {
      i++;
      data.getReport().setStringValue(PREFIX_INFO + i, sheet.getCell(0, 0).getContent());
      sheet.removeRow(0);
    }

    header = sheet.getHeaderRow();
    for (Row row: sheet.rows()) {
      for (i = 1; i < sheet.getColumnCount(); i++) {
        l1 = new L1Point();
        l1.setX(row.getCell(0).toDouble());              // X
        l1.setY(header.getCell(i).toDouble());           // Y
	l2 = new L2Point(0, row.getCell(i).toDouble());  // Z is always 0
	l1.add(l2);
        data.add(l1);
      }
    }

    m_ReadData.add(data);
  }
}
