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
 * MultiSpectrumFilter.java
 * Copyright (C) 2014-2017 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.ClassCrossReference;
import adams.core.QuickInfoHelper;
import adams.core.base.BaseRegExp;
import adams.data.conversion.SpectraToMultiSpectrum;
import adams.data.multifilter.AbstractMultiSpectrumFilter;
import adams.data.report.AbstractField;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;
import adams.db.DatabaseConnectionHandler;
import adams.db.DatabaseConnectionUser;
import adams.flow.core.ActorUtils;
import adams.flow.core.Token;
import adams.flow.provenance.ActorType;
import adams.flow.provenance.Provenance;
import adams.flow.provenance.ProvenanceContainer;
import adams.flow.provenance.ProvenanceInformation;

/**
 <!-- globalinfo-start -->
 * Generates a single spectrum from a multi-spectrum using the specified filter.<br>
 * For some filters that use several spectra, the order of the spectra is important. The internal order of the MultiSpectrum is used to pick the spectra, which can be influenced via custom spectrum comparators.<br>
 * <br>
 * See also:<br>
 * adams.data.conversion.SpectraToMultiSpectrum<br>
 * adams.flow.transformer.SpectrumFileReader
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;adams.data.spectrum.MultiSpectrum<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;adams.data.spectrum.Spectrum<br>
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
 * &nbsp;&nbsp;&nbsp;default: MultiSpectrumFilter
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
 * &nbsp;&nbsp;&nbsp;If set to true, the flow execution at this level gets stopped in case this
 * &nbsp;&nbsp;&nbsp;actor encounters an error; the error gets propagated; useful for critical
 * &nbsp;&nbsp;&nbsp;actors.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-silent &lt;boolean&gt; (property: silent)
 * &nbsp;&nbsp;&nbsp;If enabled, then no errors are output in the console; Note: the enclosing
 * &nbsp;&nbsp;&nbsp;actor handler must have this enabled as well.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-filter &lt;adams.data.multifilter.AbstractMultiSpectrumFilter&gt; (property: filter)
 * &nbsp;&nbsp;&nbsp;The filter to use.
 * &nbsp;&nbsp;&nbsp;default: adams.data.multifilter.PickByIndex
 * </pre>
 *
 * <pre>-transfer-report &lt;boolean&gt; (property: transferReport)
 * &nbsp;&nbsp;&nbsp;If enabled, the report values from the multi-spectrum get transferred into
 * &nbsp;&nbsp;&nbsp;the sub-spectra.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-transfer-prefix &lt;java.lang.String&gt; (property: transferPrefix)
 * &nbsp;&nbsp;&nbsp;The (optional) prefix for report fields that get transferred.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-transfer-regexp &lt;adams.core.base.BaseRegExp&gt; (property: transferRegExp)
 * &nbsp;&nbsp;&nbsp;The regular expression that the field names must match in order to get transferred.
 * &nbsp;&nbsp;&nbsp;default: .*
 * &nbsp;&nbsp;&nbsp;more: https:&#47;&#47;docs.oracle.com&#47;javase&#47;tutorial&#47;essential&#47;regex&#47;
 * &nbsp;&nbsp;&nbsp;https:&#47;&#47;docs.oracle.com&#47;javase&#47;8&#47;docs&#47;api&#47;java&#47;util&#47;regex&#47;Pattern.html
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class MultiSpectrumFilter
  extends AbstractTransformer
  implements DatabaseConnectionUser, ClassCrossReference {

  /** for serialization. */
  private static final long serialVersionUID = -8678582872628608282L;
  
  /** the filter to apply. */
  protected AbstractMultiSpectrumFilter m_Filter;

  /** whether to transfer the multi-spectrum's report into the sub-spectra. */
  protected boolean m_TransferReport;
  
  /** the (optional) prefix for fields from the multi-spectrum. */
  protected String m_TransferPrefix;
  
  /** the regular expression for field names to transfer. */
  protected BaseRegExp m_TransferRegExp;

  /** whether the database connection has been updated. */
  protected boolean m_DatabaseConnectionUpdated;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Generates a single spectrum from a multi-spectrum using the specified filter.\n"
      + "For some filters that use several spectra, the order of the spectra is important. "
      + "The internal order of the MultiSpectrum is used to pick the spectra, which can "
      + "be influenced via custom spectrum comparators.";
  }

  /**
   * Returns the cross-referenced classes.
   *
   * @return		the classes
   */
  public Class[] getClassCrossReferences() {
    return new Class[]{SpectraToMultiSpectrum.class, SpectrumFileReader.class};
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "filter", "filter",
	    new adams.data.multifilter.PickByIndex());

    m_OptionManager.add(
	    "transfer-report", "transferReport",
	    false);

    m_OptionManager.add(
	    "transfer-prefix", "transferPrefix",
	    "");

    m_OptionManager.add(
	    "transfer-regexp", "transferRegExp",
	    new BaseRegExp(BaseRegExp.MATCH_ALL));
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
  public void setFilter(AbstractMultiSpectrumFilter value) {
    m_Filter = value;
    reset();
  }

  /**
   * Returns the filter in use.
   *
   * @return		the filter
   */
  public AbstractMultiSpectrumFilter getFilter() {
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
   * Sets whether to transfer the report values from the multi-spectrum
   * into the sub-spectra.
   *
   * @param value	true if to transfer
   */
  public void setTransferReport(boolean value) {
    m_TransferReport = value;
    reset();
  }

  /**
   * Returns whether to transfer the report values from the multi-spectrum
   * into the sub-spectra.
   *
   * @return 		true if to transfer
   */
  public boolean getTransferReport() {
    return m_TransferReport;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String transferReportTipText() {
    return "If enabled, the report values from the multi-spectrum get transferred into the sub-spectra.";
  }

  /**
   * Sets the (optional) prefix for report fields that get transferred.
   *
   * @param value	the prefix
   */
  public void setTransferPrefix(String value) {
    m_TransferPrefix = value;
    reset();
  }

  /**
   * Returns the (optional) prefix for report fields that get transferred.
   *
   * @return 		the prefix
   */
  public String getTransferPrefix() {
    return m_TransferPrefix;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String transferPrefixTipText() {
    return "The (optional) prefix for report fields that get transferred.";
  }

  /**
   * Sets the regular expression that the field names must match in order
   * to get transferred.
   *
   * @param value	the regular expression
   */
  public void setTransferRegExp(BaseRegExp value) {
    m_TransferRegExp = value;
    reset();
  }

  /**
   * Returns the regular expression that the field names must match in order
   * to get transferred.
   *
   * @return 		the regular expression
   */
  public BaseRegExp getTransferRegExp() {
    return m_TransferRegExp;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String transferRegExpTipText() {
    return "The regular expression that the field names must match in order to get transferred.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;
    
    result  = QuickInfoHelper.toString(this, "filter", m_Filter, "filter: ");
    result += QuickInfoHelper.toString(this, "transferReport", (m_TransferReport ? "transfer" : "no transfer"), ", ");
    result += QuickInfoHelper.toString(this, "transferPrefix", (m_TransferPrefix.isEmpty() ? "-none-" : m_TransferPrefix), ", prefix: ");
    result += QuickInfoHelper.toString(this, "transferPrefix", m_TransferRegExp, ", regexp: ");
    
    return result;
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
    return new Class[]{Spectrum.class};
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
    Spectrum		spec;
    SampleData		sdm;
    SampleData		sd;
    AbstractField	newField;

    result = null;

    if (!m_DatabaseConnectionUpdated) {
      m_DatabaseConnectionUpdated = true;
      if (m_Filter instanceof DatabaseConnectionHandler)
	((DatabaseConnectionHandler) m_Filter).setDatabaseConnection(getDatabaseConnection());
    }

    multi = (MultiSpectrum) m_InputToken.getPayload();
    sdm   = multi.getReport();
    spec  = m_Filter.filter(multi);
    if (spec != null) {
      spec = (Spectrum) spec.getClone();
      // transfer report?
      if (m_TransferReport && (sdm != null)) {
	sd = spec.getReport();
	if (sd == null)
	  sd = new SampleData();
	for (AbstractField field: sdm.getFields()) {
	  if (m_TransferRegExp.isMatch(field.getName())) {
	    if (m_TransferPrefix.isEmpty())
	      newField = field;
	    else
	      newField = new Field(m_TransferPrefix + field.getName(), field.getDataType());
	    sd.addField(newField);
	    sd.setValue(newField, sdm.getValue(field));
	  }
	}
      }
      m_OutputToken = new Token(spec);
    }
    else {
      getLogger().warning("Failed to obtain spectrum from " + multi + " using " + m_Filter);
    }

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
