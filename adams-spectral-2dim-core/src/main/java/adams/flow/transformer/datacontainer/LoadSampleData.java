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
 * LoadSampleData.java
 * Copyright (C) 2013 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.transformer.datacontainer;

import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.db.SpectrumT;

/**
 * Obtains the reference data from the spectrum with the same sample ID
 * and the specified format.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 6910 $
 */
public class LoadSampleData
  extends AbstractDataContainerPostProcessor<Spectrum> {

  /** for serialization. */
  private static final long serialVersionUID = -3030813869957150389L;

  /** the format to obtain the reference data from. */
  protected String m_Format;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Obtains the reference data from the spectrum with the same sample ID "
        + "and the specified format and attaches it to the current one.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "format", "format",
      SampleData.DEFAULT_FORMAT);
  }

  /**
   * Sets the format of the spectrum to obtain the reference data from.
   *
   * @param value	the format
   */
  public void setFormat(String value){
    m_Format = value;
    reset();
  }

  /**
   * Returns the format of the spectrum to obtain the reference data from.
   *
   * @return		the format
   */
  public String getFormat(){
    return m_Format;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String formatTipText() {
    return "The format of the spectrum to obtain the reference data from.";
  }

  /**
   * Performs the actual post-processing.
   * 
   * @param data	the data to process
   * @return		the processed data
   */
  @Override
  protected Spectrum doPostProcess(Spectrum data) {
    Spectrum		temp;
    SpectrumT provider;

    provider = (SpectrumT) getOwner().getDataProvider();
    temp     = provider.load(data.getID(), m_Format);
    if (temp == null) {
      getLogger().warning("Failed to load: " + data.getID() + "/" + m_Format);
    }
    else {
      if(temp.hasReport())
	data.getReport().mergeWith(temp.getReport());
      else
	getLogger().warning("No report: " + temp);
    }

    return data;
  }
}
