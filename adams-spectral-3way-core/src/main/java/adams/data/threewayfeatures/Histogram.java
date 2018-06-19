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
 * Mean.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.threewayfeatures;

import adams.data.featureconverter.HeaderDefinition;
import adams.data.report.DataType;
import adams.data.statistics.AbstractArrayStatistic.StatisticContainer;
import adams.data.statistics.ArrayHistogram;
import adams.data.threeway.L1Point;
import adams.data.threeway.L2Point;
import adams.data.threeway.ThreeWayData;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a histogram from the data values.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Histogram
  extends AbstractThreeWayDataFeatureGenerator {

  private static final long serialVersionUID = 1731792416553098298L;

  /** determines how to generate the histogram. */
  protected ArrayHistogram m_Generator;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Generates a histogram from the data values.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "generator", "generator",
      new ArrayHistogram());
  }

  /**
   * Sets the histogram generator.
   *
   * @param value	the generator
   */
  public void setGenerator(ArrayHistogram value) {
    m_Generator = value;
    reset();
  }

  /**
   * Returns the histogram generator.
   *
   * @return		the generator
   */
  public ArrayHistogram getGenerator() {
    return m_Generator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String generatorTipText() {
    return "The histogram generator.";
  }

  /**
   * Returns all the data values as array.
   *
   * @param data	the container to convert
   * @return		the data values
   */
  protected Double[] toArray(ThreeWayData data) {
    List<Double>	result;

    result = new ArrayList<>();
    for (L1Point l1: data) {
      for (L2Point l2: l1)
        result.add(l2.getData());
    }

    return result.toArray(new Double[0]);
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
    ArrayHistogram	generator;
    StatisticContainer	cont;
    int			i;

    generator = (ArrayHistogram) m_Generator.shallowCopy();
    generator.clear();
    generator.add(toArray(data));
    cont = generator.calculate();

    result = new HeaderDefinition();
    for (i = 0; i < cont.getColumnCount(); i++)
      result.add(cont.getHeader(i), DataType.NUMERIC);

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
    ArrayHistogram	generator;
    StatisticContainer	cont;
    int			i;
    int			n;

    generator = (ArrayHistogram) m_Generator.shallowCopy();
    generator.clear();
    generator.add(toArray(data));
    cont   = generator.calculate();
    result = new List[cont.getRowCount()];
    for (n = 0; n < cont.getRowCount(); n++) {
      result[n] = new ArrayList<>();
      for (i = 0; i < cont.getColumnCount(); i++)
	result[n].add(cont.getCell(n, i));
    }

    return result;
  }
}
