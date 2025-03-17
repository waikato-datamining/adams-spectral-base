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
 * MultiSpectrumRemove.java
 * Copyright (C) 2014-2025 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.QuickInfoHelper;
import adams.core.annotation.DeprecatedClass;
import adams.core.base.BaseRegExp;
import adams.data.multispectrumoperation.Remove;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;
import adams.flow.core.Token;

/**
 <!-- globalinfo-start -->
 * Removes all spectra from the multi-spectrum that match the criteria.
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
 * &nbsp;&nbsp;&nbsp;default: RemoveFromMultiSpectrum
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
 * <pre>-sample-id &lt;adams.core.base.BaseRegExp&gt; (property: sampleID)
 * &nbsp;&nbsp;&nbsp;The regular expression that the sample ID of a spectrum must match in order 
 * &nbsp;&nbsp;&nbsp;to get removed.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-sample-type &lt;adams.core.base.BaseRegExp&gt; (property: sampleType)
 * &nbsp;&nbsp;&nbsp;The regular expression that the sample type of a spectrum must match in 
 * &nbsp;&nbsp;&nbsp;order to get removed.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-format &lt;adams.core.base.BaseRegExp&gt; (property: format)
 * &nbsp;&nbsp;&nbsp;The regular expression that the format of a spectrum must match in order 
 * &nbsp;&nbsp;&nbsp;to get removed.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
@DeprecatedClass(
  useInstead = {MultiSpectrumOperation.class, Remove.class}
)
public class MultiSpectrumRemove
  extends AbstractTransformer {

  /** for serialization. */
  private static final long serialVersionUID = -8678582872628608282L;
  
  /** the regular expression for the sample ID. */
  protected BaseRegExp m_SampleID;
  
  /** the regular expression for the sample type. */
  protected BaseRegExp m_SampleType;
  
  /** the regular expression for the format. */
  protected BaseRegExp m_Format;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Removes all spectra from the multi-spectrum that match the criteria.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "sample-id", "sampleID",
	    new BaseRegExp(""));

    m_OptionManager.add(
	    "sample-type", "sampleType",
	    new BaseRegExp(""));

    m_OptionManager.add(
	    "format", "format",
	    new BaseRegExp(""));
  }

  /**
   * Sets the regular expression that the sample ID of a spectrum must match 
   * in order to get removed.
   *
   * @param value	the regular expression
   */
  public void setSampleID(BaseRegExp value) {
    m_SampleID = value;
    reset();
  }

  /**
   * Returns the regular expression that the sample ID of a spectrum must match 
   * in order to get removed.
   *
   * @return 		the regular expression
   */
  public BaseRegExp getSampleID() {
    return m_SampleID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sampleIDTipText() {
    return "The regular expression that the sample ID of a spectrum must match in order to get removed.";
  }

  /**
   * Sets the regular expression that the sample type of a spectrum must match
   * in order to get removed.
   *
   * @param value	the regular expression
   */
  public void setSampleType(BaseRegExp value) {
    m_SampleType = value;
    reset();
  }

  /**
   * Returns the regular expression that the sample Type of a spectrum must match 
   * in order to get removed.
   *
   * @return 		the regular expression
   */
  public BaseRegExp getSampleType() {
    return m_SampleType;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sampleTypeTipText() {
    return "The regular expression that the sample type of a spectrum must match in order to get removed.";
  }

  /**
   * Sets the regular expression that the format of a spectrum must match 
   * in order to get removed.
   *
   * @param value	the regular expression
   */
  public void setFormat(BaseRegExp value) {
    m_Format = value;
    reset();
  }

  /**
   * Returns the regular expression that the format of a spectrum must match 
   * in order to get removed.
   *
   * @return 		the regular expression
   */
  public BaseRegExp getFormat() {
    return m_Format;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String formatTipText() {
    return "The regular expression that the format of a spectrum must match in order to get removed.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;
    
    result  = QuickInfoHelper.toString(this, "sampleID", (m_SampleID.isEmpty() ? "-none-" : m_SampleID), "ID: ");
    result += QuickInfoHelper.toString(this, "sampleType", (m_SampleType.isEmpty() ? "-none-" : m_SampleType), ", type: ");
    result += QuickInfoHelper.toString(this, "format", (m_Format.isEmpty() ? "-none-" : m_Format), ", format: ");
    
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
    return new Class[]{MultiSpectrum.class};
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
    MultiSpectrum	filtered;
    int			i;
    Spectrum		spec;

    result = null;

    multi    = (MultiSpectrum) m_InputToken.getPayload();
    filtered = (MultiSpectrum) multi.getHeader();
    for (i = 0; i < multi.size(); i++) {
      spec = multi.toList().get(i);
      if (!m_Format.isEmpty() && m_Format.isMatch(spec.getFormat()))
	continue;
      if (!m_SampleID.isEmpty() && m_SampleID.isMatch(spec.getID()))
	continue;
      if (   !m_SampleType.isEmpty() && spec.hasReport() 
	  && spec.getReport().hasValue(SampleData.SAMPLE_TYPE) 
	  && m_SampleType.isMatch(spec.getReport().getStringValue(SampleData.SAMPLE_TYPE)) )
	continue;
      filtered.add((Spectrum) spec.getClone());
    }

    m_OutputToken = new Token(filtered);

    return result;
  }
}
