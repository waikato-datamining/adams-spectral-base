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
 * SpectrumTMySQL.java
 * Copyright (C) 2016-2025 University of Waikato, Hamilton, NZ
 */

package adams.db.mysql;

import adams.core.Utils;
import adams.core.logging.LoggingHelper;
import adams.db.AbstractDatabaseConnection;
import adams.db.SQLUtils;
import adams.db.SampleDataIntf;
import adams.db.SpectrumIDConditions;
import adams.db.SpectrumIntf;
import adams.db.TableManager;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * MySQL implementation.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumT
  extends adams.db.generic.SpectrumT {

  private static final long serialVersionUID = -1371114857113756774L;

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
   * returns all the specified fields in the database, separated by TABs.
   *
   * @param fields	the field names
   * @param tables 	the involved tables
   * @param where	the where clause, can be null
   * @param cond	the conditions for the retrieval
   * @return		list of tab-separated values
   */
  public List<String> getValues(String[] fields, String tables, String where, SpectrumIDConditions cond) {
    ResultSet 		rs;
    List<String>	result;
    String		sql;
    int			i;
    String		line;
    boolean		hasSampleID;
    boolean		hasFormat;

    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": fields=" + Utils.arrayToString(fields) + ", tables=" + tables + ", where=" + where + ", cond=" + cond);

    result = new ArrayList<>();
    rs     = null;
    if (where == null)
      where = "";

    hasSampleID = !cond.getSampleIDRegExp().isEmpty() && !cond.getSampleIDRegExp().isMatchAll();
    hasFormat   = !cond.getFormat().isEmpty() && !cond.getFormat().isMatchAll();

    // sample name
    if (hasSampleID) {
      if (!where.isEmpty())
	where += " AND";
      where += " " + m_Queries.regexp("SAMPLEID", cond.getSampleIDRegExp());
    }

    // data format
    if (hasFormat) {
      if (!where.isEmpty())
	where += " AND";
      where += " " + m_Queries.regexp("FORMAT", cond.getFormat());
    }

    // limit
    if (cond.getLimit() > -1)
      where += " " + m_Queries.limit(cond.getLimit());

    if (where.isEmpty())
      where = null;
    else
      where = where.trim();

    try {
      sql = "";
      for (i = 0; i < fields.length; i++) {
	if (i > 0)
	  sql += ", ";
	sql += fields[i];
      }
      rs = select(sql, tables, where);
      if (rs == null)
	return result;

      while (rs.next()) {
	line = "";
	for (i = 0; i < fields.length; i++) {
	  if (i > 0)
	    line += "\t";
	  line += rs.getObject(fields[i]);
	}
	result.add(line);
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to get values", e);
    }
    finally{
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
