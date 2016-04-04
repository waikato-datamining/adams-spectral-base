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
 * EquiDistance.java
 * Copyright (C) 2009-2010 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractEquiDistanceWithOffset;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spectrum.SpectrumPointComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * A filter for interpolating the amplitudes of a spectrum. One can either specify a fixed number of points or just use the same amount of points as currently in the input data.
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
 * <pre>-num-points &lt;int&gt; (property: numPoints)
 * &nbsp;&nbsp;&nbsp;The number of points to generate, '-1' will use the same amount of points
 * &nbsp;&nbsp;&nbsp;as currently in the input data.
 * &nbsp;&nbsp;&nbsp;default: -1
 * </pre>
 *
 * <pre>-offset &lt;int&gt; (property: offset)
 * &nbsp;&nbsp;&nbsp;Offset to add to wave number, ignored if -1.
 * &nbsp;&nbsp;&nbsp;default: -1
 * </pre>
 *
 * <pre>-allow-oversampling (property: allowOversampling)
 * &nbsp;&nbsp;&nbsp;If set to true, then over-sampling is allowed, ie, generating more data
 * &nbsp;&nbsp;&nbsp;points than in the original data.
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class EquiDistance
  extends AbstractEquiDistanceWithOffset<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -17911247313401753L;

  /**
   * Returns a string describing the object.
   *
   * @return 		a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return
        "A filter for interpolating the amplitudes of a spectrum. "
      + "One can either specify a fixed number of points or just use the "
      + "same amount of points as currently in the input data.";
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the gui
   */
  public String offsetTipText() {
    return "Offset to add to wave number, ignored if -1.";
  }

  /**
   * Returns a SpectrumPoint with interpolated amplitude.
   *
   * @param waveno	the wave number we have to interpolate for
   * @param left	the "earlier" SpectrumPoint
   * @param right	the "later" SpectrumPoint
   * @return		the interpolated SpectrumPoint
   */
  protected SpectrumPoint interpolate(float waveno, SpectrumPoint left, SpectrumPoint right) {
    SpectrumPoint	result;
    float		wavenodiff;
    float		percLeft;
    float		percRight;

    wavenodiff = right.getWaveNumber() - left.getWaveNumber();
    percLeft   = 1.0f - ((float) (waveno - left.getWaveNumber()) / wavenodiff);
    percRight  = 1.0f - ((float) (right.getWaveNumber() - waveno) / wavenodiff);
    result     = new SpectrumPoint(
			waveno,
			      (float) left.getAmplitude()*percLeft
			    + (float) right.getAmplitude()*percRight);

    return result;
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  protected Spectrum processData(Spectrum data) {
    Spectrum			result;
    int				actualPoints;
    double			averageSpacing;
    float			waveno;
    float			wavenoStart;
    float			amplitude;
    int				i;
    List<SpectrumPoint>		orderedData;
    int				index;
    SpectrumPointComparator	comp;
    int 			sn;
    Spectrum			tmpData;
    ArrayList<Integer>		exact;
    SpectrumPoint		newPoint;

    tmpData     = data;
    orderedData = tmpData.toList();
    result      = (Spectrum) tmpData.getHeader();

    // determine actual number of points to generate
    if (m_NumPoints == -1) {
      actualPoints = orderedData.size();
    }
    else {
      actualPoints = m_NumPoints;
      if (!m_AllowOversampling && (actualPoints > orderedData.size()))
	actualPoints = orderedData.size();
    }

    // the average spacing between points
    averageSpacing = orderedData.get(orderedData.size() - 1).getWaveNumber() - orderedData.get(0).getWaveNumber();
    averageSpacing /= (actualPoints - 1);

    // initialize output data
    result.add((SpectrumPoint) orderedData.get(0).getClone());
    result.add((SpectrumPoint) orderedData.get(orderedData.size() - 1).getClone());

    // interpolate data (excluding first/last)
    exact       = new ArrayList<Integer>();
    comp        = new SpectrumPointComparator();
    wavenoStart = orderedData.get(0).getWaveNumber();
    for (i = 1; i < actualPoints - 1; i++) {
      waveno = (float)((double) wavenoStart + (double) i * averageSpacing);
      index  = Collections.binarySearch(orderedData, new SpectrumPoint(waveno, 0), comp);
      if (index >= 0) {
	exact.add(result.size());
	result.add((SpectrumPoint) orderedData.get(index).getClone());  // gets post-processed
      }
      else {
	result.add(
	    interpolate(
		waveno,
		orderedData.get(-index - 2),
		orderedData.get(-index - 1)));
      }
    }

    // post-process exact hits, using interpolated points either side
    orderedData = result.toList();
    for (i = 0; i < exact.size(); i++) {
      index = exact.get(i);
      if (index < orderedData.size() - 1)
	newPoint = interpolate(
	    orderedData.get(index).getWaveNumber(),
	    orderedData.get(index - 1),
	    orderedData.get(index + 1));
      else
	newPoint = interpolate(
	    orderedData.get(index).getWaveNumber(),
	    orderedData.get(index - 1),
	    orderedData.get(index));
      amplitude = (newPoint.getAmplitude() + orderedData.get(index).getAmplitude()) / 2;
      orderedData.get(index).setAmplitude(amplitude);
    }

    if (m_Offset > -1) {
      sn = 1;
      for (SpectrumPoint p: result.toList()) {
	p.setWaveNumber(getOffset() + sn++);
      }
    }

    return result;
  }
}
