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
 * Copyright (C) 2016-2017 FracPete (fracpete at gmail dot com)
 *
 */

package adams.gui.tools;

import adams.core.DateFormat;
import adams.core.DateUtils;
import adams.core.Properties;
import adams.core.base.BaseDate;
import adams.core.base.BaseDateTime;
import adams.core.option.OptionUtils;
import adams.data.report.AbstractField;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.db.AbstractSpectrumConditions;
import adams.db.DatabaseConnection;
import adams.db.SampleDataT;
import adams.db.SpectrumConditionsMulti;
import adams.env.Environment;
import adams.gui.chooser.DateChooserPanel;
import adams.gui.core.BasePanel;
import adams.gui.core.BaseSplitPane;
import adams.gui.core.BaseStatusBar;
import adams.gui.core.BaseTable;
import adams.gui.core.CheckableTableModel;
import adams.gui.core.GUIHelper;
import adams.gui.core.MouseUtils;
import adams.gui.core.SearchPanel;
import adams.gui.core.SearchPanel.LayoutType;
import adams.gui.core.SortableAndSearchableTableWithButtons;
import adams.gui.event.SearchEvent;
import adams.gui.goe.GenericObjectEditorDialog;
import adams.gui.selection.SelectSpectrumPanel;
import adams.gui.visualization.spectrum.SampleDataFactory;
import com.googlecode.jfilechooserbookmarks.gui.BaseScrollPane;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows the user to update/set values in selected spectra.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class UpdateSampleDataPanel
  extends BasePanel {

  private static final long serialVersionUID = 1116436734033322299L;

  /**
   * Table model for displaying the database IDs, IDs, formats and selected
   * state of spectra.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision$
   */
  public static class TableModel
    extends CheckableTableModel<SelectSpectrumPanel.TableModel> {

    /** for serialization. */
    private static final long serialVersionUID = 2776199413402687115L;

    /**
     * default constructor.
     */
    public TableModel() {
      this(new SelectSpectrumPanel.TableModel());
    }

    /**
     * the constructor.
     *
     * @param model	model to display
     */
    public TableModel(SelectSpectrumPanel.TableModel model) {
      super(model, "Update");
    }

    /**
     * Returns the selected items (sample IDs).
     *
     * @return		the selected items
     */
    public String[] getSelectedSampleIDs() {
      List<String>	result;
      int		i;

      result = new ArrayList<>();

      for (i = 0; i < getRowCount(); i++) {
	if (getSelectedAt(i))
	  result.add("" + getModel().getValueAt(i, 1));
      }

      return result.toArray(new String[result.size()]);
    }

    /**
     * Returns the sample ID at the specified location.
     *
     * @param row	the (actual, not visible) position of the spectrum
     * @return		the sample ID, null if failed to retrieve
     */
    public String getSampleIdAt(int row) {
      if ((row >= 0) && (row < getRowCount()))
	return "" + getModel().getValueAt(row, 1);
      else
	return null;
    }
  }

  /** the name of the session file. */
  public final static String SESSION_FILENAME = "UpdateSampleDataSession.props";

  /** the properties. */
  protected static Properties m_Properties;

  /** the from date. */
  protected DateChooserPanel m_TextFrom;

  /** the to date. */
  protected DateChooserPanel m_TextTo;

  /** the button for the options. */
  protected JButton m_ButtonConditions;

  /** the button for the search. */
  protected JButton m_ButtonSearch;

  /** the split pane. */
  protected BaseSplitPane m_SplitPane;

  /** the table model in use. */
  protected TableModel m_Model;

  /** the table with the spectra. */
  protected SortableAndSearchableTableWithButtons m_TableIDs;

  /** the button for selecting all. */
  protected JButton m_ButtonSelectAll;

  /** the button for selecting none. */
  protected JButton m_ButtonSelectNone;

  /** the button for inverting the selection. */
  protected JButton m_ButtonSelectInvert;

  /** the search panel for the IDs. */
  protected SearchPanel m_SearchIDs;

  /** the sample data table. */
  protected SampleDataFactory.Table m_TableSampleData;

  /** the search panel for the sample data. */
  protected SearchPanel m_SearchSampleData;

  /** the text field for the field name. */
  protected JTextField m_TextName;

  /** the combobox for the field data type. */
  protected JComboBox<DataType> m_ComboBoxType;

  /** the text field for the field value. */
  protected JTextField m_TextValue;

  /** the button for updating the sample data. */
  protected JButton m_ButtonApply;

  /** the button for closing the dialog. */
  protected JButton m_ButtonClose;

  /** the conditions to use in the search. */
  protected SpectrumConditionsMulti m_Conditions;

  /** the status bar. */
  protected BaseStatusBar m_StatusBar;

  /** whether the search is currently happening. */
  protected boolean m_Searching;

  /** the formatter to use. */
  protected DateFormat m_Formatter;

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    Properties	props;

    super.initialize();

    m_Formatter = DateUtils.getDateFormatter();
    props       = getProperties();
    m_Searching = false;
    try {
      m_Conditions = (SpectrumConditionsMulti) OptionUtils.forCommandLine(SpectrumConditionsMulti.class, props.getProperty("Conditions", new SpectrumConditionsMulti().toCommandLine()));
    }
    catch (Exception e) {
      m_Conditions = new SpectrumConditionsMulti();
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
    JLabel		label;
    BaseDate		bdate;
    Properties		props;
    AbstractField 	field;

    super.initGUI();

    props = getProperties();

    setLayout(new BorderLayout());

    panelAll = new JPanel(new BorderLayout());
    add(panelAll, BorderLayout.CENTER);

    // 1. search
    panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    panelAll.add(panel, BorderLayout.NORTH);

    bdate = new BaseDate(BaseDate.NOW);

    // from
    m_TextFrom = new DateChooserPanel();
    m_TextFrom.setCurrent(props.getDate("From", bdate.dateValue()));
    m_TextFrom.setTextColumns(10);
    label = new JLabel("From");
    label.setDisplayedMnemonic('F');
    label.setLabelFor(m_TextFrom);
    panel.add(label);
    panel.add(m_TextFrom);

    // to
    m_TextTo = new DateChooserPanel();
    m_TextTo.setCurrent(props.getDate("To", bdate.dateValue()));
    m_TextTo.setTextColumns(10);
    label = new JLabel("To");
    label.setLabelFor(m_TextTo);
    panel.add(label);
    panel.add(m_TextTo);

    // options
    m_ButtonConditions = new JButton("Options");
    m_ButtonConditions.setMnemonic('O');
    m_ButtonConditions.addActionListener((ActionEvent e) -> showConditions());
    panel.add(m_ButtonConditions);

    // search
    m_ButtonSearch = new JButton("Search");
    m_ButtonSearch.setMnemonic('S');
    m_ButtonSearch.addActionListener((ActionEvent e) -> search());
    panel.add(m_ButtonSearch);

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
    m_ButtonSelectAll = new JButton("All");
    m_ButtonSelectAll.addActionListener((ActionEvent e) -> m_Model.selectAll());
    m_TableIDs.addToButtonsPanel(m_ButtonSelectAll);
    m_ButtonSelectNone = new JButton("None");
    m_ButtonSelectNone.addActionListener((ActionEvent e) -> m_Model.selectNone());
    m_TableIDs.addToButtonsPanel(m_ButtonSelectNone);
    m_ButtonSelectInvert = new JButton("Invert");
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

    // field
    field = Field.parseField(props.getProperty("Field", ""));
    if (field.getName().isEmpty())
      field = new Field("", DataType.BOOLEAN);
    panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    panel.add(panel2, BorderLayout.SOUTH);

    m_TextName = new JTextField(10);
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

    m_ComboBoxType = new JComboBox<>(DataType.values());
    m_ComboBoxType.setSelectedItem(field.getDataType());
    panel2.add(m_ComboBoxType);

    m_TextValue = new JTextField(10);
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

    m_ButtonApply = new JButton("Apply");
    m_ButtonApply.setMnemonic('A');
    m_ButtonApply.addActionListener((ActionEvent e) -> apply());
    panel.add(m_ButtonApply);

    m_ButtonClose = new JButton("Close");
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
   * Transfers the fields to the conditions object.
   */
  protected void fieldsToConditions() {
    m_Conditions.setStartDate(new BaseDateTime(m_Formatter.format(m_TextFrom.getCurrent()) + " 00:00:00"));
    m_Conditions.setEndDate(new BaseDateTime(m_Formatter.format(m_TextTo.getCurrent()) + " 23:59:59"));
  }

  /**
   * Transfers the conditions to the fields.
   */
  protected void conditionsToFields() {
    m_TextFrom.setCurrent(m_Conditions.getStartDate().dateValue());
    m_TextTo.setCurrent(m_Conditions.getEndDate().dateValue());
  }

  /**
   * Shows GOE dialog with the conditions.
   */
  protected void showConditions() {
    GenericObjectEditorDialog	dialog;

    fieldsToConditions();

    if (getParentDialog() != null)
      dialog = new GenericObjectEditorDialog(getParentDialog(), Dialog.ModalityType.DOCUMENT_MODAL);
    else
      dialog = new GenericObjectEditorDialog(getParentFrame(), true);
    dialog.setTitle("Sample data conditions");
    dialog.getGOEEditor().setCanChangeClassInDialog(false);
    dialog.getGOEEditor().setClassType(AbstractSpectrumConditions.class);
    dialog.setCurrent(m_Conditions);
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
    if (dialog.getResult() != GenericObjectEditorDialog.APPROVE_OPTION)
      return;

    m_Conditions = (SpectrumConditionsMulti) dialog.getCurrent();
    conditionsToFields();
  }

  /**
   * Performs the search and updates the table.
   */
  protected void search() {
    SwingWorker		worker;

    fieldsToConditions();

    worker = new SwingWorker() {
      protected List<String> ids;
      @Override
      protected Object doInBackground() throws Exception {
	MouseUtils.setWaitCursor(UpdateSampleDataPanel.this);
	m_Searching = true;
	updateButtons();
	SampleDataT sdt = SampleDataT.getSingleton(DatabaseConnection.getSingleton());
	ids = sdt.getIDs(new String[]{"sp.AUTO_ID", "sp.SAMPLEID", "sp.FORMAT"}, m_Conditions);
	return null;
      }
      @Override
      protected void done() {
	super.done();
	m_Model = new TableModel(new SelectSpectrumPanel.TableModel(ids));
	m_Model.addTableModelListener((TableModelEvent e) -> updateButtons());
	m_TableIDs.setModel(m_Model);
	MouseUtils.setDefaultCursor(UpdateSampleDataPanel.this);
	m_Searching = false;
	updateButtons();
	if (ids.size() == 0) {
	  GUIHelper.showErrorMessage(
	    UpdateSampleDataPanel.this, "Failed to retrieve any IDs from database, check console for potential errors!");
	}
      }
    };
    worker.execute();
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
   * Updates the state of the buttons.
   */
  protected void updateButtons() {
    int		selCount;

    selCount = m_Model.getSelectedCount();

    m_ButtonApply.setEnabled(!m_Searching && (selCount > 0) && !m_TextName.getText().isEmpty() && !m_TextValue.getText().isEmpty());
    m_ButtonConditions.setEnabled(!m_Searching);
    m_ButtonSearch.setEnabled(!m_Searching);
  }

  /**
   * Updates and stores the properties on disk.
   *
   * @return		if successfully saved
   */
  protected boolean updateProperties() {
    Properties		props;

    props = getProperties();
    props.setDate("From", m_TextFrom.getCurrent());
    props.setDate("To", m_TextTo.getCurrent());
    props.setProperty("Conditions", m_Conditions.toCommandLine());
    props.setProperty("Field", getField().toParseableString());
    props.setProperty("Value", m_TextValue.getText());

    return props.save(Environment.getInstance().createPropertiesFilename(SESSION_FILENAME));
  }

  /**
   * Returns the session properties.
   *
   * @return		the properties
   */
  protected synchronized Properties getProperties() {
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
