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
 * ASCSpectrumWriter.java
 * Copyright (C) 2010-2013 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.output;

import adams.core.management.LocaleHelper;
import adams.core.management.OptionHandlingLocaleSupporter;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.Report;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spectrum.SpectrumPointComparator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
<!-- globalinfo-start -->
* Writer that stores spectra in the BLGG ASC format.
* <br><br>
<!-- globalinfo-end -->
*
<!-- options-start -->
* Valid options are: <br><br>
*
* <pre>-D &lt;int&gt; (property: debugLevel)
* &nbsp;&nbsp;&nbsp;The greater the number the more additional info the scheme may output to
* &nbsp;&nbsp;&nbsp;the console (0 = off).
* &nbsp;&nbsp;&nbsp;default: 0
* &nbsp;&nbsp;&nbsp;minimum: 0
* </pre>
*
* <pre>-output &lt;adams.core.io.PlaceholderFile&gt; (property: output)
* &nbsp;&nbsp;&nbsp;The file to write the container to.
* &nbsp;&nbsp;&nbsp;default: ${TMP}/out.tmp
* </pre>
*
* <pre>-instrumentName &lt;java.lang.String&gt; (property: instrumentName)
* &nbsp;&nbsp;&nbsp;Instrument Name to be used in ASC header.
* &nbsp;&nbsp;&nbsp;default: &lt;not implemented&gt;
* </pre>
*
* <pre>-accessoryName &lt;java.lang.String&gt; (property: accessoryName)
* &nbsp;&nbsp;&nbsp;Accessory Name to be used in ASC header.
* &nbsp;&nbsp;&nbsp;default: ABB-BOMEM MB160D
* </pre>
*
* <pre>-productCode &lt;java.lang.String&gt; (property: productCode)
* &nbsp;&nbsp;&nbsp;Either the attribute name with the product code in it, or the actual product
* &nbsp;&nbsp;&nbsp;code to be used.
* &nbsp;&nbsp;&nbsp;default: 01
* </pre>
*
* <pre>-productCodeFromField (property: productCodeFromField)
* &nbsp;&nbsp;&nbsp;Regex to find sample id. e.g 'sample_id'
* </pre>
*
* <pre>-dataPoints &lt;int&gt; (property: dataPoints)
* &nbsp;&nbsp;&nbsp;Number of data points. -1 means use as many as in spectrum
* &nbsp;&nbsp;&nbsp;default: -1
* </pre>
*
* <pre>-firstXPoint &lt;java.lang.Double&gt; (property: firstXPoint)
* &nbsp;&nbsp;&nbsp;First wavenumber
* &nbsp;&nbsp;&nbsp;default: 3749.3428948242
* </pre>
*
* <pre>-lastXPoint &lt;java.lang.Double&gt; (property: lastXPoint)
* &nbsp;&nbsp;&nbsp;Last wavenumber
* &nbsp;&nbsp;&nbsp;default: 9998.2477195313
* </pre>
*
<!-- options-end -->
*
* @author  dale (dale at waikato dot ac dot nz)
* @version $Revision: 2242 $
*/
public class ASCSpectrumWriter
  extends AbstractSpectrumWriter
  implements OptionHandlingLocaleSupporter {

  /** for serialization. */
  private static final long serialVersionUID = 208155740775061862L;

  /** The instrument name to put in ASC header. */
  protected String m_InstrumentName;

  /** The Accessory Name to put in ASC header. */
  protected String m_AccessoryName;

  /** Either the Field name with the product code in it, or the actual product code. Decided by m_GetProductCodeFromAttribute. */
  protected String m_ProductCode;

  /** Get the product code from the Field named ProductCode in the Report? Otherwise use the ProductCode string directly. */
  protected Boolean m_ProductCodeFromField;

  /** If -1 then calculate Nr of Datapoints, otherwise as specified, and fail if the number differs. */
  protected int m_NrDatapoints;

  /** First X WaveNumber. */
  protected Double m_FirstXPoint;

  /** Last X WaveNumber. */
  protected Double m_LastXPoint;
  
  /** whether to output the sample data as well. */
  protected boolean m_Descending;

  /** the locale to use. */
  protected Locale m_Locale;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "instrumentName", "instrumentName",
	    "<not implemented>");

    m_OptionManager.add(
	    "accessoryName", "accessoryName",
	    "ABB-BOMEM MB160D");

    m_OptionManager.add(
	    "productCode", "productCode",
	    "01");

    m_OptionManager.add(
	    "productCodeFromField", "productCodeFromField",
	    false);

    m_OptionManager.add(
	    "dataPoints", "dataPoints",
	    -1);

    m_OptionManager.add(
	    "firstXPoint", "firstXPoint",
	    3749.3428948242);

    m_OptionManager.add(
	    "lastXPoint", "lastXPoint",
	    9998.2477195313);
    
    m_OptionManager.add(
	"descending", "descending",
	false);

    m_OptionManager.add(
	    "locale", "locale",
	    LocaleHelper.getSingleton().getEnUS());
  }

  /**
   * Get number of data points. -1 means use as many as in spectrum.
   * @return number of data points
   */
  public int getDataPoints() {
    return m_NrDatapoints;
  }

  /**
   * Set number of data points. -1 means use as many as in spectrum.
   * @param mNrDatapoints number of data points
   */
  public void setDataPoints(int mNrDatapoints) {
    m_NrDatapoints = mNrDatapoints;
    reset();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String dataPointsTipText() {
    return "Number of data points. -1 means use as many as in spectrum";
  }

  /**
   * Get first X wavenumber.
   *
   * @return first X wavenumber
   */
  public Double getFirstXPoint() {
    return m_FirstXPoint;
  }

  /**
   * Set first X wavenumber.
   *
   * @param mFirstXPoint X wavenumber
   */
  public void setFirstXPoint(Double mFirstXPoint) {
    m_FirstXPoint = mFirstXPoint;
    reset();
  }
  
  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String firstXPointTipText() {
    return "First wavenumber";
  }

  /**
   * Get last X wavenumber.
   *
   * @return last X wavenumber
   */
  public Double getLastXPoint() {
    return m_LastXPoint;
  }

  /**
   * Set last X wavenumber.
   *
   * @param mLastXPoint X wavenumber
   */
  public void setLastXPoint(Double mLastXPoint) {
    m_LastXPoint = mLastXPoint;
    reset();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String lastXPointTipText() {
    return "Last wavenumber";
  }


  /**
   * Get product code from instance attribute? Or use as string.
   * @return Get product code from instance attribute?
   */
  public Boolean getProductCodeFromField() {
    return m_ProductCodeFromField;
  }

  /**
   * Set product code from instance attribute? Or use as string.
   * @param mGetProductCodeFromAttribute Set product code from instance attribute?
   */
  public void setProductCodeFromField( Boolean mGetProductCodeFromAttribute) {
    m_ProductCodeFromField = mGetProductCodeFromAttribute;
    reset();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String productCodeFromFieldTipText() {
    return "Regex to find sample id. e.g 'sample_id'";
  }


  /**
   * Get Product Code.
   * @return	product code
   */
  public String getProductCode(){
    return(m_ProductCode);
  }

  /**
   * Set product code.
   * @param productCode product code.
   */
  public void setProductCode(String productCode){
    m_ProductCode=productCode;
    reset();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String productCodeTipText() {
    return "Either the attribute name with the product code in it, or the actual product code to be used. ";
  }


  /**
   * Get accessory name.
   * @return accessory name.
   */
  public String getAccessoryName() {
    return m_AccessoryName;
  }

  /**
   * Set accessory name.
   *
   * @param mAccessoryName accessory name.
   */
  public void setAccessoryName(String mAccessoryName) {
    m_AccessoryName = mAccessoryName;
    reset();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String accessoryNameTipText() {
    return "Accessory Name to be used in ASC header.";
  }

  /**
   * Get instrument name.
   *
   * @return instrument name
   */
  public String getInstrumentName() {
    return m_InstrumentName;
  }

  /**
   * Set instrument name.
   *
   * @param mInstrumentName	instrument name
   */
  public void setInstrumentName(String mInstrumentName) {
    m_InstrumentName = mInstrumentName;
    reset();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String instrumentNameTipText() {
    return "Instrument Name to be used in ASC header.";
  }

  
  /**
   * Sets whether to output spectrum points by descending x-axis.
   *
   * @param value	if true then the output descending x-axis
   */
  public void setDescending(boolean value) {
    m_Descending = value;
    reset();
  }

  /**
   * Returns whether to output spectrum points by descending x-axis.
   *
   * @return		true if output descending x-axis
   */
  public boolean getDescending() {
    return m_Descending;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String descendingTipText() {
    return "If set to true, the spectrum is output in descending x-axis order.";
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
    return "The locale to use for formatting the numbers.";
  }

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Writer that stores spectra in the BLGG ASC format.";
  }


  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the file chooser
   */
  @Override
  public String getFormatDescription() {
    return "BLGG ASCformat";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 		the extension(s) (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"asc"};
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();
    m_OutputIsFile = true;
  }

  /**
   * Generate the ASC String to output.
   *
   * @param data	the data to write
   * @return 		ASC file as string.
   */
  protected String genASCString(Spectrum data){
    String comment = "## ";

    StringBuilder ret = new StringBuilder();
    ret.append(comment);
    ret.append("Instrument Name = "+m_InstrumentName+"\n");
    ret.append(comment+"Accessory Name = "+m_AccessoryName+"\n");
    ret.append(comment+"Product Name = ");
    String pcode=m_ProductCode;
    if (m_ProductCodeFromField){
      Report report=data.getReport();
      if (report==null){
	pcode="<Report Not Available>";
      } else {
	String pcodefield = null;
	if (report.hasValue(m_ProductCode))
	  pcodefield = report.getStringValue(new Field(m_ProductCode, DataType.STRING));
	if (pcodefield == null){
	  pcode="<Field '"+m_ProductCode+"'Not Available in Report>";
	} else {
	  pcode = pcodefield;
	}
      }
    }
    String id=data.getID().replace("'", "");
    ret.append(pcode+"\n");
    ret.append(comment+"Sample ID = "+id+"\n");
    ret.append(comment+"Nr of data points = ");
    int points=m_NrDatapoints;
    if (m_NrDatapoints == -1) {
      ret.append(data.size()+"\n");
      points=data.size();
    } else if (data.size() != m_NrDatapoints){
      getLogger().severe("Spectrum does not have the expected number of points:"+data.size()+" != "+m_NrDatapoints);
      return(null); // error, as the number of points does not match.
    } else {
      ret.append(m_NrDatapoints+"\n");
    }
    ret.append(comment+"First X Point = "+m_FirstXPoint+"\n");
    ret.append(comment+"Last X Point = "+m_LastXPoint+"\n");
    ret.append(comment+"Wave number - Absorbance value"+"\n");

    NumberFormat nf=LocaleHelper.getSingleton().getNumberFormat(getLocale());
    nf.setMaximumFractionDigits(10);
    nf.setMinimumFractionDigits(10);
    
    List<SpectrumPoint> vsp=data.toList(new SpectrumPointComparator(false,!getDescending()));

    double currwn=m_FirstXPoint;
    double diff=(m_LastXPoint-m_FirstXPoint)/(double)(points-1);
    for (int i=0;i<points;i++){
      SpectrumPoint sp=vsp.get(i);
      ret.append(nf.format(currwn)+" "+nf.format(sp.getAmplitude())+"\n");
      currwn+=diff;
    }

    return(ret.toString());
  }

  /**
   * Writer can only write single spectra.
   *
   * @param data	the data to write
   */
  @Override
  protected void checkData(List<Spectrum> data) {
    super.checkData(data);

    if (data.size() != 1)
      throw new IllegalArgumentException(
	  "Writer can only write exactly 1 spectrum at a time!");
  }

  /**
   * Performs the actual writing.
   *
   * @param data	the data to write
   * @return		true if successfully written
   */
  @Override
  protected boolean writeData(List<Spectrum> data) {
    boolean		result;
    String 		asc;
    BufferedWriter	writer;

    result = false;

    asc = genASCString(data.get(0));
    if (asc != null){
      try {
	writer = new BufferedWriter(new FileWriter(m_Output.getAbsolutePath()));
	writer.write(asc);
	writer.close();
	result = true;
      }
      catch (Exception e) {
	e.printStackTrace();
	result = false;
      }
    }

    return result;

  }
}
