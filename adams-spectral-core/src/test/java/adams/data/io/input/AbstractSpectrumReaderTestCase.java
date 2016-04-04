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
 * AbstractSpectrumReaderTest.java
 * Copyright (C) 2010-2015 University of Waikato, Hamilton, New Zealand
 */
package adams.data.io.input;

import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.test.AbstractTestHelper;
import adams.test.SpectralTestHelper;

import java.util.Date;
import java.util.List;

/**
 * Abstract test class for the spectrum readers.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2241 $
 * @param <A> the type of reader
 * @param <D> the type of data container
 */
public abstract class AbstractSpectrumReaderTestCase<A extends AbstractDataContainerReader, D extends Spectrum>
  extends AbstractDataContainerReaderTestCase<A, D> {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public AbstractSpectrumReaderTestCase(String name) {
    super(name);
  }

  /**
   * Returns the test helper class to use.
   *
   * @return		the helper class instance
   */
  @Override
  protected AbstractTestHelper newTestHelper() {
    return new SpectralTestHelper(this, "adams/data/io/input/data");
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
   * Returns the ignored line indices to use in the regression test.
   *
   * @return		the setups
   */
  @Override
  protected int[] getRegressionIgnoredLineIndices() {
    return new int[]{0};
  }

  /**
   * Loads the data to process. Overrides the insert timestamp in the report.
   *
   * @param filename	the filename to load (without path)
   * @param reader	the reader to use
   * @return		the data, null if it could not be loaded
   * @see		SampleData#INSERT_TIMESTAMP
   */
  @Override
  protected List<D> load(String filename, A reader) {
    List<D>	result;
    int		i;
    Date	date;

    if (reader instanceof AbstractSpectrumReader)
      ((AbstractSpectrumReader) reader).setUseAbsoluteSource(false);
    result = (List<D>) super.load(filename, reader);
    date   = new Date(0);
    for (i = 0; i < result.size(); i++) {
      ((Spectrum) result.get(i)).getReport().setValue(
	  new Field(SampleData.INSERT_TIMESTAMP, DataType.STRING), date);
    }

    return result;
  }
}
