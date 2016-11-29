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

/**
 * MultiSpectrumToSpectraTest.java
 * Copyright (C) 2014 University of Waikato, Hamilton, New Zealand
 */

package adams.data.conversion;

import adams.core.Utils;
import adams.core.base.BaseRegExp;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.spectrum.MultiSpectrum;
import adams.data.spectrum.Spectrum;
import adams.env.Environment;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.io.BufferedWriter;
import java.io.StringWriter;

/**
 * Tests the MultiSpectrumToSpectra conversion.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 1487 $
 */
public class MultiSpectrumToSpectraTest
  extends AbstractSpectralConversionTestCase {

  /**
   * Constructs the test case. Called by subclasses.
   *
   * @param name 	the name of the test
   */
  public MultiSpectrumToSpectraTest(String name) {
    super(name);
  }

  /**
   * Turns the data object into a useful string representation.
   *
   * @param data	the object to convert
   * @return		the string representation
   */
  @Override
  protected String toString(Object data) {
    String		result;
    StringBuilder	combined;
    StringWriter	swriter;
    BufferedWriter	bwriter;
    Spectrum[]		specs;
    String[]		lines;

    if (data instanceof Spectrum[]) {
      combined = new StringBuilder();
      specs    = (Spectrum[]) data;
      for (Spectrum sp: specs) {
	swriter = new StringWriter();
	bwriter = new BufferedWriter(swriter);
	sp.write(bwriter, true);
	lines    = Utils.split(swriter.toString(), "\n");
	lines[0] = "";
	combined.append(Utils.flatten(lines, "\n"));
	combined.append("\n");
      }
      result = combined.toString();
    }
    else {
      result = super.toString(data);
    }
    
    return result;
  }
  
  /**
   * Returns the input data to use in the regression test.
   *
   * @return		the objects
   */
  @Override
  protected Object[] getRegressionInput() {
    MultiSpectrum[]	result;
    Spectrum		sp;
    
    result    = new MultiSpectrum[3];
    
    result[0] = new MultiSpectrum();
    sp = new Spectrum();
    sp.setID("1");
    result[0].add(sp);
    sp = new Spectrum();
    sp.setID("2");
    result[0].add(sp);
    sp = new Spectrum();
    sp.setID("3");
    result[0].add(sp);

    result[1] = new MultiSpectrum();
    sp = new Spectrum();
    sp.setID("1");
    result[1].add(sp);
    sp = new Spectrum();
    sp.setID("1");
    result[1].add(sp);
    sp = new Spectrum();
    sp.setID("1");
    result[1].add(sp);
    
    result[2] = new MultiSpectrum();
    result[2].getReport().addField(new Field("A", DataType.NUMERIC));
    result[2].getReport().addField(new Field("AB", DataType.NUMERIC));
    result[2].getReport().addField(new Field("B", DataType.STRING));
    result[2].getReport().addField(new Field("BB", DataType.STRING));
    result[2].getReport().setNumericValue("A", 1.0);
    result[2].getReport().setNumericValue("AB", 2.0);
    result[2].getReport().setStringValue("B", "bbbb");
    result[2].getReport().setStringValue("BB", "BBBB");
    sp = new Spectrum();
    sp.setID("1");
    result[2].add(sp);
    sp = new Spectrum();
    sp.setID("2");
    result[2].add(sp);
    sp = new Spectrum();
    sp.setID("3");
    result[2].add(sp);
    
    return result;
  }

  /**
   * Returns the setups to use in the regression test.
   *
   * @return		the setups
   */
  @Override
  protected Conversion[] getRegressionSetups() {
    MultiSpectrumToSpectra[]	result;

    result    = new MultiSpectrumToSpectra[4];
    result[0] = new MultiSpectrumToSpectra();
    result[1] = new MultiSpectrumToSpectra();
    result[2] = new MultiSpectrumToSpectra();
    result[2].setTransferReport(true);
    result[3] = new MultiSpectrumToSpectra();
    result[3].setTransferReport(true);
    result[3].setTransferPrefix("Multi-");
    result[3].setTransferRegExp(new BaseRegExp("A.*"));

    return result;
  }

  /**
   * Returns the ignored line indices to use in the regression test.
   *
   * @return		the setups
   */
  @Override
  protected int[] getRegressionIgnoredLineIndices() {
    return new int[0];
  }

  /**
   * Returns the test suite.
   *
   * @return		the suite
   */
  public static Test suite() {
    return new TestSuite(MultiSpectrumToSpectraTest.class);
  }

  /**
   * Runs the test from commandline.
   *
   * @param args	ignored
   */
  public static void main(String[] args) {
    Environment.setEnvironmentClass(Environment.class);
    runTest(suite());
  }
}
