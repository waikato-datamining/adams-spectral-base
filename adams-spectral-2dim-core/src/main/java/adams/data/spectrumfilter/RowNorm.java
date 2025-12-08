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
 * RowNorm.java
 * Copyright (C) 2009-2014 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.statistics.StatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * A filter that returns only every n-th wave number.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-nth &lt;int&gt; (property: nthPoint)
 *         Only every n-th point will be output.
 *         default: 1
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class RowNorm
  extends AbstractFilter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 1836858988505886282L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Row wise normalisation.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  @Override
  protected Spectrum processData(Spectrum data) {
    Spectrum		result;
    List<SpectrumPoint>	points;
    List<SpectrumPoint>	pointsNew;
    double[]		x;
    double[]		norm;
    int			i;

    result    = data.getHeader();
    points    = data.toList();
    pointsNew = new ArrayList<>();
    x         = new double[points.size()];
    for (i = 0; i < points.size(); i++)
      x[i] = points.get(i).getAmplitude();
    norm   = StatUtils.rowNorm(x);
    for (i = 0; i < points.size(); i++)
      pointsNew.add(new SpectrumPoint(points.get(i).getWaveNumber(), (float) norm[i]));
    result.replaceAll(pointsNew, true);

    return result;
  }
}
