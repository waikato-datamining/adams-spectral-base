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
 * AbstractFormatsBasedMultiSpectrumFilter.java
 * Copyright (C) 2015 University of Waikato, Hamilton, New Zealand
 */
package adams.data.multifilter;

import adams.core.base.BaseString;

/**
 * Ancestor for filters that require the user to specify the formats
 * of the spectra to use for the filtering process.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractFormatsBasedMultiSpectrumFilter
  extends AbstractMultiSpectrumFilter {

  /** for serialization. */
  private static final long serialVersionUID = 396771111601239664L;
  
  /** the formats to average (empty = all).*/
  protected BaseString[] m_Formats;

  /** the new format to use. */
  protected String m_NewFormat;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "format", "formats",
	    new BaseString[0]);

    m_OptionManager.add(
      "new-format", "newFormat",
	    getDefaultNewFormat());
  }

  /**
   * Sets the formats of the spectra to use.
   *
   * @param value 	the formats
   */
  public void setFormats(BaseString[] value) {
    if (value == null)
      value = new BaseString[0];
    m_Formats = value;
    reset();
  }

  /**
   * Returns the formats of the specta to use.
   *
   * @return 		the formats
   */
  public BaseString[] getFormats() {
    return m_Formats;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public abstract String formatsTipText();

  /**
   * Returns the default format for the generated spectrum.
   *
   * @return		the default format
   */
  protected abstract String getDefaultNewFormat();

  /**
   * Sets the new format for the generated spectrum.
   *
   * @param value 	the new format
   */
  public void setNewFormat(String value) {
    m_NewFormat = value;
    reset();
  }

  /**
   * Returns the new format for the generated spectrum.
   *
   * @return 		the new format
   */
  public String getNewFormat() {
    return m_NewFormat;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public abstract String newFormatTipText();
}
