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
 * AmplitudeFinder.java
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package adams.data.columnfinder;

import adams.data.weka.columnfinder.AbstractColumnFinder;
import weka.core.Instances;

import java.util.ArrayList;

/**
 * Selects all the attributes that start with {@link #PREFIX_AMPLITUDE}.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1736 $
 */
public class AmplitudeFinder
  extends AbstractColumnFinder {

  /** for serialization. */
  private static final long serialVersionUID = -750282683662266274L;
  
  /** the prefix for a spectral attribute. */
  public final static String PREFIX_AMPLITUDE = "amplitude-";

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Selects all the attributes that start with " + PREFIX_AMPLITUDE + ".";
  }

  /**
   * Returns the columns of interest in the dataset.
   * 
   * @param data	the dataset to inspect
   * @return		the columns of interest
   */
  @Override
  protected int[] doFindColumns(Instances data) {
    int[]		result;
    ArrayList<Integer>	amps;
    int			i;
    
    amps = new ArrayList<Integer>();
    for (i = 0; i < data.numAttributes(); i++) {
      if (data.attribute(i).name().startsWith(PREFIX_AMPLITUDE))
	amps.add(i);
    }
    
    result = new int[amps.size()];
    for (i = 0; i < amps.size(); i++)
      result[i] = amps.get(i);
    
    return result;
  }
}
