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
 * SpectrumFileChecker.java
 * Copyright (C) 2010-2025 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.control;

import adams.data.io.input.DataContainerReader;
import adams.data.io.input.SimpleSpectrumReader;
import adams.data.spectrum.Spectrum;

/**
 <!-- globalinfo-start -->
 * Only passes on files&#47;directories containing spectra that could be loaded successfully.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;java.lang.String<br>
 * &nbsp;&nbsp;&nbsp;java.lang.String[]<br>
 * &nbsp;&nbsp;&nbsp;java.io.File<br>
 * &nbsp;&nbsp;&nbsp;java.io.File[]<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;java.lang.String<br>
 * &nbsp;&nbsp;&nbsp;java.lang.String[]<br>
 * &nbsp;&nbsp;&nbsp;java.io.File<br>
 * &nbsp;&nbsp;&nbsp;java.io.File[]<br>
 * <br><br>
 <!-- flow-summary-end -->
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
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: SpectrumFileChecker
 * </pre>
 *
 * <pre>-annotation &lt;adams.core.base.BaseText&gt; (property: annotations)
 * &nbsp;&nbsp;&nbsp;The annotations to attach to this actor.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-skip (property: skip)
 * &nbsp;&nbsp;&nbsp;If set to true, transformation is skipped and the input token is just forwarded
 * &nbsp;&nbsp;&nbsp;as it is.
 * </pre>
 *
 * <pre>-stop-flow-on-error (property: stopFlowOnError)
 * &nbsp;&nbsp;&nbsp;If set to true, the flow gets stopped in case this actor encounters an error;
 * &nbsp;&nbsp;&nbsp; useful for critical actors.
 * </pre>
 *
 * <pre>-progress (property: showProgress)
 * &nbsp;&nbsp;&nbsp;If set to true, progress information will be output to stdout ('.').
 * </pre>
 *
 * <pre>-tee &lt;adams.flow.core.AbstractActor [options]&gt; (property: teeActor)
 * &nbsp;&nbsp;&nbsp;The actor to siphon-off the tokens to.
 * &nbsp;&nbsp;&nbsp;default: adams.flow.sink.Null
 * </pre>
 *
 * <pre>-reader &lt;adams.data.io.input.DataContainerReader [options]&gt; (property: reader)
 * &nbsp;&nbsp;&nbsp;The reader to use for reading the files being passed through.
 * &nbsp;&nbsp;&nbsp;default: knir.data.input.SimpleSpectrumReader
 * </pre>
 *
 * <pre>-expiry-interval &lt;adams.core.base.BaseDateTime&gt; (property: expiryInterval)
 * &nbsp;&nbsp;&nbsp;The time interval before black-listed items are moved from the temporary
 * &nbsp;&nbsp;&nbsp;list to the final list; requires the keyword 'START' in the expression.
 * &nbsp;&nbsp;&nbsp;default: START +24 HOUR
 * </pre>
 *
 * <pre>-check-interval &lt;adams.core.base.BaseTime&gt; (property: checkInterval)
 * &nbsp;&nbsp;&nbsp;The time interval after which black-listed items in the temporary list are
 * &nbsp;&nbsp;&nbsp;checked again whether they finally load correctly; requires the keyword '
 * &nbsp;&nbsp;&nbsp;START' in the expression.
 * &nbsp;&nbsp;&nbsp;default: START +15 MINUTE
 * </pre>
 *
 * <pre>-log &lt;adams.core.io.PlaceholderFile&gt; (property: log)
 * &nbsp;&nbsp;&nbsp;The log file to write the files to that expired from the final black-list,
 * &nbsp;&nbsp;&nbsp; ie, never being loaded correctly; log gets ignored if pointing to a directory.
 * &nbsp;&nbsp;&nbsp;default: .
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumFileChecker
  extends AbstractDataContainerFileChecker<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -4514188922448266267L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return
        "Only passes on files/directories containing spectra that could "
      + "be loaded successfully.";
  }

  /**
   * Returns the default reader for loading the data.
   *
   * @return		the default reader
   */
  protected DataContainerReader<Spectrum> getDefaultReader() {
    return new SimpleSpectrumReader();
  }
}
