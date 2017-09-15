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
 * AbstractSpectrumHandler.java
 * Copyright (C) 2011-2017 University of Waikato, Hamilton, New Zealand
 */
package adams.gui.tools.previewbrowser;

import adams.core.io.PlaceholderFile;
import adams.core.option.OptionUtils;
import adams.data.io.input.AbstractSpectrumReader;
import adams.gui.scripting.AddDataFile;
import adams.gui.visualization.spectrum.SpectrumPanel;
import weka.core.Utils;

import javax.swing.JPanel;
import java.io.File;

/**
 * Ancestor for spectrum handlers.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1905 $
 */
public abstract class AbstractSpectrumHandler
  extends AbstractContentHandler 
  implements MultipleFileContentHandler {

  /** for serialization. */
  private static final long serialVersionUID = 3888106546913757095L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Displays the following spectrum types: " + Utils.arrayToString(getExtensions());
  }

  /**
   * Returns the reader to use.
   *
   * @param file	the file to read from
   * @return		the reader
   */
  protected abstract AbstractSpectrumReader getReader(File file);

  /**
   * Creates the actual view.
   *
   * @param file	the file to create the view for
   * @return		the view
   */
  @Override
  protected PreviewPanel createPreview(File file) {
    SpectrumPanel result;
    AbstractSpectrumReader	reader;

    result = new SpectrumPanel();
    reader = getReader(file);
    reader.setInput(new PlaceholderFile(file));
    result.getScriptingEngine().add(
	  result,
	  AddDataFile.ACTION + " " + OptionUtils.getCommandLine(reader));

    return new PreviewPanel(result);
  }

  /**
   * Creates the actual view.
   *
   * @param files	the files to       table = null;
create the view for
   * @return		the view
   */
  protected PreviewPanel createPreview(File[] files) {
    SpectrumPanel		result;
    AbstractSpectrumReader	reader;

    result = new SpectrumPanel();
    for (File file: files) {
      reader = getReader(file);
      reader.setInput(new PlaceholderFile(file));
      result.getScriptingEngine().add(
	  result,
	  AddDataFile.ACTION + " " + OptionUtils.getCommandLine(reader));
    }

    return new PreviewPanel(result);
  }

  /**
   * Returns the preview for the specified files.
   *
   * @param files	the files to create the view for
   * @return		the preview, NoPreviewAvailablePanel in case of an error
   * @see		NoPreviewAvailablePanel
   */
  public JPanel getPreview(File[] files) {
    String	msg;

    for (File file: files) {
      msg = checkFile(file);
      if (msg != null) {
	getLogger().severe(msg);
	return new NoPreviewAvailablePanel();
      }
    }
    
    return createPreview(files);
  }
}
