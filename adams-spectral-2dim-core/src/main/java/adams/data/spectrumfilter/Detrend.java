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
 * Detrend.java
 * Copyright (C) 2021 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrumfilter.detrend.AbstractDetrend;
import adams.data.spectrumfilter.detrend.RangeBased;

/**
 <!-- globalinfo-start -->
 * Performs Detrend corrections.
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
 * <pre>-correction &lt;adams.data.spectrumfilter.detrend.AbstractDetrend&gt; (property: correction)
 * &nbsp;&nbsp;&nbsp;The correction scheme to apply.
 * &nbsp;&nbsp;&nbsp;default: adams.data.spectrumfilter.detrend.RangeBased
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Detrend
  extends AbstractFilter<Spectrum> {

  private static final long serialVersionUID = 4945613765460222457L;

  /** the correction scheme to use. */
  protected AbstractDetrend m_Correction;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Performs Detrend corrections.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "correction", "correction",
      new RangeBased());
  }

  /**
   * Sets the correction scheme to use.
   *
   * @param value 	the correction
   */
  public void setCorrection(AbstractDetrend value) {
    m_Correction = value;
    reset();
  }

  /**
   * Returns the correction scheme in use.
   *
   * @return 		the correction
   */
  public AbstractDetrend getCorrection() {
    return m_Correction;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String correctionTipText() {
    return "The correction scheme to apply.";
  }

  /**
   * Performs the actual filtering. Just returns the input data if not trained.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  @Override
  protected Spectrum processData(Spectrum data) {
    return m_Correction.correct(data);
  }
}
