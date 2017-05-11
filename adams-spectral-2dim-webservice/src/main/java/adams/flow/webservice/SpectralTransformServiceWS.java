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
 * SpectralTransformServiceWS.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.webservice;

import nz.ac.waikato.adams.webservice.spectral.transform.SpectralTransformService;
import org.apache.cxf.jaxws.EndpointImpl;

import javax.xml.ws.Endpoint;

/**
 * Webservice for Spectral.
 * 
 * @author Fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2095 $
 */
public class SpectralTransformServiceWS
  extends AbstractWebServiceProvider {

  /** for serilaization */
  private static final long serialVersionUID = -6865165378146103361L;

  /** end point for the web service */
  protected transient EndpointImpl m_Endpoint;
  
  /** the webservice implementation to use. */
  protected SpectralTransformService m_Implementation;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return 
	"Provides a Spectral web service with the following services available:\n"
	+ "- transform spectrum\n"
	+ "Enable logging to see inbound/outgoing messages.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();
    
    m_OptionManager.add(
	"implementation", "implementation", 
	new SimpleSpectralTransformService());
  }

  /**
   * Returns the default URL for the service.
   * 
   * @return		the URL
   */
  @Override
  public String getDefaultURL() {
    return "http://localhost:9090/SpectralTransformServicePort";
  }

  /**
   * Sets the webservice implementation to use.
   * 
   * @param value	the implementation
   */
  public void setImplementation(SpectralTransformService value) {
    m_Implementation = value;
    reset();
  }

  /**
   * Returns the webservice implementation to use.
   * 
   * @return 		the implementation
   */
  public SpectralTransformService getImplementation() {
    return m_Implementation;
  }

  /**
   * Description of this option.
   * 
   * @return 		the description for the GUI
   */
  public String implementationTipText() {
    return "The implementation of the webservice to use.";
  }

  /**
   * Performs the actual start of the service.
   * 
   * @throws Exception	if start fails
   */
  @Override
  protected void doStart() throws Exception {
    SpectralTransformService implementer;

    implementer = (SpectralTransformService) WebserviceUtils.copyImplementation(m_Implementation);
    if (implementer instanceof OwnedBySpectralTransformServiceWS)
      ((OwnedBySpectralTransformServiceWS) implementer).setOwner(this);
    m_Endpoint  = (EndpointImpl) Endpoint.publish(getURL(), implementer);

    javax.xml.ws.soap.SOAPBinding binding = (javax.xml.ws.soap.SOAPBinding) m_Endpoint.getBinding();
    binding.setMTOMEnabled(true);

    configureInterceptors(m_Endpoint);
  }

  /**
   * Performs the actual stop of the service.
   * 
   * @throws Exception	if stopping fails
   */
  @Override
  protected void doStop() throws Exception {
    if (m_Endpoint != null) {
      m_Endpoint.getServer().stop();
      m_Endpoint = null;
    }
  }
}
