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
 * AbstractDatabaseFilter.java
 * Copyright (C) 2009-2026 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractDatabaseConnectionFilter;
import adams.data.spectrum.Spectrum;
import adams.db.AbstractDatabaseConnection;
import adams.db.DatabaseConnection;
import adams.db.SampleDataF;
import adams.db.SpectrumF;

/**
 * Abstract superclass for filters that operate on the database.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractDatabaseFilter
  extends AbstractDatabaseConnectionFilter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 1733701959783280281L;

  /**
   * Returns the default database connection.
   *
   * @return		the default database connection
   */
  protected AbstractDatabaseConnection getDefaultDatabaseConnection() {
    return DatabaseConnection.getSingleton();
  }

  /**
   * Returns the correct table object based on the filter's setup.
   *
   * @return		the table object
   */
  protected SpectrumF getSpectrumTable() {
    return SpectrumF.getSingleton(getDatabaseConnection());
  }

  /**
   * Returns the correct table object based on the filter's setup.
   *
   * @return		the table object
   */
  protected SampleDataF getSampleDataTable() {
    return SampleDataF.getSingleton(getDatabaseConnection());
  }
}
