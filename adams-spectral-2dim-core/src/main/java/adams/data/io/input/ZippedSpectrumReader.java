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
 * ZippedSpectrumReader.java
 * Copyright (C) 2018-2024 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input;

import adams.core.base.BaseRegExp;
import adams.core.io.FileUtils;
import adams.core.io.PlaceholderFile;
import adams.core.io.TempUtils;
import adams.data.spectrum.Spectrum;
import adams.env.Environment;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Reads spectra from the zip file using the specified base reader.
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
 * <pre>-reader &lt;adams.data.io.input.AbstractSpectrumReader&gt; (property: reader)
 * &nbsp;&nbsp;&nbsp;The reader to use for reading the spectra from the zip file.
 * &nbsp;&nbsp;&nbsp;default: adams.data.io.input.SimpleSpectrumReader
 * </pre>
 *
 * <pre>-reg-exp &lt;adams.core.base.BaseRegExp&gt; (property: regExp)
 * &nbsp;&nbsp;&nbsp;The regular expression that the file names must match in order to be extracted.
 * &nbsp;&nbsp;&nbsp;default: .*
 * &nbsp;&nbsp;&nbsp;more: https:&#47;&#47;docs.oracle.com&#47;javase&#47;8&#47;docs&#47;api&#47;java&#47;util&#47;regex&#47;Pattern.html
 * </pre>
 *
 * <pre>-invert &lt;boolean&gt; (property: invertMatching)
 * &nbsp;&nbsp;&nbsp;If set to true, the matching sense of the regular expression is inverted.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-buffer &lt;int&gt; (property: bufferSize)
 * &nbsp;&nbsp;&nbsp;The size of the buffer in bytes for the data stream.
 * &nbsp;&nbsp;&nbsp;default: 1024
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ZippedSpectrumReader
  extends AbstractSpectrumReader {

  private static final long serialVersionUID = 4033490304361538289L;

  /** the base reader to use. */
  protected AbstractSpectrumReader m_Reader;

  /** the regular expression that the filenames must match to be extracted. */
  protected BaseRegExp m_RegExp;

  /** invert matching sense. */
  protected boolean m_InvertMatching;

  /** the buffer size to use. */
  protected int m_BufferSize;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads spectra from the zip file using the specified base reader.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "reader", "reader",
      new SimpleSpectrumReader());

    m_OptionManager.add(
      "reg-exp", "regExp",
      new BaseRegExp(BaseRegExp.MATCH_ALL));

    m_OptionManager.add(
      "invert", "invertMatching",
      false);

    m_OptionManager.add(
      "buffer", "bufferSize",
      1024);
  }

  /**
   * Initializes the reader.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_InputIsFile = true;
  }

  /**
   * Sets the reader to use.
   *
   * @param value 	the reader to use
   */
  public void setReader(AbstractSpectrumReader value) {
    m_Reader = value;
    reset();
  }

  /**
   * Returns the reader to use.
   *
   * @return 		the reader to use
   */
  public AbstractSpectrumReader getReader() {
    return m_Reader;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for
   * displaying in the GUI or for listing the options.
   */
  public String readerTipText() {
    return "The reader to use for reading the spectra from the zip file.";
  }

  /**
   * Sets the regular expression that the filenames must match.
   *
   * @param value	the regular expression
   */
  public void setRegExp(BaseRegExp value) {
    m_RegExp = value;
    reset();
  }

  /**
   * Returns the regular expression that the filenames must match.
   *
   * @return 		the regular expression
   */
  public BaseRegExp getRegExp() {
    return m_RegExp;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return         tip text for this property suitable for
   *             displaying in the GUI or for listing the options.
   */
  public String regExpTipText() {
    return
      "The regular expression that the file names must match in order to "
	+ "be extracted.";
  }

  /**
   * Sets whether to invert the matching sense of the regular expression.
   *
   * @param value	true if the matching sense is to be inverted
   */
  public void setInvertMatching(boolean value) {
    m_InvertMatching = value;
    reset();
  }

  /**
   * Returns whether to invert the matching sense of the regular expression.
   *
   * @return 		true if the matching sense is to be inverted
   */
  public boolean getInvertMatching() {
    return m_InvertMatching;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return         tip text for this property suitable for
   *             displaying in the GUI or for listing the options.
   */
  public String invertMatchingTipText() {
    return
      "If set to true, the matching sense of the regular expression is inverted.";
  }

  /**
   * Sets the buffer size for the stream.
   *
   * @param value	the size in bytes
   */
  public void setBufferSize(int value) {
    m_BufferSize = value;
    reset();
  }

  /**
   * Returns the buffer size for the stream.
   *
   * @return 		the size in bytes
   */
  public int getBufferSize() {
    return m_BufferSize;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return         tip text for this property suitable for
   *             displaying in the GUI or for listing the options.
   */
  public String bufferSizeTipText() {
    return "The size of the buffer in bytes for the data stream.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Multi-spectra zip file";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"zip"};
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    ZipFile				archive;
    Enumeration<ZipArchiveEntry> 	enm;
    ZipArchiveEntry			entry;
    File 				outFile;
    BufferedInputStream			in;
    BufferedOutputStream		out;
    FileOutputStream			fos;
    int					len;
    long				read;
    byte[]				buffer;
    List<Spectrum>			sublist;

    try {
      buffer  = new byte[m_BufferSize];
      archive = ZipFile.builder().setFile(m_Input.getAbsoluteFile()).get();
      enm     = archive.getEntries();
      while (enm.hasMoreElements()) {
	entry = enm.nextElement();

	if (entry.isDirectory())
	  continue;

	// does name match?
	if (!m_RegExp.isMatchAll() && !m_RegExp.isEmpty()) {
	  if (m_InvertMatching && m_RegExp.isMatch(entry.getName()))
	    continue;
	  else if (!m_InvertMatching && !m_RegExp.isMatch(entry.getName()))
	    continue;
	}

	if (isLoggingEnabled())
	  getLogger().info("Reading: " + entry.getName());

	in      = null;
	out     = null;
	fos     = null;
	outFile = null;
	try {
	  // assemble output name
	  outFile = TempUtils.createTempFile(FileUtils.replaceExtension(entry.getName(), ""), "." + FileUtils.getExtension(entry.getName()));

	  // extract data
	  in   = new BufferedInputStream(archive.getInputStream(entry));
	  fos  = new FileOutputStream(outFile.getAbsolutePath());
	  out  = new BufferedOutputStream(fos, m_BufferSize);
	  read = 0;
	  while (read < entry.getSize()) {
	    len   = in.read(buffer);
	    read += len;
	    out.write(buffer, 0, len);
	  }
	}
	catch (Exception e) {
	  getLogger().log(Level.SEVERE, "Error extracting '" + entry.getName() + "' to '" + outFile + "'!", e);
	}
	finally {
	  FileUtils.closeQuietly(in);
	  FileUtils.closeQuietly(out);
	  FileUtils.closeQuietly(fos);
	}

	// read file
	m_Reader.setInput(new PlaceholderFile(outFile));
	sublist = m_Reader.read();
	if (sublist != null)
	  m_ReadData.addAll(sublist);

	// delete file again
	if (outFile != null)
	  FileUtils.delete(outFile);
      }
    }
    catch (Exception e) {
      m_ReadData.clear();
      getLogger().log(Level.SEVERE, "Failed to read from: " + m_Input, e);
    }
  }

  /**
   * Runs the reader from the command-line.
   *
   * If the option {@link #OPTION_OUTPUTDIR} is specified then the read spectra
   * get output as .spec files in that directory.
   *
   * @param args	the command-line options to use
   */
  public static void main(String[] args) {
    runReader(Environment.class, ZippedSpectrumReader.class, args);
  }
}
