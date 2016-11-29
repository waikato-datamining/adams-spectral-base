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
 *    SpectrumClusterer.java
 *    Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.clusterers;

import adams.data.instances.ArffUtils;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
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
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @version $Revision: 2242 $
 */
public class SpectrumClusterer
  extends SingleClustererEnhancer
  implements DensityBasedClusterer {

  /** for serialization */
  static final long serialVersionUID = -4523450618538717400L;

  /** The filter for removing sample ID/database ID. */
  protected Remove m_Remove = null;

  /** The actual clusterer. */
  protected DensityBasedClusterer m_ActualClusterer;

  /**
   * Returns a string describing this clusterer.
   *
   * @return 		a description of the clusterer suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return
        "Automatically removes some IDs from the dataset "
      + "before training the base clusterer: "
      + ArffUtils.getIDName() + ", "
      + ArffUtils.getDBIDName() + ", "
      + ArffUtils.getSampleIDName() + ".\n"
      + "If the base cluster algorithm is not a density based one, then "
      + "the " + MakeDensityBasedClusterer.class.getName() + " wrapper is "
      + "automatically used.";
  }

  /**
   * Default constructor.
   */
  public SpectrumClusterer() {
    super();

    m_Clusterer = new SimpleKMeans();
  }

  /**
   * Returns default capabilities of the clusterer.
   *
   * @return      the capabilities of this clusterer
   */
  public Capabilities getCapabilities() {
    Capabilities	result;

    result = super.getCapabilities();

    result.enable(Capability.STRING_ATTRIBUTES);
    result.enable(Capability.NUMERIC_ATTRIBUTES);

    return result;
  }

  /**
   * Build the clusterer on the filtered data.
   *
   * @param data 	the training data
   * @throws Exception 	if the clusterer could not be built successfully
   */
  public void buildClusterer(Instances data) throws Exception {
    data = new Instances(data);

    m_Remove = ArffUtils.getRemoveFilter(data);
    if (m_Remove != null) {
      m_Remove.setInputFormat(data);
      data = Filter.useFilter(data, m_Remove);
    }

    if (m_Clusterer instanceof DensityBasedClusterer)
      m_ActualClusterer = (DensityBasedClusterer) AbstractClusterer.makeCopy(m_Clusterer);
    else
      m_ActualClusterer = new MakeDensityBasedClusterer(m_Clusterer);

    // can clusterer handle the data?
    m_ActualClusterer.getCapabilities().testWithFail(data);

    m_ActualClusterer.buildClusterer(data);
  }

  /**
   * Filters the instance if the Remove filter has been set up,
   * otherwise it just returns the instance as it is.
   *
   * @param instance	the instance to filter, if required
   * @return		the filtered instance
   * @throws Exception	if filtering fails
   */
  protected Instance filter(Instance instance) throws Exception {
    if (m_Remove != null) {
      m_Remove.input(instance);
      m_Remove.batchFinished();
      instance = m_Remove.output();
    }

    return instance;
  }

  /**
   * Clusters a given instance after filtering.
   *
   * @param instance 	the instance to be clustered
   * @return 		the cluster distribution for the given instance
   * @throws Exception 	if instance could not be clustered successfully
   */
  public double[] distributionForInstance(Instance instance) throws Exception {
    return m_ActualClusterer.distributionForInstance(filter(instance));
  }

  /**
   * Returns the prior probability of each cluster.
   *
   * @return the prior probability for each cluster
   * @exception Exception if priors could not be
   * returned successfully
   */
  public double[] clusterPriors() throws Exception {
    return m_ActualClusterer.clusterPriors();
  }

  /**
   * Computes the log of the conditional density (per cluster) for a given instance.
   *
   * @param instance the instance to compute the density for
   * @return an array containing the estimated densities
   * @exception Exception if the density could not be computed
   * successfully
   */
  public double[] logDensityPerClusterForInstance(Instance instance) throws Exception {
    return m_ActualClusterer.logDensityPerClusterForInstance(filter(instance));
  }

  /**
   * Computes the density for a given instance.
   *
   * @param instance the instance to compute the density for
   * @return the density.
   * @exception Exception if the density could not be computed successfully
   */
  public double logDensityForInstance(Instance instance) throws Exception {
    return m_ActualClusterer.logDensityForInstance(filter(instance));
  }

  /**
   * Returns the logs of the joint densities for a given instance.
   *
   * @param inst the instance
   * @return the array of values
   * @exception Exception if values could not be computed
   */
  public double[] logJointDensitiesForInstance(Instance inst) throws Exception {
    return m_ActualClusterer.logJointDensitiesForInstance(filter(inst));
  }

  /**
   * Output a representation of this clusterer
   *
   * @return a representation of this clusterer
   */
  public String toString() {
    return m_ActualClusterer.toString();
  }

  /**
   * Returns the revision string.
   *
   * @return		the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 2242 $");
  }

  /**
   * Main method for executing this clusterer.
   *
   * @param args 	the commandline options, use -h for help
   */
  public static void main(String [] args)  {
    runClusterer(new SpectrumClusterer(), args);
  }
}
