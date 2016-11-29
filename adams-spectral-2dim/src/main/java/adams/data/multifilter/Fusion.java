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

/**
 * Fusion.java
 * Copyright (C) 2015 University of Waikato, Hamilton, New Zealand
 */
package adams.data.multifilter;

import adams.core.Utils;
import adams.core.base.BaseFloat;
import adams.core.base.BaseString;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Fuses the sub-spectra specified by the format option, by placing them side-by-side using the format option as the order.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-no-id-update &lt;boolean&gt; (property: dontUpdateID)
 * &nbsp;&nbsp;&nbsp;If enabled, suppresses updating the ID of adams.data.id.IDHandler data containers.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-format &lt;adams.core.base.BaseString&gt; [-format ...] (property: formats)
 * &nbsp;&nbsp;&nbsp;The formats of the spectra to fuse.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-new-format &lt;java.lang.String&gt; (property: newFormat)
 * &nbsp;&nbsp;&nbsp;The new format to use for the fused spectrum; use '&#64;' to create an automatic 
 * &nbsp;&nbsp;&nbsp;format string generated from all the format strings.
 * &nbsp;&nbsp;&nbsp;default: &#64;
 * </pre>
 * 
 * <pre>-wave-number-start &lt;adams.core.base.BaseFloat&gt; [-wave-number-start ...] (property: waveNumberStarts)
 * &nbsp;&nbsp;&nbsp;The starting wave numbers for the sub-spectra; -1 indicates to use next 
 * &nbsp;&nbsp;&nbsp;available one.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-wave-number-step-size &lt;adams.core.base.BaseFloat&gt; [-wave-number-step-size ...] (property: waveNumberStepSizes)
 * &nbsp;&nbsp;&nbsp;The wave number step sizes for each sub-spectrum.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-new-format-separator &lt;java.lang.String&gt; (property: newFormatSeparator)
 * &nbsp;&nbsp;&nbsp;The separator to use when automatically generating a format string.
 * &nbsp;&nbsp;&nbsp;default: -
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class Fusion
  extends AbstractFormatsBasedMultiSpectrumFilter {

  /** for serialization. */
  private static final long serialVersionUID = 396771111601239664L;

  /** the starting points for the wave numbers in the merged spectrum (use -1 for the next available one). */
  protected BaseFloat[] m_WaveNumberStarts;

  /** the step sizes for the wave numbers. */
  protected BaseFloat[] m_WaveNumberStepSizes;

  /** the separator to use when automatically creating new format string. */
  protected String m_NewFormatSeparator;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return 
	"Fuses the sub-spectra specified by the format option, by placing "
	  + "them side-by-side using the format option as the order.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "wave-number-start", "waveNumberStarts",
      new BaseFloat[0]);

    m_OptionManager.add(
      "wave-number-step-size", "waveNumberStepSizes",
      new BaseFloat[0]);

    m_OptionManager.add(
      "new-format-separator", "newFormatSeparator",
      "-");
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String formatsTipText() {
    return "The formats of the spectra to fuse.";
  }

  /**
   * Returns the default format for the generated spectrum.
   *
   * @return		the default format
   */
  @Override
  protected String getDefaultNewFormat() {
    return "@";
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String newFormatTipText() {
    return "The new format to use for the fused spectrum; use '@' to create an automatic format string generated from all the format strings.";
  }

  /**
   * Sets the starting wave number for each sub-spectrum.
   *
   * @param value 	the starts; use -1 to use next available one
   */
  public void setWaveNumberStarts(BaseFloat[] value) {
    m_WaveNumberStarts = value;
    reset();
  }

  /**
   * Returns the starting wave number for each sub-spectrum.
   *
   * @return 		the starts; -1 if to use next available one
   */
  public BaseFloat[] getWaveNumberStarts() {
    return m_WaveNumberStarts;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String waveNumberStartsTipText() {
    return "The starting wave numbers for the sub-spectra; -1 indicates to use next available one.";
  }

  /**
   * Sets the wave number step sizes for each sub-spectrum.
   *
   * @param value 	the step sizes
   */
  public void setWaveNumberStepSizes(BaseFloat[] value) {
    m_WaveNumberStepSizes = value;
    reset();
  }

  /**
   * Returns the wave number step sizes for each sub-spectrum.
   *
   * @return 		the step sizes
   */
  public BaseFloat[] getWaveNumberStepSizes() {
    return m_WaveNumberStepSizes;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String waveNumberStepSizesTipText() {
    return "The wave number step sizes for each sub-spectrum.";
  }

  /**
   * Sets the separator to use when automatically generating a new format string.
   *
   * @param value 	the new format separator
   */
  public void setNewFormatSeparator(String value) {
    m_NewFormatSeparator = value;
    reset();
  }

  /**
   * Returns the separator to use when automatically generating a new format string.
   *
   * @return 		the new format separator
   */
  public String getNewFormatSeparator() {
    return m_NewFormatSeparator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String newFormatSeparatorTipText() {
    return "The separator to use when automatically generating a format string.";
  }

  /**
   * Makes sure that the wave number starts/step sizes have the same length
   * as the format array.
   *
   * @param data	the data to filter
   */
  @Override
  protected void checkData(MultiSpectrum data) {
    super.checkData(data);

    if (m_Formats.length == 0)
      throw new IllegalStateException("No formats specified to fuse!");

    m_WaveNumberStarts    = (BaseFloat[]) Utils.adjustArray(m_WaveNumberStarts,    m_Formats.length, new BaseFloat("-1.0"));
    m_WaveNumberStepSizes = (BaseFloat[]) Utils.adjustArray(m_WaveNumberStepSizes, m_Formats.length, new BaseFloat("1.0"));
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data, null if failed to generate output
   */
  @Override
  protected Spectrum processData(MultiSpectrum data) {
    Spectrum		result;
    HashSet<String>	formats;
    List<Spectrum>	spectra;
    Spectrum		spec;
    int			i;
    int			n;
    float		wave;
    float		start;
    String		format;

    result = null;

    // get spectra to fuse
    spectra = new ArrayList<Spectrum>();
    formats = new HashSet<String>();
    for (BaseString f: m_Formats) {
      formats.add(f.getValue());
      for (Spectrum sp: data) {
	if (sp.getFormat().equals(f.getValue()))
	  spectra.add(sp);
      }
    }

    // create new header
    result = spectra.get(0).getHeader();
    result.setID(data.getID());
    if (m_NewFormat.equals("@")) {
      format = "";
      for (BaseString f: m_Formats) {
	if (format.length() > 0)
	  format += m_NewFormatSeparator;
	format += f.getValue();
      }
    }
    else {
      format = m_NewFormat;
    }
    result.setFormat(format);

    // all formats present?
    if (formats.size() != m_Formats.length) {
      result.getNotes().addError(getClass(), "Not all formats present: expected=" + Utils.arrayToString(m_Formats) + ", found=" + formats);
      return result;
    }

    // fuse spectra
    wave = 0.0f;
    for (i = 0; i < spectra.size(); i++) {
      spec = spectra.get(i);
      // new starting point
      start = m_WaveNumberStarts[i].floatValue();
      if (start == -1) {
	if (i == 0)
	  wave = 0.0f;
      }
      else {
	wave = start - m_WaveNumberStepSizes[i].floatValue();
      }
      // copy spectral data
      for (n = 0; n < spec.size(); n++) {
	wave += m_WaveNumberStepSizes[i].floatValue();
	result.add(new SpectrumPoint(wave, spec.toList().get(n).getAmplitude()));
      }
    }

    return result;
  }
}
