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
 * MeanSpectrumCosineSimilarityBasedThreshold.java
 * Copyright (C) 2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.multispectrumoperation.outlierremoval;

import adams.core.MessageCollection;
import adams.core.QuickInfoHelper;
import adams.data.filter.Filter;
import adams.data.filter.PassThrough;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;
import adams.data.statistics.StatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates the mean spectrum from the incoming spectra and then calculates
 * the cosine similarity between the mean spectrum and the spectra.
 * Spectra with a similarity score that falls below threshold get removed.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class MeanSpectrumCosineSimilarityBasedThreshold
  extends AbstractOutlierRemoval {

  private static final long serialVersionUID = -4429806017426736253L;

  /** the prefilter to apply before computing the cosine similarity. */
  protected Filter m_PreFilter;

  /** the threshold. */
  protected double m_Threshold;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Calculates the mean spectrum from the incoming spectra and then calculates "
	     + "the cosine similarity between the mean spectrum and the spectra. "
	     + "Spectra with a similarity score that falls below threshold get removed.";
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
      -1.0, -1.0, 1.0);
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
    return "The pre-filter to apply before computing cosine similarity.";
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
    return "The threshold to use for the cosine similarity (two proportional vectors "
	     + "have a cosine similarity of +1, two orthogonal vectors have a similarity "
	     + "of 0, and two opposite vectors have a similarity of −1).";
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
    List<Spectrum> 	original;
    List<Spectrum> 	spectra;
    double[][]		ampl;
    int			i;
    int			n;
    double 		cs;
    double[]		mean;

    original = multi.toList();
    spectra  = new ArrayList<>(multi.toList());

    if (spectra.isEmpty()) {
      getLogger().warning("No spectra to perform outlier removal on!");
      return multi;
    }

    // pre-filter?
    if (!(m_PreFilter instanceof PassThrough)) {
      for (i = 0; i < spectra.size(); i++)
	spectra.set(i, (Spectrum) m_PreFilter.filter(spectra.get(i)));
    }

    // get amplitudes, calculate mean spectrum
    ampl = new double[spectra.size()][];
    mean = new double[spectra.get(0).size()];
    for (i = 0; i < spectra.size(); i++) {
      ampl[i] = new double[spectra.get(i).size()];
      for (n = 0; n < spectra.get(i).size(); n++) {
	ampl[i][n] = spectra.get(i).toList().get(n).getAmplitude();
	mean[n] += ampl[i][n];
      }
      mean[n] /= spectra.size();
    }

    // calculate cosine similarity
    result = (MultiSpectrum) multi.getHeader();
    for (i = 0; i < ampl.length; i++) {
      cs = StatUtils.cosineSimilarity(mean, ampl[i]);
      if (isLoggingEnabled())
	getLogger().info("mean vs " + i + ": cs/" + cs + " >= threshold/" + m_Threshold + " = " + (cs >= m_Threshold));
      if (cs >= m_Threshold)
	result.add((Spectrum) original.get(i).getClone());
    }

    return result;
  }
}
