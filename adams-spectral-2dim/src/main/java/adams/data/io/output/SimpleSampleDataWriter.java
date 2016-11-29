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
 * SimpleSampleDataWriter.java
 * Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.output;

import adams.data.sampledata.SampleData;

/**
 <!-- globalinfo-start -->
 * Writes reports in properties file format.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-output &lt;adams.core.io.PlaceholderFile&gt; (property: output)
 *         The file to write the report to.
 *         default: ${TMP}/out.chrom
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SimpleSampleDataWriter
  extends AbstractSimpleReportWriter<SampleData>
  implements SampleDataWriter {

  /** for serialization. */
  private static final long serialVersionUID = 1281189381638349284L;
}
