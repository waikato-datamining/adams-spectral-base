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
 * SubtractSpectrum.java
 * Copyright (C) 2008-2026 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractDatabaseConnectionFilter;
import adams.data.filter.Filter;
import adams.data.filter.PassThrough;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.db.AbstractDatabaseConnection;
import adams.db.DatabaseConnection;
import adams.db.SpectrumF;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Subtract a spectrum, after filtering.
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
 * <pre>-filter &lt;adams.data.filter.Filter&gt; (property: filter)
 * &nbsp;&nbsp;&nbsp;The filter to use.
 * &nbsp;&nbsp;&nbsp;default: adams.data.filter.PassThrough
 * </pre>
 *
 * <pre>-database-id &lt;int&gt; (property: databaseID)
 * &nbsp;&nbsp;&nbsp;The database ID of the spectrum to load.
 * &nbsp;&nbsp;&nbsp;default: 69052
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SubtractSpectrum
  extends AbstractDatabaseConnectionFilter<Spectrum> {

  private static final long serialVersionUID = 3414623712198778099L;

  /**
   * Returns the default database connection.
   *
   * @return		the default database connection
   */
  protected AbstractDatabaseConnection getDefaultDatabaseConnection() {
    return DatabaseConnection.getSingleton();
  }

  /** the filter to run. */
  protected Filter<Spectrum> m_Filter;

  /** the DB ID of the spectrum to load and subtract. */
  protected int m_DatabaseID;

  /**
   * Returns a string describing the object.
   *
   * @return 		a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return "Subtract a spectrum, after filtering.";
  }
  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "filter", "filter",
      new PassThrough());

    m_OptionManager.add(
      "database-id", "databaseID",
      69052);
  }

  /**
   * Sets the filter to run.
   *
   * @param value 	the filter
   */
  public void setFilter(Filter value) {
    m_Filter = value;
    reset();
  }

  /**
   * Returns the filter being used.
   *
   * @return 		the filter
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
    return "The filter to use.";
  }

  /**
   * Sets the database ID of the spectrum to load.
   *
   * @param value 	the ID
   */
  public void setDatabaseID(int value) {
    m_DatabaseID = value;
    reset();
  }

  /**
   * Returns the database ID of the spectrum to load.
   *
   * @return 		the ID
   */
  public int getDatabaseID() {
    return m_DatabaseID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String databaseIDTipText() {
    return "The database ID of the spectrum to load.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  protected Spectrum processData(Spectrum data) {
    Spectrum		result;
    int			i;

    Spectrum f1 = m_Filter.filter(data);
    Spectrum sp = SpectrumF.getSingleton(getDatabaseConnection()).load(m_DatabaseID);
    Spectrum f2 = m_Filter.filter(sp);

    result = data.getHeader();
    List<SpectrumPoint> list1 = f1.toList();
    List<SpectrumPoint> list2 = f2.toList();

    for (i = 0; i < list1.size(); i++) {
      // scale point
      SpectrumPoint point1 = list1.get(i);
      SpectrumPoint point2 = list2.get(i);

      SpectrumPoint pointNew = new SpectrumPoint(
	point1.getWaveNumber(),
	(point1.getAmplitude() - point2.getAmplitude()));

      // add to output
      result.add(pointNew);
    }

    return result;
  }
}
