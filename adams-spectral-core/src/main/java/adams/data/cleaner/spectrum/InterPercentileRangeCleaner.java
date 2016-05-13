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
 * InterPercentileRangeCleaner.java
 * Copyright (C) 2009-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.data.cleaner.spectrum;

import adams.core.logging.LoggingObject;
import adams.data.filter.Filter;
import adams.data.report.AbstractField;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.statistics.Percentile;
import adams.data.statistics.StatUtils;
import adams.db.AbstractConditions;
import adams.db.AbstractDatabaseConnection;
import adams.db.AbstractSpectrumConditions;
import adams.db.Conditions;
import adams.db.DatabaseConnection;
import adams.db.DatabaseConnectionHandler;
import adams.db.SampleDataT;
import adams.db.SpectrumConditionsMulti;
import adams.db.SpectrumT;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

/**
 <!-- globalinfo-start -->
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1913 $
 */
public class InterPercentileRangeCleaner
  extends AbstractSerializableCleaner
  implements DatabaseConnectionHandler {

  /** for serialization. */
  private static final long serialVersionUID = 8506630100168216828L;

  /**
   * Container class for storing the lower and upper percentile, as well as
   * the wave number.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 1913 $
   */
  public static class InterPercentileRange
    implements Serializable, Comparable {

    /** for serialization. */
    private static final long serialVersionUID = -4914338394436675245L;

    /** wave number or field. */
    protected Comparable m_ID;

    /** lower percentile. */
    protected double m_LowerPercentile;

    /** upper percentile. */
    protected double m_UpperPercentile;

    /**
     * Initializes the container.
     *
     * @param id	the wave number or field
     * @param lower	the lower percentile
     * @param upper	the upper percentile
     */
    public InterPercentileRange(Comparable id, double lower, double upper) {
      m_ID              = id;
      m_LowerPercentile = lower;
      m_UpperPercentile = upper;
    }

    /**
     * Returns the ID.
     *
     * @return		the wave number or field
     */
    public Comparable getID() {
      return m_ID;
    }

    /**
     * Returns the lower percentile.
     *
     * @return		the lower percentile
     */
    public double getLowerPercentile() {
      return m_LowerPercentile;
    }

    /**
     * Returns the upper percentile.
     *
     * @return		the upper percentile
     */
    public double getUpperPercentile() {
      return m_UpperPercentile;
    }

    /**
     * Returns the range between the upper and lower percentile.
     *
     * @return		the range
     */
    public double getRange() {
      return m_UpperPercentile - m_LowerPercentile;
    }

    /**
     * Compares itself with the specified object. Only compares the wave number.
     * Returns a negative integer, zero, or a positive integer as this object is
     * less than, equal to, or greater than the specified object.
     *
     * @param o		the object to compare with
     * @return		-1 if less than, 0 if equal, +1 if greated than the
     * 			specified object. +1 if specified object is null.
     */
    public int compareTo(Object o) {
      InterPercentileRange	ipr;

      if (o == null)
	return +1;

      if (!(o instanceof InterPercentileRange))
	return -1;

      ipr = (InterPercentileRange) o;

      return getID().compareTo(ipr.getID());
    }

    /**
     * Returns whether two objects are the same.
     *
     * @param o		the object to compare with
     * @return		true if the same (wave number)
     */
    @Override
    public boolean equals(Object o) {
      return (compareTo(o) == 0);
    }

    /**
     * Returns a string representation of the container.
     *
     * @return		a string representation
     */
    @Override
    public String toString() {
      return "id=" + m_ID + ", lp=" + m_LowerPercentile + ", up=" + m_UpperPercentile;
    }
  }

  /**
   * A helper class for collecting the data from the database and computing
   * the inter-percentile ranges (IPRs).
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 1913 $
   */
  public static class DataCollector
    extends LoggingObject {

    /** for serialization. */
    private static final long serialVersionUID = 5195103855036692899L;

    /** the spectrum IDs to collected the data from. */
    protected List<Integer> m_IDs;

    /** the chunk size. */
    protected int m_ChunkSize;

    /** the lower percentile. */
    protected double m_LowerPercentile;

    /** the upper percentile. */
    protected double m_UpperPercentile;

    /** the inter-percentile ranges for the amplitudes (wavenumber - IPR). */
    protected Hashtable<Float,InterPercentileRange> m_AmplitudeRanges;

    /** the inter-percentile ranges for the fields (field - IPR). */
    protected Hashtable<Field,InterPercentileRange> m_FieldRanges;

    /** the inter-percentile ranges for the amplitudes (list of wavenumber - lower/upper percentile) per chunk. */
    protected List<Hashtable<Float,Float[]>> m_AmplitudeRangesPerChunk;

    /** the inter-percentile ranges for the fields (field - lower/upper percentile) per chunk. */
    protected List<Hashtable<Field,Float[]>> m_FieldRangesPerChunk;

    /** the wave numbers that were encountered. */
    protected HashSet<Float> m_WaveNumbers;

    /** the fields that were encountered. */
    protected HashSet<Field> m_Fields;

    /** the database connection. */
    protected AbstractDatabaseConnection m_DatabaseConnection;

    /**
     * Initializes the data collector.
     *
     * @param IDs		the IDs to use
     * @param lowerPercentile	the lower percentile to retrieve
     * @param upperPercentile	the upper percentile to retrieve
     * @param chunkSize		the size of the chunks
     */
    public DataCollector(AbstractDatabaseConnection dbcon, List<Integer> IDs, double lowerPercentile, double upperPercentile, int chunkSize) {
      m_DatabaseConnection = dbcon;
      m_AmplitudeRanges    = new Hashtable<Float,InterPercentileRange>();
      m_FieldRanges        = new Hashtable<Field,InterPercentileRange>();
      m_IDs                = new ArrayList<>(IDs);
      m_ChunkSize          = chunkSize;
      m_LowerPercentile    = lowerPercentile;
      m_UpperPercentile    = upperPercentile;
    }

    /**
     * Returns the IDs used for data collection.
     *
     * @return		the IDs
     */
    public List<Integer> getIDs() {
      return new ArrayList<>(m_IDs);
    }

    /**
     * Returns the inter-percentile ranges for the amplitudes.
     *
     * @return		the ranges
     */
    public Hashtable<Float,InterPercentileRange> getAmplitudeRanges() {
      return new Hashtable<Float,InterPercentileRange>(m_AmplitudeRanges);
    }

    /**
     * Returns the ranges for the fields.
     *
     * @return		the ranges
     */
    public Hashtable<Field,InterPercentileRange> getFieldRanges() {
      return new Hashtable<Field,InterPercentileRange>(m_FieldRanges);
    }

    /**
     * Returns the size of the chunks being used.
     *
     * @return		the size
     */
    public int getChunkSize() {
      return m_ChunkSize;
    }

    /**
     * Returns the lower percentile.
     *
     * @return		the percentile
     */
    public double getLowerPercentile() {
      return m_LowerPercentile;
    }

    /**
     * Returns the upper percentile.
     *
     * @return		the percentile
     */
    public double getUpperPercentile() {
      return m_UpperPercentile;
    }

    /**
     * Returns the SpectrumT singleton to use.
     *
     * @return		the table singleton to use
     */
    protected SpectrumT getSpectrumT() {
      return SpectrumT.getSingleton(m_DatabaseConnection);
    }

    /**
     * Collects a single chunk.
     *
     * @param fromIndex		the start index (incl.)
     * @param toIndex		the end index (incl.)
     * @return			an array of length two, storing the collected
     * 				ranges for the amplitudes and the fields
     */
    protected Object[] collectChunk(int fromIndex, int toIndex) {
      Object[]						result;
      Spectrum						sp;
      int						i;
      int						n;
      SpectrumT						table;
      List<SpectrumPoint>				points;
      SpectrumPoint					point;
      List<AbstractField>				fields;
      AbstractField					field;
      Hashtable<Float,Percentile<Float>>		rangesAmpl;
      Hashtable<AbstractField,Percentile<Float>>	rangesField;

      result      = new Object[2];
      table       = getSpectrumT();
      rangesAmpl  = null;
      rangesField = null;

      for (i = fromIndex; i <= toIndex; i++) {
	sp = table.load(m_IDs.get(i));

	// init hashtables
	if (i == fromIndex) {
	  rangesAmpl  = new Hashtable<Float,Percentile<Float>>(sp.size());
	  rangesField = new Hashtable<AbstractField,Percentile<Float>>();
	  result[0]   = rangesAmpl;
	  result[1]   = rangesField;
	}

	// spectrum
	points = sp.toList();
	for (n = 0; n < points.size(); n++) {
	  point = points.get(n);
	  if (!rangesAmpl.containsKey(point.getWaveNumber()))
	    rangesAmpl.put(point.getWaveNumber(), new Percentile<Float>());
	  rangesAmpl.get(point.getWaveNumber()).add(point.getAmplitude());
	}

	// fields
	if (sp.hasReport()) {
	  fields = sp.getReport().getFields();
	  for (n = 0; n < fields.size(); n++) {
	    field = fields.get(n);
	    if (field.getDataType() != DataType.NUMERIC)
	      continue;
	    if (!rangesField.containsKey(field))
	      rangesField.put(field, new Percentile<Float>());
	    rangesField.get(field).add(sp.getReport().getDoubleValue(field).floatValue());
	  }
	}
      }

      return result;
    }

    /**
     * Collects the data in chunks and computes the ranges based on that
     * (picks the median).
     */
    protected void collectChunks() {
      int					start;
      int					end;
      boolean					first;
      Object[]					chunk;
      Hashtable<Float,Percentile<Float>>	amplitudes;
      Hashtable<Field,Percentile<Float>>	fields;
      Hashtable<Float,Float[]>			percAmpl;
      Hashtable<Field,Float[]>			percField;
      Enumeration<Float>			enmFloat;
      Float					keyFloat;
      Enumeration<Field>			enmField;
      Field					keyField;

      m_AmplitudeRangesPerChunk = new ArrayList<>();
      m_FieldRangesPerChunk     = new ArrayList<>();

      start = -m_ChunkSize;
      end   = 0;
      first = true;
      while (end < m_IDs.size() - 1) {
	start += m_ChunkSize;
	end    = start + m_ChunkSize - 1;
	if (end >= m_IDs.size())
	  end = m_IDs.size() - 1;

	// discard last non-full chunk (as long as it is no the only one!)
	if (end - start + 1 < m_ChunkSize) {
	  if (first)
	    getLogger().severe("Only one chunk collected, which is incomplete!");
	  else
	    getLogger().severe("Discarded last chunk, as it was incomplete!");
	  if (!first)
	    break;
	}

	chunk = collectChunk(start, end);

	// amplitudes
	amplitudes = (Hashtable<Float,Percentile<Float>>) chunk[0];
	percAmpl   = new Hashtable<Float,Float[]>();
	m_AmplitudeRangesPerChunk.add(percAmpl);
	enmFloat  = amplitudes.keys();
	while (enmFloat.hasMoreElements()) {
	  keyFloat = enmFloat.nextElement();
	  m_WaveNumbers.add(keyFloat);
	  percAmpl.put(
	      keyFloat,
	      new Float[]{
		  amplitudes.get(keyFloat).getPercentile(m_LowerPercentile),
		  amplitudes.get(keyFloat).getPercentile(m_UpperPercentile)});
	}

	// fields
	fields     = (Hashtable<Field,Percentile<Float>>) chunk[1];
	percField  = new Hashtable<Field,Float[]>();
	m_FieldRangesPerChunk.add(percField);
	enmField  = fields.keys();
	while (enmField.hasMoreElements()) {
	  keyField = enmField.nextElement();
	  m_Fields.add(keyField);
	  percField.put(
	      keyField,
	      new Float[]{
		  fields.get(keyField).getPercentile(m_LowerPercentile),
		  fields.get(keyField).getPercentile(m_UpperPercentile)});
	}

	first = false;
      }
    }

    /**
     * Starts the collection of the data.
     */
    public void collect() {
      double[]		lower;
      double[]		upper;
      int		i;
      int		n;
      List<Float>	waveNumbers;
      Float		waveNumber;
      List<Field>	fields;
      Field		field;

      m_WaveNumbers = new HashSet<Float>();
      m_Fields      = new HashSet<Field>();
      m_AmplitudeRanges.clear();
      m_FieldRanges.clear();

      collectChunks();

      // amplitudes
      waveNumbers = new ArrayList<>(m_WaveNumbers);
      for (i = 0; i < waveNumbers.size(); i++) {
	waveNumber = waveNumbers.get(i);
	lower      = new double[m_AmplitudeRangesPerChunk.size()];
	upper      = new double[m_AmplitudeRangesPerChunk.size()];
	for (n = 0; n < m_AmplitudeRangesPerChunk.size(); n++) {
	  lower[n] = m_AmplitudeRangesPerChunk.get(n).get(waveNumber)[0];
	  upper[n] = m_AmplitudeRangesPerChunk.get(n).get(waveNumber)[1];
	}
	Arrays.sort(lower);
	Arrays.sort(upper);
	m_AmplitudeRanges.put(
	    waveNumber,
	    new InterPercentileRange(
		waveNumber,
		StatUtils.median(lower),
		StatUtils.median(upper)));
      }

      // fields
      fields = new ArrayList<>(m_Fields);
      for (i = 0; i < fields.size(); i++) {
	field = fields.get(i);
	lower = new double[m_FieldRangesPerChunk.size()];
	upper = new double[m_FieldRangesPerChunk.size()];
	for (n = 0; n < m_FieldRangesPerChunk.size(); n++) {
	  lower[n] = m_FieldRangesPerChunk.get(n).get(field)[0];
	  upper[n] = m_FieldRangesPerChunk.get(n).get(field)[1];
	}
	Arrays.sort(lower);
	Arrays.sort(upper);
	m_FieldRanges.put(
	    field,
	    new InterPercentileRange(
		field,
		StatUtils.median(lower),
		StatUtils.median(upper)));
      }
    }
  }

  /** the chunk size when retrieving spectra from the database. */
  protected int m_ChunkSize;

  /** the spectrum retrieval conditions. */
  protected AbstractSpectrumConditions m_Conditions;

  /** the maximum factor a value can be above the upper percentile or below the
   * lower percentile before considered "un-clean". */
  protected double m_Factor;

  /** the lower percentile. */
  protected double m_LowerPercentile;

  /** the upper percentile. */
  protected double m_UpperPercentile;

  /** the interpercentiles to use for the amplitudes. */
  protected Hashtable<Float,InterPercentileRange> m_AmplitudeIPRs;

  /** the interpercentiles to use for the fields. */
  protected Hashtable<Field,InterPercentileRange> m_FieldIPRs;

  /** the sample type regular expression. */
  protected String m_SampleType;

  /** the database connection. */
  protected AbstractDatabaseConnection m_DatabaseConnection;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "Generates inter-percentile ranges based on data obtained from the "
      + "database. In order to cope with the amounts of data, the data is "
      + "read in chunks and the inter-percentile ranges computed on these "
      + "chunks. As final inter-percentile ranges, the median is used.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "chunk-size", "chunkSize",
	    1000);

    m_OptionManager.add(
	    "conditions", "conditions",
	    Conditions.getSingleton().getDefault(new SpectrumConditionsMulti()));

    m_OptionManager.add(
	    "lower", "lowerPercentile",
	    0.25);

    m_OptionManager.add(
	    "upper", "upperPercentile",
	    0.75);

    m_OptionManager.add(
	    "factor", "factor",
	    3.0);
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_AmplitudeIPRs      = null;
    m_FieldIPRs          = null;
    m_SampleType         = null;
    m_DatabaseConnection = DatabaseConnection.getSingleton();
  }

  /**
   * Regenerates all the objects that are necessary for serialization.
   */
  public void initSerializationSetup() {
    DataCollector	collector;

    collector = new DataCollector(
			getDatabaseConnection(),
			getSampleDataT().getDBIDsOfReference(m_Conditions),
			m_LowerPercentile,
			m_UpperPercentile,
			m_ChunkSize);

    collector.collect();

    m_SampleType    = m_Conditions.getSampleTypeRegExp().getValue();
    m_AmplitudeIPRs = collector.getAmplitudeRanges();
    m_FieldIPRs     = collector.getFieldRanges();

    collector = null;
  }

  /**
   * Returns the member variables to serialize to a file.
   *
   * @return		the objects to serialize
   */
  public Object[] retrieveSerializationSetup() {
    return new Object[]{
	m_PreFilter,
	m_AmplitudeIPRs,
	m_FieldIPRs,
	m_SampleType};
  }

  /**
   * Updates the member variables with the provided objects obtained from
   * deserialization.
   *
   * @param value	the deserialized objects
   */
  public void setSerializationSetup(Object[] value) {
    m_PreFilter     = (Filter) value[0];
    m_AmplitudeIPRs = (Hashtable<Float,InterPercentileRange>) value[1];
    m_FieldIPRs     = (Hashtable<Field,InterPercentileRange>) value[2];
    m_SampleType    = (String) value[3];
  }

  /**
   * Sets the chunk size to use for loading data from the database.
   *
   * @param value	the chunk size
   */
  public void setChunkSize(int value) {
    m_ChunkSize = value;
    reset();
  }

  /**
   * Returns the chunk size to use for loading data from the database.
   *
   * @return		the chunk size
   */
  public int getChunkSize() {
    return m_ChunkSize;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String chunkSizeTipText() {
    return "The size of the chunks of data to retrieve from the database.";
  }

  /**
   * Sets the conditions container to use for retrieving the spectra.
   *
   * @param value 	the conditions
   */
  public void setConditions(AbstractConditions value) {
    if (value instanceof AbstractSpectrumConditions) {
      m_Conditions = (AbstractSpectrumConditions) value;
      reset();
    }
    else {
      getLogger().severe(
	  "Only " + AbstractSpectrumConditions.class.getName() + " derived containers are allowed!");
    }
  }

  /**
   * Returns the conditions container to use for retrieving the spectra.
   *
   * @return 		the conditions
   */
  public AbstractConditions getConditions() {
    return m_Conditions;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String conditionsTipText() {
    return "The conditions for retrieving the spectra from the database.";
  }

  /**
   * Sets the factor to multiply the inter-percentile range with.
   *
   * @param value	the factor
   */
  public void setFactor(double value){
    m_Factor = value;
    reset();
  }

  /**
   * Returns the factor to multiply the inter-percentile range with.
   *
   * @return		the factor
   */
  public double getFactor(){
    return m_Factor;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String factorTipText() {
    return
        "A value X is considered un-clean, if it doesn't fullfil the following condition:\n"
      + "LowerPercentile - IPR*factor <= X <= UpperPercentile + IPR*factor";
  }

  /**
   * Sets the lower percentile.
   *
   * @param value	the percentile
   */
  public void setLowerPercentile(double value){
    m_LowerPercentile = value;
    reset();
  }

  /**
   * Returns the lower percentile.
   *
   * @return		the percentile
   */
  public double getLowerPercentile(){
    return m_LowerPercentile;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String lowerPercentileTipText() {
    return "The lower percentile; use 0.25 for obtaining the 1st quartile.";
  }

  /**
   * Sets the upper percentile.
   *
   * @param value	the percentile
   */
  public void setUpperPercentile(double value){
    m_UpperPercentile = value;
    reset();
  }

  /**
   * Returns the upper percentile.
   *
   * @return		the percentile
   */
  public double getUpperPercentile(){
    return m_UpperPercentile;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String upperPercentileTipText() {
    return "The upper percentile; use 0.75 for obtaining the 3rd quartile.";
  }

  /**
   * Returns the currently used database connection object, can be null.
   *
   * @return		the current object
   */
  public AbstractDatabaseConnection getDatabaseConnection() {
    return m_DatabaseConnection;
  }

  /**
   * Sets the database connection object to use.
   *
   * @param value	the object to use
   */
  public void setDatabaseConnection(AbstractDatabaseConnection value) {
    m_DatabaseConnection = value;
    reset();
  }

  /**
   * Returns the SampleDataT singleton to use.
   *
   * @return		the table singleton to use
   */
  protected SampleDataT getSampleDataT() {
    return SampleDataT.getSingleton(getDatabaseConnection());
  }

  /**
   * Returns the SpectrumT singleton to use.
   *
   * @return		the table singleton to use
   */
  protected SpectrumT getSpectrumT() {
    return SpectrumT.getSingleton(getDatabaseConnection());
  }

  /**
   * Checks the specified value against the inter-percentile setup whether
   * it is valid or not.
   *
   * @param ipr		the inter-percentile range setup for the value
   * @param value	the value to check
   * @return		null if OK, otherwise error message
   */
  protected String checkValue(InterPercentileRange ipr, double value) {
    String	result;
    double	threshold;
    String	thresholdStr;

    result = null;

    // lower
    threshold    = ipr.getLowerPercentile() - ipr.getRange() * m_Factor;
    thresholdStr = value + " < " + threshold + " (= " + ipr.getLowerPercentile() + " - " + ipr.getRange() + " * " + m_Factor + ")";
    if (value < threshold)
      result = thresholdStr;

    // upper
    threshold    = ipr.getUpperPercentile() + ipr.getRange() * m_Factor;
    thresholdStr = value + " > " + threshold + " (= " + ipr.getUpperPercentile() + " + " + ipr.getRange() + " * " + m_Factor + ")";
    if (value > ipr.getUpperPercentile() + ipr.getRange() * m_Factor)
      result = thresholdStr;

    return result;
  }

  /**
   * Checks the spectrum, whether all of its values (amplitudes + fields) are
   * within the boundaries.
   *
   * @param data	the spectrum to check
   * @return		null if OK, otherwise the error message
   */
  protected String checkSpectrum(Spectrum data) {
    String			result;
    int				i;
    InterPercentileRange	ipr;
    List<SpectrumPoint>		points;
    SpectrumPoint		point;
    List<AbstractField>		fields;
    AbstractField		field;

    result = null;

    points = data.toList();

    // check spectrum
    for (i = 0; i < points.size(); i++) {
      point = points.get(i);
      ipr   = m_AmplitudeIPRs.get(point.getWaveNumber());
      if (ipr == null) {
	result = "InterPercentileRange not found!";
	break;
      }
      result = checkValue(ipr, point.getAmplitude());
      if (result != null) {
	result = "Wave number " + point.getWaveNumber() + " failed: " + result;
	break;
      }
    }

    // check fields
    if ((result == null) && data.hasReport()) {
      fields = data.getReport().getFields();
      for (i = 0; i < fields.size(); i++) {
	field = fields.get(i);
	if (field.getDataType() != DataType.NUMERIC)
	  continue;
	if (!m_FieldIPRs.containsKey(field))
	  continue;
	ipr = m_FieldIPRs.get(field);
	result = checkValue(ipr, data.getReport().getDoubleValue(field));
	if (result != null) {
	  result = "Field '" + field + "' failed: " + result;
	  break;
	}
      }
    }

    return result;
  }

  /**
   * Performs the check.
   *
   * @param data	the spectrum to check
   * @return		null if ok, otherwise error message
   */
  @Override
  protected String performCheck(Spectrum data) {
    String	result;
    Field	sampleType;

    result = null;

    // check current spectrum against number of amplitudes
    if (m_AmplitudeIPRs.size() != data.size())
      result =   "Number of amplitudes differ - expected: " + m_AmplitudeIPRs.size()
               + ", found: " + data.size();

    // check whether sample types are compatible
    if (!m_SampleType.equals(".*") && !m_SampleType.equals("")) {
      sampleType = new Field(SampleData.SAMPLE_TYPE, DataType.STRING);
      if (data.hasReport() && data.getReport().hasValue(sampleType)) {
	if (!data.getReport().getStringValue(sampleType).matches(m_SampleType))
	  result =   "Sample mismatch: '" + data.getReport().getStringValue(sampleType)
	           + "' does not match '" + m_SampleType + "'!";
      }
    }

    // perform checks against stored IPRs
    if (result == null) {
      result = checkSpectrum(data);
      if (result != null)
	result = "#" + data.getDatabaseID() + "/" + data.getID() + "\t" + result;
    }

    return result;
  }
}
