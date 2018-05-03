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
 * TextOverlay.java
 * Copyright (C) 2015 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.visualization.threewaydata.heatmapviewer.overlay;

import adams.data.report.Field;
import adams.data.report.Report;
import adams.gui.core.Fonts;
import adams.gui.visualization.image.ImagePanel.PaintPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 <!-- globalinfo-start -->
 * Displays text as overlay, either the fixed text, or if empty, a report value.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-enabled &lt;boolean&gt; (property: enabled)
 * &nbsp;&nbsp;&nbsp;If enabled, this overlay is painted over the image.
 * &nbsp;&nbsp;&nbsp;default: true
 * </pre>
 * 
 * <pre>-x &lt;int&gt; (property: x)
 * &nbsp;&nbsp;&nbsp;The X position of the overlay (&gt;=0: absolute, -1: left, -2: center, -3: 
 * &nbsp;&nbsp;&nbsp;right).
 * &nbsp;&nbsp;&nbsp;default: -3
 * &nbsp;&nbsp;&nbsp;minimum: -3
 * </pre>
 * 
 * <pre>-y &lt;int&gt; (property: y)
 * &nbsp;&nbsp;&nbsp;The Y position of the overlay (&gt;=0: absolute, -1: top, -2: center, -3: bottom
 * &nbsp;&nbsp;&nbsp;).
 * &nbsp;&nbsp;&nbsp;default: -1
 * &nbsp;&nbsp;&nbsp;minimum: -3
 * </pre>
 * 
 * <pre>-width &lt;int&gt; (property: width)
 * &nbsp;&nbsp;&nbsp;The width of the bounding box around the text.
 * &nbsp;&nbsp;&nbsp;default: 100
 * &nbsp;&nbsp;&nbsp;minimum: -1
 * </pre>
 * 
 * <pre>-height &lt;int&gt; (property: height)
 * &nbsp;&nbsp;&nbsp;The height of the bounding box around the text.
 * &nbsp;&nbsp;&nbsp;default: 20
 * &nbsp;&nbsp;&nbsp;minimum: -1
 * </pre>
 * 
 * <pre>-offset-x &lt;int&gt; (property: offsetX)
 * &nbsp;&nbsp;&nbsp;The X offset for the text.
 * &nbsp;&nbsp;&nbsp;default: 4
 * </pre>
 * 
 * <pre>-offset-y &lt;int&gt; (property: offsetY)
 * &nbsp;&nbsp;&nbsp;The Y offset for the text.
 * &nbsp;&nbsp;&nbsp;default: -4
 * </pre>
 * 
 * <pre>-text &lt;java.lang.String&gt; (property: text)
 * &nbsp;&nbsp;&nbsp;The fixed text to output.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-report-field &lt;adams.data.report.Field&gt; (property: reportField)
 * &nbsp;&nbsp;&nbsp;The report field to display.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-text-color &lt;java.awt.Color&gt; (property: textColor)
 * &nbsp;&nbsp;&nbsp;The text color.
 * &nbsp;&nbsp;&nbsp;default: #ff0000
 * </pre>
 * 
 * <pre>-font &lt;java.awt.Font&gt; (property: font)
 * &nbsp;&nbsp;&nbsp;The font to use for the text.
 * &nbsp;&nbsp;&nbsp;default: helvetica-PLAIN-12
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class TextOverlay
  extends AbstractPositionableThreeWayDataOverlayWithDimensions {

  /** for serialization. */
  private static final long serialVersionUID = 805661569976845842L;

  /** the X offset of the overlay. */
  protected int m_OffsetX;

  /** the Y offset of the overlay. */
  protected int m_OffsetY;

  /** the fixed text. */
  protected String m_Text;

  /** the report value. */
  protected Field m_ReportField;

  /** the color of the text. */
  protected Color m_TextColor;
  
  /** the font to use. */
  protected Font m_Font;

  /**
   * Returns a string describing the object.
   *
   * @return 		a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return 
	"Displays text as overlay, either the fixed text, or if empty, a report value.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "offset-x", "offsetX",
	    4);

    m_OptionManager.add(
	    "offset-y", "offsetY",
	    -4);

    m_OptionManager.add(
	    "text", "text",
	    "");

    m_OptionManager.add(
	    "report-field", "reportField",
	    new Field());

    m_OptionManager.add(
	    "text-color", "textColor",
	    Color.RED);

    m_OptionManager.add(
	    "font", "font",
             Fonts.getSansFont());
  }

  /**
   * Returns the default height for the overlay.
   *
   * @return		the default height
   */
  @Override
  protected int getDefaultHeight() {
    return 20;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  @Override
  public String heightTipText() {
    return "The height of the bounding box around the text.";
  }

  /**
   * Returns the default width for the overlay.
   *
   * @return		the default width
   */
  @Override
  protected int getDefaultWidth() {
    return 100;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  @Override
  public String widthTipText() {
    return "The width of the bounding box around the text.";
  }

  /**
   * Sets the X offset for the text.
   *
   * @param value 	the X offset
   */
  public void setOffsetX(int value) {
    m_OffsetX = value;
    reset();
  }

  /**
   * Returns the currently set X offset for the text.
   *
   * @return 		the X offset
   */
  public int getOffsetX() {
    return m_OffsetX;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String offsetXTipText() {
    return "The X offset for the text.";
  }

  /**
   * Sets the Y offset of the text.
   *
   * @param value 	the Y offset
   */
  public void setOffsetY(int value) {
    m_OffsetY = value;
    reset();
  }

  /**
   * Returns the currently set Y offset of the text.
   *
   * @return 		the Y offset
   */
  public int getOffsetY() {
    return m_OffsetY;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String offsetYTipText() {
    return "The Y offset for the text.";
  }

  /**
   * Sets the fixed text to output.
   *
   * @param value	the text
   */
  public void setText(String value) {
    m_Text = value;
    reset();
  }

  /**
   * Returns the fixed text to output.
   *
   * @return		the text
   */
  public String getText() {
    return m_Text;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String textTipText() {
    return "The fixed text to output.";
  }

  /**
   * Sets the report field to output.
   *
   * @param value	the field
   */
  public void setReportField(Field value) {
    m_ReportField = value;
    reset();
  }

  /**
   * Returns the report field to output.
   *
   * @return		the field
   */
  public Field getReportField() {
    return m_ReportField;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String reportFieldTipText() {
    return "The report field to display.";
  }

  /**
   * Sets the color for the text.
   *
   * @param value	the text color
   */
  public void setTextColor(Color value) {
    m_TextColor = value;
    reset();
  }

  /**
   * Returns the color for the text.
   *
   * @return		the text color
   */
  public Color getTextColor() {
    return m_TextColor;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String textColorTipText() {
    return "The text color.";
  }

  /**
   * Sets the font for the text.
   *
   * @param value	the font
   */
  public void setFont(Font value) {
    m_Font = value;
    reset();
  }

  /**
   * Returns the font for the text.
   *
   * @return		the font
   */
  public Font getFont() {
    return m_Font;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String fontTipText() {
    return "The font to use for the text.";
  }

  /**
   * Notifies the overlay that the image has changed.
   *
   * @param panel	the panel this overlay belongs to
   */
  @Override
  protected synchronized void doImageChanged(PaintPanel panel) {
  }

  /**
   * Performs the actual painting.
   * 
   * @param panel	the associated viewer
   * @param g		the graphics context
   * @param x		the actual x coordinate
   * @param y		the actual y coordinate
   */
  @Override
  protected void doPaintOverlay(PaintPanel panel, Graphics g, int x, int y) {
    String	str;
    Report	report;

    str = "";
    if (m_Text.length() > 0)
      str = m_Text;
    else if (m_DataPanel.getData().hasReport()) {
      report = m_DataPanel.getData().getReport();
      if (report.hasValue(m_ReportField))
	str = "" + report.getValue(m_ReportField);
    }

    g.setColor(m_TextColor);
    g.setFont(m_Font);
    g.drawString(str, x + m_OffsetX, y + m_OffsetY);
  }
}
