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
 * L2Point.java
 * Copyright (C) 2017-2018 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.data.threeway;

import adams.data.container.AbstractDataPoint;
import adams.data.container.DataPoint;

/**
 * Level 2 data point.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class L2Point
  extends AbstractDataPoint {

  /** for serialization. */
  private static final long serialVersionUID = -3787806109050570412L;

  /** z. */
  protected double m_Z;

  /** value. */
  protected double m_Data;

  /**
   * Constructor.
   */
  public L2Point() {
    this(-1, -1);
  }

  /**
   * Constructor.
   *
   * @param z		the z
   * @param data	the value
   */
  public L2Point(double z, double data) {
    this(null, z, data);
  }

  /**
   * Constructor.
   *
   * @param parent	the parent
   * @param z		the z
   * @param data	the value
   */
  public L2Point(L1Point parent, double z, double data) {
    m_Parent = parent;
    m_Z = z;
    m_Data = data;
  }

  /**
   * Obtains the stored variables from the other data point.
   *
   * @param other	the data point to get the values from
   */
  public void assign(DataPoint other) {
    L2Point point;

    super.assign(other);

    point = (L2Point) other;

    setZ(point.getZ());
    setData(point.getData());
  }

  /**
   * Sets the Z.
   *
   * @param value	the z
   */
  public void setZ(double value) {
    m_Z = value;
  }

  /**
   * Returns the Z.
   *
   * @return		the z
   */
  public double getZ() {
    return m_Z;
  }

  /**
   * Sets the value.
   *
   * @param value	the value
   */
  public void setData(double value) {
    m_Data = value;
  }

  /**
   * Returns the value.
   *
   * @return		the value
   */
  public double getData() {
    return m_Data;
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
    int		result;
    L2Point p;

    if (o == null)
      return 1;

    p      = (L2Point) o;
    result = Double.compare(getZ(), p.getZ());

    if (result == 0)
      result = Double.compare(getData(), p.getData());

    return result;
  }

  /**
   * Returns a string representation of the GC points.
   *
   * @return		a string representation
   */
  public String toString() {
    String	result;

    result  = "Z=" + getZ();
    result += ", V=" + getData();

    return result;
  }
}
