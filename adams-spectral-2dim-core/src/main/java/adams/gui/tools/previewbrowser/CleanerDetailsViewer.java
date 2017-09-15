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
 * CleanerDetailsViewer.java
 * Copyright (C) 2012-2017 University of Waikato, Hamilton, New Zealand
 */
package adams.gui.tools.previewbrowser;

import adams.data.cleaner.CleanerDetails;
import adams.data.spreadsheet.SpreadSheet;
import adams.gui.core.BaseScrollPane;
import adams.gui.core.SpreadSheetTable;
import adams.gui.core.TextEditorPanel;

/**
 * Viewer for details of a cleaner.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1801 $
 */
public class CleanerDetailsViewer
  extends AbstractSerializedObjectViewer {

  /** for serialization. */
  private static final long serialVersionUID = 4164063606439170399L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Outputs the details of a cleaner.";
  }

  /**
   * Returns whether viewer handles this object.
   * 
   * @param obj		the object to check
   * @return		true if the object can be handled
   */
  @Override
  public boolean handles(Object obj) {
    return (obj instanceof CleanerDetails);
  }

  /**
   * Creates a {@link PreviewPanel} for the given object.
   * 
   * @param obj		the object to create a preview for
   * @return		the preview, null if failed to generate
   */
  @Override
  protected PreviewPanel createPreview(Object obj) {
    PreviewPanel	result;
    Object		details;
    SpreadSheetTable	table;
    TextEditorPanel	editor;

    result  = null;
    details = ((CleanerDetails) obj).getDetails();
    
    if (details instanceof SpreadSheet) {
      table  = new SpreadSheetTable((SpreadSheet) details);
      result = new PreviewPanel(new BaseScrollPane(table), table);
    }
    else {
      editor = new TextEditorPanel();
      editor.setEditable(false);
      editor.setContent(details.toString());
      result = new PreviewPanel(editor);
    }
    
    return result;
  }
}
