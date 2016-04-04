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
 * SpectrumStatistic.java
 * Copyright (C) 2008-2015 University of Waikato, Hamilton, New Zealand
 */

package adams.data.statistics;

import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.List;

/**
 * Statistical information specific to a spectrum.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 11831 $
 */
public class SpectrumStatistic
  extends AbstractDataStatistic<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -2482267274581297567L;

  public static final String SUM_AMPLITUDE = "sum Amplitude";
  public static final String MEDIAN_AMPLITUDE = "median Amplitude";
  public static final String STDEV_AMPLITUDE = "stdev Amplitude";
  public static final String MEAN_AMPLITUDE = "mean Amplitude";
  public static final String MAX_AMPLITUDE = "max Amplitude";
  public static final String MIN_AMPLITUDE = "min Amplitude";
  public static final String MEDIAN_DELTA_WAVE_NUMBER = "median delta Wave number";
  public static final String STDEV_DELTA_WAVE_NUMBER = "stdev delta Wave number";
  public static final String MEAN_DELTA_WAVE_NUMBER = "mean delta Wave number";
  public static final String MAX_DELTA_WAVE_NUMBER = "max delta Wave number";
  public static final String MIN_DELTA_WAVE_NUMBER = "min delta Wave number";
  public static final String LAST_WAVE_NUMBER = "Last Wave number";
  public static final String FIRST_WAVE_NUMBER = "First Wave number";
  public static final String NUMBER_OF_POINTS = "Number of points";
  public static final String DATABASE_ID = "Database ID";

  /**
   * Initializes the statistic.
   */
  public SpectrumStatistic() {
    super();
  }

  /**
   * Initializes the statistic.
   *
   * @param data	the spectrum to generate the statistics for
   */
  public SpectrumStatistic(Spectrum data) {
    super(data);
  }

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Calculates a view statistics for a spectrum.";
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
   * Sets the data to use as basis for the calculations.
   *
   * @param value	the spectrum to use, can be null
   */
  @Override
  public void setData(Spectrum value) {
    m_Calculated = false;
    m_Data       = value;
  }

  /**
   * Returns the currently stored spectrum.
   *
   * @return		the spectrum, can be null
   */
  @Override
  public Spectrum getData() {
    return m_Data;
  }

  /**
   * Returns a description for this statistic, i.e., spectrum ID.
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
    List<SpectrumPoint> points;
    int			i;
    Float[]		deltaWaves;
    Float[]		amplitudes;
    float		firstWaveNumber;
    float		lastWaveNumber;
    float		sumAmplitudes;

    super.calculate();

    if (m_Data == null)
      return;

    points          = m_Data.toList();
    firstWaveNumber = Float.NaN;
    lastWaveNumber  = Float.NaN;
    deltaWaves      = new Float[0];
    amplitudes      = new Float[0];
    sumAmplitudes   = 0.0f;

    // gather statistics
    if (points.size() > 0) {
      firstWaveNumber = points.get(0).getWaveNumber();
      if (points.size() > 1)
	lastWaveNumber = points.get(points.size() - 1).getWaveNumber();
      deltaWaves = new Float[points.size() - 1];
      amplitudes = new Float[points.size()];
      for (i = 0; i < points.size(); i++) {
	if (i > 0)
	  deltaWaves[i - 1] = points.get(i).getWaveNumber() - points.get(i - 1).getWaveNumber();
	amplitudes[i] = points.get(i).getAmplitude();
	sumAmplitudes += points.get(i).getAmplitude();
      }
    }

    add(DATABASE_ID, m_Data.getDatabaseID());
    add(NUMBER_OF_POINTS, points.size());
    add(FIRST_WAVE_NUMBER, firstWaveNumber);
    add(LAST_WAVE_NUMBER, lastWaveNumber);
    add(MIN_DELTA_WAVE_NUMBER, numberToDouble(StatUtils.min(deltaWaves)));
    add(MAX_DELTA_WAVE_NUMBER, numberToDouble(StatUtils.max(deltaWaves)));
    add(MEAN_DELTA_WAVE_NUMBER, numberToDouble(StatUtils.mean(deltaWaves)));
    add(STDEV_DELTA_WAVE_NUMBER, numberToDouble(StatUtils.stddev(deltaWaves, true)));
    add(MEDIAN_DELTA_WAVE_NUMBER, numberToDouble(StatUtils.median(deltaWaves)));
    add(MIN_AMPLITUDE, numberToDouble(StatUtils.min(amplitudes)));
    add(MAX_AMPLITUDE, numberToDouble(StatUtils.max(amplitudes)));
    add(MEAN_AMPLITUDE, numberToDouble(StatUtils.mean(amplitudes)));
    add(STDEV_AMPLITUDE, numberToDouble(StatUtils.stddev(amplitudes, true)));
    add(MEDIAN_AMPLITUDE, numberToDouble(StatUtils.median(amplitudes)));
    add(SUM_AMPLITUDE, sumAmplitudes);

    deltaWaves = null;
    amplitudes = null;
  }
}
