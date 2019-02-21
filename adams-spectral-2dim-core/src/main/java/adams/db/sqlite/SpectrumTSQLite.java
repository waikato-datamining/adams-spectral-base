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
 * SpectrumTSQLite.java
 * Copyright (C) 2016-2019 University of Waikato, Hamilton, NZ
 */

package adams.db.sqlite;

import adams.data.report.Field;
import adams.data.spectrum.Spectrum;
import adams.db.AbstractDatabaseConnection;
import adams.db.SpectrumT;

import java.sql.ResultSet;
import java.util.logging.Level;

/**
 * SQLite implementation.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumTSQLite
  extends SpectrumT {

  private static final long serialVersionUID = -7283681599103306270L;

  /**
   * Constructor - initalise with database connection.
   *
   * @param dbcon the database context this table is used in
   */
  public SpectrumTSQLite(AbstractDatabaseConnection dbcon) {
    super(dbcon);
  }

  /**
   * Adds a spectrum to the database. Returns the created auto-id, and sets in
   * Spectrum.
   *
   * @param sp  	spectrum Header
   * @return  	new ID, or null if fail
   */
  public synchronized Integer add(Spectrum sp) {
    Integer 		result;
    StringBuilder 	q;
    ResultSet 		rs;

    result = null;

    if (getDebug())
      getLogger().info("Entered add");

    q  = addQuery(sp);
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
	    getSampleDataT().store(sp.getID(), sp.getReport(), false, true, new Field[0]);
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
      closeAll(rs);
    }

    return result;
  }
}
