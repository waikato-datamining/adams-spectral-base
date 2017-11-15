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
 * Range.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer.predictioncheck;

import adams.core.base.BaseInterval;
import adams.flow.container.EvaluationContainer;

import java.util.Map;

/**
 <!-- globalinfo-start -->
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Range
  extends AbstractPredictionCheck {

  private static final long serialVersionUID = -6595520022086963318L;

  /** the suffix for the passed flag. */
  public final static String SUFFIX_PASSED = ".passed";

  /** the evaluation to check. */
  protected String m_Evaluation;
  
  /** the allowed range. */
  protected BaseInterval m_Range;
  
  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Ensures that the specified (numeric) evaluator results are within a specified range.\n"
      + "Adds a new boolean evaluation to the container, with the same name as "
	+ "the checked one, but appended with the suffix '" + SUFFIX_PASSED + "'.\n"
	+ "E.g., when checking the evaluation 'sqr', 'sqr." + SUFFIX_PASSED + "' "
	+ "will get added, either with 'true' or 'false' as value.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "evaluation", "evaluation",
      "");

    m_OptionManager.add(
      "range", "range",
      new BaseInterval(BaseInterval.ALL));
  }

  /**
   * Sets the evaluation to check.
   *
   * @param value	the evaluation
   */
  public void setEvaluation(String value) {
    m_Evaluation = value;
    reset();
  }

  /**
   * Returns the evaluation to check.
   *
   * @return		the evaluation
   */
  public String getEvaluation() {
    return m_Evaluation;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String evaluationTipText() {
    return "The name of the evaluation value to check.";
  }

  /**
   * Sets the allowed range of the numeric value.
   *
   * @param value	the range
   */
  public void setRange(BaseInterval value) {
    m_Range = value;
    reset();
  }

  /**
   * Returns the allowed range of the numeric value.
   *
   * @return		the range
   */
  public BaseInterval getRange() {
    return m_Range;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rangeTipText() {
    return "The allowed range of the numeric value of the evaluation.";
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

    result = super.check(cont);

    if (result == null) {
      if (m_Evaluation.trim().isEmpty())
        result = "No evaluation specified!";
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
    Object			obj;

    evals = (Map<String,Object>) cont.getValue(EvaluationContainer.VALUE_EVALUATIONS);
    if (evals != null) {
      if (evals.containsKey(m_Evaluation)) {
        obj = evals.get(m_Evaluation);
        if (obj instanceof Number)
          evals.put(m_Evaluation + SUFFIX_PASSED, (m_Range.isInside(((Number) obj).doubleValue())));
      }
    }

    return cont;
  }
}
