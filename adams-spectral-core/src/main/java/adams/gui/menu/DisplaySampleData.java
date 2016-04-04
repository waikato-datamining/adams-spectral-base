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
 * DisplaySampleData.java
 * Copyright (C) 2016 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.gui.menu;

import adams.core.io.PlaceholderFile;
import adams.data.io.input.AbstractReportReader;
import adams.data.report.Report;
import adams.gui.application.AbstractApplicationFrame;
import adams.gui.application.UserMode;
import adams.gui.core.BasePanel;
import adams.gui.core.SearchPanel;
import adams.gui.core.SearchPanel.LayoutType;
import adams.gui.event.SearchEvent;
import adams.gui.event.SearchListener;
import adams.gui.chooser.SampleDataFileChooser;
import adams.gui.visualization.spectrum.SampleDataFactory;
import adams.gui.visualization.spectrum.SampleDataFactory.Panel;

import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Displays sample data from loaded files.
 * <br><br>
 * If parameters are provided, the first parameter must be the class (and
 * optional parameters) and the second one the actual file/directory to load.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class DisplaySampleData
  extends AbstractParameterHandlingMenuItemDefinition {

  /** for serialization. */
  private static final long serialVersionUID = 2703609932127345924L;

  /**
   * Initializes the menu item.
   *
   * @param owner	the owning application
   */
  public DisplaySampleData(AbstractApplicationFrame owner) {
    super(owner);
  }

  /**
   * Returns the file name of the icon.
   *
   * @return		the filename or null if no icon available
   */
  @Override
  public String getIconName() {
    return "report.gif";
  }

  /**
   * Launches the functionality of the menu item.
   */
  @Override
  public void launch() {
    AbstractReportReader reader;
    if (m_Parameters.length == 2) {
      reader = (AbstractReportReader) AbstractReportReader.forCommandLine(m_Parameters[0]);
      reader.setInput(new PlaceholderFile(m_Parameters[1]));
    }
    else {
      // choose report
      SampleDataFileChooser chooser = new SampleDataFileChooser();
      int retVal = chooser.showOpenDialog(null);
      if (retVal != SampleDataFileChooser.APPROVE_OPTION)
	return;
      // load report
      reader = (AbstractReportReader) chooser.getReader();
    }
    List<Report> reports = reader.read();
    // create frame
    BasePanel panel = new BasePanel(new BorderLayout());
    final Panel rpanel = SampleDataFactory.getPanelForReports(reports);
    final SearchPanel search = new SearchPanel(LayoutType.HORIZONTAL, true);
    search.addSearchListener(new SearchListener() {
      public void searchInitiated(SearchEvent e) {
	rpanel.search(search.getSearchText(), search.isRegularExpression());
	search.grabFocus();
      }
    });
    BasePanel panel2 = new BasePanel(new FlowLayout(FlowLayout.LEFT));
    panel2.add(search);
    panel.add(rpanel, BorderLayout.CENTER);
    panel.add(panel2, BorderLayout.SOUTH);
    rpanel.setDividerLocation(600);
    createChildFrame(panel, 800, 600);
    Runnable runnable = new Runnable() {
      public void run() {
	if (rpanel.getContainerManager().count() > 0)
	  rpanel.setCurrentTable(0);
      }
    };
    SwingUtilities.invokeLater(runnable);
  }

  /**
   * Returns the title of the window (and text of menuitem).
   *
   * @return 		the title
   */
  @Override
  public String getTitle() {
    return "Display SampleData";
  }

  /**
   * Whether the panel can only be displayed once.
   *
   * @return		true if the panel can only be displayed once
   */
  @Override
  public boolean isSingleton() {
    return false;
  }

  /**
   * Returns the user mode, which determines visibility as well.
   *
   * @return		the user mode
   */
  @Override
  public UserMode getUserMode() {
    return UserMode.BASIC;
  }

  /**
   * Returns the category of the menu item in which it should appear, i.e.,
   * the name of the menu.
   *
   * @return		the category/menu name
   */
  @Override
  public String getCategory() {
     return CATEGORY_VISUALIZATION;
 }
}