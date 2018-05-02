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

  /** x. */
  protected double m_X;

  /** y. */
  protected double m_Y;

  /**
   * Constructor.
   */
  public L2Point() {
    this(-1, -1);
  }

  /**
   * Constructor.
   *
   * @param x		the x
   * @param y		the y
   */
  public L2Point(double x, double y) {
    this(null, x, y);
  }

  /**
   * Constructor.
   *
   * @param parent	the parent
   * @param x		the x
   * @param y		the y
   */
  public L2Point(L1Point parent, double x, double y) {
    m_Parent = parent;
    m_X      = x;
    m_Y      = y;
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

    setX(point.getX());
    setY(point.getY());
  }

  /**
   * Sets the X.
   *
   * @param value	the x
   */
  public void setX(double value) {
    m_X = value;
  }

  /**
   * Returns the X.
   *
   * @return		the x
   */
  public double getX() {
    return m_X;
  }

  /**
   * Sets the y.
   *
   * @param value	the y
   */
  public void setY(double value) {
    m_Y = value;
  }

  /**
   * Returns the Y.
   *
   * @return		the y
   */
  public double getY() {
    return m_Y;
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
    else
      result = 0;

    p = (L2Point) o;

    if (result == 0)
      result = Double.compare(getX(), p.getX());

    if (result == 0)
      result = Double.compare(getY(), p.getY());

    return result;
  }

  /**
   * Returns a string representation of the GC points.
   *
   * @return		a string representation
   */
  public String toString() {
    String	result;

    result  = "x=" + getX();
    result += ", y=" + getY();

    return result;
  }
}
