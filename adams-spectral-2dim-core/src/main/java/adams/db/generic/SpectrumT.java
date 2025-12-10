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
 * Copyright (C) 2008-2025 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.db.generic;

import adams.core.Constants;
import adams.core.Utils;
import adams.core.base.BaseDouble;
import adams.core.logging.LoggingHelper;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.db.AbstractDatabaseConnection;
import adams.db.AbstractIndexedTable;
import adams.db.AbstractSpectrumConditions;
import adams.db.ColumnMapping;
import adams.db.SQLUtils;
import adams.db.SampleDataF;
import adams.db.SpectrumConditionsMulti;
import adams.db.SpectrumConditionsSingle;
import adams.db.SpectrumIDConditions;
import adams.db.SpectrumIntf;
import adams.db.SpectrumIterator;
import adams.db.SpectrumUtils;
import adams.db.TableManager;
import adams.db.indices.Index;
import adams.db.indices.IndexColumn;
import adams.db.indices.Indices;
import adams.db.types.AutoIncrementType;
import adams.db.types.ColumnType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Manages Spectrum tables. Spectrums can be cached, see the
 * <code>Performance</code> class.
 *
 * @author dale
 */
public abstract class SpectrumT
  extends AbstractIndexedTable
  implements SpectrumIntf {

  /** for serialization. */
  private static final long serialVersionUID = 8400767916698176690L;

  /** the table manager. */
  protected static TableManager<SpectrumT> m_TableManager;

  /** whether to stop the bulk add. */
  protected boolean m_BulkAddStopped;

  /**
   * Constructor - initalise with database connection.
   *
   * @param dbcon	the database context this table is used in
   */
  protected SpectrumT(AbstractDatabaseConnection dbcon) {
    super(dbcon, TABLE_NAME);
  }

  /**
   * Initialise table & sub-tables.
   *
   * @return success?
   */
  @Override
  public synchronized boolean init(){
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName());

    if (!tableExists()) {
      if (!super.init())
	return false;
    }
    else {
      if (!columnsMatch(getColumnMapping(), true, true))
        return false;
    }

    return getSampleDataHandler().init();
  }

  /**
   * Checks whether the container exists in the database.
   *
   * @param id		the database ID of the data container
   * @return		true if the container exists
   */
  public boolean exists(int id) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": id=" + id);
    return isThere("AUTO_ID = " + id);
  }

  /**
   * Checks whether the container exists in the database.
   * Uses {@link SampleData#DEFAULT_FORMAT} as format.
   *
   * @param id		the ID of the data container
   * @return		true if the container exists
   * @see		#exists(String, String)
   */
  public boolean exists(String id) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": id=" + id);
    return exists(id, SampleData.DEFAULT_FORMAT);
  }

  /**
   * Checks whether the container exists in the database.
   *
   * @param id		the ID of the data container
   * @return		true if the container exists
   */
  public boolean exists(String id, String format) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": id=" + id + ", format=" + format);
    return isThere("SAMPLEID = " + SQLUtils.backquote(id) + " AND FORMAT = " + SQLUtils.backquote(format));
  }

  /**
   * Load a spectrum with given database ID. Get from cache if available
   *
   * @param auto_id	the database ID
   * @return 		Spectrum, or null if not found
   */
  public synchronized Spectrum load(int auto_id){
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": auto_id=" + auto_id);
    return loadFromDB(auto_id, "", true);
  }

  /**
   * Load a spectrum with given ID. Get from cache if available
   * Uses {@link SampleData#DEFAULT_FORMAT} as format.
   *
   * @param id		the ID
   * @return 		Spectrum, or null if not found
   * @see		#load(String, String)
   */
  public synchronized Spectrum load(String id){
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": id=" + id);
    return load(id, SampleData.DEFAULT_FORMAT);
  }

  /**
   * Load a spectrum with given sample ID and type. Get from cache if available
   *
   * @param sample_id	the sample ID
   * @param format	the format
   * @return 		Spectrum, or null if not found
   */
  public synchronized Spectrum load(String sample_id, String format){
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": sample_id" + sample_id + ", format=" + format);
    return loadFromDB(sample_id, format, true);
  }

  /**
   * Load a data container with given auto_id, without passing it through
   * the global filter.
   *
   * @param auto_id	the database ID
   * @return 		the data container, or null if not found
   */
  public Spectrum loadRaw(int auto_id) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": auto_id=" + auto_id);
    return loadFromDB(auto_id, "", true);
  }

  /**
   * Load a spectrum with given sample ID and type, without filtering through
   * the global container filter.
   *
   * @param sample_id	the sample ID
   * @param format	the format
   * @return 		Spectrum, or null if not found
   */
  public Spectrum loadRaw(String sample_id, String format) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": sample_id=" + sample_id + ", format=" + format);
    return loadFromDB(sample_id, format, true);
  }

  /**
   * Load a spectrum from DB with given auto_id.
   *
   * @param auto_id	the database ID
   * @param rlike 	regex for spectrum ID
   * @param raw		whether to return the raw spectrum or filter it
   * 			through the global container filter
   * @return 		Spectrum, or null if not found
   */
  public Spectrum loadFromDB(int auto_id, String rlike, boolean raw) {
    Spectrum	result;
    ResultSet 	rs;

    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": auto_id=" + auto_id + ", rlike=" + rlike + ", raw=" + raw);

    result = null;
    rs     = null;
    try {
      if (rlike.isEmpty())
	rs = select("*", "AUTO_ID=" + auto_id);
      else
	rs = select("*", "AUTO_ID=" + auto_id + " AND " + m_Queries.regexp("SAMPLEID", rlike));

      result = SpectrumUtils.resultsetToSpectrum(rs, getSampleDataHandler());
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to process DB ID " + auto_id, e);
    }
    finally{
      SQLUtils.closeAll(rs);
    }

    return result;
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
  public Spectrum loadFromDB(String sample_id, String format, boolean raw) {
    Spectrum	result;
    ResultSet 	rs;

    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": sample_id=" + sample_id + ", format=" + format + ", raw=" + raw);

    result = null;
    rs     = null;
    try {
      rs     = select("*", "SAMPLEID = " + SQLUtils.backquote(sample_id) + " AND FORMAT = " + SQLUtils.backquote(format));
      result = SpectrumUtils.resultsetToSpectrum(rs, getSampleDataHandler());
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to process Sample ID " + sample_id, e);
    }
    finally{
      SQLUtils.closeAll(rs);
    }

    return result;
  }

  /**
   * Returns the database ID for given spectrum ID. Get from cache if available
   * Uses {@link SampleData#DEFAULT_FORMAT} as format.
   *
   * @param id		the ID
   * @return 		Spectrum, or null if not found
   * @see		#load(String, String)
   */
  public synchronized int getDatabaseID(String id){
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": id=" + id);
    return getDatabaseID(id, SampleData.DEFAULT_FORMAT);
  }

  /**
   * Returns the database ID for given sample ID and type. Get from cache if available
   *
   * @param sample_id	the sample ID
   * @param format	the format
   * @return 		the database ID, {@link Constants#NO_ID}
   */
  public synchronized int getDatabaseID(String sample_id, String format){
    int         result;
    ResultSet   rs;

    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": sample_id=" + sample_id + ", format=" + format);

    result = Constants.NO_ID;

    rs = null;
    try {
      rs = select("AUTO_ID", "SAMPLEID = " + SQLUtils.backquote(sample_id) + " AND FORMAT = " + SQLUtils.backquote(format));
      if (rs.next())
        result = rs.getInt(1);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to determine databse ID: " + sample_id + "/" + format, e);
    }
    finally {
      SQLUtils.closeAll(rs);
    }

    return result;
  }

  /**
   * returns all the specified fields in the database, separated by TABs.
   *
   * @param fields	the field names
   * @param cond	the conditions for the retrieval
   * @return		list of tab-separated values
   */
  public List<String> getValues(String[] fields, SpectrumIDConditions cond) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": fields=" + Utils.arrayToString(fields) + ", cond=" + cond);
    return getValues(fields, null, cond);
  }

  /**
   * returns all the specified fields in the database, separated by TABs.
   *
   * @param fields	the field names
   * @param where	the where clause, can be null
   * @param cond	the conditions for the retrieval
   * @return		list of tab-separated values
   */
  public List<String> getValues(String[] fields, String where, SpectrumIDConditions cond) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": fields=" + Utils.arrayToString(fields) + ", where=" + where + ", cond=" + cond);
    return getValues(fields, getTableName(), where, cond);
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
  public List<String> getValues(String[] fields, String tables, String where, SpectrumIDConditions cond) {
    ResultSet 		rs;
    List<String>	result;
    int			i;
    StringBuilder	line;
    List<String>	whereParts;
    boolean		hasSampleID;
    boolean		hasSampleType;
    boolean		hasFormat;

    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": fields=" + Utils.arrayToString(fields) + ", tables=" + tables + ", where=" + where + ", cond=" + cond);

    result = new ArrayList<>();
    rs     = null;
    whereParts = new ArrayList<>();
    if ((where != null) && !where.isEmpty())
      whereParts.add(where);

    hasSampleID   = !cond.getSampleIDRegExp().isEmpty() && !cond.getSampleIDRegExp().isMatchAll();
    hasSampleType = !cond.getSampleTypeRegExp().isEmpty() && !cond.getSampleTypeRegExp().isMatchAll();
    hasFormat     = !cond.getFormat().isEmpty() && !cond.getFormat().isMatchAll();

    // sample name
    if (hasSampleID)
      whereParts.add(m_Queries.regexp("SAMPLEID", cond.getSampleIDRegExp()));

    // sample type
    if (hasSampleType)
      whereParts.add(m_Queries.regexp("SAMPLETYPE", cond.getSampleTypeRegExp()));

    // data format
    if (hasFormat)
      whereParts.add(m_Queries.regexp("FORMAT", cond.getFormat()));

    where = Utils.flatten(whereParts, " AND ");

    // sorting
    where += " ORDER BY AUTO_ID";

    // limit
    if (cond.getLimit() > -1)
      where += " " + m_Queries.limit(cond.getLimit());

    try {
      rs = select(Utils.flatten(fields, ", "), tables, where);
      if (rs == null)
	return result;

      while (rs.next()) {
	line = new StringBuilder();
	for (i = 0; i < fields.length; i++) {
	  if (i > 0)
	    line.append("\t");
	  line.append(rs.getObject(fields[i]));
	}
	result.add(line.toString());
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to get values", e);
    }
    finally{
      SQLUtils.closeAll(rs);
    }

    return result;
  }

  /**
   * Turns the spectrum points into a string to be stored in the database.
   * Format: wave1:ampltd1,wave2:ampltd2,...
   *
   * @param sp		the spectrum to convert
   * @param storeWaveNo 	whether to store the wave numbers as well
   * @return		the generated string
   */
  protected String pointsToString(Spectrum sp, boolean storeWaveNo) {
    StringBuilder	result;

    result = new StringBuilder();

    for (SpectrumPoint point: sp.toList()) {
      if (result.length() > 0)
	result.append(",");
      if (storeWaveNo) {
	result.append(point.getWaveNumber());
	result.append(":");
      }
      result.append(point.getAmplitude());
    }

    return result.toString();
  }

  /**
   * Generates the query string for adding a spectrum.
   *
   * @param sp		the spectrum to turn into query
   * @param storeWaveNo 	whether to store the wave numbers as well
   * @return		the generated query
   */
  protected StringBuilder addQuery(Spectrum sp, boolean storeWaveNo) {
    StringBuilder 	q;

    q = new StringBuilder();
    q.append("INSERT INTO ").append(getTableName()).append(" (SAMPLEID, SAMPLETYPE, FORMAT, POINTS) VALUES (");

    // sample ID
    q.append(SQLUtils.backquote(sp.getID()));

    // sample type
    q.append(",");
    q.append(SQLUtils.backquote(sp.getType()));

    // format
    q.append(",");
    q.append(SQLUtils.backquote(sp.getFormat()));

    // spectrum points (if points table disabled)
    q.append(",");
    q.append("'");
    q.append(pointsToString(sp, storeWaveNo));
    q.append("'");
    q.append(")");

    return q;
  }

  /**
   * Adds a spectrum to the database. Returns the created auto-id, and sets in
   * Spectrum. Wave numbers get stored.
   *
   * @param sp  	spectrum Header
   * @return  	new ID, or null if fail
   */
  public synchronized Integer add(Spectrum sp) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": sp=" + sp);
    return add(sp, true);
  }

  /**
   * Adds a spectrum to the database. Returns the created auto-id, and sets in
   * Spectrum.
   *
   * @param sp  	the spectrum to add
   * @param storeWaveNo	whether to store the wave numbers as well
   * @return  		new ID, or null if fail
   */
  public Integer add(Spectrum sp, boolean storeWaveNo) {
    Integer 		result;
    StringBuilder 	q;
    ResultSet 		rs;

    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": sp=" + sp + ", storeWaveNo=" + storeWaveNo);

    result = null;

    if (getDebug())
      getLogger().info("Entered add");

    q  = addQuery(sp, storeWaveNo);
    rs = null;
    try {
      if (getDebug())
	getLogger().info("Try insert keygen");
      rs = executeGeneratedKeys(q.toString());
      if (getDebug())
	getLogger().info("Try insert keygen ret");

      if (rs != null){
	if (rs.next()) {
	  result = rs.getInt(1);
	  sp.setDatabaseID(result);

	  // store report (never overwrites, just adds additional fields)
	  if (sp.hasReport())
	    getSampleDataHandler().store(sp.getID(), sp.getReport(), false, true, new Field[]{new Field(SampleData.INSERT_TIMESTAMP, DataType.STRING)});
        }
	else {
	  getLogger().severe("no gen keys");
	}
      }
      else {
	getLogger().severe("null genkeys");
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to insert: " + sp,  e);
      result = null;
    }
    finally {
      SQLUtils.closeAll(rs);
    }

    return result;
  }

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
  @Override
  public boolean bulkAdd(Spectrum[] sp, boolean storeWaveNo, int batchSize, boolean autoCommit, boolean newConnection) {
    boolean		result;
    PreparedStatement 	delete;
    PreparedStatement	insert;
    boolean		committed;
    int			i;
    int			n;
    boolean		useSameConnection;
    Connection 		connection;

    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName());

    connection        = null;
    m_BulkAddStopped  = false;
    useSameConnection = true;

    if (newConnection) {
      try {
	if (getDatabaseConnection().getUser().isEmpty())
	  connection = DriverManager.getConnection(getDatabaseConnection().getURL());
	else
	  connection = DriverManager.getConnection(getDatabaseConnection().getURL(), getDatabaseConnection().getUser(), getDatabaseConnection().getPassword().getValue());
	connection.setAutoCommit(autoCommit);
	useSameConnection = false;
      }
      catch(Exception e) {
	getLogger().warning("Failed to open separate connection to " + getDatabaseConnection().getURL() + ", re-using existing one.");
      }
    }

    if (!newConnection || useSameConnection) {
      if (!autoCommit) {
	try {
	  connection = getConnection(false);
	  connection.setAutoCommit(false);
	}
	catch (Exception e) {
	  getLogger().log(Level.WARNING, "Failed to turn off auto-commit!", e);
	}
      }
    }

    if (connection == null) {
      getLogger().warning("Falling back on default connection: " + getDatabaseConnection());
      connection = getConnection(false);
    }

    if (connection == null) {
      getLogger().severe("Cannot insert data, due to failure of obtaining connection from: " + getDatabaseConnection());
      return false;
    }

    try {
      delete = prepareStatement(connection, "DELETE FROM " + getTableName() + " WHERE SAMPLEID = ? AND FORMAT = ?", false);
      insert = prepareStatement(connection, "INSERT INTO " + getTableName() + "(SAMPLEID, SAMPLETYPE, FORMAT, POINTS) VALUES(?, ?, ?, ?)", false);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to prepare statements!", e);
      return false;
    }

    result    = true;
    n         = 0;
    committed = true;
    for (i = 0; i < sp.length; i++) {
      // stopped?
      if (m_BulkAddStopped)
	break;

      try {
	// delete
	delete.setString(1, sp[i].getID());
	delete.setString(2, sp[i].getFormat());
	delete.addBatch();

	// insert
	insert.setString(1, sp[i].getID());
	insert.setString(2, sp[i].getType());
	insert.setString(3, sp[i].getFormat());
	insert.setString(4, pointsToString(sp[i], storeWaveNo));
	insert.addBatch();

	n++;
	committed = false;
	if (n % batchSize == 0) {
	  if (isLoggingEnabled())
	    getLogger().info(LoggingHelper.getMethodName() + ": committing batches, # records so far: " + n);
	  delete.executeBatch();
	  insert.executeBatch();
	  if (!autoCommit)
	    connection.commit();
	  delete.clearBatch();
	  insert.clearBatch();
	  committed = true;
	}
      }
      catch (Exception e) {
	result = false;
	break;
      }
    }

    try {
      if (!committed) {
	delete.executeBatch();
	insert.executeBatch();
	if (!autoCommit)
	  connection.commit();
      }
      delete.clearBatch();
      insert.clearBatch();
    }
    catch (Exception e) {
      // ignored
    }

    SQLUtils.close(delete);
    SQLUtils.close(insert);

    if (!autoCommit && useSameConnection) {
      try {
	getConnection(false).setAutoCommit(true);
      }
      catch (Exception e) {
	getLogger().log(Level.WARNING, "Failed to turn on auto-commit!", e);
      }
    }

    if (!useSameConnection) {
      try {
	connection.close();
      }
      catch (Exception e) {
	getLogger().log(Level.WARNING, "Failed to close connection!", e);
      }
    }

    return result && !m_BulkAddStopped;
  }

  /**
   * Interrupts a currently running bulk store, if possible.
   */
  @Override
  public void stopBulkAdd() {
    m_BulkAddStopped = true;
  }

  /**
   * Return columns for this table.
   *
   * @return database columns
   */
  @Override
  protected ColumnMapping getColumnMapping() {
    ColumnMapping cm = new ColumnMapping();
    cm.addMapping("AUTO_ID",  new AutoIncrementType());  // auto increment ID
    cm.addMapping("SAMPLEID", new ColumnType(Types.VARCHAR, 255)); // text id
    cm.addMapping("SAMPLETYPE",   new ColumnType(Types.VARCHAR, 20)); // sample type
    cm.addMapping("FORMAT",   new ColumnType(Types.VARCHAR, 20)); // format of data
    cm.addMapping("POINTS",   new ColumnType(Types.LONGVARCHAR, -1)); // for storing the points as string
    return cm;
  }

  /**
   * Return indices for this table.
   *
   * @return indices
   */
  @Override
  protected Indices getIndices() {
    Indices indices = new Indices();

    Index index = new Index();
    index.add(new IndexColumn("AUTO_ID"));
    indices.add(index);

    index = new Index();
    index.add(new IndexColumn("SAMPLEID"));
    indices.add(index);

    index = new Index();
    index.add(new IndexColumn("SAMPLETYPE"));
    indices.add(index);

    index = new Index();
    index.add(new IndexColumn("FORMAT"));
    indices.add(index);

    index = new Index();
    index.add(new IndexColumn("SAMPLEID"));
    index.add(new IndexColumn("FORMAT"));
    indices.add(index);

    index = new Index();
    index.add(new IndexColumn("SAMPLEID"));
    index.add(new IndexColumn("SAMPLETYPE"));
    index.add(new IndexColumn("FORMAT"));
    indices.add(index);

    return indices;
  }

  /**
   * Removes the spectrum and its sample data.
   * Uses {@link SampleData#DEFAULT_FORMAT} as format.
   *
   * @param sample_id	the sample ID of the spectrum
   * @param keepReport	if true does not delete associated report
   * @return		true if no error
   * @see		#remove(String, String, boolean)
   */
  public boolean remove(String sample_id, boolean keepReport) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": sample_id=" + sample_id + ", keepReport=" + keepReport);
    return remove(sample_id, SampleData.FORMAT, keepReport);
  }

  /**
   * Removes the spectrum and its sample data.
   *
   * @param sample_id	the sample ID of the spectrum
   * @param format	the format of the spectrum (eg NIR)
   * @param keepReport	if true does not delete associated report
   * @return		true if no error
   */
  public synchronized boolean remove(String sample_id, String format, boolean keepReport) {
    ResultSet	rs;

    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": sample_id=" + sample_id + ", format=" + format + ", keepReport=" + keepReport);

    rs = null;
    try {
      rs = select("AUTO_ID", "SAMPLEID = " + SQLUtils.backquote(sample_id) + " AND FORMAT = " + SQLUtils.backquote(format));
      if (rs.next()) {
	return remove(rs.getInt(1), keepReport);
      }
      else {
	getLogger().severe("Failed to locate DB-ID for: " + sample_id + "/" + format);
	return false;
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to determine DB-ID for: " + sample_id + "/" + format, e);
      return false;
    }
    finally {
      SQLUtils.closeAll(rs);
    }
  }

  /**
   * Removes the spectrum and its sample data.
   *
   * @param id		the ID of the spectrum to remove from the database
   * @param keepReport	if true does not delete associated report
   * @return		true if no error
   */
  public synchronized boolean remove(int id, boolean keepReport) {
    boolean	result;
    String	sql;
    Spectrum	sp;

    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": id=" + id + ", keepReport=" + keepReport);

    sp = load(id);

    // delete spectrum
    sql = "DELETE FROM " + getTableName() + " WHERE AUTO_ID = " + id;
    try {
      execute(sql);
      result = true;
    }
    catch (Exception e) {
      result = false;
      getLogger().log(Level.SEVERE, "Failed to remove: " + id, e);
    }

    if (result && (sp != null) && !keepReport)
      // delete sample data
      result = SampleDataF.getSingleton(getDatabaseConnection()).remove(sp.getID());

    return result;
  }

  /**
   * Returns an iterator over the spectra that were identified by the conditions.
   *
   * @param conditions		the conditions to use
   * @param newConnection 	whether to use a separate connection
   * @return			the iterator, null if failed to instantiate
   */
  @Override
  public SpectrumIterator iterate(AbstractSpectrumConditions conditions, boolean newConnection) {
    String			select;
    StringBuilder		sql;
    List<String>		where;
    int				i;
    StringBuilder		tables;
    boolean			hasInstrument;
    boolean			hasSampleID;
    boolean			hasFormat;
    boolean			hasSampleType;
    BaseDouble[]		minValues;
    BaseDouble[]		maxValues;
    Field[]			fields;
    Field[]			required;
    String			sort;
    Connection 			connection;
    Statement			stmt;
    ResultSet			rs;
    StringBuilder		query;

    where = new ArrayList<>();

    // fix conditions
    conditions.check();
    hasInstrument = !conditions.getInstrument().isEmpty() && !conditions.getInstrument().isMatchAll();
    hasSampleID   = !conditions.getSampleIDRegExp().isEmpty() && !conditions.getSampleIDRegExp().isMatchAll();
    hasFormat     = !conditions.getFormat().isEmpty() && !conditions.getFormat().isMatchAll();
    hasSampleType = !conditions.getSampleTypeRegExp().isEmpty() && !conditions.getSampleTypeRegExp().isMatchAll();

    if (conditions instanceof SpectrumConditionsSingle) {
      minValues = new BaseDouble[]{((SpectrumConditionsSingle) conditions).getMinimumValue()};
      maxValues = new BaseDouble[]{((SpectrumConditionsSingle) conditions).getMaximumValue()};
      fields    = new Field[]{((SpectrumConditionsSingle) conditions).getField()};
      required  = new Field[]{((SpectrumConditionsSingle) conditions).getRequiredField()};
    }
    else if (conditions instanceof SpectrumConditionsMulti) {
      minValues = ((SpectrumConditionsMulti) conditions).getMinimumValues();
      maxValues = ((SpectrumConditionsMulti) conditions).getMaximumValues();
      fields    = ((SpectrumConditionsMulti) conditions).getFields();
      required  = ((SpectrumConditionsMulti) conditions).getRequiredFields();
    }
    else {
      throw new IllegalArgumentException("Unhandled conditions class: " + conditions.getClass().getName());
    }

    getLogger().severe("Looking for: " + conditions);
    try {
      // SELECT
      select = "AUTO_ID, SAMPLEID, SAMPLETYPE, FORMAT, POINTS";

      // FROM
      tables = new StringBuilder(getTableName() + " sp");
      if (conditions.getSortOnInsertTimestamp())
	tables.append(", ").append(getSampleDataHandler().getTableName()).append(" sd");
      if (fields.length > 0) {
	for (i = 0; i < fields.length; i++) {
	  if (!fields[i].getName().isEmpty())
	    tables.append(", ").append(getSampleDataHandler().getTableName()).append(" sd").append(i);
	}
      }
      if (!conditions.getStartDate().isInfinity())
	tables.append(", ").append(getSampleDataHandler().getTableName()).append(" sd_start");
      if (!conditions.getEndDate().isInfinity())
	tables.append(", ").append(getSampleDataHandler().getTableName()).append(" sd_end");
      if (hasInstrument)
	tables.append(", ").append(getSampleDataHandler().getTableName()).append(" sd_instrument");
      if (conditions.getExcludeDummies() || conditions.getOnlyDummies())
	tables.append(", ").append(getSampleDataHandler().getTableName()).append(" sd_dummies");
      if (required.length > 0) {
	for (i = 0; i < required.length; i++) {
	  if (!required[i].getName().isEmpty())
	    tables.append(", ").append(getSampleDataHandler().getTableName()).append(" sd_req").append(i);
	}
      }
      // for sorting by date
      if (conditions.getSortOnInsertTimestamp())
	tables.append(", ").append(getSampleDataHandler().getTableName()).append(" sd_sort_by_date");

      // WHERE
      if (fields.length > 0) {
	for (i = 0; i < fields.length; i++) {
	  if (!fields[i].getName().isEmpty()) {
	    where.add("sd" + i + ".ID = sp.SAMPLEID");
	    where.add("sd" + i + ".NAME = " + SQLUtils.backquote(fields[i].getName()));
	  }
	}
      }

      for (i = 0; i < minValues.length; i++) {
	if (minValues[i].doubleValue() > -1)
	  where.add("sd" + i + ".VALUE >= " + minValues[i]);
	if (maxValues[i].doubleValue() > -1)
	  where.add("sd" + i + ".VALUE <= " + maxValues[i]);
      }

      if (hasSampleID)
	where.add(m_Queries.regexp("sp.SAMPLEID", conditions.getSampleIDRegExp()));

      if (hasSampleType)
	where.add(m_Queries.regexp("sp.SAMPLETYPE", conditions.getSampleTypeRegExp()));

      if (hasFormat)
	where.add(m_Queries.regexp("sp.FORMAT", conditions.getFormat()));

      if (!conditions.getStartDate().isInfinity()) {
	where.add("sd_start" + ".ID = sp.SAMPLEID");
	where.add("sd_start" + ".NAME = " + SQLUtils.backquote(SampleData.INSERT_TIMESTAMP));
	where.add("sd_start" + ".VALUE >= " + SQLUtils.backquote(conditions.getStartDate().stringValue()));
      }

      if (!conditions.getEndDate().isInfinity()) {
	where.add("sd_end" + ".ID = sp.SAMPLEID");
	where.add("sd_end" + ".NAME = " + SQLUtils.backquote(SampleData.INSERT_TIMESTAMP));
	where.add("sd_end" + ".VALUE <= " + SQLUtils.backquote(conditions.getEndDate().stringValue()));
      }

      if (hasInstrument) {
	where.add("sd_instrument" + ".ID = sp.SAMPLEID");
	where.add("sd_instrument" + ".NAME = " + SQLUtils.backquote(SampleData.INSTRUMENT));
	where.add(m_Queries.regexp("sd_instrument" + ".VALUE", conditions.getInstrument()));
      }

      if (conditions.getExcludeDummies() || conditions.getOnlyDummies()) {
	where.add("sd_dummies.ID = sp.SAMPLEID");
	where.add("sd_dummies.NAME = " + SQLUtils.backquote(SampleData.FIELD_DUMMYREPORT));
	where.add("sd_dummies.VALUE = " + SQLUtils.backquote("" + conditions.getOnlyDummies()));
      }

      if (required.length > 0) {
	for (i = 0; i < required.length; i++) {
	  if (!required[i].getName().isEmpty()) {
	    where.add("sd_req" + i + ".ID = sp.SAMPLEID");
	    where.add("sd_req" + i + ".NAME = " + SQLUtils.backquote(required[i].getName()));
	  }
	}
      }

      if (conditions.getSortOnInsertTimestamp()) {
	where.add("sd.ID = " + "sp.SAMPLEID");
	where.add("sd.NAME = " + SQLUtils.backquote(SampleData.INSERT_TIMESTAMP));
	where.add("sd_sort_by_date" + ".ID = sp.SAMPLEID");
	where.add("sd_sort_by_date" + ".NAME = " + SQLUtils.backquote(SampleData.INSERT_TIMESTAMP));
      }

      // generate SQL
      sql = new StringBuilder(Utils.flatten(where, " AND "));

      // ordering
      if (conditions.getLatest())
	sort = " DESC";
      else
	sort = " ASC";
      if (conditions.getSortOnInsertTimestamp())
	sql.append(" ORDER BY sd_sort_by_date.VALUE").append(sort);
      else if (conditions.getSortOnSampleID())
	sql.append(" ORDER BY sp.SAMPLEID").append(sort);
      else
	sql.append(" ORDER BY sp.AUTO_ID").append(sort);

      // limit
      if (conditions.getLimit() > 0)
	sql.append(" ").append(m_Queries.limit(conditions.getLimit()));

      // query database
      query = new StringBuilder("SELECT ").append(select)
		.append(" FROM ").append(tables)
		.append(" WHERE ").append(sql);
      if (newConnection) {
	connection = getDatabaseConnection().newConnection(false);
	stmt       = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
	rs         = stmt.executeQuery(query.toString());
	return new SpectrumIterator(this, getSampleDataHandler(), rs, connection);
      }
      else {
	rs = select(select, tables.toString(), sql.toString());
	return new SpectrumIterator(this, getSampleDataHandler(), rs, null);
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to get IDs: " + conditions, e);
    }

    return null;
  }

  /**
   * Initializes the table. Used by the "InitializeTables" tool.
   *
   * @param dbcon	the database context
   */
  public static synchronized void initTable(AbstractDatabaseConnection dbcon) {
    getSingleton(dbcon).init();
  }

  /**
   * Returns the singleton of the table (active).
   *
   * @param dbcon	the database connection to get the singleton for
   * @return		the singleton
   */
  public static synchronized SpectrumIntf getSingleton(AbstractDatabaseConnection dbcon) {
    if (m_TableManager == null)
      m_TableManager = new TableManager<>(TABLE_NAME, dbcon.getOwner());
    if (!m_TableManager.has(dbcon))
      m_TableManager.add(dbcon, new adams.db.mysql.SpectrumT(dbcon));

    return m_TableManager.get(dbcon);
  }
}
