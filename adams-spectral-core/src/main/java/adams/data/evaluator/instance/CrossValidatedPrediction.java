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
 * CrossValidatedPrediction.java
 * Copyright (C) 2016 University of Waikato, Hamilton, New Zealand
 */
package adams.data.evaluator.instance;

import adams.core.Randomizable;
import adams.core.option.OptionUtils;
import adams.data.statistics.StatUtils;
import adams.flow.container.WekaTrainTestSetContainer;
import gnu.trove.list.array.TDoubleArrayList;
import weka.classifiers.Classifier;
import weka.classifiers.CrossValidationFoldGenerator;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.neighboursearch.LinearNNSearch;
import weka.core.neighboursearch.NearestNeighbourSearch;

import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Evaluator that (kind of) cross-validates the specified classifier on the neighborhood determined for the instance under evaluation.<br>
 * A classifier is built on each of the training sets that is generated for the cross-validation. Each classifier makes a prediction for the Instance that is currently being evaluated, recording the prediction. From the recorded predictions the statistic is computed and output as evaluation value.
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
 * <pre>-statistic &lt;STDEV|RANGE&gt; (property: statistic)
 * &nbsp;&nbsp;&nbsp;The statistic to use as evaluation output.
 * &nbsp;&nbsp;&nbsp;default: STDEV
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class CrossValidatedPrediction
  extends AbstractNearestNeighborBasedEvaluator
  implements Randomizable, WekaClassifierBasedEvaluator {

  /** for serialization. */
  private static final long serialVersionUID = -6086808426732510366L;

  /**
   * The type of statistic to return.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 2242 $
   */
  public enum StatisticType {
    STDEV,
    RANGE
  }

  /** the size of the neighborhood. */
  protected int m_NumNeighbors;

  /** the classifier to cross-validate. */
  protected Classifier m_Classifier;

  /** the number of folds. */
  protected int m_Folds;

  /** the seed value. */
  protected long m_Seed;

  /** the measure to output as evaluation. */
  protected StatisticType m_Statistic;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Evaluator that (kind of) cross-validates the specified classifier on the "
        + "neighborhood determined for the instance under evaluation.\n"
	+ "A classifier is built on each of the training sets that is generated "
	+ "for the cross-validation. Each classifier makes a prediction for the "
        + "Instance that is currently being evaluated, recording the prediction. "
        + "From the recorded predictions the statistic is computed and output as "
	+ "evaluation value.";
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
      StatisticType.STDEV);
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
  public void setStatistic(StatisticType value) {
    m_Statistic = value;
    reset();
  }

  /**
   * Returns the statistic used as evaluation output.
   *
   * @return 		the statistic
   */
  public StatisticType getStatistic() {
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
    Float				result;
    Instances				neighbors;
    int					folds;
    CrossValidationFoldGenerator	generator;
    WekaTrainTestSetContainer		cont;
    TDoubleArrayList			values;
    Classifier				cls;

    result = m_MissingEvaluation;

    try {
      // get neighborhood
      neighbors = m_ActualSearch.kNearestNeighbours(data, m_NumNeighbors);
      if (m_Folds < 2)
	folds = neighbors.numInstances();
      else
        folds = Math.min(m_Folds, neighbors.numInstances());
      generator = new CrossValidationFoldGenerator(neighbors, folds, m_Seed, true);
      values    = new TDoubleArrayList();
      while (generator.hasNext()) {
	cont = generator.next();
	cls  = (Classifier) OptionUtils.shallowCopy(m_Classifier);
	cls.buildClassifier((Instances) cont.getValue(WekaTrainTestSetContainer.VALUE_TRAIN));
	values.add(cls.classifyInstance(data));
      }
      switch (m_Statistic) {
	case STDEV:
	  result = (float) StatUtils.stddev(values.toArray(), true);
	  break;
	case RANGE:
	  result = (float) (StatUtils.max(values.toArray()) - StatUtils.min(values.toArray()));
	  break;
	default:
	  throw new IllegalStateException("Unhandled statistic: " + m_Statistic);
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to cross-validate neighborhood!", e);
      result = m_MissingEvaluation;
    }

    return result;
  }
}
