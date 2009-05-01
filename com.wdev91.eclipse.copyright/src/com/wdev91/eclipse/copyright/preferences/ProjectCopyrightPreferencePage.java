/*******************************************************************************
 * Copyright (c) 2008-2009 Eric Wuillai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Eric Wuillai - initial API and implementation
 ******************************************************************************/
package com.wdev91.eclipse.copyright.preferences;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import com.wdev91.eclipse.copyright.Messages;
import com.wdev91.eclipse.copyright.model.CopyrightManager;
import com.wdev91.eclipse.copyright.model.HeaderFormat;
import com.wdev91.eclipse.copyright.model.ProjectPreferences;

public class ProjectCopyrightPreferencePage extends PropertyPage {
	protected IProject project;
	protected Button enableButton;
  protected Text headerText;
  protected FormatsPanel formats;
  protected TabFolder tab;
  protected boolean firstEnabled = true;

  @Override
  protected Control createContents(Composite parent) {
    Font font = parent.getFont();

    Composite top = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(1, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    top.setLayout(layout);
    top.setFont(font);

    enableButton = new Button(top, SWT.CHECK);
    enableButton.setText(Messages.ProjectCopyrightPreferencePage_checkboxEnable);
    new Label(top, SWT.SEPARATOR | SWT.HORIZONTAL)
        .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    tab = new TabFolder(top, SWT.TOP);
    tab.setLayoutData(new GridData(GridData.FILL_BOTH));

    TabItem headerTab = new TabItem(tab, SWT.NONE);
    headerTab.setText(Messages.CopyrightPreferencePage_labelHeader);
    headerText = new Text(tab, SWT.BORDER | SWT.MULTI | SWT.WRAP
                          | SWT.H_SCROLL | SWT.V_SCROLL);
    headerTab.setControl(headerText);

    TabItem formatsTab = new TabItem(tab, SWT.NONE);
    formatsTab.setText(Messages.ProjectCopyrightPreferencePage_tabFormats);
    formats = new FormatsPanel(tab, SWT.NONE);
    formatsTab.setControl(formats);

    enableButton.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
      }

      public void widgetSelected(SelectionEvent e) {
        boolean enabled = enableButton.getSelection();
        if ( firstEnabled && enabled ) {
          if ( MessageDialog.openQuestion(getShell(),
                                          Messages.ProjectCopyrightPreferencePage_tabFormats,
                                          Messages.ProjectCopyrightPreferencePage_msgInitialize) ) {
            formats.setFormats(CopyrightManager.getHeadersFormats());
          }
          firstEnabled = false;
        }
        doEnable(enabled);
      }
    });

    ProjectPreferences preferences = CopyrightManager.getProjectPreferences(project);
    if ( preferences != null && preferences != ProjectPreferences.NO_PREFS ) {
      enableButton.setSelection(true);
      String text = preferences.getHeaderText();
      if ( text != null ) {
        headerText.setText(text);
      }
      Map<String, HeaderFormat> hf = preferences.getFormats();
      if ( formats != null ) {
        formats.setFormats(hf.values());
      }
      doEnable(true);
    } else {
      doEnable(false);
    }

    return top;
  }

  private void doEnable(boolean enabled) {
    headerText.setEnabled(enabled);
    formats.setEnabled(enabled);
  }

  @Override
  protected void performDefaults() {
    enableButton.setSelection(false);
    doEnable(false);
    super.performDefaults();
  }

  @Override
  public boolean performOk() {
    Collection<HeaderFormat> headerFormats = formats.getFormats();
    for (HeaderFormat format : headerFormats) {
      if ( format.getBeginLine().trim().length() == 0
           || format.getEndLine().trim().length() == 0 ) {
        MessageDialog.openError(getShell(), Messages.ProjectCopyrightPreferencePage_errTitle,
                                NLS.bind(Messages.HeadersPreferencePage_errorInvalidHeaderFormat,
                                    Platform.getContentTypeManager()
                                            .getContentType(format.getContentId())
                                            .getName()));
        return false;
      }
    }
    ProjectPreferences preferences = enableButton.getSelection()
                                     ? new ProjectPreferences(headerText.getText(),
                                                              headerFormats)
                                     : null;
    try {
      CopyrightManager.saveProjectPreferences(project, preferences);
      return super.performOk();
    } catch (IOException e) {
      MessageDialog.openError(getShell(), Messages.ProjectCopyrightPreferencePage_errTitle,
                              NLS.bind(Messages.ProjectCopyrightPreferencePage_errmsgOnSave, e.getMessage()));
      return false;
    }
  }

	@Override
	public void setElement(IAdaptable element) {
		super.setElement(element);
		project = (IProject) getElement().getAdapter(IResource.class);
	}
}
