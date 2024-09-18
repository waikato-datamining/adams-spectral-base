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
 * AbstractInPlaceSpectrumTransformer.java
 * Copyright (C) 2018-2024 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.transformer;

import adams.data.InPlaceProcessing;
import adams.data.spectrum.Spectrum;

/**
 * Ancestor for spectrum transformers that allow the processing to
 * happen in-place, rather than on a copy of the data.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractInPlaceSpectrumTransformer
  extends AbstractTransformer
  implements InPlaceProcessing {
  
  /** for serialization. */
  private static final long serialVersionUID = 2926699145420350035L;
  
  /** whether to skip creating a copy of the spreadsheet. */
  protected boolean m_NoCopy;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "no-copy", "noCopy",
      false);
  }

  /**
   * Sets whether to skip creating a copy of the spectrum before processing it.
   *
   * @param value	true if to skip creating copy
   */
  @Override
  public void setNoCopy(boolean value) {
    m_NoCopy = value;
    reset();
  }

  /**
   * Returns whether to skip creating a copy of the spectrum before processing it.
   *
   * @return		true if copying is skipped
   */
  @Override
  public boolean getNoCopy() {
    return m_NoCopy;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  @Override
  public String noCopyTipText() {
    return "If enabled, no copy of the spectrum is created before processing it.";
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the Class of objects that can be processed
   */
  @Override
  public Class[] accepts() {
    return new Class[]{Spectrum.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the Class of the generated tokens
   */
  @Override
  public Class[] generates() {
    return new Class[]{Spectrum.class};
  }
}
