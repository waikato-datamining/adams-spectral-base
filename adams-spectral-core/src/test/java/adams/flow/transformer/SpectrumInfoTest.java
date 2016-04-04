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
 * SpectrumInfoTest.java
 * Copyright (C) 2013 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.option.AbstractArgumentOption;
import adams.env.Environment;
import adams.flow.AbstractSpectrumFlowTest;
import adams.flow.control.Flow;
import adams.flow.core.Actor;
import adams.flow.standalone.DatabaseConnection;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test for SpectrumInfo actor.
 *
 * @author fracpete
 * @author adams.core.option.FlowJUnitTestProducer (code generator)
 * @version $Revision: 2017 $
 */
public class SpectrumInfoTest
  extends AbstractSpectrumFlowTest {

  /**
   * Initializes the test.
   *
   * @param name	the name of the test
   */
  public SpectrumInfoTest(String name) {
    super(name);
  }

  /**
   * 
   * Returns a test suite.
   *
   * @return		the test suite
   */
  public static Test suite() {
    return new TestSuite(SpectrumInfoTest.class);
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
      Actor[] actors1 = new Actor[11];

      // Flow.GlobalActors
      adams.flow.standalone.CallableActors globalactors2 = new adams.flow.standalone.CallableActors();
      argOption = (AbstractArgumentOption) globalactors2.getOptionManager().findByProperty("actors");
      Actor[] actors3 = new Actor[1];

      // Flow.GlobalActors.Display
      adams.flow.sink.Display display4 = new adams.flow.sink.Display();
      actors3[0] = display4;
      globalactors2.setActors(actors3);

      actors1[0] = globalactors2;

      // Flow.DatabaseConnection
      DatabaseConnection dbcon = new DatabaseConnection();
      dbcon.setURL(getDatabaseURL());
      dbcon.setUser(getDatabaseUser());
      dbcon.setPassword(getDatabasePassword());
      actors1[1] = dbcon;

      // Flow.SpectrumIdSupplier
      adams.flow.source.SpectrumIdSupplier spectrumidsupplier9 = new adams.flow.source.SpectrumIdSupplier();
      argOption = (AbstractArgumentOption) spectrumidsupplier9.getOptionManager().findByProperty("conditions");
      adams.db.SpectrumConditionsMulti spectrumconditionsmulti11 = new adams.db.SpectrumConditionsMulti();
      argOption = (AbstractArgumentOption) spectrumconditionsmulti11.getOptionManager().findByProperty("limit");
      spectrumconditionsmulti11.setLimit((Integer) argOption.valueOf("1"));
      spectrumidsupplier9.setConditions(spectrumconditionsmulti11);

      actors1[2] = spectrumidsupplier9;

      // Flow.SpectrumDbReader
      adams.flow.transformer.SpectrumDbReader spectrumdbreader13 = new adams.flow.transformer.SpectrumDbReader();
      argOption = (AbstractArgumentOption) spectrumdbreader13.getOptionManager().findByProperty("postProcessor");
      adams.flow.transformer.datacontainer.NoPostProcessing nopostprocessing15 = new adams.flow.transformer.datacontainer.NoPostProcessing();
      spectrumdbreader13.setPostProcessor(nopostprocessing15);

      actors1[3] = spectrumdbreader13;

      // Flow.Tee
      adams.flow.control.Tee tee16 = new adams.flow.control.Tee();
      argOption = (AbstractArgumentOption) tee16.getOptionManager().findByProperty("actors");
      Actor[] actors17 = new Actor[2];

      // Flow.Tee.SpectrumInfo
      adams.flow.transformer.SpectrumInfo spectruminfo18 = new adams.flow.transformer.SpectrumInfo();
      actors17[0] = spectruminfo18;

      // Flow.Tee.GlobalSink
      adams.flow.sink.CallableSink globalsink19 = new adams.flow.sink.CallableSink();
      argOption = (AbstractArgumentOption) globalsink19.getOptionManager().findByProperty("callableName");
      globalsink19.setCallableName((adams.flow.core.CallableActorReference) argOption.valueOf("Display"));
      actors17[1] = globalsink19;
      tee16.setActors(actors17);

      actors1[4] = tee16;

      // Flow.Tee-1
      adams.flow.control.Tee tee21 = new adams.flow.control.Tee();
      argOption = (AbstractArgumentOption) tee21.getOptionManager().findByProperty("name");
      tee21.setName((String) argOption.valueOf("Tee-1"));
      argOption = (AbstractArgumentOption) tee21.getOptionManager().findByProperty("actors");
      Actor[] actors23 = new Actor[2];

      // Flow.Tee-1.SpectrumInfo
      adams.flow.transformer.SpectrumInfo spectruminfo24 = new adams.flow.transformer.SpectrumInfo();
      argOption = (AbstractArgumentOption) spectruminfo24.getOptionManager().findByProperty("type");
      spectruminfo24.setType((adams.flow.transformer.SpectrumInfo.InfoType) argOption.valueOf("DB_ID"));
      actors23[0] = spectruminfo24;

      // Flow.Tee-1.GlobalSink
      adams.flow.sink.CallableSink globalsink26 = new adams.flow.sink.CallableSink();
      argOption = (AbstractArgumentOption) globalsink26.getOptionManager().findByProperty("callableName");
      globalsink26.setCallableName((adams.flow.core.CallableActorReference) argOption.valueOf("Display"));
      actors23[1] = globalsink26;
      tee21.setActors(actors23);

      actors1[5] = tee21;

      // Flow.Tee-2
      adams.flow.control.Tee tee28 = new adams.flow.control.Tee();
      argOption = (AbstractArgumentOption) tee28.getOptionManager().findByProperty("name");
      tee28.setName((String) argOption.valueOf("Tee-2"));
      argOption = (AbstractArgumentOption) tee28.getOptionManager().findByProperty("actors");
      Actor[] actors30 = new Actor[2];

      // Flow.Tee-2.SpectrumInfo
      adams.flow.transformer.SpectrumInfo spectruminfo31 = new adams.flow.transformer.SpectrumInfo();
      argOption = (AbstractArgumentOption) spectruminfo31.getOptionManager().findByProperty("type");
      spectruminfo31.setType((adams.flow.transformer.SpectrumInfo.InfoType) argOption.valueOf("NUM_WAVES"));
      actors30[0] = spectruminfo31;

      // Flow.Tee-2.GlobalSink
      adams.flow.sink.CallableSink globalsink33 = new adams.flow.sink.CallableSink();
      argOption = (AbstractArgumentOption) globalsink33.getOptionManager().findByProperty("callableName");
      globalsink33.setCallableName((adams.flow.core.CallableActorReference) argOption.valueOf("Display"));
      actors30[1] = globalsink33;
      tee28.setActors(actors30);

      actors1[6] = tee28;

      // Flow.Tee-3
      adams.flow.control.Tee tee35 = new adams.flow.control.Tee();
      argOption = (AbstractArgumentOption) tee35.getOptionManager().findByProperty("name");
      tee35.setName((String) argOption.valueOf("Tee-3"));
      argOption = (AbstractArgumentOption) tee35.getOptionManager().findByProperty("actors");
      Actor[] actors37 = new Actor[2];

      // Flow.Tee-3.SpectrumInfo
      adams.flow.transformer.SpectrumInfo spectruminfo38 = new adams.flow.transformer.SpectrumInfo();
      argOption = (AbstractArgumentOption) spectruminfo38.getOptionManager().findByProperty("type");
      spectruminfo38.setType((adams.flow.transformer.SpectrumInfo.InfoType) argOption.valueOf("MIN_WAVE"));
      actors37[0] = spectruminfo38;

      // Flow.Tee-3.GlobalSink
      adams.flow.sink.CallableSink globalsink40 = new adams.flow.sink.CallableSink();
      argOption = (AbstractArgumentOption) globalsink40.getOptionManager().findByProperty("callableName");
      globalsink40.setCallableName((adams.flow.core.CallableActorReference) argOption.valueOf("Display"));
      actors37[1] = globalsink40;
      tee35.setActors(actors37);

      actors1[7] = tee35;

      // Flow.Tee-4
      adams.flow.control.Tee tee42 = new adams.flow.control.Tee();
      argOption = (AbstractArgumentOption) tee42.getOptionManager().findByProperty("name");
      tee42.setName((String) argOption.valueOf("Tee-4"));
      argOption = (AbstractArgumentOption) tee42.getOptionManager().findByProperty("actors");
      Actor[] actors44 = new Actor[2];

      // Flow.Tee-4.SpectrumInfo
      adams.flow.transformer.SpectrumInfo spectruminfo45 = new adams.flow.transformer.SpectrumInfo();
      argOption = (AbstractArgumentOption) spectruminfo45.getOptionManager().findByProperty("type");
      spectruminfo45.setType((adams.flow.transformer.SpectrumInfo.InfoType) argOption.valueOf("MAX_WAVE"));
      actors44[0] = spectruminfo45;

      // Flow.Tee-4.GlobalSink
      adams.flow.sink.CallableSink globalsink47 = new adams.flow.sink.CallableSink();
      argOption = (AbstractArgumentOption) globalsink47.getOptionManager().findByProperty("callableName");
      globalsink47.setCallableName((adams.flow.core.CallableActorReference) argOption.valueOf("Display"));
      actors44[1] = globalsink47;
      tee42.setActors(actors44);

      actors1[8] = tee42;

      // Flow.Tee-5
      adams.flow.control.Tee tee49 = new adams.flow.control.Tee();
      argOption = (AbstractArgumentOption) tee49.getOptionManager().findByProperty("name");
      tee49.setName((String) argOption.valueOf("Tee-5"));
      argOption = (AbstractArgumentOption) tee49.getOptionManager().findByProperty("actors");
      Actor[] actors51 = new Actor[2];

      // Flow.Tee-5.SpectrumInfo
      adams.flow.transformer.SpectrumInfo spectruminfo52 = new adams.flow.transformer.SpectrumInfo();
      argOption = (AbstractArgumentOption) spectruminfo52.getOptionManager().findByProperty("type");
      spectruminfo52.setType((adams.flow.transformer.SpectrumInfo.InfoType) argOption.valueOf("MIN_AMPLITUDE"));
      actors51[0] = spectruminfo52;

      // Flow.Tee-5.GlobalSink
      adams.flow.sink.CallableSink globalsink54 = new adams.flow.sink.CallableSink();
      argOption = (AbstractArgumentOption) globalsink54.getOptionManager().findByProperty("callableName");
      globalsink54.setCallableName((adams.flow.core.CallableActorReference) argOption.valueOf("Display"));
      actors51[1] = globalsink54;
      tee49.setActors(actors51);

      actors1[9] = tee49;

      // Flow.Tee-6
      adams.flow.control.Tee tee56 = new adams.flow.control.Tee();
      argOption = (AbstractArgumentOption) tee56.getOptionManager().findByProperty("name");
      tee56.setName((String) argOption.valueOf("Tee-6"));
      argOption = (AbstractArgumentOption) tee56.getOptionManager().findByProperty("actors");
      Actor[] actors58 = new Actor[2];

      // Flow.Tee-6.SpectrumInfo
      adams.flow.transformer.SpectrumInfo spectruminfo59 = new adams.flow.transformer.SpectrumInfo();
      argOption = (AbstractArgumentOption) spectruminfo59.getOptionManager().findByProperty("type");
      spectruminfo59.setType((adams.flow.transformer.SpectrumInfo.InfoType) argOption.valueOf("MAX_AMPLITUDE"));
      actors58[0] = spectruminfo59;

      // Flow.Tee-6.GlobalSink
      adams.flow.sink.CallableSink globalsink61 = new adams.flow.sink.CallableSink();
      argOption = (AbstractArgumentOption) globalsink61.getOptionManager().findByProperty("callableName");
      globalsink61.setCallableName((adams.flow.core.CallableActorReference) argOption.valueOf("Display"));
      actors58[1] = globalsink61;
      tee56.setActors(actors58);

      actors1[10] = tee56;
      flow.setActors(actors1);

      argOption = (AbstractArgumentOption) flow.getOptionManager().findByProperty("flowExecutionListener");
      adams.flow.execution.NullListener nulllistener64 = new adams.flow.execution.NullListener();
      flow.setFlowExecutionListener(nulllistener64);

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

