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
 * AbstractSpectrumWriter.java
 * Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.output;

import adams.core.ClassLister;
import adams.data.spectrum.Spectrum;

/**
 * Abstract ancestor for writers that write spectra to files in various
 * formats.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1968 $
 */
public abstract class AbstractSpectrumWriter
  extends AbstractDataContainerWriter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 7316606397547533438L;
  
  /**
   * Returns whether writing of multiple containers is supported.
   * 
   * @return 		true if multiple containers are supported
   */
  @Override
  public boolean canWriteMultiple() {
    return false;
  }

  /**
   * Returns a list with classnames of writers.
   *
   * @return the writer classnames
   */
  public static String[] getWriters() {
    return ClassLister.getSingleton().getClassnames(AbstractSpectrumWriter.class);
  }
}
