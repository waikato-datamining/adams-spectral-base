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
 * AbstractThreeWayDataMerge.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer.threewaydatamerge;

import adams.core.option.AbstractOptionHandler;
import adams.data.threeway.ThreeWayData;

/**
 * Ancestor for merge schemes.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractThreeWayDataMerge
  extends AbstractOptionHandler {

  private static final long serialVersionUID = 6228860062968639340L;

  /**
   * Hook method for checks before attempting to merge.
   * <br>
   * Default implementation only ensures that data is present.
   *
   * @param data	the data to check
   * @return		null if checks passed, otherwise error message
   */
  protected String check(ThreeWayData[] data) {
    if ((data == null) || (data.length == 0))
      return "No data provided!";
    return null;
  }

  /**
   * Merges the data containers into a single one.
   *
   * @param data	the data to merge
   * @return		the merged container
   */
  protected abstract ThreeWayData doMerge(ThreeWayData[] data);

  /**
   * Merges the data containers into a single one.
   *
   * @param data	the data to merge
   * @return		the merged container
   */
  public ThreeWayData merge(ThreeWayData[] data) {
    String	msg;

    msg = check(data);
    if (msg != null)
      throw new IllegalStateException(msg);

    return doMerge(data);
  }
}
