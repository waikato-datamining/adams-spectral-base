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
 * SpectrumPointHitDetector.java
 * Copyright (C) 2009-2017 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.visualization.spectrum;

import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spectrum.SpectrumUtils;
import adams.gui.visualization.core.AxisPanel;
import adams.gui.visualization.core.plot.AbstractDistanceBasedHitDetector;
import adams.gui.visualization.core.plot.Axis;
import adams.gui.visualization.core.plot.ContainerHitDetector;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Detects selections of spectrum points in the spectrum panel.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumPointHitDetector
  extends AbstractDistanceBasedHitDetector<List<SpectrumPoint>, String>
  implements ContainerHitDetector<List<SpectrumPoint>, String, SpectrumContainer> {

  /** for serialization. */
  private static final long serialVersionUID = 7459498872766468963L;

  /** the owner of this detector. */
  protected SpectrumPanel m_Owner;

  /**
   * Initializes the hit detector.
   *
   * @param owner	the panel that uses this detector
   */
  public SpectrumPointHitDetector(SpectrumPanel owner) {
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
    return "Detects selections of spectrum points in the spectrum panel.";
  }

  /**
   * Returns the owner.
   *
   * @return		the owning panel
   */
  public SpectrumPanel getOwner() {
    return m_Owner;
  }

  /**
   * Checks for a hit.
   * <br><br>
   * For calculating distance between point and line, see <a href="http://local.wasp.uwa.edu.au/~pbourke/geometry/pointline/">here</a>
   *
   * @param e		the MouseEvent (for coordinates)
   * @return		the associated object with the hit, otherwise null
   */
  @Override
  protected List<SpectrumPoint> isHit(MouseEvent e) {
    List<SpectrumPoint>		result;
    float			amplitude;
    float			waveno;
    float			diffAmplitude;
    float			diffWaveno;
    float			diffPixel;
    int				i;
    Spectrum			s;
    SpectrumPoint 		sp;
    SpectrumPoint		sp2;
    AxisPanel			axisBottom;
    AxisPanel			axisLeft;
    int[]			indices;
    int				index;
    double			dist;
    List<SpectrumPoint> 	points;
    SpectrumContainerModel	model;

    result     = new ArrayList<>();
    axisBottom = m_Owner.getPlot().getAxis(Axis.BOTTOM);
    axisLeft   = m_Owner.getPlot().getAxis(Axis.LEFT);
    amplitude  = (float) axisLeft.posToValue((int) e.getY());
    waveno     = (float) axisBottom.posToValue((int) e.getX());
    model      = (SpectrumContainerModel) m_Owner.getContainerList().getContainerModel();

    for (i = 0; i < model.getRowCount(); i++) {
      if (!model.getContainerAt(i).isVisible())
	continue;

      // check for hit
      s       = model.getContainerAt(i).getData();
      points  = s.toList();
      indices = SpectrumUtils.findEnclosingWaveNumbers(points, waveno);

      if (getDebug())
	getLogger().info("\n" + s.getID() + ":");

      // do we have only one point available?
      if ((indices[0] == -1) || (indices[1] == -1)) {
	index = SpectrumUtils.findClosestWaveNumber(points, waveno);
	if (index == -1)
	  continue;
	sp = points.get(index);

	// do X and Y fit?
	diffWaveno = sp.getWaveNumber() - waveno;
	diffPixel     = Math.abs(axisBottom.valueToPos(diffWaveno) - axisBottom.valueToPos(0));
	if (getDebug())
	  getLogger().info("diff waveno=" + diffPixel);
	if (diffPixel > m_MinimumPixelDifference)
	  continue;
	diffAmplitude = sp.getAmplitude() - amplitude;
	diffPixel     = Math.abs(axisLeft.valueToPos(diffAmplitude) - axisLeft.valueToPos(0));
	if (getDebug())
	  getLogger().info("diff amplitude=" + diffPixel);
	if (diffPixel > m_MinimumPixelDifference)
	  continue;
      }
      else {
	sp  = points.get(indices[0]);
	sp2 = points.get(indices[1]);
	dist = distance(
	    	new Point2D.Double(axisBottom.valueToPos(sp.getWaveNumber()), axisLeft.valueToPos(sp.getAmplitude())),
	    	new Point2D.Double(axisBottom.valueToPos(sp2.getWaveNumber()), axisLeft.valueToPos(sp2.getAmplitude())),
	    	new Point2D.Double(e.getX(), e.getY()));
	if (getDebug())
	  getLogger().info("dist line=" + dist);
	if (dist > m_MinimumPixelDifference)
	  continue;
      }

      // add hit
      if (getDebug())
	getLogger().info("hit!");
      result.add(sp);
    }

    if (result.size() > 0)
      return result;
    else
      return null;
  }

  /**
   * Performs the action when a hit is detected.
   *
   * @param e		the MouseEvent (for coordinates)
   * @param hit		the object that got determined by the hit
   * @return		the generated appendix for the tiptext
   */
  @Override
  protected String processHit(MouseEvent e, List<SpectrumPoint> hit) {
    String			result;
    int				i;
    Spectrum 			sp;
    SpectrumContainer 		cont;

    result = " (";
    for (i = 0; i < hit.size(); i++) {
      if (i > 0)
	result += ", ";
      sp  = (Spectrum) hit.get(i).getParent();
      cont = m_Owner.getContainerManager().newContainer(sp);
      result += cont.getDisplayID();
    }
    result += ")";

    return result;
  }

  /**
   * Detects hits and associates them with the containers.
   *
   * @param e		the mouse event to analyze for a hit
   * @return		optional result of processing the event
   */
  @Override
  public SpectrumContainer[] containers(MouseEvent e) {
    List<SpectrumContainer>	result;
    List<SpectrumPoint> 	hit;
    SpectrumContainerManager	manager;
    int				index;

    result  = new ArrayList<>();
    hit     = isHit(e);
    if (hit == null)
      return new SpectrumContainer[0];

    manager = getOwner().getContainerManager();
    for (SpectrumPoint point: hit) {
      index = manager.indexOf(point.getParent().getID());
      if (index > -1)
        result.add(manager.get(index));
    }

    return result.toArray(new SpectrumContainer[result.size()]);
  }

  /**
   * Cleans up data structures, frees up memory.
   */
  @Override
  public void cleanUp() {
    m_Owner = null;

    super.cleanUp();
  }
}
