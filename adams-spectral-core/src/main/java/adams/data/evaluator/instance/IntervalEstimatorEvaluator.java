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

/**
 * IntervalEstimatorEvaluator.java
 * Copyright (C) 2015-2016 University of Waikato, Hamilton, NZ
 */

package adams.data.evaluator.instance;

import adams.core.Utils;
import adams.core.option.OptionUtils;
import weka.classifiers.Classifier;
import weka.classifiers.IntervalEstimator;
import weka.classifiers.functions.GaussianProcessesNoWeights;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;

/**
 <!-- globalinfo-start -->
 * Stores the interval provided by the interval estimator classifier
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-missing-evaluation &lt;float&gt; (property: missingEvaluation)
 * &nbsp;&nbsp;&nbsp;The value to use as replacement for missing evaluations.
 * &nbsp;&nbsp;&nbsp;default: -999999.0
 * </pre>
 * 
 * <pre>-serialization-file &lt;adams.core.io.PlaceholderFile&gt; (property: serializationFile)
 * &nbsp;&nbsp;&nbsp;The file to serialize the generated internal model to.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 * 
 * <pre>-override-serialized-file &lt;boolean&gt; (property: overrideSerializedFile)
 * &nbsp;&nbsp;&nbsp;If set to true, then any serialized file will be ignored and the setup for 
 * &nbsp;&nbsp;&nbsp;serialization will be regenerated.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-classifier &lt;weka.classifiers.Classifier&gt; (property: classifier)
 * &nbsp;&nbsp;&nbsp;The classifier to use (must implement weka.classifiers.IntervalEstimator
 * &nbsp;&nbsp;&nbsp;).
 * &nbsp;&nbsp;&nbsp;default: weka.classifiers.functions.GaussianProcesses -L 1.0 -N 0 -K \"weka.classifiers.functions.supportVector.PolyKernel -E 1.0 -C 250007\"
 * </pre>
 * 
 * <pre>-confidence-level &lt;double&gt; (property: confidenceLevel)
 * &nbsp;&nbsp;&nbsp;The confidence level to use (0-1).
 * &nbsp;&nbsp;&nbsp;default: 0.95
 * &nbsp;&nbsp;&nbsp;minimum: 0.0
 * &nbsp;&nbsp;&nbsp;maximum: 1.0
 * </pre>
 * 
 * <pre>-normalize &lt;boolean&gt; (property: normalize)
 * &nbsp;&nbsp;&nbsp;If enabled, the confidence intervals get normalized to the class range.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class IntervalEstimatorEvaluator
  extends AbstractSerializableEvaluator
  implements WekaClassifierBasedEvaluator {

  private static final long serialVersionUID = -4254958807591488789L;

  /** the lower limit. */
  public final static String LIMIT_LOWER = "LimitLower";

  /** the upper limit. */
  public final static String LIMIT_UPPER = "LimitUpper";

  /** the interval estimator to use. */
  protected Classifier m_Classifier;

  /** the actual classifier in use. */
  protected Classifier m_ActualClassifier;

  /** the confidence level. */
  protected double m_ConfidenceLevel;

  /** whether to normalize. */
  protected boolean m_Normalize;

  /** the training data. */
  protected Instances m_TrainingData;

  /** the header. */
  protected Instances m_Header;

  /** the class range. */
  protected double[] m_ClassRange;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Stores the interval provided by the interval estimator classifier";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	"classifier", "classifier",
	new GaussianProcessesNoWeights());

    m_OptionManager.add(
	"confidence-level", "confidenceLevel",
	0.95, 0.0, 1.0);

    m_OptionManager.add(
	"normalize", "normalize",
	false);
  }

  /**
   * Returns the default value in case of missing evaluations.
   *
   * @return		the default value
   */
  @Override
  protected float getDefaultMissingEvaluation() {
    return -999999f;
  }

  /**
   * Sets the interval estimator to use.
   *
   * @param value	the classifier
   */
  public void setClassifier(Classifier value) {
    if (value instanceof IntervalEstimator) {
      m_Classifier = value;
      reset();
    }
    else {
      getLogger().warning(
	value.getClass().getName() + " does not implement the " + IntervalEstimator.class.getName() + " interface!");
    }
  }

  /**
   * Returns the interval estimator in use.
   *
   * @return		the classifier
   */
  public Classifier getClassifier() {
    return m_Classifier;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String classifierTipText() {
    return "The classifier to use (must implement " + IntervalEstimator.class.getName() + ").";
  }

  /**
   * Sets the confidence level to use.
   *
   * @param value	the level (0-1)
   */
  public void setConfidenceLevel(double value) {
    if (getOptionManager().isValid("confidenceLevel", value)) {
      m_ConfidenceLevel = value;
      reset();
    }
  }

  /**
   * Returns the confidence level in use.
   *
   * @return		the level (0-1)
   */
  public double getConfidenceLevel() {
    return m_ConfidenceLevel;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String confidenceLevelTipText() {
    return "The confidence level to use (0-1).";
  }

  /**
   * Sets whether to normalize the confidence intervals using the class range.
   *
   * @param value	true if to normalize
   */
  public void setNormalize(boolean value) {
    m_Normalize = value;
    reset();
  }

  /**
   * Returns whether to normalize the confidence intervals using the class range.
   *
   * @return		true if to normalize
   */
  public boolean getNormalize() {
    return m_Normalize;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String normalizeTipText() {
    return "If enabled, the confidence intervals get normalized to the class range.";
  }

  /**
   * Builds the evaluator.
   *
   * @param data	the instance to check
   * @return		true if build successful
   */
  @Override
  protected boolean performBuild(Instances data) {
    m_TrainingData = data;

    try {
      m_ActualClassifier = (Classifier) OptionUtils.shallowCopy(m_Classifier);
      m_ActualClassifier.buildClassifier(data);
      m_Header = new Instances(data, 0);
      m_ClassRange = new double[]{
	data.attributeStats(data.classIndex()).numericStats.min,
	data.attributeStats(data.classIndex()).numericStats.max,
      };
      m_SerializableObjectHelper.saveSetup();
      return true;
    }
    catch (Exception e) {
      Utils.handleException(this, "Failed to train classifier!", e);
      return false;
    }
  }

  /**
   * Post-processes the confidence levels if necessary.
   *
   * @param levels	the levels to process
   * @return		the updated levels
   */
  protected double[][] postProcess(double[][] levels) {
    double[][]	result;
    int		i;
    double	range;

    if (!m_Normalize)
      return levels;

    result = new double[levels.length][2];
    range  = m_ClassRange[1] - m_ClassRange[0];
    for (i = 0; i < levels.length; i++) {
      result[i][0] = levels[i][0] / range;
      result[i][1] = levels[i][1] / range;
    }

    return result;
  }

  /**
   * Performs the actual evaluation. Returns the range of the first confidence
   * interval (upper - lower).
   *
   * @param data	the instance to check
   * @return		evaluation metric, {@link #m_MissingEvaluation} in case
   * 			the class value is missing
   */
  @Override
  protected Float performEvaluate(Instance data) {
    Float	result;
    double[][]	conf;

    try {
      conf = ((IntervalEstimator) m_ActualClassifier).predictIntervals(data, m_ConfidenceLevel);
      conf = postProcess(conf);
      result = new Float(conf[0][1] - conf[0][0]);
    }
    catch (Exception e) {
      Utils.handleException(this, "Failed to perform evaluation!", e);
      result = super.performEvaluate(data);
    }

    return result;
  }

  /**
   * Performs the actual evaluation, allowing return of multiple evaluation metrics.
   * Returns the lower/upper limit of the first confidence interval.
   *
   * @param data	the instance to check
   * @return		evaluation metrics, {@link #m_MissingEvaluation} in case
   * 			the class value is missing
   */
  @Override
  protected HashMap<String, Float> performMultiEvaluate(Instance data) {
    HashMap<String,Float>	result;
    double[][]	conf;

    result = new HashMap<>();

    try {
      conf = ((IntervalEstimator) m_ActualClassifier).predictIntervals(data, m_ConfidenceLevel);
      conf = postProcess(conf);
      result.put(LIMIT_LOWER, (float) conf[0][0]);
      result.put(LIMIT_UPPER, (float) conf[0][1]);
    }
    catch (Exception e) {
      Utils.handleException(this, "Failed to perform evaluation!", e);
      result = null;
    }

    return result;
  }

  /**
   * Regenerates all the objects that are necessary for serialization.
   */
  @Override
  public void initSerializationSetup() {
    if (m_ActualClassifier == null)
      performBuild(m_TrainingData);
  }

  /**
   * Returns the member variables to serialize to a file.
   *
   * @return		the objects to serialize
   */
  @Override
  public Object[] retrieveSerializationSetup() {
    return new Object[]{
      m_ActualClassifier,
      m_Header,
      m_ConfidenceLevel,
      m_Normalize,
      m_ClassRange
    };
  }

  /**
   * Updates the member variables with the provided objects obtained from
   * deserialization.
   *
   * @param value	the deserialized objects
   */
  @Override
  public void setSerializationSetup(Object[] value) {
    m_ActualClassifier = (Classifier) value[0];
    m_Header           = (Instances) value[1];
    m_ConfidenceLevel  = (Double) value[2];
    m_Normalize        = (Boolean) value[3];
    m_ClassRange       = (double[]) value[4];
  }

  /**
   * Cleans up data structures, frees up memory.
   */
  @Override
  public void cleanUp() {
    m_TrainingData = null;
  }
}
