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
 * LRCoeff.java
 * Copyright (C) 2009-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractDatabaseConnectionFilter;
import adams.db.AbstractConditions;
import adams.db.AbstractDatabaseConnection;
import adams.data.instances.AbstractInstanceGenerator;
import adams.data.instances.SimpleInstanceGenerator;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.db.AbstractSpectrumConditions;
import adams.db.DatabaseConnection;
import adams.db.SampleDataT;
import adams.db.SpectrumConditionsMulti;
import adams.db.SpectrumT;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;

import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

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
public class LRCoeff
  extends AbstractDatabaseConnectionFilter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 485943495825693082L;


  /** the generator to use. */
  protected AbstractInstanceGenerator m_Generator;

  /** the spectrum retrieval conditions. */
  protected AbstractSpectrumConditions m_Conditions;

  /** apply coefficients to spectrum. */
  protected boolean m_Apply;

  /** display absolute values. */
  protected boolean m_Absolute;

  /** display absolute values. */
  protected boolean m_Scale;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Plot LR coefficients against Reference.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "generator", "generator",
	    new SimpleInstanceGenerator());

    m_OptionManager.add(
	    "conditions", "conditions",
	    new SpectrumConditionsMulti());

    m_OptionManager.add(
	    "apply", "apply",
	    false);

    m_OptionManager.add(
	    "absolute", "absolute",
	    false);

    m_OptionManager.add(
	    "scale", "scale",
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
   * Set scale?
   * @param abs	scale?
   */
  public void setScale(boolean sc) {
    m_Scale=sc;
    reset();
  }

  /**
   * Get scale?
   * @return	scale?
   */
  public boolean getScale() {
    return(m_Scale);
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
   * Set apply coeff to spectrum?
   * @param apply	apply?
   */
  public void setApply(boolean apply) {
    m_Apply=apply;
    reset();
  }

  /**
   * Get apply coeff to spectrum?
   * @return	apply?
   */
  public boolean getApply() {
    return(m_Apply);
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String applyTipText() {
    return "Apply coefficients to spectrum?";
  }

  /**
   * Sets the generator to use.
   *
   * @param value	the generator
   */
  public void setGenerator(AbstractInstanceGenerator value) {
    m_Generator = value;
    updateDatabaseConnection();
    reset();
  }

  /**
   * Updates the database connection in dependent schemes.
   */
  @Override
  protected void updateDatabaseConnection() {
    m_Generator.setDatabaseConnection(getDatabaseConnection());
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
   * Returns the generator in use.
   *
   * @return		the generator
   */
  public AbstractInstanceGenerator getGenerator() {
    return m_Generator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String generatorTipText() {
    return "The generator to use for turning spectra into weka.core.Instance objects.";
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
   * The default implementation only checks whether there is any data set.
   *
   * @param data	the data to filter
   */
  @Override
  protected void checkData(Spectrum data) {
    String	check;

    super.checkData(data);

    check = m_Generator.checkSetup();
    if (check != null)
      throw new IllegalStateException(
	  "Failed to initialize Instance generator: " + check);
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

    result = data.getHeader();
    points = data.toList();

    double min=Double.POSITIVE_INFINITY;
    double max=Double.NEGATIVE_INFINITY;
    Instances header=null;
    List<Integer> ids=SampleDataT.getSingleton(getDatabaseConnection()).getDBIDsOfReference(m_Conditions);
    for (Integer id:ids) {
      Spectrum sp=SpectrumT.getSingleton(getDatabaseConnection()).load(id);
      Instance inst  = m_Generator.generate(sp);
      if (header == null) {
	header=new Instances(m_Generator.getOutputHeader());
      }
      if (header !=null) {
	inst.setDataset(header);
	header.add(inst);
      }
    }

    for (SpectrumPoint spoint:points) {
      if (spoint.getAmplitude() < min) {
	min=spoint.getAmplitude();
      }
      if (spoint.getAmplitude() > max) {
	max=spoint.getAmplitude();
      }
    }
    LinearRegression lr=new LinearRegression();
    lr.setEliminateColinearAttributes(false);
    lr.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));

    try {
      lr.buildClassifier(header);
    } catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to build linear regression", e);
    }
    double[] coeff=lr.coefficients();

    if (getApply()) {
      int count=0;
      for (SpectrumPoint spoint:points) {
        coeff[count]=spoint.getAmplitude()*coeff[count++];
      }
    }

    coeff=scaleToMinMax(coeff, min, max,getAbsolute());

    int count=0;
    for (SpectrumPoint spoint:points) {
      result.add(new SpectrumPoint(spoint.getWaveNumber(),(float)coeff[count++]));
    }

    return result;
  }
}
