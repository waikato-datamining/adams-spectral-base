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
 * AbstractCleanerTestCase.java
 * Copyright (C) 2010-2024 University of Waikato, Hamilton, New Zealand
 */
package adams.data.cleaner.spectrum;

import adams.core.CleanUpHandler;
import adams.core.Destroyable;
import adams.core.io.FileUtils;
import adams.data.spectrum.Spectrum;
import adams.db.DatabaseConnectionHandler;
import adams.test.AbstractSpectralDatabaseTestCase;
import adams.test.AbstractTestHelper;
import adams.test.SpectralTestHelper;
import adams.test.TmpFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Ancestor for test cases tailored for cleaners.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractCleanerTestCase
  extends AbstractSpectralDatabaseTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public AbstractCleanerTestCase(String name) {
    super(name);
  }

  /**
   * Returns the test helper class to use.
   *
   * @return		the helper class instance
   */
  @Override
  protected AbstractTestHelper newTestHelper() {
    return new SpectralTestHelper(this, "adams/data/cleaner/spectrum/data");
  }

  /**
   * Loads the data to process.
   *
   * @param filename	the filename to load (without path)
   * @return		the data, null if it could not be loaded
   */
  protected Spectrum load(String filename) {
    return ((SpectralTestHelper) m_TestHelper).load(filename);
  }

  /**
   * Processes the input data and returns the processed data.
   *
   * @param data	the data to work on
   * @param scheme	the scheme to process the data with
   * @return		the processed data
   */
  protected String process(Spectrum data, AbstractCleaner scheme) {
    return scheme.check(data);
  }

  /**
   * Saves the data in the tmp directory.
   *
   * @param data	the data to save
   * @param filename	the filename to save to (without path)
   * @return		true if successfully saved
   */
  protected boolean save(String data, String filename) {
    return FileUtils.saveToFile(new String[]{data}, new File(m_TestHelper.getTmpDirectory() + File.separator + filename));
  }

  /**
   * Returns the filenames (without path) of the input data files to use
   * in the regression test.
   *
   * @return		the filenames
   */
  protected abstract String[][] getRegressionInputFiles();

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  protected abstract AbstractCleaner[] getRegressionSetups();

  /**
   * Creates an output filename based on the input filename.
   *
   * @param input	the input filename (no path)
   * @param noSetup	the number of the setup
   * @param noFile	the number of the file
   * @return		the generated output filename (no path)
   */
  protected String createOutputFilename(String input, int noSetup, int noFile) {
    String	result;
    int		index;
    String	ext;

    ext = "-out" + noSetup + "_" + noFile;

    index = input.lastIndexOf('.');
    if (index == -1) {
      result = input + ext;
    }
    else {
      result  = input.substring(0, index);
      result += ext;
      result += input.substring(index);
    }

    return result;
  }

  /**
   * Compares the processed data against previously saved output data.
   */
  public void testRegression() {
    Spectrum		data;
    String		processed;
    boolean		ok;
    String		regression;
    int			i;
    int			n;
    String[][]		input;
    AbstractCleaner[]	setups;
    String[][]		output;
    List<TmpFile> 	outputFiles;

    if (m_NoRegressionTest)
      return;

    input   = getRegressionInputFiles();
    output  = new String[input.length][];
    setups  = getRegressionSetups();
    assertEquals("Number of files and setups differ!", input.length, setups.length);

    // process data
    for (i = 0; i < input.length; i++) {
      output[i] = new String[input[i].length];

      for (n = 0; n < input[i].length; n++) {
	m_TestHelper.copyResourceToTmp(input[i][n]);

	data = load(input[i][n]);
	assertNotNull("Could not load data for regression test from " + input[i][n], data);

	if (setups[i] instanceof DatabaseConnectionHandler)
	  ((DatabaseConnectionHandler) setups[i]).setDatabaseConnection(getDatabaseConnection());

	processed    = process(data, setups[i]);
	output[i][n] = createOutputFilename(input[i][n], i, n);
	ok           = save((processed == null ? "passed" : processed), output[i][n]);
	assertTrue("Failed to save regression data?", ok);

	m_TestHelper.deleteFileFromTmp(input[i][n]);
      }
    }

    // test regression
    outputFiles = new ArrayList<>();
    for (i = 0; i < output.length; i++) {
      for (n = 0; n < output[i].length; n++)
	outputFiles.add(new TmpFile(output[i][n]));
    }
    regression = m_Regression.compare(outputFiles.toArray(new TmpFile[0]));
    assertNull("Output differs:\n" + regression, regression);

    // remove output, clean up scheme
    for (i = 0; i < output.length; i++) {
      if (setups[i] instanceof Destroyable)
	((Destroyable) setups[i]).destroy();
      else if (setups[i] instanceof CleanUpHandler)
	((CleanUpHandler) setups[i]).cleanUp();
      for (n = 0; n < output[i].length; n++)
	m_TestHelper.deleteFileFromTmp(output[i][n]);
    }
    cleanUpAfterRegression();
  }

  /**
   * For further cleaning up after the regression tests.
   * <br><br>
   * Default implementation does nothing.
   */
  protected void cleanUpAfterRegression() {
  }
}
