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
 * SpreadSheetToSpectrum.java
 * Copyright (C) 2013 University of Waikato, Hamilton, New Zealand
 */
package adams.data.conversion;

import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.spreadsheet.SpreadSheetColumnIndex;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

/**
 <!-- globalinfo-start -->
 * Turns a spreadsheet into a spectrum. The columns that act as wavenumber and amplitude must be specified.
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
 * &nbsp;&nbsp;&nbsp;The column in the spreadsheet that contains the wavenumber information.
 * &nbsp;&nbsp;&nbsp;default: 1
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; apart from column names (case-sensitive), the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 * 
 * <pre>-col-amplitude &lt;adams.data.spreadsheet.SpreadSheetColumnIndex&gt; (property: columnAmplitude)
 * &nbsp;&nbsp;&nbsp;The column in the spreadsheet that contains the amplitude information.
 * &nbsp;&nbsp;&nbsp;default: 2
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; apart from column names (case-sensitive), the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 * 
 * <pre>-id &lt;java.lang.String&gt; (property: ID)
 * &nbsp;&nbsp;&nbsp;The ID for the spectrum.
 * &nbsp;&nbsp;&nbsp;default: 
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
 * @version $Revision: 2242 $
 */
public class SpreadSheetToSpectrum
  extends AbstractConversion {

  /** for serialization. */
  private static final long serialVersionUID = -258589003642261978L;

  /** the wavenumber column. */
  protected SpreadSheetColumnIndex m_ColumnWaveNumber;

  /** the amplitude column. */
  protected SpreadSheetColumnIndex m_ColumnAmplitude;
  
  /** the ID to use for the spectrum. */
  protected String m_ID;
  
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
    return "Turns a spreadsheet into a spectrum. The columns that act as wavenumber and amplitude must be specified.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "col-wave-number", "columnWaveNumber",
	    new SpreadSheetColumnIndex("1"));

    m_OptionManager.add(
	    "col-amplitude", "columnAmplitude",
	    new SpreadSheetColumnIndex("2"));

    m_OptionManager.add(
	    "id", "ID",
	    "");

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
    return "The column in the spreadsheet that contains the wavenumber information.";
  }

  /**
   * Sets the column with the amplitude information.
   *
   * @param value	the column
   */
  public void setColumnAmplitude(SpreadSheetColumnIndex value) {
    m_ColumnAmplitude = value;
    reset();
  }

  /**
   * Returns the column with the amplitude information.
   *
   * @return 		the column
   */
  public SpreadSheetColumnIndex getColumnAmplitude() {
    return m_ColumnAmplitude;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String columnAmplitudeTipText() {
    return "The column in the spreadsheet that contains the amplitude information.";
  }

  /**
   * Sets the ID to use for the spectrum.
   *
   * @param value	the ID
   */
  public void setID(String value) {
    m_ID = value;
    reset();
  }

  /**
   * Returns the ID to use for the spectrum.
   *
   * @return 		the ID
   */
  public String getID() {
    return m_ID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String IDTipText() {
    return "The ID for the spectrum.";
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
    return Spectrum.class;
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  @Override
  protected Object doConvert() throws Exception {
    Spectrum		result;
    SpreadSheet		sheet;
    int			colWave;
    int			colAmp;
    
    sheet = (SpreadSheet) m_Input;

    m_ColumnWaveNumber.setData(sheet);
    colWave = m_ColumnWaveNumber.getIntIndex();
    if (colWave == -1)
      throw new IllegalStateException("Failed to locate wavenumber column: " + m_ColumnWaveNumber);
    if (!sheet.isNumeric(colWave))
      throw new IllegalStateException("Wavenumber column is not numeric: " + m_ColumnWaveNumber);

    m_ColumnAmplitude.setData(sheet);
    colAmp = m_ColumnAmplitude.getIntIndex();
    if (colAmp == -1)
      throw new IllegalStateException("Failed to locate amplitude column: " + m_ColumnAmplitude);
    if (!sheet.isNumeric(colAmp))
      throw new IllegalStateException("Amplitude column is not numeric: " + m_ColumnAmplitude);
    
    result = new Spectrum();
    result.setReport(new SampleData());
    result.setID(m_ID);
    result.setFormat(m_Format);

    for (Row row: sheet.rows()) {
      if (row.hasCell(colWave) && row.hasCell(colAmp)) {
	result.add(new SpectrumPoint(
	    row.getCell(colWave).toDouble().floatValue(),
	    row.getCell(colAmp).toDouble().floatValue()));
      }
    }
    
    return result;
  }
}
