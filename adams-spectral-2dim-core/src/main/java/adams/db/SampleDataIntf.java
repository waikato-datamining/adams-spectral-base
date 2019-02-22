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
 * SampleDataIntf.java
 * Copyright (C) 2019 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.db;

import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;

import java.util.List;

/**
 * Interface for sample data reports table.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public interface SampleDataIntf
  extends FieldProvider<Field>, ReportProviderByID<SampleData>, InstrumentProvider {

  /** this table name. */
  public final static String TABLE_NAME = "sampledata";

  /**
   * Returns the corresponding Spectrum handler.
   *
   * @return		the corresponding handler
   */
  public SpectrumIntf getSpectrumHandler();

  /**
   * Initialise table & sub-tables.
   *
   * @return success?
   */
  public boolean init();

  /**
   * Returns all available fields.
   *
   * @param dtype	the type to limit the search to, use "null" for all
   * @return		the list of fields
   */
  public List<Field> getFields(DataType dtype);

  /**
   * Checks whether the report exists in the database.
   *
   * @param id	the ID of parent data container
   * @return		true if the report exists
   */
  public boolean exists(String id);

  /**
   * Get params.
   *
   * @param id		sample ID of spectrum
   * @return		the hashtable
   */
  public SampleData load(String id);

  /**
   * Return a list (Vector) of IDs of spectra that match the defined
   * conditions. Since the alphanumeric IDs can be of numeric nature as well,
   * we're returning them surrounded with double quotes to avoid them being
   * interpreted as database IDs.
   *
   * @param cond	the conditions that the spectra must meet
   * @return		list of spectrum ids
   */
  public List<String> getIDs(AbstractConditions cond);

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
  public List<String> getIDs(String[] columns, AbstractConditions cond);

  /**
   * Return a list of database IDs of data containers that match the defined
   * conditions.
   *
   * @param conditions	the conditions that the conatiners must meet
   * @return		list of database IDs
   */
  public List<Integer> getDBIDs(AbstractConditions conditions);

  /**
   * Returns a list of sample IDs of of sample data without associated spectra.
   *
   * @param conditions	the conditions that the sampledata must meet
   * @return		list of sample IDs
   */
  public List<String> getOrphanedIDs(OrphanedSampleDataConditions conditions);

  /**
   * Returns all the various instruments.
   *
   * @return		the instruments
   */
  public List<String> getInstruments();
}
