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
 * RowWiseSpreadSheetSpectrumReader.java
 * Copyright (C) 2015-2024 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input;

import adams.core.base.BaseRegExp;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.spreadsheet.SpreadSheetColumnIndex;
import adams.data.spreadsheet.SpreadSheetColumnRange;
import adams.data.spreadsheet.rowfinder.AllFinder;
import adams.data.spreadsheet.rowfinder.RowFinder;
import adams.env.Environment;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Reads spectra from rows in a spreadsheet obtained with the specified spreadsheet reader.<br>
 * Sample ID and sample data columns get removed automatically from the range of wave columns.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * &nbsp;&nbsp;&nbsp;min-user-mode: Expert
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
 * <pre>-reader &lt;adams.data.io.input.SpreadSheetReader&gt; (property: reader)
 * &nbsp;&nbsp;&nbsp;The spreadsheet reader to use for reading the raw data.
 * &nbsp;&nbsp;&nbsp;default: adams.data.io.input.CsvSpreadSheetReader -data-row-type adams.data.spreadsheet.DenseDataRow -spreadsheet-type adams.data.spreadsheet.DefaultSpreadSheet
 * </pre>
 *
 * <pre>-row-finder &lt;adams.data.spreadsheet.rowfinder.RowFinder&gt; (property: rowFinder)
 * &nbsp;&nbsp;&nbsp;The row finder to use for optionally filtering the rows.
 * &nbsp;&nbsp;&nbsp;default: adams.data.spreadsheet.rowfinder.AllFinder
 * </pre>
 *
 * <pre>-sample-id-column &lt;adams.data.spreadsheet.SpreadSheetColumnIndex&gt; (property: sampleIDColumn)
 * &nbsp;&nbsp;&nbsp;The column containing the sample ID.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-wave-columns &lt;adams.data.spreadsheet.SpreadSheetColumnRange&gt; (property: waveColumns)
 * &nbsp;&nbsp;&nbsp;The columns containing the wave amplitudes.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 * <pre>-header-contains-wave-number &lt;boolean&gt; (property: headerContainsWaveNumber)
 * &nbsp;&nbsp;&nbsp;The columns containing the wave amplitudes.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-wave-number-regexp &lt;adams.core.base.BaseRegExp&gt; (property: waveNumberRegExp)
 * &nbsp;&nbsp;&nbsp;The regular expression to identify the wave number (1st group is used).
 * &nbsp;&nbsp;&nbsp;default: (.*)
 * &nbsp;&nbsp;&nbsp;more: https:&#47;&#47;docs.oracle.com&#47;javase&#47;tutorial&#47;essential&#47;regex&#47;
 * &nbsp;&nbsp;&nbsp;https:&#47;&#47;docs.oracle.com&#47;en&#47;java&#47;javase&#47;11&#47;docs&#47;api&#47;java.base&#47;java&#47;util&#47;regex&#47;Pattern.html
 * </pre>
 *
 * <pre>-sample-data-columns &lt;adams.data.spreadsheet.SpreadSheetColumnRange&gt; (property: sampleDataColumns)
 * &nbsp;&nbsp;&nbsp;The columns containing the sample data.
 * &nbsp;&nbsp;&nbsp;default:
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; column names (case-sensitive) as well as the following placeholders can be used: first, second, third, last_2, last_1, last; numeric indices can be enforced by preceding them with '#' (eg '#12'); column names can be surrounded by double quotes.
 * </pre>
 *
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class RowWiseSpreadSheetSpectrumReader
  extends AbstractSpectrumReader
  implements MetaFileReader {

  private static final long serialVersionUID = -5214547184281992617L;

  /**
   * Ancestor for sheet iterators.
   */
  public static abstract class SheetIterator
    implements Iterator<SpreadSheet> {

    /** the reader. */
    protected SpreadSheetReader m_Reader;

    /** the input file. */
    protected File m_Input;

    /**
     * Initializes the iterator.
     *
     * @param reader	the reader to use
     * @param input	the file to read from
     */
    public SheetIterator(SpreadSheetReader reader, File input) {
      m_Reader = reader;
      m_Input  = input;
    }
  }

  /**
   * Iterator for single-sheet readers.
   */
  public static class SingleSheetIterator
    extends SheetIterator {

    /** the list of sheets. */
    protected List<SpreadSheet> m_Sheets;

    /**
     * Initializes the iterator.
     *
     * @param reader	the reader to use
     * @param input	the file to read from
     */
    public SingleSheetIterator(SpreadSheetReader reader, File input) {
      super(reader, input);
    }

    /**
     * Reads the sheets if necessary and returns them.
     *
     * @return		the sheets to iterate over
     */
    protected List<SpreadSheet> sheet() {
      if (m_Sheets == null) {
	m_Sheets = new ArrayList<>();
	m_Sheets.add(m_Reader.read(m_Input));
      }
      return m_Sheets;
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
      return !sheet().isEmpty();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public SpreadSheet next() {
      if (sheet().isEmpty())
	throw new NoSuchElementException();
      return sheet().remove(0);
    }
  }

  /**
   * Iterator for multi-sheet readers.
   */
  public static class MultiSheetIterator
    extends SheetIterator {

    /** the list of sheets. */
    protected List<SpreadSheet> m_Sheets;

    /**
     * Initializes the iterator.
     *
     * @param reader	the reader to use
     * @param input	the file to read from
     */
    public MultiSheetIterator(MultiSheetSpreadSheetReader reader, File input) {
      super(reader, input);
    }

    /**
     * Reads the sheets if necessary and returns them.
     *
     * @return		the sheets to iterate over
     */
    protected List<SpreadSheet> sheets() {
      if (m_Sheets == null)
	m_Sheets = ((MultiSheetSpreadSheetReader) m_Reader).readRange(m_Input);
      return m_Sheets;
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
      return !sheets().isEmpty();
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public SpreadSheet next() {
      if (sheets().isEmpty())
	throw new NoSuchElementException();
      return sheets().remove(0);
    }
  }

  /**
   * Iterator for chunked spreadsheet readers.
   */
  public static class ChunkedSheetIterator
    extends SheetIterator {

    /** the next chunk. */
    protected SpreadSheet m_Chunk;

    /**
     * Initializes the iterator.
     *
     * @param reader	the reader to use
     * @param input	the file to read from
     */
    public ChunkedSheetIterator(ChunkedSpreadSheetReader reader, File input) {
      super(reader, input);
      m_Chunk = m_Reader.read(input);
    }

    /**
     * Reads the next chunk if necessary.
     *
     * @return		the next chunk, null if none available
     */
    protected SpreadSheet chunk() {
      if (m_Chunk == null) {
	if (((ChunkedSpreadSheetReader) m_Reader).hasMoreChunks())
	  m_Chunk = ((ChunkedSpreadSheetReader) m_Reader).nextChunk();
      }
      return m_Chunk;
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
      return (chunk() != null);
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public SpreadSheet next() {
      SpreadSheet	result;

      if (chunk() == null)
	throw new NoSuchElementException();

      result  = chunk();
      m_Chunk = null;

      return result;
    }

  }

  /** the spreadsheet reader to use. */
  protected SpreadSheetReader m_Reader;

  /** the row finder to use. */
  protected RowFinder m_RowFinder;

  /** the column containing the sample ID. */
  protected SpreadSheetColumnIndex m_SampleIDColumn;

  /** the range of columns containing wave amplitudes. */
  protected SpreadSheetColumnRange m_WaveColumns;

  /** whether the column header is the wave number. */
  protected boolean m_HeaderContainsWaveNumber;

  /** the regular expression to extract the wave number from the header (first group is used). */
  protected BaseRegExp m_WaveNumberRegExp;

  /** the range of columns containing sample data. */
  protected SpreadSheetColumnRange m_SampleDataColumns;


  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
      "Reads spectra from rows in a spreadsheet obtained with the "
	+ "specified spreadsheet reader.\n"
	+ "Sample ID and sample data columns get removed automatically from the "
	+ "range of wave columns.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "reader", "reader",
      new CsvSpreadSheetReader());

    m_OptionManager.add(
      "row-finder", "rowFinder",
      new AllFinder());

    m_OptionManager.add(
      "sample-id-column", "sampleIDColumn",
      new SpreadSheetColumnIndex());

    m_OptionManager.add(
      "wave-columns", "waveColumns",
      new SpreadSheetColumnRange());

    m_OptionManager.add(
      "header-contains-wave-number", "headerContainsWaveNumber",
      false);

    m_OptionManager.add(
      "wave-number-regexp", "waveNumberRegExp",
      new BaseRegExp("(.*)"));

    m_OptionManager.add(
      "sample-data-columns", "sampleDataColumns",
      new SpreadSheetColumnRange());
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Row-wise: " + m_Reader.getFormatDescription();
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return m_Reader.getFormatExtensions();
  }

  /**
   * Returns the underlying format extensions.
   *
   * @return		the format extensions (excluding dot)
   */
  public String[] getActualFormatExtensions() {
    return m_Reader.getFormatExtensions();
  }

  /**
   * Sets the spreadsheet reader to use.
   *
   * @param value	the reader
   */
  public void setReader(SpreadSheetReader value) {
    m_Reader = value;
    reset();
  }

  /**
   * Returns the spreadsheet reader in use.
   *
   * @return		the reader
   */
  public SpreadSheetReader getReader() {
    return m_Reader;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String readerTipText() {
    return "The spreadsheet reader to use for reading the raw data.";
  }

  /**
   * Sets the row finder to use for filtering the rows.
   *
   * @param value	the row finder
   */
  public void setRowFinder(RowFinder value) {
    m_RowFinder = value;
    reset();
  }

  /**
   * Returns the row finder to use for filtering the rows.
   *
   * @return		the row finder
   */
  public RowFinder getRowFinder() {
    return m_RowFinder;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rowFinderTipText() {
    return "The row finder to use for optionally filtering the rows.";
  }

  /**
   * Sets the column containing the sample ID.
   *
   * @param value	the column
   */
  public void setSampleIDColumn(SpreadSheetColumnIndex value) {
    m_SampleIDColumn = value;
    reset();
  }

  /**
   * Returns the column containing the sample ID.
   *
   * @return		the column
   */
  public SpreadSheetColumnIndex getSampleIDColumn() {
    return m_SampleIDColumn;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sampleIDColumnTipText() {
    return "The column containing the sample ID.";
  }

  /**
   * Sets the columns containing the wave amplitudes.
   *
   * @param value	the columns
   */
  public void setWaveColumns(SpreadSheetColumnRange value) {
    m_WaveColumns = value;
    reset();
  }

  /**
   * Returns the columns containing the wave amplitudes.
   *
   * @return		the columns
   */
  public SpreadSheetColumnRange getWaveColumns() {
    return m_WaveColumns;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String waveColumnsTipText() {
    return "The columns containing the wave amplitudes.";
  }

  /**
   * Sets whether to extract the wave number from the header columns.
   *
   * @param value	true if to extract
   */
  public void setHeaderContainsWaveNumber(boolean value) {
    m_HeaderContainsWaveNumber = value;
    reset();
  }

  /**
   * Returns whether to extract the wave number from the header columns.
   *
   * @return		true if to extract
   */
  public boolean getHeaderContainsWaveNumber() {
    return m_HeaderContainsWaveNumber;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String headerContainsWaveNumberTipText() {
    return "The columns containing the wave amplitudes.";
  }

  /**
   * Sets the regular expression to identify the wave number (1st group is used).
   *
   * @param value	the expression
   */
  public void setWaveNumberRegExp(BaseRegExp value) {
    m_WaveNumberRegExp = value;
    reset();
  }

  /**
   * Returns the regular expression to identify the wave number (1st group is used).
   *
   * @return		the expression
   */
  public BaseRegExp getWaveNumberRegExp() {
    return m_WaveNumberRegExp;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String waveNumberRegExpTipText() {
    return "The regular expression to identify the wave number (1st group is used).";
  }

  /**
   * Sets the columns containing the sample data.
   *
   * @param value	the columns
   */
  public void setSampleDataColumns(SpreadSheetColumnRange value) {
    m_SampleDataColumns = value;
    reset();
  }

  /**
   * Returns the columns containing the sample data.
   *
   * @return		the columns
   */
  public SpreadSheetColumnRange getSampleDataColumns() {
    return m_SampleDataColumns;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String sampleDataColumnsTipText() {
    return "The columns containing the sample data.";
  }

  /**
   * Indentifies the wave numbers.
   *
   * @param sheet	the spreadsheet to analyze
   * @param cols	the columns
   * @return		the wave numbers
   */
  protected TFloatArrayList identifyWaveNumbers(SpreadSheet sheet, int[] cols) {
    TFloatArrayList	result;
    String		name;
    String		group;
    int			i;
    int			col;

    result = new TFloatArrayList();

    for (i = 0; i < cols.length; i++) {
      col = cols[i];
      if (m_HeaderContainsWaveNumber) {
	name = sheet.getColumnName(col);
	group = name.replaceAll(m_WaveNumberRegExp.getValue(), "$1");
	try {
	  result.add(Float.parseFloat(group));
	}
	catch (Exception e) {
	  result.add(i + 1);
	  getLogger().severe("Failed to parse column header/wave number #" + (col+1) + ": " + name + "/" + group);
	}
      }
      else {
	result.add(i + 1);
      }
    }

    return result;
  }

  /**
   * Returns the sample data fields.
   *
   * @param sheet	the sheet to analyze
   * @param cols	the columns
   * @return		the fields
   */
  protected List<Field> identifySampleData(SpreadSheet sheet, int[] cols) {
    List<Field>		result;
    Field		field;

    result = new ArrayList<>();

    for (int col: cols) {
      if (sheet.isNumeric(col))
	field = new Field(sheet.getColumnName(col), DataType.NUMERIC);
      else
	field = new Field(sheet.getColumnName(col), DataType.STRING);
      result.add(field);
    }

    return result;
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    TIntArrayList	cols;
    TFloatArrayList	waveNo;
    int[]		waveCols;
    List<SpectrumPoint>	points;
    List<Field> 	sdFields;
    Field		field;
    int[]		sdCols;
    int			idCol;
    Spectrum 		sp;
    SpectrumPoint 	point;
    SampleData 		sd;
    int			i;
    int			sheetNo;
    SheetIterator	iterator;
    SpreadSheet		sheet;
    boolean		filter;
    int[]		rowIndices;
    List<Row>		rows;

    if (m_Reader instanceof MultiSheetSpreadSheetReader)
      iterator = new MultiSheetIterator((MultiSheetSpreadSheetReader) m_Reader, m_Input);
    else if (m_Reader instanceof ChunkedSpreadSheetReader)
      iterator = new ChunkedSheetIterator((ChunkedSpreadSheetReader) m_Reader, m_Input);
    else
      iterator = new SingleSheetIterator(m_Reader, m_Input);
    if (m_Stopped)
      return;

    filter   = !(m_RowFinder instanceof AllFinder);
    sheetNo  = 0;
    points   = new ArrayList<>();
    rows     = new ArrayList<>();
    idCol    = -1;
    waveCols = new int[0];
    waveNo   = new TFloatArrayList();
    sdCols   = new int[0];
    sdFields = new ArrayList<>();
    while (iterator.hasNext()) {
      sheet = iterator.next();
      sheetNo++;
      if (m_Stopped)
	return;

      // filter rows?
      rows.clear();
      if (filter) {
	rowIndices = m_RowFinder.findRows(sheet);
	for (int rowIndex: rowIndices)
	  rows.add(sheet.getRow(rowIndex));
      }
      else {
	rows.addAll(sheet.rows());
      }

      // any rows
      if (rows.isEmpty())
	continue;

      if (sheetNo == 1) {
	// ID
	m_SampleIDColumn.setData(sheet);
	idCol = m_SampleIDColumn.getIntIndex();

	// sample data
	m_SampleDataColumns.setData(sheet);
	cols = new TIntArrayList(m_SampleDataColumns.getIntIndices());
	cols.remove(idCol);
	sdCols = cols.toArray();
	sdFields = identifySampleData(sheet, sdCols);

	// wave numbers
	m_WaveColumns.setData(sheet);
	cols = new TIntArrayList(m_WaveColumns.getIntIndices());
	cols.remove(idCol);
	for (int col : sdCols)
	  cols.remove(col);
	waveCols = cols.toArray();
	waveNo = identifyWaveNumbers(sheet, waveCols);
      }

      for (Row row : rows) {
	if (m_Stopped)
	  return;
	sp = new Spectrum();

	// wave numbers
	points.clear();
	for (i = 0; i < waveCols.length; i++) {
	  if (m_Stopped)
	    return;
	  if (row.hasCell(waveCols[i]) && !row.getCell(waveCols[i]).isMissing()) {
	    try {
	      point = new SpectrumPoint(waveNo.get(i), row.getCell(waveCols[i]).toDouble().floatValue());
	      points.add(point);
	    }
	    catch (Exception e) {
	      getLogger().log(
		Level.SEVERE,
		"Failed to convert cell in col #" + (waveCols[i] + 1) + " of sheet " + sheetNo + ": "
		  + row.getCell(waveCols[i]), e);
	    }
	  }
	}
	sp.addAll(points);

	// sample data
	sd = new SampleData();
	for (i = 0; i < sdCols.length; i++) {
	  if (m_Stopped)
	    return;
	  if (row.hasCell(sdCols[i]) && !row.getCell(sdCols[i]).isMissing()) {
	    field = sdFields.get(i);
	    sd.addField(field);
	    sd.setValue(field, row.getCell(sdCols[i]).getContent());
	  }
	}
	sp.setReport(sd);

	// sample ID
	if (idCol != -1) {
	  if (row.hasCell(idCol) && !row.getCell(idCol).isMissing())
	    sp.setID(row.getCell(idCol).getContent());
	}

	m_ReadData.add(sp);
	if (isLoggingEnabled())
	  getLogger().info("Added: " + sp);
      }
    }
  }

  /**
   * Stops the execution.
   */
  public void stopExecution() {
    super.stopExecution();
    m_Reader.stopExecution();
  }

  /**
   * Runs the reader from the command-line.
   *
   * If the option {@link #OPTION_OUTPUTDIR} is specified then the read spectra
   * get output as .spec files in that directory.
   *
   * @param args	the command-line options to use
   */
  public static void main(String[] args) {
    runReader(Environment.class, RowWiseSpreadSheetSpectrumReader.class, args);
  }
}
