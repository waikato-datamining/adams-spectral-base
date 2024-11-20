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
 * MultiClassifierEvaluator.java
 * Copyright (C) 2016-2024 University of Waikato, Hamilton, New Zealand
 */
package adams.data.evaluator.instance;

import adams.core.Randomizable;
import adams.core.StoppableUtils;
import adams.core.StoppableWithFeedback;
import adams.core.option.OptionUtils;
import adams.flow.core.EvaluationHelper;
import adams.flow.core.EvaluationStatistic;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.StoppableEvaluation;
import weka.classifiers.functions.PLSClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Multi-classifier based evaluator. Generates predictions for each of the classifiers on an incoming instance. Outputs info on the range of the predictions, and uses base classifier MAE to normalise the RESULT_SCORE
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
 * <pre>-classifier &lt;weka.classifiers.Classifier&gt; [-classifier ...] (property: classifiers)
 * &nbsp;&nbsp;&nbsp;The classifiers to be used.
 * &nbsp;&nbsp;&nbsp;default: weka.classifiers.functions.PLSClassifier -filter \"weka.filters.supervised.attribute.PLSFilter -C 20 -M -A PLS1 -P center\" -S 1
 * </pre>
 * 
 * <pre>-base &lt;weka.classifiers.Classifier&gt; (property: base)
 * &nbsp;&nbsp;&nbsp;The base classifier to be used.
 * &nbsp;&nbsp;&nbsp;default: weka.classifiers.functions.PLSClassifier -filter \"weka.filters.supervised.attribute.PLSFilter -C 20 -M -A PLS1 -P center\" -S 1
 * </pre>
 * 
 * <pre>-seed &lt;long&gt; (property: seed)
 * &nbsp;&nbsp;&nbsp;The seed value to use for cross-validation
 * &nbsp;&nbsp;&nbsp;default: 1
 * </pre>
 * 
 * <pre>-folds &lt;int&gt; (property: folds)
 * &nbsp;&nbsp;&nbsp;The number of folds to use
 * &nbsp;&nbsp;&nbsp;default: 10
 * &nbsp;&nbsp;&nbsp;minimum: 2
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author dale
 * @version $Revision: 7 $
 */
public class MultiClassifierEvaluator
  extends AbstractSerializableEvaluator
  implements Randomizable, StoppableWithFeedback {

  /** serial uid	 */
  private static final long serialVersionUID = -1524226172394611174L;

  /** WEKA classifiers to use. */
  protected Classifier[] m_Classifiers;

  /** WEKA base classifier to use. */
  protected Classifier m_Base;

  /** the seed value. */
  protected long m_Seed;

  /** the number of folds. */
  protected int m_Folds;

  /** Instances for training bags. */
  protected Instances m_TrainingData;

  /** the results. */
  protected Evaluation m_CrossvalidationResults;

  /** Header of instances to process. */
  protected Instances m_Header;

  /** whether the execution was stopped. */
  protected boolean m_Stopped;

  /**
   * Global info.
   */
  @Override
  public String globalInfo() {
    return "Multi-classifier based evaluator. Generates predictions for each of the classifiers" +
      " on an incoming instance. Outputs info on the range of the predictions, and uses base classifier MAE "+
      "to normalise the RESULT_SCORE";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "classifier", "classifiers",
      new Classifier[]{new PLSClassifier()});

    m_OptionManager.add(
      "base", "base",
      new PLSClassifier());

    m_OptionManager.add(
      "seed", "seed",
      1L);

    m_OptionManager.add(
      "folds", "folds",
      10, 2, null);
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
   * Set the classifiers to be used.
   *
   * @param value	the classifiers
   */
  public void setClassifiers(Classifier[] value) {
    m_Classifiers = value;
    reset();
  }

  /**
   * Get the classifiers to be used.
   *
   * @return		the classifiers
   */
  public Classifier[] getClassifiers() {
    return m_Classifiers;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String classifiersTipText() {
    return "The classifiers to be used.";
  }

  /**
   * Set the base classifier to be used.
   *
   * @param value	the base classifier
   */
  public void setBase(Classifier value) {
    m_Base = value;
    reset();
  }

  /**
   * Get the base classifier to be used.
   *
   * @return		the base classifier
   */
  public Classifier getBase() {
    return m_Base;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String baseTipText() {
    return "The base classifier to be used.";
  }

  /**
   * Sets the seed value.
   *
   * @param value	the seed
   */
  public void setSeed(long value) {
    m_Seed = value;
    reset();
  }

  /**
   * Returns the seed value.
   *
   * @return  		the seed
   */
  public long getSeed() {
    return m_Seed;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String seedTipText() {
    return "The seed value to use for cross-validation";
  }

  /**
   * Sets the number of folds to use (>= 2).
   *
   * @param value	the folds
   */
  public void setFolds(int value) {
    if (getOptionManager().isValid("folds", value)) {
      m_Folds = value;
      reset();
    }
  }

  /**
   * Returns the number of folds in use.
   *
   * @return  		the folds
   */
  public int getFolds() {
    return m_Folds;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String foldsTipText() {
    return "The number of folds to use";
  }

  /**
   * Set training data.
   *
   * @param value	the data
   */
  public void setData(Instances value) {
    m_TrainingData = value;
  }

  /**
   * Get training data.
   * @return	training instances
   */
  public Instances getData() {
    return m_TrainingData;
  }

  /**
   * Performs the actual evaluation, allowing return of multiple evaluation metrics.
   *
   * @param data	the instance to check
   * @return		evaluation metrics, {@link #m_MissingEvaluation} in case
   * 			the class value is missing
   */
  protected HashMap<String,Float> performMultiEvaluate(Instance data) {
    HashMap<String,Float> 	result;
    double 			min;
    double 			max;
    double 			res;
    double 			mae;

    result = new HashMap<>();

    try {
      min = Double.MAX_VALUE;
      max = Double.NEGATIVE_INFINITY;
      for (Classifier c: m_Classifiers) {
	res = c.classifyInstance(data);
	min = Math.min(min, res);
	max = Math.max(max, res);
      }
      mae = EvaluationHelper.getValue(m_CrossvalidationResults, EvaluationStatistic.MEAN_ABSOLUTE_ERROR, -1);
      result.put("MAE", (float)mae);
      result.put("MIN", (float)min);
      result.put("MAX", (float)max);
      result.put("RESULT", (float)(max-min));
      result.put("RESULT_SCORE", (float)((max-min)/mae));
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to evaluate: " + data, e);
      result = null;
    }

    return result;
  }

  /**
   * Get predictions for each of the models.
   *
   * @param data	instance
   * @return measure
   */
  @Override
  protected Float performEvaluate(Instance data) {
    float	result;
    double 	min;
    double 	max;
    double 	res;
    double 	mae;

    try {
      min = Double.MAX_VALUE;
      max = Double.MIN_VALUE;
      for (Classifier c: m_Classifiers) {
	res = c.classifyInstance(data);
	min = Math.min(min, res);
	max = Math.max(max, res);
      }
      mae = EvaluationHelper.getValue(m_CrossvalidationResults, EvaluationStatistic.MEAN_ABSOLUTE_ERROR, -1);
      result = (float) (Math.abs(max - min) / mae);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to evaluate: " + data, e);
      result = Float.NaN;
    }

    return result;
  }

  /**
   * Regenerates all the objects that are necessary for serialization.
   */
  @Override
  public void initSerializationSetup() {
    if (m_Classifiers == null)
      performBuild(getData());
  }

  /**
   * Returns the member variables to serialize to a file.
   *
   * @return		the objects to serialize
   */
  @Override
  public Object[] retrieveSerializationSetup() {
    return new Object[]{
      m_Classifiers,
      m_CrossvalidationResults,
      m_Header
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
    m_Classifiers            = (Classifier[]) value[0];
    m_CrossvalidationResults = (Evaluation) value[1];
    m_Header                 = (Instances) value[2];
  }

  /**
   * Builds the evaluator.
   *
   * @param data	the instance to check
   * @return		true if build successful
   */
  @Override
  protected boolean performBuild(Instances data) {
    setData(data);

    m_Stopped = false;

    try {
      m_CrossvalidationResults = new StoppableEvaluation(data);
      m_CrossvalidationResults.crossValidateModel(m_Base, data, m_Folds, new Random(m_Seed));
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to cross-validate classifier: " + OptionUtils.getCommandLine(m_Base), e);
      return false;
    }

    if (m_Stopped)
      return false;

    m_Header = new Instances(m_TrainingData, 0);

    for (Classifier c: m_Classifiers) {
      try {
	c.buildClassifier(data);
      }
      catch (Exception e) {
	getLogger().log(Level.SEVERE, "Failed to build classifier: " + OptionUtils.getCommandLine(c), e);
	return false;
      }
    }

    m_SerializableObjectHelper.saveSetup();

    return true;
  }

  /**
   * Clean up training instances.
   */
  @Override
  public void cleanUp() {
    m_TrainingData = null;
  }

  /**
   * Stops the execution. No message set.
   */
  @Override
  public void stopExecution() {
    StoppableUtils.stopExecution(m_CrossvalidationResults);
    m_Stopped = true;
  }

  /**
   * Whether the execution has been stopped.
   *
   * @return		true if stopped
   */
  @Override
  public boolean isStopped() {
    return m_Stopped;
  }
}
