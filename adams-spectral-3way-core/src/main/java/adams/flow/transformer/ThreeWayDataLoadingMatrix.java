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
 * ThreeWayDataLoadingMatrix.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer;

import adams.core.QuickInfoHelper;
import adams.core.Utils;
import adams.flow.container.ThreeWayDataModelContainer;
import adams.flow.core.Token;
import nz.ac.waikato.cms.adams.multiway.algorithm.api.LoadingMatrixAccessor;
import nz.ac.waikato.cms.adams.multiway.data.tensor.Tensor;

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
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ThreeWayDataLoadingMatrix
  extends AbstractTransformer {

  private static final long serialVersionUID = 413455226633735153L;

  /** the name of the matrix to retrieve. */
  protected String m_Matrix;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Retrieves the specified loading matrix from the model.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "matrix", "matrix",
      "");
  }

  /**
   * Sets the name of the loading matrix to retrieve.
   *
   * @param value	the name
   */
  public void setMatrix(String value) {
    m_Matrix = value;
    reset();
  }

  /**
   * Returns the name of the loading matrix to retrieve.
   *
   * @return		the name
   */
  public String getMatrix() {
    return m_Matrix;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String matrixTipText() {
    return "The name of the loading matrix to retrieve.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    return QuickInfoHelper.toString(this, "matrix", m_Matrix, "matrix: ");
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the Class of objects that can be processed
   */
  @Override
  public Class[] accepts() {
    return new Class[]{ThreeWayDataModelContainer.class, LoadingMatrixAccessor.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the Class of the generated tokens
   */
  @Override
  public Class[] generates() {
    return new Class[]{Tensor.class};
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String			result;
    ThreeWayDataModelContainer	cont;
    LoadingMatrixAccessor	model;
    Tensor			matrix;

    result = null;

    model = null;
    if (m_InputToken.hasPayload(ThreeWayDataModelContainer.class)) {
      cont = m_InputToken.getPayload(ThreeWayDataModelContainer.class);
      if (cont.getValue(ThreeWayDataModelContainer.VALUE_MODEL) instanceof LoadingMatrixAccessor)
        model = cont.getValue(ThreeWayDataModelContainer.VALUE_MODEL, LoadingMatrixAccessor.class);
      else
        result = "Model is not a " + Utils.classToString(LoadingMatrixAccessor.class) + ": " + Utils.classToString(cont.getValue(ThreeWayDataModelContainer.VALUE_MODEL));
    }
    else if (m_InputToken.hasPayload(LoadingMatrixAccessor.class)) {
      model = m_InputToken.getPayload(LoadingMatrixAccessor.class);
    }
    else {
      result = m_InputToken.unhandledData();
    }

    if (result == null) {
      if (model != null) {
	matrix = model.getLoadingMatrices().get(m_Matrix);
	if (matrix != null)
	  m_OutputToken = new Token(matrix);
	else
	  result = "Loading matrix not present: " + m_Matrix;
      }
      else {
        result = "No model available?";
      }
    }

    return result;
  }
}
