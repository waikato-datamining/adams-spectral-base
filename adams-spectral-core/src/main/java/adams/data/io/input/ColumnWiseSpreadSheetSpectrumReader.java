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
 * ColumnWiseSpreadSheetSpectrumReader.java
 * Copyright (C) 2013-2015 University of Waikato, Hamilton, New Zealand
 */
package adams.data.io.input;

import adams.core.Index;
import adams.core.Range;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spreadsheet.Cell;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.spreadsheet.SpreadSheetColumnIndex;
import adams.data.spreadsheet.SpreadSheetColumnRange;

/**
 <!-- globalinfo-start -->
 * Reads spectra from columns in a spreadsheet obtained with the specified spreadsheet reader.
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
 * &nbsp;&nbsp;&nbsp;default: NIR
 * </pre>
 *
 * <pre>-keep-format &lt;boolean&gt; (property: keepFormat)
 * &nbsp;&nbsp;&nbsp;If enabled the format obtained from the file is not replaced by the format
 * &nbsp;&nbsp;&nbsp;defined here.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-reader &lt;adams.data.io.input.SpreadSheetReader&gt; (property: reader)
 * &nbsp;&nbsp;&nbsp;The spreadsheet reader to use for reading the raw data.
 * &nbsp;&nbsp;&nbsp;default: adams.data.io.input.CsvSpreadSheetReader -data-row-type adams.data.spreadsheet.DenseDataRow -spreadsheet-type adams.data.spreadsheet.SpreadSheet
 * </pre>
 *
 * <pre>-spectrum-columns &lt;adams.data.spreadsheet.SpreadSheetColumnRange&gt; (property: spectrumColumns)
 * &nbsp;&nbsp;&nbsp;The columns containing the spectral data
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last
 * </pre>
 *
 * <pre>-amplitude-rows &lt;adams.core.Range&gt; (property: amplitudeRows)
 * &nbsp;&nbsp;&nbsp;The rows containing the data with the amplitudes
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 *
 * <pre>-wave-number-column &lt;adams.data.spreadsheet.SpreadSheetColumnIndex&gt; (property: waveNumberColumn)
 * &nbsp;&nbsp;&nbsp;The column containing the wave number.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last
 * </pre>
 *
 * <pre>-sample-data-labels-column &lt;adams.data.spreadsheet.SpreadSheetColumnIndex&gt; (property: sampleDataLabelsColumn)
 * &nbsp;&nbsp;&nbsp;The column containing the labels for the sample data values
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last
 * </pre>
 *
 * <pre>-sample-data-rows &lt;adams.core.Range&gt; (property: sampleDataRows)
 * &nbsp;&nbsp;&nbsp;The rows containing the data with the sample data
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 *
 * <pre>-sample-id-location &lt;HEADER|ROW&gt; (property: sampleIDLocation)
 * &nbsp;&nbsp;&nbsp;Where the sample ID is located; if ROW is selected the 'sampleIDRow' option
 * &nbsp;&nbsp;&nbsp;must be filled in as well.
 * &nbsp;&nbsp;&nbsp;default: ROW
 * </pre>
 *
 * <pre>-sample-id-row &lt;adams.core.Index&gt; (property: sampleIDRow)
 * &nbsp;&nbsp;&nbsp;The row containing the sample ID
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2332 $
 */
public class ColumnWiseSpreadSheetSpectrumReader
  extends AbstractSpectrumReader
  implements MetaFileReader {

  /** for serialization. */
  private static final long serialVersionUID = 2977214258003686356L;

  /**
   * Determines whether the sample ID is located.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 2332 $
   */
  public enum SampleIDLocation {
    HEADER,
    ROW
  }
  
  /** the spreadsheet reader to use. */
  protected SpreadSheetReader m_Reader;
  
  /** the range of columns to load spectra from. */
  protected SpreadSheetColumnRange m_SpectrumColumns;
  
  /** the range of rows containing the amplitudes. */
  protected Range m_AmplitudeRows;
  
  /** the column with the wave numbers. */
  protected SpreadSheetColumnIndex m_WaveNumberColumn;
  
  /** the column containing the labels for the sample data values. */
  protected SpreadSheetColumnIndex m_SampleDataLabelsColumn;
  
  /** the range of rows containing the sample data. */
  protected Range m_SampleDataRows;
  
  /** the sample ID location. */
  protected SampleIDLocation m_SampleIDLocation;
  
  /** the row containing the sample ID. */
  protected Index m_SampleIDRow;
  
  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return 
	"Reads spectra from columns in a spreadsheet obtained with the "
	+ "specified spreadsheet reader.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "reader", "reader",
	    new CsvSpreadSheetReader());

    m_OptionManager.add(
	    "spectrum-columns", "spectrumColumns",
	    new SpreadSheetColumnRange());

    m_OptionManager.add(
	    "amplitude-rows", "amplitudeRows",
	    new Range());

    m_OptionManager.add(
	    "wave-number-column", "waveNumberColumn",
	    new SpreadSheetColumnIndex());

    m_OptionManager.add(
	    "sample-data-labels-column", "sampleDataLabelsColumn",
	    new SpreadSheetColumnIndex());

    m_OptionManager.add(
	    "sample-data-rows", "sampleDataRows",
	    new Range());

    m_OptionManager.add(
	    "sample-id-location", "sampleIDLocation",
	    SampleIDLocation.ROW);

    m_OptionManager.add(
	    "sample-id-row", "sampleIDRow",
	    new Index());
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Column-wise: " + m_Reader.getFormatDescription();
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return m_Reader.getFormatExtensions();
  }

  /**
   * Returns the underlying format extensions.
   * 
   * @return		the format extensions (excluding dot)
   */
  public String[] getActualFormatExtensions() {
    return m_Reader.getFormatExtensions();
  }

  /**
   * Sets the spreadsheet reader to use.
   *
   * @param value	the reader
   */
  public void setReader(SpreadSheetReader value) {
    m_Reader = value;
    reset();
  }

  /**
   * Returns the spreadsheet reader in use.
   *
   * @return		the reader
   */
  public SpreadSheetReader getReader() {
    return m_Reader;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String readerTipText() {
    return "The spreadsheet reader to use for reading the raw data.";
  }

  /**
   * Sets the columns containing spectral data.
   *
   * @param value	the range
   */
  public void setSpectrumColumns(SpreadSheetColumnRange value) {
    m_SpectrumColumns = value;
    reset();
  }

  /**
   * Returns the coumns containing spectral data.
   *
   * @return		the range
   */
  public SpreadSheetColumnRange getSpectrumColumns() {
    return m_SpectrumColumns;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String spectrumColumnsTipText() {
    return "The columns containing the spectral data";
  }

  /**
   * Sets the range of rows containing the amplitudes.
   *
   * @param value	the range
   */
  public void setAmplitudeRows(Range value) {
    m_AmplitudeRows = value;
    reset();
  }

  /**
   * Returns the range of rows containing the amplitudes.
   *
   * @return		the range
   */
  public Range getAmplitudeRows() {
    return m_AmplitudeRows;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String amplitudeRowsTipText() {
    return "The rows containing the data with the amplitudes";
  }

  /**
   * Sets the column containing the wave numbers.
   *
   * @param value	the column
   */
  public void setWaveNumberColumn(SpreadSheetColumnIndex value) {
    m_WaveNumberColumn = value;
    reset();
  }

  /**
   * Returns the column containing the wave numbers.
   *
   * @return		the column
   */
  public SpreadSheetColumnIndex getWaveNumberColumn() {
    return m_WaveNumberColumn;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String waveNumberColumnTipText() {
    return "The column containing the wave number.";
  }

  /**
   * Sets the column containing the labels for the sample data values.
   *
   * @param value	the column
   */
  public void setSampleDataLabelsColumn(SpreadSheetColumnIndex value) {
    m_SampleDataLabelsColumn = value;
    reset();
  }

  /**
   * Returns the column containing the labels for the sample data values.
   *
   * @return		the column
   */
  public SpreadSheetColumnIndex getSampleDataLabelsColumn() {
    return m_SampleDataLabelsColumn;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sampleDataLabelsColumnTipText() {
    return "The column containing the labels for the sample data values";
  }

  /**
   * Sets the range of rows containing the sample data.
   *
   * @param value	the range
   */
  public void setSampleDataRows(Range value) {
    m_SampleDataRows = value;
    reset();
  }

  /**
   * Returns the range of rows containing the sample data.
   *
   * @return		the range
   */
  public Range getSampleDataRows() {
    return m_SampleDataRows;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sampleDataRowsTipText() {
    return "The rows containing the data with the sample data";
  }

  /**
   * Sets whether the sample ID is located.
   *
   * @param value	the location
   */
  public void setSampleIDLocation(SampleIDLocation value) {
    m_SampleIDLocation = value;
    reset();
  }

  /**
   * Returns the where the sample ID is located.
   *
   * @return		the location
   */
  public SampleIDLocation getSampleIDLocation() {
    return m_SampleIDLocation;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sampleIDLocationTipText() {
    return 
	"Where the sample ID is located; if " + SampleIDLocation.ROW + " is "
	+ "selected the 'sampleIDRow' option must be filled in as well.";
  }

  /**
   * Sets the row that contains the sample ID.
   *
   * @param value	the row index
   */
  public void setSampleIDRow(Index value) {
    m_SampleIDRow = value;
    reset();
  }

  /**
   * Returns the row that contains the sample ID.
   *
   * @return		the row index
   */
  public Index getSampleIDRow() {
    return m_SampleIDRow;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sampleIDRowTipText() {
    return "The row containing the sample ID";
  }

  /**
   * Performs some checks on the provided cols/rows.
   */
  @Override
  protected void checkData() {
    super.checkData();
    
    if (m_SpectrumColumns.getRange().trim().length() == 0)
      throw new IllegalStateException("No columns for spectral data defined!");
    if (m_AmplitudeRows.getRange().trim().length() == 0)
      throw new IllegalStateException("No rows for amplitudes defined!");
  }
  
  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    SpreadSheet		sheet;
    Spectrum 		sp;
    SampleData sd;
    int[]		cols;
    int[]		rows;
    int			i;
    Cell		cell;
    Field 		field;
    int			sampleID;
    int			waveNo;
    float		wave;
    int			labels;
    String		label;

    // read spreadsheet
    sheet = m_Reader.read(m_Input);
    if (m_Stopped)
      return;
    if (sheet == null) {
      getLogger().severe("Failed to read spreadsheet: " + m_Input);
      return;
    }

    // configure ranges/indices
    m_SpectrumColumns.setSpreadSheet(sheet);
    m_AmplitudeRows.setMax(sheet.getRowCount());
    m_WaveNumberColumn.setSpreadSheet(sheet);
    waveNo = m_WaveNumberColumn.getIntIndex();
    m_SampleDataLabelsColumn.setSpreadSheet(sheet);
    m_SampleDataRows.setMax(sheet.getRowCount());
    m_SampleIDRow.setMax(sheet.getRowCount());
    sampleID = m_SampleIDRow.getIntIndex();
    labels   = m_SampleDataLabelsColumn.getIntIndex();
    
    // read spectra
    cols = m_SpectrumColumns.getIntIndices();
    for (int col: cols) {
      sp = new Spectrum();
      sd = new SampleData();
      sp.setReport(sd);
      m_ReadData.add(sp);
      
      // sample ID
      if (m_SampleIDLocation == SampleIDLocation.ROW) {
	if (sampleID > -1) {
	  cell = sheet.getCell(sampleID, col);
	  if ((cell != null) && !cell.isMissing())
	    sp.setID(cell.getContent());
	}
      }
      else if (m_SampleIDLocation == SampleIDLocation.HEADER) {
	cell = sheet.getHeaderRow().getCell(col);
	if ((cell != null) && !cell.isMissing())
	  sp.setID(cell.getContent());
      }
      
      // amplitudes
      rows = m_AmplitudeRows.getIntIndices();
      for (i = 0; i < rows.length; i++) {
	wave = i;
	if (waveNo != -1) {
	  cell = sheet.getCell(rows[i], waveNo);
	  if ((cell != null) && !cell.isMissing() && cell.isNumeric())
	    wave = cell.toDouble().floatValue();
	}
	cell = sheet.getCell(rows[i], col);
	if ((cell != null) && !cell.isMissing() && cell.isNumeric())
	  sp.add(new SpectrumPoint(wave, cell.toDouble().floatValue()));
	else
	  System.out.println("missing: " + rows[i]);
      }
      
      // sample data
      if (labels > -1) {
	rows = m_SampleDataRows.getIntIndices();
	for (i = 0; i < rows.length; i++) {
	  cell = sheet.getCell(rows[i], labels);
	  if ((cell != null) && !cell.isMissing()) {
	    label = cell.getContent();
	    cell  = sheet.getCell(rows[i], col);
	    if ((cell != null) && !cell.isMissing()) {
	      if (cell.isNumeric()) {
		field = new Field(label, DataType.NUMERIC);
		sd.addField(field);
		sd.setNumericValue(label, cell.toDouble());
	      }
	      else if (cell.isBoolean()) {
		field = new Field(label, DataType.BOOLEAN);
		sd.addField(field);
		sd.setBooleanValue(label, cell.toBoolean());
	      }
	      else {
		field = new Field(label, DataType.STRING);
		sd.addField(field);
		sd.setStringValue(label, cell.getContent());
	      }
	    }
	  }
	}
      }
    }
  }

  /**
   * Stops the execution.
   */
  public void stopExecution() {
    super.stopExecution();
    m_Reader.stopExecution();
  }
}
