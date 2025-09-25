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
 * SpectrumContainerModel.java
 * Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.visualization.spectrum;

import adams.gui.visualization.container.ContainerListManager;
import adams.gui.visualization.container.ContainerModel;

/**
 * A model for displaying the currently loaded spectra.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public class SpectrumContainerModel
  extends ContainerModel<SpectrumContainerManager, SpectrumContainer> {

  /** for serialization. */
  private static final long serialVersionUID = -7545555938539538659L;

  /**
   * Initializes the model.
   *
   * @param manager	the managing object to obtain the data from
   */
  public SpectrumContainerModel(ContainerListManager<SpectrumContainerManager> manager) {
    super((manager == null) ? null : manager.getContainerManager());
  }

  /**
   * Initializes the model.
   *
   * @param manager	the manager to obtain the data from
   */
  public SpectrumContainerModel(SpectrumContainerManager manager) {
    super(manager);
  }

  /**
   * Initializes members.
   */
  protected void initialize() {
    super.initialize();

    m_Generator           = new SpectrumContainerDisplayIDGenerator();
    m_ColumnNameGenerator = new SpectrumContainerTableColumnNameGenerator();
  }
}