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
 * SpectrumCorrelation.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package adams.data.compare;

import adams.data.statistics.StatUtils;
import adams.data.spectrum.Spectrum;

/**
 <!-- globalinfo-start -->
 * Compares the spectral data of two spectra using correlation coefficient.<br>
 * Does not use wave numbers to determine common data points, simply the index.<br>
 * Outputs true if the minimum is achieved, otherwise false.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-minimum &lt;double&gt; (property: minimum)
 * &nbsp;&nbsp;&nbsp;The minimum correlation coefficient that must be achieved to output a 0 
 * &nbsp;&nbsp;&nbsp;instead of a 1.
 * &nbsp;&nbsp;&nbsp;default: 0.9
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SpectrumCorrelation
  extends AbstractObjectCompare<Spectrum, Boolean> {

  private static final long serialVersionUID = -1143756663174768745L;

  /** the minimum accepted correlation coefficient. */
  protected double m_Minimum;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Compares the spectral data of two spectra using correlation coefficient.\n"
        + "Does not use wave numbers to determine common data points, simply the index.\n"
	+ "Outputs true if the minimum is achieved, otherwise false.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "minimum", "minimum",
      0.9);
  }

  /**
   * Sets the minimum for the correlation coefficient.
   *
   * @param value	the minimum
   */
  public void setMinimum(double value) {
    m_Minimum = value;
    reset();
  }

  /**
   * Returns the minimum for the correlation coefficient.
   *
   * @return		the minimum
   */
  public double getMinimum() {
    return m_Minimum;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String minimumTipText() {
    return "The minimum correlation coefficient that must be achieved to output a 0 instead of a 1.";
  }

  /**
   * Returns the classes that it can handle.
   *
   * @return		the array of classes
   */
  @Override
  public Class[] accepts() {
    return new Class[]{Spectrum.class};
  }

  /**
   * Returns the type of output that it generates.
   *
   * @return		the class of the output
   */
  @Override
  public Class generates() {
    return Boolean.class;
  }

  /**
   * Performs the actual comparison of the two objects.
   *
   * @param o1		the first object
   * @param o2		the second object
   * @return		the result of the comparison
   */
  @Override
  protected Boolean doCompareObjects(Spectrum o1, Spectrum o2) {
    boolean	result;
    double[]	amp1;
    double[]	amp2;
    int		i;
    int		len;
    double	corr;

    len  = Math.min(o1.size(), o2.size());
    amp1 = new double[len];
    for (i = 0; i < len; i++)
      amp1[i] = o1.toList().get(i).getAmplitude();
    amp2 = new double[len];
    for (i = 0; i < len; i++)
      amp2[i] = o2.toList().get(i).getAmplitude();

    corr   = StatUtils.correlationCoefficient(amp1, amp2);
    result = (corr >= m_Minimum);
    if (isLoggingEnabled())
      getLogger().info(corr + " >= " + m_Minimum + "? " + result);

    return result;
  }
}
