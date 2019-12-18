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
 * Copyright (C) 2008-2019 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.visualization.spectrum;

import adams.core.Properties;
import adams.core.io.FileUtils;
import adams.core.io.PlaceholderFile;
import adams.core.option.OptionUtils;
import adams.data.instances.AbstractInstanceGenerator;
import adams.data.io.output.AbstractDataContainerWriter;
import adams.data.io.output.MetaFileWriter;
import adams.data.report.AbstractField;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.statistics.ArrayHistogram;
import adams.data.statistics.InformativeStatistic;
import adams.db.AbstractDatabaseConnection;
import adams.db.DatabaseConnection;
import adams.event.DatabaseConnectionChangeEvent;
import adams.gui.core.AntiAliasingSupporter;
import adams.gui.core.ColorHelper;
import adams.gui.core.GUIHelper;
import adams.gui.dialog.SpreadSheetDialog;
import adams.gui.event.PaintListener;
import adams.gui.scripting.AbstractScriptingEngine;
import adams.gui.scripting.SpectralScriptingEngine;
import adams.gui.visualization.container.ContainerTable;
import adams.gui.visualization.container.DataContainerPanelWithContainerList;
import adams.gui.visualization.core.ColorProvider;
import adams.gui.visualization.core.CoordinatesPaintlet;
import adams.gui.visualization.core.CoordinatesPaintlet.Coordinates;
import adams.gui.visualization.core.CrossHairTracker;
import adams.gui.visualization.core.DefaultColorProvider;
import adams.gui.visualization.core.Paintlet;
import adams.gui.visualization.core.PlotPanel;
import adams.gui.visualization.core.plot.Axis;
import adams.gui.visualization.core.plot.HitDetectorSupporter;
import adams.gui.visualization.core.plot.TipTextCustomizer;
import adams.gui.visualization.report.ReportContainer;
import adams.gui.visualization.report.ReportFactory;
import adams.gui.visualization.statistics.InformativeStatisticFactory;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.ConverterUtils.DataSink;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Special panel for displaying the spectral data.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumPanel
  extends DataContainerPanelWithContainerList<Spectrum, SpectrumContainerManager, SpectrumContainer>
  implements PaintListener, TipTextCustomizer, AntiAliasingSupporter, HitDetectorSupporter<SpectrumPointHitDetector> {

  /** for serialization. */
  private static final long serialVersionUID = -9059718549932104312L;

  /** whether to adjust to visible data or not. */
  protected boolean m_AdjustToVisibleData;

  /** paintlet for drawing the X-axis. */
  protected CoordinatesPaintlet m_CoordinatesPaintlet;

  /** the cross-hair paintlet. */
  protected CrossHairTracker m_CrossHairTracker;

  /** paintlet for drawing the spectrum. */
  protected AbstractSpectrumPaintlet m_SpectrumPaintlet;

  /** paintlet for drawing the selected wave number. */
  protected SelectedWaveNumberPaintlet m_SelectedWaveNumberPaintlet;

  /** for detecting hits. */
  protected SpectrumPointHitDetector m_SpectrumPointHitDetector;

  /** the dialog for the histogram setup. */
  protected HistogramFactory.SetupDialog m_HistogramSetup;

  /** the maximum number of columns for the tooltip. */
  protected int m_ToolTipMaxColumns;

  /** the maximum number of rows for the tooltip. */
  protected int m_ToolTipMaxRows;

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
  public AbstractSpectrumPaintlet getContainerPaintlet() {
    return m_SpectrumPaintlet;
  }

  /**
   * Returns the hit detector.
   *
   * @return		the hit detector
   */
  public SpectrumPointHitDetector getHitDetector() {
    return m_SpectrumPointHitDetector;
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
    m_ToolTipMaxRows = props.getInteger("Plot.ToolTip.MaxRows", 40);

    panel = new JPanel();
    panel.setMinimumSize(new Dimension(1, props.getInteger("Axis.Bottom.Width", 0)));
    panel.setPreferredSize(new Dimension(1, props.getInteger("Axis.Bottom.Width", 0)));
    m_SidePanel.add(panel, BorderLayout.SOUTH);

    // paintlets
    m_SpectrumPaintlet = new SpectrumPaintlet();
    m_SpectrumPaintlet.setStrokeThickness(props.getDouble("Plot.StrokeThickness", 1.0).floatValue());
    m_SpectrumPaintlet.setPanel(this);
    ((SpectrumPaintlet) m_SpectrumPaintlet).setAntiAliasingEnabled(props.getBoolean("Plot.AntiAliasing", true));

    m_SelectedWaveNumberPaintlet = new SelectedWaveNumberPaintlet();
    m_SelectedWaveNumberPaintlet.setPanel(this);

    m_CoordinatesPaintlet = new CoordinatesPaintlet();
    m_CoordinatesPaintlet.setYInvisible(true);
    m_CoordinatesPaintlet.setPanel(this);
    m_CoordinatesPaintlet.setXColor(props.getColor("Plot.CoordinatesColor." + Coordinates.X, Color.DARK_GRAY));
    m_CoordinatesPaintlet.setYColor(props.getColor("Plot.CoordinatesColor." + Coordinates.Y, Color.DARK_GRAY));

    m_CrossHairTracker = new CrossHairTracker();
    m_CrossHairTracker.setEnabled(props.getBoolean("Plot.CrossHairTracker.Enabled", false));
    m_CrossHairTracker.setTextCoordinates(props.getBoolean("Plot.CrossHairTracker.Text", false));
    m_CrossHairTracker.setColor(props.getColor("Plot.CrossHairTracker.Color", Color.BLACK));
    m_CrossHairTracker.setPanel(this);
    getPlot().addMouseMovementTracker(m_CrossHairTracker);

    m_SpectrumPointHitDetector = new SpectrumPointHitDetector(this);

    getPlot().addHitDetector(new WaveNumberHitDetector(this));
    getPlot().setPopupMenuCustomizer(this);
    getPlot().setTipTextCustomizer(this);

    m_PanelZoomOverview = new SpectrumZoomOverviewPanel();
    m_PlotWrapperPanel.add(m_PanelZoomOverview, BorderLayout.SOUTH);
    m_PanelZoomOverview.setDataContainerPanel(this);

    try {
      getContainerManager().setColorProvider(
	  (ColorProvider) OptionUtils.forAnyCommandLine(
	      ColorProvider.class,
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
   * Returns the container list.
   *
   * @return		the list
   */
  @Override
  protected SpectrumContainerList createContainerList() {
    SpectrumContainerList 	result;

    result = new SpectrumContainerList();
    result.setManager(getContainerManager());
    result.setAllowSearch(getProperties().getBoolean("ContainerList.AllowSearch", false));
    result.setPopupMenuSupplier(this);
    result.setDisplayDatabaseID(true);
    result.addTableModelListener((TableModelEvent e) -> {
      final ContainerTable table = result.getTable();
      if ((table.getRowCount() > 0) && (table.getSelectedRowCount() == 0))
	SwingUtilities.invokeLater(() -> table.getSelectionModel().addSelectionInterval(0, 0));
    });

    return result;
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
   * Returns the cross-hair tracker.
   *
   * @return		the tracker
   */
  public CrossHairTracker getCrossHairTracker() {
    return m_CrossHairTracker;
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
   * Returns the paintlet used for painting the data.
   *
   * @return		the paintlet
   */
  @Override
  public Paintlet getDataPaintlet() {
    return m_SpectrumPaintlet;
  }

  /**
   * Sets the paintlet to use for painting the data.
   *
   * @param value	the paintlet
   */
  public void setDataPaintlet(Paintlet value) {
    removePaintlet(m_SpectrumPaintlet);
    m_SpectrumPaintlet = (AbstractSpectrumPaintlet) value;
    m_SpectrumPaintlet.setPanel(this);
    addPaintlet(m_SpectrumPaintlet);
  }

  /**
   * Saves the visible spectra to a directory.
   */
  public void saveVisibleSpectra() {
    AbstractDataContainerWriter 	writer;
    String 				filename;
    String[] 				ext;
    List<Spectrum> 			data;
    String 				prefix;

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
      for (SpectrumContainer c: getTableModelContainers(true))
	data.add(c.getData());
      if (!writer.write(data))
	GUIHelper.showErrorMessage(this, "Failed to write spectra to '" + filename + "'!");
    }
    else {
      prefix = m_ExportDialog.getDirectory().getAbsolutePath();
      for (SpectrumContainer c: getTableModelContainers(true)) {
	filename = prefix + File.separator + FileUtils.createFilename(c.getDisplayID(), "") + "." + ext[0];
	writer.setOutput(new PlaceholderFile(filename));
	if (!writer.write(c.getData())) {
	  GUIHelper.showErrorMessage(this, "Failed to write spectrum #" + c + " to '" + filename + "'!");
	  break;
	}
      }
    }
  }

  /**
   * Exports the visible spectra to an ARFF file.
   */
  public void exportVisibleSpectra() {
    Instances			data;
    Instance 			inst;
    AbstractInstanceGenerator 	generator;
    AbstractFileSaver		saver;
    File			file;
    DataSink			sink;

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

    generator = m_ExportDatasetDialog.getGenerator();
    data      = null;
    for (SpectrumContainer cont: getTableModelContainers(true)) {
      inst = generator.generate(cont.getData());
      if (data == null)
	data = new Instances(inst.dataset(), 0);
      data.add(inst);
    }

    file = m_ExportDatasetDialog.getFile();
    saver = (AbstractFileSaver) OptionUtils.shallowCopy(m_ExportDatasetDialog.getExport());
    try {
      saver.setFile(file.getAbsoluteFile());
      sink = new DataSink(saver);
      sink.write(data);
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

    hit = m_SpectrumPointHitDetector.detect(event);
    if (hit != null)
      result += hit;

    result = GUIHelper.processTipText(result, m_ToolTipMaxColumns, m_ToolTipMaxRows);

    return result;
  }

  /**
   * Returns true if storing the color in the report of container's data object
   * is supported.
   *
   * @return		true if supported
   */
  @Override
  public boolean supportsStoreColorInReport() {
    return true;
  }

  /**
   * Stores the color of the container in the report of container's
   * data object.
   *
   * @param indices	the indices of the containers of the container manager
   * @param name	the field name to use
   */
  @Override
  public void storeColorInReport(int[] indices, String name) {
    Field 		field;
    SpectrumContainer	cont;

    field = new Field(name, DataType.STRING);
    for (int index: indices) {
      cont = getContainerManager().get(index);
      cont.getData().getReport().addField(field);
      cont.getData().getReport().setValue(field, ColorHelper.toHex(cont.getColor()));
    }
  }

  /**
   * Returns true if storing a value in the report of container's data object
   * is supported.
   *
   * @return		true if supported
   */
  public boolean supportsStoreValueInReport() {
    return true;
  }

  /**
   * Stores the value in the report of container's data object.
   *
   * @param indices	the indices of the containers of the container manager
   * @param field	the field to use
   * @param value	the value to store
   */
  public void storeValueInReport(int[] indices, AbstractField field, Object value) {
    SpectrumContainer cont;

    getContainerManager().startUpdate();
    for (int index: indices) {
      cont = getContainerManager().get(index);
      cont.getData().getReport().addField(field);
      cont.getData().getReport().setValue(field, value);
    }
    getContainerManager().finishUpdate();
  }

  /**
   * Displays a dialog with the given statistics.
   *
   * @param stats	the statistics to display
   */
  public void showStatistics(List<InformativeStatistic> stats) {
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
  public void showHistogram(List<SpectrumContainer> data) {
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
  public void showSpectralData(SpectrumContainer cont) {
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
  public void showSampleData(List<SpectrumContainer> data) {
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
  public void selectWaveNumber() {
    String 	retVal;
    float	value;

    retVal = GUIHelper.showInputDialog(this, "Please input a wave number");
    if (retVal == null) {
      getSelectedWaveNumberPaintlet().setEnabled(false);
      update();
      return;
    }

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
    getSelectedWaveNumberPaintlet().setEnabled(true);
    update();
  }

  /**
   * Returns the indices of the selected spectra.
   *
   * @return		the indices
   */
  public int[] getSelectedIndices() {
    return m_ContainerList.getTable().getSelectedRows();
  }

  /**
   * Returns the selected spectra.
   *
   * @return		the spectra
   */
  public Spectrum[] getSelectedSpectra() {
    List<Spectrum>		result;

    result = new ArrayList<>();
    for (int index: getSelectedIndices())
      result.add(getContainerManager().get(index).getData());

    return result.toArray(new Spectrum[result.size()]);
  }

  /**
   * Sets whether to use anti-aliasing.
   *
   * @param value	if true then anti-aliasing is used
   */
  public void setAntiAliasingEnabled(boolean value) {
    if (m_SpectrumPaintlet instanceof AntiAliasingSupporter)
      ((AntiAliasingSupporter) m_SpectrumPaintlet).setAntiAliasingEnabled(value);
    if (m_PanelZoomOverview.getContainerPaintlet() instanceof AntiAliasingSupporter)
      ((AntiAliasingSupporter) m_PanelZoomOverview.getContainerPaintlet()).setAntiAliasingEnabled(value);
  }

  /**
   * Returns whether anti-aliasing is used.
   *
   * @return		true if anti-aliasing is used
   */
  public boolean isAntiAliasingEnabled() {
    return (m_SpectrumPaintlet instanceof AntiAliasingSupporter)
      && ((AntiAliasingSupporter) m_SpectrumPaintlet).isAntiAliasingEnabled();
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
    m_ContainerList.cleanUp();

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
