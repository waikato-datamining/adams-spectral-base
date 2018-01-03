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
 * SelectSpectrumPanel.java
 * Copyright (C) 2009-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.selection;

import adams.db.AbstractConditions;
import adams.db.AbstractDatabaseConnection;
import adams.db.AbstractSpectrumConditions;
import adams.db.Conditions;
import adams.db.DatabaseConnection;
import adams.db.SampleDataT;
import adams.db.SpectrumConditionsMulti;
import adams.db.SpectrumIDConditions;
import adams.db.SpectrumT;
import adams.gui.core.ClearableModel;
import adams.gui.core.CustomSearchTableModel;
import adams.gui.core.SearchParameters;

import java.util.List;

/**
 * A panel that lists all the spectrums currently in the database.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1556 $
 */
public class SelectSpectrumPanel
  extends AbstractConditionalDatabaseSelectionPanel<Integer,AbstractConditions> {

  /** for serialization. */
  private static final long serialVersionUID = 4706113852954159879L;

  /**
   * A simple table model for displaying the database IDs and Names.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 1556 $
   */
  public static class TableModel
    extends AbstractSelectionTableModel<Integer>
    implements ClearableModel, CustomSearchTableModel {

    /** for serialization. */
    private static final long serialVersionUID = 2776199413402687115L;

    /** the IDs to display. */
    protected Integer[] m_IDs;

    /** the NAMEs to display. */
    protected String[] m_SampleID;

    /** the FORMATs to display. */
    protected String[] m_Format;

    /**
     * default constructor.
     */
    public TableModel() {
      this(new String[0]);
    }

    /**
     * the constructor.
     *
     * @param values	the IDs/Names/Instruments to display
     */
    public TableModel(List<String> values) {
      this(values.toArray(new String[values.size()]));
    }

    /**
     * the constructor.
     *
     * @param values	the IDs/Names/Instruments to display
     */
    public TableModel(String[] values) {
      m_IDs      = new Integer[values.length];
      m_SampleID = new String[values.length];
      m_Format   = new String[values.length];
      for (int i = 0; i < values.length; i++) {
	String[] parts = values[i].split("\t");
	m_IDs[i]      = Integer.parseInt(parts[0]);
	m_SampleID[i] = parts[1];
	m_Format[i]   = parts[2];
      }
    }

    /**
     * Returns the actual number of entries in the table.
     *
     * @return		the number of entries
     */
    public int getRowCount() {
      return m_IDs.length;
    }

    /**
     * Returns the number of columns in the table, i.e., 3.
     *
     * @return		the number of columns, always 3
     */
    public int getColumnCount() {
      return 3;
    }

    /**
     * Returns the name of the column.
     *
     * @param column 	the column to get the name for
     * @return		the name of the column
     */
    public String getColumnName(int column) {
      if (column == 0)
	return "Database ID";
      else if (column == 1)
	return "Sample ID";
      else if (column == 2)
	return "Format";
      else
	throw new IllegalArgumentException("Column " + column + " is invalid!");
    }

    /**
     * Returns the class type of the column.
     *
     * @param columnIndex	the column to get the class for
     * @return			the class for the column
     */
    public Class getColumnClass(int columnIndex) {
      if (columnIndex == 0)
	return Integer.class;
      else if (columnIndex == 1)
	return String.class;
      else if (columnIndex == 2)
	return String.class;
      else
	throw new IllegalArgumentException("Column " + columnIndex + " is invalid!");
    }

    /**
     * Returns the ID at the given position.
     *
     * @param row	the row
     * @param column	the column
     * @return		the ID
     */
    public Object getValueAt(int row, int column) {
      if (column == 0)
	return m_IDs[row];
      else if (column == 1)
	return m_SampleID[row];
      else if (column == 2)
	return m_Format[row];
      else
	throw new IllegalArgumentException("Column " + column + " is invalid!");
    }

    /**
     * Returns the ID at the specified position.
     *
     * @param row	the (actual, not visible) position of the ID
     * @return		the ID at the position, null if not valid index
     */
    public Integer getItemAt(int row) {
      if ((row >= 0) && (row < m_IDs.length))
	return m_IDs[row];
      else
	return null;
    }

    /**
     * Returns the index of the given (visible) ID, -1 if not found.
     *
     * @param id	the ID to look for
     * @return		the index, -1 if not found
     */
    public int indexOf(Integer id) {
      int	result;
      int	i;

      result = -1;

      if (id != null) {
	for (i = 0; i < m_IDs.length; i++) {
	  if (id.equals(m_IDs[i])) {
	    result = i;
	    break;
	  }
	}
      }

      return result;
    }

    /**
     * Tests whether the search matches the specified row.
     *
     * @param params	the search parameters
     * @param row	the row of the underlying, unsorted model
     * @return		true if the search matches this row
     */
    public boolean isSearchMatch(SearchParameters params, int row) {
      // ID columns
      if (params.getLong()) {
	// ID
	if (params.matches(m_IDs[row]))
	  return true;
      }

      // sample ID
      if (params.matches(m_SampleID[row]))
	return true;
      // format
      if (params.matches(m_Format[row]))
	return true;

      return false;
    }

    /**
     * Clears the internal model.
     */
    public void clear() {
      m_IDs      = new Integer[0];
      m_SampleID = new String[0];
      m_Format   = new String[0];

      fireTableDataChanged();
    }
  }

  /**
   * initializes the GUI elements.
   */
  protected void initGUI() {
    super.initGUI();

    setDefaultPopupMenuSupplier();
    setCountsVisible(true);
  }

  /**
   * Returns the default database connection.
   *
   * @return		the default connection
   */
  protected AbstractDatabaseConnection getDefaultDatabaseConnection() {
    return DatabaseConnection.getSingleton();
  }

  /**
   * Returns an empty table model.
   *
   * @return		the model
   */
  protected TableModel newTableModel() {
    return new TableModel();
  }

  /**
   * Returns the class of the items displayed, same as "T".
   *
   * @return		the class of the items
   */
  protected Class getItemClass() {
    return Integer.class;
  }

  /**
   * Returns the default conditions to use.
   *
   * @return		the conditions
   */
  protected AbstractConditions getDefaultConditions() {
    return Conditions.getSingleton().getDefault(new SpectrumConditionsMulti());
  }

  /**
   * Returns the approved conditions that can be used.
   *
   * @return		the approved conditions
   */
  protected Class[] getApprovedConditions() {
    return new Class[]{SpectrumIDConditions.class, AbstractSpectrumConditions.class};
  }

  /**
   * Updates the options object used internally and refreshes the display if
   * necessary.
   */
  protected void updateOptions() {
    m_Conditions = getDefaultConditions();

    if (m_DataDisplayed)
      refresh();
  }

  /**
   * Returns the correct table object, depending whether active or store table
   * is used.
   *
   * @return		the table object
   */
  protected SpectrumT getSpectrumTable() {
    return SpectrumT.getSingleton(getDatabaseConnection());
  }

  /**
   * Returns the correct table object, depending whether active or store table
   * is used.
   *
   * @return		the table object
   */
  protected SampleDataT getSampleDataTable() {
    return SampleDataT.getSingleton(getDatabaseConnection());
  }

  /**
   * Does a refresh if data hasn't been displayed.
   *
   * @see	#refresh()
   */
  public void refreshIfNecessary() {
    if (!m_DataDisplayed)
      refresh();
  }

  /**
   * Performs the actual refresh.
   */
  protected void doRefresh() {
    List<String> 	ids;

    ids = getSampleDataTable().getIDs(
      new String[]{"sp.AUTO_ID", "sp.SAMPLEID", "sp.FORMAT"}, m_Conditions);
    m_TableDataModel.removeTableModelListener(m_TableData);
    m_TableDataModel = new TableModel(ids);
    m_TableDataModel.addTableModelListener(m_TableData);
    m_TableData.setModel(m_TableDataModel);
  }

  /**
   * Gets called when the database connection gets disconnected.
   */
  protected void databaseDisconnected() {
    ((TableModel) m_TableDataModel).clear();
  }
}
