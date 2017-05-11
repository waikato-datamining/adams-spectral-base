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
 * SampleDataFileReaderTest.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.base.BaseString;
import adams.core.io.PlaceholderDirectory;
import adams.data.io.input.SimpleCSVSampleDataReader;
import adams.data.io.output.SimpleCSVSampleDataWriter;
import adams.db.JdbcUrl;
import adams.env.Environment;
import adams.flow.AbstractSpectrumFlowTest;
import adams.flow.control.Flow;
import adams.flow.core.Actor;
import adams.flow.source.StringConstants;
import adams.flow.standalone.DatabaseConnection;
import adams.test.TmpFile;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.File;

/**
 * Tests the SampleDataFileReader/Writer actor.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public class SampleDataFileReaderTest
  extends AbstractSpectrumFlowTest {

  /**
   * Initializes the test.
   *
   * @param name	the name of the test
   */
  public SampleDataFileReaderTest(String name) {
    super(name);
  }

  /**
   * Called by JUnit before each test method.
   *
   * @throws Exception if an error occurs
   */
  protected void setUp() throws Exception {
    super.setUp();

    m_TestHelper.copyResourceToTmp("871553-nir.csv");
  }

  /**
   * Called by JUnit after each test method.
   *
   * @throws Exception	if tear-down fails
   */
  protected void tearDown() throws Exception {
    m_TestHelper.deleteFileFromTmp("1.csv");
    m_TestHelper.deleteFileFromTmp("871553-nir.csv");

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
	new BaseString("${TMP}/871553-nir.csv")
    });

    SampleDataFileReader fr = new SampleDataFileReader();
    fr.setReader(new SimpleCSVSampleDataReader());

    SimpleCSVSampleDataWriter writer = new SimpleCSVSampleDataWriter();
    SampleDataFileWriter fw = new SampleDataFileWriter();
    fw.setWriter(writer);
    fw.setOutputDir(new PlaceholderDirectory(m_TestHelper.getTmpDirectory()));

    Flow flow = new Flow();
    flow.setActors(new Actor[]{dbcon, sc, fr, fw});

    return flow;
  }

  /**
   * Performs a regression test, comparing against previously generated output.
   */
  public void testRegression() {
    performRegressionTest(
	new File[]{
	    new TmpFile("1.csv")});
  }

  /**
   * Returns a test suite.
   *
   * @return		the test suite
   */
  public static Test suite() {
    return new TestSuite(SampleDataFileReaderTest.class);
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
