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
 * SimpleSavitzkyGolayTest.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */
package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;
import adams.data.spectrum.Spectrum;

/**
 * Test class for the SimpleSavitzkyGolay filter. Run from the command line with: <br><br>
 * java knir.data.filter.SimpleSavitzkyGolayTest
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SimpleSavitzkyGolayTest
  extends AbstractSpectrumFilterTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public SimpleSavitzkyGolayTest(String name) {
    super(name);
  }

  /**
   * Returns the configured filter.
   *
   * @return		the filter
   */
  public AbstractFilter<Spectrum> getFilter() {
    return new SimpleSavitzkyGolay();
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
	"872280-nir.spec"
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  protected AbstractFilter[] getRegressionSetups() {
    SimpleSavitzkyGolay[]	result;

    result = new SimpleSavitzkyGolay[2];

    result[0] = new SimpleSavitzkyGolay();

    result[1] = new SimpleSavitzkyGolay();
    result[1].setWindowSize(15);

    return result;
  }

  /**
   * Tests whether the number of wave numbers is reduced by 1.
   */
  public void testNumWaveNumbers() {
    Spectrum		data;
    Spectrum		processed;
    SimpleSavitzkyGolay	filter;

    data = load("872280-nir.spec");
    assertNotNull("Could not load data for test", data);

    filter    = new SimpleSavitzkyGolay();
    processed = process(data, filter);
    assertNotNull("Failed to process data?", processed);

    assertEquals("Number of wave numbers not reduced by size of window", data.size() - filter.getWindowSize() + 1, processed.size());
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(SimpleSavitzkyGolayTest.class);
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
