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
 * Flatten.java
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
 * Simply flattens the 3-way data structure (x -> y -> z).
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Flatten
  extends AbstractThreeWayDataFeatureGenerator {

  private static final long serialVersionUID = 1731792416553098298L;

  /** the n-th point to use. */
  protected int m_NthPoint;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Simply flattens the 3-way data structure (x -> y -> z).";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "nth", "nthPoint",
      1, 1, null);
  }

  /**
   * Sets the nth point setting.
   *
   * @param value 	the nth point to use
   */
  public void setNthPoint(int value) {
    if (getOptionManager().isValid("nthPoint", value)) {
      m_NthPoint = value;
      reset();
    }
  }

  /**
   * Returns the nth point setting.
   *
   * @return 		the nth point
   */
  public int getNthPoint() {
    return m_NthPoint;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String nthPointTipText() {
    return "Only every n-th point will be output.";
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
    int			count;
    int			i;

    count = 0;
    for (L1Point l1: data)
      count += l1.size();

    result = new HeaderDefinition();
    for (i = 0; i < count; i++) {
      if (i % m_NthPoint == 0)
	result.add("Value-" + (i + 1), DataType.NUMERIC);
    }

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
    int			i;

    result = new List[1];
    result[0] = new ArrayList<>();

    i = 0;
    for (L1Point l1: data) {
      for (L2Point l2: l1) {
	if (i % m_NthPoint == 0)
	  result[0].add(l2.getData());
	i++;
      }
    }

    return result;
  }
}
