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
 * PostProcessingContainer.java
 * Copyright (C) 2017-2019 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.container;

import adams.data.postprocessor.instances.AbstractPostProcessor;
import adams.data.report.Report;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Container used by post-processors.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class PostProcessingContainer
  extends AbstractContainer {

  /** for serialization. */
  private static final long serialVersionUID = -7431411279172104723L;

  /** the identifier for the output instance. */
  public final static String VALUE_OUTPUT_INSTANCE = "Output Instance";

  /** the identifier for the output instances. */
  public final static String VALUE_OUTPUT_INSTANCES = "Output Instances";

  /** the identifier for the input instance. */
  public final static String VALUE_INPUT_INSTANCE = "Input Instance";

  /** the identifier for the input instances. */
  public final static String VALUE_INPUT_INSTANCES = "Input Instances";

  /** the identifier for the post-processor. */
  public final static String VALUE_POSTPROCESSOR = "Post-processor";

  /** the identifier for the report. */
  public final static String VALUE_REPORT = "Report";

  /**
   * Default constructor.
   */
  public PostProcessingContainer() {
    super();
  }

  /**
   * Initializes the help strings.
   */
  protected void initHelp() {
    super.initHelp();

    addHelp(VALUE_OUTPUT_INSTANCE, "output data row", Instance.class);
    addHelp(VALUE_OUTPUT_INSTANCES, "output dataset", Instances.class);
    addHelp(VALUE_INPUT_INSTANCE, "input data row", Instance.class);
    addHelp(VALUE_INPUT_INSTANCES, "input dataset", Instances.class);
    addHelp(VALUE_POSTPROCESSOR, "post-processor", AbstractPostProcessor.class);
    addHelp(VALUE_REPORT, "report", Report.class);
  }

  /**
   * Initializes the container with the WEKA instance and the post-processor.
   *
   * @param instIn	the input instance
   * @param instOut 	the output instance
   * @param post	the associated post-processor
   */
  public PostProcessingContainer(Instance instIn, Instance instOut, AbstractPostProcessor post) {
    super();
    store(VALUE_OUTPUT_INSTANCE, instOut);
    store(VALUE_INPUT_INSTANCE, instIn);
    store(VALUE_POSTPROCESSOR, post);
  }

  /**
   * Initializes the container with the WEKA instances and the post-processor.
   *
   * @param dataOut	the output instances
   * @param dataIn	the input instances
   * @param post	the post-processor
   */
  public PostProcessingContainer(Instances dataIn, Instances dataOut, AbstractPostProcessor post) {
    super();
    store(VALUE_OUTPUT_INSTANCES, dataOut);
    store(VALUE_INPUT_INSTANCES, dataIn);
    store(VALUE_POSTPROCESSOR, post);
  }

  /**
   * Returns all value names that can be used (theoretically).
   *
   * @return		enumeration over all possible value names
   */
  @Override
  public Iterator<String> names() {
    List<String>	result;

    result = new ArrayList<>();

    result.add(VALUE_INPUT_INSTANCE);
    result.add(VALUE_INPUT_INSTANCES);
    result.add(VALUE_OUTPUT_INSTANCE);
    result.add(VALUE_OUTPUT_INSTANCES);
    result.add(VALUE_POSTPROCESSOR);
    result.add(VALUE_REPORT);

    return result.iterator();
  }

  /**
   * Checks whether the setup of the container is valid.
   *
   * @return		true if all the necessary values are available
   */
  @Override
  public boolean isValid() {
    return (hasValue(VALUE_OUTPUT_INSTANCE) && hasValue(VALUE_POSTPROCESSOR))
      || (hasValue(VALUE_OUTPUT_INSTANCES) && hasValue(VALUE_POSTPROCESSOR));
  }
}
