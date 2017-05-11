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
 * SpectrumImageWriterTest.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.sink;

import adams.core.io.PlaceholderFile;
import adams.data.io.input.SimpleSpectrumReader;
import adams.db.JdbcUrl;
import adams.env.Environment;
import adams.flow.AbstractFlowTest;
import adams.flow.control.Flow;
import adams.flow.core.Actor;
import adams.flow.source.FileSupplier;
import adams.flow.standalone.DatabaseConnection;
import adams.flow.transformer.SpectrumFileReader;
import adams.gui.print.PNGWriter;
import adams.test.TmpFile;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests the SpectrumImageWriter actor.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1865 $
 */
public class SpectrumImageWriterTest
  extends AbstractFlowTest {

  /**
   * Initializes the test.
   *
   * @param name	the name of the test
   */
  public SpectrumImageWriterTest(String name) {
    super(name);
  }

  /**
   * Called by JUnit before each test method.
   *
   * @throws Exception if an error occurs
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m_TestHelper.copyResourceToTmp("871553-nir.spec");
  }

  /**
   * Called by JUnit after each test method.
   *
   * @throws Exception	if tear-down fails
   */
  @Override
  protected void tearDown() throws Exception {
    m_TestHelper.deleteFileFromTmp("871553-nir.spec");
    m_TestHelper.deleteFileFromTmp("-1.png");

    super.tearDown();
  }

  /**
   * Used to create an instance of a specific actor.
   *
   * @return a suitably configured <code>Actor</code> value
   */
  @Override
  public Actor getActor() {
    DatabaseConnection dbcon = new DatabaseConnection();
    dbcon.setURL(new JdbcUrl(getDatabaseURL()));
    dbcon.setUser(getDatabaseUser());
    dbcon.setPassword(getDatabasePassword());

    FileSupplier sfs = new FileSupplier();
    sfs.setFiles(new PlaceholderFile[]{new TmpFile("871553-nir.spec")});

    SimpleSpectrumReader reader = new SimpleSpectrumReader();
    SpectrumFileReader fr = new SpectrumFileReader();
    fr.setReader(reader);

    SpectrumImageWriter imgwriter = new SpectrumImageWriter();
    imgwriter.setOutput(new PlaceholderFile(m_TestHelper.getTmpDirectory()));
    imgwriter.setWriter(new PNGWriter());

    Flow flow = new Flow();
    flow.setActors(new Actor[]{dbcon, sfs, fr, imgwriter});

    return flow;
  }

  /**
   * Returns a test suite.
   *
   * @return		the test suite
   */
  public static Test suite() {
    return new TestSuite(SpectrumImageWriterTest.class);
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
