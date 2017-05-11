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
 * Upload.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.webservice.spectrum;

import adams.flow.webservice.AbstractWebServiceClientSink;
import adams.flow.webservice.WebserviceUtils;
import adams.data.spectrum.Spectrum;
import adams.flow.core.RatsSpectrumHelper;
import nz.ac.waikato.adams.webservice.rats.spectrum.RatsSpectrumService;
import nz.ac.waikato.adams.webservice.rats.spectrum.RatsSpectrumServiceService;
import nz.ac.waikato.adams.webservice.rats.spectrum.UploadRequest;
import nz.ac.waikato.adams.webservice.rats.spectrum.UploadResponse;

import javax.xml.ws.BindingProvider;
import java.net.URL;

/**
 * Uploads a spectrum.
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2137 $
 */
public class Upload 
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
    return "Stores a spectrum using the RATS spectrum webservice.";
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
    return getClass().getClassLoader().getResource("wsdl/knir/RatsSpectrumService.wsdl");
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
    RatsSpectrumServiceService ratsServiceService;
    RatsSpectrumService ratsService;
    ratsServiceService = new RatsSpectrumServiceService(getWsdlLocation());
    ratsService = ratsServiceService.getRatsSpectrumServicePort();
    WebserviceUtils.configureClient(
	m_Owner,
	ratsService, 
	m_ConnectionTimeout, 
	m_ReceiveTimeout, 
	(getUseAlternativeURL() ? getAlternativeURL() : null),
	null,
	m_OutInterceptor);
    //check against schema
    WebserviceUtils.enableSchemaValidation(((BindingProvider) ratsService));
   
    UploadRequest request = new UploadRequest();
    request.setId(m_SpectrumIn.getID());
    request.setFormat(m_SpectrumIn.getFormat());
    request.setSpectrum(RatsSpectrumHelper.knirToWebservice(m_SpectrumIn));
    UploadResponse response = ratsService.upload(request);
    
    // failed to generate data?
    if (!response.isSuccess())
      throw new IllegalStateException(response.getMessage());
  }
}
