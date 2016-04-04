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
 * AbstractSpectrumSmootherTestCase.java
 * Copyright (C) 2010 University of Waikato, Hamilton, New Zealand
 */
package adams.data.spectrumsmoothing;

import adams.data.sampledata.SampleData;
import adams.data.smoothing.AbstractSmoother;
import adams.data.spectrum.Spectrum;
import adams.test.AbstractTestHelper;
import adams.test.SpectralTestHelper;

import java.util.Date;

/**
 * Ancestor for smoother scheme test cases.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1999 $
 */
public abstract class AbstractSpectrumSmootherTestCase
  extends adams.data.smoothing.AbstractSmootherTestCase<AbstractSmoother, Spectrum> {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public AbstractSpectrumSmootherTestCase(String name) {
    super(name);
  }

  /**
   * Returns the name of the database properties file to use.
   *
   * @return		the filename
   */
  @Override
  protected String getDatabasePropertiesFile() {
    return "adams/test/SpectralTestDatabase.props";
  }

  /**
   * Returns the test helper class to use.
   *
   * @return		the helper class instance
   */
  @Override
  protected AbstractTestHelper newTestHelper() {
    return new SpectralTestHelper(this, "adams/data/spectrumsmoothing/data");
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
