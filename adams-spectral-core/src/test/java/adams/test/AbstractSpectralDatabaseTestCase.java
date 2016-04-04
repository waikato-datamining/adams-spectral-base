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
 * AbstractSpectralDatabaseTestCase.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.test;

/**
 * Ancestor for database test cases in the spectral framework.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class AbstractSpectralDatabaseTestCase
  extends AbstractDatabaseTestCase {

  public static final String TEST_DATABASE = "adams/test/SpectralTestDatabase.props";

  /**
   * Constructs the <code>AbstractDatabaseTest</code>. Called by subclasses.
   *
   * @param name the name of the test class
   */
  public AbstractSpectralDatabaseTestCase(String name) {
    super(name);
  }

  /**
   * Returns the name of the database properties file to use.
   *
   * @return		the filename
   */
  protected String getDatabasePropertiesFile() {
    return TEST_DATABASE;
  }
}
