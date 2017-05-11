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
 * Get.java
 * Copyright (C) 2014-2016 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.webservice;

import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.flow.core.GetSpectrumHelper;
import nz.ac.waikato.adams.webservice.spectral.get.GetRequest;
import nz.ac.waikato.adams.webservice.spectral.get.GetResponse;
import nz.ac.waikato.adams.webservice.spectral.get.SpectralGetService;
import nz.ac.waikato.adams.webservice.spectral.get.SpectralGetServiceService;

import javax.xml.ws.BindingProvider;
import java.net.URL;

/**
 * Gets a spectrum.
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2138 $
 */
public class Get 
  extends AbstractWebServiceClientSource<Spectrum>{

  /** for serialization*/
  private static final long serialVersionUID = -338043583699608760L;

  /** the spectrum ID. */
  protected String m_ID;

  /** the spectrum format. */
  protected String m_Format;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Gets a spectrum using the Spectral web service.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	"id", "ID", 
	"");

    m_OptionManager.add(
	"format", "format", 
	SampleData.DEFAULT_FORMAT);
  }

  /**
   * Sets the ID of the spectrum to retrieve
   * 
   * @param value	the ID
   */
  public void setID(String value) {
    m_ID = value;
    reset();
  }

  /**
   * Returns the ID of the spectrum to retrieve.
   * 
   * @return		the ID
   */
  public String getID() {
    return m_ID;
  }

  /**
   * Description of this option.
   * 
   * @return		description of the option
   */
  public String IDTipText() {
    return "The ID of the spectrum to retrieve.";
  }

  /**
   * Sets the format of the spectrum to retrieve
   * 
   * @param value	the format
   */
  public void setFormat(String value) {
    m_Format = value;
    reset();
  }

  /**
   * Returns the format of the spectrum to retrieve.
   * 
   * @return		the format
   */
  public String getFormat() {
    return m_Format;
  }

  /**
   * Description of this option.
   * 
   * @return		description of the option
   */
  public String formatTipText() {
    return "The format of the spectrum to retrieve.";
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
    return getClass().getClassLoader().getResource("wsdl/knir/SpectralGetService.wsdl");
  }

  /**
   * Performs the actual webservice query.
   * 
   * @throws Exception	if accessing webservice fails for some reason
   */
  @Override
  protected void doQuery() throws Exception {
    SpectralGetServiceService knirServiceService;
    SpectralGetService knirService;
    knirServiceService = new SpectralGetServiceService(getWsdlLocation());
    knirService = knirServiceService.getSpectralGetServicePort();
    WebserviceUtils.configureClient(
	m_Owner,
	knirService, 
	m_ConnectionTimeout, 
	m_ReceiveTimeout, 
	(getUseAlternativeURL() ? getAlternativeURL() : null),
	m_InInterceptor,
	null);
    //check against schema
    WebserviceUtils.enableSchemaValidation(((BindingProvider) knirService));
   
    GetRequest request = new GetRequest();
    request.setId(m_ID);
    request.setFormat(m_Format);
    GetResponse response = knirService.get(request);
    
    // failed to generate data?
    if (!response.isSuccess())
      throw new IllegalStateException(response.getMessage());
    setResponseData(GetSpectrumHelper.webserviceToKnir(response.getSpectrum()));
  }
}
