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
 * MultiSpectrumViewerPanel.java
 * Copyright (C) 2011-2013 University of Waikato, Hamilton, New Zealand
 */
package adams.gui.visualization.spectrum;

import adams.core.io.PlaceholderFile;
import adams.data.io.input.AbstractSpectrumReader;
import adams.data.io.input.SimpleSpectrumReader;
import adams.data.io.output.AbstractSpectrumWriter;
import adams.data.spectrum.Spectrum;
import adams.gui.chooser.SpectrumFileChooser;
import adams.gui.core.BasePanel;
import adams.gui.core.BaseTabbedPane;
import adams.gui.core.GUIHelper;
import adams.gui.core.MenuBarProvider;
import adams.gui.goe.GenericObjectEditorDialog;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * A panel for viewing spectra in tabs.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1969 $
 */
public class MultiSpectrumPanel
  extends BasePanel
  implements MenuBarProvider {

  /** for serialization. */
  private static final long serialVersionUID = -2974452961562600571L;

  /** the file chooser to use for loading files. */
  protected SpectrumFileChooser m_FileChooser;

  /** for displaying the spectra in tabs. */
  protected BaseTabbedPane m_TabbedPane;

  /** for choosing an appropriate reader. */
  protected GenericObjectEditorDialog m_GOEDialog;

  /** the menu bar. */
  protected JMenuBar m_MenuBar;

  /** the "open" file menu item. */
  protected JMenuItem m_MenuItemFileOpen;

  /** the "save as" file menu item. */
  protected JMenuItem m_MenuItemFileSaveAs;

  /** the "exit" file menu item. */
  protected JMenuItem m_MenuItemFileExit;

  /** the "new tab" view menu item. */
  protected JMenuItem m_MenuItemViewNewTab;

  /** the "close tab" view menu item. */
  protected JMenuItem m_MenuItemViewCloseTab;

  /** the counter for the tabs. */
  protected int m_TabCounter;

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_FileChooser = new SpectrumFileChooser();
  }

  /**
   * Initializes the widgets.
   */
  @Override
  protected void initGUI() {
    super.initGUI();

    setLayout(new BorderLayout());

    m_TabbedPane = new BaseTabbedPane();
    m_TabbedPane.setCloseTabsWithMiddleMouseButton(true);
    add(m_TabbedPane, BorderLayout.CENTER);
  }

  /**
   * Returns the GOE dialog.
   *
   * @return		the dialog
   */
  protected synchronized GenericObjectEditorDialog getGOEDialog() {
    if (m_GOEDialog == null) {
      if (getParentDialog() != null)
	m_GOEDialog = new GenericObjectEditorDialog(getParentDialog());
      else
	m_GOEDialog = new GenericObjectEditorDialog(getParentFrame());
      m_GOEDialog.getGOEEditor().setCanChangeClassInDialog(true);
      m_GOEDialog.getGOEEditor().setClassType(AbstractSpectrumReader.class);
      m_GOEDialog.getGOEEditor().setValue(new SimpleSpectrumReader());
      m_GOEDialog.setModalityType(ModalityType.DOCUMENT_MODAL);
    }

    return m_GOEDialog;
  }

  /**
   * Initializes the menubar.
   */
  public JMenuBar getMenuBar() {
    JMenu	menu;
    JMenuItem	menuitem;

    if (m_MenuBar == null) {
      m_MenuBar = new JMenuBar();

      // File menu
      menu = new JMenu("File");
      menu.setMnemonic('F');
      menu.addChangeListener(new ChangeListener() {
	public void stateChanged(ChangeEvent e) {
	  updateMenu();
	}
      });
      m_MenuBar.add(menu);

      // File/Open
      menuitem = new JMenuItem("Open...");
      menu.add(menuitem);
      menuitem.setMnemonic('O');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed O"));
      menuitem.setIcon(GUIHelper.getIcon("open.gif"));
      menuitem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  load();
	}
      });
      m_MenuItemFileOpen = menuitem;

      // File/Save as
      menuitem = new JMenuItem("Save as...");
      menu.add(menuitem);
      menuitem.setMnemonic('S');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed S"));
      menuitem.setIcon(GUIHelper.getIcon("save.gif"));
      menuitem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  saveAs();
	}
      });
      m_MenuItemFileSaveAs = menuitem;

      // File/Close
      menuitem = new JMenuItem("Close");
      menu.addSeparator();
      menu.add(menuitem);
      menuitem.setMnemonic('C');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed Q"));
      menuitem.setIcon(GUIHelper.getIcon("exit.png"));
      menuitem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  closeWindow();
	}
      });
      m_MenuItemFileExit = menuitem;

      // View menu
      menu = new JMenu("View");
      menu.setMnemonic('V');
      menu.addChangeListener(new ChangeListener() {
	public void stateChanged(ChangeEvent e) {
	  updateMenu();
	}
      });
      m_MenuBar.add(menu);

      // View/New tab
      menuitem = new JMenuItem("New tab");
      menu.add(menuitem);
      menuitem.setMnemonic('N');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed N"));
      menuitem.setIcon(GUIHelper.getIcon("new.gif"));
      menuitem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  newTab();
	}
      });
      m_MenuItemViewNewTab = menuitem;

      // View/Close tab
      menuitem = new JMenuItem("Close tab");
      menu.add(menuitem);
      menuitem.setMnemonic('C');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed W"));
      menuitem.setIcon(GUIHelper.getIcon("delete.gif"));
      menuitem.addActionListener(new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	  closeTab();
	}
      });
      m_MenuItemViewCloseTab = menuitem;
    }

    return m_MenuBar;
  }

  /**
   * Displays a dialog for opening a spectrum file.
   */
  protected void load() {
    int		retVal;

    retVal = m_FileChooser.showOpenDialog(this);
    if (retVal != SpectrumFileChooser.APPROVE_OPTION)
      return;

    load((AbstractSpectrumReader) m_FileChooser.getReader());
  }

  /**
   * Loads the spectra from the file.
   *
   * @param file	the file to load the spectra from
   * @param openNewTab	whether to open a new tab
   */
  public void load(PlaceholderFile file, boolean openNewTab) {
    AbstractSpectrumReader	reader;

    if (openNewTab)
      newTab();

    getGOEDialog().setLocationRelativeTo(this);
    getGOEDialog().setTitle("Opening " + file.getName() + " [" + file.getPath() + "]");
    getGOEDialog().setVisible(true);
    if (getGOEDialog().getResult() != GenericObjectEditorDialog.APPROVE_OPTION)
      return;

    reader = (AbstractSpectrumReader) m_GOEDialog.getCurrent();
    reader.setInput(file);
    load(reader);
  }

  /**
   * Loads the spectra using the configured reader.
   *
   * @param reader	the configured reader to use
   */
  protected void load(AbstractSpectrumReader reader) {
    SpectrumPanel		panel;
    List<Spectrum>		spectra;
    int				i;
    SpectrumContainerManager	manager;
    SpectrumContainer		cont;

    if (m_TabbedPane.getTabCount() == 0)
      newTab();
    panel   = getSelectedPanel();
    spectra = reader.read();
    manager = panel.getContainerManager();
    manager.startUpdate();
    for (i = 0; i < spectra.size(); i++) {
      cont = manager.newContainer(spectra.get(i));
      manager.add(cont);
    }
    manager.finishUpdate();
  }

  /**
   * Displays a dialog for saving a spectrum to a file.
   */
  protected void saveAs() {
    int				retVal;
    AbstractSpectrumWriter writer;
    SpectrumPanel		panel;
    Spectrum[]			spectra;

    panel = getSelectedPanel();
    if (panel == null)
      return;

    retVal = m_FileChooser.showSaveDialog(this);
    if (retVal != SpectrumFileChooser.APPROVE_OPTION)
      return;

    spectra = panel.getSelectedSpectra();
    writer  = (AbstractSpectrumWriter) m_FileChooser.getWriter();
    if (!writer.write(spectra[0]))
      GUIHelper.showErrorMessage(this, "Failed to save spectrum to " + writer.getOutput() + "!");
  }

  /**
   * Adds a new tab.
   */
  protected void newTab() {
    SpectrumPanel	panel;

    panel = new SpectrumPanel();
    panel.getContainerManager().setReloadable(false);
    m_TabCounter++;
    m_TabbedPane.addTab("View" + m_TabCounter, panel);
    m_TabbedPane.setSelectedIndex(m_TabbedPane.getTabCount() - 1);
  }

  /**
   * Closes the current tab.
   */
  protected void closeTab() {
    int		index;

    index = m_TabbedPane.getSelectedIndex();
    if (index == -1)
      return;

    m_TabbedPane.remove(index);
  }

  /**
   * Closes the frame/window.
   */
  protected void closeWindow() {
    closeParent();
  }

  /**
   * Returns the currently selected spectrum panel.
   *
   * @return		the spectrum panel
   */
  protected SpectrumPanel getSelectedPanel() {
    SpectrumPanel	result;
    int			index;

    result = null;

    index = m_TabbedPane.getSelectedIndex();
    if (index == -1)
      return result;

    result = (SpectrumPanel) m_TabbedPane.getComponent(index);

    return result;
  }

  /**
   * updates the enabled state of the menu items.
   */
  protected void updateMenu() {
    SpectrumPanel	panel;

    if (m_MenuBar == null)
      return;

    panel = getSelectedPanel();

    // File
    m_MenuItemFileOpen.setEnabled(true);
    m_MenuItemFileSaveAs.setEnabled((panel != null) && (panel.getSelectedIndices().length == 1));
    m_MenuItemFileExit.setEnabled(true);

    // View
    m_MenuItemViewNewTab.setEnabled(true);
    m_MenuItemViewCloseTab.setEnabled(panel != null);
  }
}
