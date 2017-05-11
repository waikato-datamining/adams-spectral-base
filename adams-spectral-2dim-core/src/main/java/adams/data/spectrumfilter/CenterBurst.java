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
 * CenterBurst.java
 * Copyright (C) 2015 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spectrum.SpectrumUtils;
import adams.data.statistics.SpectrumStatistic;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Locates the highest burst (negative or positive) in the spectrum and then retrieves these points from the spectrum:<br>
 * (numPoints &#47; 2 - 1), burst, (numPoints &#47; 2)
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
 * <pre>-num-points &lt;int&gt; (property: numPoints)
 * &nbsp;&nbsp;&nbsp;The number of points to return around the burst.
 * &nbsp;&nbsp;&nbsp;default: 512
 * &nbsp;&nbsp;&nbsp;minimum: 2
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class CenterBurst
  extends AbstractFilter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 2319957467336388607L;

  /** the number of data points around the burst (even number). */
  protected int m_NumPoints;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return
      "Locates the highest burst (negative or positive) in the spectrum "
	+ "and then retrieves these points from the spectrum:\n"
	+ "(numPoints / 2 - 1), burst, (numPoints / 2)";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "num-points", "numPoints",
      512, 2, null);
  }

  /**
   * Sets the number of points to use.
   *
   * @param value	the number of points
   */
  public void setNumPoints(int value) {
    if ((value >= 2) && (value % 2 == 0)) {
      m_NumPoints = value;
      reset();
    }
    else {
      getLogger().warning("Number of points must be >= 2 and an even number, provided: " + value);
    }
  }

  /**
   * Returns the number of points to use.
   *
   * @return		the number of points
   */
  public int getNumPoints() {
    return m_NumPoints;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String numPointsTipText() {
    return "The number of points to return around the burst.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  protected Spectrum processData(Spectrum data) {
    Spectrum			result;
    Spectrum			left;
    Spectrum			right;
    List<SpectrumPoint> 	points;
    int				i;
    int				burstIndex;
    float			burstValue;
    float			value;
    float			inc;

    result = data.getHeader();
    if (data.size() == 0)
      return result;

    points = data.toList();
    inc    = (float) data.toStatistic().getStatistic(SpectrumStatistic.MEAN_DELTA_WAVE_NUMBER);

    // find burst
    burstIndex = 0;
    burstValue = Math.abs(points.get(0).getAmplitude());
    for (i = 1; i < points.size(); i++) {
      value =Math.abs(points.get(i).getAmplitude());
      if (burstValue < value) {
	burstIndex = i;
	burstValue = value;
      }
    }

    // left of burst
    left = data.getHeader();
    for (i = burstIndex - 1; (i >= 0) && (left.size() < m_NumPoints / 2 - 1); i--)
      left.add((SpectrumPoint) points.get(i).getClone());
    // pad
    if (left.size() == 0)
      value = points.get(burstIndex).getAmplitude();
    else
      value = left.toList().get(0).getAmplitude();
    left = SpectrumUtils.pad(left, m_NumPoints / 2 - 1, true, inc, value);

    // right of burst
    right = data.getHeader();
    for (i = burstIndex + 1; (i < points.size()) && (right.size() < m_NumPoints / 2); i++)
      right.add((SpectrumPoint) points.get(i).getClone());
    // pad
    if (right.size() == 0)
      value = points.get(burstIndex).getAmplitude();
    else
      value = right.toList().get(right.size() - 1).getAmplitude();
    right = SpectrumUtils.pad(right, m_NumPoints / 2, false, inc, value);

    // merge spectra
    result.add((SpectrumPoint) points.get(burstIndex).getClone());
    result.mergeWith(left);
    result.mergeWith(right);

    return result;
  }
}
