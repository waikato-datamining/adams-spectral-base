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
 * SegmentedDownSample.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package weka.filters.unsupervised.attribute;

import adams.core.base.BaseRegExp;
import adams.data.instances.ArffUtils;
import gnu.trove.list.array.TIntArrayList;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;
import weka.filters.AllFilter;
import weka.filters.Filter;
import weka.filters.SimpleBatchFilter;
import weka.filters.UnsupervisedFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 <!-- globalinfo-start -->
 * Configures a weka.filters.unsupervised.attribute.PartitionedMultiFilter2, using the supplied number of splits and the nth points to configure the weka.filters.unsupervised.attribute.DownSample filter to apply to the subsets.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p>
 *
 * <pre> -nth-points &lt;list&gt;
 *  The blank-separated list of number of points to use for the savitzky-golay window.
 *  (default: 1).</pre>
 *
 * <pre> -exclude &lt;expr&gt;
 *  The regular expression for identifying attributes to exclude from
 *  the splits (default: ^(sample_id)$)</pre>
 *
 * <pre> -U
 *  Flag for leaving unused attributes out of the output, by default
 *  these are included in the filter output.</pre>
 *
 * <pre> -output-debug-info
 *  If set, filter is run in debug mode and
 *  may output additional info to the console</pre>
 *
 * <pre> -do-not-check-capabilities
 *  If set, filter capabilities are not checked before filter is built
 *  (use with caution).</pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SegmentedDownSample
  extends SimpleBatchFilter
  implements UnsupervisedFilter {

  private static final long serialVersionUID = -717299328781734196L;

  /** the default for the nth point. */
  public final static int DEFAULT_NTH_POINT = 1;

  /** the default for the exclude expression. */
  public final static String DEFAULT_EXCLUDE = "^(" + ArffUtils.getSampleIDName() + ")$";

  /** the blank-separated list of number of savgol points to use. */
  protected List<Integer> m_NthPoints = new ArrayList<>(Arrays.asList(DEFAULT_NTH_POINT));

  /** the regular expression for attributes to exclude from the splits. */
  protected BaseRegExp m_Exclude = new BaseRegExp(DEFAULT_EXCLUDE);

  /** Whether unused attributes are left out of the output. */
  protected boolean m_RemoveUnused = false;

  /** the filter used internally. */
  protected PartitionedMultiFilter2 m_Filter = null;

  /** the filtered data from the first pass. */
  protected Instances m_FirstPassData = null;

  /**
   * Returns a string describing this filter.
   *
   * @return a description of the filter suitable for displaying in the
   *         explorer/experimenter gui
   */
  @Override
  public String globalInfo() {
    return
      "Configures a " + PartitionedMultiFilter2.class.getName() + ", using "
	+ "the supplied number of splits and the nth points to configure the "
	+ DownSample.class.getName() + " filter to apply to the subsets.";
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return 		an enumeration of all the available options.
   */
  public Enumeration listOptions() {
    Vector 	result;
    Enumeration	enm;

    result = new Vector();

    result.addElement(new Option(
      "\tThe blank-separated list of number of points to use for the savitzky-golay window.\n"
	+ "\t(default: " + DEFAULT_NTH_POINT + ").",
      "nth-points", 1, "-nth-points <list>"));

    result.addElement(new Option(
      "\tThe regular expression for identifying attributes to exclude from\n"
	+ "\tthe splits (default: " + DEFAULT_EXCLUDE + ")",
      "exclude", 1, "-exclude <expr>"));

    result.addElement(new Option(
      "\tFlag for leaving unused attributes out of the output, by default\n"
	+ "\tthese are included in the filter output.", "U", 0, "-U"));

    enm = super.listOptions();
    while (enm.hasMoreElements())
      result.add(enm.nextElement());

    return result.elements();
  }

  /**
   * Parses a list of options for this object.
   * Also resets the state of the filter (this reset doesn't affect the
   * options).
   *
   * @param options 	the list of options as an array of strings
   * @throws Exception 	if an option is not supported
   * @see    		#reset()
   */
  public void setOptions(String[] options) throws Exception {
    String		tmpStr;

    reset();

    tmpStr = Utils.getOption("nth-points", options);
    if (!tmpStr.isEmpty())
      setNthPoints(tmpStr);
    else
      setNthPoints("" + DEFAULT_NTH_POINT);

    tmpStr = Utils.getOption("exclude", options);
    if (!tmpStr.isEmpty())
      setExclude(new BaseRegExp(tmpStr));
    else
      setExclude(new BaseRegExp(DEFAULT_EXCLUDE));

    setRemoveUnused(Utils.getFlag("U", options));

    super.setOptions(options);
  }

  /**
   * Gets the current settings of the filter.
   *
   * @return 		an array of strings suitable for passing to setOptions
   */
  public String[] getOptions() {
    List<String>	result;

    result = new ArrayList<>();

    result.add("-nth-points");
    result.add("" + getNthPoints());

    result.add("-exclude");
    result.add("" + getExclude());

    if (getRemoveUnused())
      result.add("-U");

    result.addAll(Arrays.asList(super.getOptions()));

    return result.toArray(new String[result.size()]);
  }

  /**
   * Sets the blank-separated list number of points to use for the savitzky-golay window (>= 1).
   *
   * @param value 	the number of points
   */
  public void setNthPoints(String value) {
    List<Integer>	points;
    Integer		point;
    String[]		parts;
    String		msg;
    int			i;

    msg    = null;
    points = new ArrayList<>();
    parts  = value.replaceAll("  ", " ").split(" ");

    if (parts.length > 0) {
      for (i = 0; i < parts.length; i++) {
	try {
	  point = Integer.parseInt(parts[i]);
	}
	catch (Exception e) {
	  msg = "Failed to parse point #" + (i+1) + ": " + parts[i];
	  break;
	}
	if (point < 1) {
	  msg = "Number of points at position " + (i+1) + " does not satisfy >= 1: " + point;
	  break;
	}
	points.add(point);
      }
    }
    else {
      msg = "At least one # of points (= split) is required";
    }

    if (msg != null) {
      System.err.println(msg);
    }
    else {
      m_NthPoints = points;
      reset();
    }
  }

  /**
   * Returns the blank-separated list number of points to use for the savitzky-golay window (>= 1).
   *
   * @return 		the number of points
   */
  public String getNthPoints() {
    return adams.core.Utils.flatten(m_NthPoints, " ");
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String nthPointsTipText() {
    return "The blank-separated list of nth points to use for the downsample filter.";
  }

  /**
   * Sets the regular expression that identifies attributes to be excluded
   * from the splits.
   *
   * @param value 	the expression
   */
  public void setExclude(BaseRegExp value) {
    m_Exclude = value;
    reset();
  }

  /**
   * Returns the regular expression that identifies attributes to be excluded
   * from the splits.
   *
   * @return 		the expression
   */
  public BaseRegExp getExclude() {
    return m_Exclude;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String excludeTipText() {
    return "The regular expression for identifying attributes to exclude from the splits.";
  }

  /**
   * Sets whether unused attributes (ones that are not covered by any of the
   * ranges) are removed from the output.
   *
   * @param value if true then the unused attributes get removed
   */
  public void setRemoveUnused(boolean value) {
    m_RemoveUnused = value;
    reset();
  }

  /**
   * Gets whether unused attributes (ones that are not covered by any of the
   * ranges) are removed from the output.
   *
   * @return true if unused attributes are removed
   */
  public boolean getRemoveUnused() {
    return m_RemoveUnused;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String removeUnusedTipText() {
    return "If true then unused attributes (ones that are not covered by any "
      + "of the splits) will be removed from the output.";
  }

  /**
   * resets the filter, i.e., m_NewBatch to true and m_FirstBatchDone to false.
   */
  @Override
  protected void reset() {
    super.reset();

    m_Filter        = null;
    m_FirstPassData = null;
  }

  @Override
  public boolean allowAccessToFullInputFormat() {
    return true;
  }

  /**
   * Determines the output format based on the input format and returns this. In
   * case the output format cannot be returned immediately, i.e.,
   * immediateOutputFormat() returns false, then this method will be called from
   * batchFinished().
   *
   * @param inputFormat the input format to base the output format on
   * @return the output format
   * @throws Exception in case the determination goes wrong
   */
  @Override
  protected Instances determineOutputFormat(Instances inputFormat) throws Exception {
    PartitionedMultiFilter2 	part;
    TIntArrayList 		indices;
    int 			i;
    int				n;
    String 			name;
    List<Filter> 		filters;
    DownSample 			down;
    List<weka.core.Range>	ranges;
    weka.core.Range		range;
    adams.core.Range		tmpRange;
    TIntArrayList		attsExcluded;
    TIntArrayList		attsSplits;
    int				start;
    int				end;
    int				fringeLeft;
    int				fringeRight;
    int				segment;

    // determine attributes to use in splits
    attsExcluded = new TIntArrayList();
    attsSplits   = new TIntArrayList();
    for (i = 0; i < inputFormat.numAttributes(); i++) {
      name = inputFormat.attribute(i).name();
      if (inputFormat.classIndex() == i)
	continue;
      if (m_Exclude.isMatch(name)) {
	attsExcluded.add(i);
	continue;
      }
      attsSplits.add(i);
    }

    // partitioned filter
    part = new PartitionedMultiFilter2();
    part.setRemoveUnused(m_RemoveUnused);
    filters     = new ArrayList<>();
    ranges      = new ArrayList<>();
    fringeLeft  = 0;
    fringeRight = 0;
    if (m_NthPoints.size() > 0) {
      fringeLeft  = m_NthPoints.get(0);
      fringeRight = m_NthPoints.get(m_NthPoints.size() - 1);
    }
    segment = ((attsSplits.size() - fringeLeft - fringeRight) / m_NthPoints.size());
    if (getDebug())
      System.out.println("#nthPoint=" + m_NthPoints.size() + ", #attsSplit=" + attsSplits.size() + ", fringeLeft=" + fringeLeft + ", fringeRight=" + fringeRight + ", segment=" + segment);
    for (i = 0; i < m_NthPoints.size(); i++) {
      down = new DownSample();
      down.setNthPoint(m_NthPoints.get(i));
      filters.add(down);

      // start
      start = segment * i + fringeLeft - m_NthPoints.get(i);
      if (start < 0)
	start = 0;
      // end
      end = segment * (i+1) + fringeLeft + m_NthPoints.get(i);
      if (i == m_NthPoints.size() - 1)
	end = attsSplits.size() - 1;
      if (end >= attsSplits.size())
	end = attsSplits.size() - 1;

      // create range
      indices = new TIntArrayList();
      for (n = start; n <= end; n++)
	indices.add(attsSplits.get(n));
      tmpRange = new adams.core.Range();
      tmpRange.setIndices(indices.toArray());
      range = new weka.core.Range(tmpRange.getRange());
      ranges.add(range);
    }

    // excluded attributes?
    if (!m_RemoveUnused && (attsExcluded.size() > 0)) {
      filters.add(new AllFilter());
      tmpRange = new adams.core.Range();
      tmpRange.setIndices(attsExcluded.toArray());
      range = new weka.core.Range(tmpRange.getRange());
      ranges.add(range);
    }

    part.setFilters(filters.toArray(new Filter[filters.size()]));
    part.setRanges(ranges.toArray(new weka.core.Range[ranges.size()]));

    if (getDebug())
      System.out.println(getClass().getName() + ": " + Utils.toCommandLine(part));

    m_Filter = part;
    m_Filter.setInputFormat(inputFormat);

    try {
      m_FirstPassData = Filter.useFilter(inputFormat, m_Filter);
    }
    catch (Exception e) {
      throw new Exception("Following setup generated exception:\n" + Utils.toCommandLine(m_Filter), e);
    }

    return new Instances(m_FirstPassData, 0);
  }

  /**
   * Processes the given data (may change the provided dataset) and returns the
   * modified version. This method is called in batchFinished().
   *
   * @param instances the data to process
   * @return the modified data
   * @throws Exception in case the processing goes wrong
   */
  @Override
  protected Instances process(Instances instances) throws Exception {
    Instances	result;

    if (m_FirstPassData != null) {
      result          = m_FirstPassData;
      m_FirstPassData = null;
    }
    else {
      result = Filter.useFilter(instances, m_Filter);
    }

    return result;
  }
}
