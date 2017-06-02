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
 * MSPoint.java
 * Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.data.chromatogram;

import adams.data.container.AbstractDataPoint;
import adams.data.container.DataPoint;

import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * A data point in the mass spectrum.
 *
 * @author dale
 * @version $Revision: 3800 $
 */
public class MSPoint
  extends AbstractDataPoint {

  /** for serialization. */
  private static final long serialVersionUID = -3787806109050570412L;

  /** mass/charge. */
  protected float m_mz;

  /** abundance (total count). */
  protected int m_Abundance;

  /**
   * Constructor.
   */
  public MSPoint() {
    this(-1.0f, -1);
  }

  /**
   * Constructor. Initialise mass/charge and abundance count.
   *
   * @param mz		mass/charge
   * @param count	abundance
   */
  public MSPoint(float mz, int count) {
    this(null, mz, count);
  }

  /**
   * Constructor. Initialise mass/charge and abundance count.
   *
   * @param parent	this mass spectrum's GC point
   * @param mz		mass/charge
   * @param count	abundance
   */
  public MSPoint(GCPoint parent, float mz, int count){
    m_Parent    = parent;
    m_mz        = mz;
    m_Abundance = count;
  }

  /**
   * Obtains the stored variables from the other data point.
   *
   * @param other	the data point to get the values from
   */
  public void assign(DataPoint other) {
    MSPoint	point;

    super.assign(other);

    point = (MSPoint) other;

    setMassCharge(point.getMassCharge());
    setAbundance(point.getAbundance());
  }

  /**
   * Sets the mass/charge ratio.
   *
   * @param value	the ratio
   */
  public void setMassCharge(float value) {
    m_mz = value;
  }

  /**
   * Get mass/charge ratio.
   *
   * @return	mz
   */
  public float getMassCharge(){
    return(m_mz);
  }

  /**
   * Sets the abundance.
   *
   * @param value	the abundance
   */
  public void setAbundance(int value) {
    m_Abundance = value;
  }

  /**
   * Get abundance.
   *
   * @return	abundance
   */
  public int getAbundance(){
    return(m_Abundance);
  }

  /**
   * Compares this object with the specified object for order.  Returns a
   * negative integer, zero, or a positive integer as this object is less
   * than, equal to, or greater than the specified object.
   *
   * @param   o the object to be compared.
   * @return  a negative integer, zero, or a positive integer as this object
   *		is less than, equal to, or greater than the specified object.
   * @throws ClassCastException if the specified object's type prevents it
   *         from being compared to this object.
   */
  public int compareTo(Object o) {
    int		result;
    MSPoint	p;

    if (o == null)
      return 1;
    else
      result = 0;

    p = (MSPoint) o;

    if (result == 0)
      result = new Double(getMassCharge()).compareTo(new Double(p.getMassCharge()));

    if (result == 0)
      result = new Long(getAbundance()).compareTo(new Long(p.getAbundance()));

    return result;
  }

  /**
   * Returns a string representation of the GC points.
   *
   * @return		a string representation
   */
  public String toString() {
    String	result;

    result  = "m/z=" + getMassCharge();
    result += ", Abundance=" + getAbundance();

    return result;
  }

  /**
   * Writes the MS point to a writer.
   *
   * @param writer	the writer to write to
   * @param data	the MS point to write
   * @return		true if successful
   */
  public static boolean write(BufferedWriter writer, MSPoint data) {
    boolean	result;

    result = true;

    try {
      writer.write("  " + data.getMassCharge() + "/" + data.getAbundance());
      writer.newLine();
    }
    catch (Exception e) {
      result = false;
      e.printStackTrace();
    }

    return result;
  }

  /**
   * Reads an MS point from the reader, if possible.
   *
   * @param reader	the reader to read from
   * @return		the MS point or null if not possible to read/error occurred
   */
  public static MSPoint read(BufferedReader reader) {
    MSPoint	result;
    String	line;
    String[]	parts;

    result = null;

    try {
      line = reader.readLine();
      if ((line.trim().length() > 0) && (line.startsWith("  "))) {
	parts  = line.trim().split("\\/");
	result = new MSPoint(Float.parseFloat(parts[0]), Integer.parseInt(parts[1]));
      }
    }
    catch (Exception e) {
      result = null;
      e.printStackTrace();
    }

    return result;
  }
}
