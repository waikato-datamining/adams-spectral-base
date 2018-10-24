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
 * AbstractWekaSpectrumAnalysis.java
 * Copyright (C) 2016-2018 University of Waikato, Hamilton, NZ
 */

package adams.data.spectrumanalysis;

import adams.data.instances.AbstractInstanceGenerator;
import adams.data.instances.SimpleInstanceGenerator;
import adams.data.spectrum.Spectrum;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;

/**
 * Ancestor for Weka-based analysis schemes, i.e., ones that use
 * {@link Instances} as basis.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractWekaSpectrumAnalysis
  extends AbstractSpectrumAnalysis {

  private static final long serialVersionUID = -4503181684140574897L;

  /** the instance generator to use. */
  protected AbstractInstanceGenerator m_Generator;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "generator", "generator",
      new SimpleInstanceGenerator());
  }

  /**
   * Sets the instance generator.
   *
   * @param value	the generator
   */
  public void setGenerator(AbstractInstanceGenerator value) {
    m_Generator = value;
    reset();
  }

  /**
   * Returns the instance generator.
   *
   * @return		the generator
   */
  public AbstractInstanceGenerator getGenerator() {
    return m_Generator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String generatorTipText() {
    return "The generator to use for generating Weka data from the spectra.";
  }

  /**
   * Hook method for checks.
   *
   * @param data	the data to check
   */
  protected void check(Instances data) {
    if (data == null)
      throw new IllegalStateException("No data provided!");
  }

  /**
   * Performs the actual analysis.
   *
   * @param data	the data to analyze
   * @return		null if successful, otherwise error message
   * @throws Exception	if analysis fails
   */
  protected abstract String doAnalyze(Instances data) throws Exception;

  /**
   * Performs the actual analysis.
   *
   * @param data	the data to analyze
   * @return		null if successful, otherwise error message
   * @throws Exception	if analysis fails
   */
  @Override
  protected String doAnalyze(List<Spectrum> data) throws Exception {
    String	result;
    Instances	dataset;
    Instance	inst;

    dataset = null;
    for (Spectrum sp: data) {
      inst = m_Generator.generate(sp);
      if (dataset == null)
	dataset = new Instances(inst.dataset(), 0);
      dataset.add(inst);
    }

    if (dataset == null) {
      result = "No Instances generated?";
    }
    else {
      if (dataset.classIndex() == -1)
	dataset.setClassIndex(dataset.numAttributes() - 1);
      result = doAnalyze(dataset);
    }

    return result;
  }
}
