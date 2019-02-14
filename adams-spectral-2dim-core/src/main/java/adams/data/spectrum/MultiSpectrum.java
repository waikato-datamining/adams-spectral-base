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
 * MultiSpectrum.java
 * Copyright (C) 2014-2019 University of Waikato, Hamilton, New Zealand
 */
package adams.data.spectrum;

import adams.data.Notes;
import adams.data.NotesHandler;
import adams.data.container.AbstractDataContainer;
import adams.data.container.DataContainer;
import adams.data.container.DataPoint;
import adams.data.container.DataPointComparator;
import adams.data.report.MutableReportHandler;
import adams.data.sampledata.SampleData;

/**
 * For storing multiple spectra.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class MultiSpectrum
  extends AbstractDataContainer<Spectrum>
  implements DataPoint, MutableReportHandler<SampleData>, NotesHandler {
  
  /** for serialization. */
  private static final long serialVersionUID = 5237779170231061484L;
  
  /** the report. */
  protected SampleData m_SampleData;
  
  /** the default comparator. */
  protected static DataPointComparator<Spectrum> m_Comparator;
  
  /** the parent. */
  protected DataContainer m_Parent;

  /** the notes for the chromatogram. */
  protected Notes m_Notes;

  /** a custom comparator. */
  protected AbstractSpectrumComparator m_CustomComparator;

  /**
   * Default constructor.
   */
  public MultiSpectrum() {
    super();
    
    m_SampleData = new SampleData();
    m_Notes      = new Notes();
    if (m_Comparator == null)
      m_Comparator = newComparator();
    m_CustomComparator = null;
  }
  
  /**
   * Returns a new instance of the default comparator.
   *
   * @return		the comparator instance
   */
  @Override
  public DataPointComparator<Spectrum> newComparator() {
    return new SpectrumComparator();
  }

  /**
   * Returns the comparator in use (custom one if defined, otherwise the default one).
   *
   * @return		the comparator in use
   */
  @Override
  public DataPointComparator<Spectrum> getComparator() {
    if (m_CustomComparator != null)
      return m_CustomComparator;
    else
      return m_Comparator;
  }

  /**
   * Sets the custom comparator to use.
   *
   * @param value	the comparator to use, null to unset
   */
  public void setCustomComparator(AbstractSpectrumComparator value) {
    m_CustomComparator = value;
  }

  /**
   * Returns the custom comparaor in use.
   *
   * @return		the custom comparator, null if none set
   */
  public AbstractSpectrumComparator getCustomComparator() {
    return m_CustomComparator;
  }

  /**
   * Sets the container this point belongs to.
   *
   * @param value	the container
   */
  @Override
  public void setParent(DataContainer value) {
    m_Parent = value;
  }

  /**
   * Returns the container this point belongs to.
   *
   * @return		the container, can be null
   */
  @Override
  public DataContainer getParent() {
    return m_Parent;
  }

  /**
   * Returns whether the point belongs to a container.
   *
   * @return		true if the point belongs to a container
   */
  @Override
  public boolean hasParent() {
    return (m_Parent !=null);
  }

  /**
   * Obtains the stored variables from the other data point.
   *
   * @param other	the data point to get the values from
   */
  @Override
  public void assign(DataPoint other) {
    if (other instanceof MultiSpectrum)
      assign((DataContainer<Spectrum>) other);
  }

  /**
   * Obtains the stored variables from the other data point, but not the
   * actual data points.
   *
   * @param other	the data point to get the values from
   */
  @Override
  public void assign(DataContainer<Spectrum> other) {
    MultiSpectrum	multi;
    
    super.assign(other);
    
    if (other instanceof MultiSpectrum) {
      multi = (MultiSpectrum) other;
      if (multi.hasReport())
	setReport((SampleData) multi.getReport().getClone());
      m_Notes = new Notes();
      m_Notes.mergeWith(multi.getNotes());
    }
  }
  
  /**
   * Returns a new instance of a DataContainer point.
   *
   * @return		the new DataContainer point
   */
  @Override
  public Spectrum newPoint() {
    Spectrum	result;
    
    result = new Spectrum();
    result.setParent(this);
    
    return result;
  }

  /**
   * Returns whether sample data is present.
   *
   * @return		true if sample data present
   */
  public boolean hasReport() {
    return (m_SampleData != null);
  }

  /**
   * Set sample data. Also sets the database ID and sample ID in the sample
   * data (without "'").
   *
   * @param value		the sample data
   */
  public void setReport(SampleData value){
    m_SampleData = value;
    m_SampleData.setID(getID().replace("'", ""));
  }

  /**
   * get sample data.
   *
   * @return		the sample data
   */
  public SampleData getReport(){
    return m_SampleData;
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
   * Returns a string representation of the sequence.
   *
   * @return		the string representation
   */
  @Override
  public String toString() {
    return "id=" + getID() + ", #spectra=" + size();
  }
}
