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
 * ColumnWiseSpreadSheetSpectrumReaderTest.java
 * Copyright (C) 2013-2016 University of Waikato, Hamilton, New Zealand
 */
package adams.data.io.input;

import adams.core.Index;
import adams.core.Range;
import adams.core.base.BaseCharset;
import adams.core.base.BaseRegExp;
import adams.data.spreadsheet.SpreadSheetColumnIndex;
import adams.data.spreadsheet.SpreadSheetColumnRange;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the ColumnWiseSpreadSheetSpectrumReader data container. Run from the command line with: <br><br>
 * java adams.data.io.input.ColumnWiseSpreadSheetSpectrumReaderTest
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class ColumnWiseSpreadSheetSpectrumReaderTest
  extends AbstractSpectrumReaderTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public ColumnWiseSpreadSheetSpectrumReaderTest(String name) {
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
	"Spectra_08_01_13.csv"
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  @Override
  protected AbstractSpectrumReader[] getRegressionSetups() {
    ColumnWiseSpreadSheetSpectrumReader[]	result;
    CsvSpreadSheetReader			reader;
    
    result    = new ColumnWiseSpreadSheetSpectrumReader[1];
    result[0] = new ColumnWiseSpreadSheetSpectrumReader();
    result[0].setFormat("XRF");
    result[0].setInstrument("Olympus");
    result[0].setSpectrumColumns(new SpreadSheetColumnRange("2-13"));
    result[0].setAmplitudeRows(new Range("21-2068"));
    result[0].setSampleDataLabelsColumn(new SpreadSheetColumnIndex("1"));
    result[0].setSampleDataRows(new Range("2-20"));
    result[0].setSampleIDRow(new Index("1"));
    reader = new CsvSpreadSheetReader();
    reader.setSeparator("\\t");
    reader.setEncoding(new BaseCharset("UTF-16"));
    reader.setMissingValue(new BaseRegExp(""));
    reader.setNoHeader(true);
    result[0].setReader(reader);
    
    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(ColumnWiseSpreadSheetSpectrumReaderTest.class);
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
