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
 * AbstractBaselineCorrectionTestCase.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */
package adams.data.spectrumbaseline;

import adams.data.baseline.AbstractBaselineCorrection;
import adams.test.AbstractSpectralDatabaseTestCase;
import adams.test.AbstractTestHelper;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.test.SpectralTestHelper;

import java.util.Date;

/**
 * Ancestor for baseline correction scheme test cases.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1998 $
 */
public abstract class AbstractSpectrumBaselineCorrectionTestCase
  extends adams.data.baseline.AbstractBaselineCorrectionTestCase<AbstractBaselineCorrection, Spectrum> {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public AbstractSpectrumBaselineCorrectionTestCase(String name) {
    super(name);
  }

  /**
   * Returns the name of the database properties file to use.
   *
   * @return		the filename
   */
  protected String getDatabasePropertiesFile() {
    return AbstractSpectralDatabaseTestCase.TEST_DATABASE;
  }

  /**
   * Returns the test helper class to use.
   *
   * @return		the helper class instance
   */
  @Override
  protected AbstractTestHelper newTestHelper() {
    return new SpectralTestHelper(this, "adams/data/spectrumbaseline/data");
  }

  /**
   * Loads the data to process.
   *
   * @param filename	the filename to load (without path)
   * @return		the data, null if it could not be loaded
   */
  @Override
  protected Spectrum load(String filename) {
    Spectrum	result;
    
    result = super.load(filename);
    result.setID("Test");
    
    return result;
  }
  
  /**
   * Saves the data in the tmp directory.
   *
   * @param data	the data to save
   * @param filename	the filename to save to (without path)
   * @return		true if successfully saved
   */
  @Override
  protected boolean save(Spectrum data, String filename) {
    data.getReport().setStringValue(SampleData.INSERT_TIMESTAMP, new Date(0).toString());
    return super.save(data, filename);
  }
  
  /**
   * Returns the ignored line indices to use in the regression test.
   *
   * @return		the setups
   */
  @Override
  protected int[] getRegressionIgnoredLineIndices() {
    return new int[]{0};
  }
}
