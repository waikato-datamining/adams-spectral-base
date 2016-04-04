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
 * DeleteSpectrumPanel.java
 * Copyright (C) 2009-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.tools;

import adams.core.Utils;
import adams.db.DatabaseConnection;
import adams.gui.core.BasePanel;
import adams.gui.core.GUIHelper;
import adams.gui.core.MenuBarProvider;
import adams.gui.scripting.DeleteData;
import adams.gui.scripting.ScriptingCommand;
import adams.gui.scripting.ScriptingCommandCode;
import adams.gui.scripting.SpectralScriptingEngine;
import adams.gui.selection.SelectSpectrumPanel;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * A panel for deleting spectra.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1619 $
 */
public class DeleteSpectrumPanel
  extends BasePanel
  implements MenuBarProvider {

  /** for serialization. */
  private static final long serialVersionUID = -8615718902362054580L;

  /** the menu bar, if used. */
  protected JMenuBar m_MenuBar;

  /** the panel for selecting the spectra. */
  protected SelectSpectrumPanel m_PanelSpectrum;

  /** the button for executing the deletion. */
  protected JButton m_ButtonDelete;

  /** the button for closing the dialog/frame. */
  protected JButton m_ButtonClose;

  /**
   * Initializes the widgets.
   */
  @Override
  public void initGUI() {
    JPanel	panel;

    super.initGUI();

    setLayout(new BorderLayout());

    // for selecting the spectra
    m_PanelSpectrum = new SelectSpectrumPanel();
    m_PanelSpectrum.setMultipleSelection(true);
    m_PanelSpectrum.addListSelectionListener(e -> updateButtons());
    add(m_PanelSpectrum, BorderLayout.CENTER);

    // the buttons
    panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    add(panel, BorderLayout.SOUTH);

    m_ButtonDelete = new JButton("Delete");
    m_ButtonDelete.setMnemonic('D');
    m_ButtonDelete.addActionListener(e -> {
      delete();
      closeParent();
    });
    panel.add(m_ButtonDelete);

    m_ButtonClose = new JButton("Close");
    m_ButtonClose.setMnemonic('l');
    m_ButtonClose.addActionListener(e -> closeParent());
    panel.add(m_ButtonClose);

    updateButtons();
  }

  /**
   * Removes the listed spectra.
   */
  protected void delete() {
    Integer[]		ids;
    final String	idsStr;

    ids    = m_PanelSpectrum.getItems();
    idsStr = Utils.flatten(ids, ",");
    // DatabaseConnection.getSingleton() is OK, since issued from the main GUI
    SpectralScriptingEngine.getSingleton(DatabaseConnection.getSingleton()).add(
	new ScriptingCommand(
	    this,
	    DeleteData.ACTION + " " + idsStr,
	    new ScriptingCommandCode() {
	      public void execute() {
		GUIHelper.showInformationMessage(
		    DeleteSpectrumPanel.this, "Spectra deleted!\n" + idsStr);
	      }
	    }));
  }

  /**
   * Updates the enabled state of the buttons.
   */
  protected void updateButtons() {
    Integer[]	ids;

    ids = m_PanelSpectrum.getItems();

    m_ButtonDelete.setEnabled((ids != null) && (ids.length > 0));
    m_ButtonClose.setEnabled(true);
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

    if (m_MenuBar == null) {
      result = new JMenuBar();

      // Data
      menu = new JMenu("Data");
      result.add(menu);
      menu.setMnemonic('D');
      menu.addChangeListener(e -> updateMenu());

      // File/Close
      menuitem = new JMenuItem("Close");
      menu.addSeparator();
      menu.add(menuitem);
      menuitem.setMnemonic('C');
      menuitem.setAccelerator(GUIHelper.getKeyStroke("ctrl pressed Q"));
      menuitem.setIcon(GUIHelper.getIcon("exit.png"));
      menuitem.addActionListener(e -> closeParent());

      // update menu
      m_MenuBar = result;
      updateMenu();
    }
    else {
      result = m_MenuBar;
    }

    return result;
  }

  /**
   * updates the enabled state of the menu items.
   */
  protected void updateMenu() {
    if (m_MenuBar == null)
      return;
  }
}
