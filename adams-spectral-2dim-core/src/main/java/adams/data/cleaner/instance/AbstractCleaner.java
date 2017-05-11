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
 * AbstractCleaner.java
 * Copyright (C) 2009-2015 University of Waikato, Hamilton, New Zealand
 */

package adams.data.cleaner.instance;

import adams.core.ClassLister;
import adams.core.ShallowCopySupporter;
import adams.core.option.AbstractOptionConsumer;
import adams.core.option.AbstractOptionHandler;
import adams.core.option.ArrayConsumer;
import adams.core.option.OptionUtils;
import adams.flow.core.Actor;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.AllFilter;
import weka.filters.Filter;

/**
 * Abstract base class for cleaners handling <code>weka.core.Instance</code>
 * objects.
 *
 * Derived classes only have to override the <code>check(Instance)</code>
 * method. The <code>reset()</code> method can be used to reset an
 * algorithms internal state, e.g., after setting options.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2355 $
 */
public abstract class AbstractCleaner
  extends AbstractOptionHandler
  implements Comparable, ShallowCopySupporter<AbstractCleaner> {

  /** for serialization. */
  private static final long serialVersionUID = 3610605513320220903L;

  /** for storing error messages in case the cleaning of the initial Instances
   * object failed. */
  protected String m_CleanInstancesError;

  /** the pre-filter to use. */
  protected Filter m_PreFilter;

  /** the actual pre-filter to use. */
  protected Filter m_ActualPreFilter;

  /** the flow context. */
  protected transient Actor m_FlowContext;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "pre-filter", "preFilter",
	    new AllFilter());
  }

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();

    m_ActualPreFilter = null;
  }

  /**
   * Sets the filter to use for pre-filtering the data.
   *
   * @param value	the filter
   */
  public void setPreFilter(Filter value) {
    m_PreFilter = value;
    reset();
  }

  /**
   * Returns the filter to use for pre-filtering the data.
   *
   * @return		the filter
   */
  public Filter getPreFilter() {
    return m_PreFilter;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String preFilterTipText() {
    return "The filter to use for pre-filtering the data.";
  }

  /**
   * Sets the flow context.
   *
   * @param value	the context
   */
  public void setFlowContent(Actor value) {
    m_FlowContext = value;
  }

  /**
   * Returns the flow context.
   *
   * @return		the context, null if not available
   */
  public Actor getFlowContext() {
    return m_FlowContext;
  }

  /**
   * Performs the some pre-checks whether the data is actually suitable.
   *
   * @param data	the instance to check
   */
  protected void preCheck(Instance data) {
    if (data == null)
      throw new IllegalStateException("Data is null!");
  }

  /**
   * Performs the some pre-checks whether the data is actually suitable.
   *
   * @param data	the instances to clean
   */
  protected void preCheck(Instances data) {
    if (data == null)
      throw new IllegalStateException("Data is null!");
  }

  /**
   * Performs the actual check.
   *
   * @param data	the instance to check
   * @return		null if ok, otherwise error message
   */
  protected abstract String performCheck(Instance data);

  /**
   * Performs the actual check.
   *
   * @param data	the instance to check
   * @return		null if ok, otherwise error message
   */
  protected abstract Instances performClean(Instances data);

  /**
   * Performs the filtering.
   *
   * @param data	the data to filter
   */
  protected Instances preFilter(Instances data) {
    Instances	result;

    try {
      m_ActualPreFilter = (Filter) OptionUtils.shallowCopy(m_PreFilter);
      m_ActualPreFilter.setInputFormat(data);
      result = Filter.useFilter(data, m_ActualPreFilter);
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }

    return result;
  }

  /**
   * Performs the filtering.
   *
   * @param data	the data to filter
   */
  protected Instance preFilter(Instance data) {
    Instance	result;
    Instances	dataset;

    if (m_PreFilter instanceof AllFilter)
      return data;

    if (m_ActualPreFilter == null) {
      getLogger().info("No pre-filter initialized, using single instance to perform initialization!");
      dataset = new Instances(data.dataset(), 1);
      dataset.add(data);
      preFilter(dataset);
    }

    try {
      m_ActualPreFilter.input(data);
      m_ActualPreFilter.batchFinished();
      result = m_ActualPreFilter.output();
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }

    return result;
  }

  /**
   * Performs the check.
   *
   * @param data	the instance to check
   * @return		null if ok, otherwise error message
   */
  public String check(Instance data) {
    preCheck(data);
    return performCheck(preFilter(data));
  }

  /**
   * Performs the clean.
   *
   * @param data	the instances to clean
   * @return		cleaned instances
   */
  public Instances clean(Instances data) {
    m_CleanInstancesError = null;
    preCheck(data);
    return performClean(preFilter(data));
  }

  /**
   * Checks whether there was an error during cleaning the Instances object.
   *
   * @return		true if there was an error
   */
  public boolean hasCleanInstancesError() {
    return (m_CleanInstancesError != null);
  }

  /**
   * Returns the error message that was recorded during cleaning the Instances
   * object (if any).
   *
   * @return		the error message, null if none occurred
   */
  public String getCleanInstancesError() {
    return m_CleanInstancesError;
  }

  /**
   * Compares this object with the specified object for order.  Returns a
   * negative integer, zero, or a positive integer as this object is less
   * than, equal to, or greater than the specified object.
   * <br><br>
   * Only compares the commandlines of the two objects.
   *
   * @param o 	the object to be compared.
   * @return  	a negative integer, zero, or a positive integer as this object
   *		is less than, equal to, or greater than the specified object.
   *
   * @throws ClassCastException 	if the specified object's type prevents it
   *         				from being compared to this object.
   */
  public int compareTo(Object o) {
    if (o == null)
      return 1;

    return OptionUtils.getCommandLine(this).compareTo(OptionUtils.getCommandLine(o));
  }

  /**
   * Returns whether the two objects are the same.
   * <br><br>
   * Only compares the commandlines of the two objects.
   *
   * @param o	the object to be compared
   * @return	true if the object is the same as this one
   */
  @Override
  public boolean equals(Object o) {
    return (compareTo(o) == 0);
  }

  /**
   * Returns a shallow copy of itself, i.e., based on the commandline options.
   *
   * @return		the shallow copy
   */
  public AbstractCleaner shallowCopy() {
    return shallowCopy(false);
  }

  /**
   * Returns a shallow copy of itself, i.e., based on the commandline options.
   *
   * @param expand	whether to expand variables to their current values
   * @return		the shallow copy
   */
  public AbstractCleaner shallowCopy(boolean expand) {
    return (AbstractCleaner) OptionUtils.shallowCopy(this, expand);
  }

  /**
   * Returns a list with classnames of filters.
   *
   * @return		the filter classnames
   */
  public static String[] getCleaners() {
    return ClassLister.getSingleton().getClassnames(AbstractCleaner.class);
  }

  /**
   * Instantiates the filter with the given options.
   *
   * @param classname	the classname of the filter to instantiate
   * @param options	the options for the filter
   * @return		the instantiated filter or null if an error occurred
   */
  public static AbstractCleaner forName(String classname, String[] options) {
    AbstractCleaner	result;

    try {
      result = (AbstractCleaner) OptionUtils.forName(AbstractCleaner.class, classname, options);
    }
    catch (Exception e) {
      e.printStackTrace();
      result = null;
    }

    return result;
  }

  /**
   * Instantiates the filter from the given commandline
   * (i.e., classname and optional options).
   *
   * @param cmdline	the classname (and optional options) of the
   * 			filter to instantiate
   * @return		the instantiated filter
   * 			or null if an error occurred
   */
  public static AbstractCleaner forCommandLine(String cmdline) {
    return (AbstractCleaner) AbstractOptionConsumer.fromString(ArrayConsumer.class, cmdline);
  }
}
