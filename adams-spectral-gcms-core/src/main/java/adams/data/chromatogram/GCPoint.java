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
 * GCPoint.java
 * Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.data.chromatogram;

import adams.core.io.PlaceholderFile;
import adams.data.container.AbstractDataContainer;
import adams.data.container.DataContainer;
import adams.data.container.DataPoint;
import adams.data.container.DataPointComparator;
import adams.data.statistics.GCPointStatistic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

/**
 * Mass Spectrogram, plus GC date (time, abundance).
 *
 * @author dale
 * @version $Revision: 4217 $
 */
public class GCPoint
  extends AbstractDataContainer<MSPoint>
  implements DataPoint {

  /** for serialization. */
  private static final long serialVersionUID = -33831711862356573L;

  /** the file extension for a GC point (incl. dot). */
  public final static String FILE_EXTENSION = ".gc";

  /** the parent chromatogram. */
  protected Chromatogram m_Parent;

  /** time (numsecs * 100 since start). */
  protected long m_timestamp;

  /** total abundance. */
  protected long m_abundance;

  /** mspoint of greatest abundance. */
  protected MSPoint m_MaxAbundance;

  /** mspoint of smallest abundance. */
  protected MSPoint m_MinAbundance;

  /** mspoint of greatest m/z. */
  protected MSPoint m_MaxMassCharge;

  /** mspoint of smallest m/z. */
  protected MSPoint m_MinMassCharge;

  /** the default comparator. */
  protected static DataPointComparator<MSPoint> m_Comparator;

  /**
   * Constructor.
   */
  public GCPoint() {
    this(-1, -1);
  }

  /**
   * Constructor.
   *
   * @param timestamp	secs*100
   * @param abundance	total ion count (?)
   */
  public GCPoint(long timestamp, long abundance) {
    this(null, timestamp, abundance);
  }

  /**
   * Constructor.
   *
   * @param parent	the chromatogram this GC point belongs to
   * @param timestamp	secs*100
   * @param abundance	total ion count (?)
   */
  public GCPoint(Chromatogram parent, long timestamp, long abundance) {
    m_timestamp     = timestamp;
    m_abundance     = abundance;
    m_Parent        = parent;
    m_MaxAbundance  = null;
    m_MinAbundance  = null;
    m_MaxMassCharge = null;
    m_MinMassCharge = null;
    if (m_Comparator == null)
      m_Comparator = newComparator();
  }

  /**
   * Returns a new instance of the default comparator.
   *
   * @return		the comparator instance
   */
  public DataPointComparator<MSPoint> newComparator() {
    return new MSComparator();
  }

  /**
   * Returns the comparator in use.
   *
   * @return		the comparator in use
   */
  public DataPointComparator<MSPoint> getComparator() {
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
  public void assign(DataContainer<MSPoint> other) {
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
    GCPoint	point;

    point = (GCPoint) other;

    setTimestamp(point.getTimestamp());
    setAbundance(point.getAbundance());
    setParent((Chromatogram) point.getParent());
  }

  /**
   * Returns a new instance of a DataContainer point.
   *
   * @return		the new DataContainer point
   */
  public MSPoint newPoint() {
    return new MSPoint(-1.0f, -1);
  }

  /**
   * Sets the spectrum this point belongs to.
   *
   * @param value	the spectrum
   */
  public void setParent(DataContainer value) {
    m_Parent = (Chromatogram) value;
  }

  /**
   * Returns the spectrum this point belongs to.
   *
   * @return		the spectrum, can be null
   */
  public DataContainer getParent() {
    return m_Parent;
  }

  /**
   * Returns whether the point belongs to a spectrum.
   *
   * @return		true if the point belongs to a spectrum
   */
  public boolean hasParent() {
    return (m_Parent != null);
  }

  /**
   * Invalidates the min/max abundance/timestamp points.
   */
  protected synchronized void invalidateMinMax() {
    m_MinAbundance  = null;
    m_MaxAbundance  = null;
    m_MinMassCharge = null;
    m_MaxMassCharge = null;
  }

  /**
   * Initializes the min/max abundance/timestmap points.
   */
  protected synchronized void validateMinMax() {
    if (m_MinAbundance != null)
      return;

    for (MSPoint point: this) {
      if (    (m_MaxAbundance == null)
	   || (point.getAbundance() > m_MaxAbundance.getAbundance()) )
	m_MaxAbundance = point;
      if (    (m_MinAbundance == null)
	   || (point.getAbundance() < m_MinAbundance.getAbundance()))
	m_MinAbundance = point;
      if (    (m_MaxMassCharge == null)
	   || (point.getMassCharge() > m_MaxMassCharge.getMassCharge()) )
	m_MaxMassCharge = point;
      if (    (m_MinMassCharge == null)
	   || (point.getMassCharge() < m_MinMassCharge.getMassCharge()) )
	m_MinMassCharge = point;
    }
  }

  /**
   * Get point with greatest abundance.
   *
   * @return	ms point
   */
  public MSPoint getMaxAbundance() {
    validateMinMax();
    return m_MaxAbundance;
  }

  /**
   * Get point with smallest abundance.
   *
   * @return	ms point
   */
  public MSPoint getMinAbundance() {
    validateMinMax();
    return m_MinAbundance;
  }

  /**
   * Get point with greatest m/z.
   *
   * @return	ms point
   */
  public MSPoint getMaxMassCharge() {
    validateMinMax();
    return m_MaxMassCharge;
  }

  /**
   * Get point with smallest m/z.
   *
   * @return	ms point
   */
  public MSPoint getMinMassCharge() {
    validateMinMax();
    return m_MinMassCharge;
  }

  /**
   * Add a Mass Spec datapoint.
   *
   * @param mz		mass/charge ratio
   * @param abund	abundance
   */
  public void add(float mz, int abund) {
    add(new MSPoint(mz,abund));
  }

  /**
   * Returns the MSPoint with the exact mass-to-charge, null if not found.
   *
   * @param mz		the m/z to look for
   * @return		the MSPoint or null if not found
   */
  public MSPoint find(float mz) {
    MSPoint	result;

    result = findClosest(mz);
    if ((result != null) && (result.getMassCharge() != mz))
      result = null;

    return result;
  }

  /**
   * Returns the MSPoint with a mass-to-charge closest to the one provided.
   *
   * @param mz		the m/z to look for
   * @return		the MSPoint
   */
  public MSPoint findClosest(float mz) {
    MSPoint	result;
    int		index;
    MSPoint	currPoint;
    double	currDist;
    double	dist;
    int		i;

    result = null;

    if (m_Points.size() == 0)
      return result;

    index = Collections.binarySearch(m_Points, new MSPoint(mz, 0), m_Comparator);

    // no exact match -> find closest
    if (index < 0) {
      index = -index;
      if (index >= m_Points.size())
	index = m_Points.size() - 1;
      result = m_Points.get(index);
      dist   = Math.abs(mz - result.getMassCharge());

      for (i = index - 2; i <= index + 2; i++) {
	if ((i >= 0) && (i < m_Points.size())) {
	  currPoint = m_Points.get(i);
	  currDist  = Math.abs(mz - currPoint.getMassCharge());

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
   * Get timestamp.
   *
   * @return	timestamp in seconds * 100
   */
  public long getTimestamp() {
    return(m_timestamp);
  }

  /**
   * Set timestamp.
   *
   * @param ts	timestamp in seconds * 100
   */
  public void setTimestamp(long ts) {
    m_timestamp=ts;
  }

  /**
   * Sets the abundance.
   *
   * @param value	the abundance
   */
  public void setAbundance(long value) {
    m_abundance = value;
  }

  /**
   * Get abundance.
   *
   * @return abundance
   */
  public long getAbundance() {
    return(m_abundance);
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
    GCPoint	p;

    result = super.compareToHeader(o);
    if (result != 0)
      return result;

    p = (GCPoint) o;

    if (result == 0)
      result = new Long(getTimestamp()).compareTo(new Long(p.getTimestamp()));

    if (result == 0)
      result = new Long(getAbundance()).compareTo(new Long(p.getAbundance()));

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

    result  = "Timestamp=" + getTimestamp();
    result += ", Abundance=" + getAbundance();
    result += ", # MS points=" + size();
    if (size() > 0) {
      result += ", min-m/z=" + m_Points.get(0).getMassCharge();
      result += ", max-m/z=" + m_Points.get(m_Points.size() - 1).getMassCharge();
    }

    return result;
  }

  /**
   * Returns a statistic object of this GC point.
   *
   * @return		statistics about this GC point
   */
  public GCPointStatistic toStatistic() {
    return new GCPointStatistic(this);
  }

  /**
   * Returns a vector with the points. Generates a new vector, use toList()
   * instead.
   *
   * @return		a vector with all the points
   */
  @Deprecated
  public Vector<MSPoint> toVector() {
    return toVector(getComparator());
  }

  /**
   * Returns a vector with the points.
   *
   * @param comparator	the comparator to use
   * @return		a vector with all the points
   */
  @Deprecated
  public Vector<MSPoint> toVector(DataPointComparator comparator) {
    Vector<MSPoint>	result;

    result = new Vector<MSPoint>(m_Points);
    Collections.sort(result, comparator);

    return result;
  }

  /**
   * Writes the GC point to a file.
   *
   * @param filename	the file to write to
   * @param data	the GC point to write
   * @return		true if successful
   */
  public static boolean write(String filename, GCPoint data) {
    boolean		result;
    BufferedWriter	writer;

    try {
      writer = new BufferedWriter(new FileWriter(filename));
      result = write(writer, data);
      writer.flush();
      writer.close();
      result = true;
    }
    catch (Exception e) {
      result = false;
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Writes the GC point to a writer.
   *
   * @param writer	the writer to write to
   * @param data	the GC point to write
   * @return		true if successful
   */
  public static boolean write(BufferedWriter writer, GCPoint data) {
    boolean		result;
    Iterator<MSPoint>	msIter;

    result = true;

    try {
      writer.write("" + data.getTimestamp() + "/" + data.getAbundance());
      writer.newLine();
      msIter = data.toList().iterator();
      while (msIter.hasNext() && result)
	MSPoint.write(writer, msIter.next());
      writer.newLine();
    }
    catch (Exception e) {
      result = false;
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Reads a GC point from the reader.
   *
   * @param filename	the file to read the point from
   * @return		the GC point or null if not possible/error occurred
   */
  public static GCPoint read(String filename) {
    GCPoint		result;
    BufferedReader	reader;

    result = null;

    try {
      reader = new BufferedReader(new FileReader(new PlaceholderFile(filename).getAbsolutePath()));
      result = read(reader);
      reader.close();
    }
    catch (Exception e) {
      result = null;
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Reads a GC point from the reader.
   *
   * @param reader	the reader to read the point from
   * @return		the GC point or null if not possible/error occurred
   */
  public static GCPoint read(BufferedReader reader) {
    GCPoint	result;
    String	line;
    String[]	parts;
    MSPoint	mspoint;

    result = null;

    try {
      while ((line = reader.readLine()) != null) {
	if (line.trim().length() > 0)
	  break;
      }
      if ((line != null) && (line.trim().length() > 0)) {
	parts  = line.split("\\/");
	result = new GCPoint(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
	while ((mspoint = MSPoint.read(reader)) != null)
	  result.add(mspoint);
      }
    }
    catch (Exception e) {
      result = null;
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Only for testing find/findClosest methods.
   *
   * @param args	ignored
   */
  public static void main(String[] args) {
    GCPoint gp = new GCPoint(0, 0);
    gp.add(10, 0);
    gp.add(20, 0);
    gp.add(30, 0);
    gp.add(40, 0);

    MSPoint p = new MSPoint(0, 0);
    for (int i = 0; i < 50; i++) {
      System.out.println(p + " -> exact: " + gp.find(p.getMassCharge()));
      System.out.println(p + " -> closest: " + gp.findClosest(p.getMassCharge()));
      p.setMassCharge(p.getMassCharge() + 1);
    }
  }
}
