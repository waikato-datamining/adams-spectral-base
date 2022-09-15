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
 * BulkSampleDataDbWriter.java
 * Copyright (C) 2021 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.QuickInfoHelper;
import adams.core.base.BaseRegExp;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.db.AbstractDatabaseConnection;
import adams.db.DatabaseConnection;
import adams.db.SampleDataF;
import adams.flow.core.ActorUtils;
import adams.flow.core.Token;

/**
 <!-- globalinfo-start -->
 * Writes sample data to the database and outputs whether the operation was successful.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;adams.data.sampledata.SampleData[]<br>
 * &nbsp;&nbsp;&nbsp;adams.data.spectrum.Spectrum[]<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;java.lang.Boolean<br>
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
 * &nbsp;&nbsp;&nbsp;default: BulkSampleDataDbWriter
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
 * <pre>-data-type &lt;S|N|B|U&gt; [-data-type ...] (property: dataTypes)
 * &nbsp;&nbsp;&nbsp;The data types to import.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-skip-fields &lt;boolean&gt; (property: skipFields)
 * &nbsp;&nbsp;&nbsp;If enabled, the supplied regular expression is used for identifying fields
 * &nbsp;&nbsp;&nbsp;to skip (name + data type, e.g., 'blah[N]').
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-skip-fields-regexp &lt;adams.core.base.BaseRegExp&gt; (property: skipFieldsRegExp)
 * &nbsp;&nbsp;&nbsp;The regular expression for identifying fields to skip (if enabled).
 * &nbsp;&nbsp;&nbsp;default: (Parent ID|Sample ID|Sample Type|Format|Insert Timestamp|Instrument|Source).*
 * &nbsp;&nbsp;&nbsp;more: https:&#47;&#47;docs.oracle.com&#47;javase&#47;tutorial&#47;essential&#47;regex&#47;
 * &nbsp;&nbsp;&nbsp;https:&#47;&#47;docs.oracle.com&#47;javase&#47;8&#47;docs&#47;api&#47;java&#47;util&#47;regex&#47;Pattern.html
 * </pre>
 *
 * <pre>-batch-size &lt;int&gt; (property: batchSize)
 * &nbsp;&nbsp;&nbsp;The batch size to use for the bulk operation.
 * &nbsp;&nbsp;&nbsp;default: 1000
 * &nbsp;&nbsp;&nbsp;minimum: 1
 * </pre>
 *
 * <pre>-auto-commit &lt;boolean&gt; (property: autoCommit)
 * &nbsp;&nbsp;&nbsp;If disabled, auto-commit gets turned off for the bulk update, which may
 * &nbsp;&nbsp;&nbsp;impact other transactions; use with caution.
 * &nbsp;&nbsp;&nbsp;default: true
 * </pre>
 *
 * <pre>-new-connection &lt;boolean&gt; (property: newConnection)
 * &nbsp;&nbsp;&nbsp;If enabled, a new database connection is opened (and then closed) just for
 * &nbsp;&nbsp;&nbsp;this operation; use this when turning off auto-commit.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class BulkSampleDataDbWriter
    extends AbstractDbTransformer
    implements SampleDataDatabaseWriter {

  /** for serialization. */
  private static final long serialVersionUID = -5253006932367969870L;

  /** the types of data to import. */
  protected adams.data.report.DataType[] m_DataTypes;

  /** whether to skip fields. */
  protected boolean m_SkipFields;

  /** the fields to ignore. */
  protected BaseRegExp m_SkipFieldsRegExp;

  /** the number of records in a batch. */
  protected int m_BatchSize;

  /** whether to use auto-commit. */
  protected boolean m_AutoCommit;

  /** whether to use a new database connection. */
  protected boolean m_NewConnection;

  /** the instance that is currently running a bulk store. */
  protected transient SampleDataF m_SampleDataF;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Writes sample data to the database and outputs whether the operation was successful.";
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
        "data-type", "dataTypes",
        new adams.data.report.DataType[0]);

    m_OptionManager.add(
        "skip-fields", "skipFields",
        false);

    m_OptionManager.add(
        "skip-fields-regexp", "skipFieldsRegExp",
        new BaseRegExp("("
            + adams.data.sampledata.SampleData.PROPERTY_PARENTID + "|"
            + adams.data.sampledata.SampleData.SAMPLE_ID + "|"
            + adams.data.sampledata.SampleData.SAMPLE_TYPE + "|"
            + adams.data.sampledata.SampleData.FORMAT + "|"
            + adams.data.sampledata.SampleData.INSERT_TIMESTAMP + "|"
            + adams.data.sampledata.SampleData.INSTRUMENT + "|"
            + adams.data.sampledata.SampleData.SOURCE
            + ").*"));

    m_OptionManager.add(
        "batch-size", "batchSize",
        1000, 1, null);

    m_OptionManager.add(
        "auto-commit", "autoCommit",
        true);

    m_OptionManager.add(
        "new-connection", "newConnection",
        false);
  }

  /**
   * Sets the data types to import.
   *
   * @param value 	the data types
   */
  public void setDataTypes(adams.data.report.DataType[] value) {
    m_DataTypes = value;
    reset();
  }

  /**
   * Returns the data types to import
   *
   * @return 		the data types
   */
  public adams.data.report.DataType[] getDataTypes() {
    return m_DataTypes;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String dataTypesTipText() {
    return "The data types to import.";
  }

  /**
   * Sets whether to skip fields.
   *
   * @param value 	true if to skip fields
   */
  public void setSkipFields(boolean value) {
    m_SkipFields = value;
    reset();
  }

  /**
   * Returns whether to skip fields.
   *
   * @return 		true if to skip fields
   */
  public boolean getSkipFields() {
    return m_SkipFields;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String skipFieldsTipText() {
    return "If enabled, the supplied regular expression is used for identifying fields to skip (name + data type, e.g., 'blah[N]').";
  }

  /**
   * Sets the regular expression for identifying fields to skip (if enabled).
   *
   * @param value 	the expression
   */
  public void setSkipFieldsRegExp(BaseRegExp value) {
    m_SkipFieldsRegExp = value;
    reset();
  }

  /**
   * Returns the regular expression for identifying fields to skip (if enabled).
   *
   * @return 		the expression
   */
  public BaseRegExp getSkipFieldsRegExp() {
    return m_SkipFieldsRegExp;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String skipFieldsRegExpTipText() {
    return "The regular expression for identifying fields to skip (if enabled).";
  }

  /**
   * Sets the batch size to use.
   *
   * @param value	the size
   */
  public void setBatchSize(int value){
    m_BatchSize = value;
    reset();
  }

  /**
   * Returns the batch size in use.
   *
   * @return		the size
   */
  public int getBatchSize(){
    return m_BatchSize;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String batchSizeTipText() {
    return "The batch size to use for the bulk operation.";
  }

  /**
   * Sets whether to use auto-commit for bulk transaction. May impact other transactions, therefore use with caution!
   *
   * @param value 	true if to use auto-commit
   */
  public void setAutoCommit(boolean value) {
    m_AutoCommit = value;
    reset();
  }

  /**
   * Returns whether to use auto-commit for bulk transactions. May impact other transactions, therefore use with caution!
   *
   * @return 		true if to use auto-commit
   */
  public boolean getAutoCommit() {
    return m_AutoCommit;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String autoCommitTipText() {
    return "If disabled, auto-commit gets turned off for the bulk update, which may impact other transactions; use with caution.";
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
    String 	result;

    result = QuickInfoHelper.toString(this, "dataTypes", m_DataTypes, "types: ");
    if (m_SkipFields)
      result += QuickInfoHelper.toString(this, "skipFieldsRegExp", m_SkipFieldsRegExp, ", skip: ");
    result += QuickInfoHelper.toString(this, "batchSize", m_BatchSize, ", batch size: ");
    result += QuickInfoHelper.toString(this, "autoCommit", (m_AutoCommit ? "auto-commit ON" : "auto-commit OFF"), ", ");
    result += QuickInfoHelper.toString(this, "newConnection", (m_NewConnection ? "new conn" : "re-use conn"), ", ");

    return result;
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
   * Returns the class that the consumer accepts.
   *
   * @return		the report class
   */
  @Override
  public Class[] accepts() {
    return new Class[]{
        SampleData[].class,
        Spectrum[].class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the Class of the generated tokens
   */
  @Override
  public Class[] generates() {
    return new Class[]{Boolean.class};
  }

  /**
   * Performs the actual database query.
   *
   * @return null if everything is fine, otherwise error message
   */
  @Override
  protected String queryDatabase() {
    String		result;
    SampleData[]	records;
    Spectrum[]		spectra;
    int			i;
    boolean		success;

    result  = null;
    records = new SampleData[0];

    if (m_InputToken.hasPayload(SampleData[].class)) {
      records = m_InputToken.getPayload(SampleData[].class);
    }
    else if (m_InputToken.hasPayload(Spectrum[].class)) {
      spectra = m_InputToken.getPayload(Spectrum[].class);
      records = new SampleData[spectra.length];
      for (i = 0; i < spectra.length; i++)
        records[i] = spectra[i].getReport();
    }
    else {
      result = m_InputToken.unhandledData();
    }

    if (result == null) {
      m_SampleDataF = SampleDataF.getSingleton(m_DatabaseConnection);
      success       = m_SampleDataF.bulkStore(records, m_DataTypes, (m_SkipFields ? m_SkipFieldsRegExp.getValue() : null), m_BatchSize, m_AutoCommit, m_NewConnection);
      m_OutputToken = new Token(success);
    }

    m_SampleDataF = null;

    return result;
  }

  /**
   * Stops the execution. No message set.
   */
  @Override
  public void stopExecution() {
    if (m_SampleDataF != null)
      m_SampleDataF.stopBulkStore();
    super.stopExecution();
  }

  /**
   * Cleans up after the execution has finished. Graphical output is left
   * untouched.
   */
  @Override
  public void wrapUp() {
    m_SampleDataF = null;
    super.wrapUp();
  }
}
