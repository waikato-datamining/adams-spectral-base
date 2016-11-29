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
 * ScaleTest.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */
package adams.data.spectrumfilter;

import adams.data.filter.Filter;
import adams.data.spectrum.Spectrum;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the Scale filter. Run from the command line with: <br><br>
 * java adams.data.spectrumfilter.ScaleTest
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class ScaleTest
  extends AbstractSpectrumFilterTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public ScaleTest(String name) {
    super(name);
  }

  /**
   * Returns the configured filter.
   *
   * @return		the filter
   */
  public Filter<Spectrum> getFilter() {
    return new Scale();
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
	"872280-nir.spec",
	"872280-nir.spec",
	"872280-nir.spec"
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  protected Filter[] getRegressionSetups() {
    Scale[]	result;

    result = new Scale[4];

    result[0] = new Scale();

    result[1] = new Scale();
    result[1].setMinAmplitude(5);

    result[2] = new Scale();
    result[2].setMaxAmplitude(200);

    result[3] = new Scale();
    result[3].setMinAmplitude(5);
    result[3].setMaxAmplitude(200);

    return result;
  }

  /**
   * Tests whether the range of amplitudes is really between the min/max values.
   */
  public void testRange() {
    Spectrum	data;
    Spectrum	processed;
    Scale	filter;

    data = load("872280-nir.spec");
    assertNotNull("Could not load data for test", data);

    filter = new Scale();
    filter.setMinAmplitude(10);
    filter.setMaxAmplitude(100);
    processed = process(data, filter);
    assertNotNull("Failed to process data?", processed);

    assertTrue("Minimum boundary violated", processed.getMinAmplitude().getAmplitude() >= filter.getMinAmplitude());
    assertTrue("Maximum boundary violated", processed.getMaxAmplitude().getAmplitude() <= filter.getMaxAmplitude());
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(ScaleTest.class);
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
