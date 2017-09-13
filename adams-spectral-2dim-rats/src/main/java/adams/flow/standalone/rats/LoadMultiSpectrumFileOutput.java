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
 * LoadMultiSpectrumFileOutput.java
 * Copyright (C) 2014-2015 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.standalone.rats;

import adams.core.AtomicMoveSupporter;
import adams.core.QuickInfoHelper;
import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.core.io.PlaceholderFile;
import adams.data.conversion.SpectraToMultiSpectrum;
import adams.data.io.input.AbstractSpectrumReader;
import adams.data.io.input.SimpleSpectrumReader;
import adams.data.spectrum.Spectrum;
import adams.flow.control.StorageName;
import adams.flow.control.StorageQueueHandler;
import adams.flow.standalone.rats.output.AbstractRatOutput;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Loads spectrum files with the specified reader and puts them in the specified queue as MultiSpectrum.<br>
 * Depending on whether a file could be successfully loaded or not, either .success or .failure gets appended to the filename.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-reader &lt;knir.data.input.AbstractSpectrumReader&gt; (property: reader)
 * &nbsp;&nbsp;&nbsp;The reader to use for importing the data.
 * &nbsp;&nbsp;&nbsp;default: knir.data.input.SimpleSpectrumReader
 * </pre>
 * 
 * <pre>-queue-spectra &lt;adams.flow.control.StorageName&gt; (property: queueSpectra)
 * &nbsp;&nbsp;&nbsp;The name of the queue in the internal storage where the spectra are stored.
 * &nbsp;&nbsp;&nbsp;default: spectra
 * </pre>
 * 
 * <pre>-queue-sucessful &lt;adams.flow.control.StorageName&gt; (property: queueSuccessful)
 * &nbsp;&nbsp;&nbsp;The name of the (optional) queue in the internal storage where successful 
 * &nbsp;&nbsp;&nbsp;filenames are stored.
 * &nbsp;&nbsp;&nbsp;default: successful
 * </pre>
 * 
 * <pre>-queue-failed &lt;adams.flow.control.StorageName&gt; (property: queueFailed)
 * &nbsp;&nbsp;&nbsp;The name of the (optional) queue in the internal storage where failed filenames 
 * &nbsp;&nbsp;&nbsp;are stored.
 * &nbsp;&nbsp;&nbsp;default: failed
 * </pre>
 * 
 * <pre>-atomic-move &lt;boolean&gt; (property: atomicMove)
 * &nbsp;&nbsp;&nbsp;If true, then an atomic move operation will be attempted (NB: not supported 
 * &nbsp;&nbsp;&nbsp;by all operating systems).
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class LoadMultiSpectrumFileOutput
  extends AbstractRatOutput
  implements AtomicMoveSupporter {

  /** for serialization. */
  private static final long serialVersionUID = -918039790676614469L;

  /** the suffix for successful files. */
  public final static String SUFFIX_SUCESS = ".success";

  /** the suffix for failed files. */
  public final static String SUFFIX_FAILURE = ".failure";
  
  /** the sprectrum reader to use. */
  protected AbstractSpectrumReader m_Reader;
  
  /** the name of the queue in the internal storage for spectra. */
  protected StorageName m_QueueSpectra;
  
  /** the name of the queue in the internal storage for successful files. */
  protected StorageName m_QueueSuccessful;

  /** the name of the queue in the internal storage for failed files. */
  protected StorageName m_QueueFailed;

  /** whether to perform an atomic move. */
  protected boolean m_AtomicMove;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return 
	"Loads spectrum files with the specified reader and puts them in the "
	+ "specified queue as MultiSpectrum.\n"
	+ "Depending on whether a file could be successfully loaded or not, "
	  + "either " + SUFFIX_SUCESS + " or " + SUFFIX_FAILURE + " gets "
	+ "appended to the filename.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "reader", "reader",
      getDefaultReader());

    m_OptionManager.add(
      "queue-spectra", "queueSpectra",
      new StorageName("spectra"));

    m_OptionManager.add(
      "queue-sucessful", "queueSuccessful",
      new StorageName("successful"));

    m_OptionManager.add(
      "queue-failed", "queueFailed",
      new StorageName("failed"));

    m_OptionManager.add(
      "atomic-move", "atomicMove",
      false);
  }

  /**
   * Returns the default reader to use.
   *
   * @return		the default reader
   */
  protected AbstractSpectrumReader getDefaultReader() {
    return new SimpleSpectrumReader();
  }

  /**
   * Sets the reader to use.
   *
   * @param value	the filter
   */
  public void setReader(AbstractSpectrumReader value) {
    m_Reader = value;
    reset();
  }

  /**
   * Returns the reader in use.
   *
   * @return		the reader
   */
  public AbstractSpectrumReader getReader() {
    return m_Reader;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String readerTipText() {
    return "The reader to use for importing the data.";
  }

  /**
   * Sets the name for the queue in the internal storage for failed files.
   *
   * @param value	the name
   */
  public void setQueueSpectra(StorageName value) {
    m_QueueSpectra = value;
    reset();
  }

  /**
   * Returns the name for the queue in the internal storage for failed files.
   *
   * @return		the name
   */
  public StorageName getQueueSpectra() {
    return m_QueueSpectra;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String queueSpectraTipText() {
    return "The name of the queue in the internal storage where the spectra are stored.";
  }

  /**
   * Sets the name for the queue in the internal storage for successful files.
   *
   * @param value	the name
   */
  public void setQueueSuccessful(StorageName value) {
    m_QueueSuccessful = value;
    reset();
  }

  /**
   * Returns the name for the queue in the internal storage for successful files.
   *
   * @return		the name
   */
  public StorageName getQueueSuccessful() {
    return m_QueueSuccessful;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String queueSuccessfulTipText() {
    return "The name of the (optional) queue in the internal storage where successful filenames are stored.";
  }

  /**
   * Sets the name for the queue in the internal storage for failed files.
   *
   * @param value	the name
   */
  public void setQueueFailed(StorageName value) {
    m_QueueFailed = value;
    reset();
  }

  /**
   * Returns the name for the queue in the internal storage for failed files.
   *
   * @return		the name
   */
  public StorageName getQueueFailed() {
    return m_QueueFailed;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String queueFailedTipText() {
    return "The name of the (optional) queue in the internal storage where failed filenames are stored.";
  }

  /**
   * Sets whether to attempt atomic move operation.
   *
   * @param value	if true then attempt atomic move operation
   */
  public void setAtomicMove(boolean value) {
    m_AtomicMove = value;
    reset();
  }

  /**
   * Returns whether to attempt atomic move operation.
   *
   * @return 		true if to attempt atomic move operation
   */
  public boolean getAtomicMove() {
    return m_AtomicMove;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String atomicMoveTipText() {
    return
        "If true, then an atomic move operation will be attempted "
	  + "(NB: not supported by all operating systems).";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;
    
    result  = QuickInfoHelper.toString(this, "reader", getReader(), "reader: ");
    result += QuickInfoHelper.toString(this, "queueSpectra", getQueueSpectra(), ", spectra: ");
    result += QuickInfoHelper.toString(this, "queueFailed", getQueueFailed(), ", failed: ");
    result += QuickInfoHelper.toString(this, "queueSuccessful", getQueueSuccessful(), ", sucessful: ");
    
    return result;
  }

  /**
   * Returns the type of data that gets accepted.
   * 
   * @return		the type of data
   */
  @Override
  public Class[] accepts() {
    return new Class[]{String.class, File.class};
  }

  /**
   * Moves the file and adds it to the queue if successful.
   * 
   * @param source	the source file
   * @param target	the target file
   * @param queue	the queue to add it to if successful, null to ignore
   */
  protected void moveFile(File source, File target, StorageQueueHandler queue) {
    try {
      if (FileUtils.move(source, target, m_AtomicMove)) {
	if (queue != null)
	  queue.add(target.getAbsolutePath());
      }
      else {
	if (isLoggingEnabled())
	  getLogger().severe("Failed to rename file '" + source + "' to '" + target + "'!");
      }
    }
    catch (Exception e) {
      if (isLoggingEnabled())
	getLogger().log(Level.SEVERE, "Failed to rename file '" + source + "' to '" + target + "'!", e);
    }
  }
  
  /**
   * Performs the actual transmission.
   * 
   * @return		null if successful, otherwise error message
   */
  @Override
  protected String doTransmit() {
    String			result;
    PlaceholderFile		input;
    StorageQueueHandler		spectra;
    StorageQueueHandler		success;
    StorageQueueHandler		failed;
    List<Spectrum>		specs;
    PlaceholderFile		target;
    SpectraToMultiSpectrum	conv;
    
    result  = null;
    input   = null;
    spectra = null;
    success = null;
    failed  = null;
    conv    = new SpectraToMultiSpectrum();
    if (m_Input instanceof String)
      input = new PlaceholderFile((String) m_Input);
    else if (m_Input instanceof File)
      input = new PlaceholderFile((File) m_Input);
    else
      result = "Unhandled input class '" + Utils.classToString(m_Input) + "', "
	  + "expected: " + Utils.classesToString(accepts());

    if (result == null) {
      spectra = getQueue(m_QueueSpectra);
      success = getQueue(m_QueueSuccessful);
      failed  = getQueue(m_QueueFailed);
      if (spectra == null)
	result = "Queue for spectra not available: " + m_QueueSpectra;
    }
    
    if (result == null) {
      try {
	input = new PlaceholderFile(input);
	m_Reader.setInput(input);
	specs = m_Reader.read();
	if (isLoggingEnabled())
	  getLogger().info("Reading file '" + input + "': " + specs.size() + " spectra");
	if ((specs.size() == 0) || ((specs.size() == 1) && (specs.get(0).size() == 0))) {
	  // rename file
	  target = new PlaceholderFile(input.getAbsolutePath() + SUFFIX_FAILURE);
	  moveFile(input, target, failed);
	}
	else {
	  conv.setInput(specs.toArray(new Spectrum[specs.size()]));
	  result = conv.convert();
	  if (result == null) {
	    // queue spectrum
	    spectra.add(conv.getOutput());
	    specs.clear();
	    // rename file
	    target = new PlaceholderFile(input.getAbsolutePath() + SUFFIX_SUCESS);
	    moveFile(input, target, success);
	  }
	  else {
	    // rename file
	    target = new PlaceholderFile(input.getAbsolutePath() + SUFFIX_FAILURE);
	    moveFile(input, target, failed);
	  }
	}
      }
      catch (Exception e) {
	getLogger().log(Level.SEVERE, "Failed to read '" + input + "'!", e);
	// rename file
	target = new PlaceholderFile(input.getAbsolutePath() + SUFFIX_FAILURE);
	moveFile(input, target, failed);
      }
    }
    
    return result;
  }
}
