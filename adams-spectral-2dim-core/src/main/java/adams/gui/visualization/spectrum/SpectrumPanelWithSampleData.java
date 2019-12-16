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
 * SpectrumPanelWithSampleData.java
 * Copyright (C) 2018-2019 University of Waikato, Hamilton, NZ
 */

package adams.gui.visualization.spectrum;

import adams.core.io.PlaceholderFile;
import adams.data.io.input.AbstractSpectrumReader;
import adams.data.spectrum.Spectrum;
import adams.gui.core.BasePanel;
import adams.gui.core.BaseScrollPane;
import adams.gui.core.BaseTabbedPane;
import adams.gui.core.SearchPanel;
import adams.gui.core.SearchPanel.LayoutType;
import adams.gui.event.SearchEvent;
import adams.gui.event.SearchListener;
import adams.gui.visualization.report.ReportFactory.Model;
import adams.gui.visualization.spectrum.SampleDataFactory.Table;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.io.File;
import java.util.List;

/**
 * Spectrum panel that also shows the sample data next to it.
 * Only ever shows a single spectrum.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumPanelWithSampleData
  extends BasePanel {

  private static final long serialVersionUID = 5545824510664254675L;

  /** the tabbed pane. */
  protected BaseTabbedPane m_TabbedPane;

  /** the spectrum panel. */
  protected SpectrumPanel m_PanelSpectrum;

  /** the sample data table. */
  protected SampleDataFactory.Table m_TableSampleData;

  /**
   * Initializes the members.
   */
  @Override
  protected void initGUI() {
    JPanel 	panel;

    super.initGUI();

    setLayout(new BorderLayout());

    m_TabbedPane = new BaseTabbedPane();
    add(m_TabbedPane, BorderLayout.CENTER);

    m_PanelSpectrum = new SpectrumPanel();
    m_PanelSpectrum.setSidePanelVisible(false);
    m_TabbedPane.addTab("Spectrum", m_PanelSpectrum);

    panel = new BasePanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    m_TabbedPane.addTab("Sample data", panel);

    m_TableSampleData = new Table();
    panel.add(new BaseScrollPane(m_TableSampleData), BorderLayout.CENTER);

    // search
    final SearchPanel searchPanel = new SearchPanel(LayoutType.HORIZONTAL, true);
    searchPanel.addSearchListener(new SearchListener() {
      public void searchInitiated(SearchEvent e) {
	m_TableSampleData.search(searchPanel.getSearchText(), searchPanel.isRegularExpression());
	searchPanel.grabFocus();
      }
    });
    panel.add(searchPanel, BorderLayout.SOUTH);
  }

  /**
   * Displays the spectrum. Removes any existing spectrum beforehand.
   *
   * @param data	the spectrum to display
   */
  public void display(Spectrum data) {
    SpectrumContainer	cont;

    cont = m_PanelSpectrum.getContainerManager().newContainer(data);
    m_PanelSpectrum.getContainerManager().clear();
    m_PanelSpectrum.getContainerManager().add(cont);
    m_TableSampleData.setModel(new Model(data.getReport()));
  }

  /**
   * Displays the spectrum. Removes any existing spectrum beforehand.
   *
   * @param reader	the reader to use for loading the spectrum
   * @param file	the spectrum file to load
   */
  public void display(AbstractSpectrumReader reader, File file) {
    List<Spectrum> 	spec;

    reader.setInput(new PlaceholderFile(file));
    spec = reader.read();
    if (spec.size() > 0)
      display(spec.get(0));
  }
}
