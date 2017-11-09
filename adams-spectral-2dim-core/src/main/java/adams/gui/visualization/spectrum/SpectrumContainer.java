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
 * SpectrumContainer.java
 * Copyright (C) 2009-2017 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.visualization.spectrum;

import adams.core.Constants;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.gui.event.DataChangeEvent;
import adams.gui.event.DataChangeEvent.Type;
import adams.gui.visualization.container.AbstractContainer;
import adams.gui.visualization.container.ColorContainer;
import adams.gui.visualization.container.DatabaseContainer;
import adams.gui.visualization.container.NamedContainer;
import adams.gui.visualization.container.VisibilityContainer;

import java.awt.Color;

/**
 * A container class for a spectrum and additional information.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2195 $
 */
public class SpectrumContainer
  extends AbstractContainer
  implements VisibilityContainer, NamedContainer, ColorContainer, DatabaseContainer {

  /** for serialization. */
  private static final long serialVersionUID = -2589474045543243525L;

  /** whether the spectrum is visible. */
  protected boolean m_Visible;

  /** the associated color. */
  protected Color m_Color;

  /**
   * Initializes the container.
   *
   * @param manager	the owning manager
   * @param data	the spectrum to store (visible)
   */
  public SpectrumContainer(SpectrumContainerManager manager, Spectrum data) {
    super(manager, data);
  }

  /**
   * Initializes members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_Visible = true;
    m_Color   = Color.WHITE;
  }

  /**
   * Sets the ID to use for display.
   *
   * @param value	the ID
   */
  public void setID(String value) {
    getData().setID(value);
  }

  /**
   * Returns the ID used for display.
   *
   * @return		the ID
   */
  public String getID() {
    return ((Spectrum) m_Payload).getID();
  }

  /**
   * Returns an ID to be used in the GUI (the DB ID with added single quotes
   * to make it unique among the spectrums in the container manager).
   *
   * @return		a short ID for the GUI
   */
  public String getDisplayID() {
    String	result;
    SampleData  sd;

    sd = getData().getReport();

    if ((sd != null) && sd.hasValue(SampleData.FORMAT))
      result = getData().getID() + "/" + sd.getStringValue(SampleData.FORMAT);
    else
      result = getData().getID();

    return result;
  }

  /**
   * Sets the spectrum.
   *
   * @param value	the spectrum
   */
  public void setData(Spectrum value) {
    setPayload(value);
  }

  /**
   * Returns the stored spectrum.
   *
   * @return		the spectrum
   */
  public Spectrum getData() {
    return (Spectrum) getPayload();
  }

  /**
   * Sets the spectrums visibility.
   *
   * @param value	if true then the spectrum will be visible
   */
  public void setVisible(boolean value) {
    m_Visible = value;

    if ((!m_Updating) && (getManager() != null))
      getManager().notifyDataChangeListeners(
          new DataChangeEvent(getManager(), Type.VISIBILITY, getManager().indexOf(this)));
  }

  /**
   * Returns whether the spectrum is visible.
   *
   * @return		true if the spectrum is visible
   */
  public boolean isVisible() {
    return m_Visible;
  }

  /**
   * Sets the color to use.
   *
   * @param value	the color
   */
  public void setColor(Color value) {
    m_Color = value;

    if ((!m_Updating) && (getManager() != null))
      getManager().notifyDataChangeListeners(
          new DataChangeEvent(getManager(), Type.UPDATE, getManager().indexOf(this)));
  }

  /**
   * Returns the current color in use.
   *
   * @return		the color
   */
  public Color getColor() {
    return m_Color;
  }

  /**
   * Returns the database ID.
   *
   * @return		the database ID
   */
  public int getDatabaseID() {
    if (getData().hasReport())
      return getData().getDatabaseID();
    else
      return Constants.NO_ID;
  }

  /**
   * Updates itself with the values from given container (the manager is
   * excluded!). Derived classes need to override this method.
   *
   * @param c		the container to get the values from
   */
  @Override
  public void assign(AbstractContainer c) {
    super.assign(c);

    m_Updating = true;

    if (c instanceof SpectrumContainer) {
      setID(((SpectrumContainer) c).getID());
      setVisible(((SpectrumContainer) c).isVisible());
      setColor(((SpectrumContainer) c).getColor());
    }

    m_Updating = false;
  }

  /**
   * Returns the hashcode of the ID string.
   *
   * @return		the hashcode
   */
  @Override
  public int hashCode() {
    return Long.toString(getData().getUniqueID()).hashCode();
  }

  /**
   * Returns a short string representation of the container.
   *
   * @return		a string representation
   */
  @Override
  public String toString() {
    return "DB ID: " + getData().getDatabaseID() + ", ID: " + getID() + ", Visible: " + isVisible() + ", Color: " + getColor();
  }
}