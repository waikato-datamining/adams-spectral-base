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
 * ExtractIdAndTypeSpectrumReader.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input;

import adams.core.Utils;
import adams.data.groupextraction.GroupExtractor;
import adams.data.idextraction.IDExtractor;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import com.github.fracpete.javautils.enumerate.Enumerated;

import static com.github.fracpete.javautils.Enumerate.enumerate;

/**
 <!-- globalinfo-start -->
 * Uses the specified ID and group extractor to obtain and update sample ID and type of the spectra read by the specified base reader.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
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
 * &nbsp;&nbsp;&nbsp;The base reader to use.
 * &nbsp;&nbsp;&nbsp;default: adams.data.io.input.SimpleSpectrumReader
 * </pre>
 *
 * <pre>-id-extraction &lt;adams.data.idextraction.IDExtractor&gt; (property: IDExtraction)
 * &nbsp;&nbsp;&nbsp;The ID extractor to use.
 * &nbsp;&nbsp;&nbsp;default: adams.data.idextraction.Null
 * </pre>
 *
 * <pre>-group-extraction &lt;adams.data.groupextraction.GroupExtractor&gt; (property: groupExtraction)
 * &nbsp;&nbsp;&nbsp;For extracting the sample type from the spectra.
 * &nbsp;&nbsp;&nbsp;default: adams.data.groupextraction.Null
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ExtractIdAndTypeSpectrumReader
  extends AbstractSpectrumReader {

  private static final long serialVersionUID = -4533162912311366691L;

  /** the base reader. */
  protected AbstractSpectrumReader m_Reader;

  /** the ID extractor. */
  protected IDExtractor m_IDExtraction;

  /** the group extractor. */
  protected GroupExtractor m_GroupExtraction;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Uses the specified ID and group extractor to obtain and update "
      + "sample ID and type of the spectra read by the specified base reader.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "reader", "reader",
      new SimpleSpectrumReader());

    m_OptionManager.add(
      "id-extraction", "IDExtraction",
      new adams.data.idextraction.Null());

    m_OptionManager.add(
      "group-extraction", "groupExtraction",
      new adams.data.groupextraction.Null());
  }

  /**
   * Sets the base reader to use.
   *
   * @param value	the reader
   */
  public void setReader(AbstractSpectrumReader value) {
    m_Reader = value;
    reset();
  }

  /**
   * Returns the base reader to use.
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
    return "The base reader to use.";
  }

  /**
   * Sets the sample ID extractor to use.
   *
   * @param value	the extractor
   */
  public void setIDExtraction(IDExtractor value) {
    m_IDExtraction = value;
    reset();
  }

  /**
   * Returns the sample ID extractor to use.
   *
   * @return		the extractor
   */
  public IDExtractor getIDExtraction() {
    return m_IDExtraction;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String IDExtractionTipText() {
    return "The ID extractor to use.";
  }

  /**
   * Sets the scheme for extracting the sample type.
   *
   * @param value	the extractor
   */
  public void setGroupExtraction(GroupExtractor value) {
    m_GroupExtraction = value;
    reset();
  }

  /**
   * Returns the scheme for extracting the sample type.
   *
   * @return		the extractor
   */
  public GroupExtractor getGroupExtraction() {
    return m_GroupExtraction;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String groupExtractionTipText() {
    return "For extracting the sample type from the spectra.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Meta reader (extracts ID/type)";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"*"};
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    String	id;
    String	type;

    m_Reader.setInput(m_Input);
    m_ReadData = m_Reader.read();
    for (Enumerated<Spectrum> r: enumerate(m_ReadData)) {
      // ID
      if (!(m_IDExtraction instanceof adams.data.idextraction.Null)) {
	if (m_IDExtraction.handles(r.value)) {
	  id = m_IDExtraction.extractID(r.value);
	  if (id != null) {
	    r.value.setID(id);
	    if (isLoggingEnabled())
	      getLogger().info("Using extracted sample ID: " + id);
	  }
	  else {
	    if (isLoggingEnabled())
	      getLogger().info("No ID extracted, not updated");
	  }
	}
	else {
	  getLogger().warning("ID extractor " + Utils.classToString(m_IDExtraction) + " cannot handle " + Utils.classToString(r.value));
	}
      }

      // group
      if (!(m_GroupExtraction instanceof adams.data.groupextraction.Null)) {
	if (m_GroupExtraction.handles(r.value)) {
	  type = m_GroupExtraction.extractGroup(r.value);
	  if (type != null) {
	    r.value.getReport().setStringValue(SampleData.SAMPLE_TYPE, type);
	    if (isLoggingEnabled())
	      getLogger().info("Using extracted sample type: " + type);
	  }
	  else {
	    if (isLoggingEnabled())
	      getLogger().info("No sample type extracted, not updated");
	  }
	}
	else {
	  getLogger().warning("Group extractor " + Utils.classToString(m_GroupExtraction) + " cannot handle " + Utils.classToString(r.value));
	}
      }
    }
  }
}
