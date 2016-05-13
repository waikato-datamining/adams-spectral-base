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
 * SubRangeTest.java
 * Copyright (C) 2010-2016 University of Waikato, Hamilton, New Zealand
 */
package adams.data.spectrumfilter;

import adams.data.filter.Filter;
import adams.data.spectrum.Spectrum;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the SubRange filter. Run from the command line with: <br><br>
 * java adams.data.spectrumfilter.SubRangeTest
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SubRangeTest
  extends AbstractSpectrumFilterTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public SubRangeTest(String name) {
    super(name);
  }

  /**
   * Returns the configured filter.
   *
   * @return		the filter
   */
  public Filter<Spectrum> getFilter() {
    return new SubRange();
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
	"872280-nir.spec",
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
    SubRange[]	result;

    result = new SubRange[8];

    result[0] = new SubRange();

    result[1] = new SubRange();
    result[1].setInvert(true);

    result[2] = new SubRange();
    result[2].setMinWaveNumber(20);

    result[3] = new SubRange();
    result[3].setMinWaveNumber(20);
    result[3].setInvert(true);

    result[4] = new SubRange();
    result[4].setMaxWaveNumber(500);

    result[5] = new SubRange();
    result[5].setMaxWaveNumber(500);
    result[5].setInvert(true);

    result[6] = new SubRange();
    result[6].setMinWaveNumber(20);
    result[6].setMaxWaveNumber(500);

    result[7] = new SubRange();
    result[7].setMinWaveNumber(20);
    result[7].setMaxWaveNumber(500);
    result[7].setInvert(true);

    return result;
  }

  /**
   * Tests whether the range of amplitudes is really between the min/max values.
   */
  public void testRange() {
    Spectrum	data;
    Spectrum	processed;
    SubRange	filter;

    data = load("872280-nir.spec");
    assertNotNull("Could not load data for test", data);

    filter = new SubRange();
    filter.setMinWaveNumber(100);
    filter.setMaxWaveNumber(200);
    processed = process(data, filter);
    assertNotNull("Failed to process data?", processed);

    assertTrue("Minimum boundary violated", processed.getMinWaveNumber().getWaveNumber() <= filter.getMinWaveNumber());
    assertTrue("Maximum boundary violated", processed.getMaxWaveNumber().getWaveNumber() >= filter.getMaxWaveNumber());
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(SubRangeTest.class);
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
