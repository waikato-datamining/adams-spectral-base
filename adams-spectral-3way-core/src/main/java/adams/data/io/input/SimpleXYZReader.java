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
 * SimpleXYZReader.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input;

import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.threeway.L1Point;
import adams.data.threeway.L2Point;
import adams.data.threeway.ThreeWayData;

import java.util.HashMap;
import java.util.Map;

/**
 <!-- globalinfo-start -->
 * Reads 3-day data in CSV format with X, Y, Z and data columns.
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
public class SimpleXYZReader
  extends AbstractThreeWayDataReader {

  private static final long serialVersionUID = 3881844348241663649L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads 3-day data in CSV format with X, Y, Z and data columns.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "XYZ data";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"csv"};
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    CsvSpreadSheetReader	reader;
    SpreadSheet			sheet;
    ThreeWayData		data;
    L1Point l1;
    L2Point l2;
    Map<Double,Map<Double,L1Point>> cache;
    double			x;
    double			y;
    double			z;
    double			d;

    data   = new ThreeWayData();
    cache = new HashMap<>();
    reader = new CsvSpreadSheetReader();
    reader.setSeparator(",");
    sheet  = reader.read(m_Input);
    if (sheet.getColumnCount() != 4) {
      getLogger().severe("Requires four columns (x/y/z/data), found: " + sheet.getColumnCount());
      return;
    }

    for (Row row: sheet.rows()) {
      x = row.getCell(0).toDouble();
      y = row.getCell(1).toDouble();
      z = row.getCell(2).toDouble();
      d = row.getCell(3).toDouble();

      if (cache.containsKey(x) && cache.get(x).containsKey(y)) {
        l1 = cache.get(x).get(y);
      }
      else {
	l1 = new L1Point(x, y);
	if (!cache.containsKey(x))
	  cache.put(x, new HashMap<>());
	cache.get(x).put(y, l1);
	data.add(l1);
      }
      l2 = new L2Point(z, d);
      l1.add(l2);
    }

    m_ReadData.add(data);
  }
}
