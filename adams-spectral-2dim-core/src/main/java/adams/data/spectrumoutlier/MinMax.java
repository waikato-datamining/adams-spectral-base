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
 * MinMax.java
 * Copyright (C) 2008-2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumoutlier;

import adams.data.container.DataContainer;
import adams.data.outlier.AbstractOutlierDetector;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.Report;
import adams.data.report.ReportHandler;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Detects data containers where a report value is too high/low.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D &lt;int&gt; (property: debugLevel)
 * &nbsp;&nbsp;&nbsp;The greater the number the more additional info the scheme may output to
 * &nbsp;&nbsp;&nbsp;the console (0 = off).
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 *
 * <pre>-min &lt;double&gt; (property: min)
 * &nbsp;&nbsp;&nbsp;Min value of field in report.
 * &nbsp;&nbsp;&nbsp;default: 25.0
 * </pre>
 *
 * <pre>-max &lt;double&gt; (property: max)
 * &nbsp;&nbsp;&nbsp;Max value of field in report.
 * &nbsp;&nbsp;&nbsp;default: 40.0
 * </pre>
 *
 * <pre>-field &lt;adams.data.report.Field&gt; (property: field)
 * &nbsp;&nbsp;&nbsp;Field in report.
 * &nbsp;&nbsp;&nbsp;default: Toluene-d8\\tConc
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  Dale (dale at cs dot waikato dot ac dot nz)
 */
public class MinMax
  extends AbstractOutlierDetector<DataContainer> {

  /** for serialization. */
  private static final long serialVersionUID = 8061387654170301948L;

  /** the report field.*/
  protected Field m_Field;

  /** min. */
  protected double m_Min;

  /** whether to check the lower bound. */
  protected boolean m_CheckMin;

  /** max. */
  protected double m_Max;

  /** whether to check the upper bound. */
  protected boolean m_CheckMax;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Detects data containers where a report value is too high/low.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "field", "field",
      new Field("CAN1", DataType.NUMERIC));

    m_OptionManager.add(
      "min", "min",
      25.0);

    m_OptionManager.add(
      "check-min", "checkMin",
      true);

    m_OptionManager.add(
      "max", "max",
      40.0);

    m_OptionManager.add(
      "check-max", "checkMax",
      true);
  }

  /**
   * Sets the field.
   *
   * @param value	the field
   */
  public void setField(Field value) {
    m_Field = value;
    reset();
  }

  /**
   * Returns the field.
   *
   * @return 		the field
   */
  public Field getField() {
    return m_Field;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return         tip text for this property suitable for
   *             displaying in the GUI or for listing the options.
   */
  public String fieldTipText() {
    return "Field in report.";
  }

  /**
   * Sets the minimum.
   *
   * @param value	min
   */
  public void setMin(double value) {
    m_Min = value;
    reset();
  }

  /**
   * Returns the currently set minimum.
   *
   * @return 		the minimum
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
    return "Min value of field in report.";
  }

  /**
   * Sets whether to check the lower bound.
   *
   * @param value	true if to check
   */
  public void setCheckMin(boolean value) {
    m_CheckMin = value;
    reset();
  }

  /**
   * Returns whether to check the lower bound.
   *
   * @return 		true if to check
   */
  public boolean getCheckMin() {
    return m_CheckMin;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return         tip text for this property suitable for
   *             displaying in the GUI or for listing the options.
   */
  public String checkMinTipText() {
    return "If enabled, the lower bound (= min) will be checked.";
  }

  /**
   * Sets the max.
   *
   * @param value	min
   */
  public void setMax(double value) {
    m_Max = value;
    reset();
  }

  /**
   * Returns the currently set max.
   *
   * @return 		the max
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
    return "Max value of field in report.";
  }

  /**
   * Sets whether to check the upper bound.
   *
   * @param value	true if to check
   */
  public void setCheckMax(boolean value) {
    m_CheckMax = value;
    reset();
  }

  /**
   * Returns whether to check the upper bound.
   *
   * @return 		true if to check
   */
  public boolean getCheckMax() {
    return m_CheckMax;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return         tip text for this property suitable for
   *             displaying in the GUI or for listing the options.
   */
  public String checkMaxTipText() {
    return "If enabled, the upper bound (= max) will be checked.";
  }

  /**
   * Performs the actual detection.
   *
   * @param data	the data to process
   * @return		the detections
   */
  @Override
  protected List<String> processData(DataContainer data) {
    List<String>	result;
    String		msg;
    Report 		report;
    Double 		value;

    result = new ArrayList<>();
    msg    = "";
    report = null;
    if (data instanceof ReportHandler)
      report = ((ReportHandler) data).getReport();

    if (report == null) {
      msg = "No report available";
      result.add(msg);
    }
    else {
      value = report.getDoubleValue(m_Field);
      if (value == null) {
	msg = "Field '" + m_Field + "' not found";
	result.add(msg);
      }
      else {
	if (m_CheckMin) {
	  if (value < m_Min) {
	    msg = m_Field + " too small (< " + m_Min + ") : " + value;
	    result.add(msg);
	  }
	}
	if (m_CheckMax) {
	  if (value > m_Max) {
	    msg = m_Field + " too big (> " + m_Max + "): " + value;
	    result.add(msg);
	  }
	}
      }
    }
    if (isLoggingEnabled())
      getLogger().info(data + " - " + getClass().getName() + ": " + msg);

    return result;
  }
}
