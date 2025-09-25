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
 * ZippedSpectrumWriter.java
 * Copyright (C) 2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.output;

import adams.core.ObjectCopyHelper;
import adams.core.io.FileUtils;
import adams.data.io.input.ZippedSpectrumReader;
import adams.data.spectrum.Spectrum;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Writes spectra to the zip file using the specified base writer (must support streaming).
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
 * <pre>-output &lt;adams.core.io.PlaceholderFile&gt; (property: output)
 * &nbsp;&nbsp;&nbsp;The file to write the spectra to.
 * &nbsp;&nbsp;&nbsp;default: ${TMP}&#47;out.tmp
 * </pre>
 *
 * <pre>-writer &lt;adams.data.io.output.StreamableDataContainerWriter&gt; (property: writer)
 * &nbsp;&nbsp;&nbsp;The base writer to use for writing the spectra in the zip file.
 * &nbsp;&nbsp;&nbsp;default: adams.data.io.output.SimpleSpectrumWriter
 * </pre>
 *
 <!-- options-end -->
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class ZippedSpectrumWriter
  extends AbstractSpectrumWriter {

  private static final long serialVersionUID = -3813846494467116842L;

  /** the base writer to use. */
  protected StreamableDataContainerWriter m_Writer;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Writes spectra to the zip file using the specified base writer (must support streaming).";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "writer", "writer",
      new SimpleSpectrumWriter());
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_OutputIsFile = true;
  }

  /**
   * Sets the base writer to us.
   *
   * @param value 	the writer to use
   */
  public void setWriter(StreamableDataContainerWriter value) {
    m_Writer = value;
    reset();
  }

  /**
   * The base writer to use.
   *
   * @return 		the writer to use
   */
  public StreamableDataContainerWriter getWriter() {
    return m_Writer;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String writerTipText() {
    return "The base writer to use for writing the spectra in the zip file.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the file chooser
   */
  @Override
  public String getFormatDescription() {
    return new ZippedSpectrumReader().getFormatDescription();
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new ZippedSpectrumReader().getFormatExtensions();
  }

  /**
   * Returns whether writing of multiple containers is supported.
   *
   * @return 		true if multiple containers are supported
   */
  @Override
  public boolean canWriteMultiple() {
    return true;
  }

  /**
   * Generates a new name. Uses the sample ID by default, but may use (index+1) to disambiguate it.
   *
   * @param index	the current index of the spectra to write
   * @param data	the spectrum to write
   * @param names	the names so far
   * @return		the new name
   */
  protected String newName(int index, Spectrum data, Set<String> names) {
    String	result;

    result = data.getID() + "." + m_Writer.getDefaultFormatExtension();
    if (names.contains(result))
      result = data.getID() + "-" + (index+1) + "." + m_Writer.getDefaultFormatExtension();
    names.add(result);

    return result;
  }

  /**
   * Performs the actual writing.
   *
   * @param data the data to write
   * @return true if successfully written
   */
  @Override
  protected boolean writeData(List<Spectrum> data) {
    boolean				result;
    int					i;
    ZipArchiveOutputStream 		out;
    FileOutputStream 			fos;
    String				filename;
    String				msg;
    ZipArchiveEntry 			entry;
    Set<String>				names;
    StreamableDataContainerWriter	writer;
    ByteArrayOutputStream		bos;
    byte[]				bytes;

    out    = null;
    fos    = null;
    result = true;
    try {
      // create ZIP file
      fos   = new FileOutputStream(m_Output.getAbsolutePath());
      out   = new ZipArchiveOutputStream(new BufferedOutputStream(fos));
      names = new HashSet<>();
      for (i = 0; i < data.size(); i++) {
	// generate byte array from spectrum
	writer = ObjectCopyHelper.copyObject(m_Writer);
	bos = new ByteArrayOutputStream();
	writer.write(bos, data.get(i));
	bytes = bos.toByteArray();
	writer.cleanUp();
	// generate entry
	filename = newName(i, data.get(i), names);
	entry = new ZipArchiveEntry(filename);
	entry.setSize(bytes.length);
	out.putArchiveEntry(entry);
	out.write(bytes, 0, bytes.length);
	out.closeArchiveEntry();
      }
    }
    catch (Exception e) {
      msg = "Failed to generate archive '" + m_Output + "': ";
      getLogger().log(Level.SEVERE, msg, e);
      result = false;
    }
    finally {
      FileUtils.closeQuietly(out);
      FileUtils.closeQuietly(fos);
    }

    return result;
  }
}
