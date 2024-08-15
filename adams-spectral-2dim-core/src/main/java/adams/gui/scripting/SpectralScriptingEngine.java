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
 * SpectralScriptingEngine.java
 * Copyright (C) 2016-2024 University of Waikato, Hamilton, NZ
 */

package adams.gui.scripting;

import adams.core.Properties;
import adams.core.scriptingengine.BackgroundScriptingEngineRegistry;
import adams.db.AbstractDatabaseConnection;
import adams.env.SpectralScriptingEngineDefinition;

/**
 * Scripting engine for spectral module.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectralScriptingEngine
  extends ScriptingEngine {

  private static final long serialVersionUID = -8499786260111786279L;

  /** the name of the props file. */
  public final static String FILENAME = "SpectralScriptingEngine.props";

  /** the scripting engine manager. */
  private static ScriptingEngineManager m_ScriptingEngineManager;

  /** the properties for scripting. */
  private static Properties m_Properties;

  /**
   * Returns the properties key to use for retrieving the properties.
   *
   * @return		the key
   */
  @Override
  protected String getDefinitionKey() {
    return SpectralScriptingEngineDefinition.KEY;
  }

  /**
   * Provides access to the properties object.
   *
   * @return		the properties
   */
  @Override
  protected synchronized Properties getProperties() {
    if (m_Properties == null)
      m_Properties = readProperties();

    return m_Properties;
  }

  /**
   * Returns the singleton instance of the scripting engine.
   *
   * @param dbcon	the database context
   * @return		the singleton
   */
  public synchronized static AbstractScriptingEngine getSingleton(AbstractDatabaseConnection dbcon) {
    SpectralScriptingEngine	engine;

    if (m_ScriptingEngineManager == null)
      m_ScriptingEngineManager = new ScriptingEngineManager();
    if (!m_ScriptingEngineManager.has(dbcon)) {
      engine = new SpectralScriptingEngine();
      engine.setDatabaseConnection(dbcon);
      m_ScriptingEngineManager.add(dbcon, engine);
      BackgroundScriptingEngineRegistry.getSingleton().register(engine);
    }

    return m_ScriptingEngineManager.get(dbcon);
  }
}
