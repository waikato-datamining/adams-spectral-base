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
 * RemoveMisclassifiedCleaner.java
 * Copyright (C) 2009-2018 University of Waikato, Hamilton, New Zealand
 */

package adams.data.cleaner.instance;

import adams.core.option.OptionUtils;
import weka.classifiers.Classifier;
import weka.classifiers.functions.PLSClassifier;
import weka.classifiers.meta.SpectrumClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveMisclassifiedRel;

import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Removes instances that are misclassified by the given relative difference, unless they are within the absolute difference.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D &lt;int&gt; (property: debugLevel)
 * &nbsp;&nbsp;&nbsp;The greater the number the more additional info the scheme may output to
 * &nbsp;&nbsp;&nbsp;the console (0 = off).
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 *
 * <pre>-serialization-file &lt;adams.core.io.PlaceholderFile&gt; (property: serializationFile)
 * &nbsp;&nbsp;&nbsp;The file to serialize the generated internal model to.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 *
 * <pre>-override-serialized-file (property: overrideSerializedFile)
 * &nbsp;&nbsp;&nbsp;If set to true, then any serialized file will be ignored and the setup for
 * &nbsp;&nbsp;&nbsp;serialization will be regenerated.
 * </pre>
 *
 * <pre>-reldiff &lt;double&gt; (property: reldiff)
 * &nbsp;&nbsp;&nbsp;Maximum relative difference between target and prediction; predictions beyond
 * &nbsp;&nbsp;&nbsp;this difference will be removed; to turn off, use 0.
 * &nbsp;&nbsp;&nbsp;default: 0.25
 * </pre>
 *
 * <pre>-absdiff &lt;double&gt; (property: absdiff)
 * &nbsp;&nbsp;&nbsp;If the diffence between target and prediction is &lt;= this value, then the
 * &nbsp;&nbsp;&nbsp;instance will not be removed regardless of relative difference.
 * &nbsp;&nbsp;&nbsp;default: 1.0
 * </pre>
 *
 * <pre>-iterations &lt;int&gt; (property: iterations)
 * &nbsp;&nbsp;&nbsp;Number of passes for the misclassification process.
 * &nbsp;&nbsp;&nbsp;default: 1
 * </pre>
 *
 * <pre>-folds &lt;int&gt; (property: folds)
 * &nbsp;&nbsp;&nbsp;Number of folds for the cross-validation.
 * &nbsp;&nbsp;&nbsp;default: 3
 * </pre>
 *
 * <pre>-classifier &lt;weka.classifiers.Classifier&gt; (property: classifier)
 * &nbsp;&nbsp;&nbsp;Classifier to use for generating predictions.
 * &nbsp;&nbsp;&nbsp;default: weka.classifiers.meta.SpectrumClassifier -W weka.classifiers.functions.PLSClassifier -- -filter \"weka.filters.supervised.attribute.PLSFilter -C 20 -M -A PLS1 -P center\"
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  dale (dale at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class RemoveMisclassifiedCleaner
  extends AbstractSerializableCleaner {

  /** for serialization. */
  private static final long serialVersionUID = 6885583838230927644L;

  /** the relative difference. */
  protected double m_RelDiff;

  /** absolute difference. */
  protected double m_AbsDiff;

  /** number of fold for the cross validation. */
  protected int m_Folds;

  /** number of cleaning iterations to run. */
  protected int m_Iterations;

  /** WEKA classifier used for modeling. */
  protected Classifier m_Classifier;

  /** the filter. */
  protected RemoveMisclassifiedRel m_Filter;

  /** the filtered data generated when initializing the postprocessor. */
  protected Instances m_FilteredInitData;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
    "Removes instances that are misclassified by the given relative difference, unless they are within the absolute difference.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	"reldiff", "reldiff",
	0.25);

    m_OptionManager.add(
	"absdiff", "absdiff",
	1.0);

    m_OptionManager.add(
	"iterations", "iterations",
	1);

    m_OptionManager.add(
	"folds", "folds",
	3);

    SpectrumClassifier sc=new SpectrumClassifier();
    PLSClassifier pls=new PLSClassifier();
    sc.setClassifier(pls);

    m_OptionManager.add(
	"classifier", "classifier",
	sc);
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();
  }

  /**
   * Sets the relative difference.
   *
   * @param value 	relative difference
   */
  public void setReldiff(double value) {
    m_RelDiff = value;
    reset();
  }

  /**
   * Returns the relative difference.
   *
   * @return 		the relative difference
   */
  public double getReldiff() {
    return m_RelDiff;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String reldiffTipText() {
    return
        "Maximum relative difference between target and prediction; "
      + "predictions beyond this difference will be removed; to turn off, "
      + "use 0.";
  }

  /**
   * Sets the absolute difference.
   *
   * @param value 	absolute difference
   */
  public void setAbsdiff(double value) {
    m_AbsDiff = value;
    reset();
  }

  /**
   * Returns the absolute difference.
   *
   * @return 		the absolute difference
   */
  public double getAbsdiff() {
    return m_AbsDiff;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String absdiffTipText() {
    return "If the diffence between target and prediction is <= this value, then the instance will not be removed regardless of relative difference.";
  }

  /**
   * Sets the iterations.
   *
   * @param value 	iterations
   */
  public void setIterations(int value) {
    m_Iterations = value;
    reset();
  }

  /**
   * Returns the iterations.
   *
   * @return 		the iterations
   */
  public int getIterations() {
    return m_Iterations;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String iterationsTipText() {
    return "Number of passes for the misclassification process.";
  }

  /**
   * Sets the folds.
   *
   * @param value 	folds
   */
  public void setFolds(int value) {
    m_Folds = value;
    reset();
  }

  /**
   * Returns the folds.
   *
   * @return 		the folds
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
    return "Number of folds for the cross-validation.";
  }

  /**
   * Sets the classifier.
   *
   * @param value 	classifier
   */
  public void setClassifier(Classifier value) {
    m_Classifier = value;
    reset();
  }

  /**
   * Returns the classifier.
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
   * 			displaying in the GUI or for listing the options.
   */
  public String classifierTipText() {
    return "Classifier to use for generating predictions.";
  }

  /**
   * Performs the actual check.
   *
   * @param data	the Instance to check
   * @return		null if ok, otherwise error message
   */
  @Override
  protected String performCheck(Instance data) {
    return null;
  }

  /**
   * Regenerates all the objects that are necessary for serialization.
   */
  public void initSerializationSetup() {
    m_Filter = new RemoveMisclassifiedRel();
    m_Filter.setAbsErr(getAbsdiff());
    m_Filter.setThreshold(getReldiff());
    m_Filter.setClassIndex(m_InitData.classIndex());
    m_Filter.setMaxIterations(getIterations());
    m_Filter.setNumFolds(getFolds());
    m_Filter.setClassifier(getClassifier());

    try {
      m_Filter.setInputFormat(m_InitData);
      m_FilteredInitData = Filter.useFilter(m_InitData, m_Filter);
    }
    catch (Exception e) {
      throw new IllegalStateException("Failed to build: " + OptionUtils.getCommandLine(m_Filter), e);
    }
  }

  /**
   * Returns the member variables to serialize to a file.
   *
   * @return		the objects to serialize
   */
  public Object[] retrieveSerializationSetup() {
    return new Object[]{
	m_PreFilter,
	m_Filter
    };
  }

  /**
   * Updates the member variables with the provided objects obtained from
   * deserialization.
   *
   * @param value	the deserialized objects
   */
  public void setSerializationSetup(Object[] value) {
    m_PreFilter = (Filter) value[0];
    m_Filter    = (RemoveMisclassifiedRel) value[1];
  }

  /**
   * Clean Instances.
   *
   * @param instances	Instances
   */
  @Override
  protected Instances performClean(Instances instances) {
    Instances	result;
    String	msg;

    if (m_FilteredInitData != null) {
      result             = m_FilteredInitData;
      m_FilteredInitData = null;
    }
    else {
      try {
	result = Filter.useFilter(instances, m_Filter);
      }
      catch (Exception e) {
	msg = "Failed to apply filter '" + OptionUtils.getCommandLine(m_Filter) + "': ";
	m_CleanInstancesError = msg + e;
	getLogger().log(Level.SEVERE, msg, e);
	result = null; // TODO or return original instances?
      }
    }

    return result;
  }
}
