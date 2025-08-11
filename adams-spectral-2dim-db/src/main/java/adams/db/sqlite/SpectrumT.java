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
 * SpectrumT.java
 * Copyright (C) 2016-2019 University of Waikato, Hamilton, NZ
 */

package adams.db.sqlite;

import adams.data.report.Field;
import adams.data.spectrum.Spectrum;
import adams.db.AbstractDatabaseConnection;
import adams.db.SQLUtils;
import adams.db.SampleDataIntf;
import adams.db.SpectrumIntf;
import adams.db.TableManager;

import java.sql.ResultSet;
import java.util.logging.Level;

/**
 * SQLite implementation.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumT
  extends adams.db.generic.SpectrumT {

  private static final long serialVersionUID = -7283681599103306270L;

  /** the table manager. */
  protected static TableManager<SpectrumT> m_TableManager;

  /**
   * Constructor - initalise with database connection.
   *
   * @param dbcon the database context this table is used in
   */
  public SpectrumT(AbstractDatabaseConnection dbcon) {
    super(dbcon);
  }

  /**
   * Returns the corresponding SampleData handler.
   *
   * @return		the corresponding handler
   */
  public SampleDataIntf getSampleDataHandler() {
    return SampleDataT.getSingleton(getDatabaseConnection());
  }

  /**
   * Adds a spectrum to the database. Returns the created auto-id, and sets in
   * Spectrum.
   *
   * @param sp  	spectrum Header
   * @return  	new ID, or null if fail
   */
  public synchronized Integer add(Spectrum sp, boolean storeWaveNo) {
    Integer 		result;
    StringBuilder 	q;
    ResultSet 		rs;

    result = null;

    if (getDebug())
      getLogger().info("Entered add");

    q  = addQuery(sp, storeWaveNo);
    rs = null;
    try {
      execute(q.toString());
      if (getDebug())
        getLogger().info("try last_insert_rowid()");
      rs = doSelect(false, "last_insert_rowid();", null, null);

      if (rs != null){
	if (rs.next()) {
	  result = rs.getInt(1);
	  sp.setDatabaseID(result);

	  // store report (never overwrites, just adds additional fields)
	  if (sp.hasReport())
	    getSampleDataHandler().store(sp.getID(), sp.getReport(), false, true, new Field[0]);
        }
	else {
	  getLogger().severe("no last_insert_rowid");
	  result = null;
	}
      }
      else {
	getLogger().severe("null last_insert_rowid");
	result = null;
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to insert: " + sp,  e);
      result = null;
    }
    finally {
      SQLUtils.closeAll(rs);
    }

    return result;
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
  public static synchronized SpectrumIntf getSingleton(AbstractDatabaseConnection dbcon) {
    if (m_TableManager == null)
      m_TableManager = new TableManager<>(TABLE_NAME, dbcon.getOwner());
    if (!m_TableManager.has(dbcon))
      m_TableManager.add(dbcon, new SpectrumT(dbcon));

    return m_TableManager.get(dbcon);
  }
}
