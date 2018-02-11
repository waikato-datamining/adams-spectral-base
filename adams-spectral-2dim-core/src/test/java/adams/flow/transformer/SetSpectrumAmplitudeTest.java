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
 * SetSpectrumAmplitudeTest.java
 * Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.Index;
import adams.core.VariableName;
import adams.core.base.BaseText;
import adams.core.io.PlaceholderDirectory;
import adams.core.option.AbstractArgumentOption;
import adams.data.conversion.UnknownToUnknown;
import adams.data.io.output.CsvSpreadSheetWriter;
import adams.data.io.output.SpreadSheetSpectrumWriter;
import adams.env.Environment;
import adams.flow.AbstractFlowTest;
import adams.flow.control.Flow;
import adams.flow.control.StorageName;
import adams.flow.control.Trigger;
import adams.flow.core.AbstractActor;
import adams.flow.core.Actor;
import adams.flow.execution.NullListener;
import adams.flow.source.NewSpectrum;
import adams.flow.source.Start;
import adams.flow.source.StorageForLoop;
import adams.flow.source.StorageValue;
import adams.flow.standalone.SetVariable;
import adams.flow.transformer.AbstractDataContainerFileWriter.FileNameGeneration;
import adams.flow.transformer.SetSpectrumAmplitude.UpdateType;
import adams.test.TmpFile;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for SetSpectrumAmplitude actor.
 *
 * @author fracpete
 * @author adams.core.option.FlowJUnitTestProducer (code generator)
 */
public class SetSpectrumAmplitudeTest
  extends AbstractFlowTest {

  /**
   * Initializes the test.
   *
   * @param name	the name of the test
   */
  public SetSpectrumAmplitudeTest(String name) {
    super(name);
  }

  /**
   * Called by JUnit before each test method.
   *
   * @throws Exception 	if an error occurs.
   */
  protected void setUp() throws Exception {
    super.setUp();
    
    m_TestHelper.deleteFileFromTmp("dumpfile.txt");
  }

  /**
   * Called by JUnit after each test method.
   *
   * @throws Exception	if tear-down fails
   */
  protected void tearDown() throws Exception {
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
    return new TestSuite(SetSpectrumAmplitudeTest.class);
  }

  /**
   * Used to create an instance of a specific actor.
   *
   * @return a suitably configured <code>AbstractActor</code> value
   */
  public AbstractActor getActor() {
    AbstractArgumentOption    argOption;
    
    Flow flow = new Flow();
    
    try {
      List<Actor> actors = new ArrayList<>();

      // Flow.SetVariable
      SetVariable setvariable = new SetVariable();
      argOption = (AbstractArgumentOption) setvariable.getOptionManager().findByProperty("variableName");
      setvariable.setVariableName((VariableName) argOption.valueOf("max_wave"));
      argOption = (AbstractArgumentOption) setvariable.getOptionManager().findByProperty("variableValue");
      setvariable.setVariableValue((BaseText) argOption.valueOf("10"));
      actors.add(setvariable);

      // Flow.Start
      Start start = new Start();
      actors.add(start);

      // Flow.new spectrum
      Trigger trigger = new Trigger();
      argOption = (AbstractArgumentOption) trigger.getOptionManager().findByProperty("name");
      trigger.setName((String) argOption.valueOf("new spectrum"));
      List<Actor> actors2 = new ArrayList<>();

      // Flow.new spectrum.NewSpectrum
      NewSpectrum newspectrum = new NewSpectrum();
      argOption = (AbstractArgumentOption) newspectrum.getOptionManager().findByProperty("ID");
      newspectrum.setID((String) argOption.valueOf("12345"));
      actors2.add(newspectrum);

      // Flow.new spectrum.SetStorageValue
      SetStorageValue setstoragevalue = new SetStorageValue();
      argOption = (AbstractArgumentOption) setstoragevalue.getOptionManager().findByProperty("storageName");
      setstoragevalue.setStorageName((StorageName) argOption.valueOf("spec"));
      actors2.add(setstoragevalue);
      trigger.setActors(actors2.toArray(new Actor[0]));

      actors.add(trigger);

      // Flow.fill with data
      Trigger trigger2 = new Trigger();
      argOption = (AbstractArgumentOption) trigger2.getOptionManager().findByProperty("name");
      trigger2.setName((String) argOption.valueOf("fill with data"));
      List<Actor> actors3 = new ArrayList<>();

      // Flow.fill with data.StorageForLoop
      StorageForLoop storageforloop = new StorageForLoop();
      argOption = (AbstractArgumentOption) storageforloop.getOptionManager().findByProperty("loopUpper");
      argOption.setVariable("@{max_wave}");
      argOption = (AbstractArgumentOption) storageforloop.getOptionManager().findByProperty("variableName");
      storageforloop.setVariableName((VariableName) argOption.valueOf("i"));
      argOption = (AbstractArgumentOption) storageforloop.getOptionManager().findByProperty("storageName");
      storageforloop.setStorageName((StorageName) argOption.valueOf("spec"));
      actors3.add(storageforloop);

      // Flow.fill with data.SetSpectrumAmplitude
      SetSpectrumAmplitude setspectrumamplitude = new SetSpectrumAmplitude();
      setspectrumamplitude.setNoCopy(true);

      argOption = (AbstractArgumentOption) setspectrumamplitude.getOptionManager().findByProperty("type");
      setspectrumamplitude.setType((UpdateType) argOption.valueOf("INSERT"));
      argOption = (AbstractArgumentOption) setspectrumamplitude.getOptionManager().findByProperty("waveNumber");
      argOption.setVariable("@{i}");
      argOption = (AbstractArgumentOption) setspectrumamplitude.getOptionManager().findByProperty("amplitude");
      argOption.setVariable("@{i}");
      actors3.add(setspectrumamplitude);

      // Flow.fill with data.SetStorageValue
      SetStorageValue setstoragevalue2 = new SetStorageValue();
      argOption = (AbstractArgumentOption) setstoragevalue2.getOptionManager().findByProperty("storageName");
      setstoragevalue2.setStorageName((StorageName) argOption.valueOf("spec"));
      actors3.add(setstoragevalue2);
      trigger2.setActors(actors3.toArray(new Actor[0]));

      actors.add(trigger2);

      // Flow.update
      Trigger trigger3 = new Trigger();
      argOption = (AbstractArgumentOption) trigger3.getOptionManager().findByProperty("name");
      trigger3.setName((String) argOption.valueOf("update"));
      List<Actor> actors4 = new ArrayList<>();

      // Flow.update.StorageValue
      StorageValue storagevalue = new StorageValue();
      argOption = (AbstractArgumentOption) storagevalue.getOptionManager().findByProperty("storageName");
      storagevalue.setStorageName((StorageName) argOption.valueOf("spec"));
      UnknownToUnknown unknowntounknown = new UnknownToUnknown();
      storagevalue.setConversion(unknowntounknown);

      actors4.add(storagevalue);

      // Flow.update.SetID
      SetID setid = new SetID();
      argOption = (AbstractArgumentOption) setid.getOptionManager().findByProperty("ID");
      setid.setID((String) argOption.valueOf("12345-updated"));
      actors4.add(setid);

      // Flow.update.index
      SetSpectrumAmplitude setspectrumamplitude2 = new SetSpectrumAmplitude();
      argOption = (AbstractArgumentOption) setspectrumamplitude2.getOptionManager().findByProperty("name");
      setspectrumamplitude2.setName((String) argOption.valueOf("index"));
      argOption = (AbstractArgumentOption) setspectrumamplitude2.getOptionManager().findByProperty("index");
      setspectrumamplitude2.setIndex((Index) argOption.valueOf("3"));
      actors4.add(setspectrumamplitude2);

      // Flow.update.wave no
      SetSpectrumAmplitude setspectrumamplitude3 = new SetSpectrumAmplitude();
      argOption = (AbstractArgumentOption) setspectrumamplitude3.getOptionManager().findByProperty("name");
      setspectrumamplitude3.setName((String) argOption.valueOf("wave no"));
      argOption = (AbstractArgumentOption) setspectrumamplitude3.getOptionManager().findByProperty("type");
      setspectrumamplitude3.setType((UpdateType) argOption.valueOf("WAVE_NUMBER"));
      argOption = (AbstractArgumentOption) setspectrumamplitude3.getOptionManager().findByProperty("waveNumber");
      setspectrumamplitude3.setWaveNumber((Float) argOption.valueOf("5.0"));
      actors4.add(setspectrumamplitude3);

      // Flow.update.wave no (closest)
      SetSpectrumAmplitude setspectrumamplitude4 = new SetSpectrumAmplitude();
      argOption = (AbstractArgumentOption) setspectrumamplitude4.getOptionManager().findByProperty("name");
      setspectrumamplitude4.setName((String) argOption.valueOf("wave no (closest)"));
      argOption = (AbstractArgumentOption) setspectrumamplitude4.getOptionManager().findByProperty("type");
      setspectrumamplitude4.setType((UpdateType) argOption.valueOf("WAVE_NUMBER_CLOSEST"));
      argOption = (AbstractArgumentOption) setspectrumamplitude4.getOptionManager().findByProperty("waveNumber");
      setspectrumamplitude4.setWaveNumber((Float) argOption.valueOf("7.1"));
      actors4.add(setspectrumamplitude4);

      // Flow.update.insert
      SetSpectrumAmplitude setspectrumamplitude5 = new SetSpectrumAmplitude();
      argOption = (AbstractArgumentOption) setspectrumamplitude5.getOptionManager().findByProperty("name");
      setspectrumamplitude5.setName((String) argOption.valueOf("insert"));
      argOption = (AbstractArgumentOption) setspectrumamplitude5.getOptionManager().findByProperty("type");
      setspectrumamplitude5.setType((UpdateType) argOption.valueOf("INSERT"));
      argOption = (AbstractArgumentOption) setspectrumamplitude5.getOptionManager().findByProperty("waveNumber");
      setspectrumamplitude5.setWaveNumber((Float) argOption.valueOf("11.0"));
      argOption = (AbstractArgumentOption) setspectrumamplitude5.getOptionManager().findByProperty("amplitude");
      setspectrumamplitude5.setAmplitude((Float) argOption.valueOf("5.0"));
      actors4.add(setspectrumamplitude5);

      // Flow.update.SpectrumFileWriter
      SpectrumFileWriter spectrumfilewriter = new SpectrumFileWriter();
      SpreadSheetSpectrumWriter spreadsheetspectrumwriter = new SpreadSheetSpectrumWriter();
      CsvSpreadSheetWriter csvspreadsheetwriter = new CsvSpreadSheetWriter();
      spreadsheetspectrumwriter.setWriter(csvspreadsheetwriter);

      spectrumfilewriter.setWriter(spreadsheetspectrumwriter);

      argOption = (AbstractArgumentOption) spectrumfilewriter.getOptionManager().findByProperty("outputDir");
      spectrumfilewriter.setOutputDir((PlaceholderDirectory) argOption.valueOf("${TMP}"));
      argOption = (AbstractArgumentOption) spectrumfilewriter.getOptionManager().findByProperty("fileNameGeneration");
      spectrumfilewriter.setFileNameGeneration((FileNameGeneration) argOption.valueOf("SUPPLIED"));
      argOption = (AbstractArgumentOption) spectrumfilewriter.getOptionManager().findByProperty("suppliedFileName");
      spectrumfilewriter.setSuppliedFileName((String) argOption.valueOf("dumpfile.txt"));
      actors4.add(spectrumfilewriter);
      trigger3.setActors(actors4.toArray(new Actor[0]));

      actors.add(trigger3);
      flow.setActors(actors.toArray(new Actor[0]));

      NullListener nulllistener = new NullListener();
      flow.setFlowExecutionListener(nulllistener);

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

