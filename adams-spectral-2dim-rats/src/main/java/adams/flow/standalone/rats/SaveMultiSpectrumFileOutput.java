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

/**
 * SaveMultiSpectrumFileOutput.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.standalone.rats;

import adams.core.QuickInfoHelper;
import adams.core.Utils;
import adams.core.io.AbstractFilenameGenerator;
import adams.core.io.DefaultFilenameGenerator;
import adams.core.io.PlaceholderFile;
import adams.flow.standalone.rats.output.AbstractRatOutput;
import adams.data.io.output.AbstractSpectrumWriter;
import adams.data.io.output.SimpleSpectrumWriter;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Writes the input spectrum to the file that the filename generator produces.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -Multi->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-filename-generator &lt;adams.core.io.AbstractFilenameGenerator&gt; (property: filenameGenerator)
 * &nbsp;&nbsp;&nbsp;The generator to use for generating the output filename.
 * &nbsp;&nbsp;&nbsp;default: adams.core.io.DefaultFilenameGenerator
 * </pre>
 * 
 * <pre>-writer &lt;knir.data.output.AbstractSpectrumWriter&gt; (property: writer)
 * &nbsp;&nbsp;&nbsp;The writer to use for writing the spectra to disk.
 * &nbsp;&nbsp;&nbsp;default: knir.data.output.SimpleSpectrumWriter
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class SaveMultiSpectrumFileOutput
  extends AbstractRatOutput {

  /** for serialization. */
  private static final long serialVersionUID = 1549363174731144309L;

  /** the filename generator to use. */
  protected AbstractFilenameGenerator m_FilenameGenerator;
  
  /** the sprectrum writer to use. */
  protected AbstractSpectrumWriter m_Writer;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Writes the input MultiSpectrum to the file that the filename generator produces.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "filename-generator", "filenameGenerator",
	    getDefaultFilenameGenerator());

    m_OptionManager.add(
	    "writer", "writer",
	    getDefaultWriter());
  }

  /**
   * Returns the default filename generator to use.
   *
   * @return		the default generator
   */
  protected AbstractFilenameGenerator getDefaultFilenameGenerator() {
    return new DefaultFilenameGenerator();
  }

  /**
   * Sets the filename generator to use.
   *
   * @param value	the generator
   */
  public void setFilenameGenerator(AbstractFilenameGenerator value) {
    m_FilenameGenerator = value;
    reset();
  }

  /**
   * Returns the filename generator in use.
   *
   * @return		the generator
   */
  public AbstractFilenameGenerator getFilenameGenerator() {
    return m_FilenameGenerator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String filenameGeneratorTipText() {
    return "The generator to use for generating the output filename.";
  }

  /**
   * Returns the default writer to use.
   *
   * @return		the default writer
   */
  protected AbstractSpectrumWriter getDefaultWriter() {
    return new SimpleSpectrumWriter();
  }

  /**
   * Sets the writer to use.
   *
   * @param value	the filter
   */
  public void setWriter(AbstractSpectrumWriter value) {
    m_Writer = value;
    reset();
  }

  /**
   * Returns the writer in use.
   *
   * @return		the writer
   */
  public AbstractSpectrumWriter getWriter() {
    return m_Writer;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String writerTipText() {
    return "The writer to use for writing the spectra to disk.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;
    
    result  = QuickInfoHelper.toString(this, "filenameGenerator", getFilenameGenerator(), "filenames: ");
    result += QuickInfoHelper.toString(this, "writer", getWriter(), ", writer: ");
    
    return result;
  }

  /**
   * Returns the type of data that gets accepted.
   * 
   * @return		the type of data
   */
  @Override
  public Class[] accepts() {
    return new Class[]{MultiSpectrum.class};
  }

  /**
   * Performs the actual transmission.
   * 
   * @return		null if successful, otherwise error message
   */
  @Override
  protected String doTransmit() {
    String		result;
    MultiSpectrum	input;
    List<Spectrum>	specs;
    String		file;
    boolean		ok;
    
    result  = null;
    input   = null;
    if (m_Input instanceof MultiSpectrum)
      input = (MultiSpectrum) m_Input;
    else
      result = "Unhandled input class '" + Utils.classToString(m_Input.getClass()) + "', "
	  + "expected: " + Utils.classesToString(accepts());

    if (result == null) {
      specs = new ArrayList<Spectrum>();
      for (Spectrum sp: input)
	specs.add(sp);
      file = m_FilenameGenerator.generate(input);
      m_Writer.setOutput(new PlaceholderFile(file));
      ok = m_Writer.write(specs);
      if (!ok)
	getLogger().severe("Failed to write MultiSpectrum " + input + " to '" + file + "'!");
      else if (isLoggingEnabled())
	getLogger().info("MultiSpectrum " + input + " written to: " + file);
    }
    
    return result;
  }
}
