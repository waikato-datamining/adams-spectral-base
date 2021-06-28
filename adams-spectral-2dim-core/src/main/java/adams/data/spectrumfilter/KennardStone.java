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
 * KennardStone.java
 * Copyright (C) 2016-2021 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.container.DataContainer;
import adams.data.filter.AbstractBatchFilter;
import adams.data.filter.BatchFilter;
import adams.data.filter.Filter;
import adams.data.filter.PassThrough;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Batch filter that applies the Kennard-stone algorithm to select a subset of spectra
 * Created by Michael on 5/19/2016.
 */
public class KennardStone
  extends AbstractBatchFilter {

  private static final long serialVersionUID = 8266258749271797113L;

  /** Number of spectra to select in subset */
  protected int m_NumberInSubset;

  /** Pre filter to apply before selection */
  protected Filter m_PreFilter;

  /** Batch filter to apply before selection */
  protected BatchFilter m_BatchFilter;

  /** whether to invert the selection. */
  protected boolean m_Invert;

  @Override
  public String globalInfo() {
    return "Apply the Kennard-Stone algorithm to the array of spectra. Each spectrum has the pre filter applied"
      + "and the array of pre filtered spectra have a batch filter applied before the algorithm is applied";
  }

  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "number-in-subset", "numberInSubset",
      -1);

    m_OptionManager.add(
      "pre-filter", "preFilter",
      new SavitzkyGolay());

    m_OptionManager.add(
      "batch-filter", "batchFilter",
      new PassThrough<>());

    m_OptionManager.add(
      "invert", "invert",
      false);
  }

  /**
   * Set the number of spectra to select in subset
   *
   * @param value       Size of subset
   */
  public void setNumberInSubset(int value) {
    m_NumberInSubset = value;
    reset();
  }

  /**
   * get the number of spectra to select in subset
   *
   * @return      Size of subset
   */
  public int getNumberInSubset() {
    return m_NumberInSubset;
  }

  /**
   * Tip text for this property
   *
   * @return      Description for displaying in the GUI
   */
  public String numberInSubsetTipText() {
    return "Number of spectra in subset array";
  }

  /**
   * Set the filter to apply before selection
   *
   * @param value       Filter to apply
   */
  public void setPreFilter(Filter value) {
    m_PreFilter = value;
    reset();
  }

  /**
   * Get the filter to apply before selection
   *
   * @return      Filter to apply
   */
  public Filter getPreFilter() {
    return m_PreFilter;
  }

  /**
   * Tip text for this property
   *
   * @return      Description for displaying in the GUI
   */
  public String preFilterTipText() {
    return "Pre filter to use on the spectra";
  }

  /**
   * Set the batch filter to apply before selection
   *
   * @param value       Batch filter
   */
  public void setBatchFilter(BatchFilter value) {
    m_BatchFilter = value;
    reset();
  }

  /**
   * Get the batch filter to apply before selection
   *
   * @return      Batch filter
   */
  public BatchFilter getBatchFilter() {
    return m_BatchFilter;
  }

  /**
   * Description for this property
   *
   * @return      Description for displaying in the GUI
   */
  public String batchFilterTipText() {
    return "Batch filter to apply to the spectra";
  }

  /**
   * Set whether to return the remaining spectra are returned rather than the chosen ones.
   * 
   * @param value       true if to invert
   */
  public void setInvert(boolean value) {
    m_Invert = value;
    reset();
  }

  /**
   * Get whether to return the remaining spectra are returned rather than the chosen ones.
   * 
   * @return      true if to invert
   */
  public boolean getInvert() {
    return m_Invert;
  }

  /**
   * Description for this property.
   * 
   * @return      Description for displaying in the GUI
   */
  public String invertTipText() {
    return "If enabled, the remaining spectra are returned rather than the chosen ones.";
  }

  @Override
  protected DataContainer[] processBatchData(DataContainer[] data) {
    List<Spectrum> result;
    Spectrum[] preFiltered;
    int numSpectraInitial = data.length;
    if(m_NumberInSubset == -1) {
      return data;
    }
    else {
      //copy of original un-filtered spectra
      preFiltered = new Spectrum[numSpectraInitial];
      for (int i = 0; i < numSpectraInitial; i++) {
	preFiltered[i] = (Spectrum) data[i];
      }

      //Apply the prefilter
      for (int i = 0; i < numSpectraInitial; i++) {
	data[i] = processData(data[i]);
      }

      //Apply the batch filter
      data = m_BatchFilter.batchFilter(data);

      //calculate the intersample matrix once
      double distanceInitial;
      Double[] distArraySingle;
      Double[][] distances2D = new Double[numSpectraInitial][numSpectraInitial];
      for (int i = 0; i < numSpectraInitial - 1; i++) {
	Spectrum s = (Spectrum) data[i];
	distArraySingle = new Double[numSpectraInitial];
	for (int j = i + 1; j < numSpectraInitial; j++) {
	  Spectrum s1 = (Spectrum) data[j];
	  distanceInitial = calculateDistance(s, s1);
	  distArraySingle[j] = distanceInitial;
	}
	distances2D[i] = distArraySingle;
      }

      //Keep a record of chosen and remaining indices
      ArrayList<Integer> chosen = new ArrayList<>();
      ArrayList<Integer> remaining = new ArrayList<>();
      for (int i = 0; i < numSpectraInitial; i++) {
	remaining.add(i);
      }

      //find 2 samples that are furthest apart using uniform distance
      double maxDistance = 0;
      distanceInitial = -1;

      int chosen1 = -1;
      int chosen2 = -1;
      for (int i = 0; i < numSpectraInitial - 1; i++) {
	for (int j = i + 1; j < numSpectraInitial; j++) {
	  distanceInitial = distances2D[i][j];
	  if (distanceInitial > maxDistance) {
	    maxDistance = distanceInitial;
	    chosen1 = i;
	    chosen2 = j;
	  }
	}
      }
      chosen.add(chosen1);
      chosen.add(chosen2);
      remaining.remove(remaining.indexOf(chosen1));
      remaining.remove(remaining.indexOf(chosen2));

      int indexTest;
      int indexExisting;
      double maxDistanceTest;
      double lowestDistanceSingle;
      int bestIndex;
      double thisDistance;

      //Loop through until the right amount are found.
      for (int m = 3; m <= m_NumberInSubset; m++) {
	maxDistanceTest = 0;
	bestIndex = -1;
	for (int i = 0; i < remaining.size(); i++) {
	  lowestDistanceSingle = Double.POSITIVE_INFINITY;
	  indexTest = remaining.get(i);
	  for (int j = 0; j < chosen.size(); j++) {
	    indexExisting = chosen.get(j);
	    thisDistance = distances2D[Math.min(indexTest, indexExisting)][Math.max(indexTest, indexExisting)];
	    if (thisDistance < lowestDistanceSingle) {
	      lowestDistanceSingle = thisDistance;
	    }
	  }
	  if (lowestDistanceSingle > maxDistanceTest) {
	    maxDistanceTest = lowestDistanceSingle;
	    bestIndex = indexTest;
	  }
	}
	chosen.add(bestIndex);
	remaining.remove(remaining.indexOf(bestIndex));
      }

      if (m_Invert) {
        result = new ArrayList<>();
        HashSet<Integer> chosenSet = new HashSet<>(chosen);
        for (int i = 0; i < preFiltered.length; i++) {
          if (!chosenSet.contains(i))
            result.add(preFiltered[i]);
	}
      }
      else {
	result = new ArrayList<>();
	for (int i = 0; i < chosen.size(); i++)
	  result.add(preFiltered[chosen.get(i)]);
      }
      return result.toArray(new Spectrum[0]);
    }
  }

  /**
   * Calculate the distance between any two spectra. Currently just uses euclidean distance
   *
   * @param spec1     	Spectrum1
   * @param spec2     	Spectrum2
   * @return		the distance
   */
  protected double calculateDistance(Spectrum spec1, Spectrum spec2) {
    Object[] specPoint1 = spec1.toArray();
    Object[] specPoint2 = spec2.toArray();

    double[] spec1Array = new double[spec1.size()];
    double[] spec2Array = new double[spec2.size()];
    double val1 = -1;
    double val2 = -1;

    for(int i = 0; i < spec1.size(); i++) {
      val1 = ((SpectrumPoint)specPoint1[i]).getAmplitude();
      val2 = ((SpectrumPoint)specPoint2[i]).getAmplitude();
      spec1Array[i] = val1;
      spec2Array[i] = val2;
    }

    double toReturn = -1;
    org.apache.commons.math3.ml.distance.EuclideanDistance eD = new org.apache.commons.math3.ml.distance.EuclideanDistance();
    toReturn = eD.compute(spec1Array, spec2Array);

    return toReturn;
  }

  @Override
  protected DataContainer processData(DataContainer data) {
    Spectrum spec = (Spectrum)data;
    spec = (Spectrum)(m_PreFilter.filter(spec));

    return spec;
  }
}
