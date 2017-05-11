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
 * WekaClassifierBasedEvaluator.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.data.evaluator.instance;

import weka.classifiers.Classifier;

/**
 * Interface for evaluators that make use of classifiers.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface WekaClassifierBasedEvaluator
  extends Evaluator {

  /**
   * Sets the classifier to use.
   *
   * @param value 	the classifier
   */
  public void setClassifier(Classifier value);

  /**
   * Returns the classifier in use.
   *
   * @return 		the classifier
   */
  public Classifier getClassifier();

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String classifierTipText();
}
