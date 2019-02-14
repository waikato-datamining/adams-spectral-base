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
 * SpectraToMultiSpectrum.java
 * Copyright (C) 2014-2019 University of Waikato, Hamilton, New Zealand
 */
package adams.data.conversion;

import adams.core.ClassCrossReference;
import adams.core.ObjectCopyHelper;
import adams.data.spectrum.AbstractSpectrumComparator;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Generates a adams.data.spectrum.MultiSpectrum from the incoming array of adams.data.spectrum.Spectrum.<br>
 * If the first spectrum in the array is a report-only spectrum (ie no spectral data points), then this report is used as the 'global' report for the multi-spectrum. This spectrum's ID is also used as the multi-spectrum's ID.<br>
 * <br>
 * See also:<br>
 * adams.data.conversion.MultiSpectrumToSpectra
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-use-custom-comparator &lt;boolean&gt; (property: useCustomComparator)
 * &nbsp;&nbsp;&nbsp;If enabled, the specified custom comparator is used for sorting the spectra
 * &nbsp;&nbsp;&nbsp;in the generated MultiSpectrum.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-custom-comparator &lt;adams.data.spectrum.AbstractSpectrumComparator&gt; (property: customComparator)
 * &nbsp;&nbsp;&nbsp;The custom comparator to use for sorting the spectra in the generated MultiSpectrum.
 * &nbsp;&nbsp;&nbsp;default: adams.data.spectrum.SpectrumComparator
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpectraToMultiSpectrum
  extends AbstractConversion
  implements ClassCrossReference {

  /** for serialization. */
  private static final long serialVersionUID = -3142325533710057331L;

  /** whether to use a custom comparator. */
  protected boolean m_UseCustomComparator;

  /** the custom comparator to use. */
  protected AbstractSpectrumComparator m_CustomComparator;

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
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "use-custom-comparator", "useCustomComparator",
      false);

    m_OptionManager.add(
      "custom-comparator", "customComparator",
      new SpectrumComparator());
  }

  /**
   * Sets whether to use a custom comparator.
   *
   * @param value 	true if to use custom comparator
   */
  public void setUseCustomComparator(boolean value) {
    m_UseCustomComparator = value;
    reset();
  }

  /**
   * Returns whether to use a custom comparator.
   *
   * @return 		true if to use a custom comparator
   */
  public boolean getUseCustomComparator() {
    return m_UseCustomComparator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String useCustomComparatorTipText() {
    return "If enabled, the specified custom comparator is used for sorting the spectra in the generated MultiSpectrum.";
  }

  /**
   * Sets the custom comparator.
   *
   * @param value 	the custom comparator
   */
  public void setCustomComparator(AbstractSpectrumComparator value) {
    m_CustomComparator = value;
    reset();
  }

  /**
   * Returns the custom comparator.
   *
   * @return 		the custom comparator
   */
  public AbstractSpectrumComparator getCustomComparator() {
    return m_CustomComparator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String customComparatorTipText() {
    return "The custom comparator to use for sorting the spectra in the generated MultiSpectrum.";
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
    specs    = new ArrayList<>(Arrays.asList(input));
    output   = new MultiSpectrum();
    if (m_UseCustomComparator)
      output.setCustomComparator(ObjectCopyHelper.copyObject(m_CustomComparator));
    ids      = new HashSet<>();
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
