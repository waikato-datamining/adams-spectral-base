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
 * CorrelationCoefficient.java
 * Copyright (C) 2008-2014 University of Waikato, Hamilton, New Zealand
 */

package adams.data.statistics;

/**
 <!-- globalinfo-start -->
 * Computes the correlation coefficient.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 *         If set to true, statistic class may output additional info to the
 *         console.
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4314 $
 */
public class CorrelationCoefficient
  extends CorrelationStatistic {

  /** for serialization. */
  private static final long serialVersionUID = 8181275932224042155L;

  /**
   * Correlation-coefficient-specific correlation container object.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 4314 $
   */
  public static class Coefficient
    extends CorrelationStatistic.Correlation {

    /** for serialization. */
    private static final long serialVersionUID = 2804965917380956839L;

    public static final String CORRELATION = "Correlation";

    /** the correlation. */
    protected double m_Correlation;

    /**
     * Initializes the correlation.
     *
     * @param owner	the statistics object this correlation belongs to
     */
    public Coefficient(CorrelationStatistic owner) {
      super(owner);
      setCorrelation(Double.NaN);
    }

    /**
     * Returns a string describing the object.
     *
     * @return 			a description suitable for displaying in the gui
     */
    @Override
    public String globalInfo() {
      return "Computes the correlation coefficient.";
    }

    /**
     * Sets the correlation.
     *
     * @param value	the correlation
     */
    @Override
    public void setCorrelation(Object value) {
      m_Correlation = (Double) value;
      m_Calculated  = false;
    }

    /**
     * Returns the correlation.
     *
     * @return		the correlation
     */
    @Override
    public Object getCorrelation() {
      return m_Correlation;
    }

    /**
     * Returns the normalized correlation. Just returns the correlation.
     *
     * @return		the normalized correlation
     */
    @Override
    public Object getNormalizedCorrelation() {
      return getCorrelation();
    }

    /**
     * Returns a description for this statistic.
     *
     * @return		the description
     */
    public String getStatisticDescription() {
      return "Correlation-coefficient";
    }

    /**
     * Re-calculates the statistics.
     */
    @Override
    protected void calculate() {
      super.calculate();

      add(CORRELATION, m_Correlation);
    }
  }

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Computes the correlation coefficient.";
    }

  /**
   * Computes the correlation between the two data vectors and returns it.
   *
   * @param y1	the first data array
   * @param y2	the second data array
   * @return		the computed correlation
   */
  @Override
  public Correlation getCorrelation(double[] y1, double[] y2) {
    Coefficient		result;
    double		c;

    result = new Coefficient(this);
    c      = StatUtils.correlationCoefficient(y1, y2);

    getLogger().info("Correlation: " + c);

    result.setCorrelation(c);

    return result;
  }

  /**
   * Returns whether a normalized correlation, in the range [0..1], is
   * available.
   *
   * @return		always true
   */
  @Override
  public boolean hasNormalizedCorrelation() {
    return true;
  }
}
