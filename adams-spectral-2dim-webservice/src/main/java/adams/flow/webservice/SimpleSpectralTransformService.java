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

import adams.core.Utils;
import adams.core.option.AbstractOptionHandler;
import adams.data.spectrum.Spectrum;
import adams.db.DatabaseConnectionUser;
import adams.flow.core.Actor;
import adams.flow.core.ActorUtils;
import adams.flow.core.CallableActorHelper;
import adams.flow.core.CallableActorReference;
import adams.flow.core.Compatibility;
import adams.flow.core.InputConsumer;
import adams.flow.core.OutputProducer;
import adams.flow.core.Token;
import adams.flow.core.TransformSpectrumHelper;
import nz.ac.waikato.adams.webservice.spectral.transform.SpectralTransformService;
import nz.ac.waikato.adams.webservice.spectral.transform.TransformRequest;
import nz.ac.waikato.adams.webservice.spectral.transform.TransformResponse;

/**
 * Class that implements the Spectral web service.  
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SimpleSpectralTransformService
  extends AbstractOptionHandler
  implements SpectralTransformService, OwnedBySpectralTransformServiceWS, DatabaseConnectionUser {

  /** for serialization. */
  private static final long serialVersionUID = -6102580694812360595L;

  /** web service object   */
  protected adams.flow.webservice.SpectralTransformServiceWS m_Owner;

  /** the database connection. */
  protected adams.db.AbstractDatabaseConnection m_DatabaseConnection;
  
  /**
   * Default Constructor.
   * <br><br>
   * NB: the owning webservice needs to get set before using this implemention,
   * using the {@link #setOwner(SpectralTransformServiceWS)} method.
   */
  public SimpleSpectralTransformService() {
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
    return "Simple implementation of a Spectral transform webservice.";
  }

  /**
   * Determines the database connection in the flow.
   *
   * @return		the database connection to use
   */
  protected adams.db.AbstractDatabaseConnection getDatabaseConnection() {
    return ActorUtils.getDatabaseConnection(
	  m_Owner.getFlowContext(),
	  adams.flow.standalone.DatabaseConnectionProvider.class,
	  adams.db.DatabaseConnection.getSingleton());
  }

  /**
   * Sets the owner of this webservice.
   * 
   * @param value	the owner
   */
  public void setOwner(SpectralTransformServiceWS value) {
    m_Owner = value;
    
    if ((m_Owner != null) && (m_Owner.getFlowContext() != null))
      m_DatabaseConnection = getDatabaseConnection();
    else
      m_DatabaseConnection = null;
  }
  
  /**
   * Returns the current owner of this webservice.
   * 
   * @return		the owner, null if none set
   */
  public SpectralTransformServiceWS getOwner() {
    return m_Owner;
  }

  /**
   * Transforms a spectrum.
   */
  @Override
  public TransformResponse transform(TransformRequest parameters) {
    TransformResponse		result;
    CallableActorHelper		helper;
    Actor 			callable;
    Compatibility		comp;
    String			msg;
    Token			output;
    Spectrum			sp;

    m_Owner.getLogger().info("transform: " + parameters.getId() + "/" + parameters.getFormat());
    
    result = new TransformResponse();
    result.setId(parameters.getId());
    result.setFormat(parameters.getFormat());

    helper   = new CallableActorHelper();
    callable = helper.findCallableActor(m_Owner.getFlowContext().getRoot(), new CallableActorReference(parameters.getAction()));

    // not found
    if (callable == null) {
      result.setSuccess(false);
      result.setMessage("Failed to find callable actor '" + parameters.getAction() + "'!");
      return result;
    }
    
    // not a transformer
    if (!ActorUtils.isTransformer(callable)) {
      result.setSuccess(false);
      result.setMessage("Callable actor '" + parameters.getAction() + "' is not a transformer!");
      return result;
    }
    
    // wrong input/output
    comp = new Compatibility();
    if (!comp.isCompatible(new Class[]{Spectrum.class}, ((InputConsumer) callable).accepts())) {
      result.setSuccess(false);
      result.setMessage("Callable transformer '" + parameters.getAction() + "' does not accept " + Spectrum.class.getName() + "!");
      return result;
    }
    if (!comp.isCompatible(((OutputProducer) callable).generates(), new Class[]{Spectrum.class})) {
      result.setSuccess(false);
      result.setMessage("Callable transformer '" + parameters.getAction() + "' does not generate " + Spectrum.class.getName() + "!");
      return result;
    }
    
    try {
      synchronized(callable) {
	((InputConsumer) callable).input(new Token(TransformSpectrumHelper.webserviceToKnir(parameters.getSpectrum())));
	msg = callable.execute();
	if (msg != null) {
	  result.setSuccess(false);
	  result.setMessage(msg);
	  return result;
	}
	else {
	  if (((OutputProducer) callable).hasPendingOutput()) {
	    output = ((OutputProducer) callable).output();
	    sp     = (Spectrum) output.getPayload();
	    result.setSuccess(true);
	    result.setSpectrum(TransformSpectrumHelper.knirToWebservice(sp));
	  }
	  else {
	    result.setSuccess(false);
	    result.setMessage("Callable transformer '" + parameters.getAction() + "' did not produce any output!");
	    return result;
	  }
	}
      }
    } 
    catch (java.lang.Exception ex) {
      result.setSuccess(false);
      result.setMessage(Utils.handleException(m_Owner, "Failed to transform data using callable transformer '" + parameters.getAction() + "'!", ex));
    }
    
    return result;
  }
}