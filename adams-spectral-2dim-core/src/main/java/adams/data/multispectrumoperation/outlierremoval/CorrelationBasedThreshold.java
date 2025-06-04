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
 * CorrelationBasedThreshold.java
 * Copyright (C) 2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.multispectrumoperation.outlierremoval;

import adams.core.MessageCollection;
import adams.core.QuickInfoHelper;
import adams.core.Utils;
import adams.data.filter.Filter;
import adams.data.filter.PassThrough;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;
import adams.data.statistics.StatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies the pre-filter and then computes the correlation coefficients
 * between the spectra. Spectra that fall below the specified threshold
 * with other spectra get removed.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class CorrelationBasedThreshold
  extends AbstractOutlierRemoval {

  private static final long serialVersionUID = 7832507967407117131L;

  /** the prefilter to apply before computing the correlation coefficient. */
  protected Filter m_PreFilter;

  /** the threshold. */
  protected double m_Threshold;

  /** the threshold for below CC counts for rejecting spectra. */
  protected int m_BelowCountThreshold;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Applies the pre-filter and then computes the correlation coefficients "
	     + "between the spectra. Spectra that fall below the specified threshold "
	     + "with other spectra too many times get removed (see 'belowCountThreshold').";
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
      "threshold", "threshold",
      0.0, 0.0, 1.0);

    m_OptionManager.add(
      "below-count-threshold", "belowCountThreshold",
      -1);
  }

  /**
   * Sets the pre-filter to use.
   *
   * @param value	the filter
   */
  public void setPreFilter(Filter value) {
    m_PreFilter = value;
    reset();
  }

  /**
   * Returns the pre-filter to use.
   *
   * @return 		the filter
   */
  public Filter getPreFilter() {
    return m_PreFilter;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String preFilterTipText() {
    return "The pre-filter to apply before computing correlation coefficients.";
  }

  /**
   * Sets the threshold to use.
   *
   * @param value	the threshold
   */
  public void setThreshold(double value) {
    if (getOptionManager().isValid("threshold", value)) {
      m_Threshold = value;
      reset();
    }
  }

  /**
   * Returns the threshold to use.
   *
   * @return 		the threshold
   */
  public double getThreshold() {
    return m_Threshold;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String thresholdTipText() {
    return "The threshold to use for the correlation coefficient.";
  }

  /**
   * Sets the number of spectra below threshold that trigger rejection of the spectrum.
   * If &lt;= 0, the number gets subtracted from the number of incoming spectra to determine the actual count.
   *
   * @param value	the count
   */
  public void setBelowCountThreshold(int value) {
    if (getOptionManager().isValid("belowCountThreshold", value)) {
      m_BelowCountThreshold = value;
      reset();
    }
  }

  /**
   * Returns the number of spectra below threshold that trigger rejection of the spectrum.
   * If &lt;= 0, the number gets subtracted from the number of incoming spectra to determine the actual count.
   *
   * @return 		the count
   */
  public int getBelowCountThreshold() {
    return m_BelowCountThreshold;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String belowCountThresholdTipText() {
    return "For each spectrum the number of times it falls below the CC threshold is counted and this threshold "
	     + "determines whether a spectrum gets rejected (count >= threshold) or not (< threshold); "
	     + "if <= 0 this number gets subtracted from the incoming number of spectra to determine the actual count threshold.";
  }

  /**
   * Returns a quick info about the object, which can be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result = QuickInfoHelper.toString(this, "preFilter", m_PreFilter, "pre: ");
    result += QuickInfoHelper.toString(this, "threshold", m_Threshold, ", threshold: ");
    result += QuickInfoHelper.toString(this, "belowCountThreshold", m_BelowCountThreshold, ", below: ");

    return result;
  }

  /**
   * Performs the actual outlier removal.
   *
   * @param multi  the data to process
   * @param errors for collecting errors
   * @return the clean data, null if failed to process
   */
  @Override
  protected MultiSpectrum doRemoveOutliers(MultiSpectrum multi, MessageCollection errors) {
    MultiSpectrum	result;
    List<Spectrum>	original;
    List<Spectrum> 	spectra;
    double[][]		ampl;
    int			i;
    int			n;
    double		cc;
    int[]		count;
    int 		countThreshold;

    original = multi.toList();
    spectra  = new ArrayList<>(multi.toList());

    if (m_BelowCountThreshold <= 0)
      countThreshold = spectra.size() + m_BelowCountThreshold;
    else
      countThreshold = m_BelowCountThreshold;

    // pre-filter?
    if (!(m_PreFilter instanceof PassThrough)) {
      for (i = 0; i < spectra.size(); i++)
	spectra.set(i, (Spectrum) m_PreFilter.filter(spectra.get(i)));
    }

    // get amplitudes
    ampl = new double[spectra.size()][];
    for (i = 0; i < spectra.size(); i++) {
      ampl[i] = new double[spectra.get(i).size()];
      for (n = 0; n < spectra.get(i).size(); n++)
	ampl[i][n] = spectra.get(i).toList().get(n).getAmplitude();
    }

    // compute correlation coefficients
    count = new int[ampl.length];
    for (i = 0; i < ampl.length; i++) {
      for (n = 0; n < ampl.length; n++) {
	if (n == i)
	  continue;
	cc = StatUtils.correlationCoefficient(ampl[i], ampl[n]);
	if (cc < m_Threshold)
	  count[i]++;
	if (isLoggingEnabled())
	  getLogger().info(i + " vs " + n + ": cc/" + cc + " < treshold/" + m_Threshold + " = " + (cc < m_Threshold));
      }
    }
    if (isLoggingEnabled())
      getLogger().info("Below threshold counts (#spectra=" + spectra.size() + ", count threshold=" + countThreshold + "): " + Utils.arrayToString(count));

    result = (MultiSpectrum) multi.getHeader();
    for (i = 0; i < count.length; i++) {
      // CC not below threshold for all other spectra? -> add
      if (count[i] < countThreshold) {
	result.add((Spectrum) original.get(i).getClone());
	if (isLoggingEnabled())
	  getLogger().info((i+1) + ": keep");
      }
      else {
	if (isLoggingEnabled())
	  getLogger().info((i+1) + ": remove");
      }
    }

    return result;
  }
}
