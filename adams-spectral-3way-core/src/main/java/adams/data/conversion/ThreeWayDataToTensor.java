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
 * ThreeWayDataToTensor.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.conversion;

import adams.data.container.TensorContainer;
import adams.data.threeway.L1Point;
import adams.data.threeway.L2Point;
import adams.data.threeway.ThreeWayData;
import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.set.TDoubleSet;
import gnu.trove.set.hash.TDoubleHashSet;
import nz.ac.waikato.cms.adams.multiway.data.tensor.Tensor;

/**
 <!-- globalinfo-start -->
 * Converts 3-way data into the Tensor data structure.
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
public class ThreeWayDataToTensor
  extends AbstractConversion {

  private static final long serialVersionUID = -2098679748781880163L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Converts 3-way data into the Tensor data structure.";
  }

  /**
   * Returns the class that is accepted as input.
   *
   * @return		the class
   */
  @Override
  public Class accepts() {
    return ThreeWayData.class;
  }

  /**
   * Returns the class that is generated as output.
   *
   * @return		the class
   */
  @Override
  public Class generates() {
    return TensorContainer.class;
  }

  /**
   * Performs the actual conversion.
   *
   * @return		the converted data
   * @throws Exception	if something goes wrong with the conversion
   */
  @Override
  protected Object doConvert() throws Exception {
    TensorContainer	result;
    ThreeWayData	data;
    TDoubleSet 		setX;
    TDoubleSet 		setY;
    TDoubleSet 		setZ;
    TDoubleList		listX;
    TDoubleList		listY;
    TDoubleList		listZ;
    double[][][]	tdata;
    int			x;
    int			y;
    int			z;

    data = (ThreeWayData) m_Input;
    setX = new TDoubleHashSet();
    setY = new TDoubleHashSet();
    setZ = new TDoubleHashSet();
    for (L1Point l1: data) {
      setX.add(l1.getX());
      setY.add(l1.getY());
      for (L2Point l2: l1)
        setZ.add(l2.getZ());
    }

    listX = new TDoubleArrayList(setX);
    listX.sort();
    listY = new TDoubleArrayList(setY);
    listY.sort();
    listZ = new TDoubleArrayList(setZ);
    listZ.sort();

    tdata = new double[listX.size()][listY.size()][listZ.size()];
    for (L1Point l1: data) {
      x = listX.indexOf(l1.getX());
      y = listY.indexOf(l1.getY());
      for (L2Point l2: l1) {
        z              = listZ.indexOf(l2.getZ());
        tdata[x][y][z] = l2.getData();
      }
    }
    result = new TensorContainer();
    result.setReport(data.getReport().getClone());
    result.getNotes().addProcessInformation(this);
    result.setContent(Tensor.create(tdata));

    return result;
  }
}
