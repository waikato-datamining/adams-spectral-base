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
 * Spectrum.java
 * Copyright (C) 2009-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrum;

import adams.core.Constants;
import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.core.io.PlaceholderFile;
import adams.data.DatabaseNotesHandler;
import adams.data.Notes;
import adams.data.container.AbstractDataContainer;
import adams.data.container.DataContainer;
import adams.data.container.DataPoint;
import adams.data.container.DataPointComparator;
import adams.data.id.MutableDatabaseIDHandler;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.MutableReportHandler;
import adams.data.sampledata.SampleData;
import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.spreadsheet.SpreadSheetSupporter;
import adams.data.statistics.InformativeStatisticSupporter;
import adams.data.statistics.SpectrumStatistic;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

/**
 * Abstract superclass for containers for sequence points.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 12579 $
 */
public class Spectrum
  extends AbstractDataContainer<SpectrumPoint>
  implements DataPoint, DatabaseNotesHandler, MutableDatabaseIDHandler, 
             MutableReportHandler<SampleData>, SpreadSheetSupporter,
             InformativeStatisticSupporter<SpectrumStatistic>{

  /** for serialization. */
  private static final long serialVersionUID = -2239372693204149173L;

  /** the file extension. */
  public final static String FILE_EXTENSION = ".spec";

  /** the separator for multiple spectra in a single file. */
  public final static String SEPARATOR = "---";

  /** the database ID. */
  protected int m_DatabaseID;

  /** Sample Data. */
  protected SampleData m_SampleData;

  /** the notes for the chromatogram. */
  protected Notes m_Notes;

  /** point of greatest amplitude. */
  protected SpectrumPoint m_MaxAmplitude;

  /** point of smallest amplitude. */
  protected SpectrumPoint m_MinAmplitude;

  /** point of greatest wave number. */
  protected SpectrumPoint m_MaxWaveNumber;

  /** point of smallest wave number. */
  protected SpectrumPoint m_MinWaveNumber;

  /** the parent. */
  protected DataContainer m_Parent;
  
  /** the default comparator. */
  protected static DataPointComparator<SpectrumPoint> m_Comparator;
  

  /**
   * Initializes the container.
   */
  public Spectrum() {
    super();

    m_Points        = new ArrayList<>();
    m_ID            = "" + new Date();
    m_DatabaseID    = Constants.NO_ID;
    m_SampleData    = null;
    m_Notes         = new Notes();
    m_MinAmplitude  = null;
    m_MaxAmplitude  = null;
    m_MinWaveNumber = null;
    m_MaxWaveNumber = null;
    m_Parent        = null;
    if (m_Comparator == null)
      m_Comparator = newComparator();
    setReport(new SampleData());
  }

  /**
   * Returns the hash code for this DataContainer.
   *
   * @return		the hash code
   */
  @Override
  public int hashCode() {
    return new String(getID() + " " + getFormat() + super.hashCode()).hashCode();
  }

  /**
   * Method that gets notified about changes in the collection of data points.
   * Just passes the modified state through.
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
   * Returns a new instance of the default comparator.
   *
   * @return		the comparator instance
   */
  public DataPointComparator<SpectrumPoint> newComparator() {
    return new SpectrumPointComparator();
  }

  /**
   * Returns the comparator in use.
   *
   * @return		the comparator in use
   */
  public DataPointComparator<SpectrumPoint> getComparator() {
    return m_Comparator;
  }

  /**
   * Sets the container this point belongs to.
   *
   * @param value	the container
   */
  @Override
  public void setParent(DataContainer value) {
    m_Parent = value;
  }

  /**
   * Returns the container this point belongs to.
   *
   * @return		the container, can be null
   */
  @Override
  public DataContainer getParent() {
    return m_Parent;
  }

  /**
   * Returns whether the point belongs to a container.
   *
   * @return		true if the point belongs to a container
   */
  @Override
  public boolean hasParent() {
    return (m_Parent != null);
  }

  /**
   * Invalidates the min/max amplitude/wavenumber points.
   */
  protected synchronized void invalidateMinMax() {
    m_MinAmplitude  = null;
    m_MaxAmplitude  = null;
    m_MinWaveNumber = null;
    m_MaxWaveNumber = null;
  }

  /**
   * Initializes the min/max amplitude/wavenumber points.
   */
  protected synchronized void validateMinMax() {
    if (m_MinAmplitude != null)
      return;

    for (SpectrumPoint point: this) {
      if (    (m_MaxAmplitude == null)
	   || (point.getAmplitude() > m_MaxAmplitude.getAmplitude()) )
	m_MaxAmplitude = point;
      if (    (m_MinAmplitude == null)
	   || (point.getAmplitude() < m_MinAmplitude.getAmplitude()))
	m_MinAmplitude = point;
      if (    (m_MaxWaveNumber == null)
	   || (point.getWaveNumber() > m_MaxWaveNumber.getWaveNumber()) )
	m_MaxWaveNumber = point;
      if (    (m_MinWaveNumber == null)
 	   || (point.getWaveNumber() < m_MinWaveNumber.getWaveNumber()) )
	m_MinWaveNumber = point;
    }
  }

  /**
   * Get point with greatest amplitude.
   *
   * @return	the spectrum point
   */
  public SpectrumPoint getMaxAmplitude(){
    validateMinMax();
    return m_MaxAmplitude;
  }

  /**
   * Get point with smallest amplitude.
   *
   * @return	the spectrum point
   */
  public SpectrumPoint getMinAmplitude(){
    validateMinMax();
    return m_MinAmplitude;
  }

  /**
   * Get point with greatest wave number.
   *
   * @return	the spectrum point
   */
  public SpectrumPoint getMaxWaveNumber(){
    validateMinMax();
    return m_MaxWaveNumber;
  }

  /**
   * Get point with smallest wave number.
   *
   * @return	the spectrum point
   */
  public SpectrumPoint getMinWaveNumber(){
    validateMinMax();
    return m_MinWaveNumber;
  }

  /**
   * Returns whether sample data is present.
   *
   * @return		true if sample data present
   */
  public boolean hasReport() {
    return (m_SampleData != null);
  }

  /**
   * Set sample data. Also sets the database ID and sample ID in the sample
   * data (without "'").
   *
   * @param value		the sample data
   */
  public void setReport(SampleData value){
    m_SampleData = value;
    m_SampleData.setDatabaseID(getDatabaseID());
    m_SampleData.setID(getID().replace("'", ""));
  }

  /**
   * get sample data.
   *
   * @return		the sample data
   */
  public SampleData getReport(){
    return m_SampleData;
  }

  /**
   * Sets the database ID of the sequence.
   *
   * @param value	the new database ID
   */
  public void setDatabaseID(int value) {
    m_DatabaseID = value;
  }

  /**
   * Returns the database ID of the sequence.
   *
   * @return		the database ID
   */
  public int getDatabaseID() {
    return m_DatabaseID;
  }

  /**
   * Sets the ID of the spectrum.
   *
   * @param value	the new ID
   */
  @Override
  public void setID(String value) {
    super.setID(value);

    if (m_SampleData != null)
      m_SampleData.setID(value);
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
   * Sets the data format (always converted to upper case). Use null to set
   * default format.
   *
   * @param value	the data format
   */
  public void setFormat(String value) {
    if (hasReport()) {
      if (value == null)
	getReport().addParameter(SampleData.FORMAT, SampleData.DEFAULT_FORMAT);
      else
	getReport().addParameter(SampleData.FORMAT, value.toUpperCase());
    }
  }

  /**
   * Returns the data format.
   *
   * @return		the data format
   */
  public String getFormat() {
    Field	field;

    field = new Field(SampleData.FORMAT, DataType.STRING);

    if (hasReport() && getReport().hasValue(field))
      return (String) getReport().getValue(field);
    else
      return SampleData.DEFAULT_FORMAT;
  }

  /**
   * Obtains the stored variables from the other data point, but not the
   * actual data points.
   *
   * @param other	the data point to get the values from
   */
  @Override
  public void assign(DataContainer<SpectrumPoint> other) {
    Spectrum	sp;

    super.assign(other);

    sp = (Spectrum) other;

    setDatabaseID(sp.getDatabaseID());
    if (sp.hasReport())
      setReport((SampleData) sp.getReport().getClone());
    m_Notes = new Notes();
    m_Notes.mergeWith(sp.getNotes());
  }

  /**
   * Obtains the stored variables from the other data point.
   *
   * @param other	the data point to get the values from
   */
  @Override
  public void assign(DataPoint other) {
    if (other instanceof Spectrum)
      assign((DataContainer<SpectrumPoint>) other);
  }

  /**
   * Returns a spectrum with just the header information, but no spectrum
   * points.
   *
   * @return		the new header
   */
  @Override
  public Spectrum getHeader() {
    Spectrum	result;

    result = new Spectrum();
    result.assign((DataContainer<SpectrumPoint>) this);

    return result;
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
    int		result;
    Spectrum	sp;

    result = super.compareToHeader(o);
    if (result != 0)
      return result;

    sp = (Spectrum) o;

    result = Utils.compare(getFormat(), sp.getFormat());

    if (result == 0)
      result = new Integer(getDatabaseID()).compareTo(sp.getDatabaseID());

    if (result == 0)
      result = Utils.compare(getReport(), sp.getReport());

    return result;
  }

  /**
   * Returns a statistic object of this spectrum.
   *
   * @return		statistics about this spectrum
   */
  public SpectrumStatistic toStatistic() {
    return new SpectrumStatistic(this);
  }

  /**
   * Returns a new instance of a sequence point.
   *
   * @return		the new sequence point
   */
  public SpectrumPoint newPoint() {
    return new SpectrumPoint();
  }

  /**
   * Returns the file header.
   *
   * @return		the header
   */
  public String getFileHeader() {
    return "waveno,amplitude";
  }

  /**
   * Writes its content to the given file. Does not output the report.
   *
   * @param filename	the file to write to
   * @return		true if successfully written
   */
  public boolean write(String filename) {
    return write(filename, false);
  }

  /**
   * Writes its content to the given file.
   *
   * @param filename	the file to write to
   * @param report	whether to output the report as well
   * @return		true if successfully written
   */
  public boolean write(String filename, boolean report) {
    boolean		result;
    BufferedWriter	writer;
    FileOutputStream    fos;
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
      result = write(writer, report);
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
   * Writes its content with the given writer. Does not output the report.
   *
   * @param writer	the writer to use
   * @return		true if successfully written
   */
  public boolean write(BufferedWriter writer) {
    return write(writer, false);
  }

  /**
   * Writes its content with the given writer.
   *
   * @param writer	the writer to use
   * @param report	whether to output the report as well
   * @return		true if successfully written
   */
  public boolean write(BufferedWriter writer, boolean report) {
    boolean			result;
    Iterator<SpectrumPoint>	iter;
    String[]			lines;

    try {
      // report?
      if (hasReport() && report) {
	lines = getReport().toProperties().toComment().split("\n");
	Arrays.sort(lines);
	writer.write(Utils.flatten(lines, "\n"));
	writer.write("\n");
      }

      iter = iterator();
      if (iter.hasNext()) {
	// header
	writer.write(getFileHeader());
	writer.write("\n");

	// data points
	while (iter.hasNext()) {
	  writer.write(iter.next().toString());
	  writer.write("\n");
	}
      }
      
      writer.flush();

      result = true;
    }
    catch (Exception e) {
      result = false;
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Returns the SpectrumPoint with the exact wave number, null if not found.
   *
   * @param waveno	the wave number to look for
   * @return		the SpectrumPoint or null if not found
   */
  public SpectrumPoint find(float waveno) {
    SpectrumPoint	result;
    int		index;

    result = null;

    index = SpectrumUtils.findWaveNumber(m_Points, waveno);
    if (index > -1)
      result = m_Points.get(index);

    return result;
  }

  /**
   * Returns the SpectrumPoint with a wave number closest to the one provided.
   *
   * @param waveno	the wave number to look for
   * @return		the SpectrumPoint
   */
  public SpectrumPoint findClosest(float waveno) {
    SpectrumPoint	result;
    int			index;

    result = null;

    index = SpectrumUtils.findClosestWaveNumber(m_Points, waveno);
    if (index > -1)
      result = m_Points.get(index);

    return result;
  }

  /**
   * Returns the content as spreadsheet.
   * 
   * @return		the content
   */
  public SpreadSheet toSpreadSheet() {
    SpreadSheet		result;
    Row			row;
    int			i;
    SpectrumPoint	point;
    
    result = new DefaultSpreadSheet();
    if (hasReport()) {
      result.addComment(getReport().toString());
    }
    else {
      result.addComment(getID());
      result.addComment(getFormat());
    }
    
    // header
    row = result.getHeaderRow();
    row.addCell("W").setContent("WaveNumber");
    row.addCell("A").setContent("Amplitude");
    
    // data
    for (i = 0; i < size(); i++) {
      point = toList().get(i);
      row   = result.addRow();
      row.addCell("W").setContent(point.getWaveNumber());
      row.addCell("A").setContent(point.getAmplitude());
    }
    
    return result;
  }

  /**
   * Returns a string representation of the sequence.
   *
   * @return		the string representation
   */
  @Override
  public String toString() {
    return "DB-ID=" + getDatabaseID() + ", SampleID=" + getID() + ", Format=" + getFormat() + ", #points=" + size();
  }
}
