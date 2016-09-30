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
 * PLS.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumanalysis;

import adams.data.spreadsheet.SpreadSheet;
import weka.core.Instances;

/**
 <!-- globalinfo-start -->
 * Performs partial least squares analysis.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-generator &lt;adams.data.instances.AbstractInstanceGenerator&gt; (property: generator)
 * &nbsp;&nbsp;&nbsp;The generator to use for generating Weka data from the spectra.
 * &nbsp;&nbsp;&nbsp;default: adams.data.instances.SimpleInstanceGenerator
 * </pre>
 * 
 * <pre>-pls &lt;adams.data.instancesanalysis.PLS&gt; (property: PLS)
 * &nbsp;&nbsp;&nbsp;The PLS analysis setup to use.
 * &nbsp;&nbsp;&nbsp;default: adams.data.instancesanalysis.PLS
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class PLS
  extends AbstractWekaSpectrumAnalysis {

  private static final long serialVersionUID = 5953885738893607037L;

  /** the PLS analysis to use. */
  protected adams.data.instancesanalysis.PLS m_PLS;

  /** the loadings. */
  protected SpreadSheet m_Loadings;

  /** the scores. */
  protected SpreadSheet m_Scores;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Performs partial least squares analysis.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "pls", "PLS",
      new adams.data.instancesanalysis.PLS());
  }

  /**
   * Sets the PLS analysis.
   *
   * @param value	the analysis
   */
  public void setPLS(adams.data.instancesanalysis.PLS value) {
    m_PLS = value;
    reset();
  }

  /**
   * Returns the PLS analysis.
   *
   * @return		the analysis
   */
  public adams.data.instancesanalysis.PLS getPLS() {
    return m_PLS;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String PLSTipText() {
    return "The PLS analysis setup to use.";
  }

  /**
   * Performs the actual analysis.
   *
   * @param data	the data to analyze
   * @return		null if successful, otherwise error message
   * @throws Exception	if analysis fails
   */
  @Override
  protected String doAnalyze(Instances data) throws Exception {
    String	result;

    m_Loadings = null;
    m_Scores   = null;
    result     = m_PLS.analyze(data);
    if (result == null) {
      m_Loadings = m_PLS.getLoadings();
      m_Scores   = m_PLS.getScores();
    }

    return result;
  }

  /**
   * Returns the loadings.
   *
   * @return		the loadings, null if not available
   */
  public SpreadSheet getLoadings() {
    return m_Loadings;
  }

  /**
   * Returns the scores.
   *
   * @return		the scores, null if not available
   */
  public SpreadSheet getScores() {
    return m_Scores;
  }
}
