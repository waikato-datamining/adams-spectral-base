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
 * MinMax.java
 * Copyright (C) 2009-2013 University of Waikato, Hamilton, New Zealand
 */

package adams.data.cleaner.instance;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Checks an attribute in the Instance whether the value is within a certain range.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D &lt;int&gt; (property: debugLevel)
 * &nbsp;&nbsp;&nbsp;The greater the number the more additional info the scheme may output to
 * &nbsp;&nbsp;&nbsp;the console (0 = off).
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 *
 * <pre>-att-name &lt;java.lang.String&gt; (property: attributeName)
 * &nbsp;&nbsp;&nbsp;The name of the attribute to work on; uses last attribute if left empty.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-min &lt;double&gt; (property: minimum)
 * &nbsp;&nbsp;&nbsp;The minimum value the field is allowed to have (inclusive), use -1 to disable.
 * &nbsp;&nbsp;&nbsp;default: -1.0
 * </pre>
 *
 * <pre>-max &lt;double&gt; (property: maximum)
 * &nbsp;&nbsp;&nbsp;The maximum value that the field is allowed to have (incl), use -1 to disable.
 * &nbsp;&nbsp;&nbsp;default: -1.0
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class MinMax
extends AbstractCleaner {

  /** for serialization. */
  private static final long serialVersionUID = -9181146507722202238L;

  /** the attribute name to check, uses last if left empty. */
  protected String m_AttributeName;

  /** the minimum value of the attribute. */
  protected double m_Minimum;

  /** the maximum value of the attribute. */
  protected double m_Maximum;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
    "Checks an attribute in the Instance whether the value is within a "
    + "certain range.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	"att-name", "attributeName",
        "");

    m_OptionManager.add(
	"min", "minimum",
	-1.0);

    m_OptionManager.add(
	"max", "maximum",
	-1.0);
  }

  /**
   * Sets the name of the attribute to work on.
   *
   * @param value 	the attribute name
   */
  public void setAttributeName(String value) {
    m_AttributeName = value;
    reset();
  }

  /**
   * Returns the name of the attribute to work on.
   *
   * @return 		the attribute name
   */
  public String getAttributeName() {
    return m_AttributeName;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String attributeNameTipText() {
    return "The name of the attribute to work on; uses last attribute if left empty.";
  }

  /**
   * Sets the minimum value the field can have (incl.).
   *
   * @param value 	the minimum
   */
  public void setMinimum(double value) {
    m_Minimum = value;
    reset();
  }

  /**
   * Returns the minimum value the field can have (incl.).
   *
   * @return 		the minimum
   */
  public double getMinimum() {
    return m_Minimum;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String minimumTipText() {
    return "The minimum value the field is allowed to have (inclusive), use -1 to disable.";
  }

  /**
   * Sets maximum value the field is allowed to have (incl).
   *
   * @param value 	the maximum
   */
  public void setMaximum(double value) {
    m_Maximum = value;
    reset();
  }

  /**
   * Returns the maximum value the field is allowed to have (incl).
   *
   * @return 		the maximum
   */
  public double getMaximum() {
    return m_Maximum;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String maximumTipText() {
    return "The maximum value that the field is allowed to have (incl), use -1 to disable.";
  }

  /**
   * Performs the actual check.
   *
   * @param data	the Instance to check
   * @return		null if ok, otherwise error message
   */
  @Override
  protected String performCheck(Instance data) {
    Double	value;
    Attribute	att;

    try {
      if (m_AttributeName.length() == 0) {
	att = data.dataset().attribute(data.dataset().numAttributes() - 1);
      }
      else {
	att = data.dataset().attribute(m_AttributeName);
	if (att == null)
	  return "Attribute '" + m_AttributeName + "' not found!";
      }
      if (isLoggingEnabled())
	getLogger().info("Using attribute #" + att.index() + "/'" + att.name() + "'");

      value = data.value(att);

      if ((m_Minimum != -1.0) && (value < m_Minimum))
	return "Value of '" + m_AttributeName + "' below minimum: " + value + "<" + m_Minimum;

      if ((m_Maximum != -1.0) && (value > m_Maximum))
	return "Value of '" + m_AttributeName + "' over maximum: " + value + ">" + m_Maximum;
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Check failed", e);
      return e.toString();
    }

    return null;
  }

  /**
   * Clean Instances.
   *
   * @param instances	Instances
   */
  @Override
  protected Instances performClean(Instances instances) {
    Instances 	result;
    Double	value;
    Attribute	att;
    int		i;
    Instance 	data;

    result = new Instances(instances,0);
    if (m_AttributeName.length() == 0) {
      att = instances.attribute(instances.numAttributes() - 1);
    }
    else {
      att = instances.attribute(m_AttributeName);
      if (att == null) {
	m_CleanInstancesError = "Cannot find attribute '" + m_AttributeName + "'!";
	getLogger().severe(m_CleanInstancesError);
	return null;  // TODO original dataset?
      }
    }

    try {
      for (i = 0; i < instances.numInstances(); i++) {
	data  = instances.get(i);
	value = data.value(att);

	if (((m_Minimum != -1.0) && (value < m_Minimum)) || ((m_Maximum != -1.0) && (value > m_Maximum)))
	  continue;

	result.add((Instance) data.copy());
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to clean", e);
      result = null; // TODO or return original instances?
    }

    return result;
  }
}
