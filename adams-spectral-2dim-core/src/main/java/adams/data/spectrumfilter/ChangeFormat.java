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
 * ChangeFormat.java
 * Copyright (C) 2009-2010 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;

import java.util.Date;

/**
 <!-- globalinfo-start -->
 * A filter that uses the sample ID to retrieve different formats from the database. The sample data can be retrieved from a different source.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 * &nbsp;&nbsp;&nbsp;If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-store (property: useStoreTable)
 * &nbsp;&nbsp;&nbsp;If set to true, then the data is retrieved from the 'store' tables instead
 * &nbsp;&nbsp;&nbsp;of the 'active' ones.
 * </pre>
 *
 * <pre>-spectrum &lt;java.lang.String&gt; (property: spectrumFormat)
 * &nbsp;&nbsp;&nbsp;The format of the spectrum data to retrieve from the database.
 * &nbsp;&nbsp;&nbsp;default: NIR
 * </pre>
 *
 * <pre>-sample-data &lt;java.lang.String&gt; (property: sampleDataFormat)
 * &nbsp;&nbsp;&nbsp;The format of the sampleData data to retrieve from the database.
 * &nbsp;&nbsp;&nbsp;default: NIR
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class ChangeFormat
  extends AbstractDatabaseFilter {

  /** for serialization. */
  private static final long serialVersionUID = 3367159059984454488L;

  /** the format of the spectrum to load from the database. */
  protected String m_SpectrumFormat;

  /** the format to get the sample data from. */
  protected String m_SampleDataFormat;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return
        "A filter that uses the sample ID to retrieve different formats from "
      + "the database. The sample data can be retrieved from a different source.";
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "spectrum", "spectrumFormat",
	    SampleData.DEFAULT_FORMAT);

    m_OptionManager.add(
	    "sample-data", "sampleDataFormat",
	    SampleData.DEFAULT_FORMAT);
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String useStoreTableTipText() {
    return
        "If set to true, then the data is retrieved from the 'store' tables "
      + "instead of the 'active' ones.";
  }

  /**
   * Sets the format for the spectrum.
   *
   * @param value 	the format
   */
  public void setSpectrumFormat(String value) {
    m_SpectrumFormat = value;
    reset();
  }

  /**
   * Returns the format for the spectrum.
   *
   * @return 		the format
   */
  public String getSpectrumFormat() {
    return m_SpectrumFormat;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String spectrumFormatTipText() {
    return "The format of the spectrum data to retrieve from the database.";
  }

  /**
   * Sets the format for the sample data.
   *
   * @param value 	the format
   */
  public void setSampleDataFormat(String value) {
    m_SampleDataFormat = value;
    reset();
  }

  /**
   * Returns the format for the sample data.
   *
   * @return 		the format
   */
  public String getSampleDataFormat() {
    return m_SampleDataFormat;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sampleDataFormatTipText() {
    return "The format of the sampleData data to retrieve from the database.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  protected Spectrum processData(Spectrum data) {
    Spectrum	result;
    String	sampleID;
    Spectrum	sp;

    // no report?
    if (!data.hasReport()) {
      result = (Spectrum) data.getClone();
      result.setReport(
	  SampleData.createDummy(
	      SampleData.DEFAULT_INSTRUMENT, new Date(), data.getFormat()));
      result.getNotes().addError(
	  getClass(), "No sample data available - added dummy report!");
      return result;
    }

    // sample ID?
    sampleID = data.getReport().getStringValue(new Field(SampleData.SAMPLE_ID, DataType.STRING));
    if (sampleID == null) {
      result = (Spectrum) data.getClone();
      result.getNotes().addError(
	  getClass(), "No sample ID available!");
      return result;
    }

    // load spectrum
    result = getSpectrumTable().load(sampleID, m_SpectrumFormat);

    // load different report?
    if (!m_SampleDataFormat.equals(m_SpectrumFormat)) {
      sp = getSpectrumTable().load(sampleID, m_SampleDataFormat);
      result.setReport((SampleData) sp.getReport().getClone());
      result.getNotes().addNote(
	  getClass(), "Original sample data format: " + data.getFormat());
      result.getNotes().addNote(
	  getClass(), "Original sample data DB ID: " + data.getReport().getDatabaseID());
    }

    return result;
  }
}
