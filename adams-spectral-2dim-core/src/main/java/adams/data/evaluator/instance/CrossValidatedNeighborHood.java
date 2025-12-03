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
 * CrossValidatedNeighborHood.java
 * Copyright (C) 2016-2025 University of Waikato, Hamilton, New Zealand
 */
package adams.data.evaluator.instance;

import adams.core.Randomizable;
import adams.core.StoppableWithFeedback;
import adams.flow.core.EvaluationHelper;
import adams.flow.core.EvaluationStatistic;
import weka.classifiers.Classifier;
import weka.classifiers.StoppableEvaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.neighboursearch.LinearNNSearch;
import weka.core.neighboursearch.NearestNeighbourSearch;

import java.util.Random;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Evaluator that cross-validates the specified classifier on the neighborhood determined for the instance under evaluation. Outputs the specified measure as evaluation value.
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
 * &nbsp;&nbsp;&nbsp;default: NaN
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
 * <pre>-search &lt;weka.core.neighboursearch.NearestNeighbourSearch&gt; (property: search)
 * &nbsp;&nbsp;&nbsp;The nearest neighbor search to use.
 * &nbsp;&nbsp;&nbsp;default: weka.core.neighboursearch.LinearNNSearch -A \"weka.core.EuclideanDistance -R first-last\"
 * </pre>
 * 
 * <pre>-classifier &lt;weka.classifiers.Classifier&gt; (property: classifier)
 * &nbsp;&nbsp;&nbsp;The classifier to evaluate on the neighborhood.
 * &nbsp;&nbsp;&nbsp;default: weka.classifiers.functions.LinearRegression -S 1 -C -R 1.0E-8 -num-decimal-places 4
 * </pre>
 * 
 * <pre>-num-neighbors &lt;int&gt; (property: numNeighbors)
 * &nbsp;&nbsp;&nbsp;The number of neighbors to use in the neighborhood.
 * &nbsp;&nbsp;&nbsp;default: 100
 * &nbsp;&nbsp;&nbsp;minimum: 1
 * </pre>
 * 
 * <pre>-folds &lt;int&gt; (property: folds)
 * &nbsp;&nbsp;&nbsp;The number of folds to use for cross-validation; performs leave-one-out 
 * &nbsp;&nbsp;&nbsp;cross-validation if less than 2.
 * &nbsp;&nbsp;&nbsp;default: 10
 * &nbsp;&nbsp;&nbsp;minimum: -1
 * </pre>
 * 
 * <pre>-seed &lt;long&gt; (property: seed)
 * &nbsp;&nbsp;&nbsp;The seed value for the cross-validation.
 * &nbsp;&nbsp;&nbsp;default: 1
 * </pre>
 * 
 * <pre>-statistic &lt;NUMBER_CORRECT|NUMBER_INCORRECT|NUMBER_UNCLASSIFIED|PERCENT_CORRECT|PERCENT_INCORRECT|PERCENT_UNCLASSIFIED|KAPPA_STATISTIC|MEAN_ABSOLUTE_ERROR|ROOT_MEAN_SQUARED_ERROR|RELATIVE_ABSOLUTE_ERROR|ROOT_RELATIVE_SQUARED_ERROR|CORRELATION_COEFFICIENT|SF_PRIOR_ENTROPY|SF_SCHEME_ENTROPY|SF_ENTROPY_GAIN|SF_MEAN_PRIOR_ENTROPY|SF_MEAN_SCHEME_ENTROPY|SF_MEAN_ENTROPY_GAIN|KB_INFORMATION|KB_MEAN_INFORMATION|KB_RELATIVE_INFORMATION|TRUE_POSITIVE_RATE|NUM_TRUE_POSITIVES|FALSE_POSITIVE_RATE|NUM_FALSE_POSITIVES|TRUE_NEGATIVE_RATE|NUM_TRUE_NEGATIVES|FALSE_NEGATIVE_RATE|NUM_FALSE_NEGATIVES|IR_PRECISION|IR_RECALL|F_MEASURE|MATTHEWS_CORRELATION_COEFFICIENT|AREA_UNDER_ROC|AREA_UNDER_PRC|WEIGHTED_TRUE_POSITIVE_RATE|WEIGHTED_FALSE_POSITIVE_RATE|WEIGHTED_TRUE_NEGATIVE_RATE|WEIGHTED_FALSE_NEGATIVE_RATE|WEIGHTED_IR_PRECISION|WEIGHTED_IR_RECALL|WEIGHTED_F_MEASURE|WEIGHTED_MATTHEWS_CORRELATION_COEFFICIENT|WEIGHTED_AREA_UNDER_ROC|WEIGHTED_AREA_UNDER_PRC&gt; (property: statistic)
 * &nbsp;&nbsp;&nbsp;The statistic to use as evaluation output.
 * &nbsp;&nbsp;&nbsp;default: ROOT_MEAN_SQUARED_ERROR
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class CrossValidatedNeighborHood
  extends AbstractNearestNeighborBasedEvaluator
  implements Randomizable, WekaClassifierBasedEvaluator, StoppableWithFeedback {

  /** for serialization. */
  private static final long serialVersionUID = -6086808426732510366L;

  /** the size of the neighborhood. */
  protected int m_NumNeighbors;

  /** the classifier to cross-validate. */
  protected Classifier m_Classifier;

  /** the number of folds. */
  protected int m_Folds;

  /** the seed value. */
  protected long m_Seed;

  /** the measure to output as evaluation. */
  protected EvaluationStatistic m_Statistic;

  /** whether the execution was stopped. */
  protected boolean m_Stopped;

  /** the current evaluation. */
  protected transient StoppableEvaluation m_Evaluation;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Evaluator that cross-validates the specified classifier on the "
        + "neighborhood determined for the instance under evaluation. "
        + "Outputs the specified measure as evaluation value.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "classifier", "classifier",
      getDefaultClassifier());

    m_OptionManager.add(
      "num-neighbors", "numNeighbors",
      100, 1, null);

    m_OptionManager.add(
      "folds", "folds",
      10, -1, null);

    m_OptionManager.add(
      "seed", "seed",
      1L);

    m_OptionManager.add(
      "statistic", "statistic",
      EvaluationStatistic.ROOT_MEAN_SQUARED_ERROR);
  }

  /**
   * Returns the default search algorithm to use.
   *
   * @return		the default
   */
  protected NearestNeighbourSearch getDefaultSearch() {
    return new LinearNNSearch();
  }

  /**
   * Sets the number of neighbors to use in the neighborhood.
   *
   * @param value 	the number of neighbors
   */
  public void setNumNeighbors(int value) {
    if (getOptionManager().isValid("numNeighbors", value)) {
      m_NumNeighbors = value;
      reset();
    }
  }

  /**
   * Returns the number of neighbors to use in the neighborhood.
   *
   * @return 		the number of neighbors
   */
  public int getNumNeighbors() {
    return m_NumNeighbors;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String numNeighborsTipText() {
    return "The number of neighbors to use in the neighborhood.";
  }

  /**
   * Returns the default classifier.
   *
   * @return		the default
   */
  protected Classifier getDefaultClassifier() {
    LinearRegression	result;

    result = new LinearRegression();
    result.setEliminateColinearAttributes(false);
    result.setAttributeSelectionMethod(new SelectedTag(LinearRegression.SELECTION_NONE, LinearRegression.TAGS_SELECTION));

    return result;
  }

  /**
   * Sets the classifier to use.
   *
   * @param value 	the classifier
   */
  public void setClassifier(Classifier value) {
    m_Classifier = value;
    reset();
  }

  /**
   * Returns the classifier in use.
   *
   * @return 		the classifier
   */
  public Classifier getClassifier() {
    return m_Classifier;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String classifierTipText() {
    return "The classifier to evaluate on the neighborhood.";
  }

  /**
   * Sets the number of folds.
   *
   * @param value 	the number of folds, LOO if < 2
   */
  public void setFolds(int value) {
    if (getOptionManager().isValid("folds", value)) {
      m_Folds = value;
      reset();
    }
  }

  /**
   * Returns the number of folds.
   *
   * @return 		the number of folds, LOO if < 2
   */
  public int getFolds() {
    return m_Folds;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String foldsTipText() {
    return
      "The number of folds to use for cross-validation; performs leave-one-out "
	+ "cross-validation if less than 2.";
  }

  /**
   * Sets the seed value for cross-validation.
   *
   * @param value 	the seed
   */
  public void setSeed(long value) {
    m_Seed = value;
    reset();
  }

  /**
   * Returns the seed value for cross-validation.
   *
   * @return 		the seed
   */
  public long getSeed() {
    return m_Seed;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String seedTipText() {
    return "The seed value for the cross-validation.";
  }

  /**
   * Sets the statistic to use as evaluation output.
   *
   * @param value 	the statistic
   */
  public void setStatistic(EvaluationStatistic value) {
    m_Statistic = value;
    reset();
  }

  /**
   * Returns the statistic used as evaluation output.
   *
   * @return 		the statistic
   */
  public EvaluationStatistic getStatistic() {
    return m_Statistic;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String statisticTipText() {
    return "The statistic to use as evaluation output.";
  }

  /**
   * Returns the member variables to serialize to a file.
   *
   * @return		the objects to serialize
   */
  @Override
  public Object[] retrieveSerializationSetup() {
    return new Object[]{
      m_ActualSearch,
      m_Header,
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
    m_ActualSearch = (NearestNeighbourSearch) value[0];
    m_Header       = (Instances) value[1];
  }

  /**
   * Builds the evaluator.
   *
   * @param data	the instance to check
   * @return		always true
   */
  @Override
  protected boolean performBuild(Instances data) {
    if (!initSearch(data))
      return false;

    m_SerializableObjectHelper.saveSetup();

    return true;
  }

  /**
   * Performs the actual evaluation.
   *
   * @param data	the instance to check
   * @return		evaluation metric, {@link #m_MissingEvaluation} in case
   * 			the class value is missing
   */
  protected Float performEvaluate(Instance data) {
    float			result;
    Instances			neighbors;
    int				folds;

    m_Stopped = false;

    try {
      // get neighborhood
      neighbors = m_ActualSearch.kNearestNeighbours(data, m_NumNeighbors);
      if (m_Folds < 2)
	folds = neighbors.numInstances();
      else
        folds = Math.min(m_Folds, neighbors.numInstances());
      m_Evaluation = new StoppableEvaluation(neighbors);
      m_Evaluation.crossValidateModel(m_Classifier, neighbors, folds, new Random(m_Seed));
      result = (float) EvaluationHelper.getValue(m_Evaluation, m_Statistic, 0);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to cross-validate neighborhood!", e);
      result = m_MissingEvaluation;
    }

    m_Evaluation = null;
    if (m_Stopped)
      result = m_MissingEvaluation;

    return result;
  }

  /**
   * Stops the execution. No message set.
   */
  @Override
  public void stopExecution() {
    if (m_Evaluation != null)
      m_Evaluation.stopExecution();
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
