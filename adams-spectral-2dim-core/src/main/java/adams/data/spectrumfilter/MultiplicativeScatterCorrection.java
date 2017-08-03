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

import adams.data.filter.AbstractFilter;
import adams.data.filter.TrainableBatchFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.statistics.StatUtils;

/**
 <!-- globalinfo-start -->
 * Performs Multiplicative Scatter Correction.<br>
 * Assumes that all spectra have the same wave numbers.
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
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MultiplicativeScatterCorrection
  extends AbstractFilter<Spectrum>
  implements TrainableBatchFilter<Spectrum> {

  private static final long serialVersionUID = 4945613765460222457L;

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
      + "Assumes that all spectra have the same wave numbers.";
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
    double[]		ampl;

    if (data.length == 0)
      throw new IllegalStateException("No spectra provided for training!");

    if (isLoggingEnabled())
      getLogger().info("Training on " + data.length + " spectra");

    m_Average = new Spectrum();
    m_Average.setID("avg(" + data.length + " spectra)");

    ampl = new double[data.length];
    for (i = 0; i < data[0].size(); i++) {
      for (n = 0; n < data.length; n++)
        ampl[n] = data[n].toList().get(i).getAmplitude();
      m_Average.add(
        new SpectrumPoint(
          data[0].toList().get(i).getWaveNumber(),
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
    super.checkData(data);

    if (data.size() != m_Average.size())
      throw new IllegalStateException(
        "Different number of wave numbers (avg vs input): "
	  + m_Average.size() + " != " + data.size());
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
    double[]		x;
    double[]		y;
    int			i;
    double[]		lr;
    double		inter;
    double		slope;
    SpectrumPoint	point;

    if (!isTrained()) {
      getLogger().warning("Not trained, just returning input data: " + data);
      return data;
    }

    // perform linear regression
    x = new double[m_Average.size()];
    y = new double[m_Average.size()];
    for (i = 0; i < m_Average.size(); i++) {
      x[i] = data.toList().get(i).getAmplitude();
      y[i] = m_Average.toList().get(i).getAmplitude();
    }
    lr    = StatUtils.linearRegression(x, y);
    inter = lr[0];
    slope = lr[1];

    if (isLoggingEnabled())
      getLogger().info(data.getID() + ": intercept=" + inter + ", slope=" + slope);

    // correct spectrum
    result = (Spectrum) data.getClone();
    for (i = 0; i < result.size(); i++) {
      point = result.toList().get(i);
      point.setAmplitude((float) ((point.getAmplitude() - inter) / slope));
    }

    return result;
  }
}
