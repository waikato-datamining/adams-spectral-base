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
 * SpecLibSpectrumReader.java
 * Copyright (C) 2014-2021 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.io.FileUtils;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.env.Environment;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Reads spectra in USGS SpecLib ASCII format.<br>
 * <br>
 * http:&#47;&#47;speclab.cr.usgs.gov&#47;spectral.lib06&#47;
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
 * <pre>-min-wavenumber &lt;float&gt; (property: minWaveNumber)
 * &nbsp;&nbsp;&nbsp;Wave numbers smaller than this value get ignored.
 * &nbsp;&nbsp;&nbsp;default: 0.0
 * </pre>
 * 
 * <pre>-max-wavenumber &lt;float&gt; (property: maxWaveNumber)
 * &nbsp;&nbsp;&nbsp;Wave numbers larger than this value get ignored.
 * &nbsp;&nbsp;&nbsp;default: 3.4028235E38
 * </pre>
 * 
 * <pre>-min-amplitude &lt;float&gt; (property: minAmplitude)
 * &nbsp;&nbsp;&nbsp;Amplitudes smaller than this value get ignored.
 * &nbsp;&nbsp;&nbsp;default: 0.0
 * </pre>
 * 
 * <pre>-max-amplitude &lt;float&gt; (property: maxAmplitude)
 * &nbsp;&nbsp;&nbsp;Amplitudes larger than this value get ignored.
 * &nbsp;&nbsp;&nbsp;default: 3.4028235E38
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpecLibSpectrumReader
  extends AbstractSpectrumReader {

  /** for serialization. */
  private static final long serialVersionUID = 3095955240781741734L;
  
  /** the minimum wavenumber to allow. */
  protected float m_MinWaveNumber;
  
  /** the maximum wavenumber to allow. */
  protected float m_MaxWaveNumber;
  
  /** the minimum amplitude to allow. */
  protected float m_MinAmplitude;
  
  /** the maximum amplitude to allow. */
  protected float m_MaxAmplitude;
  
  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "Reads spectra in USGS SpecLib ASCII format.\n\n"
	+ "http://speclab.cr.usgs.gov/spectral.lib06/";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "SpecLib ASCII format";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"asc"};
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "min-wavenumber", "minWaveNumber",
	    0.0f);

    m_OptionManager.add(
	    "max-wavenumber", "maxWaveNumber",
	    Float.MAX_VALUE);

    m_OptionManager.add(
	    "min-amplitude", "minAmplitude",
	    0.0f);

    m_OptionManager.add(
	    "max-amplitude", "maxAmplitude",
	    Float.MAX_VALUE);
  }

  /**
   * Sets the smallest allowed wavenumber.
   *
   * @param value	the minimum
   */
  public void setMinWaveNumber(float value) {
    m_MinWaveNumber = value;
    reset();
  }

  /**
   * Returns the smallest allowed wavenumber.
   *
   * @return		the minimum
   */
  public float getMinWaveNumber() {
    return m_MinWaveNumber;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String minWaveNumberTipText() {
    return "Wave numbers smaller than this value get ignored.";
  }

  /**
   * Sets the largest allowed wavenumber.
   *
   * @param value	the maximum
   */
  public void setMaxWaveNumber(float value) {
    m_MaxWaveNumber = value;
    reset();
  }

  /**
   * Returns the largest allowed wavenumber.
   *
   * @return		the maximum
   */
  public float getMaxWaveNumber() {
    return m_MaxWaveNumber;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String maxWaveNumberTipText() {
    return "Wave numbers larger than this value get ignored.";
  }

  /**
   * Sets the smallest allowed amplitude.
   *
   * @param value	the minimum
   */
  public void setMinAmplitude(float value) {
    m_MinAmplitude = value;
    reset();
  }

  /**
   * Returns the smallest allowed amplitude.
   *
   * @return		the minimum
   */
  public float getMinAmplitude() {
    return m_MinAmplitude;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String minAmplitudeTipText() {
    return "Amplitudes smaller than this value get ignored.";
  }

  /**
   * Sets the largest allowed amplitude.
   *
   * @param value	the maximum
   */
  public void setMaxAmplitude(float value) {
    m_MaxAmplitude = value;
    reset();
  }

  /**
   * Returns the largest allowed amplitude.
   *
   * @return		the maximum
   */
  public float getMaxAmplitude() {
    return m_MaxAmplitude;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String maxAmplitudeTipText() {
    return "Amplitudes larger than this value get ignored.";
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    Spectrum		sp;
    SampleData sd;
    List<String>	content;
    String[]		parts;
    boolean		data;
    boolean		title;
    boolean		title2;
    float		wave;
    float		ampl;
    
    content = FileUtils.loadFromFile(m_Input);
    data    = false;
    title   = false;
    title2  = false;
    sp      = new Spectrum();
    sd      = new SampleData();
    sp.setReport(sd);
    m_ReadData.add(sp);
    for (String line: content) {
      if (line.startsWith("-----")) {
	data = true;
	continue;
      }
      if (!data)
	continue;
      
      if (!title) {
	// Example: "Alun_Na+Kaol+Hemat  MV00-11a W1R1Fc AREF"
	parts = line.replaceAll("[ ][ ]*", " ").split(" ");
	sp.setID(parts[1]);
	title = true;
	continue;
      }
      if (!title2) {
	// Example: "copy of splib05a r 7203"
	sd.addField(new Field("Comment", DataType.STRING));
	sd.setStringValue("Comment", line.trim());
	title2 = true;
	continue;
      }
      
      // Example: "       0.430000       0.163323       0.000000"
      parts = line.trim().replaceAll("[ ][ ]*", " ").split(" ");
      if (parts.length == 3) {
	wave = Float.parseFloat(parts[0]);
	ampl = Float.parseFloat(parts[1]);
	if (    (wave >= m_MinWaveNumber) && (wave <= m_MaxWaveNumber) 
	     && (ampl >= m_MinAmplitude) && (ampl <= m_MaxAmplitude) )
	  sp.add(new SpectrumPoint(wave, ampl));
	else if (isLoggingEnabled())
	  getLogger().fine("Ignored amplitude: " + wave + "/" + ampl);
      }
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
    runReader(Environment.class, SpecLibSpectrumReader.class, args);
  }
}
