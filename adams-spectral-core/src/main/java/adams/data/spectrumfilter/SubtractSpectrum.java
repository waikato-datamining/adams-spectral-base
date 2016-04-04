/*
 * Scale.java
 * Copyright (C) 2008 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractDatabaseConnectionFilter;
import adams.data.filter.AbstractFilter;
import adams.data.filter.PassThrough;
import adams.db.AbstractDatabaseConnection;
import adams.db.DatabaseConnection;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.db.SpectrumT;

import java.util.List;

/**
 <!-- globalinfo-start -->
 * Scales the amplitudes to a given maximum.
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
 * <pre>-min &lt;double&gt; (property: minAmplitude)
 *         The minimum amplitude to scale to.
 *         default: 0.0
 * </pre>
 *
 * <pre>-max &lt;double&gt; (property: maxAmplitude)
 *         The maximum amplitude to scale to.
 *         default: 100.0
 * </pre>
 *
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2242 $
 */
public class SubtractSpectrum
  extends AbstractDatabaseConnectionFilter<Spectrum> {

  /** for serialization. */
 
  /**
   * Returns the default database connection.
   *
   * @return		the default database connection
   */
  protected AbstractDatabaseConnection getDefaultDatabaseConnection() {
    return DatabaseConnection.getSingleton();
  }
  /**
   * 
   */
  private static final long serialVersionUID = 3414623712198778099L;

  /** the filter to run. */
  protected AbstractFilter<Spectrum> m_Filter;
  
  protected int m_id=69052;

  /**
   * Returns a string describing the object.
   *
   * @return 		a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return "Subtract a spectrum, after filtering.";
  }
  /**
   * Adds options to the internal list of options.
   */
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
	"filter", "filter",
	new PassThrough());
  }

  /**
   * Sets the filter to run.
   *
   * @param value 	the filter
   */
  public void setFilter(AbstractFilter value) {
    m_Filter = value;
  }

  /**
   * Returns the filter being used.
   *
   * @return 		the filter
   */
  public AbstractFilter getFilter() {
    return m_Filter;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String filterTipText() {
    return "The filter to use.";
  }
  


  

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  protected Spectrum processData(Spectrum data) {
    Spectrum		result;
    List<SpectrumPoint>	list;
    double		min;
    double		max;
    double		scale;
    int			i;
   


    Spectrum f1=m_Filter.filter(data);
    Spectrum sp=SpectrumT.getSingleton(getDatabaseConnection()).load(new Integer(m_id));
    Spectrum f2=m_Filter.filter(sp);
   
    result = data.getHeader();
    List<SpectrumPoint> list1 = f1.toList();
    List<SpectrumPoint> list2 = f2.toList();
    
   
    for (i = 0; i < list1.size(); i++) {
      // scale point
      SpectrumPoint point1    = list1.get(i);
      SpectrumPoint point2    = list2.get(i);
     
      SpectrumPoint pointNew = new SpectrumPoint(
	  		point1.getWaveNumber(),
	  		(float) ((point1.getAmplitude() -point2.getAmplitude())));

      // add to output
      result.add(pointNew);
    }

    return result;
  }
}
