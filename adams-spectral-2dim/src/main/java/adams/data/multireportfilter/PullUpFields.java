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
 * PullUpFields.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package adams.data.multireportfilter;

import adams.data.report.Field;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;

/**
 <!-- globalinfo-start -->
 * Distributes the sample data among all the sub-spectra, never overwrites already existing values.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-field &lt;knir.data.sampledata.Field&gt; [-field ...] (property: fields)
 * &nbsp;&nbsp;&nbsp;The fields to pull up from the sub-reports and store in the top-level one.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class PullUpFields
  extends AbstractMultiSpectrumReportFilter {

  private static final long serialVersionUID = 9130818615270130876L;

  /** the fields to pull up from the sub-spectra and place in the top-level report. */
  protected Field[] m_Fields;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Distributes the sample data among all the sub-spectra, never overwrites already existing values.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "field", "fields",
      new Field[0]);
  }

  /**
   * Sets the fields to pull up.
   *
   * @param value 	the fields
   */
  public void setFields(Field[] value) {
    m_Fields = value;
    reset();
  }

  /**
   * Returns the fields to pull up.
   *
   * @return 		the fields
   */
  public Field[] getFields() {
    return m_Fields;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String fieldsTipText() {
    return "The fields to pull up from the sub-reports and store in the top-level one.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data, null if failed to generate output
   */
  @Override
  protected MultiSpectrum processData(MultiSpectrum data) {
    MultiSpectrum     result;

    result = (MultiSpectrum) data.getClone();
    for (Field field: m_Fields)
      result.getReport().addField(field);

    for (Spectrum sub : result) {
      for (Field field: m_Fields) {
	if (sub.hasReport() && sub.getReport().hasValue(field))
	  result.getReport().setValue(field, sub.getReport().getValue(field));
      }
    }

    return result;
  }
}
