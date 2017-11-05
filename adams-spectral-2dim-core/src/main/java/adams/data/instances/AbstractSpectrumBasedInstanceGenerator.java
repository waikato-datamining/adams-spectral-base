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
 * AbstractSpectrumBasedInstanceGenerator.java
 * Copyright (C) 2009-2017 University of Waikato, Hamilton, New Zealand
 */

package adams.data.instances;

import adams.core.base.BaseString;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.sampledata.SampleData;
import adams.data.spectrum.Spectrum;
import adams.db.AbstractDatabaseConnection;
import adams.db.DatabaseConnection;
import adams.db.SpectrumT;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.SelectedTag;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;

import java.util.logging.Level;

/**
 * Abstract base class for schemes that turn spectra/sample data into
 * weka.core.Instance objects.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public abstract class AbstractSpectrumBasedInstanceGenerator
  extends AbstractInstanceGenerator<Spectrum>
  implements InstanceGeneratorWithSampleID {

  /** for serialization. */
  private static final long serialVersionUID = 2083516575994387184L;

  /** whether to add the sample ID. */
  protected boolean m_AddSampleID;

  /** the notes to add as attributes. */
  protected BaseString[] m_Notes;

  /** whether to load the sample data if only dummy. */
  protected boolean m_LoadSampleData;

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "add-sample-id", "addSampleID",
	    false);

    m_OptionManager.add(
	    "notes", "notes",
	    new BaseString[0]);

    m_OptionManager.add(
	    "load-sample-data", "loadSampleData",
	    false);
  }

  /**
   * Returns the default database connection.
   *
   * @return		the default database connection
   */
  @Override
  protected AbstractDatabaseConnection getDefaultDatabaseConnection() {
    return DatabaseConnection.getSingleton();
  }

  /**
   * Sets whether the sample ID is added to the data or not.
   *
   * @param value 	true if sample ID should be added
   */
  public void setAddSampleID(boolean value) {
    m_AddSampleID = value;
    reset();
  }

  /**
   * Returns whether the sample ID is added.
   *
   * @return 		true if sample ID is added
   */
  public boolean getAddSampleID() {
    return m_AddSampleID;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String addSampleIDTipText() {
    return "If set to true, then the sample ID will be added to the output.";
  }

  /**
   * Sets the notes to add as attributes.
   *
   * @param value	the notes prefixes, e.g., "PROCESS INFORMATION"
   */
  public void setNotes(BaseString[] value) {
    m_Notes = value;
    reset();
  }

  /**
   * Returns the current notes to add as attributes.
   *
   * @return		the notes prefixes, e.g., "PROCESS INFORMATION"
   */
  public BaseString[] getNotes() {
    return m_Notes;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the gui
   */
  public String notesTipText() {
    return "The notes to add as attributes to the generated data, eg 'PROCESS INFORMATION'.";
  }

  /**
   * Sets whether to load the sample data via the sample ID if only dummy report available.
   *
   * @param value 	true if the sample data should be retrieved if only dummy report available
   */
  public void setLoadSampleData(boolean value) {
    m_LoadSampleData = value;
    reset();
  }

  /**
   * Returns whether to load the sample data via the sample ID if only dummy report available.
   *
   * @return 		true if the sample data should be retrieved if only dummy report available
   */
  public boolean getLoadSampleData() {
    return m_LoadSampleData;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String loadSampleDataTipText() {
    return "If set to true, then the sample data will be loaded if only dummy report available, using the sample ID.";
  }

  /**
   * Checks the input spectrum.
   * <br><br>
   * The default implementation only checks whether there is any data set
   * and loads missing sample data via the sample ID if requested.
   *
   * @param data	the data to process
   */
  @Override
  protected void checkInput(Spectrum data) {
    SpectrumT	table;
    Spectrum	sp;
    String	sampleID;

    super.checkInput(data);

    if (!m_Offline) {
      if (data.hasReport() && data.getReport().isDummyReport() && m_LoadSampleData) {
        table = SpectrumT.getSingleton(getDatabaseConnection());
	sampleID = data.getReport().getStringValue(new Field(SampleData.SAMPLE_ID, DataType.STRING));
	if (sampleID != null) {
	  sp = table.load(sampleID, SampleData.DEFAULT_FORMAT);
	  data.setReport(sp.getReport());
	}
	else {
	  getLogger().severe("No sample ID available for #" + data.getDatabaseID() + "?");
	}
      }
    }
  }

  /**
   * Adds IDs, notes, additional fields to header.
   *
   * @param data	the input data
   */
  @Override
  protected void postProcessHeader(Spectrum data) {
    int		i;
    Add		add;

    // notes to add to the output?
    if (m_Notes.length > 0) {
      for (i = m_Notes.length - 1; i >= 0; i--) {
	try {
	  add = new Add();
	  add.setAttributeIndex("1");
	  add.setAttributeName(ArffUtils.getNoteName(m_Notes[i].stringValue()));
	  add.setAttributeType(new SelectedTag(Attribute.STRING, Add.TAGS_TYPE));
	  add.setInputFormat(m_OutputHeader);
	  m_OutputHeader = Filter.useFilter(m_OutputHeader, add);
	}
	catch (Exception e) {
	  getLogger().log(Level.SEVERE, 
	      "Error initializing the Add filter for note '" + m_Notes[i] + "'!", e);
	}
      }
    }

    // add the sample ID to the output?
    if (m_AddSampleID) {
      try {
	add = new Add();
	add.setAttributeIndex("1");
	add.setAttributeName(ArffUtils.getSampleIDName());
	add.setAttributeType(new SelectedTag(Attribute.STRING, Add.TAGS_TYPE));
	add.setInputFormat(m_OutputHeader);
	m_OutputHeader = Filter.useFilter(m_OutputHeader, add);
      }
      catch (Exception e) {
	getLogger().log(Level.SEVERE, "Error initializing the Add filter for sample ID!", e);
      }
    }

    super.postProcessHeader(data);
  }

  /**
   * Adds the IDs, notes, additional fields to the data.
   *
   * @param data	the input data
   * @param inst	the generated instance
   * @return		the processed instance
   */
  @Override
  protected Instance postProcessOutput(Spectrum data, Instance inst) {
    Instance	result;
    int		i;
    double[]	values;
    String	valueStr;
    int		index;

    inst   = super.postProcessOutput(data, inst);
    values = inst.toDoubleArray();

    if (m_AddSampleID) {
      index         = m_OutputHeader.attribute(ArffUtils.getSampleIDName()).index();
      values[index] = m_OutputHeader.attribute(index).addStringValue(data.getID().replaceAll("'", ""));
    }

    // notes fields
    for (i = 0; i < m_Notes.length; i++) {
      valueStr = data.getNotes().getPrefixSubset(m_Notes[i].stringValue()).toString();
      index    = m_OutputHeader.attribute(ArffUtils.getNoteName(m_Notes[i].stringValue())).index();
      if (valueStr == null)
	values[index] = weka.core.Utils.missingValue();
      else
	values[index] = m_OutputHeader.attribute(index).addStringValue(valueStr);
    }

    result = new DenseInstance(1.0, values);
    result.setDataset(m_OutputHeader);

    return result;
  }
}
