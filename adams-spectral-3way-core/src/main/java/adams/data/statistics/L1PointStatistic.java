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
 * L1PointStatistic.java
 * Copyright (C) 2017-2018 University of Waikato, Hamilton, New Zealand
 */

package adams.data.statistics;

import adams.data.threeway.L1Point;
import adams.data.threeway.L2Point;
import adams.data.threeway.ThreeWayData;

import java.util.List;

/**
 * A Level 1 point specific statistic.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class L1PointStatistic
  extends AbstractDataStatistic<L1Point> {

  /** for serialization. */
  private static final long serialVersionUID = 3716117305318401778L;

  /** the key for the database ID. */
  public final static String KEY_DBID = "Database ID";

  /** the key for abundance. */
  public final static String KEY_Y = "Y";

  /** the key for the timestamp. */
  public final static String KEY_X = "X";

  /** the key for number of MS points. */
  public final static String KEY_NUM_LEVEL2 = "Number of level 2 points";

  /** the key for min x. */
  public final static String KEY_MIN_X = "min x";

  /** the key for max x. */
  public final static String KEY_MAX_X = "max x";

  /** the key for mean x. */
  public final static String KEY_MEAN_X = "mean x";

  /** the key for median x. */
  public final static String KEY_MEDIAN_X = "median x";

  /** the key for min y. */
  public final static String KEY_MIN_Y = "min y";

  /** the key for max y. */
  public final static String KEY_MAX_Y = "max y";

  /** the key for mean y. */
  public final static String KEY_MEAN_Y = "mean y";

  /** the key for median y. */
  public final static String KEY_MEDIAN_Y = "median y";

  /**
   * Initializes the statistic.
   */
  public L1PointStatistic() {
    super();
  }

  /**
   * Initializes the statistic.
   *
   * @param data	the gc point to generate the statistics for
   */
  public L1PointStatistic(L1Point data) {
    super(data);
  }

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return "Calculates a view statistics for a level 1 point.";
  }

  /**
   * Cleans up data structures, frees up memory.
   */
  public void cleanUp() {
    super.cleanUp();
    m_Data = null;
  }

  /**
   * Returns a description for this statistic, i.e., chromatgram ID and
   * timestamp.
   *
   * @return		the description
   */
  public String getStatisticDescription() {
    return   m_Data.getParent().getID() + " (" + ((ThreeWayData) m_Data.getParent()).getDatabaseID() + ")";
  }

  /**
   * calculates the statistics.
   */
  protected void calculate() {
    List<L2Point> points;
    int			i;
    Double[] 		x;
    Double[] 		y;

    super.calculate();

    if (m_Data == null)
      return;

    points = m_Data.toList();
    x      = new Double[0];
    y      = new Double[0];

    // gather statistics
    if (points.size() > 0) {
      y = new Double[points.size()];
      x = new Double[points.size()];
      for (i = 0; i < points.size(); i++) {
	x[i] = points.get(i).getZ();
	y[i] = points.get(i).getData();
      }
    }

    if (m_Data.getParent() != null)
      add(KEY_DBID, ((ThreeWayData) m_Data.getParent()).getDatabaseID());
    add(KEY_Y, m_Data.getY());
    add(KEY_X, m_Data.getX());
    add(KEY_NUM_LEVEL2, points.size());
    add(KEY_MIN_X, numberToDouble(StatUtils.min(x)));
    add(KEY_MAX_X, numberToDouble(StatUtils.max(x)));
    add(KEY_MEAN_X, numberToDouble(StatUtils.mean(x)));
    add(KEY_MEDIAN_X, numberToDouble(StatUtils.median(x)));
    add(KEY_MIN_Y, numberToDouble(StatUtils.min(y)));
    add(KEY_MAX_Y, numberToDouble(StatUtils.max(y)));
    add(KEY_MEAN_Y, numberToDouble(StatUtils.mean(y)));
    add(KEY_MEDIAN_Y, numberToDouble(StatUtils.median(y)));
  }
}
