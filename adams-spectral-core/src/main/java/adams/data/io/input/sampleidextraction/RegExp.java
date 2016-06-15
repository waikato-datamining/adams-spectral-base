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
 * SimpleExtraction.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input.sampleidextraction;

import adams.core.base.BaseRegExp;
import adams.data.spectrum.Spectrum;

import java.io.File;

/**
 <!-- globalinfo-start -->
 * Uses the specified group from a regular expression as sample ID.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-regexp &lt;adams.core.base.BaseRegExp&gt; (property: regExp)
 * &nbsp;&nbsp;&nbsp;The regular expression for extracting the sample ID from the file name (
 * &nbsp;&nbsp;&nbsp;w&#47;o path).
 * &nbsp;&nbsp;&nbsp;default: (.*)\\\\.txt
 * </pre>
 * 
 * <pre>-group &lt;int&gt; (property: group)
 * &nbsp;&nbsp;&nbsp;The regular expression group that contains the sample ID.
 * &nbsp;&nbsp;&nbsp;default: 1
 * &nbsp;&nbsp;&nbsp;minimum: 1
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class RegExp
  extends AbstractSampleIDExtraction {

  private static final long serialVersionUID = 8066127884918088949L;

  /** regexp to extract sample ID from file name. */
  protected BaseRegExp m_RegExp;

  /** the regexp group to use. */
  protected int m_Group;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Uses the specified group from a regular expression as sample ID.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "regexp", "regExp",
      new BaseRegExp("(.*)\\.txt"));

    m_OptionManager.add(
      "group", "group",
      1, 1, null);
  }

  /**
   * Sets the regular expression to use for extracting the sample ID from the
   * file name (w/o path).
   *
   * @param value	the expression
   */
  public void setRegExp(BaseRegExp value) {
    m_RegExp = value;
    reset();
  }

  /**
   * Returns the regular expression in use to extract the sample ID from the
   * file name (w/o path).
   *
   * @return 		the expression
   */
  public BaseRegExp getRegExp() {
    return m_RegExp;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String regExpTipText() {
    return
      "The regular expression for extracting the sample ID from the file "
	+ "name (w/o path).";
  }

  /**
   * Sets the regular expression group that contains the sample ID from the
   * file name (w/o path).
   *
   * @param value	the group
   */
  public void setGroup(int value) {
    if (getOptionManager().isValid("groupSampleID", value)) {
      m_Group = value;
      reset();
    }
  }

  /**
   * Returns the regular expression group that contains the sample ID from the
   * file name (w/o path).
   *
   * @return 		the group
   */
  public int getGroup() {
    return m_Group;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String groupTipText() {
    return "The regular expression group that contains the sample ID.";
  }

  /**
   * Performs the actual extraction.
   *
   * @param file	the current file
   * @param spec	the current spectrum
   * @return		the extracted sample ID
   */
  @Override
  protected String doExtract(File file, Spectrum spec) {
    return file.getName().replaceFirst(m_RegExp.getValue(), "$" + m_Group);
  }
}
