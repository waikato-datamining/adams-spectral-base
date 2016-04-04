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
 * SampleIdSource.java
 * Copyright (C) 2013 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.source;

/**
 * Interface for ID sources that output either database IDs (= integers)
 * or sample IDs (= strings).
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1866 $
 */
public interface SampleIdSource
  extends SpectrumIdSource {

  /**
   * Sets whether to generate database IDs or sample IDs.
   *
   * @param value	if true then sample IDs are generated
   */
  public void setGenerateSampleIDs(boolean value);

  /**
   * Returns whether to read from the active or store table.
   *
   * @return		true if the store table is used
   */
  public boolean getGenerateSampleIDs();

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String generateSampleIDsTipText();
}
