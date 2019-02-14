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
 * ManualListPanel.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.gui.tools.idprovider;

import adams.core.Properties;
import adams.core.base.BaseObject;
import adams.core.base.BaseString;
import adams.db.DatabaseConnection;
import adams.db.SpectrumT;
import adams.gui.core.BaseButton;
import adams.gui.core.BaseTextField;
import adams.gui.core.GUIHelper;
import adams.gui.core.MouseUtils;
import adams.gui.goe.GenericArrayEditorPanel;

import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows the user to specify the IDs manually.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ManualListPanel
  extends AbstractIDProviderPanel {

  private static final long serialVersionUID = 3363574734910132337L;

  public static final String MANUAL_FORMAT = "Manual.Format";

  /** whether IDs are currently being loaded. */
  protected boolean m_Loading;

  /** the list of IDs. */
  protected GenericArrayEditorPanel m_PanelList;

  /** the spectrum format. */
  protected BaseTextField m_TextFormat;

  /** the button for loading the spreadsheet. */
  protected BaseButton m_ButtonLoad;

  /**
   * Initializes the panel with the owner.
   *
   * @param owner the owning panel
   */
  public ManualListPanel(IDConsumer owner) {
    super(owner);
  }

  /**
   * For initializing members.
   */
  @Override
  protected void initialize() {
    if (m_Owner == null)
      return;

    super.initialize();

    m_Loading = false;
  }

  /**
   * For initializing the GUI.
   */
  @Override
  protected void initGUI() {
    JLabel 		label;
    Properties		props;

    if (m_Owner == null)
      return;

    super.initGUI();

    props = m_Owner.getProperties();

    setLayout(new FlowLayout(FlowLayout.LEFT));

    m_PanelList = new GenericArrayEditorPanel(new BaseString[0]);
    m_PanelList.setCurrent(new BaseString[0]);
    m_PanelList.addChangeListener((ChangeEvent e) -> load());
    add(m_PanelList);

    m_TextFormat = new BaseTextField(props.getProperty(MANUAL_FORMAT, "NIR"));
    m_TextFormat.setColumns(10);
    label = new JLabel("Format");
    label.setDisplayedMnemonic('F');
    label.setLabelFor(m_TextFormat);
    add(label);
    add(m_TextFormat);

    m_ButtonLoad = new BaseButton("Load");
    m_ButtonLoad.addActionListener((ActionEvent e) -> load());
    add(m_ButtonLoad);
  }

  /**
   * finishes the initialization.
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
    return "Manual list";
  }

  /**
   * Returns the parameters as options.
   *
   * @return  		the options
   */
  @Override
  public Properties getPanelProperties() {
    Properties		result;

    result = new Properties();
    result.setProperty(MANUAL_FORMAT, m_TextFormat.getText());

    return result;
  }

  /**
   * Returns whether IDs are currently being determined.
   *
   * @return		true if determining IDs
   */
  @Override
  public boolean isWorking() {
    return m_Loading;
  }

  /**
   * Updates the state of the buttons.
   */
  @Override
  public void updateButtons() {
    m_ButtonLoad.setEnabled((((BaseString[]) m_PanelList.getCurrent()).length > 0) && !m_TextFormat.getText().isEmpty());
  }

  /**
   * Loads the spreadsheet and determines the IDs.
   */
  protected void load() {
    SwingWorker worker;

    worker = new SwingWorker() {
      protected List<String> ids;
      protected String error;
      @Override
      protected Object doInBackground() throws Exception {
        if (m_Owner instanceof Component)
	  MouseUtils.setWaitCursor((Component) m_Owner);
        ids = new ArrayList<>();
        error = null;
        m_Loading = true;
	String[] items = BaseObject.toStringArray((BaseString[]) m_PanelList.getCurrent());
	SpectrumT spt = SpectrumT.getSingleton(DatabaseConnection.getSingleton());
	String format = m_TextFormat.getText();
	for (String item: items) {
	  if (spt.exists(item, format))
	    ids.add(spt.getDatabaseID(item, format) + "\t" + item + "\t" + format);
	  else
	    getLogger().warning("Failed to determine database ID for: " + item + "/" + format);
	}
	return null;
      }

      @Override
      protected void done() {
	super.done();
	m_Owner.setIDs(ids.toArray(new String[0]));
        if (m_Owner instanceof Component)
	  MouseUtils.setDefaultCursor((Component) m_Owner);
	m_Loading = false;
	updateButtons();
	if (ids.size() == 0) {
	  String msg;
	  if (error == null)
	    msg = "Failed to retrieve any IDs from manual list, check console for potential errors!";
	  else
	    msg = "Failed to retrieve any IDs from manual list:\n" + error;
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
}
