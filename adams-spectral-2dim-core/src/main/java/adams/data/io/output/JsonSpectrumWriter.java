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
 * JsonSpectrumWriter.java
 * Copyright (C) 2016-2021 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.output;

import adams.core.io.PrettyPrintingSupporter;
import adams.data.report.Field;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumJsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.Writer;
import java.util.List;
import java.util.logging.Level;

/**
<!-- globalinfo-start -->
* Writes spectra in JSON format.<br>
* Output format for single spectrum:<br>
* - outputting the complete report:<br>
* {<br>
*   "id": "someid",<br>
*   "format": "NIR",<br>
*   "waves": [1.0, 2.0],<br>
*   "amplitudes": [1.1, 2.1],<br>
*   "report": {<br>
*     "Sample ID": "someid",<br>
*     "GLV2": 1.123,<br>
*     "valid": true<br>
*   }<br>
* }<br>
* <br>
* - outputting specific reference and meta-data values:<br>
* {<br>
*   "id": "someid",<br>
*   "format": "NIR",<br>
*   "waves": [1.0, 2.0],<br>
*   "amplitudes": [1.1, 2.1],<br>
*   "reference": {<br>
*     "GLV2": 1.123<br>
*   },<br>
*   "meta-data": {<br>
*     "valid": true<br>
*   }<br>
* }<br>
* <br>
* Multiple spectra get wrapped in an array called 'spectra'.
* <br><br>
<!-- globalinfo-end -->
*
<!-- options-start -->
* <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
* &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
* &nbsp;&nbsp;&nbsp;default: WARNING
* </pre>
*
* <pre>-output &lt;adams.core.io.PlaceholderFile&gt; (property: output)
* &nbsp;&nbsp;&nbsp;The file to write the container to.
* &nbsp;&nbsp;&nbsp;default: ${TMP}&#47;out.tmp
* </pre>
*
* <pre>-use-reference-and-metadata &lt;boolean&gt; (property: useReferenceAndMetaData)
* &nbsp;&nbsp;&nbsp;If enabled, the only the specified reference and meta-data report values
* &nbsp;&nbsp;&nbsp;are output (in separate sections).
* &nbsp;&nbsp;&nbsp;default: false
* </pre>
*
* <pre>-reference-value &lt;adams.data.report.Field&gt; [-reference-value ...] (property: referenceValues)
* &nbsp;&nbsp;&nbsp;The reference values to output.
* &nbsp;&nbsp;&nbsp;default:
* </pre>
*
* <pre>-metadata-value &lt;adams.data.report.Field&gt; [-metadata-value ...] (property: metaDataValues)
* &nbsp;&nbsp;&nbsp;The meta-data values to output.
* &nbsp;&nbsp;&nbsp;default:
* </pre>
*
* <pre>-pretty-printing &lt;boolean&gt; (property: prettyPrinting)
* &nbsp;&nbsp;&nbsp;If enabled, the output is printed in a 'pretty' format.
* &nbsp;&nbsp;&nbsp;default: false
* </pre>
* 
<!-- options-end -->
*
* @author FracPete (fracpete at waikato dot ac dot nz)
*/
public class JsonSpectrumWriter
  extends AbstractTextBasedSpectrumWriter
  implements PrettyPrintingSupporter {

  /** for serialization. */
  private static final long serialVersionUID = 208155740775061862L;

  /** whether to output speficied reference and meta-data values. */
  protected boolean m_UseReferenceAndMetaData;

  /** the reference values. */
  protected Field[] m_ReferenceValues;

  /** the meta-data values. */
  protected Field[] m_MetaDataValues;

  /** whether to use pretty-printing. */
  protected boolean m_PrettyPrinting;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Writes spectra in JSON format.\n"
      + "Output format for single spectrum:\n"
      + "- outputting the complete report:\n"
      + SpectrumJsonUtils.example(false) + "\n"
      + "- outputting specific reference and meta-data values:\n"
      + SpectrumJsonUtils.example(true) + "\n"
      + "Multiple spectra get wrapped in an array called 'spectra'.";
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "use-reference-and-metadata", "useReferenceAndMetaData",
      false);

    m_OptionManager.add(
      "reference-value", "referenceValues",
      new Field[0]);

    m_OptionManager.add(
      "metadata-value", "metaDataValues",
      new Field[0]);

    m_OptionManager.add(
      "pretty-printing", "prettyPrinting",
      false);
  }

  /**
   * Sets whether only the specified reference and meta-data report values are
   * output (in separate sections).
   *
   * @param value	true if to use pretty-printing
   */
  public void setUseReferenceAndMetaData(boolean value) {
    m_UseReferenceAndMetaData = value;
    reset();
  }

  /**
   * Returns whether only the specified reference and meta-data report values
   * are output (in separate sections).
   *
   * @return		true if to use pretty-printing
   */
  public boolean getUseReferenceAndMetaData() {
    return m_UseReferenceAndMetaData;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String useReferenceAndMetaDataTipText() {
    return "If enabled, the only the specified reference and meta-data report values are output (in separate sections).";
  }

  /**
   * Sets the reference values to output.
   *
   * @param value	the fields
   */
  public void setReferenceValues(Field[] value) {
    m_ReferenceValues = value;
    reset();
  }

  /**
   * Returns the reference values to output.
   *
   * @return		the fields
   */
  public Field[] getReferenceValues() {
    return m_ReferenceValues;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String referenceValuesTipText() {
    return "The reference values to output.";
  }

  /**
   * Sets the meta-data values to output.
   *
   * @param value	the fields
   */
  public void setMetaDataValues(Field[] value) {
    m_MetaDataValues = value;
    reset();
  }

  /**
   * Returns the meta-data values to output.
   *
   * @return		the fields
   */
  public Field[] getMetaDataValues() {
    return m_MetaDataValues;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String metaDataValuesTipText() {
    return "The meta-data values to output.";
  }

  /**
   * Sets whether to use pretty-printing or not.
   *
   * @param value	true if to use pretty-printing
   */
  public void setPrettyPrinting(boolean value) {
    m_PrettyPrinting = value;
    reset();
  }

  /**
   * Returns whether pretty-printing is used or not.
   *
   * @return		true if to use pretty-printing
   */
  public boolean getPrettyPrinting() {
    return m_PrettyPrinting;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String prettyPrintingTipText() {
    return "If enabled, the output is printed in a 'pretty' format.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the file chooser
   */
  @Override
  public String getFormatDescription() {
    return "JSON format";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 		the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"json"};
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();
    m_OutputIsFile = true;
  }

  /**
   * Returns whether writing of multiple containers is supported.
   *
   * @return 		true if multiple containers are supported
   */
  @Override
  public boolean canWriteMultiple() {
    return true;
  }

  /**
   * Performs the actual writing.
   *
   * @param data	the data to write
   * @param writer 	the writer to write the spectra to
   * @return		true if successfully written
   */
  @Override
  protected boolean writeData(List<Spectrum> data, Writer writer) {
    boolean		result;
    JsonObject		jspec;
    JsonArray		jspecs;
    JsonObject		jcont;
    GsonBuilder		builder;
    Gson 		gson;
    String		content;
    String		msg;

    jcont  = new JsonObject();
    jspecs = new JsonArray();
    for (Spectrum spec: data) {
      if (m_UseReferenceAndMetaData)
	jspec = SpectrumJsonUtils.toJson(spec, m_ReferenceValues, m_MetaDataValues);
      else
	jspec = SpectrumJsonUtils.toJson(spec);
      jspecs.add(jspec);
    }
    jcont.add("spectra", jspecs);
    builder = new GsonBuilder();
    if (m_PrettyPrinting)
      builder.setPrettyPrinting();
    gson    = builder.create();
    content = gson.toJson(jcont);

    try {
      writer.write(content);
      result = true;
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to write spectra with writer!", e);
      result = false;
    }

    return result;
  }
}
