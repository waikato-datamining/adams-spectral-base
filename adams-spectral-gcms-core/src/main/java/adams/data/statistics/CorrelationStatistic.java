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
 * CorrelationStatistic.java
 * Copyright (C) 2008-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.data.statistics;

import adams.core.ClassLister;
import adams.core.Performance;
import adams.core.logging.LoggingObject;
import adams.core.option.OptionHandler;
import adams.core.option.OptionManager;
import adams.core.option.OptionUtils;
import adams.data.chromatogram.Chromatogram;
import adams.data.chromatogram.GCPoint;
import adams.multiprocess.AbstractJob;
import adams.multiprocess.JobList;
import adams.multiprocess.JobRunner;
import adams.multiprocess.LocalJobRunner;

import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

/**
 * Inteface for statistic classes that determine a correlation between
 * two data vectors.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4402 $
 */
public abstract class CorrelationStatistic
  extends LoggingObject
  implements OptionHandler {

  /** for serialization. */
  private static final long serialVersionUID = 83568968813274926L;

  /**
   * A job class specific to correlation-statistics.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 4402 $
   */
  public static class CorrelationStatisticJob
    extends AbstractJob {

    /** for serialization. */
    private static final long serialVersionUID = -7881887611199679762L;

    /** the correlation statistic to use. */
    protected CorrelationStatistic m_CorrelationStatistic;

    /** the reference data to use. */
    protected Chromatogram m_ReferenceData;

    /** the data to compare. */
    protected Chromatogram m_Data;

    /** the correlation. */
    protected Correlation m_Correlation;

    /**
     * Initializes the job.
     *
     * @param statistic		the correlation statistic to use for computing
     * 				the correlation
     * @param reference		the reference data
     * @param data		the data to compare with
     */
    public CorrelationStatisticJob(CorrelationStatistic statistic, Chromatogram reference, Chromatogram data) {
      super();

      m_CorrelationStatistic = statistic;
      m_ReferenceData        = reference;
      m_Data                 = data;
      m_Correlation          = null;
    }

    /**
     * Returns the statistic algorithm being used.
     *
     * @return		the algorithm in use
     */
    public CorrelationStatistic getCorrelationStatistic() {
      return m_CorrelationStatistic;
    }

    /**
     * The reference data.
     *
     * @return		the reference
     */
    public Chromatogram getReferenceData() {
      return m_ReferenceData;
    }

    /**
     * The data to compare with.
     *
     * @return		the data
     */
    public Chromatogram getData() {
      return m_Data;
    }

    /**
     * The correlation, if any.
     *
     * @return		the correlation, or null in case of an error
     */
    public Correlation getCorrelation() {
      return m_Correlation;
    }

    /**
     * Checks whether all pre-conditions have been met.
     *
     * @return		null if everything is OK, otherwise an error message
     */
    @Override
    protected String preProcessCheck() {
      if (m_CorrelationStatistic == null)
	return "No correlation statistic set!";

      if (m_ReferenceData == null)
	return "No reference data set!";

      if (m_Data == null)
	return "No data set!";

      return null;
    }

    /**
     * Does the actual execution of the job.
     * 
     * @throws Exception if fails to execute job
     */
    @Override
    protected void process() throws Exception {
      m_Correlation = m_CorrelationStatistic.getCorrelation(
	  m_ReferenceData.toList(),
	  m_Data.toList());
    }

    /**
     * Checks whether all post-conditions have been met.
     *
     * @return		null if everything is OK, otherwise an error message
     */
    @Override
    protected String postProcessCheck() {
      if (m_Correlation == null)
	return "Correlation result is null!";

      return null;
    }

    /**
     * Cleans up data structures, frees up memory.
     * Sets the input data to null.
     */
    @Override
    public void cleanUp() {
      super.cleanUp();

      m_Data                 = null;
      m_Correlation          = null;
      m_CorrelationStatistic.destroy();
      m_CorrelationStatistic = null;
      m_ReferenceData        = null;
    }

    /**
     * Returns additional information to be added to the error message.
     *
     * @return		the additional information
     */
    @Override
    protected String getAdditionalErrorInformation() {
      return m_Data.getNotes() + "\nReference data notes:\n" + m_ReferenceData.getNotes();
    }

    /**
     * Returns a string representation of the job.
     *
     * @return		a string representation
     */
    @Override
    public String toString() {
      return   "data:" + m_Data.getID() + ", "
	     + "correlation-statistic: " + OptionUtils.getCommandLine(m_CorrelationStatistic);
    }
  }

  /**
   * Contains the Correlation container object.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 4402 $
   */
  public abstract static class Correlation
    extends AbstractDataStatistic<Chromatogram> {

    /** for serialization. */
    private static final long serialVersionUID = 2250034713752487305L;

    /** the owning statistics object. */
    protected CorrelationStatistic m_Owner;

    /**
     * Initializes the correlation.
     *
     * @param owner	the statistics object this correlation belongs to
     */
    public Correlation(CorrelationStatistic owner) {
      super();

      m_Owner = owner;
    }

    /**
     * Returns the statistics object this correlation belongs to.
     *
     * @return		the owning statistic object
     */
    public CorrelationStatistic getOwner() {
      return m_Owner;
    }

    /**
     * Sets the correlation.
     *
     * @param value	the correlation
     */
    public abstract void setCorrelation(Object value);

    /**
     * Returns the correlation.
     *
     * @return		the correlation
     */
    public abstract Object getCorrelation();

    /**
     * Returns the normalized correlation. The default implementation ALWAYS
     * returns null.
     *
     * @return		the normalized correlation
     */
    public Object getNormalizedCorrelation() {
      return null;
    }
  }

  /** for managing the available options. */
  protected OptionManager m_OptionManager;

  /** whether debugging is on. */
  protected boolean m_Debug;

  /**
   * Initializes the denoiser algorithm.
   */
  public CorrelationStatistic() {
    super();
    initialize();
    defineOptions();
    getOptionManager().setDefaults();
  }

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public abstract String globalInfo();

  /**
   * Initializes the members.
   */
  protected void initialize() {
  }

  /**
   * Resets the correlation statistic algorithm.
   * Derived classes must call this method in set-methods of parameters to
   * assure the invalidation of previously generated data.
   */
  public void reset() {
  }

  /**
   * Returns a new instance of the option manager.
   *
   * @return		the manager to use
   */
  protected OptionManager newOptionManager() {
    return new OptionManager(this);
  }

  /**
   * Adds options to the internal list of options. Derived classes must
   * override this method to add additional options.
   */
  public void defineOptions() {
    m_OptionManager = newOptionManager();

    m_OptionManager.add(
	"D", "debug", false);
  }

  /**
   * Returns the option manager.
   *
   * @return		the manager
   */
  public OptionManager getOptionManager() {
    if (m_OptionManager == null)
      defineOptions();

    return m_OptionManager;
  }

  /**
   * Cleans up the options.
   */
  public void cleanUpOptions() {
    if (m_OptionManager != null) {
      m_OptionManager.cleanUp();
      m_OptionManager = null;
    }
  }

  /**
   * Frees up memory in a "destructive" non-reversible way.
   * <br><br>
   * Cleans up the options.
   *
   * @see	#cleanUpOptions()
   */
  public void destroy() {
    cleanUpOptions();
  }

  /**
   * Set debugging mode.
   *
   * @param value 	true if debug output should be printed
   */
  public void setDebug(boolean value) {
    m_Debug = value;
    getLogger().setLevel(value ? Level.INFO : Level.OFF);
    reset();
  }

  /**
   * Returns whether debugging is turned on.
   *
   * @return 		true if debugging output is on
   */
  public boolean getDebug() {
    return m_Debug;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String debugTipText() {
    return "If set to true, statistic class may output additional info to the console.";
  }

  /**
   * Returns the common points (according to timestamp).
   *
   * @param data1	the first data vector
   * @param data2	the second data vector
   * @param newData1	data vector with the common points of the first data vector
   * @param newData2	data vector with the common points of the second data vector
   * @param timestamp	if true then the common points are points with the same
   * 			timestamp, otherwise just the biggest possible chunk of
   * 			points is taken starting with index 0
   */
  protected void getCommonPoints(List<GCPoint> data1, List<GCPoint> data2, List<GCPoint> newData1, List<GCPoint> newData2, boolean timestamp) {
    int		i;
    int		j;
    GCPoint	point1;
    GCPoint	point2;

    newData1.clear();
    newData2.clear();

    if (timestamp) {
      for (i = 0, j = 0; (i < data1.size()) && (j < data2.size()); ) {
	point1 = data1.get(i);
	point2 = data2.get(j);

	if (point1.getTimestamp() == point2.getTimestamp()) {
	  newData1.add(point1);
	  newData2.add(point2);
	  i++;
	  j++;
	}
	else if (point1.getTimestamp() < point2.getTimestamp()) {
	  i++;
	}
	else if (point1.getTimestamp() > point2.getTimestamp()) {
	  j++;
	}
      }
    }
    else {
      j = Math.min(data1.size(), data2.size());
      for (i = 0; i < j; i++) {
	newData1.add(data1.get(i));
	newData2.add(data2.get(i));
      }
    }
  }

  /**
   * Checks whether the vector contains GC points.
   *
   * @param data	the vector to check
   * @return		true if the vector contains GC points
   */
  protected boolean isGCPointVector(List data) {
    boolean	result;
    GCPoint	value;

    result = false;

    if (data.size() > 0) {
      try {
	value  = (GCPoint) data.get(0);
	result = true;
      }
      catch (Exception e) {
	// ignored
      }
    }

    return result;
  }

  /**
   * Checks whether the vector contains Doubles.
   *
   * @param data	the vector to check
   * @return		true if the vector contains Doubles
   */
  protected boolean isDoubleVector(List data) {
    boolean	result;
    Double	value;

    result = false;

    if (data.size() > 0) {
      try {
	value  = (Double) data.get(0);
	result = true;
      }
      catch (Exception e) {
	// ignored
      }
    }

    return result;
  }

  /**
   * Computes the correlation between the two data vectors and returns it.
   * The common points are not determined based on timestamp, but just the
   * biggest subset starting with index 0.
   *
   * @param data1	the first data vector
   * @param data2	the second data vector
   * @return		the computed correlation
   */
  public Correlation getCorrelation(List data1, List data2) {
    return getCorrelation(data1, data2, false);
  }

  /**
   * Computes the correlation between the two data vectors and returns it.
   *
   * @param data1	the first data vector
   * @param data2	the second data vector
   * @param timestamp	whether to obtain common points based on the timestamp
   * 			or just take the biggest common subset of points
   * 			starting with index 0
   * @return		the computed correlation
   */
  public Correlation getCorrelation(List data1, List data2, boolean timestamp) {
    Correlation		result;
    double[]		array1;
    double[]		array2;
    Vector<GCPoint>	newData1;
    Vector<GCPoint>	newData2;
    int			i;

    array1   = null;
    array2   = null;
    newData1 = null;
    newData2 = null;

    if (isGCPointVector(data1) || isGCPointVector(data2)) {
      newData1 = new Vector<>();
      newData2 = new Vector<>();
      getCommonPoints(data1, data2, newData1, newData2, timestamp);

      array1 = new double[newData1.size()];
      array2 = new double[newData1.size()];

      for (i = 0; i < array1.length; i++) {
	array1[i] = newData1.get(i).getAbundance();
	array2[i] = newData2.get(i).getAbundance();
      }
    }
    else if (isDoubleVector(data1) || isDoubleVector(data2)) {
      if (data1.size() != data2.size())
	throw new IllegalArgumentException(
	    "Double vectors differ in size ("
	    + data1.size() + " != " + data2.size() + ")!");

      array1 = new double[data1.size()];
      array2 = new double[data1.size()];

      for (i = 0; i < array1.length; i++) {
	array1[i] = (Double) data1.get(i);
	array2[i] = (Double) data2.get(i);
      }
    }
    else {
      throw new IllegalArgumentException("Unsupported vector type!");
    }

    // determine actual correlation
    result = getCorrelation(array1, array2);

    // some generic statistics
    if (newData1 != null) {
      result.add("Number of points in smaller Vector", Math.min(data1.size(), data2.size()));
      result.add("Number of points in bigger Vector", Math.min(data1.size(), data2.size()));
      result.add("Number of common points", array1.length);
    }
    else {
      result.add("Number of points", array1.length);
    }

    return result;
  }

  /**
   * Computes the correlation between the two data vectors and returns it.
   *
   * @param data1	the first data array
   * @param data2	the second data array
   * @return		the computed correlation
   */
  public abstract Correlation getCorrelation(double[] data1, double[] data2);

  /**
   * Returns whether a normalized correlation, in the range [0..1], is
   * available. The default implementation always returns FALSE.
   *
   * @return		true if a normalized correlation is available
   */
  public boolean hasNormalizedCorrelation() {
    return false;
  }

  /**
   * Returns a shallow copy of itself, i.e., based on the commandline options.
   *
   * @return		the shallow copy
   */
  public CorrelationStatistic shallowCopy() {
    return shallowCopy(false);
  }

  /**
   * Returns a shallow copy of itself, i.e., based on the commandline options.
   *
   * @param expand	whether to expand variables to their current values
   * @return		the shallow copy
   */
  public CorrelationStatistic shallowCopy(boolean expand) {
    return (CorrelationStatistic) OptionUtils.shallowCopy(this, expand);
  }

  /**
   * Returns the commandline string.
   *
   * @return		 the commandline
   */
  public String toCommandLine() {
    return OptionUtils.getCommandLine(this);
  }

  /**
   * Instantiates the correlation statistic with the given options.
   *
   * @param classname	the classname of the correlation statistic to instantiate
   * @param options	the options for the correlation statistic
   * @return		the instantiated correlation statistic or null if an
   * 			error occurred
   */
  public static CorrelationStatistic forName(String classname, String[] options) {
    CorrelationStatistic	result;

    try {
      result = (CorrelationStatistic) OptionUtils.forName(CorrelationStatistic.class, classname, options);
    }
    catch (Exception e) {
      e.printStackTrace();
      result = null;
    }

    return result;
  }

  /**
   * Returns a list with classnames of correlation statistics.
   *
   * @return		the classnames
   */
  public static String[] getCorrelationStatistics() {
    return ClassLister.getSingleton().getClassnames(CorrelationStatistic.class);
  }

  /**
   * Passes the data through the given statistics algorithm and returns the
   * correlation.
   *
   * @param statistic	the correlation statistic to use
   * @param reference	the reference data
   * @param data	the data to compare with
   * @return		the correlation
   */
  public static Correlation correlate(CorrelationStatistic statistic, Chromatogram reference, Chromatogram data) {
    Vector<Chromatogram>	list;
    Vector<Correlation>		result;

    list = new Vector<>();
    list.add(data);
    result = correlate(statistic, reference, list);

    return result.get(0);
  }

  /**
   * Passes the data through the given statistics algorithm and returns a vector
   * containing the correlation for each chromatogram. Makes use of
   * multiple cores, i.e., for each dataset a new thread will be run with a
   * copy of the statistics algorithm.
   *
   * @param statistic	the statistics algorithm to use for calculating the
   * 			correlations (a new algorithm with the same options will
   * 			be created and used in each thread)
   * @param reference	the reference data
   * @param data	the data to compare with
   * @return		the correlations
   */
  public static Vector<Correlation> correlate(CorrelationStatistic statistic, Chromatogram reference, Vector<Chromatogram> data) {
    Vector<Correlation>			result;
    CorrelationStatistic		threadStatistic;
    JobRunner<CorrelationStatisticJob> 	runner;
    JobList<CorrelationStatisticJob>	jobs;
    CorrelationStatisticJob		job;
    int					i;

    result = new Vector<Correlation>();

    if (Performance.getMultiProcessingEnabled()) {
      runner = new LocalJobRunner<>();
      jobs   = new JobList<>();

      // fill job list
      for (i = 0; i < data.size(); i++) {
	threadStatistic = statistic.shallowCopy(true);
	jobs.add(new CorrelationStatisticJob(threadStatistic, reference, data.get(i)));
      }
      runner.add(jobs);
      runner.start();
      runner.stop();

      // gather results
      for (i = 0; i < jobs.size(); i++) {
	job = jobs.get(i);
	// success? If not, just add null
	if (job.getCorrelation() != null)
	  result.add(job.getCorrelation());
	else
	  result.add(null);
	job.cleanUp();
      }
    }
    else {
      for (i = 0; i < data.size(); i++) {
	threadStatistic = statistic.shallowCopy(true);
	result.add(threadStatistic.getCorrelation(reference.toList(), data.get(i).toList()));
      }
    }

    return result;
  }
}
