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
 * DPTSpectrumReader.java
 * Copyright (C) 2009-2015 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.io.FileUtils;
import adams.core.management.LocaleHelper;
import adams.core.management.LocaleSupporter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Reads spectra in DPT format.
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
 * <pre>-locale &lt;java.util.Locale&gt; (property: locale)
 * &nbsp;&nbsp;&nbsp;The locale to use for parsing the numbers.
 * &nbsp;&nbsp;&nbsp;default: en_us
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 7 $
 */
public class DPTSpectrumReader
  extends AbstractSpectrumReader
  implements LocaleSupporter {

  /** for serialization. */
  private static final long serialVersionUID = -6042011573847847479L;

  /** the locale to use. */
  protected Locale m_Locale;

  /**
   * Class for parsing a DPT file.
   */
  protected class ParsedFile {

    /** vector of 2d double (double[2]) wavenumber,absorbance. */
    protected List<float[]> m_dp = new ArrayList<float[]>();

    /** the last error that occurred. */
    protected String m_LastError = "";

    /**
     * Returns the last error that occurred.
     *
     * @return		the last error
     */
    public String getLastError() {
      return m_LastError;
    }

    public List<float[]> getPoints() {
      return m_dp;
    }

    /**
     * Parses the given lines.
     *
     * @param lines	the lines to parse
     * @return		true if successfully parsed
     */
    public boolean parse(List<String> lines) {
      NumberFormat nf = LocaleHelper.getSingleton().getNumberFormat(getLocale());
      nf.setMaximumFractionDigits(4);
      for (String line: lines) {
	String vals[] = line.trim().split("\\s");
	if (vals.length != 2) {
	  m_LastError = "Data line corrupt:" + line + " split into:" + vals.length;
	  for (int j = 0; j < vals.length; j++) {
	    m_LastError += " (" + vals[j] + ")";
	  }
	  return false;
	}
	float[] f = new float[2];
	try {
	  Number n = nf.parse(vals[0]);
	  f[0] = n.floatValue();
	  n = nf.parse(vals[1]);
	  f[1] = n.floatValue();
	}
	catch(Exception e) {
	  m_LastError = "Data line corrupt: " + line;
	  getLogger().log(Level.SEVERE, m_LastError, e);
	  return false;
	}
	m_dp.add(f);

      }
      return true;
    }
  }

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads spectra in DPT format.";
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
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "DPT Format";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension(s) (without the dot!)
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
    return "The locale to use for parsing the numbers.";
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    Spectrum	sp;
    ParsedFile 	pf;

    sp = new Spectrum();
    pf = new ParsedFile();
    pf.parse(FileUtils.loadFromFile(m_Input));
    for (float[] d: pf.getPoints())
      sp.add(new SpectrumPoint(d[0], d[1]));
    sp.setID(FileUtils.replaceExtension(m_Input.getName(), ""));

    m_ReadData.add(sp);
  }
}
