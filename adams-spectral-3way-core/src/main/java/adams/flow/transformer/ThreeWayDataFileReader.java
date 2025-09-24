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
 * ThreeWayDataFileReader.java
 * Copyright (C) 2018-2025 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.data.io.input.DataContainerReader;
import adams.data.io.input.SimpleEEMReader;
import adams.data.threeway.ThreeWayData;

/**
 <!-- globalinfo-start -->
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class ThreeWayDataFileReader
  extends AbstractDataContainerFileReader<ThreeWayData> {

  /** for serialization. */
  private static final long serialVersionUID = 1429977151568224156L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "Loads a file/directory containing 2-way data from disk with the "
      + "specified reader and passes it on.";
  }

  /**
   * Returns the default reader to use.
   *
   * @return		the default reader
   */
  @Override
  protected DataContainerReader getDefaultReader() {
    return new SimpleEEMReader();
  }

  /**
   * Returns the base class of the items.
   *
   * @return		the class
   */
  @Override
  protected Class getItemClass() {
    return ThreeWayData.class;
  }
}
