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
 * MultiUpload.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.webservice.multispectrum;

import adams.flow.webservice.AbstractWebServiceClientSink;
import adams.flow.webservice.WebserviceUtils;
import adams.data.spectrum.MultiSpectrum;
import adams.flow.core.RatsMultiSpectrumHelper;
import nz.ac.waikato.adams.webservice.rats.multispectrum.RatsMultiSpectrumService;
import nz.ac.waikato.adams.webservice.rats.multispectrum.RatsMultiSpectrumServiceService;
import nz.ac.waikato.adams.webservice.rats.multispectrum.UploadRequest;
import nz.ac.waikato.adams.webservice.rats.multispectrum.UploadResponse;

import javax.xml.ws.BindingProvider;
import java.net.URL;

/**
 * Uploads a MultiSpectrum.
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2088 $
 */
public class MultiUpload 
  extends AbstractWebServiceClientSink<MultiSpectrum>{

  /** for serialization*/
  private static final long serialVersionUID = -338043583699608760L;
  
  /** input spectrum */
  protected MultiSpectrum m_SpectrumIn;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Stores a MultiSpectrum using the RATS MultiSpectrum webservice.";
  }

  /**
   * Returns the classes that are accepted input.
   * 
   * @return		the classes that are accepted
   */
  @Override
  public Class[] accepts() {
    return new Class[]{MultiSpectrum.class};
  }

  /**
   * Returns the WSDL location.
   * 
   * @return		the location
   */
  @Override
  public URL getWsdlLocation() {
    return getClass().getClassLoader().getResource("wsdl/knir/RatsMultiSpectrumService.wsdl");
  }

  /**
   * Sets the data for the request, if any.
   * 
   * @param value	the request data
   */
  @Override
  public void setRequestData(MultiSpectrum value) {
    m_SpectrumIn = value;
  }

  /**
   * Performs the actual webservice query.
   * 
   * @throws Exception	if accessing webservice fails for some reason
   */
  @Override
  protected void doQuery() throws Exception {
    RatsMultiSpectrumServiceService ratsServiceService;
    RatsMultiSpectrumService ratsService;
    ratsServiceService = new RatsMultiSpectrumServiceService(getWsdlLocation());
    ratsService = ratsServiceService.getRatsMultiSpectrumServicePort();
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
    request.setSpectrum(RatsMultiSpectrumHelper.knirToWebservice(m_SpectrumIn));
    UploadResponse response = ratsService.upload(request);
    
    // failed to generate data?
    if (!response.isSuccess())
      throw new IllegalStateException(response.getMessage());
  }
}
