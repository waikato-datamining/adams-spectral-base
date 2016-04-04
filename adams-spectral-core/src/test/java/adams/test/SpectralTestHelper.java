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
 * SpectralTestHelper.java
 * Copyright (C) 2010-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.test;

import adams.core.base.BasePassword;
import adams.db.AbstractDatabaseConnection;
import adams.data.io.input.SimpleSpectrumReader;
import adams.data.io.output.SimpleSpectrumWriter;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.db.DatabaseConnection;

/**
 * A helper class specific to the project.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2222 $
 */
public class SpectralTestHelper
  extends AbstractTestHelper<Spectrum, Spectrum> {

  /**
   * Initializes the helper class.
   *
   * @param owner	the owning test case
   * @param dataDir	the data directory to use
   */
  public SpectralTestHelper(AdamsTestCase owner, String dataDir) {
    super(owner, dataDir);
  }

  /**
   * Returns the database connection.
   *
   * @param url		the URL to use
   * @param user	the database user
   * @param password	the database password
   */
  public AbstractDatabaseConnection getDatabaseConnection(String url, String user, BasePassword password) {
    m_DatabaseConnection = DatabaseConnection.getSingleton(url, user, password);
    return m_DatabaseConnection;
  }

  /**
   * Tries to connect to the database.
   *
   * @param url		the URL to use
   * @param user	the database user
   * @param password	the database password
   */
  public void connect(String url, String user, BasePassword password) {
    String	lastError;

    m_DatabaseConnection = DatabaseConnection.getSingleton(url, user, password);
    lastError            = m_DatabaseConnection.getLastConnectionError();
    if (!m_DatabaseConnection.isConnected()) {
      try {
	m_DatabaseConnection.connect();
      }
      catch (Exception e) {
	e.printStackTrace();
      }
    }
    if (!m_DatabaseConnection.isConnected()) {
      if (m_DatabaseConnection.getLastConnectionError().length() > 0)
	lastError = m_DatabaseConnection.getLastConnectionError();
      throw new IllegalStateException(
	  "Failed to connect to database:\n"
	  + m_DatabaseConnection.toStringShort() + " (" + lastError + ")");
    }
  }

  /**
   * Loads the data to process using the SimpleSpectrumReader.
   *
   * @param filename	the filename to load (without path)
   * @return		the data, null if it could not be loaded
   * @see		#getDataDirectory()
   * @see		SimpleSpectrumReader
   */
  public Spectrum load(String filename) {
    Spectrum			result;
    SimpleSpectrumReader	reader;

    copyResourceToTmp(filename);

    result = null;
    reader = new SimpleSpectrumReader();
    reader.setInput(new TmpFile(filename));
    if (reader.read().size() > 0)
      result = reader.read().get(0);
    reader.destroy();

    deleteFileFromTmp(filename);

    return result;
  }

  /**
   * Saves the data in the tmp directory using the SimpleSpectrumWriter.
   *
   * @param data	the data to save
   * @param filename	the filename to save to (without path)
   * @return		true if successfully saved
   * @see		SimpleSpectrumWriter
   */
  public boolean save(Spectrum data, String filename) {
    boolean			result;
    SimpleSpectrumWriter	writer;
    TmpFile			output;

    data = (Spectrum) data.getClone();
    data.getReport().setStringValue(SampleData.INSERT_TIMESTAMP, "2000-01-01 00:00:00");

    writer = new SimpleSpectrumWriter();
    writer.setOutputSampleData(true);
    output = new TmpFile(filename);
    writer.setOutput(output);
    writer.write(data);
    result = output.exists();
    writer.destroy();

    return result;
  }
}
