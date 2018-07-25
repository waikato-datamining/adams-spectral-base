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
 * AbstractFieldInstanceGeneratorWithClass.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.instances;

import adams.core.base.BaseString;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.spectrum.Spectrum;

/**
 * Ancestor for generators that use fields and a target to generate the instance.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractFieldInstanceGeneratorWithClass
  extends AbstractFieldInstanceGenerator
  implements InstanceGeneratorWithTargetField, InstanceGeneratorWithClassLabels {

  private static final long serialVersionUID = -2636454665763906454L;

  /** the field that acts as class attribute. */
  protected Field m_Field;

  /** the nominal labels to use, in case of a nominal class. */
  protected BaseString[] m_ClassLabels;

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "class-field", "field",
      new Field("ADN1", DataType.NUMERIC));

    m_OptionManager.add(
      "class-label", "classLabels",
      new BaseString[0]);
  }

  /**
   * Sets the field to act as class attribute.
   *
   * @param value	the field
   */
  public void setField(Field value) {
    m_Field = value;
    reset();
  }

  /**
   * Returns the field that acts as class attribute.
   *
   * @return		the field
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
    return "The field to act as class attribute.";
  }

  /**
   * Sets the labels for a nominal class attribute.
   *
   * @param value	the labels
   */
  public void setClassLabels(BaseString[] value) {
    m_ClassLabels = value;
    reset();
  }

  /**
   * Returns the labels for a nominal class attribute.
   *
   * @return		the labels
   */
  public BaseString[] getClassLabels() {
    return m_ClassLabels;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String classLabelsTipText() {
    return "The class labels to use for a nominal class.";
  }

  /**
   * Checks whether the number of waves are the same.
   *
   * @param data	the input data
   */
  protected void checkHeader(Spectrum data) {
    int		size;

    size = m_OutputHeader.numAttributes();
    size -= m_Notes.length;
    size --;  // class
    if (m_AddDatabaseID)
      size--;
    if (m_AddSampleID)
      size--;

    if (size != m_Fields.length)
      throw new IllegalStateException(
	  "Number of fields and output attributes differ "
            + "(#" + data.getDatabaseID() + "/" + data.getID() + "/" + data.getFormat() + "): "
            + m_Fields.length + " != " + size);
  }
}
