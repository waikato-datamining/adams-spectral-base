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
 * TensorContainer.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.container;

import nz.ac.waikato.cms.adams.multiway.data.tensor.Tensor;

/**
 * Container for a {@link Tensor}.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class TensorContainer
  extends AbstractSimpleContainer<Tensor> {

  private static final long serialVersionUID = -6219023432625866714L;

  /**
   * Returns a clone of the content.
   *
   * @return		the clone
   */
  @Override
  protected Tensor cloneContent() {
    return m_Content.dup();
  }
}
