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

/**
 * ThreeWayHeatmapReader.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input;

import adams.data.conversion.ThreeWayDataToHeatmap;
import adams.data.heatmap.Heatmap;
import adams.data.threeway.ThreeWayData;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Uses the specified reader to load 3-way data and turns it into a heatmap.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-input &lt;adams.core.io.PlaceholderFile&gt; (property: input)
 * &nbsp;&nbsp;&nbsp;The file to read and turn into a container.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 * 
 * <pre>-create-dummy-report &lt;boolean&gt; (property: createDummyReport)
 * &nbsp;&nbsp;&nbsp;If true, then a dummy report is created if none present.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-use-absolute-source &lt;boolean&gt; (property: useAbsoluteSource)
 * &nbsp;&nbsp;&nbsp;If enabled the source report field stores the absolute file name rather 
 * &nbsp;&nbsp;&nbsp;than just the name.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-reader &lt;adams.data.io.input.AbstractThreeWayDataReader&gt; (property: reader)
 * &nbsp;&nbsp;&nbsp;The reader to use for reading the 3-way data.
 * &nbsp;&nbsp;&nbsp;default: adams.data.io.input.SimpleEEMReader
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ThreeWayHeatmapReader
  extends AbstractHeatmapReader {

  private static final long serialVersionUID = -2800345085313533207L;

  /** the reader to use. */
  protected AbstractThreeWayDataReader m_Reader;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Uses the specified reader to load 3-way data and turns it into a heatmap.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "reader", "reader",
      getDefaultReader());
  }

  /**
   * Returns the default reader to use.
   * 
   * @return		the reader
   */
  protected AbstractThreeWayDataReader getDefaultReader() {
    return new SimpleEEMReader();
  }

  /**
   * Sets the reader to use.
   *
   * @param value	the reader
   */
  public void setReader(AbstractThreeWayDataReader value) {
    m_Reader = value;
    reset();
  }

  /**
   * Returns the reader to use.
   *
   * @return		the reader
   */
  public AbstractThreeWayDataReader getReader() {
    return m_Reader;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String readerTipText() {
    return "The reader to use for reading the 3-way data.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "3-way data reader";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"*"};
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    List<ThreeWayData> 		list;
    ThreeWayDataToHeatmap	conv;
    Heatmap			heatmap;
    int				i;
    String			msg;

    m_Reader.setInput(m_Input);
    list = m_Reader.read();
    conv = new ThreeWayDataToHeatmap();
    for (i = 0; i < list.size(); i++) {
      conv.setInput(list.get(i));
      msg = conv.convert();
      if (msg != null) {
	getLogger().severe("Failed to convert 3-way data item #" + (i+1) + ": " + msg);
      }
      else {
	heatmap = (Heatmap) conv.getOutput();
	m_ReadData.add(heatmap);
      }
      conv.cleanUp();
    }
  }
}
