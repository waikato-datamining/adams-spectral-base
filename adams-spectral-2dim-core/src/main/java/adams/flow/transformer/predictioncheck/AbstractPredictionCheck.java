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
 * AbstractPredictionCheck.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer.predictioncheck;

import adams.core.option.AbstractOptionHandler;
import adams.flow.container.EvaluationContainer;

/**
 * Ancestor for prediction checks.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractPredictionCheck
  extends AbstractOptionHandler {

  private static final long serialVersionUID = 7570611371164863558L;

  /**
   * Hook method for checks.
   *
   * @param cont	the container to check
   * @return		null if successful, otherwise error message
   */
  protected String check(EvaluationContainer cont) {
    if (cont == null)
      return "No evaluation container provided!";
    return null;
  }

  /**
   * Performs the actual checks on the prediction.
   *
   * @param cont	the container to check
   * @return		the updated container
   */
  public abstract EvaluationContainer doCheckPrediction(EvaluationContainer cont);

  /**
   * Performs checks on the prediction.
   *
   * @param cont	the container to check
   * @return		the updated container
   */
  public EvaluationContainer checkPrediction(EvaluationContainer cont) {
    String		msg;

    msg = check(cont);
    if (msg != null)
      throw new IllegalStateException(msg);
    return  doCheckPrediction(cont);
  }
}
