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
 * PLS.java
 * Copyright (C) 2016 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractBatchFilter;
import adams.data.instances.AbstractInstanceGenerator;
import adams.data.instances.SimpleInstanceGenerator;
import adams.data.instancesanalysis.pls.AbstractPLS;
import adams.data.instancesanalysis.pls.PLS1;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import weka.core.Instance;
import weka.core.Instances;

/**
 <!-- globalinfo-start -->
 * Turns the spectra internally into instances and transforms them using the specified PLS algorithm. The generated output is then converted back into spectra.<br>
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
 * <pre>-generator &lt;adams.data.instances.AbstractInstanceGenerator&gt; (property: generator)
 * &nbsp;&nbsp;&nbsp;The instance generator to use for turning the spectra into Weka Instance 
 * &nbsp;&nbsp;&nbsp;objects.
 * &nbsp;&nbsp;&nbsp;default: adams.data.instances.SimpleInstanceGenerator
 * </pre>
 * 
 * <pre>-algorithm &lt;adams.data.instancesanalysis.pls.AbstractPLS&gt; (property: algorithm)
 * &nbsp;&nbsp;&nbsp;The PLS algorithm to use
 * &nbsp;&nbsp;&nbsp;default: adams.data.instancesanalysis.pls.PLS1
 * </pre>
 * 
 <!-- options-end -->
 *
 */
public class PLS
  extends AbstractBatchFilter<Spectrum> {

  private static final long serialVersionUID = 8266258749271797113L;

  /** the instance generator to use. */
  protected AbstractInstanceGenerator m_Generator;

  /** the PLS algorithm. */
  protected AbstractPLS m_Algorithm;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Turns the spectra internally into instances and transforms them using the "
	+ "specified PLS algorithm. The generated output is then converted back "
	+ "into spectra.\n"
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
      "algorithm", "algorithm",
      new PLS1());
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
   * Sets PLS algorithm to use.
   *
   * @param value	the algorithm
   */
  public void setAlgorithm(AbstractPLS value) {
    m_Algorithm = value;
    reset();
  }

  /**
   * Returns PLS algorithm in use.
   *
   * @return      	the algorithm
   */
  public AbstractPLS getAlgorithm() {
    return m_Algorithm;
  }

  /**
   * Tip text for this property.
   *
   * @return      description for displaying in the GUI
   */
  public String algorithmTipText() {
    return "The PLS algorithm to use";
  }

  /**
   * Performs the actual batch filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  @Override
  protected Spectrum[] processBatchData(Spectrum[] data) {
    Spectrum[]		result;
    Instances		insts;
    Instances		pls;
    Instance 		inst;
    TIntList indices;
    int			i;
    int			n;
    SpectrumPoint 	point;

    // generate Instances
    insts   = null;
    indices = new TIntArrayList();
    for (i = 0; i < data.length; i++) {
      inst = m_Generator.generate(data[i]);
      if (inst.classIsMissing()) {
	getLogger().warning("Spectrum #" + (i+1) + "(" + data[i] + ") had no reference value?");
	continue;
      }
      if (insts == null)
	insts = new Instances(inst.dataset(), data.length);
      insts.add(inst);
      indices.add(i);
    }
    if (insts == null)
      throw new IllegalStateException("No instances with class attribute generated: " + m_Algorithm.toCommandLine());
    insts.compactify();

    // transform data
    try {
      m_Algorithm.determineOutputFormat(insts);
      pls = m_Algorithm.transform(insts);
    }
    catch (Exception e) {
      throw new IllegalStateException("Failed to process instances using: " + m_Algorithm.toCommandLine(), e);
    }

    // create new spectra
    result = new Spectrum[indices.size()];
    for (i = 0; i < indices.size(); i++) {
      result[i] = new Spectrum();
      result[i].setID(data[indices.get(i)].getID());
      result[i].getReport().mergeWith(data[indices.get(i)].getReport());
      for (n = 0; n < m_Algorithm.getNumComponents(); n++) {
	point = new SpectrumPoint(n+1, (float) pls.instance(i).value(n));
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
