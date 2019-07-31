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
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Data;
import nom.tam.fits.Fits;
import nom.tam.fits.HeaderCard;
import nom.tam.image.compression.hdu.CompressedImageData;
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
  extends AbstractSpectrumReader {

  private static final long serialVersionUID = 1863004313666024729L;

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
   * Turns a cell into a SpectrumPoint.
   *
   * @param cell	the cell to convert
   * @param col		the column index of the cell
   * @param row		the row index of the cell (used as wave number if cell only contains array of length 1)
   * @return		the spectrum point, null if failed to generate
   */
  protected SpectrumPoint cellToPoint(Object cell, int col, int row) {
    if (Array.getLength(cell) == 2) {
      if (cell instanceof byte[])
	return new SpectrumPoint(((byte[]) cell)[1], ((byte[]) cell)[0]);
      else if (cell instanceof int[])
	return new SpectrumPoint(((int[]) cell)[1], ((int[]) cell)[0]);
      else if (cell instanceof long[])
	return new SpectrumPoint(((long[]) cell)[1], ((long[]) cell)[0]);
      else if (cell instanceof float[])
	return new SpectrumPoint(((float[]) cell)[1], ((float[]) cell)[0]);
      else if (cell instanceof double[])
	return new SpectrumPoint((float) ((double[]) cell)[1], (float) ((double[]) cell)[0]);
      else
	getLogger().warning("Unhandled cell type: " + Utils.classToString(cell));
      return null;
    }
    else if (Array.getLength(cell) == 1) {
      if (cell instanceof byte[])
	return new SpectrumPoint(row, ((byte[]) cell)[0]);
      else if (cell instanceof int[])
	return new SpectrumPoint(row, ((int[]) cell)[0]);
      else if (cell instanceof long[])
	return new SpectrumPoint(row, ((long[]) cell)[0]);
      else if (cell instanceof float[])
	return new SpectrumPoint(row, ((float[]) cell)[0]);
      else if (cell instanceof double[])
	return new SpectrumPoint(row, (float) ((double[]) cell)[0]);
      else
	getLogger().warning("Unhandled cell type: " + Utils.classToString(cell));
      return null;
    }
    else {
      getLogger().warning("Unhandled array length of cell: " + Array.getLength(cell) + "/" + Utils.classToString(cell));
      return null;
    }
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    Fits 			fits;
    int 			i;
    int				r;
    int				c;
    Spectrum			sp;
    SampleData			sd;
    Field			field;
    BasicHDU<?>			hdu;
    Data			data;
    CompressedImageData 	cidata;
    Object			cell;
    SpectrumPoint		point;
    Cursor<String, HeaderCard>	iter;
    HeaderCard			card;

    try {
      fits = new Fits(m_Input.getAbsolutePath());
      fits.read();
      if (isLoggingEnabled())
        getLogger().info("#HDU: " + fits.getNumberOfHDUs());
      for (i = 0; i < fits.getNumberOfHDUs(); i++) {
        hdu  = fits.getHDU(i);
        data = hdu.getData();
        if (data == null) {
          if (isLoggingEnabled())
	    getLogger().info("HDU #" + i + ": no data");
          continue;
	}
        if (data instanceof CompressedImageData) {
	  if (isLoggingEnabled())
	    getLogger().info("HDU #" + i + ": compressed image data");

	  // init spectrum
	  sp = new Spectrum();
	  sp.setID(m_Input.getName() + "-" + i);

	  // add meta-data
	  iter = hdu.getHeader().iterator();
	  sd   = sp.getReport();
	  while (iter.hasNext()) {
	    card = iter.next();
	    if ((card.getKey() != null) && (card.getValue() != null)) {
	      if (Utils.isDouble(card.getValue()))
	        field = new Field(card.getKey(), DataType.NUMERIC);
	      else if (Utils.isBoolean(card.getValue()))
	        field = new Field(card.getKey(), DataType.BOOLEAN);
	      else
	        field = new Field(card.getKey(), DataType.STRING);
	      sd.addField(field);
	      sd.setValue(field, card.getValue());
	    }
	  }

	  // add data points
          cidata = (CompressedImageData) data;
	  for (c = 0; c < cidata.getData().getNCols(); c++) {
	    for (r = 0; r < cidata.getData().getNRows(); r++) {
	      cell  = cidata.getData().getElement(r, c);
	      point = cellToPoint(cell, c, r);
	      if (point != null)
	        sp.add(point);
	    }
	  }

	  // add spectrum
	  m_ReadData.add(sp);
	}
	else {
	  getLogger().warning("HDU #" + i + ": unhandled data type: " + Utils.classToString(data));
	}
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to read FITS: " + m_Input, e);
    }
  }
}
