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
 * SampleDataFileWriter.java
 * Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.data.io.output.AbstractReportWriter;
import adams.data.io.output.SimpleSampleDataWriter;
import adams.data.sampledata.SampleData;

/**
 <!-- globalinfo-start -->
 * Saves a quantitation report to disk with the specified writer and passes the absolute filename on.<br>
 * As filename/directory name (depending on the writer) the database ID of the report is used (below the specified output directory).
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input/output:<br>
 * - accepts:<br>
 * <pre>   knir.data.sampledata.SampleData</pre>
 * - generates:<br>
 * <pre>   java.lang.String</pre>
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 *         The name of the actor.
 *         default: SampleDataFileWriter
 * </pre>
 *
 * <pre>-annotation &lt;adams.core.base.BaseText&gt; (property: annotations)
 *         The annotations to attach to this actor.
 *         default:
 * </pre>
 *
 * <pre>-skip (property: skip)
 *         If set to true, transformation is skipped and the input token is just forwarded
 *          as it is.
 * </pre>
 *
 * <pre>-writer &lt;adams.data.io.output.AbstractReportWriter [options]&gt; (property: writer)
 *         The writer to use for saving the reports.
 *         default: knir.data.output.SimpleSampleDataWriter -output ${TMP}/out.chrom
 * </pre>
 *
 * <pre>-dir &lt;adams.core.io.PlaceholderFile&gt; (property: outputDir)
 *         The output directory for the reports.
 *         default: .
 * </pre>
 *
 * Default options for knir.data.output.SimpleSampleDataWriter (-writer/writer):
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-output &lt;adams.core.io.PlaceholderFile&gt; (property: output)
 *         The file to write the report to.
 *         default: ${TMP}/out.chrom
 * </pre>
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SampleDataFileWriter
  extends AbstractReportFileWriter<SampleData> {

  /** for serialization. */
  private static final long serialVersionUID = -5209437097716008045L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return
        "Saves a quantitation report to disk with the "
      + "specified writer and passes the absolute filename on.\n"
      + "As filename/directory name (depending on the writer) the "
      + "database ID of the report is used (below the specified output "
      + "directory).";
  }

  /**
   * Returns the default writer.
   *
   * @return		the writer
   */
  protected AbstractReportWriter<SampleData> getDefaultWriter() {
    return new SimpleSampleDataWriter();
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the type of report
   */
  public Class[] accepts() {
    return new Class[]{SampleData.class};
  }
}
