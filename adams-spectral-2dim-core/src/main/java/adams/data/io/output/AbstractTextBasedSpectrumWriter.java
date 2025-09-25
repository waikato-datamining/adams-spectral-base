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
 * AbstractTextBasedSpectrumWriter.java
 * Copyright (C) 2025 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.output;

import adams.core.io.FileUtils;
import adams.data.spectrum.Spectrum;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Ancestor for text-based spectrum writers that support streaming.
 *
 * @author fracpete (fracpete at waikato dot ac dot nz)
 */
public abstract class AbstractTextBasedSpectrumWriter
  extends AbstractSpectrumWriter
  implements StreamableDataContainerWriter<Spectrum> {

  private static final long serialVersionUID = -6803837346254765883L;

  /**
   * Performs the actual writing.
   *
   * @param data	the data to write
   * @param writer 	the writer to write the spectra to
   * @return		true if successfully written
   */
  protected abstract boolean writeData(List<Spectrum> data, Writer writer);

  /**
   * Performs the actual writing.
   *
   * @param data	the data to write
   * @return		true if successfully written
   */
  @Override
  protected boolean writeData(List<Spectrum> data) {
    FileWriter		fwriter;
    BufferedWriter	bwriter;

    fwriter = null;
    bwriter = null;
    try {
      fwriter = new FileWriter(m_Output.getAbsoluteFile());
      bwriter = new BufferedWriter(fwriter);
      return writeData(data, bwriter);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to write spectra to: " + m_Output, e);
      return false;
    }
    finally {
      FileUtils.closeQuietly(bwriter);
      FileUtils.closeQuietly(fwriter);
    }
  }

  /**
   * Performs checks and writes the data to the stream.
   *
   * @param stream 	the stream to write to
   * @param data	the data to write
   * @return		true if successfully written
   * @see		#write(OutputStream stream, List)
   */
  public boolean write(OutputStream stream, Spectrum data) {
    return write(stream, new ArrayList<>(Collections.singletonList(data)));
  }

  /**
   * Performs checks and writes the data to the stream.
   *
   * @param stream 	the stream to write to
   * @param data	the data to write
   * @return		true if successfully written
   */
  public boolean write(OutputStream stream, List<Spectrum> data) {
    OutputStreamWriter	writer;

    writer = null;
    try {
      writer = new OutputStreamWriter(stream);
      return writeData(data, writer);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to write spectra to stream!", e);
      return false;
    }
    finally {
      FileUtils.closeQuietly(writer);
    }
  }
}
