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
 * MaxWaves.java
 * Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumoutlier;

import adams.data.outlier.AbstractOutlierDetector;
import adams.data.spectrum.Spectrum;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Detects spectra that have too many wave numbers.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-max-waves &lt;int&gt; (property: maxWaves)
 * &nbsp;&nbsp;&nbsp;The maximum number of wave numbers that a spectrum must have.
 * &nbsp;&nbsp;&nbsp;default: 800
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  FracPete (fracpete at waikato dot ac dot nz)
 */
public class MaxWaves
  extends AbstractOutlierDetector<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -5300001549269138646L;

  /** maximum number of wave numbers that the spectrum must contain.*/
  protected int m_MaxWaves;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Detects spectra that have too many wave numbers.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "max-waves", "maxWaves",
      800);
  }

  /**
   * Sets the maximum number of wave numbers.
   *
   * @param value	the number of waves
   */
  public void setMaxWaves(int value){
    m_MaxWaves = value;
    reset();
  }

  /**
   * Returns the currently set maximum number of wave numbers.
   *
   * @return 		the maximum number of waves
   */
  public int getMaxWaves(){
    return m_MaxWaves;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return         tip text for this property suitable for
   *             displaying in the GUI or for listing the options.
   */
  public String maxWavesTipText(){
    return "The maximum number of wave numbers that a spectrum must have.";
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

    if (data.size() > m_MaxWaves) {
      msg = "Too many wave numbers: " + data.size() + " > " + m_MaxWaves;
      result.add(msg);
      if (isLoggingEnabled())
        getLogger().info(data.getDatabaseID() + " - " + getClass().getName() + ": " + msg);
    }

    return result;
  }
}
