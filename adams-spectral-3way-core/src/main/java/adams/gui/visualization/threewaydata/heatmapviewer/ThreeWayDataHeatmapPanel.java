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
 * ThreeWayDataPanel.java
 * Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */
package adams.gui.visualization.threewaydata.heatmapviewer;

import adams.core.Properties;
import adams.data.conversion.Conversion;
import adams.data.conversion.HeatmapToBufferedImage;
import adams.data.conversion.MultiConversion;
import adams.data.conversion.ThreeWayDataToHeatmap;
import adams.data.image.AbstractImageContainer;
import adams.data.threeway.ThreeWayData;
import adams.gui.core.BasePanel;
import adams.gui.core.BaseScrollPane;
import adams.gui.core.BaseSplitPane;
import adams.gui.core.ColorHelper;
import adams.gui.core.SearchPanel;
import adams.gui.core.SearchPanel.LayoutType;
import adams.gui.event.SearchEvent;
import adams.gui.visualization.core.AbstractColorGradientGenerator;
import adams.gui.visualization.core.BiColorGenerator;
import adams.gui.visualization.image.ImagePanel;
import adams.gui.visualization.image.selectionshape.RectanglePainter;
import adams.gui.visualization.report.ReportFactory;
import adams.gui.visualization.threewaydata.heatmapviewer.overlay.AbstractThreeWayDataOverlay;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Panel for displaying a single 3-way data structure.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class ThreeWayDataHeatmapPanel
  extends BasePanel {

  /** for serialization. */
  private static final long serialVersionUID = 1897625268125110563L;

  /** the setup for the panel. */
  protected static Properties m_Properties;

  /** the owner. */
  protected ThreeWayDataHeatmapViewerPanel m_Owner;

  /** the data on display. */
  protected ThreeWayData m_Data;

  /** the panel for displaying the data as image. */
  protected ImagePanel m_DataImage;

  /** the report of the data. */
  protected ReportFactory.Table m_ReportTable;

  /** the search panel for the data report. */
  protected SearchPanel m_SearchPanel;

  /** the split pane for image/spreadsheet and report. */
  protected BaseSplitPane m_SplitPane;

  /** the color generator to use. */
  protected AbstractColorGradientGenerator m_ColorGenerator;

  /** the color to use for missing values. */
  protected Color m_MissingValueColor;

  /**
   * Initializes the panel.
   *
   * @param owner	the owner
   */
  public ThreeWayDataHeatmapPanel(ThreeWayDataHeatmapViewerPanel owner) {
    super();

    m_Owner = owner;
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    Properties	props;

    super.initialize();

    props                = getProperties();
    m_Data               = new ThreeWayData();
    m_ColorGenerator     = AbstractColorGradientGenerator.forCommandLine(props.getProperty("Image.GradientColorGenerator", new BiColorGenerator().toCommandLine()));
    m_MissingValueColor  = props.getColor("Image.MissingValueColor", ColorHelper.valueOf("#88ff0000"));
  }

  /**
   * Initializes the widgets.
   */
  @Override
  protected void initGUI() {
    Properties		props;
    JPanel		panel;
    RectanglePainter	painter;

    super.initGUI();

    props = getProperties();

    setLayout(new BorderLayout());


    m_SplitPane = new BaseSplitPane();
    m_SplitPane.setDividerLocation(props.getInteger("Panel.DividerLocation", 600));
    add(m_SplitPane, BorderLayout.CENTER);

    painter = new RectanglePainter();
    painter.setColor(Color.RED);
    m_DataImage = new ImagePanel();
    m_DataImage.setSelectionEnabled(true);
    m_DataImage.setSelectionShapePainter(painter);
    m_SplitPane.setLeftComponent(m_DataImage);

    m_ReportTable = new ReportFactory.Table();

    m_SearchPanel = new SearchPanel(LayoutType.HORIZONTAL, true, "_Search", true, null);
    m_SearchPanel.setMinimumChars(2);
    m_SearchPanel.addSearchListener((SearchEvent e) -> {
      search(m_SearchPanel.getSearchText(), m_SearchPanel.isRegularExpression());
    });

    panel = new JPanel(new BorderLayout());
    panel.add(new BaseScrollPane(m_ReportTable), BorderLayout.CENTER);
    panel.add(m_SearchPanel, BorderLayout.SOUTH);
    m_SplitPane.setRightComponent(panel);
  }

  /**
   * Returns the owner.
   *
   * @return		the owner
   */
  public ThreeWayDataHeatmapViewerPanel getOwner() {
    return m_Owner;
  }

  /**
   * Regenerates the image of the current heatmap and redisplays it.
   *
   * @return		null if everything OK, otherwiser error message
   */
  protected String refresh() {
    String			result;
    Properties			props;
    StringBuilder		errors;
    String			error;
    ThreeWayDataToHeatmap	tw2hm;
    HeatmapToBufferedImage	hm2bi;
    MultiConversion		multi;

    if (m_Data.size() == 0)
      return null;

    errors = new StringBuilder();
    props  = getProperties();

    tw2hm = new ThreeWayDataToHeatmap();

    hm2bi = new HeatmapToBufferedImage();
    hm2bi.setGenerator(m_ColorGenerator);
    hm2bi.setMissingValueColor(m_MissingValueColor);

    multi = new MultiConversion();
    multi.setSubConversions(new Conversion[]{tw2hm, hm2bi});
    multi.setInput(m_Data);
    result = multi.convert();
    if (result != null) {
      error = "Failed to generate image: " + result;
      if (errors.length() > 0)
	errors.append("\n");
      errors.append(error);
      System.err.println(error);
      m_DataImage.setCurrentImage((BufferedImage) null);
    }
    else {
      m_DataImage.setCurrentImage(((AbstractImageContainer) hm2bi.getOutput()).toBufferedImage());
      m_DataImage.setScale(props.getDouble("Image.Scale", -1.0));
    }

    if (errors.length() == 0)
      return null;
    else
      return errors.toString();
  }

  /**
   * Sets the data to display.
   *
   * @param value	the data to display
   */
  public void setData(ThreeWayData value) {
    StringBuilder	errors;
    String		error;

    if (value == null)
      return;

    m_Data = (ThreeWayData) value.getClone();
    errors = new StringBuilder();

    // image
    error = refresh();
    if (error != null)
      errors.append(error);

    // report
    m_ReportTable.setModel(new ReportFactory.Model(m_Data.getReport()));

    // display errors in owner's statusbar
    if ((errors.length() > 0) && (m_Owner != null))
      m_Owner.showStatus(errors.toString());
  }

  /**
   * The current data on display.
   *
   * @return		the data on display
   */
  public ThreeWayData getData() {
    return m_Data;
  }

  /**
   * Sets the generator for the color gradient.
   * 
   * @param value	the generator
   */
  public void setColorGenerator(AbstractColorGradientGenerator value) {
    m_ColorGenerator = value;
    refresh();
  }
  
  /**
   * Returns the generator for the color gradient.
   * 
   * @return		the generator
   */
  public AbstractColorGradientGenerator getColorGenerator() {
    return m_ColorGenerator;
  }

  /**
   * Adds the heatmap overlay.
   *
   * @param overlay     the overlay to add
   */
  public void addOverlay(AbstractThreeWayDataOverlay overlay) {
    overlay = (AbstractThreeWayDataOverlay) overlay.shallowCopy();
    overlay.setDataPanel(this);
    getImagePanel().addImageOverlay(overlay);
  }

  /**
   * Removes all overlays.
   */
  public void removeOverlays() {
    getImagePanel().clearImageOverlays();
  }

  /**
   * Sets the color to use for missing values.
   *
   * @param value	the color
   */
  public void setMissingValueColor(Color value) {
    m_MissingValueColor = value;
    refresh();
  }

  /**
   * Returns the color to use for missing values.
   *
   * @return		the color
   */
  public Color getMissingValueColor() {
    return m_MissingValueColor;
  }

  /**
   * Returns the database ID or filename as title.
   *
   * @return		the title
   */
  public String getTitle() {
    return m_Data.getID();
  }

  /**
   * Returns the underlying image panel
   *
   * @return		the panel
   */
  public ImagePanel getImagePanel() {
    return m_DataImage;
  }

  /**
   * Performs a search.
   *
   * @param text	the search text
   * @param isRegExp	whether the search text is a regular expression
   */
  public void search(String text, boolean isRegExp) {
    m_ReportTable.search(text, isRegExp);
  }

  /**
   * Sets whether to display the search panel or not.
   *
   * @param value	if true then the search panel is displayed
   */
  public void setSearchPanelVisible(boolean value) {
    m_SearchPanel.setVisible(value);
  }

  /**
   * Returns whether the search panel is visible.
   *
   * @return		true if the search panel is visible
   */
  public boolean isSearchPanelVisible() {
    return m_SearchPanel.isVisible();
  }

  /**
   * Returns the properties for this panel.
   *
   * @return		the properties file for this panel
   */
  public static synchronized Properties getProperties() {
    String 	props;

    if (m_Properties == null) {
      try {
	props = ThreeWayDataHeatmapViewerPanel.class.getName().replaceAll("\\.", "/") + ".props";
	m_Properties = Properties.read(props);
      }
      catch (Exception e) {
	m_Properties = new Properties();
      }
    }

    return m_Properties;
  }
  
  /**
   * Sets the zoom factor (0-16). Use -1 to fit inside panel.
   *
   * @param zoom	the zoom factor
   */
  public void setZoom(double zoom) {
    m_DataImage.setScale(zoom);
  }
  
  /**
   * Returns the zoom factor (0-16).
   * 
   * @return		the zoom factor
   */
  public double getZoom() {
    return m_DataImage.getScale();
  }

  /**
   * Returns whether the report table is visible.
   *
   * @return		true if visible
   */
  public boolean isReportVisible() {
    return !m_SplitPane.isRightComponentHidden();
  }

  /**
   * Sets the visibility state of the report table.
   *
   * @param value	true if visible
   */
  public void setReportVisible(boolean value) {
    m_SplitPane.setRightComponentHidden(!value);
  }
}
