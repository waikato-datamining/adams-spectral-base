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
 * WekaFilterToReport.java
 * Copyright (C) 2017 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.core.MessageCollection;
import adams.core.base.BaseRegExp;
import adams.core.io.PlaceholderFile;
import adams.core.logging.LoggingLevel;
import adams.data.filter.AbstractFilter;
import adams.data.instances.AbstractInstanceGenerator;
import adams.data.instances.SimpleInstanceGenerator;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.report.Report;
import adams.data.spectrum.Spectrum;
import adams.flow.control.StorageName;
import adams.flow.core.AbstractModelLoader.ModelLoadingType;
import adams.flow.core.Actor;
import adams.flow.core.CallableActorReference;
import adams.flow.core.FlowContextHandler;
import adams.flow.core.ModelLoaderSupporter;
import adams.flow.core.WekaFilterModelLoader;
import weka.core.Attribute;
import weka.core.Instance;

import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Generates a Weka Instance using the specified instance generator. The Instance gets pushed through the prebuilt Weka filter that was loaded. The values of the matching attributes from the filtered data get transferred to the spectrum's report.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 *
 * <pre>-no-id-update &lt;boolean&gt; (property: dontUpdateID)
 * &nbsp;&nbsp;&nbsp;If enabled, suppresses updating the ID of adams.data.id.IDHandler data containers.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 *
 * <pre>-generator &lt;adams.data.instances.AbstractInstanceGenerator&gt; (property: generator)
 * &nbsp;&nbsp;&nbsp;The generator to use for turning spectra into weka.core.Instance objects.
 * &nbsp;&nbsp;&nbsp;default: adams.data.instances.SimpleInstanceGenerator
 * </pre>
 *
 * <pre>-model-loading-type &lt;AUTO|FILE|SOURCE_ACTOR|STORAGE&gt; (property: modelLoadingType)
 * &nbsp;&nbsp;&nbsp;Determines how to load the model, in case of AUTO, first the model file
 * &nbsp;&nbsp;&nbsp;is checked, then the callable actor and then the storage.
 * &nbsp;&nbsp;&nbsp;default: AUTO
 * </pre>
 *
 * <pre>-model &lt;adams.core.io.PlaceholderFile&gt; (property: modelFile)
 * &nbsp;&nbsp;&nbsp;The file to load the model from, ignored if pointing to a directory.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 *
 * <pre>-model-actor &lt;adams.flow.core.CallableActorReference&gt; (property: modelActor)
 * &nbsp;&nbsp;&nbsp;The callable actor (source) to obtain the model from, ignored if not present.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-model-storage &lt;adams.flow.control.StorageName&gt; (property: modelStorage)
 * &nbsp;&nbsp;&nbsp;The storage item to obtain the model from, ignored if not present.
 * &nbsp;&nbsp;&nbsp;default: storage
 * </pre>
 *
 * <pre>-prefix &lt;java.lang.String&gt; (property: prefix)
 * &nbsp;&nbsp;&nbsp;The prefix to use for the report fields.
 * &nbsp;&nbsp;&nbsp;default:
 * </pre>
 *
 * <pre>-regexp &lt;adams.core.base.BaseRegExp&gt; (property: regExp)
 * &nbsp;&nbsp;&nbsp;The regular expression that the Instance attribute names must match in order
 * &nbsp;&nbsp;&nbsp;to get transferred into the report.
 * &nbsp;&nbsp;&nbsp;default: .*
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class WekaFilterToReport
  extends AbstractFilter<Spectrum>
  implements FlowContextHandler, ModelLoaderSupporter {

  /** for serialization. */
  private static final long serialVersionUID = 2319957467336388607L;

  /** the instance generator to use. */
  protected AbstractInstanceGenerator m_Generator;

  /** the loader for the Weka filter. */
  protected WekaFilterModelLoader m_ModelLoader;

  /** the Weka filter. */
  protected weka.filters.Filter m_Filter;

  /** the prefix for the report. */
  protected String m_Prefix;

  /** the regular expression for the attribute names to transfer. */
  protected BaseRegExp m_RegExp;

  /** the flow context. */
  protected Actor m_FlowContext;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return
      "Generates a Weka Instance using the specified instance generator. The "
	+ "Instance gets pushed through the prebuilt Weka filter that was loaded. "
	+ "The values of the matching attributes from the filtered data get "
	+ "transferred to the spectrum's report.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "generator", "generator",
      new SimpleInstanceGenerator());

    m_OptionManager.add(
      "model-loading-type", "modelLoadingType",
      ModelLoadingType.AUTO);

    m_OptionManager.add(
      "model", "modelFile",
      new PlaceholderFile("."));

    m_OptionManager.add(
      "model-actor", "modelActor",
      new CallableActorReference());

    m_OptionManager.add(
      "model-storage", "modelStorage",
      new StorageName());

    m_OptionManager.add(
      "prefix", "prefix",
      "");

    m_OptionManager.add(
      "regexp", "regExp",
      new BaseRegExp(BaseRegExp.MATCH_ALL));
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_ModelLoader = new WekaFilterModelLoader();
    m_FlowContext = null;
  }

  /**
   * Resets the scheme.
   */
  @Override
  public void reset() {
    super.reset();

    m_ModelLoader.reset();
    m_Filter = null;
  }

  /**
   * Sets the flow context.
   *
   * @param value	the context
   */
  @Override
  public void setFlowContext(Actor value) {
    m_FlowContext = value;
  }

  /**
   * Returns the flow context.
   *
   * @return		the context
   */
  @Override
  public Actor getFlowContext() {
    return m_FlowContext;
  }

  /**
   * Sets the logging level.
   *
   * @param value 	the level
   */
  @Override
  public synchronized void setLoggingLevel(LoggingLevel value) {
    super.setLoggingLevel(value);
    m_ModelLoader.setLoggingLevel(value);
  }

  /**
   * Sets the generator to use.
   *
   * @param value	the generator
   */
  public void setGenerator(AbstractInstanceGenerator value){
    m_Generator = value;
    reset();
  }

  /**
   * Returns the generator in use.
   *
   * @return		the generator
   */
  public AbstractInstanceGenerator getGenerator(){
    return m_Generator;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String generatorTipText() {
    return "The generator to use for turning spectra into weka.core.Instance objects.";
  }

  /**
   * Sets the loading type. In case of {@link ModelLoadingType#AUTO}, first
   * file, then callable actor, then storage.
   *
   * @param value	the type
   */
  public void setModelLoadingType(ModelLoadingType value) {
    m_ModelLoader.setModelLoadingType(value);
    reset();
  }

  /**
   * Returns the loading type. In case of {@link ModelLoadingType#AUTO}, first
   * file, then callable actor, then storage.
   *
   * @return		the type
   */
  public ModelLoadingType getModelLoadingType() {
    return m_ModelLoader.getModelLoadingType();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String modelLoadingTypeTipText() {
    return m_ModelLoader.modelLoadingTypeTipText();
  }

  /**
   * Sets the file to load the model from.
   *
   * @param value	the model file
   */
  public void setModelFile(PlaceholderFile value) {
    m_ModelLoader.setModelFile(value);
    reset();
  }

  /**
   * Returns the file to load the model from.
   *
   * @return		the model file
   */
  public PlaceholderFile getModelFile() {
    return m_ModelLoader.getModelFile();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String modelFileTipText() {
    return m_ModelLoader.modelFileTipText();
  }

  /**
   * Sets the filter source actor.
   *
   * @param value	the source
   */
  public void setModelActor(CallableActorReference value) {
    m_ModelLoader.setModelActor(value);
    reset();
  }

  /**
   * Returns the filter source actor.
   *
   * @return		the source
   */
  public CallableActorReference getModelActor() {
    return m_ModelLoader.getModelActor();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String modelActorTipText() {
    return m_ModelLoader.modelActorTipText();
  }

  /**
   * Sets the filter storage item.
   *
   * @param value	the storage item
   */
  public void setModelStorage(StorageName value) {
    m_ModelLoader.setModelStorage(value);
    reset();
  }

  /**
   * Returns the filter storage item.
   *
   * @return		the storage item
   */
  public StorageName getModelStorage() {
    return m_ModelLoader.getModelStorage();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String modelStorageTipText() {
    return m_ModelLoader.modelStorageTipText();
  }

  /**
   * Sets the prefix for the report fields.
   *
   * @param value	the prefix
   */
  public void setPrefix(String value) {
    m_Prefix = value;
    reset();
  }

  /**
   * Returns the prefix for the report fields.
   *
   * @return		the prefix
   */
  public String getPrefix() {
    return m_Prefix;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String prefixTipText() {
    return "The prefix to use for the report fields.";
  }

  /**
   * Sets the regular expression that the Instance attribute names must match
   * in order to get transferred into the report.
   *
   * @param value	the expression
   */
  public void setRegExp(BaseRegExp value) {
    m_RegExp = value;
    reset();
  }

  /**
   * Returns the regular expression that the Instance attribute names must
   * match in order to get transferred into the report.
   *
   * @return		the expression
   */
  public BaseRegExp getRegExp() {
    return m_RegExp;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String regExpTipText() {
    return "The regular expression that the Instance attribute names must match in order to get transferred into the report.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  protected Spectrum processData(Spectrum data) {
    Spectrum			result;
    MessageCollection		errors;
    Instance 			inst;
    Instance			filtered;
    Report			report;
    Field			field;
    int				i;
    Attribute			att;
    Object			val;

    result = (Spectrum) data.getClone();

    // load filter if necessary
    if (m_Filter == null) {
      m_ModelLoader.setFlowContext(getFlowContext());
      errors   = new MessageCollection();
      m_Filter = m_ModelLoader.getModel(errors);
      if (!errors.isEmpty()) {
	getLogger().severe(errors.toString());
	return result;
      }
    }

    try {
      // generate instance
      inst = m_Generator.generate(data);

      // filter instance
      m_Filter.input(inst);
      m_Filter.batchFinished();
      filtered = m_Filter.output();

      // transfer data
      report = result.getReport();
      for (i = 0; i < filtered.numAttributes(); i++) {
	att = filtered.attribute(i);
	if (m_RegExp.isMatch(att.name())) {
	  field = null;
	  val   = null;

	  switch (att.type()) {
	    case Attribute.NUMERIC:
	      field = new Field(m_Prefix + att.name(), DataType.NUMERIC);
	      val   = filtered.value(i);
	      break;

	    case Attribute.NOMINAL:
	    case Attribute.STRING:
	    case Attribute.DATE:
	      field = new Field(m_Prefix + att.name(), DataType.STRING);
	      val   = filtered.stringValue(i);
	      break;
	  }

	  if (field != null) {
	    report.addField(field);
	    report.setValue(field, val);
	  }
	  else {
	    getLogger().warning("Failed to transfer attribute: " + att.name() + "/" + Attribute.typeToString(att.type()));
	  }
	}
      }
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to generate/filter spectrum!", e);
    }

    return result;
  }
}
