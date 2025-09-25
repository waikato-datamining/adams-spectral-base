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
 * DPTSpectrumWriter.java
 * Copyright (C) 2009-2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.output;

import adams.core.Utils;
import adams.core.management.LocaleHelper;
import adams.core.management.LocaleSupporter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spectrum.SpectrumPointComparator;

import java.io.Writer;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Writer that stores spectra in the simple CSV format.
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
 * <pre>-locale &lt;java.util.Locale&gt; (property: locale)
 * &nbsp;&nbsp;&nbsp;The locale to use for writing the numbers.
 * &nbsp;&nbsp;&nbsp;default: en_us
 * </pre>
 * 
 * <pre>-descending &lt;boolean&gt; (property: descending)
 * &nbsp;&nbsp;&nbsp;If set to true, the spectrum is output in descending x-axis order.
 * &nbsp;&nbsp;&nbsp;default: true
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class DPTSpectrumWriter
  extends AbstractTextBasedSpectrumWriter
  implements LocaleSupporter {

  /** for serialization. */
  private static final long serialVersionUID = 5913174460217317839L;

  /** the locale to use. */
  protected Locale m_Locale;

  /** whether to output the sample data as well. */
  protected boolean m_Descending;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Writer that stores spectra in the simple CSV format.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "locale", "locale",
      LocaleHelper.getSingleton().getEnUS());

    m_OptionManager.add(
      "descending", "descending",
      true);
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
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Simple DPT format";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 		the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"dpt"};
  }

  /**
   * Sets the locale to use.
   *
   * @param value	the locale
   */
  public void setLocale(Locale value) {
    m_Locale = value;
    reset();
  }

  /**
   * Returns the locale in use.
   *
   * @return 		the locale
   */
  public Locale getLocale() {
    return m_Locale;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String localeTipText() {
    return "The locale to use for writing the numbers.";
  }

  /**
   * Sets whether to output spectrum points by descending x-axis.
   *
   * @param value	if true then the output descending x-axis
   */
  public void setDescending(boolean value) {
    m_Descending = value;
    reset();
  }

  /**
   * Returns whether to output spectrum points by descending x-axis.
   *
   * @return		true if output descending x-axis
   */
  public boolean getDescending() {
    return m_Descending;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String descendingTipText() {
    return "If set to true, the spectrum is output in descending x-axis order.";
  }

  /**
   * Writer can only write single chromatograms.
   *
   * @param data	the data to write
   */
  @Override
  protected void checkData(List<Spectrum> data) {
    super.checkData(data);

    if (data.size() != 1)
      throw new IllegalArgumentException(
      "Writer can only write exactly 1 spectrum at a time!");
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
    try {
      Spectrum spec = data.get(0);
      List<SpectrumPoint> points = spec.toList(new SpectrumPointComparator(false, !getDescending()));
      int count = 0;
      for (SpectrumPoint sp:points) {
	count++;
	writer.write(Utils.doubleToString(sp.getWaveNumber(), 8, m_Locale));
	writer.write("\t");
	writer.write(Utils.doubleToString(sp.getAmplitude(), 8, m_Locale));
	writer.write("\n");
	if (count % 100 == 0)
	  writer.flush();
      }
      writer.flush();
      writer.close();
      return true;
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to write spectra with writer!", e);
      return false;
    }
  }
}
