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
 * TrinamixSpectrumReader.java
 * Copyright (C) 2024 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.io.FileUtils;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.List;

/**
 * Reader for the trinamiX (https://trinamixsensing.com/) CSV format.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class TrinamixSpectrumReader
  extends AbstractSpectrumReader {

  private static final long serialVersionUID = -4783711375310420549L;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reader for the trinamiX (https://trinamixsensing.com/) CSV format.";
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
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    Spectrum		spec;
    List<String> 	lines;
    int			minLines;
    String		sampleID;
    String		timestamp;
    String[]		parts;
    float[]		waveno;
    float[]		absorb;
    int			i;

    // read file
    lines = FileUtils.loadFromFile(m_Input);

    // sufficient lines?
    minLines = 5;
    if (lines.size() < minLines) {
      getLogger().severe("Insufficient number of lines in file (min=" + minLines + ", found=" + lines.size() + "): " + m_Input);
      return;
    }

    // sample ID
    if (lines.get(0).contains("sample id;")) {
      sampleID = lines.get(0).split(";")[1];
    }
    else {
      getLogger().severe("Failed to locate sample ID: " + m_Input);
      return;
    }

    // timestamp
    timestamp = null;
    if (lines.get(1).contains("timestamp;"))
      timestamp = lines.get(1).split(";")[1];

    // waveno
    if (lines.get(3).contains("Wavelength (nm);")) {
      parts = lines.get(3).split(";");
      waveno = new float[parts.length - 1];
      for (i = 1; i < parts.length; i++)
	waveno[i - 1] = Float.parseFloat(parts[i]);
    }
    else {
      getLogger().severe("Failed to locate wavelengths: " + m_Input);
      return;
    }

    // relative absorbance
    if (lines.get(4).contains("Relative absorbance")) {
      parts = lines.get(4).split(";");
      absorb = new float[parts.length - 1];
      for (i = 1; i < parts.length; i++)
	absorb[i - 1] = Float.parseFloat(parts[i]);
    }
    else {
      getLogger().severe("Failed to locate relative absorbance: " + m_Input);
      return;
    }

    // create spectrum
    if (waveno.length == absorb.length) {
      spec = new Spectrum();
      spec.setID(sampleID);
      if (timestamp != null)
	spec.getReport().setStringValue("Timestamp", timestamp);
      for (i = 0; i < waveno.length; i++)
	spec.add(new SpectrumPoint(waveno[i], absorb[i]));
      m_ReadData.add(spec);
    }
    else {
      getLogger().severe("Differing number of values for wave numbers and relative absorbance: " + waveno.length + " != " + absorb.length);
    }
  }
}
