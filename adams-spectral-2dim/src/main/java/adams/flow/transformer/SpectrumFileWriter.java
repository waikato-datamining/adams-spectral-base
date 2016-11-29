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
 * SpectrumFileWriter.java
 * Copyright (C) 2009-2014 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.data.io.output.AbstractDataContainerWriter;
import adams.data.conversion.MultiSpectrumToSpectra;
import adams.data.io.output.SimpleSpectrumWriter;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;

import java.lang.reflect.Array;

/**
 <!-- globalinfo-start -->
 * Saves a spectrum to disk with the specified writer and passes the absolute filename on.<br>
 * As filename&#47;directory name (depending on the writer) the database ID of the spectrum is used (below the specified output directory).
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;knir.data.spectrum.Spectrum<br>
 * &nbsp;&nbsp;&nbsp;knir.data.spectrum.Spectrum[]<br>
 * &nbsp;&nbsp;&nbsp;knir.data.spectrum.MultiSpectrum<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;java.lang.String<br>
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
 * &nbsp;&nbsp;&nbsp;default: SpectrumFileWriter
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
 * <pre>-writer &lt;adams.data.io.output.AbstractDataContainerWriter&gt; (property: writer)
 * &nbsp;&nbsp;&nbsp;The writer to use for saving the data.
 * &nbsp;&nbsp;&nbsp;default: knir.data.output.SimpleSpectrumWriter
 * </pre>
 * 
 * <pre>-dir &lt;adams.core.io.PlaceholderDirectory&gt; (property: outputDir)
 * &nbsp;&nbsp;&nbsp;The output directory for the data.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SpectrumFileWriter
  extends AbstractDataContainerFileWriter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -7990944411836957831L;

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
        "Saves a spectrum to disk with the "
      + "specified writer and passes the absolute filename on.\n"
      + "As filename/directory name (depending on the writer) the "
      + "database ID of the spectrum is used (below the specified output "
      + "directory).";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "output-report", "outputReport",
	    false);
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
   * Returns the default writer to use.
   *
   * @return		the default writer
   */
  @Override
  protected AbstractDataContainerWriter<Spectrum> getDefaultWriter() {
    return new SimpleSpectrumWriter();
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the data type
   */
  @Override
  public Class[] accepts() {
    Class	cls;

    cls = Array.newInstance(getDataContainerClass(), 0).getClass();

    return new Class[]{cls.getComponentType(), cls, MultiSpectrum.class};
  }

  /**
   * Returns the data container class in use.
   *
   * @return		the container class
   */
  @Override
  protected Class getDataContainerClass() {
    return Spectrum.class;
  }
  
  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String			result;
    MultiSpectrum		multi;
    MultiSpectrumToSpectra	conv;
    Spectrum[]			conts;
    
    if (m_InputToken.getPayload() instanceof MultiSpectrum) {
      multi    = (MultiSpectrum) m_InputToken.getPayload();
      conv     = new MultiSpectrumToSpectra();
      conv.setOutputReport(m_OutputReport);
      conv.setInput(multi);
      result = conv.convert();
      if (result == null) {
	conts = (Spectrum[]) conv.getOutput();
	result = doWrite(conts, conts[0]);
      }
      conv.cleanUp();
    }
    else {
      result = super.doExecute();
    }
    
    return result;
  }
}
