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
 * SampleDataFactory.java
 * Copyright (C) 2010-2013 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.visualization.spectrum;

import adams.data.report.Report;
import adams.data.sampledata.SampleData;
import adams.db.ReportProvider;
import adams.db.SampleDataF;
import adams.gui.chooser.AbstractReportFileChooser;
import adams.gui.chooser.SampleDataFileChooser;
import adams.gui.core.BasePanel;
import adams.gui.core.BaseScrollPane;
import adams.gui.core.SearchPanel;
import adams.gui.core.SearchPanel.LayoutType;
import adams.gui.event.SearchEvent;
import adams.gui.event.SearchListener;
import adams.gui.visualization.report.ReportContainer;
import adams.gui.visualization.report.ReportContainerManager;
import adams.gui.visualization.report.ReportFactory;
import adams.gui.visualization.report.ReportFactory.Model;
import adams.gui.visualization.report.reportfactory.AbstractTableAction;
import adams.gui.visualization.report.reportfactory.CopyFieldName;
import adams.gui.visualization.report.reportfactory.CopyFieldValue;
import adams.gui.visualization.report.reportfactory.DatabaseAddField;
import adams.gui.visualization.report.reportfactory.DatabaseModifyValue;
import adams.gui.visualization.report.reportfactory.DatabaseRemoveField;
import adams.gui.visualization.report.reportfactory.ExcludedFlag;
import adams.gui.visualization.report.reportfactory.PrintReport;
import adams.gui.visualization.report.reportfactory.SaveReport;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.util.List;

/**
 * A factory for GUI components for sample data.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1970 $
 */
public class SampleDataFactory {

  /**
   * A specialized table for displaying a SampleData.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 1970 $
   */
  public static class Table<ReportProviderByID>
    extends ReportFactory.Table {

    /** for serialization. */
    private static final long serialVersionUID = -4065569582552285461L;

    /**
     * Initializes the table.
     */
    public Table() {
      super();
    }

    /**
     * Initializes the table.
     *
     * @param report	the report to base the table on
     */
    public Table(SampleData report) {
      super(report);
    }

    /**
     * Initializes the table.
     *
     * @param model	the model to use
     */
    public Table(TableModel model) {
      super(model);
    }

    /**
     * Returns the provider for accessing the reports in the database.
     *
     * @return		the provider
     */
    @Override
    public ReportProvider<?,?> getReportProvider() {
      return SampleDataF.getSingleton(m_DatabaseConnection);
    }

    /**
     * Returns the file chooser to use for exporting the reports.
     *
     * @return		the filechooser, null if not available
     */
    @Override
    protected AbstractReportFileChooser newReportFileChooser() {
      SampleDataFileChooser result;

      result = new SampleDataFileChooser();
      result.setAutoAppendExtension(true);

      return result;
    }

    /**
     * Returns the default actions for the popup menu.
     * 
     * @return		the default actions
     */
    @Override
    protected String[] getDefaultPopupActions() {
      return new String[]{
	  CopyFieldName.class.getName(),
	  CopyFieldValue.class.getName(),
	  DatabaseModifyValue.class.getName(),
	  AbstractTableAction.SEPARATOR,
	  DatabaseRemoveField.class.getName(),
	  ExcludedFlag.class.getName(),
	  DatabaseAddField.class.getName(),
	  AbstractTableAction.SEPARATOR,
	  SaveReport.class.getName(),
	  PrintReport.class.getName(),
      };    
    }
  }

  /**
   * A specialized panel that displays reports.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 1970 $
   * @see Report
   */
  public static class Panel
    extends ReportFactory.Panel<ReportContainer, ReportContainerManager> {

    /** for serialization. */
    private static final long serialVersionUID = -2563183937371175033L;

    /**
     * Initializes the tabbed pane with not reports.
     */
    public Panel() {
      super();

      getReportContainerList().setDisplayDatabaseID(true);
      getReportContainerList().setDisplayStringGenerator(new SampleDataContainerDisplayIDGenerator());
    }

    /**
     * Creates a new table.
     *
     * @param model	the model to use
     * @return		the new table
     */
    @Override
    protected Table newTable(Model model) {
      return new Table(model);
    }
  }

  /**
   * Returns a new model for the given report.
   *
   * @param report	the report to create a model for
   * @return		the model
   */
  public static Model getModel(SampleData report) {
    return new Model(report);
  }

  /**
   * Returns a new table for the given report.
   *
   * @param report	the report to create a table for
   * @return		the table
   */
  public static Table getTable(SampleData report) {
    return new Table(report);
  }

  /**
   * Returns a new panel for the given report.
   *
   * @param report	the report to create a table/panel for
   * @return		the panel
   */
  public static BasePanel getPanel(SampleData report) {
    BasePanel	result;
    final Table	table;
    JPanel	panel;

    result = new BasePanel(new BorderLayout());
    result.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    // table
    table = new Table(new Model(report));
    result.add(new BaseScrollPane(table), BorderLayout.CENTER);

    // search
    panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    result.add(panel, BorderLayout.SOUTH);
    final SearchPanel searchPanel = new SearchPanel(LayoutType.HORIZONTAL, true);
    searchPanel.addSearchListener(new SearchListener() {
      public void searchInitiated(SearchEvent e) {
	table.search(searchPanel.getSearchText(), searchPanel.isRegularExpression());
	searchPanel.grabFocus();
      }
    });
    panel.add(searchPanel);

    return result;
  }

  /**
   * A specialized dialog that displays reports.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 1970 $
   */
  public static class Dialog
    extends ReportFactory.Dialog<ReportContainer, ReportContainerManager> {

    /** for serialization. */
    private static final long serialVersionUID = 377068894443930941L;

    /**
     * Initializes the dialog.
     *
     * @param owner	the component that controls the dialog
     * @param modality	the type of modality
     */
    public Dialog(java.awt.Dialog owner, ModalityType modality) {
      super(owner, modality);
    }

    /**
     * Initializes the dialog.
     *
     * @param owner	the component that controls the dialog
     * @param modal	if true then the dialog will be modal
     */
    public Dialog(java.awt.Frame owner, boolean modal) {
      super(owner, modal);
    }

    /**
     * Returns a new tabbed pane instance.
     *
     * @return		the tabbed pane
     */
    @Override
    protected ReportFactory.Panel newPanel() {
      return getPanel((List<ReportContainer>) null);
    }
  }

  /**
   * Returns a new table for the given reports.
   *
   * @param reports	the reports to create a tabbed pane for
   * @return		the tabbed pane
   */
  public static Panel getPanel(List<ReportContainer> reports) {
    Panel	result;

    result = new Panel();
    result.setData(reports);

    return result;
  }

  /**
   * Returns a new table for the given reports.
   *
   * @param reports	the reports to create a tabbed pane for
   * @return		the tabbed pane
   */
  public static Panel getPanelForReports(List reports) {
    Panel	result;

    result = new Panel();
    result.setReports(reports);

    return result;
  }

  /**
   * Returns a new dialog for displaying quantitation reports.
   *
   * @param owner	the owning component
   * @param modality	the type of modality
   * @return		the dialog
   */
  public static ReportFactory.Dialog getDialog(java.awt.Dialog owner, ModalityType modality) {
    return new Dialog(owner, modality);
  }

  /**
   * Returns a new dialog for displaying quantitation reports.
   *
   * @param owner	the owning component
   * @param modal	if true then the dialog will be modal
   * @return		the dialog
   */
  public static ReportFactory.Dialog getDialog(java.awt.Frame owner, boolean modal) {
    return new Dialog(owner, modal);
  }
}
