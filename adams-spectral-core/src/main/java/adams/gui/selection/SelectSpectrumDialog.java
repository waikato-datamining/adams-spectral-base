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
 * SelectSpectrumDialog.java
 * Copyright (C) 2008-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.selection;

import adams.db.AbstractDatabaseConnection;

import java.awt.Dialog;
import java.awt.Frame;

/**
 * Dialog for loading spectrum data from the database and displaying it.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public class SelectSpectrumDialog
  extends AbstractDatabaseSelectionDialog<Integer,SelectSpectrumPanel> {

  /** for serialization. */
  private static final long serialVersionUID = 5094069941150471500L;

  /**
   * Creates a modal dialog.
   *
   * @param owner	the owning dialog
   */
  public SelectSpectrumDialog(Dialog owner) {
    this(owner, "Load data");
  }

  /**
   * Creates a modal dialog.
   *
   * @param owner	the owning dialog
   * @param title	the title of the dialog
   */
  public SelectSpectrumDialog(Dialog owner, String title) {
    super(owner, title);
  }

  /**
   * Creates a modal dialog.
   *
   * @param owner	the owning frame
   */
  public SelectSpectrumDialog(Frame owner) {
    this(owner, "Load data");
  }

  /**
   * Creates a modal dialog.
   *
   * @param owner	the owning frame
   * @param title	the title of the dialog
   */
  public SelectSpectrumDialog(Frame owner, String title) {
    super(owner, title);
  }

  /**
   * Returns a new instance of the panel to use in the GUI.
   *
   * @return		the panel to use
   */
  protected SelectSpectrumPanel newPanel() {
    return new SelectSpectrumPanel();
  }

  /**
   * Returns the currently used database connection object, can be null.
   *
   * @return		the current object
   */
  public AbstractDatabaseConnection getDatabaseConnection() {
    return m_Panel.getDatabaseConnection();
  }

  /**
   * Sets the database connection object to use.
   *
   * @param value	the object to use
   */
  public void setDatabaseConnection(AbstractDatabaseConnection value) {
    m_Panel.setDatabaseConnection(value);
  }
}
