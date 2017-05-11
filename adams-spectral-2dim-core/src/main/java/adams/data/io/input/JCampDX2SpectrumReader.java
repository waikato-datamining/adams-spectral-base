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
 * JCampDX2SpectrumReader.java
 * Copyright (C) 2013-2016 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import org.jcamp.math.IArray1D;
import org.jcamp.math.IArray2D;
import org.jcamp.parser.ErrorHandlerAdapter;
import org.jcamp.parser.JCAMPBlock;
import org.jcamp.parser.JCAMPNTuplePage;
import org.jcamp.parser.JCAMPReader;
import org.jcamp.spectrum.Spectrum1D;
import org.jcamp.spectrum.notes.Note;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Reads spectra in JCamp-DX format.<br>
 * <br>
 * For more information see:<br>
 * http:&#47;&#47;www.jcamp-dx.org&#47;
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
 * <pre>-keep-format &lt;boolean&gt; (property: keepFormat)
 * &nbsp;&nbsp;&nbsp;If enabled the format obtained from the file is not replaced by the format 
 * &nbsp;&nbsp;&nbsp;defined here.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-use-absolute-source &lt;boolean&gt; (property: useAbsoluteSource)
 * &nbsp;&nbsp;&nbsp;If enabled the source report field stores the absolute file name rather 
 * &nbsp;&nbsp;&nbsp;than just the name.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-mode &lt;STRICT|RELAXED&gt; (property: mode)
 * &nbsp;&nbsp;&nbsp;The reader mode.
 * &nbsp;&nbsp;&nbsp;default: STRICT
 * </pre>
 * 
 * <pre>-validate &lt;boolean&gt; (property: validate)
 * &nbsp;&nbsp;&nbsp;If enabled, the parser is validating the input.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-add-raw-metadata &lt;boolean&gt; (property: addRawMetaData)
 * &nbsp;&nbsp;&nbsp;If enabled, the raw meta-data in the file is added to the report as well.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class JCampDX2SpectrumReader
  extends AbstractSpectrumReader {

  /** for serialization. */
  private static final long serialVersionUID = 3095955240781741734L;

  /**
   * The enum for the reader mode.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 2242 $
   */
  public enum ReaderMode {
    STRICT,
    RELAXED
  }

  /** whether to add the raw meta-data. */
  protected boolean m_AddRawMetaData;

  /** the mode. */
  protected ReaderMode m_Mode;

  /** whether the reader validates. */
  protected boolean m_Validate;

  /** whether to use the filename as ID. */
  protected boolean m_UseFilenameAsID;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "Reads spectra in JCamp-DX format.\n\n"
      + "For more information see:\n"
      + "http://www.jcamp-dx.org/";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "mode", "mode",
      ReaderMode.STRICT);

    m_OptionManager.add(
      "validate", "validate",
      false);

    m_OptionManager.add(
      "add-raw-metadata", "addRawMetaData",
      false);

    m_OptionManager.add(
      "use-filename-as-id", "useFilenameAsID",
      false);
  }

  /**
   * Sets the reader mode.
   *
   * @param value	the mode
   */
  public void setMode(ReaderMode value) {
    m_Mode = value;
    reset();
  }

  /**
   * Returns the reader mode.
   *
   * @return		the mode
   */
  public ReaderMode getMode() {
    return m_Mode;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String modeTipText() {
    return "The reader mode.";
  }

  /**
   * Sets whether to use a validating parser.
   *
   * @param value	true if to validate
   */
  public void setValidate(boolean value) {
    m_Validate = value;
    reset();
  }

  /**
   * Returns whether to use a validating parser.
   *
   * @return		true if to validate
   */
  public boolean getValidate() {
    return m_Validate;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String validateTipText() {
    return "If enabled, the parser is validating the input.";
  }

  /**
   * Sets whether to add the raw meta-data to the report.
   *
   * @param value	true if to add
   */
  public void setAddRawMetaData(boolean value) {
    m_AddRawMetaData = value;
    reset();
  }

  /**
   * Returns whether to add the raw meta-data to the report.
   *
   * @return		true if added
   */
  public boolean getAddRawMetaData() {
    return m_AddRawMetaData;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String addRawMetaDataTipText() {
    return "If enabled, the raw meta-data in the file is added to the report as well.";
  }

  /**
   * Sets whether to use the filename as ID.
   *
   * @param value 	true if to use filename
   */
  public void setUseFilenameAsID(boolean value) {
    m_UseFilenameAsID = value;
    reset();
  }

  /**
   * Returns whether to use the filename as ID.
   *
   * @return 		true if to use filename
   */
  public boolean getUseFilenameAsID() {
    return m_UseFilenameAsID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String useFilenameAsIDTipText() {
    return
      "If enabled, the filename gets used as ID.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "JCamp-DX format 2";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"jdx", "dx"};
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    Spectrum			sp;
    SpectrumPoint 		point;
    SampleData 			sd;
    List<String>		content;
    JCAMPBlock 			block;
    String[]			parts;
    int				i;
    int				pos;
    IArray2D			data;
    org.jcamp.spectrum.Spectrum	spec;
    JCAMPNTuplePage		page;
    Spectrum1D			sp1d;
    Double			x;
    Double			y;
    IArray1D			xArray;
    IArray1D			yArray;
    Iterator			iter;
    Note			note;

    content = FileUtils.loadFromFile(m_Input);
    
    try {
      // read DX data
      block = new JCAMPBlock(Utils.flatten(content, "\n"), new ErrorHandlerAdapter());
      block.setValidating(m_Validate);

      // spectrum
      spec = JCAMPReader.getInstance(m_Validate, m_Mode.toString()).createSpectrum(block);
      if (block.isNTupleBlock()) {
	for (pos = 0; pos < block.getNTuple().getPages().length; pos++) {
	  page   = block.getNTuple().getPages()[pos];
	  data   = page.getXYData();
	  xArray = data.getXArray();
	  yArray = data.getYArray();
	  sp     = new Spectrum();
          if (m_UseFilenameAsID)
            sp.setID(FileUtils.replaceExtension(m_Input.getName(), ""));
          else
            sp.setID("" + spec.getTitle() + ": " + (pos+1));
	  sp.getReport().setNumericValue("Page", (pos+1));
	  for (i = 0; i < xArray.getLength(); i++) {
	    x     = xArray.pointAt(i);
	    y     = yArray.pointAt(i);
	    point = new SpectrumPoint(x.floatValue(), y.floatValue());
	    sp.add(point);
	  }
	  m_ReadData.add(sp);
	}
      }
      else {
	if (spec instanceof Spectrum1D) {
	  sp1d = (Spectrum1D) spec;
	  sp   = new Spectrum();
          if (m_UseFilenameAsID)
            sp.setID(FileUtils.replaceExtension(m_Input.getName(), ""));
          else
	    sp.setID("" + spec.getTitle());
	  m_ReadData.add(sp);
	  for (i = 0; i < sp1d.getXData().getLength(); i++) {
	    x     = sp1d.getXData().pointAt(i);
	    y     = sp1d.getYData().pointAt(i);
	    point = new SpectrumPoint(x.floatValue(), y.floatValue());
	    sp.add(point);
	  }
	}
	else {
	  getLogger().severe("Expected " + Spectrum1D.class.getName() + ", but got " + spec.getClass().getName() + " instead, failed to read!");
	}
      }

      // report
      for (Spectrum spectrum: m_ReadData) {
	sd = new SampleData();
	iter = spec.getNotes().iterator();
	while (iter.hasNext()) {
	  note = (Note) iter.next();
	  if (note.getValue() != null) {
	    sd.addField(new Field(note.getDescriptor().toString(), DataType.STRING));
	    sd.setStringValue(note.getDescriptor().toString(), note.getValue().toString());
	  }
	}
	spectrum.getReport().mergeWith(sd);
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to parse JCampDX file: " + m_Input, e);
    }
    
    // failed to read?
    if (m_ReadData.size() == 0)
      return;
    
    // raw meta-data
    if (!m_AddRawMetaData)
      return;
    
    parts = new String[2];
    for (String line: content) {
      if (line.startsWith("##")) {
	line = line.substring(2);
	pos  = line.indexOf('=');
	if (pos > -1) {
	  parts[0] = line.substring(0, pos).trim();
	  parts[1] = line.substring(pos + 1).trim();
	  for (i = 0; i < m_ReadData.size(); i++) {
	    if (!m_ReadData.get(i).hasReport())
	      m_ReadData.get(i).setReport(new SampleData());
	    sd = m_ReadData.get(i).getReport();
	    sd.addField(new Field(parts[0], DataType.STRING));
	    sd.setStringValue(parts[0], parts[1]);
	  }
	}
      }
      else {
	break;
      }
    }
    content = null;
  }
}
