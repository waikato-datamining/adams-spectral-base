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
 * Copyright (C) 2009-2026 University of Waikato, Hamilton, New Zealand
 */

package adams.data.instances;

import adams.core.Utils;
import adams.data.statistics.StatUtils;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

import java.util.Arrays;

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
   * @see		#PREFIX_WAVE_NUMBER
   */
  public static String getWaveNumberName(int index) {
    return getWaveNumberName(PREFIX_WAVE_NUMBER, index);
  }

  /**
   * Returns the name of an attribute for a wave number. Gets prefixed
   * with the specified prefix.
   *
   * @param prefix	the prefix string to use
   * @param index	the 0-based index
   * @return		the attribute name
   */
  public static String getWaveNumberName(String prefix, int index) {
    return prefix + (index+1);
  }

  /**
   * Returns the name of an attribute for a wave number. Gets prefixed
   * with "wave-number-".
   *
   * @param waveno	the wave number
   * @return		the attribute name
   * @see		#PREFIX_WAVE_NUMBER
   */
  public static String getWaveNumberName(float waveno) {
    return getWaveNumberName(PREFIX_WAVE_NUMBER, waveno);
  }

  /**
   * Returns the name of an attribute for a wave number. Gets prefixed
   * with the specified prefix.
   *
   * @param prefix	the prefix string to use
   * @param waveno	the wave number
   * @return		the attribute name
   */
  public static String getWaveNumberName(String prefix, float waveno) {
    return prefix + waveno;
  }

  /**
   * Returns the name of an attribute for an amplitude. Gets prefixed
   * with "amplitude-".
   *
   * @param index	the 0-based index
   * @return		the attribute name
   * @see		#PREFIX_AMPLITUDE
   */
  public static String getAmplitudeName(int index) {
    return getAmplitudeName(PREFIX_AMPLITUDE, index);
  }

  /**
   * Returns the name of an attribute for an amplitude. Gets prefixed
   * with the specified prefix.
   *
   * @param prefix	the prefix string to use
   * @param index	the 0-based index
   * @return		the attribute name
   */
  public static String getAmplitudeName(String prefix, int index) {
    return prefix + (index+1);
  }

  /**
   * Returns the name of an attribute for an amplitude. Gets prefixed
   * with "amplitude-".
   *
   * @param waveno	the wave number
   * @return		the attribute name
   */
  public static String getAmplitudeName(float waveno) {
    return getAmplitudeName(PREFIX_AMPLITUDE, waveno);
  }

  /**
   * Returns the name of an attribute for an amplitude. Gets prefixed
   * with the specified prefix.
   *
   * @param prefix	the prefix string to use
   * @param waveno	the wave number
   * @return		the attribute name
   */
  public static String getAmplitudeName(String prefix, float waveno) {
    return prefix + waveno;
  }

  /**
   * Returns the indices of the amplitudes in the dataset.
   *
   * @param data	the dataset to analyze
   * @param oneBased 	whether to return 1-based or 0-based indices
   * @return		the indices
   */
  public static int[] amplitudes(Instances data, boolean oneBased) {
    int[]	result;
    TIntList 	atts;
    int		i;

    atts = new TIntArrayList();
    for (i = 0; i < data.numAttributes(); i++) {
      if (data.attribute(i).name().startsWith(PREFIX_AMPLITUDE))
	atts.add(i);
    }

    result = atts.toArray();
    if (oneBased)
      result = Utils.toOneBasedIndices(result);

    return result;
  }

  /**
   * Returns the indices of the attributes to remove, i.e.,
   * ID attributes and string attributes. Class attribute is never listed.
   *
   * @param data	the data to analyze
   * @param oneBased 	whether to return 1-based or 0-based indices
   * @return		the indices to remove
   */
  public static int[] toRemove(Instances data, boolean oneBased) {
    int[]	result;
    TIntSet	atts;
    Attribute	att;
    int		i;

    atts = new TIntHashSet();
    if ((att = data.attribute(ArffUtils.getDBIDName())) != null)
      atts.add(att.index());
    if ((att = data.attribute(ArffUtils.getIDName())) != null)
      atts.add(att.index());
    if ((att = data.attribute(ArffUtils.getSampleIDName())) != null)
      atts.add(att.index());
    for (i = 0; i < data.numAttributes(); i++) {
      if (i == data.classIndex())
	continue;
      if (data.attribute(i).isString())
	atts.add(i);
    }

    result = atts.toArray();
    Arrays.sort(result);

    if (oneBased)
      result = Utils.toOneBasedIndices(result);

    return result;
  }

  /**
   * Initializes the Remove filter for removing all IDs (and string attributes) 
   * from the dataset.
   *
   * @param data	the data to use for the analysis
   * @return		the configured filter, null if no filtering required
   */
  public static Remove getRemoveFilter(Instances data) {
    Remove		result;
    int[]		indices;

    indices = toRemove(data, true);

    if (indices.length > 0) {
      result = new Remove();
      result.setAttributeIndices(adams.core.Utils.flatten(StatUtils.toNumberArray(indices), ","));
    }
    else {
      result = null;
    }

    return result;
  }
}
