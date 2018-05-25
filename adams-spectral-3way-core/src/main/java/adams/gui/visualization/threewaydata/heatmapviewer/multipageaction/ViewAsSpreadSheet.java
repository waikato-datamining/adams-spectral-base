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

import adams.data.spreadsheet.SpreadSheet;
import adams.gui.dialog.ApprovalDialog;
import adams.gui.dialog.SpreadSheetPanel;
import adams.gui.visualization.threewaydata.heatmapviewer.ThreeWayDataHeatmapPanel;
import adams.gui.visualization.threewaydata.heatmapviewer.ThreeWayDataHeatmapViewerMultiPagePane;

import javax.swing.JMenuItem;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;

/**
 * Views the 3-way data in a spreadsheet table.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ViewAsSpreadSheet
  extends AbstractMultiPageMenuItem {

  private static final long serialVersionUID = 1297273340581059101L;

  /**
   * The name for the menu item.
   *
   * @return		the name
   */
  public String getName() {
    return "View as spreadsheet";
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
    return "spreadsheet.png";
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
            SpreadSheet sheet = panel.getData().toSpreadSheet();
            SpreadSheetPanel sheetPanel = new SpreadSheetPanel();
            sheetPanel.setSpreadSheet(sheet);
            ApprovalDialog dialog;
            if (multi.getParentDialog() != null)
              dialog = new ApprovalDialog(multi.getParentDialog(), ModalityType.MODELESS);
            else
              dialog = new ApprovalDialog(multi.getParentFrame(), false);
            dialog.setTitle("3-way data: " + multi.getSelectedTitle());
            dialog.setDiscardVisible(false);
            dialog.setCancelVisible(false);
            dialog.setApproveCaption("OK");
            dialog.getContentPane().add(sheetPanel, BorderLayout.CENTER);
            dialog.pack();
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
