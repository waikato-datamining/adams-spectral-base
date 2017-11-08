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
 * Information.java
 * Copyright (C) 2016-2017 University of Waikato, Hamilton, NZ
 */

package adams.gui.visualization.spectrum.containerlistpopup;

import adams.data.spectrum.Spectrum;
import adams.data.statistics.InformativeStatistic;
import adams.gui.visualization.container.DataContainerPanelWithContainerList;
import adams.gui.visualization.container.datacontainerpanel.containerlistpopup.AbstractContainerListPopupCustomizer;
import adams.gui.visualization.spectrum.SpectrumContainer;
import adams.gui.visualization.spectrum.SpectrumContainerManager;
import adams.gui.visualization.spectrum.SpectrumPanel;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays information about the spectra.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Information
  extends AbstractContainerListPopupCustomizer<Spectrum, SpectrumContainerManager, SpectrumContainer> {

  private static final long serialVersionUID = -4547544768633536080L;

  /**
   * The name.
   *
   * @return		the name
   */
  @Override
  public String getName() {
    return "Information";
  }

  /**
   * The group this customizer belongs to.
   *
   * @return		the group
   */
  @Override
  public String getGroup() {
    return "view";
  }

  /**
   * Checks whether this action can handle the panel.
   *
   * @param panel	the panel to check
   * @return		true if handled
   */
  @Override
  public boolean handles(DataContainerPanelWithContainerList<Spectrum, SpectrumContainerManager, SpectrumContainer> panel) {
    return (panel instanceof SpectrumPanel);
  }

  /**
   * Returns a popup menu for the table of the container list.
   *
   * @param context	the context
   * @param menu	the popup menu to customize
   */
  @Override
  public void customize(final Context<Spectrum,SpectrumContainerManager,SpectrumContainer> context, JPopupMenu menu) {
    JMenuItem				item;
    final List<SpectrumContainer> 	visibleConts;

    visibleConts = context.visibleConts;
    item = new JMenuItem("Information");
    item.addActionListener((ActionEvent e) -> {
      List<InformativeStatistic> stats = new ArrayList<>();
      for (SpectrumContainer cont: visibleConts)
	stats.add(cont.getData().toStatistic());
      ((SpectrumPanel) context.panel).showStatistics(stats);
    });
    menu.add(item);
  }
}
