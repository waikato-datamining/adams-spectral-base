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
 * IQRCleaner.java
 * Copyright (C) 2011-2018 University of Waikato, Hamilton, New Zealand
 */

package adams.data.cleaner.instance;

import adams.core.Range;
import adams.core.option.OptionUtils;
import adams.data.cleaner.CleanerDetails;
import adams.data.spreadsheet.DefaultSpreadSheet;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.unsupervised.attribute.InterquartileRange;
import weka.filters.unsupervised.attribute.InterquartileRange.ValueType;
import weka.filters.unsupervised.instance.RemoveInstancesWithMissingValue;

import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Removes instances outside the given IQR multiplier.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-pre-filter &lt;weka.filters.Filter&gt; (property: preFilter)
 * &nbsp;&nbsp;&nbsp;The filter to use for pre-filtering the data.
 * &nbsp;&nbsp;&nbsp;default: weka.filters.AllFilter
 * </pre>
 * 
 * <pre>-serialization-file &lt;adams.core.io.PlaceholderFile&gt; (property: serializationFile)
 * &nbsp;&nbsp;&nbsp;The file to serialize the generated internal model to.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 * 
 * <pre>-override-serialized-file &lt;boolean&gt; (property: overrideSerializedFile)
 * &nbsp;&nbsp;&nbsp;If set to true, then any serialized file will be ignored and the setup for 
 * &nbsp;&nbsp;&nbsp;serialization will be regenerated.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-filter &lt;weka.filters.Filter&gt; (property: filter)
 * &nbsp;&nbsp;&nbsp;The IQR filter to use; parameters get set internally.
 * &nbsp;&nbsp;&nbsp;default: weka.filters.unsupervised.attribute.InterquartileRange -R first-last -O 3.0 -E 6.0
 * </pre>
 * 
 * <pre>-iqr &lt;double&gt; (property: iqr)
 * &nbsp;&nbsp;&nbsp;IQR multipler for min&#47;max values.
 * &nbsp;&nbsp;&nbsp;default: 4.25
 * &nbsp;&nbsp;&nbsp;minimum: 0.0
 * </pre>
 * 
 * <pre>-attribute-range &lt;adams.core.Range&gt; (property: attributeRange)
 * &nbsp;&nbsp;&nbsp;The attribute range to work on.
 * &nbsp;&nbsp;&nbsp;default: first-last
 * &nbsp;&nbsp;&nbsp;example: A range is a comma-separated list of single 1-based indices or sub-ranges of indices ('start-end'); 'inv(...)' inverts the range '...'; the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 * 
 * <pre>-remove-with-missing &lt;boolean&gt; (property: removeWithMissing)
 * &nbsp;&nbsp;&nbsp;If enabled, instances with missing values get removed.
 * &nbsp;&nbsp;&nbsp;default: true
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  dale (dale at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class IQRCleaner
  extends AbstractSerializableCleaner
  implements CleanerDetails<SpreadSheet> {

  /** for serialization. */
  private static final long serialVersionUID = 738490230290004179L;

  /** the IQR filter. */
  protected InterquartileRange m_Filter;

  /** the actual IQR filter. */
  protected Filter m_ActualFilter;

  /** the maximum value of the attribute. */
  protected double m_IQR;

  /** the attribute range to work on. */
  protected Range m_Range;
  
  /** whether to remove instances with missing values. */
  protected boolean m_RemoveWithMissing;
  
  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
	"Removes instances outside the given IQR multiplier.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	"filter", "filter",
	new InterquartileRange());

    m_OptionManager.add(
	"iqr", "iqr",
	4.25, 0.0, null);

    m_OptionManager.add(
	"attribute-range", "attributeRange",
	new Range(Range.ALL));

    m_OptionManager.add(
	"remove-with-missing", "removeWithMissing",
	true);
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();
    
    m_Range = new Range();
  }

  /**
   * Sets the IQR filter.
   *
   * @param value 	the filter
   */
  public void setFilter(Filter value) {
    if (value instanceof InterquartileRange) {
      m_Filter = (InterquartileRange) value;
      reset();
    }
    else {
      getLogger().severe(
	  "Only " + InterquartileRange.class.getName() 
	  + " and derived classes are allowed, provided: " + Utils.toCommandLine(value));
    }
  }

  /**
   * Returns the IQR filter.
   *
   * @return 		the filter
   */
  public Filter getFilter() {
    return m_Filter;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String filterTipText() {
    return "The IQR filter to use; parameters get set internally.";
  }

  /**
   * Sets the IQR multiplier.
   *
   * @param value 	iqr
   */
  public void setIqr(double value) {
    m_IQR = value;
    reset();
  }

  /**
   * Returns the iqr multiplier.
   *
   * @return 		the iqr
   */
  public double getIqr() {
    return m_IQR;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String iqrTipText() {
    return "IQR multipler for min/max values.";
  }

  /**
   * Sets the attribute range to work on.
   *
   * @param value 	the range
   */
  public void setAttributeRange(Range value) {
    m_Range = value;
    reset();
  }

  /**
   * Returns the attribute range to work on.
   *
   * @return 		the range
   */
  public Range getAttributeRange() {
    return m_Range;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String attributeRangeTipText() {
    return "The attribute range to work on.";
  }

  /**
   * Sets whether to remove instances with missing values.
   *
   * @param value 	true if to remove
   */
  public void setRemoveWithMissing(boolean value) {
    m_RemoveWithMissing = value;
    reset();
  }

  /**
   * Returns whether to remove instances with missing values.
   *
   * @return 		true if to remove
   */
  public boolean getRemoveWithMissing() {
    return m_RemoveWithMissing;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String removeWithMissingTipText() {
    return "If enabled, instances with missing values get removed.";
  }

  /**
   * Regenerates all the objects that are necessary for serialization.
   */
  @Override
  public void initSerializationSetup() {
    RemoveInstancesWithMissingValue	remove;
    InterquartileRange			iqr;

    remove = new RemoveInstancesWithMissingValue();
    remove.setIgnoreClass(true);

    iqr = (InterquartileRange) adams.core.Utils.deepCopy(m_Filter);
    iqr.setOutlierFactor(m_IQR);
    iqr.setExtremeValuesFactor(m_IQR + 1.0);
    iqr.setExtremeValuesAsOutliers(true);
    iqr.setAttributeIndices(m_Range.getRange());

    if (m_RemoveWithMissing) {
      m_ActualFilter = new MultiFilter();
      ((MultiFilter) m_ActualFilter).setFilters(new Filter[]{remove, iqr});
    }
    else {
      m_ActualFilter = iqr;
    }

    try {
      Instances filtered = preFilter(m_InitData);
      m_ActualFilter.setInputFormat(filtered);
      Filter.useFilter(filtered, m_ActualFilter);
    }
    catch (Exception e) {
      throw new IllegalStateException("Failed to build: " + OptionUtils.getCommandLine(m_ActualFilter), e);
    }
  }

  /**
   * Returns the member variables to serialize to a file.
   *
   * @return		the objects to serialize
   */
  @Override
  public Object[] retrieveSerializationSetup() {
    return new Object[]{
	m_PreFilter,
	m_ActualFilter
    };
  }

  /**
   * Updates the member variables with the provided objects obtained from
   * deserialization.
   *
   * @param value	the deserialized objects
   */
  @Override
  public void setSerializationSetup(Object[] value) {
    m_PreFilter    = (Filter) value[0];
    m_ActualFilter = (Filter) value[1];
  }

  /**
   * Performs the actual check.
   *
   * @param data	the Instance to check
   * @return		null if no outlier/extreme value detected
   */
  @Override
  protected String performCheck(Instance data) {
    String	result;
    Instance	filtered;
    double	value;
    String	msg;
    
    result = null;
    
    try {
      m_ActualFilter.input(data);
      m_ActualFilter.batchFinished();
      filtered = m_ActualFilter.output();
      value    = filtered.value(filtered.dataset().attribute("Outlier"));
      if (value != 0.0)
	result = "Outlier detected";
    }
    catch (Exception e) {
      msg    = "Failed to filter instance: ";
      result = msg + e;
      getLogger().log(Level.SEVERE, msg, e);
    }
    
    return result;
  }

  /**
   * Clean Instances
   *
   * @param instances	Instances
   */
  @Override
  protected Instances performClean(Instances instances) {
    Double	value;
    Instances 	result;
    Instances 	filtered;
    int 	i;
    Instance 	data;

    result = new Instances(instances,0);

    try {
      filtered = Filter.useFilter(instances, m_ActualFilter);
      for (i = 0; i < filtered.numInstances(); i++){
	data  = filtered.get(i);
	value = data.value(instances.numAttributes());
	if (value == 0.0)
	  result.add((Instance) instances.get(i).copy());
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to filter instances and remove outliers:", e);
      result = null; // TODO or return original instances?
    }

    return result;
  }
  
  /**
   * Returns details on the IQR filter.
   * 
   * @return		the details as spreadsheet, null if no filter available yet
   */
  @Override
  public SpreadSheet getDetails() {
    SpreadSheet		result;
    Row			row;
    int			i;	
    InterquartileRange	filter;
    
    if (m_ActualFilter == null)
      return null;
    if (m_ActualFilter instanceof InterquartileRange)
      filter = (InterquartileRange) m_ActualFilter;
    else if (m_ActualFilter instanceof MultiFilter)
      filter = (InterquartileRange) ((MultiFilter) m_ActualFilter).getFilters()[1];
    else
      return null;
    if (filter.getValues(ValueType.IQR) == null)
      return null;

    // generate output
    result = new DefaultSpreadSheet();
    
    // header
    row = result.getHeaderRow();
    row.addCell("0").setContent("Attribute Index");
    for (ValueType type: ValueType.values())
      row.addCell(type.toString()).setContent(type.toString());
    
    // data
    for (i = 0; i < filter.getValues(ValueType.IQR).length; i++) {
      row = result.addRow("" + result.getRowCount());
      row.addCell("0").setContent("" + (i+1));
      for (ValueType type: ValueType.values())
	row.addCell(type.toString()).setContent(filter.getValues(type)[i]);
    }
    
    return result;
  }
}
