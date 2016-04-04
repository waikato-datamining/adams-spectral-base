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

/**
 * SpectrumZoomOverviewPanel.java
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package adams.gui.visualization.spectrum;

import adams.data.spectrum.Spectrum;
import adams.gui.visualization.container.AbstractDataContainerZoomOverviewPanel;

/**
 * Panel that shows the zoom in the spectrum panel as overlay.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1279 $
 */
public class SpectrumZoomOverviewPanel
  extends AbstractDataContainerZoomOverviewPanel<SpectrumPanel, SpectrumPaintlet, SpectrumZoomOverviewPaintlet, Spectrum, SpectrumContainerManager> {

  /** for serialization. */
  private static final long serialVersionUID = -5141649373267221710L;

  /**
   * Creates a new zoom paintlet.
   * 
   * @return		the paintlet
   */
  protected SpectrumZoomOverviewPaintlet newZoomPaintlet() {
    return new SpectrumZoomOverviewPaintlet();
  }
}
