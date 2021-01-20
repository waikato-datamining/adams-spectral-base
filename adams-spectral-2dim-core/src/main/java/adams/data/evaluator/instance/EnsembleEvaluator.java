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
 * EnsembleEvaluator.java
 * Copyright (C) 2015 University of Waikato, Hamilton, NZ
 */

package adams.data.evaluator.instance;

import adams.core.ObjectCopyHelper;
import adams.core.Randomizable;
import adams.core.logging.LoggingHelper;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegressionJ;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Random;

/**
 <!-- globalinfo-start -->
 * Builds an ensemble of classifiers on the training data to determine the MAE per classifier, to be used as normalization factor.<br>
 * At evaluation time, each classifier makes a prediction on the instance and the classification is divided by the normalization factor for this classifier. The range between min&#47;max normalized prediction is the evaluation output.
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
 * &nbsp;&nbsp;&nbsp;The classifiers to use in the ensemble.
 * &nbsp;&nbsp;&nbsp;default: weka.classifiers.functions.LinearRegressionJ -S 0 -R 1.0E-8
 * </pre>
 * 
 * <pre>-seed &lt;long&gt; (property: seed)
 * &nbsp;&nbsp;&nbsp;The seed value for the cross-validation.
 * &nbsp;&nbsp;&nbsp;default: 1
 * </pre>
 * 
 * <pre>-num-folds &lt;int&gt; (property: numFolds)
 * &nbsp;&nbsp;&nbsp;The number of folds to use for evaluating the classifiers.
 * &nbsp;&nbsp;&nbsp;default: 10
 * &nbsp;&nbsp;&nbsp;minimum: 2
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class EnsembleEvaluator
  extends AbstractSerializableEvaluator
  implements Randomizable {

  private static final long serialVersionUID = -4254958807591488789L;

  /** the classifiers to use. */
  protected Classifier[] m_Classifiers;

  /** the actual classifiers in use. */
  protected Classifier[] m_ActualClassifiers;

  /** the seed value. */
  protected long m_Seed;

  /** the number of folds to use for cross-validation. */
  protected int m_NumFolds;

  /** the normalization factor. */
  protected double[] m_Normalize;

  /** the training data. */
  protected Instances m_TrainingData;

  /** the header. */
  protected Instances m_Header;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Builds an ensemble of classifiers on the training data to determine "
	+ "the MAE per classifier, to be used as normalization factor.\n"
	+ "At evaluation time, each classifier makes a prediction on the "
	+ "instance and the classification is divided by the normalization "
	+ "factor for this classifier. The range between min/max normalized "
	+ "prediction is the evaluation output.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "classifier", "classifiers",
      new Classifier[]{new LinearRegressionJ()});

    m_OptionManager.add(
      "seed", "seed",
      1L);

    m_OptionManager.add(
      "num-folds", "numFolds",
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
   * Sets the classifiers to use.
   *
   * @param value	the classifiers
   */
  public void setClassifiers(Classifier[] value) {
    m_Classifiers = value;
    reset();
  }

  /**
   * Returns the classifiers in use.
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
    return "The classifiers to use in the ensemble.";
  }

  /**
   * Sets the seed value.
   *
   * @param value	the seed
   */
  @Override
  public void setSeed(long value) {
    m_Seed = value;
    reset();
  }

  /**
   * Returns the seed value.
   *
   * @return  		the seed
   */
  @Override
  public long getSeed() {
    return m_Seed;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  @Override
  public String seedTipText() {
    return "The seed value for the cross-validation.";
  }

  /**
   * Sets the number of folds.
   *
   * @param value	the number of folds
   */
  public void setNumFolds(int value) {
    if (getOptionManager().isValid("numFolds", value)) {
      m_NumFolds = value;
      reset();
    }
  }

  /**
   * Returns the number of folds.
   *
   * @return		the number of folds
   */
  public int getNumFolds() {
    return m_NumFolds;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String numFoldsTipText() {
    return "The number of folds to use for evaluating the classifiers.";
  }

  /**
   * Builds the evaluator.
   *
   * @param data	the instance to check
   * @return		true if build successful
   */
  @Override
  protected boolean performBuild(Instances data) {
    int		i;
    Evaluation 	eval;

    if (data == null)
      return false;

    m_TrainingData      = data;
    m_Header            = new Instances(data, 0);
    m_ActualClassifiers = new Classifier[m_Classifiers.length];
    m_Normalize         = new double[m_Classifiers.length];
    for (i = 0; i < m_Classifiers.length; i++) {
      try {
	m_ActualClassifiers[i] = ObjectCopyHelper.copyObject(m_Classifiers[i]);
	m_ActualClassifiers[i].buildClassifier(data);
	// determine normalization factor
	eval = new Evaluation(data);
	eval.crossValidateModel(m_Classifiers[i], data, m_NumFolds, new Random(m_Seed));
	m_Normalize[i] = eval.meanAbsoluteError();
      }
      catch (Exception e) {
	LoggingHelper.handleException(this, "Failed to train classifier #" + (i+1) + "!", e);
	return false;
      }
    }
    m_SerializableObjectHelper.saveSetup();
    return true;
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
    double	min;
    double	max;
    double[]	preds;
    int		i;

    try {
      preds = new double[m_ActualClassifiers.length];
      min   = Double.MAX_VALUE;
      max   = Double.MIN_VALUE;
      for (i = 0; i < m_ActualClassifiers.length; i++) {
	preds[i]  = m_ActualClassifiers[i].classifyInstance(data);
	preds[i] /= m_Normalize[i];
	min       = Math.min(min, preds[i]);
	max       = Math.max(max, preds[i]);
      }
      result = new Float(max - min);
    }
    catch (Exception e) {
      LoggingHelper.handleException(this, "Failed to perform evaluation!", e);
      result = super.performEvaluate(data);
    }

    return result;
  }

  /**
   * Regenerates all the objects that are necessary for serialization.
   */
  @Override
  public void initSerializationSetup() {
    if (m_ActualClassifiers == null)
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
      m_ActualClassifiers,
      m_Header,
      m_Normalize
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
    m_ActualClassifiers = (Classifier[]) value[0];
    m_Header            = (Instances) value[1];
    m_Normalize         = (double[]) value[2];
  }

  /**
   * Cleans up data structures, frees up memory.
   */
  @Override
  public void cleanUp() {
    m_TrainingData = null;
  }
}
