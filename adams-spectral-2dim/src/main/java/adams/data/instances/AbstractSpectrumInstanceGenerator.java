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
 * AbstractSpectrumInstanceGenerator.java
 * Copyright (C) 2009-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.data.instances;

import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.SelectedTag;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;

import java.util.logging.Level;

/**
 * Abstract base class for schemes that turn spectra/sample data into
 * weka.core.Instance objects (spectra is included).
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1905 $
 */
public abstract class AbstractSpectrumInstanceGenerator
  extends AbstractSpectrumBasedInstanceGenerator {

  /** for serialization. */
  private static final long serialVersionUID = 2083516575994387184L;

  /** additional fields to add to the output data. */
  protected Field[] m_AdditionalFields;

  /** drops the prefix for the additional fields. */
  protected boolean m_NoAdditionalFieldsPrefix;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "additional", "additionalFields",
	    new Field[0]);

    m_OptionManager.add(
	    "no-additional-prefix", "noAdditionalFieldsPrefix",
	    false);
  }

  /**
   * Sets whether to drop the prefix for the additional fields.
   *
   * @param value	if true then no prefix
   */
  public void setNoAdditionalFieldsPrefix(boolean value) {
    m_NoAdditionalFieldsPrefix = value;
    reset();
  }

  /**
   * Returns whether to drop the prefix for the additional fields.
   *
   * @return		true if no prefix
   */
  public boolean getNoAdditionalFieldsPrefix() {
    return m_NoAdditionalFieldsPrefix;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String noAdditionalFieldsPrefixTipText() {
    return "If enabled, the additional fields won't get a prefix for their name.";
  }

  /**
   * Sets the additional fields to add.
   *
   * @param value	the fields
   */
  public void setAdditionalFields(Field[] value) {
    m_AdditionalFields = value;
    reset();
  }

  /**
   * Returns the additional fields to add.
   *
   * @return		the fields
   */
  public Field[] getAdditionalFields() {
    return m_AdditionalFields;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String additionalFieldsTipText() {
    return "The additional fields from the sample data to add to the output.";
  }

  /**
   * Checks whether the number of waves are the same.
   *
   * @param data	the input data
   */
  @Override
  protected void checkHeader(Spectrum data) {
    int		size;

    size = m_OutputHeader.numAttributes();
    size -= m_AdditionalFields.length;
    size -= m_Notes.length;
    if (m_AddDatabaseID)
      size--;
    if (m_AddSampleID)
      size--;

    if (size != data.size())
      throw new IllegalStateException(
	  "Spectrum and output data differ in number of waves "
            + "(#" + data.getDatabaseID() + "/" + data.getID() + "/" + data.getFormat() + "): "
            + data.size() + " != " + size);
  }

  /**
   * Generates the header of the output data.
   *
   * @param data	the input data
   */
  @Override
  protected abstract void generateHeader(Spectrum data);

  /**
   * Returns the position used in the "Add" filter for adding the additional
   * fields to the dataset. "first", "second", "third", "last_2", "last_1"
   * and "last" are valid as well.
   *
   * @return		the position string
   * @see		AbstractSpectrumBasedInstanceGenerator#interpretePosition(weka.core.Instances, String)
   */
  protected String getAdditionalFieldsPosition() {
    return "last+1";
  }

  /**
   * Returns the prefix to use for the additional fields.
   *
   * @return		the prefix
   */
  protected String getAdditionalFieldsPrefix() {
    return ArffUtils.PREFIX_ADDITIONALFIELDS;
  }

  /**
   * Adds the specified field to the output format.
   *
   * @param field	the field/field to add
   */
  protected void addField(Field field) {
    Add		add;
    String	prefix;

    if (getNoAdditionalFieldsPrefix())
      prefix = "";
    else
      prefix = getAdditionalFieldsPrefix();

    try {
      add = new Add();
      add.setAttributeIndex(interpretePosition(m_OutputHeader, getAdditionalFieldsPosition()));
      add.setAttributeName(prefix + field.getName());
      if (field.getDataType() == DataType.NUMERIC) {
	add.setAttributeType(new SelectedTag(Attribute.NUMERIC, Add.TAGS_TYPE));
      }
      else if (field.getDataType() == DataType.BOOLEAN) {
	add.setAttributeType(new SelectedTag(Attribute.NOMINAL, Add.TAGS_TYPE));
	add.setNominalLabels(LABEL_FALSE + "," + LABEL_TRUE);
      }
      else {
	add.setAttributeType(new SelectedTag(Attribute.STRING, Add.TAGS_TYPE));
      }
      add.setInputFormat(m_OutputHeader);
      m_OutputHeader = Filter.useFilter(m_OutputHeader, add);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, 
	  "Error initializing the Add filter for additional field '"
	  + field.getName() + "'!", e);
    }
  }

  /**
   * Adds IDs, notes, additional fields to header.
   *
   * @param data	the input data
   */
  @Override
  protected void postProcessHeader(Spectrum data) {
    int		i;

    // additional fields to add to the output?
    if (m_AdditionalFields.length > 0) {
      if (getAdditionalFieldsPosition().indexOf("last") > -1) {
	for (i = 0; i < m_AdditionalFields.length; i++)
	  addField(m_AdditionalFields[i]);
      }
      else {
	for (i = m_AdditionalFields.length - 1; i >= 0; i--)
	  addField(m_AdditionalFields[i]);
      }
    }

    super.postProcessHeader(data);
  }

  /**
   * Adds the IDs, notes, additional fields to the data.
   *
   * @param data	the input data
   * @param inst	the generated instance
   * @return		the processed instance
   */
  @Override
  protected Instance postProcessOutput(Spectrum data, Instance inst) {
    Instance	result;
    int		i;
    double[]	values;
    int		index;
    SampleData	sampledata;
    String	prefix;

    values     = inst.toDoubleArray();
    sampledata = data.getReport();

    // additional sample data fields
    if (getNoAdditionalFieldsPrefix())
      prefix = "";
    else
      prefix = getAdditionalFieldsPrefix();
    for (i = 0; i < m_AdditionalFields.length; i++) {
      index         = m_OutputHeader.attribute(prefix + m_AdditionalFields[i].getName()).index();
      values[index] = weka.core.Utils.missingValue();
      if ((sampledata != null) && sampledata.hasValue(m_AdditionalFields[i])) {
	if (m_AdditionalFields[i].getDataType() == DataType.NUMERIC)
	  values[index] = sampledata.getDoubleValue(m_AdditionalFields[i]);
	else if (m_AdditionalFields[i].getDataType() == DataType.BOOLEAN)
	  values[index] = m_OutputHeader.attribute(index).indexOfValue(sampledata.getBooleanValue(m_AdditionalFields[i]) ? LABEL_TRUE : LABEL_FALSE);
	else
	  values[index] = m_OutputHeader.attribute(index).addStringValue("" + sampledata.getValue(m_AdditionalFields[i]));
      }
    }

    result = new DenseInstance(1.0, values);
    result.setDataset(m_OutputHeader);

    result = super.postProcessOutput(data, result);

    return result;
  }
}
