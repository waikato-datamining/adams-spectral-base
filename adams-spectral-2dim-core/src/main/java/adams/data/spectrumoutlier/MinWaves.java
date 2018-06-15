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
 * MinWaves.java
 * Copyright (C) 2011-2018 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumoutlier;

import adams.data.outlier.AbstractOutlierDetector;
import adams.data.spectrum.Spectrum;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Detects spectra that have too few wave numbers.
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
 * <pre>-min-waves &lt;int&gt; (property: minWaves)
 * &nbsp;&nbsp;&nbsp;The minimum number of wave numbers that a spectrum must have.
 * &nbsp;&nbsp;&nbsp;default: 800
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  FracPete (fracpete at waikato dot ac dot nz)
 */
public class MinWaves
  extends AbstractOutlierDetector<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -5300001549269138646L;

  /** minimum number of wave numbers that the spectrum must contain.*/
  protected int m_MinWaves;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Detects spectra that have too few wave numbers.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "min-waves", "minWaves",
	    800);
  }

  /**
   * Sets the minimum number of wave numbers.
   *
   * @param value	the number of waves
   */
  public void setMinWaves(int value){
    m_MinWaves = value;
    reset();
  }

  /**
   * Returns the currently set minimum number of wave numbers.
   *
   * @return 		the minimum number of waves
   */
  public int getMinWaves(){
    return m_MinWaves;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return         tip text for this property suitable for
   *             displaying in the GUI or for listing the options.
   */
  public String minWavesTipText(){
    return "The minimum number of wave numbers that a spectrum must have.";
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

    result = new ArrayList<>();

    if (data.size() < m_MinWaves) {
      msg = "Not enough wave numbers: " + data.size() + " < " + m_MinWaves;
      result.add(msg);
      if (isLoggingEnabled())
	getLogger().info(data.getDatabaseID() + " - " + getClass().getName() + ": " + msg);
    }

    return result;
  }
}
