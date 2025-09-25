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
 * JCampDX2SpectrumWriter.java
 * Copyright (C) 2013 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.output;

import adams.core.io.FileUtils;
import adams.data.io.input.JCampDX2SpectrumReader;
import adams.data.report.AbstractField;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import org.jcamp.parser.JCAMPWriter;
import org.jcamp.spectrum.ArrayData;
import org.jcamp.spectrum.MassSpectrum;
import org.jcamp.spectrum.OrderedArrayData;
import org.jcamp.units.Unit;

import java.util.List;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Writer that stores spectra in the simple CSV format.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 * 
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
 * <pre>-output-sample-data &lt;boolean&gt; (property: outputSampleData)
 * &nbsp;&nbsp;&nbsp;If set to true, the sample data gets stored in the file as well (as comment
 * &nbsp;&nbsp;&nbsp;).
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class JCampDX2SpectrumWriter
  extends AbstractSpectrumWriter {

  /** for serialization. */
  private static final long serialVersionUID = 5290679698357490093L;

  /** the comments. */
  public final static String COMMENT = "##";
  
  /** whether to output the sample data as well. */
  protected boolean m_OutputSampleData;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Writer that stores spectra in the simple CSV format.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "output-sample-data", "outputSampleData",
	    false);
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the file chooser
   */
  @Override
  public String getFormatDescription() {
    return new JCampDX2SpectrumReader().getFormatDescription();
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 		the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new JCampDX2SpectrumReader().getFormatExtensions();
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
   * Sets whether to output the sample data as well.
   *
   * @param value	if true then the sample data gets output as well
   */
  public void setOutputSampleData(boolean value) {
    m_OutputSampleData = value;
    reset();
  }

  /**
   * Returns whether to output eh sample data as well.
   *
   * @return		true if the sample data is output as well
   */
  public boolean getOutputSampleData() {
    return m_OutputSampleData;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String outputSampleDataTipText() {
    return "If set to true, the sample data gets stored in the file as well (as comment).";
  }

  /**
   * Writer can only write single chromatograms.
   *
   * @param data	the data to write
   */
  @Override
  protected void checkData(List<Spectrum> data) {
    super.checkData(data);

    if (data.size() != 1)
      throw new IllegalArgumentException(
	  "Writer can only write exactly 1 spectrum at a time!");
  }

  /**
   * Performs the actual writing.
   *
   * @param data	the data to write
   * @return		true if successfully written
   */
  @Override
  protected boolean writeData(List<Spectrum> data) {
    boolean 		result;
    Spectrum		sp;
    SampleData sd;
    SpectrumPoint point;
    MassSpectrum	mass;
    OrderedArrayData	x;
    ArrayData		y;
    double[]		xValues;
    double[]		yValues;
    int			i;
    List<AbstractField>	fields;
    StringBuilder	comments;

    result = true;

    sp = data.get(0);
    
    if (m_OutputSampleData && sp.hasReport()) {
      sd       = sp.getReport();
      fields   = sp.getReport().getFields();
      comments = new StringBuilder();
      for (AbstractField field: fields) {
	if (comments.length() > 0)
	  comments.append("\r\n");
	comments.append(COMMENT);
	comments.append(field.getName());
	comments.append("= ");
	comments.append(sd.getValue(field));
      }
      result = FileUtils.writeToFile(m_Output.getAbsolutePath(), comments, false);
    }
    
    // transfer data
    xValues = new double[sp.size()];
    yValues = new double[sp.size()];
    for (i = 0; i < xValues.length; i++) {
      point      = sp.toList().get(i);
      xValues[i] = point.getWaveNumber();
      yValues[i] = point.getAmplitude();
    }
    
    x    = new OrderedArrayData(xValues, Unit.getUnitFromString(""));
    y    = new ArrayData(yValues, Unit.getUnitFromString(""));
    mass = new MassSpectrum(x, y);
    try {
      result = FileUtils.writeToFile(m_Output.getAbsolutePath(), JCAMPWriter.getInstance().toJCAMP(mass), m_OutputSampleData);
      if (!result)
	getLogger().severe("Failed to write spectrum to " + m_Output + ", check console!");
    }
    catch (Exception e) {
      result = false;
      getLogger().log(Level.SEVERE, "Failed to write spectrum to: " + m_Output, e);
    }

    // report?
    if (sp.hasReport()) {
      fields = sp.getReport().getFields();
      for (AbstractField field: fields)
	mass.setNote(field.getName(), sp.getReport().getValue(field));
    }
    
    return result;
  }
}
