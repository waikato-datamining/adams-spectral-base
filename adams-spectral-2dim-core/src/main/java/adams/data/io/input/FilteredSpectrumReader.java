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
 * FilteredSpectrumReader.java
 * Copyright (C) 2023 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.data.filter.Filter;
import adams.data.filter.PassThrough;
import adams.data.spectrum.Spectrum;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Uses the specified reader to read the spectral data before applying the supplied filter to it.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * &nbsp;&nbsp;&nbsp;min-user-mode: Expert
 * </pre>
 *
 * <pre>-input &lt;adams.core.io.PlaceholderFile&gt; (property: input)
 * &nbsp;&nbsp;&nbsp;The file to read and turn into a container.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 *
 * <pre>-create-dummy-report &lt;boolean&gt; (property: createDummyReport)
 * &nbsp;&nbsp;&nbsp;If true, then a dummy report is created if none present.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-instrument &lt;java.lang.String&gt; (property: instrument)
 * &nbsp;&nbsp;&nbsp;The name of the instrument that generated the spectra (if not already present
 * &nbsp;&nbsp;&nbsp;in data).
 * &nbsp;&nbsp;&nbsp;default: unknown
 * </pre>
 *
 * <pre>-format &lt;java.lang.String&gt; (property: format)
 * &nbsp;&nbsp;&nbsp;The data format string.
 * &nbsp;&nbsp;&nbsp;default: NIR
 * </pre>
 *
 * <pre>-keep-format &lt;boolean&gt; (property: keepFormat)
 * &nbsp;&nbsp;&nbsp;If enabled the format obtained from the file is not replaced by the format
 * &nbsp;&nbsp;&nbsp;defined here.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-use-absolute-source &lt;boolean&gt; (property: useAbsoluteSource)
 * &nbsp;&nbsp;&nbsp;If enabled the source report field stores the absolute file name rather
 * &nbsp;&nbsp;&nbsp;than just the name.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-reader &lt;adams.data.io.input.AbstractSpectrumReader&gt; (property: reader)
 * &nbsp;&nbsp;&nbsp;The reader to use for reading the data before applying the filter.
 * &nbsp;&nbsp;&nbsp;default: adams.data.io.input.SimpleSpectrumReader
 * </pre>
 *
 * <pre>-filter &lt;adams.data.filter.Filter&gt; (property: filter)
 * &nbsp;&nbsp;&nbsp;The filter to apply to the data that was read.
 * &nbsp;&nbsp;&nbsp;default: adams.data.filter.PassThrough
 * </pre>
 *
 <!-- options-end -->
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class FilteredSpectrumReader
  extends AbstractSpectrumReader {

  private static final long serialVersionUID = -4969515852816781181L;

  /** the underlying reader. */
  protected AbstractSpectrumReader m_Reader;

  /** the filter to apply. */
  protected Filter m_Filter;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Uses the specified reader to read the spectral data before applying the supplied filter to it.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "reader", "reader",
      getDefaultReader());

    m_OptionManager.add(
      "filter", "filter",
      getDefaultFilter());
  }

  /**
   * Returns the default instrument of the spectra.
   *
   * @return		the default
   */
  protected AbstractSpectrumReader getDefaultReader() {
    return new SimpleSpectrumReader();
  }

  /**
   * Sets the underlying reader.
   *
   * @param value	the reader
   */
  public void setReader(AbstractSpectrumReader value) {
    m_Reader      = value;
    m_InputIsFile = m_Reader.isInputFile();
    reset();
  }

  /**
   * Returns the underlying reader.
   *
   * @return		the reader
   */
  public AbstractSpectrumReader getReader() {
    return m_Reader;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String readerTipText() {
    return "The reader to use for reading the data before applying the filter.";
  }

  /**
   * Returns the default instrument of the spectra.
   *
   * @return		the default
   */
  protected Filter getDefaultFilter() {
    return new PassThrough();
  }

  /**
   * Sets the filter to apply to the data that was read.
   *
   * @param value	the filter
   */
  public void setFilter(Filter value) {
    m_Filter = value;
    reset();
  }

  /**
   * Returns the filter to apply to the data that was read.
   *
   * @return		the filter
   */
  public Filter getFilter() {
    return m_Filter;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String filterTipText() {
    return "The filter to apply to the data that was read.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the
   * file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Filtered spectrum reader";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"*"};
  }

  /**
   * Hook method for checking the data.
   */
  @Override
  protected void checkData() {
    m_InputIsFile = m_Reader.isInputFile();
    super.checkData();
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    List<Spectrum> 	read;
    Spectrum		filtered;

    m_ReadData.clear();
    m_Reader.setInput(m_Input);
    read = m_Reader.read();

    if (m_Filter instanceof PassThrough) {
      m_ReadData.addAll(read);
    }
    else {
      for (Spectrum sp : read) {
	filtered = (Spectrum) m_Filter.filter(sp);
	m_ReadData.add(filtered);
      }
    }
  }
}
