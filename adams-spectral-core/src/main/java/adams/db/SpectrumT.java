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
 * Copyright (C) 2008-2016 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.db;

import adams.core.Constants;
import adams.core.LRUCache;
import adams.core.Performance;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.db.indices.Index;
import adams.db.indices.IndexColumn;
import adams.db.indices.Indices;
import adams.db.types.AutoIncrementType;
import adams.db.types.ColumnType;
import adams.event.DatabaseConnectionChangeEvent;

import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Manages Spectrum tables. Spectrums can be cached, see the
 * <code>Performance</code> class.
 *
 * @author dale
 * @version $Revision: 12453 $
 */
public class SpectrumT
  extends AbstractIndexedTable
  implements DataProvider<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 8400767916698176690L;

  /** the table names. */
  public final static String TABLE_NAME = "spectrum";

  public static final String MAX_NUM_SPECTRUMS_CACHED = "maxNumSpectrumsCached";

  /** Stores Spectrums for Reference (key is AUTO_ID). */
  protected static LRUCache<Integer, Spectrum> m_cacheDatabaseID;

  /** Stores Spectrums for Reference (key is SAMPLE_ID TAB FORMAT). */
  protected static LRUCache<String, Spectrum> m_cacheSampleIDFormat;

  /** the table manager. */
  protected static TableManager<SpectrumT> m_TableManager;

  /**
   * Constructor - initalise with database connection.
   *
   * @param dbcon	the database context this table is used in
   */
  protected SpectrumT(AbstractDatabaseConnection dbcon) {
    super(dbcon, SpectrumT.TABLE_NAME);

    int maxCached = Performance.getInteger(MAX_NUM_SPECTRUMS_CACHED, 100);
    if (m_cacheDatabaseID == null)
      m_cacheDatabaseID = new LRUCache<>(maxCached);
    if (m_cacheSampleIDFormat == null)
      m_cacheSampleIDFormat = new LRUCache<>(maxCached);
  }

  /**
   * Returns the corresponding SampleDataT table.
   *
   * @return		the corresponding table
   */
  public SampleDataT getSampleDataT() {
    return SampleDataT.getSingleton(getDatabaseConnection());
  }

  /**
   * Initialise table & sub-tables.
   *
   * @return success?
   */
  @Override
  public synchronized boolean init(){
    if (!tableExists()) {
      if (!super.init())
	return false;
    }
    else {
      if (!columnsMatch(getColumnMapping(), true, true))
        return false;
    }

    if (!getSampleDataT().init())
      return false;

    return true;
  }

  /**
   * Checks whether the container exists in the database.
   *
   * @param id		the database ID of the data container
   * @return		true if the container exists
   */
  public boolean exists(int id) {
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
    return exists(id, SampleData.DEFAULT_FORMAT);
  }

  /**
   * Checks whether the container exists in the database.
   *
   * @param id		the ID of the data container
   * @return		true if the container exists
   */
  public boolean exists(String id, String format) {
    return isThere("SAMPLEID = " + backquote(id) + " AND FORMAT = " + backquote(format));
  }

  /**
   * Load a spectrum with given database ID. Get from cache if available
   *
   * @param auto_id	the database ID
   * @return 		Spectrum, or null if not found
   */
  public synchronized Spectrum load(int auto_id){
    Spectrum sp = m_cacheDatabaseID.get(auto_id);
    if (sp == null) {
      sp = loadFromDB(auto_id, "", true);
      if (m_cacheDatabaseID.isEnabled())
	m_cacheDatabaseID.put(sp.getDatabaseID(), sp);
    }
    return sp;
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
    Spectrum sp = m_cacheSampleIDFormat.get(sample_id + "\t" + format);

    if (sp == null) {
      sp = loadFromDB(sample_id, format, true);
      if (m_cacheSampleIDFormat.isEnabled())
	m_cacheSampleIDFormat.put(sample_id + "\t" + format, sp);
    }
    else {
      sp = (Spectrum) sp.getClone();
    }

    return sp;
  }

  /**
   * Load a data container with given auto_id, without passing it through
   * the global filter.
   *
   * @param auto_id	the databae ID
   * @return 		the data container, or null if not found
   */
  public Spectrum loadRaw(int auto_id) {
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
    return loadFromDB(sample_id, format, true);
  }

  /**
   * Turns a ResultSet into a spectrum.
   *
   * @param rs		the ResultSet to use
   * @param raw		whether to return the raw spectrum or filter through
   * 			the global container filter
   * @return		the spectrum, null in case of an error
   * @throws Exception	if something goes wrong
   */
  protected Spectrum resultsetToSpectrum(ResultSet rs, boolean raw) throws Exception {
    Spectrum			result;
    int				auto_id;
    String[]			points;
    String[]			point;
    int				i;
    SpectrumPoint		sp;
    ArrayList<SpectrumPoint>	list;

    result = null;

    if ((rs != null) && (rs.next())) {
      result = new Spectrum();
      result.setID(rs.getString("SAMPLEID"));
      auto_id = rs.getInt("AUTO_ID");
      result.setDatabaseID(auto_id);
      points = rs.getString("POINTS").split(",");
      list   = new ArrayList<>(points.length + 1);
      for (i = 0; i < points.length; i++) {
        if (points[i].indexOf(':') == -1)
          continue;
        point = points[i].split(":");
        sp    = new SpectrumPoint(Float.parseFloat(point[0]), Float.parseFloat(point[1]));
        list.add(sp);
      }
      result.addAll(list);
      list.clear();
      result.setReport(getSampleDataT().load(result.getID()));
      result.setFormat(rs.getString("FORMAT"));
    }

    return result;
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
  public Spectrum loadFromDB(int auto_id, String rlike, boolean raw) {
    Spectrum	result;
    ResultSet 	rs;
    String	regexp;

    result = null;
    rs     = null;
    regexp = JDBC.regexpKeyword(getDatabaseConnection());
    try {
      if (rlike.equals(""))
	rs = select("*", "AUTO_ID=" + auto_id);
      else
	rs = select("*", "AUTO_ID=" + auto_id + " AND SAMPLEID " + regexp + " " + backquote(rlike));

      result = resultsetToSpectrum(rs, raw);
    }
    catch (Exception e) {
      result = null;
      getLogger().log(Level.SEVERE, "Failed to process DB ID " + auto_id, e);
    }
    finally{
      closeAll(rs);
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

    result = null;
    rs     = null;
    try {
      rs     = select("*", "SAMPLEID = " + backquote(sample_id) + " AND FORMAT = " + backquote(format));
      result = resultsetToSpectrum(rs, raw);
    }
    catch (Exception e) {
      result = null;
      getLogger().log(Level.SEVERE, "Failed to process Sample ID " + sample_id, e);
    }
    finally{
      closeAll(rs);
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

    result = Constants.NO_ID;

    rs = null;
    try {
      rs = select("AUTO_ID", "SAMPLEID = " + backquote(sample_id) + " AND FORMAT = " + backquote(format));
      if (rs.next())
        result = rs.getInt(1);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to determine databse ID: " + sample_id + "/" + format, e);
    }
    finally {
      closeAll(rs);
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
    String		sql;
    int			i;
    String		line;
    boolean		hasSampleID;
    boolean		hasFormat;
    String		regexp;

    result = new ArrayList<>();
    rs     = null;
    regexp = JDBC.regexpKeyword(getDatabaseConnection());
    if (where == null)
      where = "";

    hasSampleID = !cond.getSampleIDRegExp().isEmpty() && !cond.getSampleIDRegExp().isMatchAll();
    hasFormat   = !cond.getFormat().isEmpty() && !cond.getFormat().isMatchAll();

    // sample name
    if (hasSampleID) {
      if (where.length() > 0)
	where += " AND";
      where += " SAMPLEID " + regexp + " " + backquote(cond.getSampleIDRegExp());
    }

    // data format
    if (hasFormat) {
      if (where.length() > 0)
	where += " AND";
      where += " FORMAT " + regexp + " " + backquote(cond.getFormat());
    }

    // limit
    if (cond.getLimit() > -1)
      where += " LIMIT " + cond.getLimit();

    if (where.length() == 0)
      where = null;
    else
      where = where.trim();

    try {
      sql = "";
      for (i = 0; i < fields.length; i++) {
	if (i > 0)
	  sql += ", ";
	sql += fields[i];
      }
      rs = select(sql, tables, where);
      if (rs == null)
	return result;

      while (rs.next()) {
	line = "";
	for (i = 0; i < fields.length; i++) {
	  if (i > 0)
	    line += "\t";
	  line += rs.getObject(fields[i]);
	}
	result.add(line);
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to get values", e);
    }
    finally{
      closeAll(rs);
    }

    return result;
  }

  /**
   * Turns the spectrum points into a string to be stored in the database.
   * Format: wave1:ampltd1,wave2:ampltd2,...
   *
   * @param sp		the spectrum to convert
   * @return		the generated string
   */
  protected String pointsToString(Spectrum sp) {
    StringBuilder	result;

    result = new StringBuilder();

    for (SpectrumPoint point: sp.toList()) {
      if (result.length() > 0)
	result.append(",");
      result.append(Float.toString(point.getWaveNumber()));
      result.append(":");
      result.append(Float.toString(point.getAmplitude()));
    }

    return result.toString();
  }

  protected StringBuilder addQuery(Spectrum sp) {
    StringBuilder 	q;

    q = new StringBuilder();
    q.append("INSERT INTO " + getTableName() + " (SAMPLEID, FORMAT, POINTS) VALUES (");

    // sample ID
    q.append(backquote(sp.getID()));

    // format
    q.append(",");
    q.append(backquote(sp.getFormat()));

    // spectrum points (if points table disabled)
    q.append(",");
    q.append("'");
    q.append(pointsToString(sp));
    q.append("'");
    q.append(")");

    return q;
  }

  /**
   * Adds a spectrum to the database. Returns the created auto-id, and sets in
   * Spectrum.
   *
   * @param sp  	spectrum Header
   * @return  	new ID, or null if fail
   */
  public synchronized Integer add(Spectrum sp) {
    Integer 		result;
    StringBuilder 	q;
    ResultSet 		rs;

    result = null;

    if (getDebug())
      getLogger().info("Entered add");

    q  = addQuery(sp);
    rs = null;
    try {
      if (getDebug())
	getLogger().info("Try insert keygen");
      rs = executeGeneratedKeys(q.toString());
      if (getDebug())
	getLogger().info("Try insert keygen ret");

      if (rs != null){
	if (rs.next()) {
	  result = rs.getInt("auto_id");
	  sp.setDatabaseID(result);

	  // store report (never overwrites, just adds additional fields)
	  if (sp.hasReport())
	    getSampleDataT().store(sp.getID(), sp.getReport(), false, true, new Field[0]);
        }
	else {
	  getLogger().severe("no gen keys");
	  result = null;
	}
      }
      else {
	getLogger().severe("null genkeys");
	result = null;
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to insert: " + sp,  e);
      result = null;
    }
    finally {
      closeAll(rs);
    }

    return result;
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
    cm.addMapping("FORMAT",   new ColumnType(Types.VARCHAR, 20)); // format of data
    cm.addMapping("POINTS",   new ColumnType(Types.LONGVARCHAR, -1)); // for storing the points as string
    return cm;
  }

  /**
   * Return columns for this table (old format).
   *
   * @return database columns
   */
  protected ColumnMapping getColumnMappingOld() {
    ColumnMapping cm = new ColumnMapping();
    cm.addMapping("AUTO_ID",  new AutoIncrementType());  // auto increment ID
    cm.addMapping("SAMPLEID", new ColumnType(Types.VARCHAR, 255)); // text id
    cm.addMapping("FORMAT",   new ColumnType(Types.VARCHAR, 20)); // format of data
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
    index.add(new IndexColumn("FORMAT"));
    indices.add(index);
    return indices;
  }

  /**
   * Clears the caches.
   */
  protected void clearCaches() {
    if (m_cacheDatabaseID != null) {
      m_cacheDatabaseID.setEnabled(false);
      m_cacheDatabaseID.clear();
      m_cacheDatabaseID.setEnabled(true);
    }

    if (m_cacheSampleIDFormat != null) {
      m_cacheSampleIDFormat.setEnabled(false);
      m_cacheSampleIDFormat.clear();
      m_cacheSampleIDFormat.setEnabled(true);
    }
  }

  /**
   * Removes the spectrum and its sample data.
   * Uses {@link SampleData#DEFAULT_FORMAT} as format.
   *
   * @param sample_id	the sample ID of the spectrum
   * @return		true if no error
   * @see		SpectrumT#remove(String, String)
   */
  public boolean remove(String sample_id) {
    return remove(sample_id, SampleData.FORMAT);
  }

  /**
   * Removes the spectrum and its sample data.
   *
   * @param sample_id	the sample ID of the spectrum
   * @param format	the format of the spectrum (eg NIR)
   * @return		true if no error
   */
  public synchronized boolean remove(String sample_id, String format) {
    ResultSet	rs;
    String	id;
    String	form;
   
    rs = null;
    try {
      rs = select("AUTO_ID", "SAMPLEID = " + backquote(sample_id) + " AND FORMAT = " + backquote(format));
      if (rs.next()) {
	return remove(rs.getInt(1));
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
      closeAll(rs);
    }
  }

  /**
   * Removes the spectrum and its sample data.
   *
   * @param id		the ID of the spectrum to remove from the database
   * @return		true if no error
   */
  public synchronized boolean remove(int id) {
    boolean	result;
    String	sql;
    Spectrum	sp;

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

    if (result && (sp != null))
      // delete sample data
      result = SampleDataT.getSingleton(getDatabaseConnection()).remove(sp.getID());

    // invalidate chromatogram
    removeFromCache(id);

    return result;
  }

  /**
   * Removes the spectrum from the cache.
   *
   * @param sp		the spectrum to remove
   */
  public synchronized void removeFromCache(Spectrum sp) {
    removeFromCache(sp.getDatabaseID());
  }

  /**
   * Removes the spectrum from the cache.
   *
   * @param id		the ID of the spectrum to remove
   */
  public synchronized void removeFromCache(int id) {
    if (m_cacheDatabaseID.contains(id))
      m_cacheDatabaseID.remove(id);
  }

  /**
   * A change in the database connection occurred. Derived classes can
   * override this method to react to changes in the connection.
   *
   * @param e		the event
   */
  @Override
  public void databaseConnectionStateChanged(DatabaseConnectionChangeEvent e) {
    clearCaches();
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
  public static synchronized SpectrumT getSingleton(AbstractDatabaseConnection dbcon) {
    if (m_TableManager == null)
      m_TableManager = new TableManager<SpectrumT>(TABLE_NAME, dbcon.getOwner());
    if (!m_TableManager.has(dbcon)) {
      if (JDBC.isMySQL(dbcon))
        m_TableManager.add(dbcon, new SpectrumTMySQL(dbcon));
      else if (JDBC.isPostgreSQL(dbcon))
        m_TableManager.add(dbcon, new SpectrumTPostgreSQL(dbcon));
      else if (JDBC.isSQLite(dbcon))
        m_TableManager.add(dbcon, new SpectrumTSQLite(dbcon));
      else
        throw new IllegalArgumentException("Unrecognized JDBC URL: " + dbcon.getURL());
    }

    return m_TableManager.get(dbcon);
  }
}
