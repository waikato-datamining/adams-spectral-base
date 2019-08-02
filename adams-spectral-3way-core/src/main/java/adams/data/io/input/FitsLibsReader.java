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
 * FitsLibsReader.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input;

import adams.core.Utils;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.Report;
import adams.data.threeway.L1Point;
import adams.data.threeway.L2Point;
import adams.data.threeway.ThreeWayData;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Data;
import nom.tam.fits.Fits;
import nom.tam.fits.HeaderCard;
import nom.tam.image.compression.hdu.CompressedImageData;
import nom.tam.image.compression.hdu.CompressedImageHDU;
import nom.tam.util.Cursor;

import java.lang.reflect.Array;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class FitsLibsReader
  extends AbstractThreeWayDataReader {

  private static final long serialVersionUID = 1863004313666024729L;

  /** whether the data is unsigned. */
  protected boolean m_Unsigned;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads LIBS data in FITS format.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "unsigned", "unsigned",
      false);
  }

  /**
   * Sets whether the numbers are to be interpreted as unsigned.
   *
   * @param value	true if unsigned
   */
  public void setUnsigned(boolean value) {
    m_Unsigned = value;
    reset();
  }

  /**
   * Returns whether the numbers are to be interpreted as unsigned.
   *
   * @return		true if unsigned
   */
  public boolean getUnsigned() {
    return m_Unsigned;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String unsignedTipText() {
    return "If enabled, the numbers are interpreted as unsigned, adding half of their maximum value (eg for short this is 32768).";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "FITS LIBS";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"fits"};
  }

  /**
   * Turns the array value into a double.
   *
   * @param array	the array to obtain the value from
   * @param index	the index in the array
   * @return		the double value
   */
  protected double arrayToDouble(Object array, int index) {
    Object	value;

    value = Array.get(array, index);
    if (value instanceof Byte) {
      if (m_Unsigned)
	return ((Byte) value).doubleValue() + 128;
      else
	return ((Byte) value).doubleValue();
    }
    else if (value instanceof Short) {
      if (m_Unsigned)
	return ((Short) value).doubleValue() + 128;
      else
	return ((Short) value).doubleValue();
    }
    else if (value instanceof Integer) {
      if (m_Unsigned)
	return ((Integer) value).doubleValue() + 128;
      else
	return ((Integer) value).doubleValue();
    }
    else if (value instanceof Long) {
      if (m_Unsigned)
	return ((Long) value).doubleValue() + 128;
      else
	return ((Long) value).doubleValue();
    }
    else {
      getLogger().warning("Unhandled array value type: " + Utils.classToString(value) + " (" + value + ")");
      return Double.NaN;
    }
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    Fits 			fits;
    int 			x;
    int 			y;
    int 			z;
    ThreeWayData 		twd;
    Report 			report;
    Field			field;
    BasicHDU<?>			hdu;
    CompressedImageHDU		chdu;
    Data			data;
    int				rows;
    int				cols;
    Object 			array;
    Cursor<String, HeaderCard>	iter;
    HeaderCard			card;
    L1Point			l1;
    L2Point			l2;

    try {
      fits = new Fits(m_Input.getAbsolutePath());
      fits.read();
      if (isLoggingEnabled())
        getLogger().info("#HDU: " + fits.getNumberOfHDUs());

      twd    = new ThreeWayData();
      report = twd.getReport();
      m_ReadData.add(twd);

      for (x = 0; x < fits.getNumberOfHDUs(); x++) {
        hdu  = fits.getHDU(x);
        data = hdu.getData();
        if (data == null) {
          if (isLoggingEnabled())
	    getLogger().info("HDU #" + x + ": no data");
          continue;
	}
        if (data instanceof CompressedImageData) {
	  if (isLoggingEnabled())
	    getLogger().info("HDU #" + x + ": compressed image data");

	  chdu = (CompressedImageHDU) hdu;
	  twd.setID(m_Input.getName() + "-" + x);

	  // add meta-data
	  iter = hdu.getHeader().iterator();
	  rows = 1;
	  cols = 1;
	  while (iter.hasNext()) {
	    card = iter.next();
	    if ((card.getKey() != null) && (card.getValue() != null)) {
	      if (Utils.isDouble(card.getValue()))
	        field = new Field(x + "." + card.getKey(), DataType.NUMERIC);
	      else if (Utils.isBoolean(card.getValue()))
	        field = new Field(x + "." + card.getKey(), DataType.BOOLEAN);
	      else
	        field = new Field(x + "." + card.getKey(), DataType.STRING);
	      report.addField(field);
	      report.setValue(field, card.getValue());
	      if (card.getKey().equals("ZNAXIS1"))
	        rows = Integer.parseInt(card.getValue());
	      else if (card.getKey().equals("ZNAXIS2"))
	        cols = Integer.parseInt(card.getValue());
	    }
	  }

	  // add data points
          array = chdu.getUncompressedData().array();
	  for (y = 0; y < rows; y++) {
	    l1 = new L1Point(x, y);
	    twd.add(l1);
	    for (z = 0; z < cols; z++) {
	      l2 = new L2Point(z, arrayToDouble(array, y*rows + z));
	      l1.add(l2);
	    }
	  }
	}
	else {
	  getLogger().warning("HDU #" + x + ": unhandled data type: " + Utils.classToString(data));
	}
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to read FITS: " + m_Input, e);
    }
  }
}
