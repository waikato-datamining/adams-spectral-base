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
 * SpectrumDiffTest.java
 * Copyright (C) 2014-2015 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.base.BaseClassname;
import adams.core.option.AbstractArgumentOption;
import adams.env.Environment;
import adams.flow.AbstractSpectrumFlowTest;
import adams.flow.control.Flow;
import adams.flow.core.Actor;
import adams.test.TmpFile;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test for SpectrumDiff actor.
 *
 * @author fracpete
 * @version $Revision$
 */
public class SpectrumDiffTest
  extends AbstractSpectrumFlowTest {

  /**
   * Initializes the test.
   *
   * @param name	the name of the test
   */
  public SpectrumDiffTest(String name) {
    super(name);
  }

  /**
   * Called by JUnit before each test method.
   *
   * @throws Exception 	if an error occurs.
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m_TestHelper.copyResourceToTmp("100000GN13.spec");
    m_TestHelper.copyResourceToTmp("100002GN13.spec");
    m_TestHelper.deleteFileFromTmp("dumpfile.spec");
  }

  /**
   * Called by JUnit after each test method.
   *
   * @throws Exception	if tear-down fails
   */
  @Override
  protected void tearDown() throws Exception {
    m_TestHelper.deleteFileFromTmp("100000GN13.spec");
    m_TestHelper.deleteFileFromTmp("100002GN13.spec");
    m_TestHelper.deleteFileFromTmp("dumpfile.spec");

    super.tearDown();
  }

  /**
   * Performs a regression test, comparing against previously generated output.
   */
  public void testRegression() {
    performRegressionTest(
        new TmpFile[]{
          new TmpFile("dumpfile.spec")
        });
  }

  /**
   *
   * Returns a test suite.
   *
   * @return		the test suite
   */
  public static Test suite() {
    return new TestSuite(SpectrumDiffTest.class);
  }

  /**
   * Used to create an instance of a specific actor.
   *
   * @return a suitably configured <code>Actor</code> value
   */
  @Override
  public Actor getActor() {
    AbstractArgumentOption    argOption;

    Flow flow = new Flow();

    try {
      argOption = (AbstractArgumentOption) flow.getOptionManager().findByProperty("actors");
      Actor[] actors1 = new Actor[6];

      // Flow.FileSupplier
      adams.flow.source.FileSupplier filesupplier2 = new adams.flow.source.FileSupplier();
      filesupplier2.setOutputArray(true);

      argOption = (AbstractArgumentOption) filesupplier2.getOptionManager().findByProperty("files");
      adams.core.io.PlaceholderFile[] files3 = new adams.core.io.PlaceholderFile[2];
      files3[0] = (adams.core.io.PlaceholderFile) argOption.valueOf("${TMP}/100000GN13.spec");
      files3[1] = (adams.core.io.PlaceholderFile) argOption.valueOf("${TMP}/100002GN13.spec");
      filesupplier2.setFiles(files3);
      actors1[0] = filesupplier2;

      // Flow.ArrayProcess
      adams.flow.control.ArrayProcess arrayprocess4 = new adams.flow.control.ArrayProcess();
      argOption = (AbstractArgumentOption) arrayprocess4.getOptionManager().findByProperty("actors");
      Actor[] actors5 = new Actor[1];

      // Flow.ArrayProcess.SpectrumFileReader
      adams.flow.transformer.SpectrumFileReader spectrumfilereader6 = new adams.flow.transformer.SpectrumFileReader();
      argOption = (AbstractArgumentOption) spectrumfilereader6.getOptionManager().findByProperty("reader");
      adams.data.io.input.SimpleSpectrumReader simplespectrumreader8 = new adams.data.io.input.SimpleSpectrumReader();
      spectrumfilereader6.setReader(simplespectrumreader8);

      actors5[0] = spectrumfilereader6;
      arrayprocess4.setActors(actors5);

      actors1[1] = arrayprocess4;

      // Flow.SpectrumDiff
      adams.flow.transformer.SpectrumDiff spectrumdiff9 = new adams.flow.transformer.SpectrumDiff();
      actors1[2] = spectrumdiff9;

      // Flow.SetID
      SetID setid10 = new SetID();
      argOption = (AbstractArgumentOption) setid10.getOptionManager().findByProperty("ID");
      setid10.setID((String) argOption.valueOf("dumpfile"));
      actors1[3] = setid10;

      // Flow.Cast
      adams.flow.control.Cast cast12 = new adams.flow.control.Cast();
      argOption = (AbstractArgumentOption) cast12.getOptionManager().findByProperty("classname");
      cast12.setClassname((BaseClassname) argOption.valueOf("adams.data.spectrum.Spectrum"));
      actors1[4] = cast12;

      // Flow.SpectrumFileWriter
      adams.flow.transformer.SpectrumFileWriter spectrumfilewriter14 = new adams.flow.transformer.SpectrumFileWriter();
      argOption = (AbstractArgumentOption) spectrumfilewriter14.getOptionManager().findByProperty("writer");
      adams.data.io.output.SimpleSpectrumWriter simplespectrumwriter16 = new adams.data.io.output.SimpleSpectrumWriter();
      spectrumfilewriter14.setWriter(simplespectrumwriter16);

      argOption = (AbstractArgumentOption) spectrumfilewriter14.getOptionManager().findByProperty("outputDir");
      spectrumfilewriter14.setOutputDir((adams.core.io.PlaceholderDirectory) argOption.valueOf("${TMP}"));
      actors1[5] = spectrumfilewriter14;
      flow.setActors(actors1);

      argOption = (AbstractArgumentOption) flow.getOptionManager().findByProperty("flowExecutionListener");
      adams.flow.execution.NullListener nulllistener19 = new adams.flow.execution.NullListener();
      flow.setFlowExecutionListener(nulllistener19);

    }
    catch (Exception e) {
      fail("Failed to set up actor: " + e);
    }

    return flow;
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

