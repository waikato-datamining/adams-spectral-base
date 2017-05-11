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
 * InstancesToSpectrum.java
 * Copyright (C) 2010-2013 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.flow.core.Token;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import weka.core.Attribute;
import weka.core.Instance;

/**
 <!-- globalinfo-start -->
 * Converts a weka.core.Instance to a Spectrum.<br>
 * By default, the wave numbers start at 0 and use an increment of 1.<br>
 * If 'first' and 'last' wave number are supplied, the step size is calculated based on the number of amplitudes present in the Instance.<br>
 * If only 'first' wave number is supplied, then the supplied wave step size is used.<br>
 * 'first' and 'last' get ignored if a value of less than 0 is supplied.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;weka.core.Instance<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;knir.data.spectrum.Spectrum<br>
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 * 
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: InstanceToSpectrum
 * </pre>
 * 
 * <pre>-annotation &lt;adams.core.base.BaseText&gt; (property: annotations)
 * &nbsp;&nbsp;&nbsp;The annotations to attach to this actor.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-skip &lt;boolean&gt; (property: skip)
 * &nbsp;&nbsp;&nbsp;If set to true, transformation is skipped and the input token is just forwarded 
 * &nbsp;&nbsp;&nbsp;as it is.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-stop-flow-on-error &lt;boolean&gt; (property: stopFlowOnError)
 * &nbsp;&nbsp;&nbsp;If set to true, the flow gets stopped in case this actor encounters an error;
 * &nbsp;&nbsp;&nbsp; useful for critical actors.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-productCode &lt;java.lang.String&gt; (property: productCode)
 * &nbsp;&nbsp;&nbsp;Either the attribute name with the product code in it, or the actual product 
 * &nbsp;&nbsp;&nbsp;code to be used. 
 * &nbsp;&nbsp;&nbsp;default: 01
 * </pre>
 * 
 * <pre>-sampleID &lt;java.lang.String&gt; (property: sampleID)
 * &nbsp;&nbsp;&nbsp;Regex to find sample id. e.g 'sample_id'
 * &nbsp;&nbsp;&nbsp;default: sample_id
 * </pre>
 * 
 * <pre>-getProductCodeFromAttribute &lt;java.lang.Boolean&gt; (property: getProductCodeFromAttribute)
 * &nbsp;&nbsp;&nbsp;Regex to find sample id. e.g 'sample_id'
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-spectralDataAttributes &lt;java.lang.String&gt; (property: spectralDataAttributes)
 * &nbsp;&nbsp;&nbsp;Regex to choose attributes to use as spectum amplitudes'
 * &nbsp;&nbsp;&nbsp;default: amplitude.*
 * </pre>
 * 
 * <pre>-first-wave &lt;float&gt; (property: firstWave)
 * &nbsp;&nbsp;&nbsp;The starting point for the wave numbers, ignored if less than 0.
 * &nbsp;&nbsp;&nbsp;default: -1.0
 * </pre>
 * 
 * <pre>-last-wave &lt;float&gt; (property: lastWave)
 * &nbsp;&nbsp;&nbsp;The end point for the wave numbers, ignored if less than 0.
 * &nbsp;&nbsp;&nbsp;default: -1.0
 * </pre>
 * 
 * <pre>-wave-step &lt;float&gt; (property: waveStep)
 * &nbsp;&nbsp;&nbsp;The step size for the wave numbers to use, if 'lastWave' is not defined.
 * &nbsp;&nbsp;&nbsp;default: 1.0
 * &nbsp;&nbsp;&nbsp;minimum: 0.0
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class InstanceToSpectrum
  extends AbstractTransformer {

  /** for serialization. */
  private static final long serialVersionUID = 4722189022566624536L;

  /** Either the attribute name with the product code in it, or the actual product code. Decided by m_GetProductCodeFromAttribute. */
  protected String m_ProductCode;

  /** Get the product code from the attribute named ProductCode in the Instance? Otherwise use the ProductCode string directly. */
  protected Boolean m_ProductCodeFromAttribute;

  /** Regex to find sample id. e.g "sampleID" */
  protected String m_SampleIDregex;

  /** Regex to find spectral data. e.g "amplitude.*" */
  protected String m_SpectralData;

  /** the first wave number. */
  protected float m_FirstWave;

  /** the last wave number. */
  protected float m_LastWave;

  /** the wave number step. */
  protected float m_WaveStep;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return 
	"Converts a weka.core.Instance to a Spectrum.\n"
	+ "By default, the wave numbers start at 0 and use an increment of 1.\n"
	+ "If 'first' and 'last' wave number are supplied, the step size is "
	+ "calculated based on the number of amplitudes present in the Instance.\n"
	+ "If only 'first' wave number is supplied, then the supplied wave step "
	+ "size is used.\n"
	+ "'first' and 'last' get ignored if a value of less than 0 is supplied.";
  }
  
  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "productCode", "productCode",
	    "01");

    m_OptionManager.add(
	    "sampleID", "sampleID",
	    "sample_id");

    m_OptionManager.add(
	    "getProductCodeFromAttribute", "getProductCodeFromAttribute",
	    false);

    m_OptionManager.add(
	    "spectralDataAttributes", "spectralDataAttributes",
	    "amplitude.*");

    m_OptionManager.add(
	    "first-wave", "firstWave",
	    -1.0f);

    m_OptionManager.add(
	    "last-wave", "lastWave",
	    -1.0f);

    m_OptionManager.add(
	    "wave-step", "waveStep",
	    1.0f, 0.0f, null);
  }

  /**
   * Get sample id regex.
   * @return	sample id regex
   */
  public String getSampleID() {
    return m_SampleIDregex;
  }

  /**
   * Get sample id regex.
   * @param mSampleIDregex sample id regex
   */
  public void setSampleID(String mSampleIDregex) {
    m_SampleIDregex = mSampleIDregex;
    reset();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sampleIDTipText() {
    return "Regex to find sample id. e.g 'sample_id'";
  }

  /**
   * Get product code from instance attribute? Or use as string.
   * @return Get product code from instance attribute?
   */
  public Boolean getGetProductCodeFromAttribute() {
    return m_ProductCodeFromAttribute;
  }


  /**
   * Set product code from instance attribute? Or use as string.
   * @param mGetProductCodeFromAttribute Set product code from instance attribute?
   */
  public void setGetProductCodeFromAttribute(Boolean mGetProductCodeFromAttribute) {
    m_ProductCodeFromAttribute = mGetProductCodeFromAttribute;
    reset();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String getProductCodeFromAttributeTipText() {
    return "Regex to find sample id. e.g 'sample_id'";
  }

  /**
   * Get which attributes to use for spectral data.
   * @return regex attribute name.
   */
  public String getSpectralDataAttributes() {
    return m_SpectralData;
  }

  /**
   * Set which attributes to use for spectral data.
   * @param mSpectralData	regex attribute name.
   */
  public void setSpectralDataAttributes(String mSpectralData) {
    m_SpectralData = mSpectralData;
    reset();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String spectralDataAttributesTipText() {
    return "Regex to choose attributes to use as spectum amplitudes'";
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
   * Sets the starting point for the wave numbers. Ignored if less than zero.
   *
   * @param value	the first wave number
   */
  public void setFirstWave(float value) {
    m_FirstWave = value;
    reset();
  }

  /**
   * Returns the starting point for the wave numbers. Ignored if less than zero.
   *
   * @return		the first wave number
   */
  public float getFirstWave() {
    return m_FirstWave;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String firstWaveTipText() {
    return "The starting point for the wave numbers, ignored if less than 0.";
  }

  /**
   * Sets the end point for the wave numbers. Ignored if less than zero.
   *
   * @param value	the last wave number
   */
  public void setLastWave(float value) {
    m_LastWave = value;
    reset();
  }

  /**
   * Returns the end point for the wave numbers. Ignored if less than zero.
   *
   * @return		the last wave number
   */
  public float getLastWave() {
    return m_LastWave;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String lastWaveTipText() {
    return "The end point for the wave numbers, ignored if less than 0.";
  }

  /**
   * Sets the step size for the wave numbers, if lastWave is not defined.
   *
   * @param value	the step size
   */
  public void setWaveStep(float value) {
    m_WaveStep = value;
    reset();
  }

  /**
   * Returns the step size for the wave numbers, if lastWave is not defined.
   *
   * @return		the step size
   */
  public float getWaveStep() {
    return m_WaveStep;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for displaying in the GUI or
   *         for listing the options.
   */
  public String waveStepTipText() {
    return "The step size for the wave numbers to use, if 'lastWave' is not defined.";
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		<!-- flow-accepts-start -->weka.core.Instance.class<!-- flow-accepts-end -->
   */
  public Class[] accepts() {
    return new Class[]{Instance.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		<!-- flow-generates-start -->knir.data.spectrum.Spectrum.class<!-- flow-generates-end -->
   */
  public Class[] generates() {
    return new Class[]{Spectrum.class};
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String	result;
    Instance	inst;
    int		i;
    Spectrum 	spectrum;
    float 	x;
    float	step;
    SampleData 	report;
    String 	sampleid;
    String 	productcode;
    Attribute 	att;
    int		points;

    result   = null;
    spectrum = new Spectrum();
    inst     = (Instance) m_InputToken.getPayload();
    if (m_FirstWave >= 0.0f)
      x = m_FirstWave;
    else
      x = 0.0f;
    if ((m_FirstWave >= 0.0f) && (m_LastWave >= 0.0f)) {
      points = 0;
      for (i = 0; i < inst.numAttributes(); i++){
	att = inst.attribute(i);
	if (att.name().matches(m_SpectralData))
	  points++;
      }
      step = (m_LastWave - m_FirstWave) / (float) (points - 1);
    }
    else if (m_FirstWave >= 0.0f) {
      step = m_WaveStep;
    }
    else {
      step = 1.0f;
    }

    report      = new SampleData();
    sampleid    = "unknown";
    productcode = m_ProductCode; //default

    for (i = 0; i < inst.numAttributes(); i++){
      att = inst.attribute(i);
      if (att.name().matches(m_SpectralData)) {
	spectrum.add(new SpectrumPoint(x, (float) inst.value(i)));
	x += step;
      }
      if (m_ProductCodeFromAttribute && att.name().matches(m_ProductCode))
	productcode = inst.stringValue(att);
      if (att.name().matches(m_SampleIDregex))
	sampleid = inst.stringValue(att);
    }

    spectrum.setID(sampleid);
    report.addParameter("Sample ID", sampleid);
    report.addParameter("Sample Type", productcode);
    spectrum.setReport(report);
    m_OutputToken = new Token(spectrum);

    return result;
  }
}
