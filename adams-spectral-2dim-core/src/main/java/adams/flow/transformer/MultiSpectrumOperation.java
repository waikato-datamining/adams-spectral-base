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
 * MultiSpectrumOperation.java
 * Copyright (C) 2025 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.MessageCollection;
import adams.core.QuickInfoHelper;
import adams.data.multispectrumoperation.AbstractMultiSpectrumOperation;
import adams.data.multispectrumoperation.PassThrough;
import adams.data.spectrum.MultiSpectrum;
import adams.flow.core.Token;

/**
 <!-- globalinfo-start -->
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class MultiSpectrumOperation
  extends AbstractTransformer {

  /** for serialization. */
  private static final long serialVersionUID = -8678582872628608282L;

  /** the operation to apply. */
  protected AbstractMultiSpectrumOperation m_Operation;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Applies the specified operation to the multi-spectrum passing through.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "operation", "operation",
      new PassThrough());
  }

  /**
   * Sets the multi-spectrum operation to apply.
   *
   * @param value	the operation
   */
  public void setOperation(AbstractMultiSpectrumOperation value) {
    m_Operation = value;
    reset();
  }

  /**
   * Returns the multi-spectrum operation to apply.
   *
   * @return 		the operation
   */
  public AbstractMultiSpectrumOperation getOperation() {
    return m_Operation;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String operationTipText() {
    return "The multi-spectrum operation to apply.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    return QuickInfoHelper.toString(this, "operation", m_Operation, "operation: ");
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the default DataContainer class for the project
   */
  public Class[] accepts() {
    return new Class[]{MultiSpectrum.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the default DataContainer class for the project
   */
  public Class[] generates() {
    return new Class[]{MultiSpectrum.class};
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String		result;
    MultiSpectrum	multi;
    MultiSpectrum 	processed;
    MessageCollection	errors;

    result    = null;
    multi     = (MultiSpectrum) m_InputToken.getPayload();
    errors    = new MessageCollection();
    processed = m_Operation.apply(multi, errors);
    if (errors.isEmpty() && (processed != null))
      m_OutputToken = new Token(processed);
    else if (!errors.isEmpty())
      result = errors.toString();

    return result;
  }
}
