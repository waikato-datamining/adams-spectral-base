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
 * SimpleEEMWriter.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.io.output;

import adams.data.io.input.SimpleEEMReader;
import adams.data.report.DataType;
import adams.data.report.Field;
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
 * Writes 3-way data in spreadsheet format (tab-separated columns).
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
public class SimpleEEMWriter
  extends AbstractThreeWayDataWriter {

  private static final long serialVersionUID = 5576166671141967708L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Writes 3-way data in spreadsheet format (tab-separated columns).";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the file chooser
   */
  @Override
  public String getFormatDescription() {
    return new SimpleEEMReader().getFormatDescription();
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new SimpleEEMReader().getFormatExtensions();
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
    int				i;

    three = data.get(0);

    // generate spreadsheet
    sheet = new DefaultSpreadSheet();

    // header
    header = sheet.getHeaderRow();
    for (LevelOnePoint l1 : three) {
      header.addCell("S").setContentAsString("Sample");
      for (LevelTwoPoint l2: l1) {
        if (!header.hasCell("" + l2.getX()))
          header.addCell("" + l2.getX()).setContent(l2.getX());
      }
    }

    // comments? see reader
    i = 1;
    while (three.getReport().hasValue(SimpleEEMReader.PREFIX_INFO + i)) {
      row = sheet.addRow();
      row.addCell("S").setContent("" + three.getReport().getValue(new Field(SimpleEEMReader.PREFIX_INFO + i, DataType.UNKNOWN)));
      i++;
    }

    // data
    for (LevelOnePoint l1 : three) {
      row = sheet.addRow();
      row.addCell("S").setContent(l1.getX());
      for (LevelTwoPoint l2: l1)
        row.addCell("" + l2.getX()).setContent(l2.getY());
    }

    // write data
    writer = new CsvSpreadSheetWriter();
    writer.setQuoteCharacter("");
    writer.setSeparator("\\t");
    writer.setOutputComments(false);
    return writer.write(sheet, m_Output);
  }
}
