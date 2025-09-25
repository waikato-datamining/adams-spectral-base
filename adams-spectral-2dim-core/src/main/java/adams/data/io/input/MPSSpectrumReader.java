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
 * MPSSpectrumReader.java
 * Copyright (C) 2017-2025 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input;

import adams.core.DateFormat;
import adams.core.DateUtils;
import adams.core.Utils;
import adams.data.DateFormatString;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.env.Environment;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Reads XRF spectra in MPS format.
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
 * <pre>-date-format &lt;adams.data.DateFormatString&gt; (property: dateFormat)
 * &nbsp;&nbsp;&nbsp;The format to use for parsing the 'TimeMeasured' date.
 * &nbsp;&nbsp;&nbsp;default: dd-MMM-yyyy HH:mm:ss
 * &nbsp;&nbsp;&nbsp;more: https:&#47;&#47;docs.oracle.com&#47;javase&#47;8&#47;docs&#47;api&#47;java&#47;text&#47;SimpleDateFormat.html
 * </pre>
 *
 * <pre>-normalize-by-live-time &lt;boolean&gt; (property: normalizeByLiveTime)
 * &nbsp;&nbsp;&nbsp;Normalizes the amplitudes using the 'LiveTime' value.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class MPSSpectrumReader
    extends AbstractTextBasedSpectrumReader {

  private static final long serialVersionUID = -5193326571132442647L;

  /** the separator. */
  public final static String SEPARATOR = ": ";

  public static final String KEY_SAMPLE_IDENT = "SampleIdent";

  public static final String KEY_TIME_MEASURED = "TimeMeasured";

  public static final String KEY_ZERO_FIT = "ZeroFit";

  public static final String KEY_GAIN_FIT = "GainFit";

  public final static String KEY_LIVE_TIME = "LiveTime";

  public static final String DATEFORMAT_TIME_MEASURED = "dd-MMM-yyyy HH:mm:ss";

  /** the format to use for parsing the date in the file. */
  protected DateFormatString m_DateFormat;

  /** whether to normalize the amplitude by live time. */
  protected boolean m_NormalizeByLiveTime;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads XRF spectra in MPS format.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	"date-format", "dateFormat",
	new DateFormatString(DATEFORMAT_TIME_MEASURED));

    m_OptionManager.add(
	"normalize-by-live-time", "normalizeByLiveTime",
	false);
  }

  /**
   * Sets the date format for parsing the {@link #KEY_TIME_MEASURED} field.
   *
   * @param value	the format
   */
  public void setDateFormat(DateFormatString value) {
    m_DateFormat = value;
    reset();
  }

  /**
   * Returns the date format for parsing the {@link #KEY_TIME_MEASURED} field.
   *
   * @return		the format
   */
  public DateFormatString getDateFormat() {
    return m_DateFormat;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String dateFormatTipText() {
    return "The format to use for parsing the '" + KEY_TIME_MEASURED + "' date.";
  }

  /**
   * Sets whether to normalize the amplitudes with the {@link #KEY_LIVE_TIME} field.
   *
   * @param value	true if to normalize
   */
  public void setNormalizeByLiveTime(boolean value) {
    m_NormalizeByLiveTime = value;
    reset();
  }

  /**
   * Returns whether to normalize the amplitudes with the {@link #KEY_LIVE_TIME} field.
   *
   * @return		true if to normalize
   */
  public boolean getNormalizeByLiveTime() {
    return m_NormalizeByLiveTime;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String normalizeByLiveTimeTipText() {
    return "Normalizes the amplitudes using the '" + KEY_LIVE_TIME + "' value.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "MPS XRF Spectrum";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"mps"};
  }

  /**
   * Performs the actual reading.
   *
   * @param lines 	the content to read from
   */
  @Override
  protected void readData(List<String> lines) {
    SampleData		sd;
    Spectrum		sp;
    String[]		parts;
    int			i;
    Field		field;
    DateFormat 		dfMPS;
    DateFormat		df;
    double		zeroFit;
    double		gainFit;
    double		liveTime;
    int			index;

    dfMPS    = m_DateFormat.toDateFormat();
    df       = DateUtils.getTimestampFormatter();
    sp       = new Spectrum();
    sd       = new SampleData();
    sp.setReport(sd);
    index    = 0;
    zeroFit  = 0.0;
    gainFit  = 0.0;
    liveTime = 1.0;
    for (String line: lines) {
      // empty?
      if (line.trim().isEmpty())
	continue;
      // meta-data?
      if (line.contains(SEPARATOR)) {
	parts = line.split(SEPARATOR);
	for (i = 0; i < parts.length; i++)
	  parts[i] = parts[i].trim();
	if (parts.length == 2) {
	  if (parts[0].startsWith(KEY_SAMPLE_IDENT)) {
	    sp.setID(parts[1]);
	  }
	  else if (parts[0].startsWith(KEY_TIME_MEASURED)) {
	    field = new Field(SampleData.INSERT_TIMESTAMP, DataType.STRING);
	    sd.addField(field);
	    try {
	      sd.setValue(field, df.format(dfMPS.parse(parts[1])));
	    }
	    catch (Exception e) {
	      getLogger().warning("Unparseable date: " + parts[1]);
	      sd.setValue(field, parts[1]);
	    }
	    field = new Field(parts[0], DataType.STRING);
	    sd.addField(field);
	    try {
	      sd.setValue(field, df.format(dfMPS.parse(parts[1])));
	    }
	    catch (Exception e) {
	      getLogger().warning("Unparseable date: " + parts[1]);
	      sd.setValue(field, parts[1]);
	    }
	  }
	  else {
	    if (Utils.isDouble(parts[1])) {
	      field = new Field(parts[0], DataType.NUMERIC);
	      sd.addField(field);
	      sd.setValue(field, parts[1]);
	      if (parts[0].startsWith(KEY_ZERO_FIT))
		zeroFit = Double.parseDouble(parts[1]);
	      else if (parts[0].startsWith(KEY_GAIN_FIT))
		gainFit = Double.parseDouble(parts[1]);
	      else if (parts[0].startsWith(KEY_LIVE_TIME) && m_NormalizeByLiveTime)
		liveTime = Double.parseDouble(parts[1]);
	    }
	    else {
	      field = new Field(parts[0], DataType.STRING);
	      sd.addField(field);
	      sd.setValue(field, parts[1]);
	    }
	  }
	}
      }
      else {
	sp.add(
	    new SpectrumPoint(
		(float) (zeroFit + index * gainFit),
		(float) (Double.parseDouble(line) / liveTime)));
	index++;
      }
    }

    m_ReadData.add(sp);
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
    runReader(Environment.class, MPSSpectrumReader.class, args);
  }
}
