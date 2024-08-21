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
 * MultiColumnSpreadSheetSampleDataReaderTest.java
 * Copyright (C) 2024 University of Waikato, Hamilton, New Zealand
 */
package adams.data.io.input;

import adams.data.spreadsheet.SpreadSheetColumnIndex;
import adams.data.spreadsheet.SpreadSheetColumnRange;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the MultiColumnSpreadSheetSampleDataReader data container. Run from the command line with: <br><br>
 * java adams.data.io.input.MultiColumnSpreadSheetSampleDataReaderTest
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class MultiColumnSpreadSheetSampleDataReaderTest
  extends AbstractSampleDataReaderTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public MultiColumnSpreadSheetSampleDataReaderTest(String name) {
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
	"sampledata-multicol.csv"
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  protected AbstractReportReader[] getRegressionSetups() {
    MultiColumnSpreadSheetSampleDataReader[]	result;

    result = new MultiColumnSpreadSheetSampleDataReader[1];
    result[0] = new MultiColumnSpreadSheetSampleDataReader();
    result[0].setColumnSampleID(new SpreadSheetColumnIndex("1"));
    result[0].setColumnsSampleData(new SpreadSheetColumnRange("2-5"));
    result[0].setColumnsNumeric(new SpreadSheetColumnRange("2,3"));
    result[0].setColumnsBoolean(new SpreadSheetColumnRange("4"));
    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(MultiColumnSpreadSheetSampleDataReaderTest.class);
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
