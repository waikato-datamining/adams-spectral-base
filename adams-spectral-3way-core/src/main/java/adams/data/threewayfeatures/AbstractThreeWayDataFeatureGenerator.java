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
 * AbstractImageFeatureGenerator.java
 * Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */

package adams.data.threewayfeatures;

import adams.core.CleanUpHandler;
import adams.core.ShallowCopySupporter;
import adams.core.base.BaseString;
import adams.core.option.AbstractOptionHandler;
import adams.core.option.OptionUtils;
import adams.data.featureconverter.AbstractFeatureConverter;
import adams.data.featureconverter.HeaderDefinition;
import adams.data.featureconverter.SpreadSheet;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.Report;
import adams.data.threeway.ThreeWayData;

import java.lang.reflect.Array;
import java.util.List;

/**
 * Abstract base class for ThreeWayData feature generation.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractThreeWayDataFeatureGenerator
  extends AbstractOptionHandler
  implements Comparable, CleanUpHandler, ShallowCopySupporter<AbstractThreeWayDataFeatureGenerator> {

  /** for serialization. */
  private static final long serialVersionUID = 4566948525813804085L;

  /** the feature converter to use. */
  protected AbstractFeatureConverter m_Converter;

  /** the prefix to use for the fields. */
  protected String m_Prefix;

  /** fields to add to the output data. */
  protected Field[] m_Fields;

  /** the notes to add as attributes. */
  protected BaseString[] m_Notes;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "converter", "converter",
      new SpreadSheet());

    m_OptionManager.add(
      "prefix", "prefix",
      "");

    m_OptionManager.add(
      "field", "fields",
      new Field[0]);

    m_OptionManager.add(
      "notes", "notes",
      new BaseString[0]);
  }

  /**
   * Resets the scheme, i.e., the header information.
   */
  @Override
  protected void reset() {
    super.reset();

    if (m_Converter != null)
      m_Converter.reset();
  }

  /**
   * Sets the feature converter to use.
   *
   * @param value	the converter
   */
  public void setConverter(AbstractFeatureConverter value) {
    m_Converter = value;
    reset();
  }

  /**
   * Returns the feature converter in use.
   *
   * @return		the converter
   */
  public AbstractFeatureConverter getConverter() {
    return m_Converter;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String converterTipText() {
    return "The feature converter to use to produce the output data.";
  }

  /**
   * Sets the (optional) prefix for the feature names.
   *
   * @param value	the prefix
   */
  public void setPrefix(String value) {
    m_Prefix = value;
    reset();
  }

  /**
   * Returns the (optional) prefix for the feature names.
   *
   * @return		the prefix
   */
  public String getPrefix() {
    return m_Prefix;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String prefixTipText() {
    return "The (optional) prefix to use for the feature names.";
  }

  /**
   * Sets the targets to add.
   *
   * @param value	the targets
   */
  public void setFields(Field[] value) {
    m_Fields = value;
    reset();
  }

  /**
   * Returns the targets to add.
   *
   * @return		the targets
   */
  public Field[] getFields() {
    return m_Fields;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String fieldsTipText() {
    return "The fields to add to the output.";
  }

  /**
   * Sets the notes to add as attributes.
   *
   * @param value	the notes prefixes, e.g., "PROCESS INFORMATION"
   */
  public void setNotes(BaseString[] value) {
    m_Notes = value;
    reset();
  }

  /**
   * Returns the current notes to add as attributes.
   *
   * @return		the notes prefixes, e.g., "PROCESS INFORMATION"
   */
  public BaseString[] getNotes() {
    return m_Notes;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the gui
   */
  public String notesTipText() {
    return "The notes to add as attributes to the generated data, eg 'PROCESS INFORMATION'.";
  }

  /**
   * Returns the class of the dataset that the converter generates.
   *
   * @return		the format
   */
  public Class getDatasetFormat() {
    return m_Converter.getDatasetFormat();
  }

  /**
   * Returns the class of the row that the converter generates.
   *
   * @return		the format
   */
  public Class getRowFormat() {
    return m_Converter.getRowFormat();
  }

  /**
   * Optional checks of the data.
   * <br><br>
   * Default implementation only checks whether data is null.
   *
   * @param data	the data to check
   */
  protected void checkData(ThreeWayData data) {
    if (data == null)
      throw new IllegalStateException("No data provided!");
  }

  /**
   * Creates the header from template data.
   *
   * @param data	the data to act as a template
   * @return		the generated header
   */
  public abstract HeaderDefinition createHeader(ThreeWayData data);

  /**
   * Post-processes the header, adding fields and notes.
   *
   * @param header	the header to process
   * @return		the post-processed header
   */
  public HeaderDefinition postProcessHeader(HeaderDefinition header) {
    HeaderDefinition	result;
    int			i;

    result = header;

    // notes
    for (i = 0; i < m_Notes.length; i++)
      header.add(m_Notes[i].getValue(), DataType.STRING);

    // fields
    for (i = 0; i < m_Fields.length; i++)
      header.add(m_Fields[i].getName(), m_Fields[i].getDataType());

    // prefix
    if (!m_Prefix.isEmpty()) {
      for (i = 0; i < header.size(); i++)
        header.rename(i, m_Prefix + header.getName(i));
    }

    return result;
  }

  /**
   * Performs the actual feature generation.
   *
   * @param data	the data to process
   * @return		the generated features
   */
  public abstract List<Object>[] generateRows(ThreeWayData data);

  /**
   * Post-processes the generated row, adding notes and fields.
   *
   * @param data	the data container
   * @param generated	the generated to post-process
   * @return		the updated generated data
   */
  public List<Object> postProcessRow(ThreeWayData data, List<Object> generated) {
    int		i;
    String	valueStr;
    Report	report;

    // notes
    for (i = 0; i < m_Notes.length; i++) {
      valueStr = data.getNotes().getPrefixSubset(m_Notes[i].getValue()).toString();
      generated.add(valueStr);
    }

    // fields
    report = data.getReport();
    for (i = 0; i < m_Fields.length; i++) {
      if (report.hasValue(m_Fields[i])) {
        switch (m_Fields[i].getDataType()) {
          case NUMERIC:
            generated.add(report.getDoubleValue(m_Fields[i]));
            break;
          case BOOLEAN:
            generated.add(report.getBooleanValue(m_Fields[i]));
            break;
          default:
            generated.add(report.getStringValue(m_Fields[i]));
            break;
        }
      }
      else {
        generated.add(null);
      }
    }

    return generated;
  }

  /**
   * Post-processes the generated rows, adding notes and fields.
   *
   * @param data	the data container
   * @param generated	the inst to process
   * @return		the updated instance
   */
  public List<Object>[] postProcessRows(ThreeWayData data, List<Object>[] generated) {
    List<Object>[]	result;
    int			i;

    result = new List[generated.length];
    for (i = 0; i < result.length; i++)
      result[i] = postProcessRow(data, generated[i]);

    return result;
  }

  /**
   * Process the given data. This method will also create the header if
   * necessary.
   *
   * @param data	the data to process
   * @return		the generated array
   * @see		#createHeader(ThreeWayData)
   */
  public Object[] generate(ThreeWayData data) {
    Object[]		result;
    HeaderDefinition	header;
    List<Object>[] 	generated;
    int			i;

    checkData(data);

    // create header if necessary
    if (!m_Converter.isInitialized()) {
      header = createHeader(data);
      if (header == null)
        throw new IllegalStateException("Failed to create header!");
      header = postProcessHeader(header);
      m_Converter.generateHeader(header);
    }

    generated = generateRows(data);
    generated = postProcessRows(data, generated);
    result = (Object[]) Array.newInstance(m_Converter.getRowFormat(), generated.length);
    for (i = 0; i < result.length; i++)
      result[i] = m_Converter.generateRow(generated[i]);

    return result;
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
  public AbstractThreeWayDataFeatureGenerator shallowCopy() {
    return shallowCopy(false);
  }

  /**
   * Returns a shallow copy of itself, i.e., based on the commandline options.
   *
   * @param expand	whether to expand variables to their current values
   * @return		the shallow copy
   */
  public AbstractThreeWayDataFeatureGenerator shallowCopy(boolean expand) {
    return (AbstractThreeWayDataFeatureGenerator) OptionUtils.shallowCopy(this, expand);
  }

  /**
   * Cleans up data structures, frees up memory.
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
}
