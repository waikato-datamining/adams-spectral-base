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
 * WaveNumberHitDetector.java
 * Copyright (C) 2009-2017 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.visualization.spectrum;

import adams.data.spectrum.SpectrumPoint;
import adams.gui.scripting.SelectWaveNumber;
import adams.gui.visualization.core.plot.AbstractHitDetector;
import adams.gui.visualization.core.plot.Axis;

import java.awt.event.MouseEvent;

/**
 * Detects selections of wave numbers.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class WaveNumberHitDetector
  extends AbstractHitDetector<Double, Object> {

  /** for serialization. */
  private static final long serialVersionUID = -2638471486048948476L;

  /** the owner of this detector. */
  protected SpectrumPanel m_Owner;

  /**
   * Initializes the hit detector.
   *
   * @param owner
   *          the panel that uses this detector
   */
  public WaveNumberHitDetector(SpectrumPanel owner) {
    super();

    m_Owner = owner;
  }

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Detects selections of wave numbers.";
  }

  /**
   * Returns the owner.
   *
   * @return the owning panel
   */
  public SpectrumPanel getOwner() {
    return m_Owner;
  }

  /**
   * Checks for a hit.
   *
   * @param e		the MouseEvent (for coordinates)
   * @return 		the associated object with the hit, otherwise null
   */
  protected Double isHit(MouseEvent e) {
    Double result;

    result = null;

    if (m_Owner.getContainerManager().count() > 0)
      result = m_Owner.getPlot().getAxis(Axis.BOTTOM).posToValue(e.getX());

    return result;
  }

  /**
   * Performs the action when a hit is detected.
   *
   * @param e		the MouseEvent (for coordinates)
   * @param hit		the object that got determined by the hit
   * @return 		always null
   */
  protected Object processHit(MouseEvent e, Double hit) {
    if (m_Owner.getScriptingEngine().isRecording()) {
      m_Owner.getScriptingEngine().add(
	  m_Owner,
	  SelectWaveNumber.ACTION + " " + hit);
    }
    else {
      select(m_Owner, hit.floatValue());
    }

    return null;
  }

  /**
   * Cleans up data structures, frees up memory.
   */
  public void cleanUp() {
    m_Owner = null;

    super.cleanUp();
  }

  /**
   * Returns the closest spectrum point to the specified wave number across
   * all visible spectra.
   *
   * @param manager	the container manager
   * @param waveno	the wave number to get the closest point for
   * @return		the point, null if none found
   */
  protected static SpectrumPoint findClosestSpectrumPoint(SpectrumContainerManager manager, float waveno) {
    SpectrumPoint	result;
    int			i;
    SpectrumContainer 	cont;
    SpectrumPoint	point;

    result = null;
    point   = null;
    if (manager.count() > 0) {
      for (i = 0; i < manager.count(); i++) {
	cont = manager.get(i);
	if (!cont.isVisible())
	  continue;
	point = cont.getData().findClosest(waveno);
	if (point != null) {
	  if (result == null)
	    result = point;
	  else if (Math.abs(result.getWaveNumber() - waveno) > Math.abs(point.getWaveNumber() - waveno))
	    result = point;
	}
      }
    }

    return result;
  }

  /**
   * Selects a wave number.
   *
   * @param panel	the spectrum panel to operate one
   * @param waveno	the wave number to select
   */
  public static void select(SpectrumPanel panel, float waveno) {
    panel.getSelectedWaveNumberPaintlet().setPoint(findClosestSpectrumPoint(panel.getContainerManager(), waveno));
  }
}
