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
 * ParafacLossHistory.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer.threewaydatatrain;

import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.flow.container.ThreeWayDataModelContainer;
import nz.ac.waikato.cms.adams.multiway.algorithm.PARAFAC;
import nz.ac.waikato.cms.adams.multiway.algorithm.api.AbstractAlgorithm;

import java.util.List;

/**
 * Turns the PARAFAC loss history into a spreadsheet.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ParafacLossHistory
  extends AbstractThreeWayDataTrainPostProcessor {

  private static final long serialVersionUID = 1343020510079098493L;

  /** the name in the container for the loss history. */
  public final static String KEY_LOSSHISTORY = "Loss-history";

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Turns the PARAFAC loss history into a spreadsheet and stores it "
      + "in the container under '" + KEY_LOSSHISTORY + "'.";
  }

  /**
   * Returns whether the algorithm can be handle.
   *
   * @param algorithm	the algorithm to check
   * @return		true if handled
   */
  @Override
  public boolean canHandle(AbstractAlgorithm algorithm) {
    return (algorithm instanceof PARAFAC);
  }

  /**
   * Post-processes the container.
   *
   * @param cont	the container to post-process
   * @return		null if successful, otherwise error message
   */
  @Override
  protected String doPostProcess(ThreeWayDataModelContainer cont) {
    String		result;
    PARAFAC		model;
    List<List<Double>>	lossHistory;
    SpreadSheet		sheet;
    Row			row;
    int			cols;
    int			i;

    result = null;
    model  = cont.getValue(ThreeWayDataModelContainer.VALUE_MODEL, PARAFAC.class);
    if (model != null) {
      lossHistory = model.getLossHistory();
      cols = 0;
      for (List<Double> item: lossHistory)
        cols = Math.max(cols, item.size());
      if (cols == 0)
        return "No loss history available!";

      // create spreadsheet
      sheet = new DefaultSpreadSheet();
      row   = sheet.getHeaderRow();
      row.addCell("H").setContentAsString("History");
      for (i = 0; i < cols; i++)
        row.addCell("L" + (i+1)).setContentAsString("Loss-" + (i+1));
      for (List<Double> item: lossHistory) {
        row = sheet.addRow();
        row.addCell("H").setContent(sheet.getRowCount());
        for (i = 0; i < item.size(); i++)
	  row.addCell("L" + (i+1)).setContent(item.get(i));
      }

      // update container
      cont.addAdditionalName(KEY_LOSSHISTORY);
      cont.setValue(KEY_LOSSHISTORY, sheet);
    }

    return result;
  }
}
