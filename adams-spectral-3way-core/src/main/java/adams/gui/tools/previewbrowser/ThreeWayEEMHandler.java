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
 * ThreeWayEEMHandler.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.gui.tools.previewbrowser;

import adams.core.io.PlaceholderFile;
import adams.data.io.input.SimpleEEMReader;
import adams.data.threeway.ThreeWayData;
import adams.gui.visualization.threewaydata.heatmapviewer.ThreeWayDataHeatmapPanel;

import java.io.File;
import java.util.List;

/**
 * Handles 3-way data EEM fluorescence files.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ThreeWayEEMHandler
  extends AbstractContentHandler {

  private static final long serialVersionUID = -2050561508051439752L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Handles 3-way data EEM fluorescence files.";
  }

  /**
   * Returns the list of extensions (without dot) that this handler can
   * take care of.
   *
   * @return		the list of extensions (no dot)
   */
  @Override
  public String[] getExtensions() {
    return new String[]{"dat"};
  }

  /**
   * Creates the actual preview.
   *
   * @param file	the file to create the view for
   * @return		the preview
   */
  @Override
  public PreviewPanel createPreview(File file) {
    SimpleEEMReader		reader;
    List<ThreeWayData> 		list;
    ThreeWayDataHeatmapPanel	panel;

    reader = new SimpleEEMReader();
    reader.setInput(new PlaceholderFile(file));
    list = reader.read();
    if (list.size() == 1) {
      panel = new ThreeWayDataHeatmapPanel(null);
      panel.setData(list.get(0));
      return new PreviewPanel(panel, panel.getImagePanel().getPaintPanel());
    }
    else {
      return new NoPreviewAvailablePanel();
    }
  }
}
