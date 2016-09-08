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
 * SegmentedSavitzkyGolayDerivativeOrder.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package adams.core.discovery.genetic;

import adams.core.discovery.PropertyPath.PropertyContainer;
import weka.filters.unsupervised.attribute.SegmentedSavitzkyGolay;

/**
 * SegmentedSavitzkyGolay numPoints handler.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SegmentedSavitzkyGolayDerivativeOrder
  extends AbstractGeneticIntegerDiscoveryHandler {

  private static final long serialVersionUID = 5906809683195878500L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Handles the derivativeOrder parameter of the SegmentedSavitzkyGolay filter.";
  }

  /**
   * Returns the default minimum.
   *
   * @return		the default
   */
  @Override
  protected int getDefaultMinimum() {
    return 0;
  }

  /**
   * Returns the default maximum.
   *
   * @return		the default
   */
  @Override
  protected int getDefaultMaximum() {
    return 2;
  }

  /**
   * Returns the default list.
   *
   * @return		the default
   */
  protected String getDefaultList() {
    return "0 1 2";
  }

  /**
   * Returns the integer value from the property container.
   *
   * @param cont	the container
   * @return		the value
   */
  protected int getValue(PropertyContainer cont) {
    return ((SegmentedSavitzkyGolay) cont.getObject()).getDerivativeOrder();
  }

  /**
   * Sets the integer value in the property container.
   *
   * @param cont	the container
   * @param value	the value to set
   */
  protected void setValue(PropertyContainer cont, int value) {
    ((SegmentedSavitzkyGolay) cont.getObject()).setDerivativeOrder(value);
    int po = value + 1;
    if (po < 2)
      po = 2;
    ((SegmentedSavitzkyGolay) cont.getObject()).setPolynomialOrder(po);
  }

  /**
   * Checks whether this object is handled by this discovery handler.
   *
   * @param obj		the object to check
   * @return		true if handled
   */
  @Override
  protected boolean handles(Object obj) {
    return (obj instanceof SegmentedSavitzkyGolay);
  }
}
