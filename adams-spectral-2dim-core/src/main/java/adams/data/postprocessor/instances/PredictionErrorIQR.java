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
 * PredictionErrorIQR.java
 * Copyright (C) 2024 University of Waikato, Hamilton, New Zealand
 */

package adams.data.postprocessor.instances;

import adams.core.Performance;
import adams.core.Randomizable;
import adams.core.StoppableWithFeedback;
import adams.core.ThreadLimiter;
import adams.core.base.BaseDouble;
import adams.core.base.BaseInteger;
import adams.core.option.OptionUtils;
import adams.data.postprocessor.PostProcessorDetails;
import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.statistics.StatUtils;
import adams.flow.core.Actor;
import adams.multiprocess.WekaCrossValidationExecution;
import weka.classifiers.Classifier;
import weka.classifiers.CrossValidationHelper;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.rules.ZeroR;
import weka.core.Instance;
import weka.core.Instances;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Post-processor that removes outliers using a coarse IQR approach on the predictions errors of one or more classifiers.<br>
 * <br>
 * parameters:<br>
 * - list of classifiers<br>
 * - foreach classifier; number or folds, an IQR multiplier, number of iterations or number of consecutive non-removal iterations before stop.<br>
 * <br>
 * algorithm:<br>
 * foreach classifier<br>
 * loop<br>
 * do xval, get predictions, remove all examples where error &gt; percentile75+IQR*multiplier of errors<br>
 * stop if done num_iterations, or consecutive zero removals<br>
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * &nbsp;&nbsp;&nbsp;min-user-mode: Expert
 * </pre>
 *
 * <pre>-classifier &lt;weka.classifiers.Classifier&gt; [-classifier ...] (property: classifiers)
 * &nbsp;&nbsp;&nbsp;The classifiers to cross-validate internally, using their predictions errors
 * &nbsp;&nbsp;&nbsp;to determine outliers.
 * &nbsp;&nbsp;&nbsp;default: weka.classifiers.rules.ZeroR
 * </pre>
 *
 * <pre>-num-folds &lt;adams.core.base.BaseInteger&gt; [-num-folds ...] (property: numFolds)
 * &nbsp;&nbsp;&nbsp;The number of cross-validation folds per classifier.
 * &nbsp;&nbsp;&nbsp;default: 10
 * </pre>
 *
 * <pre>-iqr-multiplier &lt;adams.core.base.BaseDouble&gt; [-iqr-multiplier ...] (property: IQRMultiplier)
 * &nbsp;&nbsp;&nbsp;The multiplier for the IQR filter to determine outlier values.
 * &nbsp;&nbsp;&nbsp;default: 0.1
 * </pre>
 *
 * <pre>-num-iterations &lt;adams.core.base.BaseInteger&gt; [-num-iterations ...] (property: numIterations)
 * &nbsp;&nbsp;&nbsp;The number of iterations per classifier.
 * &nbsp;&nbsp;&nbsp;default: 0
 * </pre>
 *
 * <pre>-max-non-removal-iterations &lt;adams.core.base.BaseInteger&gt; [-max-non-removal-iterations ...] (property: maxNonRemovalIterations)
 * &nbsp;&nbsp;&nbsp;The maximum number non-removal iterations per classifier.
 * &nbsp;&nbsp;&nbsp;default: 2
 * </pre>
 *
 * <pre>-seed &lt;long&gt; (property: seed)
 * &nbsp;&nbsp;&nbsp;The seed value for the randomization.
 * &nbsp;&nbsp;&nbsp;default: 1
 * </pre>
 *
 * <pre>-use-absolute-error &lt;boolean&gt; (property: useAbsoluteError)
 * &nbsp;&nbsp;&nbsp;If set to true, then the error will be absolute (no direction).
 * &nbsp;&nbsp;&nbsp;default: true
 * </pre>
 *
 * <pre>-num-threads &lt;int&gt; (property: numThreads)
 * &nbsp;&nbsp;&nbsp;The number of threads to use for parallel execution; &gt; 0: specific number
 * &nbsp;&nbsp;&nbsp;of cores to use (capped by actual number of cores available, 1 = sequential
 * &nbsp;&nbsp;&nbsp;execution); = 0: number of cores; &lt; 0: number of free cores (eg -2 means
 * &nbsp;&nbsp;&nbsp;2 free cores; minimum of one core is used); overrides the value defined
 * &nbsp;&nbsp;&nbsp;by the fold generator scheme.
 * &nbsp;&nbsp;&nbsp;default: 1
 * </pre>
 *
 <!-- options-end -->
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 * @author Dale (dale at waikato dot ac dot nz)
 */
public class PredictionErrorIQR
  extends AbstractPostProcessor
  implements Randomizable, StoppableWithFeedback, ThreadLimiter, PostProcessorDetails<SpreadSheet> {

  private static final long serialVersionUID = -3236239648802264362L;

  /** the classifiers to use for internal cross-validation. */
  protected Classifier[] m_Classifiers;

  /** the number of folds per classifier. */
  protected BaseInteger[] m_NumFolds;

  /** the IQR multiplier per classifier. */
  protected BaseDouble[] m_IQRMultiplier;

  /** the number of iterations per classifier. */
  protected BaseInteger[] m_NumIterations;

  /** the maximum number of non-removal iterations per classifier. */
  protected BaseInteger[] m_MaxNonRemovalIterations;

  /** the seed value. */
  protected long m_Seed;

  /** the random number generator in use. */
  protected transient Random m_Random;

  /** whether to use absolute errors. */
  protected boolean m_UseAbsoluteError;

  /** the number of threads to use for parallel execution. */
  protected int m_NumThreads;

  /** the current evaluation. */
  protected transient WekaCrossValidationExecution m_CrossValidation;

  /** whether the execution was stopped. */
  protected boolean m_Stopped;

  /** the flow context. */
  protected transient Actor m_FlowContext;

  /** the count before. */
  protected int m_CountBefore;

  /** the count after. */
  protected int m_CountAfter;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Post-processor that removes outliers using a coarse IQR approach on the predictions errors of one or more classifiers.\n\n"
	+ "parameters:\n"
	+ "- list of classifiers\n"
	+ "- foreach classifier; number or folds, an IQR multiplier, number of iterations or number of consecutive non-removal iterations before stop.\n"
	+ "\n"
	+ "algorithm:\n"
	+ "foreach classifier\n"
	+ "loop\n"
	+ "do xval, get predictions, remove all examples where error > percentile75+IQR*multiplier of errors\n"
	+ "stop if done num_iterations, or consecutive zero removals\n";
  }

  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "classifier", "classifiers",
      new Classifier[]{new ZeroR()});

    m_OptionManager.add(
      "num-folds", "numFolds",
      new BaseInteger[]{new BaseInteger(10)});

    m_OptionManager.add(
      "iqr-multiplier", "IQRMultiplier",
      new BaseDouble[]{new BaseDouble(0.1)});

    m_OptionManager.add(
      "num-iterations", "numIterations",
      new BaseInteger[]{new BaseInteger(0)});

    m_OptionManager.add(
      "max-non-removal-iterations", "maxNonRemovalIterations",
      new BaseInteger[]{new BaseInteger(2)});

    m_OptionManager.add(
      "seed", "seed",
      1L);

    m_OptionManager.add(
      "use-absolute-error", "useAbsoluteError",
      true);

    m_OptionManager.add(
      "num-threads", "numThreads",
      1);
  }

  /**
   * Adjusts the arrays to the new length.
   *
   * @param length	the new length
   */
  protected void adjustArrays(int length) {
    m_Classifiers             = (Classifier[])  adams.core.Utils.adjustArray(m_Classifiers,             length, new ZeroR());
    m_NumFolds                = (BaseInteger[]) adams.core.Utils.adjustArray(m_NumFolds,                length, new BaseInteger(10));
    m_IQRMultiplier           = (BaseDouble[])  adams.core.Utils.adjustArray(m_IQRMultiplier,           length, new BaseDouble(0.1));
    m_NumIterations           = (BaseInteger[]) adams.core.Utils.adjustArray(m_NumIterations,           length, new BaseInteger(0));
    m_MaxNonRemovalIterations = (BaseInteger[]) adams.core.Utils.adjustArray(m_MaxNonRemovalIterations, length, new BaseInteger(0));
  }

  /**
   * Sets the classifiers to use internally.
   *
   * @param value 	the classifiers
   */
  public void setClassifiers(Classifier[] value) {
    m_Classifiers = value;
    adjustArrays(m_Classifiers.length);
    reset();
  }

  /**
   * Returns the classifiers to use internally.
   *
   * @return 		the classifiers
   */
  public Classifier[] getClassifiers() {
    return m_Classifiers;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the gui
   */
  public String classifiersTipText() {
    return "The classifiers to cross-validate internally, using their predictions errors to determine outliers.";
  }

  /**
   * Sets the number of cross-validation folds per classifier.
   *
   * @param value 	the folds
   */
  public void setNumFolds(BaseInteger[] value) {
    m_NumFolds = value;
    adjustArrays(m_NumFolds.length);
    reset();
  }

  /**
   * Returns the number of cross-validation folds per classifier.
   *
   * @return 		the folds
   */
  public BaseInteger[] getNumFolds() {
    return m_NumFolds;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the gui
   */
  public String numFoldsTipText() {
    return "The number of cross-validation folds per classifier.";
  }

  /**
   * Sets the IQR multipliers per classifier.
   *
   * @param value 	the multipliers
   */
  public void setIQRMultiplier(BaseDouble[] value) {
    m_IQRMultiplier = value;
    adjustArrays(m_IQRMultiplier.length);
    reset();
  }

  /**
   * Returns the IQR multipliers per classifier.
   *
   * @return 		the multipliers
   */
  public BaseDouble[] getIQRMultiplier() {
    return m_IQRMultiplier;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the gui
   */
  public String IQRMultiplierTipText() {
    return "The multiplier for the IQR filter to determine outlier values.";
  }

  /**
   * Sets the number of iterations per classifier.
   *
   * @param value 	the iterations
   */
  public void setNumIterations(BaseInteger[] value) {
    m_NumIterations = value;
    adjustArrays(m_NumIterations.length);
    reset();
  }

  /**
   * Returns the number of iterations per classifier.
   *
   * @return 		the iterations
   */
  public BaseInteger[] getNumIterations() {
    return m_NumIterations;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the gui
   */
  public String numIterationsTipText() {
    return "The number of iterations per classifier.";
  }

  /**
   * Sets the maximum number of non-removal iterations per classifier.
   *
   * @param value 	the max
   */
  public void setMaxNonRemovalIterations(BaseInteger[] value) {
    m_MaxNonRemovalIterations = value;
    adjustArrays(m_MaxNonRemovalIterations.length);
    reset();
  }

  /**
   * Returns the maximum number of non-removal iterations per classifier.
   *
   * @return 		the max
   */
  public BaseInteger[] getMaxNonRemovalIterations() {
    return m_MaxNonRemovalIterations;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the gui
   */
  public String maxNonRemovalIterationsTipText() {
    return "The maximum number non-removal iterations per classifier.";
  }

  /**
   * Sets the seed value.
   *
   * @param value	the seed
   */
  @Override
  public void setSeed(long value) {
    m_Seed = value;
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
    return "The seed value for the randomization.";
  }

  /**
   * Sets whether to use an absolute error (ie no direction).
   *
   * @param value	true if to use absolute error
   */
  public void setUseAbsoluteError(boolean value) {
    m_UseAbsoluteError = value;
    reset();
  }

  /**
   * Returns whether to use an absolute error (ie no direction).
   *
   * @return		true if to use absolute error
   */
  public boolean getUseAbsoluteError() {
    return m_UseAbsoluteError;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String useAbsoluteErrorTipText() {
    return "If set to true, then the error will be absolute (no direction).";
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
    return Performance.getNumThreadsHelp() + "; overrides the value defined by the fold generator scheme.";
  }

  /**
   * Performs some pre-checks whether the data is actually suitable.
   *
   * @param data	the dataset to check
   */
  protected void preCheck(Instances data) {
    int		i;

    super.preCheck(data);

    if (data.classIndex() == -1)
      throw new IllegalStateException("No class attribute set!");
    if (!data.classAttribute().isNumeric())
      throw new IllegalStateException("Class attribute not numeric: " + data.classAttribute().name());

    if (m_Classifiers.length == 0)
      throw new IllegalStateException("At least one classifier must be defined!");
    for (i = 0; i < m_IQRMultiplier.length; i++) {
      if (m_IQRMultiplier[i].doubleValue() <= 0.0)
	throw new IllegalStateException("IQR multiplier must be >0 (index #" + m_IQRMultiplier[i] + ")!");
    }
    for (i = 0; i < m_NumIterations.length; i++) {
      if ((m_NumIterations[i].intValue() <= 0) && (m_MaxNonRemovalIterations[i].intValue() <= 0))
	throw new IllegalStateException("Either 'num-iterations' or 'max-non-removal-iterations' must be greater than 0 (pair #" + (i+1) + ")!");
      if ((m_NumIterations[i].intValue() > 0) && (m_MaxNonRemovalIterations[i].intValue() > 0))
	throw new IllegalStateException("Only one of 'num-iterations' and 'max-non-removal-iterations' can be greater than 0 (pair #" + (i+1) + ")!");
    }
  }

  /**
   * Cleans the data using the specified classifier and parameters.
   *
   * @param data		the data to clean
   * @param classifier		the classifier to cross-validate and obtain predictions from
   * @param numFolds		the cross-validation folds
   * @param seed		the seed for randomizing the data for cross-validation
   * @param iqrMultiplier	the multiplier for the IQR outliers
   * @return			the cleaned up data
   */
  protected Instances cleanData(Instances data, Classifier classifier, int numFolds, long seed, double iqrMultiplier) {
    Instances		result;
    String		msg;
    double[]		errors;
    int			i;
    int[]		origIndices;
    List<Prediction> 	preds;
    Prediction		pred;
    double		iqr;
    double		p25;
    double		p75;
    double		upper;

    try {
      // cross-validate
      m_CrossValidation = new WekaCrossValidationExecution();
      m_CrossValidation.setClassifier(classifier);
      m_CrossValidation.setData(data);
      m_CrossValidation.setFolds(numFolds);
      m_CrossValidation.setSeed(m_Seed);
      m_CrossValidation.setNumThreads(m_NumThreads);
      m_CrossValidation.setDiscardPredictions(false);
      m_CrossValidation.setFlowContext(m_FlowContext);
      msg = m_CrossValidation.execute();

      if ((msg != null) || m_Stopped)
	return data;

      // back to original order
      origIndices = CrossValidationHelper.crossValidationIndices(data, numFolds, new Random(seed), false);
      preds       = CrossValidationHelper.alignPredictions(m_CrossValidation.getEvaluation().predictions(), origIndices);

      // compute thresholds
      errors = new double[preds.size()];
      for (i = 0; i < preds.size(); i++) {
	pred = preds.get(i);
	if (m_UseAbsoluteError)
	  errors[i] = Math.abs(pred.actual() - pred.predicted());
	else
	  errors[i] = pred.actual() - pred.predicted();
      }
      p25   = StatUtils.quartile(errors, 0.25);
      p75   = StatUtils.quartile(errors, 0.75);
      iqr   = p75 - p25;
      if (isLoggingEnabled())
	getLogger().info("p25=" + p25 + ", p75=" + p75 + ", iqr=" + iqr + " - " + data.relationName());
      upper = p75 + iqr * iqrMultiplier;
      if (isLoggingEnabled())
	getLogger().info("upper=" + upper + " - " + data.relationName());

      result = new Instances(data, data.numInstances());
      for (i = 0; i < data.numInstances(); i++) {
	if (errors[i] > upper)
	  continue;
	result.add(data.instance(i));
      }
      result.compactify();

      return result;
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "cleanData: Failed to cross-validate?" + " - " + data.relationName(), e);
      return data;
    }
    finally {
      m_CrossValidation = null;
    }
  }

  /**
   * Cleans the data using the specified classifier and parameters.
   *
   * @param data			the data to clean
   * @param classifier			the classifier to cross-validate and obtain predictions from
   * @param numFolds			the cross-validation folds
   * @param seed			the seed for randomizing the data for cross-validation
   * @param iqrMultiplier		the multiplier for the IQR outliers
   * @param numIterations		the number of iterations to perform (ignored if 0)
   * @param maxNonRemovalIterations	the maximum number of iterations with no outliers being removed before stopping (ignored if 0)
   * @return				the cleaned up data
   */
  protected Instances iterate(Instances data, Classifier classifier, int numFolds, long seed, double iqrMultiplier, int numIterations, int maxNonRemovalIterations) {
    Instances	result;
    Instances	old;
    int		i;
    int		count;

    result = data;

    if (numIterations > 0) {
      for (i = 0; i < numIterations; i++) {
	if (m_Stopped)
	  return data;
	if (isLoggingEnabled())
	  getLogger().info("Iteration #" + (i+1) + ": size before=" + result.numInstances() + " - " + data.relationName());
	result = cleanData(result, classifier, numFolds, seed, iqrMultiplier);
	if (isLoggingEnabled())
	  getLogger().info("Iteration #" + (i+1) + ": size after=" + result.numInstances() + " - " + data.relationName());
      }
    }
    else {
      i     = 0;
      count = 0;
      while (true) {
	if (m_Stopped)
	  return data;
	if (isLoggingEnabled())
	  getLogger().info("Iteration #" + (i+1) + ": size before=" + result.numInstances() + " - " + data.relationName());
	old    = result;
	result = cleanData(old, classifier, numFolds, seed, iqrMultiplier);
	if (isLoggingEnabled())
	  getLogger().info("Iteration #" + (i+1) + ": size after=" + result.numInstances() + " - " + data.relationName());

	if (result.numInstances() == old.numInstances()) {
	  count++;
	  if (count >= maxNonRemovalIterations) {
	    if (isLoggingEnabled())
	      getLogger().info("Maximum number of iterations with no removals reached (" + maxNonRemovalIterations + "), exiting!" + " - " + data.relationName());
	    break;
	  }
	}
	else {
	  count = 0;
	}
	i++;
      }
    }

    return result;
  }

  /**
   * Performs the actual postprocessing.
   *
   * @param data the dataset to process
   * @return the processed dataset
   */
  @Override
  protected Instances performPostProcess(Instances data) {
    Instances	result;
    int		i;

    result        = data;
    m_Random      = new Random(m_Seed);
    m_CountBefore = data.numInstances();
    m_CountAfter  = -1;

    for (i = 0; i < m_Classifiers.length; i++) {
      if (m_Stopped) {
	getLogger().warning("Execution stopped, exiting!" + " - " + data.relationName());
	return data;
      }
      if (result.numInstances() == 0) {
	getLogger().warning("No data left, exiting cleaning loop!" + " - " + data.relationName());
	break;
      }
      if (isLoggingEnabled())
	getLogger().info("Using classifier #" + (i+1) + ": " + OptionUtils.getCommandLine(m_Classifiers[i]) + " - " + data.relationName());
      result = iterate(
	result,
	m_Classifiers[i],
	m_NumFolds[i].intValue(),
	m_Random.nextLong(),
	m_IQRMultiplier[i].doubleValue(),
	m_NumIterations[i].intValue(),
	m_MaxNonRemovalIterations[i].intValue());
    }

    if (isLoggingEnabled())
      getLogger().info("# outliers removed: " + (data.numInstances() - result.numInstances()) + " - " + data.relationName());

    m_CountAfter = result.numInstances();

    return result;
  }

  /**
   * Performs the actual postprocessing.
   *
   * @param data the instance to process
   * @return the processed instance
   */
  @Override
  protected Instance performPostProcess(Instance data) {
    // only works on datasets, not individual rows
    return data;
  }

  /**
   * Returns details for the cleaner.
   *
   * @return		the details
   */
  public SpreadSheet getDetails() {
    SpreadSheet	result;
    Row		row;

    result = new DefaultSpreadSheet();

    row = result.getHeaderRow();
    row.addCell("N").setContentAsString("Name");
    row.addCell("V").setContentAsString("Value");

    row = result.addRow();
    row.addCell("N").setContentAsString("# instances before");
    row.addCell("V").setContent(m_CountBefore);
    row = result.addRow();
    row.addCell("N").setContentAsString("# instances after");
    row.addCell("V").setContent(m_CountAfter);
    row = result.addRow();
    row.addCell("N").setContentAsString("Outliers removed");
    row.addCell("V").setContent(m_CountBefore - m_CountAfter);

    return result;
  }

  /**
   * Stops the execution. No message set.
   */
  @Override
  public void stopExecution() {
    if (m_CrossValidation != null)
      m_CrossValidation.stopExecution();
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

  /**
   * Cleans up data structures, frees up memory.
   */
  @Override
  public void cleanUp() {
    // nothing to do
  }
}
