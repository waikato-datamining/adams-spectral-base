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
 * MultiSpectrumOutlierDetector.java
 * Copyright (C) 2015-2017 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.transformer;

import adams.core.QuickInfoHelper;
import adams.data.multifilter.AbstractMultiSpectrumFilter;
import adams.data.outlier.AbstractOutlierDetector;
import adams.data.outlier.PassThrough;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;
import adams.db.DatabaseConnectionHandler;
import adams.db.DatabaseConnectionUser;
import adams.flow.core.ActorUtils;
import adams.flow.core.Token;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Uses the specified multi-spectrum filter to obtain a single spectrum from the multi-spectrum and applies the outlier detector to it.<br>
 * Any resulting detections get added to the multi-spectrum itself.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input&#47;output:<br>
 * - accepts:<br>
 * &nbsp;&nbsp;&nbsp;knir.data.spectrum.MultiSpectrum<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;knir.data.spectrum.MultiSpectrum<br>
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: MultiSpectrumOutlierDetector
 * </pre>
 * 
 * <pre>-annotation &lt;adams.core.base.BaseAnnotation&gt; (property: annotations)
 * &nbsp;&nbsp;&nbsp;The annotations to attach to this actor.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-skip &lt;boolean&gt; (property: skip)
 * &nbsp;&nbsp;&nbsp;If set to true, transformation is skipped and the input token is just forwarded 
 * &nbsp;&nbsp;&nbsp;as it is.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-stop-flow-on-error &lt;boolean&gt; (property: stopFlowOnError)
 * &nbsp;&nbsp;&nbsp;If set to true, the flow gets stopped in case this actor encounters an error;
 * &nbsp;&nbsp;&nbsp; useful for critical actors.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-silent &lt;boolean&gt; (property: silent)
 * &nbsp;&nbsp;&nbsp;If enabled, then no errors are output in the console.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-filter &lt;knir.data.multifilter.AbstractMultiSpectrumFilter&gt; (property: filter)
 * &nbsp;&nbsp;&nbsp;The filter to use.
 * &nbsp;&nbsp;&nbsp;default: knir.data.multifilter.PickByIndex
 * </pre>
 * 
 * <pre>-detector &lt;adams.data.outlier.AbstractOutlierDetector&gt; (property: detector)
 * &nbsp;&nbsp;&nbsp;The outlier detector to use.
 * &nbsp;&nbsp;&nbsp;default: adams.data.outlier.PassThrough
 * </pre>
 * 
 * <pre>-only-warning &lt;boolean&gt; (property: onlyWarning)
 * &nbsp;&nbsp;&nbsp;If enabled, the detections get added merely as warnings instead of as errors.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class MultiSpectrumOutlierDetector
  extends AbstractTransformer
  implements DatabaseConnectionUser {

  /** for serialization. */
  private static final long serialVersionUID = -8678582872628608282L;
  
  /** the filter to apply for extracting the spectrum to apply the outlier detector to. */
  protected AbstractMultiSpectrumFilter m_Filter;

  /** the outlier detector to apply. */
  protected AbstractOutlierDetector m_Detector;

  /** whether the detection is only added as warning instead of error. */
  protected boolean m_OnlyWarning;

  /** whether the database connection has been updated. */
  protected boolean m_DatabaseConnectionUpdated;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Uses the specified multi-spectrum filter to obtain a single spectrum "
        + "from the multi-spectrum and applies the outlier detector to it.\n"
        + "Any resulting detections get added to the multi-spectrum itself.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "filter", "filter",
	    new adams.data.multifilter.PickByIndex());

    m_OptionManager.add(
	    "detector", "detector",
	    new PassThrough());

    m_OptionManager.add(
	    "only-warning", "onlyWarning",
	    false);
  }

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();

    m_DatabaseConnectionUpdated = false;
  }

  /**
   * Sets the filter to use.
   *
   * @param value	the filter
   */
  public void setFilter(AbstractMultiSpectrumFilter value) {
    m_Filter = value;
    reset();
  }

  /**
   * Returns the filter in use.
   *
   * @return		the filter
   */
  public AbstractMultiSpectrumFilter getFilter() {
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
   * Sets the outlier detector to use.
   *
   * @param value	the detector
   */
  public void setDetector(AbstractOutlierDetector value) {
    m_Detector = value;
    reset();
  }

  /**
   * Returns the outlier detector in use.
   *
   * @return		the detector
   */
  public AbstractOutlierDetector getDetector() {
    return m_Detector;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String detectorTipText() {
    return "The outlier detector to use.";
  }

  /**
   * Sets whether the detections are added as error or warning.
   *
   * @param value	if true then the detections are added as warning
   * 			instead of as error
   */
  public void setOnlyWarning(boolean value) {
    m_OnlyWarning = value;
    reset();
  }

  /**
   * Returns whether the detections are added as error or warning.
   *
   * @return 		true if the detections get added as warning instead
   * 			of as error
   */
  public boolean getOnlyWarning() {
    return m_OnlyWarning;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for displaying in
   * 			the GUI or for listing the options.
   */
  public String onlyWarningTipText() {
    return "If enabled, the detections get added merely as warnings instead of as errors.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;
    String      value;
    
    result  = QuickInfoHelper.toString(this, "filter", m_Filter, "filter: ");
    result += QuickInfoHelper.toString(this, "detector", m_Detector, ", detector: ");
    value = QuickInfoHelper.toString(this, "onlyWarning", m_OnlyWarning, "warning only", ", ");
    if (value != null)
      result += value;

    return result;
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the default DataContainer class for the project
   */
  public Class[] accepts() {
    return new Class[]{MultiSpectrum.class};
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the default DataContainer class for the project
   */
  public Class[] generates() {
    return new Class[]{MultiSpectrum.class};
  }

  /**
   * Determines the database connection in the flow.
   *
   * @return		the database connection to use
   */
  protected adams.db.AbstractDatabaseConnection getDatabaseConnection() {
    return ActorUtils.getDatabaseConnection(
	  this,
	  adams.flow.standalone.DatabaseConnectionProvider.class,
	  adams.db.DatabaseConnection.getSingleton());
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String		      result;
    MultiSpectrum	      multi;
    Spectrum		      spec;
    AbstractOutlierDetector   detector;
    List<String>              detection;
    int                       i;

    result = null;

    if (!m_DatabaseConnectionUpdated) {
      m_DatabaseConnectionUpdated = true;
      if (m_Filter instanceof DatabaseConnectionHandler)
	((DatabaseConnectionHandler) m_Filter).setDatabaseConnection(getDatabaseConnection());
    }

    multi = (MultiSpectrum) m_InputToken.getPayload();
    spec  = m_Filter.filter(multi);
    if (spec != null) {
      spec      = (Spectrum) spec.getClone();
      detector = m_Detector.shallowCopy(true);
      detection = detector.detect(spec);
      detector.destroy();
      if (isLoggingEnabled())
        getLogger().info("Data: " + multi + ", detection size: " + detection.size());
      if (detection.size() > 0) {
        multi = (MultiSpectrum) multi.getClone();
        for (i = 0; i < detection.size(); i++) {
          if (m_OnlyWarning)
            multi.getNotes().addWarning(m_Detector.getClass(), detection.get(i));
          else
            multi.getNotes().addError(m_Detector.getClass(), detection.get(i));
          if (isLoggingEnabled())
            getLogger().info((i + 1) + ". " + detection.get(i));
        }
      }
      m_OutputToken = new Token(multi);
    }
    else {
      getLogger().warning("Failed to obtain spectrum from " + multi + " using " + m_Filter);
    }

    m_Filter.cleanUp();

    return result;
  }
}
