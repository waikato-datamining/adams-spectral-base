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
 * SpectrumToArray.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.data.conversion;

import adams.core.QuickInfoHelper;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;

/**
 <!-- globalinfo-start -->
 * Turns either the wave numbers or amplitudes of the incoming spectrum into a float array.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-type &lt;WAVENUMBER|AMPLITUDE&gt; (property: type)
 * &nbsp;&nbsp;&nbsp;The type of data to output.
 * &nbsp;&nbsp;&nbsp;default: AMPLITUDE
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumToArray
  extends AbstractConversion {

  private static final long serialVersionUID = -1070850083570790160L;

  /**
   * The type of data to output.
   */
  public enum DataType {
    WAVENUMBER,
    AMPLITUDE,
  }

  /** the data to output. */
  protected DataType m_Type;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Turns either the wave numbers or amplitudes of the incoming spectrum into a float array.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "type", "type",
      DataType.AMPLITUDE);
  }

  /**
   * Sets the type of data to output.
   *
   * @param value 	the type
   */
  public void setType(DataType value) {
    m_Type = value;
    reset();
  }

  /**
   * Returns the type of data to output.
   *
   * @return 		the type
   */
  public DataType getType() {
    return m_Type;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String typeTipText() {
    return "The type of data to output.";
  }

  /**
   * Returns a quick info about the object, which can be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    return QuickInfoHelper.toString(this, "type", m_Type);
  }

  /**
   * Returns the class that is accepted as input.
   *
   * @return		the class
   */
  @Override
  public Class accepts() {
    return Spectrum.class;
  }

  /**
   * Returns the class that is generated as output.
   *
   * @return		the class
   */
  @Override
  public Class generates() {
    return float[].class;
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  @Override
  protected Object doConvert() throws Exception {
    Spectrum	input;
    TFloatList  result;

    input  = (Spectrum) m_Input;

    result = new TFloatArrayList();
    for (SpectrumPoint p: input.toList()) {
      switch (m_Type) {
	case WAVENUMBER:
	  result.add(p.getWaveNumber());
	  break;
	case AMPLITUDE:
	  result.add(p.getAmplitude());
	  break;
	default:
	  throw new IllegalStateException("Unhandled data type: " + m_Type);
      }
    }

    return result.toArray();
  }
}
