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
 * Notes.java
 * Copyright (C) 2008-2010 University of Waikato, Hamilton, New Zealand
 */

package adams.data.chromatogram;


/**
 * A helper class for the Chromatogram class for storing meta-data.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 3800 $
 */
public class Notes
  extends adams.data.Notes {

  /** for serialization. */
  private static final long serialVersionUID = -6179090129357843542L;

  /** the standards that were used. */
  public final static String STANDARD_INFORMATION = "Standard";

  /** the calibration sets that were used. */
  public final static String CALIBRATION_SET_INFORMATION = "Calibration set";

  /**
   * Initializes the notes.
   */
  public Notes() {
    super();
  }

  /**
   * Adds information about standards.
   *
   * @param standard		the standard to add to the list of used standards
   */
  public void addStandardInformation(String standard) {
    addNote(STANDARD_INFORMATION, standard);
  }

  /**
   * Checks whether at least one standard information is among the notes.
   *
   * @return		true if notes contain at least one standard information
   */
  public boolean hasStandardInformation() {
    return hasNotes(STANDARD_INFORMATION);
  }

  /**
   * Returns the standard information subset.
   *
   * @return		the standard informations (if any)
   */
  public adams.data.Notes getStandardInformation() {
    return getPrefixSubset(STANDARD_INFORMATION);
  }

  /**
   * Adds information about calibration sets.
   *
   * @param calib		the calibration set to add to the list of used calibration sets
   */
  public void addCalibrationSetInformation(String calib) {
    addNote(CALIBRATION_SET_INFORMATION, calib);
  }

  /**
   * Checks whether at least one calibration set information is among the notes.
   *
   * @return		true if notes contain at least one calibration set information
   */
  public boolean hasCalibrationSetInformation() {
    return hasNotes(CALIBRATION_SET_INFORMATION);
  }

  /**
   * Returns the calibration set information subset.
   *
   * @return		the calibration set informations (if any)
   */
  public adams.data.Notes getCalibrationSetInformation() {
    return getPrefixSubset(CALIBRATION_SET_INFORMATION);
  }

  /**
   * Returns the other notes, not warning/error/process information.
   *
   * @return		the other notes (if any)
   */
  public adams.data.Notes getOthers() {
    adams.data.Notes	result;
    adams.data.Notes	excluded;

    excluded = new Notes();
    excluded.mergeWith(getWarnings());
    excluded.mergeWith(getErrors());
    excluded.mergeWith(getProcessInformation());
    excluded.mergeWith(getStandardInformation());
    excluded.mergeWith(getCalibrationSetInformation());

    result = this.minus(excluded);

    return result;
  }
}
