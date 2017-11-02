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
 * Intensity.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumimage;

import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Generates a simple intensity image.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Intensity
  extends AbstractSpectrumImageGeneratorWithRange {

  private static final long serialVersionUID = -8471257386690272971L;

  /** the image width to generate. */
  protected int m_Width;

  /** the image height to generate. */
  protected int m_Height;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Generates a simple intensity image.\n"
	+ "If no dimensions provided (ie -1 for width and height), a square image is produced.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "width", "width",
      -1, -1, null);

    m_OptionManager.add(
      "height", "height",
      -1, -1, null);
  }

  /**
   * Sets the width of the image.
   *
   * @param value	the width, -1 for auto-calculation
   */
  public void setWidth(int value) {
    if (getOptionManager().isValid("width", value)) {
      m_Width = value;
      reset();
    }
  }

  /**
   * Returns the width of the image.
   *
   * @return		the width, -1 if auto-calculation
   */
  public int getWidth() {
    return m_Width;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String widthTipText() {
    return "The width of the image to generate; use -1 for automatic calculation.";
  }

  /**
   * Sets the height of the image.
   *
   * @param value	the height, -1 for auto-calculation
   */
  public void setHeight(int value) {
    if (getOptionManager().isValid("height", value)) {
      m_Height = value;
      reset();
    }
  }

  /**
   * Returns the height of the image.
   *
   * @return		the height, -1 if auto-calculation
   */
  public int getHeight() {
    return m_Height;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String heightTipText() {
    return "The height of the image to generate; use -1 for automatic calculation.";
  }

  /**
   * Converts the spectrum into an image.
   *
   * @param spectrum	the spectrum to convert
   * @return		the generated image
   */
  @Override
  protected BufferedImage doGenerate(Spectrum spectrum) {
    BufferedImage 	result;
    int			width;
    int			height;
    int			i;
    int			x;
    int			y;
    double		ampl;
    double		range;
    double resolution;
    int			val;
    List<SpectrumPoint> points;

    points = spectrum.toList();

    // dimensions
    if ((m_Width < 1) && (m_Height < 1)) {
      width  = (int) Math.ceil(Math.sqrt(spectrum.size()));
      height = width;
    }
    else if (m_Width < 1) {
      width  = (int) Math.ceil(spectrum.size() / m_Height);
      height = m_Height;
    }
    else if (m_Height < 1) {
      width  = m_Width;
      height = (int) Math.ceil(spectrum.size() / m_Width);
    }
    else {
      width  = m_Width;
      height = m_Height;
    }
    result = newImage(width, height);

    // determine range/resolution
    range = m_MaxAmplitude - m_MinAmplitude;
    switch (m_ImageType) {
      case GRAY:
	resolution = (range / Math.pow(2, 8));  // 8bit color space
        break;
      case RGB:
	resolution = (range / Math.pow(2, 24));  // 24bit color space
        break;
      default:
        throw new IllegalStateException("Unhandled image type: " + m_ImageType);
    }

    i = 0;
    for (y = 0; y < height; y++) {
      for (x = 0; x < width; x++) {
        ampl = 0.0;
        if (i < points.size()) {
	  ampl = points.get(i).getAmplitude();
	  ampl = Math.max(ampl, m_MinAmplitude);
	  ampl = Math.min(ampl, m_MaxAmplitude);
	  ampl = (ampl - m_MinAmplitude) / range;
	}
	val = (int) (ampl / resolution);
        setPixel(result, x, y, val);
        i++;
      }
    }

    return result;
  }
}
