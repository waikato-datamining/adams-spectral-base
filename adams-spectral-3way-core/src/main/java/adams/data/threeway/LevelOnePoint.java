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
 * LevelOnePoint.java
 * Copyright (C) 2017-2018 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.data.threeway;

import adams.data.container.AbstractDataContainer;
import adams.data.container.DataContainer;
import adams.data.container.DataPoint;
import adams.data.container.DataPointComparator;
import adams.data.statistics.LevelOnePointStatistic;

import java.util.Collections;

/**
 * Level 1 data point.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class LevelOnePoint
  extends AbstractDataContainer<LevelTwoPoint>
  implements DataPoint {

  /** for serialization. */
  private static final long serialVersionUID = -33831711862356573L;

  /** the parent. */
  protected ThreeWayData m_Parent;

  /** x. */
  protected double m_X;

  /** y. */
  protected double m_Y;

  /** point of greatest x. */
  protected LevelTwoPoint m_MaxX;

  /** point of smallest x. */
  protected LevelTwoPoint m_MinX;

  /** point of greatest y. */
  protected LevelTwoPoint m_MaxY;

  /** point of smallest y. */
  protected LevelTwoPoint m_MinY;

  /** the default comparator. */
  protected static DataPointComparator<LevelTwoPoint> m_Comparator;

  /**
   * Constructor.
   */
  public LevelOnePoint() {
    this(-1, -1);
  }

  /**
   * Constructor.
   *
   * @param x	the x
   * @param y	the y
   */
  public LevelOnePoint(double x, double y) {
    this(null, x, y);
  }

  /**
   * Constructor.
   *
   * @param parent	the parent
   * @param x		the x
   * @param y		the y
   */
  public LevelOnePoint(ThreeWayData parent, double x, double y) {
    m_X      = x;
    m_Y      = y;
    m_Parent = parent;
    m_MaxY   = null;
    m_MinY   = null;
    m_MaxX   = null;
    m_MinX   = null;
    if (m_Comparator == null)
      m_Comparator = newComparator();
  }

  /**
   * Returns a new instance of the default comparator.
   *
   * @return		the comparator instance
   */
  public DataPointComparator<LevelTwoPoint> newComparator() {
    return new LevelTwoPointComparator(true, true);
  }

  /**
   * Returns the comparator in use.
   *
   * @return		the comparator in use
   */
  public DataPointComparator<LevelTwoPoint> getComparator() {
    return m_Comparator;
  }

  /**
   * Returns a clone of itself. Sets the parent to null!
   *
   * @return		the clone
   */
  @Override
  public Object getClone() {
    DataPoint	result;

    result = (DataPoint) super.getClone();
    result.setParent(null);

    return result;
  }

  /**
   * Obtains the stored variables from the other data point, but not the
   * actual data points.
   *
   * @param other	the data point to get the values from
   */
  @Override
  public void assign(DataContainer<LevelTwoPoint> other) {
    super.assign(other);

    assign((DataPoint) other);
  }

  /**
   * Method that gets notified about changes in the collection of data points.
   *
   * @param modified	whether the action modified the collection
   * @return		the same as the input
   */
  @Override
  protected boolean modifiedListener(boolean modified) {
    if (modified)
      invalidateMinMax();

    return modified;
  }

  /**
   * Obtains the stored variables from the other data point.
   *
   * @param other	the data point to get the values from
   */
  public void assign(DataPoint other) {
    LevelOnePoint point;

    point = (LevelOnePoint) other;

    setX(point.getX());
    setY(point.getY());
    setParent(point.getParent());
  }

  /**
   * Returns a new instance of a DataContainer point.
   *
   * @return		the new DataContainer point
   */
  public LevelTwoPoint newPoint() {
    return new LevelTwoPoint(-1.0f, -1);
  }

  /**
   * Sets the parent this point belongs to.
   *
   * @param value	the parent
   */
  public void setParent(DataContainer value) {
    m_Parent = (ThreeWayData) value;
  }

  /**
   * Returns the parent this point belongs to.
   *
   * @return		the parent, can be null
   */
  public DataContainer getParent() {
    return m_Parent;
  }

  /**
   * Returns whether the point has a parent.
   *
   * @return		true if the point belongs to a data structure
   */
  public boolean hasParent() {
    return (m_Parent != null);
  }

  /**
   * Invalidates the min/max abundance/timestamp points.
   */
  protected synchronized void invalidateMinMax() {
    m_MinY = null;
    m_MaxY = null;
    m_MinX = null;
    m_MaxX = null;
  }

  /**
   * Initializes the min/max abundance/timestmap points.
   */
  protected synchronized void validateMinMax() {
    if (m_MinY != null)
      return;

    for (LevelTwoPoint point: this) {
      if (    (m_MaxY == null)
	   || (point.getY() > m_MaxY.getY()) )
	m_MaxY = point;
      if (    (m_MinY == null)
	   || (point.getY() < m_MinY.getY()))
	m_MinY = point;
      if (    (m_MaxX == null)
	   || (point.getX() > m_MaxX.getX()) )
	m_MaxX = point;
      if (    (m_MinX == null)
	   || (point.getX() < m_MinX.getX()) )
	m_MinX = point;
    }
  }

  /**
   * Get point with greatest X.
   *
   * @return	point
   */
  public LevelTwoPoint getMaxY() {
    validateMinMax();
    return m_MaxY;
  }

  /**
   * Get point with smallest Y.
   *
   * @return	point
   */
  public LevelTwoPoint getMinY() {
    validateMinMax();
    return m_MinY;
  }

  /**
   * Get point with greatest X.
   *
   * @return	point
   */
  public LevelTwoPoint getMaxX() {
    validateMinMax();
    return m_MaxX;
  }

  /**
   * Get point with smallest X.
   *
   * @return	point
   */
  public LevelTwoPoint getMinX() {
    validateMinMax();
    return m_MinX;
  }

  /**
   * Add a LevelTwoPoint.
   *
   * @param x		the x
   * @param y		the y
   */
  public void add(double x, double y) {
    add(new LevelTwoPoint(x, y));
  }

  /**
   * Returns the LevelTwoPoint with the exact x, null if not found.
   *
   * @param x		the x to look for
   * @return		the LevelTwoPoint or null if not found
   */
  public LevelTwoPoint find(double x) {
    LevelTwoPoint result;

    result = findClosest(x);
    if ((result != null) && (result.getX() != x))
      result = null;

    return result;
  }

  /**
   * Returns the LevelTwoPoint with the x closest to the one provided.
   *
   * @param x		the x to look for
   * @return		the LevelTwoPoint
   */
  public LevelTwoPoint findClosest(double x) {
    LevelTwoPoint 	result;
    int			index;
    LevelTwoPoint 	currPoint;
    double		currDist;
    double		dist;
    int			i;

    result = null;

    if (m_Points.size() == 0)
      return result;

    index = Collections.binarySearch(m_Points, new LevelTwoPoint(x, 0), m_Comparator);

    // no exact match -> find closest
    if (index < 0) {
      index = -index;
      if (index >= m_Points.size())
	index = m_Points.size() - 1;
      result = m_Points.get(index);
      dist   = Math.abs(x - result.getX());

      for (i = index - 2; i <= index + 2; i++) {
	if ((i >= 0) && (i < m_Points.size())) {
	  currPoint = m_Points.get(i);
	  currDist  = Math.abs(x - currPoint.getX());

	  if (currDist < dist) {
	    dist   = currDist;
	    result = currPoint;
	  }
	}
      }
    }
    else {
      result = m_Points.get(index);
    }

    return result;
  }

  /**
   * Set the X.
   *
   * @param x		the X
   */
  public void setX(double x) {
    m_X = x;
  }

  /**
   * Return the X.
   *
   * @return		X
   */
  public double getX() {
    return m_X;
  }

  /**
   * Sets the Y.
   *
   * @param value	the Y
   */
  public void setY(double value) {
    m_Y = value;
  }

  /**
   * Returns the Y.
   *
   * @return 		the Y
   */
  public double getY() {
    return m_Y;
  }

  /**
   * Uses the sum of the Y values of all level 2 points for its own Y value.
   */
  public void updateY() {
    double	sum;

    sum = 0.0;
    for (LevelTwoPoint p: this)
      sum += p.getY();
    setY(sum);
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
  @Override
  public int compareToHeader(Object o) {
    int		result;
    LevelOnePoint p;

    result = super.compareToHeader(o);
    if (result != 0)
      return result;

    p = (LevelOnePoint) o;

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
  @Override
  public String toString() {
    String	result;

    result  = "X=" + getX();
    result += ", Y=" + getY();
    result += ", # points=" + size();

    return result;
  }

  /**
   * Returns a statistic object of this GC point.
   *
   * @return		statistics about this GC point
   */
  public LevelOnePointStatistic toStatistic() {
    return new LevelOnePointStatistic(this);
  }
}
