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
 * Join.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer.threewaydatamerge;

import adams.data.threeway.L1Point;
import adams.data.threeway.ThreeWayData;

/**
 * Simply merges all the data points into a single container.
 * May remove already existing ones.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Join
  extends AbstractThreeWayDataMerge {

  private static final long serialVersionUID = -784874739859607194L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Simply merges all the data points into a single container.\n"
      + "May remove already existing ones.";
  }

  /**
   * Merges the data containers into a single one.
   *
   * @param data	the data to merge
   * @return		the merged container
   */
  @Override
  protected ThreeWayData doMerge(ThreeWayData[] data) {
    ThreeWayData 	result;
    int			i;

    result = (ThreeWayData) data[0].getClone();
    for (i = 1; i < data.length; i++) {
      for (L1Point l1: data[i])
	result.add((L1Point) l1.getClone());
    }

    return result;
  }
}
