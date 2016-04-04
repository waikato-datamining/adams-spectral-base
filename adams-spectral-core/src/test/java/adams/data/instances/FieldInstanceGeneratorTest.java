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
 * FieldInstanceGeneratorTest.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */
package adams.data.instances;

import adams.core.base.BaseString;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the FieldInstanceGenerator generator. Run from the command line with: <br><br>
 * java knir.data.instances.FieldInstanceGeneratorTest
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class FieldInstanceGeneratorTest
  extends AbstractInstanceGeneratorTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public FieldInstanceGeneratorTest(String name) {
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
	"872280-nir.spec"
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  protected AbstractInstanceGenerator[] getRegressionSetups() {
    FieldInstanceGenerator[]	result;

    result = new FieldInstanceGenerator[3];

    result[0] = new FieldInstanceGenerator();
    result[0].setFields(new Field[]{
	new Field("GLV2", DataType.NUMERIC),
	new Field("CALU", DataType.NUMERIC),
	new Field("MAGU", DataType.NUMERIC),
	new Field("LUT2", DataType.NUMERIC),
	new Field("STT6", DataType.NUMERIC),
	new Field("CAN1", DataType.NUMERIC),
    });

    result[1] = new FieldInstanceGenerator();
    result[1].setFields(new Field[]{
	new Field("GLV2", DataType.NUMERIC),
	new Field("CALU", DataType.NUMERIC),
	new Field("MAGU", DataType.NUMERIC),
	new Field("LUT2", DataType.NUMERIC),
	new Field("STT6", DataType.NUMERIC),
	new Field("CAN1", DataType.NUMERIC),
    });
    result[1].setAddDatabaseID(true);
    result[1].setAddSampleID(true);

    result[2] = new FieldInstanceGenerator();
    result[2].setFields(new Field[]{
	new Field("GLV2", DataType.NUMERIC),
	new Field("CALU", DataType.NUMERIC),
	new Field("MAGU", DataType.NUMERIC),
	new Field("LUT2", DataType.NUMERIC),
	new Field("STT6", DataType.NUMERIC),
	new Field("CAN1", DataType.NUMERIC),
    });
    result[2].setNotes(new BaseString[]{new BaseString("PROCESS INFORMATION")});

    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(FieldInstanceGeneratorTest.class);
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
