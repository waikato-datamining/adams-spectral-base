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
 * SpecLibSampleDataReader.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.Constants;
import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.Report;
import adams.data.sampledata.SampleData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Reads USGS SpecLib HTML Description files.<br>
 * <br>
 * http:&#47;&#47;speclab.cr.usgs.gov&#47;spectral.lib06&#47;
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
 * &nbsp;&nbsp;&nbsp;The file to read and turn into a report.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 * 
 * <pre>-offline &lt;boolean&gt; (property: offline)
 * &nbsp;&nbsp;&nbsp;If set to true, the database won't get queried, e.g., for obtaining the 
 * &nbsp;&nbsp;&nbsp;parent ID.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-store &lt;boolean&gt; (property: useStoreTable)
 * &nbsp;&nbsp;&nbsp;If set to true, then the data will get read from the store table, otherwise 
 * &nbsp;&nbsp;&nbsp;the active one.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-format &lt;java.lang.String&gt; (property: format)
 * &nbsp;&nbsp;&nbsp;The type of spectrum to use (used internally to determine the database ID 
 * &nbsp;&nbsp;&nbsp;of the spectrum).
 * &nbsp;&nbsp;&nbsp;default: NIR
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1286 $
 */
public class SpecLibSampleDataReader
  extends AbstractSampleDataReader {

  /** for serialization. */
  private static final long serialVersionUID = 5951148282369493129L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return 
	"Reads USGS SpecLib HTML Description files.\n\n"
	+ "http://speclab.cr.usgs.gov/spectral.lib06/";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "SpecLib Description format";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"html"};
  }

  /**
   * Returns a new instance of the report class in use.
   *
   * @return		the new (empty) report
   */
  @Override
  public SampleData newInstance() {
    return new SampleData();
  }

  /**
   * Tries to determine the parent ID for the current report.
   *
   * @param report	the report to determine the ID for
   * @return		the parent database ID, -1 if it cannot be determined
   */
  @Override
  protected int determineParentID(Report report) {
    return Constants.NO_ID;
  }

  /**
   * Performs the actual reading.
   *
   * @return		the reports that were read
   */
  @Override
  protected List<SampleData> readData() {
    List<SampleData>	result;
    SampleData		sd;
    List<String>	content;
    HashSet<String>	fields;
    String[]		parts;
    String		identifier;
    
    content = FileUtils.loadFromFile(m_Input);
    sd      = newInstance();
    result  = new ArrayList<SampleData>();
    result.add(sd);
    
    // accepted fields
    fields  = new HashSet<String>();
    fields.add("TITLE");
    fields.add("DOCUMENTATION_FORMAT");
    fields.add("SAMPLE_ID");
    fields.add("MINERAL_TYPE");
    fields.add("MINERAL");
    fields.add("FORMULA");
    fields.add("COLLECTION_LOCALITY");
    fields.add("ORIGINAL_DONOR");
    fields.add("CURRENT_SAMPLE_LOCATION");
    fields.add("ULTIMATE_SAMPLE_LOCATION");
    
    // fields
    for (String line: content) {
      // Example: "TITLE: Actinolite HS22 DESCRIPT"
      if (line.indexOf(':') > -1) {
	identifier = line.substring(0, line.indexOf(':'));
	if (fields.contains(identifier)) {
	  if (identifier.equals("SAMPLE_ID"))
	    identifier = SampleData.SAMPLE_ID;
	  else
	    sd.addField(new Field(identifier, DataType.STRING));
	  sd.setStringValue(identifier, line.substring(line.indexOf(':') + 1).trim().replaceAll(" DESCRIPT.*$", "").trim());
	}
      }
    }
    
    // composition
    for (String line: content) {
      // Example: "COMPOSITION:</TD>      <TD>SiO2</TD><TD>   54.53 </TD><TD>wt%</TD>   <TD>SiO<sub>2</sub> </TD></TR>"
      if (line.startsWith("COMPOSITION:")) {
	parts = line.replace(" ", "").replace("</TD>", "").replace("<TD>", "\t").split("\t");
	if (parts.length > 2) {
	  if (Utils.isDouble(parts[2])) {
	    sd.addField(new Field(parts[1], DataType.NUMERIC));
	    sd.setNumericValue(parts[1], new Double(parts[2]));
	  }
	}
      }
    }
      
    return result;
  }
}
