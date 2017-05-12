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
 * SoilCaresScannerSpectrumReaderTest.java
 * Copyright (C) 2017 University of Waikato, Hamilton, New Zealand
 */
package adams.data.io.input;

import adams.data.io.input.SoilCaresScannerSpectrumReader.AmplitudeType;
import adams.data.io.input.SoilCaresScannerSpectrumReader.SpectrumType;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the SoilCaresScannerSpectrumReader data container. Run from the command line with: <br><br>
 * java adams.data.io.input.SoilCaresScannerSpectrumReaderTest
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SoilCaresScannerSpectrumReaderTest
  extends AbstractSpectrumReaderTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public SoilCaresScannerSpectrumReaderTest(String name) {
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
	"soilcares.zip",
	"soilcares.zip",
	"soilcares.zip",
	"soilcares.zip",
	"soilcares.zip",
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  protected AbstractSpectrumReader[] getRegressionSetups() {
    SoilCaresScannerSpectrumReader[]	result;

    result    = new SoilCaresScannerSpectrumReader[5];
    result[0] = new SoilCaresScannerSpectrumReader();
    result[1] = new SoilCaresScannerSpectrumReader();
    result[1].setSpectrumType(SpectrumType.BACK);
    result[2] = new SoilCaresScannerSpectrumReader();
    result[2].setSpectrumType(SpectrumType.CORR);
    result[3] = new SoilCaresScannerSpectrumReader();
    result[3].setAmplitudeType(AmplitudeType.INTERFEROGRAM);
    result[4] = new SoilCaresScannerSpectrumReader();
    result[4].setAmplitudeType(AmplitudeType.SPECTRUM);

    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(SoilCaresScannerSpectrumReaderTest.class);
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
