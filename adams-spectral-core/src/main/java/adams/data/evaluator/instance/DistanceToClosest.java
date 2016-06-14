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
 * DistanceToClosest.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.data.evaluator.instance;

import adams.core.option.OptionUtils;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.LinearNNSearch;
import weka.core.neighboursearch.NearestNeighbourSearch;

import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Uses the specified nearest neighbor search to locate the closest instance in the training data and returns the distance to it.
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
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class DistanceToClosest
  extends AbstractSerializableEvaluator {

  private static final long serialVersionUID = 8219254664592725340L;

  /** the nearest neighbor algorithm to use. */
  protected NearestNeighbourSearch m_Search;

  /** the actual nearest neighbor algorithm in use. */
  protected NearestNeighbourSearch m_ActualSearch;

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
      "Uses the specified nearest neighbor search to locate the closest "
	+ "instance in the training data and returns the distance to it.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "search", "search",
      new LinearNNSearch());
  }

  /**
   * Sets the nearest neighbor search algorithm.
   *
   * @param value 	the algorithm
   */
  public void setSearch(NearestNeighbourSearch value) {
    m_Search = value;
    reset();
  }

  /**
   * Returns the nearest neighbor search algorithm.
   *
   * @return 		the algorithm
   */
  public NearestNeighbourSearch getSearch() {
    return m_Search;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String searchTipText() {
    return "The nearest neighbor search to use.";
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
   * Builds the evaluator.
   *
   * @param data	the instance to check
   * @return		true if build successful
   */
  @Override
  protected boolean performBuild(Instances data) {
    if (data == null)
      return false;

    m_TrainingData = data;
    m_ActualSearch = (NearestNeighbourSearch) OptionUtils.shallowCopy(m_Search);
    if (m_ActualSearch == null) {
      getLogger().severe("Failed to create copy of search algorithm!");
      return false;
    }

    try {
      m_ActualSearch.setInstances(data);
      m_Header = new Instances(data, 0);
      m_SerializableObjectHelper.saveSetup();
    }
    catch (Exception e) {
      m_Header       = null;
      m_ActualSearch = null;
      getLogger().log(Level.SEVERE, "Failed to initialize search algorithm with training data!", e);
    }

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
    Float	result;
    double[]	dist;

    result = m_MissingEvaluation;

    try {
      m_ActualSearch.kNearestNeighbours(data, 1);
      dist = m_ActualSearch.getDistances();
      if (dist.length > 0)
	result = (float) dist[0];
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to find nearest neighbor!", e);
      result = m_MissingEvaluation;
    }

    return result;
  }

  /**
   * Cleans up data structures, frees up memory.
   */
  @Override
  public void cleanUp() {
    m_TrainingData = null;
  }
}
