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
 * AbstractIDProviderPanel.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.gui.tools.idprovider;

import adams.core.Properties;
import adams.core.logging.Logger;
import adams.core.logging.LoggingHelper;
import adams.core.logging.LoggingSupporter;
import adams.gui.core.BasePanel;

import java.util.logging.Level;

/**
 * Panel that generates IDs.
 */
public abstract class AbstractIDProviderPanel
  extends BasePanel
  implements LoggingSupporter {

  private static final long serialVersionUID = 8422417141327768407L;

  /** the owner. */
  protected IDConsumer m_Owner;

  /** the logger. */
  protected Logger m_Logger;

  /**
   * Initializes the panel with the owner.
   *
   * @param owner	the owning panel
   */
  public AbstractIDProviderPanel(IDConsumer owner) {
    super();
    m_Owner = owner;
    initialize();
    initGUI();
    finishInit();
  }

  /**
   * Returns the owning panel.
   *
   * @return		the panel
   */
  public IDConsumer getOwner() {
    return m_Owner;
  }

  /**
   * Returns the name of the panel.
   *
   * @return		the name
   */
  public abstract String getPanelName();

  /**
   * Returns the parameters as options.
   *
   * @return  		the options
   */
  public abstract Properties getPanelProperties();

  /**
   * Returns whether IDs are currently being determined.
   *
   * @return		true if determining IDs
   */
  public abstract boolean isWorking();

  /**
   * Updates the state of the buttons.
   */
  public abstract void updateButtons();

  /**
   * Returns the logger in use.
   *
   * @return		the logger
   */
  public synchronized Logger getLogger() {
    if (m_Logger == null)
      m_Logger = LoggingHelper.getLogger(getClass());
    return m_Logger;
  }

  /**
   * Returns whether logging is enabled.
   *
   * @return		true if at least {@link Level#INFO}
   */
  public boolean isLoggingEnabled() {
    return LoggingHelper.isAtLeast(getLogger(), Level.INFO);
  }
}
