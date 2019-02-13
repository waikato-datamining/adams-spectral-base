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
 * UpdateSampleDataPanel.java
 * Copyright (C) 2016-2019 FracPete (fracpete at gmail dot com)
 *
 */

package adams.gui.tools;

import adams.core.ClassLister;
import adams.core.Properties;
import adams.core.Utils;
import adams.data.report.AbstractField;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.Report;
import adams.data.sampledata.SampleData;
import adams.db.DatabaseConnection;
import adams.db.SampleDataT;
import adams.env.Environment;
import adams.gui.core.BaseButton;
import adams.gui.core.BaseComboBox;
import adams.gui.core.BasePanel;
import adams.gui.core.BaseSplitPane;
import adams.gui.core.BaseStatusBar;
import adams.gui.core.BaseTable;
import adams.gui.core.BaseTextField;
import adams.gui.core.ConsolePanel;
import adams.gui.core.GUIHelper;
import adams.gui.core.MouseUtils;
import adams.gui.core.SearchPanel;
import adams.gui.core.SearchPanel.LayoutType;
import adams.gui.core.SortableAndSearchableTableWithButtons;
import adams.gui.event.SearchEvent;
import adams.gui.selection.SelectSpectrumPanel;
import adams.gui.tools.idprovider.AbstractIDProviderPanel;
import adams.gui.tools.idprovider.IDConsumer;
import adams.gui.tools.idprovider.TableModel;
import adams.gui.visualization.spectrum.SampleDataFactory;
import com.googlecode.jfilechooserbookmarks.gui.BaseScrollPane;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allows the user to update/set values in selected spectra.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class UpdateSampleDataPanel
  extends BasePanel
  implements IDConsumer {

  private static final long serialVersionUID = 1116436734033322299L;

  /** the name of the session file. */
  public final static String SESSION_FILENAME = "UpdateSampleDataSession.props";

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

  /** the button for selecting all. */
  protected BaseButton m_ButtonSelectAll;

  /** the button for selecting none. */
  protected BaseButton m_ButtonSelectNone;

  /** the button for inverting the selection. */
  protected BaseButton m_ButtonSelectInvert;

  /** the search panel for the IDs. */
  protected SearchPanel m_SearchIDs;

  /** the sample data table. */
  protected SampleDataFactory.Table m_TableSampleData;

  /** the panel for the buttons for the sampledata table. */
  protected JPanel m_PanelSampleDataButtons;

  /** the button for removing sample data reference values. */
  protected BaseButton m_ButtonRemoveReferenceValue;

  /** the search panel for the sample data. */
  protected SearchPanel m_SearchSampleData;

  /** the text field for the field name. */
  protected BaseTextField m_TextName;

  /** the combobox for the field data type. */
  protected BaseComboBox<DataType> m_ComboBoxType;

  /** the text field for the field value. */
  protected BaseTextField m_TextValue;

  /** the button for updating the sample data. */
  protected BaseButton m_ButtonApply;

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
    JPanel		panel3;
    JPanel		panelTable;
    JLabel		label;
    Properties		props;
    AbstractField 	field;
    List<String> 	panelNames;

    super.initGUI();

    props = getProperties();

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
    m_Model = new TableModel();
    m_Model.addTableModelListener((TableModelEvent e) -> updateButtons());
    m_TableIDs = new SortableAndSearchableTableWithButtons(m_Model);
    m_TableIDs.setAutoResizeMode(BaseTable.AUTO_RESIZE_OFF);
    m_TableIDs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    m_TableIDs.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> showReport());
    m_ButtonSelectAll = new BaseButton("All");
    m_ButtonSelectAll.addActionListener((ActionEvent e) -> m_Model.selectAll());
    m_TableIDs.addToButtonsPanel(m_ButtonSelectAll);
    m_ButtonSelectNone = new BaseButton("None");
    m_ButtonSelectNone.addActionListener((ActionEvent e) -> m_Model.selectNone());
    m_TableIDs.addToButtonsPanel(m_ButtonSelectNone);
    m_ButtonSelectInvert = new BaseButton("Invert");
    m_ButtonSelectInvert.addActionListener((ActionEvent e) -> m_Model.invertSelection());
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
    m_TableSampleData = new SampleDataFactory.Table();
    m_TableSampleData.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> updateButtons());
    m_TableSampleData.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    panel2 = new JPanel(new BorderLayout());
    panel2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    panel2.add(new BaseScrollPane(m_TableSampleData));

    m_PanelSampleDataButtons = new JPanel(new GridLayout(0, 1));
    panel3 = new JPanel(new BorderLayout());
    panel3.add(m_PanelSampleDataButtons, BorderLayout.NORTH);

    m_ButtonRemoveReferenceValue = new BaseButton("Remove");
    m_ButtonRemoveReferenceValue.addActionListener((ActionEvent e) -> removeReferenceValues());
    m_PanelSampleDataButtons.add(m_ButtonRemoveReferenceValue);

    m_SearchSampleData = new SearchPanel(LayoutType.HORIZONTAL, true);
    m_SearchSampleData.setButtonCaption("Search");
    m_SearchSampleData.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    m_SearchSampleData.addSearchListener((SearchEvent e) ->
      m_TableSampleData.search(e.getParameters().getSearchString(), e.getParameters().isRegExp()));

    panelTable = new JPanel(new BorderLayout(5, 5));
    panelTable.add(panel2, BorderLayout.CENTER);
    panelTable.add(panel3, BorderLayout.EAST);
    panelTable.add(m_SearchSampleData, BorderLayout.SOUTH);
    m_SplitPane.setRightComponent(panelTable);

    // field
    field = Field.parseField(props.getProperty("Field", ""));
    if (field.getName().isEmpty())
      field = new Field("", DataType.BOOLEAN);
    panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    panel.add(panel2, BorderLayout.SOUTH);

    m_TextName = new BaseTextField(10);
    m_TextName.setText(field.getName());
    m_TextName.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
	updateButtons();
      }
      @Override
      public void removeUpdate(DocumentEvent e) {
	updateButtons();
      }
      @Override
      public void changedUpdate(DocumentEvent e) {
	updateButtons();
      }
    });
    label = new JLabel("Field");
    label.setDisplayedMnemonic('d');
    label.setLabelFor(m_TextName);
    panel2.add(label);
    panel2.add(m_TextName);

    m_ComboBoxType = new BaseComboBox<>(DataType.values());
    m_ComboBoxType.setSelectedItem(field.getDataType());
    panel2.add(m_ComboBoxType);

    m_TextValue = new BaseTextField(10);
    m_TextValue.setText(props.getProperty("Value", ""));
    m_TextValue.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
	updateButtons();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
	updateButtons();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
	updateButtons();
      }
    });
    panel2.add(m_TextValue);

    // 3. apply
    panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panelAll.add(panel, BorderLayout.SOUTH);

    m_ButtonApply = new BaseButton("Apply");
    m_ButtonApply.setMnemonic('A');
    m_ButtonApply.addActionListener((ActionEvent e) -> apply());
    panel.add(m_ButtonApply);

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
    m_Model = new TableModel(new SelectSpectrumPanel.TableModel(ids));
    m_Model.addTableModelListener((TableModelEvent e) -> updateButtons());
    m_TableIDs.setModel(m_Model);
    updateButtons();
    if (ids.length == 0) {
      GUIHelper.showErrorMessage(
	UpdateSampleDataPanel.this, "No IDs found, check console for potential errors!");
    }
    else {
      updateProperties();
    }
  }

  /**
   * Returns the current values as field.
   *
   * @return		the field
   */
  protected Field getField() {
    return new Field(m_TextName.getText(), (DataType) m_ComboBoxType.getSelectedItem());
  }

  /**
   * Updates the selected spectra.
   */
  protected void apply() {
    final Field		field;
    final String	value;
    final String[]	sel;
    SwingWorker		worker;

    field = getField();
    value = m_TextValue.getText();
    sel   = m_Model.getSelectedSampleIDs();

    worker = new SwingWorker() {
      protected boolean successful;
      @Override
      protected Object doInBackground() throws Exception {
	successful = true;
	MouseUtils.setWaitCursor(UpdateSampleDataPanel.this);
	SampleDataT sdt = SampleDataT.getSingleton(DatabaseConnection.getSingleton());
	for (int i = 0; i < sel.length; i++) {
	  m_StatusBar.showStatus("Updating: " + (i+1) + "/" + sel.length + "...");
	  SampleData sd = sdt.load(sel[i]);
	  if (sd != null) {
	    sd.setValue(field, value);
	    if (!sdt.store(sel[i], sd)) {
	      successful = false;
	      GUIHelper.showErrorMessage(
		UpdateSampleDataPanel.this, "Failed to store sample data for ID " + sel[i] + "!");
	    }
	  }
	}
	return null;
      }
      @Override
      protected void done() {
	super.done();
	MouseUtils.setDefaultCursor(UpdateSampleDataPanel.this);
	m_StatusBar.clearStatus();
	if (successful)
	  updateProperties();
      }
    };
    worker.execute();
  }

  /**
   * Loads the associated report and displays it.
   */
  protected void showReport() {
    SampleDataT 	sdt;
    String		id;
    SampleData		sd;

    if (m_TableIDs.getSelectedRowCount() == 0) {
      m_TableSampleData.setModel(SampleDataFactory.getModel(new SampleData()));
      return;
    }

    id  = m_Model.getSampleIdAt(m_TableIDs.getActualRow(m_TableIDs.getSelectedRow()));
    sdt = SampleDataT.getSingleton(DatabaseConnection.getSingleton());
    sd  = sdt.load(id);
    if (sd != null)
      m_TableSampleData.setModel(SampleDataFactory.getModel(sd));
    else
      m_TableSampleData.setModel(SampleDataFactory.getModel(new SampleData()));
  }

  /**
   * Removes any selected reference values from the database.
   */
  protected void removeReferenceValues() {
    Report		report;
    String		msg;
    SampleDataT		samplet;
    String		id;

    if (m_TableSampleData.getSelectedRowCount() == 0)
      return;

    report = m_TableSampleData.getSelectionAsReport();

    msg = "Do you want to delete the following reference values?\n"
      + Utils.flatten(report.getFields(), ", ");
    if (GUIHelper.showConfirmMessage(this, msg) != GUIHelper.APPROVE_OPTION)
      return;

    m_ButtonRemoveReferenceValue.setEnabled(false);

    id      = m_Model.getSampleIdAt(m_TableIDs.getActualRow(m_TableIDs.getSelectedRow()));
    samplet = SampleDataT.getSingleton(DatabaseConnection.getSingleton());
    for (AbstractField field: report.getFields()) {
      if (!samplet.remove(id, field)) {
        GUIHelper.showErrorMessage(this, "Failed to remove field '" + field + "' for ID '" + id + "'!");
        break;
      }
      else {
        m_TableSampleData.getReport().removeValue(field);
        m_TableSampleData.setReport(m_TableSampleData.getReport());
      }
    }

    m_TableSampleData.setSelectedRows(new int[0]);
  }

  /**
   * Updates the state of the buttons.
   */
  protected void updateButtons() {
    int		selCount;

    selCount = m_Model.getSelectedCount();

    m_ButtonApply.setEnabled(!m_CurrentIDProvider.isWorking() && (selCount > 0) && !m_TextName.getText().isEmpty() && !m_TextValue.getText().isEmpty());
    m_ButtonRemoveReferenceValue.setEnabled(m_TableSampleData.getSelectedRowCount() > 0);
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
    props.setProperty("Field", getField().toParseableString());
    props.setProperty("Value", m_TextValue.getText());

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
