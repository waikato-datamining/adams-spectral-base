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
 * ChromatogramStatistic.java
 * Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 */

package adams.data.statistics;

import adams.data.chromatogram.Chromatogram;
import adams.data.chromatogram.GCPoint;

import java.util.List;

/**
 * Statistical information specific to a chromatogram.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4221 $
 */
public class ChromatogramStatistic
  extends AbstractDataStatistic<Chromatogram> {

  /** for serialization. */
  private static final long serialVersionUID = -2482267274581297567L;

  /** the key for the database ID. */
  public final static String KEY_DBID = "Database ID";
  
  /** the key for number of GC points. */
  public final static String KEY_NUM_GCPOINTS = "Number of GC points";
  
  /** the key for number of MS points. */
  public final static String KEY_NUM_MSPOINTS = "Number of MS points";
  
  /** the key for first timestamp. */
  public final static String KEY_FIRST_TIMESTAMP = "First timestamp";
  
  /** the key for last timestamp. */
  public final static String KEY_LAST_TIMESTAMP = "Last timestamp";
  
  /** the key for min delta time. */
  public final static String KEY_MIN_DELTA_TIME = "min delta Time";
  
  /** the key for max delta time. */
  public final static String KEY_MAX_DELTA_TIME = "max delta Time";
  
  /** the key for mean delta time. */
  public final static String KEY_MEAN_DELTA_TIME = "mean delta Time";
  
  /** the key for stdev delta time. */
  public final static String KEY_STDEV_DELTA_TIME = "stdev delta Time";
  
  /** the key for median delta time. */
  public final static String KEY_MEDIAN_DELTA_TIME = "median delta Time";
  
  /** the key for min abundance. */
  public final static String KEY_MIN_ABUNDANCE = "min Abundance";
  
  /** the key for max abundance. */
  public final static String KEY_MAX_ABUNDANCE = "max Abundance";
  
  /** the key for mean abundance. */
  public final static String KEY_MEAN_ABUNDANCE = "mean Abundance";
  
  /** the key for stdev abundance. */
  public final static String KEY_STDEV_ABUNDANCE = "stdev Abundance";
  
  /** the key for median abundance. */
  public final static String KEY_MEDIAN_ABUNDANCE = "median Abundance";
  
  /** the key for sum abundance. */
  public final static String KEY_SUM_ABUNDANCE = "sum Abundances";
  
  
  /**
   * Initializes the statistic.
   */
  public ChromatogramStatistic() {
    super();
  }

  /**
   * Initializes the statistic.
   *
   * @param data	the chromatogram to generate the statistics for
   */
  public ChromatogramStatistic(Chromatogram data) {
    super(data);
  }

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Calculates a view statistics for a chromatogram.";
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
   * Returns a description for this statistic, i.e., chromatogram ID.
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
    List<GCPoint> points;
    int			i;
    Long[]		deltaTimes;
    Long[]		abundances;
    double		firstTimestamp;
    double		lastTimestamp;
    Long		sumAbundances;
    Long		numMS;

    super.calculate();

    if (m_Data == null)
      return;

    points         = m_Data.toList();
    firstTimestamp = Double.NaN;
    lastTimestamp  = Double.NaN;
    deltaTimes     = new Long[0];
    abundances     = new Long[0];
    sumAbundances  = 0L;
    numMS          = 0L;

    // gather statistics
    if (points.size() > 0) {
      firstTimestamp = points.get(0).getTimestamp();
      if (points.size() > 1)
	lastTimestamp = points.get(points.size() - 1).getTimestamp();
      deltaTimes = new Long[points.size() - 1];
      abundances = new Long[points.size()];
      for (i = 0; i < points.size(); i++) {
	if (i > 0)
	  deltaTimes[i - 1] = points.get(i).getTimestamp() - points.get(i - 1).getTimestamp();
	abundances[i] = points.get(i).getAbundance();
	sumAbundances += points.get(i).getAbundance();
	numMS += points.get(i).size();
      }
    }

    add(KEY_DBID, m_Data.getDatabaseID());
    add(KEY_NUM_GCPOINTS, points.size());
    add(KEY_NUM_MSPOINTS, numMS);
    add(KEY_FIRST_TIMESTAMP, firstTimestamp);
    add(KEY_LAST_TIMESTAMP, lastTimestamp);
    add(KEY_MIN_DELTA_TIME, numberToDouble(StatUtils.min(deltaTimes)));
    add(KEY_MAX_DELTA_TIME, numberToDouble(StatUtils.max(deltaTimes)));
    add(KEY_MEAN_DELTA_TIME, numberToDouble(StatUtils.mean(deltaTimes)));
    add(KEY_STDEV_DELTA_TIME, numberToDouble(StatUtils.stddev(deltaTimes, true)));
    add(KEY_MEDIAN_DELTA_TIME, numberToDouble(StatUtils.median(deltaTimes)));
    add(KEY_MIN_ABUNDANCE, numberToDouble(StatUtils.min(abundances)));
    add(KEY_MAX_ABUNDANCE, numberToDouble(StatUtils.max(abundances)));
    add(KEY_MEAN_ABUNDANCE, numberToDouble(StatUtils.mean(abundances)));
    add(KEY_MEDIAN_ABUNDANCE, numberToDouble(StatUtils.median(abundances)));
    add(KEY_STDEV_ABUNDANCE, numberToDouble(StatUtils.stddev(abundances, true)));
    add(KEY_SUM_ABUNDANCE, sumAbundances);

    deltaTimes = null;
    abundances = null;
  }
}
