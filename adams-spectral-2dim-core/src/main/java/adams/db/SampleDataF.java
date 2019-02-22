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
 * SampleDataF.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.db;

import adams.data.report.AbstractField;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;

import java.util.List;

/**
 * Facade for sample data.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SampleDataF
  extends AbstractTableFacade
  implements SampleDataIntf {

  private static final long serialVersionUID = -6057642240735031240L;

  /** the facade manager. */
  protected static FacadeManager<SampleDataF> m_TableManager;

  /** the backend. */
  protected SampleDataIntf m_DB;

  /**
   * Constructor.
   *
   * @param dbcon     the database context to use
   * @param tableName the name of the table
   */
  public SampleDataF(AbstractDatabaseConnection dbcon, String tableName) {
    super(dbcon, tableName);

    m_DB = AbstractSpectralDbBackend.getSingleton().getSampleData(dbcon);
  }

  /**
   * Returns the corresponding Spectrum handler.
   *
   * @return		the corresponding handler
   */
  @Override
  public SpectrumIntf getSpectrumHandler() {
    return m_DB.getSpectrumHandler();
  }

  /**
   * Initialise table & sub-tables.
   *
   * @return success?
   */
  @Override
  public boolean init() {
    return m_DB.init();
  }

  /**
   * Returns all available fields.
   *
   * @return		the list of fields
   */
  @Override
  public List<Field> getFields() {
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
    return m_DB.remove(id);
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
    return m_DB.remove(id, field);
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
    return m_DB.store(id, report);
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
    return m_DB.store(id, report, removeExisting, merge, overwrite);
  }

  /**
   * Get params.
   *
   * @param id		sample ID of spectrum
   * @return		the hashtable
   */
  @Override
  public SampleData load(String id) {
    return m_DB.load(id);
  }

  /**
   * Return a list of IDs of spectra that match the defined
   * conditions. Since the alphanumeric IDs can be of numeric nature as well,
   * we're returning them surrounded with double quotes to avoid them being
   * interpreted as database IDs.
   *
   * @param cond	the conditions that the spectra must meet
   * @return		list of spectrum ids
   */
  @Override
  public List<String> getIDs(AbstractConditions cond) {
    return m_DB.getIDs(cond);
  }

  /**
   * Return a list of IDs of spectra that match the defined
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
    return m_DB.getOrphanedIDs(conditions);
  }

  /**
   * Returns all the various instruments.
   *
   * @return		the instruments
   */
  @Override
  public List<String> getInstruments() {
    return m_DB.getInstruments();
  }

  /**
   * Returns the singleton of the facade.
   *
   * @param dbcon	the database connection to get the singleton for
   * @return		the singleton
   */
  public static synchronized SampleDataF getSingleton(AbstractDatabaseConnection dbcon) {
    if (m_TableManager == null)
      m_TableManager = new FacadeManager<>(TABLE_NAME, dbcon.getOwner());
    if (!m_TableManager.has(dbcon))
      m_TableManager.add(dbcon, new SampleDataF(dbcon, TABLE_NAME));

    return m_TableManager.get(dbcon);
  }
}
