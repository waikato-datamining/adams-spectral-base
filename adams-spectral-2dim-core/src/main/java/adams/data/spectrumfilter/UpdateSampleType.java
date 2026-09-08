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
 * UpdateSampleType.java
 * Copyright (C) 2026 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.core.option.OptionUtils;
import adams.data.filter.AbstractFilter;
import adams.data.groupextraction.GroupExtractor;
import adams.data.groupextraction.Manual;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;

/**
 <!-- globalinfo-start -->
 * Updates the sample ID using the specified ID extractor.
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
 * <pre>-no-id-update &lt;boolean&gt; (property: dontUpdateID)
 * &nbsp;&nbsp;&nbsp;If enabled, suppresses updating the ID of adams.data.id.IDHandler data containers.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-no-processing-info-update &lt;boolean&gt; (property: dontUpdateProcessingInfo)
 * &nbsp;&nbsp;&nbsp;If enabled, suppresses updating the processing information of adams.data.NotesHandler
 * &nbsp;&nbsp;&nbsp;data containers.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-extractor &lt;adams.data.groupextraction.GroupExtractor&gt; (property: extractor)
 * &nbsp;&nbsp;&nbsp;The scheme to use for extracting the group.
 * &nbsp;&nbsp;&nbsp;default: adams.data.groupextraction.Manual
 * </pre>
 *
 <!-- options-end -->
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class UpdateSampleType
  extends AbstractFilter<Spectrum> {

  private static final long serialVersionUID = 1798831616767361030L;

  /** the group extractor to use. */
  protected GroupExtractor m_Extractor;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Updates the sample ID using the specified ID extractor.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "extractor", "extractor",
      new Manual());
  }

  /**
   * Sets the group extractor to use.
   *
   * @param value	the extractor
   */
  public void setExtractor(GroupExtractor value) {
    m_Extractor = value;
    reset();
  }

  /**
   * Returns the group extractor to use.
   *
   * @return		the extractor
   */
  public GroupExtractor getExtractor() {
    return m_Extractor;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String extractorTipText() {
    return "The scheme to use for extracting the group.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data the data to filter
   * @return the filtered data
   */
  @Override
  protected Spectrum processData(Spectrum data) {
    Spectrum result;

    result = (Spectrum) data.getClone();

    if (!m_Extractor.handles(data)) {
      getLogger().warning("Group extractor does not handle spectra: " + OptionUtils.getCommandLine(m_Extractor));
      return result;
    }

    if (!result.hasReport())
      result.setReport(new SampleData());
    result.getReport().setStringValue(SampleData.SAMPLE_TYPE, m_Extractor.extractGroup(data));
    return result;
  }
}
