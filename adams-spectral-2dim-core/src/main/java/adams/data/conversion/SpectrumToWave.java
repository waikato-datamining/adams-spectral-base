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
 * SpectrumToWave.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.data.conversion;

import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Turns a spectrum into a WAV.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 <!-- options-end -->
 *
 * Converts a spectrum into a WAV.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class SpectrumToWave
  extends AbstractConversion {

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Turns a spectrum into a WAV.";
  }

  /**
   * Returns the class that is accepted as input.
   *
   * @return		the class
   */
  @Override
  public Class accepts() {
    return Spectrum.class;
  }

  /**
   * Returns the class that is generated as output.
   *
   * @return		the class
   */
  @Override
  public Class generates() {
    return Wave.class;
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  @Override
  protected Object doConvert() throws Exception {
    // Get the input spectrum
    Spectrum input = (Spectrum) m_Input;

    // Get the list of points sorted by wavenumber
    List<SpectrumPoint> points = input.toList();
    points.sort(input.getComparator());

    // Get the normalised amplitudes as a raw array
    float norm = input.getMaxAmplitude().getAmplitude();
    float[] data = new float[points.size()];
    for (int i = 0; i < data.length; i++) {
      data[i] = points.get(i).getAmplitude() / norm;
    }

    // Convert the raw data into WAV data
    byte[] wav_data = new byte[2 * data.length];
    for (int wavenumber = 0; wavenumber < data.length; wavenumber++) {
      short val = (short) (Short.MAX_VALUE * data[wavenumber]);
      for (int i = 0; i < 2; i++) {
        byte b = (byte) (val & 0xff);
        wav_data[wavenumber * 2 + i] = b;
        val = (short) (val >> 8);
      }
    }

    // Create the WAV header for the spectrum data
    WaveHeader waveHeader = new WaveHeader();
    waveHeader.setSampleRate(1); // 44100
    waveHeader.setChunkSize(36 + wav_data.length);
    waveHeader.setSubChunk2Size(wav_data.length);
    waveHeader.setBitsPerSample(16);
    waveHeader.setChannels(1);
    waveHeader.setByteRate(2);

    return new Wave(waveHeader, wav_data);
  }
}