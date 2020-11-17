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
 * ExportValidationSets.java
 * Copyright (C) 2017-2020 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.gui.menu;

import adams.core.MessageCollection;
import adams.core.Properties;
import adams.core.io.FileUtils;
import adams.core.io.PlaceholderDirectory;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.Report;
import adams.db.DatabaseConnection;
import adams.db.SampleDataF;
import adams.db.SpectrumConditionsSingle;
import adams.gui.application.AbstractApplicationFrame;
import adams.gui.application.AbstractBasicMenuItemDefinition;
import adams.gui.application.ChildFrame;
import adams.gui.application.UserMode;
import adams.gui.core.GUIHelper;
import adams.gui.core.PropertiesParameterPanel.PropertyType;
import adams.gui.wizard.AbstractWizardPage;
import adams.gui.wizard.FinalPage;
import adams.gui.wizard.ParameterPanelPage;
import adams.gui.wizard.SelectDirectoryPage;
import adams.gui.wizard.WizardPane;

import javax.swing.SwingWorker;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays the dialog for exporting validation sets.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class ExportValidationSets
  extends AbstractBasicMenuItemDefinition {

  /** for serialization. */
  private static final long serialVersionUID = 7464423632324133713L;

  public static final String COMPONENTS = "components";

  public static final String VALIDATION_REPORT_FLAG = "validation_report_flag";

  /**
   * Initializes the menu item.
   */
  public ExportValidationSets() {
    this(null);
  }

  /**
   * Initializes the menu item.
   *
   * @param owner	the owning application
   */
  public ExportValidationSets(AbstractApplicationFrame owner) {
    super(owner);
  }

  /**
   * Returns the file name of the icon.
   *
   * @return		the filename or null if no icon available
   */
  public String getIconName() {
    return "exportvalidationsets.gif";
  }

  /**
   * Launches the functionality of the menu item.
   */
  public void launch() {
    final WizardPane	wizard;
    ParameterPanelPage 	components;
    Properties		props;
    SelectDirectoryPage outdir;
    FinalPage		finalpage;
    final ChildFrame 	frame;

    wizard = new WizardPane();
    wizard.setCustomFinishText("Export");

    components = new ParameterPanelPage("Components");
    components.setDescription(
      "List the components that you want to generate validation text files for.\n"
	+ "Enter the component names that you want to export as well as the "
	+ "boolean flag in the sample data that indicates whether a sample is to "
	+ "be used in validation sets.");
    components.getParameterPanel().addPropertyType(COMPONENTS, PropertyType.COMMA_SEPARATED_LIST);
    components.getParameterPanel().setLabel(COMPONENTS, "Components");
    components.getParameterPanel().addPropertyType(VALIDATION_REPORT_FLAG, PropertyType.STRING);
    components.getParameterPanel().setLabel(VALIDATION_REPORT_FLAG, "Report flag to use");
    components.setPageCheck((AbstractWizardPage page) -> {
      Properties p = page.getProperties();
      if (p.getProperty(COMPONENTS) == null)
        return false;
      if (p.getProperty(COMPONENTS).trim().length() == 0)
        return false;
      if (p.getProperty(VALIDATION_REPORT_FLAG) == null)
        return false;
      if (p.getProperty(VALIDATION_REPORT_FLAG).trim().length() == 0)
        return false;
      return true;
    });
    props = new Properties();
    props.setProperty(COMPONENTS, "");
    props.setProperty(VALIDATION_REPORT_FLAG, "validation");
    components.getParameterPanel().setProperties(props);
    wizard.addPage(components);

    outdir = new SelectDirectoryPage("Output");
    outdir.setDescription("Select the output directory for storing the validation set text files.");
    wizard.addPage(outdir);

    finalpage = new FinalPage();
    finalpage.setLogo(null);
    finalpage.setDescription("<html><h2>Ready</h2>Please click on <b>Export</b> to start the process.</html>");
    wizard.addPage(finalpage);

    frame = createChildFrame(wizard, GUIHelper.makeSmaller(GUIHelper.getDefaultDialogDimension()));
    wizard.addActionListener((ActionEvent e) -> {
      if (!e.getActionCommand().equals(WizardPane.ACTION_FINISH)) {
	frame.dispose();
	return;
      }
      doExport(frame, wizard.getProperties(false));
    });
    wizard.update();
  }

  /**
   * Peforms the export.
   *
   * @param frame	the frame
   * @param props	the configuration
   */
  protected void doExport(ChildFrame frame, Properties props) {
    final PlaceholderDirectory		dir;
    final String[]			comps;
    final Map<String,String> 		files;
    String				flag;
    Field				field;
    final SpectrumConditionsSingle	cond;
    SwingWorker				worker;

    dir   = new PlaceholderDirectory(props.getProperty(SelectDirectoryPage.KEY_DIRECTORY, "."));
    comps = props.getProperty(COMPONENTS).split(",");
    files = new HashMap<>();
    for (String comp: comps)
      files.put(comp, dir.getAbsolutePath() + File.separator + comp.toLowerCase() + ".txt");
    flag  = props.getProperty(VALIDATION_REPORT_FLAG);
    field = new Field(flag, DataType.BOOLEAN);
    cond  = new SpectrumConditionsSingle();
    cond.setField(new Field(flag, DataType.BOOLEAN));
    cond.setLimit(-1);
    worker = new SwingWorker() {
      protected MessageCollection errors = new MessageCollection();
      protected int numIDs = -1;

      @Override
      protected Object doInBackground() throws Exception {
        getLogger().info("Removing old files...");
        for (String comp: comps) {
          File file = new File(files.get(comp));
          if (file.exists()) {
	    if (!file.delete())
	      errors.add("Failed to delete validation set file: " + file);
	  }
	}

	getLogger().info("Searching database...");
	SampleDataF sdt = SampleDataF.getSingleton(DatabaseConnection.getSingleton());
	List<String> ids = sdt.getIDs(cond);
	numIDs = ids.size();

	getLogger().info("Found " + ids.size() + " spectra");
	for (int i = 0; i < ids.size(); i++) {
	  Report report = sdt.load(ids.get(i));
	  if (report == null) {
	    errors.add("Failed to load report for sample ID: " + ids.get(i));
	  }
	  else {
	    if (report.hasValue(field) && report.getBooleanValue(field)) {
	      for (String comp : comps) {
		if (report.hasValue(comp))
		  FileUtils.writeToFile(files.get(comp), ids.get(i), true);
	      }
	    }
	  }
	  if ((i+1) % 100 == 0)
	    getLogger().info("Processed " + (i+1) + "/" + ids.size() + " IDs...");
	}
	getLogger().info("Finished!");
	return null;
      }

      @Override
      protected void done() {
        if (errors.isEmpty())
	  GUIHelper.showInformationMessage(null, "Successfully exported " + numIDs + " IDs to " + dir + "!");
        else
          GUIHelper.showErrorMessage(null, "Errors exporting " + numIDs + " sample IDs to " + dir + "!\n" + errors);
	frame.dispose();
	super.done();
      }
    };
    worker.execute();
  }

  /**
   * Returns the title of the window (and text of menuitem).
   *
   * @return 		the title
   */
  public String getTitle() {
    return "Export validation sets";
  }

  /**
   * Whether the panel can only be displayed once.
   *
   * @return		true if the panel can only be displayed once
   */
  public boolean isSingleton() {
    return false;
  }

  /**
   * Returns the user mode, which determines visibility as well.
   *
   * @return		the user mode
   */
  public UserMode getUserMode() {
    return UserMode.BASIC;
  }

  /**
   * Returns the category of the menu item in which it should appear, i.e.,
   * the name of the menu.
   *
   * @return		the category/menu name
   */
  public String getCategory() {
    return CATEGORY_TOOLS;
  }
}