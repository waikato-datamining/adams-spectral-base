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
 * SavitzkyGolayRange.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package weka.filters.unsupervised.attribute;

import adams.core.option.AbstractOptionHandler;

/**
 * Range definition for a SavitzkyGolay filter setup.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SavitzkyGolayRange
  extends AbstractOptionHandler {

  private static final long serialVersionUID = -63683154445730452L;

  /** the start of the range. */
  protected int m_Start;

  /** the end of the range. */
  protected int m_End;

  /** the polynomial order. */
  protected int m_PolynomialOrder = 2;

  /** the order of the derivative. */
  protected int m_DerivativeOrder = 1;

  /** the number of points (window size = numpoints*2 + 1). */
  protected int m_NumPoints;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Defines a range of attributes to work on with a specific window size.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "start", "start",
      1, 1, null);

    m_OptionManager.add(
      "end", "end",
      100, 1, null);

    m_OptionManager.add(
      "polynomial", "polynomialOrder",
      2, 2, null);

    m_OptionManager.add(
      "derivative", "derivativeOrder",
      1, 0, null);

    m_OptionManager.add(
      "num-points", "numPoints",
      3);
  }

  /**
   * Sets the first amplitude.
   *
   * @param value	the first amplitude
   */
  public void setStart(int value) {
    m_Start = value;
    reset();
  }

  /**
   * Returns the first amplitude
   *
   * @return		the first amplitude
   */
  public int getStart() {
    return m_Start;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String startTipText() {
    return "The first amplitude to include (1-based index).";
  }

  /**
   * Sets the last amplitude.
   *
   * @param value	the last amplitude
   */
  public void setEnd(int value) {
    m_End = value;
    reset();
  }

  /**
   * Returns the last amplitude
   *
   * @return		the last amplitude
   */
  public int getEnd() {
    return m_End;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String endTipText() {
    return "The last amplitude to include (1-based index).";
  }

  /**
   * Sets the polynomial order.
   *
   * @param value 	the order
   */
  public void setPolynomialOrder(int value) {
    if (getOptionManager().isValid("polynomialOrder", value)) {
      m_PolynomialOrder = value;
      reset();
    }
  }

  /**
   * Returns the polynominal order.
   *
   * @return 		the order
   */
  public int getPolynomialOrder() {
    return m_PolynomialOrder;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String polynomialOrderTipText() {
    return "The polynomial order to use, must be at least 2.";
  }

  /**
   * Sets the order of the derivative.
   *
   * @param value 	the order
   */
  public void setDerivativeOrder(int value) {
    if (getOptionManager().isValid("derivativeOrder", value)) {
      m_DerivativeOrder = value;
      reset();
    }
  }

  /**
   * Returns the order of the derivative.
   *
   * @return 		the order
   */
  public int getDerivativeOrder() {
    return m_DerivativeOrder;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String derivativeOrderTipText() {
    return "The order of the derivative to use, >= 0.";
  }

  /**
   * Sets the number of points for the window (window = numPoints*2 + 1).
   *
   * @param value	the number of points
   */
  public void setNumPoints(int value) {
    m_NumPoints = value;
    reset();
  }

  /**
   * Returns the number of points for the window (window = numPoints*2 + 1).
   *
   * @return		the number of points
   */
  public int getNumPoints() {
    return m_NumPoints;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String numPointsTipText() {
    return "The number of points for the window (window = numPoints*2 + 1).";
  }
}
