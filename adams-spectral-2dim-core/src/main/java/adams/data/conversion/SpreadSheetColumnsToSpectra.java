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
 * SpreadSheetColumnsToSpectra.java
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
 * Turns spreadsheet columns into spectra.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-col-wave-number &lt;adams.data.spreadsheet.SpreadSheetColumnIndex&gt; (property: columnWaveNumber)
 * &nbsp;&nbsp;&nbsp;The (optional) column in the spreadsheet that contains the wavenumber information.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
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
 * &nbsp;&nbsp;&nbsp;The columns in the spreadsheet that contain the amplitude information.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-rows-amplitude &lt;adams.data.spreadsheet.SpreadSheetRowRange&gt; (property: rowsAmplitude)
 * &nbsp;&nbsp;&nbsp;The rows that contain amplitude information.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 *
 * <pre>-col-sampledata-names &lt;adams.data.spreadsheet.SpreadSheetColumnIndex&gt; (property: colSampleDataNames)
 * &nbsp;&nbsp;&nbsp;The (optional) column that contains the sample data names.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-rows-sampledata &lt;adams.data.spreadsheet.SpreadSheetRowRange&gt; (property: rowsSampleData)
 * &nbsp;&nbsp;&nbsp;The rows that contain sampledata.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 *
 * <pre>-row-id &lt;adams.data.spreadsheet.SpreadSheetRowIndex&gt; (property: rowID)
 * &nbsp;&nbsp;&nbsp;The (optional) row that contains the sample ID.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; the following placeholders can be used as well: first, second, third, last_2, last_1, last
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
public class SpreadSheetColumnsToSpectra
  extends AbstractConversion {

  /** for serialization. */
  private static final long serialVersionUID = -258589003642261978L;

  public static final String DEFAULT_WAVENO_REGEXP = "(.*)";

  /** the (optional) wavenumber column. */
  protected SpreadSheetColumnIndex m_ColumnWaveNumber;

  /** the regular expression to extract the wave number from the header (first group is used). */
  protected BaseRegExp m_WaveNumberRegExp;

  /** the column with amplitudes. */
  protected SpreadSheetColumnRange m_ColumnsAmplitude;

  /** the rows to get the amplitudes from. */
  protected SpreadSheetRowRange m_RowsAmplitude;

  /** the column with the sample data names. */
  protected SpreadSheetColumnIndex m_ColSampleDataNames;

  /** the rows to get the sample data from. */
  protected SpreadSheetRowRange m_RowsSampleData;

  /** the (optional) row with the sample ID. */
  protected SpreadSheetRowIndex m_RowID;
  
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
    return "Turns spreadsheet columns into spectra.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "col-wave-number", "columnWaveNumber",
      new SpreadSheetColumnIndex());

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
      "col-sampledata-names", "colSampleDataNames",
      new SpreadSheetColumnIndex());

    m_OptionManager.add(
      "rows-sampledata", "rowsSampleData",
      new SpreadSheetRowRange());

    m_OptionManager.add(
      "row-id", "rowID",
      new SpreadSheetRowIndex());

    m_OptionManager.add(
      "format", "format",
      "NIR");

    m_OptionManager.add(
      "instrument", "instrument",
      "unknown");
  }

  /**
   * Sets the column with the wavenumber information.
   *
   * @param value	the column
   */
  public void setColumnWaveNumber(SpreadSheetColumnIndex value) {
    m_ColumnWaveNumber = value;
    reset();
  }

  /**
   * Returns the column with the wavenumber information.
   *
   * @return 		the column
   */
  public SpreadSheetColumnIndex getColumnWaveNumber() {
    return m_ColumnWaveNumber;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnWaveNumberTipText() {
    return "The (optional) column in the spreadsheet that contains the wavenumber information.";
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
   * Sets the columns with the amplitude information.
   *
   * @param value	the columns
   */
  public void setColumnsAmplitude(SpreadSheetColumnRange value) {
    m_ColumnsAmplitude = value;
    reset();
  }

  /**
   * Returns the columns with the amplitude information.
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
    return "The columns in the spreadsheet that contain the amplitude information.";
  }

  /**
   * Sets the (optional) row that contains the sample ID.
   *
   * @param value	the row
   */
  public void setRowID(SpreadSheetRowIndex value) {
    m_RowID = value;
    reset();
  }

  /**
   * Returns the (optional) row that contains the sample ID.
   *
   * @return 		the row
   */
  public SpreadSheetRowIndex getRowID() {
    return m_RowID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rowIDTipText() {
    return "The (optional) row that contains the sample ID.";
  }

  /**
   * Sets the rows with amplitude information.
   *
   * @param value	the rows
   */
  public void setRowsAmplitude(SpreadSheetRowRange value) {
    m_RowsAmplitude = value;
    reset();
  }

  /**
   * Returns the rows with amplitude information.
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
    return "The rows that contain amplitude information.";
  }

  /**
   * Sets the column that contains the sample data names.
   *
   * @param value	the column
   */
  public void setColSampleDataNames(SpreadSheetColumnIndex value) {
    m_ColSampleDataNames = value;
    reset();
  }

  /**
   * Returns the column that contains the sample data names.
   *
   * @return 		the column
   */
  public SpreadSheetColumnIndex getColSampleDataNames() {
    return m_ColSampleDataNames;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String colSampleDataNamesTipText() {
    return "The (optional) column that contains the sample data names.";
  }

  /**
   * Sets the rows with sampledata.
   *
   * @param value	the rows
   */
  public void setRowsSampleData(SpreadSheetRowRange value) {
    m_RowsSampleData = value;
    reset();
  }

  /**
   * Returns the rows with sampledata.
   *
   * @return 		the rows
   */
  public SpreadSheetRowRange getRowsSampleData() {
    return m_RowsSampleData;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rowsSampleDataTipText() {
    return "The rows that contain sampledata.";
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

    result = QuickInfoHelper.toString(this, "rowID", (m_RowID.isEmpty() ? "-none-" : m_RowID.getIndex()), "ID: ");
    result += QuickInfoHelper.toString(this, "rowsAmplitude", (m_RowsAmplitude.isEmpty() ? "-none-" : m_RowsAmplitude.getRange()), ", amplitudes: ");
    result += QuickInfoHelper.toString(this, "rowsSampleData", (m_RowsSampleData.isEmpty() ? "-none-" : m_RowsSampleData.getRange()), ", sampledata: ");
    result += QuickInfoHelper.toString(this, "waveNumberRegExp", m_WaveNumberRegExp, ", wave no regexp: ");

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
    int			colWave;
    int[] 		colsAmp;
    int			i;
    int			n;
    int[]		rowsAmp;
    int			colMeta;
    int[]		rowsMeta;
    int			rowID;
    Row			row;
    int			wave;
    Cell 		cell;
    boolean 		useRegexp;
    String 		wavenoStr;
    float		waveno;

    sheet = (SpreadSheet) m_Input;

    m_ColumnWaveNumber.setSpreadSheet(sheet);
    colWave = m_ColumnWaveNumber.getIntIndex();
    useRegexp = !m_WaveNumberRegExp.getValue().equals(DEFAULT_WAVENO_REGEXP);

    m_ColumnsAmplitude.setSpreadSheet(sheet);
    colsAmp = m_ColumnsAmplitude.getIntIndices();
    if (colsAmp.length == 0)
      throw new IllegalStateException("Failed to locate amplitude columns: " + m_ColumnsAmplitude);

    m_RowsAmplitude.setSpreadSheet(sheet);
    rowsAmp = m_RowsAmplitude.getIntIndices();
    if (rowsAmp.length == 0)
      throw new IllegalStateException("Failed to locate amplitude rows: " + m_RowsAmplitude);

    m_ColSampleDataNames.setSpreadSheet(sheet);
    colMeta = m_ColSampleDataNames.getIntIndex();

    m_RowsSampleData.setSpreadSheet(sheet);
    rowsMeta = m_RowsSampleData.getIntIndices();

    m_RowID.setSpreadSheet(sheet);
    rowID = m_RowID.getIntIndex();
    
    result = new Spectrum[colsAmp.length];
    for (i = 0; i < colsAmp.length; i++) {
      result[i] = new Spectrum();
      result[i].setReport(new SampleData());
      if ((rowID > -1) && sheet.hasCell(rowID, colsAmp[i]) && !sheet.getCell(rowID, colsAmp[i]).isMissing())
	result[i].setID(sheet.getCell(rowID, colsAmp[i]).getContent());
      else
        result[i].setID("" + (i+1));
      result[i].setFormat(m_Format);

      // wave numbers
      wave = 0;
      for (n = 0; n < rowsAmp.length; n++) {
        if (rowsAmp[n] == rowID)
          continue;
        wave++;
        row = sheet.getRow(rowsAmp[n]);
        if (colWave == -1) {
	  if (row.hasCell(colsAmp[i])) {
	    result[i].add(new SpectrumPoint(
	      wave,
	      row.getCell(colsAmp[i]).toDouble().floatValue()));
	  }
	}
	else {
	  if (row.hasCell(colWave) && row.hasCell(colsAmp[i])) {
	    wavenoStr = row.getCell(colWave).getContent();
	    if (useRegexp)
	      wavenoStr = wavenoStr.replaceAll(m_WaveNumberRegExp.getValue(), "$1");
	    waveno = Float.parseFloat(wavenoStr);  // TODO locale?
	    result[i].add(new SpectrumPoint(
	      waveno,
	      row.getCell(colsAmp[i]).toDouble().floatValue()));
	  }
	}
      }

      // sample data
      if (colMeta > -1) {
	for (n = 0; n < rowsMeta.length; n++) {
	  if (rowsMeta[n] == rowID)
	    continue;
	  row = sheet.getRow(rowsMeta[n]);
	  if (row.hasCell(colsAmp[i]) && !row.getCell(colsAmp[i]).isMissing()) {
	    cell = row.getCell(colsAmp[i]);
	    if (cell.isNumeric())
	      result[i].getReport().setNumericValue(row.getCell(colMeta).getContent(), cell.toDouble());
	    else if (cell.isBoolean())
	      result[i].getReport().setBooleanValue(row.getCell(colMeta).getContent(), cell.toBoolean());
	    else
	      result[i].getReport().setStringValue(row.getCell(colMeta).getContent(), cell.getContent());
	  }
	}
      }
    }
    
    return result;
  }
}
