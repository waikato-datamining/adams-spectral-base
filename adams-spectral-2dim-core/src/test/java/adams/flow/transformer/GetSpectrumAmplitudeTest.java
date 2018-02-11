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
 * GetSpectrumAmplitudeTest.java
 * Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.Index;
import adams.core.io.PlaceholderFile;
import adams.core.option.AbstractArgumentOption;
import adams.data.io.input.OpusSpectrumReader;
import adams.env.Environment;
import adams.flow.AbstractFlowTest;
import adams.flow.control.Flow;
import adams.flow.control.Tee;
import adams.flow.core.AbstractActor;
import adams.flow.core.Actor;
import adams.flow.core.CallableActorReference;
import adams.flow.execution.NullListener;
import adams.flow.sink.CallableSink;
import adams.flow.sink.DumpFile;
import adams.flow.source.FileSupplier;
import adams.flow.standalone.CallableActors;
import adams.flow.transformer.GetSpectrumAmplitude.RetrievalType;
import adams.test.TmpFile;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for GetSpectrumAmplitude actor.
 *
 * @author fracpete
 * @author adams.core.option.FlowJUnitTestProducer (code generator)
 */
public class GetSpectrumAmplitudeTest
  extends AbstractFlowTest {

  /**
   * Initializes the test.
   *
   * @param name	the name of the test
   */
  public GetSpectrumAmplitudeTest(String name) {
    super(name);
  }

  /**
   * Called by JUnit before each test method.
   *
   * @throws Exception 	if an error occurs.
   */
  protected void setUp() throws Exception {
    super.setUp();
    
    m_TestHelper.copyResourceToTmp("141009_001-01_0-6.0");
    m_TestHelper.deleteFileFromTmp("dumpfile.txt");
  }

  /**
   * Called by JUnit after each test method.
   *
   * @throws Exception	if tear-down fails
   */
  protected void tearDown() throws Exception {
    m_TestHelper.deleteFileFromTmp("141009_001-01_0-6.0");
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
    return new TestSuite(GetSpectrumAmplitudeTest.class);
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

      // Flow.CallableActors
      CallableActors callableactors = new CallableActors();
      List<Actor> actors2 = new ArrayList<>();

      // Flow.CallableActors.DumpFile
      DumpFile dumpfile = new DumpFile();
      argOption = (AbstractArgumentOption) dumpfile.getOptionManager().findByProperty("outputFile");
      dumpfile.setOutputFile((PlaceholderFile) argOption.valueOf("${TMP}/dumpfile.txt"));
      dumpfile.setAppend(true);
      actors2.add(dumpfile);
      callableactors.setActors(actors2.toArray(new Actor[0]));

      actors.add(callableactors);

      // Flow.FileSupplier
      FileSupplier filesupplier = new FileSupplier();
      argOption = (AbstractArgumentOption) filesupplier.getOptionManager().findByProperty("files");
      List<PlaceholderFile> files = new ArrayList<>();
      files.add((PlaceholderFile) argOption.valueOf("${TMP}/141009_001-01_0-6.0"));
      filesupplier.setFiles(files.toArray(new PlaceholderFile[0]));
      actors.add(filesupplier);

      // Flow.SpectrumFileReader
      SpectrumFileReader spectrumfilereader = new SpectrumFileReader();
      OpusSpectrumReader opusspectrumreader = new OpusSpectrumReader();
      spectrumfilereader.setReader(opusspectrumreader);

      actors.add(spectrumfilereader);

      // Flow.index
      Tee tee = new Tee();
      argOption = (AbstractArgumentOption) tee.getOptionManager().findByProperty("name");
      tee.setName((String) argOption.valueOf("index"));
      List<Actor> actors3 = new ArrayList<>();

      // Flow.index.GetSpectrumAmplitude
      GetSpectrumAmplitude getspectrumamplitude = new GetSpectrumAmplitude();
      argOption = (AbstractArgumentOption) getspectrumamplitude.getOptionManager().findByProperty("index");
      getspectrumamplitude.setIndex((Index) argOption.valueOf("8"));
      actors3.add(getspectrumamplitude);

      // Flow.index.CallableSink
      CallableSink callablesink = new CallableSink();
      argOption = (AbstractArgumentOption) callablesink.getOptionManager().findByProperty("callableName");
      callablesink.setCallableName((CallableActorReference) argOption.valueOf("DumpFile"));
      actors3.add(callablesink);
      tee.setActors(actors3.toArray(new Actor[0]));

      actors.add(tee);

      // Flow.wave no
      Tee tee2 = new Tee();
      argOption = (AbstractArgumentOption) tee2.getOptionManager().findByProperty("name");
      tee2.setName((String) argOption.valueOf("wave no"));
      List<Actor> actors4 = new ArrayList<>();

      // Flow.wave no.GetSpectrumAmplitude
      GetSpectrumAmplitude getspectrumamplitude2 = new GetSpectrumAmplitude();
      argOption = (AbstractArgumentOption) getspectrumamplitude2.getOptionManager().findByProperty("type");
      getspectrumamplitude2.setType((RetrievalType) argOption.valueOf("WAVE_NUMBER"));
      argOption = (AbstractArgumentOption) getspectrumamplitude2.getOptionManager().findByProperty("waveNumber");
      getspectrumamplitude2.setWaveNumber((Float) argOption.valueOf("3648.9077"));
      actors4.add(getspectrumamplitude2);

      // Flow.wave no.CallableSink
      CallableSink callablesink2 = new CallableSink();
      argOption = (AbstractArgumentOption) callablesink2.getOptionManager().findByProperty("callableName");
      callablesink2.setCallableName((CallableActorReference) argOption.valueOf("DumpFile"));
      actors4.add(callablesink2);
      tee2.setActors(actors4.toArray(new Actor[0]));

      actors.add(tee2);

      // Flow.wave no (closest)
      Tee tee3 = new Tee();
      argOption = (AbstractArgumentOption) tee3.getOptionManager().findByProperty("name");
      tee3.setName((String) argOption.valueOf("wave no (closest)"));
      List<Actor> actors5 = new ArrayList<>();

      // Flow.wave no (closest).GetSpectrumAmplitude
      GetSpectrumAmplitude getspectrumamplitude3 = new GetSpectrumAmplitude();
      argOption = (AbstractArgumentOption) getspectrumamplitude3.getOptionManager().findByProperty("type");
      getspectrumamplitude3.setType((RetrievalType) argOption.valueOf("WAVE_NUMBER_CLOSEST"));
      argOption = (AbstractArgumentOption) getspectrumamplitude3.getOptionManager().findByProperty("waveNumber");
      getspectrumamplitude3.setWaveNumber((Float) argOption.valueOf("3648.9"));
      actors5.add(getspectrumamplitude3);

      // Flow.wave no (closest).CallableSink
      CallableSink callablesink3 = new CallableSink();
      argOption = (AbstractArgumentOption) callablesink3.getOptionManager().findByProperty("callableName");
      callablesink3.setCallableName((CallableActorReference) argOption.valueOf("DumpFile"));
      actors5.add(callablesink3);
      tee3.setActors(actors5.toArray(new Actor[0]));

      actors.add(tee3);
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

