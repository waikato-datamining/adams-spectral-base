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
 * SpectrumDbWriter.java
 * Copyright (C) 2009-2019 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.Constants;
import adams.data.spectrum.Spectrum;
import adams.db.AbstractDatabaseConnection;
import adams.db.DataProvider;
import adams.db.DatabaseConnection;
import adams.db.SpectrumF;
import adams.db.SpectrumIntf;
import adams.flow.core.ActorUtils;

/**
 <!-- globalinfo-start -->
 * Writes a spectrum to the database and passes its ID on.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;adams.data.spectrum.Spectrum<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;java.lang.Integer<br>
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
 * &nbsp;&nbsp;&nbsp;default: SpectrumDbWriter
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
 * <pre>-pre-processor &lt;adams.flow.transformer.datacontainer.AbstractDataContainerPreProcessor&gt; (property: preProcessor)
 * &nbsp;&nbsp;&nbsp;The pre-processor to apply to the data.
 * &nbsp;&nbsp;&nbsp;default: adams.flow.transformer.datacontainer.NoPreProcessing
 * </pre>
 *
 * <pre>-overwrite-existing &lt;boolean&gt; (property: overwriteExisting)
 * &nbsp;&nbsp;&nbsp;If enabled, an existing spectrum gets removed from the database first before
 * &nbsp;&nbsp;&nbsp;the new one is being written; if disabled, then new spectra that already
 * &nbsp;&nbsp;&nbsp;exist in the database won't get added, forwarding -1.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-keep-report &lt;boolean&gt; (property: keepReport)
 * &nbsp;&nbsp;&nbsp;If enabled, any existing report is kept in the database.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-output-container &lt;boolean&gt; (property: outputContainer)
 * &nbsp;&nbsp;&nbsp;If enabled, the spectrum (with updated database ID if not -1) is output
 * &nbsp;&nbsp;&nbsp;instead of the database ID.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-store-wave-no &lt;boolean&gt; (property: storeWaveNo)
 * &nbsp;&nbsp;&nbsp;If enabled, the wave numbers get stored in the database as well.
 * &nbsp;&nbsp;&nbsp;default: true
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumDbWriter
  extends AbstractDataContainerDbWriter<Spectrum> 
  implements SpectrumDatabaseWriter {

  /** for serialization. */
  private static final long serialVersionUID = 1307281845108207161L;

  /** whether to store the wave numbers as well. */
  protected boolean m_StoreWaveNo;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Writes a spectrum to the database and passes its ID on.";
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  @Override
  public String overwriteExistingTipText() {
    return 
	"If enabled, an existing spectrum gets removed from the database "
	+ "first before the new one is being written; if disabled, then new "
	+ "spectra that already exist in the database won't get added, "
	+ "forwarding " + Constants.NO_ID + ".";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "store-wave-no", "storeWaveNo",
      true);
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  @Override
  public String outputContainerTipText() {
    return "If enabled, the spectrum (with updated database ID if not " + Constants.NO_ID + ") is output instead of the database ID.";
  }

  /**
   * Sets whether to store the wave numbers as well.
   *
   * @param value 	true if to store
   */
  public void setStoreWaveNo(boolean value) {
    m_StoreWaveNo = value;
    reset();
  }

  /**
   * Returns whether to store the wave numbers as well.
   *
   * @return 		true if to store
   */
  public boolean getStoreWaveNo() {
    return m_StoreWaveNo;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String storeWaveNoTipText() {
    return "If enabled, the wave numbers get stored in the database as well.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result = super.getQuickInfo();
    if (!m_StoreWaveNo)
      result += ", no wave numbers";

    return result;
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the type of data to store
   */
  @Override
  public Class[] accepts() {
    return new Class[]{Spectrum.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the classes
   */
  @Override
  public Class[] generates() {
    if (m_OutputContainer)
      return new Class[]{Spectrum.class};
    else
      return new Class[]{Integer.class};
  }

  /**
   * Returns the default database connection.
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
   * Returns the data provider to use for storing the container in the database.
   *
   * @param cont	the container
   * @return		the data provider
   */
  @Override
  public DataProvider<Spectrum> getDataProvider(Spectrum cont) {
    return SpectrumF.getSingleton(m_DatabaseConnection);
  }

  /**
   * Adds the container to the database.
   *
   * @param provider	the provider to use
   * @param cont	the container to store
   * @return		the database ID, {@link Constants#NO_ID} if failed
   */
  public Integer add(DataProvider provider, Spectrum cont) {
    return ((SpectrumIntf) provider).add(cont, m_StoreWaveNo);
  }

  /**
   * Returns whether the container already exists in the database.
   * 
   * @param provider	the provider to use for checking
   * @param cont	the container to look for
   * @return		true if already stored in database
   */
  @Override
  public boolean exists(DataProvider provider, Spectrum cont) {
    return ((SpectrumIntf) provider).exists(cont.getID(), cont.getFormat());
  }

  /**
   * Removes the container from the database.
   * 
   * @param provider	the provider to use for removing
   * @param cont	the container to remove
   * @return		true if successfully removed
   */
  @Override
  public boolean remove(DataProvider provider, Spectrum cont) {
    return ((SpectrumIntf) provider).remove(cont.getID(), cont.getFormat(), m_KeepReport);
  }

  /**
   * Loads the container from the database.
   * 
   * @param provider	the provider to use
   * @param cont	the container to store
   * @return		the container, null if failed to load
   */
  @Override
  public Spectrum load(DataProvider provider, Spectrum cont) {
    return ((SpectrumIntf) provider).load(cont.getID(), cont.getFormat());
  }
}
