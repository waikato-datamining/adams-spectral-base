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
 * Triangle.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumfilter.amplitudetransform;

import adams.data.spectrum.SpectrumPoint;

/**
 <!-- globalinfo-start -->
 * Multiplies the amplitude by the value from a triangle function.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-min &lt;float&gt; (property: min)
 * &nbsp;&nbsp;&nbsp;The minimum for the triangle.
 * &nbsp;&nbsp;&nbsp;default: 0.0
 * </pre>
 * 
 * <pre>-max &lt;float&gt; (property: max)
 * &nbsp;&nbsp;&nbsp;The maximum for the triangle.
 * &nbsp;&nbsp;&nbsp;default: 1.0
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class Triangle
  extends AbstractAmplitudeTransformer {

  private static final long serialVersionUID = 1342682133382785357L;

  /** the minimum for the triangle. */
  protected float m_Min;

  /** the maximum for the triangle. */
  protected float m_Max;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Multiplies the amplitude by the value from a triangle function.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "min", "min",
      0.0f);

    m_OptionManager.add(
      "max", "max",
      1.0f);
  }

  /**
   * Sets the minimum for the triangle.
   *
   * @param value	the minimum
   */
  public void setMin(float value) {
    m_Min = value;
    reset();
  }

  /**
   * Returns the minimum for the triangle.
   *
   * @return		the minimum
   */
  public float getMin() {
    return m_Min;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String minTipText() {
    return "The minimum for the triangle.";
  }

  /**
   * Sets the maximum for the triangle.
   *
   * @param value	the maximum
   */
  public void setMax(float value) {
    m_Max = value;
    reset();
  }

  /**
   * Returns the maximum for the triangle.
   *
   * @return		the maximum
   */
  public float getMax() {
    return m_Max;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String maxTipText() {
    return "The maximum for the triangle.";
  }

  /**
   * Transform the spectrum point and returns a new object.
   *
   * @param index	the 0-based index of the current point
   * @return		the new point
   */
  @Override
  protected SpectrumPoint transform(int index, SpectrumPoint point) {
    SpectrumPoint	result;
    int			size;
    float		factor;

    size = point.getParent().size();
    if (index < size / 2)
      factor = m_Min + (m_Max - m_Min) / (size / 2) * index;
    else
      factor = m_Min + (m_Max - m_Min) / (size / 2) * (size - index - 1);

    result = new SpectrumPoint(point.getWaveNumber(), point.getAmplitude() * factor);

    return result;
  }
}
