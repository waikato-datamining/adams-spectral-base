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
 * ThreeWayDataModelContainer.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.flow.container;

import adams.data.container.TensorContainer;
import nz.ac.waikato.cms.adams.multiway.algorithm.api.AbstractAlgorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Container for spectrum filters.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ThreeWayDataModelContainer
  extends AbstractContainer {

  private static final long serialVersionUID = -7791501313124716613L;

  /** the model key. */
  public final static String VALUE_MODEL = "Model";

  /** the training data key. */
  public final static String VALUE_TRAIN = "Train";

  /**
   * Initializes the container.
   * <br><br>
   * Only used for generating help information.
   */
  public ThreeWayDataModelContainer() {
    this(null, (TensorContainer) null);
  }

  /**
   * Initializes the container with the model and the associated data.
   *
   * @param model	the model
   * @param train	the dataset, can be null
   */
  public ThreeWayDataModelContainer(AbstractAlgorithm model, TensorContainer train) {
    super();
    store(VALUE_MODEL, model);
    store(VALUE_TRAIN, train);
  }

  /**
   * Initializes the container with the model and the associated data.
   *
   * @param model	the model
   * @param train	the dataset, can be null
   */
  public ThreeWayDataModelContainer(AbstractAlgorithm model, TensorContainer[] train) {
    super();
    store(VALUE_MODEL, model);
    store(VALUE_TRAIN, train);
  }

  /**
   * Initializes help strings specific to the filter.
   */
  protected void initFilterHelp() {
    addHelp(VALUE_MODEL, "model object", AbstractAlgorithm.class);
    addHelp(VALUE_TRAIN, "train data", new Class[]{TensorContainer.class, TensorContainer[].class});
  }

  /**
   * Returns all value names that can be used (theoretically).
   *
   * @return		iterator over all possible value names
   */
  @Override
  public Iterator<String> names() {
    List<String> result;

    result = new ArrayList<>();

    result.add(VALUE_MODEL);
    result.add(VALUE_TRAIN);

    return result.iterator();
  }

  /**
   * Checks whether the setup of the container is valid.
   *
   * @return		true if all the necessary values are available
   */
  @Override
  public boolean isValid() {
    return hasValue(VALUE_MODEL) && hasValue(VALUE_TRAIN);
  }
}
