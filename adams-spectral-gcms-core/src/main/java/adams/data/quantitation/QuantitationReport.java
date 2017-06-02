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
 * QuantitationReport.java
 * Copyright (C) 2008-2016 University of Waikato, Hamilton, New Zealand
 *
 */

package adams.data.quantitation;

import adams.core.DateFormat;
import adams.core.DateUtils;
import adams.core.Properties;
import adams.data.id.MutableIDHandler;
import adams.data.report.AbstractField;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.Report;

import java.util.Date;
import java.util.Hashtable;

/**
 * Read and store data from the Quantitation Report.
 *
 * @author dale
 * @version $Revision: 4381 $
 */
public class QuantitationReport
  extends Report
  implements MutableIDHandler {

  /** for serialization. */
  private static final long serialVersionUID = -7112738075589393290L;

  /** target compounds header. */
  public final static String COMPOUNDS = "Target Compounds";
  // End Header names

  // fields
  /** field: Data File. */
  public final static String FIELD_SAMPLEID = "SampleID";

  /** field: Data File. */
  public final static String FIELD_DATAFILE = "Data File";

  /** field: Vial. */
  public final static String FIELD_VIAL = "Vial";

  /** field: Sample. */
  public final static String FIELD_SAMPLE = "Sample";

  /** field: Inst. */
  public final static String FIELD_INST = "Inst";

  /** field: Acq On. */
  public final static String FIELD_ACQON = "Acq On";

  /** field: Acq On Sortable. */
  public final static String FIELD_ACQON_SORTABLE = "Acq On - Sortable";

  /** field: Detector. */
  public final static String FIELD_DETECTOR = "Detector";

  /** field: Acq Meth. */
  public final static String FIELD_ACQMETH = "Acq Meth";

  /** field: Operator. */
  public final static String FIELD_OPERATOR = "Operator";

  /** field: Quant Time. */
  public final static String FIELD_QUANTTIME = "Quant Time";

  /** field: Quant Results File. */
  public final static String FIELD_QUANTRESULTSFILE = "Quant Results File";

  /** field: MS IntFile. */
  public final static String FIELD_MSINTFILE = "MS IntFile";

  /** field: Multiplr. */
  public final static String FIELD_MULTIPLR = "Multiplr";

  /** field: Quant Method|Method Loaded. */
  public final static String FIELD_QUANTMETH = "Quant Method|Method Loaded";

  /** field: Title. */
  public final static String FIELD_TITLE = "Title";

  /** field: Last Update. */
  public final static String FIELD_LASTUPDATE = "Last Update";

  /** field: Response via. */
  public final static String FIELD_RESPONSEVIA = "Response via";

  /** qr retention times updated? */
  public final static String FIELD_RT_UPDATED = "RT_UPDATED";

  /** field: Calibration set. */
  public final static String FIELD_CALIBRATION_SET = "Calibration set";

  /** compound: Retention Time difference from calibration. -1 means not found*/
  public final static String COMPOUND_RETENTIONTIME_DIFF_FROM_CALIB="RT_DIFF";

  /** compound: Retention Time. */
  public final static String COMPOUND_RETENTIONTIME="R.T.";

  /** compound: QIon. */
  public final static String COMPOUND_QION="QIon";

  /** compound: Response. */
  public final static String COMPOUND_RESPONSE="Response";

  /** compound: Concentration. */
  public final static String COMPOUND_CONCENTRATION="Conc";

  /** compound: comment suffix for fields. */
  public final static String COMPOUND_COMMENT_SUFFIX = "-Comment";

  /** compound: Calibration Concentration. */
  public final static String COMPOUND_CALIBRATION_CONCENTRATION="Calibration-Conc";

  /** compound: Calibration Concentration. */
  public final static String COMPOUND_CALIBRATION_CONCENTRATION_STD="Calibration-Conc-Std";

  /** compound: Units. */
  public final static String COMPOUND_UNITS="Units";

  /** compound: Dev(min). */
  public final static String COMPOUND_DEVMIN="Dev(Min)";

  /** compound: Qvalue. */
  public final static String COMPOUND_QVALUE="QValue";

  /** compound: manual integration. */
  public final static String COMPOUND_MANUALINTEGRATION="m";

  /** compound: qoor. */
  public final static String COMPOUND_QOOR="qoor";

  /** compound GEX AREA. */
  public final static String COMPOUND_GEX_AREA="GEX_AREA";

  /** compound AREA. */
  public final static String COMPOUND_AREA="AREA";

  /** compound standardised AREA. */
  public final static String COMPOUND_STD_AREA="STD_AREA";

  /** compound Concentration. */
  public final static String COMPOUND_CALC_CONCENTRATION="CONCENTRATION";

  /** compound GEX AREA per conc unit. */
  public final static String COMPOUND_CALIB_GEX_AREA_DIV_CONC="GEX_CALIB_AREA_DIV_CONC";

  /** compound GEX AREA of calib. */
  public final static String COMPOUND_CALIB_GEX_AREA="GEX_CALIB_AREA";

  /** compound GEX A. */
  public final static String COMPOUND_CALIB_GEX_A="GEX_CALIB_A";

  /** compound GEX B. */
  public final static String COMPOUND_CALIB_GEX_B="GEX_CALIB_B";

  /** compound GEX H. */
  public final static String COMPOUND_CALIB_GEX_H="GEX_CALIB_H";

  /** compound GEX TM. */
  public final static String COMPOUND_CALIB_GEX_TM="GEX_CALIB_TM";

  /** compound GEX TO. */
  public final static String COMPOUND_CALIB_GEX_TO="GEX_CALIB_TO";

  /** compound GEX AREA as fraction of Calibration Std. */
  public final static String COMPOUND_GEX_AREA_STD="GEX_AREA_STDMULT";

  /** compound GEX AREA as fraction of Internal Std. */
  public final static String COMPOUND_GEX_AREA_INT_RATIO="GEX_AREA_INT_RATIO";

  /** compound GEX AREA calculated from calibration and standard spike. */
  public final static String COMPOUND_GEX_CALC="GEX_CALC";

  /** compound GEX AREA calculated from calibration ignoring standard spike. */
  public final static String COMPOUND_GEX_CALC_UNSTDISE="GEX_CALC_UNSTD";

  /** compound GEX AREA RMSE of peak. */
  public final static String COMPOUND_GEX_RMSE_PCT="GEX_RMSE_PCT";

  /** compound GEX AREA RMSE of calib peak. */
  public final static String COMPOUND_GEX_RMSE_CALIB_PCT="GEX_RMSE_CALIB_PCT";

  /** predicted response suffix. */
  public final static String COMPOUND_PREDICTED_RESPONSE = "Predicted response";

  /** CAS suffix. */
  public final static String COMPOUND_CAS = "CAS";

  /** the separator. */
  public final static String COMPOUND_SEPARATOR = Field.SEPARATOR;

  /** the date format for Date Acquired. */
  public final static String ACQON_DATE_FORMAT = "d MMM yyyy h:mm a";

  /** the field for being a standard. */
  public final static String FIELD_ISSTANDARD = "Is Standard";

  /** the field for being a calibration standard. */
  public final static String FIELD_ISCALIBRATIONSTANDARD = "Is Calibration Standard";

  /** the compound fields. */
  protected Hashtable<String, Field> m_compoundtypes;

  /**
   * Default constructor.
   */
  public QuantitationReport() {
    super();

    // by default, we assume it is not a dummy report
    setDummyReport(false);
  }

  /**
   * Returns the ID, i.e, sample ID.
   *
   * @return		the ID, or null if no sample ID set
   * @see		#FIELD_SAMPLEID
   */
  public String getID() {
    String	result;
    Field	field;

    result = null;

    field = new Field(FIELD_SAMPLEID, DataType.STRING);
    if (hasValue(field))
      result = getStringValue(field);

    return result;
  }

  /**
   * Sets the ID. Removes any single quotes from the ID string.
   *
   * @param value	the ID
   */
  public void setID(String value) {
    setValue(new Field(FIELD_SAMPLEID, DataType.STRING), value.replace("'", ""));
  }

  /**
   * Set field types.
   */
  @Override
  protected void initFields(){
    super.initFields();

    addField(new Field(FIELD_SAMPLEID,DataType.STRING));
    addField(new Field(FIELD_DATAFILE,DataType.STRING));
    addField(new Field(FIELD_VIAL,DataType.NUMERIC));
    addField(new Field(FIELD_SAMPLE,DataType.STRING));
    addField(new Field(FIELD_INST,DataType.STRING));
    addField(new Field(FIELD_ACQON,DataType.STRING));
    addField(new Field(FIELD_DETECTOR,DataType.STRING));
    addField(new Field(FIELD_ACQMETH,DataType.STRING));
    addField(new Field(FIELD_OPERATOR,DataType.STRING));
    addField(new Field(FIELD_QUANTTIME,DataType.STRING));
    addField(new Field(FIELD_QUANTRESULTSFILE,DataType.STRING));
    addField(new Field(FIELD_MSINTFILE,DataType.STRING));
    addField(new Field(FIELD_MULTIPLR,DataType.NUMERIC));
    addField(new Field(FIELD_QUANTMETH,DataType.STRING));
    addField(new Field(FIELD_TITLE,DataType.STRING));
    addField(new Field(FIELD_LASTUPDATE,DataType.STRING));
    addField(new Field(FIELD_RESPONSEVIA,DataType.STRING));

    m_compoundtypes = new Hashtable<String, Field>();
    addCompoundType(new Field(COMPOUND_RETENTIONTIME,DataType.NUMERIC));
    addCompoundType(new Field(COMPOUND_QION,DataType.NUMERIC));
    addCompoundType(new Field(COMPOUND_RESPONSE,DataType.NUMERIC));
    addCompoundType(new Field(COMPOUND_CONCENTRATION,DataType.NUMERIC));
    addCompoundType(new Field(COMPOUND_CONCENTRATION + COMPOUND_COMMENT_SUFFIX,DataType.STRING));
    addCompoundType(new Field(COMPOUND_UNITS,DataType.STRING));
    addCompoundType(new Field(COMPOUND_DEVMIN,DataType.NUMERIC));
    addCompoundType(new Field(COMPOUND_QVALUE,DataType.NUMERIC));
    addCompoundType(new Field(COMPOUND_RETENTIONTIME_DIFF_FROM_CALIB,DataType.NUMERIC));
    addCompoundType(new Field(COMPOUND_MANUALINTEGRATION,DataType.BOOLEAN));
    addCompoundType(new Field(COMPOUND_QOOR,DataType.BOOLEAN));
  }

  /**
   * Is the sample name in useable format?
   *
   * @param sn		samplename
   * @return		true if ok
   */
  public static boolean sampleNameOK(String sn){
    return(sn.toUpperCase().matches("APHSV\\.[0-9][0-9]*\\.[0-9][0-9]*"));
  }

  /**
   * Adds the given compound type.
   *
   * @param type		the type to add
   */
  public void addCompoundType(Field type){
    m_compoundtypes.put(type.getName(), type);
  }

  /**
   * Adds a compound parameter.
   *
   * @param compound 	the compound  (= prefix)
   * @param key		the key (= suffix)
   * @param value	the value
   */
  public void addCompoundParameter(String compound, String key, String value){
    Field f=m_compoundtypes.get(key);
    if (f== null){ // assume string
      m_Params.put(new Field(compound+COMPOUND_SEPARATOR+key,DataType.UNKNOWN), value);
      //m_SystemErr.println("No Type found for:"+key);
    }else {
      Object o=f.valueOf(value);
      if (o == null){
	getLogger().info("Null object from:"+value.toString());
	return;
      }
      m_Params.put(new Field(compound+COMPOUND_SEPARATOR+key,DataType.UNKNOWN), o);
    }
  }

  /**
   * Adds a compound parameter.
   *
   * @param compound 	the compound  (= prefix)
   * @param key		the key (= suffix)
   * @param value	the value
   */
  public void addCompoundParameter(String compound, String key, DataType type, Object value){
    Field f=m_compoundtypes.get(key);
    if (f== null){ // assume string
      m_Params.put(new Field(compound+COMPOUND_SEPARATOR+key,type), value.toString());
      //m_SystemErr.println("No Type found for:"+key);
    }else { //TODO check correct type
      m_Params.put(new Field(compound+COMPOUND_SEPARATOR+key,type), value);
    }
  }

  /**
   * Adds a compound parameter.
   *
   * @param compound 	the compound  (= prefix)
   * @param key		the key (= suffix)
   * @param value	the value
   */
  public void addLongCompoundParameter(String compound, String key, Long value){
    m_Params.put(new Field(compound+COMPOUND_SEPARATOR+key,DataType.NUMERIC), new Double(value.doubleValue()));
  }

  /**
   * Adds a compound.
   *
   * @param c 	the compound
   */
  public void addCompound(String c) {
    Field cfield = new Field(QuantitationReport.COMPOUNDS,DataType.STRING);
    String cs = (String) m_Params.get(cfield);
    if (cs == null) {
      m_Params.put(cfield, c);
    }
    else {
      m_Params.put(cfield, (String) m_Params.get(cfield) + "," + c);
    }
  }

  /**
   * Returns a quantitation report that only contains the values compound fields.
   *
   * @return		the report with the subset
   */
  public QuantitationReport getCompoundSubset() {
    return getCompoundSubset(null, null);
  }

  /**
   * Returns a quantitation report that only contains the values of compound
   * fields.
   *
   * @param prefix	the prefix of the fields to retrieve, e..g, only "Toluene-d8"
   * @param suffix	the suffix of the fields to retrieve, e.g., only "Conc"
   * @return		the report with the subset
   */
  public QuantitationReport getCompoundSubset(String prefix, String suffix) {
    QuantitationReport			result;
    Hashtable<AbstractField,Object> 	params;

    result = new QuantitationReport();

    params = new Hashtable<AbstractField,Object>();
    for (AbstractField key: getFields()) {
      if (!key.isCompound())
	continue;
      if ((prefix != null) && (!key.split()[0].equals(prefix)))
	continue;
      if ((suffix != null) && (!key.split()[1].equals(suffix)))
	continue;

      params.put(key, getValue(key));
    }
    result.setParams(params);

    return result;
  }

  /**
   * Updates certain dependant fields. This method should be called before
   * saving it to the database, after loading it from the database or when
   * a quantitation report has been created by hand.
   */
  @Override
  public void update() {
    Field	field;

    super.update();

    // create and store a sortable "Acq On" date
    field = new Field(QuantitationReport.FIELD_ACQON, DataType.STRING);
    if (getValue(field) != null) {
      DateFormat df = new DateFormat(QuantitationReport.ACQON_DATE_FORMAT);
      DateFormat dfSort = DateUtils.getTimestampFormatter();
      try {
	Date d = df.parse(getValue(field).toString());
	addParameter(QuantitationReport.FIELD_ACQON_SORTABLE, dfSort.format(d));
      }
      catch (Exception e) {
	e.printStackTrace();
      }
    }

    // create and store a field saying whether chromatogram is standard or not
    // (default: not a standard)
    field = new Field(QuantitationReport.FIELD_ISSTANDARD, DataType.BOOLEAN);
    if (getValue(field) == null)
      setValue(field, new Boolean(false));

    // create and store a field saying whether chromatogram is calibration
    // standard or not (default: not a calibration standard)
    field = new Field(QuantitationReport.FIELD_ISCALIBRATIONSTANDARD, DataType.BOOLEAN);
    if (getValue(field) == null)
      setValue(field, new Boolean(false));
  }

  /**
   * Generates a basic report for the instrument.
   *
   * @param instrument	the instrument to generate the report for
   * @param acquired	the date the data was acquired
   * @return		the generated report
   */
  public static QuantitationReport createBasic(String instrument, Date acquired) {
    QuantitationReport	result;

    result = new QuantitationReport();
    result.addParameter(QuantitationReport.FIELD_INST, instrument);
    result.addParameter(QuantitationReport.FIELD_ACQON, new DateFormat(QuantitationReport.ACQON_DATE_FORMAT).format(acquired));

    return result;
  }

  /**
   * Parses the string generated by the toString() method.
   *
   * @param s		the string to parse
   * @return		the generated report
   */
  public static QuantitationReport parseReport(String s) {
    QuantitationReport	result;
    Report		report;

    report = Report.parseReport(s);
    result = new QuantitationReport();
    result.assign(report);

    return result;
  }

  /**
   * Parses the properties (generated with the toProperties() method) and
   * generates a report object from it.
   *
   * @param props	the properties to generate the report from
   * @return		the report
   */
  public static QuantitationReport parseProperties(Properties props) {
    QuantitationReport	result;
    Report		report;

    report = Report.parseProperties(props);
    result = new QuantitationReport();
    result.assign(report);

    return result;
  }
}
