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
 * Transform.java
 * Copyright (C) 2014-2019 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.webservice;

import adams.data.spectrum.Spectrum;
import adams.flow.core.TransformSpectrumHelper;
import nz.ac.waikato.adams.webservice.spectral.transform.SpectralTransformService;
import nz.ac.waikato.adams.webservice.spectral.transform.SpectralTransformServiceService;
import nz.ac.waikato.adams.webservice.spectral.transform.TransformRequest;
import nz.ac.waikato.adams.webservice.spectral.transform.TransformResponse;

import javax.xml.ws.BindingProvider;
import java.net.URL;

/**
 * Transforms a spectrum.
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class Transform 
  extends AbstractWebServiceClientTransformer<Spectrum, Spectrum>{

  /** for serialization*/
  private static final long serialVersionUID = -338043583699608760L;
  
  /** the actor to call. */
  protected String m_Action;
  
  /** input spectrum */
  protected Spectrum m_SpectrumIn;

  /** the service instance. */
  protected transient SpectralTransformServiceService m_Service;

  /** the port instance. */
  protected transient SpectralTransformService m_Port;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Transforms a spectrum using the Spectral web service.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	"action", "action", 
	"");
  }

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();

    m_Service = null;
    m_Port    = null;
  }

  /**
   * Sets the action (callable actor on the server side) to execute.
   * 
   * @param value	the action
   */
  public void setAction(String value) {
    m_Action = value;
    reset();
  }

  /**
   * Returns the action (callable actor on the server side) to execute.
   * 
   * @return		the action
   */
  public String getAction() {
    return m_Action;
  }

  /**
   * Description of this option.
   * 
   * @return		description of the option
   */
  public String actionTipText() {
    return "The action (or callable actor) to execute on the server side for transforming the spectra.";
  }

  /**
   * Returns the classes that are accepted input.
   * 
   * @return		the classes that are accepted
   */
  @Override
  public Class[] accepts() {
    return new Class[]{Spectrum.class};
  }

  /**
   * Returns the classes that this client generates.
   * 
   * @return		the classes
   */
  @Override
  public Class[] generates() {
    return new Class[]{Spectrum.class};
  }

  /**
   * Returns the WSDL location.
   * 
   * @return		the location
   */
  @Override
  public URL getWsdlLocation() {
    return getClass().getClassLoader().getResource("wsdl/knir/SpectralTransformService.wsdl");
  }

  /**
   * Sets the data for the request, if any.
   * 
   * @param value	the request data
   */
  @Override
  public void setRequestData(Spectrum value) {
    m_SpectrumIn = value;
  }

  /**
   * Performs the actual webservice query.
   * 
   * @throws Exception	if accessing webservice fails for some reason
   */
  @Override
  protected void doQuery() throws Exception {
    if (m_Service == null) {
      m_Service = new SpectralTransformServiceService(getWsdlLocation());
      m_Port = m_Service.getSpectralTransformServicePort();
      WebserviceUtils.configureClient(
        m_Owner,
        m_Port,
        m_ConnectionTimeout,
        m_ReceiveTimeout,
        (getUseAlternativeURL() ? getAlternativeURL() : null),
        m_InInterceptor,
        m_OutInterceptor);
      //check against schema
      WebserviceUtils.enableSchemaValidation(((BindingProvider) m_Port));
    }
   
    TransformRequest request = new TransformRequest();
    request.setId(m_SpectrumIn.getID());
    request.setFormat(m_SpectrumIn.getFormat());
    request.setAction(m_Action);
    request.setSpectrum(TransformSpectrumHelper.knirToWebservice(m_SpectrumIn));
    TransformResponse response = m_Port.transform(request);
    
    // failed to generate data?
    if (!response.isSuccess())
      throw new IllegalStateException(response.getMessage());
    setResponseData(TransformSpectrumHelper.webserviceToKnir(response.getSpectrum()));
  }

  /**
   * Cleans up the client.
   */
  @Override
  public void cleanUp() {
    m_Service = null;
    m_Port    = null;

    super.cleanUp();
  }
}
