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
 * RemoveDuplicateSpectra.java
 * Copyright (C) 2026 University of Waikato, Hamilton, New Zealand
 */

package adams.data.cleaner.instance;

import adams.data.instances.ArffUtils;
import adams.data.instances.InstanceComparator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Removes duplicate spectra from the dataset.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class RemoveDuplicateSpectra
  extends AbstractCleaner {

  private static final long serialVersionUID = -2381335210666399384L;

  /**
   * The removal mode.
   */
  public enum Mode {
    FAST,
    ACCURATE,
  }

  /** how the duplicates are removed. */
  protected Mode m_Mode;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Removes duplicate spectra from the dataset.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "mode", "mode",
      Mode.FAST);
  }

  /**
   * Sets the mode for removing the duplicates.
   *
   * @param value	the mode
   */
  public void setMode(Mode value) {
    m_Mode = value;
    reset();
  }

  /**
   * Returns the mode for removing the duplicates.
   *
   * @return  		the mode
   */
  public Mode getMode() {
    return m_Mode;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String modeTipText() {
    return "How the duplicate spectra are removed.";
  }

  /**
   * Performs the actual check.
   *
   * @param data the instance to check
   * @return null if ok, otherwise error message
   */
  @Override
  protected String performCheck(Instance data) {
    return null;
  }

  /**
   * Removes the specified rows from the data.
   *
   * @param data	the data to process
   * @param indices	the rows to remove
   * @return		the reduced dataset (copy)
   */
  protected Instances remove(Instances data, int[] indices) {
    Instances		result;
    TIntSet		remove;
    int			i;

    remove = new TIntHashSet(indices);
    result = new Instances(data, data.numInstances() - indices.length);
    for (i = 0; i < data.numInstances(); i++) {
      if (!remove.contains(i))
	result.add((Instance) data.instance(i).copy());
    }

    return result;
  }

  /**
   * Performs a fast identification of duplicates.
   *
   * @param data 	the data to check
   * @return 		the (potentially) updated dataset
   */
  protected Instances fast(Instances data) {
    int[]			indices;
    Map<Double, List<Integer>>	counts;
    Instance			inst;
    int				i;
    double			sum;
    TIntSet			remove;

    indices = ArffUtils.amplitudes(data, false);
    counts  = new HashMap<>();
    for (i = 0; i < data.numInstances(); i++) {
      sum = 0;
      inst = data.instance(i);
      for (int index: indices)
	sum += inst.value(index);
      if (!counts.containsKey(sum))
	counts.put(sum, new ArrayList<>());
      counts.get(sum).add(i);
    }

    remove = new TIntHashSet();
    for (List<Integer> count: counts.values()) {
      if (count.size() > 1) {
	for (i = 1; i < count.size(); i++)
	  remove.add(count.get(i));
      }
    }

    if (remove.isEmpty())
      return data;
    else
      return remove(data, remove.toArray());
  }

  /**
   * Performs an accurate identification of duplicates.
   *
   * @param data 	the data to check
   * @return 		the (potentially) updated dataset
   */
  protected Instances accurate(Instances data) {
    int[]		ampls;
    InstanceComparator	comp;
    TIntList		remove;
    int			start;
    int			i;
    Instance		curr;
    int			res;

    // sort on amplitudes alone
    ampls = ArffUtils.amplitudes(data, false);
    comp  = new InstanceComparator(ampls);
    data  = new Instances(data);
    data.sort(comp);

    remove = new TIntArrayList();
    start  = 0;
    while (start < data.numInstances() - 1) {
      curr = data.instance(start);
      for (i = start + 1; i < data.numInstances(); i++) {
	res = comp.compare(curr, data.instance(i));
	if (res == 0) {
	  remove.add(i);
	}
	else {
	  start = i;
	  break;
	}
      }
    }

    return remove(data, remove.toArray());
  }

  /**
   * Performs the actual check.
   *
   * @param data the instance to check
   * @return the cleaned data, null in case of error
   */
  @Override
  protected Instances performClean(Instances data) {
    Instances	result;
    long	start;
    long	duration;

    start = System.currentTimeMillis();
    switch (m_Mode) {
      case FAST:
	result = fast(data);
	break;

      case ACCURATE:
	result = accurate(data);
	break;

      default:
	throw new IllegalStateException("Unhandled mode: " + m_Mode);
    }
    duration = System.currentTimeMillis() - start;
    if (isLoggingEnabled()) {
      getLogger().info("Dataset: " + data.relationName());
      getLogger().info("Mode: " + m_Mode);
      getLogger().info("Time to remove: " + duration + " msec");
      getLogger().info("Rows in dataset: " + data.numInstances());
      getLogger().info("# rows removed: " + (data.numInstances() - result.numInstances()));
    }

    return result;
  }
}
