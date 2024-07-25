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
 * SpectrumExplorer.java
 * Copyright (C) 2009-2022 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.visualization.spectrum;

import adams.core.CleanUpHandler;
import adams.core.ObjectCopyHelper;
import adams.core.StatusMessageHandler;
import adams.core.Utils;
import adams.core.io.PlaceholderFile;
import adams.core.option.OptionUtils;
import adams.data.filter.PassThrough;
import adams.data.io.input.AbstractDataContainerReader;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spectrum.SpectrumUtils;
import adams.data.spectrumanalysis.FastICA;
import adams.data.spectrumanalysis.PCA;
import adams.data.spectrumanalysis.PLS;
import adams.db.AbstractDatabaseConnection;
import adams.db.DatabaseConnection;
import adams.db.DatabaseConnectionHandler;
import adams.event.DatabaseConnectionChangeEvent;
import adams.event.DatabaseConnectionChangeEvent.EventType;
import adams.event.DatabaseConnectionChangeListener;
import adams.flow.control.Flow;
import adams.gui.application.ChildFrame;
import adams.gui.chooser.SpectrumFileChooser;
import adams.gui.core.AntiAliasingSupporter;
import adams.gui.core.BaseStatusBar;
import adams.gui.core.BaseTabbedPane;
import adams.gui.core.BaseTable;
import adams.gui.core.GUIHelper;
import adams.gui.core.ImageManager;
import adams.gui.core.MenuBarProvider;
import adams.gui.core.RecentFilesHandlerWithCommandline;
import adams.gui.core.RecentFilesHandlerWithCommandline.Setup;
import adams.gui.core.SearchPanel;
import adams.gui.core.SearchPanel.LayoutType;
import adams.gui.core.Undo.UndoPoint;
import adams.gui.core.UndoHandlerWithQuickAccess;
import adams.gui.core.UndoPanel;
import adams.gui.dialog.ApprovalDialog;
import adams.gui.event.DataChangeEvent;
import adams.gui.event.DataChangeListener;
import adams.gui.event.FilterEvent;
import adams.gui.event.FilterListener;
import adams.gui.event.RecentItemEvent;
import adams.gui.event.RecentItemListener;
import adams.gui.event.UndoEvent;
import adams.gui.goe.GenericObjectEditorDialog;
import adams.gui.menu.ConnectToDatabases;
import adams.gui.scripting.AbstractScriptingEngine;
import adams.gui.scripting.AddData;
import adams.gui.scripting.AddDataFile;
import adams.gui.scripting.AddDataFiles;
import adams.gui.scripting.ClearData;
import adams.gui.scripting.DisableUndo;
import adams.gui.scripting.EnableUndo;
import adams.gui.scripting.Filter;
import adams.gui.scripting.FilterOverlay;
import adams.gui.scripting.RunFlow;
import adams.gui.scripting.RunFlowOverlay;
import adams.gui.scripting.ScriptingDialog;
import adams.gui.scripting.ScriptingEngineHandler;
import adams.gui.selection.SelectSpectrumDialog;
import adams.gui.sendto.SendToActionSupporter;
import adams.gui.sendto.SendToActionUtils;
import adams.gui.visualization.container.ContainerListManager;
import adams.gui.visualization.container.ContainerTable;
import adams.gui.visualization.container.FilterDialog;
import adams.gui.visualization.core.ColorProvider;
import adams.gui.visualization.core.Paintlet;
import adams.gui.visualization.report.ReportContainer;
import adams.gui.visualization.stats.scatterplot.AbstractScatterPlotOverlay;
import adams.gui.visualization.stats.scatterplot.Coordinates;
import adams.gui.visualization.stats.scatterplot.ScatterPlot;
import adams.gui.visualization.stats.scatterplot.action.ViewDataClickAction;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static adams.gui.flow.FlowEditorPanel.getPropertiesEditor;

/**
 * A panel for exploring Spectrums, manipulating them with filters, etc.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2246 $
 */
public class SpectrumExplorer
  extends UndoPanel
  implements MenuBarProvider, StatusMessageHandler,
             ContainerListManager<SpectrumContainerManager>,
             DatabaseConnectionChangeListener, DatabaseConnectionHandler,
             DataChangeListener,
             ScriptingEngineHandler, CleanUpHandler,
             FilterListener<Spectrum>, SendToActionSupporter,
             UndoHandlerWithQuickAccess {

  /** for serialization. */
  private static final long serialVersionUID = 3953271131937711340L;

  /** the file to store the recent files in. */
  public final static String SESSION_FILE = "SpectrumExplorerSession.props";

  /** the panel for displaying. */
  protected SpectrumPanel m_PanelSpectrum;

  /** the status bar. */
  protected BaseStatusBar m_StatusBar;

  /** the menu bar, if used. */
  protected JMenuBar m_MenuBar;

  /** the clear data menu item. */
  protected JMenuItem m_MenuItemClearData;

  /** the toggle undo menu item. */
  protected JCheckBoxMenuItem m_MenuItemEnableUndo;

  /** the undo menu item. */
  protected JMenuItem m_MenuItemUndo;

  /** the redo menu item. */
  protected JMenuItem m_MenuItemRedo;

  /** the filter menu item. */
  protected JMenuItem m_MenuItemProcessFilter;

  /** the ica menu item. */
  protected JMenuItem m_MenuItemProcessICA;

  /** the pca menu item. */
  protected JMenuItem m_MenuItemProcessPCA;

  /** the pls menu item. */
  protected JMenuItem m_MenuItemProcessPLS;

  /** the menu item for scripts. */
  protected JMenu m_MenuScripts;

  /** the stop execution menu item. */
  protected JMenuItem m_MenuItemStopExecution;

  /** the start recording menu item. */
  protected JMenuItem m_MenuItemStartRecording;

  /** the stop recording menu item. */
  protected JMenuItem m_MenuItemStopRecording;

  /** the overlay flow output menu item. */
  protected JMenuItem m_MenuItemOverlayFlowOutput;

  /** the refresh scripts menu item. */
  protected JMenuItem m_MenuItemRefreshScripts;

  /** the menu item for view related stuff. */
  protected JMenu m_MenuView;

  /** the toggle selected GC point menu item. */
  protected JMenuItem m_MenuItemViewSelectedWaveNumber;

  /** the toggle zoom overview menu item. */
  protected JMenuItem m_MenuItemViewZoomOverview;

  /** the toggle anti-aliasing menu item. */
  protected JMenuItem m_MenuItemViewAntiAliasing;

  /** the color provider menu item. */
  protected JMenuItem m_MenuItemViewColorProvider;

  /** the paintlet  menu item. */
  protected JMenuItem m_MenuItemViewPaintlet;

  /** the menu item for window related stuff. */
  protected JMenu m_MenuWindow;

  /** the menuitem for creating a new window. */
  protected JMenuItem m_MenuItemWindowNew;

  /** the menuitem for duplicating theew window. */
  protected JMenuItem m_MenuItemWindowDuplicate;

  /** the current filter. */
  protected adams.data.filter.Filter<Spectrum> m_CurrentFilter;

  /** the current PCA analysis. */
  protected PCA m_CurrentPCA;

  /** the current ICA analysis. */
  protected FastICA m_CurrentICA;

  /** the current PLS analysis. */
  protected PLS m_CurrentPLS;

  /** indicates whether the filtered data was overlayed over the original. */
  protected boolean m_FilterOverlayOriginalData;

  /** the filter dialog. */
  protected FilterDialog m_DialogFilter;

  /** the PCA dialog. */
  protected GenericObjectEditorDialog m_DialogPCA;

  /** the ICA dialog. */
  protected GenericObjectEditorDialog m_DialogICA;

  /** the PLS dialog. */
  protected GenericObjectEditorDialog m_DialogPLS;

  /** the dialog for loading data. */
  protected SelectSpectrumDialog m_LoadDialog;

  /** the file chooser for importing data. */
  protected SpectrumFileChooser m_SpectrumFileChooser;

  /** the dialog for managing scripts. */
  protected ScriptingDialog m_ScriptingDialog;

  /** the tabbed pane for the data to display. */
  protected BaseTabbedPane m_TabbedPane;

  /** the sample data reports. */
  protected SampleDataFactory.Panel m_Reports;

  /** for searching the fields in the reports. */
  protected SearchPanel m_SearchPanel;

  /** the database connection. */
  protected AbstractDatabaseConnection m_DatabaseConnection;
  
  /** the dialog for selecting the color provider. */
  protected GenericObjectEditorDialog m_DialogColorProvider;

  /** the dialog for selecting the paintlet. */
  protected GenericObjectEditorDialog m_DialogPaintlet;

  /** the recent files handler. */
  protected RecentFilesHandlerWithCommandline<JMenu> m_RecentFilesHandler;

  /**
   * default constructor.
   */
  public SpectrumExplorer() {
    super(Spectrum.class, true);
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_LoadDialog          = null;
    m_ScriptingDialog     = null;
    m_DialogColorProvider = null;
    m_DialogPaintlet      = null;
    m_SpectrumFileChooser = new SpectrumFileChooser();
    m_SpectrumFileChooser.setMultiSelectionEnabled(true);
    m_CurrentFilter       = new PassThrough();
    m_CurrentICA          = new FastICA();
    m_CurrentPCA          = new PCA();
    m_CurrentPLS          = new PLS();
    m_DialogICA           = null;
    m_DialogPCA           = null;
    m_DialogPLS           = null;
    m_RecentFilesHandler  = null;
    m_DatabaseConnection  = DatabaseConnection.getSingleton();
    m_DatabaseConnection.addChangeListener(this);
  }

  /**
   * Initializes the widgets.
   */
  @Override
  protected void initGUI() {
    JPanel	panel;
    JPanel	panelData;
    JPanel	panelReport;

    super.initGUI();

    setLayout(new BorderLayout());

    m_TabbedPane = new BaseTabbedPane();
    add(m_TabbedPane, BorderLayout.CENTER);

    // 1. page: graph
    panelData = new JPanel(new BorderLayout());
    m_TabbedPane.addTab("Data", panelData);
    m_TabbedPane.addChangeListener(e -> {
      ContainerTable dtable = getSpectrumPanel().getContainerList().getTable();
      // data
      if (m_TabbedPane.getSelectedIndex() == 0) {
	BaseTable rtable = m_Reports.getReportContainerList().getTable();
	if ((rtable == null) || (rtable.getSelectedRowCount() != 1))
	  return;
	int row = rtable.getSelectedRow();
	dtable.getSelectionModel().clearSelection();
	dtable.getSelectionModel().setSelectionInterval(row, row);
      }
      // reports
      else if (m_TabbedPane.getSelectedIndex() == 1) {
	if (dtable.getSelectedRowCount() != 1)
	  return;
	m_Reports.setCurrentTable(dtable.getSelectedRow());
      }
    });

    // the spectrums
    m_PanelSpectrum = new SpectrumPanel();
    m_PanelSpectrum.setDatabaseConnection(getDatabaseConnection());
    m_PanelSpectrum.getContainerManager().addDataChangeListener(this);
    m_PanelSpectrum.setStatusMessageHandler(this);
    panelData.add(m_PanelSpectrum, BorderLayout.CENTER);

    // 2. page: sample data
    panelReport = new JPanel(new BorderLayout());
    m_TabbedPane.addTab("Sample data", panelReport);
    m_Reports = SampleDataFactory.getPanel((List<ReportContainer>) null);
    m_Reports.setDataContainerPanel(m_PanelSpectrum);
    panelReport.add(m_Reports, BorderLayout.CENTER);

    m_SearchPanel = new SearchPanel(LayoutType.HORIZONTAL, true);
    m_SearchPanel.addSearchListener(e -> {
      m_Reports.search(
	m_SearchPanel.getSearchText(), m_SearchPanel.isRegularExpression());
      m_SearchPanel.grabFocus();
    });
    panel = new JPanel(new BorderLayout());
    panel.add(m_SearchPanel, BorderLayout.WEST);
    panelReport.add(panel, BorderLayout.SOUTH);

    // the status bar
    m_StatusBar = new BaseStatusBar();
    add(m_StatusBar, BorderLayout.SOUTH);

    // if the plot is focussed, display selected point
    getSpectrumPanel().getPlot().getContent().addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
	SpectrumPoint point = getSpectrumPanel().getSelectedWaveNumberPaintlet().getPoint();
	// no point selected? -> select one in the middle
	if ((point == null) && (getContainerManager().countVisible() > 0)) {
	  for (int i = 0; i < getContainerManager().count(); i++) {
	    if (getContainerManager().isVisible(i)) {
	      List<SpectrumPoint> points = getContainerManager().get(i).getData().toList();
	      if (points.size() > 0) {
		point = points.get(points.size() / 2);
		getSpectrumPanel().getSelectedWaveNumberPaintlet().setPoint(point);
		break;
	      }
	    }
	  }
	}
      }
    });

    // add KeyListener for moving the selected SpectrumPoint around
    getSpectrumPanel().getPlot().getContent().addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
	int movement = 0;
	SpectrumPoint point = getSpectrumPanel().getSelectedWaveNumberPaintlet().getPoint();

	// determine direction and increment of movement
	if (point != null) {
	  if (e.getKeyCode() == KeyEvent.VK_LEFT) {
	    if (!e.isAltDown() && !e.isControlDown()) {
	      if (e.isShiftDown())
		movement = -10;
	      else
		movement = -1;
	      e.consume();
	    }
	  }
	  else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
	    if (!e.isAltDown() && !e.isControlDown()) {
	      if (e.isShiftDown())
		movement = 10;
	      else
		movement = 1;
	      e.consume();
	    }
	  }
	}

	// move selected point
	if (movement != 0) {
	  Spectrum data = (Spectrum) point.getParent();
	  if (data != null) {
	    int index = SpectrumUtils.findWaveNumber(data.toList(), point);
	    index += movement;
	    if (index < 0)
	      index = 0;
	    if (index >= data.size())
	      index = data.size() - 1;
	    // set new points
	    SpectrumPoint newPoint = data.toList().get(index);
	    getSpectrumPanel().getSelectedWaveNumberPaintlet().setPoint(newPoint);
	  }
	}

	if (!e.isConsumed())
	  super.keyPressed(e);
      }
    });

    // disable selection of wave numbers by default (use menu to enable again)
    getSpectrumPanel().getSelectedWaveNumberPaintlet().setEnabled(false);
  }

  /**
   * Returns the panel for painting the spectrums.
   *
   * @return		the panel
   */
  public SpectrumPanel getSpectrumPanel() {
    return m_PanelSpectrum;
  }

  /**
   * Displays a message.
   *
   * @param msg		the message to display
   */
  @Override
  public void showStatus(String msg) {
    m_StatusBar.showStatus(msg);
  }

  /**
   * Gets called if the data of the spectrum panel has changed.
   *
   * @param e		the event that the spectrum panel sent
   */
  @Override
  public void dataChanged(DataChangeEvent e) {
    updateMenu();
  }

  /**
   * Returns the current scripting engine, can be null.
   *
   * @return		the current engine
   */
  @Override
  public AbstractScriptingEngine getScriptingEngine() {
    return m_PanelSpectrum.getScriptingEngine();
  }

  /**
   * A change in the database connection occurred. Clears the undo cache.
   *
   * @param e		the event
   */
  @Override
  public void databaseConnectionStateChanged(DatabaseConnectionChangeEvent e) {
    m_Undo.clear();
    if (m_LoadDialog != null)
      m_LoadDialog.setItems(new Integer[0]);
    if (e.getType() == EventType.CONNECT)
      setDatabaseConnection(e.getDatabaseConnection());
  }

  /**
   * An undo event occurred.
   *
   * @param e		the event
   */
  @Override
  public void undoOccurred(UndoEvent e) {
    updateMenu();
  }

  /**
   * Returns the current container manager.
   *
   * @return		the manager
   */
  @Override
  public SpectrumContainerManager getContainerManager() {
    return m_PanelSpectrum.getContainerManager();
  }

  /**
   * Sets the manager for handling the containers.
   *
   * @param value	the manager
   */
  @Override
  public void setContainerManager(SpectrumContainerManager value) {
    m_PanelSpectrum.setContainerManager(value);
  }

  /**
   * updates the enabled state of the menu items.
   */
  protected void updateMenu() {
    boolean	dataLoaded;

    if (m_MenuBar == null)
      return;

    dataLoaded = (getContainerManager().count() > 0);

    m_MenuItemClearData.setEnabled(dataLoaded);

    m_MenuItemEnableUndo.setSelected(m_Undo.isEnabled());
    m_MenuItemUndo.setEnabled(m_Undo.canUndo());
    if (m_Undo.canUndo()) {
      m_MenuItemUndo.setText("Undo - " + m_Undo.peekUndoComment());
      m_MenuItemUndo.setToolTipText(m_Undo.peekUndoComment());
    }
    else {
      m_MenuItemUndo.setText("Undo");
      m_MenuItemUndo.setToolTipText(null);
    }
    m_MenuItemRedo.setEnabled(m_Undo.canRedo());
    if (m_Undo.canRedo()) {
      m_MenuItemRedo.setText("Redo - " + m_Undo.peekRedoComment());
      m_MenuItemRedo.setToolTipText(m_Undo.peekRedoComment());
    }
    else {
      m_MenuItemRedo.setText("Redo");
      m_MenuItemRedo.setToolTipText(null);
    }
    m_MenuItemProcessFilter.setEnabled(dataLoaded);
    m_MenuItemProcessICA.setEnabled(dataLoaded);
    m_MenuItemProcessPCA.setEnabled(dataLoaded);
    m_MenuItemProcessPLS.setEnabled(dataLoaded);

    m_MenuItemStopExecution.setEnabled(getScriptingEngine().isProcessing());
    m_MenuItemStartRecording.setEnabled(!getScriptingEngine().isRecording());
    m_MenuItemStopRecording.setEnabled(getScriptingEngine().isRecording());
    m_MenuItemViewAntiAliasing.setEnabled(getSpectrumPanel().getDataPaintlet() instanceof AntiAliasingSupporter);
    m_MenuItemViewAntiAliasing.setSelected(getSpectrumPanel().isAntiAliasingEnabled());
  }

  /**
   * re-builds the "Scripts" menu.
   */
  public void refreshScripts() {
    JMenuItem		menuitem;
    List<String>	scripts;
    int			i;
    String		name;

    scripts = getScriptingEngine().getAvailableScripts();

    // remove currently listed scripts
    i = 0;
    while (i < m_MenuScripts.getItemCount()) {
      if (m_MenuScripts.getItem(i) != m_MenuItemRefreshScripts) {
	i++;
      }
      else {
	i++;
	while (i < m_MenuScripts.getItemCount())
	  m_MenuScripts.remove(i);
      }
    }
    m_MenuScripts.addSeparator();

    // add scripts
    if (scripts.size() > 0) {
      for (i = 0; i < scripts.size(); i++) {
	final File file = new File(scripts.get(i));
	name = file.getName().replaceAll("_", " ");
	final boolean isFlow = name.endsWith("." + Flow.FILE_EXTENSION);
	if (isFlow)
	  name = name.replaceAll("\\." + Flow.FILE_EXTENSION + "$", "") + " [Flow]";
	menuitem = new JMenuItem(name);
	m_MenuScripts.add(menuitem);
        menuitem.addActionListener(e -> {
          getScriptingEngine().clear();
          if (isFlow) {
            if (m_MenuItemOverlayFlowOutput.isSelected())
              getScriptingEngine().add(this, RunFlowOverlay.ACTION + " " + file.getAbsolutePath());
            else
              getScriptingEngine().add(this, RunFlow.ACTION + " " + file.getAbsolutePath());
          }
          else {
            getScriptingEngine().add(this, file);
          }
	});
      }
    }
    else {
      menuitem = new JMenuItem("no scripts available");
      menuitem.setEnabled(false);
      m_MenuScripts.add(menuitem);
    }
  }

  /**
   * Executes a script.
   */
  public void manageScripts() {
    if (m_ScriptingDialog == null) {
      if (getParentDialog() != null)
	m_ScriptingDialog = new ScriptingDialog(getParentDialog(), this);
      else
	m_ScriptingDialog = new ScriptingDialog(getParentFrame(), this);

      m_ScriptingDialog.setLocationRelativeTo(this);
      m_ScriptingDialog.setDatabaseConnection(getDatabaseConnection());
    }

    m_ScriptingDialog.setVisible(true);
  }

  /**
   * Starts the recording of commands.
   */
  public void startRecording() {
    if (!getScriptingEngine().isRecording())
      getScriptingEngine().startRecording();
    updateMenu();
  }

  /**
   * Stops the recording of commands.
   */
  public void stopRecording() {
    if (getScriptingEngine().isRecording())
      getScriptingEngine().stopRecording();
    updateMenu();
  }

  /**
   * Creates a menu bar (singleton per panel object). Can be used in frames.
   *
   * @return		the menu bar
   */
  @Override
  public JMenuBar getMenuBar() {
    JMenuBar		result;
    JMenu		menu;
    JMenuItem		menuitem;
    JMenu		submenu;

    if (m_MenuBar == null) {
      result = new JMenuBar();

      // File
      menu = new JMenu("File");
      result.add(menu);
      menu.setMnemonic('F');
      menu.addChangeListener(e -> updateMenu());

      // File/Clear
      menuitem = new JMenuItem("Clear");
      menu.add(menuitem);
      menuitem.setMnemonic('C');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed N"));
      menuitem.setIcon(ImageManager.getIcon("new.gif"));
      menuitem.addActionListener(e -> clearData());
      m_MenuItemClearData = menuitem;

      menu.addSeparator();

      // File/Database
      menuitem = new JMenuItem("Database...");
      menu.add(menuitem);
      menuitem.setMnemonic('D');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed D"));
      menuitem.setIcon(ImageManager.getIcon("database.gif"));
      menuitem.addActionListener(e ->loadData());

      // File/Open
      menuitem = new JMenuItem("Open...");
      menu.add(menuitem);
      menuitem.setMnemonic('O');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed O"));
      menuitem.setIcon(ImageManager.getIcon("open.gif"));
      menuitem.addActionListener(e -> loadDataFromDisk());

      // File/Recent files
      submenu = new JMenu("Open recent");
      menu.add(submenu);
      m_RecentFilesHandler = new RecentFilesHandlerWithCommandline<>(
	  SESSION_FILE, getPropertiesEditor().getInteger("MaxRecentFlows", 5), submenu);
      m_RecentFilesHandler.addRecentItemListener(new RecentItemListener<JMenu,Setup>() {
	@Override
	public void recentItemAdded(RecentItemEvent<JMenu, Setup> e) {
	  // ignored
	}
	@Override
	public void recentItemSelected(RecentItemEvent<JMenu, Setup> e) {
	  AbstractDataContainerReader reader = (AbstractDataContainerReader) e.getItem().getHandler();
	  reader.setInput(new PlaceholderFile(e.getItem().getFile()));
	  getScriptingEngine().setDatabaseConnection(getDatabaseConnection());
	  getScriptingEngine().add(SpectrumExplorer.this, AddDataFile.ACTION + " " + OptionUtils.getCommandLine(reader));
	}
      });

      // File/Send to
      menu.addSeparator();
      if (SendToActionUtils.addSendToSubmenu(this, menu))
	menu.addSeparator();

      // File/Close
      menuitem = new JMenuItem("Close");
      menu.add(menuitem);
      menuitem.setMnemonic('C');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed Q"));
      menuitem.setIcon(ImageManager.getIcon("exit.png"));
      menuitem.addActionListener(e -> close());

      // Edit
      menu = new JMenu("Edit");
      result.add(menu);
      menu.setMnemonic('E');
      menu.addChangeListener(e -> updateMenu());

      // Edit/Enable Undo
      menuitem = new JCheckBoxMenuItem("Undo enabled");
      menu.add(menuitem);
      menuitem.setMnemonic('n');
      menuitem.setSelected(m_Undo.isEnabled());
      menuitem.addActionListener(e -> {
        if (m_MenuItemEnableUndo.isSelected())
          getScriptingEngine().add(this, EnableUndo.ACTION);
        else
          getScriptingEngine().add(this, DisableUndo.ACTION);
      });
      m_MenuItemEnableUndo = (JCheckBoxMenuItem) menuitem;

      // Edit/Undo
      menuitem = new JMenuItem("Undo");
      menu.add(menuitem);
      menuitem.setMnemonic('U');
      menuitem.setEnabled(m_Undo.canUndo());
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed Z"));
      menuitem.setIcon(ImageManager.getIcon("undo.gif"));
      menuitem.addActionListener(e ->undo());
      m_MenuItemUndo = menuitem;

      menuitem = new JMenuItem("Redo");
      menu.add(menuitem);
      menuitem.setMnemonic('R');
      menuitem.setEnabled(m_Undo.canUndo());
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed Y"));
      menuitem.setIcon(ImageManager.getIcon("redo.gif"));
      menuitem.addActionListener(e ->redo());
      m_MenuItemRedo = menuitem;

      // Process
      menu = new JMenu("Process");
      result.add(menu);
      menu.setMnemonic('P');
      menu.addChangeListener(e -> updateMenu());

      // Process/Filter
      menuitem = new JMenuItem("Filter...");
      menu.add(menuitem);
      menuitem.setMnemonic('F');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed F"));
      menuitem.setIcon(ImageManager.getIcon("run.gif"));
      menuitem.addActionListener(e -> filter());
      m_MenuItemProcessFilter = menuitem;

      // Process/ICA
      menuitem = new JMenuItem("ICA...");
      menu.add(menuitem);
      menuitem.setMnemonic('I');
      menuitem.setIcon(ImageManager.getIcon("scatterplot.gif"));
      menuitem.addActionListener(e -> ica());
      m_MenuItemProcessICA = menuitem;

      // Process/PCA
      menuitem = new JMenuItem("PCA...");
      menu.add(menuitem);
      menuitem.setMnemonic('P');
      menuitem.setIcon(ImageManager.getIcon("scatterplot.gif"));
      menuitem.addActionListener(e -> pca());
      m_MenuItemProcessPCA = menuitem;

      // Process/PLS
      menuitem = new JMenuItem("PLS...");
      menu.add(menuitem);
      menuitem.setMnemonic('L');
      menuitem.setIcon(ImageManager.getIcon("scatterplot.gif"));
      menuitem.addActionListener(e -> pls());
      m_MenuItemProcessPLS = menuitem;

      // Scripts
      menu = new JMenu("Scripts");
      result.add(menu);
      menu.setMnemonic('S');
      menu.addChangeListener(e -> updateMenu());
      m_MenuScripts = menu;

      // Scripts/Stop execution
      menuitem = new JMenuItem("Stop execution");
      menu.add(menuitem);
      menuitem.setMnemonic('x');
      menuitem.setIcon(ImageManager.getIcon("stop.gif"));
      menuitem.addActionListener(e -> getScriptingEngine().stop());
      m_MenuItemStopExecution = menuitem;

      menu.addSeparator();

      // Scripts/Manage scripts
      menuitem = new JMenuItem("Manage...");
      menu.add(menuitem);
      menuitem.setMnemonic('m');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed M"));
      menuitem.setIcon(ImageManager.getEmptyIcon());
      menuitem.addActionListener(e -> manageScripts());

      // Scripts/Start recording
      menuitem = new JMenuItem("Start recording");
      menu.add(menuitem);
      menuitem.setMnemonic('S');
      menuitem.setIcon(ImageManager.getEmptyIcon());
      menuitem.addActionListener(e -> startRecording());
      m_MenuItemStartRecording = menuitem;

      // Scripts/Start recording
      menuitem = new JMenuItem("Stop recording");
      menu.add(menuitem);
      menuitem.setMnemonic('t');
      menuitem.setIcon(ImageManager.getEmptyIcon());
      menuitem.addActionListener(e -> stopRecording());
      m_MenuItemStopRecording = menuitem;

      // Scripts/Overlay flow output
      menuitem = new JCheckBoxMenuItem("Overlay flow output");
      menu.add(menuitem);
      menuitem.setMnemonic('o');
      menuitem.setSelected(true);
      m_MenuItemOverlayFlowOutput = menuitem;

      // Scripts/Refresh
      menuitem = new JMenuItem("Refresh");
      menu.add(menuitem);
      menuitem.setMnemonic('R');
      menuitem.addActionListener(e -> refreshScripts());
      m_MenuItemRefreshScripts = menuitem;

      // View
      menu = new JMenu("View");
      result.add(menu);
      menu.setMnemonic('V');
      menu.addChangeListener(e -> updateMenu());
      m_MenuView = menu;

      // View/Display selected wave number
      menuitem = new JCheckBoxMenuItem("Display selected wave number");
      menu.add(menuitem);
      menuitem.setMnemonic('G');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl shift pressed W"));
      menuitem.setSelected(getSpectrumPanel().getSelectedWaveNumberPaintlet().isEnabled());
      menuitem.addActionListener(e -> {
        getSpectrumPanel().getSelectedWaveNumberPaintlet().setEnabled(m_MenuItemViewSelectedWaveNumber.isSelected());
        getSpectrumPanel().update();
      });
      m_MenuItemViewSelectedWaveNumber = menuitem;

      // View/Display zoom overview
      menuitem = new JCheckBoxMenuItem("Display zoom overview");
      menu.add(menuitem);
      menuitem.setMnemonic('Z');
      menuitem.setSelected(isZoomOverviewPanelVisible());
      menuitem.addActionListener(e -> setZoomOverviewPanelVisible(m_MenuItemViewZoomOverview.isSelected()));
      m_MenuItemViewZoomOverview = menuitem;

      // View/Anti-aliasing
      menuitem = new JCheckBoxMenuItem("Anti-aliasing");
      menu.add(menuitem);
      menuitem.setMnemonic('A');
      menuitem.setSelected(getSpectrumPanel().isAntiAliasingEnabled());
      menuitem.addActionListener(e -> getSpectrumPanel().setAntiAliasingEnabled(m_MenuItemViewAntiAliasing.isSelected()));
      m_MenuItemViewAntiAliasing = menuitem;

      // View/Color provider
      menuitem = new JMenuItem("Color provider...");
      menu.add(menuitem);
      menuitem.setMnemonic('P');
      menuitem.addActionListener(e -> selectColorProvider());
      m_MenuItemViewColorProvider = menuitem;

      // View/Paintlet
      menuitem = new JMenuItem("Paintlet...");
      menu.add(menuitem);
      menuitem.setMnemonic('P');
      menuitem.addActionListener((ActionEvent e) -> selectPaintlet());
      m_MenuItemViewPaintlet = menuitem;

      // Window
      menu = new JMenu("Window");
      result.add(menu);
      menu.setMnemonic('W');
      menu.addChangeListener(e -> updateMenu());
      m_MenuWindow = menu;

      // Window/New window
      menuitem = new JMenuItem("New window");
      menu.add(menuitem);
      menuitem.setIcon(ImageManager.getIcon("new.gif"));
      menuitem.setMnemonic('N');
      menuitem.addActionListener(e -> newWindow(true));
      m_MenuItemWindowNew = menuitem;

      // Window/Duplicate window
      menuitem = new JMenuItem("Duplicate window");
      menu.add(menuitem);
      menuitem.setIcon(ImageManager.getIcon("copy.gif"));
      menuitem.setMnemonic('D');
      menuitem.addActionListener(e -> duplicateWindow(true));
      m_MenuItemWindowDuplicate = menuitem;

      // update menu
      m_MenuBar = result;
      refreshScripts();
      updateMenu();
    }
    else {
      result = m_MenuBar;
    }

    return result;
  }

  /**
   * Removes all the data.
   */
  public void clearData() {
    getScriptingEngine().setDatabaseConnection(getDatabaseConnection());
    getScriptingEngine().add(this, ClearData.ACTION);
  }

  /**
   * pops up the dialog for loading the data from a database.
   */
  public void loadData() {
    if (!getDatabaseConnection().isConnected()) {
      GUIHelper.showErrorMessage(this, "No active database connection available!");
      GUIHelper.launchMenuItem(this, ConnectToDatabases.class);
      return;
    }
    
    if (m_LoadDialog == null) {
      if (getParentDialog() != null)
	m_LoadDialog = new SelectSpectrumDialog(getParentDialog());
      else
	m_LoadDialog = new SelectSpectrumDialog(getParentFrame());
      m_LoadDialog.setDatabaseConnection(getDatabaseConnection());
    }

    m_LoadDialog.setTitle("Load data");
    m_LoadDialog.setMultipleSelection(true);
    m_LoadDialog.setLocationRelativeTo(this);
    m_LoadDialog.setVisible(true);

    if (m_LoadDialog.getOption() == SelectSpectrumDialog.APPROVE_OPTION) {
      if (m_LoadDialog.getItems().length > 0)
	getScriptingEngine().add(this, AddData.ACTION + " " + Utils.arrayToString(m_LoadDialog.getItems()));
    }
  }

  /**
   * pops up file chooser dialog for spectrum readers.
   */
  public void loadDataFromDisk() {
    int				retVal;
    int				i;
    PlaceholderFile[]		files;
    List<String>		opts;
    AbstractDataContainerReader	reader;

    retVal = m_SpectrumFileChooser.showOpenDialog(this);
    if (retVal != SpectrumFileChooser.APPROVE_OPTION)
      return;

    files  = m_SpectrumFileChooser.getSelectedPlaceholderFiles();
    reader = m_SpectrumFileChooser.getReader();
    if (files.length == 1) {
      reader.setInput(files[0]);
      getScriptingEngine().setDatabaseConnection(getDatabaseConnection());
      getScriptingEngine().add(this,AddDataFile.ACTION + " " + OptionUtils.getCommandLine(reader));
      if (m_RecentFilesHandler != null)
	m_RecentFilesHandler.addRecentItem(new Setup(files[0], reader));
    }
    else {
      opts = new ArrayList<>();
      opts.add(OptionUtils.getCommandLine(reader));
      for (i = 0; i < files.length; i++)
	opts.add(files[i].toString());
      getScriptingEngine().setDatabaseConnection(getDatabaseConnection());
      getScriptingEngine().add(this, AddDataFiles.ACTION + " " + OptionUtils.joinOptions(opts.toArray(new String[opts.size()])));
      if (m_RecentFilesHandler != null) {
	for (i = 0; i < files.length; i++)
	  m_RecentFilesHandler.addRecentItem(new Setup(files[i], reader));
      }
    }
  }

  /**
   * closes the dialog/frame.
   */
  public void close() {
    cleanUp();
    if (getParentFrame() != null) {
      getParentFrame().setVisible(false);
      getParentFrame().dispose();
    }
    else if (getParentDialog() != null) {
      getParentDialog().setVisible(false);
      getParentDialog().dispose();
    }
  }

  /**
   * performs an undo if possible.
   */
  public void undo() {
    if (!m_Undo.canUndo())
      return;

    SwingWorker worker = new SwingWorker() {
      @Override
      protected Object doInBackground() throws Exception {
	showStatus("Performing Undo...");

	// add redo point
	m_Undo.addRedo(getContainerManager().getAll(), m_Undo.peekUndoComment());

	UndoPoint point = m_Undo.undo();
	List<SpectrumContainer> data = (List<SpectrumContainer>) point.getData();
	getContainerManager().clear();
	getContainerManager().addAll(data);

	return "Done!";
      };

      @Override
      protected void done() {
        super.done();
	showStatus("");
      }
    };
    worker.execute();
  }

  /**
   * performs a redo if possible.
   */
  public void redo() {
    if (!m_Undo.canRedo())
      return;

    SwingWorker worker = new SwingWorker() {
      @Override
      protected Object doInBackground() throws Exception {
	showStatus("Performing Redo...");

	// add undo point
	m_Undo.addUndo(getContainerManager().getAll(), m_Undo.peekRedoComment(), true);

	UndoPoint point = m_Undo.redo();
	List<SpectrumContainer> data = (List<SpectrumContainer>) point.getData();
	getContainerManager().clear();
	getContainerManager().addAll(data);

	return "Done!";
      };

      @Override
      protected void done() {
        super.done();
	showStatus("");
      }
    };
    worker.execute();
  }

  /**
   * Adds an undo point with the given comment.
   *
   * @param comment	the comment for the undo point
   */
  public void addUndoPoint(String comment) {
    if (isUndoSupported() && getUndo().isEnabled())
      m_Undo.addUndo(getContainerManager().getAll(), comment, true);
  }

  /**
   * pops up GOE dialog for filter.
   */
  public void filter() {
    if (m_DialogFilter == null) {
      if (getParentDialog() != null)
	m_DialogFilter = new FilterDialog(getParentDialog());
      else
	m_DialogFilter = new FilterDialog(getParentFrame());
      m_DialogFilter.setFilterListener(this);
    }

    m_DialogFilter.setFilter(m_CurrentFilter);
    m_DialogFilter.setOverlayOriginalData(m_FilterOverlayOriginalData);
    m_DialogFilter.setLocationRelativeTo(this);
    m_DialogFilter.setVisible(true);
  }

  /**
   * Filters the data.
   *
   * @param e		the event
   */
  @Override
  public void filter(FilterEvent<Spectrum> e) {
    m_CurrentFilter             = e.getFilter();
    m_FilterOverlayOriginalData = e.getOverlayOriginalData();

    if (m_FilterOverlayOriginalData)
      getScriptingEngine().add(this, FilterOverlay.ACTION + " " + OptionUtils.getCommandLine(m_CurrentFilter));
    else
      getScriptingEngine().add(this, Filter.ACTION + " " + OptionUtils.getCommandLine(m_CurrentFilter));
  }

  /**
   * Performs ICA on the visible spectra.
   */
  public void ica() {
    SwingWorker		worker;

    if (m_DialogICA == null) {
      if (getParentDialog() != null)
	m_DialogICA = new GenericObjectEditorDialog(getParentDialog(), ModalityType.DOCUMENT_MODAL);
      else
	m_DialogICA = new GenericObjectEditorDialog(getParentFrame(), true);
      m_DialogICA.setTitle("ICA");
      m_DialogICA.setUISettingsPrefix(FastICA.class);
      m_DialogICA.getGOEEditor().setCanChangeClassInDialog(false);
      m_DialogICA.getGOEEditor().setClassType(FastICA.class);
      m_DialogICA.setCurrent(m_CurrentICA);
      m_DialogICA.pack();
      m_DialogICA.setLocationRelativeTo(this);
    }
    m_DialogICA.setCurrent(m_CurrentICA);
    m_DialogICA.setVisible(true);
    if (m_DialogICA.getResult() != GenericObjectEditorDialog.APPROVE_OPTION)
      return;
    m_CurrentICA = (FastICA) m_DialogICA.getCurrent();

    worker = new SwingWorker() {
      protected FastICA m_ICA;
      @Override
      protected Object doInBackground() throws Exception {
	m_ICA = ObjectCopyHelper.copyObject(m_CurrentICA);
	List<Spectrum> list = new ArrayList<>();
	for (SpectrumContainer cont: getContainerManager().getAllVisible())
	  list.add(cont.getData());
	String result = m_ICA.analyze(list);
	if (result != null) {
	  GUIHelper.showErrorMessage(SpectrumExplorer.this, result);
	  m_ICA = null;
	}
	return null;
      }
      @Override
      protected void done() {
	super.done();
	if (m_ICA != null) {
	  ApprovalDialog dialog;
	  if (getParentDialog() != null)
	    dialog = new ApprovalDialog(getParentDialog(), ModalityType.MODELESS);
	  else
	    dialog = new ApprovalDialog(getParentFrame(), false);
	  dialog.setDefaultCloseOperation(ApprovalDialog.DISPOSE_ON_CLOSE);
	  dialog.setTitle("ICA");
	  BaseTabbedPane tabbedPane = new BaseTabbedPane();
	  // components
	  ScatterPlot plot = new ScatterPlot();
	  plot.setData(m_ICA.getComponents());
	  plot.setMouseClickAction(new ViewDataClickAction());
	  plot.setOverlays(new AbstractScatterPlotOverlay[]{new Coordinates()});
	  plot.reset();
	  tabbedPane.addTab("Components", plot);
	  // sources
	  plot = new ScatterPlot();
	  plot.setData(m_ICA.getSources());
	  plot.setMouseClickAction(new ViewDataClickAction());
	  plot.setOverlays(new AbstractScatterPlotOverlay[]{new Coordinates()});
	  plot.reset();
	  tabbedPane.addTab("Sources", plot);
	  // display dialog
	  dialog.getContentPane().add(tabbedPane, BorderLayout.CENTER);
	  dialog.setSize(GUIHelper.getDefaultLargeDialogDimension());
	  dialog.setLocationRelativeTo(SpectrumExplorer.this);
	  dialog.setVisible(true);
	}
      }
    };
    worker.execute();
  }

  /**
   * Performs PCA on the visible spectra.
   */
  public void pca() {
    SwingWorker		worker;

    if (m_DialogPCA == null) {
      if (getParentDialog() != null)
	m_DialogPCA = new GenericObjectEditorDialog(getParentDialog(), ModalityType.DOCUMENT_MODAL);
      else
	m_DialogPCA = new GenericObjectEditorDialog(getParentFrame(), true);
      m_DialogPCA.setTitle("PCA");
      m_DialogPCA.setUISettingsPrefix(PCA.class);
      m_DialogPCA.getGOEEditor().setCanChangeClassInDialog(false);
      m_DialogPCA.getGOEEditor().setClassType(PCA.class);
      m_DialogPCA.setCurrent(m_CurrentPCA);
      m_DialogPCA.pack();
      m_DialogPCA.setLocationRelativeTo(this);
    }
    m_DialogPCA.setCurrent(m_CurrentPCA);
    m_DialogPCA.setVisible(true);
    if (m_DialogPCA.getResult() != GenericObjectEditorDialog.APPROVE_OPTION)
      return;
    m_CurrentPCA = (PCA) m_DialogPCA.getCurrent();

    worker = new SwingWorker() {
      protected PCA m_PCA;
      @Override
      protected Object doInBackground() throws Exception {
	m_PCA = ObjectCopyHelper.copyObject(m_CurrentPCA);
	List<Spectrum> list = new ArrayList<>();
	for (SpectrumContainer cont: getContainerManager().getAllVisible())
	  list.add(cont.getData());
	String result = m_PCA.analyze(list);
	if (result != null) {
	  GUIHelper.showErrorMessage(SpectrumExplorer.this, result);
	  m_PCA = null;
	}
	return null;
      }
      @Override
      protected void done() {
	super.done();
	if (m_PCA != null) {
	  ApprovalDialog dialog;
	  if (getParentDialog() != null)
	    dialog = new ApprovalDialog(getParentDialog(), ModalityType.MODELESS);
	  else
	    dialog = new ApprovalDialog(getParentFrame(), false);
	  dialog.setDefaultCloseOperation(ApprovalDialog.DISPOSE_ON_CLOSE);
	  dialog.setTitle("PCA");
	  BaseTabbedPane tabbedPane = new BaseTabbedPane();
	  // loadings
	  ScatterPlot plot = new ScatterPlot();
	  plot.setData(m_PCA.getLoadings());
	  plot.setMouseClickAction(new ViewDataClickAction());
	  plot.setOverlays(new AbstractScatterPlotOverlay[]{new Coordinates()});
	  plot.reset();
	  tabbedPane.addTab("Loadings", plot);
	  // scores
	  plot = new ScatterPlot();
	  plot.setData(m_PCA.getScores());
	  plot.setMouseClickAction(new ViewDataClickAction());
	  plot.setOverlays(new AbstractScatterPlotOverlay[]{new Coordinates()});
	  plot.reset();
	  tabbedPane.addTab("Scores", plot);
	  // display dialog
	  dialog.getContentPane().add(tabbedPane, BorderLayout.CENTER);
	  dialog.setSize(GUIHelper.getDefaultLargeDialogDimension());
	  dialog.setLocationRelativeTo(SpectrumExplorer.this);
	  dialog.setVisible(true);
	}
      }
    };
    worker.execute();
  }

  /**
   * Performs PLS on the visible spectra.
   */
  public void pls() {
    SwingWorker		worker;

    if (m_DialogPLS == null) {
      if (getParentDialog() != null)
	m_DialogPLS = new GenericObjectEditorDialog(getParentDialog(), ModalityType.DOCUMENT_MODAL);
      else
	m_DialogPLS = new GenericObjectEditorDialog(getParentFrame(), true);
      m_DialogPLS.setTitle("PLS");
      m_DialogPLS.setUISettingsPrefix(PLS.class);
      m_DialogPLS.getGOEEditor().setCanChangeClassInDialog(false);
      m_DialogPLS.getGOEEditor().setClassType(PLS.class);
      m_DialogPLS.setCurrent(m_CurrentPLS);
      m_DialogPLS.pack();
      m_DialogPLS.setLocationRelativeTo(this);
    }
    m_DialogPLS.setCurrent(m_CurrentPLS);
    m_DialogPLS.setVisible(true);
    if (m_DialogPLS.getResult() != GenericObjectEditorDialog.APPROVE_OPTION)
      return;
    m_CurrentPLS = (PLS) m_DialogPLS.getCurrent();

    worker = new SwingWorker() {
      protected PLS m_PLS;
      @Override
      protected Object doInBackground() throws Exception {
	m_PLS = ObjectCopyHelper.copyObject(m_CurrentPLS);
	List<Spectrum> list = new ArrayList<>();
	for (SpectrumContainer cont: getContainerManager().getAllVisible())
	  list.add(cont.getData());
	String result = m_PLS.analyze(list);
	if (result != null) {
	  GUIHelper.showErrorMessage(SpectrumExplorer.this, result);
	  m_PLS = null;
	}
	return null;
      }
      @Override
      protected void done() {
	super.done();
	if (m_PLS != null) {
	  ApprovalDialog dialog;
	  if (getParentDialog() != null)
	    dialog = new ApprovalDialog(getParentDialog(), ModalityType.MODELESS);
	  else
	    dialog = new ApprovalDialog(getParentFrame(), false);
	  dialog.setDefaultCloseOperation(ApprovalDialog.DISPOSE_ON_CLOSE);
	  dialog.setTitle("PLS");
	  BaseTabbedPane tabbedPane = new BaseTabbedPane();
	  // loadings
	  ScatterPlot plot = new ScatterPlot();
	  plot.setData(m_PLS.getLoadings());
	  plot.setMouseClickAction(new ViewDataClickAction());
	  plot.setOverlays(new AbstractScatterPlotOverlay[]{new Coordinates()});
	  plot.reset();
	  tabbedPane.addTab("Loadings", plot);
	  // scores
	  plot = new ScatterPlot();
	  plot.setData(m_PLS.getScores());
	  plot.setMouseClickAction(new ViewDataClickAction());
	  plot.setOverlays(new AbstractScatterPlotOverlay[]{new Coordinates()});
	  plot.reset();
	  tabbedPane.addTab("Scores", plot);
	  // display dialog
	  dialog.getContentPane().add(tabbedPane, BorderLayout.CENTER);
	  dialog.setSize(GUIHelper.getDefaultLargeDialogDimension());
	  dialog.setLocationRelativeTo(SpectrumExplorer.this);
	  dialog.setVisible(true);
	}
      }
    };
    worker.execute();
  }

  /**
   * Sets the zoom overview panel visible or not.
   *
   * @param value	if true then the panel is visible
   */
  public void setZoomOverviewPanelVisible(boolean value) {
    m_PanelSpectrum.setZoomOverviewPanelVisible(value);
  }

  /**
   * Returns whether the zoom overview panel is visible or not.
   *
   * @return		true if visible
   */
  public boolean isZoomOverviewPanelVisible() {
    return m_PanelSpectrum.isZoomOverviewPanelVisible();
  }

  /**
   * Lets the user select a new color provider.
   */
  protected void selectColorProvider() {
    if (m_DialogColorProvider == null) {
      if (getParentDialog() != null)
	m_DialogColorProvider = new GenericObjectEditorDialog(getParentDialog(), ModalityType.DOCUMENT_MODAL);
      else
	m_DialogColorProvider = new GenericObjectEditorDialog(getParentFrame(), true);
      m_DialogColorProvider.setTitle("Select color provider");
      m_DialogColorProvider.setUISettingsPrefix(ColorProvider.class);
      m_DialogColorProvider.getGOEEditor().setClassType(ColorProvider.class);
      m_DialogColorProvider.getGOEEditor().setCanChangeClassInDialog(true);
    }
    
    m_DialogColorProvider.setCurrent(getContainerManager().getColorProvider().shallowCopy());
    m_DialogColorProvider.setLocationRelativeTo(m_DialogColorProvider.getParent());
    m_DialogColorProvider.setVisible(true);
    if (m_DialogColorProvider.getResult() != GenericObjectEditorDialog.APPROVE_OPTION)
      return;
    getContainerManager().setColorProvider(((ColorProvider) m_DialogColorProvider.getCurrent()).shallowCopy());
  }

  /**
   * Lets the user select a new paintlet.
   */
  protected void selectPaintlet() {
    Paintlet 	paintlet;
    boolean	zoomVisible;

    if (m_DialogPaintlet == null) {
      if (getParentDialog() != null)
	m_DialogPaintlet = new GenericObjectEditorDialog(getParentDialog(), ModalityType.DOCUMENT_MODAL);
      else
	m_DialogPaintlet = new GenericObjectEditorDialog(getParentFrame(), true);
      m_DialogPaintlet.setTitle("Select paintlet");
      m_DialogPaintlet.setUISettingsPrefix(AbstractSpectrumPaintlet.class);
      m_DialogPaintlet.getGOEEditor().setClassType(AbstractSpectrumPaintlet.class);
      m_DialogPaintlet.getGOEEditor().setCanChangeClassInDialog(true);
    }
    
    m_DialogPaintlet.setCurrent(getSpectrumPanel().getDataPaintlet().shallowCopy());
    m_DialogPaintlet.setLocationRelativeTo(m_DialogPaintlet.getParent());
    m_DialogPaintlet.setVisible(true);
    if (m_DialogPaintlet.getResult() != GenericObjectEditorDialog.APPROVE_OPTION)
      return;
    paintlet = (Paintlet) m_DialogPaintlet.getCurrent();
    if (paintlet instanceof AntiAliasingSupporter)
      ((AntiAliasingSupporter) paintlet).setAntiAliasingEnabled(getSpectrumPanel().isAntiAliasingEnabled());
    getSpectrumPanel().setDataPaintlet(paintlet);
    zoomVisible = getSpectrumPanel().isZoomOverviewPanelVisible();
    getSpectrumPanel().getZoomOverviewPanel().setDataContainerPanel(getSpectrumPanel());
    getSpectrumPanel().setZoomOverviewPanelVisible(zoomVisible);
  }

  /**
   * Returns the currently used database connection object, can be null.
   *
   * @return		the current object
   */
  @Override
  public AbstractDatabaseConnection getDatabaseConnection() {
    return m_DatabaseConnection;
  }

  /**
   * Sets the database connection object to use.
   *
   * @param value	the object to use
   */
  @Override
  public void setDatabaseConnection(AbstractDatabaseConnection value) {
    m_DatabaseConnection.removeChangeListener(this);
    m_DatabaseConnection = value;
    m_DatabaseConnection.addChangeListener(this);
    m_PanelSpectrum.setDatabaseConnection(value);
    if (m_LoadDialog != null)
      m_LoadDialog.setDatabaseConnection(value);
  }

  /**
   * Returns the classes that the supporter generates.
   *
   * @return		the classes
   */
  @Override
  public Class[] getSendToClasses() {
    return new Class[]{JComponent.class};
  }

  /**
   * Checks whether something to send is available.
   *
   * @param cls		the classes to retrieve an item for
   * @return		true if an object is available for sending
   */
  @Override
  public boolean hasSendToItem(Class[] cls) {
    return (getContainerManager().countVisible() > 0);
  }

  /**
   * Returns the object to send.
   *
   * @param cls		the classes to retrieve the item for
   * @return		the item to send
   */
  @Override
  public Object getSendToItem(Class[] cls) {
    Object	result;

    result = null;

    if (SendToActionUtils.isAvailable(JComponent.class, cls)) {
      if (getContainerManager().countVisible() > 0) {
	result = this;
      }
    }

    return result;
  }

  /**
   * Opens a new window.
   *
   * @param visible 	whether to make the window visible
   */
  protected SpectrumExplorer newWindow(boolean visible) {
    SpectrumExplorer 	result;
    ChildFrame 		oldFrame;
    ChildFrame 		newFrame;

    result    = null;
    oldFrame = (ChildFrame) GUIHelper.getParent(this, ChildFrame.class);
    if (oldFrame != null) {
      newFrame = oldFrame.getNewWindow();
      newFrame.setVisible(visible);
      result  = (SpectrumExplorer) newFrame.getContentPane().getComponent(0);

      // transfer settings
      result.getContainerManager().setColorProvider(ObjectCopyHelper.copyObject(getContainerManager().getColorProvider()));
      result.getSpectrumPanel().setDataPaintlet(ObjectCopyHelper.copyObject(getSpectrumPanel().getDataPaintlet()));
      result.m_CurrentFilter = ObjectCopyHelper.copyObject(m_CurrentFilter);
      result.m_CurrentICA = ObjectCopyHelper.copyObject(m_CurrentICA);
      result.m_CurrentPCA = ObjectCopyHelper.copyObject(m_CurrentPCA);
      result.m_CurrentPLS = ObjectCopyHelper.copyObject(m_CurrentPLS);
      result.m_FilterOverlayOriginalData = m_FilterOverlayOriginalData;
    }

    return result;
  }

  /**
   * Opens a new window with the same content/setup.
   *
   * @param visible 	whether to make the window visible
   */
  protected SpectrumExplorer duplicateWindow(boolean visible) {
    SpectrumExplorer		result;
    ChildFrame			frame;
    SpectrumContainerManager	managerThis;
    SpectrumContainerManager	managerNew;

    result = newWindow(false);

    // duplicate content
    if (result != null) {
      managerThis = getContainerManager();
      managerNew  = result.getContainerManager();
      managerNew.startUpdate();
      managerNew.addAll(managerThis.getAll());
      managerNew.finishUpdate();

      if (visible) {
	frame = (ChildFrame) GUIHelper.getParent(result, ChildFrame.class);
	frame.setVisible(true);
      }
    }


    return result;
  }

  /**
   * Cleans up data structures, frees up memory.
   */
  @Override
  public void cleanUp() {
    m_PanelSpectrum.getContainerManager().removeDataChangeListener(this);
    DatabaseConnection.getSingleton().removeChangeListener(this);
    m_PanelSpectrum.cleanUp();
    if (m_LoadDialog != null)
      m_LoadDialog.cleanUp();
    if (m_ScriptingDialog != null)
      m_ScriptingDialog.cleanUp();
    if (m_DialogColorProvider != null) {
      m_DialogColorProvider.dispose();
      m_DialogColorProvider = null;
    }
    if (m_DialogPaintlet != null) {
      m_DialogPaintlet.dispose();
      m_DialogPaintlet = null;
    }
    if (m_DialogICA != null) {
      m_DialogICA.dispose();
      m_DialogICA = null;
    }
    if (m_DialogPCA != null) {
      m_DialogPCA.dispose();
      m_DialogPCA = null;
    }
    if (m_DialogPLS != null) {
      m_DialogPLS.dispose();
      m_DialogPLS = null;
    }
    if (m_Undo != null)
      m_Undo.cleanUp();
  }
}
