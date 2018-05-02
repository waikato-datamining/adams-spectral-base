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
 * ThreeWayDataFileChooser.java
 * Copyright (C) 2018 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.chooser;

import adams.data.io.input.AbstractDataContainerReader;
import adams.data.io.input.AbstractThreeWayDataReader;
import adams.data.io.input.SimpleEEMReader;
import adams.data.io.output.AbstractDataContainerWriter;
import adams.data.io.output.AbstractThreeWayDataWriter;
import adams.data.io.output.SimpleEEMWriter;
import adams.data.threeway.ThreeWayData;

import java.io.File;

/**
 * A specialized JFileChooser that lists all available file Readers and Writers
 * for 3-way data.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class ThreeWayDataFileChooser
  extends AbstractDataContainerFileChooser<ThreeWayData, AbstractDataContainerReader<ThreeWayData>, AbstractDataContainerWriter<ThreeWayData>> {

  /** for serialization. */
  private static final long serialVersionUID = -5373058011025481738L;

  /**
   * Constructs a FileChooser pointing to the user's default directory.
   */
  public ThreeWayDataFileChooser() {
    super();
  }

  /**
   * Constructs a FileChooser using the given File as the path.
   *
   * @param currentDirectory	the path to start in
   */
  public ThreeWayDataFileChooser(File currentDirectory) {
    super(currentDirectory);
  }

  /**
   * Constructs a FileChooser using the given path.
   *
   * @param currentDirectory	the path to start in
   */
  public ThreeWayDataFileChooser(String currentDirectory) {
    super(currentDirectory);
  }

  /**
   * Returns the default reader.
   *
   * @return		the default reader
   */
  @Override
  protected AbstractDataContainerReader<ThreeWayData> getDefaultReader() {
    return new SimpleEEMReader();
  }

  /**
   * Returns the default writer.
   *
   * @return		the default writer
   */
  @Override
  protected AbstractDataContainerWriter<ThreeWayData> getDefaultWriter() {
    return new SimpleEEMWriter();
  }

  /**
   * Returns the reader superclass for the GOE.
   *
   * @return		the reader class
   */
  @Override
  protected Class getReaderClass() {
    return AbstractThreeWayDataReader.class;
  }

  /**
   * Returns the writer superclass for the GOE.
   *
   * @return		the writer class
   */
  @Override
  protected Class getWriterClass() {
    return AbstractThreeWayDataWriter.class;
  }

  /**
   * Performs the actual initialization of the filters.
   */
  @Override
  protected void doInitializeFilters() {
    initFilters(this, true, AbstractThreeWayDataReader.getReaders());
    initFilters(this, false, AbstractThreeWayDataWriter.getWriters());
  }
}
