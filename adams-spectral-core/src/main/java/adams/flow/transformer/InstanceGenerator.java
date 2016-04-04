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
 * InstanceGenerator.java
 * Copyright (C) 2009-2011 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.data.report.Report;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.flow.core.ActorUtils;
import adams.flow.core.Token;
import weka.core.Instance;

/**
 <!-- globalinfo-start -->
 * Generates weka.core.Instance objects from spectra or reports&#47;sample data.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;knir.data.spectrum.Spectrum<br>
 * &nbsp;&nbsp;&nbsp;adams.data.report.Report<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;weka.core.Instance<br>
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D &lt;int&gt; (property: debugLevel)
 * &nbsp;&nbsp;&nbsp;The greater the number the more additional info the scheme may output to
 * &nbsp;&nbsp;&nbsp;the console (0 = off).
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 *
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: InstanceGenerator
 * </pre>
 *
 * <pre>-annotation &lt;adams.core.base.BaseText&gt; (property: annotations)
 * &nbsp;&nbsp;&nbsp;The annotations to attach to this actor.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-skip (property: skip)
 * &nbsp;&nbsp;&nbsp;If set to true, transformation is skipped and the input token is just forwarded
 * &nbsp;&nbsp;&nbsp;as it is.
 * </pre>
 *
 * <pre>-stop-flow-on-error (property: stopFlowOnError)
 * &nbsp;&nbsp;&nbsp;If set to true, the flow gets stopped in case this actor encounters an error;
 * &nbsp;&nbsp;&nbsp; useful for critical actors.
 * </pre>
 *
 * <pre>-generator &lt;knir.data.instances.AbstractInstanceGenerator&gt; (property: generator)
 * &nbsp;&nbsp;&nbsp;The generator to use for turning spectra into weka.core.Instance objects.
 * &nbsp;&nbsp;&nbsp;default: knir.data.instances.SimpleInstanceGenerator
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class InstanceGenerator
  extends AbstractTransformer {

  /** for serialization. */
  private static final long serialVersionUID = 9173099269238100664L;

  /** the generator to use. */
  protected adams.data.instances.AbstractInstanceGenerator m_Generator;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return "Generates weka.core.Instance objects from spectra or reports/sample data.";
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "generator", "generator",
	    new adams.data.instances.SimpleInstanceGenerator());
  }

  /**
   * Sets the generator to use.
   *
   * @param value	the generator
   */
  public void setGenerator(adams.data.instances.AbstractInstanceGenerator value){
    m_Generator = value;
    reset();
  }

  /**
   * Returns the generator in use.
   *
   * @return		the generator
   */
  public adams.data.instances.AbstractInstanceGenerator getGenerator(){
    return m_Generator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String generatorTipText() {
    return "The generator to use for turning spectra into weka.core.Instance objects.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  public String getQuickInfo() {
    String	variable;

    variable = getOptionManager().getVariableForProperty("generator");

    if (variable != null)
      return variable;
    else if (m_Generator != null)
      return m_Generator.getClass().getName();
    else
      return null;
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		<!-- flow-accepts-start -->knir.data.spectrum.Spectrum.class, adams.data.report.Report.class<!-- flow-accepts-end -->
   */
  public Class[] accepts() {
    return new Class[]{Spectrum.class, Report.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		<!-- flow-generates-start -->weka.core.Instance.class<!-- flow-generates-end -->
   */
  public Class[] generates() {
    return new Class[]{Instance.class};
  }

  /**
   * Initializes the item for flow execution. Also calls the reset() method
   * first before anything else.
   *
   * @return		null if everything is fine, otherwise error message
   */
  public String setUp() {
    String	result;

    result = super.setUp();

    if (result == null) {
      m_Generator.setDatabaseConnection(
	  ActorUtils.getDatabaseConnection(
	      this,
	      adams.flow.standalone.DatabaseConnection.class,
	      adams.db.DatabaseConnection.getSingleton()));
      result = m_Generator.checkSetup();
    }

    return result;
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  protected String doExecute() {
    String	result;
    Spectrum	spectrum;
    SampleData	sd;
    Instance	inst;

    result = null;

    if (m_InputToken.getPayload() instanceof Spectrum) {
      spectrum = (Spectrum) m_InputToken.getPayload();
    }
    else {
      if (m_InputToken.getPayload() instanceof SampleData) {
	sd = (SampleData) m_InputToken.getPayload();
      }
      else {
	sd = new SampleData();
	sd.mergeWith((Report) m_InputToken.getPayload());
      }
      spectrum = new Spectrum();
      spectrum.setDatabaseID(sd.getDatabaseID());
      spectrum.setID(sd.getID());
      spectrum.setReport(sd);
    }

    inst          = m_Generator.generate(spectrum);
    m_OutputToken = new Token(inst);

    return result;
  }
}
