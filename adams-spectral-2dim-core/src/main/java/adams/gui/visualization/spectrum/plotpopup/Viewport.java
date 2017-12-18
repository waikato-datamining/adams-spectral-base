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
 * Viewport.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.gui.visualization.spectrum.plotpopup;

import adams.core.base.BaseRegExp;
import adams.data.report.AbstractField;
import adams.data.report.Report;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spectrum.SpectrumUtils;
import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.gui.core.GUIHelper;
import adams.gui.dialog.SpreadSheetDialog;
import adams.gui.visualization.container.DataContainerPanelWithContainerList;
import adams.gui.visualization.container.datacontainerpanel.plotpopup.AbstractPlotPopupCustomizer;
import adams.gui.visualization.core.AxisPanel;
import adams.gui.visualization.core.plot.Axis;
import adams.gui.visualization.spectrum.SpectrumContainer;
import adams.gui.visualization.spectrum.SpectrumContainerManager;
import adams.gui.visualization.spectrum.SpectrumPanel;

import javax.swing.JColorChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Allows the user to perform operations on the spectra visible in the current
 * viewport.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Viewport
  extends AbstractPlotPopupCustomizer<Spectrum, SpectrumContainerManager, SpectrumContainer> {

  private static final long serialVersionUID = 3295471324320509106L;

  /**
   * The name.
   *
   * @return		the name
   */
  @Override
  public String getName() {
    return "Viewport";
  }

  /**
   * The group this customizer belongs to.
   *
   * @return		the group
   */
  @Override
  public String getGroup() {
    return "graphics";
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
   * Locates the visible spectra that are on display in the current viewport.
   *
   * @param panel	the panel to use
   * @return		the list of spectra
   */
  protected List<SpectrumContainer> containersInViewport(DataContainerPanelWithContainerList<Spectrum, SpectrumContainerManager, SpectrumContainer> panel) {
    List<SpectrumContainer>	result;
    AxisPanel 			axisX;
    AxisPanel 			axisY;
    float			minX;
    float			maxX;
    float			minY;
    float			maxY;
    float 			amp;
    int[]			indices;
    int				from;
    int				to;
    int				i;
    Spectrum			sp;
    List<SpectrumPoint>		points;

    result = new ArrayList<>();
    axisX  = panel.getPlot().getAxis(Axis.BOTTOM);
    axisY  = panel.getPlot().getAxis(Axis.LEFT);
    minX   = (float) axisX.getActualMinimum();
    maxX   = (float) axisX.getActualMaximum();
    minY   = (float) axisY.getActualMinimum();
    maxY   = (float) axisY.getActualMaximum();

    for (SpectrumContainer c: panel.getTableModelContainers(true)) {
      sp      = c.getData();
      points  = sp.toList();
      indices = SpectrumUtils.findEnclosingWaveNumbers(points, minX);
      from    = (indices[1] == -1) ? indices[0] : indices[1];
      if (from == -1)
	from = 0;
      indices = SpectrumUtils.findEnclosingWaveNumbers(points, maxX);
      to      = indices[0];
      if (to == -1)
	to = points.size() - 1;
      for (i = from; i <= to; i++) {
	amp = points.get(i).getAmplitude();
	// in viewport?
	if ((amp >= minY) && (amp <= maxY)) {
	  result.add(c);
	  break;
	}
      }
    }

    return result;
  }

  /**
   * Displays the IDs etc of the spectra.
   *
   * @param panel	the affected panel
   * @param conts	the containers to display
   */
  protected void display(DataContainerPanelWithContainerList<Spectrum, SpectrumContainerManager, SpectrumContainer> panel, List<SpectrumContainer> conts) {
    String 		regexpStr;
    BaseRegExp		regexp;
    Set<AbstractField>	fields;
    List<String>	names;
    Report		report;
    SpreadSheet		sheet;
    Row			row;
    Spectrum		sp;
    SpreadSheetDialog	dialog;

    // prompt for regexp
    regexpStr = GUIHelper.showInputDialog(
      panel, "Please enter regexp for fields to include (other than ID and format):", "(Source)");
    if (regexpStr == null)
      return;
    regexp = new BaseRegExp();
    if (!regexp.isValid(regexpStr)) {
      GUIHelper.showErrorMessage(panel, "Invalid regular expression: " + regexpStr);
      return;
    }
    regexp.setValue(regexpStr);

    // get all fields that match regexp
    names  = new ArrayList<>();
    fields = new HashSet<>();
    for (SpectrumContainer c: conts) {
      report = c.getData().getReport();
      for (AbstractField field: report.getFields()) {
	if (regexp.isMatch(field.getName())) {
	  if (!fields.contains(field)) {
	    fields.add(field);
	    names.add(field.getName());
	  }
	}
      }
    }
    Collections.sort(names);
    names.remove(SampleData.SAMPLE_ID);
    names.remove(SampleData.FORMAT);
    names.add(0, SampleData.SAMPLE_ID);
    names.add(1, SampleData.FORMAT);

    // compile spreadsheet
    sheet = new DefaultSpreadSheet();
    row   = sheet.getHeaderRow();
    for (String name: names)
      row.addCell(name).setContentAsString(name);
    for (SpectrumContainer c: conts) {
      sp     = c.getData();
      report = sp.getReport();
      row    = sheet.addRow();
      row.addCell(SampleData.SAMPLE_ID).setContentAsString(sp.getID());
      row.addCell(SampleData.FORMAT).setContentAsString(sp.getFormat());
      for (AbstractField field: fields) {
	if (report.hasValue(field))
	  row.addCell(field.getName()).setNative(report.getValue(field));
      }
    }

    // display spreadsheet
    if (panel.getParentDialog() != null)
      dialog = new SpreadSheetDialog(panel.getParentDialog(), ModalityType.MODELESS);
    else
      dialog = new SpreadSheetDialog(panel.getParentFrame(), false);
    dialog.setDefaultCloseOperation(SpreadSheetDialog.DISPOSE_ON_CLOSE);
    dialog.setTitle("Spectra in viewport");
    dialog.setSpreadSheet(sheet);
    dialog.pack();
    dialog.setLocationRelativeTo(panel);
    dialog.setVisible(true);
  }

  /**
   * Allows the user to choose the color for all the spectra.
   *
   * @param panel	the affected panel
   * @param conts	the containers to modify
   */
  protected void chooseColor(DataContainerPanelWithContainerList<Spectrum, SpectrumContainerManager, SpectrumContainer> panel, List<SpectrumContainer> conts) {
    SpectrumContainerManager	manager;
    Color 			newColor;

    // prompt user
    newColor = JColorChooser.showDialog(
      panel,
      "Choose color",
      Color.BLUE);
    if (newColor == null)
      return;

    manager = panel.getContainerManager();
    manager.startUpdate();

    for (SpectrumContainer cont: conts)
      cont.setColor(newColor);

    manager.finishUpdate();
  }

  /**
   * Allows the user to hide the spectra.
   *
   * @param panel	the affected panel
   * @param conts	the containers to hide
   */
  protected void hide(DataContainerPanelWithContainerList<Spectrum, SpectrumContainerManager, SpectrumContainer> panel, List<SpectrumContainer> conts) {
    SpectrumContainerManager	manager;

    manager = panel.getContainerManager();
    manager.startUpdate();

    for (SpectrumContainer cont: conts)
      cont.setVisible(false);

    manager.finishUpdate();
  }

  /**
   * Allows the user to hide all other spectra.
   *
   * @param panel	the affected panel
   * @param conts	the containers to keep
   */
  protected void hideOthers(DataContainerPanelWithContainerList<Spectrum, SpectrumContainerManager, SpectrumContainer> panel, List<SpectrumContainer> conts) {
    SpectrumContainerManager	manager;
    int				i;
    Set<SpectrumContainer>	keep;

    keep = new HashSet<>(conts);
    manager = panel.getContainerManager();
    manager.startUpdate();

    for (i = 0; i < manager.count(); i++) {
      if (!keep.contains(manager.get(i)))
        manager.get(i).setVisible(false);
    }

    manager.finishUpdate();
  }

  /**
   * Returns a popup menu for the table of the container list.
   *
   * @param panel	the affected panel
   * @param e		the mouse event
   * @param menu	the popup menu to customize
   */
  @Override
  public void customize(DataContainerPanelWithContainerList<Spectrum, SpectrumContainerManager, SpectrumContainer> panel, MouseEvent e, JPopupMenu menu) {
    JMenuItem			submenu;
    JMenuItem			item;
    List<SpectrumContainer>	conts;

    conts = containersInViewport(panel);

    submenu = new JMenu("Viewport");
    menu.add(submenu);
    menu.setEnabled(conts.size() > 0);

    if (conts.size() > 0) {
      item = new JMenuItem("Display...");
      item.addActionListener((ActionEvent ae) -> display(panel, conts));
      submenu.add(item);

      item = new JMenuItem("Choose color...");
      item.addActionListener((ActionEvent ae) -> chooseColor(panel, conts));
      submenu.add(item);

      item = new JMenuItem("Hide");
      item.addActionListener((ActionEvent ae) -> hide(panel, conts));
      submenu.add(item);

      item = new JMenuItem("Hide others");
      item.addActionListener((ActionEvent ae) -> hideOthers(panel, conts));
      submenu.add(item);
    }
  }
}
