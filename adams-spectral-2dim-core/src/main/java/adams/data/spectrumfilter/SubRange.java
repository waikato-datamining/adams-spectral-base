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
 * SubRange.java
 * Copyright (C) 2009-2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Returns a sub-range of wave numbers from a spectrum.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 * &nbsp;&nbsp;&nbsp;If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-min &lt;double&gt; (property: minWaveNumber)
 * &nbsp;&nbsp;&nbsp;The smallest wave number to include in the output; use -1 for unlimited.
 * &nbsp;&nbsp;&nbsp;default: -1.0
 * </pre>
 *
 * <pre>-max &lt;double&gt; (property: maxWaveNumber)
 * &nbsp;&nbsp;&nbsp;The largest wave number to include in the output; use -1 for unlimited.
 * &nbsp;&nbsp;&nbsp;default: -1.0
 * </pre>
 *
 * <pre>-invert (property: invert)
 * &nbsp;&nbsp;&nbsp;If set to true, then all but the selected range will be returned.
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SubRange
  extends AbstractFilter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 2319957467336388607L;

  /** the minimum wave number. */
  protected double m_MinWaveNumber;

  /** the maximum wave number. */
  protected double m_MaxWaveNumber;

  /** whether to invert the matching sense. */
  protected boolean m_Invert;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return "Returns a sub-range of wave numbers from a spectrum.";
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "min", "minWaveNumber",
	    -1.0, -1.0, null);

    m_OptionManager.add(
	    "max", "maxWaveNumber",
	    -1.0, -1.0, null);

    m_OptionManager.add(
	    "invert", "invert",
	    false);
  }

  /**
   * Sets the minimum wave number to include in the output.
   *
   * @param value 	the minimum
   */
  public void setMinWaveNumber(double value) {
    m_MinWaveNumber = value;
    reset();
  }

  /**
   * Returns the minimum wave number to include in the output.
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
    return "The smallest wave number to include in the output; use -1 for unlimited.";
  }

  /**
   * Sets the maximum wave number to include in the output.
   *
   * @param value 	the maximum
   */
  public void setMaxWaveNumber(double value) {
    m_MaxWaveNumber = value;
    reset();
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
    return "The largest wave number to include in the output; use -1 for unlimited.";
  }

  /**
   * Whether to invert the matching sense.
   *
   * @param value 	true if to return everything apart the selected range
   */
  public void setInvert(boolean value) {
    m_Invert = value;
    reset();
  }

  /**
   * Returns whether to invert the matching sense.
   *
   * @return 		true if to return everything apart the selected range
   */
  public boolean getInvert() {
    return m_Invert;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String invertTipText() {
    return "If set to true, then all but the selected range will be returned.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  protected Spectrum processData(Spectrum data) {
    Spectrum		result;
    List<SpectrumPoint>	points;
    List<SpectrumPoint>	pointsNew;
    double		min;
    double		max;

    result = data.getHeader();

    if (m_MinWaveNumber == -1)
      min = data.getMinWaveNumber().getWaveNumber();
    else
      min = m_MinWaveNumber;
    if (m_MaxWaveNumber == -1)
      max = data.getMaxWaveNumber().getWaveNumber();
    else
      max = m_MaxWaveNumber;

    points    = data.toList();
    pointsNew = new ArrayList<>();
    for (SpectrumPoint p: points) {
      if (m_Invert) {
	if ((p.getWaveNumber() < min) || (p.getWaveNumber() > max))
	  pointsNew.add(new SpectrumPoint(p.getWaveNumber(), p.getAmplitude()));
      }
      else {
	if ((p.getWaveNumber() >= min) && (p.getWaveNumber() <= max))
	  pointsNew.add(new SpectrumPoint(p.getWaveNumber(), p.getAmplitude()));
      }
    }
    result.addAll(pointsNew);

    return result;
  }
}
