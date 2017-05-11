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
 * AbstractEvaluator.java
 * Copyright (C) 2009-2017 University of Waikato, Hamilton, New Zealand
 */

package adams.data.evaluator.instance;

import adams.core.ClassLister;
import adams.core.option.AbstractOptionConsumer;
import adams.core.option.AbstractOptionHandler;
import adams.core.option.ArrayConsumer;
import adams.core.option.OptionUtils;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;

/**
 * Abstract base class for evaluator handling <code>weka.core.Instance</code>
 * objects.
 *
 * Derived classes only have to override the <code>check(Instance)</code>
 * method. The <code>reset()</code> method can be used to reset an
 * algorithms internal state, e.g., after setting options.
 *
 * @author  dale (dale at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public abstract class AbstractEvaluator
  extends AbstractOptionHandler
  implements Evaluator {

  /** for serialization. */
  private static final long serialVersionUID = -582592424411578426L;

  /** the default metric. */
  public final static String DEFAULT_METRIC = "DEFAULT";

  /** the value to return in case no evaluation can be performed. */
  protected float m_MissingEvaluation;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	"missing-evaluation", "missingEvaluation",
	getDefaultMissingEvaluation());
  }
  
  /**
   * Returns the default value in case of missing evaluations.
   * 
   * @return		the default value
   */
  protected abstract float getDefaultMissingEvaluation();

  /**
   * Sets the replacement string for missing evaluations.
   *
   * @param value	the replacement
   */
  public void setMissingEvaluation(float value) {
    m_MissingEvaluation = value;
    reset();
  }

  /**
   * Returns the replacement string for missing evaluations.
   *
   * @return		the replacement
   */
  public float getMissingEvaluation() {
    return m_MissingEvaluation;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String missingEvaluationTipText() {
    return "The value to use as replacement for missing evaluations.";
  }

  /**
   * Performs the some pre-checks whether the data is actually suitable.
   * Data needs to be non-null and a class attribute defined.
   *
   * @param data	the instance to check
   */
  protected void preCheck(Instance data) {
    if (data == null)
      throw new IllegalStateException("Data is null!");
    if (data.classIndex() == -1)
      throw new IllegalStateException("No class attribute set!");
  }

  /**
   * Performs the some pre-checks whether the data is actually suitable.
   *
   * @param data	the instance to check
   */
  protected void preCheck(Instances data) {
    if (data == null)
      throw new IllegalStateException("Data is null!");
  }

  /**
   * Performs the actual evaluation.
   * <br><br>
   * Default implementation returns {@link #m_MissingEvaluation}.
   *
   * @param data	the instance to check
   * @return		evaluation metric, {@link #m_MissingEvaluation} in case
   * 			the class value is missing
   */
  protected Float performEvaluate(Instance data) {
    return m_MissingEvaluation;
  }

  /**
   * Performs the actual evaluation, allowing return of multiple evaluation metrics.
   * <br><br>
   * Default implementation returns null.
   *
   * @param data	the instance to check
   * @return		evaluation metrics, {@link #m_MissingEvaluation} in case
   * 			the class value is missing
   */
  protected HashMap<String,Float> performMultiEvaluate(Instance data) {
    return null;
  }

  /**
   * Builds the evaluator.
   *
   * @param data	the instance to check
   * @return		true if build successful
   */
  protected abstract boolean performBuild(Instances data);

  /**
   * Performs the check. First attempts to retrieve data from {@link #performMultiEvaluate(Instance)}
   * and, if it receives null, calls {@link #performEvaluate(Instance)}.
   *
   * @param data	the instance to check
   * @return		evaluation metrics, {@link #m_MissingEvaluation} in case
   * 			the class value is missing
   */
  public HashMap<String,Float> evaluate(Instance data) {
    HashMap<String,Float>   result;
    float                   eval;

    preCheck(data);

    if (data.classIsMissing()) {
      getLogger().warning("No class value, cannot evaluate ('" + data.classAttribute().name() + "')!");
      result = new HashMap<>();
      result.put(DEFAULT_METRIC, m_MissingEvaluation);
    }
    else {
      result = performMultiEvaluate(data);
      if (result == null) {
        eval = performEvaluate(data);
        result = new HashMap<>();
        result.put(DEFAULT_METRIC, eval);
      }
    }

    return result;
  }

  /**
   * Performs the check.
   *
   * @param data	the instances to check
   * @return	evaluation metric
   */
  public boolean build(Instances data) {
    preCheck(data);
    return performBuild(data);
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
  @Override
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
  @Override
  public AbstractEvaluator shallowCopy() {
    return shallowCopy(false);
  }

  /**
   * Returns a shallow copy of itself, i.e., based on the commandline options.
   *
   * @param expand	whether to expand variables to their current values
   * @return		the shallow copy
   */
  @Override
  public AbstractEvaluator shallowCopy(boolean expand) {
    return (AbstractEvaluator) OptionUtils.shallowCopy(this, expand);
  }

  /**
   * Returns a list with classnames of filters.
   *
   * @return		the filter classnames
   */
  public static String[] getEvaluators() {
    return ClassLister.getSingleton().getClassnames(AbstractEvaluator.class);
  }

  /**
   * Instantiates the filter with the given options.
   *
   * @param classname	the classname of the filter to instantiate
   * @param options	the options for the filter
   * @return		the instantiated filter or null if an error occurred
   */
  public static AbstractEvaluator forName(String classname, String[] options) {
    AbstractEvaluator	result;

    try {
      result = (AbstractEvaluator) OptionUtils.forName(AbstractEvaluator.class, classname, options);
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
  public static AbstractEvaluator forCommandLine(String cmdline) {
    return (AbstractEvaluator) AbstractOptionConsumer.fromString(ArrayConsumer.class, cmdline);
  }
}
