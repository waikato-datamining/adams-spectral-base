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
 * ThreeWayDataHeatmapDisplay.java
 * Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.sink;

import adams.core.QuickInfoHelper;
import adams.core.option.OptionUtils;
import adams.data.threeway.ThreeWayData;
import adams.flow.core.Token;
import adams.gui.core.BasePanel;
import adams.gui.visualization.core.AbstractColorGradientGenerator;
import adams.gui.visualization.core.BiColorGenerator;
import adams.gui.visualization.threewaydata.heatmapviewer.ThreeWayDataHeatmapPanel;
import adams.gui.visualization.threewaydata.heatmapviewer.overlay.AbstractThreeWayDataOverlay;

import javax.swing.JComponent;
import java.awt.BorderLayout;
import java.awt.Color;

/**
 <!-- globalinfo-start -->
 * Actor that displays 3-way data as heatmaps.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;adams.data.threeway.ThreeWayData<br>
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: ThreeWayDataHeatmapDisplay
 * </pre>
 *
 * <pre>-annotation &lt;adams.core.base.BaseAnnotation&gt; (property: annotations)
 * &nbsp;&nbsp;&nbsp;The annotations to attach to this actor.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-skip &lt;boolean&gt; (property: skip)
 * &nbsp;&nbsp;&nbsp;If set to true, transformation is skipped and the input token is just forwarded
 * &nbsp;&nbsp;&nbsp;as it is.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-stop-flow-on-error &lt;boolean&gt; (property: stopFlowOnError)
 * &nbsp;&nbsp;&nbsp;If set to true, the flow execution at this level gets stopped in case this
 * &nbsp;&nbsp;&nbsp;actor encounters an error; the error gets propagated; useful for critical
 * &nbsp;&nbsp;&nbsp;actors.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-silent &lt;boolean&gt; (property: silent)
 * &nbsp;&nbsp;&nbsp;If enabled, then no errors are output in the console; Note: the enclosing
 * &nbsp;&nbsp;&nbsp;actor handler must have this enabled as well.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-short-title &lt;boolean&gt; (property: shortTitle)
 * &nbsp;&nbsp;&nbsp;If enabled uses just the name for the title instead of the actor's full
 * &nbsp;&nbsp;&nbsp;name.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-display-in-editor &lt;boolean&gt; (property: displayInEditor)
 * &nbsp;&nbsp;&nbsp;If enabled displays the panel in a tab in the flow editor rather than in
 * &nbsp;&nbsp;&nbsp;a separate frame.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-width &lt;int&gt; (property: width)
 * &nbsp;&nbsp;&nbsp;The width of the dialog.
 * &nbsp;&nbsp;&nbsp;default: 1000
 * &nbsp;&nbsp;&nbsp;minimum: -1
 * </pre>
 *
 * <pre>-height &lt;int&gt; (property: height)
 * &nbsp;&nbsp;&nbsp;The height of the dialog.
 * &nbsp;&nbsp;&nbsp;default: 700
 * &nbsp;&nbsp;&nbsp;minimum: -1
 * </pre>
 *
 * <pre>-x &lt;int&gt; (property: x)
 * &nbsp;&nbsp;&nbsp;The X position of the dialog (&gt;=0: absolute, -1: left, -2: center, -3: right
 * &nbsp;&nbsp;&nbsp;).
 * &nbsp;&nbsp;&nbsp;default: -1
 * &nbsp;&nbsp;&nbsp;minimum: -3
 * </pre>
 *
 * <pre>-y &lt;int&gt; (property: y)
 * &nbsp;&nbsp;&nbsp;The Y position of the dialog (&gt;=0: absolute, -1: top, -2: center, -3: bottom
 * &nbsp;&nbsp;&nbsp;).
 * &nbsp;&nbsp;&nbsp;default: -1
 * &nbsp;&nbsp;&nbsp;minimum: -3
 * </pre>
 *
 * <pre>-writer &lt;adams.gui.print.JComponentWriter&gt; (property: writer)
 * &nbsp;&nbsp;&nbsp;The writer to use for generating the graphics output.
 * &nbsp;&nbsp;&nbsp;default: adams.gui.print.NullWriter
 * </pre>
 *
 * <pre>-color-generator &lt;adams.gui.visualization.core.AbstractColorGradientGenerator&gt; (property: colorGenerator)
 * &nbsp;&nbsp;&nbsp;The generator for the color gradient.
 * &nbsp;&nbsp;&nbsp;default: adams.gui.visualization.core.BiColorGenerator
 * </pre>
 *
 * <pre>-overlay &lt;adams.gui.visualization.threewaydata.heatmapviewer.overlay.AbstractThreeWayDataOverlay&gt; [-overlay ...] (property: overlays)
 * &nbsp;&nbsp;&nbsp;The overlay(s) to use.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-missing-value-color &lt;java.awt.Color&gt; (property: missingValueColor)
 * &nbsp;&nbsp;&nbsp;The color to use for missing values.
 * &nbsp;&nbsp;&nbsp;default: #00ffffff
 * </pre>
 *
 * <pre>-zoom &lt;double&gt; (property: zoom)
 * &nbsp;&nbsp;&nbsp;The zoom level in percent.
 * &nbsp;&nbsp;&nbsp;default: 100.0
 * &nbsp;&nbsp;&nbsp;minimum: -1.0
 * &nbsp;&nbsp;&nbsp;maximum: 1600.0
 * </pre>
 *
 * <pre>-show-report-table &lt;boolean&gt; (property: showReportTable)
 * &nbsp;&nbsp;&nbsp;Determines the visibility of the report table.
 * &nbsp;&nbsp;&nbsp;default: true
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class ThreeWayDataHeatmapDisplay
  extends AbstractGraphicalDisplay
  implements DisplayPanelProvider {

  /** for serialization. */
  private static final long serialVersionUID = -5963541661512220421L;

  /** the generator for the color gradient. */
  protected AbstractColorGradientGenerator m_ColorGenerator;

  /** the overlays to use. */
  protected AbstractThreeWayDataOverlay[] m_Overlays;

  /** the color for missing values. */
  protected Color m_MissingValueColor;

  /** the zoom level. */
  protected double m_Zoom;

  /** whether to show the report table. */
  protected boolean m_ShowReportTable;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Actor that displays 3-way data as heatmaps.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "color-generator", "colorGenerator",
      new BiColorGenerator());

    m_OptionManager.add(
      "overlay", "overlays",
      new AbstractThreeWayDataOverlay[0]);

    m_OptionManager.add(
      "missing-value-color", "missingValueColor",
      new Color(255, 255, 255, 0));

    m_OptionManager.add(
      "zoom", "zoom",
      100.0, -1.0, 1600.0);

    m_OptionManager.add(
      "show-report-table", "showReportTable",
      true);
  }

  /**
   * Returns the default width for the dialog.
   *
   * @return		the default width
   */
  @Override
  protected int getDefaultWidth() {
    return 1000;
  }

  /**
   * Returns the default height for the dialog.
   *
   * @return		the default height
   */
  @Override
  protected int getDefaultHeight() {
    return 700;
  }

  /**
   * Sets the generator for the color gradient.
   *
   * @param value 	the generator
   */
  public void setColorGenerator(AbstractColorGradientGenerator value) {
    m_ColorGenerator = value;
    reset();
  }

  /**
   * Returns the generator for the color gradient.
   *
   * @return 		the generator
   */
  public AbstractColorGradientGenerator getColorGenerator() {
    return m_ColorGenerator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String colorGeneratorTipText() {
    return "The generator for the color gradient.";
  }

  /**
   * Sets the overlays to use.
   *
   * @param value 	the overlays
   */
  public void setOverlays(AbstractThreeWayDataOverlay[] value) {
    m_Overlays = value;
    reset();
  }

  /**
   * Returns the overlays to use.
   *
   * @return 		the overlays
   */
  public AbstractThreeWayDataOverlay[] getOverlays() {
    return m_Overlays;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String overlaysTipText() {
    return "The overlay(s) to use.";
  }

  /**
   * Sets the color for missing values.
   *
   * @param value	the color
   */
  public void setMissingValueColor(Color value) {
    m_MissingValueColor = value;
    reset();
  }

  /**
   * Returns the color for missing values.
   *
   * @return		the color
   */
  public Color getMissingValueColor() {
    return m_MissingValueColor;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String missingValueColorTipText() {
    return "The color to use for missing values.";
  }

  /**
   * Sets the zoom level in percent (0-1600).
   *
   * @param value 	the zoom, -1 to fit window, or 0-1600
   */
  public void setZoom(double value) {
    if ((value == -1) || ((value > 0) && (value <= 1600))) {
      m_Zoom = value;
      reset();
    }
    else {
      getLogger().warning("Zoom must -1 to fit window or 0 < x < 1600, provided: " + value);
    }
  }

  /**
   * Returns the zoom level in percent.
   *
   * @return 		the zoom
   */
  public double getZoom() {
    return m_Zoom;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String zoomTipText() {
    return "The zoom level in percent.";
  }

  /**
   * Sets whether the report value is visible.
   *
   * @param value 	true if visible
   */
  public void setShowReportTable(boolean value) {
    m_ShowReportTable = value;
    reset();
  }

  /**
   * Returns whether the report table is visible.
   *
   * @return 		true if visible
   */
  public boolean getShowReportTable() {
    return m_ShowReportTable;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String showReportTableTipText() {
    return "Determines the visibility of the report table.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result  = super.getQuickInfo();
    result += QuickInfoHelper.toString(this, "colorGenerator", m_ColorGenerator, ", generator: ");
    result += QuickInfoHelper.toString(this, "missingValueColor", m_MissingValueColor, ", missing: ");
    result += QuickInfoHelper.toString(this, "zoom", m_Zoom, ", zoom: ");

    return result;
  }

  /**
   * Clears the content of the panel.
   */
  @Override
  public void clearPanel() {
    if (m_Panel != null)
      ((ThreeWayDataHeatmapPanel) m_Panel).setData(null);
  }

  /**
   * Creates the panel to display in the dialog.
   *
   * @return		the panel
   */
  @Override
  protected BasePanel newPanel() {
    ThreeWayDataHeatmapPanel	result;

    result = new ThreeWayDataHeatmapPanel(null);
    result.setColorGenerator(m_ColorGenerator);
    for (AbstractThreeWayDataOverlay overlay: m_Overlays)
      result.addOverlay(overlay);
    result.setMissingValueColor(m_MissingValueColor);
    result.setZoom(m_Zoom / 100.0);
    result.setReportVisible(m_ShowReportTable);

    return result;
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		<!-- flow-accepts-start -->adams.data.threeway.ThreeWayData.class<!-- flow-accepts-end -->
   */
  @Override
  public Class[] accepts() {
    return new Class[]{ThreeWayData.class};
  }

  /**
   * Displays the token (the panel and dialog have already been created at
   * this stage).
   *
   * @param token	the token to display
   */
  @Override
  protected void display(Token token) {
    ((ThreeWayDataHeatmapPanel) m_Panel).setData((ThreeWayData) token.getPayload());
  }

  /**
   * Creates a new display panel for the token.
   *
   * @param token	the token to display in a new panel, can be null
   * @return		the generated panel
   */
  @Override
  public AbstractDisplayPanel createDisplayPanel(Token token) {
    AbstractDisplayPanel	result;

    result = new AbstractComponentDisplayPanel(getClass().getSimpleName()) {
      private static final long serialVersionUID = -9139363702312636367L;

      protected ThreeWayDataHeatmapPanel m_Panel;
      @Override
      protected void initGUI() {
	super.initGUI();
	setLayout(new BorderLayout());
	m_Panel = new ThreeWayDataHeatmapPanel(null);
	m_Panel.setColorGenerator((AbstractColorGradientGenerator) OptionUtils.shallowCopy(m_ColorGenerator));
        for (AbstractThreeWayDataOverlay overlay: m_Overlays)
          m_Panel.addOverlay(overlay);
	m_Panel.setMissingValueColor(m_MissingValueColor);
	m_Panel.setZoom(m_Zoom / 100.0);
	m_Panel.setReportVisible(m_ShowReportTable);
	add(m_Panel, BorderLayout.CENTER);
      }
      @Override
      public void display(Token token) {
	m_Panel.setData((ThreeWayData) token.getPayload());
      }
      @Override
      public void cleanUp() {
      }
      @Override
      public void clearPanel() {
      }
      @Override
      public JComponent supplyComponent() {
	return m_Panel;
      }
    };
    
    if (token != null)
      result.display(token);

    return result;
  }

  /**
   * Returns whether the created display panel requires a scroll pane or not.
   *
   * @return		true if the display panel requires a scroll pane
   */
  @Override
  public boolean displayPanelRequiresScrollPane() {
    return false;
  }
}
