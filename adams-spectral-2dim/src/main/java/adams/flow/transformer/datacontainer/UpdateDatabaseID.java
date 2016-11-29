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
 * UpdateDatabaseID.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.transformer.datacontainer;

import adams.db.DataProvider;
import adams.data.spectrum.Spectrum;

/**
 * Attempts to update the database ID of the spectrum.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class UpdateDatabaseID
  extends AbstractDataContainerPreProcessor<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = 8536463609958106232L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Attempts to update the database ID of the spectrum.";
  }

  /**
   * Performs the actual pre-processing.
   * 
   * @param data	the data to process
   * @return		the processed data
   */
  @Override
  protected Spectrum doPreProcess(Spectrum data) {
    Spectrum		temp;
    DataProvider	provider;
    
    provider = getOwner().getDataProvider(data);
    temp     = (Spectrum) getOwner().load(provider, data);
    if (temp != null)
      data.setDatabaseID(temp.getDatabaseID());
    
    return data;
  }
}
