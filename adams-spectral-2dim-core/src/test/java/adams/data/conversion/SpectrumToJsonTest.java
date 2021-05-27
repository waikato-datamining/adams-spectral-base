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
 * SpectrumToJsonTest.java
 * Copyright (C) 2018-2021 University of Waikato, Hamilton, NZ
 */

package adams.data.conversion;

import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.spectrum.Spectrum;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the SpectrumToJson conversion.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumToJsonTest
  extends AbstractSpectralConversionTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name the name of the test
   */
  public SpectrumToJsonTest(String name) {
    super(name);
  }

  /**
   * Returns the input data to use in the regression test.
   *
   * @return		the objects
   */
  @Override
  protected Object[] getRegressionInput() {
    Object[]	result;
    Spectrum	sp;

    try {
      sp = (Spectrum) m_TestHelper.load("872280-nir.spec");
      result = new Object[]{sp};
    }
    catch (Exception e) {
      result = new Object[0];
    }
    return result;
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  @Override
  protected Conversion[] getRegressionSetups() {
    SpectrumToJson[] result;

    result    = new SpectrumToJson[2];
    result[0] = new SpectrumToJson();
    result[1] = new SpectrumToJson();
    result[1].setUseReferenceAndMetaData(true);
    result[1].setReferenceValues(new Field[]{new Field("CAN1", DataType.NUMERIC), new Field("MAGU", DataType.NUMERIC), new Field("Clay", DataType.NUMERIC)});
    result[1].setMetaDataValues(new Field[]{new Field("jaar", DataType.NUMERIC), new Field("Nr prakt", DataType.STRING), new Field("Format", DataType.STRING)});

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
    return new TestSuite(SpectrumToJsonTest.class);
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
