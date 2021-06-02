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
 * CALSpectrumReader.java
 * Copyright (C) 2009-2021 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.IEEE754;
import adams.core.io.FileUtils;
import adams.data.io.input.foss.FossHelper;
import adams.data.io.input.foss.FossHelper.FossFields;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.env.Environment;

import java.util.Vector;

/**
 <!-- globalinfo-start -->
 * Reads spectra in BLGG ASC format.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-input &lt;java.io.File&gt; (property: input)
 *         The file to read and turn into a spectrum.
 *         default: .
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class CALSpectrumReader
  extends AbstractSpectrumReader {

  /** for serialization. */
  private static final long serialVersionUID = -1173018986741833982L;

  /** where to get sample type from. see param defs*/
  protected String sample_type;

  /** where to get sample id from. see param defs*/
  protected String sample_id;

  /** starting spectrum. **/
  protected int m_start;

  /** maximum to load. **/
  protected int m_max;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return "Reads spectra in FOSS Cal format.";
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return 			a description suitable for displaying in the
   * 				file chooser
   */
  public String getFormatDescription() {
    return "FOSS CAL Format";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 			the extension(s) (without the dot!)
   */
  public String[] getFormatExtensions() {
    return new String[]{"cal"};
  }

  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "typefield", "typefield",
      "Code");

    m_OptionManager.add(
      "idfield", "idfield",
      "ID");

    m_OptionManager.add(
      "start", "start",
      1);

    m_OptionManager.add(
      "max", "max",
      -1);
  }
  /**
   * Sets the nth point setting.
   *
   * @param value 	the nth point to use
   */
  public void setMax(int value) {
    m_max = value;
    reset();
  }

  /**
   * Returns the nth point setting.
   *
   * @return 		the nth point
   */
  public int getMax() {
    return m_max;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String maxTipText() {
    return "Maximum spectra to load.";
  }
  /**
   * Sets the start point setting.
   *
   * @param value 	the nth point to use
   */
  public void setStart(int value) {
    m_start = value;
    reset();
  }

  /**
   * Returns the start point setting.
   *
   * @return 		the nth point
   */
  public int getStart() {
    return m_start;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String startTipText() {
    return "Spectrum number to start loading from.";
  }

  /**
   * Get id field.
   *
   * @return	id field
   */
  public String getIdfield(){
    return(sample_id);
  }

  /**
   * Set id field.
   *
   * @param tf
   */
  public void setIdfield(String tf){
    sample_id=tf;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String idfieldTipText(){
    return("ID|Field1|Field2|Field3|[prefix]");
  }

  /**
   * Get type field.
   *
   * @return	type field
   */
  public String getTypefield(){
    return(sample_type);
  }

  /**
   * Set type field.
   *
   * @param tf
   */
  public void setTypefield(String tf){
    sample_type=tf;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String typefieldTipText(){
    return("Code|Field1|Field2|Field3|ID|[sample_type]");
  }

  /**
   * Get SampleID.
   *
   * @param ff	fields loaded from cal file
   * @return	sampleid
   */
  protected String getID(FossFields ff){
    if (sample_id.equalsIgnoreCase("id")){
      return(ff.id);
    }
    if (sample_id.equalsIgnoreCase("field1")){
      return(ff.id1);
    }
    if (sample_id.equalsIgnoreCase("field2")){
      return(ff.id2);
    }
    if (sample_id.equalsIgnoreCase("field3")){
      return(ff.id3);
    }
    return(sample_id+m_Input.getName()+ff.getRowNum());
  }

  /**
   * Get Sampletype.
   *
   * @param ff	fields loaded from cal file
   * @return	sampletype
   */

  protected String getSampleType(FossFields ff){
    if (sample_type.equalsIgnoreCase("code")){
      return(""+ff.product_code);
    }
    if (sample_type.equalsIgnoreCase("field1")){
      return(ff.id1);
    }
    if (sample_type.equalsIgnoreCase("field2")){
      return(ff.id2);
    }
    if (sample_type.equalsIgnoreCase("field3")){
      return(ff.id3);
    }
    if (sample_type.equalsIgnoreCase("id")){
      return(ff.id);
    }

    return(sample_type);
  }

  /**
   * Performs the actual reading.
   */
  protected void readData() {

    FossHelper fh=new FossHelper(FileUtils.loadFromBinaryFile(m_Input.getAbsoluteFile()));

    fh.processHeader();
    Vector<String> v=fh.getReferenceNames();
    int num_deleted=0;
    int act_count=0; // start at 1
    double[] wn=fh.getWavenumbers();
    for (int i=0;i<fh.getTotal();i++){
      FossFields ff=fh.getFields(i);
      if (ff.deleted){
	num_deleted++;
	continue;
      }
      act_count++;
      if (act_count<getStart()){
	continue;
      }
      ff.setNumDeleted(num_deleted);

      String id=getID(ff);
      if (id.equals("")){
	continue;
      }
      String sampletype=getSampleType(ff);
      if (sampletype.equals("")){
	continue;
      }

      Spectrum sp = new Spectrum();
      sp.setID(id);

      double[] nir=IEEE754.toDoubleArray(fh.getSpectraForRow(i));
      if (wn==null || nir.length != wn.length){
	getLogger().severe("Different no. of wavenumbers and amplitudes");
	for (int j = 0; j < nir.length; j++) {
	  sp.add(new SpectrumPoint((float)j, (float) nir[j]));
	}
      } else {
	for (int j = 0; j < nir.length; j++) {
	  sp.add(new SpectrumPoint((float)wn[j], (float) nir[j]));
	}
      }


      SampleData sd=new SampleData();
      sd.addParameter(SampleData.SAMPLE_TYPE,sampletype);

      if (fh.getRefCount() > 0){ // no reference data, ignore
	if (fh.getRefCount() != v.size()){ // different number of reference values than names
	  getLogger().severe("Reference data is inconsistant");
	} else {
	  float[] d=fh.getRefForRow(i);

	  int count=0;
	  for (String ref:v){
	    ref=ref.toLowerCase();
	    if (d[count] == 0){
	      count++;
	      continue;
	    }
	    sd.addField(new Field(ref, DataType.NUMERIC));
	    sd.addParameter(ref, (double)d[count++]);
	  }
	}
      }
      sp.setReport(sd);
      m_ReadData.add(sp);
      if (getMax()!= -1 && m_ReadData.size() >= getMax()){
	break;
      }
    }
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
    runReader(Environment.class, CALSpectrumReader.class, args);
  }
}
