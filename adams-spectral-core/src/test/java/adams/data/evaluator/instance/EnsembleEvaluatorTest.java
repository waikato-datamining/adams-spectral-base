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
 * EnsembleEvaluatorTest.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.data.evaluator.instance;

import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;
import weka.classifiers.Classifier;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.functions.LinearRegressionJ;

/**
 * Test class for the EnsembleEvaluator evaluator. Run from the command line with: <br><br>
 * java adams.data.evaluator.instance.EnsembleEvaluatorTest
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class EnsembleEvaluatorTest
  extends AbstractEvaluatorTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name the name of the test
   */
  public EnsembleEvaluatorTest(String name) {
    super(name);
  }

  /**
   * Returns the filenames (without path) of the input data files to use
   * in the regression test.
   *
   * @return		the filenames
   */
  @Override
  protected String[] getRegressionInputFiles() {
    return new String[]{
      "bolts.arff",
      "bolts.arff",
    };
  }

  /**
   * Returns the class indices for the input data files to be used in the
   * regression test.
   *
   * @return		the class indices ('first', 'last', 1-based, or empty string for none)
   */
  @Override
  protected String[] getRegressionInputClasses() {
    return new String[]{
      "last",
      "last",
    };
  }

  /**
   * Returns the split percentages for the input files (train/test; 0.6 = 60% for train).
   *
   * @return		the setups
   */
  @Override
  protected double[] getRegressionInputSplitPercentages() {
    return new double[]{
      0.8,
      0.8,
    };
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  @Override
  protected AbstractEvaluator[] getRegressionSetups() {
    EnsembleEvaluator[]	result;

    result = new EnsembleEvaluator[2];
    result[0] = new EnsembleEvaluator();
    result[1] = new EnsembleEvaluator();
    result[1].setClassifiers(
      new Classifier[]{
	new LinearRegressionJ(),
	new GaussianProcesses(),
      }
    );

    return result;
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(EnsembleEvaluatorTest.class);
  }

  /**
   * Runs the test from commandline.
   *
   * @param args	ignored
   */
  public static void main(String[] args) {
    Environment.setEnvironmentClass(Environment.class);
    runTest(suite());
  }
}
