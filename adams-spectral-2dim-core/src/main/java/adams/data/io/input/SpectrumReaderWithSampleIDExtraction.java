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
 * SpectrumReaderWithSampleIDExtraction.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input;

import adams.data.io.input.sampleidextraction.SampleIDExtraction;

/**
 * Interface for spectrum readers that extract the sample ID from the filename.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public interface SpectrumReaderWithSampleIDExtraction {

  /**
   * Sets the scheme for extracting the sample ID from the filename.
   *
   * @param value	the extraction
   */
  public void setSampleIDExtraction(SampleIDExtraction value);

  /**
   * Returns the scheme for extracting the sample ID from the filename.
   *
   * @return 		the extraction
   */
  public SampleIDExtraction getSampleIDExtraction();

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sampleIDExtractionTipText();
}
