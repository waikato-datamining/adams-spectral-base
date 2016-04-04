

/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 * BestBySLR.java
 *
 */

  package weka.filters.unsupervised.attribute;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.SimpleBatchFilter;

import java.util.Enumeration;
import java.util.Vector;

/**
 <!-- globalinfo-start -->
 * Chooses a random subset of attributes, either an absolute number or a percentage. The class is always included in the output (as the last attribute).
 * <p/>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p/>
 *
 * <pre> -D
 *  Turns on output of debugging information.</pre>
 *
 * <pre> -N &lt;double&gt;
 *  The number of attributes to  select.</pre>

 *
 <!-- options-end -->
 *
 * @author  dale (dale at waikato dot ac dot nz)
 * @version $Revision: 1.2 $
 */
   public class BestBySLR
  extends SimpleBatchFilter {

  /** for serialization. */


  /** The number of attributes to choose. */
     protected int m_NumAttributes = 1;

  Remove m_Remove=null;

  /**
   * Returns a string describing this filter.
   *
   * @return            a description of the filter suitable for
   *              displaying in the explorer/experimenter gui
   */
     public String globalInfo() {
    return
      "Returns the top N attributes as determined by Simple Linear Regression";
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
     public Enumeration listOptions() {
    Vector        result;
    Enumeration   enm;

    result = new Vector();

    enm = super.listOptions();
    while (enm.hasMoreElements())
      result.addElement(enm.nextElement());

    result.addElement(new Option(
      "\tThe number of attributes to  select.",
      "N", 1, "-N <int>"));


    return result.elements();
  }

  /**
   * Gets the current settings of the filter.
   *
   * @return an array of strings suitable for passing to setOptions
   */
     public String[] getOptions() {
    int                 i;
    Vector<String>      result;
    String[]            options;

    result  = new Vector<String>();
    options = super.getOptions();
    for (i = 0; i < options.length; i++)
      result.add(options[i]);

    result.add("-N");
    result.add("" + m_NumAttributes);

    return result.toArray(new String[result.size()]);
  }

  /**
   * Parses a given list of options. <p/>
   *
   <!-- options-start -->
   * Valid options are: <p/>
   *
   * <pre> -D
   *  Turns on output of debugging information.</pre>
   *
   * <pre> -N &lt;int&gt;
   *  The number of attributes to select.</pre>
   *

   *
   <!-- options-end -->
   *
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported 
   */
     public void setOptions(String[] options) throws Exception {
    String  tmpStr;

    tmpStr = Utils.getOption("N", options);
    if (tmpStr.length() != 0)
      setNumAttributes(Integer.parseInt(tmpStr));
    else
      setNumAttributes(1);


    super.setOptions(options);
  }

  /**
   * Returns the tip text for this property.
   *
   * @return            tip text for this property suitable for
   *              displaying in the explorer/experimenter gui
   */
     public String numAttributesTipText() {
    return "The number of attributes to choose";
  }

  /**
   * Get the number of attributes (&lt; 1 percentage, &gt;= 1 absolute number).
   *
   * @return            the number of attributes.
   */
     public int getNumAttributes() {
    return m_NumAttributes;
  }

  /**
   * Set the number of attributes. 
   *
   * @param value the number of attributes to use.
   */
     public void setNumAttributes(int value) {
    m_NumAttributes = value;
  }


  /**
   * Returns the Capabilities of this filter.
   *
   * @return            the capabilities of this object
   * @see               Capabilities
   */
     public Capabilities getCapabilities() {
    Capabilities result = super.getCapabilities();

    // attributes
    result.enableAllAttributes();
    result.enable(Capability.MISSING_VALUES);

    // class
    result.enableAllClasses();
    result.enable(Capability.MISSING_CLASS_VALUES);
    result.enable(Capability.NO_CLASS);

    return result;
  }


  /**
   * Return array of attributes in sorted order
   * @param insts instances
   * @return sorted attribute array
   */
  protected int[] getSortedAttributeArray(Instances insts){
    // remove instances with missing class
    insts = new Instances(insts);
    insts.deleteWithMissingClass();

    double sq[]=new double[insts.numAttributes()-1];
    // Compute mean of target value
    double yMean = insts.meanOrMode(insts.classIndex());

    // Choose best attribute
    double minMsq = Double.MAX_VALUE;
    Attribute m_attribute = null;
    int chosen = -1;
    double chosenSlope = Double.NaN;
    double chosenIntercept = Double.NaN;
    for (int i = 0; i < insts.numAttributes(); i++) {
      if (i != insts.classIndex()) {
	m_attribute = insts.attribute(i);

	// Compute slope and intercept
	double xMean = insts.meanOrMode(i);
	double sumWeightedXDiffSquared = 0;
	double sumWeightedYDiffSquared = 0;
	double m_slope = 0;
	for (int j = 0; j < insts.numInstances(); j++) {
	  Instance inst = insts.instance(j);
	  if (!inst.isMissing(i) && !inst.classIsMissing()) {
	    double xDiff = inst.value(i) - xMean;
	    double yDiff = inst.classValue() - yMean;
	    double weightedXDiff = inst.weight() * xDiff;
	    double weightedYDiff = inst.weight() * yDiff;
	    m_slope += weightedXDiff * yDiff;
	    sumWeightedXDiffSquared += weightedXDiff * xDiff;
	    sumWeightedYDiffSquared += weightedYDiff * yDiff;
	  }
	}

	// Skip attribute if not useful
	if (sumWeightedXDiffSquared == 0) {
	  continue;
	}
	double numerator = m_slope;
	m_slope /= sumWeightedXDiffSquared;
	double m_intercept = yMean - m_slope * xMean;

	// Compute sum of squared errors
	double msq = sumWeightedYDiffSquared - m_slope * numerator;

	sq[i]=msq;
	// Check whether this is the best attribute
      }
    }
    return(Utils.sort(sq));
  }

  public boolean allowAccessToFullInputFormat() {
    return true;
  }

  /**
   * Determines the output format based on the input format and returns 
   * this. In case the output format cannot be returned immediately, i.e.,
   * hasImmediateOutputFormat() returns false, then this method will called
   * from batchFinished() after the call of preprocess(Instances), in which,
   * e.g., statistics for the actual processing step can be gathered.
   *
   * @param inputFormat     the input format to base the output format on
   * @return                the output format
   * @throws Exception      in case the determination goes wrong
   */
     protected Instances determineOutputFormat(Instances inputFormat) throws Exception {
    Instances           result;


       int[] rem=getSortedAttributeArray(inputFormat);

       int[] indices=new int[m_NumAttributes+1];
       for (int i=0;i<m_NumAttributes;i++){
	 indices[i]=rem[i];
       }
       if (inputFormat.classIndex() != -1) {
	 indices[m_NumAttributes] = inputFormat.classIndex();
       }
       m_Remove=new Remove();
       m_Remove.setAttributeIndicesArray(indices);
       m_Remove.setInvertSelection(true);
       m_Remove.setInputFormat(inputFormat);
       result= Filter.useFilter(inputFormat,m_Remove);

    return result;
  }

  @Override
  protected Instances process(Instances instances) throws Exception {

    Instances result= Filter.useFilter(instances,m_Remove);
    return result;
  }


  /**
   * Returns the revision string.
   *
   * @return            the revision
   */
     public String getRevision() {
    return RevisionUtils.extract("$Revision: 1.2 $");
  }

  /**
   * Runs the filter with the given parameters. Use -h to list options.
   *
   * @param args  the commandline options
   */
     public static void main(String[] args) {
    runFilter(new BestBySLR(), args);
  }
}