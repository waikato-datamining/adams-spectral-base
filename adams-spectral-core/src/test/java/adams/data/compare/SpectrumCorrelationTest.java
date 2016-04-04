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
 * SpectrumCorrelationTest.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package adams.data.compare;

import adams.core.logging.LoggingLevel;
import adams.env.Environment;
import adams.test.TmpFile;
import junit.framework.Test;
import junit.framework.TestSuite;
import adams.data.io.input.SimpleSpectrumReader;

/**
 * Test class for the SpectrumCorrelation object comparison. Run from the command line with: <br><br>
 * java knir.data.compare.SpectrumCorrelationTest
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SpectrumCorrelationTest
  extends AbstractSpectralObjectCompareTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public SpectrumCorrelationTest(String name) {
    super(name);
  }

  /**
   * Returns the object arrays to process in regression.
   *
   * @return		the arrays
   */
  @Override
  protected Object[][] getRegressionArrays() {
    Object[][]			result;
    SimpleSpectrumReader	reader;
    int				i;
    
    result = new Object[1][2];

    m_TestHelper.copyResourceToTmp("1000483.spec");
    m_TestHelper.copyResourceToTmp("1000484.spec");

    reader = new SimpleSpectrumReader();
    reader.setLoggingLevel(LoggingLevel.FINE);
    reader.setKeepFormat(true);
    for (i = 0; i < result.length; i++) {
      reader.setInput(new TmpFile("1000483.spec"));
      result[i][0] = reader.read().get(0);
      reader.setInput(new TmpFile("1000484.spec"));
      result[i][1] = reader.read().get(0);
    }

    m_TestHelper.deleteFileFromTmp("1000483.spec");
    m_TestHelper.deleteFileFromTmp("1000484.spec");

    return result;
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  @Override
  protected AbstractObjectCompare[] getRegressionSetups() {
    SpectrumCorrelation[]	result;

    result    = new SpectrumCorrelation[2];
    result[0] = new SpectrumCorrelation();
    result[1] = new SpectrumCorrelation();
    result[1].setMinimum(0.99);

    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(SpectrumCorrelationTest.class);
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
