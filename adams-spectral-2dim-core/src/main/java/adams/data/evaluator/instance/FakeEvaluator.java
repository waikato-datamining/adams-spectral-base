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
 * FakeEvaluator.java
 * Copyright (C) 2015-2024 University of Waikato, Hamilton, New Zealand
 */
package adams.data.evaluator.instance;

import adams.core.Randomizable;
import adams.core.UniqueIDs;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Random;

/**
 <!-- globalinfo-start -->
 * Fake evaluator, outputs random values based on min&#47;max values.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-missing-evaluation &lt;float&gt; (property: missingEvaluation)
 * &nbsp;&nbsp;&nbsp;The value to use as replacement for missing evaluations.
 * &nbsp;&nbsp;&nbsp;default: NaN
 * </pre>
 * 
 * <pre>-build-wait &lt;int&gt; (property: buildWait)
 * &nbsp;&nbsp;&nbsp;The time in msec to wait when calling 'performBuild'.
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 * 
 * <pre>-eval-wait &lt;int&gt; (property: evalWait)
 * &nbsp;&nbsp;&nbsp;The time in msec to wait when calling 'performEvaluate'.
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 * 
 * <pre>-eval-min &lt;float&gt; (property: evalMin)
 * &nbsp;&nbsp;&nbsp;The minimum value to output.
 * &nbsp;&nbsp;&nbsp;default: 0.0
 * </pre>
 * 
 * <pre>-eval-max &lt;float&gt; (property: evalMax)
 * &nbsp;&nbsp;&nbsp;The maximum value to output.
 * &nbsp;&nbsp;&nbsp;default: 1.0
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class FakeEvaluator
  extends AbstractEvaluator
  implements Randomizable {

  /** for serialization. */
  private static final long serialVersionUID = -6086808426732510366L;

  /** the seed. */
  protected long m_Seed;

  /** the build wait time in msec. */
  protected int m_BuildWait;

  /** the eval wait time in msec. */
  protected int m_EvalWait;

  /** the minimum to use for the evaluations. */
  protected float m_EvalMin;

  /** the maximum to use for the evaluations. */
  protected float m_EvalMax;

  /** for generating the random numbers. */
  protected Random m_Random;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Fake evaluator, outputs random values based on min/max values.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "build-wait", "buildWait",
      0, 0, null);

    m_OptionManager.add(
	"eval-wait", "evalWait",
	0, 0, null);

    m_OptionManager.add(
	"eval-min", "evalMin",
	0.0f);

    m_OptionManager.add(
	"eval-max", "evalMax",
	1.0f);
  }

  /**
   * Sets the seed value for the random values.
   *
   * @param value 	the seed
   */
  public void setSeed(long value) {
    m_Seed = value;
    reset();
  }

  /**
   * Returns the seed value for the random values.
   *
   * @return 		the seed
   */
  public long getSeed() {
    return m_Seed;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String seedTipText() {
    return "The seed value to use for the random values.";
  }

  /**
   * Sets the time in msec to wait when calling buildClassifier.
   *
   * @param value 	the time in msec
   */
  public void setBuildWait(int value) {
    if (value >= 0) {
      m_BuildWait = value;
      reset();
    }
    else {
      getLogger().warning("BuildWait time must be >= 0, provided: " + value);
    }
  }

  /**
   * Returns the time in msec to wait when calling buildClassifier.
   *
   * @return 		the time in msec
   */
  public int getBuildWait() {
    return m_BuildWait;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String buildWaitTipText() {
    return "The time in msec to wait when calling 'performBuild'.";
  }

  /**
   * Sets the time in msec to wait when calling classifyInstance.
   *
   * @param value 	the time in msec
   */
  public void setEvalWait(int value) {
    if (value >= 0) {
      m_EvalWait = value;
      reset();
    }
    else {
      getLogger().warning("EvalWait time must be >= 0, provided: " + value);
    }
  }

  /**
   * Returns the time in msec to wait when calling classifyInstance.
   *
   * @return 		the time in msec
   */
  public int getEvalWait() {
    return m_EvalWait;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String evalWaitTipText() {
    return "The time in msec to wait when calling 'performEvaluate'.";
  }

  /**
   * Sets the minimum value to output.
   *
   * @param value 	the minimum value
   */
  public void setEvalMin(float value) {
    m_EvalMin = value;
    reset();
  }

  /**
   * Returns the minimum value to output.
   *
   * @return 		the minimum value
   */
  public float getEvalMin() {
    return m_EvalMin;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String evalMinTipText() {
    return "The minimum value to output.";
  }

  /**
   * Sets the maximum value to output.
   *
   * @param value 	the maximum value
   */
  public void setEvalMax(float value) {
    m_EvalMax = value;
    reset();
  }

  /**
   * Returns the maximum value to output.
   *
   * @return 		the maximum value
   */
  public float getEvalMax() {
    return m_EvalMax;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String evalMaxTipText() {
    return "The maximum value to output.";
  }

  /**
   * Returns the default value in case of missing evaluations.
   * 
   * @return		the default value
   */
  @Override
  protected float getDefaultMissingEvaluation() {
    return Float.NaN;
  }

  /**
   * Waits for a specified amount of time.
   *
   * @param msec    the time in msec to wait
   */
  protected void wait(int msec) {
    String  wait;
    int   interval;
    int   current;

    wait     = UniqueIDs.next();
    interval = Math.min(100, msec / 10);
    current  = 0;
    while (current < msec) {
      try {
        synchronized(wait) {
          wait.wait(interval);
        }
        current += interval;
      }
      catch (InterruptedException i) {
        break;
      }
      catch (Exception e) {
        // ignored
      }
    }
  }

  /**
   * Returns the random number generator to use.
   *
   * @return the random number generator
   */
  protected synchronized Random getRandom() {
    if (m_Random == null)
      m_Random = new Random(m_Seed);
    return m_Random;
  }

  /**
   * Performs the actual evaluation.
   *
   * @param data	the instance to check
   * @return		always 1.0
   */
  @Override
  protected Float performEvaluate(Instance data) {
    float    result;

    // wait
    if (m_EvalWait > 0)
      wait(m_EvalWait);

    result = getRandom().nextFloat();
    result = result * (m_EvalMax - m_EvalMin) + m_EvalMin;

    return result;
  }

  /**
   * Builds the evaluator.
   *
   * @param data	the instance to check
   * @return		false if min/max are invalid
   */
  @Override
  protected boolean performBuild(Instances data) {
    if (m_EvalMin >= m_EvalMax)
      return false;

    // wait
    if (m_BuildWait > 0)
      wait(m_BuildWait);

    return true;
  }

  /**
   * Cleans up data structures, frees up memory.
   */
  @Override
  public void cleanUp() {
  }
}
