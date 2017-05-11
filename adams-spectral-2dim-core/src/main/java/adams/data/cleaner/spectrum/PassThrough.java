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
 * PassThrough.java
 * Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 */

package adams.data.cleaner.spectrum;

import adams.data.spectrum.Spectrum;

/**
 <!-- globalinfo-start -->
 * Dummy cleaner that flags everything as clean.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 * 
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 * 
 <!-- options-end -->
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class PassThrough
  extends AbstractCleaner {
  
  /** for serialization. */
  private static final long serialVersionUID = -1612101002834979326L;

  /**
   * Returns a string describing the object.
   * 
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return "Dummy cleaner that flags everything as clean.";
  }
  
  /**
   * Performs the actual check.
   * 
   * @param data	the Spectrum to check
   * @return		always null (i.e., OK)
   */
  protected String performCheck(Spectrum data) {
    return null;
  }
}
