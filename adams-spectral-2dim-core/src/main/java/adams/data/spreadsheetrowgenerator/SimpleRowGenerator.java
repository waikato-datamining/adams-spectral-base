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
 * SimpleRowGenerator.java
 * Copyright (C) 2021 University of Waikato, Hamilton, NZ
 */

package adams.data.spreadsheetrowgenerator;

import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;

import java.util.List;

/**
 * Outputs spectral and sample data.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SimpleRowGenerator
  extends AbstractSpreadSheetRowGenerator {

  private static final long serialVersionUID = -565276918178382521L;

  /** whether to use the wave numbers in the header. */
  protected boolean m_WaveNumbersInHeader;

  /** the amplitude column prefix. */
  protected String m_AmplitudePrefix;

  /** reference value fields to add to the output data. */
  protected Field[] m_ReferenceValues;

  /** the reference value field prefix. */
  protected String m_ReferenceValuePrefix;

  /** meta-data fields to add to the output data. */
  protected Field[] m_MetaDataFields;

  /** the meta-data field prefix. */
  protected String m_MetaDataPrefix;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Outputs spectral and sample data.";
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "wave-numbers-in-header", "waveNumbersInHeader",
      false);

    m_OptionManager.add(
      "amplitude-prefix", "amplitudePrefix",
      "");

    m_OptionManager.add(
      "reference-value", "referenceValues",
      new Field[0]);

    m_OptionManager.add(
      "reference-value-prefix", "referenceValuePrefix",
      "");

    m_OptionManager.add(
      "meta-data-field", "metaDataFields",
      new Field[0]);

    m_OptionManager.add(
      "meta-data-prefix", "metaDataPrefix",
      "");
  }

  /**
   * Sets whether to use the wave numbers in the header.
   *
   * @param value	true if in header
   */
  public void setWaveNumbersInHeader(boolean value) {
    m_WaveNumbersInHeader = value;
    reset();
  }

  /**
   * Returns whether to use the wave numbers in the header.
   *
   * @return		true if in header
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
    return "If enabled, the wave numbers are used in the header.";
  }

  /**
   * Sets the column prefix for the amplitudes.
   *
   * @param value	the prefix
   */
  public void setAmplitudePrefix(String value) {
    m_AmplitudePrefix = value;
    reset();
  }

  /**
   * Returns the column prefix for the amplitudes.
   *
   * @return		the prefix
   */
  public String getAmplitudePrefix() {
    return m_AmplitudePrefix;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String amplitudePrefixTipText() {
    return "The column prefix to use for the amplitudes.";
  }

  /**
   * Sets the reference value fields to add.
   *
   * @param value	the fields
   */
  public void setReferenceValues(Field[] value) {
    m_ReferenceValues = value;
    reset();
  }

  /**
   * Returns the reference value fields to add.
   *
   * @return		the fields
   */
  public Field[] getReferenceValues() {
    return m_ReferenceValues;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String referenceValuesTipText() {
    return "The reference value fields to add to the output.";
  }

  /**
   * Sets the column prefix for the reference values.
   *
   * @param value	the prefix
   */
  public void setReferenceValuePrefix(String value) {
    m_ReferenceValuePrefix = value;
    reset();
  }

  /**
   * Returns the column prefix for the reference values.
   *
   * @return		the prefix
   */
  public String getReferenceValuePrefix() {
    return m_ReferenceValuePrefix;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String referenceValuePrefixTipText() {
    return "The column prefix to use for the reference values.";
  }

  /**
   * Sets the meta-data fields to add.
   *
   * @param value	the fields
   */
  public void setMetaDataFields(Field[] value) {
    m_MetaDataFields = value;
    reset();
  }

  /**
   * Returns the meta-data fields to add.
   *
   * @return		the fields
   */
  public Field[] getMetaDataFields() {
    return m_MetaDataFields;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String metaDataFieldsTipText() {
    return "The meta-data fields to add to the output.";
  }

  /**
   * Sets the column prefix for the meta-data values.
   *
   * @param value	the prefix
   */
  public void setMetaDataPrefix(String value) {
    m_MetaDataPrefix = value;
    reset();
  }

  /**
   * Returns the column prefix for the meta-data values.
   *
   * @return		the prefix
   */
  public String getMetaDataPrefix() {
    return m_MetaDataPrefix;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String metaDataPrefixTipText() {
    return "The column prefix to use for the meta-data values.";
  }

  /**
   * Checks whether the number of waves are the same.
   *
   * @param data	the input data
   */
  protected void checkHeader(Spectrum data) {
    int		size;

    size = m_OutputHeader.getColumnCount();
    if (m_AddDatabaseID)
      size--;
    if (m_AddSampleID)
      size--;
    size -= m_ReferenceValues.length;
    size -= m_MetaDataFields.length;

    if (size != data.size())
      throw new IllegalStateException(
	  "Number of wave numbers and output columns differ "
            + "(#" + data.getDatabaseID() + "/" + data.getID() + "/" + data.getFormat() + "): "
            + data.size() + " != " + size);
  }

  /**
   * Generates the header of the output data.
   *
   * @param data the input data
   */
  @Override
  protected void generateHeader(Spectrum data) {
    SpreadSheet  	header;
    Row			row;
    int			i;
    List<SpectrumPoint> points;

    header = new DefaultSpreadSheet();
    row    = header.getHeaderRow();

    points = data.toList();
    for (i = 0; i < points.size(); i++) {
      if (m_WaveNumbersInHeader)
	row.addCell("ampl-" + i).setContentAsString(m_AmplitudePrefix + points.get(i).getWaveNumber());
      else
	row.addCell("ampl-" + i).setContentAsString(m_AmplitudePrefix + (i+1));
    }
    for (i = 0; i < m_ReferenceValues.length; i++)
      row.addCell("ref-" + i + "-" + m_ReferenceValues[i].getName()).setContentAsString(m_ReferenceValuePrefix + m_ReferenceValues[i].getName());
    for (i = 0; i < m_MetaDataFields.length; i++)
      row.addCell("meta-" + i + "-" + m_MetaDataFields[i].getName()).setContentAsString(m_MetaDataPrefix + m_MetaDataFields[i].getName());

    m_OutputHeader = header;
  }
  
  /**
   * Generates the actual data.
   *
   * @param data the input data to transform
   * @return the generated data
   */
  @Override
  protected Row generateOutput(Spectrum data) {
    Row			result;
    int			i;
    SampleData		sd;
    String		index;
    List<SpectrumPoint> points;

    result = m_OutputHeader.addRow();
    sd     = data.getReport();

    // amplitudes
    points = data.toList();
    for (i = 0; i < points.size(); i++)
      result.addCell("ampl-" + i).setContent(points.get(i).getAmplitude());

    // reference values
    for (i = 0; i < m_ReferenceValues.length; i++) {
      index = "ref-" + i + "-" + m_ReferenceValues[i].getName();
      if (sd.hasValue(m_ReferenceValues[i])) {
        switch (m_ReferenceValues[i].getDataType()) {
	  case NUMERIC:
	    result.addCell(index).setContent(sd.getDoubleValue(m_ReferenceValues[i]));
	    break;
	  case BOOLEAN:
	    result.addCell(index).setContent(sd.getBooleanValue(m_ReferenceValues[i]));
	    break;
	  default:
	    result.addCell(index).setContentAsString(sd.getStringValue(m_ReferenceValues[i]));
	    break;
	}
      }
    }
    
    // meta-data values
    for (i = 0; i < m_MetaDataFields.length; i++) {
      index = "meta-" + i + "-" + m_MetaDataFields[i].getName();
      if (sd.hasValue(m_MetaDataFields[i])) {
        switch (m_MetaDataFields[i].getDataType()) {
	  case NUMERIC:
	    result.addCell(index).setContent(sd.getDoubleValue(m_MetaDataFields[i]));
	    break;
	  case BOOLEAN:
	    result.addCell(index).setContent(sd.getBooleanValue(m_MetaDataFields[i]));
	    break;
	  default:
	    result.addCell(index).setContentAsString(sd.getStringValue(m_MetaDataFields[i]));
	    break;
	}
      }
    }

    m_OutputHeader.removeRow(m_OutputHeader.getRowCount() - 1);

    return result;
  }
}
