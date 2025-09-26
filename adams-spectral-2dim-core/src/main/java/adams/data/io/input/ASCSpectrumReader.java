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
 * ASCSpectrumReader.java
 * Copyright (C) 2009-2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.core.management.LocaleHelper;
import adams.core.management.LocaleSupporter;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.env.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Reads spectra in BLGG ASC format.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-input &lt;java.io.File&gt; (property: input)
 *         The file to read and turn into a spectrum.
 *         default: .
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class ASCSpectrumReader
  extends AbstractTextBasedSpectrumReader
  implements LocaleSupporter, StreamableTextBasedDataContainerReader<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -27209265703137172L;

  /** the locale to use. */
  protected Locale m_Locale;

  /**
   * Class for parsing an ASC file.
   */
  protected class ParsedFile {

    /** Name->Value, String->String. ie "Sample ID" -> "994370" */
    protected Hashtable<String,String> m_ht = new Hashtable<>();

    /** vector of 2d double (double[2]) wavenumber,absorbance. */
    protected List m_dp = new ArrayList();

    /** the last error that occurred. */
    protected String lastError = "";

    /** whether to force replacing comma with point. */
    protected boolean m_ForceCommaToPoint;

    /**
     * Returns the last error that occurred.
     *
     * @return		the last error
     */
    public String getLastError(){
      return lastError;
    }

    /**
     * Returns the type.
     *
     * @return		the type
     */
    public String getType() {
      return (String) m_ht.get("Product Name");
    }

    /**
     * Returns the ID.
     *
     * @return		the ID
     */
    public String getID() {
      return (String) m_ht.get("Sample ID");
    }

    /**
     * Returns the number of data points.
     *
     * @return		the number of data points, or null if it can't be parsed
     */
    public Integer getNumDatapoints() {
      String number = m_ht.get("Nr of data points");
      try {
	return Integer.parseInt(number);
      }
      catch (Exception e) {
	return null;
      }
    }

    /**
     * Returns the Wave number array.
     *
     * @return		the array
     */
    public double[] getWaveNumberArray(){
      double[] d = new double[m_dp.size()];

      for (int i = 0; i < d.length;i++){
	double[] db = (double[]) m_dp.get(i);
	d[i] = db[0];
      }

      return d;
    }

    /**
     * get hashtable property data.
     *
     * @return		the hashtable
     */
    public Hashtable<String,String> getProperties(){
      return m_ht;
    }

    /**
     * Returns the NIR array.
     *
     * @return		the array
     */
    public double[] getNIRArray(){
      double[] d = new double[m_dp.size()];

      for (int i = 0; i < d.length;i++){
	double[] db = (double[]) m_dp.get(i);
	d[i] = db[1];
      }

      return d;
    }

    /**
     * Parses the given file.
     *
     * @param afile	the file to parse
     * @return		true if successfully parsed
     */
    public boolean parse(File afile){
      BufferedReader br = null;
      StringBuilder buf;
      try {
	br  = new BufferedReader(new FileReader(afile.getAbsolutePath()));
	buf = new StringBuilder();
	while (br.ready())
	  buf.append((char) br.read());
	br.close();
	return parse(buf.toString());
      }
      catch (Exception e) {
	FileUtils.closeQuietly(br);
	return false;
      }
    }

    /**
     * Parses the given string.
     *
     * @param in	the string to parse
     * @return		true if successfully parsed
     */
    public boolean parse(String in) {
      m_ForceCommaToPoint = getLocale().equals(LocaleHelper.getSingleton().getEnUS());
      String[] lines = in.split("\\n");
      boolean processingHeader=true;
      for (int i = 0; i < lines.length; i++){
	String line = lines[i];
	if (line.startsWith("##")) {
	  if (processingHeader) {
	    line = line.substring(2).trim(); // remove "##"
	    String[] vals = line.trim().split("=");
	    if (vals.length != 2) {
	      continue;
	    }
	    m_ht.put(vals[0].trim(),vals[1].trim());
	  }
	  else {
	    lastError = "Found Header Line inside Data";
	    return false;
	  }
	}
	else {
	  processingHeader = false;
	  String[] vals = line.trim().split("\\s");
	  double[] d = new double[2];
	  if (vals.length != 2) {
	    lastError = "Data line corrupt:" + line + " split into:" + vals.length;
	    for (int j = 0; j < vals.length; j++){
	      lastError += " (" + vals[j] + ")";
	    }
	    return false;
	  }
	  try {
	    if (m_ForceCommaToPoint) {
	      vals[0] = vals[0].replace(",", ".");
	      vals[1] = vals[1].replace(",", ".");
	    }
	    NumberFormat nf = LocaleHelper.getSingleton().getNumberFormat(getLocale());
	    nf.setMaximumFractionDigits(4);
	    Number n = nf.parse(vals[0]);
	    d[0] = n.doubleValue();
	    n = nf.parse(vals[1]);
	    d[1] = n.doubleValue();
	  }
	  catch(Exception e) {
	    lastError = "Data line corrupt: " + line;
	    getLogger().log(Level.SEVERE, lastError, e);
	    return false;
	  }
	  m_dp.add(d);
	}
      }
      return true;
    }
  }

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads spectra in BLGG ASC format.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "locale", "locale",
	    LocaleHelper.getSingleton().getEnUS());
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "BLGG ASC Format";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"asc"};
  }

  /**
   * Sets the locale to use.
   *
   * @param value	the locale
   */
  public void setLocale(Locale value) {
    m_Locale = value;
    reset();
  }

  /**
   * Returns the locale in use.
   *
   * @return 		the locale
   */
  public Locale getLocale() {
    return m_Locale;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String localeTipText() {
    return "The locale to use for parsing the numbers.";
  }

  /**
   * Turns the key into a proper SampleData constant.
   *
   * @param key		the key to transform if necessary
   * @return		the fixed key
   */
  protected String fixKey(String key) {
    String	result;

    result = key;

    if (key.equals("SampleType"))
      result = SampleData.SAMPLE_TYPE;
    else if (key.equals("Product Name"))
      result = SampleData.SAMPLE_TYPE;

    return result;
  }

  /**
   * Performs the actual reading.
   *
   * @param content 	the content to read from
   */
  @Override
  protected void readData(List<String> content) {
    Spectrum	sp;
    ParsedFile 	pf;
    double[] 	nir;
    double[] 	wave;
    int 	i;

    sp = new Spectrum();
    pf = new ParsedFile();
    pf.parse(Utils.flatten(content, "\n"));

    // NIR array
    nir = pf.getNIRArray();
    if (nir == null || nir.length==0){
      throw new IllegalStateException("No spectral data loaded from file.");

    }
    if (pf.getNumDatapoints() != nir.length){
      throw new IllegalStateException("Mismatched wavenumber length. Expected "+pf.getNumDatapoints()+ ", read "+nir.length);
    }
    // wave numbers
    wave = pf.getWaveNumberArray();

    sp.setID(pf.getID());
    for (i = 0; i < nir.length; i++) {
      sp.add(new SpectrumPoint((float) wave[i], (float) nir[i]));
    }
    SampleData sd=new SampleData();
    Hashtable<String, String> props=pf.getProperties();
    for (String key:props.keySet()) {
      sd.addParameter(fixKey(key), props.get(key));
    }
    sp.setReport(sd);

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
    runReader(Environment.class, ASCSpectrumReader.class, args);
  }
}
