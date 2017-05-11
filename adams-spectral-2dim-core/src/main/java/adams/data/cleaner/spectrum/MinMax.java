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
 * MinMax.java
 * Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 */

package adams.data.cleaner.spectrum;

import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.spectrum.Spectrum;

/**
 <!-- globalinfo-start -->
 * Checks a field in the sample data whether the value is within a certain range.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-field &lt;knir.data.sampledata.Field&gt; (property: field)
 *         The field in the sample data to check.
 *         default: knir.data.sampledata.Field
 * </pre>
 *
 * <pre>-min &lt;double&gt; (property: minimum)
 *         The minimum value the field is allowed to have (inclusive).
 *         default: 0.0
 * </pre>
 *
 * <pre>-max &lt;double&gt; (property: maximum)
 *         Only every n-th point will be output.
 *         default: 1000.0
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class MinMax
  extends AbstractCleaner {

  /** for serialization. */
  private static final long serialVersionUID = -8989133595138625428L;

  /** the field in the sample data to check. */
  protected Field m_Field;

  /** the minimum value of the field. */
  protected double m_Minimum;

  /** the maximum value of the field. */
  protected double m_Maximum;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return
        "Checks a field in the sample data whether the value is within a "
      + "certain range.";
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "field", "field",
	    new Field("moisture", DataType.NUMERIC));

    m_OptionManager.add(
	    "min", "minimum",
	    -1.0);

    m_OptionManager.add(
	    "max", "maximum",
	    -1.0);
  }

  /**
   * Sets the field to use.
   *
   * @param value 	the field to use
   */
  public void setField(Field value) {
    m_Field = value;
    reset();
  }

  /**
   * Returns the field in use.
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
    return "The field in the sample data to check.";
  }

  /**
   * Sets the minimum value the field can have (incl.).
   *
   * @param value 	the minimum
   */
  public void setMinimum(double value) {
    m_Minimum = value;
    reset();
  }

  /**
   * Returns the minimum value the field can have (incl.).
   *
   * @return 		the minimum
   */
  public double getMinimum() {
    return m_Minimum;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String minimumTipText() {
    return "The minimum value the field is allowed to have (inclusive), use -1 to disable.";
  }

  /**
   * Sets maximum value the field is allowed to have (incl).
   *
   * @param value 	the maximum
   */
  public void setMaximum(double value) {
    m_Maximum = value;
    reset();
  }

  /**
   * Returns the maximum value the field is allowed to have (incl).
   *
   * @return 		the maximum
   */
  public double getMaximum() {
    return m_Maximum;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String maximumTipText() {
    return "The maximum value that the field is allowed to have (incl), use -1 to disable.";
  }

  /**
   * Performs the actual check.
   *
   * @param data	the spectrum to check
   * @return		null if ok, otherwise error message
   */
  protected String performCheck(Spectrum data) {
    Double	value;

    if (!data.hasReport())
      return "No sample data available";

    value = data.getReport().getDoubleValue(m_Field);
    if (value == null)
      return "Field '" + m_Field + "' not present";

    if ((m_Minimum != -1.0) && (value < m_Minimum))
      return "Value '" + m_Field + "' below minimum: " + value + "<" + m_Minimum;

    if ((m_Maximum != -1.0) && (value > m_Maximum))
      return "Value '" + m_Field + "' over maximum: " + value + ">" + m_Maximum;

    return null;
  }
}
