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
 * AbstractMultiSpectrumFilterTestCase.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package adams.data.multifilter;

import adams.data.AbstractDataProcessorTestCase;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;
import adams.test.AbstractTestHelper;
import adams.test.MultiSpectrumTestHelper;

/**
 * Ancestor for filter test cases.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public abstract class AbstractMultiSpectrumFilterTestCase
  extends AbstractDataProcessorTestCase<AbstractMultiSpectrumFilter, MultiSpectrum, Spectrum> {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public AbstractMultiSpectrumFilterTestCase(String name) {
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
    return new MultiSpectrumTestHelper(this, "adams/data/multifilter/data");
  }

  /**
   * Processes the input data and returns the processed data.
   *
   * @param data	the data to work on
   * @param scheme	the scheme to process the data with
   * @return		the processed data
   */
  @Override
  protected Spectrum process(MultiSpectrum data, AbstractMultiSpectrumFilter scheme) {
    Spectrum	result;
    
    result = (Spectrum) scheme.filter(data);
    if ((result != null) && result.hasReport())
      result.getReport().removeValue(new Field(SampleData.INSERT_TIMESTAMP, DataType.STRING));
    
    return result;
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
