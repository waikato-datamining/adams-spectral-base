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
 * SpectrumDatabaseWriter.java
 * Copyright (C) 2012-2016 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.transformer;

import adams.core.ShallowCopySupporter;
import adams.data.spectrum.Spectrum;
import adams.flow.core.Actor;
import adams.flow.core.InputConsumer;
import adams.flow.core.OutputProducer;

/**
 * Indicator interface for database writers for spectra.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2357 $
 */
public interface SpectrumDatabaseWriter
  extends Actor, ShallowCopySupporter<Actor>, InputConsumer, OutputProducer, DataContainerDbWriter<Spectrum> {

  /**
   * Sets whether to remove existing containers.
   *
   * @param value 	true if to remove existing containers
   */
  public void setOverwriteExisting(boolean value);

  /**
   * Returns whether to remove existing containers.
   *
   * @return 		true if to remove existing containers
   */
  public boolean getOverwriteExisting();

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public abstract String overwriteExistingTipText();
}
