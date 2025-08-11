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
 * SampleDataT.java
 * Copyright (C) 2025 University of Waikato, Hamilton, NZ
 */

package adams.db.mssql;

import adams.db.AbstractDatabaseConnection;
import adams.db.SampleDataIntf;
import adams.db.SpectrumIntf;
import adams.db.TableManager;

/**
 * H2 implementation.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SampleDataT
  extends adams.db.generic.SampleDataT {

  private static final long serialVersionUID = 5345945766136653603L;

  /** the table manager. */
  protected static TableManager<SampleDataT> m_TableManager;

  /**
   * Constructor.
   *
   * @param dbcon the database context this table is used in
   */
  public SampleDataT(AbstractDatabaseConnection dbcon) {
    super(dbcon);
  }

  /**
   * Returns the corresponding Spectrum handler.
   *
   * @return		the corresponding handler
   */
  public SpectrumIntf getSpectrumHandler() {
    return SpectrumT.getSingleton(getDatabaseConnection());
  }

  /**
   * Initializes the table. Used by the "InitializeTables" tool.
   *
   * @param dbcon	the database context
   */
  public static synchronized void initTable(AbstractDatabaseConnection dbcon) {
    getSingleton(dbcon).init();
  }

  /**
   * Returns the singleton of the table (active).
   *
   * @param dbcon	the database connection to get the singleton for
   * @return		the singleton
   */
  public static synchronized SampleDataIntf getSingleton(AbstractDatabaseConnection dbcon) {
    if (m_TableManager == null)
      m_TableManager = new TableManager<>(TABLE_NAME, dbcon.getOwner());
    if (!m_TableManager.has(dbcon))
      m_TableManager.add(dbcon, new SampleDataT(dbcon));

    return m_TableManager.get(dbcon);
  }
}
