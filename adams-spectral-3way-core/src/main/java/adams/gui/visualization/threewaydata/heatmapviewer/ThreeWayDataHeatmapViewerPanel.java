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
 * ThreeWayDataHeatmapViewerPanel.java
 * Copyright (C) 2018-2022 University of Waikato, Hamilton, New Zealand
 */
package adams.gui.visualization.threewaydata.heatmapviewer;

import adams.core.CleanUpHandler;
import adams.core.Properties;
import adams.core.StatusMessageHandler;
import adams.core.io.PlaceholderFile;
import adams.data.filter.Filter;
import adams.data.io.input.AbstractThreeWayDataReader;
import adams.data.io.output.AbstractDataContainerWriter;
import adams.data.threeway.ThreeWayData;
import adams.data.threewayfilter.DownSample;
import adams.gui.chooser.BaseColorChooser;
import adams.gui.chooser.HeatmapFileChooser;
import adams.gui.chooser.ThreeWayDataFileChooser;
import adams.gui.core.BasePanel;
import adams.gui.core.BaseStatusBar;
import adams.gui.core.CustomColorImageIcon;
import adams.gui.core.GUIHelper;
import adams.gui.core.ImageManager;
import adams.gui.core.MenuBarProvider;
import adams.gui.core.RecentFilesHandlerWithCommandline;
import adams.gui.core.RecentFilesHandlerWithCommandline.Setup;
import adams.gui.core.SearchPanel;
import adams.gui.core.SearchPanel.LayoutType;
import adams.gui.event.FilterEvent;
import adams.gui.event.FilterListener;
import adams.gui.event.RecentItemEvent;
import adams.gui.event.RecentItemListener;
import adams.gui.event.SearchEvent;
import adams.gui.event.SearchListener;
import adams.gui.goe.GenericObjectEditorDialog;
import adams.gui.plugin.ToolPluginSupporter;
import adams.gui.sendto.SendToActionSupporter;
import adams.gui.sendto.SendToActionUtils;
import adams.gui.visualization.container.FilterDialog;
import adams.gui.visualization.core.AbstractColorGradientGenerator;
import adams.gui.visualization.heatmap.overlay.Centroid;
import adams.gui.visualization.heatmap.selection.AbstractSelectionProcessor;
import adams.gui.visualization.heatmap.selection.NullProcessor;
import adams.gui.visualization.threewaydata.heatmapviewer.overlay.AbstractThreeWayDataOverlay;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for viewing/processing 3-way data as data structures.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class ThreeWayDataHeatmapViewerPanel
  extends BasePanel
  implements MenuBarProvider, StatusMessageHandler,
  FilterListener<ThreeWayData>, SendToActionSupporter, CleanUpHandler,
  ToolPluginSupporter<ThreeWayDataHeatmapPanel> {

  /** for serialization. */
  private static final long serialVersionUID = -2642034258827736757L;

  /** the file to store the recent files in. */
  public final static String SESSION_FILE = "ThreeWayDataHeatmapViewerPanelSession.props";

  /** the setup for the panel. */
  protected static Properties m_Properties;

  /** the tabbed pane for the data structures. */
  protected ThreeWayDataHeatmapViewerMultiPagePane m_MultiPagePane;

  /** the status bar. */
  protected BaseStatusBar m_StatusBar;

  /** the menu bar. */
  protected JMenuBar m_MenuBar;

  /** the "load recent" submenu. */
  protected JMenu m_MenuItemFileOpenRecent;

  /** the "save as" menu item. */
  protected JMenuItem m_MenuItemFileSaveAs;

  /** the "close current" menu item. */
  protected JMenuItem m_MenuItemFileCloseCurrent;

  /** the "close all" menu item. */
  protected JMenuItem m_MenuItemFileCloseAll;

  /** the filter menu item. */
  protected JMenuItem m_MenuItemProcessFilterHeatmap;

  /** the filter all menu item. */
  protected JMenuItem m_MenuItemProcessFilterAllHeatmaps;

  /** whether to apply overlay changes to all panels. */
  protected JMenuItem m_MenuItemViewApplyAll;

  /** the remove overlays menu item. */
  protected JMenuItem m_MenuItemViewRemoveOverlays;

  /** the add overlay menu item. */
  protected JMenuItem m_MenuItemViewAddOverlay;

  /** the menu "zoom". */
  protected JMenu m_MenuViewZoom;

  /** the menu item "zoom in". */
  protected JMenuItem m_MenuItemViewZoomIn;

  /** the menu item "zoom out". */
  protected JMenuItem m_MenuItemViewZoomOut;

  /** the color generator menu item. */
  protected JMenuItem m_MenuItemViewColorGenerator;

  /** the missing value color menu item. */
  protected JMenuItem m_MenuItemViewMissingValueColor;

  /** for loading data structures from disk. */
  protected ThreeWayDataFileChooser m_FileChooser;

  /** the current filter. */
  protected Filter<ThreeWayData> m_CurrentFilter;

  /** the filter dialog. */
  protected FilterDialog m_DialogFilter;

  /** indicates whether the filtered data was overlayed over the original. */
  protected boolean m_FilterOverlayOriginalData;

  /** indicates whether filter is applied to all data structures. */
  protected boolean m_FilterAll;

  /** the dialog for selecting an overlay. */
  protected GenericObjectEditorDialog m_DialogOverlay;

  /** the dialog for selecting an selection processor. */
  protected GenericObjectEditorDialog m_DialogSelectionProcessor;

  /** the search panel for the heatmap report. */
  protected SearchPanel m_SearchPanel;

  /** the dialog for selecting the color provider. */
  protected GenericObjectEditorDialog m_DialogColorGenerator;

  /** the dialog for selecting the missing value color. */
  protected BaseColorChooser m_DialogMissingValueColor;

  /** the recent files handler. */
  protected RecentFilesHandlerWithCommandline<JMenu> m_RecentFilesHandler;

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    Properties		props;

    super.initialize();

    props                      = getProperties();
    m_FileChooser              =  new ThreeWayDataFileChooser(props.getPath("InitialDir", "%h"));
    m_CurrentFilter            = new DownSample();
    m_FilterAll                = false;
    m_DialogOverlay            = null;
    m_DialogSelectionProcessor = null;
    m_DialogColorGenerator     = null;
    m_DialogMissingValueColor  = null;
    m_RecentFilesHandler       = null;
  }

  /**
   * Initializes the widgets.
   */
  @Override
  protected void initGUI() {
    JPanel	panel;
    JPanel	panel2;

    super.initGUI();

    setLayout(new BorderLayout());

    panel = new JPanel(new BorderLayout());
    add(panel, BorderLayout.CENTER);

    m_MultiPagePane = new ThreeWayDataHeatmapViewerMultiPagePane();
    m_MultiPagePane.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
	if ((m_MenuItemViewMissingValueColor != null) && (getCurrentPanel() != null))
	  m_MenuItemViewMissingValueColor.setIcon(new CustomColorImageIcon(16, 16, getCurrentPanel().getMissingValueColor()));
      }
    });
    panel.add(m_MultiPagePane, BorderLayout.CENTER);

    m_SearchPanel = new SearchPanel(LayoutType.HORIZONTAL, true, "_Search", true, null);
    m_SearchPanel.setMinimumChars(2);
    m_SearchPanel.addSearchListener(new SearchListener() {
      @Override
      public void searchInitiated(SearchEvent e) {
	search(m_SearchPanel.getSearchText(), m_SearchPanel.isRegularExpression());
      }
    });
    panel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panel2.add(m_SearchPanel);
    panel.add(panel2, BorderLayout.SOUTH);

    m_StatusBar = new BaseStatusBar();
    add(m_StatusBar, BorderLayout.SOUTH);
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
    int			i;
    int[]		zooms;
    String[]		shortcuts;

    if (m_MenuBar == null) {
      result = new JMenuBar();

      // File
      menu = new JMenu("File");
      result.add(menu);
      menu.setMnemonic('F');
      menu.addChangeListener(new ChangeListener() {
	@Override
	public void stateChanged(ChangeEvent e) {
	  updateMenu();
	}
      });

      // File/Load from file
      menuitem = new JMenuItem("Open...");
      menu.add(menuitem);
      menuitem.setMnemonic('o');
      menuitem.setIcon(ImageManager.getIcon("open.gif"));
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed O"));
      menuitem.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  load();
	}
      });

      // File/Recent files
      submenu = new JMenu("Open recent");
      menu.add(submenu);
      m_RecentFilesHandler = new RecentFilesHandlerWithCommandline<JMenu>(
	SESSION_FILE, getProperties().getInteger("MaxRecentFiles", 5), submenu);
      m_RecentFilesHandler.addRecentItemListener(new RecentItemListener<JMenu,Setup>() {
	@Override
	public void recentItemAdded(RecentItemEvent<JMenu,Setup> e) {
	  // ignored
	}
	@Override
	public void recentItemSelected(RecentItemEvent<JMenu,Setup> e) {
	  load(new File[]{e.getItem().getFile()}, (AbstractThreeWayDataReader) e.getItem().getHandler());
	}
      });
      m_MenuItemFileOpenRecent = submenu;

      // File/Save As
      menuitem = new JMenuItem("Save as...");
      menu.add(menuitem);
      menuitem.setMnemonic('S');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl shift pressed S"));
      menuitem.setIcon(ImageManager.getIcon("save.gif"));
      menuitem.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  saveAs();
	}
      });
      m_MenuItemFileSaveAs = menuitem;

      // File/Close page
      menuitem = new JMenuItem("Close page");
      menu.addSeparator();
      menu.add(menuitem);
      menuitem.setMnemonic('t');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed W"));
      menuitem.setIcon(ImageManager.getIcon("delete.gif"));
      menuitem.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  closeCurrent();
	}
      });
      m_MenuItemFileCloseCurrent = menuitem;

      // File/Close all pages
      menuitem = new JMenuItem("Close all pages");
      menu.add(menuitem);
      menuitem.setMnemonic('a');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed N"));
      menuitem.setIcon(ImageManager.getIcon("delete_all.gif"));
      menuitem.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  closeAll();
	}
      });
      m_MenuItemFileCloseAll = menuitem;

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
      menuitem.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  close();
	}
      });

      // Process
      menu = new JMenu("Process");
      result.add(menu);
      menu.setMnemonic('P');
      menu.addChangeListener(new ChangeListener() {
	@Override
	public void stateChanged(ChangeEvent e) {
	  updateMenu();
	}
      });

      // Process/Filter heatmap
      menuitem = new JMenuItem("Filter...");
      menu.add(menuitem);
      menuitem.setMnemonic('F');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed F"));
      menuitem.setIcon(ImageManager.getIcon("run.gif"));
      menuitem.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  m_FilterAll = false;
	  filter();
	}
      });
      m_MenuItemProcessFilterHeatmap = menuitem;

      // Process/Filter heatmap
      menuitem = new JMenuItem("Filter all...");
      menu.add(menuitem);
      menuitem.setMnemonic('a');
      menuitem.setIcon(ImageManager.getEmptyIcon());
      menuitem.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  m_FilterAll = true;
	  filter();
	}
      });
      m_MenuItemProcessFilterAllHeatmaps = menuitem;

      // View
      menu = new JMenu("View");
      result.add(menu);
      menu.setMnemonic('V');
      menu.addChangeListener(new ChangeListener() {
	@Override
	public void stateChanged(ChangeEvent e) {
	  updateMenu();
	}
      });

      // View/Zoom
      submenu = new JMenu("Zoom");
      menu.add(submenu);
      submenu.setMnemonic('Z');
      submenu.setIcon(ImageManager.getIcon("glasses.gif"));
      submenu.addChangeListener(new ChangeListener() {
	public void stateChanged(ChangeEvent e) {
	  updateMenu();
	}
      });
      m_MenuViewZoom = submenu;

      //View/Zoom/Zoom in
      menuitem = new JMenuItem("Zoom in");
      submenu.add(menuitem);
      menuitem.setMnemonic('i');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl shift pressed I"));
      menuitem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  getCurrentPanel().getImagePanel().setScale(getCurrentPanel().getImagePanel().getScale() * 1.5);
	}
      });
      m_MenuItemViewZoomIn = menuitem;

      //View/Zoom/Zoom out
      menuitem = new JMenuItem("Zoom out");
      submenu.add(menuitem);
      menuitem.setMnemonic('o');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl shift pressed O"));
      menuitem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  getCurrentPanel().getImagePanel().setScale(getCurrentPanel().getImagePanel().getScale() / 1.5);
	}
      });
      m_MenuItemViewZoomOut = menuitem;

      // zoom levels
      zooms = new int[]{
	-100,
	25,
	50,
	66,
	75,
	100,
	150,
	200,
	400,
	800,
	1600,
      };
      shortcuts = new String[]{
	"F",
	"",
	"",
	"",
	"",
	"1",
	"",
	"2",
	"4",
	"",
	"",
      };
      submenu.addSeparator();
      for (i = 0; i < zooms.length; i++) {
	final int fZoom = zooms[i];
	if (zooms[i] == -100)
	  menuitem = new JMenuItem("Best fit");
	else
	  menuitem = new JMenuItem(zooms[i] + "%");
	submenu.add(menuitem);
	if (shortcuts[i].length() > 0)
	  menuitem.setAccelerator(GUIHelper.getKeyStroke(shortcuts[i]));
	menuitem.addActionListener(new ActionListener() {
	  public void actionPerformed(ActionEvent e) {
	    zoom(fZoom);
	  }
	});
      }

      // View/Apply to all panels
      menuitem = new JCheckBoxMenuItem("Apply to all");
      menu.addSeparator();
      menu.add(menuitem);
      menuitem.setSelected(false);
      menuitem.setMnemonic('A');
      menuitem.setIcon(ImageManager.getEmptyIcon());
      m_MenuItemViewApplyAll = menuitem;

      // View/Remove all overlays
      menuitem = new JMenuItem("Remove overlays");
      menu.add(menuitem);
      menuitem.setSelected(false);
      menuitem.setMnemonic('R');
      menuitem.setIcon(ImageManager.getEmptyIcon());
      menuitem.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  if (m_MenuItemViewApplyAll.isSelected()) {
	    for (int i = 0; i < getPanelCount(); i++)
	      getPanelAt(i).removeOverlays();
	  }
	  else {
	    getCurrentPanel().removeOverlays();
	  }
	}
      });
      m_MenuItemViewRemoveOverlays = menuitem;

      // View/Add overlay
      menuitem = new JMenuItem("Add overlay...");
      menu.add(menuitem);
      menuitem.setSelected(false);
      menuitem.setMnemonic('d');
      menuitem.setIcon(ImageManager.getEmptyIcon());
      menuitem.addActionListener(new ActionListener() {
	@Override
	public void actionPerformed(ActionEvent e) {
	  AbstractThreeWayDataOverlay overlay = selectOverlay();
	  if (overlay == null)
	    return;
	  if (m_MenuItemViewApplyAll.isSelected()) {
	    for (int i = 0; i < getPanelCount(); i++)
	      getPanelAt(i).addOverlay(overlay);
	  }
	  else {
	    getCurrentPanel().addOverlay(overlay);
	  }
	}
      });
      m_MenuItemViewAddOverlay = menuitem;

      // View/Color generator
      menuitem = new JMenuItem("Color generator...");
      menu.addSeparator();
      menu.add(menuitem);
      menuitem.setMnemonic('G');
      menuitem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  selectColorGenerator();
	}
      });
      m_MenuItemViewColorGenerator = menuitem;

      // View/Missing value color
      menuitem = new JMenuItem("Missing value color...");
      menuitem.setIcon(new CustomColorImageIcon(16, 16, ThreeWayDataHeatmapPanel.getProperties().getColor("Image.MissingValueColor", new Color(255, 255, 255, 0))));
      menu.add(menuitem);
      menuitem.setMnemonic('M');
      menuitem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  selectMissingValueColor();
	}
      });
      m_MenuItemViewMissingValueColor = menuitem;

      m_MenuBar = result;
      updateMenu();
    }

    return m_MenuBar;
  }

  /**
   * Returns the number of panels open.
   *
   * @return            the number of panels
   */
  public int getPanelCount() {
    return m_MultiPagePane.getPageCount();
  }

  /**
   * Returns the specified heatmap panel.
   *
   * @param index	the index of the panel
   * @return		the panel, null if invalid index
   */
  public ThreeWayDataHeatmapPanel getPanelAt(int index) {
    ThreeWayDataHeatmapPanel	result;

    result = null;

    if (index != -1)
      result = (ThreeWayDataHeatmapPanel) m_MultiPagePane.getPageAt(index);

    return result;
  }

  /**
   * Returns the currently selected heatmap panel.
   *
   * @return		the panel, null if none selected
   */
  public ThreeWayDataHeatmapPanel getCurrentPanel() {
    return getPanelAt(m_MultiPagePane.getSelectedIndex());
  }

  /**
   * Returns the all panels.
   *
   * @return		the panels
   */
  public ThreeWayDataHeatmapPanel[] getAllPanels() {
    List<ThreeWayDataHeatmapPanel>	result;
    int			i;

    result = new ArrayList<>();
    for (i = 0; i < m_MultiPagePane.getPageCount(); i++)
      result.add(getPanelAt(i));

    return result.toArray(new ThreeWayDataHeatmapPanel[result.size()]);
  }

  /**
   * updates the enabled state of the menu items.
   */
  protected void updateMenu() {
    boolean	dataLoaded;

    if (m_MenuBar == null)
      return;

    dataLoaded = (m_MultiPagePane.getPageCount() > 0);

    // File
    m_MenuItemFileSaveAs.setEnabled(getCurrentPanel() != null);
    m_MenuItemFileCloseCurrent.setEnabled(getCurrentPanel() != null);
    m_MenuItemFileCloseAll.setEnabled(dataLoaded);

    // Process
    m_MenuItemProcessFilterHeatmap.setEnabled(dataLoaded);
    m_MenuItemProcessFilterAllHeatmaps.setEnabled(dataLoaded);

    // View
    m_MenuViewZoom.setEnabled(dataLoaded);
    m_MenuItemViewZoomIn.setEnabled(dataLoaded);
    m_MenuItemViewZoomOut.setEnabled(dataLoaded);
    m_MenuItemViewApplyAll.setEnabled(dataLoaded);
    m_MenuItemViewAddOverlay.setEnabled(dataLoaded);
    m_MenuItemViewRemoveOverlays.setEnabled(dataLoaded);
    m_MenuItemViewColorGenerator.setEnabled(dataLoaded);
    m_MenuItemViewMissingValueColor.setEnabled(dataLoaded);
  }

  /**
   * Removes the current tab.
   */
  public void closeCurrent() {
    if (m_MultiPagePane.getSelectedIndex() != -1)
      m_MultiPagePane.removePageAt(m_MultiPagePane.getSelectedIndex());
  }

  /**
   * Removes all the data.
   */
  public void closeAll() {
    m_MultiPagePane.removeAllPages();
  }

  /**
   * Creates a new panel and configures it.
   *
   * @param data	the data to create the panel for
   * @return		the panel
   */
  protected ThreeWayDataHeatmapPanel newPanel(ThreeWayData data) {
    ThreeWayDataHeatmapPanel 	result;

    result = new ThreeWayDataHeatmapPanel(this);
    result.setSearchPanelVisible(false);
    result.setData(data);

    return result;
  }

  /**
   * Adds a new tab using the heatmap and adds the comment to the log.
   *
   * @param data		the map to add in a new tab
   * @param comment	the log comment
   */
  public void newTab(ThreeWayData data, String comment) {
    SwingUtilities.invokeLater(() -> {
      ThreeWayDataHeatmapPanel panel = newPanel(data);
      m_MultiPagePane.addPage(panel.getTitle(), panel);
      m_MultiPagePane.setSelectedPage(panel);
      showStatus("");
    });
  }

  /**
   * Loads data structures from disk, popping up a file chooser dialog.
   */
  public void load(final File[] files, final AbstractThreeWayDataReader reader) {
    SwingWorker 	worker;

    worker = new SwingWorker() {
      @Override
      protected Object doInBackground() throws Exception {
	for (final File file: files) {
	  showStatus("Loading file: " + file);
	  reader.setInput(new PlaceholderFile(file));
	  List<ThreeWayData> data = reader.read();
	  if (data.size() == 0) {
	    GUIHelper.showErrorMessage(ThreeWayDataHeatmapViewerPanel.this, "Failed to read data from:\n" + reader.getInput());
	    showStatus("");
	    return null;
	  }
	  ThreeWayDataHeatmapPanel panel = newPanel(data.get(0));
	  panel.setCurrentFile(file);
	  m_MultiPagePane.addPage(panel.getTitle(), panel);
	  m_MultiPagePane.setSelectedPage(panel);
	  if (m_RecentFilesHandler != null)
	    m_RecentFilesHandler.addRecentItem(new Setup(file, reader));
	}
	return null;
      }

      @Override
      protected void done() {
	showStatus("");
	super.done();
      }
    };
    worker.execute();
  }

  /**
   * Loads data structures from disk, popping up a file chooser dialog.
   */
  public void load() {
    int		retVal;

    m_FileChooser.setMultiSelectionEnabled(true);
    retVal = m_FileChooser.showOpenDialog(this);
    if (retVal != HeatmapFileChooser.APPROVE_OPTION)
      return;

    load(m_FileChooser.getSelectedFiles(), (AbstractThreeWayDataReader) m_FileChooser.getReader());
  }

  /**
   * Saves the current heatmap to disk.
   */
  protected void saveAs() {
    int				retVal;
    ThreeWayData 		data;
    PlaceholderFile		file;
    AbstractDataContainerWriter	writer;

    retVal = m_FileChooser.showSaveDialog(this);
    if (retVal != HeatmapFileChooser.APPROVE_OPTION)
      return;

    data   = getCurrentPanel().getData();
    file   = m_FileChooser.getSelectedPlaceholderFile();
    writer = m_FileChooser.getWriter();
    writer.setOutput(file);
    if (!writer.write(data))
      GUIHelper.showErrorMessage(this, "Failed to write heatmap to '" + file + "'!\nCheck console for error message.");
  }

  /**
   * closes the dialog/frame.
   */
  public void close() {
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
   * pops up GOE dialog for filter.
   */
  protected void filter() {
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
  public void filter(final FilterEvent<ThreeWayData> e) {
    int		i;
    int		count;
    Runnable	run;
    int[]	indices;

    m_CurrentFilter             = e.getFilter();
    m_FilterOverlayOriginalData = e.getOverlayOriginalData();

    count = m_MultiPagePane.getPageCount();
    if (m_FilterAll) {
      indices = new int[count];
      for (i = 0; i < count; i++)
	indices[i] = i;
    }
    else {
      indices = new int[]{m_MultiPagePane.getSelectedIndex()};
    }

    for (i = 0; i < indices.length; i++) {
      final int index = indices[i];
      run = new Runnable() {
	@Override
	public void run() {
	  Filter<ThreeWayData> filter = e.getFilter().shallowCopy();
	  ThreeWayDataHeatmapPanel panel = getPanelAt(index);
	  File current = panel.getCurrentFile();
	  ThreeWayData filtered = filter.filter(panel.getData());
	  filter.cleanUp();
	  if (e.getOverlayOriginalData()) {
	    panel = newPanel(filtered);
	    m_MultiPagePane.addPage(panel.getTitle(), panel);
	  }
	  else {
	    panel.setData(filtered);
	    m_MultiPagePane.setTitleAt(index, panel.getTitle());
	  }
	}
      };
      SwingUtilities.invokeLater(run);
    }
  }

  /**
   * Performs a search.
   *
   * @param text	the search text
   * @param isRegExp	whether the search text is a regular expression
   */
  public void search(String text, boolean isRegExp) {
    int		i;

    for (i = 0; i < m_MultiPagePane.getPageCount(); i++)
      getPanelAt(i).search(text, isRegExp);
  }

  /**
   * Zooms in/out.
   *
   * @param zoom	the zoom (in percent)
   */
  protected void zoom(int zoom) {
    getCurrentPanel().setZoom((double) zoom / 100);
  }

  /**
   * Lets the user select an overlay from a dialog. Returns the overlay
   * if successfully selected (ie not dialog aborted).
   *
   * @return            the selected overlay, null if none selected
   */
  protected AbstractThreeWayDataOverlay selectOverlay() {
    if (m_DialogOverlay == null) {
      if (getParentDialog() != null)
	m_DialogOverlay = new GenericObjectEditorDialog(getParentDialog(), ModalityType.DOCUMENT_MODAL);
      else
	m_DialogOverlay = new GenericObjectEditorDialog(getParentFrame(), true);
      m_DialogOverlay.setTitle("Select overlay");
      m_DialogOverlay.setUISettingsPrefix(AbstractThreeWayDataOverlay.class);
      m_DialogOverlay.getGOEEditor().setClassType(AbstractThreeWayDataOverlay.class);
      m_DialogOverlay.getGOEEditor().setCanChangeClassInDialog(true);
      m_DialogOverlay.setCurrent(new Centroid());
      m_DialogOverlay.setLocationRelativeTo(this);
    }

    m_DialogOverlay.setVisible(true);
    if (m_DialogOverlay.getResult() != GenericObjectEditorDialog.APPROVE_OPTION)
      return null;

    return (AbstractThreeWayDataOverlay) m_DialogOverlay.getCurrent();
  }

  /**
   * Lets the user select a selection processor from a dialog. Returns the
   * selection processor if successfully chosen (ie not dialog aborted).
   *
   * @return            the chosen selection processor, null if none selected
   */
  protected AbstractSelectionProcessor selectSelectionProcessor() {
    if (m_DialogSelectionProcessor == null) {
      if (getParentDialog() != null)
	m_DialogSelectionProcessor = new GenericObjectEditorDialog(getParentDialog(), ModalityType.DOCUMENT_MODAL);
      else
	m_DialogSelectionProcessor = new GenericObjectEditorDialog(getParentFrame(), true);
      m_DialogSelectionProcessor.setTitle("Select overlay");
      m_DialogSelectionProcessor.setUISettingsPrefix(AbstractSelectionProcessor.class);
      m_DialogSelectionProcessor.getGOEEditor().setClassType(AbstractSelectionProcessor.class);
      m_DialogSelectionProcessor.getGOEEditor().setCanChangeClassInDialog(true);
      m_DialogSelectionProcessor.setCurrent(new NullProcessor());
      m_DialogSelectionProcessor.setLocationRelativeTo(this);
    }

    m_DialogSelectionProcessor.setVisible(true);
    if (m_DialogSelectionProcessor.getResult() != GenericObjectEditorDialog.APPROVE_OPTION)
      return null;

    return (AbstractSelectionProcessor) m_DialogSelectionProcessor.getCurrent();
  }

  /**
   * Lets the user select a new color generator.
   */
  protected void selectColorGenerator() {
    if (m_DialogColorGenerator == null) {
      if (getParentDialog() != null)
	m_DialogColorGenerator = new GenericObjectEditorDialog(getParentDialog(), ModalityType.DOCUMENT_MODAL);
      else
	m_DialogColorGenerator = new GenericObjectEditorDialog(getParentFrame(), true);
      m_DialogColorGenerator.setTitle("Select color generator");
      m_DialogColorGenerator.setUISettingsPrefix(AbstractColorGradientGenerator.class);
      m_DialogColorGenerator.getGOEEditor().setClassType(AbstractColorGradientGenerator.class);
      m_DialogColorGenerator.getGOEEditor().setCanChangeClassInDialog(true);
      m_DialogColorGenerator.setLocationRelativeTo(this);
    }

    m_DialogColorGenerator.setCurrent(getCurrentPanel().getColorGenerator().shallowCopy());
    m_DialogColorGenerator.setVisible(true);
    if (m_DialogColorGenerator.getResult() != GenericObjectEditorDialog.APPROVE_OPTION)
      return;
    getCurrentPanel().setColorGenerator(((AbstractColorGradientGenerator) m_DialogColorGenerator.getCurrent()).shallowCopy());
  }

  /**
   * Shows a color dialog for selecting the color representing missing values.
   */
  protected void selectMissingValueColor() {
    if (m_DialogMissingValueColor == null) {
      if (getParentDialog() != null)
	m_DialogMissingValueColor = new BaseColorChooser(getParentDialog(), ModalityType.DOCUMENT_MODAL);
      else
	m_DialogMissingValueColor = new BaseColorChooser(getParentFrame(), true);
      m_DialogMissingValueColor.setTitle("Select missing value color");
      m_DialogMissingValueColor.setLocationRelativeTo(this);
    }

    m_DialogMissingValueColor.setColor(getCurrentPanel().getMissingValueColor());
    m_DialogMissingValueColor.setVisible(true);
    if (m_DialogMissingValueColor.getOption() != BaseColorChooser.APPROVE_OPTION)
      return;
    getCurrentPanel().setMissingValueColor(m_DialogMissingValueColor.getColor());
    m_MenuItemViewMissingValueColor.setIcon(new CustomColorImageIcon(16, 16, getCurrentPanel().getMissingValueColor()));
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
    return (getCurrentPanel() != null);
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
      if (getCurrentPanel() != null) {
	result = getCurrentPanel();
      }
    }

    return result;
  }

  /**
   * Returns the properties for this panel.
   *
   * @return		the properties file for this panel
   */
  public synchronized Properties getProperties() {
    String 	props;

    if (m_Properties == null) {
      try {
	props = getClass().getName().replaceAll("\\.", "/") + ".props";
	m_Properties = Properties.read(props);
      }
      catch (Exception e) {
	m_Properties = new Properties();
      }
    }

    return m_Properties;
  }

  /**
   * Cleans up data structures, frees up memory.
   */
  public void cleanUp() {
    if (m_DialogColorGenerator != null) {
      m_DialogColorGenerator.dispose();
      m_DialogColorGenerator = null;
    }
    if (m_DialogMissingValueColor != null) {
      m_DialogMissingValueColor.dispose();
      m_DialogMissingValueColor = null;
    }
    if (m_DialogOverlay != null) {
      m_DialogOverlay.dispose();
      m_DialogOverlay = null;
    }
    if (m_DialogSelectionProcessor != null) {
      m_DialogSelectionProcessor.dispose();
      m_DialogSelectionProcessor = null;
    }
    if (m_DialogFilter != null) {
      m_DialogFilter.dispose();
      m_DialogFilter = null;
    }
  }
}
