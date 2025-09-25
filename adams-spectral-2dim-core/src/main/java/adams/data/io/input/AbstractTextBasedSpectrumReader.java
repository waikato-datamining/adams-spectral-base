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
 * AbstractTextBasedSpectrumReader.java
 * Copyright (C) 2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.io.FileUtils;
import adams.data.spectrum.Spectrum;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Ancestor for spectrum readers that are text-based.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractTextBasedSpectrumReader
  extends AbstractSpectrumReader
  implements StreamableDataContainerReader<Spectrum> {

  private static final long serialVersionUID = -6607630529928750008L;

  /**
   * Performs the actual reading.
   *
   * @param content 	the content to read from
   */
  protected abstract void readData(List<String> content);

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    readData(FileUtils.loadFromFile(m_Input));
  }

  /**
   * Returns the data containers generated from the input stream.
   *
   * @param input the stream to read from
   * @return the data generated from the stream
   */
  @Override
  public List<Spectrum> read(InputStream input) {
    List<Spectrum>	result;

    m_ReadData.clear();
    readData(FileUtils.loadLinesFromStream(input));
    result = new ArrayList<>(m_ReadData);
    m_ReadData.clear();
    return result;
  }
}
