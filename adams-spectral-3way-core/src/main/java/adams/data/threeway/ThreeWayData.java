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
 * Copyright (C) 2017-2018 University of Waikato, Hamilton, New Zealand
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
import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.spreadsheet.SpreadSheetSupporter;
import adams.data.statistics.InformativeStatisticSupporter;
import adams.data.statistics.ThreeWayDataStatistic;
import adams.data.threewayreport.ThreeWayReport;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.set.TDoubleSet;
import gnu.trove.set.hash.TDoubleHashSet;

import java.util.Date;
import java.util.Iterator;

/**
 * Stores 3-way data and associated meta-data.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class ThreeWayData
  extends AbstractDataContainer<L1Point>
  implements DatabaseNotesHandler, MutableReportHandler<ThreeWayReport>,
             InformativeStatisticSupporter<ThreeWayDataStatistic>, SpreadSheetSupporter  {

  /** for serialization. */
  private static final long serialVersionUID = 1318149681899877295L;

  /** database id. */
  protected int m_DatabaseID = Constants.NO_ID;

  /** 'reference' values. */
  protected ThreeWayReport m_Reference;

  /** point of greatest Y. */
  protected L1Point m_MaxY;

  /** point of smallest Y. */
  protected L1Point m_MinY;

  /** point of greatest X. */
  protected L1Point m_MaxX;

  /** point of smallest X. */
  protected L1Point m_MinX;

  /** the notes for the data structure. */
  protected Notes m_Notes;

  /** the default comparator. */
  protected static DataPointComparator<L1Point> m_Comparator;

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
  public DataPointComparator<L1Point> newComparator() {
    return new L1PointComparator(true, true);
  }

  /**
   * Returns the comparator in use.
   *
   * @return		the comparator in use
   */
  public DataPointComparator<L1Point> getComparator() {
    return m_Comparator;
  }

  /**
   * Returns a new instance of a DataContainer point.
   *
   * @return		the new DataContainer point
   */
  public L1Point newPoint() {
    return new L1Point(-1, -1);
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

    for (L1Point point: this) {
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
  public L1Point getMaxY(){
    validateMinMax();
    return m_MaxY;
  }

  /**
   * Get point with smallest abundance.
   *
   * @return	gc point
   */
  public L1Point getMinY(){
    validateMinMax();
    return m_MinY;
  }

  /**
   * Get point with greatest timestamp.
   *
   * @return	gc point
   */
  public L1Point getMaxX(){
    validateMinMax();
    return m_MaxX;
  }

  /**
   * Get point with smallest Timestamp.
   *
   * @return	gc point
   */
  public L1Point getMinX(){
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
   * @param y		the Y to look for
   * @return		the level 1 point or null if not found
   * @see		#findClosest(double,double)
   */
  public L1Point find(double x, double y) {
    L1Point result;
    int		index;

    result = null;

    index = ThreeWayDataUtils.findXY(m_Points, x, y);
    if (index > -1)
      result = m_Points.get(index);

    return result;
  }

  /**
   * Returns the level 1 point with a X closest to the one provided.
   *
   * @param x		the X to look for
   * @return		the level 1 point
   * @see		#find(double,double)
   */
  public L1Point findClosest(double x, double y) {
    L1Point result;
    int		index;

    result = null;

    index = ThreeWayDataUtils.findClosestXY(m_Points, x, y);
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
    Iterator<L1Point>	iter;
    L1Point l1point;
    L1Point newL1Point;
    L2Point l2point;
    L2Point newL2Point;

    result = (ThreeWayData) getHeader();

    iter = iterator();
    while (iter.hasNext()) {
      l1point = iter.next();
      l2point = l1point.find(x);
      if (l2point != null) {
	newL1Point = new L1Point(l1point.getX(), l2point.getData());
	newL2Point = new L2Point(l2point.getZ(), l2point.getData());
	newL1Point.add(newL2Point);
	result.add(newL1Point);
      }
      else {
	newL1Point = new L1Point(l1point.getX(), 0);
	result.add(newL1Point);
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
  public void assign(DataContainer<L1Point> other) {
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

  /**
   * Returns the content as spreadsheet.
   *
   * @return		the content
   */
  public SpreadSheet toSpreadSheet() {
    SpreadSheet		result;
    Row			row;

    result = new DefaultSpreadSheet();
    row    = result.getHeaderRow();
    row.addCell("X").setContentAsString("X");
    row.addCell("Y").setContentAsString("Y");
    row.addCell("Z").setContentAsString("Z");
    row.addCell("D").setContentAsString("Data");
    for (L1Point l1: this) {
      for (L2Point l2: l1) {
        row = result.addRow();
        row.addCell("X").setContent(l1.getX());
        row.addCell("Y").setContent(l1.getY());
        row.addCell("Z").setContent(l2.getZ());
        row.addCell("D").setContent(l2.getData());
      }
    }

    return result;
  }

  /**
   * Returns the L2 point associated with the given x, y and z.
   *
   * @param x		the X of the point to retrieve
   * @param y		the Y of the point to retrieve
   * @param z		the Z of the point to retrieve
   * @return		the point, null if not found for these coordinates
   */
  public L2Point get(double x, double y, double z) {
    L2Point	result;
    L1Point	point;

    result = null;
    point  = find(x, y);
    if (point != null)
      result = point.find(z);

    return result;
  }

  /**
   * Returns all Xs.
   *
   * @return		the list of Xs
   */
  public TDoubleList getAllX() {
    TDoubleList		result;
    TDoubleSet 		set;

    result = new TDoubleArrayList();
    set    = new TDoubleHashSet();
    for (L1Point l1: this)
      set.add(l1.getX());
    result.addAll(set);
    result.sort();

    return result;
  }

  /**
   * Returns all Ys.
   *
   * @return		the list of Ys
   */
  public TDoubleList getAllY() {
    TDoubleList		result;
    TDoubleSet 		set;

    result = new TDoubleArrayList();
    set    = new TDoubleHashSet();
    for (L1Point l1: this)
      set.add(l1.getY());
    result.addAll(set);
    result.sort();

    return result;
  }

  /**
   * Returns all Zs.
   *
   * @return		the list of Zs
   */
  public TDoubleList getAllZ() {
    TDoubleList		result;
    TDoubleSet 		set;

    result = new TDoubleArrayList();
    set    = new TDoubleHashSet();
    for (L1Point l1: this) {
      for (L2Point l2: l1)
	set.add(l2.getZ());
    }
    result.addAll(set);
    result.sort();

    return result;
  }
}
