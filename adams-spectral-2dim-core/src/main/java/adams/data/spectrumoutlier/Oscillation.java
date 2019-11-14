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
 * Oscillation.java
 * Copyright (C) 2019 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumoutlier;

import adams.core.QuickInfoHelper;
import adams.data.outlier.AbstractOutlierDetector;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumUtils;
import adams.data.spectrumfilter.LOWESS;
import adams.data.spectrumfilter.SubRange;
import adams.data.statistics.StatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Detects spectra that have an oscillating signal.<br>
 * Computes the correlation coefficient between original spectrum and a LOWESS-smoothed one, using the defined wave number range.<br>
 * If the correlation coefficient falls below the specified threshold, it is considered an outlier.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-min-wave-number &lt;double&gt; (property: minWaveNumber)
 * &nbsp;&nbsp;&nbsp;The smallest wave number to include in the detection; use -1 for unlimited.
 * &nbsp;&nbsp;&nbsp;default: -1.0
 * &nbsp;&nbsp;&nbsp;minimum: -1.0
 * </pre>
 *
 * <pre>-max-wave-number &lt;double&gt; (property: maxWaveNumber)
 * &nbsp;&nbsp;&nbsp;The largest wave number to include in the detection; use -1 for unlimited.
 * &nbsp;&nbsp;&nbsp;default: -1.0
 * &nbsp;&nbsp;&nbsp;minimum: -1.0
 * </pre>
 *
 * <pre>-window-size &lt;int&gt; (property: windowSize)
 * &nbsp;&nbsp;&nbsp;The window size to use.
 * &nbsp;&nbsp;&nbsp;default: 100
 * &nbsp;&nbsp;&nbsp;minimum: 1
 * </pre>
 *
 * <pre>-threshold &lt;double&gt; (property: threshold)
 * &nbsp;&nbsp;&nbsp;The threshold to use for the correlation coefficient.
 * &nbsp;&nbsp;&nbsp;default: 0.9
 * &nbsp;&nbsp;&nbsp;minimum: 0.0
 * &nbsp;&nbsp;&nbsp;maximum: 1.0
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  FracPete (fracpete at waikato dot ac dot nz)
 */
public class Oscillation
  extends AbstractOutlierDetector<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -5300001549269138646L;

  /** the minimum wave number. */
  protected double m_MinWaveNumber;

  /** the maximum wave number. */
  protected double m_MaxWaveNumber;

  /** the LOWESS window size.*/
  protected int m_WindowSize;

  /** the threshold for the the correlation coefficient. */
  protected double m_Threshold;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Detects spectra that have an oscillating signal.\n"
      + "Computes the correlation coefficient between original spectrum and a "
      + "LOWESS-smoothed one, using the defined wave number range.\n"
      + "If the correlation coefficient falls below the specified threshold, it "
      + "is considered an outlier.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "min-wave-number", "minWaveNumber",
      -1.0, -1.0, null);

    m_OptionManager.add(
      "max-wave-number", "maxWaveNumber",
      -1.0, -1.0, null);

    m_OptionManager.add(
      "window-size", "windowSize",
      100, adams.data.utils.LOWESS.MIN_WINDOW_SIZE, null);

    m_OptionManager.add(
      "threshold", "threshold",
      0.9, 0.0, 1.0);
  }

  /**
   * Sets the minimum wave number to include in the detection.
   *
   * @param value 	the minimum
   */
  public void setMinWaveNumber(double value) {
    if (getOptionManager().isValid("minWaveNumber", value)) {
      m_MinWaveNumber = value;
      reset();
    }
  }

  /**
   * Returns the minimum wave number to include in the detection.
   *
   * @return 		the minimum
   */
  public double getMinWaveNumber() {
    return m_MinWaveNumber;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String minWaveNumberTipText() {
    return "The smallest wave number to include in the detection; use -1 for unlimited.";
  }

  /**
   * Sets the maximum wave number to include in the output.
   *
   * @param value 	the maximum
   */
  public void setMaxWaveNumber(double value) {
    if (getOptionManager().isValid("maxWaveNumber", value)) {
      m_MaxWaveNumber = value;
      reset();
    }
  }

  /**
   * Returns the maximum wave number to include in the output.
   *
   * @return 		the maximum
   */
  public double getMaxWaveNumber() {
    return m_MaxWaveNumber;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String maxWaveNumberTipText() {
    return "The largest wave number to include in the detection; use -1 for unlimited.";
  }

  /**
   * Sets the polynomial order.
   *
   * @param value 	the order
   */
  public void setWindowSize(int value) {
    if (getOptionManager().isValid("windowSize", value)) {
      m_WindowSize = value;
      reset();
    }
  }

  /**
   * Returns the polynominal order.
   *
   * @return 		the order
   */
  public int getWindowSize() {
    return m_WindowSize;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String windowSizeTipText() {
    return "The window size to use.";
  }

  /**
   * Sets the threshold for the correlation coefficient.
   *
   * @param value 	the threshold
   */
  public void setThreshold(double value) {
    if (getOptionManager().isValid("threshold", value)) {
      m_Threshold = value;
      reset();
    }
  }

  /**
   * Returns the threshold for the correlation coefficient.
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
   * Returns a quick info about the object, which can be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  public String getQuickInfo() {
    String	result;

    result = QuickInfoHelper.toString(this, "minWaveNumber", m_MinWaveNumber, "min: ");
    result += QuickInfoHelper.toString(this, "maxWaveNumber", m_MaxWaveNumber, ", max: ");
    result += QuickInfoHelper.toString(this, "windowSize", m_WindowSize, ", window: ");
    result += QuickInfoHelper.toString(this, "threshold", m_Threshold, ", threshold: ");

    return result;
  }

  /**
   * Performs the actual detection.
   *
   * @param data	the data to process
   * @return		the detections
   */
  @Override
  protected List<String> processData(Spectrum data) {
    List<String>	result;
    String		msg;
    SubRange		sub;
    LOWESS 		lowess;
    Spectrum		subset;
    Spectrum		smoothed;
    double		cc;

    result = new ArrayList<>();

    // create subset of wave numbers
    sub = new SubRange();
    sub.setMinWaveNumber(m_MinWaveNumber);
    sub.setMaxWaveNumber(m_MaxWaveNumber);
    subset = sub.filter(data);

    lowess = new LOWESS();
    lowess.setWindowSize(m_WindowSize);
    smoothed = lowess.filter(subset);

    cc = StatUtils.correlationCoefficient(SpectrumUtils.toDoubleArray(subset), SpectrumUtils.toDoubleArray(smoothed));
    if (isLoggingEnabled())
      getLogger().info(data.getID() + ": cc = " + cc);

    if (cc < m_Threshold) {
      msg = "Correlation coefficient below threshold: " + cc + " < " + m_Threshold;
      result.add(msg);
      if (isLoggingEnabled())
	getLogger().info(data.getID() + ": " + msg);
    }

    return result;
  }
}
