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

/**
 * UpdateDatabaseID.java
 * Copyright (C) 2015-2017 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.transformer.report;

import adams.core.Constants;
import adams.data.sampledata.SampleData;
import adams.db.AbstractDatabaseConnection;
import adams.db.DatabaseConnection;
import adams.db.SpectrumT;
import adams.flow.core.ActorUtils;
import adams.flow.transformer.AbstractReportDbWriter;

/**
 * Attempts to update the database ID of the sample data.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class UpdateDatabaseID
  extends AbstractReportPreProcessor<SampleData> {

  /** for serialization. */
  private static final long serialVersionUID = 8536463609958106232L;

  /** the form of this data. */
  protected String m_Format;

  /** the database connection in use. */
  protected AbstractDatabaseConnection m_DatabaseConnection;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Attempts to update the database ID of the sample data by loading the associated spectrum.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "format", "format",
	    getDefaultFormat());
  }

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();

    m_DatabaseConnection = null;
  }

  /**
   * Returns the default format of the spectra.
   *
   * @return		the default
   */
  protected String getDefaultFormat() {
    return SampleData.DEFAULT_FORMAT;
  }

  /**
   * Sets the format string of the data (always converted to upper case).
   * Use null to set default format.
   *
   * @param value 	the format
   */
  public void setFormat(String value) {
    if (value == null)
      m_Format = SampleData.DEFAULT_FORMAT;
    else
      m_Format = value.toUpperCase();
    reset();
  }

  /**
   * Returns the format string of the data.
   *
   * @return 		the format
   */
  public String getFormat() {
    return m_Format;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String formatTipText() {
    return "The data format string.";
  }

  /**
   * Returns the default database connection.
   *
   * @return 		the default database connection
   */
  protected AbstractDatabaseConnection getDefaultDatabaseConnection() {
    return DatabaseConnection.getSingleton();
  }

  /**
   * Determines the database connection in the flow.
   *
   * @return		the database connection to use
   */
  protected AbstractDatabaseConnection getDatabaseConnection() {
    return ActorUtils.getDatabaseConnection(
      m_Owner,
      adams.flow.standalone.DatabaseConnectionProvider.class,
      getDefaultDatabaseConnection());
  }

  /**
   * Performs the actual pre-processing.
   *
   * @param data	the data to process
   * @return		the processed data
   */
  @Override
  protected SampleData doPreProcess(SampleData data) {
    int	    dbid;

    if (m_DatabaseConnection == null)
      m_DatabaseConnection = getDatabaseConnection();

    if (getOwner() instanceof AbstractReportDbWriter) {
      dbid = SpectrumT.getSingleton(m_DatabaseConnection).getDatabaseID(data.getID(), m_Format);
      if (dbid != Constants.NO_ID)
        data.setDatabaseID(dbid);
    }
    
    return data;
  }
}
