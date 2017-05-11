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
 * SpectrumFeatureGeneratorTest.java
 * Copyright (C) 2014-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.option.AbstractArgumentOption;
import adams.db.JdbcUrl;
import adams.env.Environment;
import adams.flow.AbstractSpectrumFlowTest;
import adams.flow.control.Flow;
import adams.flow.core.Actor;
import adams.test.TmpFile;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test for SpectrumFeatureGenerator actor.
 *
 * @author fracpete
 * @author adams.core.option.FlowJUnitTestProducer (code generator)
 * @version $Revision$
 */
public class SpectrumFeatureGeneratorTest
  extends AbstractSpectrumFlowTest {

  /**
   * Initializes the test.
   *
   * @param name	the name of the test
   */
  public SpectrumFeatureGeneratorTest(String name) {
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
    
    m_TestHelper.deleteFileFromTmp("dumpfile.csv");
  }

  /**
   * Called by JUnit after each test method.
   *
   * @throws Exception	if tear-down fails
   */
  @Override
  protected void tearDown() throws Exception {
    m_TestHelper.deleteFileFromTmp("dumpfile.csv");
    
    super.tearDown();
  }

  /**
   * Performs a regression test, comparing against previously generated output.
   */
  public void testRegression() {
    performRegressionTest(
        new TmpFile[]{
          new TmpFile("dumpfile.csv")
        });
  }

  /**
   * 
   * Returns a test suite.
   *
   * @return		the test suite
   */
  public static Test suite() {
    return new TestSuite(SpectrumFeatureGeneratorTest.class);
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
      Actor[] actors1 = new Actor[5];

      // Flow.DatabaseConnection
      adams.flow.standalone.DatabaseConnection databaseconnection2 = new adams.flow.standalone.DatabaseConnection();
      databaseconnection2.setURL(new JdbcUrl(getDatabaseURL()));
      databaseconnection2.setUser(getDatabaseUser());
      databaseconnection2.setPassword(getDatabasePassword());

      actors1[0] = databaseconnection2;

      // Flow.SpectrumIdSupplier
      adams.flow.source.SpectrumIdSupplier spectrumidsupplier8 = new adams.flow.source.SpectrumIdSupplier();
      argOption = (AbstractArgumentOption) spectrumidsupplier8.getOptionManager().findByProperty("conditions");
      adams.db.SpectrumConditionsMulti spectrumconditionsmulti10 = new adams.db.SpectrumConditionsMulti();
      argOption = (AbstractArgumentOption) spectrumconditionsmulti10.getOptionManager().findByProperty("limit");
      spectrumconditionsmulti10.setLimit((Integer) argOption.valueOf("10"));
      spectrumidsupplier8.setConditions(spectrumconditionsmulti10);

      actors1[1] = spectrumidsupplier8;

      // Flow.SpectrumDbReader
      adams.flow.transformer.SpectrumDbReader spectrumdbreader13 = new adams.flow.transformer.SpectrumDbReader();
      argOption = (AbstractArgumentOption) spectrumdbreader13.getOptionManager().findByProperty("postProcessor");
      adams.flow.transformer.datacontainer.NoPostProcessing nopostprocessing15 = new adams.flow.transformer.datacontainer.NoPostProcessing();
      spectrumdbreader13.setPostProcessor(nopostprocessing15);

      actors1[2] = spectrumdbreader13;

      // Flow.SpectrumFeatureGenerator
      adams.flow.transformer.SpectrumFeatureGenerator spectrumfeaturegenerator16 = new adams.flow.transformer.SpectrumFeatureGenerator();
      argOption = (AbstractArgumentOption) spectrumfeaturegenerator16.getOptionManager().findByProperty("algorithm");
      adams.data.spectrum.Amplitudes amplitudes18 = new adams.data.spectrum.Amplitudes();
      argOption = (AbstractArgumentOption) amplitudes18.getOptionManager().findByProperty("converter");
      adams.data.featureconverter.FixedNumFeatures fixednumfeatures20 = new adams.data.featureconverter.FixedNumFeatures();
      argOption = (AbstractArgumentOption) fixednumfeatures20.getOptionManager().findByProperty("converter");
      adams.data.featureconverter.SpreadSheet spreadsheetfeatureconverter22 = new adams.data.featureconverter.SpreadSheet();
      argOption = (AbstractArgumentOption) spreadsheetfeatureconverter22.getOptionManager().findByProperty("dataRowType");
      adams.data.spreadsheet.DenseDataRow densedatarow24 = new adams.data.spreadsheet.DenseDataRow();
      spreadsheetfeatureconverter22.setDataRowType(densedatarow24);

      argOption = (AbstractArgumentOption) spreadsheetfeatureconverter22.getOptionManager().findByProperty("spreadSheetType");
      adams.data.spreadsheet.SpreadSheet spreadsheet26 = new adams.data.spreadsheet.DefaultSpreadSheet();
      spreadsheetfeatureconverter22.setSpreadSheetType(spreadsheet26);

      fixednumfeatures20.setConverter(spreadsheetfeatureconverter22);

      argOption = (AbstractArgumentOption) fixednumfeatures20.getOptionManager().findByProperty("numFeatures");
      fixednumfeatures20.setNumFeatures((Integer) argOption.valueOf("400"));
      amplitudes18.setConverter(fixednumfeatures20);

      spectrumfeaturegenerator16.setAlgorithm(amplitudes18);

      actors1[3] = spectrumfeaturegenerator16;

      // Flow.DumpFile
      adams.flow.sink.DumpFile dumpfile28 = new adams.flow.sink.DumpFile();
      argOption = (AbstractArgumentOption) dumpfile28.getOptionManager().findByProperty("outputFile");
      dumpfile28.setOutputFile((adams.core.io.PlaceholderFile) argOption.valueOf("${TMP}/dumpfile.csv"));
      dumpfile28.setAppend(true);

      actors1[4] = dumpfile28;
      flow.setActors(actors1);

      argOption = (AbstractArgumentOption) flow.getOptionManager().findByProperty("flowExecutionListener");
      adams.flow.execution.NullListener nulllistener31 = new adams.flow.execution.NullListener();
      flow.setFlowExecutionListener(nulllistener31);

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

