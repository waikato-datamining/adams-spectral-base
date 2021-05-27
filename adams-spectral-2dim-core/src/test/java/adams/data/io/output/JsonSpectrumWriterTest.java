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
 * JsonSpectrumWriterTest.java
 * Copyright (C) 2016-2021 University of Waikato, Hamilton, New Zealand
 */
package adams.data.io.output;

import adams.data.report.DataType;
import adams.data.report.Field;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the JsonSpectrumWriter data container. Run from the command line with: <br><br>
 * java adams.data.io.output.JsonSpectrumWriterTest
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class JsonSpectrumWriterTest
  extends AbstractSpectrumWriterTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public JsonSpectrumWriterTest(String name) {
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
	"146048-NIR-FOSS.spec",
	"146048-NIR-FOSS.spec",
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  protected AbstractDataContainerWriter[] getRegressionSetups() {
    JsonSpectrumWriter[] 	result;

    result = new JsonSpectrumWriter[2];
    result[0] = new JsonSpectrumWriter();
    result[1] = new JsonSpectrumWriter();
    result[1].setUseReferenceAndMetaData(true);
    result[1].setReferenceValues(new Field[]{new Field("NIN2", DataType.NUMERIC), new Field("PHN2", DataType.NUMERIC)});
    result[1].setMetaDataValues(new Field[]{new Field("Source", DataType.STRING), new Field("Instrument", DataType.STRING)});

    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(JsonSpectrumWriterTest.class);
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
