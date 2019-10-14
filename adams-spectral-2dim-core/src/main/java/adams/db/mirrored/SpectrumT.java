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
 * SpectrumT.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.db.mirrored;

import adams.core.Constants;
import adams.core.UniqueIDs;
import adams.core.Utils;
import adams.core.logging.LoggingHelper;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.db.AbstractDatabaseConnection;
import adams.db.AbstractSpectralDbBackend;
import adams.db.SampleDataIntf;
import adams.db.SpectrumIDConditions;
import adams.db.SpectrumIntf;
import adams.db.wrapper.AbstractWrapper;
import adams.db.wrapper.WrapperManager;

import java.util.List;

/**
 * Allows mirroring to another database.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumT
  extends AbstractWrapper<SpectrumIntf>
  implements SpectrumIntf {

  private static final long serialVersionUID = -979406390402187979L;

  /** the table manager. */
  protected static WrapperManager<SpectrumT> m_TableManager;

  /** the non-mirrored backend. */
  protected SpectrumIntf m_DB;

  /** object for blocking polling/removal of fully processed. */
  protected final Long m_Updating;

  /**
   * Initializes the mirroring.
   *
   * @param dbcon	the database connection
   * @param wrapped	the mirror
   */
  protected SpectrumT(AbstractDatabaseConnection dbcon, SpectrumIntf wrapped) {
    super(dbcon, wrapped);
    m_DB       = ((SpectralDbBackend) AbstractSpectralDbBackend.getSingleton()).getNonMirroredBackend().getSpectrum(dbcon);
    m_Updating = UniqueIDs.nextLong();
  }

  /**
   * Returns the corresponding SampleData handler.
   *
   * @return		the corresponding handler
   */
  @Override
  public SampleDataIntf getSampleDataHandler() {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName());
    return SpectralDbBackend.getSingleton().getSampleData(getDatabaseConnection());
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
   * Checks whether the container exists in the database.
   *
   * @param id		the database ID of the data container
   * @return		true if the container exists
   */
  @Override
  public boolean exists(int id) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": id=" + id);
    return m_DB.exists(id);
  }

  /**
   * Checks whether the container exists in the database.
   * Uses {@link SampleData#DEFAULT_FORMAT} as format.
   *
   * @param id		the ID of the data container
   * @return		true if the container exists
   * @see		#exists(String, String)
   */
  @Override
  public boolean exists(String id) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": id=" + id);
    return m_DB.exists(id);
  }

  /**
   * Checks whether the container exists in the database.
   *
   * @param id		the ID of the data container
   * @return		true if the container exists
   */
  @Override
  public boolean exists(String id, String format) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": id=" + id + ", format=" + format);
    return m_DB.exists(id, format);
  }

  /**
   * Load a spectrum with given database ID. Get from cache if available
   *
   * @param auto_id	the database ID
   * @return 		Spectrum, or null if not found
   */
  @Override
  public Spectrum load(int auto_id) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": auto_id=" + auto_id);
    return m_DB.load(auto_id);
  }

  /**
   * Load a spectrum with given ID. Get from cache if available
   * Uses {@link SampleData#DEFAULT_FORMAT} as format.
   *
   * @param id		the ID
   * @return 		Spectrum, or null if not found
   * @see		#load(String, String)
   */
  @Override
  public Spectrum load(String id) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": id=" + id);
    return m_DB.load(id);
  }

  /**
   * Load a spectrum with given sample ID and type. Get from cache if available
   *
   * @param sample_id	the sample ID
   * @param format	the format
   * @return 		Spectrum, or null if not found
   */
  @Override
  public Spectrum load(String sample_id, String format) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": sample_id" + sample_id + ", format=" + format);
    return m_DB.load(sample_id, format);
  }

  /**
   * Load a data container with given auto_id, without passing it through
   * the global filter.
   *
   * @param auto_id	the databae ID
   * @return 		the data container, or null if not found
   */
  @Override
  public Spectrum loadRaw(int auto_id) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": auto_id=" + auto_id);
    return m_DB.loadRaw(auto_id);
  }

  /**
   * Load a spectrum with given sample ID and type, without filtering through
   * the global container filter.
   *
   * @param sample_id	the sample ID
   * @param format	the format
   * @return 		Spectrum, or null if not found
   */
  @Override
  public Spectrum loadRaw(String sample_id, String format) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": sample_id=" + sample_id + ", format=" + format);
    return m_DB.loadRaw(sample_id, format);
  }

  /**
   * Load a spectrum from DB with given auto_id.
   *
   * @param auto_id	the databae ID
   * @param rlike 	regex for chrom name
   * @param raw		whether to return the raw spectrum or filter it
   * 			through the global container filter
   * @return 		Spectrum, or null if not found
   */
  @Override
  public Spectrum loadFromDB(int auto_id, String rlike, boolean raw) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": auto_id=" + auto_id + ", rlike=" + rlike + ", raw=" + raw);
    return m_DB.loadFromDB(auto_id, rlike, raw);
  }

  /**
   * Load a spectrum from DB with given sample_id and format.
   *
   * @param sample_id	the sample ID
   * @param format 	the format
   * @param raw		whether to return the raw spectrum or filter it
   * 			through the global container filter
   * @return 		Spectrum, or null if not found
   */
  @Override
  public Spectrum loadFromDB(String sample_id, String format, boolean raw) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": sample_id=" + sample_id + ", format=" + format + ", raw=" + raw);
    return m_DB.loadFromDB(sample_id, format, raw);
  }

  /**
   * Returns the database ID for given spectrum ID. Get from cache if available
   * Uses {@link SampleData#DEFAULT_FORMAT} as format.
   *
   * @param id		the ID
   * @return 		Spectrum, or null if not found
   * @see		#load(String, String)
   */
  @Override
  public int getDatabaseID(String id) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": id=" + id);
    return m_DB.getDatabaseID(id);
  }

  /**
   * Returns the database ID for given sample ID and type. Get from cache if available
   *
   * @param sample_id	the sample ID
   * @param format	the format
   * @return 		the database ID, {@link Constants#NO_ID}
   */
  @Override
  public int getDatabaseID(String sample_id, String format) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": sample_id=" + sample_id + ", format=" + format);
    return m_DB.getDatabaseID(sample_id, format);
  }

  /**
   * returns all the specified fields in the database, separated by TABs.
   *
   * @param fields	the field names
   * @param cond	the conditions for the retrieval
   * @return		list of tab-separated values
   */
  @Override
  public List<String> getValues(String[] fields, SpectrumIDConditions cond) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": fields=" + Utils.arrayToString(fields) + ", cond=" + cond);
    return m_DB.getValues(fields, cond);
  }

  /**
   * returns all the specified fields in the database, separated by TABs.
   *
   * @param fields	the field names
   * @param where	the where clause, can be null
   * @param cond	the conditions for the retrieval
   * @return		list of tab-separated values
   */
  @Override
  public List<String> getValues(String[] fields, String where, SpectrumIDConditions cond) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": fields=" + Utils.arrayToString(fields) + ", where=" + where + ", cond=" + cond);
    return m_DB.getValues(fields, where, cond);
  }

  /**
   * returns all the specified fields in the database, separated by TABs.
   *
   * @param fields	the field names
   * @param tables 	the involved tables
   * @param where	the where clause, can be null
   * @param cond	the conditions for the retrieval
   * @return		list of tab-separated values
   */
  @Override
  public List<String> getValues(String[] fields, String tables, String where, SpectrumIDConditions cond) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": fields=" + Utils.arrayToString(fields) + ", tables=" + tables + ", where=" + where + ", cond=" + cond);
    return m_DB.getValues(fields, tables, where, cond);
  }

  /**
   * Adds a spectrum to the database. Returns the created auto-id, and sets in
   * Spectrum.
   *
   * @param sp  	spectrum Header
   * @return  	new ID, or null if fail
   */
  @Override
  public Integer add(Spectrum sp) {
    synchronized(m_Updating) {
      if (isLoggingEnabled())
        getLogger().info(LoggingHelper.getMethodName() + ": sp=" + sp);
      getWrapped().add((Spectrum) sp.getClone());
      return m_DB.add(sp);
    }
  }

  /**
   * Adds a spectrum to the database. Returns the created auto-id, and sets in
   * Spectrum.
   *
   * @param sp  	spectrum Header
   * @param storeWaveNo   whether to store the wave numbers as well
   * @return  	new ID, or null if fail
   */
  public Integer add(Spectrum sp, boolean storeWaveNo) {
    synchronized(m_Updating) {
      if (isLoggingEnabled())
        getLogger().info(LoggingHelper.getMethodName() + ": sp=" + sp + ", storeWaveNo=" + storeWaveNo);
      getWrapped().add((Spectrum) sp.getClone(), storeWaveNo);
      return m_DB.add(sp, storeWaveNo);
    }
  }

  /**
   * Removes the spectrum and its sample data.
   * Uses {@link SampleData#DEFAULT_FORMAT} as format.
   *
   * @param sample_id	the sample ID of the spectrum
   * @param keepReport	if true does not delete associated report
   * @return		true if no error
   * @see		SpectrumIntf#remove(String, String, boolean)
   */
  @Override
  public boolean remove(String sample_id, boolean keepReport) {
    synchronized(m_Updating) {
      if (isLoggingEnabled())
        getLogger().info(LoggingHelper.getMethodName() + ": sample_id=" + sample_id + ", keepReport=" + keepReport);
      getWrapped().remove(sample_id, keepReport);
      return m_DB.remove(sample_id, keepReport);
    }
  }

  /**
   * Removes the spectrum and its sample data.
   *
   * @param sample_id	the sample ID of the spectrum
   * @param format	the format of the spectrum (eg NIR)
   * @param keepReport	if true does not delete associated report
   * @return		true if no error
   */
  @Override
  public boolean remove(String sample_id, String format, boolean keepReport) {
    synchronized(m_Updating) {
      if (isLoggingEnabled())
        getLogger().info(LoggingHelper.getMethodName() + ": sample_id=" + sample_id + ", format=" + format + ", keepReport=" + keepReport);
      getWrapped().remove(sample_id, format, keepReport);
      return m_DB.remove(sample_id, format, keepReport);
    }
  }

  /**
   * Removes the spectrum and its sample data.
   *
   * @param id		the ID of the spectrum to remove from the database
   * @param keepReport	if true does not delete associated report
   * @return		true if no error
   */
  @Override
  public boolean remove(int id, boolean keepReport) {
    Spectrum 	sp;

    synchronized(m_Updating) {
      if (isLoggingEnabled())
        getLogger().info(LoggingHelper.getMethodName() + ": id=" + id + ", keepReport=" + keepReport);
      if (exists(id)) {
        sp = m_DB.load(id);
        if (sp != null)
	  getWrapped().remove(sp.getID(), sp.getFormat(), keepReport);
      }
      return m_DB.remove(id, keepReport);
    }
  }

  /**
   * Returns the singleton of the table.
   *
   * @param dbcon	the database connection to get the singleton for
   * @param mirror 	the mirror
   * @return		the singleton
   */
  public static synchronized SpectrumT getSingleton(AbstractDatabaseConnection dbcon, SpectrumIntf mirror) {
    if (m_TableManager == null)
      m_TableManager = new WrapperManager<>(TABLE_NAME, dbcon.getOwner());
    if (!m_TableManager.has(dbcon))
      m_TableManager.add(dbcon, new SpectrumT(dbcon, mirror));

    return m_TableManager.get(dbcon);
  }
}
