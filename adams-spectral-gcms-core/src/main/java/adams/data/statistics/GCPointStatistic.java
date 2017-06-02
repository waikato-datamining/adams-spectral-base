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
 * GCPointStatistic.java
 * Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 */

package adams.data.statistics;

import adams.data.chromatogram.Chromatogram;
import adams.data.chromatogram.GCPoint;
import adams.data.chromatogram.MSPoint;

import java.util.List;

/**
 * A GC point specific statistic.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 3839 $
 */
public class GCPointStatistic
  extends AbstractDataStatistic<GCPoint> {

  /** for serialization. */
  private static final long serialVersionUID = 3716117305318401778L;

  /** the key for the database ID. */
  public final static String KEY_DBID = "Database ID of chromatogram";

  /** the key for abundance. */
  public final static String KEY_ABUNDANCE = "Abundance";

  /** the key for the timestamp. */
  public final static String KEY_TIMESTAMP = "Timestamp";

  /** the key for the time. */
  public final static String KEY_TIME = "Time";

  /** the key for number of MS points. */
  public final static String KEY_NUM_MSPOINTS = "Number of MS points";

  /** the key for min m/z. */
  public final static String KEY_MIN_MZ = "min m/z";

  /** the key for max m/z. */
  public final static String KEY_MAX_MZ = "max m/z";

  /** the key for mean m/z. */
  public final static String KEY_MEAN_MZ = "mean m/z";

  /** the key for median m/z. */
  public final static String KEY_MEDIAN_MZ = "median m/z";

  /** the key for min abundance. */
  public final static String KEY_MIN_ABUNDANCE = "min Abundance";

  /** the key for max abundance. */
  public final static String KEY_MAX_ABUNDANCE = "max Abundance";

  /** the key for mean abundance. */
  public final static String KEY_MEAN_ABUNDANCE = "mean Abundance";

  /** the key for median abundance. */
  public final static String KEY_MEDIAN_ABUNDANCE = "median Abundance";

  /**
   * Initializes the statistic.
   */
  public GCPointStatistic() {
    super();
  }

  /**
   * Initializes the statistic.
   *
   * @param data	the gc point to generate the statistics for
   */
  public GCPointStatistic(GCPoint data) {
    super(data);
  }

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return "Calculates a view statistics for a GC point.";
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
    return   m_Data.getParent().getID() + "/" + m_Data.getTimestamp()
           + " (" + ((Chromatogram) m_Data.getParent()).getDatabaseID() + ")";
  }

  /**
   * calculates the statistics.
   */
  protected void calculate() {
    List<MSPoint> points;
    int			i;
    Float[]		massCharge;
    Integer[]		abundances;

    super.calculate();

    if (m_Data == null)
      return;

    points     = m_Data.toList();
    massCharge = new Float[0];
    abundances = new Integer[0];

    // gather statistics
    if (points.size() > 0) {
      abundances = new Integer[points.size()];
      massCharge = new Float[points.size()];
      for (i = 0; i < points.size(); i++) {
	massCharge[i] = points.get(i).getMassCharge();
	abundances[i] = points.get(i).getAbundance();
      }
    }

    if (m_Data.getParent() != null)
      add(KEY_DBID, ((Chromatogram) m_Data.getParent()).getDatabaseID());
    add(KEY_ABUNDANCE, m_Data.getAbundance());
    add(KEY_TIMESTAMP, m_Data.getTimestamp());
    add(KEY_TIME, (double) m_Data.getTimestamp() / 60000);
    add(KEY_NUM_MSPOINTS, points.size());
    add(KEY_MIN_MZ, numberToDouble(StatUtils.min(massCharge)));
    add(KEY_MAX_MZ, numberToDouble(StatUtils.max(massCharge)));
    add(KEY_MEAN_MZ, numberToDouble(StatUtils.mean(massCharge)));
    add(KEY_MEDIAN_MZ, numberToDouble(StatUtils.median(massCharge)));
    add(KEY_MIN_ABUNDANCE, numberToDouble(StatUtils.min(abundances)));
    add(KEY_MAX_ABUNDANCE, numberToDouble(StatUtils.max(abundances)));
    add(KEY_MEAN_ABUNDANCE, numberToDouble(StatUtils.mean(abundances)));
    add(KEY_MEDIAN_ABUNDANCE, numberToDouble(StatUtils.median(abundances)));
  }
}
