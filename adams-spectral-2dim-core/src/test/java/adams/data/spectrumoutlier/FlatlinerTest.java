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
 * FlatlinerTest.java
 * Copyright (C) 2021 University of Waikato, Hamilton, New Zealand
 */
package adams.data.spectrumoutlier;

import adams.data.outlier.AbstractOutlierDetector;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the Flatliner outlier detector. Run from the command line with: <br><br>
 * java adams.data.spectrumoutlier.FlatlinerTest
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class FlatlinerTest
  extends AbstractSpectrumOutlierDetectorTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public FlatlinerTest(String name) {
    super(name);
  }

  /**
   * Returns the filenames (without path) of the input data files to use
   * in the regression test.
   *
   * @return		the filenames
   */
  protected String[] getRegressionInputFiles() {
    return new String[]{
	"872280-nir.spec",
	"flatliner.spec"
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  protected AbstractOutlierDetector[] getRegressionSetups() {
    Flatliner[]	result;

    result = new Flatliner[2];

    result[0] = new Flatliner();

    result[1] = new Flatliner();

    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(FlatlinerTest.class);
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
