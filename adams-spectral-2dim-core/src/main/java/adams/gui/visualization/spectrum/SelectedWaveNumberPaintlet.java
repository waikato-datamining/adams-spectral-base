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
 * SelectedWaveNumberPaintlet.java
 * Copyright (C) 2008-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.visualization.spectrum;

import adams.data.spectrum.SpectrumPoint;
import adams.gui.core.GUIHelper;
import adams.gui.event.PaintEvent.PaintMoment;
import adams.gui.visualization.core.AxisPanel;
import adams.gui.visualization.core.plot.Axis;

import java.awt.Color;
import java.awt.Graphics;

/**
 <!-- globalinfo-start -->
 * Paintlet for highlighting a selected SpectrumPoint point on a panel.
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
 * <pre>-stroke-thickness &lt;float&gt; (property: strokeThickness)
 * &nbsp;&nbsp;&nbsp;The thickness of the stroke.
 * &nbsp;&nbsp;&nbsp;default: 1.0
 * &nbsp;&nbsp;&nbsp;minimum: 0.01
 * </pre>
 *
 * <pre>-color &lt;java.awt.Color&gt; (property: color)
 * &nbsp;&nbsp;&nbsp;The color of the selected wave number.
 * &nbsp;&nbsp;&nbsp;default: #ff0000
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SelectedWaveNumberPaintlet
  extends AbstractSpectrumPaintlet {

  /** for serialization. */
  private static final long serialVersionUID = 4296847364394457330L;

  /** the point to paint. */
  protected SpectrumPoint m_Point;

  /** the color to paint the point with. */
  protected Color m_Color;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return "Paintlet for highlighting a selected SpectrumPoint point on a panel.";
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "color", "color",
	    Color.RED);
  }

  /**
   * Initializes the members.
   */
  protected void initialize() {
    super.initialize();

    m_Point = null;
  }

  /**
   * Sets the point to highlight, automatically repaints the panel.
   *
   * @param value	the point to highlight
   */
  public void setPoint(SpectrumPoint value) {
    if (    (value != m_Point)
	|| ((value != null) && !value.equals(m_Point)) ) {
      m_Point = value;
      memberChanged();
    }
  }

  /**
   * Returns the currently set point to highlight.
   *
   * @return		the point
   */
  public SpectrumPoint getPoint() {
    return m_Point;
  }

  /**
   * Sets the color to paint the point with.
   *
   * @param value	the color
   */
  public void setColor(Color value) {
    if (    (value != m_Color)
	|| ((value != null) && !value.equals(m_Color)) ) {
      m_Color = value;
      memberChanged();
    }
  }

  /**
   * Returns the currently set color to paint the point with.
   *
   * @return		the color
   */
  public Color getColor() {
    return m_Color;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String colorTipText() {
    return "The color of the selected wave number.";
  }

  /**
   * Returns when this paintlet is to be executed.
   *
   * @return		when this paintlet is to be executed
   */
  public PaintMoment getPaintMoment() {
    return PaintMoment.POST_PAINT;
  }

  /**
   * The paint routine of the paintlet.
   *
   * @param g		the graphics context to use for painting
   * @param moment	what {@link PaintMoment} is currently being painted
   */
  protected void doPerformPaint(Graphics g, PaintMoment moment) {
    AxisPanel		axisX;
    SpectrumPoint	point;

    point = getPoint();
    if (point != null) {
      axisX = getPanel().getPlot().getAxis(Axis.BOTTOM);
      g.setColor(getColor());
      GUIHelper.configureAntiAliasing(g, true);
      g.drawLine(
        axisX.valueToPos(point.getWaveNumber()),
        0,
        axisX.valueToPos(point.getWaveNumber()),
        getPanel().getHeight());
      g.drawString(
        axisX.valueToDisplay(point.getWaveNumber()),
        axisX.valueToPos(point.getWaveNumber()) + 10,
        10);
    }
  }
}
