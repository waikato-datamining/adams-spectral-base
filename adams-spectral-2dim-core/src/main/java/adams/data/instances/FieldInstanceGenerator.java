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
 * SampleDataInstanceGenerator.java
 * Copyright (C) 2011-2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.instances;

import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * A generator for turning fields of the sample data of a spectrum into weka.core.Instance objects.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D &lt;int&gt; (property: debugLevel)
 * &nbsp;&nbsp;&nbsp;The greater the number the more additional info the scheme may output to
 * &nbsp;&nbsp;&nbsp;the console (0 = off).
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 *
 * <pre>-add-db-id (property: addDatabaseID)
 * &nbsp;&nbsp;&nbsp;If set to true, then the database ID will be added to the output.
 * </pre>
 *
 * <pre>-add-sample-id (property: addSampleID)
 * &nbsp;&nbsp;&nbsp;If set to true, then the sample ID will be added to the output.
 * </pre>
 *
 * <pre>-notes &lt;adams.core.base.BaseString&gt; [-notes ...] (property: notes)
 * &nbsp;&nbsp;&nbsp;The notes to add as attributes to the generated data, eg 'PROCESS INFORMATION'
 * &nbsp;&nbsp;&nbsp;.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-load-sample-data (property: loadSampleData)
 * &nbsp;&nbsp;&nbsp;If set to true, then the sample data will be loaded if only dummy report
 * &nbsp;&nbsp;&nbsp;available, using the sample ID.
 * </pre>
 *
 * <pre>-store (property: useStoreTable)
 * &nbsp;&nbsp;&nbsp;If set to true, then the sample data will get read from the store table,
 * &nbsp;&nbsp;&nbsp;otherwise the active one.
 * </pre>
 *
 * <pre>-field &lt;adams.data.report.Field&gt; [-field ...] (property: fields)
 * &nbsp;&nbsp;&nbsp;The sample data fields to add to the output.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class FieldInstanceGenerator
  extends AbstractFieldInstanceGenerator {

  /** for serialization. */
  private static final long serialVersionUID = 7579845592900079095L;

  /** the prefix to use. */
  protected String m_Prefix;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return
        "A generator for turning fields of the sample data of a spectrum "
      + "into weka.core.Instance objects.";
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "prefix", "prefix",
      "");
  }

  /**
   * Sets the prefix for the attribute names.
   *
   * @param value	the name prefix
   */
  public void setPrefix(String value) {
    m_Prefix = value;
    reset();
  }

  /**
   * Returns the prefix for the attribute names.
   *
   * @return		the name prefix
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
    return "The prefix to use for the attributes.";
  }

  /**
   * Generates the header of the output data.
   *
   * @param data	the input data
   */
  protected void generateHeader(Spectrum data) {
    ArrayList<Attribute>	atts;
    ArrayList<String>		attValues;
    StringBuilder		name;

    atts = new ArrayList<>();

    // fields
    name = new StringBuilder();
    for (Field field : m_Fields) {
      if (field.getDataType() == DataType.NUMERIC) {
	atts.add(new Attribute(m_Prefix + ArffUtils.getFieldName(field)));
      }
      else if (field.getDataType() == DataType.BOOLEAN) {
	attValues = new ArrayList<>();
	attValues.add(LABEL_FALSE);
	attValues.add(LABEL_TRUE);
	atts.add(new Attribute(m_Prefix + ArffUtils.getFieldName(field), attValues));
      }
      else {
	atts.add(new Attribute(m_Prefix + ArffUtils.getFieldName(field), (List<String>) null));
      }
      if (name.length() > 0)
	name.append(",");
      name.append(m_Prefix).append(ArffUtils.getFieldName(field));
    }

    m_OutputHeader = new Instances(getClass().getName() + "-" + name, atts, 0);
  }

  /**
   * Generates the actual data.
   *
   * @param data	the input data to transform
   * @return		the generated data
   */
  protected Instance generateOutput(Spectrum data) {
    Instance		result;
    double[]		values;
    int			index;
    SampleData		report;
    Object		obj;

    values = new double[m_OutputHeader.numAttributes()];
    report = data.getReport();

    // fields
    if (data.hasReport()) {
      for (Field target: m_Fields) {
	index = m_OutputHeader.attribute(m_Prefix + ArffUtils.getFieldName(target)).index();
	values[index] = weka.core.Utils.missingValue();
	if (report.hasValue(target)) {
	  if (target.getDataType() == DataType.NUMERIC) {
	    obj = report.getDoubleValue(target);
	    if (obj != null)
	      values[index] = (Double) obj;
	  }
	  else if (target.getDataType() == DataType.BOOLEAN) {
	    obj = report.getBooleanValue(target);
	    if (obj != null)
	      values[index] = m_OutputHeader.attribute(index).indexOfValue(((Boolean) obj ? LABEL_TRUE : LABEL_FALSE));
	  }
	  else {
	    values[index] = m_OutputHeader.attribute(index).addStringValue("" + report.getValue(target));
	  }
	}
      }
    }

    result = new DenseInstance(1.0, values);
    result.setDataset(m_OutputHeader);

    return result;
  }
}
