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
 * TableModel.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.gui.tools.idprovider;

import adams.gui.core.CheckableTableModel;
import adams.gui.selection.SelectSpectrumPanel;

import java.util.ArrayList;
import java.util.List;

/**
 * Table model for displaying the database IDs, IDs, formats and selected
 * state of spectra.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class TableModel
  extends CheckableTableModel<SelectSpectrumPanel.TableModel> {

  /** for serialization. */
  private static final long serialVersionUID = 2776199413402687115L;

  /**
   * default constructor.
   */
  public TableModel() {
    this(new SelectSpectrumPanel.TableModel());
  }

  /**
   * the constructor.
   *
   * @param model	model to display
   */
  public TableModel(SelectSpectrumPanel.TableModel model) {
    super(model, "Update");
  }

  /**
   * Returns the selected items (sample IDs).
   *
   * @return		the selected items
   */
  public String[] getSelectedSampleIDs() {
    List<String> result;
    int		i;

    result = new ArrayList<>();

    for (i = 0; i < getRowCount(); i++) {
      if (getCheckedAt(i))
	result.add("" + getModel().getValueAt(i, 1));
    }

    return result.toArray(new String[result.size()]);
  }

  /**
   * Returns the sample ID at the specified location.
   *
   * @param row	the (actual, not visible) position of the spectrum
   * @return		the sample ID, null if failed to retrieve
   */
  public String getSampleIdAt(int row) {
    if ((row >= 0) && (row < getRowCount()))
      return "" + getModel().getValueAt(row, 1);
    else
      return null;
  }
}
