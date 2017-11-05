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
 * AbstractFieldInstanceGenerator.java
 * Copyright (C) 2009-2017 University of Waikato, Hamilton, New Zealand
 */

package adams.data.instances;

import adams.data.report.Field;
import adams.data.spectrum.Spectrum;

/**
 * Abstract base class for schemes that turn spectra into weka.core.Instance
 * objects.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public abstract class AbstractFieldInstanceGenerator
  extends AbstractSpectrumBasedInstanceGenerator
  implements InstanceGeneratorWithFields {

  /** for serialization. */
  private static final long serialVersionUID = 2083516575994387184L;

  /** fields to add to the output data. */
  protected Field[] m_Fields;

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "field", "fields",
	    new Field[0]);
  }

  /**
   * Sets the targets to add.
   *
   * @param value	the targets
   */
  public void setFields(Field[] value) {
    m_Fields = value;
    reset();
  }

  /**
   * Returns the targets to add.
   *
   * @return		the targets
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
    return "The fields to add to the output.";
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
