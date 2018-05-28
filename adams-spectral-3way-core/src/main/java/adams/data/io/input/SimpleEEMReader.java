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
 * <pre>-x &lt;double&gt; (property: X)
 * &nbsp;&nbsp;&nbsp;The value to use for the X axis.
 * &nbsp;&nbsp;&nbsp;default: 0.0
 * </pre>
 *
 * <pre>-ignore-wave-numbers &lt;boolean&gt; (property: ignoreWaveNumbers)
 * &nbsp;&nbsp;&nbsp;If enabled, the wave numbers get ignored and column and row indices are
 * &nbsp;&nbsp;&nbsp;used instead.
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

  /** the X value to use for the 3-way data. */
  protected double m_X;

  /** whether to ignore the wavenumbers in the file and just use column indices. */
  protected boolean m_IgnoreWaveNumbers;

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
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "x", "X",
      0.0);

    m_OptionManager.add(
      "ignore-wave-numbers", "ignoreWaveNumbers",
      false);
  }

  /**
   * Sets the value to use for the X axis.
   *
   * @param value	the value
   */
  public void setX(double value) {
    m_X = value;
    reset();
  }

  /**
   * Returns the value to use for the X axis.
   *
   * @return		the value
   */
  public double getX() {
    return m_X;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String XTipText() {
    return "The value to use for the X axis.";
  }

  /**
   * Sets whether to ignore the wave numbers and use row/col indices instead.
   *
   * @param value	true if to ignore
   */
  public void setIgnoreWaveNumbers(boolean value) {
    m_IgnoreWaveNumbers = value;
    reset();
  }

  /**
   * Returns whether to ignore the wave numbers and use row/col indices instead.
   *
   * @return		true if to ignore
   */
  public boolean getIgnoreWaveNumbers() {
    return m_IgnoreWaveNumbers;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String ignoreWaveNumbersTipText() {
    return "If enabled, the wave numbers get ignored and column and row indices are used instead.";
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
    int				rowIdx;

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
    rowIdx = 0;
    for (Row row: sheet.rows()) {
      l1 = new L1Point();
      l1.setX(m_X);                                                      // X is user-defined
      l1.setY(m_IgnoreWaveNumbers ? rowIdx : row.getCell(0).toDouble()); // Y
      data.add(l1);
      for (i = 1; i < sheet.getColumnCount(); i++) {
	l2 = new L2Point(
	  m_IgnoreWaveNumbers ? i : header.getCell(i).toDouble(),        // Z
	  row.getCell(i).toDouble());                                    // value
	l1.add(l2);
      }
      rowIdx++;
    }

    m_ReadData.add(data);
  }
}
