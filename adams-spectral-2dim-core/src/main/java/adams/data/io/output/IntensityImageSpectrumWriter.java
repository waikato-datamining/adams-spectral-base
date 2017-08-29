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
 * IntensityImageSpectrumWriter.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.data.io.output;

import adams.data.image.BufferedImageHelper;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Writes spectra as images using the amplitude as intensity for determining the color.<br>
 * If no dimensions provided (ie -1 for width and height), a square image is produced.
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
 * <pre>-width &lt;int&gt; (property: width)
 * &nbsp;&nbsp;&nbsp;The width of the image to generate; use -1 for automatic calculation.
 * &nbsp;&nbsp;&nbsp;default: -1
 * &nbsp;&nbsp;&nbsp;minimum: -1
 * </pre>
 *
 * <pre>-height &lt;int&gt; (property: height)
 * &nbsp;&nbsp;&nbsp;The height of the image to generate; use -1 for automatic calculation.
 * &nbsp;&nbsp;&nbsp;default: -1
 * &nbsp;&nbsp;&nbsp;minimum: -1
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class IntensityImageSpectrumWriter
  extends AbstractSpectrumWriter {

  private static final long serialVersionUID = 8367606282424805076L;

  /**
   * Enumeration of type of image to generate.
   */
  public enum ImageType {
    GRAY,
    RGB
  }

  /** the image type to generate. */
  protected ImageType m_ImageType;

  /** the minimum amplitude to use. */
  protected float m_MinAmplitude;

  /** the maximum amplitude to use. */
  protected float m_MaxAmplitude;

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
      "Writes spectra as images using the amplitude as intensity for determining the color.\n"
      + "If no dimensions provided (ie -1 for width and height), a square image is produced.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "image-type", "imageType",
      ImageType.RGB);

    m_OptionManager.add(
      "min-amplitude", "minAmplitude",
      0.0f);

    m_OptionManager.add(
      "max-amplitude", "maxAmplitude",
      1000.0f);

    m_OptionManager.add(
      "width", "width",
      -1, -1, null);

    m_OptionManager.add(
      "height", "height",
      -1, -1, null);
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
   * Sets the minimum amplitude to assume.
   *
   * @param value	the minimum
   */
  public void setMinAmplitude(float value) {
    if (getOptionManager().isValid("minAmplitude", value)) {
      m_MinAmplitude = value;
      reset();
    }
  }

  /**
   * Returns the minimum amplitude to assume.
   *
   * @return		the minimum
   */
  public float getMinAmplitude() {
    return m_MinAmplitude;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String minAmplitudeTipText() {
    return "The minimum amplitude to assume; amplitudes below get set to this value.";
  }

  /**
   * Sets the maximum amplitude to assume.
   *
   * @param value	the maximum
   */
  public void setMaxAmplitude(float value) {
    if (getOptionManager().isValid("maxAmplitude", value)) {
      m_MaxAmplitude = value;
      reset();
    }
  }

  /**
   * Returns the maximum amplitude to assume.
   *
   * @return		the maximum
   */
  public float getMaxAmplitude() {
    return m_MaxAmplitude;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String maxAmplitudeTipText() {
    return "The maximum amplitude to assume; amplitudes below get set to this value.";
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
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Intensity image";
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
    Spectrum		sp;
    int			width;
    int			height;
    BufferedImage	img;
    int			i;
    int			x;
    int			y;
    double		ampl;
    double		range;
    double		inc;
    int			val;
    List<SpectrumPoint>	points;
    String		msg;

    if (data.size() == 0)
      return false;

    if (m_MaxAmplitude <= m_MinAmplitude)
      throw new IllegalStateException(
        "max amplitude must be greater than min amplitude: min=" + m_MinAmplitude + ", max=" + m_MaxAmplitude);

    sp     = data.get(0);
    points = sp.toList();

    // dimensions
    if ((m_Width < 1) && (m_Height < 1)) {
      width  = (int) Math.ceil(Math.sqrt(sp.size()));
      height = width;
    }
    else if (m_Width < 1) {
      width  = (int) Math.ceil(sp.size() / m_Height);
      height = m_Height;
    }
    else if (m_Height < 1) {
      width  = m_Width;
      height = (int) Math.ceil(sp.size() / m_Width);
    }
    else {
      width  = m_Width;
      height = m_Height;
    }

    // create image
    range = m_MaxAmplitude - m_MinAmplitude;
    switch (m_ImageType) {
      case GRAY:
	img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
	inc = (range / Math.pow(2, 8));  // 8bit color space
        break;
      case RGB:
	img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	inc = (range / Math.pow(2, 24));  // 24bit color space
        break;
      default:
        throw new IllegalStateException("Unhandled image type: " + m_ImageType);
    }
    i     = 0;
    for (y = 0; y < height; y++) {
      for (x = 0; x < width; x++) {
        ampl = 0.0;
        if (i < points.size()) {
	  ampl = points.get(i).getAmplitude();
	  ampl = Math.max(ampl, m_MinAmplitude);
	  ampl = Math.min(ampl, m_MaxAmplitude);
	  ampl = (ampl - m_MinAmplitude) / range;
	}
	val = (int) (ampl / inc);
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
        i++;
      }
    }

    // save image
    msg = BufferedImageHelper.write(img, m_Output);
    if (msg != null)
      getLogger().severe("Failed to write image to " + m_Output + ": " + msg);

    return (msg == null);
  }
}
