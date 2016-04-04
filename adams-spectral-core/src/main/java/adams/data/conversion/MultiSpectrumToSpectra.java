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

/**
 * MultiSpectrumToSpectra.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package adams.data.conversion;

import adams.core.ClassCrossReference;
import adams.core.base.BaseRegExp;
import adams.data.report.AbstractField;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Generates an array of knir.data.spectrum.Spectrum from the incoming knir.data.spectrum.MultiSpectrum.<br>
 * One can either transfer the data (all or partial) from the 'global' report to the sub-spectra or output the 'global' report as a separate (= first) spectrum with no spectral data points.<br>
 * <br>
 * See also:<br>
 * knir.data.conversion.SpectraToMultiSpectrum
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
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
 * </pre>
 * 
 * <pre>-output-report &lt;boolean&gt; (property: outputReport)
 * &nbsp;&nbsp;&nbsp;If enabled, the 'global' report is output as well in an empty spectrum (
 * &nbsp;&nbsp;&nbsp;ie report-only, no spectral data points; first spectrum in array).
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MultiSpectrumToSpectra
  extends AbstractConversion
  implements ClassCrossReference {

  /** for serialization. */
  private static final long serialVersionUID = 4279575764380906180L;

  /** whether to transfer the multi-spectrum's report into the sub-spectra. */
  protected boolean m_TransferReport;
  
  /** the (optional) prefix for fields from the multi-spectrum. */
  protected String m_TransferPrefix;
  
  /** the regular expression for field names to transfer. */
  protected BaseRegExp m_TransferRegExp;

  /** whether to output the "global" report in an empty spectrum. */
  protected boolean m_OutputReport;
  
  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return 
	"Generates an array of " + Spectrum.class.getName() 
	+ " from the incoming " + MultiSpectrum.class.getName() + ".\n"
	+ "One can either transfer the data (all or partial) from the 'global' report "
	+ "to the sub-spectra or output the 'global' report as a separate (= first) "
	+ "spectrum with no spectral data points.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "transfer-report", "transferReport",
	    false);

    m_OptionManager.add(
	    "transfer-prefix", "transferPrefix",
	    "");

    m_OptionManager.add(
	    "transfer-regexp", "transferRegExp",
	    new BaseRegExp(BaseRegExp.MATCH_ALL));

    m_OptionManager.add(
	    "output-report", "outputReport",
	    false);
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
   * Sets whether to output the global report of the multi-spectrum as separate
   * (= first) report-only spectrum (ie no spectral data).
   *
   * @param value	true if to output
   */
  public void setOutputReport(boolean value) {
    m_OutputReport = value;
    reset();
  }

  /**
   * Returns whether to output the global report of the multi-spectrum as separate
   * (= first) report-only spectrum (ie no spectral data).
   *
   * @return 		true if to output
   */
  public boolean getOutputReport() {
    return m_OutputReport;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String outputReportTipText() {
    return 
	"If enabled, the 'global' report is output as well in an empty "
	+ "spectrum (ie report-only, no spectral data points; first spectrum "
	+ "in array).";
  }

  /**
   * Returns the class that is accepted as input.
   *
   * @return		the class
   */
  @Override
  public Class accepts() {
    return MultiSpectrum.class;
  }

  /**
   * Returns the class that is generated as output.
   *
   * @return		the class
   */
  @Override
  public Class generates() {
    return Spectrum[].class;
  }

  /**
   * Returns the cross-referenced classes.
   *
   * @return		the classes
   */
  public Class[] getClassCrossReferences() {
    return new Class[]{SpectraToMultiSpectrum.class};
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  @Override
  protected Object doConvert() throws Exception {
    MultiSpectrum	input;
    SampleData		sdm;
    SampleData		sd;
    List<Spectrum>	output;
    Spectrum		out;
    int			i;
    AbstractField	newField;
    
    input  = (MultiSpectrum) m_Input;
    output = new ArrayList<Spectrum>();
    sdm    = input.getReport();
    
    if (m_OutputReport) {
      out = new Spectrum();
      out.setReport(input.getReport());
      out.setID(input.getID());
      output.add(out);
    }
    
    for (i = 0; i < input.size(); i++) {
      out = (Spectrum) input.toList().get(i).getClone();
      output.add(out);
      if (m_TransferReport && (sdm != null)) {
	sd = out.getReport();
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
	out.setReport(sd);
      }
    }
    
    return output.toArray(new Spectrum[output.size()]);
  }
}
