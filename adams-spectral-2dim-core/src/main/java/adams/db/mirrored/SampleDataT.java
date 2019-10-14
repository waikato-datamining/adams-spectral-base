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
 * SampleDataT.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.db.mirrored;

import adams.core.UniqueIDs;
import adams.core.Utils;
import adams.core.logging.LoggingHelper;
import adams.data.report.AbstractField;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.db.AbstractConditions;
import adams.db.AbstractDatabaseConnection;
import adams.db.AbstractSpectralDbBackend;
import adams.db.OrphanedSampleDataConditions;
import adams.db.SampleDataIntf;
import adams.db.SpectrumIntf;
import adams.db.wrapper.AbstractWrapper;
import adams.db.wrapper.WrapperManager;

import java.util.List;

/**
 * Allows mirroring to another database.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SampleDataT
  extends AbstractWrapper<SampleDataIntf>
  implements SampleDataIntf {

  private static final long serialVersionUID = -4601570259375411398L;

  /** the table manager. */
  protected static WrapperManager<SampleDataT> m_TableManager;

  /** the non-mirrored backend. */
  protected SampleDataIntf m_DB;

  /** object for blocking polling/removal of fully processed. */
  protected final Long m_Updating;

  /**
   * Initializes the mirror.
   *
   * @param dbcon	the database connection
   * @param wrapped	the mirror
   */
  protected SampleDataT(AbstractDatabaseConnection dbcon, SampleDataIntf wrapped) {
    super(dbcon, wrapped);
    m_DB       = ((SpectralDbBackend) AbstractSpectralDbBackend.getSingleton()).getNonMirroredBackend().getSampleData(dbcon);
    m_Updating = UniqueIDs.nextLong();
  }

  /**
   * Returns the corresponding Spectrum handler.
   *
   * @return		the corresponding handler
   */
  @Override
  public SpectrumIntf getSpectrumHandler() {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName());
    return SpectralDbBackend.getSingleton().getSpectrum(getDatabaseConnection());
  }

  /**
   * Initialise table & sub-tables.
   *
   * @return success?
   */
  @Override
  public boolean init() {
    synchronized(m_Updating) {
      if (isLoggingEnabled())
        getLogger().info(LoggingHelper.getMethodName());
      getWrapped().init();
      return m_DB.init();
    }
  }

  /**
   * Returns all available fields.
   *
   * @return		the list of fields
   */
  @Override
  public List<Field> getFields() {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName());
    return m_DB.getFields();
  }

  /**
   * Returns all available fields.
   *
   * @param dtype	the type to limit the search to, use "null" for all
   * @return		the list of fields
   */
  @Override
  public List<Field> getFields(DataType dtype) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": dtype=" + dtype);
    return m_DB.getFields(dtype);
  }

  /**
   * Checks whether the report exists in the database.
   *
   * @param id	the ID of parent data container
   * @return		true if the report exists
   */
  @Override
  public boolean exists(String id) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": id=" + id);
    return m_DB.exists(id);
  }

  /**
   * Removes the report from the database.
   *
   * @param id	the ID of the parent data container
   * @return		true if successfully removed
   */
  @Override
  public boolean remove(String id) {
    synchronized(m_Updating) {
      if (isLoggingEnabled())
        getLogger().info(LoggingHelper.getMethodName() + ": id=" + id);
      getWrapped().remove(id);
      return m_DB.remove(id);
    }
  }

  /**
   * Removes the report field from the database.
   *
   * @param id		the ID of the parent data container
   * @param field	the field to remove
   * @return		true if successfully removed
   */
  @Override
  public boolean remove(String id, AbstractField field) {
    synchronized(m_Updating) {
      if (isLoggingEnabled())
        getLogger().info(LoggingHelper.getMethodName() + ": id=" + id + ", field=" + field);
      getWrapped().remove(id, field);
      return m_DB.remove(id, field);
    }
  }

  /**
   * Stores the report. Removes a previously existing report.
   *
   * @param id	        the id of the report
   * @param report	the report
   * @return		true if successfully inserted/updated
   */
  @Override
  public boolean store(String id, SampleData report) {
    synchronized(m_Updating) {
      if (isLoggingEnabled())
        getLogger().info(LoggingHelper.getMethodName() + ": id=" + id + ", report");
      getWrapped().store(id, (SampleData) report.getClone());
      return m_DB.store(id, report);
    }
  }

  /**
   * Stores the report. Either updates or inserts the fields.
   *
   * @param id		        the id of the report
   * @param report		the report
   * @param removeExisting	whether to remove existing an already existing
   * 				report before storing it (has precedence over
   * 				"merge")
   * @param merge		whether to merge the existing and the current
   * @param overwrite		fields to overwrite if in "merge" mode
   * @return			true if successfully inserted/updated
   */
  @Override
  public boolean store(String id, SampleData report, boolean removeExisting, boolean merge, Field[] overwrite) {
    synchronized(m_Updating) {
      getWrapped().store(id, (SampleData) report.getClone(), removeExisting, merge, overwrite);
      return m_DB.store(id, report, removeExisting, merge, overwrite);
    }
  }

  /**
   * Get params.
   *
   * @param id		sample ID of spectrum
   * @return		the hashtable
   */
  @Override
  public SampleData load(String id) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": id=" + id);
    return m_DB.load(id);
  }

  /**
   * Return a list (Vector) of IDs of spectra that match the defined
   * conditions. Since the alphanumeric IDs can be of numeric nature as well,
   * we're returning them surrounded with double quotes to avoid them being
   * interpreted as database IDs.
   *
   * @param cond	the conditions that the spectra must meet
   * @return		list of spectrum ids
   */
  @Override
  public List<String> getIDs(AbstractConditions cond) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": cond=" + cond);
    return m_DB.getIDs(cond);
  }

  /**
   * Return a list (Vector) of IDs of spectra that match the defined
   * conditions. Since the alphanumeric IDs can be of numeric nature as well,
   * we're returning them surrounded with double quotes to avoid them being
   * interpreted as database IDs. If several columns are specified, then the
   * result contains them tab-separated.
   *
   * @param columns	the columns to retrieve ("sp." for spectrum table,
   * 			"sd." for sampledata table)
   * @param cond	the conditions that the spectra must meet
   * @return		list of spectrum ids
   */
  @Override
  public List<String> getIDs(String[] columns, AbstractConditions cond) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": columns=" + Utils.arrayToString(columns) + ", cond=" + cond);
    return m_DB.getIDs(columns, cond);
  }

  /**
   * Return a list of database IDs of data containers that match the defined
   * conditions.
   *
   * @param conditions	the conditions that the conatiners must meet
   * @return		list of database IDs
   */
  @Override
  public List<Integer> getDBIDs(AbstractConditions conditions) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": conditions=" + conditions);
    return m_DB.getDBIDs(conditions);
  }

  /**
   * Returns a list of sample IDs of of sample data without associated spectra.
   *
   * @param conditions	the conditions that the sampledata must meet
   * @return		list of sample IDs
   */
  @Override
  public List<String> getOrphanedIDs(OrphanedSampleDataConditions conditions) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": conditions=" + conditions);
    return m_DB.getOrphanedIDs(conditions);
  }

  /**
   * Returns all the various instruments.
   *
   * @return		the instruments
   */
  @Override
  public List<String> getInstruments() {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName());
    return m_DB.getInstruments();
  }

  /**
   * Returns the singleton of the table.
   *
   * @param dbcon	the database connection to get the singleton for
   * @param mirror 	the mirror
   * @return		the singleton
   */
  public static synchronized SampleDataT getSingleton(AbstractDatabaseConnection dbcon, SampleDataIntf mirror) {
    if (m_TableManager == null)
      m_TableManager = new WrapperManager<>(TABLE_NAME, dbcon.getOwner());
    if (!m_TableManager.has(dbcon))
      m_TableManager.add(dbcon, new SampleDataT(dbcon, mirror));

    return m_TableManager.get(dbcon);
  }
}
