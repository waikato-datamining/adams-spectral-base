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
 * WekaFilter.java
 * Copyright (C) 2011-2020 University of Waikato, Hamilton, New Zealand
 */
package adams.data.postprocessor.instances;

import adams.core.Utils;
import adams.core.option.OptionUtils;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.AllFilter;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.SpectrumFilter;

/**
 <!-- globalinfo-start -->
 * Uses a WEKA filter for post-processing.<br>
 * Automatically wraps a SpectrumFilter meta-filter around the actual filter.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-serialization-file &lt;adams.core.io.PlaceholderFile&gt; (property: serializationFile)
 * &nbsp;&nbsp;&nbsp;The file to serialize the generated internal model to.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 *
 * <pre>-override-serialized-file &lt;boolean&gt; (property: overrideSerializedFile)
 * &nbsp;&nbsp;&nbsp;If set to true, then any serialized file will be ignored and the setup for
 * &nbsp;&nbsp;&nbsp;serialization will be regenerated.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-filter &lt;weka.filters.Filter&gt; (property: filter)
 * &nbsp;&nbsp;&nbsp;The filter to use for processing the Instances.
 * &nbsp;&nbsp;&nbsp;default: weka.filters.AllFilter
 * </pre>
 *
 * <pre>-wrap &lt;boolean&gt; (property: wrap)
 * &nbsp;&nbsp;&nbsp;Whether to wrap the filter in a weka.filters.unsupervised.attribute.SpectrumFilter.
 * &nbsp;&nbsp;&nbsp;default: true
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class WekaFilter
  extends AbstractSerializablePostProcessor {

  /** for serialization. */
  private static final long serialVersionUID = -4880335524925570270L;

  /** the WEKA filter to use. */
  protected Filter m_Filter;

  /** the actual WEKA filter to use. */
  protected Filter m_ActualFilter;

  /** the filtered data generated when initializing the postprocessor. */
  protected Instances m_FilteredInitData;

  /** whether to wrap the filter in a SpectrumFilter. */
  protected boolean m_Wrap;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return
        "Uses a WEKA filter for post-processing.\n"
      + "Automatically wraps a SpectrumFilter meta-filter around the actual filter.";
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "filter", "filter",
      getDefaultFilter());

    m_OptionManager.add(
      "wrap", "wrap",
      true);
  }

  /**
   * Returns the default filter to use.
   *
   * @return		the default filter
   */
  protected Filter getDefaultFilter() {
    return new AllFilter();
  }

  /**
   * Sets the filter to use.
   *
   * @param value 	the filter
   */
  public void setFilter(Filter value) {
    m_Filter = value;
    reset();
  }

  /**
   * Returns the filter in use.
   *
   * @return 		the filter
   */
  public Filter getFilter() {
    return m_Filter;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String filterTipText() {
    return "The filter to use for processing the Instances.";
  }

  /**
   * Sets whether to wrap the filter in a {@link SpectrumFilter}.
   *
   * @param value 	true if to wrap
   */
  public void setWrap(boolean value) {
    m_Wrap = value;
    reset();
  }

  /**
   * Returns whether to wrap the filter in a {@link SpectrumFilter}.
   *
   * @return 		true if to wrap
   */
  public boolean getWrap() {
    return m_Wrap;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String wrapTipText() {
    return "Whether to wrap the filter in a " + Utils.classToString(SpectrumFilter.class) + ".";
  }

  /**
   * Regenerates all the objects that are necessary for serialization.
   */
  public void initSerializationSetup() {
    try {
      if (m_Wrap) {
	m_ActualFilter = new SpectrumFilter();
	((SpectrumFilter) m_ActualFilter).setFilter((Filter) OptionUtils.shallowCopy(m_Filter));
      }
      else {
        m_ActualFilter = (Filter) OptionUtils.shallowCopy(m_Filter);
      }
      m_ActualFilter.setInputFormat(m_InitData);
      m_FilteredInitData = Filter.useFilter(m_InitData, m_ActualFilter);
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Returns the member variables to serialize to a file.
   *
   * @return		the objects to serialize
   */
  public Object[] retrieveSerializationSetup() {
    return new Object[]{
	m_ActualFilter
    };
  }

  /**
   * Updates the member variables with the provided objects obtained from
   * deserialization.
   *
   * @param value	the deserialized objects
   */
  public void setSerializationSetup(Object[] value) {
    m_ActualFilter = (Filter) value[0];
  }

  /**
   * Performs the actual postprocessing.
   *
   * @param data	the dataset to process
   * @return		the processed dataset
   */
  protected Instances performPostProcess(Instances data) {
    Instances	result;

    // data from first batch available?
    if (m_FilteredInitData != null) {
      result = m_FilteredInitData;
    }
    else {
      try {
	result = Filter.useFilter(data, m_ActualFilter);
      }
      catch (Exception e) {
	throw new IllegalStateException(e);
      }
    }

    return result;
  }

  /**
   * Performs the actual postprocessing.
   *
   * @param data	the instance to process
   * @return		the processed instance
   */
  protected Instance performPostProcess(Instance data) {
    Instance	result;

    try {
      m_ActualFilter.input(data);
      m_ActualFilter.batchFinished();
      result = m_ActualFilter.output();
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }

    return result;
  }

  /**
   * Cleans up data structures, frees up memory.
   */
  public void cleanUp() {
    super.cleanUp();

    m_FilteredInitData = null;
  }
}
