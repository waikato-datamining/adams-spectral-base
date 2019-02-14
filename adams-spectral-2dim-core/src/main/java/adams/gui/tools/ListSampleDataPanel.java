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
 * ListSampleDataPanel.java
 * Copyright (C) 2019 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.gui.tools;

import adams.core.ClassLister;
import adams.core.Properties;
import adams.data.report.AbstractField;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.db.DatabaseConnection;
import adams.db.SampleDataT;
import adams.env.Environment;
import adams.gui.core.BaseButton;
import adams.gui.core.BaseComboBox;
import adams.gui.core.BasePanel;
import adams.gui.core.BaseSplitPane;
import adams.gui.core.BaseStatusBar;
import adams.gui.core.BaseTable;
import adams.gui.core.ConsolePanel;
import adams.gui.core.GUIHelper;
import adams.gui.core.MouseUtils;
import adams.gui.core.SearchPanel;
import adams.gui.core.SearchPanel.LayoutType;
import adams.gui.core.SortableAndSearchableTableWithButtons;
import adams.gui.core.SpreadSheetTable;
import adams.gui.core.SpreadSheetTableModel;
import adams.gui.event.SearchEvent;
import adams.gui.selection.SelectSpectrumPanel;
import adams.gui.tools.idprovider.AbstractIDProviderPanel;
import adams.gui.tools.idprovider.IDConsumer;
import adams.gui.tools.idprovider.TableModel;
import com.googlecode.jfilechooserbookmarks.gui.BaseScrollPane;
import nz.ac.waikato.cms.locator.StringCompare;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Allows the user to list sample data from selected spectra.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class ListSampleDataPanel
  extends BasePanel
  implements IDConsumer {

  private static final long serialVersionUID = 1116436734033322299L;

  /** the name of the session file. */
  public final static String SESSION_FILENAME = "ListSampleDataSession.props";

  /** the properties. */
  protected static Properties m_Properties;

  /** the panel for the ID provider panels. */
  protected JPanel m_PanelIDProvider;

  /** the comboboxes for the ID panels. */
  protected BaseComboBox<String> m_ComboBoxIDPanels;

  /** the ID panels. */
  protected Map<String,AbstractIDProviderPanel> m_IDPanels;

  /** the split pane. */
  protected BaseSplitPane m_SplitPane;

  /** the table model in use. */
  protected TableModel m_Model;

  /** the table with the spectra. */
  protected SortableAndSearchableTableWithButtons m_TableIDs;

  /** the button for checking all. */
  protected BaseButton m_ButtonCheckAll;

  /** the button for checking none. */
  protected BaseButton m_ButtonCheckNone;

  /** the button for checking the selected ones. */
  protected BaseButton m_ButtonCheckSelected;

  /** the button for inverting the checked ones. */
  protected BaseButton m_ButtonSelectInvert;

  /** the search panel for the IDs. */
  protected SearchPanel m_SearchIDs;

  /** the sample data overview table. */
  protected SpreadSheetTable m_TableSampleData;

  /** the search panel for the sample data. */
  protected SearchPanel m_SearchSampleData;

  /** the button for displaying the sample data. */
  protected BaseButton m_ButtonDisplay;

  /** the button for closing the dialog. */
  protected BaseButton m_ButtonClose;

  /** the status bar. */
  protected BaseStatusBar m_StatusBar;

  /** the current panel. */
  protected AbstractIDProviderPanel m_CurrentIDProvider;

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    AbstractIDProviderPanel	panel;
    Constructor			constr;

    super.initialize();

    m_CurrentIDProvider = null;
    m_IDPanels = new HashMap<>();
    for (Class cls: ClassLister.getSingleton().getClasses(AbstractIDProviderPanel.class)) {
      try {
        constr = cls.getConstructor(IDConsumer.class);
        panel  = (AbstractIDProviderPanel) constr.newInstance(this);
	m_IDPanels.put(panel.getPanelName(), panel);
      }
      catch (Exception e) {
	ConsolePanel.getSingleton().append("Failed to instantiate: " + cls.getName(), e);
      }
    }
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initGUI() {
    JPanel		panelAll;
    JPanel		panel;
    JPanel		panel2;
    JPanel		panelTable;
    List<String> 	panelNames;

    super.initGUI();

    setLayout(new BorderLayout());

    panelAll = new JPanel(new BorderLayout());
    add(panelAll, BorderLayout.CENTER);

    // 1. panels
    panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    m_PanelIDProvider = new JPanel(new BorderLayout());
    panelAll.add(panel, BorderLayout.NORTH);

    panelNames = new ArrayList<>(m_IDPanels.keySet());
    Collections.sort(panelNames);
    m_ComboBoxIDPanels = new BaseComboBox<>(panelNames);
    m_ComboBoxIDPanels.setSelectedIndex(0);
    m_ComboBoxIDPanels.addActionListener((ActionEvent e) -> updateIDPanel());
    panel.add(m_ComboBoxIDPanels);
    panel.add(m_PanelIDProvider);
    m_CurrentIDProvider = m_IDPanels.get(m_ComboBoxIDPanels.getSelectedItem());
    m_PanelIDProvider.add(m_CurrentIDProvider, BorderLayout.CENTER);

    // 2. tables
    panel = new JPanel(new BorderLayout());
    panelAll.add(panel, BorderLayout.CENTER);

    m_SplitPane = new BaseSplitPane(BaseSplitPane.HORIZONTAL_SPLIT);
    m_SplitPane.setDividerLocation(0.5);
    m_SplitPane.setResizeWeight(0.5);
    m_SplitPane.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    panel.add(m_SplitPane, BorderLayout.CENTER);

    // IDs
    m_Model = new TableModel("List");
    m_Model.addTableModelListener((TableModelEvent e) -> updateButtons());
    m_TableIDs = new SortableAndSearchableTableWithButtons(m_Model);
    m_TableIDs.setAutoResizeMode(BaseTable.AUTO_RESIZE_OFF);
    m_TableIDs.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    m_ButtonCheckAll = new BaseButton("All");
    m_ButtonCheckAll.addActionListener((ActionEvent e) -> m_Model.checkAll());
    m_TableIDs.addToButtonsPanel(m_ButtonCheckAll);
    m_ButtonCheckNone = new BaseButton("None");
    m_ButtonCheckNone.addActionListener((ActionEvent e) -> m_Model.checkNone());
    m_TableIDs.addToButtonsPanel(m_ButtonCheckNone);
    m_ButtonCheckSelected = new BaseButton("Selected");
    m_ButtonCheckSelected.addActionListener((ActionEvent e) -> {
      int[] selected = m_TableIDs.getSelectedRows();
      for (int i = 0; i < selected.length; i++)
        selected[i] = m_TableIDs.getActualRow(selected[i]);
      m_Model.check(selected);
    });
    m_TableIDs.addToButtonsPanel(m_ButtonCheckSelected);
    m_ButtonSelectInvert = new BaseButton("Invert");
    m_ButtonSelectInvert.addActionListener((ActionEvent e) -> m_Model.invertChecked());
    m_TableIDs.addToButtonsPanel(m_ButtonSelectInvert);

    m_SearchIDs = new SearchPanel(LayoutType.HORIZONTAL, true);
    m_SearchIDs.setButtonCaption("Search");
    m_SearchIDs.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    m_SearchIDs.addSearchListener((SearchEvent e) ->
      m_TableIDs.search(e.getParameters().getSearchString(), e.getParameters().isRegExp()));

    panelTable = new JPanel(new BorderLayout(5, 5));
    panelTable.add(m_TableIDs, BorderLayout.CENTER);
    panelTable.add(m_SearchIDs, BorderLayout.SOUTH);
    m_SplitPane.setLeftComponent(panelTable);

    // report
    m_TableSampleData = new SpreadSheetTable(new SpreadSheetTableModel());
    m_TableSampleData.setShowSimpleHeaderPopupMenu(true);
    m_TableSampleData.setShowSimpleCellPopupMenu(true);
    m_TableSampleData.setUseOptimalColumnWidths(true);
    m_TableSampleData.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> updateButtons());
    m_TableSampleData.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    panel2 = new JPanel(new BorderLayout());
    panel2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    panel2.add(new BaseScrollPane(m_TableSampleData));

    m_SearchSampleData = new SearchPanel(LayoutType.HORIZONTAL, true);
    m_SearchSampleData.setButtonCaption("Search");
    m_SearchSampleData.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    m_SearchSampleData.addSearchListener((SearchEvent e) ->
      m_TableSampleData.search(e.getParameters().getSearchString(), e.getParameters().isRegExp()));

    panelTable = new JPanel(new BorderLayout(5, 5));
    panelTable.add(panel2, BorderLayout.CENTER);
    panelTable.add(m_SearchSampleData, BorderLayout.SOUTH);
    m_SplitPane.setRightComponent(panelTable);

    // 3. apply
    panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panelAll.add(panel, BorderLayout.SOUTH);

    m_ButtonDisplay = new BaseButton("Display");
    m_ButtonDisplay.setMnemonic('D');
    m_ButtonDisplay.addActionListener((ActionEvent e) -> display());
    panel.add(m_ButtonDisplay);

    m_ButtonClose = new BaseButton("Close");
    m_ButtonClose.setMnemonic('l');
    m_ButtonClose.addActionListener((ActionEvent e) -> closeParent());
    panel.add(m_ButtonClose);

    // 4. status bar
    m_StatusBar = new BaseStatusBar();
    add(m_StatusBar, BorderLayout.SOUTH);
  }

  /**
   * Finishes up the initialization.
   */
  @Override
  protected void finishInit() {
    super.finishInit();
    updateButtons();
  }

  /**
   * Updates the model with the specified IDs.
   */
  public void setIDs(String[] ids) {
    m_Model = new TableModel(new SelectSpectrumPanel.TableModel(ids), "List");
    m_Model.addTableModelListener((TableModelEvent e) -> updateButtons());
    m_TableIDs.setModel(m_Model);
    updateButtons();
    if (ids.length == 0) {
      GUIHelper.showErrorMessage(
	ListSampleDataPanel.this, "No IDs found, check console for potential errors!");
    }
    else {
      updateProperties();
    }
  }

  /**
   * Updates the selected spectra.
   */
  protected void display() {
    final String[]	sel;
    SwingWorker		worker;

    sel   = m_Model.getSelectedSampleIDs();

    worker = new SwingWorker() {
      protected boolean successful;
      @Override
      protected Object doInBackground() throws Exception {
	successful = true;
	MouseUtils.setWaitCursor(ListSampleDataPanel.this);

	// collect sampledata
	Set<String> namesSet = new HashSet<>();
	Map<String,SampleData> all = new HashMap<>();
	SampleDataT sdt = SampleDataT.getSingleton(DatabaseConnection.getSingleton());
	for (int i = 0; i < sel.length; i++) {
	  m_StatusBar.showStatus("Loading: " + (i+1) + "/" + sel.length + "...");
	  SampleData sd = sdt.load(sel[i]);
	  all.put(sel[i], sd);
	  for (AbstractField field: sd.getFields()) {
	    if (field.getName().equals(SampleData.SAMPLE_ID))
	      continue;
	    if (field.getName().equals(SampleData.SAMPLE_TYPE))
	      continue;
	    if (field.getName().equals(SampleData.PROPERTY_PARENTID))
	      continue;
	    if (field.getName().equals(SampleData.FIELD_DUMMYREPORT))
	      continue;
	    if (field.getName().equals(SampleData.FIELD_EXCLUDED))
	      continue;
	    namesSet.add(field.getName());
	  }
	}

	// construct sheet
	List<String> names = new ArrayList<>(namesSet);
	Collections.sort(names, new StringCompare());
	SpreadSheet sheet = new DefaultSpreadSheet();
	Row row = sheet.getHeaderRow();
	row.addCell(SampleData.SAMPLE_ID).setContentAsString("ID");
	row.addCell(SampleData.SAMPLE_TYPE).setContentAsString("Type");
	for (String name: names)
	  row.addCell(name).setContentAsString(name);
	for (int i = 0; i < sel.length; i++) {
	  SampleData sd = all.get(sel[i]);
	  row = sheet.addRow();
	  row.addCell(SampleData.SAMPLE_ID).setContentAsString(sel[i]);
	  row.addCell(SampleData.SAMPLE_TYPE).setContentAsString(all.get(sel[i]).getStringValue(SampleData.SAMPLE_TYPE));
	  for (String name: names) {
	    if (sd.hasValue(name)) {
	      DataType type = sd.getFieldType(new Field(name, DataType.UNKNOWN));
	      switch (type) {
		case BOOLEAN:
		  row.addCell(name).setContent(sd.getBooleanValue(name));
		  break;
		case NUMERIC:
		  row.addCell(name).setContent(sd.getDoubleValue(name));
		  break;
		case STRING:
		  row.addCell(name).setContent(sd.getStringValue(name));
		  break;
		case UNKNOWN:
		  row.addCell(name).setContent("" + sd.getValue(new Field(name, DataType.UNKNOWN)));
		  break;
	      }
	    }
	  }
	}

	// display
	m_TableSampleData.setModel(new SpreadSheetTableModel(sheet));
	return null;
      }
      @Override
      protected void done() {
	super.done();
	MouseUtils.setDefaultCursor(ListSampleDataPanel.this);
	m_StatusBar.clearStatus();
	if (successful)
	  updateProperties();
      }
    };
    worker.execute();
  }

  /**
   * Updates the state of the buttons.
   */
  protected void updateButtons() {
    int		selCount;

    selCount = m_Model.getCheckedCount();

    m_ButtonDisplay.setEnabled(!m_CurrentIDProvider.isWorking() && (selCount > 0));
    m_CurrentIDProvider.updateButtons();
  }

  /**
   * Updates the panel to be displayed for determining the IDs.
   */
  protected void updateIDPanel() {
    if (m_ComboBoxIDPanels.getSelectedIndex() == -1)
      return;

    m_CurrentIDProvider = m_IDPanels.get(m_ComboBoxIDPanels.getSelectedItem());
    m_PanelIDProvider.removeAll();
    m_PanelIDProvider.add(m_CurrentIDProvider, BorderLayout.CENTER);
    m_PanelIDProvider.invalidate();
    m_PanelIDProvider.revalidate();
    m_PanelIDProvider.doLayout();
  }

  /**
   * Updates and stores the properties on disk.
   *
   * @return		if successfully saved
   */
  public boolean updateProperties() {
    Properties		props;

    props = getProperties();
    props.add(m_CurrentIDProvider.getPanelProperties());

    return props.save(Environment.getInstance().createPropertiesFilename(SESSION_FILENAME));
  }

  /**
   * Returns the session properties.
   *
   * @return		the properties
   */
  public synchronized Properties getProperties() {
    if (m_Properties == null) {
      try {
	m_Properties = Properties.read(SESSION_FILENAME);
      }
      catch (Exception e) {
	m_Properties = new Properties();
      }
    }

    return m_Properties;
  }
}
