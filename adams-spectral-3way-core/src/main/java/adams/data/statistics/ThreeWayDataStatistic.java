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
 * ThreeWayDataStatistic.java
 * Copyright (C) 2017 University of Waikato, Hamilton, New Zealand
 */

package adams.data.statistics;

import adams.data.threeway.LevelOnePoint;
import adams.data.threeway.ThreeWayData;

import java.util.List;

/**
 * Statistical information specific to a ThreeWayData.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4221 $
 */
public class ThreeWayDataStatistic
  extends AbstractDataStatistic<ThreeWayData> {

  /** for serialization. */
  private static final long serialVersionUID = -2482267274581297567L;

  /** the key for the database ID. */
  public final static String KEY_DBID = "Database ID";

  /** the key for number of level 1 points. */
  public final static String KEY_NUM_LEVEL1 = "Number of level 1 points";

  /** the key for number of level 2 points. */
  public final static String KEY_NUM_LEVEL2 = "Number of level 2 points";

  /** the key for first X. */
  public final static String KEY_FIRST_X = "First x";

  /** the key for last X. */
  public final static String KEY_LAST_X = "Last x";

  /** the key for min delta X. */
  public final static String KEY_MIN_DELTA_X = "min delta x";

  /** the key for max delta X. */
  public final static String KEY_MAX_DELTA_X = "max delta x";

  /** the key for mean delta X. */
  public final static String KEY_MEAN_DELTA_X = "mean delta x";

  /** the key for stdev delta X. */
  public final static String KEY_STDEV_DELTA_X = "stdev delta x";

  /** the key for median delta X. */
  public final static String KEY_MEDIAN_DELTA_X = "median delta x";

  /** the key for min y. */
  public final static String KEY_MIN_Y = "min y";

  /** the key for max y. */
  public final static String KEY_MAX_Y = "max y";

  /** the key for mean y. */
  public final static String KEY_MEAN_Y = "mean y";

  /** the key for stdev y. */
  public final static String KEY_STDEV_Y = "stdev y";

  /** the key for median y. */
  public final static String KEY_MEDIAN_Y = "median y";

  /** the key for sum y. */
  public final static String KEY_SUM_Y = "sum y";


  /**
   * Initializes the statistic.
   */
  public ThreeWayDataStatistic() {
    super();
  }

  /**
   * Initializes the statistic.
   *
   * @param data	the data to generate the statistics for
   */
  public ThreeWayDataStatistic(ThreeWayData data) {
    super(data);
  }

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Calculates a view statistics for a 3-way data structure.";
  }

  /**
   * Cleans up data structures, frees up memory.
   */
  @Override
  public void cleanUp() {
    super.cleanUp();
    m_Data = null;
  }

  /**
   * Returns a description for this statistic, i.e., ID.
   *
   * @return		the description
   */
  public String getStatisticDescription() {
    return m_Data.getID() + " (" + m_Data.getDatabaseID() + ")";
  }

  /**
   * calculates the statistics.
   */
  @Override
  protected void calculate() {
    List<LevelOnePoint> points;
    int			i;
    Double[] 		deltaXs;
    Double[]		ys;
    double		firstX;
    double		lastX;
    double		sumYs;
    double 		numL2;

    super.calculate();

    if (m_Data == null)
      return;

    points  = m_Data.toList();
    firstX  = Double.NaN;
    lastX   = Double.NaN;
    deltaXs = new Double[0];
    ys      = new Double[0];
    sumYs   = 0.0;
    numL2   = 0.0;

    // gather statistics
    if (points.size() > 0) {
      firstX = points.get(0).getX();
      if (points.size() > 1)
	lastX = points.get(points.size() - 1).getX();
      deltaXs = new Double[points.size() - 1];
      ys  = new Double[points.size()];
      for (i = 0; i < points.size(); i++) {
	if (i > 0)
	  deltaXs[i - 1] = points.get(i).getX() - points.get(i - 1).getX();
	ys[i] = points.get(i).getY();
	sumYs += points.get(i).getY();
	numL2 += points.get(i).size();
      }
    }

    add(KEY_DBID, m_Data.getDatabaseID());
    add(KEY_NUM_LEVEL1, points.size());
    add(KEY_NUM_LEVEL2, numL2);
    add(KEY_FIRST_X, firstX);
    add(KEY_LAST_X, lastX);
    add(KEY_MIN_DELTA_X, numberToDouble(StatUtils.min(deltaXs)));
    add(KEY_MAX_DELTA_X, numberToDouble(StatUtils.max(deltaXs)));
    add(KEY_MEAN_DELTA_X, numberToDouble(StatUtils.mean(deltaXs)));
    add(KEY_STDEV_DELTA_X, numberToDouble(StatUtils.stddev(deltaXs, true)));
    add(KEY_MEDIAN_DELTA_X, numberToDouble(StatUtils.median(deltaXs)));
    add(KEY_MIN_Y, numberToDouble(StatUtils.min(ys)));
    add(KEY_MAX_Y, numberToDouble(StatUtils.max(ys)));
    add(KEY_MEAN_Y, numberToDouble(StatUtils.mean(ys)));
    add(KEY_MEDIAN_Y, numberToDouble(StatUtils.median(ys)));
    add(KEY_STDEV_Y, numberToDouble(StatUtils.stddev(ys, true)));
    add(KEY_SUM_Y, sumYs);
  }
}
