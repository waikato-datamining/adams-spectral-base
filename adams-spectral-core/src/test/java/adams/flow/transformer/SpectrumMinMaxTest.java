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
 * SpectrumMinMaxTest.java
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
 * Test for SpectrumMinMax actor.
 *
 * @author fracpete
 * @author adams.core.option.FlowJUnitTestProducer (code generator)
 * @version $Revision$
 */
public class SpectrumMinMaxTest
  extends AbstractSpectrumFlowTest {

  /**
   * Initializes the test.
   *
   * @param name	the name of the test
   */
  public SpectrumMinMaxTest(String name) {
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
    return new TestSuite(SpectrumMinMaxTest.class);
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
      argOption = (AbstractArgumentOption) filesupplier2.getOptionManager().findByProperty("files");
      adams.core.io.PlaceholderFile[] files3 = new adams.core.io.PlaceholderFile[2];
      files3[0] = (adams.core.io.PlaceholderFile) argOption.valueOf("${TMP}/100000GN13.spec");
      files3[1] = (adams.core.io.PlaceholderFile) argOption.valueOf("${TMP}/100002GN13.spec");
      filesupplier2.setFiles(files3);
      actors1[0] = filesupplier2;

      // Flow.SpectrumFileReader
      adams.flow.transformer.SpectrumFileReader spectrumfilereader4 = new adams.flow.transformer.SpectrumFileReader();
      argOption = (AbstractArgumentOption) spectrumfilereader4.getOptionManager().findByProperty("reader");
      adams.data.io.input.SimpleSpectrumReader simplespectrumreader6 = new adams.data.io.input.SimpleSpectrumReader();
      spectrumfilereader4.setReader(simplespectrumreader6);

      actors1[1] = spectrumfilereader4;

      // Flow.SpectrumMinMax
      adams.flow.transformer.SpectrumMinMax spectrumminmax7 = new adams.flow.transformer.SpectrumMinMax();
      argOption = (AbstractArgumentOption) spectrumminmax7.getOptionManager().findByProperty("interval");
      spectrumminmax7.setInterval((Integer) argOption.valueOf("2"));
      actors1[2] = spectrumminmax7;

      // Flow.ArrayProcess
      adams.flow.control.ArrayProcess arrayprocess9 = new adams.flow.control.ArrayProcess();
      argOption = (AbstractArgumentOption) arrayprocess9.getOptionManager().findByProperty("actors");
      Actor[] actors10 = new Actor[2];

      // Flow.ArrayProcess.SetID
      SetID setid11 = new SetID();
      argOption = (AbstractArgumentOption) setid11.getOptionManager().findByProperty("ID");
      setid11.setID((String) argOption.valueOf("dumpfile"));
      actors10[0] = setid11;

      // Flow.ArrayProcess.Cast
      adams.flow.control.Cast cast13 = new adams.flow.control.Cast();
      argOption = (AbstractArgumentOption) cast13.getOptionManager().findByProperty("classname");
      cast13.setClassname((BaseClassname) argOption.valueOf("adams.data.spectrum.Spectrum"));
      actors10[1] = cast13;
      arrayprocess9.setActors(actors10);

      actors1[3] = arrayprocess9;

      // Flow.Convert
      Convert convert15 = new Convert();
      argOption = (AbstractArgumentOption) convert15.getOptionManager().findByProperty("conversion");
      adams.data.conversion.SpectraToMultiSpectrum spectratomultispectrum17 = new adams.data.conversion.SpectraToMultiSpectrum();
      convert15.setConversion(spectratomultispectrum17);

      actors1[4] = convert15;

      // Flow.SpectrumFileWriter
      adams.flow.transformer.SpectrumFileWriter spectrumfilewriter18 = new adams.flow.transformer.SpectrumFileWriter();
      argOption = (AbstractArgumentOption) spectrumfilewriter18.getOptionManager().findByProperty("writer");
      adams.data.io.output.SimpleSpectrumWriter simplespectrumwriter20 = new adams.data.io.output.SimpleSpectrumWriter();
      spectrumfilewriter18.setWriter(simplespectrumwriter20);

      argOption = (AbstractArgumentOption) spectrumfilewriter18.getOptionManager().findByProperty("outputDir");
      spectrumfilewriter18.setOutputDir((adams.core.io.PlaceholderDirectory) argOption.valueOf("${TMP}"));
      actors1[5] = spectrumfilewriter18;
      flow.setActors(actors1);

      argOption = (AbstractArgumentOption) flow.getOptionManager().findByProperty("flowExecutionListener");
      adams.flow.execution.NullListener nulllistener23 = new adams.flow.execution.NullListener();
      flow.setFlowExecutionListener(nulllistener23);

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
    Environment.setEnvironmentClass(adams.env.Environment.class);
    runTest(suite());
  }
}

