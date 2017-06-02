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
 * DurbinWatson.java
 * Copyright (C) 2008-2013 University of Waikato, Hamilton, New Zealand
 */

package adams.data.statistics;

import adams.core.TechnicalInformation;
import adams.core.TechnicalInformation.Field;
import adams.core.TechnicalInformation.Type;
import adams.core.TechnicalInformationHandler;

/**
 <!-- globalinfo-start -->
 * Computes the Durbin-Watson statistic.<br>
 * <br>
 * For more information see:<br>
 * <br>
 * G. Vivo-Truyols, J.R. Torres-Lapasio, A.M. van Nederkassel, Y. Vander Heyden, D.L. Massart (2005). Automatic program for peak detection and deconvolution of multi-overlapped chromatographic signals Part I: Peak detection. Journal of Chromatography A. 1096:133-145.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- technical-bibtex-start -->
 * BibTeX:
 * <pre>
 * &#64;article{Vivo-Truyols2005,
 *    author = {G. Vivo-Truyols and J.R. Torres-Lapasio and A.M. van Nederkassel and Y. Vander Heyden and D.L. Massart},
 *    journal = {Journal of Chromatography A},
 *    pages = {133-145},
 *    title = {Automatic program for peak detection and deconvolution of multi-overlapped chromatographic signals Part I: Peak detection},
 *    volume = {1096},
 *    year = {2005},
 *    HTTP = {http://dx.doi.org/10.1016/j.chroma.2005.03.072}
 * }
 * </pre>
 * <br><br>
 <!-- technical-bibtex-end -->
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
public class DurbinWatson
  extends CorrelationStatistic
  implements TechnicalInformationHandler {

  /** for serialization. */
  private static final long serialVersionUID = 3673550225818730827L;

  /**
   * Durbin-Watson-specific correlation container object.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 4314 $
   */
  public static class DurbinWatsonCorrelation
    extends CorrelationStatistic.Correlation {

    /** for serialization. */
    private static final long serialVersionUID = -46136898760957180L;

    public static final String CORRELATION = "Correlation";

    /** the correlation. */
    protected double m_Correlation;

    /**
     * Initializes the correlation.
     *
     * @param owner	the statistics object this correlation belongs to
     */
    public DurbinWatsonCorrelation(CorrelationStatistic owner) {
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
      return
          "Computes the Durbin-Watson statistic.\n\n"
        + "For more information see:\n\n"
        + new DurbinWatson().getTechnicalInformation().toString();
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
     * Returns the normalized correlation. This maps the [0..4] into [0..1]
     * as follows: <br><br>
     * <pre>
     *   [0..2] is normalized to [0..1]
     *   (2..4] is mapped into [0..1) as follows:
     *      abs(x - 4) / 2  with x from (2..4]
     * </pre>
     *
     * @return		the normalized correlation
     */
    @Override
    public Object getNormalizedCorrelation() {
      if (m_Correlation <= 2)
	return m_Correlation / 2;
      else
	return Math.abs(m_Correlation - 4) / 2;
    }

    /**
     * Returns a description for this statistic.
     *
     * @return		the description
     */
    public String getStatisticDescription() {
      return "Durbin-Watson";
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
    return
        "Computes the Durbin-Watson statistic.\n\n"
      + "For more information see:\n\n"
      + getTechnicalInformation().toString();
  }

  /**
   * Returns an instance of a TechnicalInformation object, containing
   * detailed information about the technical background of this class,
   * e.g., paper reference or book this class is based on.
   *
   * @return 		the technical information about this class
   */
  public TechnicalInformation getTechnicalInformation() {
    TechnicalInformation 	result;

    result = new TechnicalInformation(Type.ARTICLE);
    result.setValue(Field.AUTHOR, "G. Vivo-Truyols and J.R. Torres-Lapasio and A.M. van Nederkassel and Y. Vander Heyden and D.L. Massart");
    result.setValue(Field.TITLE, "Automatic program for peak detection and deconvolution of multi-overlapped chromatographic signals Part I: Peak detection");
    result.setValue(Field.JOURNAL, "Journal of Chromatography A");
    result.setValue(Field.VOLUME, "1096");
    result.setValue(Field.PAGES, "133-145");
    result.setValue(Field.YEAR, "2005");
    result.setValue(Field.HTTP, "http://dx.doi.org/10.1016/j.chroma.2005.03.072");

    return result;
  }

  /**
   * Computes the correlation between the two data arrays and returns it.
   *
   * @param data1	the first data array
   * @param data2	the second data array
   * @return		the computed correlation
   */
  @Override
  public Correlation getCorrelation(double[] data1, double[] data2) {
    DurbinWatsonCorrelation 	result;
    double			sumDenominator;
    double			sumNumerator;
    double			correlation;
    int				i;

    result = new DurbinWatsonCorrelation(this);

    // calculate correlation
    sumNumerator   = 0.0;
    sumDenominator = 0.0;
    for (i = 0; i < data1.length; i++) {
      if (i > 0)
	sumNumerator += Math.pow((data1[i] - data2[i]) - (data1[i - 1] - data2[i - 1]), 2);
      sumDenominator += Math.pow(data1[i] - data2[i], 2);
    }
    correlation = sumNumerator / sumDenominator
                  * ((double) data1.length) / ((double) data1.length - 1);

    getLogger().info(
	  sumNumerator + "/" + sumDenominator
	  + " * (" + data1.length + "/" + data1.length + " - 1)"
	  + " = " + correlation);

    result.setCorrelation(correlation);

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
