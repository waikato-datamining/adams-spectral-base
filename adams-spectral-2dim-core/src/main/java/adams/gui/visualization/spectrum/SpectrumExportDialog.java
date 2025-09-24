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
 * SpectrumExportDialog.java
 * Copyright (C) 2013-2025 University of Waikato, Hamilton, New Zealand
 */
package adams.gui.visualization.spectrum;

import adams.data.io.output.DataContainerWriter;
import adams.data.io.output.SimpleSpectrumWriter;
import adams.gui.core.BaseCheckBox;
import adams.gui.dialog.AbstractDirectoryExportDialog;
import adams.gui.goe.GenericObjectEditorPanel;

import java.awt.Dialog;
import java.awt.Frame;

/**
 * Export dialog for spectra.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 */
public class SpectrumExportDialog
  extends AbstractDirectoryExportDialog<DataContainerWriter> {

  /** for serialization. */
  private static final long serialVersionUID = 6635283474671937011L;

  /** whether the timeseries should be saved in the same file, if possible. */
  protected BaseCheckBox m_CheckBoxCombine;

  /**
   * Creates a modeless dialog without a title with the specified Dialog as
   * its owner.
   *
   * @param owner	the owning dialog
   */
  public SpectrumExportDialog(Dialog owner) {
    super(owner);
  }

  /**
   * Creates a dialog with the specified owner Dialog and modality.
   *
   * @param owner	the owning dialog
   * @param modality	the type of modality
   */
  public SpectrumExportDialog(Dialog owner, ModalityType modality) {
    super(owner, modality);
  }

  /**
   * Creates a modeless dialog without a title with the specified Frame as
   * its owner.
   *
   * @param owner	the owning frame
   */
  public SpectrumExportDialog(Frame owner) {
    super(owner);
  }

  /**
   * Creates a dialog with the specified owner Frame, modality and an empty
   * title.
   *
   * @param owner	the owning frame
   * @param modal	whether the dialog is modal or not
   */
  public SpectrumExportDialog(Frame owner, boolean modal) {
    super(owner, modal);
  }

  /**
   * Initializes the widgets.
   */
  @Override
  protected void initGUI() {
    super.initGUI();
    
    m_CheckBoxCombine = new BaseCheckBox();
    m_PanelParams.addParameter("_Combine (if possible)", m_CheckBoxCombine);
  }

  /**
   * Returns the default title for the dialog.
   */
  @Override
  protected String getDefaultTitle() {
    return "Save visible spectra";
  }
  
  /**
   * Creates the GOE panel to use.
   */
  @Override
  protected GenericObjectEditorPanel createGOE() {
    return new GenericObjectEditorPanel(DataContainerWriter.class, new SimpleSpectrumWriter(), true);
  }

  /**
   * Sets whether the files should get combined if possible.
   * 
   * @param value	true if to combine if possible
   */
  public void setCombine(boolean value) {
    m_CheckBoxCombine.setSelected(value);
  }
  
  /**
   * Returns whether files should get combined if possible.
   * 
   * @return		true if combined
   */
  public boolean getCombine() {
    return m_CheckBoxCombine.isSelected();
  }
}
