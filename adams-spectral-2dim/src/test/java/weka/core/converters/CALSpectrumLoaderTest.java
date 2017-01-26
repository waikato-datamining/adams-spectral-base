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
 * CALSpectrumLoaderTest.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package weka.core.converters;

import adams.core.Utils;
import adams.core.base.BaseRegExp;
import adams.env.Environment;
import adams.test.AbstractTestHelper;
import adams.test.AdamsTestCase;
import adams.test.TestHelper;
import adams.test.TmpFile;
import junit.framework.Test;
import junit.framework.TestSuite;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSink;

import java.io.File;

/**
 * Tests the CALSpectrumLoader.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class CALSpectrumLoaderTest
  extends AdamsTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name the name of the test
   */
  public CALSpectrumLoaderTest(String name) {
    super(name);
  }

  /**
   * Performs a regression test, comparing against previously generated output.
   */
  public void testRegression() {
    TmpFile	data;
    TmpFile	arff;
    String	regression;

    m_TestHelper.copyResourceToTmp("CALK10.CAL");
    data = new TmpFile("CALK10.CAL");
    arff = new TmpFile("CALK10.arff");
    try {
      CALSpectrumLoader loader = new CALSpectrumLoader();
      loader.setRefRegExp(new BaseRegExp("ref_1"));
      loader.setFile(data.getAbsoluteFile());
      Instances inst = loader.getDataSet();
      DataSink.write(arff.getAbsolutePath(), inst);
      regression = m_Regression.compare(new File[]{arff});
      assertNull("Output differs:\n" + regression, regression);
    }
    catch (Exception e) {
      fail("Failed regression test!\n" + Utils.throwableToString(e));
    }
    m_TestHelper.deleteFileFromTmp("CALK10.CAL");
  }

  /**
   * Returns the test helper class to use.
   *
   * @return		the helper class instance
   */
  @Override
  protected AbstractTestHelper newTestHelper() {
    return new TestHelper(this, "weka/core/converters/data");
  }

  /**
   * Returns a test suite.
   *
   * @return		the test suite
   */
  public static Test suite() {
    return new TestSuite(CALSpectrumLoaderTest.class);
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
