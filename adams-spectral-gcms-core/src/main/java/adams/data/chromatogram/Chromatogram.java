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
 * Chromatogram.java
 * Copyright (C) 2008-2015 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.data.chromatogram;

import adams.core.Constants;
import adams.core.DateFormat;
import adams.core.DateTime;
import adams.core.Properties;
import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.core.io.PlaceholderFile;
import adams.data.DatabaseNotesHandler;
import adams.data.container.AbstractDataContainer;
import adams.data.container.DataContainer;
import adams.data.container.DataPointComparator;
import adams.data.quantitation.QuantitationReport;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.MutableReportHandler;
import adams.data.statistics.ChromatogramStatistic;
import adams.data.statistics.InformativeStatisticSupporter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Stores a complete chromatogram plus reference values (if they exist).
 *
 * @author dale
 * @version $Revision: 4313 $
 */
public class Chromatogram
  extends AbstractDataContainer<GCPoint>
  implements DatabaseNotesHandler, MutableReportHandler<QuantitationReport>,
             InformativeStatisticSupporter<ChromatogramStatistic>{

  /** for serialization. */
  private static final long serialVersionUID = 1318149681899877295L;

  /** the file extension for a chromatogram (incl. dot). */
  public final static String FILE_EXTENSION = ".chrom";

  /** the notes prefix (when reading/writing chromatograms). */
  public final static String NOTES_PREFIX = "notes.";

  /** the report prefix (when reading/writing chromatograms). */
  public final static String REPORT_PREFIX = "report.";

  /** database id. */
  protected int m_DatabaseID = Constants.NO_ID;

  /** instrument. */
  protected String m_instrument;

  /** 'reference' values. */
  protected QuantitationReport m_reference;

  /** date it was inserted in the database. */
  protected Date m_date;

  /** date it was generated. */
  protected Date m_dateGeneration;

  /** dilution factor. default is no dilution.*/
  protected int m_dilution=1;

  /** duplicate sample. Has this sample been through the extraction process before? */
  protected boolean m_duplication;

  /** is this a sample 'standard'. */
  protected boolean m_standard;

  /** mspoint of greatest abundance. */
  protected GCPoint m_MaxAbundance;

  /** mspoint of smallest abundance. */
  protected GCPoint m_MinAbundance;

  /** mspoint of greatest m/z. */
  protected GCPoint m_MaxTimestamp;

  /** mspoint of smallest m/z. */
  protected GCPoint m_MinTimestamp;

  /** the notes for the chromatogram. */
  protected Notes m_Notes;

  /** the default comparator. */
  protected static DataPointComparator<GCPoint> m_Comparator;

  /**
   * Initialise data.
   */
  public Chromatogram() {
    this(null);
  }

  /**
   * Initialise data.
   *
   * @param id	ID for chromatogram
   */
  public Chromatogram(String id){
    m_ID             = id;
    m_instrument     = null;
    m_reference      = null;
    m_date           = null;
    m_dateGeneration = null;
    m_MaxAbundance   = null;
    m_MinAbundance   = null;
    m_MaxTimestamp   = null;
    m_MinTimestamp   = null;
    m_Notes          = new Notes();
    if (m_Comparator == null)
      m_Comparator = newComparator();
  }

  /**
   * Returns a new instance of the default comparator.
   *
   * @return		the comparator instance
   */
  public DataPointComparator<GCPoint> newComparator() {
    return new GCComparator();
  }

  /**
   * Returns the comparator in use.
   *
   * @return		the comparator in use
   */
  public DataPointComparator<GCPoint> getComparator() {
    return m_Comparator;
  }

  /**
   * Returns a new instance of a DataContainer point.
   *
   * @return		the new DataContainer point
   */
  public GCPoint newPoint() {
    return new GCPoint(-1, -1);
  }

  /**
   * Invalidates the min/max abundance/timestamp points.
   */
  protected synchronized void invalidateMinMax() {
    m_MinAbundance = null;
    m_MaxAbundance = null;
    m_MinTimestamp = null;
    m_MaxTimestamp = null;
  }

  /**
   * Initializes the min/max abundance/timestmap points.
   */
  protected synchronized void validateMinMax() {
    if (m_MinAbundance != null)
      return;

    for (GCPoint point: this) {
      if (    (m_MaxAbundance == null)
	  || (point.getAbundance() > m_MaxAbundance.getAbundance()) )
	m_MaxAbundance = point;
      if (    (m_MinAbundance == null)
	  || (point.getAbundance() < m_MinAbundance.getAbundance()))
	m_MinAbundance = point;
      if (    (m_MaxTimestamp == null)
	  || (point.getTimestamp() > m_MaxTimestamp.getTimestamp()) )
	m_MaxTimestamp = point;
      if (    (m_MinTimestamp == null)
	  || (point.getTimestamp() < m_MinTimestamp.getTimestamp()) )
	m_MinTimestamp = point;
    }
  }

  /**
   * Sets the ID of the chromatogram.
   *
   * @param value	the new ID
   */
  @Override
  public void setID(String value) {
    super.setID(value);

    if (m_reference != null) {
      m_reference.setID(value);
      m_reference.update();
    }
  }

  /**
   * Get duplication status.
   *
   * @return	is duplication
   */
  public boolean getDuplication(){
    return(m_duplication);
  }
  /**
   * Set whether this sample has been through extraction before.
   *
   * @param dup		duplicate sample?
   */
  public void setDuplication(boolean dup){
    m_duplication=dup;
  }

  /**
   * Get dilution factor. 1 is no dilution
   *
   * @return	dilution factor
   */
  public int getDilutionFactor(){
    return(m_dilution);
  }

  /**
   * Set dilution factor.
   *
   * @param df	dilution factor
   */
  public void setDilutionFactor(int df){
    m_dilution=df;
  }

  /**
   * Return if this is a sample 'standard'.
   *
   * @return	is a standard?
   */
  public boolean getIsStandard(){
    return(m_standard);
  }

  /**
   * Set sample standard status.
   *
   * @param stn		is a standard?
   */
  public void setIsStandard(boolean stn){
    m_standard=stn;
  }

  /**
   * Set the database ID.
   *
   * @param id	Databse ID
   */
  public void setDatabaseID(int id){
    m_DatabaseID=id;
    if (m_reference != null) {
      m_reference.setDatabaseID(id);
      m_reference.update();
    }
  }

  /**
   * Get database id.
   *
   * @return database ID
   */
  public int getDatabaseID(){
    return(m_DatabaseID);
  }

  /**
   * Set instrument name.
   * Updates the report, if available.
   *
   * @param i		the name of the instrument
   */
  public void setInstrument(String i){
    m_instrument = i;
    if ((i != null) && (m_reference != null)) {
      m_reference.setValue(new Field(QuantitationReport.FIELD_INST, DataType.STRING), i);
      m_reference.update();
    }
  }

  /**
   * Get Instrument name.
   *
   * @return 		the instrument name
   */
  public String getInstrument(){
    return(m_instrument);
  }

  /**
   * Sets the date the chromatogram was inserted in the database.
   *
   * @param d date
   */
  public void setDate(Date d){
    m_date=d;
  }

  /**
   * Gets the date the chromatogram was inserted in the database.
   *
   * @return date
   */
  public Date getDate(){
    return(m_date);
  }

  /**
   * Sets the date the chromatogram was generated.
   * Updates the report, if available.
   *
   * @param d date
   */
  public void setDateGeneration(Date d){
    DateFormat 	df;

    m_dateGeneration = d;
    if ((d != null) && (m_reference != null)) {
      df = new DateFormat(QuantitationReport.ACQON_DATE_FORMAT);
      m_reference.setValue(new Field(QuantitationReport.FIELD_ACQON, DataType.STRING), df.format(d));
      m_reference.update();
    }
  }

  /**
   * Gets the date the chromatogram was generated.
   *
   * @return date
   */
  public Date getDateGeneration(){
    return m_dateGeneration;
  }

  /**
   * Set the quantitation report (reference values) for this chromatogram.
   * Updates the report, if available (DB-ID, ID, Instrument, DateGeneration).
   *
   * @param rp	quantation report
   */
  public void setReport(QuantitationReport rp){
    m_reference=rp;
    if (m_reference != null) {
      setDatabaseID(getDatabaseID());
      if (getID() != null)
	setID(getID());
      if (getInstrument() != null)
	setInstrument(getInstrument());
      if (getDateGeneration() != null)
	setDateGeneration(getDateGeneration());
    }
  }

  /**
   * Get the quantitation report (reference values) for this chromatogram.
   *
   * @return 	quantation report
   */
  public QuantitationReport getReport(){
    return(m_reference);
  }

  /**
   * True if Quantitation Report exists for this chromatogram.
   *
   * @return does this chromatogram have a Quantitation Report
   */
  public boolean hasReport(){
    return(m_reference != null);
  }

  /**
   * Get point with greatest abundance.
   *
   * @return	gc point
   */
  public GCPoint getMaxAbundance(){
    validateMinMax();
    return m_MaxAbundance;
  }

  /**
   * Get point with smallest abundance.
   *
   * @return	gc point
   */
  public GCPoint getMinAbundance(){
    validateMinMax();
    return m_MinAbundance;
  }

  /**
   * Get point with greatest timestamp.
   *
   * @return	gc point
   */
  public GCPoint getMaxTimestamp(){
    validateMinMax();
    return m_MaxTimestamp;
  }

  /**
   * Get point with smallest Timestamp.
   *
   * @return	gc point
   */
  public GCPoint getMinTimestamp(){
    validateMinMax();
    return m_MinTimestamp;
  }

  /**
   * Method that gets notified about changes in the collection of data points.
   *
   * @param modified	whether the action modified the collection
   * @return		the same as the input
   */
  @Override
  protected boolean modifiedListener(boolean modified) {
    if (modified)
      invalidateMinMax();

    return modified;
  }

  /**
   * Returns the GCPoint with the exact timestamp, null if not found.
   *
   * @param timestamp	the timestamp to look for
   * @return		the GCPoint or null if not found
   * @see		#findClosest(long)
   */
  public GCPoint find(long timestamp) {
    GCPoint	result;
    int		index;

    result = null;

    index = ChromatogramUtils.findTimestamp(m_Points, timestamp);
    if (index > -1)
      result = m_Points.get(index);

    return result;
  }

  /**
   * Returns the GCPoint with a timestamp closest to the one provided.
   *
   * @param timestamp	the timestamp to look for
   * @return		the GCPoint
   * @see		#find(long)
   */
  public GCPoint findClosest(long timestamp) {
    GCPoint	result;
    int		index;

    result = null;

    index = ChromatogramUtils.findClosestTimestamp(m_Points, timestamp);
    if (index > -1)
      result = m_Points.get(index);

    return result;
  }

  /**
   * Returns a chromatogram which contains only the given mass-to-charge values.
   * Adds GC points with 0 abundance if a GC point does not contain the chosen
   * m/z ratio.
   *
   * @param mz		the mass-to-charge to look for
   * @return		the new chromatogram
   */
  public Chromatogram getMassChargeSubset(float mz) {
    Chromatogram 	result;
    Iterator<GCPoint>	iter;
    GCPoint		gcpoint;
    GCPoint		newGCPoint;
    MSPoint		mspoint;
    MSPoint		newMSPoint;

    result = (Chromatogram) getHeader();

    iter = iterator();
    while (iter.hasNext()) {
      gcpoint = iter.next();
      mspoint = gcpoint.find(mz);
      if (mspoint != null) {
	newGCPoint = new GCPoint(gcpoint.getTimestamp(), mspoint.getAbundance());
	newMSPoint = new MSPoint(mspoint.getMassCharge(), mspoint.getAbundance());
	newGCPoint.add(newMSPoint);
	result.add(newGCPoint);
      }
      else {
	newGCPoint = new GCPoint(gcpoint.getTimestamp(), 0);
	result.add(newGCPoint);
      }
    }

    return result;
  }

  /**
   * Obtains the stored variables from the other data point, but not the
   * actual data points.
   *
   * @param other	the data point to get the values from
   */
  @Override
  public void assign(DataContainer<GCPoint> other) {
    Chromatogram	chr;

    super.assign(other);

    chr = (Chromatogram) other;

    setDate(chr.getDate());
    setDateGeneration(chr.getDateGeneration());
    setDatabaseID(chr.getDatabaseID());
    if (chr.hasReport())
      setReport(((QuantitationReport) chr.getReport().getClone()));
    setDilutionFactor(chr.getDilutionFactor());
    setDuplication(chr.getDuplication());
    setIsStandard(chr.getIsStandard());
    setInstrument(chr.getInstrument());
    m_Notes = new Notes();
    m_Notes.mergeWith(chr.getNotes());
  }

  /**
   * Compares this object with the specified object for order.  Returns a
   * negative integer, zero, or a positive integer as this object is less
   * than, equal to, or greater than the specified object.
   *
   * @param   o the object to be compared.
   * @return  a negative integer, zero, or a positive integer as this object
   *		is less than, equal to, or greater than the specified object.
   * @throws ClassCastException if the specified object's type prevents it
   *         from being compared to this object.
   */
  @Override
  public int compareToHeader(Object o) {
    int			result;
    Chromatogram	c;

    result = super.compareToHeader(o);
    if (result != 0)
      return result;

    c = (Chromatogram) o;

    if (result == 0)
      result = Utils.compare(getDate(), c.getDate());

    if (result == 0)
      result = new Integer(getDatabaseID()).compareTo(new Integer(c.getDatabaseID()));

    if (result == 0)
      result = Utils.compare(getReport(), c.getReport());

    if (result == 0)
      result = new Integer(getDilutionFactor()).compareTo(new Integer(c.getDilutionFactor()));

    if (result == 0)
      result = new Boolean(getDuplication()).compareTo(new Boolean(c.getDuplication()));

    if (result == 0)
      result = new Boolean(getIsStandard()).compareTo(new Boolean(c.getIsStandard()));

    if (result == 0)
      result = Utils.compare(getInstrument(), c.getInstrument());

    return result;
  }

  /**
   * Indicates whether some other chromatogram's header is "equal to" this ones.
   *
   * @param obj		the reference object with which to compare.
   * @return		true if this object is the same as the obj argument;
   * 			false otherwise.
   */
  @Override
  public boolean equalsHeader(Object obj) {
    return (compareToHeader(obj) == 0);
  }

  /**
   * Returns the hashcode. Just the hashcode of the toString()-generated
   * string.
   *
   * @return		the hashcode
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * Returns a short string representation of the chromatogram.
   *
   * @return		a string representation
   */
  @Override
  public String toString() {
    String	result;

    result  = "ID=" + getID();
    result += ", DB-ID=" + getDatabaseID();
    result += ", Ins=" + getInstrument();
    result += ", # GC points=" + size();
    if (size() > 0) {
      result += ", first timestamp=" + m_Points.get(0).getTimestamp();
      result += ", last timestamp=" + m_Points.get(m_Points.size() - 1).getTimestamp();
    }

    return result;
  }

  /**
   * Returns a statistic object of this chromatogram.
   *
   * @return		statistics about this chromatogram
   */
  public ChromatogramStatistic toStatistic() {
    return new ChromatogramStatistic(this);
  }

  /**
   * Returns the currently stored notes.
   *
   * @return		the current notes
   */
  public Notes getNotes() {
    return m_Notes;
  }

  /**
   * Returns a vector with the points. Generates a new vector, use toList()
   * instead.
   *
   * @return		a vector with all the points
   */
  @Deprecated
  public Vector<GCPoint> toVector() {
    return toVector(getComparator());
  }

  /**
   * Returns a vector with the points.
   *
   * @param comparator	the comparator to use
   * @return		a vector with all the points
   */
  @Deprecated
  public Vector<GCPoint> toVector(DataPointComparator comparator) {
    Vector<GCPoint>	result;

    result = new Vector<GCPoint>(m_Points);
    Collections.sort(result, comparator);

    return result;
  }

  /**
   * Writes the chromatogramm to a file.
   *
   * @param filename	the file to write to
   * @param data	the chromatogram to write
   * @return		true if successful
   */
  public static boolean write(String filename, Chromatogram data) {
    boolean		result;
    BufferedWriter	writer;
    FileOutputStream	fos;
    FileWriter		fw;

    filename = new PlaceholderFile(filename).getAbsolutePath();
    writer   = null;
    fw       = null;
    fos      = null;
    try {
      if (filename.endsWith(".gz")) {
	fos    = new FileOutputStream(filename);
	writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(fos)));
      }
      else {
	fw     = new FileWriter(filename);
	writer = new BufferedWriter(fw);
      }
      result = write(writer, data);
      writer.flush();
    }
    catch (Exception e) {
      result = false;
      e.printStackTrace();
    }
    finally {
      FileUtils.closeQuietly(writer);
      FileUtils.closeQuietly(fw);
      FileUtils.closeQuietly(fos);
    }

    return result;
  }

  /**
   * Writes the chromatogramm to the buffered writer. The caller must
   * close the writer explicitly, as it does not happen in this method.
   *
   * @param writer	the buffered writer to write to
   * @param data	the chromatogram to write
   * @return		true if successful
   */
  public static boolean write(BufferedWriter writer, Chromatogram data) {
    boolean			result;
    Iterator<GCPoint>		gcIter;
    Properties			props;
    Properties			propsReport;
    int				i;
    Iterator<String>		notes;
    String			classname;
    List<String>		list;
    String			allClassnames;
    String[]			lines;

    result = true;

    try {
      // header
      props = new Properties();
      props.setProperty("id", data.getID());
      if (data.getInstrument() != null)
	props.setProperty("instrument", data.getInstrument());
      props.setInteger("dbid", data.getDatabaseID());
      if (data.getDate() != null)
	props.setDateTime("date", new DateTime(data.getDate()));
      if (data.getDateGeneration() != null)
	props.setDateTime("dateGeneration", new DateTime(data.getDateGeneration()));
      props.setInteger("dilution", data.getDilutionFactor());
      props.setBoolean("duplication", data.getDuplication());
      props.setBoolean("standard", data.getIsStandard());

      // quantitation report
      if (data.hasReport()) {
	propsReport = data.getReport().toProperties();
	props.add(propsReport, "report.");
      }

      // notes
      notes         = data.getNotes().notes();
      allClassnames = "";
      while (notes.hasNext()) {
	classname = notes.next();
	if (allClassnames.length() > 0)
	  allClassnames += ",";
	allClassnames += classname;
	list = data.getNotes().getNotes(classname);
	props.setInteger("notes." + classname + ".count", list.size());
	for (i = 0; i < list.size(); i++)
	  props.setProperty("notes." + classname + "." + i, list.get(i));
      }
      props.setProperty("notes.classnames", allClassnames);

      lines = props.toComment().split("\n");
      Arrays.sort(lines);
      writer.write(Utils.flatten(lines, "\n"));
      writer.write("\n\n");

      // data
      gcIter = data.toList().iterator();
      while (gcIter.hasNext())
	GCPoint.write(writer, gcIter.next());

      writer.flush();
    }
    catch (Exception e) {
      result = false;
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Reads a chromatogram from a text file.
   *
   * @param filename	the file to read
   * @return		the chromatogram or null in case of an error
   */
  public static Chromatogram read(String filename) {
    Chromatogram	result;
    BufferedReader	reader;
    FileInputStream	fis;
    FileReader		fr;

    filename = new PlaceholderFile(filename).getAbsolutePath();
    fis      = null;
    fr       = null;
    reader   = null;
    try {
      if (filename.endsWith(".gz")) {
	fis    = new FileInputStream(filename);
	reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(fis)));
      }
      else {
	fr     = new FileReader(filename);
	reader = new BufferedReader(fr);
      }
      result = read(reader);
    }
    catch (Exception e) {
      e.printStackTrace();
      result = null;
    }
    finally {
      FileUtils.closeQuietly(reader);
      FileUtils.closeQuietly(fis);
      FileUtils.closeQuietly(fr);
    }

    return result;
  }

  /**
   * Reads a chromatogram from a buffered reader. The caller must explicitly
   * close the reader, as it does not happen in this method.
   *
   * @param reader	the buffered reader to read from
   * @return		the chromatogram or null in case of an error
   */
  public static Chromatogram read(BufferedReader reader) {
    Chromatogram	result;
    String		line;
    Vector<String>	comments;
    boolean		prolog;
    StringReader	propsReader;
    GCPoint		gcpoint;
    Properties		props;
    Properties		propsReport;
    Enumeration		enm;
    String		key;
    String[]		classnames;
    int			i;
    int			n;
    int			count;
    QuantitationReport	report;

    result = null;

    try {
      // header
      comments = new Vector<String>();
      do {
	line   = reader.readLine();
	prolog = (line != null) && (line.length() > 0);
	if (prolog)
	  comments.add(line);
      }
      while (prolog);
      // old format
      if (comments.size() == 1) {
	propsReader = new StringReader(Utils.unbackQuoteChars(comments.get(0)));
	props  = new Properties();
	props.load(propsReader);
      }
      // new format
      else {
	props = Properties.fromComment(Utils.flatten(comments, "\n"));
      }
      result = new Chromatogram(props.getProperty("id"));
      result.setDatabaseID(props.getInteger("dbid"));
      result.setDate(props.getDateTime("date"));
      result.setDateGeneration(props.getDateTime("dateGeneration"));
      result.setDilutionFactor(props.getInteger("dilution"));
      result.setDuplication(props.getBoolean("duplication"));
      result.setIsStandard(props.getBoolean("standard"));
      result.setInstrument(props.getProperty("instrument"));

      // report
      propsReport = new Properties();
      enm = props.propertyNames();
      while (enm.hasMoreElements()) {
	key = (String) enm.nextElement();
	if (key.startsWith("report."))
	  propsReport.setProperty(key.substring(7), props.getProperty(key));
      }
      report = new QuantitationReport();
      report.assign(QuantitationReport.parseProperties(propsReport));
      result.setReport(report);

      // notes
      classnames = props.getProperty("notes.classnames", "").split(",");
      for (i = 0; i < classnames.length; i++) {
	count = props.getInteger("notes." + classnames[i] + ".count", 0);
	for (n = 0; n < count; n++)
	  result.getNotes().addNote(
	      classnames[i],
	      props.getProperty("notes." + classnames[i] + "." + n, ""));
      }

      //line = reader.readLine();

      // data
      while ((gcpoint = GCPoint.read(reader)) != null)
	result.add(gcpoint);
    }
    catch (Exception e) {
      result = null;
      e.printStackTrace();
    }

    return result;
  }
}
