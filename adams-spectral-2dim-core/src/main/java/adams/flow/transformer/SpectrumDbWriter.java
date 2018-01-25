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
 * Copyright (C) 2009-2018 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.Constants;
import adams.data.spectrum.Spectrum;
import adams.db.AbstractDatabaseConnection;
import adams.db.DataProvider;
import adams.db.DatabaseConnection;
import adams.db.SpectrumT;
import adams.flow.core.ActorUtils;

/**
 <!-- globalinfo-start -->
 * Writes a spectrum to the database and passes its ID on.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input/output:<br>
 * - accepts:<br>
 * <pre>   knir.data.spectrum.Spectrum</pre>
 * - generates:<br>
 * <pre>   java.lang.Integer</pre>
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
 *         default: SpectrumDbWriter
 * </pre>
 *
 * <pre>-annotation &lt;adams.core.base.BaseText&gt; (property: annotations)
 *         The annotations to attach to this actor.
 *         default:
 * </pre>
 *
 * <pre>-skip (property: skip)
 *         If set to true, transformation is skipped and the input token is just forwarded
 *         as it is.
 * </pre>
 *
 * <pre>-store (property: useStoreTable)
 *         If set to true, then the data will get written to the 'store' table, otherwise
 *         the 'active' one.
 * </pre>
 *
 * <pre>-automatic-table (property: selectTableAutomatically)
 *         If set to true, then the table is selected automatically (has report: 'active'
 *         , missing report or only dummy report: 'store').
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
    return SpectrumT.getSingleton(m_DatabaseConnection);
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
    return ((SpectrumT) provider).exists(cont.getID(), cont.getFormat());
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
    return ((SpectrumT) provider).remove(cont.getID(), cont.getFormat(), m_KeepReport);
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
    return ((SpectrumT) provider).load(cont.getID(), cont.getFormat());
  }
}
