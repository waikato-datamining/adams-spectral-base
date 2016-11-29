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
 * OpusSpectrumReaderExt.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input;

import adams.core.IEEE754;
import adams.core.MessageCollection;
import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.core.io.PlaceholderFile;
import adams.core.logging.LoggingLevel;
import adams.data.io.input.opus.OpusBlockHelper;
import adams.data.io.input.opus.OpusBlockHelper.Block;
import adams.data.io.input.opus.OpusBlockHelper.BlockDefinition;
import adams.data.io.input.opus.OpusBlockHelper.CommandlineData;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.env.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Reads spectra in OPUS format (extended version).
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
 * <pre>-spectrum-block-type &lt;java.lang.String&gt; (property: spectrumBlockType)
 * &nbsp;&nbsp;&nbsp;The block type of the spectrum to extract, in hex notation; e.g.: 100f
 * &nbsp;&nbsp;&nbsp;default: 100f
 * </pre>
 *
 * <pre>-operation &lt;java.lang.String&gt; (property: operation)
 * &nbsp;&nbsp;&nbsp;The command-line operation to get the sample ID from, e.g., 'MeasureSample'
 * &nbsp;&nbsp;&nbsp;.
 * &nbsp;&nbsp;&nbsp;default: MeasureSample
 * </pre>
 *
 * <pre>-key &lt;java.lang.String&gt; (property: key)
 * &nbsp;&nbsp;&nbsp;The command-line key to get the sample ID from, e.g, 'NAM'.
 * &nbsp;&nbsp;&nbsp;default: SNM
 * </pre>
 *
 * <pre>-all-spectra &lt;boolean&gt; (property: allSpectra)
 * &nbsp;&nbsp;&nbsp;If enabled, all spectra stored in the file are loaded.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-add-command-lines &lt;boolean&gt; (property: addCommandLines)
 * &nbsp;&nbsp;&nbsp;If enabled, the other command-lines extracted from the file gets added to
 * &nbsp;&nbsp;&nbsp;the report.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-add-log &lt;boolean&gt; (property: addLog)
 * &nbsp;&nbsp;&nbsp;If enabled, the entire log extracted from the file gets added to the report.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class OpusSpectrumReaderExt
  extends AbstractSpectrumReader {

  /** the hex mask for the spectrum to extract. */
  protected String m_SpectrumBlockType;

  /** the commandline in the log to use for extracting the sample ID. */
  protected String m_Operation;

  /** the key in the commandline to get the sample ID from. */
  protected String m_Key;

  /** whether to load all spectra. */
  protected boolean m_AllSpectra;

  /** whether to add the meta-data of the other commandlines. */
  protected boolean m_AddCommandLines;

  /** whether to add the complete log to the report. */
  protected boolean m_AddLog;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads spectra in OPUS format (extended version).";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the
   * file chooser
   */
  @Override
  public String getFormatDescription() {
    return "OPUS Format (ext)";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"*"};
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "spectrum-block-type", "spectrumBlockType",
      Integer.toHexString(OpusBlockHelper.BLOCKTYPE_MAIN_MASK));

    m_OptionManager.add(
      "operation", "operation",
      OpusBlockHelper.OPERATION_MEASURESAMPLE);

    m_OptionManager.add(
      "key", "key",
      OpusBlockHelper.KEY_SAMPLEID2);

    m_OptionManager.add(
      "all-spectra", "allSpectra",
      false);

    m_OptionManager.add(
      "add-command-lines", "addCommandLines",
      false);

    m_OptionManager.add(
      "add-log", "addLog",
      false);
  }

  /**
   * Sets the block type of the spectrum to extract.
   *
   * @param value 	the block type (in hex)
   */
  public void setSpectrumBlockType(String value) {
    m_SpectrumBlockType = value;
    reset();
  }

  /**
   * Returns the block type of the spectrum to extract.
   *
   * @return 		the block type (in hex)
   */
  public String getSpectrumBlockType() {
    return m_SpectrumBlockType;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String spectrumBlockTypeTipText() {
    return
      "The block type of the spectrum to extract, in hex notation; e.g.: "
	+ Integer.toHexString(OpusBlockHelper.BLOCKTYPE_MAIN_MASK);
  }

  /**
   * Sets the command-line operation from which to retrieve the sample ID.
   *
   * @param value 	the operation, e.g., "MeasureSample"
   */
  public void setOperation(String value) {
    m_Operation = value;
    reset();
  }

  /**
   * Returns the command-line operation from which to retrieve the sample ID.
   *
   * @return 		the operation, e.g., "MeasureSample"
   */
  public String getOperation() {
    return m_Operation;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String operationTipText() {
    return "The command-line operation to get the sample ID from, e.g., 'MeasureSample'.";
  }

  /**
   * Sets the command-line key from which to retrieve the sample ID.
   *
   * @param value 	the key, e.g., "NAM"
   */
  public void setKey(String value) {
    m_Key = value;
    reset();
  }

  /**
   * Returns the command-line key from which to retrieve the sample ID.
   *
   * @return 		the key, e.g., "NAM"
   */
  public String getKey() {
    return m_Key;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String keyTipText() {
    return "The command-line key to get the sample ID from, e.g, 'NAM'.";
  }

  /**
   * Sets whether to load all spectra.
   *
   * @param value 	true if to load all spectra
   */
  public void setAllSpectra(boolean value) {
    m_AllSpectra = value;
    reset();
  }

  /**
   * Returns whether to load all spectra.
   *
   * @return 		true if to load all spectra
   */
  public boolean getAllSpectra() {
    return m_AllSpectra;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String allSpectraTipText() {
    return "If enabled, all spectra stored in the file are loaded.";
  }

  /**
   * Sets whether to add the other command-lines to the report.
   *
   * @param value 	true if to add the command-lines
   */
  public void setAddCommandLines(boolean value) {
    m_AddCommandLines = value;
    reset();
  }

  /**
   * Returns whether to add the other command-lines to the report.
   *
   * @return 		true if to add the command-lines
   */
  public boolean getAddCommandLines() {
    return m_AddCommandLines;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String addCommandLinesTipText() {
    return "If enabled, the other command-lines extracted from the file gets added to the report.";
  }

  /**
   * Sets whether to add the entire log to the report.
   *
   * @param value 	true if to add the log
   */
  public void setAddLog(boolean value) {
    m_AddLog = value;
    reset();
  }

  /**
   * Returns whether to add the entire log to the report.
   *
   * @return 		true if to add the log
   */
  public boolean getAddLog() {
    return m_AddLog;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String addLogTipText() {
    return "If enabled, the entire log extracted from the file gets added to the report.";
  }

  /**
   * Adds the field to the report.
   *
   * @param sd		the sample data to add the data to
   * @param name	the name of the field
   * @param type	the field type
   * @param value	the value
   */
  protected void addReportValue(SampleData sd, String name, DataType type, Object value) {
    Field 	field;

    field = new Field(name, type);
    sd.addField(field);
    sd.setValue(field, value);
  }

  /**
   * Locates the spectra and adds them to {@link #m_ReadData}.
   *
   * @param buf		the file content
   * @param blocks	the blocks
   */
  protected void findSpectra(byte[] buf, List<Block> blocks) {
    List<Block> 		dpf;
    List<Block> 		data;
    List<Block> 		tmp;
    int 			type;
    int 			i;
    int 			numPoints;
    double 			firstX;
    double 			lastX;
    double 			diff;
    int 			n;
    Spectrum 			sp;
    SampleData 			sd;
    SpectrumPoint 		point;
    StringBuilder 		text;
    StringTokenizer 		tok;
    List<String> 		log;
    List<CommandlineData> 	cmdlines;
    CommandlineData 		cmdline;
    String 			value;
    boolean 			numeric;
    String			index;
    Block			hfl;
    String			instrument;
    boolean			load;
    int				masked;

    // HFL block?
    hfl = null;
    for (Block block : blocks) {
      if ((block.getName() != null) && (block.getName().equals("HFL")))
	hfl = block;
    }

    // DPF blocks
    dpf = new ArrayList<>();
    for (Block block : blocks) {
      if ((block.getName() != null) && (block.getName().equals("DPF")))
	dpf.add(block);
    }

    // get corresponding data blocks
    data = new ArrayList<>();
    tmp = new ArrayList<>();
    for (Block d : dpf) {
      type = d.getType() - OpusBlockHelper.BLOCKTYPE_INCREMENT_DATA_TO_DPF;
      for (Block block : blocks) {
	if (block.getType() == type) {
	  data.add(block);
	  tmp.add(d);
	}
      }
    }
    dpf = tmp; // throw out DPF blocks that don't have a matching data block

    if (dpf.size() != data.size()) {
      getLogger().severe("Can't read data, due to differing number of DPF blocks and data blocks: " + dpf.size() + " != " + data.size());
      if (isLoggingEnabled()) {
	for (Block b: dpf)
	  getLogger().info(b.toString());
	for (Block b: data)
	  getLogger().info(b.toString());
      }
      return;
    }

    // parse data
    for (i = 0; i < dpf.size(); i++) {
      dpf.get(i);
      data.get(i);

      // load spectrum?
      masked = data.get(i).getType() & OpusBlockHelper.BLOCKTYPE_SPEC_MASK;
      load = m_AllSpectra
	|| Integer.toHexString(masked).equals(m_SpectrumBlockType);

      if (load) {
	numPoints = dpf.get(i).getLong(OpusBlockHelper.NPT, 8).intValue();
	firstX = dpf.get(i).getDouble(OpusBlockHelper.FXV, 8);
	lastX = dpf.get(i).getDouble(OpusBlockHelper.LXV, 8);
	diff = (lastX - firstX) / ((double) numPoints - 1.0);
	if (isLoggingEnabled())
	  getLogger().info("firstX=" + firstX + ", lastX=" + lastX + ", numPoints=" + numPoints + ", diff=" + diff);
	sd = new SampleData();
	addReportValue(sd, "Opus.FirstX", DataType.NUMERIC, firstX);
	addReportValue(sd, "Opus.LastX", DataType.NUMERIC, lastX);
	addReportValue(sd, "Opus.NumPoints", DataType.NUMERIC, numPoints);
	addReportValue(sd, "Opus.Diff", DataType.NUMERIC, diff);
	addReportValue(sd, "Opus.BlockType.DPF", DataType.STRING, Integer.toHexString(data.get(i).getType()));
	addReportValue(sd, "Opus.BlockType.Data", DataType.STRING, Integer.toHexString(data.get(i).getType()));

	if (hfl != null) {
	  instrument = hfl.getText(OpusBlockHelper.INS, 8);
	  if ((instrument != null) && !instrument.isEmpty())
	    addReportValue(sd, SampleData.INSTRUMENT, DataType.STRING, instrument);
	}

	sp = new Spectrum();
	sp.setReport(sd);

	for (n = 0; n < numPoints; n++) {
	  point = new SpectrumPoint(
	    (float) (firstX + ((double) n) * diff),
	    (float) IEEE754.toDouble(data.get(i).getLong(n * 4)));
	  sp.add(point);
	}

	m_ReadData.add(sp);
      }
    }

    // retrieve log
    text = new StringBuilder();
    for (i = 0; i < blocks.size(); i++) {
      if (blocks.get(i).getType() == OpusBlockHelper.BLOCKTYPE_TEXT)
	text.append(new String(blocks.get(i).getBufferSection()).trim());
    }
    tok = new StringTokenizer(text.toString(), "\00");  // 0-terminated strings
    log = new ArrayList<>();
    while (tok.hasMoreElements())
      log.add(tok.nextToken());
    for (i = 0; i < log.size(); i++)
      log.set(i, log.get(i).trim());
    Utils.removeEmptyLines(log, true);
    if (isLoggingEnabled())
      getLogger().fine(Utils.flatten(log, "\n"));

    // extract commandlines
    cmdlines = new ArrayList<>();
    for (i = 0; i < log.size(); i++) {
      if (log.get(i).contains(OpusBlockHelper.KEYWORD_CMDLINE)) {
	cmdline = new CommandlineData(log.get(i));
	cmdlines.add(cmdline);
	if (isLoggingEnabled())
	  getLogger().fine(cmdline.toString());
      }
    }

    // add sample-id and meta-dat
    for (Spectrum spec : m_ReadData) {
      for (i = 0; i < cmdlines.size(); i++) {
	cmdline = cmdlines.get(i);
	// sample ID
	if (cmdline.getOperation().equals(m_Operation)) {
	  if (cmdline.has(m_Key))
	    spec.setID(cmdline.get(m_Key));
	}
	// additional meta-data
	if (cmdline.getOperation().equals(m_Operation) || m_AddCommandLines) {
	  for (String key : cmdline.keySet()) {
	    value = cmdline.get(key);
	    numeric = Utils.isDouble(value);
	    index = (cmdline.getOperation().equals(m_Operation) ? "" : (i + 1) + ".");
	    addReportValue(
	      spec.getReport(),
	      "Opus." + index + cmdline.getOperation() + "." + cmdline.getType() + "." + key,
	      (numeric ? DataType.NUMERIC : DataType.STRING),
	      value);
	  }
	}
      }
      // log
      if (m_AddLog)
	addReportValue(spec.getReport(), "Opus.Log", DataType.STRING, Utils.flatten(log, "\n"));
    }
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    byte[] 			buf;
    List<BlockDefinition>	defs;
    List<Block>			blocks;
    MessageCollection		errors;
    int				i;

    try {
      buf = FileUtils.loadFromBinaryFile(m_Input);
      if (buf == null)
	throw new IllegalStateException("Failed to read data from: " + m_Input);

      // definitions
      errors = new MessageCollection();
      defs = OpusBlockHelper.readDefinitions(buf, errors);
      if (!errors.isEmpty())
	throw new IllegalStateException("Failed to obtain block definitions:\n" + errors);
      if (isLoggingEnabled()) {
	for (i = 0; i < defs.size(); i++)
	  getLogger().info("Definition #" + i + ": " + defs.get(i));
      }

      // read blocks
      blocks = OpusBlockHelper.readBlocks(buf, defs);
      if (isLoggingEnabled()) {
	for (i = 0; i < blocks.size(); i++)
	  getLogger().info("Block #" + i + ": " + blocks.get(i));
      }

      // read spectra
      findSpectra(buf, blocks);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to read '" + m_Input + "'!", e);
    }
  }

  public static void main(String[] args) {
    Environment.setEnvironmentClass(Environment.class);
    OpusSpectrumReaderExt reader = new OpusSpectrumReaderExt();
    reader.setLoggingLevel(LoggingLevel.FINE);
    String file;
    //file = "/storm/research/backup/ravensdown/nir/data/2016-04-11/_TN/331776_P06_1107.0";
    //file = "/storm/research/backup/ravensdown/nir/data/2016-04-11/_TN/P_253733.0";
    file = "/storm/research/backup/ravensdown/nir/data/2016-04-11/_TN/482371_P08_0406.0";
    //file = "/home/fracpete/temp/opus_nonsense/694_5459.0";
    reader.setInput(new PlaceholderFile(file));
    List<Spectrum> data = reader.read();
    for (int i = 0; i < data.size(); i++)
      System.out.println(i + ". " + data.get(i));
  }
}
