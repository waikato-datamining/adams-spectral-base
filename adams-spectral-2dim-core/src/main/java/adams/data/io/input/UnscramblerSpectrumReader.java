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
 * UnscramblerSpectrumReader.java
 * Copyright (C) 2021-2024 University of Waikato, Hamilton, NZ
 */

package adams.data.io.input;

import adams.core.Range;
import adams.core.Utils;
import adams.core.io.FileUtils;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.env.Environment;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public class UnscramblerSpectrumReader
  extends AbstractSpectrumReader {

  private static final long serialVersionUID = 5378576103187717542L;

  public static final String ENTRY_PROJECT = "Project.xml";

  /** the streams to close at the end. */
  protected transient List<InputStream> m_Streams;

  /**
   * Returns a string describing the object.
   *
   * @return a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Reads Camo Unscrambler project files (zip archives).";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the
   * file chooser
   */
  @Override
  public String getFormatDescription() {
    return "Unscrambler project";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return the extension (without the dot!)
   */
  @Override
  public String[] getFormatExtensions() {
    return new String[]{".unsb"};
  }

  /**
   * Parses the zip entry as XML.
   *
   * @param archive	the zip archive
   * @param file	the file to parse
   * @return		the parsed DOM document
   * @throws Exception	if parsing of XML fails
   */
  protected Document readXML(ZipFile archive, ZipArchiveEntry file) throws Exception {
    BufferedInputStream		in;
    DocumentBuilderFactory 	factory;
    DocumentBuilder 		builder;
    Document result;

    factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(false);
    factory.setNamespaceAware(false);
    factory.setXIncludeAware(false);
    factory.setExpandEntityReferences(false);
    factory.setIgnoringComments(false);
    factory.setIgnoringElementContentWhitespace(false);
    builder = factory.newDocumentBuilder();
    in      = new BufferedInputStream(archive.getInputStream(file));
    m_Streams.add(in);

    result = builder.parse(in);

    return result;
  }

  /**
   * Reads the binary file all as floats and returns them.
   *
   * @param archive	the archive to read from
   * @param file	the binary file to read
   * @return		the floats from the file
   * @throws Exception	if reading the floats fails
   */
  protected TFloatList readBin(ZipFile archive, ZipArchiveEntry file) throws Exception {
    TFloatList		result;
    InputStream		in;
    BufferedInputStream	bin;
    byte[] 		b;
    float 		f;

    result = new TFloatArrayList();
    b      = new byte[4];
    in     = archive.getInputStream(file);
    m_Streams.add(in);
    bin    = new BufferedInputStream(in);
    m_Streams.add(bin);
    while ((bin.read(b)) == 4) {
      f = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat();
      result.add(f);
    }

    return result;
  }

  /**
   * Parses the project.xml file and extracts the data matrices.
   *
   * @param archive	the archive to load the project file from
   * @param project	the entry with the project information
   * @return		the matrix files (target - name)
   * @throws Exception	if loading fails
   */
  protected Map<String,String> listMatrixFiles(ZipFile archive, ZipArchiveEntry project) throws Exception {
    Map<String,String>	result;
    Document 		doc;
    NodeList		list;
    int			i;
    Node 		node;
    Element		elem;

    result  = new HashMap<>();
    doc     = readXML(archive, project);
    list    = doc.getElementsByTagName("data");
    for (i = 0; i < list.getLength(); i++) {
      node = list.item(i);
      if (node instanceof Element) {
        elem = (Element) node;
        if (elem.hasAttribute("type") && elem.getAttribute("type").equals("matrix"))
          result.put(FileUtils.useForwardSlashes(elem.getAttribute("target")), elem.getAttribute("name"));
      }
    }

    return result;
  }

  /**
   * Determines the range(s) that most likely contains the spectra.
   *
   * @param headersCol	the column headers
   * @param ranges	the ranges (name - range)
   * @return		the names of the ranges
   */
  protected List<String> determineSpectralRange(List<String> headersCol, Map<String,Range> ranges) {
    List<String>	result;
    Range		range;
    int[]		indices;
    boolean		numeric;

    result = new ArrayList<>();
    for (String name: ranges.keySet()) {
      range = ranges.get(name);
      range.setMax(headersCol.size());
      indices = range.getIntIndices();
      numeric = true;
      for (int index: indices) {
        if (!Utils.isFloat(headersCol.get(index))) {
          numeric = false;
          break;
	}
      }
      if (numeric)
        result.add(name);
    }

    return result;
  }

  /**
   * Generates the spectra from the parsed data.
   *
   * @param headersCol	the headers for the columns
   * @param headersRow	the headers for the rows
   * @param rangesCol	the defined ranges
   * @param dataRows	the float values per row
   */
  protected void generateSpectra(List<String> headersCol, List<String> headersRow, Map<String,Range> rangesCol, Map<String,Range> rangesRow, List<TFloatList> dataRows) {
    Set<String> 		spectralRanges;
    TFloatList			wavenos;
    TFloatList			dataRow;
    int				i;
    Spectrum			sp;
    Map<String,List<Spectrum>>	sps;
    Range 			rangeCol;
    Range			rangeRow;

    sps = new HashMap<>();
    spectralRanges = new HashSet<>(determineSpectralRange(headersCol, rangesCol));
    if (isLoggingEnabled())
      getLogger().info("Spectral ranges: " + spectralRanges);

    // spectra
    for (String spectralRange: spectralRanges) {
      sps.put(spectralRange, new ArrayList<>());
      wavenos  = new TFloatArrayList();
      rangeCol = rangesCol.get(spectralRange);
      rangeCol.setMax(headersCol.size());
      for (int index: rangeCol.getIntIndices())
	wavenos.add(Utils.toFloat(headersCol.get(index)));
      for (String rangeRowName: rangesRow.keySet()) {
        rangeRow = rangesRow.get(rangeRowName);
        rangeRow.setMax(dataRows.size());
        for (int d: rangeRow.getIntIndices()) {
	  dataRow = dataRows.get(d);
	  sp      = new Spectrum();
	  sp.setID(headersRow.get(d));
	  sp.getReport().setStringValue("Range-Column", spectralRange);
	  sp.getReport().setStringValue("Range-Row", rangeRowName);
	  for (i = 0; i < dataRow.size() && i < wavenos.size(); i++)
	    sp.add(new SpectrumPoint(wavenos.get(i), dataRow.get(i)));
	  sps.get(spectralRange).add(sp);
	}
      }
    }

    // meta-data
    for (String r: rangesCol.keySet()) {
      if (spectralRanges.contains(r))
        continue;
      for (int index: rangesCol.get(r).getIntIndices()) {
	for (String rangeRowName: rangesRow.keySet()) {
	  for (int d: rangesRow.get(rangeRowName).getIntIndices()) {
	    for (String spectralRange: spectralRanges)
	      sps.get(spectralRange).get(d).getReport().setNumericValue(headersCol.get(index), dataRows.get(d).get(index));
	  }
	}
      }
    }

    for (String spectralRange: sps.keySet())
      m_ReadData.addAll(sps.get(spectralRange));
  }

  /**
   * Loads the specified matrix.
   *
   * @param archive	the archive to load the matrix from
   * @param target	the target matrix XML file to load
   * @throws Exception	if loading fails
   */
  protected void loadMatrix(ZipFile archive, ZipArchiveEntry target) throws Exception {
    ZipArchiveEntry			bin;
    String				binName;
    Enumeration<ZipArchiveEntry> 	enm;
    ZipArchiveEntry			entry;
    Document				doc;
    TFloatList				floats;
    NodeList				list;
    Node				node;
    Element				elem;
    String[]				parts;
    int					width;
    int					height;
    int					i;
    List<String>			headersCol;
    List<String>			headersRow;
    Map<String,Range> 			rangesCol;
    Map<String,Range> 			rangesRow;
    List<TFloatList>			dataRows;
    int					y;

    bin     = null;
    binName = FileUtils.useForwardSlashes(FileUtils.replaceExtension(target.getName(), ".bin"));
    enm     = archive.getEntries();
    while (enm.hasMoreElements()) {
      entry = enm.nextElement();

      if (FileUtils.useForwardSlashes(entry.getName()).equals(binName)) {
        bin = entry;
        break;
      }
    }
    if (bin == null) {
      getLogger().warning("Failed to locate binary matrix file: " + binName);
      return;
    }

    // read raw data
    doc    = readXML(archive, target);
    floats = readBin(archive, bin);

    // determine dimensions of matrix
    list = doc.getElementsByTagName("matrix");
    if (list.getLength() != 1) {
      getLogger().warning("Expected exactly one 'matrix' tag, but found: " + list.getLength());
      return;
    }
    node   = list.item(0);
    width  = -1;
    height = -1;
    if (node instanceof Element) {
      elem = (Element) node;
      if (elem.hasAttribute("size")) {
        parts = elem.getAttribute("size").split(",");
        if (parts.length == 2) {
          height = Integer.parseInt(parts[0]);
          width  = Integer.parseInt(parts[1]);
	}
      }
    }
    if ((height == -1) || (width == -1)) {
      getLogger().warning("Failed to determine size of matrix!");
      return;
    }
    else {
      if (isLoggingEnabled())
        getLogger().info("Matrix dims (r/c): " + height + "/" + width);
    }

    // determine row/col headers
    list = doc.getElementsByTagName("headers");
    if (list.getLength() != 1) {
      getLogger().warning("Expected exactly one 'headers' tag, but found: " + list.getLength());
      return;
    }
    node       = list.item(0);
    headersCol = new ArrayList<>();
    headersRow = new ArrayList<>();
    list       = node.getChildNodes();
    for (i = 0; i < list.getLength(); i++) {
      node = list.item(i);
      if (node instanceof Element) {
        elem = (Element) node;
        if (elem.getNodeName().equals("v")) {
          if (elem.hasAttribute("htype")) {
            if (elem.getAttribute("htype").equals("0"))
              headersRow.addAll(Arrays.asList(elem.getTextContent().split("\t")));
	    else if (elem.getAttribute("htype").equals("1"))
              headersCol.addAll(Arrays.asList(elem.getTextContent().split("\t")));
	  }
	}
      }
    }
    if (headersCol.size() == 0) {
      getLogger().warning("Failed to determine column headers!");
      return;
    }
    else {
      if (isLoggingEnabled()) {
	getLogger().info("Column headers (" + headersCol.size() + "): " + headersCol);
	getLogger().info("Row headers (" + headersRow.size() + "): " + headersRow);
      }
    }

    // determine ranges
    list = doc.getElementsByTagName("ranges");
    if (list.getLength() != 1) {
      getLogger().warning("Expected exactly one 'ranges' tag, but found: " + list.getLength());
      return;
    }
    node      = list.item(0);
    rangesCol = new HashMap<>();
    rangesRow = new HashMap<>();
    list      = node.getChildNodes();
    for (i = 0; i < list.getLength(); i++) {
      node = list.item(i);
      if (node instanceof Element) {
	elem = (Element) node;
	if (elem.getNodeName().equals("v")) {
	  if (elem.getAttribute("type").equals("0"))
	    rangesRow.put(elem.getAttribute("name"), new Range(elem.getAttribute("range")));
	  else if (elem.getAttribute("type").equals("1"))
	    rangesCol.put(elem.getAttribute("name"), new Range(elem.getAttribute("range")));
	}
      }
    }
    if (rangesRow.size() == 0)
      rangesRow.put("All", new Range(Range.ALL));
    if (rangesCol.size() == 0) {
      getLogger().warning("Failed to determine ranges!");
      return;
    }
    else {
      if (isLoggingEnabled()) {
	getLogger().info("Column ranges: " + rangesCol);
	getLogger().info("Row ranges: " + rangesRow);
      }
    }

    // generate data rows
    dataRows = new ArrayList<>();
    for (i = 0; i < height; i++)
      dataRows.add(new TFloatArrayList());
    for (i = 0; i < floats.size(); i++) {
      y = i / width;
      if (y < height)
	dataRows.get(y).add(floats.get(i));
    }

    // turn parsed data into spectra
    generateSpectra(headersCol, headersRow, rangesCol, rangesRow, dataRows);
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    ZipFile 				archive;
    Enumeration<ZipArchiveEntry> 	enm;
    ZipArchiveEntry			entry;
    Map<String,String> 			matrices;

    m_Streams = new ArrayList<>();

    try {
      archive  = ZipFile.builder().setFile(m_Input.getAbsolutePath()).get();
      matrices = new HashMap<>();

      // locate project file
      enm = archive.getEntries();
      while (enm.hasMoreElements()) {
	entry = enm.nextElement();

	if (isLoggingEnabled())
	  getLogger().fine("Entry: " + entry.getName());

	// project file
	if (entry.getName().equals(ENTRY_PROJECT)) {
	  if (isLoggingEnabled())
	    getLogger().info("Reading project: " + entry.getName());
	  matrices = listMatrixFiles(archive, entry);
	  break;
	}
      }

      // locate project file
      enm = archive.getEntries();
      while (enm.hasMoreElements()) {
	entry = enm.nextElement();

	if (matrices.containsKey(FileUtils.useForwardSlashes(entry.getName()))) {
	  if (isLoggingEnabled())
	    getLogger().info("Loading matrix data: " + entry.getName());
	  loadMatrix(archive, entry);
	}
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to load data from: " + m_Input, e);
    }

    // close all streams
    while (m_Streams.size() > 0)
      FileUtils.closeQuietly(m_Streams.remove(m_Streams.size() - 1));
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
    runReader(Environment.class, UnscramblerSpectrumReader.class, args);
  }
}
