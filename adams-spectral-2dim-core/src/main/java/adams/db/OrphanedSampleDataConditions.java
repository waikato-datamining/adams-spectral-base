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
 * OrphanedSampleDataConditions.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.db;

import adams.core.base.BaseDateTime;

/**
 * Allows the retrieval of sample IDs of orphaned sample data, ie no spectrum available.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class OrphanedSampleDataConditions
  extends AbstractLimitedConditions {

  private static final long serialVersionUID = -8985118865834719208L;

  /** the start date of the inserts (incl.). */
  protected BaseDateTime m_StartDate;

  /** the end date of the inserts (incl.). */
  protected BaseDateTime m_EndDate;

  /** whether to get latest (= reverse order). */
  protected boolean m_Latest;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Allows the retrieval of sample IDs of orphaned sample data, ie no spectrum available.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "start", "startDate",
      BaseDateTime.infinityPast());

    m_OptionManager.add(
      "end", "endDate",
      BaseDateTime.infinityFuture());

    m_OptionManager.add(
      "latest", "latest",
      false);
  }

  /**
   * Sets the start date.
   *
   * @param value 	the start date
   */
  public void setStartDate(BaseDateTime value) {
    m_StartDate = value;
    reset();
  }

  /**
   * Returns the start date.
   *
   * @return 		the start date
   */
  public BaseDateTime getStartDate() {
    return m_StartDate;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String startDateTipText() {
    return "The start date for the sample data (yyyy-MM-dd HH:mm:ss).";
  }

  /**
   * Sets the end date.
   *
   * @param value 	the end date
   */
  public void setEndDate(BaseDateTime value) {
    m_EndDate = value;
    reset();
  }

  /**
   * Returns the end date.
   *
   * @return 		the end date
   */
  public BaseDateTime getEndDate() {
    return m_EndDate;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String endDateTipText() {
    return "The end date for the sample data (yyyy-MM-dd HH:mm:ss).";
  }

  /**
   * Sets whether to get only the latest ones (= reverse order).
   *
   * @param value 	if true then the latest ones are returned
   */
  public void setLatest(boolean value) {
    m_Latest = value;
    reset();
  }

  /**
   * Returns whether only the latest ones are returned (= reverse order).
   *
   * @return 		true if only latest ones are returned
   */
  public boolean getLatest() {
    return m_Latest;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String latestTipText() {
    return "If set to true, order is reversed and only latest ones are returned.";
  }

  /**
   * Automatically corrects values, but does not throw any exceptions.
   */
  @Override
  public void update() {
    // nothing to do
  }
}
