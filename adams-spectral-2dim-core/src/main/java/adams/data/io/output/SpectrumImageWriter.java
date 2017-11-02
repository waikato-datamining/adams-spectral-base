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
 * SpectrumImageWriter.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.data.io.output;

import adams.data.image.BufferedImageHelper;
import adams.data.spectrum.Spectrum;
import adams.data.spectrumimage.AbstractSpectrumImageGenerator;
import adams.data.spectrumimage.Intensity;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Writes spectra as images using the supplied generator.
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
 * <pre>-generator &lt;adams.data.spectrumimage.AbstractSpectrumImageGenerator&gt; (property: generator)
 * &nbsp;&nbsp;&nbsp;The generator to use.
 * &nbsp;&nbsp;&nbsp;default: adams.data.spectrumimage.Intensity
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumImageWriter
  extends AbstractSpectrumWriter {

  private static final long serialVersionUID = 8367606282424805076L;

  /** the generator to use. */
  protected AbstractSpectrumImageGenerator m_Generator;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Writes spectra as images using the supplied generator.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "generator", "generator",
      new Intensity());
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
   * Sets the generator to use.
   *
   * @param value	the generator
   */
  public void setGenerator(AbstractSpectrumImageGenerator value) {
    m_Generator = value;
    reset();
  }

  /**
   * Returns the generator to use.
   *
   * @return		the generator
   */
  public AbstractSpectrumImageGenerator getGenerator() {
    return m_Generator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String generatorTipText() {
    return "The generator to use.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Spectrum image";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"png"};
  }

  /**
   * Performs the actual writing.
   *
   * @param data	the data to write
   * @return		true if successfully written
   */
  @Override
  protected boolean writeData(List<Spectrum> data) {
    BufferedImage	img;
    String		msg;

    if (data.size() == 0)
      return false;

    try {
      img = m_Generator.generate(data.get(0));
      msg = BufferedImageHelper.write(img, m_Output);
      if (msg != null)
        getLogger().severe("Failed to write image to " + m_Output + ": " + msg);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to generated image!", e);
      return false;
    }

    return (msg == null);
  }
}
