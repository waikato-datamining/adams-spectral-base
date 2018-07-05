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

import adams.core.Utils;
import adams.data.io.input.SimpleXYZReader;
import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.HeaderRow;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.threeway.L1Point;
import adams.data.threeway.L2Point;
import adams.data.threeway.ThreeWayData;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

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
 * <pre>-separator &lt;java.lang.String&gt; (property: separator)
 * &nbsp;&nbsp;&nbsp;The separator to use for the columns; use '\t' for tab.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-output-sample-data &lt;boolean&gt; (property: outputSampleData)
 * &nbsp;&nbsp;&nbsp;If set to true, the sample data gets stored in the file as well (as comment
 * &nbsp;&nbsp;&nbsp;).
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SimpleXYZWriter
  extends AbstractThreeWayDataWriter {

  private static final long serialVersionUID = 5576166671141967708L;

  /** the column separator. */
  protected String m_Separator;

  /** whether to output the sample data as well. */
  protected boolean m_OutputSampleData;

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
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "separator", "separator",
      " ");

    m_OptionManager.add(
      "output-sample-data", "outputSampleData",
      false);
  }

  /**
   * Sets the string to use as separator for the columns, use '\t' for tab.
   *
   * @param value	the separator
   */
  public void setSeparator(String value) {
    if (Utils.unbackQuoteChars(value).length() == 1) {
      m_Separator = Utils.unbackQuoteChars(value);
      reset();
    }
    else {
      getLogger().severe("Only one character allowed (or two, in case of backquoted ones) for separator, provided: " + value);
    }
  }

  /**
   * Returns the string used as separator for the columns, '\t' for tab.
   *
   * @return		the separator
   */
  public String getSeparator() {
    return Utils.backQuoteChars(m_Separator);
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String separatorTipText() {
    return "The separator to use for the columns; use '\\t' for tab.";
  }

  /**
   * Sets whether to output the sample data as well.
   *
   * @param value	if true then the sample data gets output as well
   */
  public void setOutputSampleData(boolean value) {
    m_OutputSampleData = value;
    reset();
  }

  /**
   * Returns whether to output eh sample data as well.
   *
   * @return		true if the sample data is output as well
   */
  public boolean getOutputSampleData() {
    return m_OutputSampleData;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String outputSampleDataTipText() {
    return "If set to true, the sample data gets stored in the file as well (as comment).";
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
    StringWriter		swriter;
    String[]			lines;

    three = data.get(0);

    // generate spreadsheet
    sheet = new DefaultSpreadSheet();

    // report as comments
    if (m_OutputSampleData) {
      swriter = new StringWriter();
      lines   = three.getReport().toProperties().toComment().split("\n");
      Arrays.sort(lines);
      swriter.write(Utils.flatten(lines, "\n"));
      swriter.write("\n");
      try {
	sheet.addComment(Arrays.asList(swriter.toString().split("\n")));
      }
      catch (Exception e) {
        getLogger().log(Level.WARNING, "Failed to store sample data in comments!", e);
      }
    }

    // header
    header = sheet.getHeaderRow();
    header.addCell("X").setContentAsString("X");
    header.addCell("Y").setContentAsString("Y");
    header.addCell("Z").setContentAsString("Z");
    header.addCell("D").setContentAsString("Data");

    // data
    for (L1Point l1 : three) {
      for (L2Point l2: l1) {
        row = sheet.addRow();
        row.addCell("X").setContent(l1.getX());
        row.addCell("Y").setContent(l1.getY());
        row.addCell("Z").setContent(l2.getZ());
        row.addCell("D").setContent(l2.getData());
      }
    }

    // write data
    writer = new CsvSpreadSheetWriter();
    writer.setQuoteCharacter("");
    writer.setSeparator(m_Separator);
    writer.setOutputComments(m_OutputSampleData);
    return writer.write(sheet, m_Output);
  }
}
