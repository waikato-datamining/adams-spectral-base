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
 * LogTransform.java
 * Copyright (C) 2009-2013 University of Waikato, Hamilton, New Zealand
 */
package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Transforms the amplitudes using log.
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
 * <pre>-base &lt;java.lang.String&gt; (property: logBase)
 * &nbsp;&nbsp;&nbsp;The base for the log ('e' or a positive number greater than 1).
 * &nbsp;&nbsp;&nbsp;default: 10
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class LogTransform
  extends AbstractFilter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 1836858988505886282L;

  /** the base of the log. */
  protected String m_LogBase;

  /** the actual base. */
  protected double m_ActualLogBase;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Transforms the amplitudes using log.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "base", "logBase",
	    "10");
  }

  /**
   * Sets the base of the log ("e" or a positive number greater than 1).
   *
   * @param value 	the base
   */
  public void setLogBase(String value) {
    boolean	ok;

    if (value.toLowerCase().equals("e")) {
      ok = true;
    }
    else {
      try {
	ok = (Double.parseDouble(value) > 1);
      }
      catch (Exception e) {
	ok = false;
      }
    }

    if (ok) {
      m_LogBase = value.toLowerCase();
      if (m_LogBase.equals("e"))
	m_ActualLogBase = Math.E;
      else
	m_ActualLogBase = Double.parseDouble(m_LogBase);
      reset();
    }
    else {
      getLogger().severe(
	  "Only 'e' and positive numbers greater than 1 are allowed"
	  + " - provided: " + value);
    }
  }

  /**
   * Returns the base of the log ("e" or a positive number greater than 1).
   *
   * @return 		the base
   */
  public String getLogBase() {
    return m_LogBase;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String logBaseTipText() {
    return "The base for the log ('e' or a positive number greater than 1).";
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
    SpectrumPoint	point;
    int			i;
    float		newAmp;

    result = data.getHeader();
    points = data.toList();

    for (i = 0; i < points.size(); i++) {
      point = (SpectrumPoint) points.get(i).getClone();
      if (point.getAmplitude() > 0)
	newAmp = (float) (Math.log(point.getAmplitude()) / Math.log(m_ActualLogBase));
      else
	newAmp = 0;
      point.setAmplitude(newAmp);
      result.add(point);
    }

    return result;
  }
}
