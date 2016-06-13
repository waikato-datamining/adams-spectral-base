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
 * ASCIIXYSpectrumReader.java
 * Copyright (C) 2016 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.base.BaseRegExp;
import adams.core.io.FileUtils;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.List;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Reads spectra in ASCII XY format.
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
 * <pre>-instrument &lt;java.lang.String&gt; (property: instrument)
 * &nbsp;&nbsp;&nbsp;The name of the instrument that generated the spectra (if not already present 
 * &nbsp;&nbsp;&nbsp;in data).
 * &nbsp;&nbsp;&nbsp;default: unknown
 * </pre>
 * 
 * <pre>-format &lt;java.lang.String&gt; (property: format)
 * &nbsp;&nbsp;&nbsp;The data format string.
 * &nbsp;&nbsp;&nbsp;default: NIR
 * </pre>
 * 
 * <pre>-keep-format &lt;boolean&gt; (property: keepFormat)
 * &nbsp;&nbsp;&nbsp;If enabled the format obtained from the file is not replaced by the format 
 * &nbsp;&nbsp;&nbsp;defined here.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-use-absolute-source &lt;boolean&gt; (property: useAbsoluteSource)
 * &nbsp;&nbsp;&nbsp;If enabled the source report field stores the absolute file name rather 
 * &nbsp;&nbsp;&nbsp;than just the name.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-separator &lt;java.lang.String&gt; (property: separator)
 * &nbsp;&nbsp;&nbsp;The separator to use for identifying X and Y columns.
 * &nbsp;&nbsp;&nbsp;default: ;
 * </pre>
 * 
 * <pre>-regexp-sample-id &lt;adams.core.base.BaseRegExp&gt; (property: regExpSampleID)
 * &nbsp;&nbsp;&nbsp;The regular expression for extracting the sample ID from the file name (
 * &nbsp;&nbsp;&nbsp;w&#47;o path).
 * &nbsp;&nbsp;&nbsp;default: (.*)\\\\.txt
 * </pre>
 * 
 * <pre>-group-sample-id &lt;int&gt; (property: groupSampleID)
 * &nbsp;&nbsp;&nbsp;The regular expression group that contains the sample ID.
 * &nbsp;&nbsp;&nbsp;default: 1
 * &nbsp;&nbsp;&nbsp;minimum: 1
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class ASCIIXYSpectrumReader
  extends AbstractSpectrumReader {

  /** for serialization. */
  private static final long serialVersionUID = -2903357410192470809L;

  /** the separator to use. */
  protected String m_Separator;

  /** regexp to extract sample ID from file name. */
  protected BaseRegExp m_RegExpSampleID;

  /** the regexp group to use. */
  protected int m_GroupSampleID;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads spectra in ASCII XY format.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "separator", "separator",
      ";");

    m_OptionManager.add(
      "regexp-sample-id", "regExpSampleID",
      new BaseRegExp("(.*)\\.txt"));

    m_OptionManager.add(
      "group-sample-id", "groupSampleID",
      1, 1, null);
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "ASCII XY format";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"txt"};
  }

  /**
   * Sets the separator to use.
   *
   * @param value	the separator
   */
  public void setSeparator(String value) {
    m_Separator = value;
    reset();
  }

  /**
   * Returns the separator in use.
   *
   * @return 		the separator
   */
  public String getSeparator() {
    return m_Separator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String separatorTipText() {
    return "The separator to use for identifying X and Y columns.";
  }

  /**
   * Sets the regular expression to use for extracting the sample ID from the
   * file name (w/o path).
   *
   * @param value	the expression
   */
  public void setRegExpSampleID(BaseRegExp value) {
    m_RegExpSampleID = value;
    reset();
  }

  /**
   * Returns the regular expression in use to extract the sample ID from the
   * file name (w/o path).
   *
   * @return 		the expression
   */
  public BaseRegExp getRegExpSampleID() {
    return m_RegExpSampleID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String regExpSampleIDTipText() {
    return
      "The regular expression for extracting the sample ID from the file "
	+ "name (w/o path).";
  }

  /**
   * Sets the regular expression group that contains the sample ID from the
   * file name (w/o path).
   *
   * @param value	the group
   */
  public void setGroupSampleID(int value) {
    if (getOptionManager().isValid("groupSampleID", value)) {
      m_GroupSampleID = value;
      reset();
    }
  }

  /**
   * Returns the regular expression group that contains the sample ID from the
   * file name (w/o path).
   *
   * @return 		the group
   */
  public int getGroupSampleID() {
    return m_GroupSampleID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String groupSampleIDTipText() {
    return "The regular expression group that contains the sample ID.";
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    Spectrum		sp;
    SpectrumPoint	point;
    String		line;
    List<String>	content;
    String[]		parts;

    try {
      sp = new Spectrum();
      sp.setID(m_Input.getName().replaceFirst(m_RegExpSampleID.getValue(), "$" + m_GroupSampleID));
      m_ReadData.add(sp);

      // read data
      content = FileUtils.loadFromFile(m_Input);

      // data points
      while (content.size() > 0) {
	line = content.get(0).trim();
	content.remove(0);
	if (line.length() == 0)
	  continue;
	parts = line.split(m_Separator);
	if (parts.length == 2) {
	  point = new SpectrumPoint(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]));
	  sp.add(point);
	}
	else {
	  getLogger().warning("Failed to parse '" + line + "'!");
	}
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to read spectral data!", e);
    }
  }
}
