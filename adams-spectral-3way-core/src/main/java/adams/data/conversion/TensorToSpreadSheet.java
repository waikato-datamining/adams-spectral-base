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
 * TensorToSpreadSheet.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.conversion;

import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import nz.ac.waikato.cms.adams.multiway.data.tensor.Tensor;

/**
 <!-- globalinfo-start -->
 * Turns a Tensor data structure into a spreadsheet.<br>
 * - 1D: single row<br>
 * - 2D: matrix<br>
 * - 3D: three index columns (X,Y,Z) and data column
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
public class TensorToSpreadSheet
  extends AbstractConversion {

  private static final long serialVersionUID = 2147610596038601776L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Turns a Tensor data structure into a spreadsheet.\n"
      + "- 1D: single row\n"
      + "- 2D: matrix\n"
      + "- 3D: three index columns (X,Y,Z) and data column";
  }

  /**
   * Returns the class that is accepted as input.
   *
   * @return		the class
   */
  @Override
  public Class accepts() {
    return Tensor.class;
  }

  /**
   * Returns the class that is generated as output.
   *
   * @return		the class
   */
  @Override
  public Class generates() {
    return SpreadSheet.class;
  }

  /**
   * Converts the 1-D tensor data.
   *
   * @param data	the tensor data
   * @return		the generated spreadsheet (single row)
   */
  protected SpreadSheet convert(double[] data) {
    SpreadSheet		result;
    Row			row;
    int 		x;

    result = new DefaultSpreadSheet();
    row    = result.getHeaderRow();
    for (x = 0; x < data.length; x++)
      row.addCell("" + x).setContentAsString("col-" + (x +1));
    row = result.addRow();
    for (x = 0; x < data.length; x++)
      row.addCell("" + x).setContent(data[x]);

    return result;
  }

  /**
   * Converts the 2-D tensor data.
   *
   * @param data	the tensor data
   * @return		the generated spreadsheet (matrix)
   */
  protected SpreadSheet convert(double[][] data) {
    SpreadSheet		result;
    Row			row;
    int 		x;
    int 		y;

    result = new DefaultSpreadSheet();
    row    = result.getHeaderRow();
    if (data.length > 0) {
      for (x = 0; x < data[0].length; x++)
	row.addCell("" + x).setContentAsString("col-" + (x + 1));
      for (y = 0; y < data.length; y++) {
	row = result.addRow();
	for (x = 0; x < data[y].length; x++)
	  row.addCell("" + x).setContent(data[y][x]);
      }
    }

    return result;
  }

  /**
   * Converts the 3-D tensor data.
   *
   * @param data	the tensor data
   * @return		the generated spreadsheet (three index cols (XYZ) and a data col)
   */
  protected SpreadSheet convert(double[][][] data) {
    SpreadSheet		result;
    Row			row;
    int 		x;
    int 		y;
    int 		z;

    result = new DefaultSpreadSheet();
    row    = result.getHeaderRow();
    row.addCell("X").setContentAsString("X");
    row.addCell("Y").setContentAsString("Y");
    row.addCell("Z").setContentAsString("Z");
    row.addCell("D").setContentAsString("Data");
    for (x = 0; x < data.length; x++) {
      for (y = 0; y < data[x].length; y++) {
	for (z = 0; z < data[x][y].length; x++) {
	  row = result.addRow();
	  row.addCell("X").setContent(x);
	  row.addCell("Y").setContent(y);
	  row.addCell("Z").setContent(z);
	  row.addCell("D").setContent(data[x][y][z]);
	}
      }
    }

    return result;
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  @Override
  protected Object doConvert() throws Exception {
    SpreadSheet		result;
    Tensor		tensor;

    tensor = (Tensor) m_Input;
    switch (tensor.order()) {
      case 1:
        result = convert(tensor.toArray1d());
        break;
      case 2:
        result = convert(tensor.toArray2d());
        break;
      case 3:
        result = convert(tensor.toArray3d());
        break;
      default:
        throw new IllegalStateException("Unhandled tensor order: " + tensor.order());
    }

    return result;
  }
}
