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
 * Copyright (C) 2025 University of Waikato, Hamilton, NZ
 */

package adams.db.mssql;

import adams.db.AbstractDatabaseConnection;
import adams.db.AbstractSpectralDbBackend;
import adams.db.JDBC;
import adams.db.SampleDataIntf;
import adams.db.SpectrumIntf;

/**
 * MS SQL Server Spectral backend.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectralDbBackend
  extends AbstractSpectralDbBackend {

  private static final long serialVersionUID = -8233202811908896313L;

  @Override
  public String globalInfo() {
    return "MS SQL Server Spectral backend.";
  }

  /**
   * Returns the handler for the spectrum table.
   *
   * @param conn	the database connection
   * @return		the handler
   */
  @Override
  public SpectrumIntf getSpectrum(AbstractDatabaseConnection conn) {
    if (!JDBC.isMSSQL(conn))
      throw new IllegalStateException("Not an MS SQL Server JDBC URL: " + conn.getURL());
    return SpectrumT.getSingleton(conn);
  }

  /**
   * Returns the handler for the sample data table.
   *
   * @param conn	the database connection
   * @return		the handler
   */
  @Override
  public SampleDataIntf getSampleData(AbstractDatabaseConnection conn) {
    if (!JDBC.isMSSQL(conn))
      throw new IllegalStateException("Not an MS SQL Server JDBC URL: " + conn.getURL());
    return SampleDataT.getSingleton(conn);
  }
}
