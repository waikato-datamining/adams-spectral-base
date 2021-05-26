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
 * SpreadSheetRowsToSpectra.java
 * Copyright (C) 2021 University of Waikato, Hamilton, New Zealand
 */
package adams.data.conversion;

import adams.core.QuickInfoHelper;
import adams.core.base.BaseRegExp;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spreadsheet.Cell;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.spreadsheet.SpreadSheetColumnIndex;
import adams.data.spreadsheet.SpreadSheetColumnRange;
import adams.data.spreadsheet.SpreadSheetRowIndex;
import adams.data.spreadsheet.SpreadSheetRowRange;

/**
 <!-- globalinfo-start -->
 * Turns spreadsheet rows into spectra.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-row-wave-number &lt;adams.data.spreadsheet.SpreadSheetRowIndex&gt; (property: rowWaveNumber)
 * &nbsp;&nbsp;&nbsp;The (optional) row in the spreadsheet that contains the wavenumber information.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 *
 * <pre>-wave-numbers-in-header &lt;boolean&gt; (property: waveNumbersInHeader)
 * &nbsp;&nbsp;&nbsp;Whether the wave numbers are stored in the header.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-wave-number-regexp &lt;adams.core.base.BaseRegExp&gt; (property: waveNumberRegExp)
 * &nbsp;&nbsp;&nbsp;The regular expression to identify the wave number (1st group is used).
 * &nbsp;&nbsp;&nbsp;default: (.*)
 * &nbsp;&nbsp;&nbsp;more: https:&#47;&#47;docs.oracle.com&#47;javase&#47;tutorial&#47;essential&#47;regex&#47;
 * &nbsp;&nbsp;&nbsp;https:&#47;&#47;docs.oracle.com&#47;javase&#47;8&#47;docs&#47;api&#47;java&#47;util&#47;regex&#47;Pattern.html
 * </pre>
 *
 * <pre>-cols-amplitude &lt;adams.data.spreadsheet.SpreadSheetColumnRange&gt; (property: columnsAmplitude)
 * &nbsp;&nbsp;&nbsp;The columns that contain amplitude information.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-rows-amplitude &lt;adams.data.spreadsheet.SpreadSheetRowRange&gt; (property: rowsAmplitude)
 * &nbsp;&nbsp;&nbsp;The rows in the spreadsheet that contain the amplitude information.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 *
 * <pre>-row-sampledata-names &lt;adams.data.spreadsheet.SpreadSheetRowIndex&gt; (property: rowSampleDataNames)
 * &nbsp;&nbsp;&nbsp;The (optional) row that contains the sample data names.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 *
 * <pre>-cols-sampledata &lt;adams.data.spreadsheet.SpreadSheetColumnRange&gt; (property: columnsSampleData)
 * &nbsp;&nbsp;&nbsp;The columns that contain sampledata.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-col-id &lt;adams.data.spreadsheet.SpreadSheetColumnIndex&gt; (property: columnID)
 * &nbsp;&nbsp;&nbsp;The (optional) column that contains the sample ID.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-format &lt;java.lang.String&gt; (property: format)
 * &nbsp;&nbsp;&nbsp;The format for the spectrum.
 * &nbsp;&nbsp;&nbsp;default: NIR
 * </pre>
 *
 * <pre>-instrument &lt;java.lang.String&gt; (property: instrument)
 * &nbsp;&nbsp;&nbsp;The instrument for the spectrum.
 * &nbsp;&nbsp;&nbsp;default: unknown
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpreadSheetRowsToSpectra
  extends AbstractConversion {

  /** for serialization. */
  private static final long serialVersionUID = -258589003642261978L;

  public static final String DEFAULT_WAVENO_REGEXP = "(.*)";

  /** the (optional) wavenumber row. */
  protected SpreadSheetRowIndex m_RowWaveNumber;

  /** whether the row wavenumbers are in the header row. */
  protected boolean m_WaveNumbersInHeader;

  /** the regular expression to extract the wave number from the header (first group is used). */
  protected BaseRegExp m_WaveNumberRegExp;

  /** the rows with amplitudes. */
  protected SpreadSheetRowRange m_RowsAmplitude;

  /** the rows to get the amplitudes from. */
  protected SpreadSheetColumnRange m_ColumnsAmplitude;

  /** the column with the sample data names. */
  protected SpreadSheetRowIndex m_RowSampleDataNames;

  /** the rows to get the sample data from. */
  protected SpreadSheetColumnRange m_ColumnsSampleData;

  /** the (optional) row with the sample ID. */
  protected SpreadSheetColumnIndex m_ColumnID;
  
  /** the format to use for the spectrum. */
  protected String m_Format;
  
  /** the instrument to use for the spectrum. */
  protected String m_Instrument;
  
  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Turns spreadsheet rows into spectra.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "row-wave-number", "rowWaveNumber",
      new SpreadSheetRowIndex());

    m_OptionManager.add(
      "wave-numbers-in-header", "waveNumbersInHeader",
      false);

    m_OptionManager.add(
      "wave-number-regexp", "waveNumberRegExp",
      new BaseRegExp(DEFAULT_WAVENO_REGEXP));

    m_OptionManager.add(
      "cols-amplitude", "columnsAmplitude",
      new SpreadSheetColumnRange());

    m_OptionManager.add(
      "rows-amplitude", "rowsAmplitude",
      new SpreadSheetRowRange());

    m_OptionManager.add(
      "row-sampledata-names", "rowSampleDataNames",
      new SpreadSheetRowIndex());

    m_OptionManager.add(
      "cols-sampledata", "columnsSampleData",
      new SpreadSheetColumnRange());

    m_OptionManager.add(
      "col-id", "columnID",
      new SpreadSheetColumnIndex());

    m_OptionManager.add(
      "format", "format",
      "NIR");

    m_OptionManager.add(
      "instrument", "instrument",
      "unknown");
  }

  /**
   * Sets the row with the wavenumber information.
   *
   * @param value	the row
   */
  public void setRowWaveNumber(SpreadSheetRowIndex value) {
    m_RowWaveNumber = value;
    reset();
  }

  /**
   * Returns the row with the wavenumber information.
   *
   * @return 		the row
   */
  public SpreadSheetRowIndex getRowWaveNumber() {
    return m_RowWaveNumber;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rowWaveNumberTipText() {
    return "The (optional) row in the spreadsheet that contains the wavenumber information.";
  }

  /**
   * Sets whether the wave numbers are in the header.
   *
   * @param value	true if in header
   */
  public void setWaveNumbersInHeader(boolean value) {
    m_WaveNumbersInHeader = value;
    reset();
  }

  /**
   * Returns whether the wave numbers are in the header.
   *
   * @return 		true if in header
   */
  public boolean getWaveNumbersInHeader() {
    return m_WaveNumbersInHeader;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String waveNumbersInHeaderTipText() {
    return "Whether the wave numbers are stored in the header.";
  }

  /**
   * Sets the regular expression to identify the wave number (1st group is used).
   *
   * @param value	the expression
   */
  public void setWaveNumberRegExp(BaseRegExp value) {
    m_WaveNumberRegExp = value;
    reset();
  }

  /**
   * Returns the regular expression to identify the wave number (1st group is used).
   *
   * @return		the expression
   */
  public BaseRegExp getWaveNumberRegExp() {
    return m_WaveNumberRegExp;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String waveNumberRegExpTipText() {
    return "The regular expression to identify the wave number (1st group is used).";
  }

  /**
   * Sets the rows with the amplitude information.
   *
   * @param value	the rows
   */
  public void setRowsAmplitude(SpreadSheetRowRange value) {
    m_RowsAmplitude = value;
    reset();
  }

  /**
   * Returns the rows with the amplitude information.
   *
   * @return 		the rows
   */
  public SpreadSheetRowRange getRowsAmplitude() {
    return m_RowsAmplitude;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rowsAmplitudeTipText() {
    return "The rows in the spreadsheet that contain the amplitude information.";
  }

  /**
   * Sets the (optional) column that contains the sample ID.
   *
   * @param value	the column
   */
  public void setColumnID(SpreadSheetColumnIndex value) {
    m_ColumnID = value;
    reset();
  }

  /**
   * Returns the (optional) column that contains the sample ID.
   *
   * @return 		the column
   */
  public SpreadSheetColumnIndex getColumnID() {
    return m_ColumnID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnIDTipText() {
    return "The (optional) column that contains the sample ID.";
  }

  /**
   * Sets the columns with amplitude information.
   *
   * @param value	the columns
   */
  public void setColumnsAmplitude(SpreadSheetColumnRange value) {
    m_ColumnsAmplitude = value;
    reset();
  }

  /**
   * Returns the columns with amplitude information.
   *
   * @return 		the columns
   */
  public SpreadSheetColumnRange getColumnsAmplitude() {
    return m_ColumnsAmplitude;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnsAmplitudeTipText() {
    return "The columns that contain amplitude information.";
  }

  /**
   * Sets the row that contains the sample data names.
   *
   * @param value	the row
   */
  public void setRowSampleDataNames(SpreadSheetRowIndex value) {
    m_RowSampleDataNames = value;
    reset();
  }

  /**
   * Returns the row that contains the sample data names.
   *
   * @return 		the row
   */
  public SpreadSheetRowIndex getRowSampleDataNames() {
    return m_RowSampleDataNames;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rowSampleDataNamesTipText() {
    return "The (optional) row that contains the sample data names.";
  }

  /**
   * Sets the columns with sampledata.
   *
   * @param value	the columns
   */
  public void setColumnsSampleData(SpreadSheetColumnRange value) {
    m_ColumnsSampleData = value;
    reset();
  }

  /**
   * Returns the columns with sampledata.
   *
   * @return 		the columns
   */
  public SpreadSheetColumnRange getColumnsSampleData() {
    return m_ColumnsSampleData;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnsSampleDataTipText() {
    return "The columns that contain sampledata.";
  }

  /**
   * Sets the format to use for the spectrum.
   *
   * @param value	the format
   */
  public void setFormat(String value) {
    m_Format = value;
    reset();
  }

  /**
   * Returns the format to use for the spectrum.
   *
   * @return 		the format
   */
  public String getFormat() {
    return m_Format;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String formatTipText() {
    return "The format for the spectrum.";
  }

  /**
   * Sets the instrument to use for the spectrum.
   *
   * @param value	the instrument
   */
  public void setInstrument(String value) {
    m_Instrument = value;
    reset();
  }

  /**
   * Returns the instrument to use for the spectrum.
   *
   * @return 		the instrument
   */
  public String getInstrument() {
    return m_Instrument;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String instrumentTipText() {
    return "The instrument for the spectrum.";
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
    return Spectrum[].class;
  }

  /**
   * Returns a quick info about the object, which can be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result = QuickInfoHelper.toString(this, "columnID", (m_ColumnID.isEmpty() ? "-none-" : m_ColumnID.getIndex()), "ID: ");
    result += QuickInfoHelper.toString(this, "columnsAmplitude", (m_ColumnsAmplitude.isEmpty() ? "-none-" : m_ColumnsAmplitude.getRange()), ", amplitudes: ");
    result += QuickInfoHelper.toString(this, "columnsSampleData", (m_ColumnsSampleData.isEmpty() ? "-none-" : m_ColumnsSampleData.getRange()), ", sampledata: ");
    result += QuickInfoHelper.toString(this, "waveNumberRegExp", m_WaveNumberRegExp, ", wave no regexp: ");
    result += QuickInfoHelper.toString(this, "waveNumbersInHeader", m_WaveNumbersInHeader, "wave nos in header", ", ");

    return result;
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  @Override
  protected Object doConvert() throws Exception {
    Spectrum[]		result;
    SpreadSheet		sheet;
    int 		rowWave;
    int[] 		rowsAmp;
    int			i;
    int			n;
    int[] 		colsAmp;
    int 		rowMeta;
    int[] 		colsMeta;
    int 		colID;
    Row			row;
    Row			rowWaveObj;
    Row			rowMetaObj;
    int			wave;
    Cell 		cell;
    boolean 		useRegexp;
    String 		wavenoStr;
    float		waveno;
    
    sheet = (SpreadSheet) m_Input;

    useRegexp  = !m_WaveNumberRegExp.getValue().equals(DEFAULT_WAVENO_REGEXP);
    rowWaveObj = null;
    if (m_WaveNumbersInHeader) {
      rowWaveObj = sheet.getHeaderRow();
    }
    else {
      m_RowWaveNumber.setSpreadSheet(sheet);
      rowWave = m_RowWaveNumber.getIntIndex();
      if (rowWave > -1)
	rowWaveObj = sheet.getRow(rowWave);
    }

    m_RowsAmplitude.setSpreadSheet(sheet);
    rowsAmp = m_RowsAmplitude.getIntIndices();
    if (rowsAmp.length == 0)
      throw new IllegalStateException("Failed to locate amplitude rows: " + m_RowsAmplitude);

    m_ColumnsAmplitude.setSpreadSheet(sheet);
    colsAmp = m_ColumnsAmplitude.getIntIndices();
    if (colsAmp.length == 0)
      throw new IllegalStateException("Failed to locate amplitude columns: " + m_ColumnsAmplitude);

    m_RowSampleDataNames.setSpreadSheet(sheet);
    rowMeta    = m_RowSampleDataNames.getIntIndex();
    rowMetaObj = null;
    if (rowMeta > -1)
      rowMetaObj = sheet.getRow(rowMeta);

    m_ColumnsSampleData.setSpreadSheet(sheet);
    colsMeta = m_ColumnsSampleData.getIntIndices();

    m_ColumnID.setSpreadSheet(sheet);
    colID = m_ColumnID.getIntIndex();
    
    result = new Spectrum[rowsAmp.length];
    for (i = 0; i < rowsAmp.length; i++) {
      row       = sheet.getRow(rowsAmp[i]);
      result[i] = new Spectrum();
      result[i].setReport(new SampleData());
      if ((colID > -1) && row.hasCell(colID) && !row.getCell(colID).isMissing())
	result[i].setID(row.getCell(colID).getContent());
      else
        result[i].setID("" + (i+1));
      result[i].setFormat(m_Format);

      // wave numbers
      wave = 0;
      for (n = 0; n < colsAmp.length; n++) {
        if (colsAmp[n] == colID)
          continue;
        wave++;
        if (rowWaveObj == null) {
	  if (row.hasCell(colsAmp[n])) {
	    result[i].add(new SpectrumPoint(
	      wave,
	      row.getCell(colsAmp[n]).toDouble().floatValue()));
	  }
	}
	else {
	  if (rowWaveObj.hasCell(colsAmp[n]) && row.hasCell(colsAmp[n])) {
	    wavenoStr = rowWaveObj.getCell(colsAmp[n]).getContent();
	    if (useRegexp)
	      wavenoStr = wavenoStr.replaceAll(m_WaveNumberRegExp.getValue(), "$1");
	    waveno = Float.parseFloat(wavenoStr);  // TODO locale?
	    result[i].add(new SpectrumPoint(
	      waveno,
	      row.getCell(colsAmp[n]).toDouble().floatValue()));
	  }
	}
      }

      // sample data
      if (rowMetaObj != null) {
	for (n = 0; n < colsMeta.length; n++) {
	  if (colsMeta[n] == colID)
	    continue;
	  if (row.hasCell(colsMeta[n]) && !row.getCell(colsMeta[n]).isMissing()) {
	    cell = row.getCell(colsMeta[n]);
	    if (cell.isNumeric())
	      result[i].getReport().setNumericValue(rowMetaObj.getCell(colsMeta[n]).getContent(), cell.toDouble());
	    else if (cell.isBoolean())
	      result[i].getReport().setBooleanValue(rowMetaObj.getCell(colsMeta[n]).getContent(), cell.toBoolean());
	    else
	      result[i].getReport().setStringValue(rowMetaObj.getCell(colsMeta[n]).getContent(), cell.getContent());
	  }
	}
      }
    }
    
    return result;
  }
}
