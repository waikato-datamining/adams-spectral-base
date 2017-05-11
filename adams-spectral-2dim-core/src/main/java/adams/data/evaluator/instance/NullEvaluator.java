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
 * NullEvaluator.java
 * Copyright (C) 2011-2012 University of Waikato, Hamilton, New Zealand
 */
package adams.data.evaluator.instance;

import weka.core.Instance;
import weka.core.Instances;

/**
 <!-- globalinfo-start -->
 * Dummy evaluator, which always return 1.0 as evaluation result.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D &lt;int&gt; (property: debugLevel)
 * &nbsp;&nbsp;&nbsp;The greater the number the more additional info the scheme may output to
 * &nbsp;&nbsp;&nbsp;the console (0 = off).
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class NullEvaluator
  extends AbstractEvaluator {

  /** for serialization. */
  private static final long serialVersionUID = -6086808426732510366L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Dummy evaluator, which always return 1.0 as evaluation result.";
  }
  
  /**
   * Returns the default value in case of missing evaluations.
   * 
   * @return		the default value
   */
  @Override
  protected float getDefaultMissingEvaluation() {
    return Float.NaN;
  }

  /**
   * Performs the actual evaluation.
   *
   * @param data	the instance to check
   * @return		always 1.0
   */
  @Override
  protected Float performEvaluate(Instance data) {
    return 1.0f;
  }

  /**
   * Builds the evaluator.
   *
   * @param data	the instance to check
   * @return		always true
   */
  @Override
  protected boolean performBuild(Instances data) {
    return true;
  }

  /**
   * Cleans up data structures, frees up memory.
   */
  @Override
  public void cleanUp() {
  }
}
