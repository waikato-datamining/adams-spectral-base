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
 * SpectrumPanel.java
 * Copyright (C) 2008-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.visualization.spectrum;

import adams.core.Properties;
import adams.core.Range;
import adams.core.io.FileUtils;
import adams.core.io.PlaceholderFile;
import adams.core.option.OptionUtils;
import adams.data.instances.AbstractSpectrumInstanceGenerator;
import adams.data.io.output.AbstractDataContainerWriter;
import adams.data.io.output.MetaFileWriter;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.statistics.ArrayHistogram;
import adams.data.statistics.InformativeStatistic;
import adams.db.AbstractDatabaseConnection;
import adams.db.DatabaseConnection;
import adams.event.DatabaseConnectionChangeEvent;
import adams.gui.core.BasePopupMenu;
import adams.gui.core.GUIHelper;
import adams.gui.dialog.SpreadSheetDialog;
import adams.gui.event.PaintListener;
import adams.gui.scripting.AbstractScriptingEngine;
import adams.gui.scripting.Invisible;
import adams.gui.scripting.SetData;
import adams.gui.scripting.SpectralScriptingEngine;
import adams.gui.scripting.Visible;
import adams.gui.sendto.SendToActionUtils;
import adams.gui.visualization.container.ContainerListPopupMenuSupplier;
import adams.gui.visualization.container.ContainerTable;
import adams.gui.visualization.container.DataContainerPanelWithSidePanel;
import adams.gui.visualization.container.NotesFactory;
import adams.gui.visualization.core.AbstractColorProvider;
import adams.gui.visualization.core.CoordinatesPaintlet;
import adams.gui.visualization.core.CoordinatesPaintlet.Coordinates;
import adams.gui.visualization.core.DefaultColorProvider;
import adams.gui.visualization.core.PlotPanel;
import adams.gui.visualization.core.PopupMenuCustomizer;
import adams.gui.visualization.core.plot.Axis;
import adams.gui.visualization.core.plot.TipTextCustomizer;
import adams.gui.visualization.report.ReportContainer;
import adams.gui.visualization.report.ReportFactory;
import adams.gui.visualization.statistics.InformativeStatisticFactory;
import com.github.fracpete.jclipboardhelper.ClipboardHelper;
import gnu.trove.list.array.TIntArrayList;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.ConverterUtils.DataSink;

import javax.swing.JColorChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Special panel for displaying the spectral data.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2289 $
 */
public class SpectrumPanel
  extends DataContainerPanelWithSidePanel<Spectrum, SpectrumContainerManager>
  implements PaintListener, ContainerListPopupMenuSupplier<SpectrumContainerManager,SpectrumContainer>,
             TipTextCustomizer, PopupMenuCustomizer {

  /** for serialization. */
  private static final long serialVersionUID = -9059718549932104312L;

  /** whether to adjust to visible data or not. */
  protected boolean m_AdjustToVisibleData;

  /** paintlet for drawing the X-axis. */
  protected CoordinatesPaintlet m_CoordinatesPaintlet;

  /** paintlet for drawing the spectrum. */
  protected SpectrumPaintlet m_SpectrumPaintlet;

  /** paintlet for drawing the selected wave number. */
  protected SelectedWaveNumberPaintlet m_SelectedWaveNumberPaintlet;

  /** the panel listing the spectra. */
  protected SpectrumContainerList m_SpectrumContainerList;

  /** whether to use store or active tables. */
  protected boolean m_UseStore;

  /** for detecting hits. */
  protected SpectrumPointHitDetector m_SpectrumPointHitDetector;

  /** the dialog for the histogram setup. */
  protected HistogramFactory.SetupDialog m_HistogramSetup;

  /** the maximum number of columns for the tooltip. */
  protected int m_ToolTipMaxColumns;

  /** the zoom overview panel. */
  protected SpectrumZoomOverviewPanel m_PanelZoomOverview;

  /** for exporting visible spectra. */
  protected SpectrumExportDialog m_ExportDialog;

  /** for exporting visible spectra to a dataset. */
  protected SpectrumDatasetExportDialog m_ExportDatasetDialog;

  /**
   * Initializes the panel without title.
   */
  public SpectrumPanel() {
    super();
  }

  /**
   * Initializes the panel with the given title.
   *
   * @param title	the title for the panel
   */
  public SpectrumPanel(String title) {
    super(title);
  }

  /**
   * For initializing members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_AdjustToVisibleData = true;
    m_UseStore            = false;
    m_HistogramSetup      = null;
  }

  /**
   * Returns the default database connection.
   *
   * @return		the default database connection
   */
  @Override
  protected AbstractDatabaseConnection getDefaultDatabaseConnection() {
    return DatabaseConnection.getSingleton();
  }

  /**
   * Returns the container manager to use.
   *
   * @return		the container manager
   */
  @Override
  protected SpectrumContainerManager newContainerManager() {
    return new SpectrumContainerManager(this, getDatabaseConnection());
  }

  /**
   * Returns the paintlet used for painting the containers.
   *
   * @return		the paintlet
   */
  @Override
  public SpectrumPaintlet getContainerPaintlet() {
    return m_SpectrumPaintlet;
  }

  /**
   * Returns whether the panel has a side panel. If that is the case, a
   * JSplitPanel is used.
   *
   * @return		true if a side panel is to be added
   */
  protected boolean hasSidePanel() {
    return true;
  }

  /**
   * Initializes the GUI.
   */
  @Override
  protected void initGUI() {
    Properties	props;
    JPanel	panel;

    super.initGUI();

    props = getProperties();

    m_ToolTipMaxColumns = props.getInteger("Plot.ToolTip.MaxColumns", 80);

    m_SpectrumContainerList = new SpectrumContainerList();
    m_SpectrumContainerList.setManager(getContainerManager());
    m_SpectrumContainerList.setAllowSearch(props.getBoolean("ContainerList.AllowSearch", false));
    m_SpectrumContainerList.setPopupMenuSupplier(this);
    m_SpectrumContainerList.setDisplayDatabaseID(true);
    m_SpectrumContainerList.addTableModelListener((TableModelEvent e) -> {
      final ContainerTable table = m_SpectrumContainerList.getTable();
      if ((table.getRowCount() > 0) && (table.getSelectedRowCount() == 0))
	SwingUtilities.invokeLater(() -> table.getSelectionModel().addSelectionInterval(0, 0));
    });

    m_SidePanel.setLayout(new BorderLayout(0, 0));
    m_SidePanel.add(m_SpectrumContainerList);

    panel = new JPanel();
    panel.setMinimumSize(new Dimension(1, props.getInteger("Axis.Bottom.Width", 0)));
    panel.setPreferredSize(new Dimension(1, props.getInteger("Axis.Bottom.Width", 0)));
    m_SidePanel.add(panel, BorderLayout.SOUTH);

    // paintlets
    m_SpectrumPaintlet = new SpectrumPaintlet();
    m_SpectrumPaintlet.setStrokeThickness(props.getDouble("Plot.StrokeThickness", 1.0).floatValue());
    m_SpectrumPaintlet.setPanel(this);
    m_SpectrumPaintlet.setAntiAliasingEnabled(props.getBoolean("Plot.AntiAliasing", true));

    m_SelectedWaveNumberPaintlet = new SelectedWaveNumberPaintlet();
    m_SelectedWaveNumberPaintlet.setPanel(this);

    m_CoordinatesPaintlet = new CoordinatesPaintlet();
    m_CoordinatesPaintlet.setYInvisible(true);
    m_CoordinatesPaintlet.setPanel(this);
    m_CoordinatesPaintlet.setXColor(props.getColor("Plot.CoordinatesColor." + Coordinates.X, Color.DARK_GRAY));
    m_CoordinatesPaintlet.setYColor(props.getColor("Plot.CoordinatesColor." + Coordinates.Y, Color.DARK_GRAY));

    m_SpectrumPointHitDetector = new SpectrumPointHitDetector(this);

    getPlot().addHitDetector(new WaveNumberHitDetector(this));
    getPlot().setPopupMenuCustomizer(this);
    getPlot().setTipTextCustomizer(this);

    m_PanelZoomOverview = new SpectrumZoomOverviewPanel();
    m_PlotWrapperPanel.add(m_PanelZoomOverview, BorderLayout.SOUTH);
    m_PanelZoomOverview.setDataContainerPanel(this);

    try {
      getContainerManager().setColorProvider(
	  (AbstractColorProvider) OptionUtils.forAnyCommandLine(
	      AbstractColorProvider.class,
	      props.getProperty("Plot.ColorProvider", DefaultColorProvider.class.getName())));
    }
    catch (Exception e) {
      System.err.println(getClass().getName() + " - Failed to set the color provider:");
      getContainerManager().setColorProvider(new DefaultColorProvider());
    }
  }

  /**
   * Finishes up the initialization.
   */
  @Override
  protected void finishInit() {
    super.finishInit();

    setAdjustToVisibleData(getProperties().getBoolean("Plot.AdjustToVisibleData", false));
  }

  /**
   * Returns the paintlet for painting the spectrum.
   *
   * @return		the paintlet
   */
  public SpectrumPaintlet getSpectrumPaintlet() {
    return m_SpectrumPaintlet;
  }

  /**
   * Returns the paintlet for painting the selected wave number.
   *
   * @return		the paintlet
   */
  public SelectedWaveNumberPaintlet getSelectedWaveNumberPaintlet() {
    return m_SelectedWaveNumberPaintlet;
  }

  /**
   * Sets whether the display is adjusted to only the visible data or
   * everything currently loaded.
   *
   * @param value	if true then plot is adjusted to visible data
   */
  public void setAdjustToVisibleData(boolean value) {
    m_AdjustToVisibleData = value;
    update();
  }

  /**
   * Returns whether the display is adjusted to only the visible spectrums
   * or all of them.
   *
   * @return		true if the plot is adjusted to only the visible data
   */
  public boolean getAdjustToVisibleData() {
    return m_AdjustToVisibleData;
  }

  /**
   * Sets the zoom overview panel visible or hides it.
   *
   * @param value	if true then the panel is displayed
   */
  public void setZoomOverviewPanelVisible(boolean value) {
    m_PanelZoomOverview.setVisible(value);
  }

  /**
   * Returns whether the zoom overview panel is visible or not.
   *
   * @return		true if visible
   */
  public boolean isZoomOverviewPanelVisible() {
    return m_PanelZoomOverview.isVisible();
  }

  /**
   * Returns the zoom overview panel.
   *
   * @return		the panel
   */
  public SpectrumZoomOverviewPanel getZoomOverviewPanel() {
    return m_PanelZoomOverview;
  }

  /**
   * Returns true if the paintlets can be executed.
   *
   * @param g		the graphics context
   * @return		true if painting can go ahead
   */
  @Override
  protected boolean canPaint(Graphics g) {
    return ((getPlot() != null) && (m_Manager != null));
  }

  /**
   * Updates the axes with the min/max of the new data.
   */
  @Override
  public void prepareUpdate() {
    List<SpectrumPoint>		points;
    SpectrumPoint		point;
    double 			minX;
    double 			maxX;
    double 			minY;
    double 			maxY;
    int				i;
    int				n;
    SpectrumContainerModel	model;

    minX  = Double.MAX_VALUE;
    maxX  = -Double.MAX_VALUE;
    minY  = Double.MAX_VALUE;
    maxY  = -Double.MAX_VALUE;
    model = (SpectrumContainerModel) getContainerList().getContainerModel();

    for (i = 0; i < model.getRowCount(); i++) {
      if (m_AdjustToVisibleData) {
	if (!model.getContainerAt(i).isVisible())
	  continue;
      }

      points = model.getContainerAt(i).getData().toList();

      if (points.size() == 0)
	continue;

      // determine min/max
      if (points.get(0).getWaveNumber() < minX)
	minX = points.get(0).getWaveNumber();
      if (points.get(points.size() - 1).getWaveNumber() > maxX)
	maxX = points.get(points.size() - 1).getWaveNumber();

      for (n = 0; n < points.size(); n++) {
	point = points.get(n);
	if (point.getAmplitude() > maxY)
	  maxY = point.getAmplitude();
	if (point.getAmplitude() < minY)
	  minY = point.getAmplitude();
      }
    }

    // center, if only 1 data point
    if (minX == maxX) {
      minX -= 1;
      maxX += 1;
    }

    // update axes
    getPlot().getAxis(Axis.LEFT).setMinimum(minY);
    getPlot().getAxis(Axis.LEFT).setMaximum(maxY);
    getPlot().getAxis(Axis.BOTTOM).setMinimum(minX);
    getPlot().getAxis(Axis.BOTTOM).setMaximum(maxX);
  }

  /**
   * Returns a popup menu for the table of the spectrum list.
   *
   * @param table	the affected table
   * @param row	the row the mouse is currently over
   * @return		the popup menu
   */
  @Override
  public BasePopupMenu getContainerListPopupMenu(final ContainerTable<SpectrumContainerManager,SpectrumContainer> table, final int row) {
    BasePopupMenu			result;
    JMenuItem				item;
    final int[] 			indices;
    final SpectrumContainerModel	model;
    final int				actRow;
    final List<SpectrumContainer> 	visibleConts;

    result = new BasePopupMenu();
    model  = (SpectrumContainerModel) getContainerList().getContainerModel();
    if (table.getSelectedRows().length == 0)
      indices = new int[]{row};
    else
      indices = table.getSelectedRows();
    for (int i = 0; i < indices.length; i++) {
      SpectrumContainer cont = model.getContainerAt(indices[i]);
      indices[i] = getContainerManager().indexOf(cont);
    }
    actRow = getContainerManager().indexOf(model.getContainerAt(row));

    visibleConts = new ArrayList<>();
    for (int i = 0; i < model.getRowCount(); i++) {
      if (model.getContainerAt(i).isVisible())
	visibleConts.add(model.getContainerAt(i));
    }

    item = new JMenuItem("Toggle visibility");
    item.addActionListener((ActionEvent e) -> {
      TIntArrayList visible = new TIntArrayList();
      TIntArrayList invisible = new TIntArrayList();
      for (int i = 0; i < model.getRowCount(); i++) {
	int index = getContainerManager().indexOf(model.getContainerAt(i));
	if (model.getContainerAt(i).isVisible())
	  invisible.add(index);
	else
	  visible.add(index);
      }
      Range range = new Range();
      range.setMax(getContainerManager().count());
      if (invisible.size() > 0) {
	range.setIndices(invisible.toArray());
	getScriptingEngine().add(
	  SpectrumPanel.this,
	  fixAction(Invisible.ACTION) + " " + range.getRange());
      }
      if (visible.size() > 0) {
	range.setIndices(visible.toArray());
	getScriptingEngine().add(
	  SpectrumPanel.this,
	  fixAction(Visible.ACTION) + " " + range.getRange());
      }
    });
    result.add(item);

    item = new JMenuItem("Show all");
    item.addActionListener((ActionEvent e) -> {
      TIntArrayList list = new TIntArrayList();
      for (int i = 0; i < model.getRowCount(); i++) {
	int index = getContainerManager().indexOf(model.getContainerAt(i));
	if (!model.getContainerAt(i).isVisible())
	  list.add(index);
      }
      if (list.size() > 0) {
	Range range = new Range();
	range.setMax(getContainerManager().count());
	range.setIndices(list.toArray());
	getScriptingEngine().add(
	  SpectrumPanel.this,
	  fixAction(Visible.ACTION) + " " + range.getRange());
      }
    });
    result.add(item);

    item = new JMenuItem("Hide all");
    item.addActionListener((ActionEvent e) -> {
      TIntArrayList list = new TIntArrayList();
      for (int i = 0; i < model.getRowCount(); i++) {
	int index = getContainerManager().indexOf(model.getContainerAt(i));
	if (model.getContainerAt(i).isVisible())
	  list.add(index);
      }
      if (list.size() > 0) {
	Range range = new Range();
	range.setMax(getContainerManager().count());
	range.setIndices(list.toArray());
	getScriptingEngine().add(
	  SpectrumPanel.this,
	  fixAction(Invisible.ACTION) + " " + range.getRange());
      }
    });
    result.add(item);

    item = new JMenuItem("Choose color...");
    item.addActionListener((ActionEvent e) -> {
      String msg = "Choose color";
      SpectrumContainer cont;
      Color color = Color.BLUE;
      if (indices.length == 1) {
	cont = getContainerManager().get(indices[0]);
	msg += " for " + cont.getData().getID();
	color = cont.getColor();
      }
      Color c = JColorChooser.showDialog(
	table,
	msg,
	color);
      if (c == null)
	return;
      for (int index : indices)
	getContainerManager().get(index).setColor(c);
    });
    result.add(item);

    if (getContainerManager().getAllowRemoval()) {
      result.addSeparator();

      item = new JMenuItem("Remove");
      item.addActionListener((ActionEvent e) -> m_SpectrumContainerList.getTable().removeContainers(indices));
      result.add(item);

      item = new JMenuItem("Remove all");
      item.addActionListener((ActionEvent e) -> m_SpectrumContainerList.getTable().removeAllContainers());
      result.add(item);
    }

    if (getContainerManager().isReloadable()) {
      result.addSeparator();

      item = new JMenuItem("Reload");
      item.addActionListener((ActionEvent e) -> {
	getScriptingEngine().add(
	  SpectrumPanel.this,
	  fixAction(SetData.ACTION) + " "
	    + (actRow + 1) + " "
	    + getContainerManager().get(actRow).getData().getDatabaseID());
      });
      result.add(item);

      item = new JMenuItem("Reload all");
      item.addActionListener((ActionEvent e) -> {
	for (int i = 0; i < model.getRowCount(); i++) {
	  SpectrumContainer cont = model.getContainerAt(i);
	  int actIndex = getContainerManager().indexOf(cont);
	  getScriptingEngine().add(
	    SpectrumPanel.this,
	    fixAction(SetData.ACTION) + " "
	      + (actIndex+1) + " "
	      + getContainerManager().get(actIndex).getData().getDatabaseID());
	}
      });
      result.add(item);
    }

    result.addSeparator();

    item = new JMenuItem("Copy ID" + (indices.length > 1 ? "s" : ""));
    item.setEnabled(indices.length > 0);
    item.addActionListener((ActionEvent e) -> {
      StringBuilder id = new StringBuilder();
      for (int i = 0; i < indices.length; i++) {
	if (id.length() > 0)
	  id.append("\n");
	id.append(getContainerManager().get(indices[i]).getDisplayID());
      }
      ClipboardHelper.copyToClipboard(id.toString());
    });
    result.add(item);

    item = new JMenuItem("Information");
    item.addActionListener((ActionEvent e) -> {
      List<InformativeStatistic> stats = new ArrayList<>();
      for (SpectrumContainer cont: visibleConts)
	stats.add(cont.getData().toStatistic());
      showStatistics(stats);
    });
    result.add(item);

    item = new JMenuItem("Spectral data");
    item.setEnabled(indices.length == 1);
    item.addActionListener((ActionEvent e) -> showSpectralData(getContainerManager().get(actRow)));
    result.add(item);

    item = new JMenuItem("Sample data");
    item.addActionListener((ActionEvent e) -> showSampleData(visibleConts));
    result.add(item);

    item = new JMenuItem("Notes");
    item.addActionListener((ActionEvent e) -> showNotes(visibleConts));
    result.add(item);

    return result;
  }

  /**
   * Optional customizing of the menu that is about to be popped up.
   *
   * @param e		the mous event
   * @param menu	the menu to customize
   */
  @Override
  public void customizePopupMenu(MouseEvent e, JPopupMenu menu) {
    JMenuItem				item;
    final SpectrumContainerModel	model;
    final List<SpectrumContainer> 	visibleConts;

    model        = (SpectrumContainerModel) getContainerList().getContainerModel();
    visibleConts = new ArrayList<>();
    for (int i = 0; i < model.getRowCount(); i++) {
      if (model.getContainerAt(i).isVisible())
	visibleConts.add(model.getContainerAt(i));
    }

    menu.addSeparator();

    item = new JMenuItem();
    item.setIcon(GUIHelper.getEmptyIcon());
    if (!getSpectrumPaintlet().isMarkersDisabled())
      item.setText("Disable markers");
    else
      item.setText("Enable markers");
    item.addActionListener((ActionEvent ae) -> {
      getSpectrumPaintlet().setMarkersDisabled(
        !getSpectrumPaintlet().isMarkersDisabled());
      repaint();
    });
    menu.add(item);

    item = new JMenuItem();
    item.setIcon(GUIHelper.getEmptyIcon());
    if (isSidePanelVisible())
      item.setText("Hide side panel");
    else
      item.setText("Show side panel");
    item.addActionListener((ActionEvent ae) -> setSidePanelVisible(!isSidePanelVisible()));
    menu.add(item);

    item = new JMenuItem();
    item.setIcon(GUIHelper.getEmptyIcon());
    if (getAdjustToVisibleData())
      item.setText("Adjust to loaded data");
    else
      item.setText("Adjust to visible data");
    item.addActionListener((ActionEvent ae) -> setAdjustToVisibleData(!getAdjustToVisibleData()));
    menu.add(item);

    menu.addSeparator();

    item = new JMenuItem("Spectrum statistics", GUIHelper.getIcon("statistics.png"));
    item.addActionListener((ActionEvent ae) -> {
      List<InformativeStatistic> stats = new ArrayList<>();
      for (int i = 0; i < model.getRowCount(); i++) {
        if (model.getContainerAt(i).isVisible())
          stats.add(model.getContainerAt(i).getData().toStatistic());
      }
      showStatistics(stats);
    });
    menu.add(item);

    item = new JMenuItem("Spectrum histogram", GUIHelper.getIcon("histogram.png"));
    item.addActionListener((ActionEvent ae) -> showHistogram(visibleConts));
    menu.add(item);

    item = new JMenuItem("Spectrum notes", GUIHelper.getEmptyIcon());
    item.addActionListener((ActionEvent ae) -> showNotes(visibleConts));
    menu.add(item);

    item = new JMenuItem("Goto wave number...", GUIHelper.getEmptyIcon());
    item.addActionListener((ActionEvent ae) -> selectWaveNumber());
    menu.add(item);

    menu.addSeparator();

    item = new JMenuItem("Save visible spectra...", GUIHelper.getIcon("save.gif"));
    item.addActionListener((ActionEvent ae) -> saveVisibleSpectra());
    menu.add(item);

    item = new JMenuItem("Export visible spectra...", GUIHelper.getIcon("arff.png"));
    item.addActionListener((ActionEvent ae) -> exportVisibleSpectra());
    menu.add(item);

    SendToActionUtils.addSendToSubmenu(this, menu);
  }

  /**
   * Saves the visible spectra to a directory.
   */
  protected void saveVisibleSpectra() {
    AbstractDataContainerWriter 	writer;
    String 				filename;
    int					i;
    SpectrumContainer 			cont;
    String[] 				ext;
    List<Spectrum> 			data;
    String 				prefix;
    SpectrumContainerModel		model;

    if (m_ExportDialog == null) {
      if (getParentDialog() != null)
	m_ExportDialog = new SpectrumExportDialog(getParentDialog(), ModalityType.DOCUMENT_MODAL);
      else
	m_ExportDialog = new SpectrumExportDialog(getParentFrame(), true);
    }
    m_ExportDialog.setLocationRelativeTo(this);
    m_ExportDialog.setVisible(true);
    if (m_ExportDialog.getOption() != SpectrumExportDialog.APPROVE_OPTION)
      return;

    model = (SpectrumContainerModel) getContainerList().getContainerModel();

    // write data
    writer = m_ExportDialog.getExport();
    if (writer instanceof MetaFileWriter)
      ext = ((MetaFileWriter) writer).getActualFormatExtensions();
    else
      ext = writer.getFormatExtensions();
    if (writer.canWriteMultiple() && m_ExportDialog.getCombine()) {
      filename = getContainerManager().getVisible(0).getDisplayID() + "_and_" + (getContainerManager().countVisible() - 1) + "_more";
      filename = FileUtils.createFilename(filename, "");
      filename = m_ExportDialog.getDirectory().getAbsolutePath() + File.separator + filename + "." + ext[0];
      writer.setOutput(new PlaceholderFile(filename));
      data = new ArrayList<>();
      for (i = 0; i < model.getRowCount(); i++) {
	if (!model.getContainerAt(i).isVisible())
	  continue;
	cont = model.getContainerAt(i);
	data.add(cont.getData());
      }
      if (!writer.write(data))
	GUIHelper.showErrorMessage(this, "Failed to write spectra to '" + filename + "'!");
    }
    else {
      prefix = m_ExportDialog.getDirectory().getAbsolutePath();
      for (i = 0; i < model.getRowCount(); i++) {
	if (!model.getContainerAt(i).isVisible())
	  continue;
	cont = model.getContainerAt(i);
	filename = prefix + File.separator + FileUtils.createFilename(cont.getDisplayID(), "") + "." + ext[0];
	writer.setOutput(new PlaceholderFile(filename));
	if (!writer.write(cont.getData())) {
	  GUIHelper.showErrorMessage(this, "Failed to write spectrum #" + (i+1) + " to '" + filename + "'!");
	  break;
	}
      }
    }
  }

  /**
   * Exports the visible spectra to an ARFF file.
   */
  protected void exportVisibleSpectra() {
    Instances				data;
    Instance 				inst;
    int					i;
    AbstractSpectrumInstanceGenerator	generator;
    SpectrumContainer 			cont;
    AbstractFileSaver			saver;
    File				file;
    SpectrumContainerModel		model;

    if (m_ExportDatasetDialog == null) {
      if (getParentDialog() != null)
	m_ExportDatasetDialog = new SpectrumDatasetExportDialog(getParentDialog(), ModalityType.DOCUMENT_MODAL);
      else
	m_ExportDatasetDialog = new SpectrumDatasetExportDialog(getParentFrame(), true);
    }
    m_ExportDatasetDialog.setLocationRelativeTo(this);
    m_ExportDatasetDialog.setVisible(true);
    if (m_ExportDatasetDialog.getOption() != SpectrumExportDialog.APPROVE_OPTION)
      return;

    model     = (SpectrumContainerModel) getContainerList().getContainerModel();
    generator = m_ExportDatasetDialog.getGenerator();
    data      = null;
    for (i = 0; i < model.getRowCount(); i++) {
      if (!model.getContainerAt(i).isVisible())
	continue;
      cont = model.getContainerAt(i);
      inst = generator.generate(cont.getData());
      if (data == null)
	data = new Instances(inst.dataset(), 0);
      data.add(inst);
    }

    file = m_ExportDatasetDialog.getFile();
    saver = m_ExportDatasetDialog.getExport();
    try {
      saver.setFile(file);
      DataSink.write(saver, data);
    }
    catch (Exception e) {
      GUIHelper.showErrorMessage(this, "Failed to export spectra to: " + file, e);
    }
  }

  /**
   * Processes the given tip text. Among the current mouse position, the
   * panel that initiated the call are also provided.
   *
   * @param panel	the content panel that initiated this call
   * @param mouse	the mouse position
   * @param tiptext	the tiptext so far
   * @return		the processed tiptext
   */
  @Override
  public String processTipText(PlotPanel panel, Point mouse, String tiptext) {
    String			result;
    MouseEvent			event;
    String			hit;

    result = tiptext;
    event  = new MouseEvent(
			getPlot().getContent(),
			MouseEvent.MOUSE_MOVED,
			new Date().getTime(),
			0,
			(int) mouse.getX(),
			(int) mouse.getY(),
			0,
			false);

    hit = (String) m_SpectrumPointHitDetector.detect(event);
    if (hit != null)
      result += hit;

    result = GUIHelper.processTipText(result, m_ToolTipMaxColumns);

    return result;
  }

  /**
   * Displays the notes for the given chromatograms.
   *
   * @param data	the chromatograms to display
   */
  protected void showNotes(List<SpectrumContainer> data) {
    NotesFactory.Dialog		dialog;

    if (getParentDialog() != null)
      dialog = NotesFactory.getDialog(getParentDialog(), ModalityType.MODELESS);
    else
      dialog = NotesFactory.getDialog(getParentFrame(), false);
    dialog.setData(data);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }

  /**
   * Displays a dialog with the given statistics.
   *
   * @param stats	the statistics to display
   */
  protected void showStatistics(List<InformativeStatistic> stats) {
    InformativeStatisticFactory.Dialog	dialog;

    if (getParentDialog() != null)
      dialog = InformativeStatisticFactory.getDialog(getParentDialog(), ModalityType.MODELESS);
    else
      dialog = InformativeStatisticFactory.getDialog(getParentFrame(), false);

    dialog.setStatistics(stats);
    dialog.setVisible(true);
  }

  /**
   * Displays the histograms for the given spectrums.
   *
   * @param data	the spectrums to display
   */
  protected void showHistogram(List<SpectrumContainer> data) {
    HistogramFactory.Dialog	dialog;
    int				i;
    Spectrum			sp;

    // get parameters for histograms
    if (m_HistogramSetup == null) {
      if (getParentDialog() != null)
	m_HistogramSetup = HistogramFactory.getSetupDialog(getParentDialog(), ModalityType.DOCUMENT_MODAL);
      else
	m_HistogramSetup = HistogramFactory.getSetupDialog(getParentFrame(), true);
    }
    m_HistogramSetup.setLocationRelativeTo(this);
    m_HistogramSetup.setVisible(true);
    if (m_HistogramSetup.getResult() != HistogramFactory.SetupDialog.APPROVE_OPTION)
      return;

    // generate histograms and display them
    if (getParentDialog() != null)
      dialog = HistogramFactory.getDialog(getParentDialog(), ModalityType.MODELESS);
    else
      dialog = HistogramFactory.getDialog(getParentFrame(), false);
    for (i = 0; i < data.size(); i++) {
      sp = data.get(i).getData();
      dialog.add((ArrayHistogram) m_HistogramSetup.getCurrent(), sp, data.get(i).getID());
    }
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }

  /**
   * Displays a dialog with the given spectral data.
   *
   * @param cont	the container to display the raw data for
   */
  protected void showSpectralData(SpectrumContainer cont) {
    SpreadSheetDialog	dialog;

    if (getParentDialog() != null)
      dialog = new SpreadSheetDialog(getParentDialog(), ModalityType.MODELESS);
    else
      dialog = new SpreadSheetDialog(getParentFrame(), false);
    dialog.setDefaultCloseOperation(SpreadSheetDialog.DISPOSE_ON_CLOSE);
    dialog.setTitle("Spectral data");
    dialog.setSize(
      GUIHelper.getInteger("DefaultSmallDialog.Width", 600),
      GUIHelper.getInteger("DefaultSmallDialog.Width", 600));
    dialog.setLocationRelativeTo(this);
    dialog.setSpreadSheet(cont.getData().toSpreadSheet());
    dialog.setNumDecimals(getProperties().getInteger("SpreadSheetPanel.NumDecimals", 3));
    dialog.setVisible(true);
  }

  /**
   * Displays a dialog with the given sample data.
   *
   * @param data	the spectrums to display the sample data for
   */
  protected void showSampleData(List<SpectrumContainer> data) {
    ReportFactory.Dialog	dialog;
    List<ReportContainer>	conts;
    ReportContainer		rc;

    if (getParentDialog() != null)
      dialog = SampleDataFactory.getDialog(getParentDialog(), ModalityType.MODELESS);
    else
      dialog = SampleDataFactory.getDialog(getParentFrame(), false);

    conts = new ArrayList<>();
    for (SpectrumContainer c: data) {
      if (c.getData().hasReport())
	rc = new ReportContainer(null, c.getData());
      else
	rc = new ReportContainer(null, new SampleData());
      conts.add(rc);
    }

    dialog.setData(conts);
    dialog.setReportContainerListWidth((int) getSidePanel().getPreferredSize().getWidth());
    dialog.setVisible(true);
  }

  /**
   * Selects the spectrum point based on the wave number.
   */
  protected void selectWaveNumber() {
    String 	retVal;
    float	value;

    retVal = GUIHelper.showInputDialog(this, "Please input a wave number");
    if (retVal == null)
      return;

    try {
      value = Float.parseFloat(retVal);
    }
    catch (Exception e) {
      e.printStackTrace();
      GUIHelper.showErrorMessage(
	  this, "Error parsing input:\n" + e);
      return;
    }

    getSelectedWaveNumberPaintlet().setPoint(new SpectrumPoint(value, 0));
  }

  /**
   * Allows changing the action, if required in derived classes.
   * <br><br>
   * The default implementation simply returns the input.
   *
   * @param action	the action to fix
   * @return		the fixed action
   */
  protected String fixAction(String action) {
    return action;
  }

  /**
   * Returns the container list.
   *
   * @return		the list
   */
  public SpectrumContainerList getContainerList() {
    return m_SpectrumContainerList;
  }

  /**
   * Returns the indices of the selected spectra.
   *
   * @return		the indices
   */
  public int[] getSelectedIndices() {
    return m_SpectrumContainerList.getTable().getSelectedRows();
  }

  /**
   * Returns the selected spectra.
   *
   * @return		the spectra
   */
  public Spectrum[] getSelectedSpectra() {
    Spectrum[]			result;
    int[]			indices;
    int				i;
    SpectrumContainerManager	manager;

    indices = getSelectedIndices();
    result  = new Spectrum[indices.length];
    manager = getContainerManager();

    for (i = 0; i < indices.length; i++)
      result[i] = manager.get(i).getData();

    return result;
  }

  /**
   * Sets whether to use active or store tables.
   *
   * @param value	if true then store tables are used
   */
  public void setUseStore(boolean value) {
    m_UseStore = value;
  }

  /**
   * Returns whether to use active or store tables.
   *
   * @return		true if store tables are used
   */
  public boolean getUseStore() {
    return m_UseStore;
  }

  /**
   * Returns the panel listing the spectrums.
   *
   * @return		the panel
   */
  public SpectrumContainerList getSpectrumContainerList() {
    return m_SpectrumContainerList;
  }

  /**
   * Sets whether to use anti-aliasing.
   *
   * @param value	if true then anti-aliasing is used
   */
  public void setAntiAliasingEnabled(boolean value) {
    m_SpectrumPaintlet.setAntiAliasingEnabled(value);
    m_PanelZoomOverview.getContainerPaintlet().setAntiAliasingEnabled(value);
  }

  /**
   * Returns whether anti-aliasing is used.
   *
   * @return		true if anti-aliasing is used
   */
  public boolean isAntiAliasingEnabled() {
    return m_SpectrumPaintlet.isAntiAliasingEnabled();
  }

  /**
   * A change in the database connection occurred.
   *
   * @param e		the event
   */
  @Override
  public void databaseConnectionStateChanged(DatabaseConnectionChangeEvent e) {
    super.databaseConnectionStateChanged(e);
    m_SelectedWaveNumberPaintlet.setPoint(null);
  }

  /**
   * Returns the current scripting engine, can be null.
   *
   * @return		the current engine
   */
  @Override
  public AbstractScriptingEngine getScriptingEngine() {
    return SpectralScriptingEngine.getSingleton(getDatabaseConnection());
  }

  /**
   * Hook method, called after the update was performed.
   */
  @Override
  protected void postUpdate() {
    super.postUpdate();

    if (m_PanelZoomOverview != null)
      m_PanelZoomOverview.update();
  }

  /**
   * Cleans up data structures, frees up memory.
   */
  @Override
  public void cleanUp() {
    m_SpectrumContainerList.cleanUp();

    if (m_SpectrumPointHitDetector != null) {
      m_SpectrumPointHitDetector.cleanUp();
      m_SpectrumPointHitDetector = null;
    }
    if (m_ExportDialog != null) {
      m_ExportDialog.dispose();
      m_ExportDialog = null;
    }
    if (m_ExportDatasetDialog != null) {
      m_ExportDatasetDialog.dispose();
      m_ExportDatasetDialog = null;
    }

    super.cleanUp();
  }
}
