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
 * LRCoeffTest.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */
package adams.data.spectrumfilter;

import adams.core.base.BaseRegExp;
import adams.data.filter.AbstractFilter;
import adams.data.instances.SimpleInstanceGenerator;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.spectrum.Spectrum;
import adams.db.SpectrumConditionsMulti;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the LRCoeff filter. Run from the command line with: <br><br>
 * java knir.data.filter.LRCoeffTest
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class LRCoeffTest
  extends AbstractSpectrumFilterTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public LRCoeffTest(String name) {
    super(name);
  }

  /**
   * Returns the configured filter.
   *
   * @return		the filter
   */
  public AbstractFilter<Spectrum> getFilter() {
    return new LRCoeff();
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
  protected AbstractFilter[] getRegressionSetups() {
    LRCoeff[]			result;
    SpectrumConditionsMulti	cond;
    SimpleInstanceGenerator 	generator;

    result = new LRCoeff[3];

    cond = new SpectrumConditionsMulti();
    cond.setFormat(new BaseRegExp("NIR"));
    cond.setLimit(100);
    cond.setFields(new Field[]{
	new Field("CAN1", DataType.NUMERIC)
    });
    generator = new SimpleInstanceGenerator();
    generator.setField(new Field("CAN1", DataType.NUMERIC));
    result[0] = new LRCoeff();
    result[0].setGenerator(generator);
    result[0].setConditions(cond);

    cond = new SpectrumConditionsMulti();
    cond.setFormat(new BaseRegExp("NIR"));
    cond.setLimit(100);
    cond.setFields(new Field[]{
	new Field("CAN1", DataType.NUMERIC)
    });
    generator = new SimpleInstanceGenerator();
    generator.setField(new Field("CAN1", DataType.NUMERIC));
    result[1] = new LRCoeff();
    result[1].setGenerator(generator);
    result[1].setScale(true);
    result[1].setConditions(cond);

    cond = new SpectrumConditionsMulti();
    cond.setFormat(new BaseRegExp("NIR"));
    cond.setLimit(100);
    cond.setFields(new Field[]{
	new Field("CAN1", DataType.NUMERIC)
    });
    generator = new SimpleInstanceGenerator();
    generator.setField(new Field("CAN1", DataType.NUMERIC));
    result[2] = new LRCoeff();
    result[2].setGenerator(generator);
    result[2].setScale(true);
    result[2].setAbsolute(true);
    result[2].setConditions(cond);

    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(LRCoeffTest.class);
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
