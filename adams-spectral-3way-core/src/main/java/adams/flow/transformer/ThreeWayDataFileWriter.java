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
 * ThreeWayDataFileWriter.java
 * Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.data.io.output.AbstractDataContainerWriter;
import adams.data.io.output.SimpleEEMWriter;
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
public class ThreeWayDataFileWriter
  extends AbstractDataContainerFileWriter<ThreeWayData> {

  /** for serialization. */
  private static final long serialVersionUID = -7990944411836957831L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "Saves 3-day data to disk with the "
      + "specified writer and passes the absolute filename on.\n"
      + "As filename/directory name (depending on the writer) the "
      + "database ID of the spectrum is used (below the specified output "
      + "directory).";
  }

  /**
   * Returns the default writer to use.
   *
   * @return		the default writer
   */
  @Override
  protected AbstractDataContainerWriter<ThreeWayData> getDefaultWriter() {
    return new SimpleEEMWriter();
  }

  /**
   * Returns the data container class in use.
   *
   * @return		the container class
   */
  @Override
  protected Class getDataContainerClass() {
    return ThreeWayData.class;
  }
}
