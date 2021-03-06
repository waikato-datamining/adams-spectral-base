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
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.flow.core;

import adams.core.MessageCollection;
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
  extends AbstractModelLoader<AbstractCleaner> {

  private static final long serialVersionUID = -2495972217256957904L;

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
   * Deserializes the model file.
   *
   * @param errors	for collecting errors
   * @return		the object read from the file, null if failed
   */
  @Override
  protected Object deserializeFile(MessageCollection errors) {
    SerializableObjectReader	reader;

    reader = new SerializableObjectReader();
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
