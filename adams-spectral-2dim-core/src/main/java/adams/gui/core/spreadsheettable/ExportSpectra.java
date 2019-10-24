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
 * ExportSpectra.java
 * Copyright (C) 2019 University of Waikato, Hamilton, NZ
 */

package adams.gui.core.spreadsheettable;

import adams.core.Properties;
import adams.core.Utils;
import adams.core.base.BaseRegExp;
import adams.core.io.PlaceholderDirectory;
import adams.core.io.PlaceholderFile;
import adams.data.io.output.AbstractSpectrumWriter;
import adams.data.io.output.SimpleSpectrumWriter;
import adams.data.report.DataType;
import adams.data.report.Field;
import adams.data.spectrum.Spectrum;
import adams.data.spectrum.SpectrumPoint;
import adams.data.spreadsheet.Row;
import adams.data.spreadsheet.SpreadSheet;
import adams.data.spreadsheet.SpreadSheetColumnIndex;
import adams.data.spreadsheet.SpreadSheetColumnRange;
import adams.gui.core.GUIHelper;
import adams.gui.core.PropertiesParameterPanel;
import adams.gui.core.PropertiesParameterPanel.PropertyType;
import adams.gui.core.SpreadSheetTable;
import adams.gui.core.spreadsheettable.SpreadSheetTablePopupMenuItemHelper.TableState;
import adams.gui.dialog.PropertiesParameterDialog;
import adams.gui.goe.GenericObjectEditorPanel;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.awt.Dialog.ModalityType;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Exports the selected rows as spectra.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class ExportSpectra
  extends AbstractProcessSelectedRows
  implements ProcessRow {

  private static final long serialVersionUID = 3101728458818516005L;

  public static final String KEY_SAMPLEID = "sampleid";

  public static final String KEY_COLUMNS = "columns";

  public static final String KEY_EXTRACTWAVENOS = "extract wavenumbers";

  public static final String KEY_WAVENOREGEXP = "wavenumber regexp";

  public static final String KEY_WAVENOGROUP = "wavenumber group";

  public static final String KEY_WRITER = "writer";

  public static final String KEY_DIRECTORY = "directory";

  /**
   * Returns a string describing the object.
   *
   * @return 			a description suitable for displaying in the gui
   */
  @Override
  public String globalInfo() {
    return "Allows the user to export the selected rows as spectra.";
  }

  /**
   * Returns the default name for the menu item.
   *
   * @return            the name
   */
  protected String getDefaultMenuItem() {
    return "Export spectra...";
  }

  /**
   * Returns the minimum number of rows that the plugin requires.
   *
   * @return		the minimum
   */
  public int minNumRows() {
    return 1;
  }

  /**
   * Returns the maximum number of rows that the plugin requires.
   *
   * @return		the maximum, -1 for none
   */
  public int maxNumRows() {
    return -1;
  }

  /**
   * Prompts the user for parameters.
   *
   * @param table	the table this is for
   * @return		the parameters, null if cancelled dialog
   */
  protected Properties promptParameters(SpreadSheetTable table) {
    PropertiesParameterDialog 	dialog;
    PropertiesParameterPanel 	panel;
    Properties 			last;

    if (GUIHelper.getParentDialog(table) != null)
      dialog = new PropertiesParameterDialog(GUIHelper.getParentDialog(table), ModalityType.DOCUMENT_MODAL);
    else
      dialog = new PropertiesParameterDialog(GUIHelper.getParentFrame(table), true);
    panel = dialog.getPropertiesParameterPanel();
    panel.addPropertyType(KEY_SAMPLEID, PropertyType.INDEX);
    panel.setLabel(KEY_SAMPLEID, "Sample ID");
    panel.setHelp(KEY_SAMPLEID, "The column index for the sample ID");
    panel.addPropertyType(KEY_COLUMNS, PropertyType.RANGE);
    panel.setLabel(KEY_COLUMNS, "Amplitudes");
    panel.setHelp(KEY_COLUMNS, "The columns representing amplitudes");
    panel.addPropertyType(KEY_EXTRACTWAVENOS, PropertyType.BOOLEAN);
    panel.setLabel(KEY_EXTRACTWAVENOS, "Extract wave nos from column names?");
    panel.setHelp(KEY_EXTRACTWAVENOS, "If wave numbers are stored in the columns, you can enable extraction");
    panel.addPropertyType(KEY_WAVENOREGEXP, PropertyType.REGEXP);
    panel.setLabel(KEY_WAVENOREGEXP, "Wave no regexp");
    panel.setHelp(KEY_WAVENOREGEXP, "The regular expression to identify the group representing the wave number in a column name");
    panel.addPropertyType(KEY_WAVENOGROUP, PropertyType.INTEGER);
    panel.setLabel(KEY_WAVENOGROUP, "Wave no regexp group");
    panel.setHelp(KEY_WAVENOGROUP, "The regexp group that represents the wave number in a column name");
    panel.addPropertyType(KEY_WRITER, PropertyType.OBJECT_EDITOR);
    panel.setLabel(KEY_WRITER, "Spectrum writer");
    panel.setHelp(KEY_WRITER, "The spectrum writer to use for export");
    panel.setChooser(KEY_WRITER, new GenericObjectEditorPanel(AbstractSpectrumWriter.class, new SimpleSpectrumWriter(), true));
    panel.addPropertyType(KEY_DIRECTORY, PropertyType.DIRECTORY_ABSOLUTE);
    panel.setLabel(KEY_DIRECTORY, "Export directory");
    panel.setHelp(KEY_DIRECTORY, "The directory to export the spectra to");
    panel.setPropertyOrder(new String[]{KEY_SAMPLEID, KEY_COLUMNS, KEY_EXTRACTWAVENOS, KEY_WAVENOREGEXP, KEY_WAVENOGROUP, KEY_WRITER, KEY_DIRECTORY});
    last = new Properties();
    last.setProperty(KEY_SAMPLEID, "");
    last.setProperty(KEY_COLUMNS, SpreadSheetColumnRange.ALL);
    last.setBoolean(KEY_EXTRACTWAVENOS, false);
    last.setProperty(KEY_WAVENOREGEXP, "(.*)");
    last.setInteger(KEY_WAVENOGROUP, 1);
    last.setObject(KEY_WRITER, new SimpleSpectrumWriter());
    last.setProperty(KEY_DIRECTORY, ".");
    dialog.setProperties(last);
    last = (Properties) table.getLastSetup(getClass(), true, false);
    if (last != null)
      dialog.setProperties(last);
    dialog.setTitle(getMenuItem());
    dialog.pack();
    dialog.setLocationRelativeTo(table.getParent());
    dialog.setVisible(true);
    if (dialog.getOption() != PropertiesParameterDialog.APPROVE_OPTION)
      return null;

    return dialog.getProperties();
  }

  /**
   * Processes the specified row.
   *
   * @param state	the table state
   * @return		true if successful
   */
  @Override
  protected boolean doProcessSelectedRows(TableState state) {
    Properties 			last;
    AbstractSpectrumWriter 	writer;
    SpreadSheetColumnRange	range;
    SpreadSheetColumnIndex 	idIndex;
    PlaceholderDirectory	dir;
    int[]			rows;
    int[] cols;
    TIntSet colsSet;
    boolean			extract;
    BaseRegExp			regexp;
    Integer			group;
    float			waveno;
    String			wavenoStr;
    Map<String,Float> 		wavenos;
    SpreadSheet 		data;
    int 			rowIdx;
    Row 			row;
    Spectrum			spec;
    SpectrumPoint		point;
    Field			field;
    Object			value;
    int				id;
    int				i;
    PlaceholderFile		output;
    boolean[]			numeric;

    rows = Utils.adjustIndices(state.actRows, 1);

    last = promptParameters(state.table);
    if (last == null)
      return false;

    writer = last.getObject(KEY_WRITER, AbstractSpectrumWriter.class);
    if (writer == null) {
      GUIHelper.showErrorMessage(
	GUIHelper.getParentComponent(state.table), "Failed to instantiate spectrum writer!");
      return false;
    }
    state.table.addLastSetup(getClass(), true, false, last);
    data       = state.table.toSpreadSheet(state.range, true);
    range      = new SpreadSheetColumnRange(last.getProperty(KEY_COLUMNS));
    range.setData(data);
    cols       = range.getIntIndices();
    colsSet    = new TIntHashSet(cols);
    idIndex    = new SpreadSheetColumnIndex(last.getProperty(KEY_SAMPLEID));
    idIndex.setData(data);
    id         = idIndex.getIntIndex();
    dir        = new PlaceholderDirectory(last.getProperty(KEY_DIRECTORY));
    extract    = last.getBoolean(KEY_EXTRACTWAVENOS, false);
    regexp     = new BaseRegExp(last.getProperty(KEY_WAVENOREGEXP, "(.*)"));
    group      = last.getInteger(KEY_WAVENOGROUP, 1);
    wavenos    = new HashMap<>();
    numeric    = new boolean[data.getColumnCount()];
    for (i = 0; i < data.getColumnCount(); i++)
      numeric[i] = (colsSet.contains(i)) || data.isNumeric(i);
    for (rowIdx = 0; rowIdx < data.getRowCount(); rowIdx++) {
      row = data.getRow(rowIdx);
      spec = new Spectrum();

      // sample id
      if (id > -1) {
	if (row.hasCell(id) && !row.getCell(id).isMissing())
	  spec.setID(row.getCell(id).getContent());
	else
	  spec.setID("" + rowIdx);
      }
      else {
	spec.setID("" + rowIdx);
      }

      // amplitudes and report
      for (i = 0; i < data.getColumnCount(); i++) {
	// skip sample id
	if (i == id)
	  continue;

	// amplitude or sampledata?
	if (row.hasCell(i) && !row.getCell(i).isMissing()) {
	  if (colsSet.contains(i)) {
	    if (extract) {
	      if (!wavenos.containsKey(data.getColumnName(i))) {
		try {
		  wavenoStr = data.getColumnName(i).replaceAll(regexp.getValue(), "$" + group);
		  waveno = Float.parseFloat(wavenoStr);
		}
		catch (Exception e) {
		  getLogger().log(
		    Level.SEVERE,
		    "Failed to extract wave number from column name '"
		      + data.getColumnName(i) + "' using regexp '" + regexp
		      + "' and group " + group + "!", e);
		  waveno = spec.size();
		}
		wavenos.put(data.getColumnName(i), waveno);
	      }
	      else {
		waveno = wavenos.get(data.getColumnName(i));
	      }
	    }
	    else {
	      waveno = spec.size();
	    }
	    point = new SpectrumPoint(waveno, row.getCell(i).toDouble().floatValue());
	    spec.add(point);
	  }
	  else {
	    if (numeric[i]) {
	      field = new Field(data.getColumnName(i), DataType.NUMERIC);
	      value = row.getCell(i).toDouble();
	    }
	    else {
	      field = new Field(data.getColumnName(i), DataType.STRING);
	      value = row.getCell(i).getContent();
	    }
	    spec.getReport().addField(field);
	    spec.getReport().setValue(field, value);
	  }
	}
      }

      // save spectrum
      output = new PlaceholderFile(dir.getAbsolutePath() + File.separator + spec.getID() + "." + writer.getDefaultFormatExtension());
      writer.setOutput(output);
      if (!writer.write(spec)) {
	GUIHelper.showErrorMessage(
	  GUIHelper.getParentComponent(state.table), "Failed to write spectrum from row #" + rows[rowIdx] + " to: " + output);
	return false;
      }
    }

    GUIHelper.showInformationMessage(
      GUIHelper.getParentComponent(state.table),
      "Exported " + data.getRowCount() + " spectra to " + dir.getAbsolutePath() + "!");

    return true;
  }

  /**
   * Processes the specified row.
   *
   * @param state	the table state
   * @return		true if successful
   */
  public boolean processRow(TableState state) {
    return processSelectedRows(state);
  }
}
