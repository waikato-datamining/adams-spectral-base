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
 * SpectrumCleanerTest.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.control;

import adams.core.base.BaseString;
import adams.data.cleaner.spectrum.MinMax;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.db.JdbcUrl;
import adams.env.Environment;
import adams.flow.AbstractSpectrumFlowTest;
import adams.flow.core.Actor;
import adams.flow.sink.DumpFile;
import adams.flow.source.StringConstants;
import adams.flow.standalone.DatabaseConnection;
import adams.flow.transformer.SpectrumFileReader;
import adams.test.TmpFile;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;

/**
 * Tests the SpectrumCleaner actor.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public class SpectrumCleanerTest
  extends AbstractSpectrumFlowTest {

  /**
   * Initializes the test.
   *
   * @param name	the name of the test
   */
  public SpectrumCleanerTest(String name) {
    super(name);
  }

  /**
   * Called by JUnit before each test method.
   *
   * @throws Exception if an error occurs
   */
  protected void setUp() throws Exception {
    super.setUp();

    m_TestHelper.deleteFileFromTmp("dumpfile-rejected.txt");

    m_TestHelper.copyResourceToTmp("871553-nir.spec");
    m_TestHelper.copyResourceToTmp("871562-nir.spec");
    m_TestHelper.copyResourceToTmp("871563-nir.spec");
    m_TestHelper.copyResourceToTmp("871598-nir.spec");
  }

  /**
   * Called by JUnit after each test method.
   *
   * @throws Exception	if tear-down fails
   */
  protected void tearDown() throws Exception {
    m_TestHelper.deleteFileFromTmp("dumpfile-rejected.txt");

    m_TestHelper.deleteFileFromTmp("871553-nir.spec");
    m_TestHelper.deleteFileFromTmp("871562-nir.spec");
    m_TestHelper.deleteFileFromTmp("871563-nir.spec");
    m_TestHelper.deleteFileFromTmp("871598-nir.spec");

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

    StringConstants sc = new StringConstants();
    sc.setStrings(new BaseString[]{
	new BaseString("${TMP}/871553-nir.spec"),
	new BaseString("${TMP}/871562-nir.spec"),
	new BaseString("${TMP}/871563-nir.spec"),
	new BaseString("${TMP}/871598-nir.spec")
    });

    SpectrumFileReader sfr = new SpectrumFileReader();

    DumpFile df_rej = new DumpFile();
    df_rej.setAppend(true);
    df_rej.setOutputFile(new TmpFile("dumpfile-rejected.txt"));

    SpectrumCleaner ic = new SpectrumCleaner();
    MinMax mm = new MinMax();
    mm.setField(new Field("Clay", DataType.NUMERIC));
    mm.setMinimum(100.0);
    mm.setMaximum(200.0);
    ic.setCleaner(mm);
    ic.setRejectionMessagesActor(df_rej);

    Flow flow = new Flow();
    flow.setActors(new Actor[]{dbcon, sc, sfr, ic});

    return flow;
  }

  /**
   * Performs a regression test, comparing against previously generated output.
   */
  public void testRegression() {
    performRegressionTest(
	new File[]{
	    new TmpFile("dumpfile-rejected.txt")});
  }

  /**
   * Returns a test suite.
   *
   * @return		the test suite
   */
  public static Test suite() {
    return new TestSuite(SpectrumCleanerTest.class);
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
