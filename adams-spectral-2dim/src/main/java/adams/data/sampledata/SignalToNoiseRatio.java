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
 * SignalToNoiseRatio.java
 * Copyright (C) 2013 University of Waikato, Hamilton, NZ
 */
package adams.data.sampledata;

import adams.data.report.AbstractReportFilter;
import adams.data.report.Report;
import adams.data.spectrumfilter.SavitzkyGolay;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import weka.core.Utils;

import java.util.List;

/**
 * @author dale
 *
 */
public class SignalToNoiseRatio extends AbstractReportFilter<Spectrum> {

  /** suid   */
  private static final long serialVersionUID = 319693711592096719L;
  
  /** smoothing window for savitsky golay */
  protected int m_Window=15;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "window", "windowSize",
	    15);
  }
  
  /**
   * Sets the window size for determining the 'smoothed' abundances.
   *
   * @param value	the window size
   */
  public void setWindowSize(int value) {
    if (value > 0) {
      m_Window = value;
      reset();
    }
    else {
      getLogger().severe(
	  this.getClass().getName() + ".windowSize: only positive numbers are allowed for window size!");
    }
  }

  /**
   * Returns the window size for determining the 'smoothed' abundances.
   *
   * @return		the window size
   */
  public int getWindowSize() {
    return m_Window;
  }
  
  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the gui
   */
  public String windowSizeTipText() {
    return "The window size for smoothing.";
  }
  
  /* (non-Javadoc)
   * @see adams.data.report.AbstractReportFilter#processData(adams.data.container.DataContainer)
   */
  @Override
  protected Spectrum processData(Spectrum data) {
    // TODO Auto-generated method stub
    
    Report r=data.getReport();

    r.setNumericValue("SNR_QUADRATIC", SNR_byParabolicRegression(data));
    r.setNumericValue("SNR_SMOOTHING", SNR_bySmoothing(data));
    r.setNumericValue("SNR_RMSENOISE", Noise_bySmoothing(data));
    return(data);
  }
  
  /**
   * Return the RMSE noise value, as a distance from the smoothed spectrum
   * 
   * @param data spectrum
   * @return RMSE noise
   */
  protected double Noise_bySmoothing(Spectrum data){
    SavitzkyGolay sg=new SavitzkyGolay();
    sg.setDerivativeOrder(0);
    sg.setNumPointsLeft(m_Window);
    sg.setNumPointsRight(m_Window);
    Spectrum ssg=sg.filter(data);
    //double sum=0;
    double sum_err=0;
    for (SpectrumPoint sp_ssg:ssg){
      SpectrumPoint sp_data=data.findClosest(sp_ssg.getWaveNumber());
      //sum+=sp_ssg.getAmplitude();
      double err=((double)sp_ssg.getAmplitude()-(double)sp_data.getAmplitude());
      sum_err+=(err*err);
    }
    double rmse=Math.sqrt(sum_err/(double)ssg.size());
    //double mean=sum/(double)ssg.size();
    return(rmse);
  }
  
  /**
   * Return the SNR measured from smoothed spectrum
   * 
   * @param data spectrum
   * @return SNR
   */
  protected double SNR_bySmoothing(Spectrum data){
    SavitzkyGolay sg=new SavitzkyGolay();
    sg.setDerivativeOrder(0);
    sg.setNumPointsLeft(m_Window);
    sg.setNumPointsRight(m_Window);
    Spectrum ssg=sg.filter(data);
    double sum=0;
    double sum_err=0;
    for (SpectrumPoint sp_ssg:ssg){
      SpectrumPoint sp_data=data.findClosest(sp_ssg.getWaveNumber());
      sum+=sp_ssg.getAmplitude();
      double err=((double)sp_ssg.getAmplitude()-(double)sp_data.getAmplitude());
      sum_err+=(err*err);
    }
    double rmse=Math.sqrt(sum_err/(double)ssg.size());
    double mean=sum/(double)ssg.size();
    return(mean/rmse);
  }
  
  /**
   * Return the SNR measured from quadratic approximation
   * 
   * @param data spectrum
   * @return SNR
   */
  protected double SNR_byParabolicRegression(Spectrum data){
    int n = 0;
    double sx = 0, sy = 0, sx2 = 0,  sx3 = 0, sx4 = 0, sxy = 0, sx2y = 0;
    
    Report r=data.getReport();
    List<SpectrumPoint> l=data.toList();
    double[] amps=new double[l.size()];
    int count=0;
    for (SpectrumPoint sp:l){
      amps[count++]=sp.getAmplitude();
      
      double x=sp.getWaveNumber();
      double y=sp.getAmplitude();
      n++;
      sx  += x;
      sx2 += x * x;
      sx3 += x * x * x;
      sx4 += x * x * x * x;
      sy  += y;
      sxy += x * y;
      sx2y += x * x * y;
    }
    double mean=Utils.mean(amps);
    double d = (sx4 * (sx2 * n - sx * sx) - sx3 * (sx3 * n - sx * sx2) + sx2 * (sx3 * sx - sx2 * sx2));
    
    double c = (sx2y * (sx2 * n - sx * sx) - sxy * (sx3 * n - sx * sx2) + sy * (sx3 * sx - sx2 * sx2)) / d;
    double b = (sx4 * (sxy * n - sy * sx) - sx3 * (sx2y * n - sy * sx2) + sx2 * (sx2y * sx - sxy * sx2)) / d;
    double a = (sx4 * (sx2 * sy - sx * sxy) - sx3 * (sx3 * sy - sx * sx2y) + sx2 * (sx3 * sxy - sx2 * sx2y)) / d;
    double ssErr=0;
    // calculate RMS 
    for (SpectrumPoint sp:l){
      double x=sp.getWaveNumber();
      double y=sp.getAmplitude();
      double err = y - (a + b*x + c*x*x);
      ssErr += err * err;
    }
    double rmse=Math.sqrt(ssErr/(double)n);
    return(mean/rmse);
  }
  

  /* (non-Javadoc)
   * @see adams.core.option.AbstractOptionHandler#globalInfo()
   */
  @Override
  public String globalInfo() {
    // TODO Auto-generated method stub
    return "Add the signal to noise ratio for the spectrum to the report.";
  }

}
