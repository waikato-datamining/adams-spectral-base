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
 * PCA.java
 * Copyright (C) 2016 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractBatchFilter;
import adams.data.instances.AbstractInstanceGenerator;
import adams.data.instances.SimpleInstanceGenerator;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.PublicPrincipalComponents;

/**
 <!-- globalinfo-start -->
 * Turns the spectra internally into instances and transforms them using principal component analysis (PCA).<br>
 * The generated output is then converted back into spectra.<br>
 * Only works as batch filter, not in single spectrum mode.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-no-id-update &lt;boolean&gt; (property: dontUpdateID)
 * &nbsp;&nbsp;&nbsp;If enabled, suppresses updating the ID of adams.data.id.IDHandler data containers.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-generator &lt;knir.data.instances.AbstractInstanceGenerator&gt; (property: generator)
 * &nbsp;&nbsp;&nbsp;The instance generator to use for turning the spectra into Weka Instance 
 * &nbsp;&nbsp;&nbsp;objects.
 * &nbsp;&nbsp;&nbsp;default: knir.data.instances.SimpleInstanceGenerator
 * </pre>
 * 
 * <pre>-variance &lt;double&gt; (property: variance)
 * &nbsp;&nbsp;&nbsp;The variance to cover.
 * &nbsp;&nbsp;&nbsp;default: 0.95
 * &nbsp;&nbsp;&nbsp;minimum: 0.0
 * </pre>
 * 
 * <pre>-max-attributes &lt;int&gt; (property: maxAttributes)
 * &nbsp;&nbsp;&nbsp;The maximum attributes.
 * &nbsp;&nbsp;&nbsp;default: -1
 * &nbsp;&nbsp;&nbsp;minimum: -1
 * </pre>
 * 
 * <pre>-max-attribute-names &lt;int&gt; (property: maxAttributeNames)
 * &nbsp;&nbsp;&nbsp;The maximum number of attribute names.
 * &nbsp;&nbsp;&nbsp;default: 5
 * &nbsp;&nbsp;&nbsp;minimum: -1
 * </pre>
 * 
 <!-- options-end -->
 *
 */
public class PCA
  extends AbstractBatchFilter<Spectrum> {

  private static final long serialVersionUID = 8266258749271797113L;

  /** the instance generator to use. */
  protected AbstractInstanceGenerator m_Generator;

  /** the variance to cover. */
  protected double m_Variance;

  /** the maximum number of attributes. */
  protected int m_MaxAttributes;

  /** the maximum number of attribute names. */
  protected int m_MaxAttributeNames;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Turns the spectra internally into instances and transforms them using principal component analysis (PCA).\n"
        + "The generated output is then converted back into spectra.\n"
	+ "Only works as batch filter, not in single spectrum mode.";
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
      "variance", "variance",
      0.95, 0.0, null);

    m_OptionManager.add(
      "max-attributes", "maxAttributes",
      -1, -1, null);

    m_OptionManager.add(
      "max-attribute-names", "maxAttributeNames",
      5, -1, null);
  }

  /**
   * Sets the instance generator to use.
   *
   * @param value	the generator
   */
  public void setGenerator(AbstractInstanceGenerator value) {
    m_Generator = value;
    reset();
  }

  /**
   * Returns the instance generator in use.
   * 
   * @return      	the generator
   */
  public AbstractInstanceGenerator getGenerator() {
    return m_Generator;
  }

  /**
   * Tip text for this property.
   *
   * @return      description for displaying in the GUI
   */
  public String generatorTipText() {
    return "The instance generator to use for turning the spectra into Weka Instance objects.";
  }

  /**
   * Sets the variance.
   *
   * @param value	the variance
   */
  public void setVariance(double value) {
    if (getOptionManager().isValid("variance", value)) {
      m_Variance = value;
      reset();
    }
  }

  /**
   * Returns the variance.
   *
   * @return		the variance
   */
  public double getVariance() {
    return m_Variance;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String varianceTipText() {
    return "The variance to cover.";
  }

  /**
   * Sets the maximum attributes.
   *
   * @param value	the maximum
   */
  public void setMaxAttributes(int value) {
    if (getOptionManager().isValid("maxAttributes", value)) {
      m_MaxAttributes = value;
      reset();
    }
  }

  /**
   * Returns the maximum attributes.
   *
   * @return		the maximum
   */
  public int getMaxAttributes() {
    return m_MaxAttributes;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String maxAttributesTipText() {
    return "The maximum attributes.";
  }

  /**
   * Sets the maximum number of attribute names.
   *
   * @param value	the maximum
   */
  public void setMaxAttributeNames(int value) {
    if (getOptionManager().isValid("maxAttributeNames", value)) {
      m_MaxAttributeNames = value;
      reset();
    }
  }

  /**
   * Returns the maximum number of attribute names.
   *
   * @return		the maximum
   */
  public int getMaxAttributeNames() {
    return m_MaxAttributeNames;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String maxAttributeNamesTipText() {
    return "The maximum number of attribute names.";
  }

  /**
   * Performs the actual batch filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  @Override
  protected Spectrum[] processBatchData(Spectrum[] data) {
    Spectrum[]			result;
    Instances			insts;
    PublicPrincipalComponents	pca;
    Instances			filtered;
    Instance 			inst;
    int				i;
    int				n;
    SpectrumPoint point;

    // generate Instances
    insts = null;
    for (i = 0; i < data.length; i++) {
      inst = m_Generator.generate(data[i]);
      if (insts == null)
	insts = new Instances(inst.dataset(), data.length);
      insts.add(inst);
    }
    if (insts == null)
      throw new IllegalStateException("No spectra provided?");
    insts.compactify();

    // remove class attribute
    if (insts.classIndex() > -1) {
      i = insts.classIndex();
      insts.setClassIndex(-1);
      insts.deleteAttributeAt(i);
    }

    // build a model using the PublicPrincipalComponents
    pca = new PublicPrincipalComponents();
    pca.setMaximumAttributes(m_MaxAttributes);
    pca.setVarianceCovered(m_Variance);
    pca.setMaximumAttributeNames(m_MaxAttributeNames);
    try {
      pca.setInputFormat(insts);
    }
    catch(Exception e) {
      throw new IllegalStateException("Failed to set data format", e);
    }
    try {
      filtered = weka.filters.Filter.useFilter(insts, pca);
    }
    catch (Exception e) {
      throw new IllegalStateException("Failed to apply filter", e);
    }

    // create new spectra
    result = new Spectrum[data.length];
    for (i = 0; i < data.length; i++) {
      result[i] = new Spectrum();
      result[i].setID(data[i].getID());
      result[i].getReport().mergeWith(data[i].getReport());
      for (n = 0; n < filtered.numAttributes(); n++) {
	point = new SpectrumPoint(n+1, (float) filtered.instance(i).value(n));
	result[i].add(point);
      }
    }

    return result;
  }

  /**
   * Does nothing.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  @Override
  protected Spectrum processData(Spectrum data) {
    return data;
  }
}
