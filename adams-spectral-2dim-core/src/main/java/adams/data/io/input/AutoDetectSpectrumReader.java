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
 * AutoDetectSpectrumReader.java
 * Copyright (C) 2026 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Uses the specified spectrum readers to read the spectra.<br>
 * It identifies the relevant reader based on the supported file extensions.<br>
 * Iterates through the list of readers till a match is determined.<br>
 * Returns no spectra if no matching reader was identified.<br>
 * All readers must process files rather than directories.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * &nbsp;&nbsp;&nbsp;min-user-mode: Expert
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
 * <pre>-base-reader &lt;adams.data.io.input.AbstractSpectrumReader&gt; [-base-reader ...] (property: baseReaders)
 * &nbsp;&nbsp;&nbsp;The configured spectrum readers to use for reading the files.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 <!-- options-end -->
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class AutoDetectSpectrumReader
  extends AbstractSpectrumReader {

  private static final long serialVersionUID = -6440528556209180915L;

  /** the readers to use. */
  protected AbstractSpectrumReader[] m_BaseReaders;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Uses the specified spectrum readers to read the spectra.\n"
	     + "It identifies the relevant reader based on the supported file extensions.\n"
	     + "Iterates through the list of readers till a match is determined.\n"
	     + "Returns no spectra if no matching reader was identified.\n"
	     + "All readers must process files rather than directories.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "base-reader", "baseReaders",
      new AbstractSpectrumReader[0]);
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_InputIsFile = true;
  }

  /**
   * Sets the readers to use.
   *
   * @param value	the readers
   */
  public void setBaseReaders(AbstractSpectrumReader[] value) {
    int		i;

    for (i = 0; i < value.length; i++) {
      if (!value[i].isInputFile()) {
	getLogger().severe("Spectrum reader #" + (i + 1) + " does not handle files: " + value[i].toCommandLine());
	return;
      }
    }

    m_BaseReaders = value;
    reset();
  }

  /**
   * Returns the readers in use.
   *
   * @return 		the readers
   */
  public AbstractSpectrumReader[] getBaseReaders() {
    return m_BaseReaders;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String baseReadersTipText() {
    return "The configured spectrum readers to use for reading the files.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the
   * file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Automatic spectrum reader";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    List<String> 	result;
    String[]		exts;

    result = new ArrayList<>();
    for (AbstractSpectrumReader reader: m_BaseReaders) {
      exts = reader.getFormatExtensions();
      for (String ext: exts) {
	// matches all?
	if (ext.equals("*"))
	  return new String[]{"*"};
	result.add(ext);
      }
    }
    if (result.isEmpty())
      result.add("*");

    return result.toArray(new String[0]);
  }

  /**
   * Checks whether the spectrum reader can handle the file (based on the extension).
   *
   * @param reader	the reader to check
   * @param file	the file to check against
   * @return		true if handled
   */
  protected boolean handles(AbstractSpectrumReader reader, File file) {
    String	fileExt;

    fileExt = FileUtils.getExtension(file).toLowerCase();

    for (String ext: reader.getFormatExtensions()) {
      ext = ext.toLowerCase();
      if (ext.equals("*"))
	return true;
      if (ext.equals(fileExt))
	return true;
    }

    return false;
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    for (AbstractSpectrumReader reader: m_BaseReaders) {
      if (handles(reader, m_Input)) {
	if (isLoggingEnabled())
	  getLogger().info("Reader '" + reader.toCommandLine() + "' handles: " + m_Input);
	reader.setInput(m_Input);
	m_ReadData.addAll(reader.read());
	return;
      }
    }
    getLogger().warning("No reader could load the file: " + m_Input);
  }
}
