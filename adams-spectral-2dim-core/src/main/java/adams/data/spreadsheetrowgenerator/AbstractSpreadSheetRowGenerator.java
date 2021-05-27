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
 * AbstractSpreadSheetRowGenerator.java
 * Copyright (C) 2021 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spreadsheetrowgenerator;

import adams.core.CleanUpHandler;
import adams.core.ShallowCopySupporter;
import adams.core.option.AbstractOptionHandler;
import adams.core.option.OptionUtils;
import adams.data.spectrum.Spectrum;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;

/**
 * Abstract base class for schemes that turn spectra into spreadsheet row objects.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractSpreadSheetRowGenerator
  extends AbstractOptionHandler
  implements Comparable, CleanUpHandler, ShallowCopySupporter<AbstractSpreadSheetRowGenerator> {

  /** for serialization. */
  private static final long serialVersionUID = 5543015283566767256L;

  public static final String COL_DBID = "db_id";

  public static final String COL_SAMPLEID = "sample_id";

  /** the generated header. */
  protected SpreadSheet m_OutputHeader;

  /** whether to add the database ID. */
  protected boolean m_AddDatabaseID;

  /** whether to add the sample ID. */
  protected boolean m_AddSampleID;

  /** whether to tolerate header changes. */
  protected boolean m_TolerateHeaderChanges;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "add-db-id", "addDatabaseID",
      false);

    m_OptionManager.add(
      "add-sample-id", "addSampleID",
      false);

    m_OptionManager.add(
      "tolerate-header-changes", "tolerateHeaderChanges",
      false);
  }

  /**
   * Resets the generator (but does not clear the input data!).
   * Derived classes must call this method in set-methods of parameters to
   * assure the invalidation of previously generated data.
   */
  @Override
  protected void reset() {
    super.reset();

    m_OutputHeader = null;
  }

  /**
   * Cleans up data structures, frees up memory.
   * Sets the input data to null.
   */
  public void cleanUp() {
    reset();
  }

  /**
   * Frees up memory in a "destructive" non-reversible way.
   * <br><br>
   * Calls cleanUp() and cleans up the options.
   */
  @Override
  public void destroy() {
    cleanUp();
    super.destroy();
  }

  /**
   * Sets whether the database ID is added to the data or not.
   *
   * @param value 	true if database ID should be added
   */
  public void setAddDatabaseID(boolean value) {
    m_AddDatabaseID = value;
    reset();
  }

  /**
   * Returns whether the database ID is added.
   *
   * @return 		true if database ID is added
   */
  public boolean getAddDatabaseID() {
    return m_AddDatabaseID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String addDatabaseIDTipText() {
    return "If set to true, then the database ID will be added to the output.";
  }

  /**
   * Sets whether the sample ID is added to the data or not.
   *
   * @param value 	true if sample ID should be added
   */
  public void setAddSampleID(boolean value) {
    m_AddSampleID = value;
    reset();
  }

  /**
   * Returns whether the sample ID is added.
   *
   * @return 		true if sample ID is added
   */
  public boolean getAddSampleID() {
    return m_AddSampleID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String addSampleIDTipText() {
    return "If set to true, then the sample ID will be added to the output.";
  }

  /**
   * Sets whether to tolerate header changes and merely re-generating the 
   * header instead of throwing an exception.
   *
   * @param value 	true if to tolerate header changes
   */
  public void setTolerateHeaderChanges(boolean value) {
    m_TolerateHeaderChanges = value;
    reset();
  }

  /**
   * Returns whether to tolerate header changes and merely re-generating the 
   * header instead of throwing an exception.
   *
   * @return 		true if to tolerate header changes
   */
  public boolean getTolerateHeaderChanges() {
    return m_TolerateHeaderChanges;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String tolerateHeaderChangesTipText() {
    return 
	"If set to true, then changes in the header get tolerated (and the "
	+ "header recreated) instead of causing an error.";
  }

  /**
   * Returns the current header.
   *
   * @return		the header, can be null
   */
  public SpreadSheet getOutputHeader() {
    return m_OutputHeader;
  }

  /**
   * Checks whether the setup is consistent.
   * <br><br>
   * Default implementation does nothing.
   *
   * @return		null if everything OK, otherwise the error message
   */
  public String checkSetup() {
    return null;
  }

  /**
   * Returns the generated data, generates it if necessary.
   *
   * @param data	the spectrum to turn into a row
   * @return		the generated data
   */
  public Row generate(Spectrum data) {
    Row	result;

    // input/profile
    checkInput(data);

    // header/instances
    if (m_OutputHeader == null) {
      generateHeader(data);
      postProcessHeader(data);
    }

    // check header
    try {
      checkHeader(data);
    }
    catch (Exception e) {
      if (m_TolerateHeaderChanges) {
	generateHeader(data);
	postProcessHeader(data);
      }
      else {
	throw new IllegalStateException(e);
      }
    }

    // output/instance
    result = generateOutput(data);
    result = postProcessOutput(data, result);

    return result;
  }

  /**
   * Checks the input spectrum.
   * <br><br>
   * The default implementation only checks whether there is any data set.
   *
   * @param data	the data to process
   */
  protected void checkInput(Spectrum data) {
    if (data == null)
      throw new IllegalStateException("No input data provided!");
  }

  /**
   * Checks whether the number of waves are the same.
   *
   * @param data	the input data
   */
  protected abstract void checkHeader(Spectrum data);

  /**
   * Generates the header of the output data.
   *
   * @param data	the input data
   */
  protected abstract void generateHeader(Spectrum data);

  /**
   * Interpretes the position string based on the given dataset.
   * "first", "second", "third", "last-2", "last-1" and "last" and "last+1" are valid as well.
   *
   * @param data	the data to base the intepretation on
   * @param position	the position string
   * @return		the numeric position string
   */
  protected String interpretePosition(SpreadSheet data, String position) {
    if (position.equals("first"))
      return "1";
    else if (position.equals("second"))
      return "2";
    else if (position.equals("third"))
      return "3";
    else if (position.equals("last-2"))
      return "" + (data.getColumnCount() - 2);
    else if (position.equals("last-1"))
      return "" + (data.getColumnCount() - 1);
    else if (position.equals("last"))
      return "" + data.getColumnCount();
    else if (position.equals("last+1"))
      return "" + (data.getColumnCount()+1);
    else
      return position;
  }

  /**
   * Adds IDs, notes, additional fields to header.
   *
   * @param data	the input data
   */
  protected void postProcessHeader(Spectrum data) {
    if (m_AddSampleID)
      m_OutputHeader.insertColumn(0, COL_SAMPLEID);
    if (m_AddDatabaseID)
      m_OutputHeader.insertColumn(0, COL_DBID);
  }

  /**
   * Generates the actual data.
   *
   * @param data	the input data to transform
   * @return		the generated data
   */
  protected abstract Row generateOutput(Spectrum data);

  /**
   * For adding IDs, notes, additional fields to the data.
   *
   * @param data	the input data
   * @param row		the generated row
   * @return		the processed row
   */
  protected Row postProcessOutput(Spectrum data, Row row) {
    int		index;

    if (m_AddSampleID) {
      index = m_OutputHeader.getHeaderRow().indexOfContent(COL_SAMPLEID);
      row.addCell(index).setContent(data.getID());
    }

    if (m_AddDatabaseID) {
      index = m_OutputHeader.getHeaderRow().indexOfContent(COL_DBID);
      row.addCell(index).setContent(data.getDatabaseID());
    }

    return row;
  }

  /**
   * Compares this object with the specified object for order.  Returns a
   * negative integer, zero, or a positive integer as this object is less
   * than, equal to, or greater than the specified object.
   * <br><br>
   * Only compares the commandlines of the two objects.
   *
   * @param o 	the object to be compared.
   * @return  	a negative integer, zero, or a positive integer as this object
   *		is less than, equal to, or greater than the specified object.
   *
   * @throws ClassCastException 	if the specified object's type prevents it
   *         				from being compared to this object.
   */
  public int compareTo(Object o) {
    if (o == null)
      return 1;

    return OptionUtils.getCommandLine(this).compareTo(OptionUtils.getCommandLine(o));
  }

  /**
   * Returns whether the two objects are the same.
   * <br><br>
   * Only compares the commandlines of the two objects.
   *
   * @param o	the object to be compared
   * @return	true if the object is the same as this one
   */
  @Override
  public boolean equals(Object o) {
    return (compareTo(o) == 0);
  }

  /**
   * Returns a shallow copy of itself, i.e., based on the commandline options.
   *
   * @return		the shallow copy
   */
  public AbstractSpreadSheetRowGenerator shallowCopy() {
    return shallowCopy(false);
  }

  /**
   * Returns a shallow copy of itself, i.e., based on the commandline options.
   *
   * @param expand	whether to expand variables to their current values
   * @return		the shallow copy
   */
  public AbstractSpreadSheetRowGenerator shallowCopy(boolean expand) {
    return (AbstractSpreadSheetRowGenerator) OptionUtils.shallowCopy(this, expand);
  }
}
