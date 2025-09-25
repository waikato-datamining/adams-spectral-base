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
 * RelabSpectrumReader.java
 * Copyright (C) 2014-2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.env.Environment;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Reads spectra in Relab ASCII format.<br>
 * <br>
 * http:&#47;&#47;www.planetary.brown.edu&#47;relabdata&#47;
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-input &lt;adams.core.io.PlaceholderFile&gt; (property: input)
 * &nbsp;&nbsp;&nbsp;The file to read and turn into a container.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 * 
 * <pre>-create-dummy-report &lt;boolean&gt; (property: createDummyReport)
 * &nbsp;&nbsp;&nbsp;If true, then a dummy report is created if none present.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-instrument &lt;java.lang.String&gt; (property: instrument)
 * &nbsp;&nbsp;&nbsp;The name of the instrument that generated the spectra (if not already present 
 * &nbsp;&nbsp;&nbsp;in data).
 * &nbsp;&nbsp;&nbsp;default: unknown
 * </pre>
 * 
 * <pre>-format &lt;java.lang.String&gt; (property: format)
 * &nbsp;&nbsp;&nbsp;The data format string.
 * &nbsp;&nbsp;&nbsp;default: NIR
 * </pre>
 * 
 * <pre>-use-filename-as-sampleid &lt;boolean&gt; (property: useFilenameAsSampleID)
 * &nbsp;&nbsp;&nbsp;If enabled, the filename without path and in uppercase is used in the sample 
 * &nbsp;&nbsp;&nbsp;ID as well.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class RelabSpectrumReader
  extends AbstractTextBasedSpectrumReader {

  /** for serialization. */
  private static final long serialVersionUID = 3095955240781741734L;
  
  /** whether to use the (uppercase) filename as sample ID. */
  protected boolean m_UseFilenameAsSampleID;
  
  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "Reads spectra in Relab ASCII format.\n\n"
	+ "http://www.planetary.brown.edu/relabdata/";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "use-filename-as-sampleid", "useFilenameAsSampleID",
	    false);
  }

  /**
   * Sets whether to use the filename (without path, uppercase) in the sample id as well.
   *
   * @param value	true if to use filename
   */
  public void setUseFilenameAsSampleID(boolean value) {
    m_UseFilenameAsSampleID = value;
    reset();
  }

  /**
   * Returns whether to use the filename (without path, uppercase) in the sample id as well.
   *
   * @return		true if to use filename
   */
  public boolean getUseFilenameAsSampleID() {
    return m_UseFilenameAsSampleID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String useFilenameAsSampleIDTipText() {
    return "If enabled, the filename without path and in uppercase is used in the sample ID as well.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Relab ASCII format";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"asc"};
  }

  /**
   * Performs the actual reading.
   *
   * @param content 	the content to read from
   */
  protected void readData(List<String> content) {
    Spectrum		sp;
    SampleData 		sd;
    int			numData;
    int			i;
    String		line;
    String[]		parts;
    float		wave;
    float		ampl;
    int			lineNo;
    StringBuilder	comments;
    String		id;
    
    sp      = new Spectrum();
    sd      = new SampleData();
    sp.setReport(sd);
    m_ReadData.add(sp);
    
    // spectrum
    numData = Integer.parseInt(content.get(0).trim());
    for (i = 0; i < numData; i++) {
      line  = content.get(i + 1);
      parts = line.trim().replaceAll("[ ][ ]*", " ").split(" ");
      if (parts.length == 2) {
	wave = Float.parseFloat(parts[0]);
	ampl = Float.parseFloat(parts[1]);
	sp.add(new SpectrumPoint(wave, ampl));
      }
    }
    
    // Spectrum ID
    // Example: " C1SF02      .ASC                        "
    lineNo = 1 + numData + 3;
    if (content.size() > lineNo) {
      sd.addField(new Field("Spectrum ID", DataType.STRING));
      sd.setStringValue("Spectrum ID", content.get(lineNo).trim().replaceAll("[ ][ ]*", " ").split(" ")[0]);
    }
    // Sample ID
    lineNo = 1 + numData + 3 + 2;
    if (content.size() > lineNo) {
      id = content.get(lineNo).trim();
      if (id.indexOf(' ') > -1)
	id = id.split(" ")[0];
      if (m_UseFilenameAsSampleID) {
	sp.setID(id + "|" + m_Input.getName().toUpperCase().replace(".ASC", ""));
	sd.addField(new Field("Original Sample ID", DataType.STRING));
	sd.setStringValue("Original Sample ID", id);
      }
      else {
	sp.setID(id);
      }
    }
    // comments
    lineNo = 1 + numData + 3 + 3;
    if (content.size() > lineNo) {
      comments = new StringBuilder();
      for (i = lineNo; i < content.size(); i++) {
	line = content.get(i).trim();
	if (line.isEmpty())
	  continue;
	if (comments.length() > 0)
	  comments.append("\n");
	comments.append(line);
      }
      sd.addField(new Field("Comments", DataType.STRING));
      sd.setStringValue("Comments", comments.toString());
    }
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
    runReader(Environment.class, RelabSpectrumReader.class, args);
  }
}
