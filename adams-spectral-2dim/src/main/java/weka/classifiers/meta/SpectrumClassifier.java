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
 *    SpectrumClassifier.java
 *    Copyright (C) 2011-2016 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.classifiers.meta;

import adams.data.instances.ArffUtils;
import weka.classifiers.AbstainingClassifier;
import weka.classifiers.SingleClassifierEnhancer;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 <!-- globalinfo-start -->
 * Automatically removes some IDs from the dataset before training the base classifier: id, db_id, sample_id.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre> -D
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console</pre>
 *
 * <pre> -W
 *  Full name of base classifier.
 *  (default: weka.classifiers.functions.GPD)</pre>
 *
 * <pre>
 * Options specific to classifier weka.classifiers.functions.GPD:
 * </pre>
 *
 * <pre> -D
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console</pre>
 *
 * <pre> -L &lt;double&gt;
 *  Level of Gaussian Noise.
 *  (default: 1.0)</pre>
 *
 * <pre> -G &lt;double&gt;
 *  Gamma for the RBF kernel.
 *  (default: 0.01)</pre>
 *
 * <pre> -N
 *  Whether to 0=normalize/1=standardize/2=neither.
 *  (default: 0=normalize)</pre>
 *
 * <pre> -D
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console</pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SpectrumClassifier
  extends SingleClassifierEnhancer
  implements AbstainingClassifier {

  /** for serialization */
  static final long serialVersionUID = -4523450618538717400L;

  /** The filter for removing sample ID/database ID. */
  protected Remove m_Remove = null;
  
  /** whether the base classifier can abstain. */
  protected boolean m_CanAbstain = false;

  /**
   * Returns a string describing this classifier.
   *
   * @return 		a description of the classifier suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return
        "Automatically removes some IDs from the dataset "
      + "before training the base classifier: "
      + ArffUtils.getIDName() + ", "
      + ArffUtils.getDBIDName() + ", "
      + ArffUtils.getSampleIDName() + ".";
  }

  /**
   * String describing default classifier.
   *
   * @return 		the default classifier classname
   */
  @Override
  protected String defaultClassifierString() {
    return "weka.classifiers.functions.GPD";
  }

  /**
   * Default constructor.
   */
  public SpectrumClassifier() {
    super();

    m_Classifier = new weka.classifiers.functions.GPD();
  }

  /**
   * Returns default capabilities of the classifier.
   *
   * @return      the capabilities of this classifier
   */
  @Override
  public Capabilities getCapabilities() {
    Capabilities	result;

    result = super.getCapabilities();

    result.enable(Capability.STRING_ATTRIBUTES);
    result.enable(Capability.NUMERIC_ATTRIBUTES);

    // the filtered classifier always needs a class
    result.disable(Capability.NO_CLASS);

    return result;
  }

  /**
   * Build the classifier on the filtered data.
   *
   * @param data 	the training data
   * @throws Exception 	if the classifier could not be built successfully
   */
  public void buildClassifier(Instances data) throws Exception {
    data = new Instances(data);
    data.deleteWithMissingClass();

    m_Remove = ArffUtils.getRemoveFilter(data);
    if (m_Remove != null) {
      m_Remove.setInputFormat(data);
      data = Filter.useFilter(data, m_Remove);
    }

    // can classifier handle the data?
    getClassifier().getCapabilities().testWithFail(data);

    m_Classifier.buildClassifier(data);

    m_CanAbstain = (m_Classifier instanceof AbstainingClassifier) && ((AbstainingClassifier) m_Classifier).canAbstain();
  }

  /**
   * Classifies a given instance after filtering.
   *
   * @param instance 	the instance to be classified
   * @return 		the class distribution for the given instance
   * @throws Exception 	if instance could not be classified successfully
   */
  @Override
  public double[] distributionForInstance(Instance instance) throws Exception {
    if (m_Remove != null) {
      m_Remove.input(instance);
      m_Remove.batchFinished();
      instance = m_Remove.output();
    }

    return m_Classifier.distributionForInstance(instance);
  }

  /**
   * Whether abstaining is possible, e.g., used in meta-classifiers.
   * 
   * @return		true if abstaining is possible
   */
  @Override
  public boolean canAbstain() {
    return m_CanAbstain;
  }

  /**
   * The prediction that made the classifier abstain.
   * 
   * @param inst	the instance to get the prediction for
   * @return		the prediction, {@link Utils#missingValue()} if abstaining is not possible
   * @throws Exception	if fails to make prediction
   */
  @Override
  public synchronized double getAbstentionClassification(Instance inst) throws Exception {
    if (m_CanAbstain) {
      if (m_Remove != null) {
	m_Remove.input(inst);
	m_Remove.batchFinished();
	inst = m_Remove.output();
      }
      
      return ((AbstainingClassifier) m_Classifier).getAbstentionClassification(inst);
    }
    else {
      return Utils.missingValue();
    }
  }

  /**
   * The class distribution that made the classifier abstain.
   * 
   * @param inst	the instance to get the prediction for
   * @return		the class distribution, null if abstaining is not possible
   * @throws Exception	if fails to make prediction
   */
  @Override
  public synchronized double[] getAbstentionDistribution(Instance inst) throws Exception {
    if (m_CanAbstain) {
      if (m_Remove != null) {
	m_Remove.input(inst);
	m_Remove.batchFinished();
	inst = m_Remove.output();
      }
      
      return ((AbstainingClassifier) m_Classifier).getAbstentionDistribution(inst);
    }
    else {
      return null;
    }
  }

  /**
   * Output a representation of this classifier
   *
   * @return a representation of this classifier
   */
  @Override
  public String toString() {
    return m_Classifier.toString();
  }

  /**
   * Returns the revision string.
   *
   * @return		the revision
   */
  @Override
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 2242 $");
  }

  /**
   * Main method for executing this classifier.
   *
   * @param args 	the commandline options, use -h for help
   */
  public static void main(String [] args)  {
    runClassifier(new SpectrumClassifier(), args);
  }
}
