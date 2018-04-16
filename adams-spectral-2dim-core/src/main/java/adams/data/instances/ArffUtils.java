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
 * ArffUtils.java
 * Copyright (C) 2009-2018 University of Waikato, Hamilton, New Zealand
 */

package adams.data.instances;

import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

import java.util.Vector;

/**
 * A helper class for turning spectrum data into ARFF files and vice versa.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class ArffUtils
  extends adams.data.weka.ArffUtils {

  public static final String SAMPLE_ID = "sample_id";

  public static final String PREFIX_WAVE_NUMBER = "wave-number-";

  public static final String PREFIX_AMPLITUDE = "amplitude-";

  /**
   * Returns the name of the attribute containing the sample ID of the spectrum.
   *
   * @return		the attribute name
   */
  public static String getSampleIDName() {
    return SAMPLE_ID;
  }

  /**
   * Returns the name of an attribute for a wave number. Gets prefixed
   * with "wave-number-".
   *
   * @param index	the 0-based index
   * @return		the attribute name
   */
  public static String getWaveNumberName(int index) {
    return PREFIX_WAVE_NUMBER + (index+1);
  }

  /**
   * Returns the name of an attribute for a wave number. Gets prefixed
   * with "wave-number-".
   *
   * @param waveno	the wave number
   * @return		the attribute name
   */
  public static String getWaveNumberName(float waveno) {
    return PREFIX_WAVE_NUMBER + waveno;
  }

  /**
   * Returns the name of an attribute for an amplitude. Gets prefixed
   * with "amplitude-".
   *
   * @param index	the 0-based index
   * @return		the attribute name
   */
  public static String getAmplitudeName(int index) {
    return PREFIX_AMPLITUDE + (index+1);
  }

  /**
   * Returns the name of an attribute for an amplitude. Gets prefixed
   * with "amplitude-".
   *
   * @param waveno	the wave number
   * @return		the attribute name
   */
  public static String getAmplitudeName(float waveno) {
    return PREFIX_AMPLITUDE + waveno;
  }

  /**
   * Initializes the Remove filter for removing all IDs (and string attributes) 
   * from the dataset.
   *
   * @param data	the data to use for the analysis
   * @return		the configured filter, null if no filtering required
   * @throws Exception	if filter setup fails
   */
  public static Remove getRemoveFilter(Instances data) {
    Remove		result;
    Vector<String>	atts;
    Attribute		att;
    int			i;

    // check names
    atts = new Vector<>();
    if ((att = data.attribute(ArffUtils.getDBIDName())) != null)
      atts.add("" +(att.index() + 1));
    if ((att = data.attribute(ArffUtils.getIDName())) != null)
      atts.add("" + (att.index() + 1));
    if ((att = data.attribute(ArffUtils.getSampleIDName())) != null)
      atts.add("" + (att.index() + 1));
    
    // check string attributes
    for (i = 0; i < data.numAttributes(); i++) {
      if (i == data.classIndex())
	continue;
      if (data.attribute(i).isString()) {
	if (!atts.contains("" + (i+1)))
	  atts.add("" + (i+1));
      }
    }
    
    if (atts.size() > 0) {
      result = new Remove();
      result.setAttributeIndices(adams.core.Utils.flatten(atts, ","));
    }
    else {
      result = null;
    }

    return result;
  }
}
