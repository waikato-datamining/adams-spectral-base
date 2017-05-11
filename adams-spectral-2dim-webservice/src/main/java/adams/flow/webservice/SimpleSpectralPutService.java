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
 * SimpleSpectralService.java
 * Copyright (C) 2013-2017 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.webservice;

import adams.core.Constants;
import adams.core.option.AbstractOptionHandler;
import adams.db.SpectrumT;
import adams.flow.core.ActorUtils;
import adams.flow.core.PutSpectrumHelper;
import nz.ac.waikato.adams.webservice.spectral.put.PutRequest;
import nz.ac.waikato.adams.webservice.spectral.put.PutResponse;
import nz.ac.waikato.adams.webservice.spectral.put.SpectralPutService;

/**
 * Class that implements the Spectral put web service.  
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2070 $
 */
public class SimpleSpectralPutService
  extends AbstractOptionHandler
  implements SpectralPutService, OwnedBySpectralPutServiceWS {

  /** for serialization. */
  private static final long serialVersionUID = -6102580694812360595L;

  /** web service object   */
  protected SpectralPutServiceWS m_Owner;

  /** the database connection. */
  protected adams.db.AbstractDatabaseConnection m_DatabaseConnection;
  
  /**
   * Default Constructor.
   * <br><br>
   * NB: the owning webservice needs to get set before using this implemention,
   * using the {@link #setOwner(SpectralPutServiceWS)} method.
   */
  public SimpleSpectralPutService() {
    super();
    setOwner(null);
  }

  /**
   * Returns a string for the GUI that describes this object.
   * 
   * @return		the description
   */
  @Override
  public String globalInfo() {
    return "Simple implementation of a Spectral put webservice, stores the spectrum in the database.";
  }

  /**
   * Determines the database connection in the flow.
   *
   * @return		the database connection to use
   */
  protected adams.db.AbstractDatabaseConnection getDatabaseConnection() {
    return ActorUtils.getDatabaseConnection(
	  m_Owner.getOwner(),
	  adams.flow.standalone.DatabaseConnectionProvider.class,
	  adams.db.DatabaseConnection.getSingleton());
  }

  /**
   * Sets the owner of this webservice.
   * 
   * @param value	the owner
   */
  public void setOwner(SpectralPutServiceWS value) {
    m_Owner = value;
    
    if ((m_Owner != null) && (m_Owner.getOwner() != null))
      m_DatabaseConnection = getDatabaseConnection();
    else
      m_DatabaseConnection = null;
  }
  
  /**
   * Returns the current owner of this webservice.
   * 
   * @return		the owner, null if none set
   */
  public SpectralPutServiceWS getOwner() {
    return m_Owner;
  }

  /**
   * Downloads a spectrum.
   */
  @Override
  public PutResponse put(PutRequest parameters) {
    PutResponse		result;
    Integer		id;

    m_Owner.getLogger().info("put: " + parameters.getId() + "/" + parameters.getFormat());
    
    result = new PutResponse();
    id     = SpectrumT.getSingleton(m_DatabaseConnection).add(PutSpectrumHelper.webserviceToKnir(parameters.getSpectrum()));
    result.setId(parameters.getId());
    result.setFormat(parameters.getFormat());
    result.setSuccess(id != Constants.NO_ID);
    
    if (id == Constants.NO_ID)
      result.setMessage("Failed to store spectrum: " + parameters.getId() + "/" + parameters.getFormat());
    else
      result.setMessage("" + id);
    
    return result;
  }
}