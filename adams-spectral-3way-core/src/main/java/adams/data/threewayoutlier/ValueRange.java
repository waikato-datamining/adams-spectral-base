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
 * ValueRange.java
 * Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */

package adams.data.threewayoutlier;

import adams.data.outlier.AbstractOutlierDetector;
import adams.data.threeway.L1Point;
import adams.data.threeway.L2Point;
import adams.data.threeway.ThreeWayData;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Detects data containers where a least one value is too low or too high.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-min &lt;double&gt; (property: min)
 * &nbsp;&nbsp;&nbsp;Minimum accepted value; use -Infinity to ignore bound.
 * &nbsp;&nbsp;&nbsp;default: -Infinity
 * </pre>
 *
 * <pre>-max &lt;double&gt; (property: max)
 * &nbsp;&nbsp;&nbsp;Maximum accepted value; use +Infinity to ignore bound.
 * &nbsp;&nbsp;&nbsp;default: Infinity
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  Dale (dale at cs dot waikato dot ac dot nz)
 */
public class ValueRange
  extends AbstractOutlierDetector<ThreeWayData> {

  /** for serialization. */
  private static final long serialVersionUID = 8061387654170301948L;

  /** min. */
  protected double m_Min;

  /** max. */
  protected double m_Max;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Detects data containers where a least one value is too low or too high.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "min", "min",
      Double.NEGATIVE_INFINITY);

    m_OptionManager.add(
      "max", "max",
      Double.POSITIVE_INFINITY);
  }

  /**
   * Sets the minimum.
   *
   * @param value	the minimum, {@link Double#NEGATIVE_INFINITY} for no bound
   */
  public void setMin(double value) {
    m_Min = value;
    reset();
  }

  /**
   * Returns the currently set minimum.
   *
   * @return 		the minimum, {@link Double#NEGATIVE_INFINITY} for no bound
   */
  public double getMin() {
    return m_Min;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return         tip text for this property suitable for
   *             displaying in the GUI or for listing the options.
   */
  public String minTipText() {
    return "Minimum accepted value; use -Infinity to ignore bound.";
  }

  /**
   * Sets the max.
   *
   * @param value	the maximum, {@link Double#POSITIVE_INFINITY\} for no bound
   */
  public void setMax(double value) {
    m_Max = value;
    reset();
  }

  /**
   * Returns the currently set max.
   *
   * @return 		the maximum, {@link Double#POSITIVE_INFINITY\} for no bound
   */
  public double getMax() {
    return m_Max;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return         tip text for this property suitable for
   *             displaying in the GUI or for listing the options.
   */
  public String maxTipText() {
    return "Maximum accepted value; use +Infinity to ignore bound.";
  }

  /**
   * Performs the actual detection.
   *
   * @param data	the data to process
   * @return		the detections
   */
  @Override
  protected List<String> processData(ThreeWayData data) {
    List<String>	result;
    String		msg;

    result = new ArrayList<>();
    for (L1Point l1: data) {
      for (L2Point l2: l1) {
        if (!Double.isInfinite(m_Min)) {
          if (l2.getData() < m_Min) {
            msg = l1.getX() + "/" + l1.getY() + "/" + l2.getZ() + ": below " + m_Min;
            result.add(msg);
            if (isLoggingEnabled())
              getLogger().info(data + " - " + getClass().getName() + ": " + msg);
          }
        }
        if (!Double.isInfinite(m_Max)) {
          if (l2.getData() > m_Max) {
            msg = l1.getX() + "/" + l1.getY() + "/" + l2.getZ() + ": above " + m_Max;
            result.add(msg);
            if (isLoggingEnabled())
              getLogger().info(data + " - " + getClass().getName() + ": " + msg);
          }
        }
      }
    }

    return result;
  }
}
