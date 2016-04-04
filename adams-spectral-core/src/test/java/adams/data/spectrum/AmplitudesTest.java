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
 * AmplitudesTest.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package adams.data.spectrum;

import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;
import adams.data.io.input.AbstractSpectrumReader;
import adams.data.io.input.SimpleSpectrumReader;

/**
 * Test class for the Amplitudes feature generator. Run from the command line with: <br><br>
 * java adams.data.spectrum.flattener.AmplitudesTest
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class AmplitudesTest
  extends AbstractSpectrumFeatureGeneratorTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public AmplitudesTest(String name) {
    super(name);
  }

  /**
   * Returns the filenames (without path) of the input data files to use
   * in the regression test.
   *
   * @return		the filenames
   */
  @Override
  protected String[] getRegressionInputFiles() {
    return new String[]{
	"871564-nir.spec",
	"871564-nir.spec",
    };
  }

  /**
   * Returns the spectrum filereaders to use in the regression test for 
   * loading the spectrum files.
   *
   * @return		the readers
   */
  @Override
  protected AbstractSpectrumReader[] getRegressionInputReaders() {
    return new AbstractSpectrumReader[]{
	new SimpleSpectrumReader(),
	new SimpleSpectrumReader(),
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  @Override
  protected AbstractSpectrumFeatureGenerator[] getRegressionSetups() {
    Amplitudes[]	result;
    
    result = new Amplitudes[2];
    result[0] = new Amplitudes();
    result[1] = new Amplitudes();
    result[1].setAddWaveNumber(true);
    
    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(AmplitudesTest.class);
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
