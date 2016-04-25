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
 * AbstractStandardiseFilter.java
 * Copyright (C) 2016 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.List;

/**
 * Ancestor for filters that standardize by interpolation.
 *
 * @author  Michael Fowke
 * @version $Revision$
 */
public abstract class AbstractStandardiseFilter
  extends AbstractFilter<Spectrum> {

  /** the first data point. */
  protected double m_First;

  /** the last data point. */
  protected double m_Last;

  /** the step. */
  protected double m_Step;

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "first", "first",
      600.0, 0.0, null);

    m_OptionManager.add(
      "last", "last",
      4000.0, 0.0, null);

    m_OptionManager.add(
      "step", "step",
      2.0, 0.0001, null);
  }

  /**
   * Sets the start data point
   *
   * @param value	the maximum
   */
  public void setFirst(double value) {
    if (getOptionManager().isValid("first", value)) {
      m_First = value;
      reset();
    }
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
    if (getOptionManager().isValid("last", value)) {
      m_Last = value;
      reset();
    }
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
    return "The last data point.";
  }
  /**
   * Sets the step
   *
   * @param value	the maximum
   */
  public void setStep(double value) {
    if (getOptionManager().isValid("step", value)) {
      m_Step = value;
      reset();
    }
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

  protected double L(double x, List<SpectrumPoint> lsp, int m){
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

  protected double interp(double int_point, List<SpectrumPoint> lsp, int poly){
    double res=0;
    for (int L=0;L<=poly;L++){
      res+=L(int_point,lsp,L)*lsp.get(L).getAmplitude();
    }
    return(res);
  }
}
