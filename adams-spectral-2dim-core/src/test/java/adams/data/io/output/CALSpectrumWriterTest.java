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
 * CALSpectrumWriterTest.java
 * Copyright (C) 2010-2025 University of Waikato, Hamilton, New Zealand
 */
package adams.data.io.output;

import adams.core.base.BaseDateTime;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the CALSpectrumWriter data container writer. Run from the command line with: <br><br>
 * java adams.data.io.output.CALSpectrumWriterTest
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class CALSpectrumWriterTest
  extends AbstractSpectrumWriterTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public CALSpectrumWriterTest(String name) {
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
	"146048-NIR-FOSS.spec"
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  protected DataContainerWriter[] getRegressionSetups() {
    CALSpectrumWriter[]	result;

    result = new CALSpectrumWriter[1];
    result[0] = new CALSpectrumWriter();
    result[0].setTimestamp(new BaseDateTime("2010-01-02 03:04:05"));

    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(CALSpectrumWriterTest.class);
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
