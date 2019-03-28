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
 * SpectralAngleMapperTest.java
 * Copyright (C) 2019 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.env.Environment;
import adams.core.option.AbstractArgumentOption;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;
import adams.flow.core.AbstractActor;
import adams.flow.control.Flow;
import adams.flow.AbstractFlowTest;
import adams.test.TmpFile;
import adams.core.io.PlaceholderFile;
import adams.core.io.SimpleFixedFilenameGenerator;
import adams.data.conversion.AnyToString;
import adams.data.io.input.SimpleSpectrumReader;
import adams.data.io.output.TextFileWriter;
import adams.data.spectrum.SpectrumComparator;
import adams.flow.control.ArrayProcess;
import adams.flow.control.StorageName;
import adams.flow.control.Trigger;
import adams.flow.control.flowrestart.NullManager;
import adams.flow.core.Actor;
import adams.flow.execution.NullListener;
import adams.flow.sink.TextWriter;
import adams.flow.source.FileSupplier;
import adams.flow.source.Start;

/**
 * Test for SpectralAngleMapper actor.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 * @author adams.core.option.FlowJUnitTestProducer (code generator)
 */
public class SpectralAngleMapperTest
  extends AbstractFlowTest {

  /**
   * Initializes the test.
   *
   * @param name	the name of the test
   */
  public SpectralAngleMapperTest(String name) {
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
    
    m_TestHelper.copyResourceToTmp("input.spec");
    m_TestHelper.copyResourceToTmp("ref1.spec");
    m_TestHelper.copyResourceToTmp("ref2.spec");
    m_TestHelper.copyResourceToTmp("ref3.spec");
    m_TestHelper.copyResourceToTmp("ref4.spec");
    m_TestHelper.copyResourceToTmp("ref5.spec");
    m_TestHelper.deleteFileFromTmp("dumpfile.txt");
  }

  /**
   * Called by JUnit after each test method.
   *
   * @throws Exception	if tear-down fails
   */
  @Override
  protected void tearDown() throws Exception {
    m_TestHelper.deleteFileFromTmp("input.spec");
    m_TestHelper.deleteFileFromTmp("ref1.spec");
    m_TestHelper.deleteFileFromTmp("ref2.spec");
    m_TestHelper.deleteFileFromTmp("ref3.spec");
    m_TestHelper.deleteFileFromTmp("ref4.spec");
    m_TestHelper.deleteFileFromTmp("ref5.spec");
    m_TestHelper.deleteFileFromTmp("dumpfile.txt");
    
    super.tearDown();
  }

  /**
   * Performs a regression test, comparing against previously generated output.
   */
  public void testRegression() {
    performRegressionTest(
        new TmpFile[]{
          new TmpFile("dumpfile.txt")
        });
  }

  /**
   * 
   * Returns a test suite.
   *
   * @return		the test suite
   */
  public static Test suite() {
    return new TestSuite(SpectralAngleMapperTest.class);
  }

  /**
   * Used to create an instance of a specific actor.
   *
   * @return a suitably configured <code>AbstractActor</code> value
   */
  @Override
  public AbstractActor getActor() {
    AbstractArgumentOption    argOption;
    
    Flow flow = new Flow();
    
    try {
      List<Actor> actors = new ArrayList<>();

      // Flow.Start
      Start start = new Start();
      actors.add(start);

      // Flow.ReadAndStoreReferences
      Trigger trigger = new Trigger();
      argOption = (AbstractArgumentOption) trigger.getOptionManager().findByProperty("name");
      trigger.setName((String) argOption.valueOf("ReadAndStoreReferences"));
      List<Actor> actors2 = new ArrayList<>();

      // Flow.ReadAndStoreReferences.FileSupplier
      FileSupplier filesupplier = new FileSupplier();
      filesupplier.setOutputArray(true);

      argOption = (AbstractArgumentOption) filesupplier.getOptionManager().findByProperty("files");
      List<PlaceholderFile> files = new ArrayList<>();
      files.add((PlaceholderFile) argOption.valueOf("${TMP}/ref1.spec"));
      files.add((PlaceholderFile) argOption.valueOf("${TMP}/ref2.spec"));
      files.add((PlaceholderFile) argOption.valueOf("${TMP}/ref3.spec"));
      files.add((PlaceholderFile) argOption.valueOf("${TMP}/ref4.spec"));
      files.add((PlaceholderFile) argOption.valueOf("${TMP}/ref5.spec"));
      filesupplier.setFiles(files.toArray(new PlaceholderFile[0]));
      actors2.add(filesupplier);

      // Flow.ReadAndStoreReferences.ReadReferenceFiles
      ArrayProcess arrayprocess = new ArrayProcess();
      argOption = (AbstractArgumentOption) arrayprocess.getOptionManager().findByProperty("name");
      arrayprocess.setName((String) argOption.valueOf("ReadReferenceFiles"));
      List<Actor> actors3 = new ArrayList<>();

      // Flow.ReadAndStoreReferences.ReadReferenceFiles.SpectrumFileReader
      SpectrumFileReader spectrumfilereader = new SpectrumFileReader();
      SimpleSpectrumReader simplespectrumreader = new SimpleSpectrumReader();
      simplespectrumreader.setKeepFormat(true);

      spectrumfilereader.setReader(simplespectrumreader);

      SpectrumComparator spectrumcomparator = new SpectrumComparator();
      spectrumfilereader.setCustomComparator(spectrumcomparator);

      actors3.add(spectrumfilereader);
      arrayprocess.setActors(actors3.toArray(new Actor[0]));

      actors2.add(arrayprocess);

      // Flow.ReadAndStoreReferences.SetStorageValue
      SetStorageValue setstoragevalue = new SetStorageValue();
      argOption = (AbstractArgumentOption) setstoragevalue.getOptionManager().findByProperty("storageName");
      setstoragevalue.setStorageName((StorageName) argOption.valueOf("references"));
      actors2.add(setstoragevalue);
      trigger.setActors(actors2.toArray(new Actor[0]));

      actors.add(trigger);

      // Flow.PerformSAM
      Trigger trigger2 = new Trigger();
      argOption = (AbstractArgumentOption) trigger2.getOptionManager().findByProperty("name");
      trigger2.setName((String) argOption.valueOf("PerformSAM"));
      List<Actor> actors4 = new ArrayList<>();

      // Flow.PerformSAM.FileSupplier
      FileSupplier filesupplier2 = new FileSupplier();
      argOption = (AbstractArgumentOption) filesupplier2.getOptionManager().findByProperty("files");
      List<PlaceholderFile> files2 = new ArrayList<>();
      files2.add((PlaceholderFile) argOption.valueOf("${TMP}/input.spec"));
      filesupplier2.setFiles(files2.toArray(new PlaceholderFile[0]));
      actors4.add(filesupplier2);

      // Flow.PerformSAM.SpectrumFileReader
      SpectrumFileReader spectrumfilereader2 = new SpectrumFileReader();
      SimpleSpectrumReader simplespectrumreader2 = new SimpleSpectrumReader();
      simplespectrumreader2.setKeepFormat(true);

      spectrumfilereader2.setReader(simplespectrumreader2);

      SpectrumComparator spectrumcomparator2 = new SpectrumComparator();
      spectrumfilereader2.setCustomComparator(spectrumcomparator2);

      actors4.add(spectrumfilereader2);

      // Flow.PerformSAM.SpectralAngleMapper
      SpectralAngleMapper spectralanglemapper = new SpectralAngleMapper();
      argOption = (AbstractArgumentOption) spectralanglemapper.getOptionManager().findByProperty("references");
      spectralanglemapper.setReferences((StorageName) argOption.valueOf("references"));
      actors4.add(spectralanglemapper);

      // Flow.PerformSAM.Convert
      Convert convert = new Convert();
      AnyToString anytostring = new AnyToString();
      convert.setConversion(anytostring);

      actors4.add(convert);

      // Flow.PerformSAM.TextWriter
      TextWriter textwriter = new TextWriter();
      TextFileWriter textfilewriter = new TextFileWriter();
      SimpleFixedFilenameGenerator simplefixedfilenamegenerator = new SimpleFixedFilenameGenerator();
      argOption = (AbstractArgumentOption) simplefixedfilenamegenerator.getOptionManager().findByProperty("name");
      simplefixedfilenamegenerator.setName((PlaceholderFile) argOption.valueOf("${TMP}/dumpfile.txt"));
      textfilewriter.setFilenameGenerator(simplefixedfilenamegenerator);

      textfilewriter.setIgnoreName(true);

      textwriter.setWriter(textfilewriter);

      actors4.add(textwriter);
      trigger2.setActors(actors4.toArray(new Actor[0]));

      actors.add(trigger2);
      flow.setActors(actors.toArray(new Actor[0]));

      NullListener nulllistener = new NullListener();
      flow.setFlowExecutionListener(nulllistener);

      NullManager nullmanager = new NullManager();
      flow.setFlowRestartManager(nullmanager);

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

