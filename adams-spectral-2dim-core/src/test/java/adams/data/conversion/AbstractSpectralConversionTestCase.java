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
 * AbstractSpectralConversionTestCase.java
 * Copyright (C) 2011-2016 University of Waikato, Hamilton, New Zealand
 */
package adams.data.conversion;

import adams.test.AbstractTestHelper;
import adams.test.SpectralTestHelper;

/**
 * Ancestor for conversion test cases.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public abstract class AbstractSpectralConversionTestCase
  extends AbstractConversionTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public AbstractSpectralConversionTestCase(String name) {
    super(name);
  }

  /**
   * Returns the name of the database properties file to use.
   *
   * @return		the filename
   */
  protected String getDatabasePropertiesFile() {
    return "adams/test/SpectralTestDatabase.props";
  }

  /**
   * Returns the test helper class to use.
   *
   * @return		the helper class instance
   */
  protected AbstractTestHelper newTestHelper() {
    return new SpectralTestHelper(this, "adams/data/conversion/data");
  }
}
