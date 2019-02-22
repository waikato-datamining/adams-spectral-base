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
 * HasSpectrum.java
 * Copyright (C) 2017-2019 University of Waikato, Hamilton, NZ
 */

package adams.flow.condition.bool;

import adams.core.QuickInfoHelper;
import adams.data.id.IDHandler;
import adams.data.sampledata.SampleData;
import adams.db.SpectrumF;
import adams.flow.core.Actor;
import adams.flow.core.Token;
import adams.flow.core.Unknown;

/**
 <!-- globalinfo-start -->
 * Checks whether a spectrum with either the specified ID or the ID from the passing through ID handler is already present in the database.<br>
 * A specified ID takes precedence of any ID of the token passing through.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-id &lt;java.lang.String&gt; (property: ID)
 * &nbsp;&nbsp;&nbsp;The (optional) ID of the spectrum to look for, if not obtained from the 
 * &nbsp;&nbsp;&nbsp;passing through ID handler.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-format &lt;java.lang.String&gt; (property: format)
 * &nbsp;&nbsp;&nbsp;The data format string.
 * &nbsp;&nbsp;&nbsp;default: NIR
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class HasSpectrum
  extends AbstractBooleanDatabaseCondition {

  private static final long serialVersionUID = 3009206473778753894L;

  /** the ID of the spectrum. */
  protected String m_ID;

  /** the form of this data. */
  protected String m_Format;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Checks whether a spectrum with either the specified ID or the ID from "
	+ "the passing through ID handler is already present in the database.\n"
	+ "A specified ID takes precedence of any ID of the token passing through.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "id", "ID",
      "");

    m_OptionManager.add(
      "format", "format",
      getDefaultFormat());
  }
  
  /**
   * Sets the ID to look, if not obtained from the passing through ID handler.
   *
   * @param value	the ID
   */
  public void setID(String value) {
    m_ID = value;
    reset();
  }

  /**
   * Returns the ID to look, if not obtained from the passing through ID handler.
   *
   * @return		the ID
   */
  public String getID() {
    return m_ID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String IDTipText() {
    return "The (optional) ID of the spectrum to look for, if not obtained from the passing through ID handler.";
  }

  /**
   * Returns the default format of the spectra.
   *
   * @return		the default
   */
  protected String getDefaultFormat() {
    return SampleData.DEFAULT_FORMAT;
  }

  /**
   * Sets the format string of the data (always converted to upper case).
   * Use null to set default format.
   *
   * @param value 	the format
   */
  public void setFormat(String value) {
    if (value == null)
      m_Format = SampleData.DEFAULT_FORMAT;
    else
      m_Format = value.toUpperCase();
    reset();
  }

  /**
   * Returns the format string of the data.
   *
   * @return 		the format
   */
  public String getFormat() {
    return m_Format;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String formatTipText() {
    return "The data format string.";
  }

  /**
   * Returns the quick info string to be displayed in the flow editor.
   *
   * @return		the info or null if no info to be displayed
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result = QuickInfoHelper.toString(this, "ID", (m_ID.isEmpty() ? "-from token-" : m_ID), "ID: ");
    result += QuickInfoHelper.toString(this, "format", (m_Format.isEmpty() ? "-default-" : m_Format), ", format: ");

    return result;
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		adams.flow.core.Unknown.class
   */
  @Override
  public Class[] accepts() {
    return new Class[]{Unknown.class, IDHandler.class};
  }

  /**
   * Performs the actual evaluation.
   *
   * @param owner	the owning actor
   * @param token	the current token passing through
   * @return		the result of the evaluation
   */
  @Override
  protected boolean doEvaluate(Actor owner, Token token) {
    boolean	result;
    String	id;
    String	format;

    result = false;

    id = null;
    if (!m_ID.isEmpty()) {
      id = m_ID;
    }
    else {
      if (token.getPayload() instanceof IDHandler)
	id = ((IDHandler) token.getPayload()).getID();
    }

    format = m_Format;
    if (format.isEmpty())
      format = SampleData.DEFAULT_FORMAT;

    if (id != null)
      result = SpectrumF.getSingleton(getDatabaseConnection()).exists(id, format);
    else
      getLogger().warning("Neither ID specified nor ID obtained from token: " + token);

    return result;
  }
}
