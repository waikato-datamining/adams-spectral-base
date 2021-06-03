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
 * AbstractSpectrumReader.java
 * Copyright (C) 2009-2021 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.ClassLister;
import adams.core.DateFormat;
import adams.core.DateUtils;
import adams.core.Stoppable;
import adams.core.io.PlaceholderDirectory;
import adams.core.io.PlaceholderFile;
import adams.core.logging.LoggingHelper;
import adams.core.option.OptionUtils;
import adams.data.io.output.SimpleSpectrumWriter;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.Report;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.env.Environment;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * Abstract ancestor for readers that read files in various formats and
 * turn them into spectrums.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractSpectrumReader
  extends AbstractDataContainerReader<Spectrum>
  implements Stoppable {

  /** for serialization. */
  private static final long serialVersionUID = -4690065186988048507L;

  public static final String OPTION_OUTPUTDIR = "--output-dir";

  /** the instrument this data is from. */
  protected String m_Instrument;

  /** the form of this data. */
  protected String m_Format;
  
  /** whether to not override the format obtained from the file. */
  protected boolean m_KeepFormat;

  /** whether to use absolute filename for the source report field or just the file's name. */
  protected boolean m_UseAbsoluteSource;

  /** whether reading was stopped. */
  protected boolean m_Stopped;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "instrument", "instrument",
	    getDefaultInstrument());

    m_OptionManager.add(
	    "format", "format",
	    getDefaultFormat());

    m_OptionManager.add(
	    "keep-format", "keepFormat",
	    getDefaultKeepFormat());

    m_OptionManager.add(
	    "use-absolute-source", "useAbsoluteSource",
	    getUseAbsoluteSource());
  }

  /**
   * Returns the default instrument of the spectra.
   *
   * @return		the default
   */
  protected String getDefaultInstrument() {
    return SampleData.DEFAULT_INSTRUMENT;
  }

  /**
   * Returns the default format of the spectra.
   *
   * @return		the default
   */
  protected String getDefaultFormat() {
    return SampleData.DEFAULT_FORMAT;
  }

  /**
   * Returns the default for keeping the format.
   *
   * @return		the default
   */
  protected boolean getDefaultKeepFormat() {
    return false;
  }

  /**
   * Returns the default for using absolute source filename.
   *
   * @return		the default
   */
  protected boolean getDefaultUseAbsoluteSource() {
    return true;
  }

  /**
   * Sets the instrument's name.
   *
   * @param value	the name
   */
  public void setInstrument(String value) {
    m_Instrument = value;
    reset();
  }

  /**
   * Returns the instrument's name.
   *
   * @return		the name
   */
  public String getInstrument() {
    return m_Instrument;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String instrumentTipText() {
    return "The name of the instrument that generated the spectra (if not already present in data).";
  }

  /**
   * Sets the format string of the data (always converted to upper case).
   * Use null to set default format.
   *
   * @param value 	the format
   */
  public void setFormat(String value) {
    if (value == null)
      m_Format = SampleData.DEFAULT_FORMAT;
    else
      m_Format = value.toUpperCase();
    reset();
  }

  /**
   * Returns the format string of the data.
   *
   * @return 		the format
   */
  public String getFormat() {
    return m_Format;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String formatTipText() {
    return "The data format string.";
  }

  /**
   * Sets whether to keep the format obtained from the file.
   *
   * @param value 	true if to keep format
   */
  public void setKeepFormat(boolean value) {
    m_KeepFormat = value;
    reset();
  }

  /**
   * Returns whether to keep the format obtained from the file.
   *
   * @return 		true if to keep format
   */
  public boolean getKeepFormat() {
    return m_KeepFormat;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String keepFormatTipText() {
    return "If enabled the format obtained from the file is not replaced by the format defined here.";
  }

  /**
   * Sets whether to use absolute source filename rather than just name.
   *
   * @param value 	true if to use absolute source
   */
  public void setUseAbsoluteSource(boolean value) {
    m_UseAbsoluteSource = value;
    reset();
  }

  /**
   * Returns whether to use absolute source filename rather than just name.
   *
   * @return 		true if to use absolute source
   */
  public boolean getUseAbsoluteSource() {
    return m_UseAbsoluteSource;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String useAbsoluteSourceTipText() {
    return
      "If enabled the source report field stores the absolute file name "
        + "rather than just the name.";
  }

  /**
   * Hook method for checking the data.
   */
  @Override
  protected void checkData() {
    super.checkData();

    m_Stopped = false;
  }

  /**
   * Creates a dummy report.
   *
   * @param cont	the data container the dummy is for
   * @return		the dummy report or null
   * @see 		#m_CreateDummyReport
   * @see		#postProcessData()
   */
  @Override
  protected Report createDummyReport(Spectrum cont) {
    SampleData	result;

    result = SampleData.createDummy(m_Instrument, new Date(), m_Format);
    result.setValue(new Field(SampleData.SAMPLE_ID, DataType.STRING), cont.getID());

    return result;
  }

  /**
   * Performs some post-processing.
   */
  @Override
  protected void postProcessData() {
    DateFormat 	dateformat;
    SampleData 	sampledata;

    if (m_Stopped)
      m_ReadData = null;

    super.postProcessData();

    if (m_ReadData != null){
      dateformat = DateUtils.getTimestampFormatter();
      for (Spectrum spc: m_ReadData) {
	if (!m_KeepFormat)
	  spc.setFormat(m_Format);
	if (spc.hasReport()) {
	  sampledata = spc.getReport();
	  if (!m_KeepFormat)
	    sampledata.addParameter(SampleData.FORMAT, m_Format);
	  if (m_UseAbsoluteSource)
	    sampledata.addParameter(SampleData.SOURCE, m_Input.getAbsolutePath());
	  else
	    sampledata.addParameter(SampleData.SOURCE, m_Input.getName());
	  if (!sampledata.hasValue(new Field(SampleData.INSTRUMENT, DataType.STRING)))
	    sampledata.addParameter(SampleData.INSTRUMENT, m_Instrument);
	  if (!sampledata.hasValue(new Field(SampleData.SAMPLE_ID, DataType.STRING)))
	    sampledata.addParameter(SampleData.SAMPLE_ID, spc.getID());
	  try {
	    if (!sampledata.hasValue(new Field(SampleData.INSERT_TIMESTAMP, DataType.STRING)))
	      sampledata.addParameter(SampleData.INSERT_TIMESTAMP, dateformat.format(new Date()));
	  }
	  catch (Exception e) {
	    getLogger().log(Level.SEVERE, "Failed to add insert timestamp", e);
	  }
	}
      }
    }
  }

  /**
   * Stops the execution.
   */
  public void stopExecution() {
    m_Stopped = true;
  }

  /**
   * Returns a list with classnames of readers.
   *
   * @return		the reader classnames
   */
  public static String[] getReaders() {
    return ClassLister.getSingleton().getClassnames(AbstractSpectrumReader.class);
  }

  /**
   * Runs the reader from commandline.
   *
   * @param env		the environment class to use
   * @param reader	the reader to execute
   * @param args	the commandline arguments, use --input to specify the input file/dir
   *                    and --output-dir to specify directory to save them as .spec files
   */
  public static void runReader(Class env, Class reader, String[] args) {
    AbstractSpectrumReader 	readerInst;
    List<Spectrum> 		specs;
    PlaceholderDirectory	outputDir;
    int				i;
    PlaceholderFile		outputFile;
    SimpleSpectrumWriter	writer;

    Environment.setEnvironmentClass(env);
    LoggingHelper.useHandlerFromOptions(args);

    try {
      if (OptionUtils.helpRequested(args)) {
	System.out.println("Help requested...\n");
        System.out.println("General options");
        System.out.println("===============\n");
        System.out.println(OPTION_OUTPUTDIR + " <dir>");
        System.out.println("\tThe directory to write the spectrum to (as .spec files).");
        System.out.println("\tOutputs the spectra to stdout if not provided.\n");
	readerInst = (AbstractSpectrumReader) OptionUtils.forName(AbstractSpectrumReader.class, reader.getName(), new String[0]);
	System.out.print("\n" + OptionUtils.list(readerInst));
	LoggingHelper.outputHandlerOption();
      }
      else {
        outputDir = null;
	if (OptionUtils.hasFlag(args, OPTION_OUTPUTDIR)) {
	  outputDir = new PlaceholderDirectory(OptionUtils.getOption(args, OPTION_OUTPUTDIR));
	  OptionUtils.removeOption(args, OPTION_OUTPUTDIR);
	}
	readerInst = (AbstractSpectrumReader) OptionUtils.forName(AbstractSpectrumReader.class, reader.getName(), args);
	specs = readerInst.read();
	if (outputDir != null) {
	  for (i = 0; i < specs.size(); i++) {
	    outputFile = new PlaceholderFile(outputDir.getAbsolutePath() + "/" + (i+1) + ".spec");
	    System.out.println((i+1) + ". " + outputFile);
	    writer = new SimpleSpectrumWriter();
	    writer.setOutputSampleData(true);
	    writer.setOutput(outputFile);
	    writer.write(specs.get(i));
	  }
	}
	else {
	  for (i = 0; i < specs.size(); i++)
	    System.out.println((i+1) + ". " + specs.get(i));
	}
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
