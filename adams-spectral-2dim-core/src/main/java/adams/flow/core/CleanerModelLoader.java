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
 * CleanerModelLoader.java
 * Copyright (C) 2017-2025 University of Waikato, Hamilton, NZ
 */

package adams.flow.core;

import adams.core.LenientModeSupporter;
import adams.core.MessageCollection;
import adams.core.SerializableObject;
import adams.core.Utils;
import adams.data.cleaner.instance.AbstractCleaner;
import adams.data.io.input.SerializableObjectReader;
import adams.flow.container.AbstractContainer;
import adams.flow.container.CleaningContainer;

/**
 * Model loader for {@link AbstractCleaner} objects.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class CleanerModelLoader
  extends AbstractModelLoader<AbstractCleaner>
  implements LenientModeSupporter {

  private static final long serialVersionUID = -2495972217256957904L;

  /** whether to be lenient. */
  protected boolean m_Lenient;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Manages " + Utils.classToString(AbstractCleaner.class) + " objects.";
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "lenient", "lenient",
      false);
  }

  /**
   * Sets whether lenient, ie first tries to load the object as {@link SerializableObject}
   * and if that fails just deserializes it.
   *
   * @param value	true if lenient
   */
  public void setLenient(boolean value) {
    m_Lenient = value;
    reset();
  }

  /**
   * Returns whether lenient, ie first tries to load the object as {@link SerializableObject}
   * and if that fails just deserializes it.
   *
   * @return		true if lenient
   */
  public boolean getLenient() {
    return m_Lenient;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String lenientTipText() {
    return "If enabled, first tries to load the object as "
	     + Utils.classToString(SerializableObject.class) +
	     " and if that fails just deserializes it.";
  }

  /**
   * Deserializes the model file.
   *
   * @param errors	for collecting errors
   * @return		the object read from the file, null if failed
   */
  @Override
  protected Object deserializeFile(MessageCollection errors) {
    SerializableObjectReader	reader;

    reader = new SerializableObjectReader();
    reader.setLenient(m_Lenient);
    return reader.read(m_ModelFile);
  }

  /**
   * Retrieves the model from the container.
   *
   * @param cont	the container to get the model from
   * @param errors	for collecting errors
   * @return		the model, null if not in container
   */
  @Override
  protected AbstractCleaner getModelFromContainer(AbstractContainer cont, MessageCollection errors) {
    if (cont instanceof CleaningContainer)
      return (AbstractCleaner) cont.getValue(CleaningContainer.VALUE_CLEANER);

    unhandledContainer(cont, errors);
    return null;
  }
}
