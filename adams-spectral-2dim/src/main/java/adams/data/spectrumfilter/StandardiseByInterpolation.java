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
 * StandardiseByInterpolation.java
 * Copyright (C) 2015-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spectrum.SpectrumUtils;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Standardises spectrum to start-end with given step.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-no-id-update &lt;boolean&gt; (property: dontUpdateID)
 * &nbsp;&nbsp;&nbsp;If enabled, suppresses updating the ID of adams.data.id.IDHandler data containers.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-first &lt;double&gt; (property: first)
 * &nbsp;&nbsp;&nbsp;Starting data point.
 * &nbsp;&nbsp;&nbsp;default: 600.0
 * </pre>
 *
 * <pre>-last &lt;double&gt; (property: last)
 * &nbsp;&nbsp;&nbsp;The last data point.
 * &nbsp;&nbsp;&nbsp;default: 4000.0
 * </pre>
 *
 * <pre>-step &lt;double&gt; (property: step)
 * &nbsp;&nbsp;&nbsp;Step size.
 * &nbsp;&nbsp;&nbsp;default: 2.0
 * </pre>
 *
 * <pre>-polynomial &lt;int&gt; (property: polynomial)
 * &nbsp;&nbsp;&nbsp;The polynomial for interpolation.
 * &nbsp;&nbsp;&nbsp;default: 2
 * </pre>
 *
 <!-- options-end -->
 *
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2419 $
 */
public class StandardiseByInterpolation
  extends AbstractStandardiseFilter {

  protected int m_Polynomial;

  /**
   * Returns a string describing the object.
   *
   * @return 		a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return "Standardises spectrum to start-end with given step.";
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "polynomial", "polynomial",
      2, 2, null);
  }


  /**
   * Sets the polynomial for interpolation
   *
   * @param value	polynomial
   */
  public void setPolynomial(int value) {
    if (getOptionManager().isValid("polynomial", value)) {
      m_Polynomial = value;
      reset();
    }
  }

  /**
   * Returns the polynomial for interpolation
   *
   * @return		the minimum
   */
  public int getPolynomial() {
    return m_Polynomial;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the gui
   */
  public String polynomialTipText() {
    return "The polynomial for interpolation.";
  }

  protected List<SpectrumPoint> getClosestPoints(double waveno, List<SpectrumPoint> data, int numpoints){
    Spectrum sp=new Spectrum();
    int found=0;
    int pos= SpectrumUtils.findClosestWaveNumber(data, (float)waveno);
    sp.add((SpectrumPoint)data.get(pos).getClone());
    int foundmin=pos,foundmax=pos;
    found++;


    while (found < numpoints) {
      double minusposdiff=Double.MAX_VALUE;
      double plusposdiff=Double.MAX_VALUE;
      if (foundmin -1 > 0) {
	minusposdiff=Math.abs(waveno-data.get(foundmin-1).getWaveNumber());
      }
      if ( foundmax+1 < data.size()){
	plusposdiff=Math.abs(waveno-data.get(foundmax+1).getWaveNumber());
      }
      if (minusposdiff < plusposdiff){
	sp.add((SpectrumPoint)data.get(foundmin-1).getClone());
	foundmin--;
      } else {
	sp.add((SpectrumPoint)data.get(foundmax+1).getClone());
	foundmax++;
      }
      found++;
    }

    return(sp.toList());
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  protected Spectrum processData(Spectrum data) {
    Spectrum		result;
    List<SpectrumPoint>	list;

    if (m_Last < m_First)
      throw new IllegalStateException("last < first!");

    list = data.toList();
    result=data.getHeader();
    double int_point=m_First;
    boolean cont=true;

    while (cont){
      List<SpectrumPoint> lsp=getClosestPoints(int_point,list,getPolynomial()+1);
      double amp=interp(int_point,lsp,lsp.size()-1);
      result.add(new SpectrumPoint((float)int_point,(float)amp));
      int_point+=getStep();
      if (int_point > m_Last +.0001){
	break;
      }
    }
    return result;
  }
}
