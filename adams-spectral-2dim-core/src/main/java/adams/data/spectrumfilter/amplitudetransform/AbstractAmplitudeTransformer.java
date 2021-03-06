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
 * AbstractAmplitudeTransformer.java
 * Copyright (C) 2015-2017 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumfilter.amplitudetransform;

import adams.core.option.AbstractOptionHandler;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

/**
 * Ancestor for amplitude transformation schemes.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractAmplitudeTransformer
  extends AbstractOptionHandler {

  private static final long serialVersionUID = 6930354949224477227L;

  /**
   * Hook method for initializing the transformer.
   * <br>
   * Default implementation does nothing.
   *
   * @param data	the spectrum to transform
   */
  protected void initialize(Spectrum data) {
  }

  /**
   * Transform the spectrum point and returns a new object.
   *
   * @param index	the 0-based index of the current point
   * @return		the new point
   */
  protected abstract SpectrumPoint transform(int index, SpectrumPoint point);

  /**
   * Transforms the spectrum.
   *
   * @param data 		the spectrum to transform
   * @return			the processed spectrum
   */
  public Spectrum transform(Spectrum data) {
    Spectrum	result;
    int		i;

    initialize(data);

    result = data.getHeader();
    for (i = 0; i < data.size(); i++)
      result.add(transform(i, data.toList().get(i)));

    return result;
  }
}
