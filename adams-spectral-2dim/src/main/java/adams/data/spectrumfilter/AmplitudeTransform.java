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
 * AmplitudeTransform.java
 * Copyright (C) 2015 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.spectrumfilter.amplitudetransform.AbstractAmplitudeTransformer;
import adams.data.spectrum.Spectrum;

/**
 <!-- globalinfo-start -->
 * Applies the specified transformer to the spectrum.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-no-id-update &lt;boolean&gt; (property: dontUpdateID)
 * &nbsp;&nbsp;&nbsp;If enabled, suppresses updating the ID of adams.data.id.IDHandler data containers.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-transformer &lt;knir.data.filter.amplitudetransform.AbstractAmplitudeTransformer&gt; (property: transformer)
 * &nbsp;&nbsp;&nbsp;The transformer to apply.
 * &nbsp;&nbsp;&nbsp;default: knir.data.filter.amplitudetransform.PassThrough
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class AmplitudeTransform
  extends AbstractFilter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 2319957467336388607L;

  /** the transformer to apply. */
  protected AbstractAmplitudeTransformer m_Transformer;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return "Applies the specified transformer to the spectrum.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "transformer", "transformer",
      new adams.data.spectrumfilter.amplitudetransform.PassThrough());
  }

  /**
   * Sets the transformer to use.
   *
   * @param value	the transformer
   */
  public void setTransformer(AbstractAmplitudeTransformer value) {
    m_Transformer = value;
    reset();
  }

  /**
   * Returns the transformer to use.
   *
   * @return		the transformer
   */
  public AbstractAmplitudeTransformer getTransformer() {
    return m_Transformer;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String transformerTipText() {
    return "The transformer to apply.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  protected Spectrum processData(Spectrum data) {
    return m_Transformer.transform(data);
  }
}
