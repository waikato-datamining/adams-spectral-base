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
 * SpectrumContainerManager.java
 * Copyright (C) 2009-2017 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.visualization.spectrum;

import adams.data.spectrum.Spectrum;
import adams.db.AbstractDatabaseConnection;
import adams.gui.scripting.Invisible;
import adams.gui.scripting.Visible;
import adams.gui.visualization.container.AbstractContainerManager;
import adams.gui.visualization.container.ColorContainerManager;
import adams.gui.visualization.container.DatabaseContainerManager;
import adams.gui.visualization.container.NamedContainerManagerWithUniqueNames;
import adams.gui.visualization.container.ReloadableContainerManager;
import adams.gui.visualization.container.VisibilityContainerManager;
import adams.gui.visualization.core.ColorProvider;
import adams.gui.visualization.core.ColorProviderWithNameSupport;
import adams.gui.visualization.core.DefaultColorProvider;
import gnu.trove.list.array.TIntArrayList;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A handler for the spectrum containers.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SpectrumContainerManager
  extends AbstractContainerManager<SpectrumContainer>
  implements VisibilityContainerManager<SpectrumContainer>, 
             ColorContainerManager<SpectrumContainer>, ReloadableContainerManager,
             DatabaseContainerManager<SpectrumContainer>, 
             NamedContainerManagerWithUniqueNames<SpectrumContainer> {

  /** for serialization. */
  private static final long serialVersionUID = -6358705201552088288L;

  /** the owning panel. */
  protected SpectrumPanel m_Owner;

  /** whether the spectra can be reloaded from the database (fake ones
   * can't be reloaded!). */
  protected boolean m_Reloadable;

  /** the color provider for managing the colors. */
  protected ColorProvider m_ColorProvider;

  /** the database connection. */
  protected AbstractDatabaseConnection m_DatabaseConnection;

  /**
   * Initializes the manager.
   *
   * @param owner	the owning panel
   * @param dbcon	the database context
   */
  public SpectrumContainerManager(SpectrumPanel owner, AbstractDatabaseConnection dbcon) {
    super();

    m_Owner              = owner;
    m_Reloadable         = true;
    m_ColorProvider      = new DefaultColorProvider();
    m_DatabaseConnection = dbcon;
  }

  /**
   * Returns the owning panel.
   *
   * @return		the owner
   */
  public SpectrumPanel getOwner() {
    return m_Owner;
  }

  /**
   * Returns the currently used database connection object, can be null.
   *
   * @return		the current object
   */
  public AbstractDatabaseConnection getDatabaseConnection() {
    return m_DatabaseConnection;
  }

  /**
   * Sets the database connection object to use.
   *
   * @param value	the object to use
   */
  public void setDatabaseConnection(AbstractDatabaseConnection value) {
    m_DatabaseConnection = value;
  }

  /**
   * Sets whether the spectra are reloadable (from the database) or not.
   *
   * @param value	true if the spectra can be reloaded
   */
  public void setReloadable(boolean value) {
    m_Reloadable = value;
  }

  /**
   * Returns whether the spectra can be reloaded from the database or not.
   *
   * @return		true if the spectra can be reloaded
   */
  public boolean isReloadable() {
    return m_Reloadable;
  }

  /**
   * Sets the color provider to use.
   *
   * @param value	the color provider
   */
  public synchronized void setColorProvider(ColorProvider value) {
    int		i;
    
    m_ColorProvider = value;
    for (i = 0; i < count(); i++)
      get(i).setColor(getColor(get(i)));
  }

  /**
   * Returns the color provider to use.
   *
   * @return		the color provider in use
   */
  public ColorProvider getColorProvider() {
    return m_ColorProvider;
  }

  /**
   * Returns the color for the container.
   *
   * @param cont	the container to get the color for
   * @return		the color
   */
  public Color getColor(SpectrumContainer cont) {
    if (m_ColorProvider instanceof ColorProviderWithNameSupport)
      return ((ColorProviderWithNameSupport) m_ColorProvider).next(cont.getID());
    else
      return m_ColorProvider.next();
  }

  /**
   * Clears the container list.
   */
  @Override
  public void clear() {
    super.clear();

    m_ColorProvider.resetColors();
  }

  /**
   * A post-hook for the add-method, after the container got added to the internal
   * list and the notifications got sent.
   * <br><br>
   * Updates the color, if WHITE.
   *
   * @param c		the container that got added
   */
  @Override
  public void postAdd(SpectrumContainer c) {
    if (c.getColor() == Color.WHITE)
      c.setColor(getColor(c));
    else
      m_ColorProvider.exclude(c.getColor());
  }

  /**
   * Removes the container at the specified position.
   *
   * @param index	the index of the container to remove
   * @return		the container that got removed
   */
  @Override
  public SpectrumContainer remove(int index) {
    if (!m_AllowRemoval)
      return null;
    
    m_ColorProvider.recycle(get(index).getColor());

    return super.remove(index);
  }

  /**
   * Returns a hashset with all the IDs of the currently stored containers.
   *
   * @return		the IDs
   */
  protected HashSet<String> getIDs() {
    HashSet<String>	result;
    int			i;

    result = new HashSet<>();

    for (i = 0; i < count(); i++)
      result.add(get(i).getID());

    return result;
  }

  /**
   * Creates a unique ID from of the given one, if necessary, testing against
   * the specified IDs.
   *
   * @param ids		the IDs to test uniqueness against
   * @param id		the ID to make unique
   * @return		the unique ID
   */
  public synchronized String getUniqueName(HashSet<String> ids, String id) {
    String	result;

    result = id;

    while (ids.contains(result))
      result += "'";

    return result;
  }

  /**
   * Updates the ID of the container, i.e., gives it a unique ID.
   *
   * @param c		the container to process
   * @param old	the old container this one is replacing, can be null
   * @return		the updated container (for convenience)
   */
  public SpectrumContainer updateName(SpectrumContainer c, SpectrumContainer old) {
    HashSet<String>	ids;

    ids = getIDs();
    if (old != null)
      ids.remove(old.getID());
    c.setID(getUniqueName(ids, c.getID()));

    return c;
  }

  /**
   * Returns a new container containing the given payload.
   *
   * @param o		the payload to encapsulate
   * @return		the new container
   */
  @Override
  public SpectrumContainer newContainer(Comparable o) {
    return new SpectrumContainer(this, (Spectrum) o);
  }

  /**
   * A pre-hook for the add method, before a container gets added to the
   * internal list.
   *
   * @param  c	the container to process
   * @return		the processed container
   */
  @Override
  protected SpectrumContainer preAdd(SpectrumContainer c) {
    return updateName(c, null);
  }

  /**
   * A pre-hook for the set method, before the container replaces the item
   * currently occupying the position.
   *
   * @param index	the position to place the container
   * @param c		the container to set
   * @return		the processed container
   */
  @Override
  protected SpectrumContainer preSet(int index, SpectrumContainer c) {
    return updateName(c, get(index));
  }

  /**
   * Returns whether the container at the specified position is visible.
   *
   * @param index	the container's position
   * @return		true if the container is visible
   */
  public boolean isVisible(int index) {
    return get(index).isVisible();
  }

  /**
   * Sets the specified container's visibility. Uses the scripting engine.
   *
   * @param index	the index of the container
   * @param visible	if true then the container will be made visible
   */
  public void setVisible(int index, boolean visible) {
    if (getOwner().getScriptingEngine().isRecording()) {
      if (visible)
	getOwner().getScriptingEngine().add(
	    getOwner(),
	    Visible.ACTION + " " + (index+1));
      else
	getOwner().getScriptingEngine().add(
	    getOwner(),
	    Invisible.ACTION + " " + (index+1));
    }
    else {
      get(index).setVisible(visible);
    }
  }

  /**
   * Returns the nth visible container.
   *
   * @param index	the index (relates only to the visible containers!)
   * @return		the container, null if index out of range
   */
  public SpectrumContainer getVisible(int index) {
    SpectrumContainer	result;
    int			i;
    int			count;

    result = null;
    count  = -1;

    for (i = 0; i < count(); i++) {
      if (isVisible(i))
	count++;
      if (count == index) {
	result = get(i);
	break;
      }
    }

    return result;
  }

  /**
   * Returns the number of visible containers.
   *
   * @return		the number of visible containers
   */
  public int countVisible() {
    int	result;
    int	i;

    result = 0;

    for (i = 0; i < count(); i++) {
      if (isVisible(i))
        result++;
    }

    return result;
  }

  /**
   * Determines the index of the spectrum with the specified ID.
   *
   * @param id	the ID of the spectrum
   * @return		the index of the spectrum or -1 if not found
   */
  public int indexOf(String id) {
    int	result;
    int	i;

    result = -1;

    for (i = 0; i < count(); i++) {
      if (get(i).getID().equals(id)) {
        result = i;
        break;
      }
    }

    return result;
  }

  /**
   * Returns the indices of all visible containers.
   *
   * @return		all containers
   */
  public int[] getVisibleIndices() {
    TIntArrayList	result;
    int			i;

    result = new TIntArrayList();

    for (i = 0; i < count(); i++) {
      if (!isVisible(i))
        continue;
      result.add(i);
    }

    return result.toArray();
  }

  /**
   * Returns (a copy of) all currently stored containers. Those containers
   * have no manager.
   *
   * @return		all containers
   */
  public List<SpectrumContainer> getAllVisible() {
    List<SpectrumContainer>	result;
    SpectrumContainer		cont;
    int				i;

    result = new ArrayList<>();

    for (i = 0; i < count(); i++) {
      if (!isVisible(i))
        continue;
      cont = (SpectrumContainer) get(i).copy();
      cont.setManager(null);
      result.add(cont);
    }

    return result;
  }
  
  /**
   * Returns whether the container matches the current search.
   * 
   * @param cont	the container to check
   * @param search	the search string
   * @param regExp	whether to perform regular expression matching
   */
  @Override
  protected boolean isMatch(SpectrumContainer cont, String search, boolean regExp) {
    if (regExp)
      return cont.getID().matches(search);
    else
      return cont.getID().toLowerCase().contains(search);
  }
}