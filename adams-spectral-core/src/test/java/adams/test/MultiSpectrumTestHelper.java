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
 * MultiSpectrumTestHelper.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */

package adams.test;

import adams.core.base.BasePassword;
import adams.core.io.FileUtils;
import adams.db.AbstractDatabaseConnection;
import adams.data.conversion.MultiSpectrumToSpectra;
import adams.data.conversion.SpectraToMultiSpectrum;
import adams.data.io.input.SimpleSpectrumReader;
import adams.data.io.output.SimpleSpectrumWriter;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;
import adams.db.DatabaseConnection;

import java.util.Arrays;
import java.util.List;

/**
 * A helper class specific to the project.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public class MultiSpectrumTestHelper
  extends AbstractTestHelper<MultiSpectrum, Spectrum> {

  /**
   * Initializes the helper class.
   *
   * @param owner	the owning test case
   * @param dataDir	the data directory to use
   */
  public MultiSpectrumTestHelper(AdamsTestCase owner, String dataDir) {
    super(owner, dataDir);
  }

  /**
   * Returns the database connection.
   *
   * @param url		the URL to use
   * @param user	the database user
   * @param password	the database password
   */
  @Override
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
  @Override
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
  @Override
  public MultiSpectrum load(String filename) {
    MultiSpectrum		result;
    SimpleSpectrumReader	reader;
    List<Spectrum>		spectra;
    SpectraToMultiSpectrum	conv;
    String			msg;

    copyResourceToTmp(filename);

    reader = new SimpleSpectrumReader();
    reader.setKeepFormat(true);
    reader.setInput(new TmpFile(filename));
    spectra = reader.read();
    reader.destroy();
    
    conv = new SpectraToMultiSpectrum();
    conv.setInput(spectra.toArray(new Spectrum[spectra.size()]));
    msg = conv.convert();
    if (msg == null) {
      result = (MultiSpectrum) conv.getOutput();
    }
    else {
      throw new IllegalStateException(
	  "Failed to load multi-spectrum '" + filename + "'!\n" + msg);
    }
    conv.destroy();

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
  @Override
  public boolean save(Spectrum data, String filename) {
    boolean			result;
    SimpleSpectrumWriter	writer;
    TmpFile			output;

    writer = new SimpleSpectrumWriter();
    writer.setOutputSampleData(true);
    output = new TmpFile(filename);
    writer.setOutput(output);
    writer.write(data);
    result = output.exists();
    writer.destroy();

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
  public boolean save(MultiSpectrum data, String filename) {
    boolean			result;
    String			msg;
    SimpleSpectrumWriter	writer;
    MultiSpectrumToSpectra	conv;
    TmpFile			output;
    Spectrum[]			conts;
    List<String>		lines;
    int				i;

    conv = new MultiSpectrumToSpectra();
    conv.setOutputReport(true);
    conv.setInput(data);
    msg = conv.convert();
    if (msg == null) {
      conts = (Spectrum[]) conv.getOutput();
      writer = new SimpleSpectrumWriter();
      writer.setOutputSampleData(true);
      output = new TmpFile(filename + ".tmp");
      writer.setOutput(output);
      writer.write(Arrays.asList(conts));
      result = output.exists();
      writer.destroy();
      // remove timestamps
      if (result) {
	lines = FileUtils.loadFromFile(output);
	i = 0;
	while (i < lines.size()) {
	  if (lines.get(i).startsWith("# #"))
	    lines.remove(i);
	  else
	    i++;
	}
	output.delete();
	output = new TmpFile(filename);
	result = FileUtils.saveToFile(lines, output);
      }
    }
    else {
      result = false;
    }

    return result;
  }
}
