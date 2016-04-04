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
 * SpectraToMultiSpectrum.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package adams.data.conversion;

import adams.core.ClassCrossReference;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Generates a knir.data.spectrum.MultiSpectrum from the incoming array of knir.data.spectrum.Spectrum.<br>
 * If the first spectrum in the array is a report-only spectrum (ie no spectral data points), then this report is used as the 'global' report for the multi-spectrum. This spectrum's ID is also used as the multi-spectrum's ID.<br>
 * <br>
 * See also:<br>
 * knir.data.conversion.MultiSpectrumToSpectra
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SpectraToMultiSpectrum
  extends AbstractConversion
  implements ClassCrossReference {

  /** for serialization. */
  private static final long serialVersionUID = -3142325533710057331L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return 
	"Generates a " + MultiSpectrum.class.getName() 
	+ " from the incoming array of " + Spectrum.class.getName() + ".\n"
	+ "If the first spectrum in the array is a report-only spectrum "
	+ "(ie no spectral data points), then this report is used as the "
	+ "'global' report for the multi-spectrum. This spectrum's ID is "
	+ "also used as the multi-spectrum's ID.";
  }

  /**
   * Returns the class that is accepted as input.
   *
   * @return		the class
   */
  @Override
  public Class accepts() {
    return Spectrum[].class;
  }

  /**
   * Returns the class that is generated as output.
   *
   * @return		the class
   */
  @Override
  public Class generates() {
    return MultiSpectrum.class;
  }

  /**
   * Returns the cross-referenced classes.
   *
   * @return		the classes
   */
  public Class[] getClassCrossReferences() {
    return new Class[]{MultiSpectrumToSpectra.class};
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  @Override
  protected Object doConvert() throws Exception {
    MultiSpectrum	output;
    Spectrum[]		input;
    List<Spectrum>	specs;
    HashSet<String>	ids;
    boolean		checkIds;
    
    input    = (Spectrum[]) m_Input;
    specs    = new ArrayList<Spectrum>(Arrays.asList(input));
    output   = new MultiSpectrum();
    ids      = new HashSet<String>();
    checkIds = true;
    
    // report-only spectrum at pos 0? -> multi-spectrum report
    if ((specs.size() > 0) && (specs.get(0).size() == 0)) {
      output.setReport(specs.get(0).getReport());
      output.setID(specs.get(0).getID());
      specs.remove(0);
      checkIds = false;
    }
    
    for (Spectrum sp: specs) {
      if (sp.getID() != null)
	ids.add(sp.getID());
      output.add(sp);
    }
    
    // unique id?
    if (checkIds && (ids.size() == 1))
      output.setID(ids.iterator().next());
    
    return output;
  }
}
