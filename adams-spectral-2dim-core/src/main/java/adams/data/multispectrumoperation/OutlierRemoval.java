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
 * OutlierRemoval.java
 * Copyright (C) 2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.multispectrumoperation;

import adams.core.MessageCollection;
import adams.core.QuickInfoHelper;
import adams.data.multispectrumoperation.outlierremoval.AbstractOutlierRemoval;
import adams.data.multispectrumoperation.outlierremoval.PassThrough;
import adams.data.spectrum.MultiSpectrum;

/**
 * Applies the specified outlier removal scheme to the multi-spectrum to remove potential outliers.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public class OutlierRemoval
  extends AbstractMultiSpectrumOperation {

  private static final long serialVersionUID = 5371840724095272683L;

  /** the outlier removal scheme to use. */
  protected AbstractOutlierRemoval m_OutlierRemoval;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Applies the specified outlier removal scheme to the multi-spectrum to remove potential outliers.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "outlier-removal", "outlierRemoval",
      new PassThrough());
  }

  /**
   * Sets the outlier removal scheme to apply.
   *
   * @param value	the removal scheme
   */
  public void setOutlierRemoval(AbstractOutlierRemoval value) {
    m_OutlierRemoval = value;
    reset();
  }

  /**
   * Returns the outlier removal scheme to apply.
   *
   * @return 		the removal scheme
   */
  public AbstractOutlierRemoval getOutlierRemoval() {
    return m_OutlierRemoval;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String outlierRemovalTipText() {
    return "The outlier removal scheme to apply.";
  }

  /**
   * Returns a quick info about the object, which can be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    return QuickInfoHelper.toString(this, "outlierRemoval", m_OutlierRemoval, "removal: ");
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data, null if failed to generate output
   */
  @Override
  protected MultiSpectrum doApply(MultiSpectrum data, MessageCollection errors) {
    return m_OutlierRemoval.removeOutliers(data, errors);
  }
}
