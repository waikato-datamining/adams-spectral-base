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
 * ViewAsSpreadSheet.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.gui.visualization.threewaydata.heatmapviewer.multipageaction;

import adams.data.statistics.InformativeStatistic;
import adams.data.statistics.ThreeWayDataStatistic;
import adams.gui.visualization.statistics.InformativeStatisticFactory;
import adams.gui.visualization.statistics.InformativeStatisticFactory.Dialog;
import adams.gui.visualization.threewaydata.heatmapviewer.ThreeWayDataHeatmapPanel;
import adams.gui.visualization.threewaydata.heatmapviewer.ThreeWayDataHeatmapViewerMultiPagePane;

import javax.swing.JMenuItem;
import javax.swing.SwingWorker;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays statistics for the 3-day data container.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Statistics
  extends AbstractMultiPageMenuItem {

  private static final long serialVersionUID = 1297273340581059101L;

  /**
   * The name for the menu item.
   *
   * @return		the name
   */
  public String getName() {
    return "Statistics";
  }

  /**
   * The name of the group this item belongs to.
   *
   * @return		the name
   */
  public String getGroup() {
    return "View";
  }

  /**
   * The name of the icon to use.
   *
   * @return		the name
   */
  public String getIconName() {
    return "statistics.png";
  }

  /**
   * Creates the menu item.
   */
  public JMenuItem getMenuItem(ThreeWayDataHeatmapViewerMultiPagePane multi) {
    JMenuItem 			result;

    result = new JMenuItem(getName());
    result.setIcon(getIcon());
    result.setEnabled(multi.hasCurrentPanel() && (multi.getCurrentPanel().getData() != null));
    if (result.isEnabled()) {
      result.addActionListener((ActionEvent ae) -> {
        SwingWorker worker = new SwingWorker() {
          @Override
          protected Object doInBackground() throws Exception {
            ThreeWayDataHeatmapPanel panel = multi.getCurrentPanel();
            ThreeWayDataStatistic stats = new ThreeWayDataStatistic();
            stats.setData(panel.getData());
            List<InformativeStatistic> statsList = new ArrayList<>();
            statsList.add(stats);
            Dialog dialog;
            if (panel.getParentDialog() != null)
              dialog = InformativeStatisticFactory.getDialog(panel.getParentDialog(), ModalityType.MODELESS);
            else
              dialog = InformativeStatisticFactory.getDialog(panel.getParentFrame(), false);
            dialog.setDefaultCloseOperation(Dialog.DISPOSE_ON_CLOSE);
            dialog.setStatistics(statsList);
            dialog.setLocationRelativeTo(dialog.getParent());
            dialog.setVisible(true);
            return null;
          }
        };
        worker.execute();
      });
    }

    return result;
  }
}
