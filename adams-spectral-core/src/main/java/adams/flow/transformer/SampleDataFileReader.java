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
 * SampleDataFileReader.java
 * Copyright (C) 2009-2013 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.data.io.input.AbstractReportReader;
import adams.db.AbstractDatabaseConnection;
import adams.flow.core.ActorUtils;
import adams.data.io.input.SimpleSampleDataReader;
import adams.data.sampledata.SampleData;
import adams.db.DatabaseConnection;

/**
 <!-- globalinfo-start -->
 * Loads a file containing sample data from disk with a specified reader and passes it on.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input/output:<br>
 * - accepts:<br>
 * <pre>   java.lang.String</pre>
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
 *         default: SampleDataFileReader
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
 * <pre>-reader &lt;adams.data.io.input.AbstractReportReader [options]&gt; (property: reader)
 *         The reader to use for importing the reports.
 *         default: knir.data.input.SimpleSampleDataReader -input .
 * </pre>
 *
 * Default options for knir.data.input.SimpleSampleDataReader (-reader/reader):
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-input &lt;adams.core.io.PlaceholderFile&gt; (property: input)
 *         The file to read and turn into a report.
 *         default: .
 * </pre>
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SampleDataFileReader
  extends AbstractReportFileReader<SampleData> {

  /** for serialization. */
  private static final long serialVersionUID = -207124154855872209L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "Loads a file containing sample data from disk with a "
      + "specified reader and passes it on.";
  }

  /**
   * Returns the default reader.
   *
   * @return		the default reader
   */
  @Override
  protected AbstractReportReader<SampleData> getDefaultReader() {
    return new SimpleSampleDataReader();
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
	  adams.flow.standalone.DatabaseConnection.class,
	  getDefaultDatabaseConnection());
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the report class
   */
  @Override
  public Class[] generates() {
    return new Class[]{SampleData.class};
  }
}
