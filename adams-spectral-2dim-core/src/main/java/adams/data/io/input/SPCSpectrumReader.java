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
 * SPCSpectrumReader.java
 * Copyright (C) 2015-2021 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.DateFormat;
import adams.core.DateUtils;
import adams.core.LittleEndian;
import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.env.Environment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
<!-- globalinfo-start -->
* Reads spectra in SPC format (Galactic Universal Data Format) format.<br>
* See the specifications here:<br>
* http:&#47;&#47;serswiki.bme.gatech.edu&#47;images&#47;a&#47;ae&#47;SPC_Open_Specification.pdf
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
<!-- options-end -->
*
* @author  fracpete (fracpete at waikato dot ac dot nz)
*/
public class SPCSpectrumReader
  extends AbstractSpectrumReader {

  /** for serialization. */
  private static final long serialVersionUID = 7690015355854851867L;

  /**
   * Parser class for SPC files.
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   */
  public static abstract class AbstractParser
    implements Serializable {

    private static final long serialVersionUID = -1355694063938040510L;

    /** the parent parser, if any. */
    protected AbstractParser m_Parent;

    /** the binary data to analyze. */
    protected byte[] m_Data;

    /** the offset to use. */
    protected int m_Offset;

    /** the number of bytes read. */
    protected int m_BytesRead;

    /**
     * Initializes the parser.
     *
     * @param parent	the parent parser
     * @param data	the data to analyze
     * @param offset	the offset to use
     */
    protected AbstractParser(AbstractParser parent, byte[] data, int offset) {
      m_Parent    = parent;
      m_Data      = data;
      m_Offset    = offset;
      m_BytesRead = 0;
    }

    /**
     * Returns the parent parser if any.
     *
     * @return		the parser, null if not available
     */
    public AbstractParser getParent() {
      return m_Parent;
    }

    /**
     * Sums up the bytes.
     *
     * @param start	the start index
     * @param len	the number of bytes to sum up
     * @return		the sum
     */
    protected long sumBytes(int start, int len) {
      long	result;
      int	i;

      result = 0;
      for (i = 0; i < len; i++)
	result += m_Data[m_Offset + start + i];

      return result;
    }

    /**
     * Copies the bytes into a new array.
     *
     * @param start	the start index
     * @param len	the number of bytes to copy
     * @return		the new array
     */
    protected byte[] copyBytes(int start, int len) {
      byte[]	result;

      result = new byte[len];
      if (len > 0)
	System.arraycopy(m_Data, m_Offset + start, result, 0, len);

      return result;
    }

    /**
     * Parses the header data.
     *
     * @return 		null if successfully parsed, otherwise error message
     */
    protected abstract String parseHeader();

    /**
     * Parses the spectral data.
     *
     * @return 		null if successfully parsed, otherwise error message
     */
    protected abstract String parseData();

    /**
     * Parses the footer data (if any).
     *
     * @return 		null if successfully parsed, otherwise error message
     */
    protected String parseFooter() {
      return null;
    }

    /**
     * For post-processing the data.
     *
     * @return 		null if successfully parsed, otherwise error message
     */
    protected String postProcess() {
      return null;
    }

    /**
     * Parses the data.
     *
     * @return 		null if successfully parsed, otherwise error message
     */
    public String parse() {
      String	result;

      result = parseHeader();
      if (result == null)
	result = parseData();
      if (result == null)
	result = parseFooter();
      if (result == null)
	result = postProcess();

      return result;
    }

    /**
     * Returns the number of bytes that were read.
     *
     * @return		the number of bytes
     */
    public int getBytesRead() {
      return m_BytesRead;
    }
  }

  /**
   * Parser class for SPC files (header).
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 2242 $
   */
  public static class FileParser
    extends AbstractParser {

    private static final long serialVersionUID = 7683359869365677333L;

    /** the ID to use. */
    protected String m_ID;

    /** the parsed spectrum. */
    protected List<Spectrum> m_Spectra;

    /** whether blocks are in 16bit integer (0:0x01h). */
    protected boolean m_Blocks16bit;

    /** whether multi-file (0:0x04h). */
    protected boolean m_MultiFile;

    /** whether ordered (0:0x10h). */
    protected boolean m_Ordered;

    /** whether axis labeled stored (0:0x20h). */
    protected boolean m_AxisLabels;

    /** whether each subfile has unique X array (0:0x40h). */
    protected boolean m_UniqueXs;

    /** whether X data is non-evenly spaced (0:0x80h). */
    protected boolean m_NonEvenX;

    /** the version (1). */
    protected String m_Version;

    /** the instrument experiment technique (2). */
    protected String m_ExperimentType;

    /** the scaling exponent. */
    protected byte m_Exponent;

    /** whether Y values are stored in IEEE 32bit floats. */
    protected boolean m_YasIEEE32bit;

    /** the number of points. */
    protected int m_NumPoints;

    /** the value of the first X. */
    protected double m_FirstX;

    /** the value of the last X. */
    protected double m_LastX;

    /** the number of sub-files. */
    protected int m_NumFiles;

    /** the label of the X axis. */
    protected String m_XAxis;

    /** the label of the Y axis. */
    protected String m_YAxis;

    /** the label of the Z axis. */
    protected String m_ZAxis;

    /** the collection date. */
    protected Date m_CollectionDate;

    /** the resolution. */
    protected String m_Resolution;

    /** the source. */
    protected String m_Source;

    /** the peak point number. */
    protected int m_PeakPointNum;

    /** the comment. */
    protected String m_Comment;

    /** the offset for LOGSTC. */
    protected int m_OffsetLogstc;

    /** the multiple Z value increment. */
    protected float m_MultZInc;

    /** the number of W places in 4D data. */
    protected int m_NumWPlanes;

    /** the multiple W value increment. */
    protected float m_MultWInc;

    /** the label of the W axis. */
    protected String m_WAxis;

    /** the log values. */
    protected SampleData m_Log;

    /**
     * Initializes the parser.
     *
     * @param id	the ID to use
     * @param data 	the data to parse
     */
    public FileParser(String id, byte[] data) {
      super(null, data, 0);
      m_ID      = id;
      m_Spectra = new ArrayList<>();
      m_Log     = null;
    }

    /**
     * Determines the experiment type.
     *
     * @param data	the byte to inspect
     * @return		the type
     */
    protected String determineExperimentType(byte data) {
      String	result;

      switch (data) {
	case 0x00:
	  result = "General";
	  break;
	case 0x01:
	  result = "Gas Chromatogram";
	  break;
	case 0x02:
	  result = "General Chromatogram";
	  break;
	case 0x03:
	  result = "HPLC Chromatogram";
	  break;
	case 0x04:
	  result = "FT-IR, FT-NIR, FT-Raman Spectrum";
	  break;
	case 0x05:
	  result = "NIR Spectrum";
	  break;
	case 0x06:
	  result = "UV-VIS Spectrum";
	  break;
	case 0x07:
	  result = "Not defined";
	  break;
	case 0x08:
	  result = "X-ray Diffraction Spectrum";
	  break;
	case 0x09:
	  result = "Mass Spectrum";
	  break;
	case 0x0A:
	  result = "NMR Spectrum";
	  break;
	case 0x0B:
	  result = "Raman Spectrum";
	  break;
	case 0x0C:
	  result = "Fluorescence Spectrum";
	  break;
	case 0x0D:
	  result = "Atomic Spectrum";
	  break;
	case 0x0E:
	  result = "Chromatography Diode Array Spectra";
	  break;
	default:
	  result = "Unknown type; " + Utils.toHex(m_Data[2]);
      }

      return result;
    }

    /**
     * Determines the label type.
     *
     * @param data	the byte to inspect
     * @param axis	the axis to determine the label for (x,y,z)
     * @return		the type
     */
    protected String determineLabelType(byte data, char axis) {
      switch (axis) {
	case 'x':
	case 'z':
	case 'w':
	  switch (data) {
	    case 0: return "Arbitrary";
	    case 1: return "Wavenumber (cm-1)";
	    case 2: return "Micrometers";
	    case 3: return "Nanonmeters";
	    case 4: return "Seconds";
	    case 5: return "Minutes";
	    case 6: return "Hz";
	    case 7: return "KHz";
	    case 8: return "MHz";
	    case 9: return "Mass (M/z)";
	    case 10: return "ppm";
	    case 11: return "Days";
	    case 12: return "Years";
	    case 13: return "Raman shift (cm-1)";
	    case 14: return "eV";
	    case 16: return "Diode number";
	    case 17: return "Channel";
	    case 18: return "°";
	    case 19: return "°F";
	    case 20: return "°C";
	    case 21: return "°K";
	    case 22: return "Data Points";
	    case 23: return "msec";
	    case 24: return "μsec";
	    case 25: return "nsec";
	    case 26: return "GHz";
	    case 27: return "cm";
	    case 28: return "m";
	    case 29: return "mm";
	    case 30: return "Hours";
	    default: return "Unknown " + axis + " axis type: " + Utils.toHex(data);
	  }

	case 'y':
	  switch (data) {
	    case 0: return "Arbitrary Intensity";
	    case 1: return "Interferogram";
	    case 2: return "Absorbance";
	    case 3: return "Kubelka-Munk";
	    case 4: return "Counts";
	    case 5: return "V";
	    case 6: return "°";
	    case 7: return "mA";
	    case 8: return "mm";
	    case 9: return "mV";
	    case 10: return "Log (1/R)";
	    case 11: return "%";
	    case 12: return "Intensity";
	    case 13: return "Relative Intensity";
	    case 14: return "Energy";
	    case 15: return "*** not used ***";
	    case 16: return "dB";
	    case 17: return "*** not used ***";
	    case 18: return "*** not used ***";
	    case 19: return "°F";
	    case 20: return "°C";
	    case 21: return "°K";
	    case 22: return "Index of Refraction [N]";
	    case 23: return "Extinction Coeff. [K]";
	    case 24: return "Real";
	    case 25: return "Imaginary";
	    case 26: return "Complex";
	    case -128: return "Transmission";
	    case -127: return "Reflectance";
	    case -126: return "Arbitrary";
	    case -125: return "Emission";
	    default: return "Unknown " + axis + " axis type: " + Utils.toHex(data);
	  }

	default:
	  throw new IllegalStateException("Unhandled axis: " + axis);
      }
    }

    /**
     * Parses the header data.
     *
     * @return 		null if successfully parsed, otherwise error message
     */
    protected String parseHeader() {
      // byte 0: Ftflags (flags)
      m_Blocks16bit = LittleEndian.isBitSet(m_Data[0], 0x01);
      m_MultiFile   = LittleEndian.isBitSet(m_Data[0], 0x04);
      m_Ordered     = LittleEndian.isBitSet(m_Data[0], 0x10);
      m_AxisLabels  = LittleEndian.isBitSet(m_Data[0], 0x20);
      m_UniqueXs    = LittleEndian.isBitSet(m_Data[0], 0x40);
      m_NonEvenX    = LittleEndian.isBitSet(m_Data[0], 0x80);

      // byte 1: Fversn (version)
      m_Version = Utils.toHex(m_Data[1]);

      // byte 2: Fexper (instrumental experiment technique)
      m_ExperimentType = determineExperimentType(m_Data[2]);

      // byte 3: Fexp
      m_Exponent     = m_Data[3];
      m_YasIEEE32bit = LittleEndian.isBitSet(m_Exponent, 0x80);
      if (!m_YasIEEE32bit)
	return "Y values are not stored as IEEE 32bit floats!";

      // byte 4-7 (dword): Fnpts number of data points
      m_NumPoints = LittleEndian.bytesToInt(copyBytes(4, 4));

      // byte 8-15 (double): Ffirst value of first X
      m_FirstX = LittleEndian.bytesToDouble(copyBytes(8, 8));

      // byte 16-23 (double): Flast value of last X
      m_LastX = LittleEndian.bytesToDouble(copyBytes(16, 8));

      // byte 24-27: Fnsub the number of sub files
      m_NumFiles = LittleEndian.bytesToInt(copyBytes(24, 4));

      // byte 28: Fxtype the X axis label
      m_XAxis = determineLabelType(m_Data[28], 'x');

      // byte 29: Fxtype the X axis label
      m_YAxis = determineLabelType(m_Data[29], 'y');

      // byte 30: Fxtype the X axis label
      m_ZAxis = determineLabelType(m_Data[30], 'z');

      // byte 32-35: Fdate file collection date/time (y=12,M=4,d=5,H=5,m=6)
      m_CollectionDate = null;
      if (sumBytes(32, 4) > 0) {
	String bits = Integer.toBinaryString(LittleEndian.bytesToInt(copyBytes(32, 4)));
	Calendar cal = new GregorianCalendar();
	cal.set(Calendar.YEAR, Integer.parseInt(bits.substring(0, 12), 2));
	cal.set(Calendar.MONTH, Integer.parseInt(bits.substring(12, 16), 2));
	cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(bits.substring(16, 21), 2));
	cal.set(Calendar.HOUR, Integer.parseInt(bits.substring(21, 26), 2));
	cal.set(Calendar.MINUTE, Integer.parseInt(bits.substring(26), 2));
	m_CollectionDate = cal.getTime();
      }

      // byte 36-44: Fres the resolution description
      m_Resolution = LittleEndian.bytesToString(copyBytes(36, 9));

      // byte 45-53: Fsource the source instrument
      m_Source = LittleEndian.bytesToString(copyBytes(45, 9));

      // byte 54-55: Fpeakpt interferogram peak points number
      m_PeakPointNum = LittleEndian.bytesToShort(copyBytes(54, 2));

      // byte 88-217: Fcmnt memo/comment
      m_Comment = LittleEndian.bytesToString(copyBytes(88, 130));

      // byte 218-247: Fcatxt custom axis labels (separated by NUL)
      if (m_AxisLabels) {
	int start = 218;
	int axis = 0;
	int len = 0;
	for (int i = 0; i < 30; i++) {
	  if (m_Data[218 + i] == 0) {
	    switch (axis) {
	      case 0:
		m_XAxis = LittleEndian.bytesToString(copyBytes(start, len));
		break;
	      case 1:
		m_YAxis = LittleEndian.bytesToString(copyBytes(start, len));
		break;
	      case 2:
		m_ZAxis = LittleEndian.bytesToString(copyBytes(start, len));
		break;
	    }
	    start += len + 1;
	    len = 0;
	    axis++;
	    continue;
	  }
	  len++;
	}
      }

      // byte 248-251: Flogoff offset to LOGSTC
      m_OffsetLogstc = LittleEndian.bytesToInt(copyBytes(248, 4));

      // byte 312-315: Fzinc multiple z value subfile increment
      m_MultZInc = LittleEndian.bytesToFloat(copyBytes(312, 4));

      // byte 316-319: Fwplanes 4D data number W planes
      m_NumWPlanes = LittleEndian.bytesToInt(copyBytes(316, 4));

      // byte 320-323: Fwinc multiple w value subfile increment
      m_MultWInc = LittleEndian.bytesToFloat(copyBytes(320, 4));

      // byte 324: Fxtype the W axis label
      m_WAxis = determineLabelType(m_Data[324], 'w');

      m_BytesRead = 512;

      return null;
    }

    /**
     * Parses the spectral data.
     *
     * @return 		null if successfully parsed, otherwise error message
     */
    protected String parseData() {
      String 		result;
      int		i;
      SubFileParser	parser;
      Spectrum		sp;

      result = null;

      for (i = 0; i < m_NumFiles; i++) {
	parser = new SubFileParser(this, m_Data, m_BytesRead, m_UniqueXs && m_NonEvenX);
	result = parser.parse();
	if (result != null)
	  break;
	sp = parser.getSpectrum();
	if (m_NumFiles > 1)
	  sp.setID(m_ID + "-" + i);
	else
	  sp.setID(m_ID);
	m_Spectra.add(sp);
	m_BytesRead += parser.getBytesRead();
      }

      return result;
    }

    /**
     * Parses the footer data (if any).
     *
     * @return 		null if successfully parsed, otherwise error message
     */
    protected String parseFooter() {
      if (m_OffsetLogstc == 0)
	return null;

      // byte 0-3: Logsizd byte size of log disk block
      int logsizd = LittleEndian.bytesToInt(copyBytes(m_OffsetLogstc + 0, 4));

      // byte 4-7: Logsizm byte size of log memory block
      //int logsizm = LittleEndian.bytesToInt(copyBytes(m_OffsetLogstc + 4, 4));

      // byte 8-11: Logtxto byte offset to Log Text data
      int logtxto = LittleEndian.bytesToInt(copyBytes(m_OffsetLogstc + 8, 4));

      // log information
      String log = LittleEndian.bytesToString(copyBytes(m_OffsetLogstc + logtxto, logsizd - logtxto - 1)).trim();
      if (log.length() > 0) {
	m_Log = new SampleData();
	String[] lines = Utils.split(log, "\r\n");
	for (String line : lines) {
	  String[] parts = line.split(" = ");
	  if (parts.length == 2) {
	    DataType type;
	    switch (parts[0].trim()) {
	      case "BEGX":
	      case "ENDX":
	      case "NPTS":
	      case "BEGZ":
	      case "ENDZ":
	      case "NSUBS":
	      case "SCANS":
	      case "SCANSBG":
	      case "GAIN":
	      case "VELOCITY":
	      case "LWN":
	      case "RAMANFREQ":
	      case "RAMANPWR":
	      case "JSTOP":
	      case "BSTOP":
	      case "PURGE":
	      case "ZFF":
	      case "PHASEPTS":
	      case "POLARIZER":
	      case "LOWPASS":
	      case "HIGHPASS":
	      case "SMOOTH":
	      case "AVGTIME":
	      case "BDELAY":
	      case "SDELAY":
	      case "CHANNEL":
	      case "NODE":
	      case "SENSORCNT":
	      case "SLIT1":
	      case "SLIT2":
	      case "PMT":
	      case "DETCHG":
	      case "DETCOR":
	      case "SRCCHG":
	      case "INDEPENDENT":
	      case "SIGNOISE":
	      case "SNLEVEL":
	      case "SNTIMEOUT":
	      case "NIR_RES":
	      case "NIR_AVERAGING":
	      case "NIR_SBW":
	      case "NIR_ENERGY":
	      case "NIR_SLITHT":
	      case "CORRECTION":
	      case "NUCFREQ":
	      case "SW_HZ":
	      case "DWELL":
	      case "DELAY":
	      case "ACQTIME":
	      case "REQSCANS":
	      case "FLTFREQ":
	      case "PULSWD":
	      case "SW_PPM":
	      case "DCOFF":
	      case "APODP(0)":
	      case "APODP(1)":
	      case "PH0":
	      case "PH1":
	      case "PV0":
	      case "PV1":
	      case "DELTA_PPM":
	      case "NORM":
	      case "THRESH":
	      case "SENS":
	      case "PICK_TYPE":
	      case "WINDOW":
	      case "PK_LABEL":
	      case "LB_FORM":
	      case "TR_OBJ":
		type = DataType.NUMERIC;
		break;
	      case "AB_APPS":
	      case "FID":
	      case "SPC_REAL":
	      case "SPC_REV":
	      case "INTBAS":
	      case "INTOFF":
	      case "BASE":
	      case "MASK_ON":
		type = DataType.BOOLEAN;
		break;
	      case "XTYPE":
		type = DataType.STRING;
		parts[1] = determineLabelType(Byte.parseByte(parts[1]), 'x');
		break;
	      case "YTYPE":
		type = DataType.STRING;
		parts[1] = determineLabelType(Byte.parseByte(parts[1]), 'y');
		break;
	      case "ZTYPE":
		type = DataType.STRING;
		parts[1] = determineLabelType(Byte.parseByte(parts[1]), 'z');
		break;
	      case "IRMODE":
		type = DataType.STRING;
		switch (parts[1].trim()) {
		  case "2":
		    parts[1] = "Mid-IR mode";
		    break;
		  case "1":
		    parts[1] = "Near-IR mode";
		    break;
		  case "-2":
		    parts[1] = "Raman shift";
		    break;
		}
		break;
	      default:
		type = DataType.STRING;
	    }
	    Field field = new Field("Log." + parts[0].trim(), type);
	    try {
	      m_Log.addField(field);
	      m_Log.setValue(field, parts[1].trim());
	    }
	    catch (Exception e) {
	      field = new Field("Log." + parts[0].trim(), DataType.STRING);
	      m_Log.addField(field);
	      m_Log.setValue(field, parts[1].trim());
	    }
	  }
	}
      }

      return null;
    }

    /**
     * For post-processing the data.
     *
     * @return 		null if successfully parsed, otherwise error message
     */
    @Override
    protected String postProcess() {
      String		result;
      DateFormat 	df;

      result = super.postProcess();

      if (result == null) {
	df = DateUtils.getTimestampFormatter();
	for (Spectrum sp: m_Spectra) {
	  // add some meta data
	  SampleData report = sp.getReport();
	  Field field;
	  if (getNumFiles() == 1) {
	    // first X
	    field = new Field("First X", DataType.NUMERIC);
	    report.addField(field);
	    report.setValue(field, getFirstX());
	    // last X
	    field = new Field("Last X", DataType.NUMERIC);
	    report.addField(field);
	    report.setValue(field, getLastX());
	    // num points
	    field = new Field("Num Points", DataType.NUMERIC);
	    report.addField(field);
	    report.setValue(field, getNumPoints());
	  }
	  // multi-file
	  field = new Field("Multi File", DataType.BOOLEAN);
	  report.addField(field);
	  report.setValue(field, m_MultiFile);
	  // comment
	  field = new Field("Comment", DataType.STRING);
	  report.addField(field);
	  report.setValue(field, m_Comment);
	  // collection date
	  if (m_CollectionDate != null) {
	    field = new Field("Collection Date", DataType.STRING);
	    report.addField(field);
	    report.setValue(field, df.format(m_CollectionDate));
	  }
	  // peak point num
	  field = new Field("Peak Point Num", DataType.NUMERIC);
	  report.addField(field);
	  report.setValue(field, m_PeakPointNum);
	  // experiment type
	  field = new Field("Experiment type", DataType.STRING);
	  report.addField(field);
	  report.setValue(field, m_ExperimentType);
	  // resolution
	  field = new Field("Resolution", DataType.STRING);
	  report.addField(field);
	  report.setValue(field, m_Resolution);
	  // source
	  field = new Field(SampleData.INSTRUMENT, DataType.STRING);
	  report.addField(field);
	  report.setValue(field, m_Source);
	  // X axis
	  field = new Field("X Axis", DataType.STRING);
	  report.addField(field);
	  report.setValue(field, m_XAxis);
	  // Y axis
	  field = new Field("Y Axis", DataType.STRING);
	  report.addField(field);
	  report.setValue(field, m_YAxis);
	  // Z axis
	  field = new Field("Z Axis", DataType.STRING);
	  report.addField(field);
	  report.setValue(field, m_ZAxis);
	  // W axis
	  field = new Field("W Axis", DataType.STRING);
	  report.addField(field);
	  report.setValue(field, m_WAxis);
	  // Version
	  field = new Field("Version", DataType.STRING);
	  report.addField(field);
	  report.setValue(field, m_Version);
	  // add log info
	  sp.getReport().mergeWith(m_Log);
	}
      }

      return result;
    }

    /**
     * Returns the parsed spectra.
     *
     * @return		the spectra
     */
    public List<Spectrum> getSpectra() {
      return m_Spectra;
    }

    /**
     * Returns the number of points to read.
     *
     * @return		the number of points
     */
    public int getNumPoints() {
      return m_NumPoints;
    }

    /**
     * Returns the X of the first value.
     *
     * @return		the first X
     */
    public double getFirstX() {
      return m_FirstX;
    }

    /**
     * Returns the X of the last value.
     *
     * @return		the last X
     */
    public double getLastX() {
      return m_LastX;
    }

    /**
     * Returns the number of sub-files.
     *
     * @return		the number of files
     */
    public int getNumFiles() {
      return m_NumFiles;
    }
  }

  /**
   * Parser class for SPC files (header).
   *
   * @author  fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 2242 $
   */
  public static class SubFileParser
    extends AbstractParser {

    private static final long serialVersionUID = -8427267207596749310L;

    /** whether to use the offset points directory. */
    protected boolean m_OffsetPointersDir;

    /** the scaling exponent. */
    protected byte m_Exponent;

    /** whether Y values are stored in IEEE 32bit floats. */
    protected boolean m_YasIEEE32bit;

    /** the subfile index. */
    protected int m_Index;

    /** the Z axis value. */
    protected float m_ZAxis;

    /** the Z axis end value. */
    protected float m_ZAxisEnd;

    /** the peak picking noise level. */
    protected float m_Noise;

    /** the number of points. */
    protected int m_NumPoints;

    /** the number of scans. */
    protected int m_NumScans;

    /** the W axis value. */
    protected float m_WAxis;

    /** the parsed spectrum. */
    protected Spectrum m_Spectrum;

    /**
     * Initializes the parser.
     *
     * @param parent	the parent parser
     * @param data 	the data to parse
     * @param offset	the offset to use
     * @param offsetPointersDir	whether to use the offset pointers directory
     */
    public SubFileParser(AbstractParser parent, byte[] data, int offset, boolean offsetPointersDir) {
      super(parent, data, offset);
      m_OffsetPointersDir = offsetPointersDir;
      m_Spectrum = new Spectrum();
      m_Spectrum.setReport(new SampleData());
    }

    /**
     * Parses the header data.
     *
     * @return 		null if successfully parsed, otherwise error message
     */
    @Override
    protected String parseHeader() {
      // byte 1: Subexp
      m_Exponent = m_Data[m_Offset + 1];

      m_YasIEEE32bit = ((m_Exponent & 0x80) > 0);
      if (!m_YasIEEE32bit)
	return "Y values are not stored as IEEE 32bit floats!";

      // byte 2-3: Subindx the subfile index
      m_Index = LittleEndian.bytesToShort(copyBytes(2, 2));

      // byte 4-7: Subtime the Z Axis value
      m_ZAxis = LittleEndian.bytesToFloat(copyBytes(4, 4));

      // byte 8-11: Subtime the Z Axis end value
      m_ZAxisEnd = LittleEndian.bytesToFloat(copyBytes(8, 4));

      // byte 12-15: Subnois the peak picking noise level
      m_Noise = LittleEndian.bytesToFloat(copyBytes(12, 4));

      // byte 16-19: Subnpts the number of points
      m_NumPoints = LittleEndian.bytesToInt(copyBytes(16, 4));

      // byte 20-23: Subscan the number of scans
      m_NumScans = LittleEndian.bytesToInt(copyBytes(20, 4));

      // byte 24-27: Subwlevel the W axis value
      m_WAxis = LittleEndian.bytesToFloat(copyBytes(24, 4));

      m_BytesRead += 32;

      return null;
    }

    /**
     * Parses the spectral data.
     *
     * @return 		null if successfully parsed, otherwise error message
     */
    @Override
    protected String parseData() {
      if (m_OffsetPointersDir)
	return "Cannot handle variable length files (SSFTC structure)!";

      int numPoints = (m_NumPoints > 0) ? m_NumPoints : ((FileParser) m_Parent).getNumPoints();
      double first = ((FileParser) m_Parent).getFirstX();
      double last = ((FileParser) m_Parent).getLastX();
      for (int i = 0; i < numPoints; i++) {
	float current = (float) (first + (last - first) / (numPoints - 1) * i);
	float ampl = LittleEndian.bytesToFloat(copyBytes(m_BytesRead, 4));
	m_Spectrum.add(new SpectrumPoint(current, ampl));
	m_BytesRead += 4;
      }

      return null;
    }

    /**
     * For post-processing the data.
     *
     * @return 		null if successfully parsed, otherwise error message
     */
    @Override
    protected String postProcess() {
      String		result;

      result = super.postProcess();

      if (result == null) {
	// add some meta data
	SampleData report = m_Spectrum.getReport();
	Field field;
	// Index
	field = new Field("SubFile Index", DataType.NUMERIC);
	report.addField(field);
	report.setValue(field, m_Index);
      }

      return result;
    }

    /**
     * Returns the parsed spectrum.
     *
     * @return		the spectrum
     */
    public Spectrum getSpectrum() {
      return m_Spectrum;
    }
  }

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return
      "Reads spectra in SPC format (Galactic Universal Data Format) format.\n"
	+ "See the specifications here:\n"
	+ "http://serswiki.bme.gatech.edu/images/a/ae/SPC_Open_Specification.pdf";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  public String getFormatDescription() {
    return "Galactic SPC Format";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension(s) (without the dot!)
   */
  public String[] getFormatExtensions() {
    return new String[]{"spc"};
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    byte[] 	data;
    FileParser parser;
    String	msg;

    data   = FileUtils.loadFromBinaryFile(m_Input);
    parser = new FileParser(FileUtils.replaceExtension(m_Input.getName(), ""), data);
    msg    = parser.parse();
    if (msg == null)
      m_ReadData.addAll(parser.getSpectra());
    else
      getLogger().severe("Failed to parse '" + m_Input + "': " + msg);
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
    runReader(Environment.class, SPCSpectrumReader.class, args);
  }
}
