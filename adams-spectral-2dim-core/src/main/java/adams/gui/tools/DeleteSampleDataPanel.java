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
 * DeleteSampleDataPanel.java
 * Copyright (C) 2019-2023 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.gui.tools;

import adams.core.ClassLister;
import adams.core.Properties;
import adams.data.report.Field;
import adams.db.DatabaseConnection;
import adams.db.SampleDataF;
import adams.env.Environment;
import adams.gui.core.BaseButton;
import adams.gui.core.BaseComboBox;
import adams.gui.core.BasePanel;
import adams.gui.core.BasePopupMenu;
import adams.gui.core.BaseSplitPane;
import adams.gui.core.BaseStatusBar;
import adams.gui.core.BaseTable;
import adams.gui.core.ConsolePanel;
import adams.gui.core.GUIHelper;
import adams.gui.core.MouseUtils;
import adams.gui.core.SearchPanel;
import adams.gui.core.SearchPanel.LayoutType;
import adams.gui.core.SortableAndSearchableTableWithButtons;
import adams.gui.event.SearchEvent;
import adams.gui.goe.GenericArrayEditor;
import adams.gui.selection.SelectSpectrumPanel;
import adams.gui.tools.idprovider.AbstractIDProviderPanel;
import adams.gui.tools.idprovider.IDConsumer;
import adams.gui.tools.idprovider.TableModel;
import com.github.fracpete.jclipboardhelper.ClipboardHelper;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Allows the user to delete sample data values from selected spectra.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class DeleteSampleDataPanel
  extends BasePanel
  implements IDConsumer {

  private static final long serialVersionUID = 1116436734033322299L;

  /** the name of the session file. */
  public final static String SESSION_FILENAME = "DeleteSampleDataSession.props";

  /** the properties. */
  protected static Properties m_Properties;

  /** the panel for the ID provider panels. */
  protected JPanel m_PanelIDProvider;

  /** the combobox for the ID panels. */
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

  /** the fields to remove. */
  protected GenericArrayEditor m_PanelFields;

  /** the button for deleting the sample values. */
  protected BaseButton m_ButtonDelete;

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
    m_SplitPane.setUISettingsParameters(getClass(), "SplitPane");
    panel.add(m_SplitPane, BorderLayout.CENTER);

    // IDs
    m_Model = new TableModel("Delete");
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
    m_TableIDs.getComponent().addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
	if (m_TableIDs.getSelectedRow() != -1) {
	  if (MouseUtils.isRightClick(e)) {
	    e.consume();
	    BasePopupMenu menu = createPopupMenu(e);
	    if (menu != null)
	      menu.showAbsolute(m_TableIDs, e);
	  }
	}
	if (!e.isConsumed())
	  super.mouseClicked(e);
      }
    });

    m_SearchIDs = new SearchPanel(LayoutType.HORIZONTAL, true);
    m_SearchIDs.setButtonCaption("Search");
    m_SearchIDs.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    m_SearchIDs.addSearchListener((SearchEvent e) ->
      m_TableIDs.search(e.getParameters().getSearchString(), e.getParameters().isRegExp()));

    panelTable = new JPanel(new BorderLayout(5, 5));
    panelTable.add(m_TableIDs, BorderLayout.CENTER);
    panelTable.add(m_SearchIDs, BorderLayout.SOUTH);
    m_SplitPane.setLeftComponent(panelTable);

    // fields
    m_PanelFields = new GenericArrayEditor();
    m_PanelFields.setValue(new Field[0]);
    m_PanelFields.setButtonsVisible(false);
    m_PanelFields.addArrayChangeListener((ChangeEvent e) -> updateButtons());
    panel2 = new JPanel(new BorderLayout());
    panel2.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    panel2.add(m_PanelFields);

    panelTable = new JPanel(new BorderLayout(5, 5));
    panelTable.add(panel2, BorderLayout.CENTER);
    m_SplitPane.setRightComponent(panelTable);

    // 3. apply
    panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    panelAll.add(panel, BorderLayout.SOUTH);

    m_ButtonDelete = new BaseButton("Delete");
    m_ButtonDelete.setMnemonic('D');
    m_ButtonDelete.addActionListener((ActionEvent e) -> delete());
    panel.add(m_ButtonDelete);

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
   * Creates the popup menu for the table.
   */
  public BasePopupMenu createPopupMenu(MouseEvent e) {
    BasePopupMenu	result;
    final int[]		rows;
    int			i;
    JMenuItem 		menuitem;

    result = null;
    if (m_TableIDs.getSelectedRows().length > 0)
      rows = m_TableIDs.getSelectedRows();
    else if (m_TableIDs.rowAtPoint(new Point(e.getX(), e.getY())) != -1)
      rows = new int[]{m_TableIDs.rowAtPoint(new Point(e.getX(), e.getY()))};
    else
      rows = new int[0];
    if (rows.length > 0) {
      result = new BasePopupMenu();
      for (i = 0; i < m_TableIDs.getColumnCount(); i++) {
        if (m_TableIDs.getColumnClass(i) == Boolean.class)
          continue;
	final int col = i;
	menuitem = new JMenuItem("Copy '" + m_TableIDs.getColumnName(i) + "'");
	menuitem.addActionListener((ActionEvent ex) -> {
	  StringBuilder list = new StringBuilder();
	  for (int row: rows) {
	    Object obj = m_TableIDs.getValueAt(row, col);
	    if (obj != null) {
	      if (list.length() > 0)
		list.append("\n");
	      list.append(obj);
	    }
	  }
	  ClipboardHelper.copyToClipboard(list.toString());
	});
	result.add(menuitem);
      }
    }

    return result;
  }

  /**
   * Updates the model with the specified IDs.
   */
  public void setIDs(String[] ids) {
    m_Model = new TableModel(new SelectSpectrumPanel.TableModel(ids), "Delete");
    m_Model.addTableModelListener((TableModelEvent e) -> updateButtons());
    m_TableIDs.setModel(m_Model);
    updateButtons();
    if (ids.length == 0) {
      GUIHelper.showErrorMessage(
	DeleteSampleDataPanel.this, "No IDs found, check console for potential errors!");
    }
    else {
      updateProperties();
    }
  }

  /**
   * Updates the selected spectra.
   */
  protected void delete() {
    final String[]	sel;
    Field[]		fields;
    SwingWorker		worker;

    sel    = m_Model.getSelectedSampleIDs();
    fields = (Field[]) m_PanelFields.getValue();
    worker = new SwingWorker() {
      protected boolean successful;
      @Override
      protected Object doInBackground() throws Exception {
	successful = true;
	MouseUtils.setWaitCursor(DeleteSampleDataPanel.this);
	SampleDataF sdt = SampleDataF.getSingleton(DatabaseConnection.getSingleton());
	for (int i = 0; i < sel.length; i++) {
	  m_StatusBar.showStatus("Deleting: " + (i+1) + "/" + sel.length + "...");
	  for (Field field: fields)
	    sdt.remove(sel[i], field);
	}
	return null;
      }
      @Override
      protected void done() {
	super.done();
	MouseUtils.setDefaultCursor(DeleteSampleDataPanel.this);
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
    int		fieldCount;

    selCount   = m_Model.getCheckedCount();
    fieldCount = ((Field[]) m_PanelFields.getValue()).length;

    m_ButtonDelete.setEnabled(!m_CurrentIDProvider.isWorking() && (selCount > 0) && (fieldCount > 0));
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
