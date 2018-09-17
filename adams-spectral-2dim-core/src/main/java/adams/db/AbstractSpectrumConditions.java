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
 * AbstractSpectrumConditions.java
 * Copyright (C) 2010-2018 University of Waikato, Hamilton, New Zealand
 */

package adams.db;

import adams.core.base.BaseDateTime;
import adams.core.base.BaseRegExp;
import adams.data.sampledata.SampleData;

/**
 * Ancestor for spectrum conditions.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractSpectrumConditions
  extends AbstractLimitedConditions
  implements ReportConditions, SampleTypeRegExpSupporter {

  /** for serialization. */
  private static final long serialVersionUID = 8972337431072505207L;

  /** the regular expression on the name of the instrument. */
  protected BaseRegExp m_Instrument;

  /** the regular expression on the name of the spectrum. */
  protected BaseRegExp m_SampleIDRegExp;

  /** the regular expression on the sample type of the spectrum. */
  protected BaseRegExp m_SampleTypeRegExp;

  /** the regular expression on the format type of the spectrum. */
  protected BaseRegExp m_Format;

  /** the start date of the spectra (incl.). */
  protected BaseDateTime m_StartDate;

  /** the end date of the spectra (incl.). */
  protected BaseDateTime m_EndDate;

  /** whether to exclude spectra with dummy reports. */
  protected boolean m_ExcludeDummies;

  /** whether to only include spectra with dummy reports. */
  protected boolean m_OnlyDummies;

  /** whether to get latest (= reverse order). */
  protected boolean m_Latest;

  /** whether to sort by insert timestamp. */
  protected boolean m_SortOnInsertTimestamp;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "instrument", "instrument",
	    new BaseRegExp(""));

    m_OptionManager.add(
	    "sampleid", "sampleIDRegExp",
	    new BaseRegExp(""));

    m_OptionManager.add(
	    "format", "format",
	    new BaseRegExp(""));

    m_OptionManager.add(
	    "type", "sampleTypeRegExp",
	    new BaseRegExp(""));

    m_OptionManager.add(
	    "start", "startDate",
	    BaseDateTime.infinityPast());

    m_OptionManager.add(
	    "end", "endDate",
	    BaseDateTime.infinityFuture());

    m_OptionManager.add(
	    "no-dummies", "excludeDummies",
	    false);

    m_OptionManager.add(
	    "only-dummies", "onlyDummies",
	    false);

    m_OptionManager.add(
	    "latest", "latest",
	    false);

    m_OptionManager.add(
	    "sort-on-insert-timestamp", "sortOnInsertTimestamp",
	    false);
  }

  /**
   * Sets the regular expression for the instrument name.
   *
   * @param value 	the regular expression
   */
  public void setInstrument(BaseRegExp value) {
    m_Instrument = value;
    reset();
  }

  /**
   * Returns the regular expression for the instrument name.
   *
   * @return 		the regular expression
   */
  public BaseRegExp getInstrument() {
    return m_Instrument;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String instrumentTipText() {
    return "The regular expression on the instrument.";
  }

  /**
   * Sets the regular expression for the spectrum name.
   *
   * @param value 	the regular expression
   */
  public void setSampleIDRegExp(BaseRegExp value) {
    m_SampleIDRegExp = value;
    reset();
  }

  /**
   * Returns the regular expression for the spectrum name.
   *
   * @return 		the regular expression
   */
  public BaseRegExp getSampleIDRegExp() {
    return m_SampleIDRegExp;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sampleIDRegExpTipText() {
    return "The regular expression on the spectrum ID.";
  }

  /**
   * Sets the regular expression for the format.
   *
   * @param value 	the regular expression
   */
  public void setFormat(BaseRegExp value) {
    m_Format = value;
    reset();
  }

  /**
   * Returns the regular expression for the format.
   *
   * @return 		the regular expression
   */
  public BaseRegExp getFormat() {
    return m_Format;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String formatTipText() {
    return "The regular expression on the data format.";
  }

  /**
   * Sets the regular expression for the sample type.
   *
   * @param value 	the regular expression
   */
  public void setSampleTypeRegExp(BaseRegExp value) {
    m_SampleTypeRegExp = value;
    reset();
  }

  /**
   * Returns the regular expression for the sample type.
   *
   * @return 		the regular expression
   */
  public BaseRegExp getSampleTypeRegExp() {
    return m_SampleTypeRegExp;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sampleTypeRegExpTipText() {
    return "The regular expression on the sample type.";
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
    return "The start date for the spectra (yyyy-MM-dd HH:mm:ss).";
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
    return "The end date for the spectra (yyyy-MM-dd HH:mm:ss).";
  }

  /**
   * Sets whether to exclude spectra with reports flagged as dummies or not.
   *
   * @param value 	if true then dummies are excluded
   */
  public void setExcludeDummies(boolean value) {
    m_ExcludeDummies = value;
    reset();
  }

  /**
   * Returns whether spectra with reports flagged as dummies are excluded or not.
   *
   * @return 		true if dummies are to be excluded
   */
  public boolean getExcludeDummies() {
    return m_ExcludeDummies;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String excludeDummiesTipText() {
    return "If set to true, then spectra with sample data flagged as dummies will be excluded.";
  }

  /**
   * Sets whether to include only spectra with reports flagged as dummies or not.
   *
   * @param value 	if true then only dummies are included
   */
  public void setOnlyDummies(boolean value) {
    m_OnlyDummies = value;
    reset();
  }

  /**
   * Returns whether only spectra with reports flagged as dummies are included or not.
   *
   * @return 		true if only dummies are to be included
   */
  public boolean getOnlyDummies() {
    return m_OnlyDummies;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String onlyDummiesTipText() {
    return "If set to true, then only spectra with sample data flagged as dummies will be included.";
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
   * Sets whether to sort on {@link SampleData#INSERT_TIMESTAMP} instead of auto ID.
   *
   * @param value 	if true then to sort on {@link SampleData#INSERT_TIMESTAMP}
   */
  public void setSortOnInsertTimestamp(boolean value) {
    m_SortOnInsertTimestamp = value;
    reset();
  }

  /**
   * Returns whether to sort on {@link SampleData#INSERT_TIMESTAMP} instead of auto ID.
   *
   * @return 		true if to sort on {@link SampleData#INSERT_TIMESTAMP}
   */
  public boolean getSortOnInsertTimestamp() {
    return m_SortOnInsertTimestamp;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sortOnInsertTimestampTipText() {
    return "If set to true, sorting is performed on the '" + SampleData.INSERT_TIMESTAMP + "' field instead of the auto ID.";
  }

  /**
   * Checks the correctness of the provided values.
   */
  @Override
  public void check() {
    super.check();

    if (m_ExcludeDummies && m_OnlyDummies)
      throw new IllegalStateException("Spectra with reports flagged as dummies can be either included or excluded, but not both!");
  }

  /**
   * Automatically corrects values.
   */
  @Override
  public void update() {
    if (m_Instrument == null)
      m_Instrument = new BaseRegExp();

    if (m_SampleIDRegExp == null)
      m_SampleIDRegExp = new BaseRegExp();

    if (m_Format == null)
      m_Format = new BaseRegExp();

    if (m_SampleTypeRegExp == null)
      m_SampleTypeRegExp = new BaseRegExp();
  }
}
