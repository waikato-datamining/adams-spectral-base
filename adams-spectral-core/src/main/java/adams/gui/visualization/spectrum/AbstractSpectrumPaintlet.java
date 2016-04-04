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
 * AbstractSpectrumPaintlet.java
 * Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.visualization.spectrum;

import adams.gui.visualization.container.AbstractDataContainerPaintlet;

/**
 * A specialized paintlet for sequence panels.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public abstract class AbstractSpectrumPaintlet
  extends AbstractDataContainerPaintlet {
  
  /** for serialization. */
  private static final long serialVersionUID = 882908294593649205L;

  /**
   * Returns the sequence panel currently in use.
   * 
   * @return		the panel in use
   */
  public SpectrumPanel getSequencePanel() {
    return (SpectrumPanel) m_Panel;
  }
}
