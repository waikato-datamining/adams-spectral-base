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
 * EvaluationContainer.java
 * Copyright (C) 2011-2019 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.container;

import adams.core.Utils;
import adams.data.evaluator.instance.AbstractEvaluator;
import adams.data.report.Report;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Container for evaluations.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class EvaluationContainer
  extends AbstractContainer
  implements ContainerWithReport {

  /** for serialization. */
  private static final long serialVersionUID = -7431411279172104723L;

  /** the identifier for the instance. */
  public final static String VALUE_INSTANCE = "Instance";

  /** the identifier for the classification. */
  public final static String VALUE_CLASSIFICATION = "Classification";

  /** the identifier for the classification of an abstaining classifier. */
  public final static String VALUE_ABSTENTION_CLASSIFICATION = "Abstention classification";

  /** the identifier for the class distribution. */
  public final static String VALUE_DISTRIBUTION = "Distribution";

  /** the identifier for the instances. */
  public final static String VALUE_INSTANCES = "Instances";

  /** the identifier for the evaluations. */
  public final static String VALUE_EVALUATIONS = "Evaluations";

  /** the identifier for the evaluatior. */
  public final static String VALUE_EVALUATOR = "Evaluator";

  /** the identifier for the component. */
  public final static String VALUE_COMPONENT = "Component";

  /** the identifier for the version. */
  public final static String VALUE_VERSION = "Version";

  /** the identifier for the (optional) ID. */
  public final static String VALUE_ID = "ID";

  /**
   * Default constructor.
   */
  public EvaluationContainer() {
    super();
    store(VALUE_EVALUATIONS, new HashMap<String,Object>());
    store(VALUE_COMPONENT, "");
  }

  /**
   * Initializes the container with the WEKA instance.
   *
   * @param inst	the instance
   */
  public EvaluationContainer(Instance inst) {
    this(inst, new HashMap<>());
  }

  /**
   * Initializes the container with the WEKA instance.
   *
   * @param inst	the instance
   * @param evaluations	the associated evaluations
   */
  public EvaluationContainer(Instance inst, Map<String,Object> evaluations) {
    this();
    store(VALUE_INSTANCE, inst);
    store(VALUE_EVALUATIONS, evaluations);
    store(VALUE_COMPONENT, "");
  }

  /**
   * Initializes the container with the WEKA instances.
   *
   * @param inst	the instances
   */
  public EvaluationContainer(Instances inst) {
    this();
    store(VALUE_INSTANCES, inst);
    store(VALUE_COMPONENT, "");
  }

  /**
   * Initializes the help strings.
   */
  protected void initHelp() {
    super.initHelp();

    addHelp(VALUE_INSTANCE, "data row", Instance.class);
    addHelp(VALUE_INSTANCES, "dataset", Instances.class);
    addHelp(VALUE_EVALUATIONS, "mapping of evaluations (String/Object)", Map.class);
    addHelp(VALUE_EVALUATOR, "evaluator used", AbstractEvaluator.class);
    addHelp(VALUE_CLASSIFICATION, "classification; " + Utils.classToString(Double.class) + " or " + Utils.classToString(String.class));
    addHelp(VALUE_ABSTENTION_CLASSIFICATION, "abstention classification; " + Utils.classToString(Double.class) + " or " + Utils.classToString(String.class));
    addHelp(VALUE_DISTRIBUTION, "class distribution", double[].class);
    addHelp(VALUE_COMPONENT, "component name", String.class);
    addHelp(VALUE_VERSION, "version", String.class);
    addHelp(VALUE_ID, "(optional) ID", String.class);
    addHelp(VALUE_REPORT, "report for storing meta-data", Report.class);
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

    result.add(VALUE_INSTANCE);
    result.add(VALUE_INSTANCES);
    result.add(VALUE_EVALUATIONS);
    result.add(VALUE_EVALUATOR);
    result.add(VALUE_CLASSIFICATION);
    result.add(VALUE_ABSTENTION_CLASSIFICATION);
    result.add(VALUE_DISTRIBUTION);
    result.add(VALUE_COMPONENT);
    result.add(VALUE_VERSION);
    result.add(VALUE_ID);
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
    return (hasValue(VALUE_INSTANCE) || hasValue(VALUE_INSTANCES));
  }
}
