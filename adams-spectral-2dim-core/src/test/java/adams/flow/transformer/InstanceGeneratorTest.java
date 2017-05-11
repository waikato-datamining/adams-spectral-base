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
 * InstanceGeneratorTest.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.base.BaseRegExp;
import adams.data.instances.SimpleInstanceGenerator;
import adams.db.JdbcUrl;
import adams.db.SpectrumConditionsMulti;
import adams.env.Environment;
import adams.flow.AbstractSpectrumFlowTest;
import adams.flow.control.Flow;
import adams.flow.core.Actor;
import adams.flow.source.SpectrumIdSupplier;
import adams.flow.standalone.DatabaseConnection;
import adams.test.TmpFile;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;

/**
 * Tests the InstanceGenerator actor.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public class InstanceGeneratorTest
  extends AbstractSpectrumFlowTest {

  /**
   * Initializes the test.
   *
   * @param name	the name of the test
   */
  public InstanceGeneratorTest(String name) {
    super(name);
  }

  /**
   * Called by JUnit after each test method.
   *
   * @throws Exception	if tear-down fails
   */
  protected void tearDown() throws Exception {
    m_TestHelper.deleteFileFromTmp("blah.arff");

    super.tearDown();
  }

  /**
   * Used to create an instance of a specific actor.
   *
   * @return a suitably configured <code>Actor</code> value
   */
  public Actor getActor() {
    DatabaseConnection dbcon = new DatabaseConnection();
    dbcon.setURL(new JdbcUrl(getDatabaseURL()));
    dbcon.setUser(getDatabaseUser());
    dbcon.setPassword(getDatabasePassword());

    SpectrumConditionsMulti cond = new SpectrumConditionsMulti();
    cond.setLimit(10);
    cond.setFormat(new BaseRegExp("NIR"));
    SpectrumIdSupplier sis = new SpectrumIdSupplier();
    sis.setConditions(cond);

    SpectrumDbReader sdr = new SpectrumDbReader();

    InstanceGenerator ig = new InstanceGenerator();
    ig.setGenerator(new SimpleInstanceGenerator());

    WekaInstanceDumper id = new WekaInstanceDumper();
    id.setOutputPrefix(new TmpFile("blah"));

    Flow flow = new Flow();
    flow.setActors(new Actor[]{dbcon, sis, sdr, ig, id});

    return flow;
  }

  /**
   * Performs a regression test, comparing against previously generated output.
   */
  public void testRegression() {
    performRegressionTest(
	new File[]{
	    new TmpFile("blah.arff")});
  }

  /**
   * Returns a test suite.
   *
   * @return		the test suite
   */
  public static Test suite() {
    return new TestSuite(InstanceGeneratorTest.class);
  }

  /**
   * Runs the test from commandline.
   *
   * @param args	ignored
   */
  public static void main(String[] args){
    Environment.setEnvironmentClass(Environment.class);
    runTest(suite());
  }
}
