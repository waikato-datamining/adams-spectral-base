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
 * ScioLabExportSpectrumReader.java
 * Copyright (C) 2017 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input;

import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spreadsheet.Cell;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;

import java.io.StringReader;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Reads the CSV export from the SCiO Lab web site.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-input &lt;adams.core.io.PlaceholderFile&gt; (property: input)
 * &nbsp;&nbsp;&nbsp;The file to read and turn into a container.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 * 
 * <pre>-create-dummy-report &lt;boolean&gt; (property: createDummyReport)
 * &nbsp;&nbsp;&nbsp;If true, then a dummy report is created if none present.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-instrument &lt;java.lang.String&gt; (property: instrument)
 * &nbsp;&nbsp;&nbsp;The name of the instrument that generated the spectra (if not already present 
 * &nbsp;&nbsp;&nbsp;in data).
 * &nbsp;&nbsp;&nbsp;default: unknown
 * </pre>
 * 
 * <pre>-format &lt;java.lang.String&gt; (property: format)
 * &nbsp;&nbsp;&nbsp;The data format string.
 * &nbsp;&nbsp;&nbsp;default: NIR
 * </pre>
 * 
 * <pre>-keep-format &lt;boolean&gt; (property: keepFormat)
 * &nbsp;&nbsp;&nbsp;If enabled the format obtained from the file is not replaced by the format 
 * &nbsp;&nbsp;&nbsp;defined here.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-use-absolute-source &lt;boolean&gt; (property: useAbsoluteSource)
 * &nbsp;&nbsp;&nbsp;If enabled the source report field stores the absolute file name rather 
 * &nbsp;&nbsp;&nbsp;than just the name.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-type &lt;SPECTRUM|WR_RAW|SAMPLE_RAW&gt; (property: type)
 * &nbsp;&nbsp;&nbsp;The type of spectrum to read from the data.
 * &nbsp;&nbsp;&nbsp;default: SPECTRUM
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 */
public class ScioLabExportSpectrumReader
  extends AbstractSpectrumReader {

  private static final long serialVersionUID = 2850033699787114065L;

  public final static String PREFIX_SPECTRUM = "spectrum_";

  public final static String PREFIX_WR_RAW = "wr_raw_";

  public final static String PREFIX_SAMPLE_RAW = "sample_raw_";

  /**
   * Determines the type of spectrum to load.
   */
  public enum SpectrumType {
    SPECTRUM,
    WR_RAW,
    SAMPLE_RAW,
  }

  /** the type of spectrum to read. */
  protected SpectrumType m_Type;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads the CSV export from the SCiO Lab web site.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "type", "type",
      SpectrumType.SPECTRUM);
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable fospectralDatar displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "SCiO Lab Export";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{"csv"};
  }

  /**
   * Sets the type of spectrum to read.
   *
   * @param value	the type
   */
  public void setType(SpectrumType value) {
    m_Type = value;
    reset();
  }

  /**
   * Returns the type of spectrum to read.
   *
   * @return 		the type
   */
  public SpectrumType getType() {
    return m_Type;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String typeTipText() {
    return "The type of spectrum to read from the data.";
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    List<String>		data;
    SampleData 			meta;
    String[]			parts;
    Spectrum			spectrum;
    Spectrum			wrraw;
    Spectrum			sampleraw;
    int				i;
    int				n;
    int				start;
    SpreadSheet			sheet;
    CsvSpreadSheetReader	csvreader;
    StringBuilder		spectralData;
    Row				row;
    String			col;
    String			waveStr;
    SpectrumPoint		point;
    Cell			cell;
    Field			field;
    String			sampleid;

    data = FileUtils.loadFromFile(m_Input.getAbsoluteFile());

    // meta data
    meta  = new SampleData();
    start = 0;
    for (i = 0; i < data.size(); i++) {
      if (data.get(i).startsWith("id,"))
	break;
      start++;
      parts = data.get(i).split(",");
      if (parts.length != 2)
	continue;
      if (Utils.isDouble(parts[1])) {
	meta.addField(new Field(parts[0], DataType.NUMERIC));
	meta.setNumericValue(parts[0], Utils.toDouble(parts[1]));
      }
      else {
	meta.addField(new Field(parts[0], DataType.STRING));
	meta.setStringValue(parts[0], parts[1].trim());
      }
    }

    // spectral data
    spectralData = new StringBuilder();
    for (i = start; i < data.size(); i++) {
      if (i > start)
	spectralData.append("\n");
      spectralData.append(data.get(i));
    }
    csvreader = new CsvSpreadSheetReader();
    sheet = csvreader.read(new StringReader(spectralData.toString()));

    for (n = 0; n < sheet.getRowCount(); n++) {
      row = sheet.getRow(n);

      // row with cell types?
      if (row.getCell(0).getContent().equals("int"))
	continue;

      spectrum  = new Spectrum();
      spectrum.getReport().mergeWith(meta);
      wrraw     = new Spectrum();
      wrraw.getReport().mergeWith(meta);
      sampleraw = new Spectrum();
      sampleraw.getReport().mergeWith(meta);

      if (m_Type == SpectrumType.SPECTRUM)
	m_ReadData.add(spectrum);
      else if (m_Type == SpectrumType.WR_RAW)
	m_ReadData.add(wrraw);
      else if (m_Type == SpectrumType.SAMPLE_RAW)
	m_ReadData.add(sampleraw);
      else
        throw new IllegalStateException("Unhandled spectrum type!");

      sampleid = row.getCell(1).getContent().trim() + "/" + row.getCell(0).getContent().trim();
      spectrum.setID(sampleid + "/spectrum");
      wrraw.setID(sampleid + "/wrraw");
      sampleraw.setID(sampleid + "/sampleraw");

      for (i = 0; i < sheet.getColumnCount(); i++) {
	col  = sheet.getColumnName(i).trim();
	cell = row.getCell(i);
	if (col.startsWith(PREFIX_SPECTRUM)) {
	  waveStr = col.substring(PREFIX_SPECTRUM.length());
	  point   = new SpectrumPoint(Float.parseFloat(waveStr), cell.toDouble().floatValue());
	  spectrum.add(point);
	}
	else if (col.startsWith(PREFIX_WR_RAW)) {
	  waveStr = col.substring(PREFIX_WR_RAW.length());
	  point   = new SpectrumPoint(Float.parseFloat(waveStr), cell.toDouble().floatValue());
	  wrraw.add(point);
	}
	else if (col.startsWith(PREFIX_SAMPLE_RAW)) {
	  waveStr = col.substring(PREFIX_SAMPLE_RAW.length());
	  point   = new SpectrumPoint(Float.parseFloat(waveStr), cell.toDouble().floatValue());
	  sampleraw.add(point);
	}
	else {
	  if (row.getCell(i).isNumeric()) {
	    field = new Field(col, DataType.NUMERIC);
	    spectrum.getReport().addField(field);
	    spectrum.getReport().setNumericValue(col, cell.toDouble());
	    wrraw.getReport().addField(field);
	    wrraw.getReport().setNumericValue(col, cell.toDouble());
	    sampleraw.getReport().addField(field);
	    sampleraw.getReport().setNumericValue(col, cell.toDouble());
	  }
	  else {
	    field = new Field(col, DataType.STRING);
	    spectrum.getReport().addField(field);
	    spectrum.getReport().setStringValue(col, cell.getContent());
	    wrraw.getReport().addField(field);
	    wrraw.getReport().setStringValue(col, cell.getContent());
	    sampleraw.getReport().addField(field);
	    sampleraw.getReport().setStringValue(col, cell.getContent());
	  }
	}
      }
    }
  }
}
