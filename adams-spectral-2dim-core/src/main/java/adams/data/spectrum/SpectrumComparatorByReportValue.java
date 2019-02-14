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
 * SpectrumComparatorByReportValue.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrum;

import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;

/**
 * Compares spectra based on the specified report field value.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumComparatorByReportValue
  extends AbstractSpectrumComparator {

  private static final long serialVersionUID = 7548983174530831739L;

  /** the field to use.*/
  protected Field m_Field;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Compares spectra based on the specified report field value.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "field", "field",
      new Field(SampleData.INSTRUMENT, DataType.STRING));
  }

  /**
   * Sets the field to inspect.
   *
   * @param value 	the field
   */
  public void setField(Field value) {
    m_Field = value;
    reset();
  }

  /**
   * Returns the field to inspect.
   *
   * @return 		the field
   */
  public Field getField() {
    return m_Field;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String fieldTipText() {
    return "The field to inspect.";
  }

  /**
   * Compares its two arguments for order. Returns a negative integer, zero,
   * or a positive integer as the first argument is less than, equal to, or
   * greater than the second.
   *
   * @param o1		the first object
   * @param o2		the second object
   * @return		a negative integer, zero, or a positive integer as
   * 			the first argument is less than, equal to, or greater
   * 			than the second.
   */
  @Override
  public int compare(Spectrum o1, Spectrum o2) {
    int 	result;

    result = Boolean.compare(o1.hasReport(), o2.hasReport());

    if (result == 0)
      result = Boolean.compare(o1.getReport().hasValue(m_Field), o2.getReport().hasValue(m_Field));

    if (result == 0) {
      switch (m_Field.getDataType()) {
	case BOOLEAN:
	  result = Boolean.compare(o1.getReport().getBooleanValue(m_Field.getName()), o2.getReport().getBooleanValue(m_Field.getName()));
	  break;
	case NUMERIC:
	  result = Double.compare(o1.getReport().getDoubleValue(m_Field.getName()), o2.getReport().getDoubleValue(m_Field.getName()));
	  break;
	case STRING:
	  result = o1.getReport().getStringValue(m_Field.getName()).compareTo(o2.getReport().getStringValue(m_Field.getName()));
	  break;
	case UNKNOWN:
	  result = ("" + o1.getReport().getValue(m_Field)).compareTo("" + o2.getReport().getValue(m_Field));
	  break;
	default:
	  throw new IllegalStateException("Unhandled data type: " + m_Field.getDataType());
      }
    }

    // flip ordering?
    if (!m_Ascending)
      result *= -1;

    return result;
  }
}
