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
 * CALSpectrumLoader.java
 * Copyright (C) 2016 University of Waikato, Hamilton, NZ
 */

package weka.core.converters;

import adams.core.base.BaseRegExp;
import adams.core.io.FileUtils;
import adams.core.io.PlaceholderFile;
import adams.data.instances.SimpleInstanceGenerator;
import adams.data.io.input.CALSpectrumReader;
import adams.data.io.input.foss.FossHelper;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.spectrum.Spectrum;
import adams.env.Environment;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WekaOptionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * Loads a CAL file.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class CALSpectrumLoader
  extends AbstractFileLoader
  implements OptionHandler {

  private static final long serialVersionUID = 3251733079008628734L;

  public static final String REF_REGEXP = "ref-regexp";

  /** the references to load. */
  protected BaseRegExp m_RefRegExp = getDefaultRefRegExp();

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return new CALSpectrumReader().globalInfo() + "\n"
      + "NB: reference names are always turned into lower-case.";
  }

  /**
   * Get the file extension used for this type of file
   *
   * @return the file extension
   */
  @Override
  public String getFileExtension() {
    return getFileExtensions()[0];
  }

  /**
   * Gets all the file extensions used for this type of file
   *
   * @return the file extensions
   */
  @Override
  public String[] getFileExtensions() {
    return new CALSpectrumReader().getFormatExtensions();
  }

  /**
   * Get a one line description of the type of file
   *
   * @return a description of the file type
   */
  @Override
  public String getFileDescription() {
    return new CALSpectrumReader().getFormatDescription();
  }

  /**
   * Returns the default regexp for the references.
   *
   * @return		the default
   */
  protected BaseRegExp getDefaultRefRegExp() {
    return new BaseRegExp(BaseRegExp.MATCH_ALL);
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  @Override
  public Enumeration listOptions() {
    Vector result = new Vector();
    WekaOptionUtils.addOption(result, refRegExpTipText(), getDefaultRefRegExp().getValue(), REF_REGEXP);
    return WekaOptionUtils.toEnumeration(result);
  }

  /**
   * Sets the OptionHandler's options using the given list. All options
   * will be set (or reset) during this call (i.e. incremental setting
   * of options is not possible).
   *
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  @Override
  public void setOptions(String[] options) throws Exception {
    setRefRegExp((BaseRegExp) WekaOptionUtils.parse(options, REF_REGEXP, getDefaultRefRegExp()));
    Utils.checkForRemainingOptions(options);
  }

  /**
   * Gets the current option settings for the OptionHandler.
   *
   * @return the list of current option settings as an array of strings
   */
  @Override
  public String[] getOptions() {
    List<String> result = new ArrayList<>();
    WekaOptionUtils.add(result, REF_REGEXP, getRefRegExp());
    return WekaOptionUtils.toArray(result);
  }

  /**
   * Sets the regexp to use for matching the reference names to include in the dataset.
   *
   * @param value	the expression
   */
  public void setRefRegExp(BaseRegExp value) {
    m_RefRegExp = value;
  }

  /**
   * Returns the regexp to use for matching the reference names to include in the dataset.
   *
   * @return		the expression
   */
  public BaseRegExp getRefRegExp() {
    return m_RefRegExp;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String refRegExpTipText() {
    return "The regular expression that reference names must match to be included in the dataset.";
  }

  /**
   * Does nothing.
   *
   * @param input the input stream
   * @throws IOException never
   */
  @Override
  public void setSource(InputStream input) throws IOException {
    // ignored
  }

  /**
   * Determines and returns (if possible) the structure (internally the header)
   * of the data set as an empty set of instances.
   * <br>
   * Inefficient, as it just returns an empty dataset derived from
   * a call to {@link #getDataSet()}.
   *
   * @return the structure of the data set as an empty set of Instances
   * @throws IOException if there is no source or parsing fails
   */
  @Override
  public Instances getStructure() throws IOException {
    return new Instances(getDataSet(), 0);
  }

  /**
   * Return the full data set. If the structure hasn't yet been determined by a
   * call to getStructure then the method should do so before processing the
   * rest of the data set.
   *
   * @return the full data set as an Instances object
   * @throws IOException if there is an error during parsing or if
   *           getNextInstance has been called on this source (either
   *           incremental or batch loading can be used, not both).
   */
  @Override
  public Instances getDataSet() throws IOException {
    Instances			result;
    File			file;
    CALSpectrumReader		reader;
    List<Spectrum>		spectra;
    FossHelper 			fh;
    Vector<String> 		refs;
    List<Field>			fields;
    int				i;
    SimpleInstanceGenerator	generator;
    Instance			inst;

    file = retrieveFile();
    if (file == null)
      throw new IOException("No file name provided!");

    reader = new CALSpectrumReader();
    reader.setInput(new PlaceholderFile(file));
    spectra = reader.read();
    if (spectra == null)
      throw new IOException("Failed to read spectra from: " + file);

    fh   = new FossHelper(FileUtils.loadFromBinaryFile(file.getAbsoluteFile()));
    refs = fh.getReferenceNames();
    fields = new ArrayList<>();
    for (i = 0; i < refs.size(); i++) {
      if (m_RefRegExp.isMatch(refs.get(i).toLowerCase()))
	fields.add(new Field(refs.get(i).toLowerCase(), DataType.NUMERIC));
    }
    generator = new SimpleInstanceGenerator();
    generator.setAddSampleID(true);
    generator.setNoAdditionalFieldsPrefix(true);
    if (fields.size() > 0) {
      generator.setField(fields.get(fields.size() - 1));
      fields.remove(fields.size() - 1);
    }
    else {
      generator.setField(new Field("dummyclass", DataType.NUMERIC));
    }
    generator.setAdditionalFields(fields.toArray(new Field[fields.size()]));
    result = null;
    for (Spectrum sp: spectra) {
      inst = generator.generate(sp);
      if (result == null) {
	result = new Instances(inst.dataset(), spectra.size());
	result.setRelationName(FileUtils.replaceExtension(file.getName(), ""));
      }
      result.add(inst);
    }

    return result;
  }

  /**
   * CALSpectrumLoader is unable to process a data set incrementally.
   *
   * @param structure		ignored
   * @return 			never returns without throwing an exception
   * @throws IOException 	always. CALSpectrumLoader is unable to process a
   * 				data set incrementally.
   */
  public Instance getNextInstance(Instances structure) throws IOException {
    throw new IOException("CALSpectrumLoader can't read data sets incrementally.");
  }

  /**
   * Returns the revision string.
   *
   * @return the revision
   */
  @Override
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 11506 $");
  }

  /**
   * Main method.
   *
   * @param args should contain the name of an input file.
   */
  public static void main(String[] args) {
    Environment.setEnvironmentClass(Environment.class);
    runFileLoader(new CALSpectrumLoader(), args);
  }
}
