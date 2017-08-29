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
 * AbstractMultiplicativeScatterCorrection.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumfilter.multiplicativescattercorrection;

import adams.core.option.AbstractOptionHandler;
import adams.data.filter.Filter;
import adams.data.filter.PassThrough;
import adams.data.spectrum.Spectrum;
import adams.data.spectrumfilter.MultiplicativeScatterCorrection;

/**
 * Ancestor for schemes that perform multiplicative scatter correction.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractMultiplicativeScatterCorrection
  extends AbstractOptionHandler {

  private static final long serialVersionUID = 6930354949224477227L;

  /** the filter to apply to the spectra internally. */
  protected Filter<Spectrum> m_PreFilter;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "pre-filter", "preFilter",
      new PassThrough());
  }

  /**
   * Sets the prefilter to use.
   *
   * @param value 	the filter
   */
  public void setPreFilter(Filter<Spectrum> value) {
    m_PreFilter = value;
    reset();
  }

  /**
   * Returns the prefilter to use.
   *
   * @return 		the filter
   */
  public Filter<Spectrum> getPreFilter() {
    return m_PreFilter;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String preFilterTipText() {
    return
      "The filter to apply to the data internally; must be the same one that "
        + "is applied in the outer " + MultiplicativeScatterCorrection.class.getName() + " filter";
  }

  /**
   * Corrects the spectrum.
   *
   * @param average 	the average spectrum
   * @param data 	the spectrum to process
   * @return		the processed spectrum
   */
  public abstract Spectrum correct(Spectrum average, Spectrum data);
}
