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
 * SimpleXYZWriter.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.io.output;

import adams.data.io.input.SimpleXYZReader;
import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.HeaderRow;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.threeway.LevelOnePoint;
import adams.data.threeway.LevelTwoPoint;
import adams.data.threeway.ThreeWayData;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Writes 3-way data in CSV format (x&#47;y&#47;z&#47;data columns).
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-output &lt;adams.core.io.PlaceholderFile&gt; (property: output)
 * &nbsp;&nbsp;&nbsp;The file to write the container to.
 * &nbsp;&nbsp;&nbsp;default: ${TMP}&#47;out.tmp
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SimpleXYZWriter
  extends AbstractThreeWayDataWriter {

  private static final long serialVersionUID = 5576166671141967708L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Writes 3-way data in CSV format (x/y/z/data columns).";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the file chooser
   */
  @Override
  public String getFormatDescription() {
    return new SimpleXYZReader().getFormatDescription();
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new SimpleXYZReader().getFormatExtensions();
  }

  /**
   * Returns whether writing of multiple containers is supported.
   *
   * @return 		true if multiple containers are supported
   */
  @Override
  public boolean canWriteMultiple() {
    return false;
  }

  /**
   * Performs the actual writing.
   *
   * @param data	the data to write
   * @return		true if successfully written
   */
  @Override
  protected boolean writeData(List<ThreeWayData> data) {
    ThreeWayData		three;
    SpreadSheet			sheet;
    Row				row;
    HeaderRow 			header;
    CsvSpreadSheetWriter	writer;

    three = data.get(0);

    // generate spreadsheet
    sheet = new DefaultSpreadSheet();

    // header
    header = sheet.getHeaderRow();
    header.addCell("X").setContentAsString("X");
    header.addCell("Y").setContentAsString("Y");
    header.addCell("Z").setContentAsString("Z");
    header.addCell("D").setContentAsString("Data");

    // data
    for (LevelOnePoint l1 : three) {
      for (LevelTwoPoint l2: l1) {
        row = sheet.addRow();
        row.addCell("X").setContent(l1.getX());
        row.addCell("Y").setContent(l1.getY());
        row.addCell("Z").setContent(l2.getX());
        row.addCell("D").setContent(l2.getY());
      }
    }

    // write data
    writer = new CsvSpreadSheetWriter();
    writer.setQuoteCharacter("");
    writer.setSeparator(",");
    writer.setOutputComments(false);
    return writer.write(sheet, m_Output);
  }
}
