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
 * Remove.java
 * Copyright (C) 2011-2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.core.Range;
import adams.data.filter.AbstractFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Removes a range of amplitudes from a spectrum.<br>
 * The matching sense can be inverted as well, i.e., removing everything else but the defined range of amplitudes.
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
 * <pre>-range &lt;java.lang.String&gt; (property: range)
 * &nbsp;&nbsp;&nbsp;The range of attributes to remove; A range is a comma-separated list of
 * &nbsp;&nbsp;&nbsp;single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)'
 * &nbsp;&nbsp;&nbsp; inverts the range '...'; the following placeholders can be used as well:
 * &nbsp;&nbsp;&nbsp; first, second, third, last_2, last_1, last
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-invert (property: invert)
 * &nbsp;&nbsp;&nbsp;If set to true, then all but the selected range will be returned.
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class Remove
  extends AbstractFilter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 2319957467336388607L;

  /** the range of amplitudes to remove. */
  protected Range m_Range;

  /** whether to invert the matching sense. */
  protected boolean m_Invert;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return
        "Removes a range of amplitudes from a spectrum.\n"
      + "The matching sense can be inverted as well, i.e., removing everything "
      + "else but the defined range of amplitudes.";
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "range", "range",
	    "");

    m_OptionManager.add(
	    "invert", "invert",
	    false);
  }

  /**
   * Initializes the members.
   */
  protected void initialize() {
    super.initialize();

    m_Range = new Range();
  }

  /**
   * Sets the range of amplitudes to remove.
   *
   * @param value 	the range
   */
  public void setRange(String value) {
    m_Range.setRange(value);
    reset();
  }

  /**
   * Returns the range of amplitudes to remove.
   *
   * @return 		the range
   */
  public String getRange() {
    return m_Range.getRange();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rangeTipText() {
    return "The range of attributes to remove; " + m_Range.getExample();
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
    Range		range;
    int			i;

    result = data.getHeader();

    range = new Range(m_Range.getRange());
    range.setInverted(m_Invert);
    range.setMax(data.size());

    points    = data.toList();
    pointsNew = new ArrayList<>();
    for (i = 0; i < points.size(); i++) {
      if (!range.isInRange(i))
	pointsNew.add((SpectrumPoint) points.get(i).getClone());
    }
    result.replaceAll(pointsNew, true);

    return result;
  }
}
