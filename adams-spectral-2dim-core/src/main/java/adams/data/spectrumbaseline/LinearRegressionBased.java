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
 * LinearRegressionBased.java
 * Copyright (C) 2009-2010 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumbaseline;

import adams.data.baseline.AbstractLinearRegressionBased;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * A simple linear regression based baseline correction scheme.<br>
 * Fits a line through the data using linear regression and then removes this line from the data to correct the baseline.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 * &nbsp;&nbsp;&nbsp;If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-ridge &lt;double&gt; (property: ridge)
 * &nbsp;&nbsp;&nbsp;The ridge parameter for linear regression.
 * &nbsp;&nbsp;&nbsp;default: 1.0E-8
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  dale (dale at cs dot waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class LinearRegressionBased
  extends AbstractLinearRegressionBased<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 4374702112834623262L;

  /**
   * Returns the dataset for linear regression.
   *
   * @param data	the original data
   * @return		the data
   */
  protected Instances getInstances(Spectrum data) {
    Instances			result;
    ArrayList<Attribute>	atts;
    int				i;
    List<SpectrumPoint>		points;
    double[]			vals;

    atts = new ArrayList<Attribute>();
    atts.add(new Attribute("x"));
    atts.add(new Attribute("y"));
    result = new Instances("linearregression", atts, data.size());
    result.setClassIndex(result.numAttributes() - 1);

    points = data.toList();
    for (i = 0; i < points.size(); i++) {
      vals = new double[]{points.get(i).getWaveNumber(), points.get(i).getAmplitude()};
      result.add(new DenseInstance(1.0, vals));
    }

    return result;
  }

  /**
   * Corrects the data with the given coefficients.
   *
   * @param data	the original data
   * @param coeff	the coefficients to use for correcting the data,
   * 			the last element is the offset
   * @return		the baseline corrected data
   */
  protected Spectrum correctData(Spectrum data, double[] coeff) {
    Spectrum		result;
    List<SpectrumPoint>	points;
    SpectrumPoint	newPoint;

    result = data.getHeader();
    points = data.toList();

    for (SpectrumPoint oldPoint: points) {
      newPoint = (SpectrumPoint) oldPoint.getClone();
      newPoint.setAmplitude((float) (newPoint.getAmplitude() - newPoint.getWaveNumber() * coeff[0] - coeff[coeff.length - 1]));
      result.add(newPoint);
    }

    return result;
  }

  /**
   * Generates fake data for the plotting the line.
   *
   * @param data	the original data
   * @param coeff	the coefficients to use for generating the line data,
   * 			the last element is the offset
   * @return		the fake data for the line
   */
  protected Spectrum generateLine(Spectrum data, double[] coeff) {
    Spectrum		result;
    List<SpectrumPoint>	points;
    SpectrumPoint	newPoint;

    result = data.getHeader();
    points = data.toList();

    for (SpectrumPoint oldPoint: points) {
      newPoint = (SpectrumPoint) oldPoint.getClone();
      newPoint.setAmplitude((float) (newPoint.getWaveNumber() * coeff[0] + coeff[coeff.length - 1]));
      result.add(newPoint);
    }

    return result;
  }
}
