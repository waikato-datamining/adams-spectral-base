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
 * AbstractThreeWayDataOverlay.java
 * Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */
package adams.gui.visualization.threewaydata.heatmapviewer.overlay;

import adams.gui.visualization.image.AbstractImageOverlay;
import adams.gui.visualization.image.ImagePanel.PaintPanel;
import adams.gui.visualization.threewaydata.heatmapviewer.ThreeWayDataHeatmapPanel;

import java.awt.Graphics;

/**
 * Ancestor for 3-way data image overlays.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractThreeWayDataOverlay
  extends AbstractImageOverlay {

  /** for serialization. */
  private static final long serialVersionUID = -8198433620642324789L;

  /** the heatmap panel. */
  protected ThreeWayDataHeatmapPanel m_DataPanel;

  /**
   * Sets the data panel this overlay is for.
   *
   * @param value	the panel
   */
  public void setDataPanel(ThreeWayDataHeatmapPanel value) {
    m_DataPanel = value;
    reset();
  }

  /**
   * Returns the data panel this overlay is for.
   *
   * @return		the panel
   */
  public ThreeWayDataHeatmapPanel getDataPanel() {
    return m_DataPanel;
  }

  /**
   * Paints the overlay over the image.
   *
   * @param panel	the panel this overlay is for
   * @param g		the graphics context
   */
  @Override
  public void paintOverlay(PaintPanel panel, Graphics g) {
    if (m_DataPanel != null)
      super.paintOverlay(panel, g);
    else
      getLogger().severe("Not data panel set!");
  }
}
