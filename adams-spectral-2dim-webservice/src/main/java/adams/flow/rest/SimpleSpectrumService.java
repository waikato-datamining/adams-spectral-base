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
 * SimpleSpectrumService.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.flow.rest;

import adams.data.conversion.SpectrumToJson;
import adams.data.spectrum.Spectrum;
import adams.db.AbstractDatabaseConnection;
import adams.db.DatabaseConnection;
import adams.db.SpectrumT;
import adams.flow.core.Actor;
import adams.flow.core.ActorUtils;
import adams.flow.core.FlowContextHandler;
import net.minidev.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * Simple REST plugin for spectral data.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SimpleSpectrumService
  extends AbstractRESTPlugin
  implements FlowContextHandler {

  private static final long serialVersionUID = -826056354423201513L;

  /** the flow context. */
  protected Actor m_FlowContext;

  /** the database to use. */
  protected AbstractDatabaseConnection m_DatabaseConnection;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Provides access to spectra.";
  }

  /**
   * Resets the plugin.
   */
  @Override
  protected void reset() {
    super.reset();

    m_DatabaseConnection = null;
  }

  /**
   * Sets the flow context.
   *
   * @param value	the actor
   */
  public void setFlowContext(Actor value) {
    m_FlowContext        = value;
    m_DatabaseConnection = null;
  }

  /**
   * Returns the flow context, if any.
   *
   * @return		the actor, null if none available
   */
  public Actor getFlowContext() {
    return m_FlowContext;
  }

  /**
   * Initializes the database from the flow context.
   */
  protected void initDatabase() {
    if (m_FlowContext == null)
      throw new IllegalStateException("No flow context, cannot initialize database connection!");
    if (m_DatabaseConnection == null) {
      m_DatabaseConnection = ActorUtils.getDatabaseConnection(
	m_FlowContext, AbstractDatabaseConnection.class, DatabaseConnection.getSingleton());
      if (m_DatabaseConnection == null)
	throw new IllegalStateException("Failed to initialize database connection!");
    }
  }

  @GET
  @Path("/get/{id}/{format}")
  @Produces("text/json")
  public String get(@PathParam("id") String id, @PathParam("format") String format) {
    Spectrum 		sp;
    JSONObject		json;
    SpectrumToJson	conv;
    String		msg;

    initDatabase();
    sp = SpectrumT.getSingleton(m_DatabaseConnection).load(id, format);
    if (sp == null) {
      json = new JSONObject();
      json.put("id", id);
      json.put("format", format);
      json.put("message", "not found");
    }
    else {
      conv = new SpectrumToJson();
      conv.setInput(sp);
      msg = conv.convert();
      if (msg == null) {
        json = (JSONObject) conv.getOutput();
	json.put("message", "success");
      }
      else {
	json = new JSONObject();
	json.put("id", id);
	json.put("format", format);
	json.put("message", msg);
      }
      conv.cleanUp();
    }
    return json.toJSONString();
  }
}
