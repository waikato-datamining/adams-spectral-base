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
 * AbstractJCampSpectrumReader.java
 * Copyright (C) 2011-2013 University of Waikato, Hamilton, New Zealand
 */

package adams.data.io.input;

import adams.core.io.FileUtils;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import jspecview.common.Coordinate;
import jspecview.common.JDXSpectrum;
import jspecview.source.JDXSource;

import java.util.Map;
import java.util.logging.Level;

/**
 * Ancestor for JCamp spectrum formats.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1905 $
 */
public abstract class AbstractJCampSpectrumReader
  extends AbstractSpectrumReader {

  /** for serialization. */
  private static final long serialVersionUID = -5324851106369867595L;

  /** JCamp field: Continuous. */
  public final static String FIELD_CONTINUOUS = "Continuous";

  /** JCamp field: DataClass. */
  public final static String FIELD_DATACLASS = "DataClass";

  /** JCamp field: DataType. */
  public final static String FIELD_DATATYPE = "DataType";

  /** JCamp field: HZtoPPM. */
  public final static String FIELD_HZTOPPM = "HZtoPPM";

  /** JCamp field: Increasing. */
  public final static String FIELD_INCREASING = "Increasing";

  /** JCamp field: Jcampdx. */
  public final static String FIELD_JCAMPDX = "Jcampdx";

  /** JCamp field: ObservedFreq. */
  public final static String FIELD_OBSERVEDFREQ = "ObservedFreq";

  /** JCamp field: Origin. */
  public final static String FIELD_ORIGIN = "Origin";

  /** JCamp field: Owner. */
  public final static String FIELD_OWNER = "Owner";

  /** JCamp field: Title. */
  public final static String FIELD_TITLE = "Title";

  /** JCamp field: XFactor. */
  public final static String FIELD_XFACTOR = "XFactor";

  /** JCamp field: XUnits. */
  public final static String FIELD_XUNITS = "XUnits";

  /** JCamp field: YFactor. */
  public final static String FIELD_YFACTOR = "YFactor";

  /** JCamp field: YUnits. */
  public final static String FIELD_YUNITS = "YUnits";

  /** JCamp field: Pathlength. */
  public final static String FIELD_PATHLENGTH = "Pathlength";

  /** JCamp field: Transmittance. */
  public final static String FIELD_TRANSMITTANCE = "Transmittance";

  /** JCamp field: Absorbance. */
  public final static String FIELD_ABSORBANCE = "Absorbance";

  /** JCamp field: Date. */
  public final static String FIELD_DATE = "Date";

  /** JCamp field: Time. */
  public final static String FIELD_TIME = "Time";

  /** whether to use the filename as ID. */
  protected boolean m_UseFilenameAsID;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "use-filename-as-id", "useFilenameAsID",
      false);
  }

  /**
   * Sets whether to use the filename as ID.
   *
   * @param value 	true if to use filename
   */
  public void setUseFilenameAsID(boolean value) {
    m_UseFilenameAsID = value;
    reset();
  }

  /**
   * Returns whether to use the filename as ID.
   *
   * @return 		true if to use filename
   */
  public boolean getUseFilenameAsID() {
    return m_UseFilenameAsID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String useFilenameAsIDTipText() {
    return
      "If enabled, the filename gets used as ID.";
  }

  /**
   * Initializes the sample data.
   *
   * @param header	the header table to use
   * @param report	the sample data to initialize
   */
  protected void initSampleData(Map<String,String> header, SampleData report) {
    Field	field;

    // header
    for (String key: header.keySet()) {
      field = new Field(key, DataType.STRING);
      report.addField(field);
      report.setValue(field, header.get(key));
    }

    // other fields
    report.addField(new Field(FIELD_CONTINUOUS, DataType.BOOLEAN));
    report.addField(new Field(FIELD_DATACLASS, DataType.STRING));
    report.addField(new Field(FIELD_DATATYPE, DataType.STRING));
    report.addField(new Field(FIELD_HZTOPPM, DataType.BOOLEAN));
    report.addField(new Field(FIELD_INCREASING, DataType.BOOLEAN));
    report.addField(new Field(FIELD_JCAMPDX, DataType.STRING));
    report.addField(new Field(FIELD_OBSERVEDFREQ, DataType.NUMERIC));
    report.addField(new Field(FIELD_ORIGIN, DataType.STRING));
    report.addField(new Field(FIELD_OWNER, DataType.STRING));
    report.addField(new Field(FIELD_TITLE, DataType.STRING));
    report.addField(new Field(FIELD_XFACTOR, DataType.NUMERIC));
    report.addField(new Field(FIELD_XUNITS, DataType.STRING));
    report.addField(new Field(FIELD_YFACTOR, DataType.NUMERIC));
    report.addField(new Field(FIELD_YUNITS, DataType.STRING));
    report.addField(new Field(FIELD_PATHLENGTH, DataType.STRING));
    report.addField(new Field(FIELD_TRANSMITTANCE, DataType.BOOLEAN));
    report.addField(new Field(FIELD_ABSORBANCE, DataType.BOOLEAN));
    report.addField(new Field(FIELD_DATE, DataType.STRING));
    report.addField(new Field(FIELD_TIME, DataType.STRING));
  }

  /**
   * Performs the actual reading.
   */
  @Override
  protected void readData() {
    Spectrum		sp;
    JDXSource		source;
    JDXSpectrum		spectrum;
    SampleData		sd;
    int			i;
    Coordinate[]	coords;
    int			n;
    SpectrumPoint point;

    try {
      source = JDXSource.createJDXSource(null, m_Input.getAbsolutePath(), null);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Error reading spectrum from '" + m_Input.getAbsolutePath() + "'!", e);
      return;
    }

    for (i = 0; i < source.getNumberOfSpectra(); i++) {
      spectrum = source.getJDXSpectrum(i);
      sp       = new Spectrum();
      m_ReadData.add(sp);
      sd       = new SampleData();
      sp.setReport(sd);
      if (m_UseFilenameAsID)
	sp.setID(FileUtils.replaceExtension(m_Input.getName(), ""));
      else if ((spectrum.getDate().length() > 0) && (spectrum.getTime().length() > 0))
	sp.setID(spectrum.getDate() + " " + spectrum.getTime());
      else
	sp.setID(spectrum.getOrigin() + " - " + spectrum.getTitle());

      // report
      initSampleData(source.getHeaderTable(), sd);
      sd.setValue(new Field(FIELD_ABSORBANCE, DataType.BOOLEAN), spectrum.isAbsorbance());
      sd.setValue(new Field(FIELD_CONTINUOUS, DataType.BOOLEAN), spectrum.isContinuous());
      sd.setValue(new Field(FIELD_HZTOPPM, DataType.BOOLEAN), spectrum.isHZtoPPM());
      sd.setValue(new Field(FIELD_INCREASING, DataType.BOOLEAN), spectrum.isIncreasing());
      sd.setValue(new Field(FIELD_TRANSMITTANCE, DataType.BOOLEAN), spectrum.isTransmittance());
      sd.setValue(new Field(FIELD_DATACLASS, DataType.STRING), spectrum.getDataClass());
      sd.setValue(new Field(FIELD_DATATYPE, DataType.STRING), spectrum.getDataType());
      sd.setValue(new Field(FIELD_DATE, DataType.STRING), spectrum.getDate());
      sd.setValue(new Field(FIELD_JCAMPDX, DataType.STRING), spectrum.getJcampdx());
      sd.setValue(new Field(FIELD_ORIGIN, DataType.STRING), spectrum.getOrigin());
      sd.setValue(new Field(FIELD_OWNER, DataType.STRING), spectrum.getOwner());
      sd.setValue(new Field(FIELD_PATHLENGTH, DataType.STRING), spectrum.getPathlength());
      sd.setValue(new Field(FIELD_TIME, DataType.STRING), spectrum.getTime());
      sd.setValue(new Field(FIELD_TITLE, DataType.STRING), spectrum.getTitle());
      sd.setValue(new Field(FIELD_XUNITS, DataType.STRING), spectrum.getXUnits());
      sd.setValue(new Field(FIELD_YUNITS, DataType.STRING), spectrum.getYUnits());
      sd.setValue(new Field(FIELD_OBSERVEDFREQ, DataType.NUMERIC), spectrum.getObservedFreq());
      sd.setValue(new Field(FIELD_XFACTOR, DataType.NUMERIC), spectrum.getXFactor());
      sd.setValue(new Field(FIELD_YFACTOR, DataType.NUMERIC), spectrum.getYFactor());

      // spectrum
      coords = spectrum.getXYCoords();
      for (n = 0; n < coords.length; n++) {
	point = new SpectrumPoint((float) coords[n].getXVal(), (float) coords[n].getYVal());
	sp.add(point);
      }
    }
  }
}
