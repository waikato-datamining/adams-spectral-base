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
 * AbstractEvaluatorTestCase.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.data.evaluator.instance;

import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.core.logging.LoggingHelper;
import adams.core.option.OptionUtils;
import adams.test.AbstractTestHelper;
import adams.test.AdamsTestCase;
import adams.test.TestHelper;
import adams.test.TmpFile;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Ancestor for Evaluator test cases.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public abstract class AbstractEvaluatorTestCase
  extends AdamsTestCase {

  /** property indicating whether regression tests should not be executed. */
  public final static String PROPERTY_NODATAREGRESSION = "adams.test.data.noregression";

  /** whether to execute the data regression test. */
  protected boolean m_NoDataRegressionTest;

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name the name of the test
   */
  public AbstractEvaluatorTestCase(String name) {
    super(name);
  }

  /**
   * Called by JUnit before each test method.
   *
   * @throws Exception if an error occurs
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m_NoDataRegressionTest = Boolean.getBoolean(PROPERTY_NODATAREGRESSION);
  }

  /**
   * Returns the test helper class to use.
   *
   * @return		the helper class instance
   */
  @Override
  protected AbstractTestHelper newTestHelper() {
    return new TestHelper(this, "adams/data/evaluator/instance/data");
  }

  /**
   * Loads the data to process.
   *
   * @param filename	the filename to load (without path)
   * @param classIndex	the class index, empty string for none, 'first', 'last' or 1-based index
   * @return		the data, null if it could not be loaded
   */
  protected Instances load(String filename, String classIndex) {
    Instances	result;

    result = null;

    if (!m_TestHelper.copyResourceToTmp(filename))
      fail("Failed to copy resource: " + filename);

    try {
      result = DataSource.read(new TmpFile(filename).getAbsolutePath());
      if (result != null) {
	if (classIndex.equals("first"))
	  result.setClassIndex(0);
	else if (classIndex.equals("last"))
	  result.setClassIndex(result.numAttributes() - 1);
	else if (!classIndex.isEmpty())
	  result.setClassIndex(Integer.parseInt(classIndex) - 1);
      }
    }
    catch (Exception e) {
      fail("Failed to load Instances from '" + filename + "'!\n" + LoggingHelper.throwableToString(e));
    }

    m_TestHelper.deleteFileFromTmp(filename);

    return result;
  }

  /**
   * Processes the input data and returns the processed data.
   *
   * @param data	the data to work on (gets randomized with "Random(1)" before split)
   * @param split	the train/test split percentage (0-1)
   * @param scheme	the scheme to process the data with
   * @return		the processed data
   */
  protected List<HashMap<String,Float>> process(Instances data, double split, AbstractEvaluator scheme) {
    List<HashMap<String,Float>> 	result;
    int					numTrain;
    Instances				train;
    int					i;
    HashMap<String,Float>		eval;

    if ((split <= 0) || (split >= 1))
      split = 0.8;

    result = new ArrayList<>();
    data   = new Instances(data);
    data.randomize(new Random(1));
    numTrain = (int) Math.round(data.numInstances() * split);
    train    = new Instances(data, 0, numTrain);
    scheme.build(train);
    for (i = numTrain; i < data.numInstances(); i++) {
      eval = scheme.evaluate(data.instance(i));
      result.add(eval);
    }

    return result;
  }

  /**
   * Saves the data in the tmp directory.
   *
   * @param data	the data to save
   * @param filename	the filename to save to (without path)
   * @return		true if successfully saved
   */
  protected boolean save(List<HashMap<String,Float>> data, String filename) {
    StringBuilder		content;
    int				i;
    HashMap<String,Float>	eval;
    List<String>		keys;

    content = new StringBuilder();
    for (i = 0; i < data.size(); i++) {
      if (i > 0)
	content.append("\n");
      content.append("evaluation #" + i + "\n");
      eval = data.get(i);
      keys = new ArrayList<>(eval.keySet());
      Collections.sort(keys);
      for (String key: keys) {
	content.append(key);
	content.append("=");
	content.append(Utils.doubleToString(eval.get(key), 6));
	content.append("\n");
      }
    }

    return FileUtils.writeToFile(new TmpFile(filename).getAbsolutePath(), content, false);
  }

  /**
   * Returns the filenames (without path) of the input data files to use
   * in the regression test.
   *
   * @return		the filenames
   */
  protected abstract String[] getRegressionInputFiles();

  /**
   * Returns the class indices for the input data files to be used in the
   * regression test.
   *
   * @return		the class indices ('first', 'last', 1-based, or empty string for none)
   */
  protected abstract String[] getRegressionInputClasses();

  /**
   * Returns the split percentages for the input files (train/test; 0.6 = 60% for train).
   *
   * @return		the setups
   */
  protected abstract double[] getRegressionInputSplitPercentages();

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  protected abstract AbstractEvaluator[] getRegressionSetups();

  /**
   * Creates an output filename based on the input filename.
   *
   * @param input	the input filename (no path)
   * @param no		the number of the test
   * @return		the generated output filename (no path)
   */
  protected String createOutputFilename(String input, int no) {
    String	result;
    int		index;
    String	ext;

    ext = "-out" + no;

    index = input.lastIndexOf('.');
    if (index == -1) {
      result = input + ext;
    }
    else {
      result  = input.substring(0, index);
      result += ext;
      result += input.substring(index);
    }

    return result;
  }

  /**
   * Compares the processed data against previously saved output data.
   */
  public void testRegression() {
    Instances			data;
    List<HashMap<String,Float>>	processed;
    boolean			ok;
    String			regression;
    int				i;
    String[]			input;
    String[]			classes;
    double[]			splits;
    AbstractEvaluator[]		setups;
    AbstractEvaluator		current;
    String[]			output;
    TmpFile[]			outputFiles;

    if (m_NoRegressionTest || m_NoDataRegressionTest)
      return;

    setUpBeforeRegression();

    input   = getRegressionInputFiles();
    classes = getRegressionInputClasses();
    output  = new String[input.length];
    setups  = getRegressionSetups();
    splits  = getRegressionInputSplitPercentages();
    assertEquals("Number of files and setups differ!",  input.length, setups.length);
    assertEquals("Number of files and classes differ!", input.length, classes.length);
    assertEquals("Number of files and splits differ!",  input.length, splits.length);

    // process data
    for (i = 0; i < input.length; i++) {
      data = load(input[i], classes[i]);
      assertNotNull("Could not load data for regression test from " + input[i], data);

      current = (AbstractEvaluator) OptionUtils.shallowCopy(setups[i], false);
      assertNotNull("Failed to create copy of algorithm: " + OptionUtils.getCommandLine(setups[i]), current);

      processed = process(data, splits[i], current);
      assertNotNull("Failed to process data?", processed);

      output[i] = createOutputFilename(input[i], i);
      ok        = save(processed, output[i]);
      assertTrue("Failed to save regression data?", ok);

      current.destroy();
    }

    // test regression
    outputFiles = new TmpFile[output.length];
    for (i = 0; i < output.length; i++)
      outputFiles[i] = new TmpFile(output[i]);
    regression = m_Regression.compare(outputFiles);
    assertNull("Output differs:\n" + regression, regression);

    // remove output, clean up scheme
    for (i = 0; i < output.length; i++) {
      setups[i].destroy();
      m_TestHelper.deleteFileFromTmp(output[i]);
    }
    cleanUpAfterRegression();
  }

  /**
   * For further setting up before the regression tests.
   * <br><br>
   * Default implementation does nothing.
   */
  protected void setUpBeforeRegression() {
  }

  /**
   * For further cleaning up after the regression tests.
   * <br><br>
   * Default implementation does nothing.
   */
  protected void cleanUpAfterRegression() {
  }
}
