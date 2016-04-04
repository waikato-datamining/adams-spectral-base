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
 * AbstractSampleDataWriterTestCase.java
 * Copyright (C) 2010-2013 University of Waikato, Hamilton, New Zealand
 */
package adams.data.io.output;

import adams.data.io.input.SimpleSampleDataReader;
import adams.data.report.Report;
import adams.data.sampledata.SampleData;
import adams.test.AbstractTestHelper;
import adams.test.SpectralTestHelper;
import adams.test.TmpFile;

/**
 * Abstract test class for the sample data readers.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1970 $
 * @param <A> the type of reader
 * @param <D> the type of report
 */
public abstract class AbstractSampleDataWriterTestCase<A extends AbstractReportWriter, D extends SampleData>
  extends AbstractReportWriterTestCase<A, D> {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public AbstractSampleDataWriterTestCase(String name) {
    super(name);
  }

  /**
   * Returns the test helper class to use.
   *
   * @return		the helper class instance
   */
  @Override
  protected AbstractTestHelper newTestHelper() {
    return new SpectralTestHelper(this, "adams/data/io/output/data");
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
    return new int[0];
  }

  /**
   * Loads the data to process.
   *
   * @param filename	the filename to load (without path)
   * @return		the data, null if it could not be loaded
   */
  @Override
  protected D load(String filename) {
    D				result;
    SimpleSampleDataReader	reader;
    Report			report;

    m_TestHelper.copyResourceToTmp(filename);
    reader = new SimpleSampleDataReader();
    reader.setInput(new TmpFile(filename));
    report = (Report) reader.read().get(0);
    result = (D) new SampleData();
    result.assign(report);
    m_TestHelper.deleteFileFromTmp(filename);

    return result;
  }
}
