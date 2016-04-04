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
 * SpectrumContainerTable.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */
package adams.gui.visualization.spectrum;

import adams.core.Range;
import adams.gui.core.BasePanel;
import adams.gui.core.GUIHelper;
import adams.gui.scripting.AbstractScriptingEngine;
import adams.gui.scripting.ClearData;
import adams.gui.scripting.RemoveData;
import adams.gui.scripting.ScriptingEngine;
import adams.gui.visualization.container.ContainerTable;

/**
 * Specialized container table for spectra.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public class SpectrumContainerTable
  extends ContainerTable<SpectrumContainerManager, SpectrumContainer> {

  /** for serialization. */
  private static final long serialVersionUID = 5100124289686775844L;

  /**
   * Returns the current scripting engine, can be null.
   *
   * @return		the current engine
   */
  public AbstractScriptingEngine getScriptingEngine() {
    return ScriptingEngine.getSingleton(getManager().getDatabaseConnection());
  }

  /**
   * Removes the containers from the table.
   *
   * @param indices	the indices in the table of the containers to remove
   */
  public void removeContainers(int[] indices) {
    Range 	range;
    BasePanel 	parent;

    parent = (BasePanel) GUIHelper.getParent(this, SpectrumPanel.class);

    if (indices == null) {
      getScriptingEngine().add(
	  parent,
	  ClearData.ACTION);
    }
    else if (indices.length > 0) {
      range = new Range();
      range.setMax(getManager().count());
      range.setIndices(indices);
      getScriptingEngine().add(
	  parent,
	  RemoveData.ACTION + " " + range.getRange());
    }
  }
}
