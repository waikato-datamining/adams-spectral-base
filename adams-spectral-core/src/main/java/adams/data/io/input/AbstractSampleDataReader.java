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
 * AbstractSampleDataReader.java
 * Copyright (C) 2009-2013 University of Waikato, Hamilton, New Zealand
 */
package adams.data.io.input;

import adams.core.Constants;
import adams.data.report.Report;
import adams.data.sampledata.SampleData;
import adams.db.AbstractDatabaseConnection;
import adams.db.DatabaseConnection;
import adams.db.DatabaseConnectionHandler;
import adams.db.SampleDataT;
import adams.db.SpectrumT;

/**
 * Ancestor for sample data readers.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1919 $
 */
public abstract class AbstractSampleDataReader
  extends AbstractReportReader<SampleData>
  implements SampleDataReader, DatabaseConnectionHandler {

  /** for serialization. */
  private static final long serialVersionUID = 5225813717569937760L;

  /** the spectrum format to use. */
  protected String m_Format;

  /** whether operate in offline mode, i.e., not query database. */
  protected boolean m_Offline;

  /** the database connection in use. */
  protected AbstractDatabaseConnection m_DatabaseConnection;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "offline", "offline",
	    false);

    m_OptionManager.add(
	    "format", "format",
	    SampleData.DEFAULT_FORMAT);
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_DatabaseConnection = DatabaseConnection.getSingleton();
  }

  /**
   * Sets whether to work in offline mode, i.e., not query the database.
   *
   * @param value	if true then offline mode
   */
  public void setOffline(boolean value) {
    m_Offline = value;
    reset();
  }

  /**
   * Returns whether to work in offline mode, i.e., not query the database.
   *
   * @return		true if in offline mode
   */
  public boolean getOffline() {
    return m_Offline;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String offlineTipText() {
    return "If set to true, the database won't get queried, e.g., for obtaining the parent ID.";
  }

  /**
   * Sets the format of spectrum to use.
   *
   * @param value	the format
   */
  public void setFormat(String value) {
    m_Format = value;
    reset();
  }

  /**
   * The format of spectrum to use.
   *
   * @return		the format
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
    return "The type of spectrum to use (used internally to determine the database ID of the spectrum).";
  }

  /**
   * Returns the currently used database connection object, can be null.
   *
   * @return		the current object
   */
  public AbstractDatabaseConnection getDatabaseConnection() {
    return m_DatabaseConnection;
  }

  /**
   * Sets the database connection object to use.
   *
   * @param value	the object to use
   */
  public void setDatabaseConnection(AbstractDatabaseConnection value) {
    m_DatabaseConnection = value;
    reset();
  }

  /**
   * Returns the table object to use for accessing the reports.
   *
   * @return		the table object
   */
  protected SampleDataT getSampleData() {
    return SampleDataT.getSingleton(getDatabaseConnection());
  }

  /**
   * Returns the table object to use for accessing the spectra.
   *
   * @return		the table object
   */
  protected SpectrumT getSpectrum() {
    return SpectrumT.getSingleton(getDatabaseConnection());
  }

  /**
   * Tries to determine the parent ID for the current report.
   *
   * @param report	the report to determine the ID for
   * @return		the parent database ID, -1 if it cannot be determined
   */
  @Override
  protected int determineParentID(Report report) {
    return Constants.NO_ID;
  }
}
