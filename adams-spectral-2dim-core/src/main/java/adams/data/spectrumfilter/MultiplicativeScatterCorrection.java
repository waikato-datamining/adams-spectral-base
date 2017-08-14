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

import adams.core.AdditionalDataProvider;
import adams.data.filter.AbstractFilter;
import adams.data.filter.Filter;
import adams.data.filter.PassThrough;
import adams.data.filter.TrainableBatchFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spectrumfilter.multiplicativescattercorrection.AbstractMultiplicativeScatterCorrection;
import adams.data.spectrumfilter.multiplicativescattercorrection.RangeBased;
import adams.data.statistics.StatUtils;

import java.util.HashMap;
import java.util.Map;

/**
 <!-- globalinfo-start -->
 * Performs Multiplicative Scatter Correction.<br>
 * Assumes that all spectra have the same wave numbers.<br>
 * The 'pre-filter' gets only applied internally.<br>
 * Intercept and slope get stored in the report, for each defined range (using prefixes Intercept. and Slope.)
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
 * <pre>-correction &lt;adams.data.spectrumfilter.multiplicativescattercorrection.AbstractMultiplicativeScatterCorrection&gt; (property: correction)
 * &nbsp;&nbsp;&nbsp;The correction scheme to apply.
 * &nbsp;&nbsp;&nbsp;default: adams.data.spectrumfilter.multiplicativescattercorrection.RangeBased -pre-filter adams.data.filter.PassThrough
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class MultiplicativeScatterCorrection
  extends AbstractFilter<Spectrum>
  implements TrainableBatchFilter<Spectrum>, AdditionalDataProvider {

  private static final long serialVersionUID = 4945613765460222457L;

  public static final String PREFIX_INTERCEPT = "Intercept.";

  public static final String PREFIX_SLOPE = "Slope.";

  public static final String ADDITIONALDATA_AVERAGE = "Average";

  /** the filter to apply to the spectra internally. */
  protected Filter<Spectrum> m_PreFilter;

  /** the correction scheme to use. */
  protected AbstractMultiplicativeScatterCorrection m_Correction;

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
	+ "The 'pre-filter' gets only applied internally.\n"
	+ "Intercept and slope get stored in the report, for "
	+ "each defined range (using prefixes " + PREFIX_INTERCEPT
	+ " and " + PREFIX_SLOPE + ")";
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
      "correction", "correction",
      new RangeBased());
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
   * Sets the correction scheme to use.
   *
   * @param value 	the correction
   */
  public void setCorrection(AbstractMultiplicativeScatterCorrection value) {
    m_Correction = value;
    reset();
  }

  /**
   * Returns the correction scheme in use.
   *
   * @return 		the correction
   */
  public AbstractMultiplicativeScatterCorrection getCorrection() {
    return m_Correction;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String correctionTipText() {
    return "The correction scheme to apply.";
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
    if (!isTrained()) {
      getLogger().warning("Not trained, just returning input data: " + data);
      return data;
    }

    return m_Correction.correct(m_Average, data);
  }

  /**
   * Returns the additional data.
   *
   * @return		the additional data
   */
  public Map<String,Object> getAdditionalData() {
    Map<String,Object>	result;

    result = new HashMap<>();
    if (m_Average != null)
      result.put(ADDITIONALDATA_AVERAGE, m_Average);

    return result;
  }
}
