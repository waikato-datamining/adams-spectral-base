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

/**
 * MultiplicativeScatterCorrection.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumfilter;

import adams.core.base.BaseInterval;
import adams.data.filter.AbstractFilter;
import adams.data.filter.Filter;
import adams.data.filter.PassThrough;
import adams.data.filter.TrainableBatchFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.statistics.StatUtils;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

/**
 <!-- globalinfo-start -->
 * Performs Multiplicative Scatter Correction.<br>
 * Assumes that all spectra have the same wave numbers.<br>
 * The 'pre-filter' gets only applied internally.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-no-id-update &lt;boolean&gt; (property: dontUpdateID)
 * &nbsp;&nbsp;&nbsp;If enabled, suppresses updating the ID of adams.data.id.IDHandler data containers.
 * &nbsp;&nbsp;&nbsp;default: false
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
 * @version $Revision$
 */
public class MultiplicativeScatterCorrection
  extends AbstractFilter<Spectrum>
  implements TrainableBatchFilter<Spectrum> {

  private static final long serialVersionUID = 4945613765460222457L;

  /** the filter to apply to the spectra internally. */
  protected Filter<Spectrum> m_PreFilter;

  /** the ranges to calculate the intercept/slope for. */
  protected BaseInterval[] m_Ranges;

  /** the average spectrum. */
  protected Spectrum m_Average;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Performs Multiplicative Scatter Correction.\n"
      + "Assumes that all spectra have the same wave numbers.\n"
      + "The 'pre-filter' gets only applied internally.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "pre-filter", "preFilter",
      new PassThrough());

    m_OptionManager.add(
      "range", "ranges",
      new BaseInterval[]{new BaseInterval(BaseInterval.ALL)});
  }

  /**
   * Resets the scheme.
   */
  @Override
  public void reset() {
    super.reset();
    resetFilter();
  }

  /**
   * Sets the prefilter to use.
   *
   * @param value 	the filter
   */
  public void setPreFilter(Filter<Spectrum> value) {
    m_PreFilter = value;
    reset();
  }

  /**
   * Returns the prefilter to use.
   *
   * @return 		the filter
   */
  public Filter<Spectrum> getPreFilter() {
    return m_PreFilter;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String preFilterTipText() {
    return
      "The filter to apply to the data internally before calculating the "
	+ "average spectrum and the intercept/slope.";
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
   * Resets the filter, i.e., flags it as "not trained".
   *
   * @see		#isTrained()
   */
  @Override
  public void resetFilter() {
    m_Average = null;
  }

  /**
   * Trains the filter with the specified data.
   */
  @Override
  public void trainFilter(Spectrum[] data) {
    int			i;
    int			n;
    int			numPoints;
    double[]		ampl;
    Spectrum[]		filtered;

    if (data.length == 0)
      throw new IllegalStateException("No spectra provided for training!");

    if (isLoggingEnabled())
      getLogger().info("Training on " + data.length + " spectra");

    m_Average = new Spectrum();
    m_Average.setID("avg(" + data.length + " spectra)");

    // pre-filter data
    if (m_PreFilter instanceof PassThrough) {
      filtered = data;
    }
    else {
      filtered = new Spectrum[data.length];
      for (i = 0; i < data.length; i++)
        filtered[i] = (Spectrum) m_PreFilter.filter(data[i]);
    }

    ampl = new double[filtered.length];
    for (i = 0; i < filtered[0].size(); i++) {
      for (n = 0; n < filtered.length; n++)
        ampl[n] = filtered[n].toList().get(i).getAmplitude();
      m_Average.add(
        new SpectrumPoint(
          filtered[0].toList().get(i).getWaveNumber(),
	  (float) StatUtils.mean(ampl)));
    }
  }

  /**
   * Returns whether the filter has been trained already and is ready to use.
   *
   * @return		true if already trained
   */
  @Override
  public boolean isTrained() {
    return (m_Average != null);
  }

  /**
   * Batch filters the data.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  @Override
  public Spectrum[] batchFilter(Spectrum[] data) {
    Spectrum[]	result;
    int		i;

    if (!isTrained())
      trainFilter(data);

    result = new Spectrum[data.length];
    for (i = 0; i < data.length; i++)
      result[i] = filter(data[i]);

    return result;
  }

  /**
   * The default implementation only checks whether there is any data set.
   *
   * @param data	the data to filter
   */
  @Override
  protected void checkData(Spectrum data) {
    Spectrum 		filtered;

    super.checkData(data);

    if (m_PreFilter instanceof PassThrough)
      filtered = data;
    else
      filtered = (Spectrum) m_PreFilter.filter(data);

    if (filtered.size() != m_Average.size())
      throw new IllegalStateException(
        "Different number of wave numbers (avg vs filtered input): "
	  + m_Average.size() + " != " + filtered.size());
  }

  /**
   * Performs the actual filtering. Just returns the input data if not trained.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  @Override
  protected Spectrum processData(Spectrum data) {
    Spectrum		result;
    Spectrum		filtered;
    TDoubleList 	x;
    TDoubleList		y;
    TDoubleList		wave;
    int			i;
    int			n;
    double[]		lr;
    double		inter;
    double		slope;
    SpectrumPoint	point;

    if (!isTrained()) {
      getLogger().warning("Not trained, just returning input data: " + data);
      return data;
    }

    // pre-filter data
    if (m_PreFilter instanceof PassThrough)
      filtered = data;
    else
      filtered = (Spectrum) m_PreFilter.filter(data);

    // create copy of spectrum
    result = (Spectrum) data.getClone();

    // iterate ranges
    x    = new TDoubleArrayList();
    y    = new TDoubleArrayList();
    wave = new TDoubleArrayList();
    for (n = 0; n < m_Ranges.length; n++) {
      x.clear();
      y.clear();
      wave.clear();
      for (i = 0; i < m_Average.size(); i++) {
        if (m_Ranges[n].isInside(filtered.toList().get(i).getWaveNumber())) {
          wave.add(filtered.toList().get(i).getWaveNumber());
	  x.add(filtered.toList().get(i).getAmplitude());
	  y.add(m_Average.toList().get(i).getAmplitude());
	}
      }

      // perform linear regression
      lr    = StatUtils.linearRegression(x.toArray(), y.toArray());
      inter = lr[0];
      slope = lr[1];

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
