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
 * OrphanedSampleDataIdSupplier.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.flow.source;

import adams.db.AbstractDatabaseConnection;
import adams.db.Conditions;
import adams.db.OrphanedSampleDataConditions;
import adams.db.SampleDataT;
import adams.flow.core.ActorUtils;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Retrieves sample IDs of sample data that have no spectra associated.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;java.lang.String<br>
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: OrphanedSampleDataIdSupplier
 * </pre>
 *
 * <pre>-annotation &lt;adams.core.base.BaseAnnotation&gt; (property: annotations)
 * &nbsp;&nbsp;&nbsp;The annotations to attach to this actor.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-skip &lt;boolean&gt; (property: skip)
 * &nbsp;&nbsp;&nbsp;If set to true, transformation is skipped and the input token is just forwarded
 * &nbsp;&nbsp;&nbsp;as it is.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-stop-flow-on-error &lt;boolean&gt; (property: stopFlowOnError)
 * &nbsp;&nbsp;&nbsp;If set to true, the flow execution at this level gets stopped in case this
 * &nbsp;&nbsp;&nbsp;actor encounters an error; the error gets propagated; useful for critical
 * &nbsp;&nbsp;&nbsp;actors.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-silent &lt;boolean&gt; (property: silent)
 * &nbsp;&nbsp;&nbsp;If enabled, then no errors are output in the console; Note: the enclosing
 * &nbsp;&nbsp;&nbsp;actor handler must have this enabled as well.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-output-array &lt;boolean&gt; (property: outputArray)
 * &nbsp;&nbsp;&nbsp;If enabled, outputs the sample IDs as array rather than one by one.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-conditions &lt;adams.db.OrphanedSampleDataConditions&gt; (property: conditions)
 * &nbsp;&nbsp;&nbsp;The conditions for retrieving the orphaned sample IDs.
 * &nbsp;&nbsp;&nbsp;default: adams.db.OrphanedSampleDataConditions
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class OrphanedSampleDataIdSupplier
  extends AbstractDbArrayProvider {

  private static final long serialVersionUID = -7422350101761537503L;

  /** the conditions for retrieving the orphaned IDs. */
  protected OrphanedSampleDataConditions m_Conditions;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Retrieves sample IDs of sample data that have no spectra associated.";
  }

  /**
   * Returns the based class of the items.
   *
   * @return		the class
   */
  @Override
  protected Class getItemClass() {
    return String.class;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  @Override
  public String outputArrayTipText() {
    return "If enabled, outputs the sample IDs as array rather than one by one.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "conditions", "conditions",
      getDefaultConditions());
  }

  /**
   * Returns the conditions container to use for retrieving the spectra.
   *
   * @return 		the conditions
   */
  protected OrphanedSampleDataConditions getDefaultConditions() {
    return (OrphanedSampleDataConditions) Conditions.getSingleton().getDefault(new OrphanedSampleDataConditions());
  }

  /**
   * Sets whether to output the items as array or as single strings.
   *
   * @param value	the conditions
   */
  public void setConditions(OrphanedSampleDataConditions value) {
    m_Conditions = value;
    reset();
  }

  /**
   * Returns whether to output the items as array or as single strings.
   *
   * @return		the conditions
   */
  public OrphanedSampleDataConditions getConditions() {
    return m_Conditions;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String conditionsTipText() {
    return "The conditions for retrieving the orphaned sample IDs.";
  }

  /**
   * Returns the default database connection.
   *
   * @return 		the default database connection
   */
  @Override
  protected AbstractDatabaseConnection getDefaultDatabaseConnection() {
    return adams.db.DatabaseConnection.getSingleton();
  }

  /**
   * Determines the database connection in the flow.
   *
   * @return		the database connection to use
   */
  protected adams.db.AbstractDatabaseConnection getDatabaseConnection() {
    return ActorUtils.getDatabaseConnection(
	  this,
	  adams.flow.standalone.DatabaseConnectionProvider.class,
	  getDefaultDatabaseConnection());
  }

  /**
   * Performs the actual database query.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String queryDatabase() {
    String		result;
    List<String> 	ids;

    result = null;
    m_Queue.clear();

    try {
      ids = SampleDataT.getSingleton(m_DatabaseConnection).getOrphanedIDs(m_Conditions);
      if (isLoggingEnabled())
        getLogger().info("# orphaned IDs: " + ids.size());
      m_Queue.addAll(ids);
    }
    catch (Exception e) {
      result = handleException("Failed to retrieve orphaned IDs using " + m_Conditions, e);
    }

    return result;
  }
}
