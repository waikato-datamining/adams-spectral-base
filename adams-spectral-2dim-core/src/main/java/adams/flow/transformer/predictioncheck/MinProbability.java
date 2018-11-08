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
 * MinProbability.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer.predictioncheck;

import adams.core.QuickInfoHelper;
import adams.flow.container.EvaluationContainer;

import java.util.HashMap;
import java.util.Map;

/**
 <!-- globalinfo-start -->
 * Ensures that the highest probability on the class distribution is at least the specified value.<br>
 * Adds a new boolean evaluation to the container with the name 'minprobability.passed'.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-minimum &lt;double&gt; (property: minimum)
 * &nbsp;&nbsp;&nbsp;The minimum probability (included) that the highest probability in the class
 * &nbsp;&nbsp;&nbsp;distribution must achieve.
 * &nbsp;&nbsp;&nbsp;default: 0.0
 * &nbsp;&nbsp;&nbsp;minimum: 0.0
 * &nbsp;&nbsp;&nbsp;maximum: 1.0
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class MinProbability
  extends AbstractPredictionCheck {

  private static final long serialVersionUID = -6595520022086963318L;

  /** the suffix for the passed flag. */
  public final static String PASSED = "minprobability.passed";

  /** the minimum probability. */
  protected double m_Minimum;
  
  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Ensures that the highest probability on the class distribution is at least the specified value.\n"
      + "Adds a new boolean evaluation to the container with the name '" + PASSED + "'.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "minimum", "minimum",
      0.0, 0.0, 1.0);
  }

  /**
   * Sets the minimum probability (included) that the highest probability
   * in the class distribution must achieve.
   *
   * @param value	the minimum
   */
  public void setMinimum(double value) {
    m_Minimum = value;
    reset();
  }

  /**
   * Returns the minimum probability (included) that the highest probability
   * in the class distribution must achieve.
   *
   * @return		the minimum
   */
  public double getMinimum() {
    return m_Minimum;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String minimumTipText() {
    return "The minimum probability (included) that the highest probability in the class distribution must achieve.";
  }

  /**
   * Returns a quick info about the object, which can be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  public String getQuickInfo() {
    return QuickInfoHelper.toString(this, "minimum", m_Minimum, ">= ");
  }

  /**
   * Hook method for checks.
   *
   * @param cont	the container to check
   * @return		null if successful, otherwise error message
   */
  @Override
  protected String check(EvaluationContainer cont) {
    String	result;
    double[]	dist;

    result = super.check(cont);

    if (result == null) {
      dist = (double[]) cont.getValue(EvaluationContainer.VALUE_DISTRIBUTION);
      if (dist == null)
        result = "No class distribution available!";
    }

    return result;
  }

  /**
   * Performs the actual checks on the prediction.
   *
   * @param cont	the container to check
   * @return		the updated container
   */
  @Override
  public EvaluationContainer doCheckPrediction(EvaluationContainer cont) {
    Map<String,Object> 		evals;
    double[]			dist;
    int				maxIndex;

    dist  = (double[]) cont.getValue(EvaluationContainer.VALUE_DISTRIBUTION);
    evals = (Map<String,Object>) cont.getValue(EvaluationContainer.VALUE_EVALUATIONS);
    if (evals == null) {
      evals = new HashMap<>();
      cont.setValue(EvaluationContainer.VALUE_EVALUATIONS, evals);
    }
    maxIndex = weka.core.Utils.maxIndex(dist);
    evals.put(PASSED, dist[maxIndex] >= m_Minimum);

    return cont;
  }
}
