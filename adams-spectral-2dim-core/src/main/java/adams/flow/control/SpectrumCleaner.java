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
 * SpectrumCleaner.java
 * Copyright (C) 2009-2018 University of Waikato, Hamilton, New Zealand
 */

package adams.flow.control;

import adams.data.cleaner.spectrum.AbstractCleaner;
import adams.data.cleaner.spectrum.PassThrough;
import adams.data.spectrum.Spectrum;
import adams.db.DatabaseConnectionHandler;
import adams.db.DatabaseConnectionUser;
import adams.flow.core.Actor;
import adams.flow.core.ActorExecution;
import adams.flow.core.ActorHandler;
import adams.flow.core.ActorHandlerInfo;
import adams.flow.core.ActorUtils;
import adams.flow.core.Compatibility;
import adams.flow.core.InputConsumer;
import adams.flow.core.OutputProducer;
import adams.flow.core.Token;
import adams.flow.sink.Null;

/**
 <!-- globalinfo-start -->
 * 'Cleanses' the tokens being passed through. The rejected tokens and rejection messages can be access via the other outputs.
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
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 *         The name of the actor.
 *         default: SpectrumCleaner
 * </pre>
 *
 * <pre>-annotation &lt;knir.core.base.BaseString&gt; [-annotation ...] (property: annotations)
 *         The annotations to attach to this actor.
 * </pre>
 *
 * <pre>-skip (property: skip)
 *         If set to true, transformation is skipped and the input token is just forwarded
 *          as it is.
 * </pre>
 *
 * <pre>-progress (property: showProgress)
 *         If set to true, progress information will be output to stdout ('.').
 * </pre>
 *
 * <pre>-cleaner &lt;knir.data.cleaner.spectrum.AbstractCleaner [options]&gt; (property: cleaner)
 *         The cleaner to apply to the data.
 *         default: knir.data.cleaner.spectrum.PassThrough
 * </pre>
 *
 * <pre>-rejected &lt;knir.flow.core.Actor [options]&gt; (property: rejectedTokensActor)
 *         The actor to send the rejected tokens to.
 *         default: knir.flow.sink.Null -name Null
 * </pre>
 *
 * <pre>-messages &lt;knir.flow.core.Actor [options]&gt; (property: rejectionMessagesActor)
 *         The actor to send the rejection messages to.
 *         default: knir.flow.sink.Null -name Null
 * </pre>
 *
 * Default options for knir.data.cleaner.spectrum.PassThrough (-cleaner/cleaner):
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 * Default options for knir.flow.sink.Null (-rejected/rejectedTokensActor):
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 *         The name of the actor.
 *         default: Null
 * </pre>
 *
 * <pre>-annotation &lt;knir.core.base.BaseString&gt; [-annotation ...] (property: annotations)
 *         The annotations to attach to this actor.
 * </pre>
 *
 * <pre>-skip (property: skip)
 *         If set to true, transformation is skipped and the input token is just forwarded
 *          as it is.
 * </pre>
 * Default options for knir.flow.sink.Null (-messages/rejectionMessagesActor):
 *
 * <pre>-D (property: debug)
 *         If set to true, scheme may output additional info to the console.
 * </pre>
 *
 * <pre>-name &lt;java.lang.String&gt; (property: name)
 *         The name of the actor.
 *         default: Null
 * </pre>
 *
 * <pre>-annotation &lt;knir.core.base.BaseString&gt; [-annotation ...] (property: annotations)
 *         The annotations to attach to this actor.
 * </pre>
 *
 * <pre>-skip (property: skip)
 *         If set to true, transformation is skipped and the input token is just forwarded
 *          as it is.
 * </pre>
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumCleaner
  extends AbstractControlActor
  implements InputConsumer, OutputProducer, DatabaseConnectionUser {

  /** for serialization. */
  private static final long serialVersionUID = -3989728996485003609L;

  /** the actor for the rejected tokens. */
  protected Actor m_RejectedTokensActor;

  /** the actor for the rejection messages. */
  protected Actor m_RejectionMessagesActor;

  /** the spectrum cleaner to use. */
  protected AbstractCleaner m_Cleaner;

  /** the input token. */
  protected transient Token m_InputToken;

  /** the output token. */
  protected transient Token m_OutputToken;

  /** whether the database connection has been updated. */
  protected boolean m_DatabaseConnectionUpdated;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return
        "'Cleanses' the tokens being passed through. The rejected tokens and "
      + "rejection messages can be access via the other outputs.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	    "cleaner", "cleaner",
	    new PassThrough());

    m_OptionManager.add(
	    "rejected", "rejectedTokensActor",
	    new Null());

    m_OptionManager.add(
	    "messages", "rejectionMessagesActor",
	    new Null());
  }

  /**
   * Initializes the members.
   */
  @Override
  protected void initialize() {
    super.initialize();

    m_RejectedTokensActor     = new Null();
    m_RejectionMessagesActor  = new Null();
  }

  /**
   * Resets the scheme.
   */
  @Override
  protected void reset() {
    super.reset();

    m_DatabaseConnectionUpdated = false;
  }

  /**
   * Sets the actor to send the rejected tokens to.
   *
   * @param value	the actor
   * @return		null if everything is fine, otherwise the error
   */
  public String setRejectedTokensActor(Actor value) {
    String	result;

    result = null;

    if (value instanceof InputConsumer) {
      m_RejectedTokensActor = value;
      reset();
      updateParent();
    }
    else {
      if (value instanceof AbstractDirectedControlActor)
	result = "You need to provide a group that processes input, like 'Branch'!";
      else
	result = "You need to provide an actor that processes input!";
    }

    return result;
  }

  /**
   * Returns the actor the rejected tokens are sent to.
   *
   * @return		the actor
   */
  public Actor getRejectedTokensActor() {
    return m_RejectedTokensActor;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rejectedTokensActorTipText() {
    return "The actor to send the rejected tokens to.";
  }

  /**
   * Sets the actor to send the rejection messages to.
   *
   * @param value	the actor
   * @return		null if everything is fine, otherwise the error
   */
  public String setRejectionMessagesActor(Actor value) {
    String	result;

    result = null;

    if (value instanceof InputConsumer) {
      m_RejectionMessagesActor = value;
      reset();
      updateParent();
    }
    else {
      if (value instanceof AbstractDirectedControlActor)
	result = "You need to provide a group that processes input, like 'Branch'!";
      else
	result = "You need to provide an actor that processes input!";
    }

    return result;
  }

  /**
   * Returns the actor to send the rejection messages to.
   *
   * @return		the actor
   */
  public Actor getRejectionMessagesActor() {
    return m_RejectionMessagesActor;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String rejectionMessagesActorTipText() {
    return "The actor to send the rejection messages to.";
  }

  /**
   * Sets the cleaner.
   *
   * @param value	the cleaner
   */
  public void setCleaner(AbstractCleaner value) {
    m_Cleaner = value;
    reset();
  }

  /**
   * Returns the cleaner.
   *
   * @return		the actor
   */
  public AbstractCleaner getCleaner() {
    return m_Cleaner;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String cleanerTipText() {
    return "The cleaner to apply to the data.";
  }

  /**
   * Returns the size of the group.
   *
   * @return		always 2
   */
  @Override
  public int size() {
    return 2;
  }

  /**
   * Returns the actor at the given position.
   *
   * @param index	the position
   * @return		the actor
   */
  @Override
  public Actor get(int index) {
    if (index == 0)
      return m_RejectedTokensActor;
    else if (index == 1)
      return m_RejectionMessagesActor;
    else
      throw new IndexOutOfBoundsException("Only two items available, requested index: " + index);
  }

  /**
   * Sets the actor at the given position.
   *
   * @param index	the position
   * @param actor	the actor to set at this position
   * @return		null if everything is fine, otherwise the error
   */
  @Override
  public String set(int index, Actor actor) {
    String	result;

    if (index == 0) {
      result = setRejectedTokensActor(actor);
      updateParent();
    }
    else if (index == 1) {
      result = setRejectionMessagesActor(actor);
      updateParent();
    }
    else {
      result = "Index out of range: " + index;
      getLogger().severe(result);
    }

    return result;
  }

  /**
   * Returns the index of the actor.
   *
   * @param actor	the name of the actor to look for
   * @return		the index of -1 if not found
   */
  @Override
  public int indexOf(String actor) {
    if (m_RejectedTokensActor.getName().equals(actor))
      return 0;
    else if (m_RejectionMessagesActor.getName().equals(actor))
      return 1;
    else
      return -1;
  }

  /**
   * Returns some information about the actor handler, e.g., whether it can
   * contain standalones and the actor execution.
   *
   * @return		the info
   */
  @Override
  public ActorHandlerInfo getActorHandlerInfo() {
    return new ActorHandlerInfo(false, false, ActorExecution.UNDEFINED, true, null, false);
  }

  /**
   * Returns the class that the consumer accepts.
   *
   * @return		<!-- flow-accepts-start -->knir.data.spectrum.Spectrum.class<!-- flow-accepts-end -->
   */
  public Class[] accepts() {
    return new Class[]{Spectrum.class};
  }

  /**
   * Initializes the sub-actors for flow execution.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  public String setUp() {
    String		result;
    Compatibility	comp;

    result = super.setUp();

    if (m_RejectedTokensActor == null)
      result = "No actor for rejected tokens provided!";
    if (m_RejectionMessagesActor == null)
      result = "No actor for rejection messages provided!";

    if ((result == null) && (!getSkip())) {
      comp = new Compatibility();
      if (!comp.isCompatible(accepts(), ((InputConsumer) m_RejectedTokensActor).accepts()))
	result = "Accepted input and actor for rejected tokens are not compatible!";
      else if (!comp.isCompatible(new Class[]{String.class}, ((InputConsumer) m_RejectionMessagesActor).accepts()))
	result = "Accepted input and actor for rejection messages are not compatible!";
    }

    return result;
  }

  /**
   * The method that accepts the input token and then processes it.
   *
   * @param token	the token to accept and process
   */
  public void input(Token token) {
    m_InputToken  = token;
    m_OutputToken = null;
  }

  /**
   * Returns whether an input token is currently present.
   *
   * @return		true if input token present
   */
  public boolean hasInput() {
    return (m_InputToken != null);
  }

  /**
   * Returns the current input token, if any.
   *
   * @return		the input token, null if none present
   */
  public Token currentInput() {
    return m_InputToken;
  }

  /**
   * Executes the actor.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  protected String doExecute() {
    String	result;
    String	msg;
    String	rejT;
    String	rejM;

    result = null;

    if (!m_DatabaseConnectionUpdated) {
      m_DatabaseConnectionUpdated = true;
      if (m_Cleaner instanceof DatabaseConnectionHandler)
	((DatabaseConnectionHandler) m_Cleaner).setDatabaseConnection(
	    ActorUtils.getDatabaseConnection(
		  this,
		  adams.flow.standalone.DatabaseConnectionProvider.class,
		  adams.db.DatabaseConnection.getSingleton()));
      m_Cleaner.setFlowContent(this);
    }

    msg = null;
    try {
      msg = m_Cleaner.check((Spectrum) m_InputToken.getPayload());
    }
    catch (Exception e) {
      result = handleException("Failed to clean spectrum: " + m_InputToken.getPayload(), e);
    }

    if (msg != null) {
      ((InputConsumer) m_RejectedTokensActor).input(m_InputToken);
      rejT = m_RejectedTokensActor.execute();
      ((InputConsumer) m_RejectionMessagesActor).input(new Token(msg));
      rejM = m_RejectionMessagesActor.execute();
      if (rejT != null) {
	if (result != null)
	  result += ", ";
	else
	  result = "";
	result += rejT;
      }
      if (rejM != null) {
	if (result != null)
	  result += ", ";
	else
	  result = "";
	result += rejM;
      }
    }
    else {
      m_OutputToken = m_InputToken;
    }

    m_InputToken = null;

    return result;
  }

  /**
   * Executes the flow item.
   *
   * @return		null if everything is fine, otherwise error message
   */
  @Override
  public String execute() {
    String	result;

    result = super.execute();
    
    if (m_Skip)
      m_OutputToken = m_InputToken;
    
    return result;
  }

  /**
   * Returns the class of objects that it generates.
   *
   * @return		<!-- flow-generates-start -->knir.data.spectrum.Spectrum.class<!-- flow-generates-end -->
   */
  public Class[] generates() {
    return new Class[]{Spectrum.class};
  }

  /**
   * Returns the generated token.
   *
   * @return		the generated token
   */
  public Token output() {
    Token	result;

    result        = m_OutputToken;
    m_OutputToken = null;

    return result;
  }

  /**
   * Checks whether there is pending output to be collected after
   * executing the flow item.
   *
   * @return		true if there is pending output
   */
  public boolean hasPendingOutput() {
    return (m_OutputToken != null);
  }
  
  /**
   * Stops the processing of tokens without stopping the flow.
   */
  public void flushExecution() {
    if (m_RejectedTokensActor instanceof ActorHandler)
      ((ActorHandler) m_RejectedTokensActor).flushExecution();
    if (m_RejectionMessagesActor instanceof ActorHandler)
      ((ActorHandler) m_RejectionMessagesActor).flushExecution();
  }

  /**
   * Stops the execution.
   */
  @Override
  public void stopExecution() {
    m_RejectedTokensActor.stopExecution();
    m_RejectionMessagesActor.stopExecution();

    super.stopExecution();
  }
}
