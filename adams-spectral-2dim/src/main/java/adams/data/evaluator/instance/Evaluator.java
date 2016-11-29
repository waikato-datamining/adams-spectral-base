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
 * Evaluator.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.data.evaluator.instance;

import adams.core.CleanUpHandler;
import adams.core.ShallowCopySupporter;
import adams.core.option.OptionHandler;
import weka.core.Instance;
import weka.core.Instances;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Interface for evaluators.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface Evaluator
  extends OptionHandler, Serializable, Comparable, CleanUpHandler,
          ShallowCopySupporter<Evaluator> {

  /**
   * Sets the replacement string for missing evaluations.
   *
   * @param value	the replacement
   */
  public void setMissingEvaluation(float value);

  /**
   * Returns the replacement string for missing evaluations.
   *
   * @return		the replacement
   */
  public float getMissingEvaluation();

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String missingEvaluationTipText();

  /**
   * Performs the check.
   *
   * @param data	the instances to check
   * @return	evaluation metric
   */
  public boolean build(Instances data);

  /**
   * Performs the check.
   *
   * @param data	the instance to check
   * @return		evaluation metrics, {@link #getMissingEvaluation()} in case
   * 			the class value is missing
   */
  public HashMap<String,Float> evaluate(Instance data);

}
