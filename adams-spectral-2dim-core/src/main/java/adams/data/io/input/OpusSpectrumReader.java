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
 * OpusSpectrumReader.java
 * Copyright (C) 2015-2021 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.IEEE754;
import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.env.Environment;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Reads spectra in OPUS format.
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
 * <pre>-sample-id &lt;java.lang.String&gt; (property: sampleID)
 * &nbsp;&nbsp;&nbsp;ID|Field1|Field2|Field3|[prefix]
 * &nbsp;&nbsp;&nbsp;default: SNM
 * </pre>
 * 
 * <pre>-start &lt;int&gt; (property: start)
 * &nbsp;&nbsp;&nbsp;Spectrum number to start loading from.
 * &nbsp;&nbsp;&nbsp;default: 1
 * </pre>
 * 
 * <pre>-max &lt;int&gt; (property: max)
 * &nbsp;&nbsp;&nbsp;Maximum spectra to load.
 * &nbsp;&nbsp;&nbsp;default: -1
 * </pre>
 * 
 * <pre>-add-trace-to-report &lt;boolean&gt; (property: addTraceToReport)
 * &nbsp;&nbsp;&nbsp;If enabled the trace of identified blocks etc gets added to the report,
 * &nbsp;&nbsp;&nbsp;using prefix Trace..
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 <!-- options-end -->
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class OpusSpectrumReader
  extends AbstractSpectrumReader {

  /** for serialization. */
  private static final long serialVersionUID = 5668937806981601061L;

  public static int BLOCKS_OFFSET = 0x24;

  /** the prefix for the trace values in the report. */
  public static String PREFIX_TRACE = "Trace.";

  /** where to get sample id from. see param defs */
  protected String m_SampleID;

  /** starting spectrum. **/
  protected int m_Start;

  /** maximum to load. **/
  protected int m_Max;

  /** trace of data that was retrieved from byte array. */
  protected HashMap<String,Object> m_Trace;

  /** whether to add the trace to the report. */
  protected boolean m_AddTraceToReport;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads spectra in OPUS format.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the
   * file chooser
   */
  @Override
  public String getFormatDescription() {
    return "OPUS Format";
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
      "sample-id", "sampleID",
      "SNM");

    m_OptionManager.add(
      "start", "start",
      1);

    m_OptionManager.add(
      "max", "max",
      -1);

    m_OptionManager.add(
      "add-trace-to-report", "addTraceToReport",
      false);
  }

  /**
   * Sets the nth point setting.
   *
   * @param value the nth point to use
   */
  public void setMax(int value) {
    m_Max = value;
    reset();
  }

  /**
   * Returns the nth point setting.
   *
   * @return the nth point
   */
  public int getMax() {
    return m_Max;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for
   * displaying in the GUI or for listing the options.
   */
  public String maxTipText() {
    return "Maximum spectra to load.";
  }

  /**
   * Sets the start point setting.
   *
   * @param value the nth point to use
   */
  public void setStart(int value) {
    m_Start = value;
    reset();
  }

  /**
   * Returns the start point setting.
   *
   * @return the nth point
   */
  public int getStart() {
    return m_Start;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for
   * displaying in the GUI or for listing the options.
   */
  public String startTipText() {
    return "Spectrum number to start loading from.";
  }

  /**
   * Get id field.
   *
   * @return id field
   */
  public String getSampleID() {
    return m_SampleID;
  }

  /**
   * Set id field.
   *
   * @param value
   */
  public void setSampleID(String value) {
    m_SampleID = value;
    reset();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for
   * displaying in the GUI or for listing the options.
   */
  public String sampleIDTipText() {
    return "ID|Field1|Field2|Field3|[prefix]";
  }

  /**
   * Returns whether to add the trace to the report.
   *
   * @return true if to add the trace
   */
  public boolean getAddTraceToReport() {
    return m_AddTraceToReport;
  }

  /**
   * Sets whether to add the trace to the report.
   *
   * @param value true if to add the trace
   */
  public void setAddTraceToReport(boolean value) {
    m_AddTraceToReport = value;
    reset();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for
   * displaying in the GUI or for listing the options.
   */
  public String addTraceToReportTipText() {
    return "If enabled the trace of identified blocks etc gets added to the report, using prefix " + PREFIX_TRACE + ".";
  }

  /**
   * Get int from 4bytes, LSByte first
   *
   * @param b      byte array
   * @param offset grab from
   * @return integer
   */
  protected int getInt(byte[] b, int offset) {
    return (int) convertToLong(b, offset);
  }

  /**
   * Get array of nir data from byte array of bruker file image
   *
   * @param file_image byte array
   * @return nir data array
   */
  protected double[] getNirArray(byte[] file_image) {
    int ndp = getABCount(file_image);
    if (ndp == -1) {
      getLogger().severe("Failed to determine number of data points!");
      return new double[0];
    }
    double[] ret = new double[ndp];
    int datastart = getABDataOffset(file_image); //findStart(file_image);
    if (datastart == -1)
      return null;
    try {
      for (int count = 0; count < ndp; count++) {
	long n = convertToLong(file_image, datastart + (count * 4));
	ret[count] = (double) n;
      }
    }
    catch (Exception e) {
      return null;
    }
    return ret;
  }

  /**
   * Starting from blocks_offset, find sequence of bytes
   * Return position of sequence, or -1 if not found
   *
   * @param buf   	byte array
   * @param byte1	the first byte
   * @param byte2	the second byte
   * @param byte3	the third byte
   * @param byte4	the fourth byte
   * @return position of sequence, or -1 if not found
   */
  protected int getBlockOffset(byte[] buf, byte byte1, byte byte2, byte byte3, byte byte4) {
    int result = -1;
    int offset = BLOCKS_OFFSET;
    boolean found = false;
    while (!found) {
      if (offset >= buf.length - 1) {
	break;
      }
      if (buf[offset] != byte1 && byte1 != -1) {
	offset += 12;
	continue;
      }
      if (buf[offset + 1] != byte2 && byte2 != -1) {
	offset += 12;
	continue;
      }
      if (buf[offset + 2] != byte3 && byte3 != -1) {
	offset += 12;
	continue;
      }
      if (buf[offset + 3] != byte4 && byte4 != -1) {
	offset += 12;
	continue;
      }
      found = true;
    }
    if (found)
      result = offset + 4;

    m_Trace.put("getBlockOffset:" + Utils.toHexArray(new byte[]{byte1, byte2, byte3, byte4}), result);

    return result;
  }

  /**
   * Starting from blocks_offset, find sequence of bytes
   * Return position of sequence, or -1 if not found
   *
   * @param buf   	byte array
   * @param byte1	the first byte
   * @param byte2	the second byte
   * @param byte3	the third byte
   * @param byte4	the fourth byte
   * @return position of sequence, or -1 if not found
   */
  protected int getBlockOffsetReverse(byte[] buf, int start, byte byte1, byte byte2, byte byte3, byte byte4) {
    int result = start;
    boolean found = false;
    while (!found) {
      if (result <= 4) {
	result = -1;
	break;
      }

      if (buf[result] != byte1) {
	result--;
	continue;
      }
      if (buf[result + 1] != byte2) {
	result--;
	continue;
      }
      if (buf[result + 2] != byte3) {
	result--;
	continue;
      }
      if (buf[result + 3] != byte4) {
	result--;
	continue;
      }
      found = true;
    }

    m_Trace.put("getBlockOffsetReverse:" + Utils.toHexArray(new byte[]{byte1, byte2, byte3, byte4}), result);

    return result;
  }

  /**
   * Find position of AB Block offset
   *
   * @param buf byte array
   * @return AB Block offset
   */
  protected int getABOffset(byte[] buf) {
    int result = getBlockOffset(buf, (byte) 0x0f, (byte) 0x10, (byte) 0, (byte) -1);
    m_Trace.put("getABOffset", result);
    return result;
  }

  /**
   * Find position of Text Block offset
   *
   * @param buf byte array
   * @return Text Block offset
   */
  protected int getTextOffset(byte[] buf) {
    int result = getBlockOffset(buf, (byte) -1, (byte) -1, (byte) 0x68, (byte) 0x40);  // h@
    m_Trace.put("getBlockOffset", result);
    return result;
  }

  /**
   * Get number of spectral values
   *
   * @param buf byte array
   * @return number of spectral values
   */
  protected int getABCount(byte[] buf) {
    int result = -1;
    int offset = getABDataOffset(buf);
    int offsetNum = -1;
    if (offset != -1)
      offsetNum = getBlockOffsetReverse(buf, offset, (byte) 0x4E, (byte) 0x50, (byte) 0x54, (byte) 0x00);    // NPT
    if (offsetNum != -1)
      result = getInt(buf, offsetNum + 8);
    m_Trace.put("getABCount", result);
    return result;
  }

  protected double[] getWaveNumbers(byte[] buf) {
    int offset = getABDataOffset(buf);
    if (offset == -1) {
      getLogger().severe("Failed to determine ABDataOffset!");
      return new double[0];
    }
    int offsetFirst = getBlockOffsetReverse(buf, offset, (byte) 0x46, (byte) 0x58, (byte) 0x56, (byte) 0x00);  // FXV
    if (offsetFirst == -1) {
      getLogger().severe("Failed to determine offset for first data point (FXV)!");
      return new double[0];
    }
    int offsetLast = getBlockOffsetReverse(buf, offset, (byte) 0x4C, (byte) 0x58, (byte) 0x56, (byte) 0x00);   // LXV
    if (offsetLast == -1) {
      getLogger().severe("Failed to determine offset for last data point (LXV)!");
      return new double[0];
    }
    int offsetNum = getBlockOffsetReverse(buf, offset, (byte) 0x4E, (byte) 0x50, (byte) 0x54, (byte) 0x00);    // NPT
    if (offsetNum == -1) {
      getLogger().severe("Failed to determine offset for number of data points (NPT)!");
      return new double[0];
    }
    int newcount = getInt(buf, offsetNum + 8);
    double firstx = convert8ToDouble(buf, offsetFirst + 8);
    double lastx = convert8ToDouble(buf, offsetLast + 8);
    double diff = (lastx - firstx) / ((double) newcount - 1.0);

    double[] result = new double[newcount];
    for (int i = 0; i < newcount; i++)
      result[i] = firstx + ((double) i * diff);

    return result;
  }

  /**
   * Get position of Text Data.
   *
   * @param buf 	byte array
   * @return 		the text data pos
   */
  protected int getTextBlockOffset(byte[] buf) {
    int result = -1;
    int offset = getTextOffset(buf);
    if (offset != -1)
      result = getInt(buf, offset + 4);
    m_Trace.put("getTextBlockOffset", result);
    return result;
  }

  /**
   * Size of Text Block (in 4-byte words).
   *
   * @param buf 	byte array of bruker file image
   * @return 		the text block size (in 4-byte words)
   */
  protected int getTextBlockSize(byte[] buf) {
    int result = -1;
    int offset = getTextOffset(buf);
    if (offset != -1)
      result = getInt(buf, offset);
    m_Trace.put("getTextBlockSize", result);
    return result;
  }

  /**
   * Get position of nir data.
   *
   * @param buf 	byte array of file image
   * @return 		nir data pos
   */
  protected int getABDataOffset(byte[] buf) {
    int result = -1;
    int offset = getABOffset(buf);
    if (offset != -1)
      result = getInt(buf, offset + 4);
    m_Trace.put("getABDataOffset", result);
    return result;
  }

  /**
   * Find a given string in byte array, from a starting pos.
   *
   * @param find  	the string to find
   * @param buf   	the byte array
   * @param start 	the starting offset
   * @return 		found?
   */
  protected boolean find(String find, byte[] buf, int start) {
    if (start + find.length() > buf.length)
      return false;
    byte[] b_find = find.getBytes();
    for (int i = 0; i < find.length(); i++) {
      byte b = b_find[i];
      if (b != buf[start + i])
	return false;
    }
    return true;
  }

  /**
   * Attempts to split a string, using the specified delimiter character.
   * A delimiter gets ignored if inside double quotes.
   *
   * @param s		the string to split
   * @return		the parts (single array element if no range)
   */
  public static String[] split(String s) {
    List<String>	result;
    int			i;
    StringBuilder	current;
    boolean		escaped;
    char		c;

    result = new ArrayList<>();

    current = new StringBuilder();
    escaped = false;
    for (i = 0; i < s.length(); i++) {
      c = s.charAt(i);
      if (c == '\'') {
	escaped = !escaped;
	current.append(c);
      }
      else if (c == ',') {
	if (escaped) {
	  current.append(c);
	}
	else {
	  result.add(current.toString().trim());
	  current.delete(0, current.length());
	}
      }
      else {
	current.append(c);
      }
    }

    // add last string
    if (current.length() > 0)
      result.add(current.toString().trim());

    return result.toArray(new String[result.size()]);
  }

  /**
   * Returns the meta-data from the buffer.
   *
   * @param buf		the buffer to read from
   * @return 		the meta data
   */
  protected HashMap<String,Object> getMetaData(byte[] buf) {
    HashMap<String,Object> result = new HashMap<>();
    int offset = getTextBlockOffset(buf);
    int length = getTextBlockSize(buf) * 4;
    byte[] newBuf = new byte[length];
    System.arraycopy(buf, offset, newBuf, 0, length);
    String s = new String(newBuf);
    if ((s.indexOf('{') > -1) && (s.indexOf('}') > -1)) {
      s = s.substring(s.indexOf('{') + 1, s.indexOf('}'));
      String[] parts = split(s);
      for (String part: parts) {
	String[] pair = part.split("=");
	if (pair.length == 2) {
	  if (pair[1].startsWith("'") && pair[1].endsWith("'"))
	    result.put(pair[0], Utils.unquote(pair[1]));
	  else if (Utils.isDouble(pair[1]))
	    result.put(pair[0], Double.parseDouble(pair[1]));
	}
      }
    }
    return result;
  }

  /**
   * Get value for key, from Text Block. Or null if not found.
   *
   * @param key 	the key to look up
   * @param buf 	byte array
   * @return 		the value
   */
  protected String getValueFor(String key, byte[] buf) {
    int offset = getTextBlockOffset(buf);
    int length = getTextBlockSize(buf) * 4;
    for (int i = offset; i < offset + length; i++) {
      if (find(key + "='", buf, i)) {
	String result = "";
	int pos = i + ((key + "='").length());
	while (buf[pos] != 0x27) {
	  result += (char) buf[pos++];
	  if (pos == buf.length - 1)
	    return null;
	}
	return result;
      }
    }
    return null;
  }

  /**
   * Convert byte to unsigned byte.
   *
   * @param b 		the byte
   * @return 		the unsigned byte
   */
  protected int byte2UByte(byte b) {
    int result = b;
    if (result < 0)
      result = result + 256;
    return result;
  }

  /**
   * Convert 4 bytes to long. LSByte first.
   *
   * @param buf      	byte array
   * @param offset 	starting pos
   * @return 		the long value
   */
  protected long convertToLong(byte[] buf, int offset) {
    long result = (long) byte2UByte(buf[offset]);
    result = result + ((long) (byte2UByte(buf[offset + 1]))) * 256;
    result = result + ((long) (byte2UByte(buf[offset + 2]))) * 65536;
    result = result + ((long) (byte2UByte(buf[offset + 3]))) * 16777216;
    return result;
  }

  /**
   * Convert 8 bytes to long. LSByte first.
   *
   * @param buf      	byte array
   * @param offset 	starting pos
   * @return 		the double value
   */
  protected double convert8ToDouble(byte[] buf, int offset) {
    byte[] b = new byte[8];
    b[0] = (byte) byte2UByte(buf[offset + 7]);
    b[1] = (byte) byte2UByte(buf[offset + 6]);
    b[2] = (byte) byte2UByte(buf[offset + 5]);
    b[3] = (byte) byte2UByte(buf[offset + 4]);
    b[4] = (byte) byte2UByte(buf[offset + 3]);
    b[5] = (byte) byte2UByte(buf[offset + 2]);
    b[6] = (byte) byte2UByte(buf[offset + 1]);
    b[7] = (byte) byte2UByte(buf[offset + 0]);

    DataInputStream d = new DataInputStream(new ByteArrayInputStream(b));
    try {
      return d.readDouble();
    }
    catch (IOException e) {
      getLogger().log(Level.SEVERE, "convert8ToDouble", e);
      return Double.NaN;
    }
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    try {
      m_Trace = new HashMap<>();
      byte[] buf = FileUtils.loadFromBinaryFile(m_Input);
      if (buf == null)
	throw new IllegalStateException("Failed to read data from: " + m_Input);
      int datastart = getABDataOffset(buf);
      if (isLoggingEnabled())
	getLogger().info("datastart=" + datastart);
      double[] nir = IEEE754.toDoubleArray(getNirArray(buf));
      double[] wn = getWaveNumbers(buf);
      int nump = getABCount(buf);
      if (isLoggingEnabled())
	getLogger().info("points=" + nump);
      String id = getValueFor(getSampleID(), buf);
      if (id == null) {
	if (isLoggingEnabled())
	  getLogger().info(getSampleID() + "=null");
	id = "ERR";
      }
      else {
	if (isLoggingEnabled())
	  getLogger().info(getSampleID() + "='" + id + "'");
      }
      Spectrum sp = new Spectrum();
      SampleData sd = new SampleData();
      HashMap<String,Object> meta = getMetaData(buf);
      for (String key: meta.keySet()) {
	Object val = meta.get(key);
	if (val instanceof Double) {
	  sd.addField(new Field(key, DataType.NUMERIC));
	  sd.setNumericValue(key, (Double) val);
	}
	else if (val instanceof String) {
	  sd.addField(new Field(key, DataType.STRING));
	  sd.setStringValue(key, (String) val);
	}
      }
      sp.setReport(sd);
      sp.setID(id);
      for (int j = 0; j < nir.length; j++) {
	sp.add(new SpectrumPoint((float) wn[j], (float) nir[j]));
      }
      m_ReadData.add(sp);
      // trace
      List<String> keys = new ArrayList<>(m_Trace.keySet());
      Collections.sort(keys);
      for (String key: keys) {
	Object value = m_Trace.get(key);
	if (isLoggingEnabled()) {
	  getLogger().info(key + "=" + value);
	}
	else if (m_AddTraceToReport) {
	  boolean numeric = value instanceof Number;
	  sd.addField(new Field(PREFIX_TRACE + key, (numeric ? DataType.NUMERIC : DataType.STRING)));
	  if (numeric)
	    sd.setNumericValue(key, ((Number) value).doubleValue());
	  else
	    sd.setStringValue(key, value.toString());
	}
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to read '" + m_Input + "'!", e);
    }
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
    runReader(Environment.class, OpusSpectrumReader.class, args);
  }
}
