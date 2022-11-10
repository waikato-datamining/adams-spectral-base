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
 * SpectrumToBufferedImage.java
 * Copyright (C) 2019-2022 University of Waikato, Hamilton, NZ
 */

package adams.data.conversion;

import adams.data.spectrum.Spectrum;
import adams.gui.visualization.core.BiColorGenerator;
import adams.gui.visualization.core.ColorGradientGenerator;
import com.musicg.wave.Wave;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 <!-- globalinfo-start -->
 * Converts a spectrum into an image by treating it like an audio signal, and producing a spectrogram for it.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-fft-sample-size &lt;int&gt; (property: FFTSampleSize)
 * &nbsp;&nbsp;&nbsp;The sample size for the fast fourier transformation; must be a power of
 * &nbsp;&nbsp;&nbsp;2.
 * &nbsp;&nbsp;&nbsp;default: 1024
 * &nbsp;&nbsp;&nbsp;minimum: 2
 * </pre>
 *
 * <pre>-overlap-factor &lt;int&gt; (property: overlapFactor)
 * &nbsp;&nbsp;&nbsp;The overlap factor (1&#47;factor), eg 4 = 1&#47;4 = 25%; 0 = no overlap.
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 *
 * <pre>-generator &lt;adams.gui.visualization.core.ColorGradientGenerator&gt; (property: generator)
 * &nbsp;&nbsp;&nbsp;The generator to use for creating the gradient colors.
 * &nbsp;&nbsp;&nbsp;default: adams.gui.visualization.core.BiColorGenerator
 * </pre>
 *
 <!-- options-end -->
 *
 * Converts a spectrum into an image by treating it like an audio signal,
 * and producing a spectrogram for it.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class SpectrumToBufferedImage
  extends AbstractConversion {

  private static final long serialVersionUID = -8920538282070058213L;

  /** the FFT sample size (power of 2). */
  protected int m_FFTSampleSize;

  /** the overlap factor (1/factor). */
  protected int m_OverlapFactor;

  /** the generator to use. */
  protected ColorGradientGenerator m_Generator;

  /** the gradient colors. */
  protected transient Color[] m_GradientColors;

  /** the lookup table. */
  protected transient TIntIntMap m_ColorLookup;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Converts a spectrum into an image by treating it like an audio " +
      "signal, and producing a spectrogram for it.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "fft-sample-size", "FFTSampleSize",
      1024, 2, null);

    m_OptionManager.add(
      "overlap-factor", "overlapFactor",
      0, 0, null);

    m_OptionManager.add(
      "generator", "generator",
      new BiColorGenerator());
  }

  /**
   * Resets the object.
   */
  @Override
  protected void reset() {
    super.reset();

    m_GradientColors = null;
    m_ColorLookup    = null;
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
    return BufferedImage.class;
  }

  /**
   * Sets the FFT sample size (power of 2).
   *
   * @param value	the sample size
   */
  public void setFFTSampleSize(int value) {
    if (getOptionManager().isValid("FFTSampleSize", value) && (Integer.bitCount(value) == 1)) {
      m_FFTSampleSize = value;
      reset();
    }
  }

  /**
   * Returns the FFT samepl size (power of 2).
   *
   * @return		the sample size
   */
  public int getFFTSampleSize() {
    return m_FFTSampleSize;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String FFTSampleSizeTipText() {
    return "The sample size for the fast fourier transformation; must be a power of 2.";
  }

  /**
   * Sets the overlap factor (1/factor).
   *
   * @param value	the factor
   */
  public void setOverlapFactor(int value) {
    if (getOptionManager().isValid("overlapFactor", value)) {
      m_OverlapFactor = value;
      reset();
    }
  }

  /**
   * Returns the overlap factor (1/factor).
   *
   * @return		the factor
   */
  public int getOverlapFactor() {
    return m_OverlapFactor;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String overlapFactorTipText() {
    return "The overlap factor (1/factor), eg 4 = 1/4 = 25%; 0 = no overlap.";
  }

  /**
   * Sets the color generator.
   *
   * @param value	the generator
   */
  public void setGenerator(ColorGradientGenerator value) {
    m_Generator = value;
    reset();
  }

  /**
   * Returns the color generator.
   *
   * @return		the generator
   */
  public ColorGradientGenerator getGenerator() {
    return m_Generator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String generatorTipText() {
    return "The generator to use for creating the gradient colors.";
  }

  /**
   * Generates the gradient colors.
   *
   * @return		the colors
   */
  protected Color[] getGradientColors() {
    if (m_GradientColors == null)
      m_GradientColors = m_Generator.generate();

    return m_GradientColors;
  }

  /**
   * Generates the color lookup.
   *
   * @return		the colors
   */
  protected TIntIntMap getColorLookup() {
    Color[]	colors;
    int		i;

    if (m_ColorLookup == null) {
      m_ColorLookup = new TIntIntHashMap();
      colors        = getGradientColors();
      for (i = 0; i < colors.length; i++)
        m_ColorLookup.put(i, colors[i].getRGB());
    }

    return m_ColorLookup;
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  @Override
  protected Object doConvert() throws Exception {
    // Defer to SpectrumToWave to get the WAV representation
    // of the spectrum
    SpectrumToWave spectrumToWave = new SpectrumToWave();
    spectrumToWave.setInput((Spectrum) m_Input);
    Wave wave = (Wave) spectrumToWave.doConvert();

    // Convert the WAV to a buffered image
    return waveToBufferedImage(wave);
  }

  /**
   * Converts a WAV to an image of its spectrogram.
   *
   * @param wave    The WAV to create an image for.
   * @return        The image of the WAV's spectrogram.
   */
  private BufferedImage waveToBufferedImage(Wave wave) {
    // Turn the wave into a spectrogram
    com.musicg.wave.extension.Spectrogram spectrogram =
      new com.musicg.wave.extension.Spectrogram(wave, m_FFTSampleSize, m_OverlapFactor);

    // Get the spectrogram data
    double[][] data = spectrogram.getNormalizedSpectrogramData();

    // Get the colour lookup
    TIntIntMap colors = getColorLookup();

    // Create the image
    BufferedImage img = new BufferedImage(data.length, data[0].length, BufferedImage.TYPE_INT_RGB);
    for (int i = 0; i < data.length; i++) {
      for (int n = 0; n < data[i].length; n++) {
        int colorIndex = (int) (data[i][n] * (colors.size() - 1));
        img.setRGB(i, img.getHeight() - n - 1, colors.get(colorIndex));
      }
    }

    return img;
  }
}