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
 * SampleDataTSQLite.java
 * Copyright (C) 2016-2019 University of Waikato, Hamilton, NZ
 */

package adams.db.sqlite;

import adams.db.AbstractDatabaseConnection;
import adams.db.SampleDataT;

/**
 * SQLite implementation.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SampleDataTSQLite
  extends SampleDataT {

  private static final long serialVersionUID = 5905403844404856859L;

  /**
   * Constructor.
   *
   * @param dbcon the database context this table is used in
   */
  public SampleDataTSQLite(AbstractDatabaseConnection dbcon) {
    super(dbcon);
  }
}
