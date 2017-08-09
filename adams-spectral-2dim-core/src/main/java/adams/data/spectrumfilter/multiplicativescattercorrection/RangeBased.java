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
 * RangeBased.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumfilter.multiplicativescattercorrection;

import adams.core.base.BaseInterval;
import adams.data.filter.PassThrough;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spectrumfilter.MultiplicativeScatterCorrection;
import adams.data.statistics.StatUtils;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

/**
 <!-- globalinfo-start -->
 * Performs the correction using slopes&#47;intercepts calculated for the defined ranges.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-pre-filter &lt;adams.data.filter.Filter&gt; (property: preFilter)
 * &nbsp;&nbsp;&nbsp;The filter to apply to the data internally before calculating the average
 * &nbsp;&nbsp;&nbsp;spectrum and the intercept&#47;slope.
 * &nbsp;&nbsp;&nbsp;default: adams.data.filter.PassThrough
 * </pre>
 *
 * <pre>-range &lt;adams.core.base.BaseInterval&gt; [-range ...] (property: ranges)
 * &nbsp;&nbsp;&nbsp;The ranges of wave numbers to use for calculating the intercept&#47;slope corrections.
 * &nbsp;&nbsp;&nbsp;default: (-Infinity;+Infinity)
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class RangeBased
  extends AbstractMultiplicativeScatterCorrection {

  private static final long serialVersionUID = -6754404982002787538L;

  /** the ranges to calculate the intercept/slope for. */
  protected BaseInterval[] m_Ranges;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Performs the correction using slopes/intercepts calculated for the defined ranges.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "range", "ranges",
      new BaseInterval[]{new BaseInterval(BaseInterval.ALL)});
  }

  /**
   * Sets the wave number ranges.
   *
   * @param value 	the ranges
   */
  public void setRanges(BaseInterval[] value) {
    m_Ranges = value;
    reset();
  }

  /**
   * Returns the wave number ranges.
   *
   * @return 		the ranges
   */
  public BaseInterval[] getRanges() {
    return m_Ranges;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rangesTipText() {
    return
      "The ranges of wave numbers to use for calculating the intercept/slope "
	+ "corrections.";
  }

  /**
   * Corrects the spectrum.
   *
   * @param average 	the average spectrum
   * @param data 	the spectrum to process
   * @return		the processed spectrum
   */
  @Override
  public Spectrum correct(Spectrum average, Spectrum data) {
    Spectrum		result;
    Spectrum		filtered;
    TDoubleList x;
    TDoubleList		y;
    TDoubleList		wave;
    int			i;
    int			n;
    double[]		lr;
    double		inter;
    double		slope;
    SpectrumPoint point;

    // create copy of spectrum
    result = (Spectrum) data.getClone();

    // pre-filter data
    if (m_PreFilter instanceof PassThrough)
      filtered = data;
    else
      filtered = (Spectrum) m_PreFilter.filter(data);

    // iterate ranges
    x    = new TDoubleArrayList();
    y    = new TDoubleArrayList();
    wave = new TDoubleArrayList();
    for (n = 0; n < m_Ranges.length; n++) {
      x.clear();
      y.clear();
      wave.clear();
      for (i = 0; i < average.size(); i++) {
	if (m_Ranges[n].isInside(filtered.toList().get(i).getWaveNumber())) {
	  wave.add(filtered.toList().get(i).getWaveNumber());
	  y.add(filtered.toList().get(i).getAmplitude());
	  x.add(average.toList().get(i).getAmplitude());
	}
      }

      // perform linear regression
      lr    = StatUtils.linearRegression(x.toArray(), y.toArray());
      inter = lr[0];
      slope = lr[1];

      // store in report
      result.getReport().setNumericValue(MultiplicativeScatterCorrection.PREFIX_INTERCEPT + m_Ranges[n], inter);
      result.getReport().setNumericValue(MultiplicativeScatterCorrection.PREFIX_SLOPE + m_Ranges[n], slope);

      if (isLoggingEnabled())
	getLogger().info(data.getID() + "/" + m_Ranges[n] + ": intercept=" + inter + ", slope=" + slope);

      // correct spectrum
      for (i = 0; i < result.size(); i++) {
	point = result.toList().get(i);
	if (m_Ranges[n].isInside(point.getWaveNumber()))
	  point.setAmplitude((float) ((point.getAmplitude() - inter) / slope));
      }
    }

    return result;
  }
}
