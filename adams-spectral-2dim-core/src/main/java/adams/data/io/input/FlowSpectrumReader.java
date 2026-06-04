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
 * FlowSpectrumReader.java
 * Copyright (C) 2026 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.MessageCollection;
import adams.core.Utils;
import adams.core.io.FlowFile;
import adams.data.spectrum.Spectrum;
import adams.flow.core.Actor;
import adams.flow.core.ActorUtils;
import adams.flow.core.Compatibility;
import adams.flow.core.InputConsumer;
import adams.flow.core.OutputProducer;
import adams.flow.core.Token;

import java.io.File;
import java.util.Arrays;

/**
 <!-- globalinfo-start -->
 * Reads the file using the specified flow.<br>
 * The flow must take a filename as input (File or String) and either output a single spectrum or an array of spectra.
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
 * <pre>-flow-file &lt;adams.core.io.FlowFile&gt; (property: flowFile)
 * &nbsp;&nbsp;&nbsp;The flow that loads the spectral data.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 *
 <!-- options-end -->
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class FlowSpectrumReader
  extends AbstractSpectrumReader {

  private static final long serialVersionUID = -6681168581072878937L;

  /** the flow to use for reading the spectra. */
  protected FlowFile m_FlowFile;

  /** the actor for loading the data. */
  protected transient Actor m_Flow;

  /** whether input needs to be a string. */
  protected boolean m_InputRequiresString;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads the file using the specified flow.\n"
	     + "The flow must take a filename as input (File or String) and either output a single spectrum or an array of spectra.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "flow-file", "flowFile",
      new FlowFile("."));
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
    MessageCollection	errors;
    Actor		flow;
    Compatibility	comp;
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
    if (!ActorUtils.isTransformer(flow))
      return "Flow is not a transformer: " + m_FlowFile;

    // check compatibility
    comp    = new Compatibility(true);
    classes = new Class[]{String.class, File.class};
    if (!comp.isCompatible(classes, ((InputConsumer) flow).accepts()))
      return "Flows does not handle input of " + Utils.classesToString(classes) + ", only: " + Utils.classToString(((InputConsumer) flow).accepts());
    classes = new Class[]{Spectrum.class, Spectrum[].class};
    if (!comp.isCompatible(((OutputProducer) flow).generates(), classes))
      return "Flows does not generate output of " + Utils.classesToString(classes) + ", but: " + Utils.classToString(((OutputProducer) flow).generates());

    // setup
    msg = flow.setUp();
    if (msg != null)
      return "Failed to setup flow '" + m_FlowFile + "': " + msg;

    m_InputRequiresString = comp.isCompatible(new Class[]{String.class}, ((InputConsumer) flow).accepts());
    m_Flow                = flow;

    return null;
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    String	msg;
    Token	output;

    msg = setupFlow();
    if (msg != null) {
      getLogger().severe(msg);
      return;
    }

    try {
      if (m_InputRequiresString)
	((InputConsumer) m_Flow).input(new Token(m_Input.getAbsolutePath()));
      else
	((InputConsumer) m_Flow).input(new Token(m_Input.getAbsoluteFile()));
      msg = m_Flow.execute();
      if (msg != null) {
	getLogger().severe("Failed to process input '" + m_Input + "': " + msg);
	return;
      }
      if (!((OutputProducer) m_Flow).hasPendingOutput()) {
	getLogger().severe("Failed to generate output from '" + m_Input + "'!");
	return;
      }
      output = ((OutputProducer) m_Flow).output();
      if (output.hasPayload(Spectrum.class))
	m_ReadData.add(output.getPayload(Spectrum.class));
      else if (output.hasPayload(Spectrum[].class))
	m_ReadData.addAll(Arrays.asList(output.getPayload(Spectrum[].class)));
      else
	getLogger().severe("Problem with generated data: " + output.unhandledData());
    }
    catch (Exception e) {
      getLogger().severe("Failed to load data using: " + m_FlowFile, e);
    }
  }
}
