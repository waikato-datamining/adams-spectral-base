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
 * SpectrumImageWriter.java
 * Copyright (C) 2009 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.sink;

import adams.core.io.FileUtils;
import adams.core.io.PlaceholderFile;
import adams.data.spectrum.Spectrum;
import adams.gui.visualization.spectrum.SpectrumContainer;
import adams.gui.visualization.spectrum.SpectrumPanel;

import javax.swing.JComponent;

/**
 <!-- globalinfo-start -->
 * Actor that takes screenshots of spectra.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input/output:<br>
 * - accepts:<br>
 * <pre>   knir.data.spectrum.Spectrum</pre>
 * <pre>   knir.data.spectrum.Spectrum[]</pre>
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
 *         default: SpectrumImageWriter
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
 * <pre>-title &lt;java.lang.String&gt; (property: title)
 *         The title of the dialog.
 *         default: Spectrum
 * </pre>
 *
 * <pre>-suffix &lt;java.lang.String&gt; (property: suffix)
 *         An optional suffix for the filename, inserted before the extension.
 *         default:
 * </pre>
 *
 * <pre>-width &lt;int&gt; (property: width)
 *         The width of the dialog.
 *         default: 800
 * </pre>
 *
 * <pre>-height &lt;int&gt; (property: height)
 *         The height of the dialog.
 *         default: 600
 * </pre>
 *
 * <pre>-output &lt;adams.core.io.PlaceholderFile&gt; (property: output)
 *         The output directory.
 *         default: .
 * </pre>
 *
 * <pre>-writer &lt;adams.gui.print.JComponentWriter [options]&gt; (property: writer)
 *         The writer to use for generating the graphics output.
 *         default: adams.gui.print.NullWriter -file . -scale-x 1.0 -scale-y 1.0 -custom-width -1 -custom-height -1
 * </pre>
 *
 * Default options for adams.gui.print.NullWriter (-writer/writer):
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-file &lt;adams.core.io.PlaceholderFile&gt; (property: file)
 *         The file to save the image to.
 *         default: .
 * </pre>
 *
 * <pre>-scaling (property: scalingEnabled)
 *         If set to true, then scaling will be used.
 * </pre>
 *
 * <pre>-scale-x &lt;double&gt; (property: XScale)
 *         The scaling factor for the X-axis.
 *         default: 1.0
 * </pre>
 *
 * <pre>-scale-y &lt;double&gt; (property: YScale)
 *         The scaling factor for the Y axis.
 *         default: 1.0
 * </pre>
 *
 * <pre>-custom-dimensions (property: useCustomDimensions)
 *         Whether to use custom dimensions or use the component's ones.
 * </pre>
 *
 * <pre>-custom-width &lt;int&gt; (property: customWidth)
 *         The custom width.
 *         default: -1
 * </pre>
 *
 * <pre>-custom-height &lt;int&gt; (property: customHeight)
 *         The custom height.
 *         default: -1
 * </pre>
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SpectrumImageWriter
  extends AbstractGraphicsGenerator {

  /** for serialization. */
  private static final long serialVersionUID = -5557890430966383263L;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return "Actor that takes screenshots of spectra.";
  }

  /**
   * Returns the default title for the dialog.
   *
   * @return		the default title
   */
  protected String getDefaultTitle() {
    return "Spectrum";
  }

  /**
   * Returns the default width for the dialog.
   *
   * @return		the default width
   */
  protected int getDefaultWidth() {
    return 800;
  }

  /**
   * Returns the default height for the dialog.
   *
   * @return		the default height
   */
  protected int getDefaultHeight() {
    return 600;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String outputTipText() {
    return "The output directory.";
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		<!-- flow-accepts-start -->knir.data.spectrum.Spectrum.class, knir.data.spectrum.Spectrum[].class<!-- flow-accepts-end -->
   */
  public Class[] accepts() {
    return new Class[]{Spectrum.class, Spectrum[].class};
  }

  /**
   * Generates the filename for the output.
   *
   * @return		the filename
   */
  protected PlaceholderFile generateFilename() {
    PlaceholderFile	result;
    Spectrum[]		spectra;
    int			i;
    String		idStr;

    // get data
    if (m_InputToken.getPayload() instanceof Spectrum)
      spectra = new Spectrum[]{(Spectrum) m_InputToken.getPayload()};
    else
      spectra = (Spectrum[]) m_InputToken.getPayload();

    idStr = "";
    for (i = 0; i < spectra.length; i++) {
      if (i > 0)
	idStr += "_";
      idStr += spectra[i].getDatabaseID();
    }

    result = new PlaceholderFile(
	m_Output,
	FileUtils.createFilename(idStr + m_Suffix + m_Writer.getExtensions()[0], "_"));

    return result;
  }

  /**
   * Generates the component to display in the frame.
   *
   * @return		the component
   */
  protected JComponent generateComponent() {
    Spectrum[]		spectra;
    int			i;
    SpectrumPanel	result;
    SpectrumContainer	cont;

    // get data
    if (m_InputToken.getPayload() instanceof Spectrum)
      spectra = new Spectrum[]{(Spectrum) m_InputToken.getPayload()};
    else
      spectra = (Spectrum[]) m_InputToken.getPayload();

    // display data
    result = new SpectrumPanel(getDefaultTitle());
    result.setSize(m_Width, m_Height);
    result.getContainerManager().startUpdate();
    for (i = 0; i < spectra.length; i++) {
      cont = result.getContainerManager().newContainer(spectra[i]);
      result.getContainerManager().add(cont);
    }
    result.getContainerManager().finishUpdate();

    return result;
  }

  /**
   * Disposes the generated component again.
   */
  protected void disposeComponent() {
    ((SpectrumPanel) m_Component).getContainerManager().clear();
    super.disposeComponent();
  }
}
