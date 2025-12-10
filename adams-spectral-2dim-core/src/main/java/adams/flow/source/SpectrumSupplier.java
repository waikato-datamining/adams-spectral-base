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
 * SpectrumSupplier.java
 * Copyright (C) 2025 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.source;

import adams.core.QuickInfoHelper;
import adams.core.VariableName;
import adams.data.spectrum.Spectrum;
import adams.db.AbstractDatabaseConnection;
import adams.db.AbstractSpectrumConditions;
import adams.db.DatabaseConnection;
import adams.db.SpectrumConditionsMulti;
import adams.db.SpectrumF;
import adams.db.SpectrumIterator;
import adams.flow.core.ActorUtils;
import adams.flow.core.Token;

/**
 <!-- globalinfo-start -->
 * Loads spectra from the database that matched the specified conditions.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;adams.data.spectrum.Spectrum<br>
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * &nbsp;&nbsp;&nbsp;min-user-mode: Expert
 * </pre>
 *
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: SpectrumSupplier
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
 * &nbsp;&nbsp;&nbsp;min-user-mode: Expert
 * </pre>
 *
 * <pre>-silent &lt;boolean&gt; (property: silent)
 * &nbsp;&nbsp;&nbsp;If enabled, then no errors are output in the console; Note: the enclosing
 * &nbsp;&nbsp;&nbsp;actor handler must have this enabled as well.
 * &nbsp;&nbsp;&nbsp;default: false
 * &nbsp;&nbsp;&nbsp;min-user-mode: Expert
 * </pre>
 *
 * <pre>-conditions &lt;adams.db.AbstractSpectrumConditions&gt; (property: conditions)
 * &nbsp;&nbsp;&nbsp;The conditions for retrieving the spectra from the database.
 * &nbsp;&nbsp;&nbsp;default: adams.db.SpectrumConditionsMulti
 * </pre>
 *
 <!-- options-end -->
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumSupplier
  extends AbstractDbSource {

  private static final long serialVersionUID = 7593497845787658829L;

  /** the conditions to use. */
  protected AbstractSpectrumConditions m_Conditions;

  /** whether to update the variable with the number of rows. */
  protected boolean m_UpdateNumRowsVar;

  /** the variable to store the number of rows in. */
  protected VariableName m_NumRowsVar;

  /** whether to use a new database connection. */
  protected boolean m_NewConnection;

  /** the iterator. */
  protected transient SpectrumIterator m_Iterator;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Loads spectra from the database that matched the specified conditions.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "conditions", "conditions",
      new SpectrumConditionsMulti());

    m_OptionManager.add(
      "update-num-rows-var", "updateNumRowsVar",
      false);

    m_OptionManager.add(
      "num-rows-var", "numRowsVar",
      new VariableName());

    m_OptionManager.add(
      "new-connection", "newConnection",
      false);
  }

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();
    closeIterator();
  }

  /**
   * Sets the conditions container to use for retrieving the spectra.
   *
   * @param value 	the conditions
   */
  public void setConditions(AbstractSpectrumConditions value) {
    m_Conditions = value;
    reset();
  }

  /**
   * Returns the conditions container to use for retrieving the spectra.
   *
   * @return 		the conditions
   */
  public AbstractSpectrumConditions getConditions() {
    return m_Conditions;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String conditionsTipText() {
    return "The conditions for retrieving the spectra from the database.";
  }

  /**
   * Sets whether to update the variable with the number of rows.
   *
   * @param value 	true if to update
   */
  public void setUpdateNumRowsVar(boolean value) {
    m_UpdateNumRowsVar = value;
    reset();
  }

  /**
   * Returns whether to update the variable with the number of rows.
   *
   * @return 		true if to update
   */
  public boolean getUpdateNumRowsVar() {
    return m_UpdateNumRowsVar;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String updateNumRowsVarTipText() {
    return "If enabled, the specified variable is updated with the number of rows in the resultset.";
  }

  /**
   * Sets the variable to store the number of rows in.
   *
   * @param value 	the name
   */
  public void setNumRowsVar(VariableName value) {
    m_NumRowsVar = value;
    reset();
  }

  /**
   * Returns the variable to store the number of rows in.
   *
   * @return 		the name
   */
  public VariableName getNumRowsVar() {
    return m_NumRowsVar;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String numRowsVarTipText() {
    return "The variable to store the number of rows in.";
  }

  /**
   * Sets whether to use a new connection for this update. Use when autoCommit is off.
   *
   * @param value 	true if to use new connection
   */
  public void setNewConnection(boolean value) {
    m_NewConnection = value;
    reset();
  }

  /**
   * Returns whether to use a new connection for this update. Use when autoCommit is off.
   *
   * @return 		true if to use new connection
   */
  public boolean getNewConnection() {
    return m_NewConnection;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String newConnectionTipText() {
    return "If enabled, a new database connection is opened (and then closed) just for this operation; use this when turning off auto-commit.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result = QuickInfoHelper.toString(this, "conditions", m_Conditions, "conditions: ");
    if (m_UpdateNumRowsVar)
      result += QuickInfoHelper.toString(this, "numRowsVar", m_NumRowsVar, ", #rows var: ");
    result += QuickInfoHelper.toString(this, "newConnection", m_NewConnection, "new connection", ", ");

    return result;
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return the Class of the generated tokens
   */
  @Override
  public Class[] generates() {
    return new Class[]{Spectrum.class};
  }

  /**
   * Returns the default database connection.
   *
   * @return 		the default database connection
   */
  @Override
  protected AbstractDatabaseConnection getDefaultDatabaseConnection() {
    return new DatabaseConnection();
  }

  /**
   * Determines the database connection in the flow.
   *
   * @return		the database connection to use
   */
  @Override
  protected AbstractDatabaseConnection getDatabaseConnection() {
    return ActorUtils.getDatabaseConnection(
      this, adams.flow.standalone.DatabaseConnectionProvider.class, getDefaultDatabaseConnection());
  }

  /**
   * Performs the actual database query.
   *
   * @return null if everything is fine, otherwise error message
   */
  @Override
  protected String queryDatabase() {
    String	result;

    result = null;

    m_Iterator = SpectrumF.getSingleton(getDatabaseConnection()).iterate(m_Conditions, m_NewConnection);
    if (m_Iterator == null) {
      result = "Failed to instantiate iterator for spectra!";
    }
    else {
      if (m_UpdateNumRowsVar)
	getVariables().set(m_NumRowsVar.getValue(), "" + m_Iterator.getSize());
    }

    return result;
  }

  /**
   * Returns the generated token.
   *
   * @return the generated token
   */
  @Override
  public Token output() {
    if (m_Iterator == null)
      return null;
    if (!m_Iterator.hasNext())
      return null;

    return new Token(m_Iterator.next());
  }

  /**
   * Checks whether there is pending output to be collected after
   * executing the flow item.
   * <br><br>
   * The method is not allowed allowed to return "true" before the
   * actor has been executed. For actors that return an infinite
   * number of tokens, the m_Executed flag can be returned.
   *
   * @return true if there is pending output
   */
  @Override
  public boolean hasPendingOutput() {
    return (m_Iterator != null) && (m_Iterator.hasNext());
  }

  /**
   * Closes the iterator, if necessary.
   */
  protected void closeIterator() {
    if (m_Iterator != null) {
      try {
	m_Iterator.close();
      }
      catch (Exception e) {
	// ignored
      }
      m_Iterator = null;
    }
  }

  /**
   * Cleans up after the execution has finished. Graphical output is left
   * untouched.
   */
  @Override
  public void wrapUp() {
    closeIterator();
    super.wrapUp();
  }
}
