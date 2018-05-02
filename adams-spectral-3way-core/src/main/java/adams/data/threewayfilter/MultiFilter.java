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
 * MultiFilter.java
 * Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */

package adams.data.threewayfilter;

import adams.data.container.DataContainer;
import adams.data.threeway.ThreeWayData;
import adams.data.threeway.ThreeWayDataUtils;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * A meta-filter that runs multiple filters over the data.<br>
 * The filter can be executed in two ways:<br>
 * 1. in series (the default):<br>
 *    Each filter runs on the data the previous filter generated.<br>
 * 2. in parallel:<br>
 *    Each filter is run on the original input data and the generated<br>
 *    outputs are merged into a single 3-way data structure again. Already existing<br>
 *    data points don't get overwritten.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-no-id-update &lt;boolean&gt; (property: dontUpdateID)
 * &nbsp;&nbsp;&nbsp;If enabled, suppresses updating the ID of adams.data.id.IDHandler data containers.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-filter &lt;adams.data.filter.Filter&gt; [-filter ...] (property: subFilters)
 * &nbsp;&nbsp;&nbsp;The array of filters to use.
 * &nbsp;&nbsp;&nbsp;default: adams.data.filter.PassThrough
 * </pre>
 *
 * <pre>-parallel &lt;boolean&gt; (property: parallelAndMerge)
 * &nbsp;&nbsp;&nbsp;If set to true, each of the filters will run on the original input data
 * &nbsp;&nbsp;&nbsp;and the results merged into a single spectrum again; otherwise the filters
 * &nbsp;&nbsp;&nbsp;will be applied subsequently, each using the output of the previous one
 * &nbsp;&nbsp;&nbsp;as input.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class MultiFilter
  extends adams.data.filter.MultiFilter<ThreeWayData> {

  /** for serialization. */
  private static final long serialVersionUID = 805661569976845842L;

  /** whether to execute the filters in parallel and merge the outputs or
   * run them in series. */
  protected boolean m_ParallelAndMerge;

  /**
   * Returns a string describing the object.
   *
   * @return 		a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "A meta-filter that runs multiple filters over the data.\n"
        + "The filter can be executed in two ways:\n"
        + "1. in series (the default):\n"
        + "   Each filter runs on the data the previous filter generated.\n"
        + "2. in parallel:\n"
        + "   Each filter is run on the original input data and the generated\n"
        + "   outputs are merged into a single 3-way data structure again. Already existing\n"
        + "   data points don't get overwritten.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "parallel", "parallelAndMerge",
      false);
  }

  /**
   * Sets whether filters are executed in series or parallel.
   *
   * @param value 	true if filters are to be executed in parallel
   */
  public void setParallelAndMerge(boolean value) {
    m_ParallelAndMerge = value;
    reset();
  }

  /**
   * Returns whether the filters are executed in parallel or series.
   *
   * @return 		true if executed in parallel
   */
  public boolean getParallelAndMerge() {
    return m_ParallelAndMerge;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String parallelAndMergeTipText() {
    return
      "If set to true, each of the filters will run on the original input "
        + "data and the results merged into a single spectrum again; "
        + "otherwise the filters will be applied subsequently, each using the "
        + "output of the previous one as input.";
  }

  /**
   * Performs the actual filtering - parallel application of filters and
   * merging of results.
   *
   * @param data	the data to process
   * @return		the merged dta
   */
  protected ThreeWayData processDataParallel(ThreeWayData data) {
    ThreeWayData			result;
    int				i;
    List<DataContainer>		outputs;
    List<ThreeWayData>		outputsC;

    // filter the data
    outputs = new ArrayList<>();
    for (i = 0; i < m_Filters.length; i++)
      outputs.add(m_Filters[i].shallowCopy(true).filter(data));

    // merge outputs
    outputsC = new ArrayList<>();
    for (i = 0; i < outputs.size(); i++)
      outputsC.add((ThreeWayData) outputs.get(i));
    result = ThreeWayDataUtils.merge(outputsC);

    return result;
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  @Override
  protected ThreeWayData processData(ThreeWayData data) {
    if (m_ParallelAndMerge)
      return processDataParallel(data);
    else
      return super.processData(data);
  }
}
