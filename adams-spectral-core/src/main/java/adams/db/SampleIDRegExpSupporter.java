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
 * SampleIDRegExpSupporter.java
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package adams.db;

import adams.core.base.BaseRegExp;

/**
 * Interface for condition classes that support regular expressions for the
 * Sample ID.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 11831 $
 */
public interface SampleIDRegExpSupporter {

  /**
   * Sets the regular expression to use on the sample ID.
   *
   * @param value 	the regular expression
   */
  public void setSampleIDRegExp(BaseRegExp value);

  /**
   * Returns the regular expression to use on the sample ID.
   *
   * @return 		the regular expression
   */
  public BaseRegExp getSampleIDRegExp();
}
