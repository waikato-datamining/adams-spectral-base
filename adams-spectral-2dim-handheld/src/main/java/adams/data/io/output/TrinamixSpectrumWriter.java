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
 * TrinamixSpectrumWriter.java
 * Copyright (C) 2024 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.output;

import adams.core.DateUtils;
import adams.core.io.FileUtils;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Writer for the trinamiX (https://trinamixsensing.com/) CSV format.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class TrinamixSpectrumWriter 
  extends AbstractSpectrumWriter {

  private static final long serialVersionUID = -1056141700225189830L;

  /** The instrument name to use. */
  protected String m_InstrumentName;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Writer for the trinamiX (https://trinamixsensing.com/) CSV format.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "instrumentName", "instrumentName",
      "");
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
   * Get instrument name.
   *
   * @return instrument name
   */
  public String getInstrumentName() {
    return m_InstrumentName;
  }

  /**
   * Set instrument name.
   *
   * @param mInstrumentName	instrument name
   */
  public void setInstrumentName(String mInstrumentName) {
    m_InstrumentName = mInstrumentName;
    reset();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String instrumentNameTipText() {
    return "The instrument name to be used; if empty, uses 'Instrument' value in sample data when available.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the
   * file chooser
   */
  @Override
  public String getFormatDescription() {
    return "trinamiX CSV";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"csv"};
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
   * Performs the actual writing.
   *
   * @param data the data to write
   * @return true if successfully written
   */
  @Override
  protected boolean writeData(List<Spectrum> data) {
    List<String>	lines;
    String		msg;
    String		timestamp;
    StringBuilder	buf;
    String		instrument;
    String		sampleID;

    if (data.isEmpty()) {
      getLogger().severe("No spectra to write to: " + m_Output);
      return false;
    }

    lines = new ArrayList<>();

    // sample id
    sampleID = data.get(0).getID();
    lines.add("sample id;" + sampleID);

    // timestamp
    timestamp = DateUtils.getTimestampFormatter().format(new Date());
    if (data.get(0).getReport().hasValue("Timestamp"))
      timestamp = data.get(0).getReport().getStringValue("Timestamp");
    else if (data.get(0).getReport().hasValue(SampleData.INSERT_TIMESTAMP))
      timestamp = data.get(0).getReport().getStringValue(SampleData.INSERT_TIMESTAMP);
    lines.add("timestamp;" + timestamp);

    // separator
    lines.add("");

    for (Spectrum sp: data) {
      // ensure that sample ID is the same
      if (!sp.getID().equals(sampleID)) {
	getLogger().warning("Skipping "  + sp.getID() + " as the sample ID differs from first one: " + sampleID);
	continue;
      }

      // instrument
      instrument = m_InstrumentName;
      if (instrument.isEmpty() && sp.getReport().hasValue("Instrument"))
	instrument = sp.getReport().getStringValue("Instrument");

      // wavenumbers
      buf = new StringBuilder(instrument).append(" - Wavelength (nm)");
      for (SpectrumPoint p : sp.toList())
	buf.append(";").append(p.getWaveNumber());
      lines.add(buf.toString());

      // amplitudes
      buf = new StringBuilder(instrument).append(" - Relative absorbance - #0");
      for (SpectrumPoint p : sp.toList())
	buf.append(";").append(p.getAmplitude());
      lines.add(buf.toString());
    }

    msg = FileUtils.saveToFileMsg(lines, m_Output, null);
    if (msg != null)
      getLogger().severe("Failed to write spectra to " + m_Output + ": " + msg);

    return (msg == null);
  }
}
