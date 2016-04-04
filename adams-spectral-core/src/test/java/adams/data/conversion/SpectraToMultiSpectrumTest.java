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
 * SpectraToMultiSpectrumTest.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */

package adams.data.conversion;

import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;
import adams.data.spectrum.Spectrum;

/**
 * Tests the SpectraToMultiSpectrum conversion.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1487 $
 */
public class SpectraToMultiSpectrumTest
  extends AbstractSpectralConversionTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public SpectraToMultiSpectrumTest(String name) {
    super(name);
  }

  /**
   * Returns the input data to use in the regression test.
   *
   * @return		the objects
   */
  @Override
  protected Object[] getRegressionInput() {
    Spectrum[][]	result;
    
    result = new Spectrum[2][3];
    result[0][0] = new Spectrum();
    result[0][0].setID("1");
    result[0][1] = new Spectrum();
    result[0][1].setID("2");
    result[0][2] = new Spectrum();
    result[0][2].setID("3");
    result[1][0] = new Spectrum();
    result[1][0].setID("1");
    result[1][1] = new Spectrum();
    result[1][1].setID("1");
    result[1][2] = new Spectrum();
    result[1][2].setID("1");

    return result;
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  @Override
  protected Conversion[] getRegressionSetups() {
    SpectraToMultiSpectrum[]	result;

    result    = new SpectraToMultiSpectrum[2];
    result[0] = new SpectraToMultiSpectrum();
    result[1] = new SpectraToMultiSpectrum();

    return result;
  }

  /**
   * Returns the ignored line indices to use in the regression test.
   *
   * @return		the setups
   */
  @Override
  protected int[] getRegressionIgnoredLineIndices() {
    return new int[0];
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(SpectraToMultiSpectrumTest.class);
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
