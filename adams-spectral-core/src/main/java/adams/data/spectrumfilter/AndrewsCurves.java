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
 * AndrewsCurves.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.core.TechnicalInformation;
import adams.core.TechnicalInformation.Field;
import adams.core.TechnicalInformation.Type;
import adams.data.filter.AbstractFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Generates Andrews Curves from array data.<br>
 * César Ignacio García Osorio, Colin Fyfe (2003). AN EXTENSION OF ANDREWS CURVES FOR DATA ANALYSIS.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-no-id-update &lt;boolean&gt; (property: dontUpdateID)
 * &nbsp;&nbsp;&nbsp;If enabled, suppresses updating the ID of adams.data.id.IDHandler data containers.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-num-points &lt;int&gt; (property: numPoints)
 * &nbsp;&nbsp;&nbsp;The number of points to generate for the curves.
 * &nbsp;&nbsp;&nbsp;default: 100
 * &nbsp;&nbsp;&nbsp;minimum: 1
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class AndrewsCurves
  extends AbstractFilter<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -6657349355724261443L;
  
  /** the number of data points. */
  protected int m_NumPoints;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return 
	"Generates Andrews Curves from array data.\n"
	+ getTechnicalInformation().toString();
  }

  /**
   * Returns an instance of a TechnicalInformation object, containing
   * detailed information about the technical background of this class,
   * e.g., paper reference or book this class is based on.
   *
   * @return 		the technical information about this class
   */
  public TechnicalInformation getTechnicalInformation() {
    TechnicalInformation 	result;

    result = new TechnicalInformation(Type.ARTICLE);
    result.setValue(Field.YEAR, "2003");
    result.setValue(Field.AUTHOR, "César Ignacio García Osorio and Colin Fyfe");
    result.setValue(Field.TITLE, "AN EXTENSION OF ANDREWS CURVES FOR DATA ANALYSIS");
    result.setValue(Field.HTTP, "http://cib.uco.es/documents/Garcia03SIGEF.pdf");

    return result;
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "num-points", "numPoints",
	    100, 1, null);
  }

  /**
   * Sets the number of points to generate.
   *
   * @param value	the number of points
   */
  public void setNumPoints(int value) {
    m_NumPoints = value;
    reset();
  }

  /**
   * Returns the number of points to generate.
   *
   * @return		the number of points
   */
  public int getNumPoints() {
    return m_NumPoints;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String numPointsTipText() {
    return "The number of points to generate for the curves.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  @Override
  protected Spectrum processData(Spectrum data) {
    Spectrum		result;
    int			i;
    int			n;
    List<SpectrumPoint>	points;
    float		t;
    float		y;

    result = data.getHeader();
    for (i = 0; i < m_NumPoints; i++)
      result.add(new SpectrumPoint(i, 0));

    points = data.toList();
    for (i = 0; i < m_NumPoints; i++) {
      t = (float) (-Math.PI + (Math.PI * 2) / m_NumPoints * i);
      y = (float) (points.get(0).getAmplitude() / Math.sqrt(2));
      for (n = 1; n < points.size(); n++) {
	if ((n + 1) % 2 == 0)
	  y += points.get(n).getAmplitude() * Math.sin(t * Math.ceil(n / 2));
	else
	  y += points.get(n).getAmplitude() * Math.cos(t * Math.ceil(n / 2));
      }
      result.toList().get(i).setAmplitude(y);
    }

    return result;
  }
}
