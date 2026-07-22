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
 * SimpleArffSpectrumReader.java
 * Copyright (C) 2026 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.base.BaseRegExp;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.weka.WekaAttributeIndex;
import adams.data.weka.WekaAttributeRange;
import adams.env.Environment;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.SimpleArffLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads spectra from ARFF files.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class SimpleArffSpectrumReader
  extends AbstractSpectrumReader {

  private static final long serialVersionUID = 667753716384716592L;

  /** the attribute containing the sample ID. */
  protected WekaAttributeIndex m_SampleIDAttribute;

  /** the range of attributes containing wave amplitudes. */
  protected WekaAttributeRange m_WaveAttributes;

  /** whether the attribute name contains the wave number. */
  protected boolean m_AttNameContainsWaveNumber;

  /** the regular expression to extract the wave number from the name (first group is used). */
  protected BaseRegExp m_WaveNumberRegExp;

  /** the range of attributes containing sample data. */
  protected WekaAttributeRange m_SampleDataAttributes;

  /** the arff reader. */
  protected transient SimpleArffLoader m_Loader;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads spectra from ARFF files.\n"
	     + "Wave numbers can be extracted from the attribute names.\n"
	     + "Sample data columns are supported as well.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "sample-id-attribute", "sampleIDAttribute",
      new WekaAttributeIndex());

    m_OptionManager.add(
      "wave-attributes", "waveAttributes",
      new WekaAttributeRange());

    m_OptionManager.add(
      "attname-contains-wave-number", "attNameContainsWaveNumber",
      false);

    m_OptionManager.add(
      "wave-number-regexp", "waveNumberRegExp",
      new BaseRegExp("(.*)"));

    m_OptionManager.add(
      "sample-data-attributes", "sampleDataAttributes",
      new WekaAttributeRange());
  }

  /**
   * Sets the attribute containing the sample ID.
   *
   * @param value	the attribute
   */
  public void setSampleIDAttribute(WekaAttributeIndex value) {
    m_SampleIDAttribute = value;
    reset();
  }

  /**
   * Returns the attribute containing the sample ID.
   *
   * @return		the attribute
   */
  public WekaAttributeIndex getSampleIDAttribute() {
    return m_SampleIDAttribute;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sampleIDAttributeTipText() {
    return "The attribute containing the sample ID.";
  }

  /**
   * Sets the attributes containing the wave amplitudes.
   *
   * @param value	the attributes
   */
  public void setWaveAttributes(WekaAttributeRange value) {
    m_WaveAttributes = value;
    reset();
  }

  /**
   * Returns the attributes containing the wave amplitudes.
   *
   * @return		the attributes
   */
  public WekaAttributeRange getWaveAttributes() {
    return m_WaveAttributes;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String waveAttributesTipText() {
    return "The attributes containing the amplitudes.";
  }

  /**
   * Sets whether to extract the wave number from the attribute names.
   *
   * @param value	true if to extract
   */
  public void setAttNameContainsWaveNumber(boolean value) {
    m_AttNameContainsWaveNumber = value;
    reset();
  }

  /**
   * Returns whether to extract the wave number from the attribute names.
   *
   * @return		true if to extract
   */
  public boolean getAttNameContainsWaveNumber() {
    return m_AttNameContainsWaveNumber;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String attNameContainsWaveNumberTipText() {
    return "Whether the attribute names contain the wave numbers.";
  }

  /**
   * Sets the regular expression to identify the wave number (1st group is used).
   *
   * @param value	the expression
   */
  public void setWaveNumberRegExp(BaseRegExp value) {
    m_WaveNumberRegExp = value;
    reset();
  }

  /**
   * Returns the regular expression to identify the wave number (1st group is used).
   *
   * @return		the expression
   */
  public BaseRegExp getWaveNumberRegExp() {
    return m_WaveNumberRegExp;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String waveNumberRegExpTipText() {
    return "The regular expression to identify the wave number (1st group is used).";
  }

  /**
   * Sets the attributes containing the sample data.
   *
   * @param value	the attributes
   */
  public void setSampleDataAttributes(WekaAttributeRange value) {
    m_SampleDataAttributes = value;
    reset();
  }

  /**
   * Returns the attributes containing the sample data.
   *
   * @return		the attributes
   */
  public WekaAttributeRange getSampleDataAttributes() {
    return m_SampleDataAttributes;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sampleDataAttributesTipText() {
    return "The attributes containing the sample data.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the
   * file chooser
   */
  @Override
  public String getFormatDescription() {
    return "ARFF files";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"arff", "arff.gz"};
  }

  /**
   * Identifies the wave numbers.
   *
   * @param data	the data to analyze
   * @param cols	the columns
   * @return		the wave numbers
   */
  protected TFloatArrayList identifyWaveNumbers(Instances data, int[] cols) {
    TFloatArrayList	result;
    String		name;
    String		group;
    int			i;
    int			col;

    result = new TFloatArrayList();

    for (i = 0; i < cols.length; i++) {
      col = cols[i];
      if (m_AttNameContainsWaveNumber) {
	name = data.attribute(col).name();
	group = name.replaceAll(m_WaveNumberRegExp.getValue(), "$1");
	try {
	  result.add(Float.parseFloat(group));
	}
	catch (Exception e) {
	  result.add(i + 1);
	  getLogger().severe("Failed to parse attribute name/wave number #" + (col+1) + ": " + name + "/" + group);
	}
      }
      else {
	result.add(i + 1);
      }
    }

    return result;
  }

  /**
   * Returns the sample data fields.
   *
   * @param data	the sheet to analyze
   * @param cols	the columns
   * @return		the fields
   */
  protected List<Field> identifySampleData(Instances data, int[] cols) {
    List<Field>		result;
    Field		field;

    result = new ArrayList<>();

    for (int col: cols) {
      if (data.attribute(col).isNumeric())
	field = new Field(data.attribute(col).name(), DataType.NUMERIC);
      else
	field = new Field(data.attribute(col).name(), DataType.STRING);
      result.add(field);
    }

    return result;
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    TIntArrayList 	cols;
    TFloatArrayList	waveNo;
    int[]		waveCols;
    List<SpectrumPoint>	points;
    List<Field> 	sdFields;
    Field		field;
    int[]		sdCols;
    int			idCol;
    Instances		data;
    Spectrum 		sp;
    SpectrumPoint 	point;
    SampleData 		sd;
    int			i;

    m_Loader = new SimpleArffLoader();
    if (m_Stopped)
      return;

    points   = new ArrayList<>();
    try {
      m_Loader = new SimpleArffLoader();
      m_Loader.setFile(m_Input.getAbsoluteFile());
      data = m_Loader.getDataSet();
    }
    catch (Exception e) {
      getLogger().severe("Failed to read the data from: " + m_Input, e);
      return;
    }

    // ID
    m_SampleIDAttribute.setData(data);
    idCol = m_SampleIDAttribute.getIntIndex();

    // sample data
    m_SampleDataAttributes.setData(data);
    cols = new TIntArrayList(m_SampleDataAttributes.getIntIndices());
    cols.remove(idCol);
    sdCols = cols.toArray();
    sdFields = identifySampleData(data, sdCols);

    // wave numbers
    m_WaveAttributes.setData(data);
    cols = new TIntArrayList(m_WaveAttributes.getIntIndices());
    cols.remove(idCol);
    for (int col : sdCols)
      cols.remove(col);
    waveCols = cols.toArray();
    waveNo = identifyWaveNumbers(data, waveCols);

    for (Instance inst: data) {
      if (m_Stopped)
	return;

      sp = new Spectrum();

      // wave numbers
      points.clear();
      for (i = 0; i < waveCols.length; i++) {
	if (m_Stopped)
	  return;
	if (!inst.isMissing(waveCols[i])) {
	  point = new SpectrumPoint(waveNo.get(i), (float) inst.value(waveCols[i]));
	  points.add(point);
	}
      }
      sp.addAll(points);

      // sample data
      sd = new SampleData();
      for (i = 0; i < sdCols.length; i++) {
	if (m_Stopped)
	  return;
	if (!inst.isMissing(sdCols[i])) {
	  field = sdFields.get(i);
	  sd.addField(field);
	  if (field.getDataType() == DataType.NUMERIC)
	    sd.setValue(field, inst.value(sdCols[i]));
	  else if (inst.attribute(sdCols[i]).isNominal() || inst.attribute(sdCols[i]).isString())
	    sd.setValue(field, inst.stringValue(sdCols[i]));
	}
      }
      sp.setReport(sd);

      // sample ID
      if (idCol != -1) {
	if (!inst.isMissing(idCol))
	  sp.setID(inst.stringValue(idCol));
      }

      m_ReadData.add(sp);
      if (isLoggingEnabled())
	getLogger().info("Added: " + sp);
    }

    m_Loader = null;
  }

  /**
   * Stops the execution.
   */
  public void stopExecution() {
    super.stopExecution();
    if (m_Loader != null)
      m_Loader.stopExecution();
  }

  /**
   * Runs the reader from the command-line.
   *
   * If the option {@link #OPTION_OUTPUTDIR} is specified then the read spectra
   * get output as .spec files in that directory.
   *
   * @param args	the command-line options to use
   */
  public static void main(String[] args) {
    runReader(Environment.class, RowWiseSpreadSheetSpectrumReader.class, args);
  }
}
