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
 * SampleDataDbReportDbWriter.java
 * Copyright (C) 2009-2017 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.db.AbstractDatabaseConnection;
import adams.db.DatabaseConnection;
import adams.db.ReportProvider;
import adams.db.SampleDataT;
import adams.flow.core.ActorUtils;

/**
 <!-- globalinfo-start -->
 * Writes sample data to the database if possible and passes the ID of the saved report on.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input/output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;knir.data.sampledata.SampleData<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;java.lang.Integer<br>
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 * &nbsp;&nbsp;&nbsp;If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: SampleDataDbWriter
 * </pre>
 *
 * <pre>-annotation &lt;adams.core.base.BaseText&gt; (property: annotations)
 * &nbsp;&nbsp;&nbsp;The annotations to attach to this actor.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-skip (property: skip)
 * &nbsp;&nbsp;&nbsp;If set to true, transformation is skipped and the input token is just forwarded
 * &nbsp;&nbsp;&nbsp;as it is.
 * </pre>
 *
 * <pre>-remove (property: removeExisting)
 * &nbsp;&nbsp;&nbsp;If true then existing reports will be removed first completely from the
 * &nbsp;&nbsp;&nbsp;database before the current one is saved.
 * </pre>
 *
 * <pre>-merge (property: merge)
 * &nbsp;&nbsp;&nbsp;If true then the information in the current report is only added to the
 * &nbsp;&nbsp;&nbsp;existing one (but 'Dummy report' is always set to 'false').
 * </pre>
 *
 * <pre>-overwrite &lt;adams.data.report.Field&gt; [-overwrite ...] (property: overwriteFields)
 * &nbsp;&nbsp;&nbsp;The fields to overwrite with the new data when in 'merge' mode.
 * </pre>
 *
 * <pre>-store (property: useStoreTable)
 * &nbsp;&nbsp;&nbsp;If set to true, then the data will get read from the store table, otherwise
 * &nbsp;&nbsp;&nbsp;the active one.
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2390 $
 */
public class SampleDataDbWriter
  extends AbstractReportDbWriterByID<SampleData>
  implements SampleDataDatabaseWriter {

  /** for serialization. */
  private static final long serialVersionUID = -5253006932367969870L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "Writes sample data to the database if possible and "
      + "passes the ID of the saved report on.";
  }

  /**
   * Returns the default database connection.
   *
   * @return 		the default database connection
   */
  @Override
  protected AbstractDatabaseConnection getDefaultDatabaseConnection() {
    return DatabaseConnection.getSingleton();
  }

  /**
   * Determines the database connection in the flow.
   *
   * @return		the database connection to use
   */
  @Override
  protected AbstractDatabaseConnection getDatabaseConnection() {
    return ActorUtils.getDatabaseConnection(
	  this,
	  adams.flow.standalone.DatabaseConnectionProvider.class,
	  getDefaultDatabaseConnection());
  }

  /**
   * Returns the report provider to use for writing the reports to the database.
   *
   * @return		the provider to use
   */
  @Override
  protected ReportProvider<SampleData,String> getReportProvider() {
    return SampleDataT.getSingleton(m_DatabaseConnection);
  }

  /**
   * Extracts the ID from the report.
   *
   * @param report	the report to extract the ID from
   * @return		the ID
   */
  @Override
  protected String extractID(SampleData report) {
    return report.getID();
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the report class
   */
  @Override
  public Class[] accepts() {
    return new Class[]{
	SampleData.class,
	Spectrum.class};
  }
}
