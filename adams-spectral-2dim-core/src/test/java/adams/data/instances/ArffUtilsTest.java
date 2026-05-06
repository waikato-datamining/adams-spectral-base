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
 * ArffUtilsTest.java
 * Copyright (C) 2026 University of Waikato, Hamilton, New Zealand
 */

package adams.data.instances;

import adams.env.Environment;
import adams.test.AdamsTestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the ArffUtils class.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class ArffUtilsTest
  extends AdamsTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name the name of the test
   */
  public ArffUtilsTest(String name) {
    super(name);
  }

  /**
   * Generates an empty dataset.
   *
   * @param dbid	add a DBID attribute?
   * @param id		add an ID attribute?
   * @param sampleid	add a sample ID attribute?
   * @param strings	add some string attributes?
   * @param cls		add a class attribute?
   * @param numAmpl	how many amplitude attributes?
   * @return		the dataset
   */
  protected Instances createDataset(boolean dbid, boolean id, boolean sampleid, boolean strings, boolean cls, int numAmpl) {
    Instances			result;
    ArrayList<Attribute> 	atts;
    int				i;

    atts = new ArrayList<>();
    if (dbid)
      atts.add(new Attribute(ArffUtils.getDBIDName()));
    if (id)
      atts.add(new Attribute(ArffUtils.getIDName()));
    if (sampleid)
      atts.add(new Attribute(ArffUtils.getSampleIDName()));
    if (strings)
      atts.add(new Attribute("string1", (List<String>) null));
    for (i = 0; i < numAmpl; i++)
      atts.add(new Attribute(ArffUtils.getAmplitudeName(i)));
    if (strings)
      atts.add(new Attribute("string2", (List<String>) null));
    if (cls)
      atts.add(new Attribute("class"));

    result = new Instances(
      "dbid=" + dbid + ", id=" + id + ", sampleid=" + sampleid + ", stringAtts=" + strings
	+ ", clsAtt=" + cls + ", numAmpl=" + numAmpl,
      atts, 0);

    return result;
  }

  /**
   * Tests the getRemoveFilter method.
   */
  public void testGetRemoveFilter() {
    Instances	data;
    Remove	filter;

    data   = createDataset(false, false, false, false, false, 0);
    filter = ArffUtils.getRemoveFilter(data);
    assertNull("Should not have returned a filter", filter);

    data   = createDataset(true, false, false, false, true, 5);
    filter = ArffUtils.getRemoveFilter(data);
    assertEquals("Index mismatch", "1", filter.getAttributeIndices());

    data   = createDataset(true, true, false, false, true, 5);
    filter = ArffUtils.getRemoveFilter(data);
    assertEquals("Index mismatch", "1,2", filter.getAttributeIndices());

    data   = createDataset(true, false, true, false, true, 5);
    filter = ArffUtils.getRemoveFilter(data);
    assertEquals("Index mismatch", "1,2", filter.getAttributeIndices());

    data   = createDataset(true, true, true, false, true, 5);
    filter = ArffUtils.getRemoveFilter(data);
    assertEquals("Index mismatch", "1,2,3", filter.getAttributeIndices());

    data   = createDataset(true, true, true, true, true, 5);
    filter = ArffUtils.getRemoveFilter(data);
    assertEquals("Index mismatch", "1,2,3,4,10", filter.getAttributeIndices());
  }

  /**
   * Tests the toRemove method.
   */
  public void testToRemove() {
    Instances	data;
    int[]	indices;

    data    = createDataset(false, false, false, false, false, 0);
    indices = ArffUtils.toRemove(data, true);
    assertEqualsArrays("Index mismatch", new int[]{}, indices);

    data    = createDataset(true, false, false, false, true, 5);
    indices = ArffUtils.toRemove(data, true);
    assertEqualsArrays("Index mismatch", new int[]{1}, indices);

    data    = createDataset(true, true, false, false, true, 5);
    indices = ArffUtils.toRemove(data, true);
    assertEqualsArrays("Index mismatch", new int[]{1,2}, indices);

    data    = createDataset(true, false, true, false, true, 5);
    indices = ArffUtils.toRemove(data, true);
    assertEqualsArrays("Index mismatch", new int[]{1,2}, indices);

    data    = createDataset(true, true, true, false, true, 5);
    indices = ArffUtils.toRemove(data, true);
    assertEqualsArrays("Index mismatch", new int[]{1,2,3}, indices);

    data    = createDataset(true, true, true, true, true, 5);
    indices = ArffUtils.toRemove(data, true);
    assertEqualsArrays("Index mismatch", new int[]{1,2,3,4,10}, indices);

    data    = createDataset(true, true, true, true, true, 5);
    indices = ArffUtils.toRemove(data, false);
    assertEqualsArrays("Index mismatch", new int[]{0,1,2,3,9}, indices);
  }

  /**
   * Tests the amplitudes method.
   */
  public void testAmplitudes() {
    Instances	data;
    int[]	indices;

    data    = createDataset(false, false, false, false, false, 0);
    indices = ArffUtils.amplitudes(data, true);
    assertEqualsArrays("Index mismatch", new int[]{}, indices);

    data    = createDataset(true, false, false, false, true, 5);
    indices = ArffUtils.amplitudes(data, true);
    assertEqualsArrays("Index mismatch", new int[]{2,3,4,5,6}, indices);

    data    = createDataset(true, true, true, true, true, 5);
    indices = ArffUtils.amplitudes(data, true);
    assertEqualsArrays("Index mismatch", new int[]{5,6,7,8,9}, indices);

    data    = createDataset(true, true, true, true, true, 5);
    indices = ArffUtils.amplitudes(data, false);
    assertEqualsArrays("Index mismatch", new int[]{4,5,6,7,8}, indices);
  }

  /**
   * Returns a test suite.
   *
   * @return		the test suite
   */
  public static Test suite() {
    return new TestSuite(ArffUtilsTest.class);
  }

  /**
   * Runs the test from commandline.
   *
   * @param args	ignored
   */
  public static void main(String[] args) {
    Environment.setEnvironmentClass(Environment.class);
    runTest(suite());
  }
}
