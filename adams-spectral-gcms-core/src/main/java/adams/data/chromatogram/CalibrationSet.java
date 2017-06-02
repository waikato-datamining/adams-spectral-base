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
 * CalibrationSet.java
 * Copyright (C) 2009-2012 University of Waikato, Hamilton, New Zealand
 */

package adams.data.chromatogram;

import adams.core.CloneHandler;
import adams.core.DateUtils;
import adams.core.logging.LoggingObject;
import adams.data.quantitation.QuantitationReport;
import adams.data.report.DataType;
import adams.data.report.Field;

import java.util.Vector;

/**
 * A container class for a complete calibration set, i.e., multiple
 * chromatograms.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4222 $
 */
public class CalibrationSet
  extends LoggingObject
  implements Comparable, CloneHandler<CalibrationSet> {

  /** for serialization. */
  private static final long serialVersionUID = -3394480783803410267L;

  /** suffix for fake chromatograms, generated based on quantitation reports. */
  public final static String FAKE_CHROMATOGRAM_SUFFIX = "-fake";

  /** the set's ID. */
  protected String m_ID;

  /** the chromatograms. */
  protected Chromatogram[] m_Set;

  /** whether the chromatograms are only dummy ones. */
  protected boolean m_IsDummy;

  /**
   * Initializes the set. NB: creates dummy chromatograms based on the data
   * stored in the reports!
   *
   * @param id		the ID/name of the calibration set
   * @param set		the chromatograms of the set
   */
  public CalibrationSet(String id, QuantitationReport[] set) {
    this(id, new Chromatogram[0]);

    m_IsDummy = true;

    // create dummy chromatograms
    Chromatogram[] newSet = new Chromatogram[set.length];
    Field sampleid = new Field(QuantitationReport.FIELD_SAMPLEID, DataType.STRING);
    for (int i = 0; i < set.length; i++) {
      if (!set[i].hasValue(sampleid))
	newSet[i] = new Chromatogram(id + "/" + i + FAKE_CHROMATOGRAM_SUFFIX);
      else
	newSet[i] = new Chromatogram(set[i].getStringValue(sampleid));
      newSet[i].setDatabaseID(set[i].getDatabaseID());
      newSet[i].setReport((QuantitationReport) set[i].getClone());
      String acqDate = set[i].getStringValue(new Field(QuantitationReport.FIELD_ACQON_SORTABLE, DataType.STRING));
      if (acqDate != null)
	newSet[i].setDateGeneration(DateUtils.getTimestampFormatter().parse(acqDate));
      String inst = set[i].getStringValue(new Field(QuantitationReport.FIELD_INST, DataType.STRING));
      if (inst != null)
	newSet[i].setInstrument(inst);
    }
    m_Set = newSet;
  }

  /**
   * Initializes the set. NB: does not create copies of the chromatograms!
   *
   * @param id		the ID/name of the calibration set
   * @param set		the chromatograms of the set
   */
  public CalibrationSet(String id, Chromatogram[] set) {
    super();

    m_ID      = id;
    m_IsDummy = false;
    m_Set     = new Chromatogram[set.length];
    for (int i = 0; i < set.length; i++)
      m_Set[i] = set[i];
  }

  /**
   * Returns a clone of itself.
   *
   * @return		the clone
   */
  public CalibrationSet getClone() {
    return subset(null);
  }

  /**
   * Returns the ID/name of the calibration set.
   *
   * @return		the ID/name
   */
  public String getID() {
    return m_ID;
  }

  /**
   * Returns whether the chromatograms are only dummy ones, e.g., when
   * initializing the calibration set with quantitation reports only.
   *
   * @return		true if chromatograms are only dummy ones
   */
  public boolean isDummy() {
    return m_IsDummy;
  }

  /**
   * Returns the number of chromatograms that make up this set.
   *
   * @return		the number of chromatograms
   */
  public int size() {
    return m_Set.length;
  }

  /**
   * Returns the chromatogram at the specified index.
   *
   * @param index	the index in the set
   * @return		the chromatogram at the position
   */
  public Chromatogram getChromatogram(int index) {
    return m_Set[index];
  }

  /**
   * Returns the quantitation report at the specified index.
   *
   * @param index	the index in the set
   * @return		the quantitation report at the position
   */
  public QuantitationReport getReport(int index) {
    return m_Set[index].getReport();
  }

  /**
   * Returns whether the chromatogram at the specified position has a
   * quantitation report.
   *
   * @param index	the index in the set
   * @return		true if a report is available
   */
  public boolean hasReport(int index) {
    return m_Set[index].hasReport();
  }

  /**
   * Creates a new set, including only the chromatograms with the
   * specified indices. NB: The chromatograms are cloned.
   *
   * @param indices	the indices of the chromatograms to include, use null
   * 			to include all
   * @return		the subset
   */
  public CalibrationSet subset(int[] indices) {
    CalibrationSet		result;
    Vector<Chromatogram>	set;
    int				i;

    set = new Vector<Chromatogram>();
    if (indices == null) {
      for (i = 0; i < m_Set.length; i++)
	set.add((Chromatogram) m_Set[i].getClone());
    }
    else {
      for (i = 0; i < indices.length; i++)
	set.add((Chromatogram) m_Set[indices[i]].getClone());
    }

    result = new CalibrationSet(m_ID, set.toArray(new Chromatogram[set.size()]));
    result.m_IsDummy = m_IsDummy;

    return result;
  }

  /**
   * Prunes chromatograms from this set that are newer than the chromatogram
   * that the set was retrieved for - unless this is explicitly allowed.
   *
   * @param c		the chromatogram the set was retrieved for
   * @return		the (potentially) pruned set
   */
  public CalibrationSet prune(Chromatogram c) {
    CalibrationSet	result;
    Vector<Integer>	allowed;
    int[]		indices;
    int			i;

    // remove newer chromatograms from set
    allowed = new Vector<Integer>();
    for (i = 0; i < size(); i++) {
      if (getChromatogram(i).getDateGeneration().compareTo(c.getDateGeneration()) < 0)
	allowed.add(i);
    }
    indices = new int[allowed.size()];
    for (i = 0; i < allowed.size(); i++)
      indices[i] = allowed.get(i);

    // create new set
    result = subset(indices);

    return result;
  }

  /**
   * Compares this object with the specified object for order.  Returns a
   * negative integer, zero, or a positive integer as this object is less
   * than, equal to, or greater than the specified object.
   * Note: only compares ID/size of sets and the headers of the stored chromatograms.
   *
   * @param   o the object to be compared.
   * @return  a negative integer, zero, or a positive integer as this object
   *		is less than, equal to, or greater than the specified object.
   * @throws ClassCastException if the specified object's type prevents it
   *         from being compared to this object.
   */
  public int compareTo(Object o) {
    int			result;
    CalibrationSet	set;
    int			i;

    if (o == null)
      return 1;

    if (!(o instanceof CalibrationSet))
      return -1;

    set = (CalibrationSet) o;

    // check ID
    result = getID().compareTo(set.getID());

    // check number of stored chromatograms
    if (result == 0)
      result = new Integer(size()).compareTo(new Integer(set.size()));

    // check chromatogram headers
    for (i = 0; i < size(); i++) {
      if (result == 0)
	result = getChromatogram(i).compareToHeader(set.getChromatogram(i));
      if (result != 0)
	break;
    }

    return result;
  }

  /**
   * Indicates whether some other object is "equal to" this one.
   *
   * @param obj		the reference object with which to compare.
   * @return		true if this object is the same as the obj argument;
   * 			false otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    return (compareTo(obj) == 0);
  }

  /**
   * Returns a string representation of the set.
   *
   * @return		the string representation
   */
  @Override
  public String toString() {
    StringBuilder	result;
    int			i;

    result = new StringBuilder();
    result.append("ID=" + getID() + ", size=" + size() + ", dummy=" + isDummy());
    for (i = 0; i < size(); i++)
      result.append(", " + (i+1) + "=" + getChromatogram(i));

    return result.toString();
  }
}
