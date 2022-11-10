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
 * SpectrumPaintletNumericField.java
 * Copyright (C) 2021-2022 University of Waikato, Hamilton, NZ
 */

package adams.gui.visualization.spectrum;

import adams.data.report.Field;
import adams.data.report.Report;
import adams.gui.visualization.core.BiColorGenerator;
import adams.gui.visualization.core.ColorGradientGenerator;

import java.awt.Color;

/**
 * Paintlet for painting the spectral graph.
 * Colors are determined by the value of the specified numeric field in the report.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumPaintletNumericField
  extends SpectrumPaintlet {

  private static final long serialVersionUID = -8532199500169281341L;

  /** the report field to get the numeric value from. */
  protected Field m_Field;

  /** the color gradient generator to use. */
  protected ColorGradientGenerator m_Generator;

  /** the default color if field is missing. */
  protected Color m_DefaultColor;

  /** the colors. */
  protected transient Color[] m_Colors;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Paintlet for painting the spectral graph.\n"
      + "Colors are determined by the value of the specified numeric field in the report.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "field", "field",
      new Field());

    m_OptionManager.add(
      "generator", "generator",
      new BiColorGenerator());

    m_OptionManager.add(
      "default-color", "defaultColor",
      Color.BLACK);
  }

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();

    m_Colors = null;
  }

  /**
   * Sets the numeric field to determine the color with.
   *
   * @param value	the numeric field
   */
  public void setField(Field value) {
    m_Field = value;
    memberChanged();
  }

  /**
   * Returns the numeric field to determine the color with.
   *
   * @return		the numeric field
   */
  public Field getField() {
    return m_Field;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String fieldTipText() {
    return "The numeric field to determine the color with.";
  }

  /**
   * Sets the color gradient generator to use.
   *
   * @param value	the generator
   */
  public void setGenerator(ColorGradientGenerator value) {
    m_Generator = value;
    memberChanged();
  }

  /**
   * Returns the color gradient generator to use.
   *
   * @return		the generator
   */
  public ColorGradientGenerator getGenerator() {
    return m_Generator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String generatorTipText() {
    return "The color gradient generator to use.";
  }

  /**
   * Sets the default color to use when then report field is missing or invalid.
   *
   * @param value	the color
   */
  public void setDefaultColor(Color value) {
    m_DefaultColor = value;
    memberChanged();
  }

  /**
   * Returns the default color to use when then report field is missing or invalid.
   *
   * @return		the color
   */
  public Color getDefaultColor() {
    return m_DefaultColor;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String defaultColorTipText() {
    return "The color to use if the report field is missing or not valid.";
  }

  /**
   * Returns the color for the data with the given index.
   *
   * @param index	the index of the spectrum
   * @return		the color for the spectrum
   */
  public Color getColor(int index) {
    Color			result;
    SpectrumContainerManager	manager;
    double			min;
    double			max;
    int				i;
    Report 			report;
    String			valueStr;
    double			value;
    double			range;
    double			inc;
    int				color;

    result = m_DefaultColor;

    if (m_Colors == null)
      m_Colors = m_Generator.generate();

    // determine min/max
    manager = getSpectrumPanel().getContainerManager();
    min     = Double.POSITIVE_INFINITY;
    max     = Double.NEGATIVE_INFINITY;
    for (i = 0; i < manager.count(); i++) {
      report = manager.get(i).getData().getReport();
      if ((report != null) && report.hasValue(m_Field)) {
        valueStr = "" + report.getValue(m_Field);
        try {
          value = Double.parseDouble(valueStr);
          min   = Math.min(min, value);
          max   = Math.max(max, value);
	}
	catch (Exception e) {
          if (isLoggingEnabled())
            getLogger().warning("Failed to parse: " + valueStr);
	}
      }
    }
    range = max - min;
    inc   = Double.NaN;
    if ((range > 0) && (m_Colors.length > 0))
      inc = range / m_Colors.length;

    // current color
    report = manager.get(index).getData().getReport();
    if ((report != null) && report.hasValue(m_Field)) {
      valueStr = "" + report.getValue(m_Field);
      try {
	value  = Double.parseDouble(valueStr);
	color  = (int) Math.floor((value - min) / inc);
	if (color >= m_Colors.length)
	  color = m_Colors.length - 1;
	result = m_Colors[color];
      }
      catch (Exception e) {
	if (isLoggingEnabled())
	  getLogger().warning("Failed to parse: " + valueStr);
      }
    }

    return result;
  }
}
