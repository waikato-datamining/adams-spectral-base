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
 * PassThrough.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer.threewaydatatrain;

import adams.flow.container.ThreeWayDataModelContainer;
import nz.ac.waikato.cms.adams.multiway.algorithm.api.AbstractAlgorithm;

/**
 * Does nothing.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class PassThrough
  extends AbstractThreeWayDataTrainPostProcessor {

  private static final long serialVersionUID = -8288666604273597958L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Dummy, does nothing.";
  }

  /**
   * Returns whether the algorithm can be handle.
   *
   * @param algorithm	the algorithm to check
   * @return		true if handled
   */
  @Override
  public boolean canHandle(AbstractAlgorithm algorithm) {
    return true;
  }

  /**
   * Post-processes the container.
   *
   * @param cont	the container to post-process
   * @return		null if successful, otherwise error message
   */
  @Override
  protected String doPostProcess(ThreeWayDataModelContainer cont) {
    return null;
  }
}
