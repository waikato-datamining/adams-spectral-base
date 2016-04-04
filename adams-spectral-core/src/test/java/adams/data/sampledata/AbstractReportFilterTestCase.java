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
 * AbstractReportFilterTestCase.java
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package adams.data.sampledata;

import adams.data.report.AbstractReportFilter;
import adams.data.spectrum.Spectrum;
import adams.test.AbstractTestHelper;
import adams.test.SpectralTestHelper;

/**
 * Ancestor for filter test cases.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1735 $
 */
public abstract class AbstractReportFilterTestCase
  extends adams.data.report.AbstractReportFilterTestCase<AbstractReportFilter, Spectrum> {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public AbstractReportFilterTestCase(String name) {
    super(name);
  }

  /**
   * Returns the name of the database properties file to use.
   *
   * @return		the filename
   */
  @Override
  protected String getDatabasePropertiesFile() {
    return "adams/test/SpectralTestDatabase.props";
  }

  /**
   * Returns the test helper class to use.
   *
   * @return		the helper class instance
   */
  @Override
  protected AbstractTestHelper newTestHelper() {
    return new SpectralTestHelper(this, "adams/data/sampledata/data");
  }

  /**
   * Returns the ignored line indices to use in the regression test.
   *
   * @return		the setups
   */
  @Override
  protected int[] getRegressionIgnoredLineIndices() {
    return new int[]{0};
  }
}
