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
 * Copyright (C) 2018-2019 University of Waikato, Hamilton, NZ
 */

package adams.db;

import adams.core.option.OptionHandler;

/**
 * Interface for classes that return actual implementations of the
 * processing database interfaces.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public interface SpectralDbBackend
  extends OptionHandler {

  /** the properties file containing the setup. */
  public final static String FILENAME = "SpectralDbBackend.props";

  /**
   * Returns the handler for the spectrum table.
   *
   * @param conn	the database connection
   * @return		the handler
   */
  public SpectrumIntf getSpectrum(AbstractDatabaseConnection conn);

  /**
   * Returns the handler for the sample data table.
   *
   * @param conn	the database connection
   * @return		the handler
   */
  public SampleDataIntf getSampleData(AbstractDatabaseConnection conn);
}
