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
 * Expression.java
 * Copyright (C) 2025 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer.predictioncheck;

import adams.core.QuickInfoHelper;
import adams.core.Utils;
import adams.core.base.BaseString;
import adams.flow.container.EvaluationContainer;
import adams.parser.BooleanExpression;
import adams.parser.BooleanExpressionText;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Evaluates the specified boolean expression. The expression makes all numeric evaluation values available as symbols for the calculation.<br>
 * Adds a new boolean evaluation with suffix .passed to the container with the result of the expression and one with the suffix .expression with the expanded expression, using the specified evaluation name.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * &nbsp;&nbsp;&nbsp;min-user-mode: Expert
 * </pre>
 *
 * <pre>-expression &lt;adams.parser.BooleanExpressionText&gt; (property: expression)
 * &nbsp;&nbsp;&nbsp;The boolean expression to evaluate; all evaluations are available as symbols.
 * &nbsp;&nbsp;&nbsp;default: true
 * </pre>
 *
 * <pre>-evaluation &lt;java.lang.String&gt; (property: evaluation)
 * &nbsp;&nbsp;&nbsp;The name of the evaluation to store the result under.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Expression
  extends AbstractPredictionCheck {

  private static final long serialVersionUID = -6595520022086963318L;

  /** the suffix for the expression. */
  public final static String SUFFIX_EXPRESSION = ".expression";

  /** the suffix for the expression. */
  public final static String SUFFIX_PASSED = ".passed";

  /** the expression to evaluate. */
  protected BooleanExpressionText m_Expression;

  /** the evaluation to check. */
  protected String m_Evaluation;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Evaluates the specified boolean expression. The expression makes all numeric evaluation values available as symbols for the calculation.\n"
	+ "Adds a new boolean evaluation with suffix " + SUFFIX_PASSED + " to the container with the result of the expression and one with the suffix "
	+ SUFFIX_EXPRESSION + " with the expanded expression, using the specified evaluation name.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "expression", "expression",
      new BooleanExpressionText("true"));

    m_OptionManager.add(
      "evaluation", "evaluation",
      "");
  }

  /**
   * Sets the boolean expression to evaluate.
   *
   * @param value	the expression
   */
  public void setExpression(BooleanExpressionText value) {
    m_Expression = value;
    reset();
  }

  /**
   * Returns the boolean expression to evaluate.
   *
   * @return		the expression
   */
  public BooleanExpressionText getExpression() {
    return m_Expression;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String expressionTipText() {
    return "The boolean expression to evaluate; all evaluations are available as symbols.";
  }

  /**
   * Sets the evaluation name to store the result under.
   *
   * @param value	the evaluation
   */
  public void setEvaluation(String value) {
    m_Evaluation = value;
    reset();
  }

  /**
   * Returns the evaluation name to store the result under.
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
    return "The name of the evaluation to store the result under.";
  }

  /**
   * Returns a quick info about the object, which can be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  public String getQuickInfo() {
    String	result;

    result  = QuickInfoHelper.toString(this, "expression", m_Expression, "expression: ");
    result += QuickInfoHelper.toString(this, "evaluation", m_Evaluation, ", evaluation: ");

    return result;
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
      if (m_Expression.isEmpty())
	result = "No expression specified!";
      else if (m_Evaluation.trim().isEmpty())
	result = "No evaluation name specified!";
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
    BooleanExpression		parser;
    List<BaseString> 		symbols;
    String			expanded;

    evals = (Map<String,Object>) cont.getValue(EvaluationContainer.VALUE_EVALUATIONS);
    if (evals != null) {
      expanded = m_Expression.getValue();
      symbols  = new ArrayList<>();
      for (String key: evals.keySet()) {
	if (Utils.isDouble("" + evals.get(key))) {
	  symbols.add(new BaseString(key + "=" + evals.get(key)));
	  expanded = expanded.replace(key, "" + evals.get(key));
	}
      }
      evals.put(m_Evaluation + SUFFIX_EXPRESSION, expanded);
      parser = new BooleanExpression();
      parser.setExpression(m_Expression.getValue());
      parser.setSymbols(symbols.toArray(new BaseString[0]));
      try {
	evals.put(m_Evaluation + SUFFIX_PASSED, parser.evaluate());
      }
      catch (Exception e) {
	evals.put(m_Evaluation + SUFFIX_PASSED, "failed");
	getLogger().log(Level.SEVERE, "Failed to evaluate expression '" + m_Expression + "' using symbols:\n" + Utils.flatten(symbols, "\n"), e);
      }
    }

    return cont;
  }
}
