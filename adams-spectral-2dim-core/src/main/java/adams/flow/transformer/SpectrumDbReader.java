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
 * SpectrumDbReader.java
 * Copyright (C) 2009-2017 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.QuickInfoHelper;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.db.AbstractDatabaseConnection;
import adams.db.DataProvider;
import adams.db.DatabaseConnection;
import adams.db.SpectrumT;
import adams.flow.core.ActorUtils;
import adams.flow.core.Token;

/**
 <!-- globalinfo-start -->
 * Loads a spectrum from the database and passes it on.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input/output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;java.lang.Integer<br>
 * &nbsp;&nbsp;&nbsp;java.lang.String<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;knir.data.spectrum.Spectrum<br>
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
 * &nbsp;&nbsp;&nbsp;default: SpectrumDbReader
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
 * <pre>-raw (property: raw)
 * &nbsp;&nbsp;&nbsp;If set to true, then the raw data is returned instead of being filtered
 * &nbsp;&nbsp;&nbsp;through the global data container filter.
 * </pre>
 *
 * <pre>-store (property: useStoreTable)
 * &nbsp;&nbsp;&nbsp;If set to true, then the data will get read from the store table, otherwise
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
 * @version $Revision: 2357 $
 */
public class SpectrumDbReader
  extends AbstractDataContainerDbReader<Spectrum>
  implements SpectrumDatabaseReader {

  /** for serialization. */
  private static final long serialVersionUID = -1045308734164860962L;

  /** what type of spectrum to retrieve from the database. */
  protected String m_Format;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Loads a spectrum from the database and passes it on.";
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
   * Returns the class of objects that it generates.
   *
   * @return		the data to retrieve from the database
   */
  @Override
  public Class[] generates() {
    return new Class[]{Spectrum.class};
  }

  /**
   * Returns the data provider to use for storing the container in the database.
   *
   * @return		the data provider
   */
  @Override
  public DataProvider<Spectrum> getDataProvider() {
    return SpectrumT.getSingleton(m_DatabaseConnection);
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
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String queryDatabase() {
    String	result;
    Spectrum	cont;
    boolean	useSampleID;

    result = null;

    useSampleID = (m_InputToken.getPayload() instanceof String);

    if (useSampleID) {
      if (m_Raw)
	cont = ((SpectrumT) getDataProvider()).loadRaw((String) m_InputToken.getPayload(), m_Format);
      else
	cont = ((SpectrumT) getDataProvider()).load((String) m_InputToken.getPayload(), m_Format);
      if (cont == null)
	result = "No container loaded for sample ID (format=" + m_Format + "): " + m_InputToken;
      else
	m_OutputToken = new Token(m_PostProcessor.postProcess(cont));
    }
    else {
      result = super.queryDatabase();
    }

    return result;
  }
}
