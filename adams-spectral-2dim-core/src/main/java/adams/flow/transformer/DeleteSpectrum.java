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
 * DeleteSpectrum.java
 * Copyright (C) 2012-2019 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.transformer;

import adams.core.QuickInfoHelper;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.db.AbstractDatabaseConnection;
import adams.db.DataProvider;
import adams.db.DatabaseConnection;
import adams.db.SpectrumF;
import adams.db.SpectrumIntf;
import adams.flow.core.ActorUtils;
import adams.flow.core.Token;

/**
 <!-- globalinfo-start -->
 * Removes the spectrum identified by database ID or sample ID.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;java.lang.Integer<br>
 * &nbsp;&nbsp;&nbsp;java.lang.String<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;java.lang.Integer<br>
 * &nbsp;&nbsp;&nbsp;java.lang.String<br>
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 * 
 * <pre>-D &lt;int&gt; (property: debugLevel)
 * &nbsp;&nbsp;&nbsp;The greater the number the more additional info the scheme may output to 
 * &nbsp;&nbsp;&nbsp;the console (0 = off).
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 * 
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: DeleteSpectrum
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
 * <pre>-stop-flow-on-error (property: stopFlowOnError)
 * &nbsp;&nbsp;&nbsp;If set to true, the flow gets stopped in case this actor encounters an error;
 * &nbsp;&nbsp;&nbsp; useful for critical actors.
 * </pre>
 * 
 * <pre>-store (property: useStoreTable)
 * &nbsp;&nbsp;&nbsp;If set to true, then the data will get deleted from the store table, otherwise 
 * &nbsp;&nbsp;&nbsp;the active one.
 * </pre>
 * 
 * <pre>-format &lt;java.lang.String&gt; (property: format)
 * &nbsp;&nbsp;&nbsp;The format of spectrum to retrieve from the database (if sample IDs arrive 
 * &nbsp;&nbsp;&nbsp;at the input port).
 * &nbsp;&nbsp;&nbsp;default: NIR
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class DeleteSpectrum
  extends AbstractDeleteDataContainer<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -8629643109218579387L;

  /** what type of spectrum to retrieve from the database. */
  protected String m_Format;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Removes the spectrum identified by database ID or sample ID.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "format", "format",
	    SampleData.DEFAULT_FORMAT);
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    return QuickInfoHelper.toString(this, "format", m_Format, "format: ");
  }

  /**
   * Sets the format of spectrum to retrieve from the database.
   *
   * @param value	the format
   */
  public void setFormat(String value){
    m_Format = value;
    reset();
  }

  /**
   * Returns the format of spectrum to retrieve from the database.
   *
   * @return		the format
   */
  public String getFormat(){
    return m_Format;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String formatTipText() {
    return "The format of spectrum to retrieve from the database (if sample IDs arrive at the input port).";
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
   * Returns the class that the consumer accepts.
   *
   * @return		<!-- flow-generates-start -->java.lang.Integer.class, java.lang.String.class<!-- flow-generates-end -->
   */
  @Override
  public Class[] generates() {
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
   * Returns the data provider to use for storing the container in the database.
   *
   * @return		the data provider
   */
  @Override
  protected DataProvider<Spectrum> getDataProvider() {
    return SpectrumF.getSingleton(m_DatabaseConnection);
  }

  /**
   * Removes the container from the database.
   * 
   * @param input	the ID of the container
   * @return		true if successfully removed
   */
  @Override
  protected boolean remove(Token input) {
    SpectrumIntf		spec;

    spec = (SpectrumIntf) getDataProvider();
    if (m_InputToken.getPayload() instanceof Integer)
      return spec.remove((Integer) m_InputToken.getPayload(), m_KeepReport);
    else
      return spec.remove((String) m_InputToken.getPayload(), m_Format, m_KeepReport);
  }
}
