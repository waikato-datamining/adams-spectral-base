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
 * Max.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.threewayfeatures;

import adams.data.featureconverter.HeaderDefinition;
import adams.data.report.DataType;
import adams.data.threeway.L1Point;
import adams.data.threeway.L2Point;
import adams.data.threeway.ThreeWayData;

import java.util.ArrayList;
import java.util.List;

/**
 * Simply determines the largest data value.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Max
  extends AbstractThreeWayDataFeatureGenerator {

  private static final long serialVersionUID = 1731792416553098298L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Simply determines the largest data value.";
  }

  /**
   * Creates the header from template data.
   *
   * @param data	the data to act as a template
   * @return		the generated header
   */
  @Override
  public HeaderDefinition createHeader(ThreeWayData data) {
    HeaderDefinition	result;

    result = new HeaderDefinition();
    result.add("max", DataType.NUMERIC);

    return result;
  }

  /**
   * Performs the actual feature generation.
   *
   * @param data	the data to process
   * @return		the generated features
   */
  @Override
  public List<Object>[] generateRows(ThreeWayData data) {
    List<Object>[]	result;
    double max;

    result = new List[1];
    result[0] = new ArrayList<>();

    max = Double.NEGATIVE_INFINITY;
    for (L1Point l1: data) {
      for (L2Point l2: l1)
        max = Math.max(max, l2.getData());
    }
    result[0].add(max);

    return result;
  }
}
