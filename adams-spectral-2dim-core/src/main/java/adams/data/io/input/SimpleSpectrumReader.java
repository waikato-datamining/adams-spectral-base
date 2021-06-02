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
 * SimpleSpectrumReader.java
 * Copyright (C) 2009-2021 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.Properties;
import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.core.io.PlaceholderFile;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.env.Environment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

/**
 <!-- globalinfo-start -->
 * Reads spectrums in the internal CSV format.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-input &lt;java.io.File&gt; (property: input)
 *         The file to read and turn into a spectrum.
 *         default: .
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SimpleSpectrumReader
  extends AbstractSpectrumReader {

  /** for serialization. */
  private static final long serialVersionUID = -2903357410192470809L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads spectrums in the internal CSV format.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Simple CSV format";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{Spectrum.FILE_EXTENSION, Spectrum.FILE_EXTENSION + ".gz"};
  }

  /**
   * Reads its content from the given reader.
   *
   * @param reader	the reader to use
   * @return		true if successfully read
   */
  protected boolean read(BufferedReader reader) {
    boolean		result;
    Spectrum		sp;
    SpectrumPoint template;
    SpectrumPoint	point;
    String		line;
    List<String>	content;
    List<String>	report;
    SampleData sd;
    Field		field;
    boolean		moreData;

    result = true;

    try {
      do {
	moreData = false;
	sp       = new Spectrum();
	m_ReadData.add(sp);
	template = sp.newPoint();
	
	// read from file
	content = new ArrayList<String>();
	while (((line = reader.readLine()) != null)) {
	  if (line.equals(Spectrum.SEPARATOR)) {
	    moreData = true;
	    break;
	  }
	  content.add(line);
	}

	// report?
	report = new ArrayList<String>();
	while ((content.size() > 0) && content.get(0).startsWith(Properties.COMMENT)) {
	  report.add(content.get(0));
	  content.remove(0);
	}
	if (report.size() > 0) {
	  sd = SampleData.parseProperties(Properties.fromComment(Utils.flatten(report, "\n")));
	  if (sd != null) {
	    sp.setID(sd.getID());
	    sp.setReport(sd);
	  }
	}

	// header - ignored
	if (content.size() > 0)
	  content.remove(0);

	// data points
	while ((content.size() > 0) && result) {
	  line   = content.get(0).trim();
	  content.remove(0);
	  if (line.length() == 0)
	    continue;
	  point  = template.parse(line);
	  result = (point != null);
	  if (result)
	    sp.add(point);
	}

	// update ID
	field = new Field(SampleData.SAMPLE_ID, DataType.STRING);
	if (sp.hasReport() && sp.getReport().hasValue(field))
	  sp.setID(sp.getReport().getStringValue(field));
      }
      while (moreData);
    }
    catch (Exception e) {
      result = false;
      getLogger().log(Level.SEVERE, "Failed to read spectral data!", e);
    }

    return result;
  }

  /**
   * Reads its content from the given file.
   *
   * @param filename	the file to read from
   * @return		true if successfully read
   */
  protected boolean read(String filename) {
    boolean		result;
    BufferedReader	reader;
    FileInputStream	fis;
    FileReader		fr;

    filename = new PlaceholderFile(filename).getAbsolutePath();
    reader   = null;
    fr       = null;
    fis      = null;
    try {
      if (filename.endsWith(".gz")) {
	fis    = new FileInputStream(filename);
	reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(fis)));
      }
      else {
	fr     = new FileReader(filename);
	reader = new BufferedReader(fr);
      }
      result = read(reader);
    }
    catch (Exception e) {
      result = false;
      getLogger().log(Level.SEVERE, "Failed to read spectral data from '" + filename + "'!", e);
    }
    finally {
      FileUtils.closeQuietly(reader);
      FileUtils.closeQuietly(fr);
      FileUtils.closeQuietly(fis);
    }

    return result;
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    read(m_Input.getAbsolutePath());
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
    runReader(Environment.class, SimpleSpectrumReader.class, args);
  }
}
