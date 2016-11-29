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
 * AbstractPostProcessor.java
 * Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 */

package adams.data.postprocessor.instances;

import adams.core.ClassLister;
import adams.core.CleanUpHandler;
import adams.core.option.AbstractOptionConsumer;
import adams.core.option.AbstractOptionHandler;
import adams.core.option.ArrayConsumer;
import adams.core.option.OptionUtils;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Abstract base class for postprocessing <code>weka.core.Instances</code>
 * objects.
 *
 * @author  dale (dale at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public abstract class AbstractPostProcessor
  extends AbstractOptionHandler
  implements Comparable, CleanUpHandler {

  /** for serialization. */
  private static final long serialVersionUID = -582592424411578426L;

  /**
   * Performs some pre-checks whether the data is actually suitable.
   *
   * @param data	the dataset to check
   */
  protected void preCheck(Instances data) {
    if (data == null)
      throw new IllegalStateException("Dataset is null!");
  }

  /**
   * Performs the actual postprocessing.
   *
   * @param data	the dataset to process
   * @return		the processed dataset
   */
  protected abstract Instances performPostProcess(Instances data);

  /**
   * Performs the processing.
   *
   * @param data	the dataset to postprocess
   * @return		the processed dataset
   */
  public Instances postProcess(Instances data) {
    preCheck(data);
    return performPostProcess(data);
  }

  /**
   * Performs some pre-checks whether the data is actually suitable.
   *
   * @param data	the instance to check
   */
  protected void preCheck(Instance data) {
    if (data == null)
      throw new IllegalStateException("Instance is null!");
  }

  /**
   * Performs the actual postprocessing.
   *
   * @param data	the instance to process
   * @return		the processed instance
   */
  protected abstract Instance performPostProcess(Instance data);

  /**
   * Performs the processing.
   *
   * @param data	the instance to postprocess
   * @return		the processed instance
   */
  public Instance postProcess(Instance data) {
    preCheck(data);
    return performPostProcess(data);
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
  public boolean equals(Object o) {
    return (compareTo(o) == 0);
  }

  /**
   * Returns a shallow copy of itself, i.e., based on the commandline options.
   *
   * @return		the shallow copy
   */
  public AbstractPostProcessor shallowCopy() {
    return shallowCopy(false);
  }

  /**
   * Returns a shallow copy of itself, i.e., based on the commandline options.
   *
   * @param expand	whether to expand variables to their current values
   * @return		the shallow copy
   */
  public AbstractPostProcessor shallowCopy(boolean expand) {
    return (AbstractPostProcessor) OptionUtils.shallowCopy(this, expand);
  }

  /**
   * Returns a list with classnames of processors.
   *
   * @return		the processor classnames
   */
  public static String[] getEvaluators() {
    return ClassLister.getSingleton().getClassnames(AbstractPostProcessor.class);
  }

  /**
   * Instantiates the processor with the given options.
   *
   * @param classname	the classname of the processor to instantiate
   * @param options	the options for the processor
   * @return		the instantiated processor or null if an error occurred
   */
  public static AbstractPostProcessor forName(String classname, String[] options) {
  	AbstractPostProcessor	result;

    try {
      result = (AbstractPostProcessor) OptionUtils.forName(AbstractPostProcessor.class, classname, options);
    }
    catch (Exception e) {
      e.printStackTrace();
      result = null;
    }

    return result;
  }

  /**
   * Instantiates the processor from the given commandline
   * (i.e., classname and optional options).
   *
   * @param cmdline	the classname (and optional options) of the
   * 			processor to instantiate
   * @return		the instantiated processor
   * 			or null if an error occurred
   */
  public static AbstractPostProcessor forCommandLine(String cmdline) {
    return (AbstractPostProcessor) AbstractOptionConsumer.fromString(ArrayConsumer.class, cmdline);
  }
}
