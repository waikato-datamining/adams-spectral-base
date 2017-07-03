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
 * ThreeWayData.java
 * Copyright (C) 2017 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.data.threeway;

import adams.core.Constants;
import adams.core.Utils;
import adams.data.DatabaseNotesHandler;
import adams.data.Notes;
import adams.data.container.AbstractDataContainer;
import adams.data.container.DataContainer;
import adams.data.container.DataPointComparator;
import adams.data.report.MutableReportHandler;
import adams.data.statistics.InformativeStatisticSupporter;
import adams.data.statistics.ThreeWayDataStatistic;
import adams.data.threewayreport.ThreeWayReport;

import java.util.Date;
import java.util.Iterator;

/**
 * Stores a complete chromatogram plus reference values (if they exist).
 *
 * @author dale
 * @version $Revision: 4313 $
 */
public class ThreeWayData
  extends AbstractDataContainer<LevelOnePoint>
  implements DatabaseNotesHandler, MutableReportHandler<ThreeWayReport>,
             InformativeStatisticSupporter<ThreeWayDataStatistic>{

  /** for serialization. */
  private static final long serialVersionUID = 1318149681899877295L;

  /** database id. */
  protected int m_DatabaseID = Constants.NO_ID;

  /** 'reference' values. */
  protected ThreeWayReport m_Reference;

  /** point of greatest Y. */
  protected LevelOnePoint m_MaxY;

  /** point of smallest Y. */
  protected LevelOnePoint m_MinY;

  /** point of greatest X. */
  protected LevelOnePoint m_MaxX;

  /** point of smallest X. */
  protected LevelOnePoint m_MinX;

  /** the notes for the data structure. */
  protected Notes m_Notes;

  /** the default comparator. */
  protected static DataPointComparator<LevelOnePoint> m_Comparator;

  /**
   * Initialise data.
   */
  public ThreeWayData() {
    this(new Date().toString());
  }

  /**
   * Initialise data.
   *
   * @param id		ID for data
   */
  public ThreeWayData(String id){
    m_ID        = id;
    m_Reference = new ThreeWayReport();
    m_MaxY      = null;
    m_MinY      = null;
    m_MaxX      = null;
    m_MinX      = null;
    m_Notes     = new Notes();
    if (m_Comparator == null)
      m_Comparator = newComparator();
  }

  /**
   * Returns a new instance of the default comparator.
   *
   * @return		the comparator instance
   */
  public DataPointComparator<LevelOnePoint> newComparator() {
    return new LevelOnePointComparator();
  }

  /**
   * Returns the comparator in use.
   *
   * @return		the comparator in use
   */
  public DataPointComparator<LevelOnePoint> getComparator() {
    return m_Comparator;
  }

  /**
   * Returns a new instance of a DataContainer point.
   *
   * @return		the new DataContainer point
   */
  public LevelOnePoint newPoint() {
    return new LevelOnePoint(-1, -1);
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

    for (LevelOnePoint point: this) {
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
   * Sets the ID of the chromatogram.
   *
   * @param value	the new ID
   */
  @Override
  public void setID(String value) {
    super.setID(value);

    if (m_Reference != null) {
      m_Reference.setID(value);
      m_Reference.update();
    }
  }

  /**
   * Set the database ID.
   *
   * @param value	Database ID
   */
  public void setDatabaseID(int value){
    m_DatabaseID = value;
    if (m_Reference != null) {
      m_Reference.setDatabaseID(value);
      m_Reference.update();
    }
  }

  /**
   * Get database id.
   *
   * @return database ID
   */
  public int getDatabaseID(){
    return m_DatabaseID;
  }

  /**
   * Set the report (reference values).
   *
   * @param value	report
   */
  public void setReport(ThreeWayReport value){
    m_Reference = value;
    if (m_Reference != null) {
      setDatabaseID(getDatabaseID());
      if (getID() != null)
	setID(getID());
    }
  }

  /**
   * Get the report (reference values).
   *
   * @return 		report
   */
  public ThreeWayReport getReport(){
    return m_Reference;
  }

  /**
   * True if Quantitation Report exists for this chromatogram.
   *
   * @return does this chromatogram have a Quantitation Report
   */
  public boolean hasReport(){
    return (m_Reference != null);
  }

  /**
   * Get point with greatest abundance.
   *
   * @return	gc point
   */
  public LevelOnePoint getMaxY(){
    validateMinMax();
    return m_MaxY;
  }

  /**
   * Get point with smallest abundance.
   *
   * @return	gc point
   */
  public LevelOnePoint getMinY(){
    validateMinMax();
    return m_MinY;
  }

  /**
   * Get point with greatest timestamp.
   *
   * @return	gc point
   */
  public LevelOnePoint getMaxX(){
    validateMinMax();
    return m_MaxX;
  }

  /**
   * Get point with smallest Timestamp.
   *
   * @return	gc point
   */
  public LevelOnePoint getMinX(){
    validateMinMax();
    return m_MinX;
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
   * Returns the level 1 point with the exact X, null if not found.
   *
   * @param x		the X to look for
   * @return		the level 1 point or null if not found
   * @see		#findClosest(double)
   */
  public LevelOnePoint find(double x) {
    LevelOnePoint result;
    int		index;

    result = null;

    index = ThreeWayDataUtils.findX(m_Points, x);
    if (index > -1)
      result = m_Points.get(index);

    return result;
  }

  /**
   * Returns the level 1 point with a X closest to the one provided.
   *
   * @param x		the X to look for
   * @return		the level 1 point
   * @see		#find(double)
   */
  public LevelOnePoint findClosest(double x) {
    LevelOnePoint result;
    int		index;

    result = null;

    index = ThreeWayDataUtils.findClosestX(m_Points, x);
    if (index > -1)
      result = m_Points.get(index);

    return result;
  }

  /**
   * Returns a data structure which contains only the given x values.
   * Adds level 1 points with y=0 if a level 1 point does not contain the chosen x.
   *
   * @param x		the x to look for
   * @return		the new data structure
   */
  public ThreeWayData getXSubset(double x) {
    ThreeWayData 		result;
    Iterator<LevelOnePoint>	iter;
    LevelOnePoint 		l1point;
    LevelOnePoint 		newLevelOnePoint;
    LevelTwoPoint 		l2point;
    LevelTwoPoint 		newLevelTwoPoint;

    result = (ThreeWayData) getHeader();

    iter = iterator();
    while (iter.hasNext()) {
      l1point = iter.next();
      l2point = l1point.find(x);
      if (l2point != null) {
	newLevelOnePoint = new LevelOnePoint(l1point.getX(), l2point.getY());
	newLevelTwoPoint = new LevelTwoPoint(l2point.getX(), l2point.getY());
	newLevelOnePoint.add(newLevelTwoPoint);
	result.add(newLevelOnePoint);
      }
      else {
	newLevelOnePoint = new LevelOnePoint(l1point.getX(), 0);
	result.add(newLevelOnePoint);
      }
    }

    return result;
  }

  /**
   * Obtains the stored variables from the other data point, but not the
   * actual data points.
   *
   * @param other	the data point to get the values from
   */
  @Override
  public void assign(DataContainer<LevelOnePoint> other) {
    ThreeWayData 	data;

    super.assign(other);

    data = (ThreeWayData) other;

    setDatabaseID(data.getDatabaseID());
    if (data.hasReport())
      setReport((ThreeWayReport) data.getReport().getClone());
    m_Notes = new Notes();
    m_Notes.mergeWith(data.getNotes());
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
    int			result;
    ThreeWayData c;

    result = super.compareToHeader(o);
    if (result != 0)
      return result;

    c = (ThreeWayData) o;

    if (result == 0)
      result = Integer.compare(getDatabaseID(), c.getDatabaseID());

    if (result == 0)
      result = Utils.compare(getReport(), c.getReport());

    return result;
  }

  /**
   * Indicates whether some other chromatogram's header is "equal to" this ones.
   *
   * @param obj		the reference object with which to compare.
   * @return		true if this object is the same as the obj argument;
   * 			false otherwise.
   */
  @Override
  public boolean equalsHeader(Object obj) {
    return (compareToHeader(obj) == 0);
  }

  /**
   * Returns the hashcode. Just the hashcode of the toString()-generated
   * string.
   *
   * @return		the hashcode
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * Returns a short string representation of the chromatogram.
   *
   * @return		a string representation
   */
  @Override
  public String toString() {
    String	result;

    result  = "ID=" + getID();
    result += ", DB-ID=" + getDatabaseID();
    result += ", # Level 1 points=" + size();
    if (size() > 0) {
      result += ", first X=" + m_Points.get(0).getX();
      result += ", last X=" + m_Points.get(m_Points.size() - 1).getX();
    }

    return result;
  }

  /**
   * Returns a statistic object of this data structure.
   *
   * @return		statistics about this data structure
   */
  public ThreeWayDataStatistic toStatistic() {
    return new ThreeWayDataStatistic(this);
  }

  /**
   * Returns the currently stored notes.
   *
   * @return		the current notes
   */
  public Notes getNotes() {
    return m_Notes;
  }
}
