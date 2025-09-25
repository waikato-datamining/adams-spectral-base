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
 * CALSpectrumWriter.java
 * Copyright (C) 2011 University of Waikato, Hamilton, New Zealand
 */
package adams.data.io.output;

import adams.data.io.output.foss.FossOutputHelper.ConstituentValues;
import adams.data.io.output.foss.FossOutputHelper.Generalheader;
import adams.data.io.output.foss.FossOutputHelper.InstrumentHeader;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;

import java.util.List;

/**
 * Write spectra to .CAL file.
 * As per .NIR file, except with 'constituent values'. i.e. reference values.
 *
 * @author dale
 *
 */
public class CALSpectrumWriter
  extends NIRSpectrumWriter {

  /** suid. */
  private static final long serialVersionUID = 1282589771157923952L;

  /** constituents. */
  protected Field[] m_Constituents;


  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "constituents", "constituents",
	    new Field[0]);
  }

  /**
   * Sets the constituents.
   *
   * @param value	the fields
   */
  public void setConstituents(Field[] value) {
    m_Constituents = value;
  }

  /**
   * Returns the constituents.
   *
   * @return		the fields
   */
  public Field[] getConstituents() {
    return m_Constituents;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String constituentsTipText() {
    return "Constituents (modeling targets).";
  }

  /**
   * Create GeneralHeader from list of Spectra.
   * Needs to update file type, and number of constituents.
   *
   * @param data	list of Spectra
   * @return		GeneralHeader
   */
  public Generalheader getGeneralHeader(List<Spectrum> data){
    Generalheader gh=super.getGeneralHeader(data);
    gh.m_type = 02; // cal file
    gh.m_num_consts = m_Constituents.length;
    return(gh);
  }

  /**
   * Create InstrumentHeader using parameters.
   * Needs to update constituent names.
   *
   * @return	InstrumentHeader
   */
  public InstrumentHeader getInstrumentHeader(){
    InstrumentHeader ih=super.getInstrumentHeader();

    for (int i=0;i<m_Constituents.length;i++){
      ih.m_constituents[i]=m_Constituents[i].getName();
    }
    return(ih);
  }

  /**
   * Create ConstituentValues for a spectrum.
   * Needs to update constituent values.
   *
   * @param sp	spectrum
   * @return	ConstituentValues
   */
  public ConstituentValues getConstituentValues(Spectrum sp){
    ConstituentValues cv=new ConstituentValues();

    SampleData sampledata = sp.getReport();
    if (sp == null){
      return(cv);
    }
    cv.m_Constituents=new Float[m_Constituents.length];
    for (int i=0;i<m_Constituents.length;i++){
      Double target = sampledata.getDoubleValue(m_Constituents[i]);
      if (target != null){
	cv.m_Constituents[i]=target.floatValue();
      } else {
	cv.m_Constituents[i]=0.0f;
      }
    }
    return(cv);
  }

  /**
   * Returns a string describing the format (used in the file chooser).
   *
   * @return a description suitable for displaying in the file chooser
   */
  public String getFormatDescription() {
    return ".CAL Format";
  }

  /**
   * Returns the extension(s) of the format.
   *
   * @return 		the extension(s) (without the dot!)
   */
  public String[] getFormatExtensions() {
    return new String[]{"cal"};
  }
}
