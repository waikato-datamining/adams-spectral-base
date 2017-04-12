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
 * RemoveOutliers.java
 * Copyright (C) 2015-2016 University of Waikato, Hamilton, NZ
 */

package adams.data.cleaner.instance;

import adams.core.Performance;
import adams.core.Randomizable;
import adams.core.ThreadLimiter;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.spreadsheet.SpreadSheetColumnIndex;
import adams.flow.container.WekaTrainTestSetContainer;
import adams.flow.control.removeoutliers.AbstractOutlierDetector;
import adams.flow.control.removeoutliers.Null;
import adams.flow.core.ActorUtils;
import adams.flow.core.Token;
import adams.flow.standalone.JobRunnerSetup;
import adams.flow.transformer.WekaPredictionsToSpreadSheet;
import adams.multiprocess.JobList;
import adams.multiprocess.JobRunner;
import adams.multiprocess.LocalJobRunner;
import adams.multiprocess.WekaCrossValidationJob;
import weka.classifiers.AggregateableEvaluationExt;
import weka.classifiers.Classifier;
import weka.classifiers.CrossValidationFoldGenerator;
import weka.classifiers.CrossValidationHelper;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegressionJ;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Cross-validates the specified classifier on the incoming data and applies the outlier detector to the actual vs predicted data to remove the outliers.<br>
 * NB: only works on full dataset, not instance by instance.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-pre-filter &lt;weka.filters.Filter&gt; (property: preFilter)
 * &nbsp;&nbsp;&nbsp;The filter to use for pre-filtering the data.
 * &nbsp;&nbsp;&nbsp;default: weka.filters.AllFilter
 * </pre>
 * 
 * <pre>-classifier &lt;weka.classifiers.Classifier&gt; (property: classifier)
 * &nbsp;&nbsp;&nbsp;The classifier to use for generating the actual vs predicted data.
 * &nbsp;&nbsp;&nbsp;default: weka.classifiers.functions.LinearRegressionJ -S 0 -R 1.0E-8
 * </pre>
 * 
 * <pre>-seed &lt;long&gt; (property: seed)
 * &nbsp;&nbsp;&nbsp;The seed value for the cross-validation.
 * &nbsp;&nbsp;&nbsp;default: 1
 * </pre>
 * 
 * <pre>-num-folds &lt;int&gt; (property: numFolds)
 * &nbsp;&nbsp;&nbsp;The number of folds to use in the cross-validation.
 * &nbsp;&nbsp;&nbsp;default: 10
 * &nbsp;&nbsp;&nbsp;minimum: 2
 * </pre>
 * 
 * <pre>-num-threads &lt;int&gt; (property: numThreads)
 * &nbsp;&nbsp;&nbsp;The number of threads to use for cross-validation; -1 = number of CPUs&#47;cores;
 * &nbsp;&nbsp;&nbsp; 0 or 1 = sequential execution.
 * &nbsp;&nbsp;&nbsp;default: 1
 * &nbsp;&nbsp;&nbsp;minimum: -1
 * </pre>
 * 
 * <pre>-detector &lt;adams.flow.control.removeoutliers.AbstractOutlierDetector&gt; (property: detector)
 * &nbsp;&nbsp;&nbsp;The outlier detector to use.
 * &nbsp;&nbsp;&nbsp;default: adams.flow.control.removeoutliers.Null
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class RemoveOutliers
  extends AbstractCleaner
  implements Randomizable, ThreadLimiter {

  private static final long serialVersionUID = -43765084294892078L;

  /** the classifier to use for evaluation. */
  protected Classifier m_Classifier;

  /** the seed value. */
  protected long m_Seed;

  /** the number of folds to use. */
  protected int m_NumFolds;

  /** the outlier detector to use. */
  protected AbstractOutlierDetector m_Detector;

  /** the number of threads to use for parallel execution. */
  protected int m_NumThreads;

  /** the jobrunner setup. */
  protected transient JobRunnerSetup m_JobRunnerSetup;

  /** the runner in use. */
  protected transient JobRunner m_JobRunner;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Cross-validates the specified classifier on the incoming data and "
	+ "applies the outlier detector to the actual vs predicted data to "
	+ "remove the outliers.\n"
	+ "NB: only works on full dataset, not instance by instance.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "classifier", "classifier",
      new LinearRegressionJ());

    m_OptionManager.add(
      "seed", "seed",
      1L);

    m_OptionManager.add(
      "num-folds", "numFolds",
      10, 2, null);

    m_OptionManager.add(
      "num-threads", "numThreads",
      1);

    m_OptionManager.add(
      "detector", "detector",
      new Null());
  }

  /**
   * Sets the classifier.
   *
   * @param value	the classifier
   */
  public void setClassifier(Classifier value) {
    m_Classifier = value;
    reset();
  }

  /**
   * Returns the classifier.
   *
   * @return  		the classifier
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
    return "The classifier to use for generating the actual vs predicted data.";
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
   * Sets the number of folds to use.
   *
   * @param value	the folds
   */
  public void setNumFolds(int value) {
    m_NumFolds = value;
    reset();
  }

  /**
   * Returns the number of folds to use in CV.
   *
   * @return  		the folds
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
    return "The number of folds to use in the cross-validation.";
  }

  /**
   * Sets the number of threads to use for cross-validation.
   *
   * @param value 	the number of threads: -1 = # of CPUs/cores; 0/1 = sequential execution
   */
  public void setNumThreads(int value) {
    m_NumThreads = value;
    reset();
  }

  /**
   * Returns the number of threads to use for cross-validation.
   *
   * @return 		the number of threads: -1 = # of CPUs/cores; 0/1 = sequential execution
   */
  public int getNumThreads() {
    return m_NumThreads;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String numThreadsTipText() {
    return Performance.getNumThreadsHelp();
  }

  /**
   * Sets the detector.
   *
   * @param value	the detector
   */
  public void setDetector(AbstractOutlierDetector value) {
    m_Detector = value;
    reset();
  }

  /**
   * Returns the detector.
   *
   * @return  		the detector
   */
  public AbstractOutlierDetector getDetector() {
    return m_Detector;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String detectorTipText() {
    return "The outlier detector to use.";
  }

  /**
   * Performs the some pre-checks whether the data is actually suitable.
   *
   * @param data	the instances to clean
   */
  @Override
  protected void preCheck(Instances data) {
    super.preCheck(data);

    if (m_FlowContext != null)
      m_JobRunnerSetup = (JobRunnerSetup) ActorUtils.findClosestType(m_FlowContext, JobRunnerSetup.class);
    else
      m_JobRunnerSetup = null;
  }

  /**
   * Performs the actual check.
   *
   * @param data	the instance to check
   * @return		always null
   */
  @Override
  protected String performCheck(Instance data) {
    return null;
  }

  /**
   * Cross-validates the classifier on the given data.
   *
   * @param data	the data to use for cross-validation
   * @param folds	the number of folds
   * @return		the evaluation
   * @throws Exception	if cross-validation fails
   */
  protected Evaluation crossValidate(Instances data, int folds) throws Exception {
    String 				msg;
    int 				numThreads;
    Evaluation				eval;
    AggregateableEvaluationExt 		evalAgg;
    CrossValidationFoldGenerator	generator;
    JobList<WekaCrossValidationJob>	list;
    WekaCrossValidationJob 		job;
    WekaTrainTestSetContainer		cont;
    int					i;

    numThreads = Performance.determineNumThreads(m_NumThreads);

    if (numThreads == 1) {
      eval = new Evaluation(data);
      eval.setDiscardPredictions(false);
      eval.crossValidateModel(m_Classifier, data, folds, new Random(m_Seed));
      return eval;
    }
    else {
      generator = new CrossValidationFoldGenerator(data, folds, m_Seed, true);
      if (m_JobRunnerSetup == null)
	m_JobRunner = new LocalJobRunner<WekaCrossValidationJob>();
      else
	m_JobRunner = m_JobRunnerSetup.newInstance();
      if (m_JobRunner instanceof ThreadLimiter)
	((ThreadLimiter) m_JobRunner).setNumThreads(m_NumThreads);
      list = new JobList<>();
      while (generator.hasNext()) {
	cont = generator.next();
	job = new WekaCrossValidationJob(
	  m_Classifier,
	  (Instances) cont.getValue(WekaTrainTestSetContainer.VALUE_TRAIN),
	  (Instances) cont.getValue(WekaTrainTestSetContainer.VALUE_TEST),
	  (Integer) cont.getValue(WekaTrainTestSetContainer.VALUE_FOLD_NUMBER),
	  false);
	list.add(job);
      }
      m_JobRunner.add(list);
      m_JobRunner.start();
      m_JobRunner.stop();
      // aggregate data
      msg     = null;
      evalAgg = new AggregateableEvaluationExt(data);
      for (i = 0; i < m_JobRunner.getJobs().size(); i++) {
	job = (WekaCrossValidationJob) m_JobRunner.getJobs().get(i);
	if (job.getEvaluation() == null) {
	  msg = "Fold #" + (i + 1) + " failed to evaluate";
	  if (!job.hasExecutionError())
	    msg += "?";
	  else
	    msg += ":\n" + job.getExecutionError();
	  break;
	}
	evalAgg.aggregate(job.getEvaluation());
	job.cleanUp();
      }
      if (msg != null)
	getLogger().severe(msg);
      list.cleanUp();
      m_JobRunner.cleanUp();
      m_JobRunner = null;
      return evalAgg;
    }
  }

  /**
   * Turns the predictions of the evaluation object into a spreadsheet.
   *
   * @param eval	the evaluation object to convert
   * @return		the generated spreadsheet
   */
  protected SpreadSheet evaluationToSpreadSheet(Evaluation eval) {
    SpreadSheet				result;
    WekaPredictionsToSpreadSheet 	conv;
    String				msg;
    Token				token;

    conv = new WekaPredictionsToSpreadSheet();
    msg = conv.setUp();
    if (msg != null) {
      getLogger().severe("Failed to convert predictions to spreadsheet (setUp): " + msg);
      conv.cleanUp();
      return null;
    }
    conv.input(new Token(eval));
    msg = conv.execute();
    if (msg != null) {
      getLogger().severe("Failed to convert predictions to spreadsheet (execute): " + msg);
      conv.cleanUp();
      return null;
    }
    if (conv.hasPendingOutput()) {
      token = conv.output();
      result = (SpreadSheet) token.getPayload();
    }
    else {
      getLogger().severe("No output data generated from predictions!");
      return null;
    }

    return result;
  }

  /**
   * Performs the actual check.
   *
   * @param data	the instance to check
   * @return		null if ok, otherwise error message
   */
  @Override
  protected Instances performClean(Instances data) {
    Instances		result;
    Evaluation		eval;
    SpreadSheet		sheet;
    Set<Integer>	outliers;
    List<Integer>	sorted;
    int			i;
    int			folds;
    int[]		indices;

    // cross-validate
    folds = m_NumFolds;
    if (folds == -1)
      folds = data.numInstances();
    try {
      eval = crossValidate(data, folds);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to cross-validate!", e);
      return null;
    }

    // create spreadsheet
    sheet = evaluationToSpreadSheet(eval);
    if (sheet == null)
      return null;

    // apply detector
    outliers = m_Detector.detect(sheet, new SpreadSheetColumnIndex("Actual"), new SpreadSheetColumnIndex("Predicted"));
    if (outliers == null) {
      getLogger().severe("Failed to detect outliers!");
      return null;
    }
    else if (isLoggingEnabled()) {
      sorted = new ArrayList<>(outliers);
      Collections.sort(sorted);
      getLogger().info("Outliers (0-based index): " + sorted);
    }

    // clean dataset
    indices = CrossValidationHelper.crossValidationIndices(data, folds, new Random(m_Seed));
    result  = new Instances(data, data.numInstances() - outliers.size());
    for (i = 0; i < indices.length; i++) {
      if (outliers.contains(i))
	continue;
      result.add((Instance) data.instance(indices[i]).copy());
    }

    return result;
  }
}
