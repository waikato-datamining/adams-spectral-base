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
 * SpectrumFileReader.java
 * Copyright (C) 2009-2019 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.ObjectCopyHelper;
import adams.data.conversion.SpectraToMultiSpectrum;
import adams.data.io.input.AbstractDataContainerReader;
import adams.data.io.input.SimpleSpectrumReader;
import adams.data.spectrum.AbstractSpectrumComparator;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumComparator;

/**
 <!-- globalinfo-start -->
 * Loads a file&#47;directory containing spectrums from disk with a specified reader and passes them on.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;java.lang.String<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;knir.data.spectrum.Spectrum<br>
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
 * &nbsp;&nbsp;&nbsp;default: SpectrumFileReader
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
 * <pre>-reader &lt;adams.data.io.input.AbstractDataContainerReader&gt; (property: reader)
 * &nbsp;&nbsp;&nbsp;The reader to use for importing the data.
 * &nbsp;&nbsp;&nbsp;default: knir.data.input.SimpleSpectrumReader
 * </pre>
 * 
 * <pre>-output-multispectrum &lt;boolean&gt; (property: outputMultiSpectrum)
 * &nbsp;&nbsp;&nbsp;If enabled, a knir.data.spectrum.MultiSpectrum is output instead of individual 
 * &nbsp;&nbsp;&nbsp;knir.data.spectrum.Spectrum.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumFileReader
  extends AbstractDataContainerFileReader<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 1429977151568224156L;

  /** whether to generate a MultiSpectrum instead. */
  protected boolean m_OutputMultiSpectrum;

  /** whether to use a custom comparator. */
  protected boolean m_UseCustomComparator;

  /** the custom comparator to use. */
  protected AbstractSpectrumComparator m_CustomComparator;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "Loads a file/directory containing spectrums from disk with a "
      + "specified reader and passes them on.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "output-multispectrum", "outputMultiSpectrum",
      false);

    m_OptionManager.add(
      "use-custom-comparator", "useCustomComparator",
      false);

    m_OptionManager.add(
      "custom-comparator", "customComparator",
      new SpectrumComparator());
  }

  /**
   * Returns the default reader to use.
   *
   * @return		the default reader
   */
  @Override
  protected AbstractDataContainerReader getDefaultReader() {
    return new SimpleSpectrumReader();
  }

  /**
   * Sets whether to output a {@link MultiSpectrum} instead.
   *
   * @param value	true if to output a {@link MultiSpectrum}
   */
  public void setOutputMultiSpectrum(boolean value) {
    m_OutputMultiSpectrum = value;
    reset();
  }

  /**
   * Returns whether to output a {@link MultiSpectrum} instead.
   *
   * @return		true if to output a {@link MultiSpectrum}
   */
  public boolean getOutputMultiSpectrum() {
    return m_OutputMultiSpectrum;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String outputMultiSpectrumTipText() {
    return "If enabled, a " + MultiSpectrum.class.getName() + " is output instead of individual " + Spectrum.class.getName() + ".";
  }

  /**
   * Sets whether to use a custom comparator.
   *
   * @param value 	true if to use custom comparator
   */
  public void setUseCustomComparator(boolean value) {
    m_UseCustomComparator = value;
    reset();
  }

  /**
   * Returns whether to use a custom comparator.
   *
   * @return 		true if to use a custom comparator
   */
  public boolean getUseCustomComparator() {
    return m_UseCustomComparator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String useCustomComparatorTipText() {
    return "If enabled, the specified custom comparator is used for sorting the spectra in the generated MultiSpectrum.";
  }

  /**
   * Sets the custom comparator.
   *
   * @param value 	the custom comparator
   */
  public void setCustomComparator(AbstractSpectrumComparator value) {
    m_CustomComparator = value;
    reset();
  }

  /**
   * Returns the custom comparator.
   *
   * @return 		the custom comparator
   */
  public AbstractSpectrumComparator getCustomComparator() {
    return m_CustomComparator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String customComparatorTipText() {
    return "The custom comparator to use for sorting the spectra in the generated MultiSpectrum.";
  }

  /**
   * Returns the base class of the items.
   *
   * @return		the class
   */
  @Override
  protected Class getItemClass() {
    if (m_OutputMultiSpectrum)
      return MultiSpectrum.class;
    else
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
    Spectrum[]			spectra;
    int				i;
    SpectraToMultiSpectrum	conv;
    MultiSpectrum		multi;
    
    result = super.doExecute();
    
    if ((result == null) && (m_OutputMultiSpectrum)) {
      spectra = new Spectrum[m_Containers.size()];
      for (i = 0; i < m_Containers.size(); i++)
	spectra[i] = (Spectrum) m_Containers.get(i);
      conv = new SpectraToMultiSpectrum();
      conv.setUseCustomComparator(m_UseCustomComparator);
      conv.setCustomComparator(ObjectCopyHelper.copyObject(m_CustomComparator));
      conv.setInput(spectra);
      result = conv.convert();
      m_Containers.clear();
      if (result == null) {
	multi = (MultiSpectrum) conv.getOutput();
	m_Containers.add(multi);
      }
      conv.cleanUp();
    }
    
    return result;
  }
}
