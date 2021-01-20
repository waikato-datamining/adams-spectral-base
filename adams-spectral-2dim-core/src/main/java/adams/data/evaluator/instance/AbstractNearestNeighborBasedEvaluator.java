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
 * AbstractNearestNeighborBasedEvaluator.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.data.evaluator.instance;

import adams.core.ObjectCopyHelper;
import weka.core.Instances;
import weka.core.neighboursearch.NearestNeighbourSearch;

import java.util.logging.Level;

/**
 * Ancestor for evaluators that use a nearest neighbor search.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractNearestNeighborBasedEvaluator
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
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "search", "search",
      getDefaultSearch());
  }

  /**
   * Returns the default search algorithm to use.
   *
   * @return		the default
   */
  protected abstract NearestNeighbourSearch getDefaultSearch();

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
   * Initializes the search algorithm.
   *
   * @param data	the data to initialize the search with
   * @return		true if successfully initialized
   */
  protected boolean initSearch(Instances data) {
    if (data == null)
      return false;

    m_TrainingData = data;
    m_ActualSearch = ObjectCopyHelper.copyObject(m_Search);
    if (m_ActualSearch == null) {
      getLogger().severe("Failed to create copy of search algorithm!");
      return false;
    }

    try {
      m_ActualSearch.setInstances(data);
      m_Header = new Instances(data, 0);
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
   * Cleans up data structures, frees up memory.
   */
  @Override
  public void cleanUp() {
    m_TrainingData = null;
  }
}
