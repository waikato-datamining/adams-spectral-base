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
 * SpectrumIterator.java
 * Copyright (C) 2025 University of Waikato, Hamilton, New Zealand
 */

package adams.db;

import adams.core.logging.LoggingHelper;
import adams.data.spectrum.Spectrum;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;

/**
 * Iterator for Spectrum result sets.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumIterator
  implements Iterator<Spectrum>, AutoCloseable {

  /** the underlying spectrum DB handler. */
  protected SpectrumIntf m_Spectrum;

  /** the underlying sampledata DB handler. */
  protected SampleDataIntf m_SampleData;

  /** the underlying resultset. */
  protected ResultSet m_ResultSet;

  /** the next spectrum. */
  protected Spectrum m_Next;

  /** the connection to close. */
  protected Connection m_Connection;

  /** the number of rows in the resultset. */
  protected int m_Size;

  /**
   * Initializes the iterator.
   *
   * @param spectrum	the spectrum handler
   * @param sampleData	the sampledata handler
   * @param resultSet	the result set to iterate
   * @param connection 	the connection to close after finished reading, if any
   */
  public SpectrumIterator(SpectrumIntf spectrum, SampleDataIntf sampleData, ResultSet resultSet, Connection connection) {
    m_Spectrum   = spectrum;
    m_SampleData = sampleData;
    m_ResultSet  = resultSet;
    m_Connection = connection;
    m_Next       = null;
    try {
      if (m_ResultSet.last()) {
	m_Size = m_ResultSet.getRow();
	m_ResultSet.beforeFirst();
      }
    }
    catch (Exception e) {
      LoggingHelper.global().log(Level.SEVERE, "Failed to determine #rows!", e);
      m_Size = -1;
    }
  }

  /**
   * Returns the number of rows.
   *
   * @return		the rows
   */
  public int getSize() {
    return m_Size;
  }

  /**
   * Returns {@code true} if the iteration has more elements.
   * (In other words, returns {@code true} if {@link #next} would
   * return an element rather than throwing an exception.)
   *
   * @return {@code true} if the iteration has more elements
   */
  @Override
  public boolean hasNext() {
    if (m_Next == null) {
      if (m_ResultSet != null) {
	try {
	  m_Next = SpectrumUtils.resultsetToSpectrum(m_ResultSet, m_SampleData);
	  // no more data?
	  if (m_Next == null) {
	    SQLUtils.closeAll(m_ResultSet);
	    m_ResultSet = null;
	  }
	}
	catch (Exception e) {
	  // ignored
	}
      }
    }
    return (m_Next != null);
  }

  /**
   * Returns the next element in the iteration.
   *
   * @return the next element in the iteration
   * @throws NoSuchElementException if the iteration has no more elements
   */
  @Override
  public Spectrum next() {
    Spectrum	result;

    if (!hasNext())
      throw new NoSuchElementException();

    result = m_Next;
    m_Next = null;

    return result;
  }

  /**
   * Closes the results set and query.
   *
   * @throws Exception	does not happen
   */
  @Override
  public void close() throws Exception {
    if (m_ResultSet != null) {
      SQLUtils.closeAll(m_ResultSet);
      m_ResultSet = null;
    }
    if (m_Connection != null) {
      m_Connection.close();
      m_Connection = null;
    }
  }
}
