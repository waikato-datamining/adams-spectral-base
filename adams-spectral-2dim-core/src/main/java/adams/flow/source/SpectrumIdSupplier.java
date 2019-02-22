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
 * SpectrumIdSupplier.java
 * Copyright (C) 2009-2019 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.source;

import adams.db.AbstractConditions;
import adams.db.Conditions;
import adams.db.DataContainerConditions;
import adams.db.ReportConditions;
import adams.db.SampleDataF;
import adams.db.SpectrumConditionsMulti;
import adams.db.SpectrumF;
import adams.db.SpectrumIDConditions;
import adams.flow.core.ActorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 <!-- globalinfo-start -->
 * Returns spectrum IDs from the database.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 * Input/output:<br>
 * - generates:<br>
 * &nbsp;&nbsp;&nbsp;java.lang.Integer<br>
 * <br><br>
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 * Valid options are: <br><br>
 *
 * <pre>-D &lt;int&gt; (property: debugLevel)
 * &nbsp;&nbsp;&nbsp;The greater the number the more additional info the scheme may output to
 * &nbsp;&nbsp;&nbsp;the console (0 = off).
 * &nbsp;&nbsp;&nbsp;default: 0
 * &nbsp;&nbsp;&nbsp;minimum: 0
 * </pre>
 *
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 * &nbsp;&nbsp;&nbsp;The name of the actor.
 * &nbsp;&nbsp;&nbsp;default: SpectrumIdSupplier
 * </pre>
 *
 * <pre>-annotation &lt;adams.core.base.BaseText&gt; (property: annotations)
 * &nbsp;&nbsp;&nbsp;The annotations to attach to this actor.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-skip (property: skip)
 * &nbsp;&nbsp;&nbsp;If set to true, transformation is skipped and the input token is just forwarded
 * &nbsp;&nbsp;&nbsp;as it is.
 * </pre>
 *
 * <pre>-output-array (property: outputArray)
 * &nbsp;&nbsp;&nbsp;Whether to output the IDs as array or one by one.
 * </pre>
 *
 * <pre>-conditions &lt;adams.db.AbstractConditions [options]&gt; (property: conditions)
 * &nbsp;&nbsp;&nbsp;The conditions for retrieving the data from the database.
 * &nbsp;&nbsp;&nbsp;default: knir.db.SpectrumConditionsMulti
 * </pre>
 *
 * <pre>-sample-ids (property: generateSampleIDs)
 * &nbsp;&nbsp;&nbsp;If set to true, then sample IDs (= string) will be generated instead of
 * &nbsp;&nbsp;&nbsp;database IDs (= integer).
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumIdSupplier
  extends AbstractConditionalIdSupplier 
  implements SampleIdSource {

  /** for serialization. */
  private static final long serialVersionUID = 3539351518933986670L;

  /** whether to return database IDs or sample IDs. */
  protected boolean m_GenerateSampleIDs;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Returns spectrum IDs from the database.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "sample-ids", "generateSampleIDs",
	    false);
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result = super.getQuickInfo();

    result += " (";
    if (m_GenerateSampleIDs)
      result += "sample IDs";
    else
      result += "DB IDs";
    result += ")";

    return result;
  }

  /**
   * Returns the conditions container to use for retrieving the spectra.
   *
   * @return 		the conditions
   */
  @Override
  protected AbstractConditions getDefaultConditions() {
    return Conditions.getSingleton().getDefault(new SpectrumConditionsMulti());
  }

  /**
   * Returns the accepted classes for condition objects.
   *
   * @return		the accepted classes
   */
  @Override
  protected Class[] getAcceptedConditions() {
    return new Class[]{ReportConditions.class, DataContainerConditions.class};
  }

  /**
   * Returns the based class of the items.
   *
   * @return		the class
   */
  @Override
  protected Class getItemClass() {
    if (m_GenerateSampleIDs)
      return String.class;
    else
      return Integer.class;
  }

  /**
   * Determines the database connection in the flow.
   *
   * @return		the database connection to use
   */
  @Override
  protected adams.db.AbstractDatabaseConnection getDatabaseConnection() {
    return ActorUtils.getDatabaseConnection(
	  this,
	  adams.flow.standalone.DatabaseConnectionProvider.class,
	  adams.db.DatabaseConnection.getSingleton());
  }

  /**
   * Returns the IDs from the database.
   *
   * @param errors	for storing any error messages
   * @return		the IDs
   */
  @Override
  protected ArrayList getIDs(StringBuilder errors) {
    ArrayList		result;
    List<String> 	ids;
    String		column;

    result = new ArrayList();

    if (m_Conditions instanceof ReportConditions) {
      if (m_GenerateSampleIDs)
	column = "sp.SAMPLEID";
      else
	column = "sp.AUTO_ID";

      ids = SampleDataF.getSingleton(m_DatabaseConnection).getIDs(
        new String[]{column}, m_Conditions);
    }
    else {
      if (m_GenerateSampleIDs)
	column = "SAMPLEID";
      else
	column = "AUTO_ID";

      ids = SpectrumF.getSingleton(m_DatabaseConnection).getValues(
        new String[]{column}, (SpectrumIDConditions) m_Conditions);
    }

    for (String id: ids) {
      if (m_GenerateSampleIDs)
	result.add(id);
      else
	result.add(new Integer(id));
    }

    return result;
  }

  /**
   * Sets whether to generate database IDs or sample IDs.
   *
   * @param value	if true then sample IDs are generated
   */
  public void setGenerateSampleIDs(boolean value){
    m_GenerateSampleIDs = value;
    reset();
  }

  /**
   * Returns whether to read from the active or store table.
   *
   * @return		true if the store table is used
   */
  public boolean getGenerateSampleIDs(){
    return m_GenerateSampleIDs;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String generateSampleIDsTipText() {
    return "If set to true, then sample IDs (= string) will be generated instead of database IDs (= integer).";
  }
}
