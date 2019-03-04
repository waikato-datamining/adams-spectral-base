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
 * SpectralDbBackend.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.db.autodetect;

import adams.db.AbstractDatabaseConnection;
import adams.db.AbstractSpectralDbBackend;
import adams.db.JDBC;
import adams.db.SampleDataIntf;
import adams.db.SpectrumIntf;

/**
 * Auto-detect Spectral backend. Detects: MySQL, PostgreSQL, SQLite.
 * Otherwise uses generic SpectrumT/SampleDataT.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectralDbBackend
  extends AbstractSpectralDbBackend {

  private static final long serialVersionUID = -8233202811908896313L;

  @Override
  public String globalInfo() {
    return "Auto-detect Spectral backend.\n"
      + "Detects: MySQL, PostgreSQL, SQLite.\n"
      + "Otherwise uses generic SpectrumT/SampleDataT.";
  }

  /**
   * Returns the handler for the spectrum table.
   *
   * @param conn	the database connection
   * @return		the handler
   */
  @Override
  public SpectrumIntf getSpectrum(AbstractDatabaseConnection conn) {
    if (JDBC.isMySQL(conn))
      return adams.db.mysql.SpectrumT.getSingleton(conn);
    else if (JDBC.isPostgreSQL(conn))
      return adams.db.postgresql.SpectrumT.getSingleton(conn);
    else if (JDBC.isSQLite(conn))
      return adams.db.sqlite.SpectrumT.getSingleton(conn);
    else
      return adams.db.generic.SpectrumT.getSingleton(conn);
  }

  /**
   * Returns the handler for the sample data table.
   *
   * @param conn	the database connection
   * @return		the handler
   */
  @Override
  public SampleDataIntf getSampleData(AbstractDatabaseConnection conn) {
    if (JDBC.isMySQL(conn))
      return adams.db.mysql.SampleDataT.getSingleton(conn);
    else if (JDBC.isPostgreSQL(conn))
      return adams.db.postgresql.SampleDataT.getSingleton(conn);
    else if (JDBC.isSQLite(conn))
      return adams.db.sqlite.SampleDataT.getSingleton(conn);
    else
      return adams.db.generic.SampleDataT.getSingleton(conn);
  }
}
