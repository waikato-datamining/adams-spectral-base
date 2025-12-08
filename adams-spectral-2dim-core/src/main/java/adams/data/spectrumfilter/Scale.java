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
 * Scale.java
 * Copyright (C) 2008-2013 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Scales the amplitudes to a given maximum.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-min &lt;double&gt; (property: minAmplitude)
 *         The minimum amplitude to scale to.
 *         default: 0.0
 * </pre>
 *
 * <pre>-max &lt;double&gt; (property: maxAmplitude)
 *         The maximum amplitude to scale to.
 *         default: 100.0
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class Scale
  extends AbstractFilter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -5971426372440154921L;

  /** the minimum amplitude to scale to. */
  protected double m_MinAmplitude;

  /** the maximum amplitude to scale to. */
  protected double m_MaxAmplitude;

  /**
   * Returns a string describing the object.
   *
   * @return 		a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Scales the amplitudes to a given range.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "min", "minAmplitude",
	    0.0);

    m_OptionManager.add(
	    "max", "maxAmplitude",
	    100.0);
  }

  /**
   * Sets the minimum amplitude to scale to.
   *
   * @param value	the maximum
   */
  public void setMinAmplitude(double value) {
    m_MinAmplitude = value;
    reset();
  }

  /**
   * Returns the minimum amplitude to scale to.
   *
   * @return		the minimum
   */
  public double getMinAmplitude() {
    return m_MinAmplitude;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the gui
   */
  public String minAmplitudeTipText() {
    return "The minimum amplitude to scale to.";
  }

  /**
   * Sets the maximum amplitude to scale to.
   *
   * @param value	the maximum
   */
  public void setMaxAmplitude(double value) {
    m_MaxAmplitude = value;
    reset();
  }

  /**
   * Returns the maximum amplitude to scale to.
   *
   * @return		the maximum
   */
  public double getMaxAmplitude() {
    return m_MaxAmplitude;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the gui
   */
  public String maxAmplitudeTipText() {
    return "The maximum amplitude to scale to.";
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
    List<SpectrumPoint>	list;
    List<SpectrumPoint>	listNew;
    double		min;
    double		max;
    double		scale;
    int			i;
    SpectrumPoint	point;
    SpectrumPoint	pointNew;

    if (m_MinAmplitude > m_MaxAmplitude)
      throw new IllegalStateException("min amplitude > max amplitude!");

    list    = data.toList();
    listNew = new ArrayList<>();

    // determine range
    min = Double.MAX_VALUE;
    max = -Double.MAX_VALUE;
    for (i = 0; i < list.size(); i++) {
      point = list.get(i);
      if (point.getAmplitude() > max)
	max = point.getAmplitude();
      if (point.getAmplitude() < min)
	min = point.getAmplitude();
    }
    getLogger().info("min: " + min + ", max: " + max);

    // scale data
    result = data.getHeader();
    scale  =   (double) (m_MaxAmplitude - m_MinAmplitude)
             / (double) (max - min);

    for (i = 0; i < list.size(); i++) {
      // scale point
      point    = list.get(i);
      pointNew = new SpectrumPoint(
	  		point.getWaveNumber(),
	  		(float) ((point.getAmplitude() - min) * scale + m_MinAmplitude));

      // add to output
      listNew.add(pointNew);
    }
    result.replaceAll(listNew, true);

    return result;
  }
}
