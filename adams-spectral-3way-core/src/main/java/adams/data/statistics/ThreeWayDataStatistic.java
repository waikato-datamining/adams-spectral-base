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

import adams.data.threeway.ThreeWayData;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

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

  /** the key for number of unique x values. */
  public final static String KEY_NUM_X = "Unique x values";

  /** the key for number of unique y values. */
  public final static String KEY_NUM_Y = "Unique y values";

  /** the key for number of unique z values. */
  public final static String KEY_NUM_Z = "Unique z values";

  /** the key for number of unique data values. */
  public final static String KEY_NUM_DATA = "Unique data values";

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

  /** the key for first Y. */
  public final static String KEY_FIRST_Y = "First y";

  /** the key for last Y. */
  public final static String KEY_LAST_Y = "Last y";

  /** the key for min delta Y. */
  public final static String KEY_MIN_DELTA_Y = "min delta y";

  /** the key for max delta Y. */
  public final static String KEY_MAX_DELTA_Y = "max delta y";

  /** the key for mean delta Y. */
  public final static String KEY_MEAN_DELTA_Y = "mean delta y";

  /** the key for stdev delta Y. */
  public final static String KEY_STDEV_DELTA_Y = "stdev delta y";

  /** the key for median delta Y. */
  public final static String KEY_MEDIAN_DELTA_Y = "median delta y";

  /** the key for first Z. */
  public final static String KEY_FIRST_Z = "First z";

  /** the key for last Z. */
  public final static String KEY_LAST_Z = "Last z";

  /** the key for min delta Z. */
  public final static String KEY_MIN_DELTA_Z = "min delta z";

  /** the key for max delta Z. */
  public final static String KEY_MAX_DELTA_Z = "max delta z";

  /** the key for mean delta Z. */
  public final static String KEY_MEAN_DELTA_Z = "mean delta z";

  /** the key for stdev delta Z. */
  public final static String KEY_STDEV_DELTA_Z = "stdev delta z";

  /** the key for median delta Z. */
  public final static String KEY_MEDIAN_DELTA_Z = "median delta z";

  /** the key for min data. */
  public final static String KEY_MIN_DATA = "min data";

  /** the key for max data. */
  public final static String KEY_MAX_DATA = "max data";

  /** the key for mean data. */
  public final static String KEY_MEAN_DATA = "mean data";

  /** the key for stdev data. */
  public final static String KEY_STDEV_DATA = "stdev data";

  /** the key for median data. */
  public final static String KEY_MEDIAN_DATA = "median data";


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
    return "Calculates a few statistics for a 3-way data structure.";
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
    int			i;
    TDoubleList		x;
    TDoubleList		y;
    TDoubleList		z;
    TDoubleList		d;
    TDoubleList		xDelta;
    TDoubleList		yDelta;
    TDoubleList		zDelta;

    super.calculate();

    if (m_Data == null)
      return;

    x = m_Data.getAllX();
    y = m_Data.getAllY();
    z = m_Data.getAllZ();
    d = m_Data.getAllData();

    xDelta = new TDoubleArrayList();
    for (i = 1; i < x.size(); i++)
      xDelta.add(x.get(i) - x.get(i - 1));
    yDelta = new TDoubleArrayList();
    for (i = 1; i < y.size(); i++)
      yDelta.add(y.get(i) - y.get(i - 1));
    zDelta = new TDoubleArrayList();
    for (i = 1; i < z.size(); i++)
      zDelta.add(z.get(i) - z.get(i - 1));

    add(KEY_DBID, m_Data.getDatabaseID());
    
    // x
    add(KEY_NUM_X, x.size());
    add(KEY_FIRST_X, x.get(0));
    add(KEY_LAST_X, x.get(x.size() - 1));
    if (x.size() > 1) {
      add(KEY_MIN_DELTA_X, numberToDouble(StatUtils.min(xDelta.toArray())));
      add(KEY_MAX_DELTA_X, numberToDouble(StatUtils.max(xDelta.toArray())));
      add(KEY_MEAN_DELTA_X, numberToDouble(StatUtils.mean(xDelta.toArray())));
      add(KEY_STDEV_DELTA_X, numberToDouble(StatUtils.stddev(xDelta.toArray(), true)));
      add(KEY_MEDIAN_DELTA_X, numberToDouble(StatUtils.median(xDelta.toArray())));
    }

    // y
    add(KEY_NUM_Y, y.size());
    add(KEY_FIRST_Y, y.get(0));
    add(KEY_LAST_Y, y.get(y.size() - 1));
    if (y.size() > 1) {
      add(KEY_MIN_DELTA_Y, numberToDouble(StatUtils.min(yDelta.toArray())));
      add(KEY_MAX_DELTA_Y, numberToDouble(StatUtils.max(yDelta.toArray())));
      add(KEY_MEAN_DELTA_Y, numberToDouble(StatUtils.mean(yDelta.toArray())));
      add(KEY_STDEV_DELTA_Y, numberToDouble(StatUtils.stddev(yDelta.toArray(), true)));
      add(KEY_MEDIAN_DELTA_Y, numberToDouble(StatUtils.median(yDelta.toArray())));
    }

    // z
    add(KEY_NUM_Z, z.size());
    add(KEY_FIRST_Z, z.get(0));
    add(KEY_LAST_Z, z.get(z.size() - 1));
    if (z.size() > 1) {
      add(KEY_MIN_DELTA_Z, numberToDouble(StatUtils.min(zDelta.toArray())));
      add(KEY_MAX_DELTA_Z, numberToDouble(StatUtils.max(zDelta.toArray())));
      add(KEY_MEAN_DELTA_Z, numberToDouble(StatUtils.mean(zDelta.toArray())));
      add(KEY_STDEV_DELTA_Z, numberToDouble(StatUtils.stddev(zDelta.toArray(), true)));
      add(KEY_MEDIAN_DELTA_Z, numberToDouble(StatUtils.median(zDelta.toArray())));
    }
    
    // data
    add(KEY_NUM_DATA, d.size());
    add(KEY_MIN_DATA, numberToDouble(StatUtils.min(d.toArray())));
    add(KEY_MAX_DATA, numberToDouble(StatUtils.max(d.toArray())));
    add(KEY_MEAN_DATA, numberToDouble(StatUtils.mean(d.toArray())));
    add(KEY_STDEV_DATA, numberToDouble(StatUtils.stddev(d.toArray(), true)));
    add(KEY_MEDIAN_DATA, numberToDouble(StatUtils.median(d.toArray())));
  }
}
