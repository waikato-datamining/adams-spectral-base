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
 * SpectrumTableModel.java
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package adams.gui.visualization.spectrum;

import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.gui.core.AbstractBaseTableModel;

/**
 * Table model for displaying a spectrum in a table.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public class SpectrumTableModel
  extends AbstractBaseTableModel {

  /** for serialization. */
  private static final long serialVersionUID = 8065207320821471943L;
  
  /** the underlying spectrum. */
  protected Spectrum m_Data;
  
  /**
   * Initializes the model with no spectrum.
   */
  public SpectrumTableModel() {
    this(null);
  }
  
  /**
   * Initializes the model with the specified spectrum.
   * 
   * @param data	the spectrum to display
   */
  public SpectrumTableModel(Spectrum data) {
    super();
    m_Data = data;
  }

  /**
   * Returns the number of rows in the model.
   *
   * @return 		the number of rows in the model
   */
  public int getRowCount() {
    if (m_Data == null)
      return 0;
    else
      return m_Data.size();
  }

  /**
   * Returns the number of columns in the model.
   *
   * @return 		the number of columns in the model
   */
  public int getColumnCount() {
    return 3;
  }

  /**
   *  Returns the name of the column.
   *
   * @param column	the column being queried
   * @return 		a string containing the default name of <code>column</code>
   */
  public String getColumnName(int column) {
    if (column == 0)
      return "Index";
    else if (column == 1)
      return "Waveno.";
    else if (column == 2)
      return "Amplitude";
    else
      throw new IllegalArgumentException("Invalid column: " + column);
  }

  /**
   * Returns the most specific superclass for all the cell values 
   * in the column.  This is used by the <code>JTable</code> to set up a 
   * default renderer and editor for the column.
   *
   * @param columnIndex  the index of the column
   * @return the common ancestor class of the object values in the model.
   */
  public Class getColumnClass(int columnIndex) {
    if (columnIndex == 0)
      return Integer.class;
    else if (columnIndex == 1)
      return Float.class;
    else if (columnIndex == 2)
      return Float.class;
    else
      throw new IllegalArgumentException("Invalid column: " + columnIndex);
  }

  /**
   * Returns the value for the cell at <code>columnIndex</code> and
   * <code>rowIndex</code>.
   *
   * @param	rowIndex	the row whose value is to be queried
   * @param	columnIndex 	the column whose value is to be queried
   * @return	the value Object at the specified cell
   */
  public Object getValueAt(int rowIndex, int columnIndex) {
    SpectrumPoint point;
    
    if (m_Data == null)
      throw new IllegalArgumentException("No spectrum available!");
    
    point = m_Data.toList().get(rowIndex);
    if (columnIndex == 0)
      return (rowIndex + 1);
    else if (columnIndex == 1)
      return point.getWaveNumber();
    else if (columnIndex == 2)
      return point.getAmplitude();
    else
      throw new IllegalArgumentException("Invalid column: " + columnIndex);
  }
  
  /**
   * Returns the underlying spectrum.
   * 
   * @return		the spectrum, null if none available
   */
  public Spectrum getData() {
    return m_Data;
  }
}
