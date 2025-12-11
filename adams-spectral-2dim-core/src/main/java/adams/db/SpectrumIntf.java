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
 * SpectrumIntf.java
 * Copyright (C) 2019-2025 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.db;

import adams.core.Constants;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;

import java.util.List;

/**
 * Interface for spectrum tables.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public interface SpectrumIntf
  extends TableInterface, DataProvider<Spectrum> {

  /** the table names. */
  public final static String TABLE_NAME = "spectrum";

  public static final String MAX_NUM_SPECTRUMS_CACHED = "maxNumSpectrumsCached";

  /**
   * Returns the corresponding SampleData handler.
   *
   * @return		the corresponding handler
   */
  public SampleDataIntf getSampleDataHandler();

  /**
   * Initialise table & sub-tables.
   *
   * @return success?
   */
  public boolean init();

  /**
   * Checks whether the container exists in the database.
   *
   * @param id		the database ID of the data container
   * @return		true if the container exists
   */
  public boolean exists(int id);

  /**
   * Checks whether the container exists in the database.
   * Uses {@link SampleData#DEFAULT_FORMAT} as format.
   *
   * @param id		the ID of the data container
   * @return		true if the container exists
   * @see		#exists(String, String)
   */
  public boolean exists(String id);

  /**
   * Checks whether the container exists in the database.
   *
   * @param id		the ID of the data container
   * @return		true if the container exists
   */
  public boolean exists(String id, String format);

  /**
   * Load a spectrum with given database ID. Get from cache if available
   *
   * @param auto_id	the database ID
   * @return 		Spectrum, or null if not found
   */
  public Spectrum load(int auto_id);

  /**
   * Load a spectrum with given ID. Get from cache if available
   * Uses {@link SampleData#DEFAULT_FORMAT} as format.
   *
   * @param id		the ID
   * @return 		Spectrum, or null if not found
   * @see		#load(String, String)
   */
  public Spectrum load(String id);

  /**
   * Load a spectrum with given sample ID and type. Get from cache if available
   *
   * @param sample_id	the sample ID
   * @param format	the format
   * @return 		Spectrum, or null if not found
   */
  public Spectrum load(String sample_id, String format);

  /**
   * Returns the database ID for given sample ID and type.
   *
   * @param sample_id	the sample ID
   * @param format	the format
   * @return 		the database ID, {@link Constants#NO_ID}
   */
  public int getDatabaseID(String sample_id, String format);

  /**
   * returns all the specified fields in the database, separated by TABs.
   *
   * @param fields	the field names
   * @param cond	the conditions for the retrieval
   * @return		list of tab-separated values
   */
  public List<String> getValues(String[] fields, SpectrumIDConditions cond);

  /**
   * returns all the specified fields in the database, separated by TABs.
   *
   * @param fields	the field names
   * @param where	the where clause, can be null
   * @param cond	the conditions for the retrieval
   * @return		list of tab-separated values
   */
  public List<String> getValues(String[] fields, String where, SpectrumIDConditions cond);

  /**
   * returns all the specified fields in the database, separated by TABs.
   *
   * @param fields	the field names
   * @param tables 	the involved tables
   * @param where	the where clause, can be null
   * @param cond	the conditions for the retrieval
   * @return		list of tab-separated values
   */
  public List<String> getValues(String[] fields, String tables, String where, SpectrumIDConditions cond);

  /**
   * Adds a spectrum to the database. Returns the created auto-id, and sets in
   * Spectrum. Wave numbers get stored.
   *
   * @param sp  	spectrum Header
   * @return  	new ID, or null if fail
   */
  public Integer add(Spectrum sp);

  /**
   * Adds a spectrum to the database. Returns the created auto-id, and sets in
   * Spectrum.
   *
   * @param sp  	spectrum Header
   * @param storeWaveNo   whether to store the wave numbers as well
   * @return  	new ID, or null if fail
   */
  public Integer add(Spectrum sp, boolean storeWaveNo);

  /**
   * Stores the spectra in the database.
   *
   * @param sp  	the spectra to add
   * @param storeWaveNo	whether to store the wave numbers as well
   * @param batchSize   the maximum number of records in one batch
   * @param autoCommit  whether to use auto-commit or not (turning off may impact other transactions!)
   * @param newConnection	uses a separate database connection just for this connection (then no auto-commit doesn't affect the rest)
   * @return 		true if successfully inserted/updated
   */
  public boolean bulkAdd(Spectrum[] sp, boolean storeWaveNo, int batchSize, boolean autoCommit, boolean newConnection);

  /**
   * Interrupts a currently running bulk store, if possible.
   */
  public void stopBulkAdd();

  /**
   * Removes the spectrum and its sample data.
   * Uses {@link SampleData#DEFAULT_FORMAT} as format.
   *
   * @param sample_id	the sample ID of the spectrum
   * @param keepReport	if true does not delete associated report
   * @return		true if no error
   * @see		SpectrumIntf#remove(String, String, boolean)
   */
  public boolean remove(String sample_id, boolean keepReport);

  /**
   * Removes the spectrum and its sample data.
   *
   * @param sample_id	the sample ID of the spectrum
   * @param format	the format of the spectrum (eg NIR)
   * @param keepReport	if true does not delete associated report
   * @return		true if no error
   */
  public boolean remove(String sample_id, String format, boolean keepReport);

  /**
   * Removes the spectrum and its sample data.
   *
   * @param id		the ID of the spectrum to remove from the database
   * @param keepReport	if true does not delete associated report
   * @return		true if no error
   */
  public boolean remove(int id, boolean keepReport);

  /**
   * Returns an iterator over the spectra that were identified by the conditions.
   *
   * @param conditions		the conditions to use
   * @param newConnection 	whether to use a separate connection
   * @return			the iterator, null if failed to instantiate
   */
  public SpectrumIterator iterate(AbstractSpectrumConditions conditions, boolean newConnection);
}
