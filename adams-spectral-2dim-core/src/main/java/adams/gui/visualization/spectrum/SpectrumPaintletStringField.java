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
 * Copyright (C) 2021 University of Waikato, Hamilton, NZ
 */

package adams.gui.visualization.spectrum;

import adams.core.base.BaseRegExp;
import adams.data.report.Field;
import adams.data.report.Report;
import adams.gui.visualization.core.ColorProvider;
import adams.gui.visualization.core.DefaultColorProvider;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Paintlet for painting the spectral graph.
 * Colors are determined by the value extracted from the specified string field in the report.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumPaintletStringField
  extends SpectrumPaintlet {

  private static final long serialVersionUID = -8532199500169281341L;

  /** the default regular expression. */
  public final static String DEFAULT_REGEXP = "(.*)";

  /** the report field to get the string value from. */
  protected Field m_Field;

  /** the regular expression to apply (extracts first group). */
  protected BaseRegExp m_RegExp;

  /** the color provider to use. */
  protected ColorProvider m_ColorProvider;

  /** the default color if field is missing. */
  protected Color m_DefaultColor;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Paintlet for painting the spectral graph.\n"
      + "Colors are determined by the value extracted from the specified string field in the report.";
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
      "regexp", "regExp",
      new BaseRegExp(DEFAULT_REGEXP));

    m_OptionManager.add(
      "color-provider", "colorProvider",
      new DefaultColorProvider());

    m_OptionManager.add(
      "default-color", "defaultColor",
      Color.BLACK);
  }

  /**
   * Sets the string field to determine the color with.
   *
   * @param value	the string field
   */
  public void setField(Field value) {
    m_Field = value;
    memberChanged();
  }

  /**
   * Returns the string field to determine the color with.
   *
   * @return		the string field
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
    return "The string field to determine the color with.";
  }

  /**
   * Sets regular expression to apply to the string field; the string obtained
   * from the first group is associated with a color.
   *
   * @param value	the expression
   */
  public void setRegExp(BaseRegExp value) {
    m_RegExp = value;
    memberChanged();
  }

  /**
   * Returns regular expression to apply to the string field; the string obtained
   * from the first group is associated with a color.
   *
   * @return		the expression
   */
  public BaseRegExp getRegExp() {
    return m_RegExp;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String regExpTipText() {
    return "The regular expression to apply to the string field; the string "
      + "obtained from the first group is associated with a color.";
  }

  /**
   * Sets the color provider to use.
   *
   * @param value 	the color provider
   */
  public void setColorProvider(ColorProvider value) {
    m_ColorProvider = value;
    memberChanged();
  }

  /**
   * Returns the color provider in use.
   *
   * @return 		the color provider
   */
  public ColorProvider getColorProvider() {
    return m_ColorProvider;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String colorProviderTipText() {
    return "The color provider that provides the colors for the determined string values.";
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
    return "The color to use if the report field is missing.";
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
    int				i;
    Report 			report;
    String			valueStr;
    Set<String> 		values;
    List<String> 		sorted;
    Map<String,Color>		colors;

    result = m_DefaultColor;

    // determine values/colors
    manager = getSpectrumPanel().getContainerManager();
    values  = new HashSet<>();
    for (i = 0; i < manager.count(); i++) {
      report = manager.get(i).getData().getReport();
      if ((report != null) && report.hasValue(m_Field)) {
        valueStr = "" + report.getValue(m_Field);
        if (!m_RegExp.getValue().equals(DEFAULT_REGEXP))
          valueStr = valueStr.replaceAll(m_RegExp.getValue(), "$1");
	values.add(valueStr);
      }
    }
    sorted = new ArrayList<>(values);
    Collections.sort(sorted);
    colors = new HashMap<>();
    m_ColorProvider.resetColors();
    for (String s: sorted)
      colors.put(s, m_ColorProvider.next());

    // current color
    report = manager.get(index).getData().getReport();
    if ((report != null) && report.hasValue(m_Field)) {
      valueStr = "" + report.getValue(m_Field);
      if (!m_RegExp.getValue().equals(DEFAULT_REGEXP))
	valueStr = valueStr.replaceAll(m_RegExp.getValue(), "$1");
      result = colors.getOrDefault(valueStr, result);
    }

    return result;
  }
}
