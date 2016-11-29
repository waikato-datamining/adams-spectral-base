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
 * AbstractSpectrumAnalysis.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumanalysis;

import adams.data.analysis.AbstractAnalysis;
import adams.data.spectrum.Spectrum;

import java.util.List;

/**
 * Ancestor for spectrum-based data analysis schemes.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractSpectrumAnalysis
  extends AbstractAnalysis<List<Spectrum>> {

  private static final long serialVersionUID = -789090762947882063L;

  /**
   * Hook method for checks.
   *
   * @param data	the data to check
   */
  @Override
  protected void check(List<Spectrum> data) {
    super.check(data);

    if (data.size() == 0)
      throw new IllegalStateException("No spectra provided!");
  }
}
