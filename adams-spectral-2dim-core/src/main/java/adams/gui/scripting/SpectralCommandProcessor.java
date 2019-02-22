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
 * SpectralCommandProcessor.java
 * Copyright (C) 2016-2019 University of Waikato, Hamilton, NZ
 */

package adams.gui.scripting;

import adams.data.spectrum.Spectrum;
import adams.db.SpectrumF;
import adams.gui.visualization.container.DataContainerPanel;
import adams.gui.visualization.spectrum.SpectrumExplorer;
import adams.gui.visualization.spectrum.SpectrumPanel;

/**
 * Command processor for the spectral module.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class SpectralCommandProcessor
  extends CommandProcessor {

  private static final long serialVersionUID = -1340060775554623535L;

  /**
   * Initializes the processor. Still needs to set the owner.
   *
   * @see	#setOwner(AbstractScriptingEngine)
   */
  public SpectralCommandProcessor() {
    super();
  }

  /**
   * Initializes the processor.
   *
   * @param owner	the owning scripting engine
   */
  public SpectralCommandProcessor(AbstractScriptingEngine owner) {
    super(owner);
  }

  /**
   * Returns the object that is to be used for the undo point.
   *
   * @return		the object to store as undo point
   */
  protected Object getUndoObject() {
    return getSpectrumPanel().getContainerManager().getAll();
  }

  /**
   * Returns the DataContainer panel.
   *
   * @return		the panel or null
   */
  public DataContainerPanel getDataContainerPanel() {
    if (getBasePanel() instanceof SpectrumExplorer)
      return ((SpectrumExplorer) getBasePanel()).getSpectrumPanel();
    else
      return super.getDataContainerPanel();
  }

  /**
   * Returns the spectrum panel, if available.
   *
   * @return		the panel
   */
  public SpectrumPanel getSpectrumPanel() {
    if (getBasePanel() instanceof SpectrumPanel)
      return (SpectrumPanel) getBasePanel();
    else if (getBasePanel() instanceof SpectrumExplorer)
      return ((SpectrumExplorer) getBasePanel()).getSpectrumPanel();
    else
      return null;
  }

  /**
   * Returns the class that is required in the flow.
   *
   * @return		the required class
   */
  protected Class getRequiredFlowClass() {
    return Spectrum.class;
  }

  /**
   * Checks the following requirement.
   *
   * @param requirement	the requirement class that needs to be present
   * @return		"" if met, error message if not met, null if not processed
   */
  protected String checkRequirement(Class requirement) {
    String	result;

    result = super.checkRequirement(requirement);

    if (result == null) {
      if (requirement == SpectrumPanel.class) {
	if (getSpectrumPanel() == null)
	  result = createRequirementError(requirement);
	else
	  result = "";
      }
    }

    return result;
  }

  /**
   * Performs further setups of the scriptlet.
   *
   * @param scriptlet	the scriptlet to work on
   */
  protected void setupScriptlet(AbstractScriptlet scriptlet) {
    super.setupScriptlet(scriptlet);
    scriptlet.setDataProvider(SpectrumF.getSingleton(getDatabaseConnection()));
  }
}
