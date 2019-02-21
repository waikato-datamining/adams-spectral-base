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
 * SpectrumTPostgreSQL.java
 * Copyright (C) 2016-2019 University of Waikato, Hamilton, NZ
 */

package adams.db.postgresql;

import adams.db.AbstractDatabaseConnection;
import adams.db.SpectrumT;

/**
 * PostgreSQL implementation.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumTPostgreSQL
  extends SpectrumT {

  private static final long serialVersionUID = 7003479237078308491L;

  /**
   * Constructor - initalise with database connection.
   *
   * @param dbcon the database context this table is used in
   */
  public SpectrumTPostgreSQL(AbstractDatabaseConnection dbcon) {
    super(dbcon);
  }
}
