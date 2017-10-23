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
 * CleaningContainer.java
 * Copyright (C) 2011-2016 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.container;

import adams.data.cleaner.instance.AbstractCleaner;
import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.SpreadSheet;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Container used by cleaners.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2391 $
 */
public class CleaningContainer
  extends AbstractContainer {

  /** for serialization. */
  private static final long serialVersionUID = -7431411279172104723L;

  /** the identifier for the instance. */
  public final static String VALUE_INSTANCE = "Instance";

  /** the identifier for the instances. */
  public final static String VALUE_INSTANCES = "Instances";

  /** the identifier for the checks. */
  public final static String VALUE_CHECKS = "Checks";

  /** the identifier for the cleaner. */
  public final static String VALUE_CLEANER = "Cleaner";

  /**
   * Default constructor.
   */
  public CleaningContainer() {
    super();
  }

  /**
   * Initializes the container with the WEKA instance.
   *
   * @param inst	the instance
   */
  public CleaningContainer(Instance inst) {
    this(inst, new DefaultSpreadSheet());
  }

  /**
   * Initializes the help strings.
   */
  protected void initHelp() {
    super.initHelp();

    addHelp(VALUE_INSTANCE, "data row; " + Instance.class.getName());
    addHelp(VALUE_INSTANCES, "dataset; " + Instances.class.getName());
    addHelp(VALUE_CHECKS, "checks; " + SpreadSheet.class.getName());
    addHelp(VALUE_CLEANER, "cleaner; " + AbstractCleaner.class.getName());
  }

  /**
   * Initializes the container with the WEKA instance.
   *
   * @param inst	the instance
   * @param checks	the associated checks
   */
  public CleaningContainer(Instance inst, SpreadSheet checks) {
    super();
    store(VALUE_INSTANCE, inst);
    store(VALUE_CHECKS, checks);
  }

  /**
   * Initializes the container with the WEKA instances.
   *
   * @param inst	the instances
   */
  public CleaningContainer(Instances inst) {
    super();
    store(VALUE_INSTANCES, inst);
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
    result.add(VALUE_CHECKS);
    result.add(VALUE_CLEANER);

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
