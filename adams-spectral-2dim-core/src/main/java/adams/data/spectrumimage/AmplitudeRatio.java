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
 * Generates a square image (width is number of wave numbers) with the
 * pixels being the ratio for each possible amplitude pair.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class AmplitudeRatio
  extends AbstractSpectrumImageGeneratorWithRange {

  private static final long serialVersionUID = -8471257386690272971L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Generates a square image (width is number of wave numbers) with the "
        + "pixels being the ratio for each possible amplitude pair.";
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
    int			x;
    int			y;
    double 		amply;
    double 		amplx;
    double		min;
    double		max;
    double		range;
    double 		resolution;
    double		ratio;
    int			val;
    List<SpectrumPoint> points;

    points = spectrum.toList();

    // dimensions
    width  = spectrum.size();
    height = spectrum.size();
    if (isLoggingEnabled())
      getLogger().info("width=" + width + ", height=" + height);
    result = newImage(width, height);

    // determine range/resolution
    min = Float.MAX_VALUE;
    max = Float.MIN_VALUE;
    for (y = 0; y < height; y++) {
      amply = rangeCheck(points.get(y).getAmplitude());
      for (x = 0; x < width; x++) {
        amplx = rangeCheck(points.get(x).getAmplitude());
	ratio = amply / amplx;
	min = Math.min(min, ratio);
	max = Math.max(max, ratio);
      }
    }
    range = (max - min);
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
    if (isLoggingEnabled())
      getLogger().info("min=" + min + ", max=" + max + ", range=" + range + ", resolution=" + resolution);

    // generate image
    for (y = 0; y < height; y++) {
      amply = rangeCheck(points.get(y).getAmplitude());
      for (x = 0; x < width; x++) {
        amplx = rangeCheck(points.get(x).getAmplitude());
	val   = (int) (((amply / amplx) - min) / resolution);
        setPixel(result, x, y, val);
      }
    }

    return result;
  }
}
