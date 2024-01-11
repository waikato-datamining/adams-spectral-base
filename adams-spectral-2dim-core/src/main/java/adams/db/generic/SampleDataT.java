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
 * Copyright (C) 2008-2024 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.db.generic;

import adams.core.DateFormat;
import adams.core.DateUtils;
import adams.core.Utils;
import adams.core.base.BaseDouble;
import adams.core.logging.LoggingHelper;
import adams.data.report.AbstractField;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.db.AbstractConditions;
import adams.db.AbstractDatabaseConnection;
import adams.db.AbstractSpectrumConditions;
import adams.db.ColumnMapping;
import adams.db.JDBC;
import adams.db.OrphanedSampleDataConditions;
import adams.db.ReportTableByID;
import adams.db.SQLUtils;
import adams.db.SampleDataIntf;
import adams.db.SpectrumConditionsMulti;
import adams.db.SpectrumConditionsSingle;
import adams.db.SpectrumF;
import adams.db.TableManager;
import adams.db.indices.Index;
import adams.db.indices.IndexColumn;
import adams.db.indices.Indices;
import adams.db.types.ColumnType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * A class for handling the sample data reports table.
 *
 * @author dale
 */
public abstract class SampleDataT
  extends ReportTableByID<SampleData, Field>
  implements SampleDataIntf {

  /** for serialization. */
  private static final long serialVersionUID = 8386415021395089076L;

  /** the table manager. */
  protected static TableManager<SampleDataT> m_TableManager;

  /** whether to stop the bulk store. */
  protected boolean m_BulkStoreStopped;

  /**
   * Constructor.
   *
   * @param dbcon	the database context this table is used in
   */
  protected SampleDataT(AbstractDatabaseConnection dbcon) {
    super(dbcon, TABLE_NAME);
  }

  /**
   * Returns all available fields.
   *
   * @param dtype	the type to limit the search to, use "null" for all
   * @return		the list of fields
   */
  public List<Field> getFields(DataType dtype) {
    List<Field>		result;
    ResultSet		rs;
    List<String>	whereParts;
    String		where;
    String		tables;
    int			i;
    DataType[]		dtypes;

    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": dtype=" + dtype);

    result = new ArrayList<>();

    if (dtype != null)
      dtypes = new DataType[]{dtype};
    else
      dtypes = DataType.values();

    for (DataType dt: dtypes) {
      rs = null;
      try {
	// assemble SQL
	whereParts = new ArrayList<>();
	where      = null;
	tables     = getTableName() + " sd";

	if (dtype != null)
	  whereParts.add("sd.TYPE = " + SQLUtils.backquote(dtype.toString()));

	if (whereParts.size() > 0) {
	  where = "";
	  for (i = 0; i < whereParts.size(); i++) {
	    if (i > 0)
	      where += " AND ";
	    where += whereParts.get(i);
	  }
	}

	// get data
	rs = selectDistinct("sd.NAME", tables, where);
	while (rs.next()) {
	  String name = rs.getString(1);
	  result.add(new Field(name, dt));
	}
      }
      catch (Exception e) {
	getLogger().log(Level.SEVERE, "Failed to get fields: " + dtype, e);
      }
      finally {
	SQLUtils.closeAll(rs);
      }
    }

    return result;
  }

  /**
   * Checks whether the report exists in the database.
   *
   * @param id	the ID of parent data container
   * @return		true if the report exists
   */
  public boolean exists(String id) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": id=" + id);
    return isThere("ID = " + SQLUtils.backquote(id));
  }

  /**
   * Get params.
   *
   * @param id		sample ID of spectrum
   * @return		the hashtable
   */
  public SampleData load(String id) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": id=" + id);
    SampleData result = new SampleData();
    ResultSet rs = null;
    try {
      rs = select(
	"ID, NAME, TYPE, VALUE",
	getTableName(),
	"ID = " + SQLUtils.backquote(id));
      while (rs.next()) {
	String name = rs.getString("NAME");
	String type = rs.getString("TYPE");
	String sval = rs.getString("VALUE");
	Field field = new Field(createField(name, type));
	try {
	  result.addField(field);
	  result.setValue(field, parse(field, sval));
	}
	catch (Exception e) {
	  getLogger().warning("Failed to parse #" + id + ": name=" + name + ", type=" + type + ", value=" + sval);
	  field = new Field(createField(name, "S"));
	  result.addField(field);
	  result.setValue(field, parse(field, sval));
	}
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to load: " + id, e);
    }
    finally {
      SQLUtils.closeAll(rs);
    }

    return result;
  }

  /**
   * Stores the report. Either updates or inserts the fields.
   *
   * @param id		the id of the report
   * @param report	the report
   * @return		true if successfully inserted
   */
  @Override
  protected boolean doStore(String id, SampleData report) {
    PreparedStatement 	stmtUpdate;
    PreparedStatement 	stmtInsert;

    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": id=" + id + ", report");

    report.update();

    if (id == null) {
      getLogger().severe("Report has ID - skipping saving!");
      return false;
    }

    // check for "Insert timestamp"
    Field field = new Field(SampleData.INSERT_TIMESTAMP, DataType.STRING);
    if (!report.hasValue(field)) {
      DateFormat dformat = DateUtils.getTimestampFormatter();
      report.addField(field);
      report.setValue(field, dformat.format(new Date()));
    }

    Hashtable<AbstractField,Object> table = report.getParams();
    boolean result = true;
    Set<String> names;
    try {
      names = new HashSet<>(selectString(false, "NAME", getTableName(), "ID = " + SQLUtils.backquote(id)));
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to query existing names for " + id, e);
      return false;
    }

    try {
      stmtUpdate = prepareStatement(
	"UPDATE " + getTableName() + " SET VALUE = ?, TYPE = ? WHERE ID = ? AND NAME = ?");
      stmtInsert = prepareStatement(
	"INSERT INTO " + getTableName() + "(ID, NAME, TYPE, VALUE) VALUES(?, ?, ?, ?)");
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to prepare update/insert statements for " + getTableName(), e);
      return false;
    }

    boolean updated = false;
    boolean inserted = false;
    for (AbstractField key : table.keySet()) {
      // format is stored in spectrum
      if (key.getName().equals(SampleData.FORMAT))
	continue;
      // we don't want to store the dummy report flag
      if (key.getName().equals(SampleData.FIELD_DUMMYREPORT))
	continue;

      // check numeric
      if (key.getDataType() == DataType.NUMERIC) {
        if (!Utils.isDouble("" + table.get(key))) {
          getLogger().warning(id + ": '" + key.getName() + "' is not numeric: " + table.get(key));
	  continue;
	}
      }
      // check boolean
      if (key.getDataType() == DataType.BOOLEAN) {
        if (!Utils.isBoolean("" + table.get(key))) {
          getLogger().warning(id + ": '" + key.getName() + "' is not boolean: " + table.get(key));
	  continue;
	}
      }

      try {
	if (names.contains(key.getName())) {
	  updated = true;
	  stmtUpdate.setString(2, key.getDataType().toString());
	  stmtUpdate.setString(3, id);
	  stmtUpdate.setString(4, key.getName());
	  switch (key.getDataType()) {
	    case STRING:
	    case UNKNOWN:
	      stmtUpdate.setString(1, table.get(key).toString());
	      stmtUpdate.addBatch();
	      break;
	    case BOOLEAN:
	      stmtUpdate.setBoolean(1, (Boolean) table.get(key));
	      stmtUpdate.addBatch();
	      break;
	    case NUMERIC:
	      stmtUpdate.setDouble(1, (Double) table.get(key));
	      stmtUpdate.addBatch();
	      break;
	    default:
	      throw new IllegalStateException("Unhandled data type for " + id + ": " + key.getDataType());
	  }
	}
	else {
	  inserted = true;
	  stmtInsert.setString(1, id);
	  stmtInsert.setString(2, key.getName());
	  stmtInsert.setString(3, key.getDataType().toString());
	  switch (key.getDataType()) {
	    case STRING:
	    case UNKNOWN:
	      stmtInsert.setString(4, table.get(key).toString());
	      stmtInsert.addBatch();
	      break;
	    case BOOLEAN:
	      stmtInsert.setBoolean(4, (Boolean) table.get(key));
	      stmtInsert.addBatch();
	      break;
	    case NUMERIC:
	      stmtInsert.setDouble(4, (Double) table.get(key));
	      stmtInsert.addBatch();
	      break;
	    default:
	      throw new IllegalStateException("Unhandled data type for " + id + ": " + key.getDataType());
	  }
	}
      }
      catch (Exception e) {
	getLogger().log(Level.SEVERE, "Failed to add insert/update statement: " + id, e);
	SQLUtils.close(stmtInsert);
	SQLUtils.close(stmtUpdate);
	return false;
      }
    }

    if (updated) {
      try {
	stmtUpdate.executeBatch();
	stmtUpdate.clearBatch();
      }
      catch (Exception e) {
	getLogger().log(Level.SEVERE, "Failed to update: " + id, e);
	result = false;
      }
    }

    if (inserted) {
      try {
	stmtInsert.executeBatch();
	stmtInsert.clearBatch();
      }
      catch (Exception e) {
	getLogger().log(Level.SEVERE, "Failed to insert: " + id, e);
	result = false;
      }
    }

    SQLUtils.close(stmtInsert);
    SQLUtils.close(stmtUpdate);

    return result;
  }

  /**
   * Column mapping for table.
   *
   * @return column mapping
   */
  @Override
  protected ColumnMapping getColumnMapping() {
    ColumnMapping cm = new ColumnMapping();
    cm.addMapping("ID",    new ColumnType(Types.VARCHAR, 255));  // ID from spectrum header
    cm.addMapping("NAME",  new ColumnType(Types.VARCHAR, 255)); // key
    cm.addMapping("TYPE",  new ColumnType(Types.VARCHAR, 1)); // type (N=numeric, S=string,B=boolean)
    cm.addMapping("VALUE", new ColumnType(Types.VARCHAR, 10240));	// String value
    return cm;
  }

  /**
   * Get table indices.
   *
   * @return	indices.
   */
  @Override
  protected Indices getIndices() {
    Indices indices = new Indices();

    Index index = new Index();
    index.add(new IndexColumn("ID"));
    indices.add(index);

    index = new Index();
    index.add(new IndexColumn("NAME"));
    indices.add(index);

    index = new Index();
    index.add(new IndexColumn("TYPE"));
    indices.add(index);

    index = new Index();
    index.add(new IndexColumn("NAME"));
    index.add(new IndexColumn("TYPE"));
    indices.add(index);

    return indices;
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
  public List<String> getIDs(AbstractConditions cond) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": cond=" + cond);
    return getIDs(new String[]{"sp.SAMPLEID"}, cond);
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
  public List<String> getIDs(String[] columns, AbstractConditions cond) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": columns=" + Utils.arrayToString(columns) + ", cond=" + cond);
    return (List<String>) getIDs(columns, cond, false);
  }

  /**
   * Return a list of database IDs of data containers that match the defined
   * conditions.
   *
   * @param conditions	the conditions that the conatiners must meet
   * @return		list of database IDs
   */
  public List<Integer> getDBIDs(AbstractConditions conditions) {
    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": conditions=" + conditions);
    return (List<Integer>) getIDs(new String[]{"sp.AUTO_ID"}, conditions, true);
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
  protected List getIDs(String[] columns, AbstractConditions cond, boolean dbids) {
    List	 		result;
    String			sql;
    List<String>		where;
    int				i;
    String			tables;
    String			select;
    String			line;
    boolean			hasInstrument;
    boolean			hasSampleID;
    boolean			hasFormat;
    boolean			hasSampleType;
    AbstractSpectrumConditions conditions;
    BaseDouble[]		minValues;
    BaseDouble[]		maxValues;
    Field[]			fields;
    Field[]			required;
    String			regexp;
    String			sort;

    if (dbids)
      result = new ArrayList<Integer>();
    else
      result = new ArrayList<String>();
    where      = new ArrayList<>();
    conditions = (AbstractSpectrumConditions) cond;
    regexp     = JDBC.regexpKeyword(getDatabaseConnection());

    // fix conditions
    conditions.check();
    hasInstrument = !conditions.getInstrument().isEmpty() && !conditions.getInstrument().isMatchAll();
    hasSampleID   = !conditions.getSampleIDRegExp().isEmpty() && !conditions.getSampleIDRegExp().isMatchAll();
    hasFormat     = !conditions.getFormat().isEmpty() && !conditions.getFormat().isMatchAll();
    hasSampleType = !conditions.getSampleTypeRegExp().isEmpty() && !conditions.getSampleTypeRegExp().isMatchAll();

    if (cond instanceof SpectrumConditionsSingle) {
      minValues = new BaseDouble[]{((SpectrumConditionsSingle) cond).getMinimumValue()};
      maxValues = new BaseDouble[]{((SpectrumConditionsSingle) cond).getMaximumValue()};
      fields    = new Field[]{((SpectrumConditionsSingle) cond).getField()};
      required  = new Field[]{((SpectrumConditionsSingle) cond).getRequiredField()};
    }
    else if (cond instanceof SpectrumConditionsMulti) {
      minValues = ((SpectrumConditionsMulti) cond).getMinimumValues();
      maxValues = ((SpectrumConditionsMulti) cond).getMaximumValues();
      fields    = ((SpectrumConditionsMulti) cond).getFields();
      required  = ((SpectrumConditionsMulti) cond).getRequiredFields();
    }
    else {
      throw new IllegalArgumentException("Unhandled conditions class: " + cond.getClass().getName());
    }

    getLogger().severe("Looking for: " + conditions);
    try {
      // SELECT
      select = "";
      for (i = 0; i < columns.length; i++) {
	if (i > 0)
	  select += ", ";
	select += columns[i];
      }

      // FROM
      tables = getSpectrumHandler().getTableName() + " sp";
      if (conditions.getSortOnInsertTimestamp())
	tables += ", " + getTableName() + " sd";
      if (fields.length > 0) {
	for (i = 0; i < fields.length; i++) {
	  if (fields[i].getName().length() > 0)
	    tables += ", " + getTableName() + " sd" + i;
	}
      }
      if (!conditions.getStartDate().isInfinity())
	tables += ", " + getTableName() + " sd_start";
      if (!conditions.getEndDate().isInfinity())
	tables += ", " + getTableName() + " sd_end";
      if (hasInstrument)
	tables += ", " + getTableName() + " sd_instrument";
      if (conditions.getExcludeDummies() || conditions.getOnlyDummies())
	tables += ", " + getTableName() + " sd_dummies";
      if (required.length > 0) {
	for (i = 0; i < required.length; i++) {
	  if (required[i].getName().length() > 0)
	    tables += ", " + getTableName() + " sd_req" + i;
	}
      }
      // for sorting by date
      if (conditions.getSortOnInsertTimestamp())
	tables += ", " + getTableName() + " sd_sort_by_date";

      // WHERE
      if (fields.length > 0) {
	for (i = 0; i < fields.length; i++) {
	  if (fields[i].getName().length() > 0) {
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
	where.add("sp.SAMPLEID " + regexp + " " + SQLUtils.backquote(conditions.getSampleIDRegExp()));

      if (hasSampleType)
	where.add("sp.SAMPLETYPE " + regexp + " " + SQLUtils.backquote(conditions.getSampleTypeRegExp()));

      if (hasFormat)
	where.add("sp.FORMAT " + regexp + " " + SQLUtils.backquote(conditions.getFormat()));

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
	where.add("sd_instrument" + ".VALUE " + regexp + " " + SQLUtils.backquote(conditions.getInstrument()));
      }

      if (conditions.getExcludeDummies() || conditions.getOnlyDummies()) {
	where.add("sd_dummies.ID = sp.SAMPLEID");
	where.add("sd_dummies.NAME = " + SQLUtils.backquote(SampleData.FIELD_DUMMYREPORT));
	where.add("sd_dummies.VALUE = " + SQLUtils.backquote("" + conditions.getOnlyDummies()));
      }

      if (required.length > 0) {
	for (i = 0; i < required.length; i++) {
	  if (required[i].getName().length() > 0) {
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
      sql = "";
      for (i = 0; i < where.size(); i++) {
	if (i > 0)
	  sql += " AND ";
	sql += where.get(i);
      }

      // ordering
      if (conditions.getLatest())
	sort = " DESC";
      else
	sort = " ASC";
      if (conditions.getSortOnInsertTimestamp())
	sql += " ORDER BY sd_sort_by_date.VALUE" + sort + ", sp.SAMPLEID ASC";
      else
	sql += " ORDER BY sp.AUTO_ID" + sort;

      // limit
      if (conditions.getLimit() > 0)
	sql += " LIMIT " + conditions.getLimit();

      // query database
      ResultSet rs = select(select, tables, sql);

      while (rs.next()) {
	if (dbids) {
	  result.add(rs.getInt(1));
	}
	else {
	  if (columns.length == 1) {
	    result.add(rs.getString(1));
	  }
	  else {
	    line = "";
	    for (i = 0; i < columns.length; i++) {
	      if (i > 0)
		line += "\t";
	      line += rs.getString(i + 1);
	    }
	    result.add(line);
	  }
	}
      }
      SQLUtils.closeAll(rs);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to get IDs: " + conditions, e);
    }

    getLogger().severe("Found #" + result.size() + " IDs for: " + conditions);

    return result;
  }

  /**
   * Returns a list of sample IDs of of sample data without associated spectra.
   *
   * @param conditions	the conditions that the sampledata must meet
   * @return		list of sample IDs
   */
  public List<String> getOrphanedIDs(OrphanedSampleDataConditions conditions) {
    List<String>	result;
    String		tables;
    String		where;
    ResultSet 		rs;

    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName() + ": conditions=" + conditions);

    result = new ArrayList<>();
    conditions.update();

    tables = getTableName() + " AS sd "
      + "LEFT OUTER JOIN " + SpectrumF.getSingleton(getDatabaseConnection()).getTableName() + " AS sp "
      + "ON sp.sampleid = sd.id";
    where  = "sd.name = '" + SampleData.INSERT_TIMESTAMP + "'"
      + "and sampleid is null";
    if (!conditions.getStartDate().isInfinity())
      where += " and sd.value >= '" + conditions.getStartDate().stringValue() + "'";
    if (!conditions.getEndDate().isInfinity())
      where += " and sd.value <= '" + conditions.getEndDate().stringValue() + "'";
    if (conditions.getLatest())
      where += " ORDER BY sd.value DESC";
    else
      where += " ORDER BY sd.value ASC";
    if (conditions.getLimit() > -1)
      where += " LIMIT " + conditions.getLimit();

    rs = null;
    try {
      rs = select("id", tables, where);
      while (rs.next())
	result.add(rs.getString(1));
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to get orphaned IDs: " + conditions, e);
    }
    finally {
      SQLUtils.closeAll(rs);
    }

    return result;
  }

  /**
   * Returns all the various instruments.
   *
   * @return		the instruments
   */
  public List<String> getInstruments() {
    List<String>	result;
    ResultSet		rs;

    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName());

    result = new ArrayList<>();

    rs = null;
    try {
      rs = selectDistinct("VALUE", "NAME = " + SQLUtils.backquote(SampleData.INSTRUMENT));
      while (rs.next())
	result.add(rs.getString(1));
    }
    catch (Exception e) {
      result.clear();
      getLogger().log(Level.SEVERE, "Failed to get instruments", e);
    }
    finally {
      SQLUtils.closeAll(rs);
    }

    if (result.size() > 1)
      Collections.sort(result);

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
   * Stores the records. Removes any previously existing reference values.
   *
   * @param records	the report
   * @param types	the data types to import
   * @param skipFields 	the fields to skip (regular expression), null to accept all
   * @param batchSize   the maximum number of records in one batch
   * @param autoCommit  whether to use auto-commit or not (turning off may impact other transactions!)
   * @param newConnection	uses a separate database connection just for this connection (then no auto-commit doesn't affect the rest)
   * @return		true if successfully inserted/updated
   */
  @Override
  public boolean bulkStore(SampleData[] records, DataType[] types, String skipFields, int batchSize, boolean autoCommit, boolean newConnection) {
    boolean		result;
    PreparedStatement	delete;
    PreparedStatement	insert;
    boolean		committed;
    int			i;
    int			n;
    Set<DataType>	typesSet;
    Pattern		skipPattern;
    boolean		useSameConnection;
    Connection		m_Connection;

    if (isLoggingEnabled())
      getLogger().info(LoggingHelper.getMethodName());

    m_BulkStoreStopped = false;
    useSameConnection  = true;

    if (newConnection) {
      try {
	if (getDatabaseConnection().getUser().equals(""))
	  m_Connection = DriverManager.getConnection(getDatabaseConnection().getUser());
	else
	  m_Connection = DriverManager.getConnection(getDatabaseConnection().getURL(), getDatabaseConnection().getUser(), getDatabaseConnection().getPassword().getValue());
	useSameConnection = false;
      }
      catch(Exception e) {
        getLogger().warning("Failed to open separate connection to " + getDatabaseConnection().getURL() + ", re-using existing one.");
	useSameConnection = true;
      }
    }

    if (!newConnection || useSameConnection) {
      if (!autoCommit) {
	try {
	  getDatabaseConnection().getConnection(false).setAutoCommit(false);
	}
	catch (Exception e) {
	  getLogger().log(Level.WARNING, "Failed to turn off auto-commit!", e);
	}
      }
    }

    try {
      delete = prepareStatement("DELETE FROM " + getTableName() + " WHERE ID = ? AND NAME = ?");
      insert = prepareStatement("INSERT INTO " + getTableName() + "(ID, NAME, TYPE, VALUE)  VALUES(?, ?, ?, ?)");
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to prepare statements!", e);
      return false;
    }

    result    = true;
    n         = 0;
    committed = true;
    typesSet  = new HashSet<>(Arrays.asList(types));
    if (skipFields != null)
      skipPattern = Pattern.compile(skipFields);
    else
      skipPattern = null;
    for (i = 0; i < records.length; i++) {
      for (AbstractField field: records[i].getFields()) {
        // stopped?
        if (m_BulkStoreStopped)
          break;

        // not accepted type?
	if (!typesSet.contains(field.getDataType()))
	  continue;

        // skip fields
	if ((skipPattern != null) && skipPattern.matcher(field.getName()).matches())
	  continue;

        try {
	  // delete
	  delete.setString(1, records[i].getID());
	  delete.setString(2, field.getName());
	  delete.addBatch();

	  // insert
	  insert.setString(1, records[i].getID());
	  insert.setString(2, field.getName());
	  insert.setString(3, field.getDataType().toDisplay());
	  insert.setString(4, "" + records[i].getValue(field));
	  insert.addBatch();

	  n++;
	  committed = false;
	  if (n % batchSize == 0) {
	    if (isLoggingEnabled())
	      getLogger().info(LoggingHelper.getMethodName() + ": committing batches, # records so far: " + n);
	    delete.executeBatch();
	    insert.executeBatch();
	    getDatabaseConnection().getConnection(false).commit();
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
    }

    try {
      if (!committed) {
	delete.executeBatch();
	insert.executeBatch();
      }
      delete.clearBatch();
      insert.clearBatch();
    }
    catch (Exception e) {
      // ignored
    }

    SQLUtils.close(delete);
    SQLUtils.close(insert);

    if (!autoCommit) {
      try {
	getDatabaseConnection().getConnection(false).setAutoCommit(true);
      }
      catch (Exception e) {
	getLogger().log(Level.WARNING, "Failed to turn on auto-commit!", e);
      }
    }

    return result && !m_BulkStoreStopped;
  }

  /**
   * Interrupts a currently running bulk store, if possible.
   */
  @Override
  public void stopBulkStore() {
    m_BulkStoreStopped = true;
  }

  /**
   * Returns the singleton of the table (active).
   *
   * @param dbcon	the database connection to get the singleton for
   * @return		the singleton
   */
  public static synchronized SampleDataIntf getSingleton(AbstractDatabaseConnection dbcon) {
    if (m_TableManager == null)
      m_TableManager = new TableManager<>(TABLE_NAME, dbcon.getOwner());
    if (!m_TableManager.has(dbcon))
      m_TableManager.add(dbcon, new adams.db.mysql.SampleDataT(dbcon));

    return m_TableManager.get(dbcon);
  }
}
