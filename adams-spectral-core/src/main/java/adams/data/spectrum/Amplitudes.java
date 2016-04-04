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
 * Amplitudes.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package adams.data.spectrum;

import adams.data.featureconverter.HeaderDefinition;
import adams.data.report.DataType;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Simple feature generator that just outputs all the amplitudes of a spectrum.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-converter &lt;adams.data.featureconverter.AbstractFeatureConverter&gt; (property: converter)
 * &nbsp;&nbsp;&nbsp;The feature converter to use to produce the output data.
 * &nbsp;&nbsp;&nbsp;default: adams.data.featureconverter.SpreadSheetFeatureConverter -data-row-type adams.data.spreadsheet.DenseDataRow -spreadsheet-type adams.data.spreadsheet.SpreadSheet
 * </pre>
 * 
 * <pre>-add-database-id &lt;boolean&gt; (property: addDatabaseID)
 * &nbsp;&nbsp;&nbsp;If enabled, the database ID of the container gets added to the data.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-add-id &lt;boolean&gt; (property: addID)
 * &nbsp;&nbsp;&nbsp;If enabled, the ID of the container gets added to the data.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-field &lt;knir.data.sampledata.Field&gt; [-field ...] (property: fields)
 * &nbsp;&nbsp;&nbsp;The fields to add to the output.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-notes &lt;adams.core.base.BaseString&gt; [-notes ...] (property: notes)
 * &nbsp;&nbsp;&nbsp;The notes to add as attributes to the generated data, eg 'PROCESS INFORMATION'
 * &nbsp;&nbsp;&nbsp;.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-add-wave-number &lt;boolean&gt; (property: addWaveNumber)
 * &nbsp;&nbsp;&nbsp;If enabled, the wave number gets added as well, preceding the amplitude.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class Amplitudes
  extends AbstractSpectrumFeatureGenerator {

  /** for serialization. */
  private static final long serialVersionUID = 9084280445189495060L;

  /** whether to include the wavenumbers. */
  protected boolean m_AddWaveNumber;
  
  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Simple feature generator that just outputs all the amplitudes of a spectrum.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "add-wave-number", "addWaveNumber",
	    false);
  }
  
  /**
   * Sets whether to add the wave numbers as well (preceding the amplitude).
   *
   * @param value	true if to add wave numbers
   */
  public void setAddWaveNumber(boolean value) {
    m_AddWaveNumber = value;
    reset();
  }

  /**
   * Returns whether to add the wave numbers as well (preceding the amplitude).
   *
   * @return		true if to add wave numbers
   */
  public boolean getAddWaveNumber() {
    return m_AddWaveNumber;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String addWaveNumberTipText() {
    return "If enabled, the wave number gets added as well, preceding the amplitude.";
  }

  /**
   * Creates the header from a template spectrum.
   *
   * @param spectrum	the spectrum to act as a template
   * @return		the generated header
   */
  @Override
  public HeaderDefinition createHeader(Spectrum spectrum) {
    HeaderDefinition	result;
    int			i;
    
    result = new HeaderDefinition();
    for (i = 0; i < spectrum.size(); i++) {
      if (m_AddWaveNumber)
	result.add("WaveNumber-" + (i+1), DataType.STRING);
      result.add("Amplitude-" + (i+1), DataType.NUMERIC);
    }
    
    return result;
  }

  /**
   * Performs the actual feature genration.
   *
   * @param spectrum	the spectrum to process
   * @return		the generated features
   */
  @Override
  public List<Object>[] generateRows(Spectrum spectrum) {
    List<Object>[]	result;
    int			i;
    SpectrumPoint	point;
    
    result    = new ArrayList[1];
    result[0] = new ArrayList();
    for (i = 0; i < spectrum.size(); i++) {
      point = (SpectrumPoint) spectrum.toList().get(i);
      if (m_AddWaveNumber)
	result[0].add(point.getWaveNumber());
      result[0].add(point.getAmplitude());
    }
    
    return result;
  }
}
