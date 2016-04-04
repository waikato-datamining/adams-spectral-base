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
 * SegmentedSavitzkyGolayDerivativeOrderTest.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package adams.core.discovery;

import adams.core.discovery.PropertyPath.PropertyContainer;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;
import weka.classifiers.functions.PLSClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.filters.unsupervised.attribute.SegmentedSavitzkyGolay;

/**
 * Tests the SegmentedSavitzkyGolayDerivativeOrder discovery handler. Use the following to run from command-line:<br>
 * knir.core.discovery.SegmentedSavitzkyGolayDerivativeOrderTest
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SegmentedSavitzkyGolayDerivativeOrderTest
  extends AbstractGeneticDiscoveryHandlerTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name the name of the test
   */
  public SegmentedSavitzkyGolayDerivativeOrderTest(String name) {
    super(name);
  }

  /**
   * Returns the handler instance to use for testing in the {@link #testPackUnpack()}
   * method.
   *
   * @return		the handler instance
   */
  @Override
  protected AbstractGeneticDiscoveryHandler getPackUnpackHandler() {
    return new SegmentedSavitzkyGolayDerivativeOrder();
  }

  /**
   * Returns the property container to use for testing in the {@link #testPackUnpack()}
   * method.
   *
   * @return		the handler instance
   */
  @Override
  protected PropertyContainer getPackUnpackContainer() {
    AbstractGeneticDiscoveryHandler	handler;
    PropertyDiscovery			discovery;

    handler = getPackUnpackHandler();
    discovery = getDiscovery();
    discovery.discover(new AbstractDiscoveryHandler[]{handler}, getRegressionObjects()[0]);

    return handler.getContainers().get(0);
  }

  /**
   * Returns the objects to use in the regression test.
   *
   * @return		the objects
   */
  @Override
  protected Object[] getRegressionObjects() {
    FilteredClassifier	result;

    result = new FilteredClassifier();
    result.setClassifier(new PLSClassifier());
    result.setFilter(new SegmentedSavitzkyGolay());

    return new Object[]{result, new SegmentedSavitzkyGolay()};
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  @Override
  protected AbstractDiscoveryHandler[] getRegressionSetups() {
    return new AbstractDiscoveryHandler[] {
      new SegmentedSavitzkyGolayDerivativeOrder(),
      new SegmentedSavitzkyGolayDerivativeOrder(),
    };
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(SegmentedSavitzkyGolayDerivativeOrderTest.class);
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
