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
 * AbstractThreeWayDataTrainPostProcessor.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer.threewaydatatrain;

import adams.core.option.AbstractOptionHandler;
import adams.flow.container.ThreeWayDataModelContainer;
import nz.ac.waikato.cms.adams.multiway.algorithm.api.AbstractAlgorithm;

/**
 * Ancestor for post-processors of a model generated by
 * {@link adams.flow.transformer.ThreeWayDataTrain}.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractThreeWayDataTrainPostProcessor
  extends AbstractOptionHandler {

  private static final long serialVersionUID = 3271228347125916688L;

  /**
   * Returns whether the algorithm can be handle.
   *
   * @param algorithm	the algorithm to check
   * @return		true if handled
   */
  public abstract boolean canHandle(AbstractAlgorithm algorithm);

  /**
   * Hook method for checking container before post-processing it.
   *
   * @param cont	the container to check
   * @return		null if successful, otherwise error message
   */
  protected String check(ThreeWayDataModelContainer cont) {
    if (cont == null)
      return "No container provided!";
    if (!cont.hasValue(ThreeWayDataModelContainer.VALUE_MODEL))
      return "Container has no model!";
    return null;
  }

  /**
   * Post-processes the container.
   *
   * @param cont	the container to post-process
   * @return		null if successful, otherwise error message
   */
  protected abstract String doPostProcess(ThreeWayDataModelContainer cont);

  /**
   * Post-processes the container.
   *
   * @param cont	the container to post-process
   * @return		null if successful, otherwise error message
   */
  public String postProcess(ThreeWayDataModelContainer cont) {
    String	result;

    result = check(cont);
    if (result == null)
      result = doPostProcess(cont);

    return result;
  }
}
