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
 * SpectralAngleMapper.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer;

import adams.core.QuickInfoHelper;
import adams.core.TechnicalInformation;
import adams.core.TechnicalInformationHandler;
import adams.core.base.BaseString;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.statistics.SpectralAngleMapperUtils;
import adams.flow.control.Storage;
import adams.flow.control.StorageName;
import adams.flow.control.StorageUser;
import adams.flow.core.Token;

import java.util.Iterator;

/**
 <!-- globalinfo-start -->
 * Performs Spectral Angle Mapping on a set of spectra. Each spectrum is treated as a vector in band-space (where each spectral band is considered a basis), and the angle between the input and reference spectra is calculated to determine similarity. Emits an array of angles, one for each reference spectrum provided.<br>
 * <br>
 * For more information see:<br>
 * <br>
 * Kruse, Fred A, Lefkoff, AB, Boardman, JW, Heidebrecht, KB, Shapiro, AT, Barloon, PJ, Goetz, AFH (1993). The spectral image processing system (SIPS)—interactive visualization and analysis of imaging spectrometer data. Remote sensing of environment. 44(2-3):145--163.<br>
 * <br>
 * Oshigami, Shoko, Yamaguchi, Yasushi, Uezato, Tatsumi, Momose, Atsushi, Arvelyna, Yessy, Kawakami, Yuu, Yajima, Taro, Miyatake, Shuichi, Nguno, Anna (2013). Mineralogical mapping of southern Namibia by application of continuum-removal MSAM method to the HyMap data. International journal of remote sensing. 34(15):5282--5295.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- technical-bibtex-start -->
 * <pre>
 * &#64;article{Kruse1993,
 *    author = {Kruse, Fred A and Lefkoff, AB and Boardman, JW and Heidebrecht, KB and Shapiro, AT and Barloon, PJ and Goetz, AFH},
 *    journal = {Remote sensing of environment},
 *    number = {2-3},
 *    pages = {145--163},
 *    publisher = {Elsevier},
 *    title = {The spectral image processing system (SIPS)—interactive visualization and analysis of imaging spectrometer data},
 *    volume = {44},
 *    year = {1993}
 * }
 *
 * &#64;article{Oshigami2013,
 *    author = {Oshigami, Shoko and Yamaguchi, Yasushi and Uezato, Tatsumi and Momose, Atsushi and Arvelyna, Yessy and Kawakami, Yuu and Yajima, Taro and Miyatake, Shuichi and Nguno, Anna},
 *    journal = {International journal of remote sensing},
 *    number = {15},
 *    pages = {5282--5295},
 *    publisher = {Taylor &amp; Francis},
 *    title = {Mineralogical mapping of southern Namibia by application of continuum-removal MSAM method to the HyMap data},
 *    volume = {34},
 *    year = {2013}
 * }
 * </pre>
 * <br><br>
 <!-- technical-bibtex-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: SpectralAngleMapper
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
 * &nbsp;&nbsp;&nbsp;If set to true, the flow execution at this level gets stopped in case this
 * &nbsp;&nbsp;&nbsp;actor encounters an error; the error gets propagated; useful for critical
 * &nbsp;&nbsp;&nbsp;actors.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-silent &lt;boolean&gt; (property: silent)
 * &nbsp;&nbsp;&nbsp;If enabled, then no errors are output in the console; Note: the enclosing
 * &nbsp;&nbsp;&nbsp;actor handler must have this enabled as well.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-references &lt;adams.flow.control.StorageName&gt; (property: references)
 * &nbsp;&nbsp;&nbsp;The name of the storage location where the reference spectra are stored.
 * &nbsp;&nbsp;&nbsp;default: storage
 * </pre>
 *
 * <pre>-cache &lt;adams.core.base.BaseString&gt; (property: cache)
 * &nbsp;&nbsp;&nbsp;The name of the LRU cache to get the reference storage from. An empty value
 * &nbsp;&nbsp;&nbsp;specifies that regular storage should be used.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-check-wave-number-alignment &lt;boolean&gt; (property: checkWaveNumberAlignment)
 * &nbsp;&nbsp;&nbsp;Whether to check that all provided spectra have the same set of wave-numbers.
 * &nbsp;&nbsp;&nbsp;default: true
 * </pre>
 *
 <!-- options-end -->
 *
 * @author Corey Sterling (csterlin at waikato dot ac dot nz)
 */
public class SpectralAngleMapper
  extends AbstractTransformer
  implements StorageUser, TechnicalInformationHandler {

  /** Auto-generated serialisation UID#. */
  private static final long serialVersionUID = -377308833325778654L;

  /** Storage of the reference spectra. */
  protected StorageName m_ReferencesStorage;

  /** The LRU cache to get the storage value from. */
  protected BaseString m_Cache;

  /** Whether to check wave-numbers match between spectra. */
  protected boolean m_CheckWaveNumberAlignment;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Performs Spectral Angle Mapping on a set of spectra. Each spectrum " +
      "is treated as a vector in band-space (where each spectral band is considered a " +
      "basis), and the angle between the input and reference spectra is calculated to " +
      "determine similarity. Emits an array of angles, one for each reference spectrum " +
      "provided.\n\n"
      + "For more information see:\n\n"
      + getTechnicalInformation().toString();
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add("references", "references", new StorageName());

    m_OptionManager.add("cache", "cache", new BaseString());

    m_OptionManager.add("check-wave-number-alignment",
      "checkWaveNumberAlignment", true);
  }

  /**
   * Gets the name of the storage location for reference spectra.
   *
   * @return  The storage name.
   */
  public StorageName getReferences() {
    return m_ReferencesStorage;
  }

  /**
   * Sets the name of the storage location for reference spectra.
   *
   * @param value The storage name.
   */
  public void setReferences(StorageName value) {
    m_ReferencesStorage = value;
    reset();
  }

  /**
   * Gets the tip-text for the references option.
   *
   * @return  The tip-text as a string.
   */
  public String referencesTipText() {
    return "The name of the storage location where the reference spectra are stored.";
  }

  /**
   * Gets the name of the LRU cache to use.
   *
   * @return  The name of the cache.
   */
  public BaseString getCache() {
    return m_Cache;
  }

  /**
   * Sets the name of the LRU cache to use.
   *
   * @param value  The name of the cache.
   */
  public void setCache(BaseString value) {
    m_Cache = value;
    reset();
  }

  /**
   * Gets the tip-text for the cache option.
   *
   * @return  The tip-text as a string.
   */
  public String cacheTipText() {
    return "The name of the LRU cache to get the reference storage from. An empty value " +
      "specifies that regular storage should be used.";
  }

  /**
   * Gets whether wave-number alignment should be checked.
   *
   * @return  True if the check should be performed, false if not.
   */
  public boolean getCheckWaveNumberAlignment() {
    return m_CheckWaveNumberAlignment;
  }

  /**
   * Sets whether wave-number alignment should be checked.
   *
   * @param value  True if the check should be performed, false if not.
   */
  public void setCheckWaveNumberAlignment(boolean value) {
    m_CheckWaveNumberAlignment = value;
    reset();
  }

  /**
   * Gets the tip-text for the checkWaveNumberAlignment option.
   *
   * @return  The tip-text as a string.
   */
  public String checkWaveNumberAlignmentTipText() {
    return "Whether to check that all provided spectra have the same set of wave-numbers.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   * <br><br>
   * Default implementation returns null.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String info = QuickInfoHelper.toString(this, "references", m_ReferencesStorage, "references: ");

    if (m_Cache.length() != 0) info += QuickInfoHelper.toString(this, "cache", m_Cache, ", cache: ");

    return info;
  }

  /**
   * Returns an instance of a TechnicalInformation object, containing
   * detailed information about the technical background of this class,
   * e.g., paper reference or book this class is based on.
   *
   * @return the technical information about this class
   */
  @Override
  public TechnicalInformation getTechnicalInformation() {
    return SpectralAngleMapperUtils.getTechnicalInformation();
  }

  /**
   * Gets the input spectrum from the input token.
   *
   * @return The input spectrum.
   */
  protected Spectrum getInput() {
    // Get the input spectrum from the input token
    Spectrum input = (Spectrum) m_InputToken.getPayload();

    // Check the input for validity
    checkInput(input);

    return input;
  }

  /**
   * Checks the input spectrum for validity.
   *
   * @param input The input spectrum to check;
   */
  protected void checkInput(Spectrum input) {
    // Make sure the spectrum has at least two bases
    if (input.size() < 2)
      throw new IllegalArgumentException("Input spectrum must have at least 2 samples.");
  }

  /**
   * Retrieves the reference spectra from storage.
   *
   * @param name The name of the storage value to get from.
   * @param cache The name of the LRU cache to use.
   * @return The array of reference spectra.
   */
  protected Spectrum[] getReferences(StorageName name, String cache) {
    // Get the storage
    Storage storage = getStorageHandler().getStorage();

    // Get the object stored at the given location in storage
    Object storedObject = null;
    if (cache.length() == 0) {
      if (storage.has(name))
        storedObject = storage.get(name);
    } else {
      if (storage.has(cache, name))
        storedObject = storage.get(cache, name);
    }

    // Check we found a stored object
    if (storedObject == null) {
      String msg = "No value stored in " + name.stringValue();
      if (cache.length() != 0) msg += " in cache " + cache;
      msg += ".";
      throw new RuntimeException(msg);
    }

    // Check the stored object is a set of reference spectra
    if (!(storedObject instanceof Spectrum[])) {
      String msg = "Value stored in " + name.stringValue();
      if (cache.length() != 0) msg += " in cache " + cache;
      msg += " is not an array of reference spectra (require " +
        Spectrum[].class.getName() + ", found " +
        storedObject.getClass().getName() + ").";
      throw new RuntimeException(msg);
    }

    // Cast the references
    Spectrum[] references = (Spectrum[]) storedObject;

    // Check the references are valid
    checkReferences(references);

    // Return the references
    return references;
  }

  /**
   * Checks the reference spectra for validity.
   *
   * @param references  The reference spectra to check.
   */
  protected void checkReferences(Spectrum[] references) {
    // Make sure there is at least one reference
    if (references.length == 0)
      throw new RuntimeException("Set of reference spectra is empty.");

    // Make sure there are no null entries in the reference array
    for (int i = 0; i < references.length; i++) {
      if (references[i] == null)
        throw new RuntimeException("Reference spectra contains null entry at " +
          "position " + i + ".");
    }
  }

  /**
   * Checks that the input and the references are able to be angle-mapped.
   *
   * @param input The input spectrum.
   * @param references  The reference spectra.
   */
  protected void checkInputVersusReferences(Spectrum input, Spectrum[] references) {
    // Check all spectra are the same size
    for (Spectrum reference : references) {
      if (reference.size() != input.size())
        throw new RuntimeException("Reference spectrum " + reference.getID() + " does " +
          "not have the same number of wave elements as the input spectrum (" +
          reference.size() + " instead of " + input.size() +").");
    }

    // Optionally check all spectra have the same wave-number profile
    if (m_CheckWaveNumberAlignment) checkWaveNumberAlignment(input, references);
  }

  /**
   * Checks that the input and reference spectra have the same wave-number profile.
   *
   * @param input The input spectrum.
   * @param references  The reference spectra.
   */
  protected void checkWaveNumberAlignment(Spectrum input, Spectrum[] references) {
    // Get the iterators to all spectra
    Iterator<SpectrumPoint> inputIterator = input.iterator();
    Iterator<SpectrumPoint>[] referenceIterators = new Iterator[references.length];
    for (int i = 0; i < references.length; i++) {
      referenceIterators[i] = references[i].iterator();
    }

    // Check each reference spectra element against the corresponding
    // input spectrum element
    int waveNumberIndex = 0;
    while (inputIterator.hasNext()) {
      // Get the required wave-number
      float waveNumber = inputIterator.next().getWaveNumber();

      // Check all reference spectra have the same wave-number
      for (int i = 0; i < references.length; i++) {
        float referenceWaveNumber = referenceIterators[i].next().getWaveNumber();
        if (referenceWaveNumber != waveNumber)
          throw new RuntimeException("Input and reference " + references[i].getID() +
            " differ in wave-number at position " + waveNumberIndex + "(" + waveNumber +
            " and " + referenceWaveNumber + " respectively).");
      }

      waveNumberIndex++;
    }
  }

  /**
   * Performs the calculation of spectral angles between the input spectrum and
   * each reference spectrum.
   *
   * @param input The input spectrum.
   * @param references  The reference spectra.
   * @return  An array of angles, one for each reference spectrum.
   */
  protected double[] calculateSpectralAngles(Spectrum input, Spectrum[] references) {
    // Convert the input into a double array
    double[] inputArray = toDoubleArray(input);

    // Convert the references into a double array
    double[][] referenceArrays = new double[references.length][];
    for (int i = 0; i < references.length; i++) {
      referenceArrays[i] = toDoubleArray(references[i]);
    }

    // Defer to the utility class
    return SpectralAngleMapperUtils.sam(inputArray, referenceArrays, false);
  }

  /**
   * Converts a spectrum into a double array of its wave amplitudes.
   *
   * @param spectrum  The spectrum to convert.
   * @return  The array of amplitudes.
   */
  protected double[] toDoubleArray(Spectrum spectrum) {
    // Get the iterator over the spectral data
    Iterator<SpectrumPoint> iterator = spectrum.iterator();

    // Create the return array
    double[] result = new double[spectrum.size()];

    // Add each amplitude to the return array
    int i = 0;
    while (iterator.hasNext()) {
      result[i] = iterator.next().getAmplitude();
      i++;
    }

    return result;
  }

  /**
   * Sets the output token to the given array of angles.
   *
   * @param angles The angles to output.
   */
  protected void setOutput(double[] angles) {
    m_OutputToken = new Token();
    m_OutputToken.setPayload(angles);
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    try {
      // Get the input and references
      Spectrum input = getInput();
      Spectrum[] references = getReferences(m_ReferencesStorage, m_Cache.stringValue());

      // Check the input matched the references
      checkInputVersusReferences(input, references);

      // Calculate the spectral angles between input and references
      double[] angles = calculateSpectralAngles(input, references);

      // Emit the calculated angles
      setOutput(angles);

      return null;
    } catch (Exception e) {
      return e.getMessage();
    }
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the Class of objects that can be processed
   */
  @Override
  public Class[] accepts() {
    return new Class[] { Spectrum.class };
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the Class of the generated tokens
   */
  @Override
  public Class[] generates() {
    return new Class[] { double[].class };
  }

  /**
   * Returns whether storage items are being used.
   *
   * @return		true if storage items are used
   */
  @Override
  public boolean isUsingStorage() {
    return !getSkip();
  }
}