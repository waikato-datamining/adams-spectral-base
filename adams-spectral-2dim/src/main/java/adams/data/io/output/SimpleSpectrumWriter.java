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
 * SimpleSpectrumWriter.java
 * Copyright (C) 2009-2015 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.output;

import adams.core.io.FileUtils;
import adams.core.io.PlaceholderFile;
import adams.data.CompressionSupporter;
import adams.data.spectrum.Spectrum;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

/**
 <!-- globalinfo-start -->
 * Writer that stores spectrums in the simple CSV format.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-output &lt;java.io.File&gt; (property: output)
 *         The file to write the spectrum to.
 *         default: /tmp/out.spec
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SimpleSpectrumWriter
  extends AbstractSpectrumWriter 
  implements CompressionSupporter {

  /** for serialization. */
  private static final long serialVersionUID = 5290679698357490093L;
  
  /** whether to output the sample data as well. */
  protected boolean m_OutputSampleData;

  /** whether to use compression. */
  protected boolean m_UseCompression;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Writer that stores spectrums in the simple CSV format.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "output-sample-data", "outputSampleData",
	    false);

    m_OptionManager.add(
	    "use-compression", "useCompression",
	    false);
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Simple CSV format";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 		the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{Spectrum.FILE_EXTENSION.replaceFirst("^\\.", "")};
  }

  /**
   * Sets whether to output the sample data as well.
   *
   * @param value	if true then the sample data gets output as well
   */
  public void setOutputSampleData(boolean value) {
    m_OutputSampleData = value;
    reset();
  }

  /**
   * Returns whether to output eh sample data as well.
   *
   * @return		true if the sample data is output as well
   */
  public boolean getOutputSampleData() {
    return m_OutputSampleData;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String outputSampleDataTipText() {
    return "If set to true, the sample data gets stored in the file as well (as comment).";
  }

  /**
   * Sets whether to use compression.
   *
   * @param value	true if to use compression
   */
  public void setUseCompression(boolean value) {
    m_UseCompression = value;
    reset();
  }

  /**
   * Returns whether compression is in use.
   *
   * @return 		true if compression is used
   */
  public boolean getUseCompression() {
    return m_UseCompression;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String useCompressionTipText() {
    return "If enabled, the spectrum is compressed using GZIP and appending '.gz' to the filename.";
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
   * Returns whether writing of multiple containers is supported.
   * 
   * @return 		true if multiple containers are supported
   */
  @Override
  public boolean canWriteMultiple() {
    return true;
  }

  /**
   * Writes its content with the given writer.
   *
   * @param data	the spectra to write
   * @param writer	the writer to use
   * @param report	whether to output the report as well
   * @return		true if successfully written
   */
  protected boolean write(List<Spectrum> data, BufferedWriter writer, boolean report) {
    boolean		result;
    int			i;

    result = true;
    
    try {
      for (i = 0; i < data.size(); i++) {
	// multiple 
	if (i > 0) {
	  writer.write(Spectrum.SEPARATOR);
	  writer.write("\n");
	}
	
	data.get(i).write(writer, report);
      }
    }
    catch (Exception e) {
      result = false;
      getLogger().log(Level.SEVERE, "Failed to write spectra to writer!", e);
    }

    return result;
  }

  /**
   * Writes its content to the given file.
   *
   * @param data	the spectra to write
   * @param filename	the file to write to
   * @param report	whether to output the report as well
   * @return		true if successfully written
   */
  protected boolean write(List<Spectrum> data, String filename, boolean report) {
    boolean		result;
    BufferedWriter	writer;
    FileOutputStream    fos;
    FileWriter		fw;

    filename = new PlaceholderFile(filename).getAbsolutePath();
    writer   = null;
    fw       = null;
    fos      = null;
    try {
      if (filename.endsWith(".gz")) {
	fos    = new FileOutputStream(filename);
	writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(fos)));
      }
      else {
	fw     = new FileWriter(filename);
	writer = new BufferedWriter(fw);
      }
      result = write(data, writer, report);
    }
    catch (Exception e) {
      result = false;
      getLogger().log(Level.SEVERE, "Failed to write spectra to '" + filename + "'!", e);
    }
    finally {
      FileUtils.closeQuietly(writer);
      FileUtils.closeQuietly(fw);
      FileUtils.closeQuietly(fos);
    }

    return result;
  }

  /**
   * Performs the actual writing.
   *
   * @param data	the data to write
   * @return		true if successfully written
   */
  @Override
  protected boolean writeData(List<Spectrum> data) {
    boolean result;

    result = write(data, m_Output.getAbsolutePath(), m_OutputSampleData);
    if (!result)
      getLogger().severe("Error writing data to '" + m_Output.getAbsolutePath() + "'!");

    return result;
  }
}
