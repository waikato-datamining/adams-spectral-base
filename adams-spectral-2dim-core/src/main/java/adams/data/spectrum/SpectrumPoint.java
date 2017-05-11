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
 * SpectrumPoint.java
 * Copyright (C) 2009-2015 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrum;

import adams.data.container.AbstractDataPoint;
import adams.data.container.DataPoint;

/**
 * Abstract superclass for sequence points.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 11831 $
 */
public class SpectrumPoint
  extends AbstractDataPoint {

  /** for serialization. */
  private static final long serialVersionUID = 6358101255560429251L;

  /** the wave number. */
  protected float m_WaveNumber;

  /** the amplitude. */
  protected float m_Amplitude;

  /**
   * Initializes the point with wave number and amplitude as -1.
   */
  public SpectrumPoint() {
    this(-1.0f, -1.0f);
  }

  /**
   * Initializes the point with no ID.
   *
   * @param waveno		the X value
   * @param amplitude		the Y value
   */
  public SpectrumPoint(float waveno, float amplitude) {
    super();

    setWaveNumber(waveno);
    setAmplitude(amplitude);
    setParent(null);
  }

  /**
   * Obtains the stored variables from the other data point.
   *
   * @param other	the data point to get the values from
   */
  public void assign(DataPoint other) {
    SpectrumPoint	point;

    super.assign(other);

    point = (SpectrumPoint) other;

    setWaveNumber(point.getWaveNumber());
    setAmplitude(point.getAmplitude());
  }

  /**
   * Sets the wave number value.
   *
   * @param value	the new wave number
   */
  public void setWaveNumber(float value) {
    m_WaveNumber = value;
  }

  /**
   * Returns the wave number.
   *
   * @return		the wave number
   */
  public float getWaveNumber() {
    return m_WaveNumber;
  }

  /**
   * Sets the amplitude.
   *
   * @param value	the new amplitude
   */
  public void setAmplitude(float value) {
    m_Amplitude = value;
  }

  /**
   * Returns the amplitude.
   *
   * @return		the amplitude
   */
  public float getAmplitude() {
    return m_Amplitude;
  }

  /**
   * Compares this object with the specified object for order.  Returns a
   * negative integer, zero, or a positive integer as this object is less
   * than, equal to, or greater than the specified object.
   *
   * @param   o the object to be compared.
   * @return  a negative integer, zero, or a positive integer as this object
   *		is less than, equal to, or greater than the specified object.
   * @throws ClassCastException if the specified object's type prevents it
   *         from being compared to this object.
   */
  public int compareTo(Object o) {
    int			result;
    SpectrumPoint	other;

    if (o == null)
      return 1;
    else
      result = 0;

    other = (SpectrumPoint) o;

    result = new Double(getWaveNumber()).compareTo(new Double(other.getWaveNumber()));
    if (result == 0)
      result = new Double(getAmplitude()).compareTo(new Double(other.getAmplitude()));

    return result;
  }

  /**
   * Parses a string and instantiates a sequence point of it.
   *
   * @param s		the string to parse
   * @return		the instantiated point, null in case of an error
   */
  public SpectrumPoint parse(String s) {
    SpectrumPoint	result;
    String[]		parts;

    result = new SpectrumPoint();
    try {
      parts  = s.split(",");
      result.setWaveNumber(Float.parseFloat(parts[0]));
      result.setAmplitude(Float.parseFloat(parts[1]));
    }
    catch (Exception e) {
      System.err.println("Failed to parse spectrum point: " + s);
      e.printStackTrace();
      result = null;
    }

    return result;
  }

  /**
   * Returns a string representation of the point.
   *
   * @return		the string representation
   */
  public String toString() {
    return getWaveNumber() + "," + getAmplitude();
  }
}
