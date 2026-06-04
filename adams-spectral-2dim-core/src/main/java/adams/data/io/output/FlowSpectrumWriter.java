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
 * FlowSpectrumWriter.java
 * Copyright (C) 2026 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.output;

import adams.core.MessageCollection;
import adams.core.Utils;
import adams.core.io.FlowFile;
import adams.data.spectrum.Spectrum;
import adams.flow.core.Actor;
import adams.flow.core.ActorUtils;
import adams.flow.core.Compatibility;
import adams.flow.core.InputConsumer;
import adams.flow.core.Token;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Writes the spectra using the specified flow.<br>
 * The flow must be a sink and accepting an array of spectra.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * &nbsp;&nbsp;&nbsp;min-user-mode: Expert
 * </pre>
 *
 * <pre>-output &lt;adams.core.io.PlaceholderFile&gt; (property: output)
 * &nbsp;&nbsp;&nbsp;The file to write the spectrum to.
 * &nbsp;&nbsp;&nbsp;default: ${TMP}&#47;out.tmp
 * </pre>
 *
 * <pre>-output-file &lt;boolean&gt; (property: outputFile)
 * &nbsp;&nbsp;&nbsp;Whether the output needs to be a file (true) or directory (true).
 * &nbsp;&nbsp;&nbsp;default: true
 * </pre>
 *
 * <pre>-flow-file &lt;adams.core.io.FlowFile&gt; (property: flowFile)
 * &nbsp;&nbsp;&nbsp;The flow that loads the spectral data.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 *
 <!-- options-end -->
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class FlowSpectrumWriter
  extends AbstractSpectrumWriter {

  private static final long serialVersionUID = -2750478632112211327L;

  /** the flow to use for reading the spectra. */
  protected FlowFile m_FlowFile;

  /** the actor for loading the data. */
  protected transient Actor m_Flow;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Writes the spectra using the specified flow.\n"
	     + "The flow must be a sink and accepting an array of spectra.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "output-file", "outputFile",
      true);

    m_OptionManager.add(
      "flow-file", "flowFile",
      new FlowFile("."));
  }

  /**
   * Sets whether the output needs to be a file or directory.
   *
   * @param value 	true if the output needs to be a file, a directory otherwise
   */
  public void setOutputFile(boolean value) {
    m_OutputIsFile = value;
    reset();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String outputFileTipText() {
    return "Whether the output needs to be a file (true) or directory (true).";
  }

  /**
   * Sets the flow to use.
   *
   * @param value	the file
   */
  public void setFlowFile(FlowFile value) {
    m_FlowFile = value;
    reset();
  }

  /**
   * Returns the flow to use.
   *
   * @return		the file
   */
  public FlowFile getFlowFile() {
    return m_FlowFile;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String flowFileTipText() {
    return "The flow that loads the spectral data.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the
   * file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Flow spectrum";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"*"};
  }

  /**
   * Sets up the flow, if necessary.
   *
   * @return		null if successful, otherwise error message
   */
  protected String setupFlow() {
    MessageCollection errors;
    Actor		flow;
    Compatibility comp;
    Class[]		classes;
    String		msg;

    if (m_Flow != null)
      return null;

    // can we load the flow?
    if (!m_FlowFile.exists())
      return "Flow file does not exist: " + m_FlowFile;
    if (m_FlowFile.isDirectory())
      return "Flow file points to directory: " + m_FlowFile;

    // load the flow
    errors = new MessageCollection();
    flow = ActorUtils.read(m_FlowFile.getAbsolutePath(), errors);
    if (flow == null) {
      if (errors.isEmpty())
	return "Failed to load flow file '" + m_FlowFile + "': unknown reason";
      else
	return "Failed to load flow file '" + m_FlowFile + "': " + errors;
    }
    if (!ActorUtils.isSink(flow))
      return "Flow is not a sink: " + m_FlowFile;

    // check compatibility
    comp    = new Compatibility(true);
    classes = new Class[]{Spectrum[].class};
    if (!comp.isCompatible(classes, ((InputConsumer) flow).accepts()))
      return "Flows does not handle input of " + Utils.classesToString(classes) + ", only: " + Utils.classToString(((InputConsumer) flow).accepts());

    // setup
    msg = flow.setUp();
    if (msg != null)
      return "Failed to setup flow '" + m_FlowFile + "': " + msg;

    m_Flow = flow;

    return null;
  }

  /**
   * Performs the actual writing.
   *
   * @param data the data to write
   * @return true if successfully written
   */
  @Override
  protected boolean writeData(List<Spectrum> data) {
    String	msg;

    msg = setupFlow();
    if (msg != null) {
      getLogger().severe(msg);
      return false;
    }
    try {
      ((InputConsumer) m_Flow).input(new Token(data.toArray(new Spectrum[0])));
      msg = m_Flow.execute();
      if (msg != null) {
	getLogger().severe("Failed to save spectra using '" + m_FlowFile + "': " + msg);
	return false;
      }
      return true;
    }
    catch (Exception e) {
      getLogger().severe("Failed to save spectra using: " + m_FlowFile, e);
      return false;
    }
  }
}
