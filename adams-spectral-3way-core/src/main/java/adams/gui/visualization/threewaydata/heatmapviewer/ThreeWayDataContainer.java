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
 * ThreeWayDataContainer.java
 * Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */
package adams.gui.visualization.threewaydata.heatmapviewer;

import adams.data.threeway.ThreeWayData;
import adams.gui.visualization.container.AbstractContainer;
import adams.gui.visualization.container.NamedContainer;

/**
 * Container for 3-way data.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class ThreeWayDataContainer
  extends AbstractContainer
  implements NamedContainer {

  /** for serialization. */
  private static final long serialVersionUID = -5760557328947147740L;

  /**
   * Initializes the container.
   *
   * @param manager	the manager this container belongs to
   * @param payload	the heatmap of this container
   */
  public ThreeWayDataContainer(ThreeWayDataContainerManager manager, ThreeWayData payload) {
    super(manager, payload);
  }

  /**
   * Sets the container's ID.
   *
   * @param value	the new ID
   */
  public void setID(String value) {
    ((ThreeWayData) getPayload()).setID(value);
  }

  /**
   * Returns the container's ID.
   *
   * @return		the ID
   */
  public String getID() {
    return ((ThreeWayData) getPayload()).getID();
  }

  /**
   * Returns the displayed container's ID.
   *
   * @return		the displayed ID
   */
  public String getDisplayID() {
    return getID();
  }
}
