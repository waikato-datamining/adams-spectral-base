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
 * Swapped.java
 * Copyright (C) 2012-2013 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumoutlier;

import adams.core.Utils;
import adams.data.outlier.AbstractOutlierDetector;
import adams.data.spectrumfilter.SavitzkyGolay;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Smooths spectrum (using SavitzkyGolay) and then splits it into two halves. Compares the amplitudes in the two halves, expecting the ones in the left half to be larger. If this is not the case, the spectrum gets flagged as outlier.<br>
 * It is possible to flip left and right half.
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
 * <pre>-flip (property: flip)
 * &nbsp;&nbsp;&nbsp;If enabled, the left and right half get swapped for the comparison.
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class Swapped
  extends AbstractOutlierDetector<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -1701525249098341015L;
  
  /** whether to flip the halves. */
  protected boolean m_Flip;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return 
	"Smooths spectrum (using SavitzkyGolay) and then splits it into two "
	+ "halves. Compares the amplitudes in the two halves, expecting the "
	+ "ones in the left half to be larger. If this is not the case, the "
	+ "spectrum gets flagged as outlier.\n"
	+ "It is possible to flip left and right half.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "flip", "flip",
	    false);
  }

  /**
   * Sets whether to swap left and right half for the comparison.
   *
   * @param value	if true the halves get swapped
   */
  public void setFlip(boolean value){
    m_Flip = value;
    reset();
  }

  /**
   * Returns whether to swap left and right half for the comparison.
   *
   * @return 		true if the halves get swapped
   */
  public boolean getFlip(){
    return m_Flip;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return         tip text for this property suitable for
   *             displaying in the GUI or for listing the options.
   */
  public String flipTipText(){
    return "If enabled, the left and right half get swapped for the comparison.";
  }

  /**
   * Performs the actual detection.
   *
   * @param data	the data to process
   * @return		the detections
   */
  @Override
  protected List<String> processData(Spectrum data) {
    List<String>	result;
    String		msg;
    SavitzkyGolay	smooth;
    Spectrum[]		halves;
    int			i;
    int			middle;
    float[]		ampl;

    result = new ArrayList<String>();

    // smooth data
    smooth = new SavitzkyGolay();
    data   = smooth.filter(data);

    // split into halves
    halves    = new Spectrum[2];
    halves[0] = data.getHeader();
    halves[1] = data.getHeader();
    middle    = data.size() / 2;
    for (i = 0; i < data.size(); i++) {
      if (i < middle)
	halves[0].add((SpectrumPoint) data.toList().get(i).getClone());
      else
	halves[1].add((SpectrumPoint) data.toList().get(i).getClone());
    }
    
    if (m_Flip)
      Utils.swap(halves);

    // check amplitudes
    ampl = new float[2];
    for (i = 0; i < 2; i++)
      ampl[i] = halves[i].getMaxAmplitude().getAmplitude() - halves[i].getMinAmplitude().getAmplitude();
    if (ampl[1] > ampl[0]) {
      if (m_Flip)
	msg = "Amplitudes in left half larger than in right one.";
      else
	msg = "Amplitudes in right half larger than in left one.";
      result.add(msg);
      if (isLoggingEnabled())
	getLogger().info(data.getDatabaseID() + " - " + getClass().getName() + ": " + msg);
    }

    return result;
  }
}
