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
 * WriteSpectrum.java
 * Copyright (C) 2009-2019 University of Waikato, Hamilton, New Zealand
 */
package adams.gui.scripting;

import adams.core.io.PlaceholderFile;
import adams.core.option.OptionUtils;
import adams.data.io.output.AbstractDataContainerWriter;
import adams.data.io.output.SimpleSpectrumWriter;
import adams.data.spectrum.Spectrum;

/**
 <!-- scriptlet-parameters-start -->
 * Action parameters:<br>
 * <pre>   write-spectrum &lt;1-based index&gt; &lt;filename&gt; [writer command-line]</pre>
 * <br><br>
 <!-- scriptlet-parameters-end -->
 *
 <!-- scriptlet-description-start -->
 * Description:
 * <pre>   Saves the spectrum at the specified position to the file.</pre>
 * <br><br>
 <!-- scriptlet-description-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class WriteSpectrum
  extends AbstractSpectrumPanelScriptlet {

  /** for serialization. */
  private static final long serialVersionUID = 8807365128098978181L;

  /** the action to execute. */
  public final static String ACTION = "write-spectrum";

  /**
   * Returns the action string used in the command processor.
   *
   * @return		the action string
   */
  public String getAction() {
    return ACTION;
  }

  /**
   * Returns a one-line listing of the options of the action.
   *
   * @return		the options or null if none
   */
  protected String getOptionsDescription() {
    return "<1-based index> <filename> [writer command-line]";
  }

  /**
   * Returns the full description of the action.
   *
   * @return		the full description
   */
  public String getDescription() {
    return "Saves the spectrum at the specified position to the file.";
  }

  /**
   * Processes the options.
   *
   * @param options	additional/optional options for the action
   * @return		null if no error, otherwise error message
   * @throws Exception 	if something goes wrong
   */
  @Override
  protected String doProcess(String options) throws Exception {
    String			result;
    String[]			list;
    int				index;
    String			filename;
    boolean			ok;
    Spectrum 			sp;
    AbstractDataContainerWriter	writer;

    result   = null;
    list     = OptionUtils.splitOptions(options);
    index    = Integer.parseInt(list[0]) - 1;
    filename = list[1];
    if (list.length > 2)
      writer = AbstractDataContainerWriter.forCommandLine(list[2]);
    else
      writer = new SimpleSpectrumWriter();
    writer.setOutput(new PlaceholderFile(filename));

    showStatus("Writing spectrum to '" + filename + "'...");

    sp = getSpectrumPanel().getContainerManager().get(index).getData();
    ok = writer.write(sp);
    if (!ok)
      result = "Error saving spectrum to file '" + filename + "'!";

    showStatus("");

    return result;
  }
}
