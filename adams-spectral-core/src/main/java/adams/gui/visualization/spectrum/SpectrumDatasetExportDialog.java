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
 * SpectrumDatasetExportDialog.java
 * Copyright (C) 2016 University of Waikato, Hamilton, New Zealand
 */
package adams.gui.visualization.spectrum;

import adams.data.instances.AbstractSpectrumInstanceGenerator;
import adams.data.instances.SimpleInstanceGenerator;
import adams.gui.dialog.AbstractFileExportDialog;
import adams.gui.goe.GenericObjectEditorPanel;
import weka.core.converters.AbstractFileSaver;
import weka.core.converters.ArffSaver;

import java.awt.Dialog;
import java.awt.Frame;

/**
 * Export dialog for spectra to a dataset.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 2019 $
 */
public class SpectrumDatasetExportDialog
  extends AbstractFileExportDialog<AbstractFileSaver> {

  /** for serialization. */
  private static final long serialVersionUID = 6635283474671937011L;

  /** the panel for the instance generator scheme to use. */
  protected GenericObjectEditorPanel m_PanelGOEGenerator;

  /**
   * Creates a modeless dialog without a title with the specified Dialog as
   * its owner.
   *
   * @param owner	the owning dialog
   */
  public SpectrumDatasetExportDialog(Dialog owner) {
    super(owner);
  }

  /**
   * Creates a dialog with the specified owner Dialog and modality.
   *
   * @param owner	the owning dialog
   * @param modality	the type of modality
   */
  public SpectrumDatasetExportDialog(Dialog owner, ModalityType modality) {
    super(owner, modality);
  }

  /**
   * Creates a modeless dialog without a title with the specified Frame as
   * its owner.
   *
   * @param owner	the owning frame
   */
  public SpectrumDatasetExportDialog(Frame owner) {
    super(owner);
  }

  /**
   * Creates a dialog with the specified owner Frame, modality and an empty
   * title.
   *
   * @param owner	the owning frame
   * @param modal	whether the dialog is modal or not
   */
  public SpectrumDatasetExportDialog(Frame owner, boolean modal) {
    super(owner, modal);
  }

  /**
   * Initializes the widgets.
   */
  @Override
  protected void initGUI() {
    super.initGUI();

    m_PanelGOEGenerator = new GenericObjectEditorPanel(AbstractSpectrumInstanceGenerator.class, new SimpleInstanceGenerator());
    m_PanelParams.addParameter(0, "_Generator", m_PanelGOEGenerator);
  }

  /**
   * Returns the default title for the dialog.
   */
  @Override
  protected String getDefaultTitle() {
    return "Export visible spectra";
  }
  
  /**
   * Creates the GOE panel to use.
   */
  @Override
  protected GenericObjectEditorPanel createGOE() {
    return new GenericObjectEditorPanel(AbstractFileSaver.class, new ArffSaver(), true);
  }

  /**
   * Sets the generator to use.
   * 
   * @param value	the generator
   */
  public void setGenerator(AbstractSpectrumInstanceGenerator value) {
    m_PanelGOEGenerator.setCurrent(value);
  }
  
  /**
   * Returns the generator to use.
   * 
   * @return		the generator
   */
  public AbstractSpectrumInstanceGenerator getGenerator() {
    return (AbstractSpectrumInstanceGenerator) m_PanelGOEGenerator.getCurrent();
  }
}
