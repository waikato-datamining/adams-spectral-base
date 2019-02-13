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
 * DatabaseSearchPanel.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.gui.tools.idprovider;

import adams.core.DateFormat;
import adams.core.DateUtils;
import adams.core.Properties;
import adams.core.base.BaseDate;
import adams.core.base.BaseDateTime;
import adams.core.option.OptionUtils;
import adams.db.AbstractSpectrumConditions;
import adams.db.DatabaseConnection;
import adams.db.SampleDataT;
import adams.db.SpectrumConditionsMulti;
import adams.gui.chooser.DateChooserPanel;
import adams.gui.core.BaseButton;
import adams.gui.core.GUIHelper;
import adams.gui.core.MouseUtils;
import adams.gui.goe.GenericObjectEditorDialog;

import javax.swing.JLabel;
import javax.swing.SwingWorker;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Panel for obtaining IDs from the database.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class DatabaseSearchPanel
  extends AbstractIDProviderPanel {

  private static final long serialVersionUID = -7249730293535628558L;

  /** the from date. */
  protected DateChooserPanel m_TextFrom;

  /** the to date. */
  protected DateChooserPanel m_TextTo;

  /** the button for the options. */
  protected BaseButton m_ButtonConditions;

  /** the button for the search. */
  protected BaseButton m_ButtonSearch;

  /** the conditions to use in the search. */
  protected SpectrumConditionsMulti m_Conditions;

  /** whether the search is currently happening. */
  protected boolean m_Searching;

  /** the formatter to use. */
  protected DateFormat m_Formatter;

  /**
   * Initializes the panel with the owner.
   *
   * @param owner the owning panel
   */
  public DatabaseSearchPanel(IDConsumer owner) {
    super(owner);
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    Properties	props;

    if (getOwner() == null)
      return;

    super.initialize();

    m_Formatter = DateUtils.getDateFormatter();
    props       = getOwner().getProperties();
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
    JLabel 		label;
    BaseDate 		bdate;
    Properties		props;

    if (m_Owner == null)
      return;

    super.initGUI();

    props = m_Owner.getProperties();

    setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));

    bdate = new BaseDate(BaseDate.NOW);

    // from
    m_TextFrom = new DateChooserPanel();
    m_TextFrom.setCurrent(props.getDate("From", bdate.dateValue()));
    m_TextFrom.setTextColumns(10);
    label = new JLabel("From");
    label.setDisplayedMnemonic('F');
    label.setLabelFor(m_TextFrom);
    add(label);
    add(m_TextFrom);

    // to
    m_TextTo = new DateChooserPanel();
    m_TextTo.setCurrent(props.getDate("To", bdate.dateValue()));
    m_TextTo.setTextColumns(10);
    label = new JLabel("To");
    label.setLabelFor(m_TextTo);
    add(label);
    add(m_TextTo);

    // options
    m_ButtonConditions = new BaseButton("Options");
    m_ButtonConditions.setMnemonic('O');
    m_ButtonConditions.addActionListener((ActionEvent e) -> showConditions());
    add(m_ButtonConditions);

    // search
    m_ButtonSearch = new BaseButton("Search");
    m_ButtonSearch.setMnemonic('S');
    m_ButtonSearch.addActionListener((ActionEvent e) -> search());
    add(m_ButtonSearch);
  }

  /**
   * Finishes up the initialization.
   */
  @Override
  protected void finishInit() {
    if (m_Owner == null)
      return;
    super.finishInit();
    updateButtons();
  }

  /**
   * Returns the name of the panel.
   *
   * @return		the name
   */
  @Override
  public String getPanelName() {
    return "Database";
  }

  /**
   * Returns the parameters as options.
   *
   * @return  		the options
   */
  @Override
  public Properties getPanelProperties() {
    Properties		props;

    props = new Properties();
    props.setDate("From", m_TextFrom.getCurrent());
    props.setDate("To", m_TextTo.getCurrent());
    props.setProperty("Conditions", m_Conditions.toCommandLine());

    return props;
  }

  /**
   * Returns whether IDs are currently being determined.
   *
   * @return		true if determining IDs
   */
  public boolean isWorking() {
    return m_Searching;
  }

  /**
   * Updates the state of the buttons.
   */
  public void updateButtons() {
    m_ButtonConditions.setEnabled(!m_Searching);
    m_ButtonSearch.setEnabled(!m_Searching);
  }

  /**
   * Performs the search and updates the table.
   */
  protected void search() {
    SwingWorker worker;

    fieldsToConditions();

    worker = new SwingWorker() {
      protected List<String> ids;
      @Override
      protected Object doInBackground() throws Exception {
        if (m_Owner instanceof Component)
	  MouseUtils.setWaitCursor((Component) m_Owner);
	m_Searching = true;
	updateButtons();
	SampleDataT sdt = SampleDataT.getSingleton(DatabaseConnection.getSingleton());
	ids = sdt.getIDs(new String[]{"sp.AUTO_ID", "sp.SAMPLEID", "sp.FORMAT"}, m_Conditions);
	return null;
      }
      @Override
      protected void done() {
	super.done();
	m_Owner.setIDs(ids.toArray(new String[0]));
        if (m_Owner instanceof Component)
	  MouseUtils.setDefaultCursor((Component) m_Owner);
	m_Searching = false;
	updateButtons();
	if (ids.size() == 0) {
	  String msg = "Failed to retrieve any IDs from database, check console for potential errors!";
	  if (m_Owner instanceof Component)
	    GUIHelper.showErrorMessage((Component) m_Owner, msg);
	  else
	    GUIHelper.showErrorMessage(null, msg);
	}
	else {
	  m_Owner.updateProperties();
	}
      }
    };
    worker.execute();
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
    GenericObjectEditorDialog dialog;

    fieldsToConditions();

    if (getParentDialog() != null)
      dialog = new GenericObjectEditorDialog(getParentDialog(), Dialog.ModalityType.DOCUMENT_MODAL);
    else
      dialog = new GenericObjectEditorDialog(getParentFrame(), true);
    dialog.setTitle("Sample data conditions");
    dialog.getGOEEditor().setCanChangeClassInDialog(false);
    dialog.getGOEEditor().setClassType(AbstractSpectrumConditions.class);
    dialog.setCurrent(m_Conditions);
    if (m_Owner instanceof Component)
      dialog.setLocationRelativeTo((Component) m_Owner);
    else
      dialog.setLocationRelativeTo(null);
    dialog.setVisible(true);
    if (dialog.getResult() != GenericObjectEditorDialog.APPROVE_OPTION)
      return;

    m_Conditions = (SpectrumConditionsMulti) dialog.getCurrent();
    conditionsToFields();
  }
}
