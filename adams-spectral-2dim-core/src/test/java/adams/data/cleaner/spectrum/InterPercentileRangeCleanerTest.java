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
 * InterPercentileRangeCleanerTest.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */
package adams.data.cleaner.spectrum;

import adams.core.base.BaseRegExp;
import adams.db.SpectrumConditionsMulti;
import adams.env.Environment;
import adams.test.TmpFile;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the InterPercentileRangeCleaner filter. Run from the command line with: <br><br>
 * java adams.data.cleaner.spectrum.InterPercentileRangeCleanerTest
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class InterPercentileRangeCleanerTest
  extends AbstractCleanerTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public InterPercentileRangeCleanerTest(String name) {
    super(name);
  }

  /**
   * Called by JUnit before each test method.
   *
   * @throws Exception if an error occurs.
   */
  protected void setUp() throws Exception {
    super.setUp();

    m_TestHelper.deleteFileFromTmp("iprc");
  }

  /**
   * Returns the filenames (without path) of the input data files to use
   * in the regression test.
   *
   * @return		the filenames
   */
  protected String[][] getRegressionInputFiles() {
    return new String[][]{
	{
	  "871553-nir.spec",
	  "871562-nir.spec",
	  "871563-nir.spec",
	  "871598-nir.spec",
	  "871609-nir.spec"
	},
	{
	  "871553-nir.spec",
	  "871562-nir.spec",
	  "871563-nir.spec",
	  "871598-nir.spec",
	  "871609-nir.spec"
	}
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  protected AbstractCleaner[] getRegressionSetups() {
    InterPercentileRangeCleaner[]	result;
    SpectrumConditionsMulti		cond;

    result = new InterPercentileRangeCleaner[2];

    cond = new SpectrumConditionsMulti();
    cond.setLimit(100);
    cond.setFormat(new BaseRegExp("NIR"));
    cond.setSortOnInsertTimestamp(true);
    result[0] = new InterPercentileRangeCleaner();
    result[0].setConditions(cond);
    result[0].setSerializationFile(new TmpFile("iprc"));

    // same setup for testing the serialized instance of the cleaner
    result[1] = (InterPercentileRangeCleaner) result[0].shallowCopy();

    return result;
  }

  /**
   * For further cleaning up after the regression tests.
   */
  protected void cleanUpAfterRegression() {
    super.cleanUpAfterRegression();

    m_TestHelper.deleteFileFromTmp("iprc");
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(InterPercentileRangeCleanerTest.class);
  }

  /**
   * Runs the test from commandline.
   *
   * @param args	ignored
   */
  public static void main(String[] args) {
    Environment.setEnvironmentClass(Environment.class);
    runTest(suite());
  }
}
