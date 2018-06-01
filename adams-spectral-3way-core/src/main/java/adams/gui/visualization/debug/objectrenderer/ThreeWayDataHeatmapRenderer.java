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
 * ThreeWayDataRenderer.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.gui.visualization.debug.objectrenderer;

import adams.data.threeway.ThreeWayData;
import adams.gui.visualization.threewaydata.heatmapviewer.ThreeWayDataHeatmapPanel;
import nz.ac.waikato.cms.locator.ClassLocator;

import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Renders 3way data objects as heatmaps.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ThreeWayDataHeatmapRenderer
  extends AbstractObjectRenderer {

  private static final long serialVersionUID = -3528006886476495175L;

  /**
   * Checks whether the renderer can handle the specified class.
   *
   * @param cls		the class to check
   * @return		true if the renderer can handle this type of object
   */
  @Override
  public boolean handles(Class cls) {
    return ClassLocator.isSubclass(ThreeWayData.class, cls);
  }

  /**
   * Performs the actual rendering.
   *
   * @param obj		the object to render
   * @param panel	the panel to render into
   * @return		null if successful, otherwise error message
   */
  @Override
  protected String doRender(Object obj, JPanel panel) {
    ThreeWayData 		data;
    ThreeWayDataHeatmapPanel 	spPanel;

    data    = (ThreeWayData) obj;
    spPanel = new ThreeWayDataHeatmapPanel(null);
    spPanel.setData(data);
    panel.add(spPanel, BorderLayout.CENTER);

    return null;
  }
}
