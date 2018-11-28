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
 * FieldInstanceGeneratorWithClass.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.data.instances;

import adams.core.base.BaseString;
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
 * Turns fields of spectra into weka.core.Instance objects, including class attribute.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class FieldInstanceGeneratorWithClass
  extends AbstractFieldInstanceGeneratorWithClass {

  private static final long serialVersionUID = -1117304913142876677L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "A generator for turning fields of the sample data of a spectrum "
      + "into weka.core.Instance objects, with one field acting as class attribute.";
  }

  /**
   * Generates the header of the output data.
   *
   * @param data	the input data
   */
  @Override
  protected void generateHeader(Spectrum data) {
    ArrayList<Attribute> atts;
    ArrayList<String>		attValues;
    StringBuilder		name;

    atts = new ArrayList<>();

    // fields
    name = new StringBuilder();
    for (Field field : m_Fields) {
      if (field.getDataType() == DataType.NUMERIC) {
	atts.add(new Attribute(ArffUtils.getFieldName(field)));
      }
      else if (field.getDataType() == DataType.BOOLEAN) {
	attValues = new ArrayList<>();
	attValues.add(LABEL_FALSE);
	attValues.add(LABEL_TRUE);
	atts.add(new Attribute(ArffUtils.getFieldName(field), attValues));
      }
      else {
	atts.add(new Attribute(ArffUtils.getFieldName(field), (List<String>) null));
      }
      if (name.length() > 0)
	name.append(",");
      name.append(ArffUtils.getFieldName(field));
    }

    // class
    if (m_Field.getDataType() == DataType.NUMERIC) {
      atts.add(new Attribute(ArffUtils.getFieldName(m_Field)));
    }
    else if (m_Field.getDataType() == DataType.BOOLEAN) {
      attValues = new ArrayList<>();
      attValues.add(LABEL_FALSE);
      attValues.add(LABEL_TRUE);
      atts.add(new Attribute(ArffUtils.getFieldName(m_Field), attValues));
    }
    else {
      if (m_ClassLabels.length == 0) {
        atts.add(new Attribute(ArffUtils.getFieldName(m_Field), (List<String>) null));
      }
      else {
        attValues = new ArrayList<>();
        for (BaseString label: m_ClassLabels)
          attValues.add(label.getValue());
        atts.add(new Attribute(ArffUtils.getFieldName(m_Field), attValues));
      }
    }

    m_OutputHeader = new Instances(getClass().getName() + "-" + name.toString(), atts, 0);
  }

  /**
   * Generates the actual data.
   *
   * @param data	the input data to transform
   * @return		the generated data
   */
  @Override
  protected Instance generateOutput(Spectrum data) {
    Instance		result;
    double[]		values;
    int			index;
    SampleData 		report;
    Object		obj;

    values = new double[m_OutputHeader.numAttributes()];
    report = data.getReport();

    // fields
    if (data.hasReport()) {
      for (Field target: m_Fields) {
	index = m_OutputHeader.attribute(ArffUtils.getFieldName(target)).index();
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

    // class
    if (data.hasReport()) {
      values[values.length - 1] = weka.core.Utils.missingValue();
      if (report.hasValue(m_Field)) {
	if (m_Field.getDataType() == DataType.NUMERIC) {
          values[values.length - 1] = report.getDoubleValue(m_Field);
        }
	else if (m_Field.getDataType() == DataType.BOOLEAN) {
          values[values.length - 1] = m_OutputHeader.classAttribute().indexOfValue((report.getBooleanValue(m_Field) ? LABEL_TRUE : LABEL_FALSE));
        }
	else {
	  if (m_ClassLabels.length == 0)
            values[values.length - 1] = m_OutputHeader.classAttribute().addStringValue("" + report.getValue(m_Field));
	  else if (m_OutputHeader.classAttribute().indexOfValue("" + report.getValue(m_Field)) > -1)
            values[values.length - 1] = m_OutputHeader.classAttribute().indexOfValue("" + report.getValue(m_Field));
        }
      }
    }

    result = new DenseInstance(1.0, values);
    result.setDataset(m_OutputHeader);

    return result;
  }
}
