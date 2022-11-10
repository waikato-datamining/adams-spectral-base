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
 * Spectrogram.java
 * Copyright (C) 2019-2022 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumimage;

import adams.data.conversion.SpectrumToBufferedImage;
import adams.data.spectrum.Spectrum;
import adams.gui.visualization.core.BiColorGenerator;
import adams.gui.visualization.core.ColorGradientGenerator;

import java.awt.image.BufferedImage;

/**
 <!-- globalinfo-start -->
 * Generates a spectrogram image of the spectrum.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-image-type &lt;GRAY|RGB&gt; (property: imageType)
 * &nbsp;&nbsp;&nbsp;The type of image to generate; also determines the resolution: gray=8bit,
 * &nbsp;&nbsp;&nbsp; rgb=24bit.
 * &nbsp;&nbsp;&nbsp;default: RGB
 * </pre>
 *
 * <pre>-min-amplitude &lt;float&gt; (property: minAmplitude)
 * &nbsp;&nbsp;&nbsp;The minimum amplitude to assume; amplitudes below get set to this value.
 * &nbsp;&nbsp;&nbsp;default: 0.0
 * </pre>
 *
 * <pre>-max-amplitude &lt;float&gt; (property: maxAmplitude)
 * &nbsp;&nbsp;&nbsp;The maximum amplitude to assume; amplitudes below get set to this value.
 * &nbsp;&nbsp;&nbsp;default: 1000.0
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
 * Generates a spectrogram image of the spectrum.
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class Spectrogram
  extends AbstractSpectrumImageGeneratorWithRange {

  private static final long serialVersionUID = -8167173639711870289L;

  /** the FFT sample size (power of 2). */
  protected int m_FFTSampleSize;

  /** the overlap factor (1/factor). */
  protected int m_OverlapFactor;

  /** the generator to use. */
  protected ColorGradientGenerator m_Generator;

  /** The conversion that does the actual work. */
  protected transient SpectrumToBufferedImage m_Convertor;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Generates a spectrogram image of the spectrum.";
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
    m_Convertor      = null;
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
   * Returns the FFT sample size (power of 2).
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
   * Hook method for checks before generating the image.
   *
   * @param spectrum	the spectrum to check
   * @return		null if successful, otherwise the error message
   */
  @Override
  protected String check(Spectrum spectrum) {
    // Perform super checks
    String error = super.check(spectrum);
    if (error != null) {
      return error;
    }

    // Perform the conversion and check the result
    ensureConvertor();
    m_Convertor.setInput(spectrum);
    return m_Convertor.convert();
  }

  /**
   * Converts the spectrum into an image.
   *
   * @param spectrum	the spectrum to convert
   * @return		the generated image
   */
  @Override
  protected BufferedImage doGenerate(Spectrum spectrum) {
    // Conversion already performed during check stage
    return (BufferedImage) m_Convertor.getOutput();
  }

  /**
   * Makes sure the convertor is available and configured
   * for use.
   */
  protected void ensureConvertor() {
    // Create the convertor if it's not available
    if (m_Convertor == null) {
      m_Convertor = new SpectrumToBufferedImage();
      m_Convertor.setFFTSampleSize(m_FFTSampleSize);
      m_Convertor.setOverlapFactor(m_OverlapFactor);
      m_Convertor.setGenerator(m_Generator);
    }
  }
}