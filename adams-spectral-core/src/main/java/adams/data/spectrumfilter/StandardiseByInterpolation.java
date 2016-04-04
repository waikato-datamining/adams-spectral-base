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
 * Scale.java
 * Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spectrum.SpectrumUtils;

import java.util.List;

/**
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public class StandardiseByInterpolation
  extends AbstractFilter<Spectrum> {

  /** for serialization. */

  protected double m_First;

  protected double m_Last;

  protected double m_Step;

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
	    "first", "first",
	    600.0);

    m_OptionManager.add(
	    "last", "last",
	    4000.0);

    m_OptionManager.add(
	    "step", "step",
	    2.0);

    m_OptionManager.add(
	    "polynomial", "polynomial",
	    2);
  }


  /**
   * Sets the polynomial for interpolation
   *
   * @param value	polynomial
   */
  public void setPolynomial(int value) {
    m_Polynomial= value;
    reset();
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


  /**
   * Sets the start data point
   *
   * @param value	the maximum
   */
  public void setFirst(double value) {
    m_First= value;
    reset();
  }

  /**
   * Returns the start data point
   *
   * @return		the minimum
   */
  public double getFirst() {
    return m_First;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the gui
   */
  public String firstTipText() {
    return "Starting data point.";
  }

  /**
   * Sets the last data point
   *
   * @param value	the maximum
   */
  public void setLast(double value) {
    m_Last = value;
    reset();
  }

  /**
   * Returns the last data point
   *
   * @return		the maximum
   */
  public double getLast() {
    return m_Last;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the gui
   */
  public String lastTipText() {
    return "The last datapoint.";
  }
  /**
   * Sets the step
   *
   * @param value	the maximum
   */
  public void setStep(double value) {
    m_Step = value;
    reset();
  }

  /**
   * Returns the step
   *
   * @return		the maximum
   */
  public double getStep() {
    return m_Step;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the gui
   */
  public String stepTipText() {
    return "Step size.";
  }

  protected List<SpectrumPoint> getClosestPoints(double waveno,List<SpectrumPoint> data, int numpoints){

    Spectrum sp=new Spectrum();
    int found=0;
    int pos=SpectrumUtils.findClosestWaveNumber(data, (float)waveno);
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

  protected double L(double x, List<SpectrumPoint> lsp,int m){
    double num=1;
    double den=1;
    for (int k=0;k<lsp.size();k++){
      if (k==m){
	continue;
      }
      num*=x-(double)lsp.get(k).getWaveNumber();
    }
    for (int k=0;k<lsp.size();k++){
      if (k==m){
	continue;
      }
      den*=(double)lsp.get(m).getWaveNumber()-(double)lsp.get(k).getWaveNumber();
    }
    return(num/den);
  }
  protected double interp(double int_point,List<SpectrumPoint> lsp, int poly){
    double res=0;
    for (int L=0;L<=poly;L++){
      res+=L(int_point,lsp,L)*lsp.get(L).getAmplitude();
    }
    return(res);
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
    double		min;
    double		max;
    double		scale;
    int			i;

    SpectrumPoint	pointNew;

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
