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
 * Copyright (C) 2008-2019 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.db.generic;

import adams.core.Constants;
import adams.core.Utils;
import adams.core.logging.LoggingHelper;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.db.AbstractDatabaseConnection;
import adams.db.AbstractIndexedTable;
import adams.db.ColumnMapping;
import adams.db.JDBC;
import adams.db.SQLUtils;
import adams.db.SampleDataF;
import adams.db.SpectrumIDConditions;
import adams.db.SpectrumIntf;
import adams.db.TableManager;
import adams.db.indices.Index;
import adams.db.indices.IndexColumn;
import adams.db.indices.Indices;
import adams.db.types.AutoIncrementType;
import adams.db.types.ColumnType;

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
 */
public abstract class SpectrumT
  extends AbstractIndexedTable
  implements SpectrumIntf {

  /** for serialization. */
  private static final long serialVersionUID = 8400767916698176690L;

  /** the table manager. */
  protected static TableManager<SpectrumT> m_TableManager;

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

    if (!getSampleDataHandler().init())
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
   * @param auto_id	the databae ID
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
        if (points[i].indexOf(':') == -1) {
	  sp = new SpectrumPoint(i, Float.parseFloat(points[i]));
	}
	else {
	  point = points[i].split(":");
	  sp = new SpectrumPoint(Float.parseFloat(point[0]), Float.parseFloat(point[1]));
	}
        list.add(sp);
      }
      result.addAll(list);
      list.clear();
      result.setReport(getSampleDataHandler().load(result.getID()));
      result.setType(rs.getString("SAMPLETYPE"));
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

    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": auto_id=" + auto_id + ", rlike=" + rlike + ", raw=" + raw);

    result = null;
    rs     = null;
    regexp = JDBC.regexpKeyword(getDatabaseConnection());
    try {
      if (rlike.equals(""))
	rs = select("*", "AUTO_ID=" + auto_id);
      else
	rs = select("*", "AUTO_ID=" + auto_id + " AND SAMPLEID " + regexp + " " + SQLUtils.backquote(rlike));

      result = resultsetToSpectrum(rs, raw);
    }
    catch (Exception e) {
      result = null;
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
      result = resultsetToSpectrum(rs, raw);
    }
    catch (Exception e) {
      result = null;
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
    String		sql;
    int			i;
    String		line;
    boolean		hasSampleID;
    boolean		hasSampleType;
    boolean		hasFormat;
    String		regexp;

    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": fields=" + Utils.arrayToString(fields) + ", tables=" + tables + ", where=" + where + ", cond=" + cond);

    result = new ArrayList<>();
    rs     = null;
    regexp = JDBC.regexpKeyword(getDatabaseConnection());
    if (where == null)
      where = "";

    hasSampleID   = !cond.getSampleIDRegExp().isEmpty() && !cond.getSampleIDRegExp().isMatchAll();
    hasSampleType = !cond.getSampleTypeRegExp().isEmpty() && !cond.getSampleTypeRegExp().isMatchAll();
    hasFormat     = !cond.getFormat().isEmpty() && !cond.getFormat().isMatchAll();

    // sample name
    if (hasSampleID) {
      if (where.length() > 0)
	where += " AND";
      where += " SAMPLEID " + regexp + " " + SQLUtils.backquote(cond.getSampleIDRegExp());
    }

    // sample type
    if (hasSampleType) {
      if (where.length() > 0)
	where += " AND";
      where += " SAMPLETYPE " + regexp + " " + SQLUtils.backquote(cond.getSampleTypeRegExp());
    }

    // data format
    if (hasFormat) {
      if (where.length() > 0)
	where += " AND";
      where += " FORMAT " + regexp + " " + SQLUtils.backquote(cond.getFormat());
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
	result.append(Float.toString(point.getWaveNumber()));
	result.append(":");
      }
      result.append(Float.toString(point.getAmplitude()));
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
    q.append("INSERT INTO " + getTableName() + " (SAMPLEID, SAMPLETYPE, FORMAT, POINTS) VALUES (");

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
   * @param sp  	spectrum Header
   * @param storeWaveNo   whether to store the wave numbers as well
   * @return  	new ID, or null if fail
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
      SQLUtils.closeAll(rs);
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
