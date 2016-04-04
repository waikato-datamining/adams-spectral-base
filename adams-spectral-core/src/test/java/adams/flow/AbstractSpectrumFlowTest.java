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
 * AbstractSpectrumFlowTest.java
 * Copyright (C) 2009 University of Waikato
 */

package adams.flow;

import adams.test.AbstractTestHelper;
import adams.test.SpectralTestHelper;

/**
 * Abstract Test class for flow actors.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public abstract class AbstractSpectrumFlowTest
  extends AbstractFlowTest {

  /**
   * Constructs the <code>AbstractFlowTest</code>. Called by subclasses.
   *
   * @param name the name of the test class
   */
  public AbstractSpectrumFlowTest(String name) {
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
    return new SpectralTestHelper(this, "adams/flow/data");
  }
}
