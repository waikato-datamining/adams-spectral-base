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
 * SpectrumCosineSimilarity.java
 * Copyright (C) 2025 University of Waikato, Hamilton, NZ
 */

package adams.data.compare;

import adams.data.spectrum.Spectrum;
import adams.data.statistics.StatUtils;

/**
 * Compares the spectral data of two spectra using cosine similarity.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumCosineSimilarity
  extends AbstractObjectCompare<Spectrum, Boolean> {

  private static final long serialVersionUID = -1143756663174768745L;

  /** the maximum accepted correlation coefficient. */
  protected double m_Maximum;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Compares the spectral data of two spectra using cosine similarity.\n"
        + "Does not use wave numbers to determine common data points, simply the index.\n"
	+ "Outputs true if the maximum is not crossed, otherwise false.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "maximum", "maximum",
      0.1);
  }

  /**
   * Sets the maximum for the cosine similarity.
   *
   * @param value	the maximum
   */
  public void setMaximum(double value) {
    m_Maximum = value;
    reset();
  }

  /**
   * Returns the maximum for the cosine similarity.
   *
   * @return		the maximum
   */
  public double getMaximum() {
    return m_Maximum;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String maximumTipText() {
    return "The maximum cosine similarity that must be achieved to output a 0 instead of a 1.";
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

    corr   = StatUtils.cosineSimilarity(amp1, amp2);
    result = (corr <= m_Maximum);
    if (isLoggingEnabled())
      getLogger().info(corr + " <= " + m_Maximum + "? " + result);

    return result;
  }
}
