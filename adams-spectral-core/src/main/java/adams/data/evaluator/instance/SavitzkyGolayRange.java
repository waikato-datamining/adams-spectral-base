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
 * SavitzkyGolayRange.java
 * Copyright (C) 2016 University of Waikato, Hamilton, New Zealand
 */
package adams.data.evaluator.instance;

import adams.core.Utils;
import adams.core.option.OptionUtils;
import adams.data.statistics.StatUtils;
import weka.classifiers.Classifier;
import weka.classifiers.functions.GPD;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.SpectrumClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.SavitzkyGolay;

/**
 <!-- globalinfo-start -->
 * This evaluator builds three classifiers:<br>
 * - on Savitzky-Golay filtered data with no derivative<br>
 * - on Savitzky-Golay filtered data with first derivative<br>
 * - on Savitzky-Golay filtered data with second derivative<br>
 * Each instance under evaluation will be pushed through the three classifiers separately and the range of the three predictions is then output as evaluation result.
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
 * <pre>-classifier-none &lt;weka.classifiers.Classifier&gt; (property: classifierNone)
 * &nbsp;&nbsp;&nbsp;The classifier to be used in conjunction with Savitzky-Golay filter with 
 * &nbsp;&nbsp;&nbsp;no derivative.
 * &nbsp;&nbsp;&nbsp;default: weka.classifiers.functions.GPD -G 0.01 -L 0.01 -N 0
 * </pre>
 * 
 * <pre>-window-size-none &lt;int&gt; (property: windowSizeNone)
 * &nbsp;&nbsp;&nbsp;The window size for the Savitzky-Golay filter with no derivative.
 * &nbsp;&nbsp;&nbsp;default: 7
 * &nbsp;&nbsp;&nbsp;minimum: 3
 * </pre>
 * 
 * <pre>-classifier-first &lt;weka.classifiers.Classifier&gt; (property: classifierFirst)
 * &nbsp;&nbsp;&nbsp;The classifier to be used in conjunction with Savitzky-Golay filter with 
 * &nbsp;&nbsp;&nbsp;first derivative.
 * &nbsp;&nbsp;&nbsp;default: weka.classifiers.functions.GPD -G 0.01 -L 0.01 -N 0
 * </pre>
 * 
 * <pre>-window-size-first &lt;int&gt; (property: windowSizeFirst)
 * &nbsp;&nbsp;&nbsp;The window size for the Savitzky-Golay filter with first derivative.
 * &nbsp;&nbsp;&nbsp;default: 7
 * &nbsp;&nbsp;&nbsp;minimum: 3
 * </pre>
 * 
 * <pre>-classifier-second &lt;weka.classifiers.Classifier&gt; (property: classifierSecond)
 * &nbsp;&nbsp;&nbsp;The classifier to be used in conjunction with Savitzky-Golay filter with 
 * &nbsp;&nbsp;&nbsp;second derivative.
 * &nbsp;&nbsp;&nbsp;default: weka.classifiers.functions.GPD -G 0.01 -L 0.01 -N 0
 * </pre>
 * 
 * <pre>-window-size-second &lt;int&gt; (property: windowSizeSecond)
 * &nbsp;&nbsp;&nbsp;The window size for the Savitzky-Golay filter with second derivative.
 * &nbsp;&nbsp;&nbsp;default: 7
 * &nbsp;&nbsp;&nbsp;minimum: 3
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SavitzkyGolayRange
  extends AbstractSerializableEvaluator {

  /** for serialization. */
  private static final long serialVersionUID = -6086808426732510366L;

  /** the classifier to be used on SG data with no derivative. */
  protected Classifier m_ClassifierNone;

  /** the window size for for SG with no derivative. */
  protected int m_WindowSizeNone;

  /** the actual classifier in use for no derivative. */
  protected SpectrumClassifier m_ActualClassifierNone;

  /** the classifier to be used on SG data with first derivative. */
  protected Classifier m_ClassifierFirst;

  /** the window size for for SG with first derivative. */
  protected int m_WindowSizeFirst;

  /** the actual classifier in use for first derivative. */
  protected SpectrumClassifier m_ActualClassifierFirst;

  /** the classifier to be used on SG data with second derivative. */
  protected Classifier m_ClassifierSecond;

  /** the window size for for SG with second derivative. */
  protected int m_WindowSizeSecond;

  /** the actual classifier in use for second derivative. */
  protected SpectrumClassifier m_ActualClassifierSecond;

  /** the training data. */
  protected Instances m_TrainingData;

  /** the header of the training data. */
  protected Instances m_Header;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "This evaluator builds three classifiers:\n"
      + "- on Savitzky-Golay filtered data with no derivative\n"
      + "- on Savitzky-Golay filtered data with first derivative\n"
      + "- on Savitzky-Golay filtered data with second derivative\n"
      + "Each instance under evaluation will be pushed through the three "
      + "classifiers separately and the range of the three predictions is "
      + "then output as evaluation result.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "classifier-none", "classifierNone",
      getDefaultClassifier(0));

    m_OptionManager.add(
	"window-size-none", "windowSizeNone",
	7, 3, null);

    m_OptionManager.add(
      "classifier-first", "classifierFirst",
      getDefaultClassifier(0));

    m_OptionManager.add(
	"window-size-first", "windowSizeFirst",
	7, 3, null);

    m_OptionManager.add(
      "classifier-second", "classifierSecond",
      getDefaultClassifier(0));

    m_OptionManager.add(
	"window-size-second", "windowSizeSecond",
	7, 3, null);
  }

  /**
   * Returns the default classifier for the specified derivative.
   *
   * @param derivative	the derivative
   * @return		the classifier setup
   */
  protected Classifier getDefaultClassifier(int derivative) {
    GPD result;

    result = new GPD();
    result.setNoise(0.01);
    result.setGamma(0.01);

    return result;
  }

  /**
   * Sets the classifier to be used in conjunction with Savitzky-Golay filter
   * with no derivative.
   *
   * @param value 	the classifier
   */
  public void setClassifierNone(Classifier value) {
    m_ClassifierNone = value;
    reset();
  }

  /**
   * Returns the classifier to be used in conjunction with Savitzky-Golay filter
   * with no derivative.
   *
   * @return 		the classifier
   */
  public Classifier getClassifierNone() {
    return m_ClassifierNone;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String classifierNoneTipText() {
    return "The classifier to be used in conjunction with Savitzky-Golay filter with no derivative.";
  }

  /**
   * Sets the Savitzky-Golay window size for filter with no derivative.
   *
   * @param value 	the window size
   */
  public void setWindowSizeNone(int value) {
    if (getOptionManager().isValid("windowSizeNone", value)) {
      if (value % 2 == 1) {
	m_WindowSizeNone = value;
	reset();
      }
      else {
	getLogger().warning("Window size (none) must be an odd number!");
      }
    }
  }

  /**
   * Returns the Savitzky-Golay window size for filter with no derivative.
   *
   * @return 		the window size
   */
  public int getWindowSizeNone() {
    return m_WindowSizeNone;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String windowSizeNoneTipText() {
    return "The window size for the Savitzky-Golay filter with no derivative.";
  }

  /**
   * Sets the classifier to be used in conjunction with Savitzky-Golay filter
   * with first derivative.
   *
   * @param value 	the classifier
   */
  public void setClassifierFirst(Classifier value) {
    m_ClassifierFirst = value;
    reset();
  }

  /**
   * Returns the classifier to be used in conjunction with Savitzky-Golay filter
   * with first derivative.
   *
   * @return 		the classifier
   */
  public Classifier getClassifierFirst() {
    return m_ClassifierFirst;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String classifierFirstTipText() {
    return "The classifier to be used in conjunction with Savitzky-Golay filter with first derivative.";
  }

  /**
   * Sets the Savitzky-Golay window size for filter with first derivative.
   *
   * @param value 	the window size
   */
  public void setWindowSizeFirst(int value) {
    if (getOptionManager().isValid("windowSizeFirst", value)) {
      if (value % 2 == 1) {
	m_WindowSizeFirst = value;
	reset();
      }
      else {
	getLogger().warning("Window size (first) must be an odd number!");
      }
    }
  }

  /**
   * Returns the Savitzky-Golay window size for filter with first derivative.
   *
   * @return 		the window size
   */
  public int getWindowSizeFirst() {
    return m_WindowSizeFirst;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String windowSizeFirstTipText() {
    return "The window size for the Savitzky-Golay filter with first derivative.";
  }

  /**
   * Sets the classifier to be used in conjunction with Savitzky-Golay filter
   * with second derivative.
   *
   * @param value 	the classifier
   */
  public void setClassifierSecond(Classifier value) {
    m_ClassifierSecond = value;
    reset();
  }

  /**
   * Returns the classifier to be used in conjunction with Savitzky-Golay filter
   * with second derivative.
   *
   * @return 		the classifier
   */
  public Classifier getClassifierSecond() {
    return m_ClassifierSecond;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String classifierSecondTipText() {
    return "The classifier to be used in conjunction with Savitzky-Golay filter with second derivative.";
  }

  /**
   * Sets the Savitzky-Golay window size for filter with second derivative.
   *
   * @param value 	the window size
   */
  public void setWindowSizeSecond(int value) {
    if (getOptionManager().isValid("windowSizeSecond", value)) {
      if (value % 2 == 1) {
	m_WindowSizeSecond = value;
	reset();
      }
      else {
	getLogger().warning("Window size (second) must be an odd number!");
      }
    }
  }

  /**
   * Returns the Savitzky-Golay window size for filter with second derivative.
   *
   * @return 		the window size
   */
  public int getWindowSizeSecond() {
    return m_WindowSizeSecond;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String windowSizeSecondTipText() {
    return "The window size for the Savitzky-Golay filter with second derivative.";
  }

  /**
   * Returns the default value in case of missing evaluations.
   * 
   * @return		the default value
   */
  @Override
  protected float getDefaultMissingEvaluation() {
    return Float.NaN;
  }

  /**
   * Performs the actual evaluation.
   *
   * @param data	the instance to check
   * @return		always 1.0
   */
  @Override
  protected Float performEvaluate(Instance data) {
    double[]	preds;

    preds = new double[3];
    try {
      preds[0] = m_ActualClassifierNone.classifyInstance(data);
      preds[1] = m_ActualClassifierFirst.classifyInstance(data);
      preds[2] = m_ActualClassifierSecond.classifyInstance(data);
      if (isLoggingEnabled())
	getLogger().info(data + "\n--> " + Utils.arrayToString(preds));
    }
    catch (Exception e) {
      Utils.handleException(this, "Failed to make prediction(s) for instance: " + data, e);
      return m_MissingEvaluation;
    }

    return (float) (StatUtils.max(preds) - StatUtils.min(preds));
  }

  /**
   * Builds the evaluator.
   *
   * @param data	the instance to check
   * @return		always true
   */
  @Override
  protected boolean performBuild(Instances data) {
    FilteredClassifier	fc;
    SavitzkyGolay	sg;

    if (data == null)
      return false;

    m_TrainingData = data;

    // none
    sg = new SavitzkyGolay();
    sg.setNumPointsLeft(m_WindowSizeNone / 2);
    sg.setNumPointsRight(m_WindowSizeNone / 2);
    sg.setDerivativeOrder(0);
    fc = new FilteredClassifier();
    fc.setFilter(sg);
    fc.setClassifier((Classifier) OptionUtils.shallowCopy(m_ClassifierNone));
    m_ActualClassifierNone = new SpectrumClassifier();
    m_ActualClassifierNone.setClassifier(fc);
    try {
      m_ActualClassifierNone.buildClassifier(data);
    }
    catch (Exception e) {
      Utils.handleException(this, "Failed to build actual classifier (none)!", e);
      return false;
    }

    // first
    sg = new SavitzkyGolay();
    sg.setNumPointsLeft(m_WindowSizeFirst / 2);
    sg.setNumPointsRight(m_WindowSizeFirst / 2);
    sg.setDerivativeOrder(1);
    fc = new FilteredClassifier();
    fc.setFilter(sg);
    fc.setClassifier((Classifier) OptionUtils.shallowCopy(m_ClassifierFirst));
    m_ActualClassifierFirst = new SpectrumClassifier();
    m_ActualClassifierFirst.setClassifier(fc);
    try {
      m_ActualClassifierFirst.buildClassifier(data);
    }
    catch (Exception e) {
      Utils.handleException(this, "Failed to build actual classifier (first)!", e);
      return false;
    }

    // second
    sg = new SavitzkyGolay();
    sg.setNumPointsLeft(m_WindowSizeSecond / 2);
    sg.setNumPointsRight(m_WindowSizeSecond / 2);
    sg.setDerivativeOrder(2);
    fc = new FilteredClassifier();
    fc.setFilter(sg);
    fc.setClassifier((Classifier) OptionUtils.shallowCopy(m_ClassifierSecond));
    m_ActualClassifierSecond = new SpectrumClassifier();
    m_ActualClassifierSecond.setClassifier(fc);
    try {
      m_ActualClassifierSecond.buildClassifier(data);
    }
    catch (Exception e) {
      Utils.handleException(this, "Failed to build actual classifier (second)!", e);
      return false;
    }

    m_Header = new Instances(data, 0);
    m_SerializableObjectHelper.saveSetup();

    return true;
  }

  /**
   * Regenerates all the objects that are necessary for serialization.
   */
  @Override
  public void initSerializationSetup() {
    if (m_ActualClassifierNone == null)
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
      m_Header,
      m_ActualClassifierNone,
      m_ActualClassifierFirst,
      m_ActualClassifierSecond,
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
    m_Header                 = (Instances) value[0];
    m_ActualClassifierNone   = (SpectrumClassifier) value[1];
    m_ActualClassifierFirst  = (SpectrumClassifier) value[2];
    m_ActualClassifierSecond = (SpectrumClassifier) value[3];
  }

  /**
   * Cleans up data structures, frees up memory.
   */
  @Override
  public void cleanUp() {
    m_TrainingData = null;
  }
}
