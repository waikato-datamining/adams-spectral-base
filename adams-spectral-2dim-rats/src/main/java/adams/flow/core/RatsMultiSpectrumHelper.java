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
 * RatsMultiSpectrumHelper.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */
package adams.flow.core;

import adams.data.report.AbstractField;
import adams.data.report.Field;
import adams.data.spectrum.SpectrumPoint;

/**
 * Helper class for converting spectra.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2085 $
 */
public class RatsMultiSpectrumHelper {

  /**
   * Converts a KNIR spectrum into a Webservice one.
   * 
   * @param input	the KNIR spectrum
   * @return		the Webservice spectrum
   */
  public static nz.ac.waikato.adams.webservice.rats.multispectrum.MultiSpectrum knirToWebservice(adams.data.spectrum.MultiSpectrum input) {
    nz.ac.waikato.adams.webservice.rats.multispectrum.MultiSpectrum	result;
    nz.ac.waikato.adams.webservice.rats.multispectrum.Spectrum		spec;
    nz.ac.waikato.adams.webservice.rats.multispectrum.Waves		waves;
    nz.ac.waikato.adams.webservice.rats.multispectrum.Wave		wave;
    nz.ac.waikato.adams.webservice.rats.multispectrum.Properties	props;
    nz.ac.waikato.adams.webservice.rats.multispectrum.Property		prop;
    adams.data.sampledata.SampleData					report;

    result = new nz.ac.waikato.adams.webservice.rats.multispectrum.MultiSpectrum();
    result.setId(input.getID());

    // report
    props = new nz.ac.waikato.adams.webservice.rats.multispectrum.Properties();
    if (input.hasReport()) {
      report = input.getReport();
      for (AbstractField field: report.getFields()) {
	prop = new nz.ac.waikato.adams.webservice.rats.multispectrum.Property();
	prop.setKey(field.getName());
	prop.setType(nz.ac.waikato.adams.webservice.rats.multispectrum.DataType.valueOf(field.getDataType().toRaw()));
	prop.setValue("" + report.getValue(field));
	props.getProp().add(prop);
      }
    }

    result.setProps(props);

    // spectra
    for (adams.data.spectrum.Spectrum sp: input) {
      spec = new nz.ac.waikato.adams.webservice.rats.multispectrum.Spectrum();
      spec.setId(sp.getID());
      spec.setFormat(sp.getFormat());

      // spectral data
      waves = new nz.ac.waikato.adams.webservice.rats.multispectrum.Waves();
      for (SpectrumPoint point: sp) {
	wave = new nz.ac.waikato.adams.webservice.rats.multispectrum.Wave();
	wave.setNumber(point.getWaveNumber());
	wave.setAmplitude(point.getAmplitude());
	waves.getWave().add(wave);
      }
      spec.setWaves(waves);

      // report
      props = new nz.ac.waikato.adams.webservice.rats.multispectrum.Properties();
      if (sp.hasReport()) {
	report = sp.getReport();
	for (AbstractField field: report.getFields()) {
	  prop = new nz.ac.waikato.adams.webservice.rats.multispectrum.Property();
	  prop.setKey(field.getName());
	  prop.setType(nz.ac.waikato.adams.webservice.rats.multispectrum.DataType.valueOf(field.getDataType().toRaw()));
	  prop.setValue("" + report.getValue(field));
	  props.getProp().add(prop);
	}
      }

      spec.setProps(props);
      
      result.getSpectra().add(spec);
    }

    return result;
  }

  /**
   * Converts a Webservice spectrum into a KNIR one.
   * 
   * @param input	the KNIR spectrum
   * @return		the Webservice spectrum
   */
  public static adams.data.spectrum.MultiSpectrum webserviceToKnir(nz.ac.waikato.adams.webservice.rats.multispectrum.MultiSpectrum input) {
    adams.data.spectrum.MultiSpectrum	result;
    adams.data.spectrum.Spectrum	spec;
    adams.data.spectrum.SpectrumPoint	point;
    adams.data.sampledata.SampleData	report;
    Field 				field;

    result = new adams.data.spectrum.MultiSpectrum();
    result.setID(input.getId());

    // report
    report = new adams.data.sampledata.SampleData();
    if (input.getProps() != null) {
      for (nz.ac.waikato.adams.webservice.rats.multispectrum.Property prop: input.getProps().getProp()) {
	field = new Field(prop.getKey(), adams.data.report.DataType.valueOf(prop.getType().toString()));
	report.addField(field);
	report.setValue(
	    field, 
	    prop.getValue());
      }
    }

    result.setReport(report);

    // spectra
    for (nz.ac.waikato.adams.webservice.rats.multispectrum.Spectrum sp: input.getSpectra()) {
      spec = new adams.data.spectrum.Spectrum();
      
      // spectral data
      if (sp.getWaves() != null) {
	for (nz.ac.waikato.adams.webservice.rats.multispectrum.Wave wave: sp.getWaves().getWave()) {
	  point = new SpectrumPoint(wave.getNumber(), wave.getAmplitude());
	  spec.add(point);
	}
      }

      // report
      report = new adams.data.sampledata.SampleData();
      if (sp.getProps() != null) {
	for (nz.ac.waikato.adams.webservice.rats.multispectrum.Property prop: sp.getProps().getProp()) {
	  field = new Field(prop.getKey(), adams.data.report.DataType.valueOf(prop.getType().toString()));
	  report.addField(field);
	  report.setValue(
	      field, 
	      prop.getValue());
	}
      }

      spec.setReport(report);

      spec.setID(sp.getId());
      spec.setFormat(sp.getFormat());
      
      result.add(spec);
    }

    return result;
  }
}
