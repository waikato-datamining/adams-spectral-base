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
 * Covariance.java
 * Copyright (C) 2008-2019 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractDatabaseConnectionFilter;
import adams.data.filter.Filter;
import adams.data.filter.PassThrough;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.statistics.StatCalc;
import adams.db.AbstractConditions;
import adams.db.AbstractDatabaseConnection;
import adams.db.AbstractSpectrumConditions;
import adams.db.DatabaseConnection;
import adams.db.DatabaseConnectionHandler;
import adams.db.SampleDataF;
import adams.db.SpectrumConditionsMulti;
import adams.db.SpectrumF;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Plot covariance against Reference.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D &lt;int&gt; (property: debugLevel)
 * &nbsp;&nbsp;&nbsp;The greater the number the more additional info the scheme may output to
 * &nbsp;&nbsp;&nbsp;the console (0 = off).
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 *
 * <pre>-conditions &lt;adams.db.AbstractConditions [options]&gt; (property: conditions)
 * &nbsp;&nbsp;&nbsp;The conditions for retrieving the spectra from the database.
 * &nbsp;&nbsp;&nbsp;default: knir.db.SpectrumConditions
 * </pre>
 *
 * <pre>-field &lt;adams.data.report.Field&gt; (property: field)
 * &nbsp;&nbsp;&nbsp;Field to use for covariance.
 * &nbsp;&nbsp;&nbsp;default: blah
 * </pre>
 *
 * <pre>-scale (property: scale)
 * &nbsp;&nbsp;&nbsp;Scale?
 * </pre>
 *
 * <pre>-absolute (property: absolute)
 * &nbsp;&nbsp;&nbsp;Use absolute values of coefficients?
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class Covariance
  extends AbstractDatabaseConnectionFilter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -6824226595390587542L;

  /** the spectrum retrieval conditions. */
  protected AbstractSpectrumConditions m_Conditions;

  /** reference field. */
  protected Field m_Field;

  /** display absolute values. */
  protected boolean m_Scale;

  /** display absolute values. */
  protected boolean m_Absolute;

  /** filter to apply to retrieved spectra. */
  protected Filter<Spectrum> m_filter;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Plot covariance against Reference.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "conditions", "conditions",
	    new SpectrumConditionsMulti());

    m_OptionManager.add(
	    "field", "field",
	    new Field("blah", DataType.NUMERIC));

    m_OptionManager.add(
	    "filter", "filter",
	    new PassThrough());

    m_OptionManager.add(
	    "scale", "scale",
	    false);

    m_OptionManager.add(
	    "absolute", "absolute",
	    false);
  }

  /**
   * Returns the default database connection.
   *
   * @return		the default database connection
   */
  @Override
  protected AbstractDatabaseConnection getDefaultDatabaseConnection() {
    return DatabaseConnection.getSingleton();
  }

  /**
   * Set filter.
   *
   * @param filter	the filter to use
   */
  public void setFilter(Filter<Spectrum> filter) {
    m_filter=filter;
    updateDatabaseConnection();
    reset();
  }

  /**
   * Get filter.
   *
   * @return	the filter in use
   */
  public Filter<Spectrum> getFilter() {
    return(m_filter);
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String filterTipText() {
    return "pre-filter for spectra";
  }

  /**
   * Set scale?
   *
   * @param value	scale?
   */
  public void setScale(boolean value) {
    m_Scale = value;
    reset();
  }

  /**
   * Get scale?
   * @return	scale?
   */
  public boolean getScale() {
    return m_Scale;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String scaleTipText() {
    return "Scale?";
  }

  /**
   * Set absolute?
   * @param abs	absolute?
   */
  public void setAbsolute(boolean abs) {
    m_Absolute=abs;
    reset();
  }

  /**
   * Get absolute?
   * @return	absolute?
   */
  public boolean getAbsolute() {
    return(m_Absolute);
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String absoluteTipText() {
    return "Use absolute values of coefficients?";
  }

  /**
   * Set Field.
   * @param f field
   */
  public void setField(Field f) {
    m_Field=f;
    reset();
  }

  /**
   * get Field.
   * @return field
   */
  public Field getField() {
    return(m_Field);
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String fieldTipText() {
    return "Field to use for covariance.";
  }


  /**
   * Sets the conditions container to use for retrieving the spectra.
   *
   * @param value 	the conditions
   */
  public void setConditions(AbstractConditions value) {
    if (value instanceof AbstractSpectrumConditions) {
      m_Conditions = (AbstractSpectrumConditions) value;
      reset();
    }
    else {
      getLogger().severe(
	  "Only " + AbstractSpectrumConditions.class.getName() + " derived containers are allowed!");
    }
  }

  /**
   * Returns the conditions container to use for retrieving the spectra.
   *
   * @return 		the conditions
   */
  public AbstractConditions getConditions() {
    return m_Conditions;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String conditionsTipText() {
    return "The conditions for retrieving the spectra from the database.";
  }

  /**
   * Updates the database connection in dependent schemes.
   */
  @Override
  protected void updateDatabaseConnection() {
    if (m_filter instanceof DatabaseConnectionHandler)
      ((DatabaseConnectionHandler) m_filter).setDatabaseConnection(getDatabaseConnection());
  }

  double[] scaleToMinMax(double[] in, double to_min, double to_max, boolean abs) {
    double[] ret=new double[in.length];

    double min=Double.POSITIVE_INFINITY;
    double max=Double.NEGATIVE_INFINITY;
    for (int i=0;i<in.length;i++) {
      if (abs) {
	in[i]=Math.abs(in[i]);
      }
      ret[i]=in[i];
      if (in[i] < min) {
	min=in[i];
      }
      if (in[i] > max) {
	max=in[i];
      }
    }
    if (getScale()) {
      double diff_in=max-min;
      double diff_to=to_max-to_min;
      double rat=diff_to/diff_in;
      for (int i=0;i<in.length;i++) {
	ret[i]=((in[i]-min)*rat)+to_min;
      }
    }
    return(ret);
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  @Override
  protected Spectrum processData(Spectrum data) {
    Spectrum			result;
    List<SpectrumPoint>		points;

    Spectrum filtered_data=m_filter.filter(data);
    result = filtered_data.getHeader();
    points = filtered_data.toList();

    // StatCalc for reference data
    StatCalc sc_ref=new StatCalc();
    StatCalc sc_spectra[] = new StatCalc[points.size()];
    double[] cov=new double[sc_spectra.length];
    for (int i=0;i<sc_spectra.length;i++) {
      sc_spectra[i]=new StatCalc();
    }
    double min=Double.POSITIVE_INFINITY;
    double max=Double.NEGATIVE_INFINITY;
    List<Integer> ids=SampleDataF.getSingleton(getDatabaseConnection()).getDBIDs(m_Conditions);
    for (Integer id:ids) {
      Spectrum sp=SpectrumF.getSingleton(getDatabaseConnection()).load(id);
      Spectrum filtered=m_filter.filter(sp);
      int count=0;
      for (SpectrumPoint spoint:filtered.toList()) {
	sc_spectra[count++].enter(spoint.getAmplitude());

	SampleData sd=sp.getReport();
	if (sd != null) {
	  Double val=(Double)sd.getValue(getField());
	  sc_ref.enter(val);
	}
      }
    }

    for (Integer id:ids) {
      Spectrum sp=SpectrumF.getSingleton(getDatabaseConnection()).load(id);
      Spectrum filtered=m_filter.filter(sp);
      int count=0;
      for (SpectrumPoint spoint:filtered.toList()) {
	SampleData sd=sp.getReport();
	if (sd != null) {
	  cov[count]+=(spoint.getAmplitude()-sc_spectra[count].getMean())*((Double)sd.getValue(getField())-sc_ref.getMean());
	}
	count++;
      }
    }

 // rescale
    for (int i=0;i<cov.length;i++) {
      cov[i] = cov[i]/(ids.size()-1);
    }


    for (SpectrumPoint spoint:points) {
      if (spoint.getAmplitude() < min) {
	min=spoint.getAmplitude();
      }
      if (spoint.getAmplitude() > max) {
	max=spoint.getAmplitude();
      }
    }
    cov=scaleToMinMax(cov, min, max,getAbsolute());
    int count=0;
    for (SpectrumPoint spoint:points) {
      result.add(new SpectrumPoint(spoint.getWaveNumber(),(float)cov[count++]));
    }

    return result;
  }
}
