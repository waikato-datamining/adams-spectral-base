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
 * FilteredSpectrumWriter.java
 * Copyright (C) 2023 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.output;

import adams.data.filter.Filter;
import adams.data.filter.PassThrough;
import adams.data.spectrum.Spectrum;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Applies the specified filter to the data before outputting it using the supplied writer.
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
 * <pre>-output &lt;adams.core.io.PlaceholderFile&gt; (property: output)
 * &nbsp;&nbsp;&nbsp;The directory to write the container to.
 * &nbsp;&nbsp;&nbsp;default: ${TMP}&#47;out.tmp
 * </pre>
 *
 * <pre>-filter &lt;adams.data.filter.Filter&gt; (property: filter)
 * &nbsp;&nbsp;&nbsp;The filter to apply to the data that was read.
 * &nbsp;&nbsp;&nbsp;default: adams.data.filter.PassThrough
 * </pre>
 *
 * <pre>-writer &lt;adams.data.io.output.AbstractSpectrumWriter&gt; (property: writer)
 * &nbsp;&nbsp;&nbsp;The writer to use for reading the data before applying the filter.
 * &nbsp;&nbsp;&nbsp;default: adams.data.io.output.SimpleSpectrumWriter
 * </pre>
 *
 <!-- options-end -->
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class FilteredSpectrumWriter
  extends AbstractSpectrumWriter {

  private static final long serialVersionUID = 8937808185716166383L;

  /** the underlying writer. */
  protected AbstractSpectrumWriter m_Writer;

  /** the filter to apply. */
  protected Filter m_Filter;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Applies the specified filter to the data before outputting it using the supplied writer.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "filter", "filter",
      getDefaultFilter());

    m_OptionManager.add(
      "writer", "writer",
      getDefaultWriter());
  }

  /**
   * Returns the default instrument of the spectra.
   *
   * @return		the default
   */
  protected AbstractSpectrumWriter getDefaultWriter() {
    return new SimpleSpectrumWriter();
  }

  /**
   * Sets the underlying writer.
   *
   * @param value	the writer
   */
  public void setWriter(AbstractSpectrumWriter value) {
    m_Writer       = value;
    m_OutputIsFile = value.isOutputFile();
    reset();
  }

  /**
   * Returns the underlying writer.
   *
   * @return		the writer
   */
  public AbstractSpectrumWriter getWriter() {
    return m_Writer;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String writerTipText() {
    return "The writer to use for reading the data before applying the filter.";
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
    return "Filtered spectrum writer";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return m_Writer.getFormatExtensions();
  }

  /**
   * Performs checks on the data.
   *
   * @param data	the data to write
   */
  @Override
  protected void checkData(List<Spectrum> data) {
    m_OutputIsFile = m_Writer.isOutputFile();
    super.checkData(data);
  }

  /**
   * Performs the actual writing.
   *
   * @param data the data to write
   * @return true if successfully written
   */
  @Override
  protected boolean writeData(List<Spectrum> data) {
    boolean		result;
    List<Spectrum> 	dataFiltered;
    Spectrum		filtered;

    result       = true;
    dataFiltered = new ArrayList<>();

    if (m_Filter instanceof PassThrough) {
      dataFiltered.addAll(data);
    }
    else {
      try {
	for (Spectrum sp : data) {
	  filtered = (Spectrum) m_Filter.filter(sp);
	  dataFiltered.add(filtered);
	}
      }
      catch (Exception e) {
	getLogger().log(Level.SEVERE, "Failed to filter data!", e);
	result = false;
      }
    }

    if (result) {
      m_Writer.setOutput(m_Output);
      result = m_Writer.write(dataFiltered);
    }

    return result;
  }
}
