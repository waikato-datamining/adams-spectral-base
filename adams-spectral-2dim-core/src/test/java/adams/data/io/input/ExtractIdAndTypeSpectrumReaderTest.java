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
 * ExtractIdAndTypeSpectrumReaderTest.java
 * Copyright (C) 2019 University of Waikato, Hamilton, New Zealand
 */
package adams.data.io.input;

import adams.core.base.BaseRegExp;
import adams.data.idextraction.ReportFieldRegExp;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the ExtractIdAndTypeSpectrumReader data container. Run from the command line with: <br><br>
 * java adams.data.io.input.ExtractIdAndTypeSpectrumReaderTest
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class ExtractIdAndTypeSpectrumReaderTest
  extends AbstractSpectrumReaderTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public ExtractIdAndTypeSpectrumReaderTest(String name) {
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
	"146048-NIR-FOSS.spec",
	"146048-NIR-FOSS.spec",
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  @Override
  protected AbstractSpectrumReader[] getRegressionSetups() {
    ExtractIdAndTypeSpectrumReader[]  result;
    ReportFieldRegExp 			regexp;

    result = new ExtractIdAndTypeSpectrumReader[2];
    result[0] = new ExtractIdAndTypeSpectrumReader();
    result[1] = new ExtractIdAndTypeSpectrumReader();
    regexp = new ReportFieldRegExp();
    regexp.setField(new Field("Source", DataType.STRING));
    regexp.setRemoveFileExt(true);
    regexp.setFind(new BaseRegExp("([0-9][0-9][0-9])([0-9][0-9][0-9].*)"));
    regexp.setReplace("$1-$2");
    result[1].setIDExtraction(regexp);

    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(ExtractIdAndTypeSpectrumReaderTest.class);
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
