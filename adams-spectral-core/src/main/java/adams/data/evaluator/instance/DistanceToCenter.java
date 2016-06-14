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
 * DistanceToCenter.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.data.evaluator.instance;

import adams.core.option.OptionUtils;
import adams.data.statistics.StatUtils;
import weka.core.DenseInstance;
import weka.core.DistanceFunction;
import weka.core.EuclideanDistance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.LinearNNSearch;
import weka.core.neighboursearch.NearestNeighbourSearch;

import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Uses the specified nearest neighbor search to determine a neighborhood. From this neighborhood the center is calculated (only using numeric attributes) and the distance to the center is returned.<br>
 * NB: normalization should be turned off in the search function.
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
 * &nbsp;&nbsp;&nbsp;default: weka.core.neighboursearch.LinearNNSearch -A \"weka.core.EuclideanDistance -D -R first-last\"
 * </pre>
 * 
 * <pre>-num-neighbors &lt;int&gt; (property: numNeighbors)
 * &nbsp;&nbsp;&nbsp;The number of neighbors to use in the neighborhood.
 * &nbsp;&nbsp;&nbsp;default: 100
 * &nbsp;&nbsp;&nbsp;minimum: 1
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class DistanceToCenter
  extends AbstractNearestNeighborBasedEvaluator {

  private static final long serialVersionUID = 8219254664592725340L;

  /** the number of neighbors to use. */
  protected int m_NumNeighbors;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Uses the specified nearest neighbor search to determine a neighborhood. "
        + "From this neighborhood the center is calculated (only using numeric "
        + "attributes) and the distance to the center is returned.\n"
        + "NB: normalization should be turned off in the search function.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "num-neighbors", "numNeighbors",
      100, 1, null);
  }

  /**
   * Returns the default search algorithm to use.
   *
   * @return		the default
   */
  @Override
  protected NearestNeighbourSearch getDefaultSearch() {
    LinearNNSearch	result;
    EuclideanDistance	dist;

    result = new LinearNNSearch();
    dist   = new EuclideanDistance();
    dist.setDontNormalize(true);
    try {
      result.setDistanceFunction(dist);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to set distance function??", e);
    }

    return result;
  }

  /**
   * Sets the number of neighbors to use in the neighborhood.
   *
   * @param value 	the number of neighbors
   */
  public void setNumNeighbors(int value) {
    m_NumNeighbors = value;
    reset();
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
   * Builds the evaluator.
   *
   * @param data	the instance to check
   * @return		true if build successful
   */
  @Override
  protected boolean performBuild(Instances data) {
    if (!initSearch(data))
      return false;

    m_SerializableObjectHelper.saveSetup();

    return true;
  }

  /**
   * Regenerates all the objects that are necessary for serialization.
   */
  @Override
  public void initSerializationSetup() {
    if (m_ActualSearch == null)
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
   * Performs the actual evaluation.
   *
   * @param data	the instance to check
   * @return		evaluation metric, {@link #m_MissingEvaluation} in case
   * 			the class value is missing
   */
  protected Float performEvaluate(Instance data) {
    Float			result;
    Instances			neighbors;
    Instance			center;
    Instances			centerDataset;
    int				i;
    double[]			values;
    DistanceFunction		distance;

    result = m_MissingEvaluation;

    try {
      // calculate center from neighborhood
      neighbors     = m_ActualSearch.kNearestNeighbours(data, m_NumNeighbors);
      centerDataset = new Instances(neighbors, 0);
      center        = new DenseInstance(neighbors.numAttributes());
      center.setDataset(centerDataset);
      for (i = 0; i < neighbors.numAttributes(); i++) {
	if (!neighbors.attribute(i).isNumeric())
	  continue;
	values = neighbors.attributeToDoubleArray(i);
	center.setValue(i, StatUtils.mean(values));
      }
      centerDataset.add(center);
      // calculate distance to center
      distance = (DistanceFunction) OptionUtils.shallowCopy(m_Search.getDistanceFunction());
      distance.setInstances(centerDataset);
      distance.update(data);
      result = (float) distance.distance(center, data);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to find nearest neighbor!", e);
      result = m_MissingEvaluation;
    }

    return result;
  }
}
