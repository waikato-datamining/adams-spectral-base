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
 * SpreadSheetIDPanel.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.gui.tools.idprovider;

import adams.core.Index;
import adams.core.Properties;
import adams.core.io.PlaceholderFile;
import adams.core.option.OptionUtils;
import adams.data.io.input.CsvSpreadSheetReader;
import adams.data.io.input.SpreadSheetReader;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.spreadsheet.SpreadSheetColumnIndex;
import adams.data.spreadsheet.SpreadSheetUtils;
import adams.db.DatabaseConnection;
import adams.db.SpectrumF;
import adams.gui.chooser.SpreadSheetFileChooserPanel;
import adams.gui.core.BaseButton;
import adams.gui.core.BaseTextField;
import adams.gui.core.ConsolePanel;
import adams.gui.core.GUIHelper;
import adams.gui.core.IndexTextField;
import adams.gui.core.MouseUtils;

import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows the user to load IDs from a spreadsheet.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpreadSheetIDPanel
  extends AbstractIDProviderPanel {

  private static final long serialVersionUID = 3363574734910132337L;

  public static final String SPREADSHEET_FILE = "Spreadsheet.File";

  public static final String SPREADSHEET_READER = "Spreadsheet.Reader";

  public static final String SPREADSHEET_COLUMN = "Spreadsheet.Column";

  public static final String SPREADSHEET_FORMAT = "Spreadsheet.Format";

  /** whether a spreadsheet is currently being loaded. */
  protected boolean m_Loading;

  /** the file to load. */
  protected SpreadSheetFileChooserPanel m_PanelFile;

  /** the column to get the ID from. */
  protected IndexTextField m_TextColumn;

  /** the spectrum format. */
  protected BaseTextField m_TextFormat;

  /** the button for loading the spreadsheet. */
  protected BaseButton m_ButtonLoad;

  /**
   * Initializes the panel with the owner.
   *
   * @param owner the owning panel
   */
  public SpreadSheetIDPanel(IDConsumer owner) {
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
    JLabel		label;
    Properties		props;
    String		reader;

    if (m_Owner == null)
      return;

    props = m_Owner.getProperties();

    super.initGUI();

    setLayout(new FlowLayout(FlowLayout.LEFT));

    m_PanelFile = new SpreadSheetFileChooserPanel();
    m_PanelFile.setCurrent(new PlaceholderFile(props.getProperty(SPREADSHEET_FILE, ".")));
    m_PanelFile.addChangeListener((ChangeEvent e) -> updateButtons());
    reader = props.getProperty(SPREADSHEET_READER, new CsvSpreadSheetReader().toCommandLine());
    try {
      m_PanelFile.setReader((SpreadSheetReader) OptionUtils.forAnyCommandLine(SpreadSheetReader.class, reader));
    }
    catch (Exception e) {
      ConsolePanel.getSingleton().append("Failed to instantiate spreadsheet reader:\n" + reader, e);
      m_PanelFile.setReader(new CsvSpreadSheetReader());
    }
    add(m_PanelFile);

    m_TextColumn = new IndexTextField(props.getProperty(SPREADSHEET_COLUMN, Index.FIRST));
    m_TextColumn.setColumns(10);
    label = new JLabel("ID column");
    label.setDisplayedMnemonic('I');
    label.setLabelFor(m_TextColumn);
    add(label);
    add(m_TextColumn);

    m_TextFormat = new BaseTextField(props.getProperty(SPREADSHEET_FORMAT, "NIR"));
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
    return "Spreadsheet";
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
    result.setProperty(SPREADSHEET_FILE, m_PanelFile.getCurrent().getAbsolutePath());
    result.setProperty(SPREADSHEET_READER, m_PanelFile.getReader().toCommandLine());
    result.setProperty(SPREADSHEET_COLUMN, m_TextColumn.getText());
    result.setProperty(SPREADSHEET_FORMAT, m_TextFormat.getText());

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
    m_ButtonLoad.setEnabled(!m_PanelFile.getCurrent().isDirectory() && m_PanelFile.getCurrent().exists() && !m_TextFormat.getText().isEmpty());
  }

  /**
   * Loads the spreadsheet and determines the IDs.
   */
  protected void load() {
    SwingWorker		worker;

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
	SpreadSheetReader reader = m_PanelFile.getReader();
	SpreadSheet sheet = reader.read(m_PanelFile.getCurrent());
	SpreadSheetColumnIndex index = new SpreadSheetColumnIndex(m_TextColumn.getText());
	index.setData(sheet);
	int column = index.getIntIndex();
	if (column == -1) {
	  error = "Failed to locate column: " + m_TextColumn.getText();
	}
	else {
	  String[] items = SpreadSheetUtils.getColumn(sheet, column, true, true);
	  SpectrumF spt = SpectrumF.getSingleton(DatabaseConnection.getSingleton());
	  String format = m_TextFormat.getText();
	  for (String item: items) {
	    if (spt.exists(item, format))
	      ids.add(spt.getDatabaseID(item, format) + "\t" + item + "\t" + format);
	    else
	      getLogger().warning("Failed to determine database ID for: " + item + "/" + format);
	  }
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
	    msg = "Failed to retrieve any IDs from '" + m_PanelFile.getCurrent() + "', check console for potential errors!";
	  else
	    msg = "Failed to retrieve any IDs from '" + m_PanelFile.getCurrent() + "':\n" + error;
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
