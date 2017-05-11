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
 * AbstractMultiSpectrumReportFilter.java
 * Copyright (C) 2015 University of Waikato, Hamilton, New Zealand
 */

package adams.data.multireportfilter;

import adams.core.ClassLister;
import adams.core.CleanUpHandler;
import adams.core.ShallowCopySupporter;
import adams.core.option.AbstractOptionConsumer;
import adams.core.option.AbstractOptionHandler;
import adams.core.option.ArrayConsumer;
import adams.core.option.OptionUtils;
import adams.data.spectrum.MultiSpectrum;

/**
 * Abstract base class for multi-spectrum report filters.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 7774 $
 */
public abstract class AbstractMultiSpectrumReportFilter
  extends AbstractOptionHandler
  implements Comparable, CleanUpHandler, ShallowCopySupporter<AbstractMultiSpectrumReportFilter> {

  /** for serialization. */
  private static final long serialVersionUID = 3610605513320220903L;

  /**
   * Resets the filter.
   * Derived classes must call this method in set-methods of parameters to
   * assure the invalidation of previously generated data.
   */
  @Override
  public void reset() {
    super.reset();
  }

  /**
   * Cleans up data structures, frees up memory.
   * Sets the input and generated data to null.
   */
  public void cleanUp() {
    reset();
  }

  /**
   * Frees up memory in a "destructive" non-reversible way.
   * <br><br>
   * Calls cleanUp() and cleans up the options.
   */
  @Override
  public void destroy() {
    cleanUp();
    super.destroy();
  }

  /**
   * Returns the filtered data.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  public MultiSpectrum filter(MultiSpectrum data) {
    MultiSpectrum	result;

    checkData(data);
    result = processData(data);

    if (result != null)
      result.getNotes().addProcessInformation(this);

    return result;
  }

  /**
   * The default implementation only checks whether there is any data set.
   *
   * @param data	the data to filter
   */
  protected void checkData(MultiSpectrum data) {
    if (data == null)
      throw new IllegalStateException("No input data provided!");
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data, null if failed to generate output
   */
  protected abstract MultiSpectrum processData(MultiSpectrum data);

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
  public AbstractMultiSpectrumReportFilter shallowCopy() {
    return shallowCopy(false);
  }

  /**
   * Returns a shallow copy of itself, i.e., based on the commandline options.
   *
   * @param expand	whether to expand variables to their current values
   * @return		the shallow copy
   */
  public AbstractMultiSpectrumReportFilter shallowCopy(boolean expand) {
    return (AbstractMultiSpectrumReportFilter) OptionUtils.shallowCopy(this, expand);
  }

  /**
   * Returns a list with classnames of filters.
   *
   * @return		the filter classnames
   */
  public static String[] getFilters() {
    return ClassLister.getSingleton().getClassnames(AbstractMultiSpectrumReportFilter.class);
  }

  /**
   * Instantiates the filter with the given options.
   *
   * @param classname	the classname of the filter to instantiate
   * @param options	the options for the filter
   * @return		the instantiated filter or null if an error occurred
   */
  public static AbstractMultiSpectrumReportFilter forName(String classname, String[] options) {
    AbstractMultiSpectrumReportFilter result;

    try {
      result = (AbstractMultiSpectrumReportFilter) OptionUtils.forName(AbstractMultiSpectrumReportFilter.class, classname, options);
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
  public static AbstractMultiSpectrumReportFilter forCommandLine(String cmdline) {
    return (AbstractMultiSpectrumReportFilter) AbstractOptionConsumer.fromString(ArrayConsumer.class, cmdline);
  }
}
