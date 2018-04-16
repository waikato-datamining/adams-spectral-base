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
 * SimpleInstanceGenerator.java
 * Copyright (C) 2009-2017 University of Waikato, Hamilton, New Zealand
 */

package adams.data.instances;

import adams.core.base.BaseString;
import adams.data.report.DataType;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * A simple scheme for turning spectra and fields of the associated sample data into weka.core.Instance objects.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-add-db-id &lt;boolean&gt; (property: addDatabaseID)
 * &nbsp;&nbsp;&nbsp;If set to true, then the database ID will be added to the output.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-tolerate-header-changes &lt;boolean&gt; (property: tolerateHeaderChanges)
 * &nbsp;&nbsp;&nbsp;If set to true, then changes in the header get tolerated (and the header
 * &nbsp;&nbsp;&nbsp;recreated) instead of causing an error.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-offline &lt;boolean&gt; (property: offline)
 * &nbsp;&nbsp;&nbsp;If set to true, the generator operates in offline mode, ie does not access
 * &nbsp;&nbsp;&nbsp;database.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-add-sample-id &lt;boolean&gt; (property: addSampleID)
 * &nbsp;&nbsp;&nbsp;If set to true, then the sample ID will be added to the output.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-notes &lt;adams.core.base.BaseString&gt; [-notes ...] (property: notes)
 * &nbsp;&nbsp;&nbsp;The notes to add as attributes to the generated data, eg 'PROCESS INFORMATION'
 * &nbsp;&nbsp;&nbsp;.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-load-sample-data &lt;boolean&gt; (property: loadSampleData)
 * &nbsp;&nbsp;&nbsp;If set to true, then the sample data will be loaded if only dummy report
 * &nbsp;&nbsp;&nbsp;available, using the sample ID.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-additional &lt;adams.data.report.Field&gt; [-additional ...] (property: additionalFields)
 * &nbsp;&nbsp;&nbsp;The additional fields from the sample data to add to the output.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-no-additional-prefix &lt;boolean&gt; (property: noAdditionalFieldsPrefix)
 * &nbsp;&nbsp;&nbsp;If enabled, the additional fields won't get a prefix for their name.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-field &lt;adams.data.report.Field&gt; (property: field)
 * &nbsp;&nbsp;&nbsp;The field to act as class attribute.
 * &nbsp;&nbsp;&nbsp;default: ADN1[N]
 * </pre>
 *
 * <pre>-class-label &lt;adams.core.base.BaseString&gt; [-class-label ...] (property: classLabels)
 * &nbsp;&nbsp;&nbsp;The class labels to use for a nominal class.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-add-wave &lt;boolean&gt; (property: addWaveNumber)
 * &nbsp;&nbsp;&nbsp;If enabled, the wave number will be added to the output data as well.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-wave-number-as-suffix &lt;boolean&gt; (property: waveNumberAsSuffix)
 * &nbsp;&nbsp;&nbsp;If enabled, the wave number is used as suffix instead of the index.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SimpleInstanceGenerator
  extends AbstractSpectrumInstanceGeneratorWithClass {

  /** for serialization. */
  private static final long serialVersionUID = 7579845592900079095L;

  /** whether to output the wave-number as well. */
  protected boolean m_AddWaveNumber;

  /** whether to use the wave number as suffix. */
  protected boolean m_WaveNumberAsSuffix;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return
        "A simple scheme for turning spectra and fields of the associated "
      + "sample data into weka.core.Instance objects.";
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "add-wave", "addWaveNumber",
      false);

    m_OptionManager.add(
      "wave-number-as-suffix", "waveNumberAsSuffix",
      false);
  }

  /**
   * Sets whether the wave number gets added to the output data as well.
   *
   * @param value 	true if wave number should be output as well
   */
  public void setAddWaveNumber(boolean value) {
    m_AddWaveNumber = value;
    reset();
  }

  /**
   * Returns whether the wave number gets added to the output as well.
   *
   * @return 		true if wave number will be output as well
   */
  public boolean getAddWaveNumber() {
    return m_AddWaveNumber;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String addWaveNumberTipText() {
    return "If enabled, the wave number will be added to the output data as well.";
  }

  /**
   * Sets whether the wave number is used as suffix instead of the index.
   *
   * @param value 	true if to use wave number
   */
  public void setWaveNumberAsSuffix(boolean value) {
    m_WaveNumberAsSuffix = value;
    reset();
  }

  /**
   * Returns whether the wave number is used as suffix instead of the index.
   *
   * @return 		true if to use wave number
   */
  public boolean getWaveNumberAsSuffix() {
    return m_WaveNumberAsSuffix;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String waveNumberAsSuffixTipText() {
    return "If enabled, the wave number is used as suffix instead of the index.";
  }

  /**
   * Checks whether the number of waves are the same.
   *
   * @param data	the input data
   */
  protected void checkHeader(Spectrum data) {
    int		size;

    size = m_OutputHeader.numAttributes() - 1;
    size -= m_AdditionalFields.length;
    size -= m_Notes.length;
    if (m_AddDatabaseID)
      size--;
    if (m_AddSampleID)
      size--;
    if (m_AddWaveNumber)
      size /= 2;

    if (size != data.size())
      throw new IllegalStateException(
	  "Spectrum and output data differ in number of waves (#" + data.getDatabaseID() + ": "
	  + data.size() + " != " + size + "\n" + data);
  }

  /**
   * Generates the header of the output data.
   *
   * @param data	the input data
   */
  protected void generateHeader(Spectrum data) {
    ArrayList<Attribute>	atts;
    ArrayList<String>		attValues;
    int				i;
    float			waveno;

    atts = new ArrayList<>();

    // spectrum
    for (i = 0; i < data.size(); i++) {
      waveno = data.toList().get(i).getWaveNumber();
      if (m_AddWaveNumber) {
	atts.add(new Attribute(m_WaveNumberAsSuffix ? ArffUtils.getWaveNumberName(m_WaveNumberPrefix, waveno) : ArffUtils.getWaveNumberName(m_WaveNumberPrefix, i)));
	atts.add(new Attribute(m_WaveNumberAsSuffix ? ArffUtils.getAmplitudeName(m_AmplitudePrefix, waveno) : ArffUtils.getAmplitudeName(m_AmplitudePrefix, i)));
      }
      else {
	atts.add(new Attribute(m_WaveNumberAsSuffix ? ArffUtils.getAmplitudeName(m_AmplitudePrefix, waveno) : ArffUtils.getAmplitudeName(m_AmplitudePrefix, i)));
      }
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

    m_OutputHeader = new Instances(getClass().getName() + "-" + ArffUtils.getFieldName(m_Field), atts, 0);
    m_OutputHeader.setClassIndex(m_OutputHeader.numAttributes() - 1);
  }

  /**
   * Generates the actual data.
   *
   * @param data	the input data to transform
   * @return		the generated data
   */
  protected Instance generateOutput(Spectrum data) {
    Instance		result;
    int			i;
    double[]		values;
    List<SpectrumPoint>	points;
    int			index;
    SampleData		report;
    float		waveno;

    values = new double[m_OutputHeader.numAttributes()];
    report = data.getReport();

    // spectrum
    points = data.toList();
    for (i = 0; i < data.size(); i++) {
      waveno = data.toList().get(i).getWaveNumber();
      if (m_AddWaveNumber) {
	index         = m_OutputHeader.attribute(m_WaveNumberAsSuffix ? ArffUtils.getWaveNumberName(m_WaveNumberPrefix, waveno) : ArffUtils.getWaveNumberName(m_WaveNumberPrefix, i)).index();
	values[index] = points.get(i).getWaveNumber();
      }
      index         = m_OutputHeader.attribute(m_WaveNumberAsSuffix ? ArffUtils.getAmplitudeName(m_AmplitudePrefix, waveno) : ArffUtils.getAmplitudeName(m_AmplitudePrefix, i)).index();
      values[index] = points.get(i).getAmplitude();
    }

    // field
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
