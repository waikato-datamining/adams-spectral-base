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
 * SpectrumTMySQL.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.db;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * MySQL implementation.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SpectrumTMySQL
  extends SpectrumT {

  private static final long serialVersionUID = -1371114857113756774L;

  /**
   * Constructor - initalise with database connection.
   *
   * @param dbcon the database context this table is used in
   */
  protected SpectrumTMySQL(AbstractDatabaseConnection dbcon) {
    super(dbcon);
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

    result = new ArrayList<>();
    rs     = null;
    if (where == null)
      where = "";

    hasSampleID = !cond.getSampleIDRegExp().isEmpty() && !cond.getSampleIDRegExp().isMatchAll();
    hasFormat   = !cond.getFormat().isEmpty() && !cond.getFormat().isMatchAll();

    // sample name
    if (hasSampleID) {
      if (where.length() > 0)
	where += " AND";
      where += " SAMPLEID RLIKE " + backquote(cond.getSampleIDRegExp());
    }

    // data format
    if (hasFormat) {
      if (where.length() > 0)
	where += " AND";
      where += " FORMAT RLIKE " + backquote(cond.getFormat());
    }

    // limit
    if (cond.getLimit() > -1)
      where += " LIMIT " + cond.getLimit();

    if (where.length() == 0)
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
      closeAll(rs);
    }

    return result;
  }
}
