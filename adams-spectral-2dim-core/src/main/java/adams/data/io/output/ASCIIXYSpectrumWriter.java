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
 * ASCIIXYSpectrumWriter.java
 * Copyright (C) 2009-2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.output;

import adams.data.io.input.ASCIIXYSpectrumReader;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Writer that stores spectra in ASCII XY format.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-output &lt;adams.core.io.PlaceholderFile&gt; (property: output)
 * &nbsp;&nbsp;&nbsp;The file to write the container to.
 * &nbsp;&nbsp;&nbsp;default: ${TMP}&#47;out.tmp
 * </pre>
 * 
 * <pre>-separator &lt;java.lang.String&gt; (property: separator)
 * &nbsp;&nbsp;&nbsp;The separator to use between X and Y column.
 * &nbsp;&nbsp;&nbsp;default: ;
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class ASCIIXYSpectrumWriter
  extends AbstractTextBasedSpectrumWriter {

  /** for serialization. */
  private static final long serialVersionUID = 5290679698357490093L;
  
  /** the separator to use. */
  protected String m_Separator;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Writer that stores spectra in ASCII XY format.";
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
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the file chooser
   */
  @Override
  public String getFormatDescription() {
    return new ASCIIXYSpectrumReader().getFormatDescription();
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 		the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new ASCIIXYSpectrumReader().getFormatExtensions();
  }

  /**
   * Sets the separator to use between X and Y columns.
   *
   * @param value	the separator
   */
  public void setSeparator(String value) {
    m_Separator = value;
    reset();
  }

  /**
   * Returns the separator to use between X and Y columns.
   *
   * @return		the separator
   */
  public String getSeparator() {
    return m_Separator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String separatorTipText() {
    return "The separator to use between X and Y column.";
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
   * Performs the actual writing.
   *
   * @param data	the data to write
   * @param writer 	the writer to write the spectra to
   * @return		true if successfully written
   */
  @Override
  protected boolean writeData(List<Spectrum> data, Writer writer) {
    Spectrum		sp;
    List<String>	lines;

    sp    = data.get(0);
    lines = new ArrayList<>();
    for (SpectrumPoint point: sp)
      lines.add(point.getWaveNumber() + m_Separator + point.getAmplitude());
    Collections.reverse(lines);
    try {
      for (String line : lines) {
	writer.write(line);
	writer.write("\n");
      }
      return true;
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to write spectrum with writer!", e);
      return false;
    }
  }
}
