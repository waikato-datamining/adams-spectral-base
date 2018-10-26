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
 * ThreeWayDataTrain.java
 * Copyright (C) 2018 University of Waikato, Hamilton, NZ
 */

package adams.flow.transformer;

import adams.core.QuickInfoHelper;
import adams.core.Utils;
import adams.core.option.OptionUtils;
import adams.data.container.TensorContainer;
import adams.flow.container.ThreeWayDataModelContainer;
import adams.flow.core.Token;
import adams.flow.transformer.threewaydatatrain.AbstractThreeWayDataTrainPostProcessor;
import adams.flow.transformer.threewaydatatrain.PassThrough;
import nz.ac.waikato.cms.adams.multiway.algorithm.PARAFAC;
import nz.ac.waikato.cms.adams.multiway.algorithm.api.AbstractAlgorithm;
import nz.ac.waikato.cms.adams.multiway.algorithm.api.SupervisedAlgorithm;
import nz.ac.waikato.cms.adams.multiway.algorithm.api.UnsupervisedAlgorithm;
import nz.ac.waikato.cms.adams.multiway.data.tensor.Tensor;

/**
 <!-- globalinfo-start -->
 <!-- globalinfo-end -->
 *
 <!-- flow-summary-start -->
 <!-- flow-summary-end -->
 *
 <!-- options-start -->
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ThreeWayDataTrain
  extends AbstractTransformer {

  private static final long serialVersionUID = -3327525716497085828L;

  /** the algorithm to build. */
  protected AbstractAlgorithm m_Algorithm;

  /** the algorithm instance that is currently being trained. */
  protected AbstractAlgorithm m_CurrentAlgorithm;

  /** the post-processor. */
  protected AbstractThreeWayDataTrainPostProcessor m_PostProcessor;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Builds the selected algorithm and forwards a container with the built model.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "algorithm", "algorithm",
      new PARAFAC());

    m_OptionManager.add(
      "post-processor", "postProcessor",
      new PassThrough());
  }

  /**
   * Sets the algorithm to use.
   *
   * @param value	the algorithm
   */
  public void setAlgorithm(AbstractAlgorithm value) {
    m_Algorithm = value;
    reset();
  }

  /**
   * Returns the algorithm in use.
   *
   * @return		the reader
   */
  public AbstractAlgorithm getAlgorithm() {
    return m_Algorithm;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String algorithmTipText() {
    return "The algorithm to build on the data.";
  }

  /**
   * Sets the post-processor to use on the model.
   *
   * @param value	the post-processor
   */
  public void setPostProcessor(AbstractThreeWayDataTrainPostProcessor value) {
    m_PostProcessor = value;
    reset();
  }

  /**
   * Returns the post-processor to use on the model.
   *
   * @return		the post-processor
   */
  public AbstractThreeWayDataTrainPostProcessor getPostProcessor() {
    return m_PostProcessor;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String postProcessorTipText() {
    return "The post-processor to apply to the model.";
  }

  /**
   * Returns a quick info about the actor, which will be displayed in the GUI.
   *
   * @return		null if no info available, otherwise short string
   */
  @Override
  public String getQuickInfo() {
    String	result;

    result = QuickInfoHelper.toString(this, "algorithm", m_Algorithm.getClass(), "algorithm: ");
    result += QuickInfoHelper.toString(this, "postProcessor", m_PostProcessor, ", post-process: ");

    return result;
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		the Class of objects that can be processed
   */
  @Override
  public Class[] accepts() {
    if (m_Algorithm instanceof UnsupervisedAlgorithm)
      return new Class[]{TensorContainer.class};
    else if (m_Algorithm instanceof SupervisedAlgorithm)
      return new Class[]{TensorContainer[].class};
    else
      throw new IllegalStateException("Unhandled algorithm: " + Utils.classToString(m_Algorithm));
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		the Class of the generated tokens
   */
  @Override
  public Class[] generates() {
    return new Class[]{ThreeWayDataModelContainer.class};
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String			result;
    TensorContainer		trainUnsuper;
    TensorContainer[]		trainSuper;
    Tensor			tensorUnsuper;
    Tensor[]			tensorSuper;
    ThreeWayDataModelContainer	cont;

    result = null;

    trainUnsuper  = null;
    trainSuper    = new TensorContainer[0];
    tensorUnsuper = null;
    tensorSuper   = new Tensor[0];
    cont          = null;
    if (m_Algorithm instanceof UnsupervisedAlgorithm) {
      trainUnsuper = m_InputToken.getPayload(TensorContainer.class);
      tensorUnsuper = trainUnsuper.getContent();
    }
    else if (m_Algorithm instanceof SupervisedAlgorithm) {
      trainSuper = m_InputToken.getPayload(TensorContainer[].class);
      if (trainSuper.length != 2) {
	result = "Supervised training requires 2 Tensor objects as input!";
      }
      else {
	tensorSuper    = new Tensor[2];
	tensorSuper[0] = trainSuper[0].getContent();
	tensorSuper[1] = trainSuper[1].getContent();
      }
    }
    else
      result = "Unhandled algorithm: " + Utils.classToString(m_Algorithm);

    if (result == null) {
      try {
        m_CurrentAlgorithm = (AbstractAlgorithm) OptionUtils.shallowCopy(m_Algorithm);
	if (m_CurrentAlgorithm instanceof UnsupervisedAlgorithm) {
	  ((UnsupervisedAlgorithm) m_CurrentAlgorithm).build(tensorUnsuper);
	  if (!isStopped())
	    cont = new ThreeWayDataModelContainer(m_CurrentAlgorithm, trainUnsuper);
	}
	else if (m_CurrentAlgorithm instanceof SupervisedAlgorithm) {
	  ((SupervisedAlgorithm) m_CurrentAlgorithm).build(tensorSuper[0], tensorSuper[1]);
	  if (!isStopped())
	    cont = new ThreeWayDataModelContainer(m_CurrentAlgorithm, trainSuper);
	}
	else {
	  result = "Unhandled algorithm: " + Utils.classToString(m_Algorithm);
	}
      }
      catch (Exception e) {
        result = handleException("Failed to build model!", e);
      }

      // post-process and output
      if (cont != null) {
        if (m_PostProcessor.canHandle(m_CurrentAlgorithm))
          m_PostProcessor.postProcess(cont);
	m_OutputToken = new Token(cont);
      }
    }
    m_CurrentAlgorithm  = null;

    return result;
  }

  /**
   * Stops the execution. No message set.
   */
  @Override
  public void stopExecution() {
    if (m_CurrentAlgorithm != null)
      m_CurrentAlgorithm.stopExecution();

    super.stopExecution();
  }
}
