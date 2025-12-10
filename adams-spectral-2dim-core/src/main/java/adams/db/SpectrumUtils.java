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
 * SpectrumUtils.java
 * Copyright (C) 2025 University of Waikato, Hamilton, New Zealand
 */

package adams.db;

import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * Helper class for Spectrum DB calls.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumUtils {

  /**
   * Turns a ResultSet into a spectrum.
   *
   * @param rs		the ResultSet to use
   * @param sampleData	the handler for loading the sample data
   * @return		the spectrum, null in case of an error
   * @throws Exception	if something goes wrong
   */
  public static Spectrum resultsetToSpectrum(ResultSet rs, SampleDataIntf sampleData) throws Exception {
    Spectrum			result;
    int				auto_id;
    String[]			points;
    String[]			point;
    int				i;
    SpectrumPoint sp;
    ArrayList<SpectrumPoint> list;

    result = null;

    if ((rs != null) && (rs.next())) {
      result = new Spectrum();
      result.setID(rs.getString("SAMPLEID"));
      auto_id = rs.getInt("AUTO_ID");
      result.setDatabaseID(auto_id);
      points = rs.getString("POINTS").split(",");
      list   = new ArrayList<>(points.length + 1);
      for (i = 0; i < points.length; i++) {
	if (points[i].indexOf(':') == -1) {
	  sp = new SpectrumPoint(i, Float.parseFloat(points[i]));
	}
	else {
	  point = points[i].split(":");
	  sp = new SpectrumPoint(Float.parseFloat(point[0]), Float.parseFloat(point[1]));
	}
	list.add(sp);
      }
      result.addAll(list);
      list.clear();
      result.setReport(sampleData.load(result.getID()));
      result.setType(rs.getString("SAMPLETYPE"));
      result.setFormat(rs.getString("FORMAT"));
    }

    return result;
  }

}
