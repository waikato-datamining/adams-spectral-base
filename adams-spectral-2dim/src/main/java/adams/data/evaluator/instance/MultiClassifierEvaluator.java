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
 * MultiClassifierEvaluator.java
 * Copyright (C) 2009-2016 University of Waikato, Hamilton, New Zealand
 */
package adams.data.evaluator.instance;

import adams.core.Randomizable;
import adams.flow.core.EvaluationHelper;
import adams.flow.core.EvaluationStatistic;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.PLSClassifier;
import weka.core.Instance;
import weka.core.Instances;

import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;

/**
 * A multi-classifier based evaluator. Compares predictions of the
 * specified models.
 *
 * @author dale
 * @version $Revision: 7 $
 */
public class MultiClassifierEvaluator
  extends AbstractSerializableEvaluator
  implements Randomizable {

  /** serial uid	 */
  private static final long serialVersionUID = -1524226172394611174L;

  /** WEKA classifiers to use. */
  protected Classifier[] m_Classifiers;

  /** WEKA base classifier to use. */
  protected Classifier m_Base;

  /** the seed value. */
  protected long m_Seed;

  /** Instances for training bags. */
  protected Instances m_TrainingData;

  /** the results. */
  protected Evaluation m_CrossvalidationResults;

  /** Header of instances to process. */
  private Instances m_Header;

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
  public String classifierTipText() {
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
   * <br><br>
   * Default implementation returns null.
   *
   * @param data	the instance to check
   * @return		evaluation metrics, {@link #m_MissingEvaluation} in case
   * 			the class value is missing
   */
  protected HashMap<String,Float> performMultiEvaluate(Instance data) {
    HashMap<String,Float> result = new HashMap<String,Float>();
    try{
      double min = Double.MAX_VALUE;
      double max = Double.NEGATIVE_INFINITY;
      for (Classifier c: m_Classifiers) {
	double res = c.classifyInstance(data);
	min = Math.min(min, res);
	max = Math.max(max, res);
      }
      double mae = EvaluationHelper.getValue(m_CrossvalidationResults, EvaluationStatistic.MEAN_ABSOLUTE_ERROR, -1);
      result.put("MAE", (float)mae);
      result.put("MIN", (float)min);
      result.put("MAX", (float)max);
      result.put("RESULT", (float)(max-min));
      result.put("RESULT_SCORE", (float)((max-min)/mae));
      return(result);

    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to evaluate: " + data, e);
      return null;
    }
  }

  /**
   * Get predictions for each of the models.
   *
   * @param data	instance
   * @return measure
   */
  @Override
  protected Float performEvaluate(Instance data) {
    try {
      double min = Double.MAX_VALUE;
      double max = Double.MIN_VALUE;
      for (Classifier c: m_Classifiers) {
	double res = c.classifyInstance(data);
	min = Math.min(min, res);
	max = Math.max(max, res);
      }
      double mae = EvaluationHelper.getValue(m_CrossvalidationResults, EvaluationStatistic.MEAN_ABSOLUTE_ERROR, -1);
      return (float) (Math.abs(max - min) / mae);

    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to evaluate: " + data, e);
      return Float.NaN;
    }
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
    m_Classifiers            = (Classifier[]) value[1];
    m_CrossvalidationResults = (Evaluation) value[2];
    m_Header                 = (Instances) value[3];
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

    try {
      m_CrossvalidationResults = new Evaluation(data);
      m_CrossvalidationResults.crossValidateModel(m_Base, data, 10, new Random(m_Seed));

      m_Header = new Instances(m_TrainingData,0);

      for (Classifier c: m_Classifiers)
	c.buildClassifier(data);

      m_SerializableObjectHelper.saveSetup();
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to build classifier(s)!", e);
      return false;
    }

    return true;
  }

  /**
   * Clean up training instances.
   */
  @Override
  public void cleanUp() {
    m_TrainingData = null;
  }
}
