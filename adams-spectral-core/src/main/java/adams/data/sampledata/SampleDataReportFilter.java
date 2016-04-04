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

/**
 * SampleDataReportFilter.java
 * Copyright (C) 2012 University of Waikato, Hamilton, New Zealand
 */
package adams.data.sampledata;

import adams.data.report.AbstractField;
import adams.data.report.AbstractFilteredReportFilter;
import adams.data.report.DataType;
import adams.data.report.Field;

/**
 <!-- globalinfo-start -->
 * Ensures that all important sample data values are kept when applying a report filter.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 * 
 * <pre>-D &lt;int&gt; (property: debugLevel)
 * &nbsp;&nbsp;&nbsp;The greater the number the more additional info the scheme may output to 
 * &nbsp;&nbsp;&nbsp;the console (0 = off).
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 * 
 * <pre>-filter &lt;adams.data.report.AbstractReportFilter&gt; (property: filter)
 * &nbsp;&nbsp;&nbsp;The filter to apply to the sample data.
 * &nbsp;&nbsp;&nbsp;default: adams.data.report.PassThrough
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SampleDataReportFilter
  extends AbstractFilteredReportFilter {

  /** for serialization. */
  private static final long serialVersionUID = -8971460862602084009L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return 
	"Ensures that all important sample data values are kept when applying "
	+ "a report filter.";
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the gui
   */
  @Override
  public String filterTipText() {
    return "The filter to apply to the sample data.";
  }

  /**
   * Returns the fields to keep.
   */
  @Override
  protected AbstractField[] getFields() {
    return new Field[]{
	new Field(SampleData.SAMPLE_ID, DataType.STRING),
	new Field(SampleData.SAMPLE_TYPE, DataType.STRING),
	new Field(SampleData.INSERT_TIMESTAMP, DataType.STRING),
	new Field(SampleData.INSTRUMENT, DataType.STRING),
	new Field(SampleData.FORMAT, DataType.STRING),
	new Field(SampleData.SOURCE, DataType.STRING)
    };
  }
}
