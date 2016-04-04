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

/**
 * RangeBasedSavitzkyGolay.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package weka.filters.unsupervised.attribute;

import adams.core.option.OptionUtils;
import weka.core.Instances;
import weka.core.Option;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.SimpleBatchFilter;
import weka.filters.UnsupervisedFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 <!-- globalinfo-start -->
 * Configures a weka.filters.unsupervised.attribute.PartitionedMultiFilter, using the supplied ranges and the number of points to configure the weka.filters.unsupervised.attribute.SavitzkyGolay2 filter to apply to that subset.
 * <p/>
 <!-- globalinfo-end -->
 *
 <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;article{Savitzky1964,
 *    author = {A. Savitzky and Marcel J.E. Golay},
 *    journal = {Analytical Chemistry},
 *    pages = {1627-1639},
 *    title = {Smoothing and Differentiation of Data by Simplified Least Squares Procedures},
 *    volume = {36},
 *    year = {1964},
 *    HTTP = {http://dx.doi.org/10.1021/ac60214a047}
 * }
 * 
 * &#64;inbook{Press1992,
 *    author = {William H. Press and Saul A. Teukolsky and William T. Vetterling and Brian P. Flannery},
 *    chapter = {14.8},
 *    edition = {Second},
 *    pages = {650-655},
 *    publisher = {Cambridge University Press},
 *    series = {Numerical Recipes in C},
 *    title = {Savitzky-Golay Smoothing Filters},
 *    year = {1992},
 *    PDF = {http://www.nrbook.com/a/bookcpdf/c14-8.pdf}
 * }
 * </pre>
 * <p/>
 <!-- technical-bibtex-end -->
 *
 <!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -range &lt;weka.filters.unsupervised.attribute.SavitzkyGolayRange + options&gt;
 *  The range definition(s) (can be specified multiple times).
 *  (default: none)</pre>
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
public class RangeBasedSavitzkyGolay
  extends SimpleBatchFilter
  implements TechnicalInformationHandler, UnsupervisedFilter {

  private static final long serialVersionUID = -717299328781734196L;

  /** the prefix for a spectral attribute. */
  public final static String PREFIX_AMPLITUDE = SpectrumFilter.PREFIX_AMPLITUDE;

  /** the ranges to use. */
  protected SavitzkyGolayRange[] m_Ranges = new SavitzkyGolayRange[0];

  /** Whether unused attributes are left out of the output. */
  protected boolean m_RemoveUnused = false;

  /** the filter used internally. */
  protected SpectrumFilter m_Filter = null;

  /** the filteed data from the first pass. */
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
      "Configures a " + PartitionedMultiFilter.class.getName() + ", using "
	+ "the supplied ranges and the number of points to configure the "
	+ SavitzkyGolay2.class.getName() + " filter to apply to that subset.";
  }

  /**
   * Returns an instance of a TechnicalInformation object, containing
   * detailed information about the technical background of this class,
   * e.g., paper reference or book this class is based on.
   *
   * @return the technical information about this class
   */
  @Override
  public TechnicalInformation getTechnicalInformation() {
    return new SavitzkyGolay2().getTechnicalInformation();
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
	"\tThe range definition(s) (can be specified multiple times).\n"
	+ "\t(default: none)",
	"range", 1, "-range <" + SavitzkyGolayRange.class.getName() + " + options>"));

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
    String			tmpStr;
    List<SavitzkyGolayRange>	ranges;
    SavitzkyGolayRange range;

    reset();

    ranges = new ArrayList<>();
    while (!(tmpStr = Utils.getOption("range", options)).isEmpty()) {
      range = (SavitzkyGolayRange) OptionUtils.forAnyCommandLine(SavitzkyGolayRange.class, tmpStr);
      if (range != null)
	ranges.add(range);
    }
    setRanges(ranges.toArray(new SavitzkyGolayRange[ranges.size()]));

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

    for (SavitzkyGolayRange range: m_Ranges) {
      result.add("-range");
      result.add(range.toCommandLine());
    }

    if (getRemoveUnused())
      result.add("-U");

    result.addAll(Arrays.asList(super.getOptions()));

    return result.toArray(new String[result.size()]);
  }

  /**
   * Sets the ranges to use.
   *
   * @param value 	the ranges
   */
  public void setRanges(SavitzkyGolayRange[] value) {
    m_Ranges = value;
    reset();
  }

  /**
   * Returns the ranges to use.
   *
   * @return 		the ranges
   */
  public SavitzkyGolayRange[] getRanges() {
    return m_Ranges;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rangesTipText() {
    return "The ranges to use.";
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
      + "of the ranges) will be removed from the output.";
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
    SpectrumFilter		spec;
    PartitionedMultiFilter	part;
    Instances			filtered;
    Map<String,Integer> 	indexMap;
    List<Integer>		indices;
    int				i;
    String			name;
    List<SavitzkyGolay2> 	savGols;
    SavitzkyGolay2 		savGol;
    List<weka.core.Range>	ranges;
    weka.core.Range		range;
    adams.core.Range		tmpRange;

    if (m_Ranges.length == 0)
      throw new IllegalArgumentException("At least one range must be defined!");

    // determine attributes of amplitudes inside spectrum filter
    spec     = new SpectrumFilter();
    filtered = spec.determineOutputFormat(inputFormat);
    indexMap = new HashMap<>();
    for (i = 0; i < filtered.numAttributes(); i++) {
      if (i == filtered.classIndex())
	continue;
      name = filtered.attribute(i).name();
      indexMap.put(name, inputFormat.attribute(name).index());
    }

    // partitioned filter
    part = new PartitionedMultiFilter();
    part.setRemoveUnused(m_RemoveUnused);
    savGols = new ArrayList<>();
    ranges  = new ArrayList<>();
    for (SavitzkyGolayRange r : m_Ranges) {
      savGol = new SavitzkyGolay2();
      savGol.setDerivativeOrder(r.getDerivativeOrder());
      savGol.setPolynomialOrder(r.getPolynomialOrder());
      savGol.setNumPoints(r.getNumPoints());
      savGols.add(savGol);

      indices = new ArrayList<>();
      for (i = r.getStart(); i <= r.getEnd(); i++) {
	if (indexMap.get(PREFIX_AMPLITUDE + i) != null)
	  indices.add(indexMap.get(PREFIX_AMPLITUDE + i));
      }
      tmpRange = new adams.core.Range();
      tmpRange.setIndices(indices.toArray(new Integer[indices.size()]));

      range = new weka.core.Range(tmpRange.getRange());
      ranges.add(range);
    }
    part.setFilters(savGols.toArray(new Filter[savGols.size()]));
    part.setRanges(ranges.toArray(new weka.core.Range[ranges.size()]));

    // configure full filter
    spec = new SpectrumFilter();
    spec.setFilter(part);
    spec.setKeepAttributeNames(true);

    if (getDebug())
      System.out.println(getClass().getName() + ": " + Utils.toCommandLine(spec));

    m_Filter = spec;
    m_Filter.setInputFormat(inputFormat);

    m_FirstPassData = Filter.useFilter(inputFormat, m_Filter);

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
