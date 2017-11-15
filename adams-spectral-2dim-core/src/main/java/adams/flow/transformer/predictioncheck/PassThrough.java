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
 * PassThrough.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer.predictioncheck;

import adams.flow.container.EvaluationContainer;

/**
 * Does nothing, just passes the container through.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class PassThrough
  extends AbstractPredictionCheck {

  private static final long serialVersionUID = -641156085256546517L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Does nothing, just passes the container through.";
  }

  /**
   * Performs the actual checks on the prediction.
   *
   * @param cont	the container to check
   * @return		the updated container
   */
  @Override
  public EvaluationContainer doCheckPrediction(EvaluationContainer cont) {
    return cont;
  }
}
