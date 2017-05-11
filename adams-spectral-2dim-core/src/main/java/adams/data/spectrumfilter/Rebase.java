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
 * Rebase.java
 * Copyright (C) 2012-2013 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Shifts all wave numbers (left or right), so that the first wave number is at the specified starting point.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 * 
 * <pre>-D &lt;int&gt; (property: debugLevel)
 * &nbsp;&nbsp;&nbsp;The greater the number the more additional info the scheme may output to 
 * &nbsp;&nbsp;&nbsp;the console (0 = off).
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 * 
 * <pre>-start &lt;float&gt; (property: start)
 * &nbsp;&nbsp;&nbsp;The new starting point for the wave numbers.
 * &nbsp;&nbsp;&nbsp;default: 0.0
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class Rebase
  extends AbstractFilter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 2319957467336388607L;

  /** the new starting point for wave numbers. */
  protected float m_Start;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "Shifts all wave numbers (left or right), so that the first wave "
	+ "number is at the specified starting point.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "start", "start",
	    0.0f);
  }

  /**
   * Sets the new starting point for the wave numbers.
   *
   * @param value 	the start
   */
  public void setStart(float value) {
    m_Start = value;
    reset();
  }

  /**
   * Returns the new starting point for the wave numbers.
   *
   * @return 		the start
   */
  public float getStart() {
    return m_Start;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String startTipText() {
    return "The new starting point for the wave numbers.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  @Override
  protected Spectrum processData(Spectrum data) {
    Spectrum		result;
    List<SpectrumPoint>	points;
    int			i;
    float		diff;
    SpectrumPoint	point;

    result = data.getHeader();
    points = data.toList();
    if (points.size() > 0) {
      diff = m_Start - points.get(0).getWaveNumber();
      if (isLoggingEnabled())
	getLogger().info("Difference: " + diff + " (= shifting " + ((diff < 0) ? "left" : "right") + ")");
      for (i = 0; i < points.size(); i++) {
	point = points.get(i);
	result.add(
	    new SpectrumPoint(
		point.getWaveNumber() + diff, 
		point.getAmplitude()));
      }
    }

    return result;
  }
}
