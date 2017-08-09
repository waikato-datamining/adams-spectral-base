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
 * ApplyMultiplicativeScatterCorrection.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumfilter.amplitudetransform;

import adams.core.base.BaseInterval;
import adams.data.report.AbstractField;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spectrumfilter.MultiplicativeScatterCorrection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 <!-- globalinfo-start -->
 * Applies multiplicative scatter correction using the intercept and slope values stores in the report (using prefixes Intercept. and Slope.).
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
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ApplyMultiplicativeScatterCorrection
  extends AbstractAmplitudeTransformer {

  private static final long serialVersionUID = 1342682133382785357L;

  /** the ranges. */
  protected List<BaseInterval> m_Ranges;

  /** the slopes. */
  protected Map<BaseInterval,Double> m_Slopes;

  /** the intercepts. */
  protected Map<BaseInterval,Double> m_Intercepts;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Applies multiplicative scatter correction using the intercept and slope "
      + "values stores in the report (using prefixes "
        + MultiplicativeScatterCorrection.PREFIX_INTERCEPT + " and " + MultiplicativeScatterCorrection.PREFIX_SLOPE + ").";
  }

  /**
   * Hook method for initializing the transformer.
   *
   * @param data	the spectrum to transform
   */
  protected void initialize(Spectrum data) {
    Field		slopeField;
    BaseInterval	range;

    m_Ranges     = new ArrayList<>();
    m_Slopes     = new HashMap<>();
    m_Intercepts = new HashMap<>();

    for (AbstractField interField : data.getReport().getFields()) {
      if (interField.getName().startsWith(MultiplicativeScatterCorrection.PREFIX_INTERCEPT)) {
        slopeField = new Field(interField.getName().replace(MultiplicativeScatterCorrection.PREFIX_INTERCEPT, MultiplicativeScatterCorrection.PREFIX_SLOPE), DataType.NUMERIC);
        range      = new BaseInterval(interField.getName().replace(MultiplicativeScatterCorrection.PREFIX_INTERCEPT, ""));
        m_Ranges.add(range);
        m_Intercepts.put(range, data.getReport().getDoubleValue(interField));
        m_Slopes.put(range, data.getReport().getDoubleValue(slopeField));
      }
    }
  }

  /**
   * Transform the spectrum point and returns a new object.
   *
   * @param index	the 0-based index of the current point
   * @return		the new point
   */
  @Override
  protected SpectrumPoint transform(int index, SpectrumPoint point) {
    SpectrumPoint	result;
    int			i;
    double		inter;
    double		slope;

    result = point;

    for (i = 0; i < m_Ranges.size(); i++) {
      if (m_Ranges.get(i).isInside(point.getWaveNumber())) {
        inter  = m_Intercepts.get(m_Ranges.get(i));
        slope  = m_Slopes.get(m_Ranges.get(i));
	result = new SpectrumPoint(point.getWaveNumber(), (float) ((point.getAmplitude() - inter) / slope));
        break;
      }
    }

    return result;
  }
}
