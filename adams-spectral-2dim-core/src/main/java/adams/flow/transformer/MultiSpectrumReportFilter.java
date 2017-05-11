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
 * MultiSpectrumReportFilter.java
 * Copyright (C) 2015-2017 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.QuickInfoHelper;
import adams.data.multireportfilter.AbstractMultiSpectrumReportFilter;
import adams.data.spectrum.MultiSpectrum;
import adams.db.DatabaseConnectionHandler;
import adams.flow.core.ActorUtils;
import adams.flow.core.Token;
import adams.flow.provenance.ActorType;
import adams.flow.provenance.Provenance;
import adams.flow.provenance.ProvenanceContainer;
import adams.flow.provenance.ProvenanceInformation;

/**
 <!-- globalinfo-start -->
 * Generates a single spectrum from a multi-spectrum using the specified filter.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;knir.data.spectrum.MultiSpectrum<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;knir.data.spectrum.MultiSpectrum<br>
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: MultiSpectrumReportFilter
 * </pre>
 * 
 * <pre>-annotation &lt;adams.core.base.BaseAnnotation&gt; (property: annotations)
 * &nbsp;&nbsp;&nbsp;The annotations to attach to this actor.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-skip &lt;boolean&gt; (property: skip)
 * &nbsp;&nbsp;&nbsp;If set to true, transformation is skipped and the input token is just forwarded 
 * &nbsp;&nbsp;&nbsp;as it is.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-stop-flow-on-error &lt;boolean&gt; (property: stopFlowOnError)
 * &nbsp;&nbsp;&nbsp;If set to true, the flow gets stopped in case this actor encounters an error;
 * &nbsp;&nbsp;&nbsp; useful for critical actors.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-silent &lt;boolean&gt; (property: silent)
 * &nbsp;&nbsp;&nbsp;If enabled, then no errors are output in the console.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-filter &lt;knir.data.multireportfilter.AbstractMultiSpectrumReportFilter&gt; (property: filter)
 * &nbsp;&nbsp;&nbsp;The filter to use.
 * &nbsp;&nbsp;&nbsp;default: knir.data.multireportfilter.PassThrough
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public class MultiSpectrumReportFilter
  extends AbstractTransformer {

  /** for serialization. */
  private static final long serialVersionUID = -8678582872628608282L;
  
  /** the filter to apply. */
  protected AbstractMultiSpectrumReportFilter m_Filter;

  /** whether the database connection has been updated. */
  protected boolean m_DatabaseConnectionUpdated;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Generates a single spectrum from a multi-spectrum using the specified filter.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "filter", "filter",
	    new adams.data.multireportfilter.PassThrough());
  }

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();

    m_DatabaseConnectionUpdated = false;
  }

  /**
   * Sets the filter to use.
   *
   * @param value	the filter
   */
  public void setFilter(AbstractMultiSpectrumReportFilter  value) {
    m_Filter = value;
    reset();
  }

  /**
   * Returns the filter in use.
   *
   * @return		the filter
   */
  public AbstractMultiSpectrumReportFilter  getFilter() {
    return m_Filter;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String filterTipText() {
    return "The filter to use.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    return QuickInfoHelper.toString(this, "filter", m_Filter, "filter: ");
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the default DataContainer class for the project
   */
  public Class[] accepts() {
    return new Class[]{MultiSpectrum.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the default DataContainer class for the project
   */
  public Class[] generates() {
    return new Class[]{MultiSpectrum.class};
  }

  /**
   * Determines the database connection in the flow.
   *
   * @return		the database connection to use
   */
  protected adams.db.AbstractDatabaseConnection getDatabaseConnection() {
    return ActorUtils.getDatabaseConnection(
	  this,
	  adams.flow.standalone.DatabaseConnectionProvider.class,
	  adams.db.DatabaseConnection.getSingleton());
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String		result;
    MultiSpectrum	multi;
    MultiSpectrum	spec;

    result = null;

    if (!m_DatabaseConnectionUpdated) {
      m_DatabaseConnectionUpdated = true;
      if (m_Filter instanceof DatabaseConnectionHandler)
	((DatabaseConnectionHandler) m_Filter).setDatabaseConnection(getDatabaseConnection());
    }

    multi = (MultiSpectrum) m_InputToken.getPayload();
    spec  = m_Filter.filter(multi);
    if (spec != null)
      m_OutputToken = new Token(spec);
    else
      getLogger().warning("Failed to obtain spectrum from " + multi + " using " + m_Filter);

    if (m_OutputToken != null)
      updateProvenance(m_OutputToken);

    m_Filter.cleanUp();

    return result;
  }

  /**
   * Updates the provenance information in the provided container.
   *
   * @param cont	the provenance container to update
   */
  public void updateProvenance(ProvenanceContainer cont) {
    if (Provenance.getSingleton().isEnabled()) {
      if (m_InputToken.hasProvenance())
	cont.setProvenance(m_InputToken.getProvenance().getClone());
      cont.addProvenance(new ProvenanceInformation(ActorType.PREPROCESSOR, m_InputToken.getPayload().getClass(), this, m_OutputToken.getPayload().getClass()));
    }
  }
}
