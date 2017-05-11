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
 * Put.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.webservice;

import adams.data.spectrum.Spectrum;
import adams.flow.core.PutSpectrumHelper;
import nz.ac.waikato.adams.webservice.spectral.put.PutRequest;
import nz.ac.waikato.adams.webservice.spectral.put.PutResponse;
import nz.ac.waikato.adams.webservice.spectral.put.SpectralPutService;
import nz.ac.waikato.adams.webservice.spectral.put.SpectralPutServiceService;

import javax.xml.ws.BindingProvider;
import java.net.URL;

/**
 * Stores a spectrum.
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2138 $
 */
public class Put 
  extends AbstractWebServiceClientSink<Spectrum>{

  /** for serialization*/
  private static final long serialVersionUID = -338043583699608760L;
  
  /** input spectrum */
  protected Spectrum m_SpectrumIn;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Stores a spectrum using the Spectral web service.";
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
   * Returns the WSDL location.
   * 
   * @return		the location
   */
  @Override
  public URL getWsdlLocation() {
    return getClass().getClassLoader().getResource("wsdl/knir/SpectralPutService.wsdl");
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
    SpectralPutServiceService knirServiceService;
    SpectralPutService knirService;
    knirServiceService = new SpectralPutServiceService(getWsdlLocation());
    knirService = knirServiceService.getSpectralPutServicePort();
    WebserviceUtils.configureClient(
	m_Owner,
	knirService, 
	m_ConnectionTimeout, 
	m_ReceiveTimeout, 
	(getUseAlternativeURL() ? getAlternativeURL() : null),
	null,
	m_OutInterceptor);
    //check against schema
    WebserviceUtils.enableSchemaValidation(((BindingProvider) knirService));
   
    PutRequest request = new PutRequest();
    request.setId(m_SpectrumIn.getID());
    request.setFormat(m_SpectrumIn.getFormat());
    request.setSpectrum(PutSpectrumHelper.knirToWebservice(m_SpectrumIn));
    PutResponse response = knirService.put(request);
    
    // failed to generate data?
    if (!response.isSuccess())
      throw new IllegalStateException(response.getMessage());
  }
}
