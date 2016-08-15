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
 * RonWeasley.java
 * Copyright (C) 2015 University of Waikato, Hamilton, New Zealand
 */

package adams.genetic;

import adams.core.Properties;
import adams.core.Utils;
import adams.core.option.OptionUtils;
import weka.classifiers.functions.GPD;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.SpectrumClassifier;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.MultiFilter;
import weka.filters.supervised.attribute.PLSFilter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.SavitzkyGolayRange;
import weka.filters.unsupervised.attribute.SegmentedSavitzkyGolay;
import weka.filters.unsupervised.attribute.SpectrumFilter;

import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Ron Weasley.
 * <br><br>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * <pre>-logging-level &lt;OFF|SEVERE|WARNING|INFO|CONFIG|FINE|FINER|FINEST&gt; (property: loggingLevel)
 * &nbsp;&nbsp;&nbsp;The logging level for outputting errors and debugging output.
 * &nbsp;&nbsp;&nbsp;default: WARNING
 * </pre>
 * 
 * <pre>-num-threads &lt;int&gt; (property: numThreads)
 * &nbsp;&nbsp;&nbsp;The number of threads to use for executing the jobs; use -1 for all available 
 * &nbsp;&nbsp;&nbsp;cores.
 * &nbsp;&nbsp;&nbsp;default: -1
 * &nbsp;&nbsp;&nbsp;minimum: -1
 * </pre>
 * 
 * <pre>-num-chrom &lt;int&gt; (property: numChrom)
 * &nbsp;&nbsp;&nbsp;The number of chromosomes, ie, the population size.
 * &nbsp;&nbsp;&nbsp;default: 50
 * </pre>
 * 
 * <pre>-stopping-criterion &lt;adams.genetic.stopping.AbstractStoppingCriterion&gt; (property: stoppingCriterion)
 * &nbsp;&nbsp;&nbsp;The stopping criterion to use.
 * &nbsp;&nbsp;&nbsp;default: adams.genetic.stopping.MaxIterations
 * </pre>
 * 
 * <pre>-seed &lt;long&gt; (property: seed)
 * &nbsp;&nbsp;&nbsp;The seed value for the random number generator.
 * &nbsp;&nbsp;&nbsp;default: 1
 * </pre>
 * 
 * <pre>-initial-weights &lt;java.lang.String&gt; (property: initialWeights)
 * &nbsp;&nbsp;&nbsp;The initial weights to use, rather than random ones (string of 0s and 1s
 * &nbsp;&nbsp;&nbsp;).
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-favor-zeroes &lt;boolean&gt; (property: favorZeroes)
 * &nbsp;&nbsp;&nbsp;Whether to favor 0s instead of 1s.
 * &nbsp;&nbsp;&nbsp;default: false
 * </pre>
 * 
 * <pre>-best &lt;java.lang.String&gt; (property: bestRange)
 * &nbsp;&nbsp;&nbsp;The range of the best attributes.
 * &nbsp;&nbsp;&nbsp;default: -none-
 * </pre>
 * 
 * <pre>-notify &lt;int&gt; (property: notificationInterval)
 * &nbsp;&nbsp;&nbsp;The time interval in seconds after which notification events about changes 
 * &nbsp;&nbsp;&nbsp;in the fitness can be sent (-1 = never send notifications; 0 = whenever 
 * &nbsp;&nbsp;&nbsp;a change occurs).
 * &nbsp;&nbsp;&nbsp;default: -1
 * </pre>
 * 
 * <pre>-bits-per-gene &lt;int&gt; (property: bitsPerGene)
 * &nbsp;&nbsp;&nbsp;The number of bits per gene to use.
 * &nbsp;&nbsp;&nbsp;default: 1
 * &nbsp;&nbsp;&nbsp;minimum: 1
 * </pre>
 * 
 * <pre>-class &lt;adams.data.weka.WekaAttributeIndex&gt; (property: classIndex)
 * &nbsp;&nbsp;&nbsp;The class index of the dataset, in case no class attribute is set.
 * &nbsp;&nbsp;&nbsp;default: last
 * &nbsp;&nbsp;&nbsp;example: An index is a number starting with 1; apart from attribute names (case-sensitive), the following placeholders can be used as well: first, second, third, last_2, last_1, last
 * </pre>
 * 
 * <pre>-folds &lt;int&gt; (property: folds)
 * &nbsp;&nbsp;&nbsp;The number of folds to use in cross-validation.
 * &nbsp;&nbsp;&nbsp;default: 10
 * &nbsp;&nbsp;&nbsp;minimum: 2
 * </pre>
 * 
 * <pre>-cv-seed &lt;int&gt; (property: crossValidationSeed)
 * &nbsp;&nbsp;&nbsp;The seed value for cross-validation.
 * &nbsp;&nbsp;&nbsp;default: 55
 * </pre>
 * 
 * <pre>-classifier &lt;weka.classifiers.Classifier&gt; (property: classifier)
 * &nbsp;&nbsp;&nbsp;The classifier to use.
 * &nbsp;&nbsp;&nbsp;default: weka.classifiers.rules.ZeroR
 * </pre>
 * 
 * <pre>-measure &lt;CC|RMSE|RRSE|MAE|RAE|ACC&gt; (property: measure)
 * &nbsp;&nbsp;&nbsp;The measure used for evaluating the fitness.
 * &nbsp;&nbsp;&nbsp;default: RMSE
 * </pre>
 * 
 * <pre>-output-dir &lt;adams.core.io.PlaceholderDirectory&gt; (property: outputDirectory)
 * &nbsp;&nbsp;&nbsp;The directory for storing the generated ARFF files.
 * &nbsp;&nbsp;&nbsp;default: ${CWD}
 * </pre>
 * 
 * <pre>-output-type &lt;NONE|SETUP|DATA|ALL&gt; (property: outputType)
 * &nbsp;&nbsp;&nbsp;The type of output to generate.
 * &nbsp;&nbsp;&nbsp;default: ALL
 * </pre>
 * 
 * <pre>-output-prefix-type &lt;NONE|RELATION|SUPPLIED&gt; (property: outputPrefixType)
 * &nbsp;&nbsp;&nbsp;The type of prefix to use for the output.
 * &nbsp;&nbsp;&nbsp;default: NONE
 * </pre>
 * 
 * <pre>-supplied-prefix &lt;java.lang.String&gt; (property: suppliedPrefix)
 * &nbsp;&nbsp;&nbsp;The prefix to use in case of SUPPLIED.
 * &nbsp;&nbsp;&nbsp;default: 
 * </pre>
 * 
 * <pre>-segments &lt;int&gt; (property: segments)
 * &nbsp;&nbsp;&nbsp;The number of segments to split into for s-g.
 * &nbsp;&nbsp;&nbsp;default: 4
 * </pre>
 * 
 * <pre>-minPLS &lt;int&gt; (property: minPLS)
 * &nbsp;&nbsp;&nbsp;The min number of pls componenents.
 * &nbsp;&nbsp;&nbsp;default: 16
 * </pre>
 * 
 * <pre>-maxPLS &lt;int&gt; (property: maxPLS)
 * &nbsp;&nbsp;&nbsp;The max number of pls componenents.
 * &nbsp;&nbsp;&nbsp;default: 32
 * </pre>
 * 
 * <pre>-minSG &lt;int&gt; (property: minSG)
 * &nbsp;&nbsp;&nbsp;The min sg smoothing window.
 * &nbsp;&nbsp;&nbsp;default: 4
 * </pre>
 * 
 * <pre>-maxSG &lt;int&gt; (property: maxSG)
 * &nbsp;&nbsp;&nbsp;The max sg smoothing window.
 * &nbsp;&nbsp;&nbsp;default: 64
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author Dale (dale at cs dot waikato dot ac dot nz)
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 4322 $
 */
public class RonWeasley
  extends AbstractClassifierBasedGeneticAlgorithm {

  private static final long serialVersionUID = -4982024446995877986L;

  protected int m_numSegments;

  protected int m_minPLSComponents;

  protected int m_maxPLSComponents;

  protected int m_minSG;

  protected int m_maxSG;

  protected int len;

  protected int numatts;

  /**
   * A job class specific to Ron Weasley.
   *
   * @author  dale
   * @version $Revision: 4322 $
   */
  public static class RonWeasleyJob
    extends ClassifierBasedGeneticAlgorithmJob<RonWeasley> {

    /** for serialization. */
    private static final long serialVersionUID = 8259167463381721274L;

    /**
     * Initializes the job.
     *
     * @param g		the algorithm object this job belongs to
     * @param num	the number of chromsomes
     * @param w		the initial weights
     * @param data	the data to use
     */
    public RonWeasleyJob(RonWeasley g, int num, int[] w, Instances data) {
      super(g, num, w, data);
    }

    /**
     * Generates a range string of attributes to keep (= one has to use
     * the inverse matching sense with the Remove filter).
     *
     * @return		the range of attributes to keep
     */
    public String getRemoveAsString() {
      String out="PLS:";
      out += getOwner().getPLSComponentFromWeights(m_Weights)+" ";
      for (int i=0;i< getOwner().getSegments();i++) {
	SavitzkyGolayRange s=new SavitzkyGolayRange();
	s.setNumPoints(getOwner().getSGFromWeights(i, m_Weights));
	out+=s.getNumPoints()+" ";
      }

      return out;
    }

    /**
     * Calculates the new fitness.
     */
    @Override
    public void calcNewFitness() {
      try {
	getLogger().fine((new StringBuilder("calc for:")).append(weightsToString()).toString());

	// was measure already calculated for this attribute setup?
	Double cc = getOwner().getResult(weightsToString());
	if (cc != null) {
	  getLogger().info((new StringBuilder("Already present: ")).append(Double.toString(cc.doubleValue())).toString());
	  m_Fitness = cc;
	  return;
	}

	Instances newInstances = new Instances(getInstances());

	SpectrumFilter sf=new SpectrumFilter();
	String out="PLS:";
	out+= getOwner().getPLSComponentFromWeights(m_Weights)+" ";
	SegmentedSavitzkyGolay ssg=new SegmentedSavitzkyGolay();
	ssg.setDerivativeOrder(0);
	String ints=""+ getOwner().getSGFromWeights(0, m_Weights);
	for (int i=1;i< getOwner().getSegments();i++) {
	  int val= getOwner().getSGFromWeights(i, m_Weights);
	  out += val+" ";
	  ints+=" "+val;
	}
	ssg.setNumPoints(ints);
	sf.setFilter(ssg);

	if (isLoggingEnabled())
	  getLogger().info(out);
	// obtain classifier
	SpectrumClassifier newClassifier = new SpectrumClassifier();
	FilteredClassifier fc=new FilteredClassifier();
	newClassifier.setClassifier(fc);
	GPD gpd=new GPD();
	gpd.setGamma(.01);
	gpd.setNoise(.01);
	fc.setClassifier(gpd);
	MultiFilter mf=new MultiFilter();

	Filter[] f=new Filter[2];PLSFilter pls=new PLSFilter();
	pls.setNumComponents(getOwner().getPLSComponentFromWeights(m_Weights));
	f[0]=sf;
	f[1]=pls;

	mf.setFilters(f);
	fc.setFilter(mf);

	// evaluate classifier
	m_Fitness = evaluateClassifier(newClassifier, newInstances, getFolds(), getSeed());

	// process fitness
	if (getOwner().setNewFitness(m_Fitness, newClassifier, m_Weights)) {
	  generateOutput(m_Fitness, newInstances, newClassifier, m_Weights);
	  // notify the listeners
	  getOwner().notifyFitnessChangeListeners(getMeasure().adjust(m_Fitness), newClassifier, m_Weights);
	}

	getOwner().addResult(weightsToString(), m_Fitness);
      }
      catch(Exception e) {
	getLogger().log(Level.SEVERE, "Error: ", e);
	m_Fitness = null;
      }
    }
  }

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Ron Weasley.";
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "segments", "segments",
      4);

    m_OptionManager.add(
      "minPLS", "minPLS",
      16);

    m_OptionManager.add(
      "maxPLS", "maxPLS",
      32);

    m_OptionManager.add(
      "minSG", "minSG",
      4);

    m_OptionManager.add(
      "maxSG", "maxSG",
      64);
  }

  /**
   * Returns the default output type to use.
   *
   * @return		the type
   */
  protected OutputType getDefaultOutputType() {
    return OutputType.SETUP;
  }

  /**
   * Set number of s-g segments to optimise
   * @param seg
   */
  public void setSegments(int seg) {
    m_numSegments=seg;
    reset();
  }

  /**
   * Get number of s-g segments
   * @return segments
   */
  public int getSegments() {
    return(m_numSegments);
  }
  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String segmentsTipText() {
    return "The number of segments to split into for s-g.";
  }

  /**
   * Get minimum pls components
   * @return min pls components
   */
  public int getMinPLS() {
    return(m_minPLSComponents);
  }

  /**
   * Set minimum pls components
   * @param pls min comps
   */
  public void setMinPLS(int pls) {
    m_minPLSComponents=pls;
    reset();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String minPLSTipText() {
    return "The min number of pls componenents.";
  }

  /**
   * Set max pls components
   * @param max pls comps.
   */
  public void setMaxPLS(int max) {
    m_maxPLSComponents=max;
    reset();
  }

  /**
   * Get max PLS components
   * @return max comps
   */
  public int getMaxPLS() {
    return(m_maxPLSComponents);
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String maxPLSTipText() {
    return "The max number of pls componenents.";
  }

  /**
   * Set minimum sg
   * @param sg
   */
  public void setMinSG(int sg) {
    m_minSG = sg;
    reset();
  }

  /**
   * Get minimum sg
   * @return min sg window
   */
  public int getMinSG() {
    return(m_minSG);
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String minSGTipText() {
    return "The min sg smoothing window.";
  }

  /**
   * Get max sg smoothing windows
   * @return sg window max
   */
  public int getMaxSG() {
    return(m_maxSG);
  }

  /**
   * Set max sg smoothing window
   * @param sg window maxs
   */
  public void setMaxSG(int sg) {
    m_maxSG=sg;
    reset();
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String maxSGTipText() {
    return "The max sg smoothing window.";
  }

  protected int getPLSBits() {
    int range=m_maxPLSComponents-m_minPLSComponents;
    return((int)(Math.floor(Utils.log2(range))+1));
  }

  protected int getSGBits() {
    int range=m_maxSG-m_minSG;
    return((int)(Math.floor(Utils.log2(range))+1));
  }

  protected int getInt(int start, int length, int[] c) {
    double j=0;
    for (int i=start;i<start+length;i++) {
      if (c[i] == 1) {
	//j = j + Math.pow(2, length - 1 - i);
	j = j + Math.pow(2, i-start);
      }
    }
    return((int)j);
  }

  protected int getPLSComponentFromWeights(int[] w) {
    return(getInt(0, getPLSBits(), w)+m_minPLSComponents);
  }

  protected int getNumSegments() {
    return m_numSegments;
  }

  public int getSGFromWeights(int segment, int[] w) {
    int plsl=getPLSBits();
    int sgl=getSGBits();
    int start=plsl+(segment * sgl);
    return(getInt(start,sgl,w)+m_minSG);
  }

  /**
   * Creates a new Job instance.
   *
   * @param chromosome		the number of chromosomes
   * @param w		the initial weights
   * @return		the instance
   * @param data	the data to use
   */
  protected RonWeasleyJob newJob(int chromosome, int[] w, Instances data) {
    return new RonWeasleyJob(this, chromosome, w, data);
  }

  /**
   * Generates a Properties file that stores information on the setup of
   * the genetic algorithm. E.g., it backs up the original relation name.
   * The generated properties file will be used as new relation name for
   * the data.
   *
   * @param data	the data to create the setup for
   * @param job		the associated job
   * @see		#PROPS_RELATION
   * @return		the generated setup
   */
  @Override
  protected Properties storeSetup(Instances data, GeneticAlgorithmJob job) {
    Properties		result;
    RonWeasleyJob		jobDL;
    Remove		remove;

    result = super.storeSetup(data, job);
    jobDL  = (RonWeasleyJob) job;

    // remove filter setup
    remove = new Remove();
    remove.setAttributeIndices(jobDL.getRemoveAsString());
    remove.setInvertSelection(true);
    result.setProperty(PROPS_FILTER, OptionUtils.getCommandLine(remove));

    return result;
  }

  /**
   * Some more initializations.
   */
  @Override
  protected void preRun() {
    super.preRun();

    // setup structures
    init(20, (getPLSBits() + (getSGBits() * getSegments())) * m_BitsPerGene);
  }
}
