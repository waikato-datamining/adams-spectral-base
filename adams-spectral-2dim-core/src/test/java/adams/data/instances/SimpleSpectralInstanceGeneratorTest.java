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
 * SimpleSpectralInstanceGeneratorTest.java
 * Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */
package adams.data.instances;

import adams.core.base.BaseString;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the SimpleSpectralInstanceGenerator generator. Run from the command line with: <br><br>
 * java adams.data.instances.SimpleSpectralInstanceGeneratorTest
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SimpleSpectralInstanceGeneratorTest
  extends AbstractInstanceGeneratorTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public SimpleSpectralInstanceGeneratorTest(String name) {
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
  protected AbstractInstanceGenerator[] getRegressionSetups() {
    SimpleSpectralInstanceGenerator[]	result;

    result = new SimpleSpectralInstanceGenerator[4];

    result[0] = new SimpleSpectralInstanceGenerator();

    result[1] = new SimpleSpectralInstanceGenerator();
    result[1].setAddDatabaseID(true);
    result[1].setAddSampleID(true);

    result[2] = new SimpleSpectralInstanceGenerator();
    result[2].setNotes(new BaseString[]{new BaseString("PROCESS INFORMATION")});

    result[3] = new SimpleSpectralInstanceGenerator();
    result[3].setAdditionalFields(
	new Field[]{
	    new Field("NATU", DataType.NUMERIC),
	    new Field("COR6", DataType.NUMERIC),
	    new Field("LUT2", DataType.NUMERIC)
	}
    );

    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(SimpleSpectralInstanceGeneratorTest.class);
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
