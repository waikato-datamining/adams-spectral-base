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
 * SpectrumIDConditions.java
 * Copyright (C) 2009-2018 University of Waikato, Hamilton, New Zealand
 */

package adams.db;

import adams.core.base.BaseRegExp;

/**
 <!-- globalinfo-start -->
 * Conditions for the retrieval of spectrum IDs.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-limit &lt;int&gt; (property: limit)
 *         The maximum number of IDs to retrieve.
 *         default: 10000
 * </pre>
 *
 * <pre>-sampleid &lt;java.lang.String&gt; (property: regexSampleID)
 *         The regular expression on the spectrum ID.
 *         default:
 * </pre>
 *
 * <pre>-format &lt;java.lang.String&gt; (property: format)
 *         The regular expression on the data format.
 *         default:
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumIDConditions
  extends AbstractLimitedConditions
  implements DataContainerConditions, SampleIDRegExpSupporter, SampleTypeRegExpSupporter {

  /** for serialization. */
  private static final long serialVersionUID = 132688351123941425L;

  /** the regular expression on the name of the spectrum. */
  protected BaseRegExp m_SampleIDRegExp;

  /** the regular expression on the type of the spectrum. */
  protected BaseRegExp m_SampleTypeRegExp;

  /** the regular expression on the format type of the spectrum. */
  protected BaseRegExp m_Format;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Conditions for the retrieval of spectrum IDs.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "sampleid", "sampleIDRegExp",
	    new BaseRegExp(""));

    m_OptionManager.add(
	    "sampletype", "sampleTypeRegExp",
	    new BaseRegExp(""));

    m_OptionManager.add(
	    "format", "format",
	    new BaseRegExp(""));
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
   * Automatically corrects values.
   */
  @Override
  public void update() {
    if (m_SampleIDRegExp == null)
      m_SampleIDRegExp = new BaseRegExp("");

    if (m_Format == null)
      m_Format = new BaseRegExp("");
  }
}
