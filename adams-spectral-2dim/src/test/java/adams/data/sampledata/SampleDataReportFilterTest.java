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
 * SampleDataReportFilterTest.java
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package adams.data.sampledata;

import adams.data.report.AbstractReportFilter;
import adams.data.report.DataType;
import adams.data.report.RemoveByDataType;
import adams.data.report.RemoveByName;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the SampleDataReportFilter class.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1727 $
 */
public class SampleDataReportFilterTest
  extends AbstractReportFilterTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public SampleDataReportFilterTest(String name) {
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
  @Override
  protected AbstractReportFilter[] getRegressionSetups() {
    SampleDataReportFilter[]	result;
    RemoveByDataType		removeDT;
    
    result = new SampleDataReportFilter[4];
    
    result[0] = new SampleDataReportFilter();
    
    result[1] = new SampleDataReportFilter();
    result[1].setFilter(new RemoveByName());
    
    result[2] = new SampleDataReportFilter();
    removeDT = new RemoveByDataType();
    removeDT.setDataTypes(new DataType[]{DataType.NUMERIC});
    removeDT.setInvertMatching(false);
    result[2].setFilter(removeDT);
    
    result[3] = new SampleDataReportFilter();
    removeDT = new RemoveByDataType();
    removeDT.setDataTypes(new DataType[]{DataType.NUMERIC});
    removeDT.setInvertMatching(true);
    result[3].setFilter(removeDT);
    
    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(SampleDataReportFilterTest.class);
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
