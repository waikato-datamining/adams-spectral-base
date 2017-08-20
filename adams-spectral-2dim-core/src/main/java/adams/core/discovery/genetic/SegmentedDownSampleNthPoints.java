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
 * SegmentedDownSampleNumPoints.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.core.discovery.genetic;

import adams.core.Utils;
import adams.core.discovery.PropertyPath.PropertyContainer;
import adams.data.statistics.StatUtils;
import weka.filters.unsupervised.attribute.SegmentedDownSample;

import java.util.logging.Level;

/**
 * SegmentedDownSample nthPoints handler.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SegmentedDownSampleNthPoints
  extends AbstractGeneticIntegerArrayDiscoveryHandler {


  private static final long serialVersionUID = 5164048153082038468L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Handles the nthPoints parameter of the SegmentedDownSample filter.";
  }

  @Override
  protected int getDefaultSize() {
    return 1;
  }

  /**
   * Returns the default minimum.
   *
   * @return		the default
   */
  @Override
  protected int getDefaultMinimum() {
    return 1;
  }

  /**
   * Returns the default maximum.
   *
   * @return		the default
   */
  @Override
  protected int getDefaultMaximum() {
    return 8;
  }

  /**
   * Returns the integer value from the property container.
   *
   * @param cont	the container
   * @return		the value
   */
  protected int[] getValue(PropertyContainer cont) {
    int[]	result;
    String[]	parts;
    int		i;

    parts  = ((SegmentedDownSample) cont.getObject()).getNthPoints().replaceAll("  ", "").split(" ");
    result = new int[parts.length];

    for (i = 0; i < parts.length; i++) {
      try {
	result[i] = Integer.parseInt(parts[i]);
      }
      catch (Exception e) {
	getLogger().log(Level.SEVERE, "Failed to parse point #" + (i+1) + ": " + parts[i], e);
      }
    }

    return result;
  }

  /**
   * Sets the integer value in the property container.
   *
   * @param cont	the container
   * @param value	the value to set
   */
  protected void setValue(PropertyContainer cont, int[] value) {
    ((SegmentedDownSample) cont.getObject()).setNthPoints(Utils.flatten(StatUtils.toNumberArray(value), " "));
  }

  /**
   * Checks whether this object is handled by this discovery handler.
   *
   * @param obj		the object to check
   * @return		true if handled
   */
  @Override
  protected boolean handles(Object obj) {
    return (obj instanceof SegmentedDownSample);
  }
}
