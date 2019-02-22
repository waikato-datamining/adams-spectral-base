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
 * SampleDataDbReader.java
 * Copyright (C) 2009-2019 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.data.sampledata.SampleData;
import adams.db.AbstractDatabaseConnection;
import adams.db.DatabaseConnection;
import adams.db.ReportProviderByID;
import adams.db.SampleDataF;
import adams.flow.core.ActorUtils;

/**
 <!-- globalinfo-start -->
 * Loads sample data from the database and passes it on.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input/output:<br>
 * - accepts:<br>
 * <pre>   java.lang.Integer</pre>
 * - generates:<br>
 * <pre>   knir.data.sampledata.SampleData</pre>
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 *         The name of the actor.
 *         default: SampleDataDbReader
 * </pre>
 *
 * <pre>-annotation &lt;adams.core.base.BaseText&gt; (property: annotations)
 *         The annotations to attach to this actor.
 *         default:
 * </pre>
 *
 * <pre>-skip (property: skip)
 *         If set to true, transformation is skipped and the input token is just forwarded
 *          as it is.
 * </pre>
 *
 * <pre>-store (property: useStoreTable)
 *         If set to true, then the data will get read from the store table, otherwise
 *          the active one.
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SampleDataDbReader
  extends AbstractReportDbReader<SampleData,String>
  implements SampleDataDatabaseReader {

  /** for serialization. */
  private static final long serialVersionUID = 7352720726300796621L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Loads sample data from the database and passes it on.";
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		<!-- flow-accepts-start -->java.lang.Integer.class, java.lang.String.class<!-- flow-accepts-end -->
   */
  @Override
  public Class[] accepts() {
    return new Class[]{Integer.class, String.class};
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
   * Returns the report provider to use.
   *
   * @return		the report provider
   */
  @Override
  protected ReportProviderByID<SampleData> getReportProvider() {
    return SampleDataF.getSingleton(m_DatabaseConnection);
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the type of report
   */
  @Override
  public Class[] generates() {
    return new Class[]{SampleData.class};
  }
}
