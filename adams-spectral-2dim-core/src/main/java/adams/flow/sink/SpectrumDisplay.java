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
 * SpectrumDisplay.java
 * Copyright (C) 2009-2017 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.sink;

import adams.data.conversion.MultiSpectrumToSpectra;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;
import adams.flow.core.DataPlotUpdaterHandler;
import adams.flow.core.Token;
import adams.flow.sink.spectrumdisplay.AbstractPlotUpdater;
import adams.flow.sink.spectrumdisplay.SimplePlotUpdater;
import adams.gui.core.BasePanel;
import adams.gui.visualization.core.ColorProvider;
import adams.gui.visualization.core.DefaultColorProvider;
import adams.gui.visualization.spectrum.AbstractSpectrumPaintlet;
import adams.gui.visualization.spectrum.SpectrumContainer;
import adams.gui.visualization.spectrum.SpectrumContainerManager;
import adams.gui.visualization.spectrum.SpectrumExplorer;
import adams.gui.visualization.spectrum.SpectrumPaintlet;

import javax.swing.JComponent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Actor that displays spectra or multi-spectra.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;adams.data.spectrum.Spectrum<br>
 * &nbsp;&nbsp;&nbsp;adams.data.spectrum.MultiSpectrum<br>
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
 * &nbsp;&nbsp;&nbsp;default: SpectrumDisplay
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
 * &nbsp;&nbsp;&nbsp;If set to true, the flow gets stopped in case this actor encounters an error;
 * &nbsp;&nbsp;&nbsp; useful for critical actors.
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
 * &nbsp;&nbsp;&nbsp;default: 600
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
 * <pre>-color-provider &lt;adams.gui.visualization.core.ColorProvider&gt; (property: colorProvider)
 * &nbsp;&nbsp;&nbsp;The color provider in use for coloring the spectra.
 * &nbsp;&nbsp;&nbsp;default: adams.gui.visualization.core.DefaultColorProvider
 * </pre>
 * 
 * <pre>-paintlet &lt;adams.gui.visualization.spectrum.AbstractSpectrumPaintlet&gt; (property: paintlet)
 * &nbsp;&nbsp;&nbsp;The paintlet to use for drawing the spectra.
 * &nbsp;&nbsp;&nbsp;default: adams.gui.visualization.spectrum.SpectrumPaintlet
 * </pre>
 * 
 * <pre>-show-side-panel &lt;boolean&gt; (property: showSidePanel)
 * &nbsp;&nbsp;&nbsp;If enabled, the side panel with the list of loaded spectra gets displayed.
 * &nbsp;&nbsp;&nbsp;default: true
 * </pre>
 * 
 * <pre>-zoom-overview &lt;boolean&gt; (property: zoomOverview)
 * &nbsp;&nbsp;&nbsp;If enabled, a zoom overview panel gets displayed as well.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-plot-updater &lt;adams.flow.core.AbstractDataPlotUpdater&gt; (property: plotUpdater)
 * &nbsp;&nbsp;&nbsp;The updating strategy for the plot.
 * &nbsp;&nbsp;&nbsp;default: adams.flow.sink.spectrumdisplay.SimplePlotUpdater
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2245 $
 */
public class SpectrumDisplay
  extends AbstractGraphicalDisplay
  implements DisplayPanelProvider, DataPlotUpdaterHandler<AbstractPlotUpdater> {

  /**
   * Panel to be used in {@link DisplayPanelManager} sink.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 2245 $
   */
  protected class DisplayPanel
    extends AbstractComponentDisplayPanel
    implements MergeableDisplayPanel<DisplayPanel> {

    private static final long serialVersionUID = 7384093089760722339L;

    protected SpectrumExplorer m_Panel;

    protected DisplayPanel(String name) {
      super(name);
    }

    @Override
    protected void initGUI() {
      super.initGUI();
      setLayout(new BorderLayout());
      m_Panel = new SpectrumExplorer();
      m_Panel.getContainerManager().setReloadable(false);
      m_Panel.getContainerManager().setAllowRemoval(false);
      m_Panel.getSpectrumPanel().setSidePanelVisible(m_ShowSidePanel);
      m_Panel.getSpectrumPanel().setDataPaintlet(m_Paintlet.shallowCopy(true));
      m_Panel.setZoomOverviewPanelVisible(m_ZoomOverview);
      add(m_Panel, BorderLayout.CENTER);
    }

    @Override
    public void display(Token token) {
      SpectrumContainer		cont;
      SpectrumContainerManager	manager;
      MultiSpectrum		multi;
      Spectrum			spec;
      int			i;
      MultiSpectrumToSpectra	conv;
      Spectrum[]		specs;
      String			msg;

      cont    = null;
      manager = m_Panel.getContainerManager();
      manager.startUpdate();
      if (token.getPayload() instanceof MultiSpectrum) {
	multi = (MultiSpectrum) token.getPayload();
	conv = new MultiSpectrumToSpectra();
	conv.setInput(multi);
	msg = conv.convert();
	if (msg == null) {
	  specs = (Spectrum[]) conv.getOutput();
	  for (i = 0; i < specs.length; i++) {
	    spec = specs[i];
	    cont = manager.newContainer(spec);
	    manager.add(cont);
	  }
	}
	else {
	  getLogger().warning(msg);
	}
	conv.destroy();
      }
      else {
	spec = (Spectrum) token.getPayload();
	cont = manager.newContainer(spec);
	manager.add(cont);
      }

      if (cont != null)
	m_PlotUpdater.update(m_Panel.getSpectrumPanel(), cont);
    }

    @Override
    public void cleanUp() {
      m_Panel.getContainerManager().clear();
    }

    @Override
    public void clearPanel() {
      m_Panel.getContainerManager().clear();
    }

    @Override
    public JComponent supplyComponent() {
      return m_Panel;
    }

    @Override
    public void mergeWith(DisplayPanel other) {
      List<SpectrumContainer>		list;

      m_Panel.getContainerManager().startUpdate();
      list = other.m_Panel.getContainerManager().getAll();
      for (SpectrumContainer cont: list) {
	cont.setColor(Color.WHITE);
	m_Panel.getContainerManager().add(cont);
      }
      m_Panel.getContainerManager().finishUpdate();
    }
  }

  /** for serialization. */
  private static final long serialVersionUID = -4952322481934379763L;

  /** the color provider to use. */
  protected ColorProvider m_ColorProvider;

  /** the paintlet to use. */
  protected AbstractSpectrumPaintlet m_Paintlet;

  /** whether to show the side panel or not. */
  protected boolean m_ShowSidePanel;

  /** whether to display the zoom overview. */
  protected boolean m_ZoomOverview;

  /** the plot updater to use. */
  protected AbstractPlotUpdater m_PlotUpdater;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Actor that displays spectra or multi-spectra.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "color-provider", "colorProvider",
      new DefaultColorProvider());

    m_OptionManager.add(
      "paintlet", "paintlet",
      new SpectrumPaintlet());

    m_OptionManager.add(
      "show-side-panel", "showSidePanel",
      true);

    m_OptionManager.add(
      "zoom-overview", "zoomOverview",
      false);

    m_OptionManager.add(
      "plot-updater", "plotUpdater",
      new SimplePlotUpdater());
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
    return 600;
  }

  /**
   * Sets the color provider to use.
   *
   * @param value 	the color provider
   */
  public void setColorProvider(ColorProvider value) {
    m_ColorProvider = value;
    reset();
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
    return "The color provider in use for coloring the spectra.";
  }

  /**
   * Sets the paintlet to use.
   *
   * @param value 	the paintlet
   */
  public void setPaintlet(AbstractSpectrumPaintlet value) {
    m_Paintlet = value;
    reset();
  }

  /**
   * Returns the paintlet in use.
   *
   * @return 		the paintlet
   */
  public AbstractSpectrumPaintlet getPaintlet() {
    return m_Paintlet;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String paintletTipText() {
    return "The paintlet to use for drawing the spectra.";
  }

  /**
   * Sets whether to show the side panel or not.
   *
   * @param value 	if true the side panel gets displayed
   */
  public void setShowSidePanel(boolean value) {
    m_ShowSidePanel = value;
    reset();
  }

  /**
   * Returns whether to show the side panel or not.
   *
   * @return 		true if the side panel gets displayed
   */
  public boolean getShowSidePanel() {
    return m_ShowSidePanel;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String showSidePanelTipText() {
    return "If enabled, the side panel with the list of loaded spectra gets displayed.";
  }

  /**
   * Sets whether to display the zoom overview.
   *
   * @param value 	if true then the zoom overview will get displayed
   */
  public void setZoomOverview(boolean value) {
    m_ZoomOverview = value;
    reset();
  }

  /**
   * Returns whether the zoom overview gets displayed.
   *
   * @return 		true if the zoom overview gets displayed
   */
  public boolean getZoomOverview() {
    return m_ZoomOverview;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String zoomOverviewTipText() {
    return "If enabled, a zoom overview panel gets displayed as well.";
  }

  /**
   * Sets the plot updater to use.
   *
   * @param value 	the updater
   */
  public void setPlotUpdater(AbstractPlotUpdater value) {
    m_PlotUpdater = value;
    reset();
  }

  /**
   * Returns the plot updater in use.
   *
   * @return 		the updater
   */
  public AbstractPlotUpdater getPlotUpdater() {
    return m_PlotUpdater;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String plotUpdaterTipText() {
    return "The updating strategy for the plot.";
  }

  /**
   * Clears the content of the panel.
   */
  @Override
  public void clearPanel() {
    if (m_Panel != null)
      ((SpectrumExplorer) m_Panel).getContainerManager().clear();
  }

  /**
   * Creates the panel to display in the dialog.
   *
   * @return		the panel
   */
  @Override
  protected BasePanel newPanel() {
    SpectrumExplorer	result;

    result = new SpectrumExplorer();
    result.getContainerManager().setReloadable(false);
    result.getContainerManager().setAllowRemoval(false);
    result.getSpectrumPanel().setSidePanelVisible(m_ShowSidePanel);
    result.setZoomOverviewPanelVisible(m_ZoomOverview);
    result.getContainerManager().setColorProvider(m_ColorProvider.shallowCopy(true));
    result.getSpectrumPanel().setDataPaintlet(m_Paintlet.shallowCopy(true));

    return result;
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		<!-- flow-accepts-start -->adams.data.spectrum.Spectrum.class, adams.data.spectrum.MultiSpectrum.class<!-- flow-accepts-end -->
   */
  @Override
  public Class[] accepts() {
    return new Class[]{Spectrum.class, MultiSpectrum.class};
  }

  /**
   * Displays the token (the panel and dialog have already been created at
   * this stage).
   *
   * @param token	the token to display
   */
  @Override
  protected void display(Token token) {
    SpectrumContainer		cont;
    SpectrumContainerManager	manager;
    MultiSpectrum		multi;
    Spectrum			spec;
    int				i;
    MultiSpectrumToSpectra	conv;
    Spectrum[]			specs;
    String			msg;

    cont    = null;
    manager = ((SpectrumExplorer) m_Panel).getContainerManager();
    manager.startUpdate();
    if (token.getPayload() instanceof MultiSpectrum) {
      multi = (MultiSpectrum) token.getPayload();
      conv = new MultiSpectrumToSpectra();
      conv.setInput(multi);
      msg = conv.convert();
      if (msg == null) {
	specs = (Spectrum[]) conv.getOutput();
	for (i = 0; i < specs.length; i++) {
	  spec = specs[i];
	  cont = manager.newContainer(spec);
	  manager.add(cont);
	}
      }
      else {
	getLogger().warning(msg);
      }
      conv.destroy();
    }
    else {
      spec = (Spectrum) token.getPayload();
      cont = manager.newContainer(spec);
      manager.add(cont);
    }

    if (cont != null)
      m_PlotUpdater.update(((SpectrumExplorer) getPanel()).getSpectrumPanel(), cont);
  }

  /**
   * Updates the panel regardless, notifying the listeners.
   */
  @Override
  public void updatePlot() {
    if (getPanel() != null)
      m_PlotUpdater.update(((SpectrumExplorer) getPanel()).getSpectrumPanel());
  }

  /**
   * Creates a new display panel for the token.
   *
   * @param token	the token to display in a new panel, can be null
   * @return		the generated panel
   */
  @Override
  public DisplayPanel createDisplayPanel(Token token) {
    DisplayPanel	result;

    result = new DisplayPanel(getClass().getSimpleName());
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

  /**
   * Cleans up after the execution has finished.
   */
  @Override
  public void wrapUp() {
    if (m_Panel != null)
      m_PlotUpdater.update(((SpectrumExplorer) getPanel()).getSpectrumPanel());

    super.wrapUp();
  }
}
