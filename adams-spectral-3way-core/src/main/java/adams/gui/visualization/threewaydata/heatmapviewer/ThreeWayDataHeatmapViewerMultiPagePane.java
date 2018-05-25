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
 * ThreeWayDataHeatmapViewerMultiPagePane.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.gui.visualization.threewaydata.heatmapviewer;

import adams.core.ClassLister;
import adams.gui.core.BasePopupMenu;
import adams.gui.core.ConsolePanel;
import adams.gui.core.MultiPagePane;
import adams.gui.visualization.threewaydata.heatmapviewer.multipageaction.AbstractMultiPageMenuItem;

import javax.swing.JMenuItem;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Specialized multi-page pane for the 3way data heatmap viewer.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ThreeWayDataHeatmapViewerMultiPagePane
  extends MultiPagePane {

  private static final long serialVersionUID = -5878611053189981659L;

  /** the menu items. */
  protected List<AbstractMultiPageMenuItem> m_MenuItems;

  /**
   * Initializes the widgets.
   */
  @Override
  protected void initGUI() {
    super.initGUI();

    setToolTipCustomizer(new ToolTipCustomizer() {
      @Override
      public String customizeToolTip(int index, String toolTip) {
	if ((index >= 0) && (index < m_PageListModel.getSize())) {
	  ThreeWayDataHeatmapPanel panel = (ThreeWayDataHeatmapPanel) m_PageListModel.get(index).getPage();
	  if (panel.getCurrentFile() != null)
	    toolTip = "<html>" + toolTip + "<br>" + panel.getCurrentFile() + "</html>";
	}
	return toolTip;
      }
    });
  }

  /**
   * For initializing members.
   */
  @Override
  protected void initialize() {
    Class[]	classes;

    super.initialize();

    m_MenuItems = new ArrayList<>();
    classes     = ClassLister.getSingleton().getClasses(AbstractMultiPageMenuItem.class);
    for (Class cls: classes) {
      try {
        m_MenuItems.add((AbstractMultiPageMenuItem) cls.newInstance());
      }
      catch (Exception e) {
        ConsolePanel.getSingleton().append(
          "Failed to instantiate multi-page pane menu item for 3way heatmap viewer: " + cls.getName(), e);
      }
    }
    Collections.sort(m_MenuItems);
  }

  /**
   * Returns whether a panel is currently selected.
   *
   * @return		true if selected
   */
  public boolean hasCurrentPanel() {
    return (getSelectedIndex() > -1);
  }

  /**
   * Returns the currently selected panel.
   *
   * @return		the panel, null if none selected
   */
  public ThreeWayDataHeatmapPanel getCurrentPanel() {
    if (!hasCurrentPanel())
      return null;
    return (ThreeWayDataHeatmapPanel) getSelectedPage();
  }

  /**
   * Generates the right-click menu for the JList.
   *
   * @param e		the event that triggered the popup
   * @return		the generated menu
   * @see		#showPopup(MouseEvent)
   */
  @Override
  protected BasePopupMenu createPopup(MouseEvent e) {
    BasePopupMenu	result;
    JMenuItem 		menuitem;
    String		group;

    result = super.createPopup(e);

    group = "";
    for (AbstractMultiPageMenuItem item: m_MenuItems) {
      menuitem = item.getMenuItem(this);
      if (menuitem != null) {
	if (!item.getGroup().equals(group)) {
	  result.addSeparator();
	  group = item.getGroup();
	}
	result.add(menuitem);
      }
    }

    return result;
  }
}
