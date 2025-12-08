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
 * AmplitudeExpression.java
 * Copyright (C) 2015-2024 University of Waikato, Hamilton, New Zealand
 */

package adams.data.spectrumfilter;

import adams.data.filter.AbstractFilter;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.parser.GrammarSupplier;
import adams.parser.MathematicalExpression;
import adams.parser.MathematicalExpressionText;
import adams.parser.mathematicalexpression.Parser;
import adams.parser.mathematicalexpression.Scanner;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.SymbolFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

/**
 <!-- globalinfo-start -->
 * Applies the specified mathematical expression to each amplitude.<br>
 * The following symbols are available:<br>
 * - A: the current amplitude<br>
 * - W: the current wave number<br>
 * - I: the 0-based index of the wave number&#47;amplitude<br>
 * - S: the number of wave numbers in the current spectrum<br>
 * <br>
 * The following grammar is used for the expressions:<br>
 * <br>
 * expr_list ::= '=' expr_list expr_part | expr_part ;<br>
 * expr_part ::=  expr ;<br>
 * <br>
 * expr      ::=   ( expr )<br>
 * <br>
 * # data types<br>
 *               | number<br>
 *               | string<br>
 *               | boolean<br>
 *               | date<br>
 * <br>
 * # constants<br>
 *               | true<br>
 *               | false<br>
 *               | pi<br>
 *               | e<br>
 *               | now()<br>
 *               | today()<br>
 * <br>
 * # negating numeric value<br>
 *               | -expr<br>
 * <br>
 * # comparisons<br>
 *               | expr &lt; expr<br>
 *               | expr &lt;= expr<br>
 *               | expr &gt; expr<br>
 *               | expr &gt;= expr<br>
 *               | expr = expr<br>
 *               | expr != expr (or: expr &lt;&gt; expr)<br>
 * <br>
 * # boolean operations<br>
 *               | ! expr (or: not expr)<br>
 *               | expr &amp; expr (or: expr and expr)<br>
 *               | expr | expr (or: expr or expr)<br>
 *               | if[else] ( expr , expr (if true) , expr (if false) )<br>
 *               | ifmissing ( variable , expr (default value if variable is missing) )<br>
 *               | has ( variable )<br>
 *               | isNaN ( expr )<br>
 * <br>
 * # arithmetics<br>
 *               | expr + expr<br>
 *               | expr - expr<br>
 *               | expr * expr<br>
 *               | expr &#47; expr<br>
 *               | expr ^ expr (power of)<br>
 *               | expr % expr (modulo)<br>
 *               ;<br>
 * <br>
 * # numeric functions<br>
 *               | abs ( expr )<br>
 *               | sqrt ( expr )<br>
 *               | cbrt ( expr )<br>
 *               | log ( expr )<br>
 *               | log10 ( expr )<br>
 *               | exp ( expr )<br>
 *               | sin ( expr )<br>
 *               | sinh ( expr )<br>
 *               | cos ( expr )<br>
 *               | cosh ( expr )<br>
 *               | tan ( expr )<br>
 *               | tanh ( expr )<br>
 *               | atan ( expr )<br>
 *               | atan2 ( exprY , exprX )<br>
 *               | hypot ( exprX , exprY )<br>
 *               | signum ( expr )<br>
 *               | rint ( expr )<br>
 *               | floor ( expr )<br>
 *               | pow[er] ( expr , expr )<br>
 *               | ceil ( expr )<br>
 *               | min ( expr1 , expr2 )<br>
 *               | max ( expr1 , expr2 )<br>
 *               | rand () (unseeded double, 0-1)<br>
 *               | rand ( seed ) (seeded double, 0-1)<br>
 *               | randint ( bound ) (unseeded int from 0 to bound-1)<br>
 *               | randint ( seed, bound ) (seeded int from 0 to bound-1)<br>
 *               | year ( expr )<br>
 *               | month ( expr )<br>
 *               | day ( expr )<br>
 *               | hour ( expr )<br>
 *               | minute ( expr )<br>
 *               | second ( expr )<br>
 *               | weekday ( expr )<br>
 *               | weeknum ( expr )<br>
 * <br>
 * # string functions<br>
 *               | substr ( expr , start [, end] )<br>
 *               | left ( expr , len )<br>
 *               | mid ( expr , start , len )<br>
 *               | right ( expr , len )<br>
 *               | rept ( expr , count )<br>
 *               | concatenate ( expr1 , expr2 [, expr3-5] )<br>
 *               | lower[case] ( expr )<br>
 *               | upper[case] ( expr )<br>
 *               | trim ( expr )<br>
 *               | matches ( expr , regexp )<br>
 *               | trim ( expr )<br>
 *               | len[gth] ( str )<br>
 *               | find ( search , expr [, pos] ) (find 'search' in 'expr', return 1-based position)<br>
 *               | contains ( str , find ) (checks whether 'str' string contains 'find' string)<br>
 *               | replace ( str , pos , len , newstr )<br>
 *               | replaceall ( str , regexp , replace ) (applies regular expression to 'str' and replaces all matches with 'replace')<br>
 *               | substitute ( str , find , replace [, occurrences] )<br>
 *               | str ( expr )<br>
 *               | str ( expr  , numdecimals )<br>
 *               | str ( expr  , decimalformat )<br>
 *               | ext ( file_str )  (extracts extension from file)<br>
 *               | replaceext ( file_str, ext_str )  (replaces the extension of the file with the new one)<br>
 *               ;<br>
 * <br>
 * Notes:<br>
 * - Variables are either all alphanumeric and _, starting with uppercase letter (e.g., "ABc_12"),<br>
 *   any character apart from "]" enclosed by "[" and "]" (e.g., "[Hello World]") or<br>
 *   enclosed by single quotes (e.g., "'Hello World'").<br>
 * - 'start' and 'end' for function 'substr' are indices that start at 1.<br>
 * - Index 'end' for function 'substr' is excluded (like Java's 'String.substring(int,int)' method)<br>
 * - Line comments start with '#'.<br>
 * - Semi-colons (';') or commas (',') can be used as separator in the formulas,<br>
 *   e.g., 'pow(2,2)' is equivalent to 'pow(2;2)'<br>
 * - dates have to be of format 'yyyy-MM-dd' or 'yyyy-MM-dd HH:mm:ss'<br>
 * - times have to be of format 'HH:mm:ss' or 'yyyy-MM-dd HH:mm:ss'<br>
 * - the characters in square brackets in function names are optional:<br>
 *   e.g. 'len("abc")' is the same as 'length("abc")'<br>
 * - 'str' uses java.text.DecimalFormat when supplying a format string<br>
 * <br>
 * A lot of the functions have been modeled after LibreOffice:<br>
 *   https:&#47;&#47;help.libreoffice.org&#47;Calc&#47;Functions_by_Category<br>
 * <br>
 * Additional functions:<br>
 * - env(String): String<br>
 * &nbsp;&nbsp;&nbsp;First argument is the name of the environment variable to retrieve.<br>
 * &nbsp;&nbsp;&nbsp;The result is the value of the environment variable.<br>
 * <br>
 * Additional procedures:<br>
 * - println(...)<br>
 * &nbsp;&nbsp;&nbsp;One or more arguments are printed as comma-separated list to stdout.<br>
 * &nbsp;&nbsp;&nbsp;If no argument is provided, a simple line feed is output.<br>
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
 * <pre>-expression &lt;adams.parser.MathematicalExpressionText&gt; (property: expression)
 * &nbsp;&nbsp;&nbsp;The mathematical expression to evaluate.
 * &nbsp;&nbsp;&nbsp;default: A
 * </pre>
 * 
 <!-- options-end -->
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class AmplitudeExpression
  extends AbstractFilter<Spectrum>
  implements GrammarSupplier {

  /** for serialization. */
  private static final long serialVersionUID = 2319957467336388607L;

  /** the placeholder for the amplitude. */
  public final static String PLACEHOLDER_AMPLITUDE = "A";

  /** the placeholder for the wave number. */
  public final static String PLACEHOLDER_WAVENUMBER = "W";

  /** the placeholder for the index. */
  public final static String PLACEHOLDER_INDEX = "I";

  /** the placeholder for the total number of wave numbers. */
  public final static String PLACEHOLDER_SIZE = "S";

  /** the mathematical expression to evaluate. */
  protected MathematicalExpressionText m_Expression;

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  public String globalInfo() {
    return
      "Applies the specified mathematical expression to each amplitude.\n"
	+ "The following symbols are available:\n"
	+ "- A: the current amplitude\n"
	+ "- W: the current wave number\n"
	+ "- I: the 0-based index of the wave number/amplitude\n"
	+ "- S: the number of wave numbers in the current spectrum\n"
	+ "\n"
	+ "The following grammar is used for the expressions:\n\n"
	+ getGrammar();
  }

  /**
   * Returns a string representation of the grammar.
   *
   * @return		the grammar, null if not available
   */
  public String getGrammar() {
    return new MathematicalExpression().getGrammar();
  }

  /**
   * Adds options to the internal list of options.
   */
  @Override
  public void defineOptions() {
    super.defineOptions();

    m_OptionManager.add(
      "expression", "expression",
      new MathematicalExpressionText(PLACEHOLDER_AMPLITUDE));
  }

  /**
   * Sets the mathematical expression to evaluate.
   *
   * @param value	the expression
   */
  public void setExpression(MathematicalExpressionText value) {
    m_Expression = value;
    reset();
  }

  /**
   * Returns the mathematical expression to evaluate.
   *
   * @return		the expression
   */
  public MathematicalExpressionText getExpression() {
    return m_Expression;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the GUI or for listing the options.
   */
  public String expressionTipText() {
    return "The mathematical expression to evaluate.";
  }

  /**
   * Performs the actual filtering.
   *
   * @param data	the data to filter
   * @return		the filtered data
   */
  protected Spectrum processData(Spectrum data) {
    Spectrum			result;
    String			exp;
    List<SpectrumPoint>		points;
    List<SpectrumPoint>		pointsNew;
    int				i;
    HashMap 			symbols;
    SymbolFactory 		sf;
    ByteArrayInputStream 	parserInput;
    Parser 			parser;
    Double			newAmp;

    result = data.getHeader();

    exp = m_Expression.getValue();
    exp = getOptionManager().getVariables().expand(exp);
    try {
      points    = data.toList();
      pointsNew = new ArrayList<>();
      symbols   = new HashMap();
      symbols.put(PLACEHOLDER_SIZE, (double) data.size());
      sf          = new ComplexSymbolFactory();
      parserInput = new ByteArrayInputStream(exp.getBytes());
      for (i = 0; i < points.size(); i++) {
	parserInput.reset();
	parser = new Parser(new Scanner(parserInput, sf), sf);
	symbols.put(PLACEHOLDER_INDEX, (double) i);
	symbols.put(PLACEHOLDER_WAVENUMBER, (double) points.get(i).getWaveNumber());
	symbols.put(PLACEHOLDER_AMPLITUDE, (double) points.get(i).getAmplitude());
	parser.setSymbols(symbols);
	parser.parse();
	newAmp = parser.getResult();
	if (newAmp != null)
	  pointsNew.add(new SpectrumPoint(points.get(i).getWaveNumber(), newAmp.floatValue()));
      }
      result.replaceAll(pointsNew, true);
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, "Failed to apply expression: " + exp);
    }

    return result;
  }
}
