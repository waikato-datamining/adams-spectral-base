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
 * AbstractMultiSpectrumFilter.java
 * Copyright (C) 2014-2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.multifilter;

import adams.core.ClassLister;
import adams.core.CleanUpHandler;
import adams.core.ShallowCopySupporter;
import adams.core.option.AbstractOptionConsumer;
import adams.core.option.AbstractOptionHandler;
import adams.core.option.ArrayConsumer;
import adams.core.option.OptionUtils;
import adams.data.NotesHandler;
import adams.data.filter.OptionalProcessingInfoUpdate;
import adams.data.id.IDHandler;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;

/**
 * Abstract base class for multi-spectrum filters.
 *
 * Derived classes only have to override the <code>processData()</code>
 * method. The <code>reset()</code> method can be used to reset an
 * algorithms internal state, e.g., after setting options, which invalidate
 * the previously generated data.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractMultiSpectrumFilter
  extends AbstractOptionHandler
  implements Comparable, CleanUpHandler, ShallowCopySupporter<AbstractMultiSpectrumFilter>, OptionalProcessingInfoUpdate {

  /** for serialization. */
  private static final long serialVersionUID = 3610605513320220903L;

  /** whether to suppress updating of ID. */
  protected boolean m_DontUpdateID;

  /** whether to suppress updating of processing information. */
  protected boolean m_DontUpdateProcessingInfo;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "no-id-update", "dontUpdateID",
      false);

    m_OptionManager.add(
      "no-processing-info-update", "dontUpdateProcessingInfo",
      false);
  }

  /**
   * Sets whether ID update is suppressed.
   *
   * @param value 	true if to suppress
   */
  public void setDontUpdateID(boolean value) {
    m_DontUpdateID = value;
    reset();
  }

  /**
   * Returns whether ID update is suppressed.
   *
   * @return 		true if suppressed
   */
  public boolean getDontUpdateID() {
    return m_DontUpdateID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String dontUpdateIDTipText() {
    return "If enabled, suppresses updating the ID of " + IDHandler.class.getName() + " data containers.";
  }

  /**
   * Sets whether processing information update is suppressed.
   *
   * @param value 	true if to suppress
   */
  @Override
  public void setDontUpdateProcessingInfo(boolean value) {
    m_DontUpdateProcessingInfo = value;
    reset();
  }

  /**
   * Returns whether processing information update is suppressed.
   *
   * @return 		true if suppressed
   */
  @Override
  public boolean getDontUpdateProcessingInfo() {
    return m_DontUpdateProcessingInfo;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  @Override
  public String dontUpdateProcessingInfoTipText() {
    return "If enabled, suppresses updating the processing information of " + NotesHandler.class.getName() + " data containers.";
  }

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
  public Spectrum filter(MultiSpectrum data) {
    Spectrum	result;

    checkData(data);
    result = processData(data);

    if (!m_DontUpdateID)
      result.setID(result.getID() + "'");

    if (!m_DontUpdateProcessingInfo)
      ((NotesHandler) result).getNotes().addProcessInformation(this);

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
  protected abstract Spectrum processData(MultiSpectrum data);

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
  public AbstractMultiSpectrumFilter shallowCopy() {
    return shallowCopy(false);
  }

  /**
   * Returns a shallow copy of itself, i.e., based on the commandline options.
   *
   * @param expand	whether to expand variables to their current values
   * @return		the shallow copy
   */
  public AbstractMultiSpectrumFilter shallowCopy(boolean expand) {
    return (AbstractMultiSpectrumFilter) OptionUtils.shallowCopy(this, expand);
  }

  /**
   * Returns a list with classnames of filters.
   *
   * @return		the filter classnames
   */
  public static String[] getFilters() {
    return ClassLister.getSingleton().getClassnames(AbstractMultiSpectrumFilter.class);
  }

  /**
   * Instantiates the filter with the given options.
   *
   * @param classname	the classname of the filter to instantiate
   * @param options	the options for the filter
   * @return		the instantiated filter or null if an error occurred
   */
  public static AbstractMultiSpectrumFilter forName(String classname, String[] options) {
    AbstractMultiSpectrumFilter	result;

    try {
      result = (AbstractMultiSpectrumFilter) OptionUtils.forName(AbstractMultiSpectrumFilter.class, classname, options);
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
  public static AbstractMultiSpectrumFilter forCommandLine(String cmdline) {
    return (AbstractMultiSpectrumFilter) AbstractOptionConsumer.fromString(ArrayConsumer.class, cmdline);
  }
}
