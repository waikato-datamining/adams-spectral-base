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
 * UpdateReportFieldTest.java
 * Copyright (C) 2026 University of Waikato, Hamilton, New Zealand
 */
package adams.data.spectrumfilter;

import adams.data.filter.Filter;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.spectrum.Spectrum;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the UpdateReportField filter. Run from the command line with: <br><br>
 * java adams.data.spectrumfilter.UpdateReportFieldTest
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class UpdateReportFieldTest
  extends AbstractSpectrumFilterTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public UpdateReportFieldTest(String name) {
    super(name);
  }

  /**
   * Returns the configured filter.
   *
   * @return		the filter
   */
  public Filter<Spectrum> getFilter() {
    return new UpdateReportField();
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
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  protected Filter[] getRegressionSetups() {
    UpdateReportField[]	result;

    result = new UpdateReportField[4];

    result[0] = new UpdateReportField();

    result[1] = new UpdateReportField();
    result[1].setField(new Field("Blah", DataType.NUMERIC));
    result[1].setValue("3.1415");

    result[2] = new UpdateReportField();
    result[2].setField(new Field("Blah", DataType.BOOLEAN));
    result[2].setValue("true");

    result[3] = new UpdateReportField();
    result[3].setField(new Field("Blah", DataType.STRING));
    result[3].setValue("hello world");

    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(UpdateReportFieldTest.class);
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
