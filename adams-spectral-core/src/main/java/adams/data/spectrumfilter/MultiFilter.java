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
 * Copyright (C) 2009-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.container.DataContainer;
import adams.data.filter.AbstractFilter;
import adams.data.filter.Filter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumUtils;

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
 *    outputs are merged into a single spectrum  again. Already existing<br>
 *    wave numbers don't get overwritten.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 * &nbsp;&nbsp;&nbsp;If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-filter &lt;adams.data.filter.AbstractFilter [options]&gt; [-filter ...] (property: subFilters)
 * &nbsp;&nbsp;&nbsp;The array of filters to use.
 * </pre>
 *
 * <pre>-parallel (property: parallelAndMerge)
 * &nbsp;&nbsp;&nbsp;If set to true, each of the filters will run on the original input data
 * &nbsp;&nbsp;&nbsp;and the results merged into a single spectrum again; otherwise the filters
 * &nbsp;&nbsp;&nbsp;will be applied subsequently, each using the output of the previous one
 * &nbsp;&nbsp;&nbsp;as input.
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class MultiFilter
  extends adams.data.filter.MultiFilter<Spectrum> {

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
      + "   outputs are merged into a single spectrum  again. Already existing\n"
      + "   wave numbers don't get overwritten.";
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
  protected Spectrum processDataParallel(Spectrum data) {
    Spectrum			result;
    int				i;
    List<Filter>		filters;
    List<DataContainer>		outputs;
    List<Spectrum>		outputsC;

    // filter the data
    filters = new ArrayList<>();
    for (i = 0; i < m_Filters.length; i++)
      filters.add(m_Filters[i].shallowCopy(true));
    outputs = AbstractFilter.filter(filters, data);

    // merge outputs
    outputsC = new ArrayList<>();
    for (i = 0; i < outputs.size(); i++)
      outputsC.add((Spectrum) outputs.get(i));
    result = SpectrumUtils.merge(outputsC);

    // clean up
    for (i = 0; i < m_Filters.length; i++)
      filters.get(i).destroy();
    filters.clear();

    return result;
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  @Override
  protected Spectrum processData(Spectrum data) {
    if (m_ParallelAndMerge)
      return processDataParallel(data);
    else
      return super.processData(data);
  }
}
