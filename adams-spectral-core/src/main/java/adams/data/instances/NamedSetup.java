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
 * NamedSetup.java
 * Copyright (C) 2010-2013 University of Waikato, Hamilton, New Zealand
 */

package adams.data.instances;

import adams.data.spectrum.Spectrum;
import weka.core.Instance;

/**
 <!-- globalinfo-start -->
 * Applies an instance generator that is referenced via its global setup name (see 'NamedSetups').
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D &lt;int&gt; (property: debugLevel)
 * &nbsp;&nbsp;&nbsp;The greater the number the more additional info the scheme may output to
 * &nbsp;&nbsp;&nbsp;the console (0 = off).
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 *
 * <pre>-target &lt;adams.data.report.Field&gt; (property: target)
 * &nbsp;&nbsp;&nbsp;The sample-data field to act as class attribute.
 * &nbsp;&nbsp;&nbsp;default: moisture
 * </pre>
 *
 * <pre>-add-db-id (property: addDatabaseID)
 * &nbsp;&nbsp;&nbsp;If set to true, then the database ID will be added to the output.
 * </pre>
 *
 * <pre>-add-sample-id (property: addSampleID)
 * &nbsp;&nbsp;&nbsp;If set to true, then the sample ID will be added to the output.
 * </pre>
 *
 * <pre>-additional &lt;adams.data.report.Field&gt; [-additional ...] (property: additionalFields)
 * &nbsp;&nbsp;&nbsp;The additional sample data fields to add to the output.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-notes &lt;adams.core.base.BaseString&gt; [-notes ...] (property: notes)
 * &nbsp;&nbsp;&nbsp;The notes to add as attributes to the generated data, eg 'PROCESS INFORMATION'
 * &nbsp;&nbsp;&nbsp;.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-load-sample-data (property: loadSampleData)
 * &nbsp;&nbsp;&nbsp;If set to true, then the sample data will be loaded if only dummy report
 * &nbsp;&nbsp;&nbsp;available, using the sample ID.
 * </pre>
 *
 * <pre>-store (property: useStoreTable)
 * &nbsp;&nbsp;&nbsp;If set to true, then the sample data will get read from the store table,
 * &nbsp;&nbsp;&nbsp;otherwise the active one.
 * </pre>
 *
 * <pre>-setup &lt;java.lang.String&gt; (property: setup)
 * &nbsp;&nbsp;&nbsp;The name of the setup to use.
 * &nbsp;&nbsp;&nbsp;default: name_of_setup
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class NamedSetup
  extends AbstractSpectrumBasedInstanceGenerator {

  /** for serialization. */
  private static final long serialVersionUID = -4264077624361118629L;

  /** the name of the setup to load. */
  protected adams.core.NamedSetup m_Setup;

  /** the actual scheme. */
  protected AbstractSpectrumBasedInstanceGenerator m_ActualScheme;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Applies an instance generator that is referenced via its global setup name (see 'NamedSetups').";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "setup", "setup",
	    new adams.core.NamedSetup());
  }

  /**
   * Resets the filter.
   */
  @Override
  public void reset() {
    super.reset();

    m_ActualScheme = null;
  }

  /**
   * Sets the setup name.
   *
   * @param value	the name
   */
  public void setSetup(adams.core.NamedSetup value) {
    m_Setup = value;
    if (!m_Setup.isDummy() && !m_Setup.exists())
      getLogger().severe("Warning: named setup '" + m_Setup + "' unknown!");
    reset();
  }

  /**
   * Returns the setup name.
   *
   * @return		the name
   */
  public adams.core.NamedSetup getSetup() {
    return m_Setup;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the gui
   */
  public String setupTipText() {
    return "The name of the setup to use.";
  }

  /**
   * Returns the named setup.
   *
   * @return		the actual scheme to use
   */
  protected AbstractSpectrumBasedInstanceGenerator getActualScheme() {
    if (m_ActualScheme == null) {
      m_ActualScheme = (AbstractSpectrumBasedInstanceGenerator) m_Setup.getSetup();
      if (m_ActualScheme == null)
	throw new IllegalStateException(
	    "Failed to instantiate named setup '" + m_Setup + "'!");
      m_ActualScheme.setDatabaseConnection(getDatabaseConnection());
    }

    return m_ActualScheme;
  }

  /**
   * Does nothing.
   *
   * @param data	the input data
   */
  @Override
  protected void checkHeader(Spectrum data) {
  }

  /**
   * Does nothing.
   *
   * @param data	the input data
   */
  @Override
  protected void generateHeader(Spectrum data) {
  }

  /**
   * Generates the actual data.
   *
   * @param data	the input data to transform
   * @return		the generated data
   */
  @Override
  protected Instance generateOutput(Spectrum data) {
    Instance	result;

    result         = getActualScheme().generate(data);
    m_OutputHeader = getActualScheme().getOutputHeader();

    return result;
  }
}
