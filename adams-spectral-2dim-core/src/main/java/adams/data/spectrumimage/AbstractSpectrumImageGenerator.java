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
 * AbstractSpectrumImageGenerator.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumimage;

import adams.core.option.AbstractOptionHandler;
import adams.data.spectrum.Spectrum;

import java.awt.image.BufferedImage;

/**
 * Ancestor for image generators from spectra.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractSpectrumImageGenerator
  extends AbstractOptionHandler {

  private static final long serialVersionUID = 4701137460302246501L;

  /** the image type to generate. */
  protected ImageType m_ImageType;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "image-type", "imageType",
      ImageType.RGB);
  }

  /**
   * Sets the type of image to generate.
   *
   * @param value	the type
   */
  public void setImageType(ImageType value) {
    m_ImageType = value;
    reset();
  }

  /**
   * Returns the type of image to generate.
   *
   * @return		the type
   */
  public ImageType getImageType() {
    return m_ImageType;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String imageTypeTipText() {
    return "The type of image to generate; also determines the resolution: gray=8bit, rgb=24bit.";
  }

  /**
   * Hook method for checks before generating the image.
   *
   * @param spectrum	the spectrum to check
   * @return		null if successful, otherwise the error message
   */
  protected String check(Spectrum spectrum) {
    if (spectrum == null)
      return "No spectrum provided!";
    if (spectrum.size() == 0)
      return "Spectrum contains no data points!";
    return null;
  }

  /**
   * Creates a new image.
   *
   * @param width	the width of the image
   * @param height	the height of the image
   * @return		the image
   */
  protected BufferedImage newImage(int width, int height) {
    switch (m_ImageType) {
      case GRAY:
	return new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
      case RGB:
	return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      default:
        throw new IllegalStateException("Unhandled image type: " + m_ImageType);
    }
  }

  /**
   * Sets the pixel in the image.
   *
   * @param img		the image to update
   * @param x		the x of the pixel
   * @param y		the y of the pixel
   * @param val		the value to set
   */
  protected void setPixel(BufferedImage img, int x, int y, int val) {
    switch (m_ImageType) {
      case GRAY:
	img.setRGB(x, y, val << 16 + val << 8 + val);
	break;
      case RGB:
	img.setRGB(x, y, val);
	break;
      default:
	throw new IllegalStateException("Unhandled image type: " + m_ImageType);
    }
  }

  /**
   * Converts the spectrum into an image.
   *
   * @param spectrum	the spectrum to convert
   * @return		the generated image
   */
  protected abstract BufferedImage doGenerate(Spectrum spectrum);

  /**
   * Converts the spectrum into an image.
   *
   * @param spectrum	the spectrum to convert
   * @return		the generated image
   */
  public BufferedImage generate(Spectrum spectrum) {
    String	msg;

    msg = check(spectrum);
    if (msg != null)
      throw new IllegalStateException(msg);

    return doGenerate(spectrum);
  }
}
