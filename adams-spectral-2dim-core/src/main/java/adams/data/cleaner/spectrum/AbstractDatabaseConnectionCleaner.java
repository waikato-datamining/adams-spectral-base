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
 * AbstractDatabaseConnectionOutlierDetector.java
 * Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 */
package adams.data.cleaner.spectrum;

import adams.db.AbstractDatabaseConnection;
import adams.db.DatabaseConnectionHandler;
import adams.db.DatabaseConnection;

/**
 * Ancestor for cleaners that require a database connection.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public abstract class AbstractDatabaseConnectionCleaner
  extends AbstractCleaner
  implements DatabaseConnectionHandler {

  /** for serialization. */
  private static final long serialVersionUID = 8289862464396965026L;

  /** the database connection. */
  protected AbstractDatabaseConnection m_DatabaseConnection;

  /**
   * Initializes the members.
   */
  protected void initialize() {
    super.initialize();

    m_DatabaseConnection = DatabaseConnection.getSingleton();
  }

  /**
   * Returns the currently used database connection object, can be null.
   *
   * @return		the current object
   */
  public AbstractDatabaseConnection getDatabaseConnection() {
    return m_DatabaseConnection;
  }

  /**
   * Sets the database connection object to use.
   *
   * @param value	the object to use
   */
  public void setDatabaseConnection(AbstractDatabaseConnection value) {
    m_DatabaseConnection = value;
    reset();
  }

  /**
   * Updates the database connection in dependent schemes.
   * <br><br>
   * Default implementation does nothing.
   */
  protected void updateDatabaseConnection() {
  }
}
