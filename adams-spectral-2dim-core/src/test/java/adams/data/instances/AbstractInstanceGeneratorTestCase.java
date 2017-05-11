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
 * AbstractCleanerTestCase.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */
package adams.data.instances;

import adams.core.io.FileUtils;
import adams.data.AbstractDataProcessorTestCase;
import adams.data.spectrum.Spectrum;
import adams.test.AbstractTestHelper;
import adams.test.SpectralTestHelper;
import adams.test.TmpFile;
import weka.core.Instance;

/**
 * Ancestor for test cases tailored for instance generators.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public abstract class AbstractInstanceGeneratorTestCase
  extends AbstractDataProcessorTestCase<AbstractInstanceGenerator, Spectrum, Instance> {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public AbstractInstanceGeneratorTestCase(String name) {
    super(name);
  }

  /**
   * Returns the name of the database properties file to use.
   *
   * @return		the filename
   */
  protected String getDatabasePropertiesFile() {
    return "adams/test/SpectralTestDatabase.props";
  }

  /**
   * Returns the test helper class to use.
   *
   * @return		the helper class instance
   */
  protected AbstractTestHelper newTestHelper() {
    return new SpectralTestHelper(this, "adams/data/instances/data");
  }

  /**
   * Processes the input data and returns the processed data.
   *
   * @param data	the data to work on
   * @param scheme	the scheme to process the data with
   * @return		the processed data
   */
  protected Instance process(Spectrum data, AbstractInstanceGenerator scheme) {
    return scheme.generate(data);
  }

  /**
   * Saves the data in the tmp directory.
   *
   * @param data	the data to save
   * @param filename	the filename to save to (without path)
   * @return		true if successfully saved
   */
  protected boolean save(Instance data, String filename) {
    return FileUtils.writeToFile(new TmpFile(filename).getAbsolutePath(), data, false);
  }

  /**
   * Returns the ignored line indices to use in the regression test.
   *
   * @return		the setups
   */
  protected int[] getRegressionIgnoredLineIndices() {
    return new int[0];
  }

  /**
   * Returns the filenames (without path) of the input data files to use
   * in the regression test.
   *
   * @return		the filenames
   */
  protected abstract String[] getRegressionInputFiles();

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  protected abstract AbstractInstanceGenerator[] getRegressionSetups();
}
