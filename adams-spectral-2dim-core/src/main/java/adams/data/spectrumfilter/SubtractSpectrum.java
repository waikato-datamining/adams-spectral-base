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
 * SubtractSpectrum.java
 * Copyright (C) 2008-2024 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractDatabaseConnectionFilter;
import adams.data.filter.Filter;
import adams.data.filter.PassThrough;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.db.AbstractDatabaseConnection;
import adams.db.DatabaseConnection;
import adams.db.SpectrumF;

import java.util.List;

/**
 <!-- globalinfo-start -->
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SubtractSpectrum
  extends AbstractDatabaseConnectionFilter<Spectrum> {

  private static final long serialVersionUID = 3414623712198778099L;

  /**
   * Returns the default database connection.
   *
   * @return		the default database connection
   */
  protected AbstractDatabaseConnection getDefaultDatabaseConnection() {
    return DatabaseConnection.getSingleton();
  }

  /** the filter to run. */
  protected Filter<Spectrum> m_Filter;
  
  protected int m_id=69052;

  /**
   * Returns a string describing the object.
   *
   * @return 		a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return "Subtract a spectrum, after filtering.";
  }
  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	"filter", "filter",
	new PassThrough());
  }

  /**
   * Sets the filter to run.
   *
   * @param value 	the filter
   */
  public void setFilter(Filter value) {
    m_Filter = value;
  }

  /**
   * Returns the filter being used.
   *
   * @return 		the filter
   */
  public Filter getFilter() {
    return m_Filter;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String filterTipText() {
    return "The filter to use.";
  }
  


  

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  protected Spectrum processData(Spectrum data) {
    Spectrum		result;
    int			i;
   
    Spectrum f1=m_Filter.filter(data);
    Spectrum sp=SpectrumF.getSingleton(getDatabaseConnection()).load(m_id);
    Spectrum f2=m_Filter.filter(sp);
   
    result = data.getHeader();
    List<SpectrumPoint> list1 = f1.toList();
    List<SpectrumPoint> list2 = f2.toList();

    for (i = 0; i < list1.size(); i++) {
      // scale point
      SpectrumPoint point1    = list1.get(i);
      SpectrumPoint point2    = list2.get(i);
     
      SpectrumPoint pointNew = new SpectrumPoint(
	  		point1.getWaveNumber(),
	  		(float) ((point1.getAmplitude() -point2.getAmplitude())));

      // add to output
      result.add(pointNew);
    }

    return result;
  }
}
