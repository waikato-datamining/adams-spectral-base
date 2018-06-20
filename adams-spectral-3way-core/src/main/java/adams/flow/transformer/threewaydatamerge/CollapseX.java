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
 * CollapseX.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer.threewaydatamerge;

import adams.data.statistics.StatUtils;
import adams.data.threeway.L1Point;
import adams.data.threeway.L2Point;
import adams.data.threeway.ThreeWayData;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates the mean/median/stdev data across the X layers.
 * This only works if the data containers share the same Y/Z values.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class CollapseX
  extends AbstractThreeWayDataMerge {

  private static final long serialVersionUID = -197127467469490678L;

  /** the statistic to calculate from the data values. */
  public enum StatisticType {
    MEAN,
    MEDIAN,
    STDEV,
  }

  /** the type of statistic to calculate. */
  protected StatisticType m_Statistic;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Calculates the mean/median/stdev data across the X layers.\n"
	+ "This only works if the data containers share the same Y/Z values.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "statistic", "statistic",
      StatisticType.MEDIAN);
  }

  /**
   * Sets the type of statistic to calculate.
   *
   * @param value 	the type
   */
  public void setStatistic(StatisticType value) {
    m_Statistic = value;
    reset();
  }

  /**
   * Returns the type of statistic to calculate.
   *
   * @return 		the type
   */
  public StatisticType getStatistic() {
    return m_Statistic;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String statisticTipText() {
    return "The type of statistic to calculate.";
  }

  /**
   * Merges the data containers into a single one.
   *
   * @param data	the data to merge
   * @return		the merged container
   */
  @Override
  protected ThreeWayData doMerge(ThreeWayData[] data) {
    ThreeWayData	result;
    TDoubleList		x;
    TDoubleList		y;
    TDoubleList		z;
    int			d;
    int			i;
    int 		j;
    int			k;
    L1Point		l1new;
    L2Point		l2new;
    double[]		values;
    double 		stats;
    List<Map<Double,double[][]>> 	layers;
    Map<Double,double[][]>		map;

    if (data.length == 1)
      return (ThreeWayData) data[0].getClone();

    result = (ThreeWayData) data[0].getHeader();

    // initialize layers
    x = new TDoubleArrayList();
    y = data[0].getAllY();
    z = data[0].getAllZ();
    for (d = 0; d < data.length; d++)
      x.addAll(data[d].getAllX());
    layers = new ArrayList<>();
    for (d = 0; d < data.length; d++) {
      map = new HashMap<>();
      layers.add(map);
      for (i = 0; i < x.size(); i++) {
	map.put(x.get(i), new double[y.size()][z.size()]);
      }
    }

    // fill layers
    for (d = 0; d < data.length; d++) {
      map = layers.get(d);
      for (L1Point l1 : data[d]) {
	for (L2Point l2: l1)
	  map.get(l1.getX())[y.indexOf(l1.getY())][z.indexOf(l2.getZ())] = l2.getData();
      }
    }

    // compute median
    for (j = 0; j < y.size(); j++) {
      l1new = new L1Point(0.0, y.get(j));
      result.add(l1new);
      for (k = 0; k < z.size(); k++) {
	values = new double[data.length];
	for (i = 0; i < x.size(); i++) {
	  for (d = 0; d < data.length; d++)
	    values[d] += layers.get(d).get(x.get(i))[j][k];
	}
	switch (m_Statistic) {
	  case MEAN:
	    stats = StatUtils.mean(values);
	    break;
	  case MEDIAN:
	    stats = StatUtils.median(values);
	    break;
	  case STDEV:
	    stats = StatUtils.stddev(values, true);
	    break;
	  default:
	    throw new IllegalStateException("Unhandled statistic type: " + m_Statistic);
	}
	l2new = new L2Point(z.get(k), stats);
	l1new.add(l2new);
      }
    }

    return result;
  }
}
