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
 * PostProcessingContainerToWekaInstanceContainer.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.data.conversion;

import adams.core.Utils;
import adams.data.instance.WekaInstanceContainer;
import adams.data.report.Report;
import adams.flow.container.PostProcessingContainer;
import weka.core.Instance;

/**
 <!-- globalinfo-start -->
 * Converts a adams.flow.container.PostProcessingContainer container to a adams.data.instance.WekaInstanceContainer one.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class PostProcessingContainerToWekaInstanceContainer
  extends AbstractConversion {

  private static final long serialVersionUID = 7412720040702868997L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Converts a " + Utils.classToString(PostProcessingContainer.class)
      + " container to a " + Utils.classToString(WekaInstanceContainer.class) + " one.";
  }

  /**
   * Returns the class that is accepted as input.
   *
   * @return		the class
   */
  @Override
  public Class accepts() {
    return PostProcessingContainer.class;
  }

  /**
   * Returns the class that is generated as output.
   *
   * @return		the class
   */
  @Override
  public Class generates() {
    return WekaInstanceContainer.class;
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  protected Object doConvert() throws Exception {
    WekaInstanceContainer	result;
    PostProcessingContainer	input;

    input = (PostProcessingContainer) m_Input;
    result = new WekaInstanceContainer();
    result.setContent(input.getValue(PostProcessingContainer.VALUE_OUTPUT_INSTANCE, Instance.class));
    if (input.hasValue(PostProcessingContainer.VALUE_REPORT))
      result.setReport(input.getValue(PostProcessingContainer.VALUE_REPORT, Report.class));

    return result;
  }
}
