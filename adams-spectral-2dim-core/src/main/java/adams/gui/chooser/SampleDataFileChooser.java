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
 * SampleDataFileChooser.java
 * Copyright (C) 2009-2013 University of Waikato, Hamilton, New Zealand
 */

package adams.gui.chooser;

import adams.core.ClassLister;
import adams.data.io.input.AbstractReportReader;
import adams.data.io.input.SampleDataReader;
import adams.data.io.input.SimpleSampleDataReader;
import adams.data.io.output.AbstractReportWriter;
import adams.data.io.output.SampleDataWriter;
import adams.data.io.output.SimpleSampleDataWriter;
import adams.data.sampledata.SampleData;

import java.io.File;

/**
 * A specialized JFileChooser that lists all available file Readers and Writers
 * for sampledata reports.
 * <br><br>
 * Based on <code>weka.gui.ConverterFileChooser</code>
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 * @see	    weka.gui.ConverterFileChooser
 */
public class SampleDataFileChooser
  extends AbstractReportFileChooser<SampleData, AbstractReportReader, AbstractReportWriter> {

  /** for serialization. */
  private static final long serialVersionUID = -53374407938356183L;

  /**
   * Constructs a FileChooser pointing to the user's default directory.
   */
  public SampleDataFileChooser() {
    super();
  }

  /**
   * Constructs a FileChooser using the given File as the path.
   *
   * @param currentDirectory	the path to start in
   */
  public SampleDataFileChooser(File currentDirectory) {
    super(currentDirectory);
  }

  /**
   * Constructs a FileChooser using the given path.
   *
   * @param currentDirectory	the path to start in
   */
  public SampleDataFileChooser(String currentDirectory) {
    super(currentDirectory);
  }

  /**
   * Returns the default reader.
   *
   * @return		the default reader
   */
  @Override
  protected AbstractReportReader<SampleData> getDefaultReader() {
    return new SimpleSampleDataReader();
  }

  /**
   * Returns the default writer.
   *
   * @return		the default writer
   */
  @Override
  protected AbstractReportWriter<SampleData> getDefaultWriter() {
    return new SimpleSampleDataWriter();
  }

  /**
   * Performs the actual initialization of the filters.
   */
  @Override
  protected void doInitializeFilters() {
    initFilters(this, true, ClassLister.getSingleton().getClassnames(SampleDataReader.class));
    initFilters(this, false, ClassLister.getSingleton().getClassnames(SampleDataWriter.class));
  }
}
