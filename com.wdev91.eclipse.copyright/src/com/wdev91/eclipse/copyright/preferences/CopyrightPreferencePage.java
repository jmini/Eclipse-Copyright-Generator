/*******************************************************************************
 * Copyright (c) 2008 Eric Wuillai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Eric Wuillai - initial API and implementation
 ******************************************************************************/
package com.wdev91.eclipse.copyright.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.wdev91.eclipse.copyright.Activator;
import com.wdev91.eclipse.copyright.Constants;
import com.wdev91.eclipse.copyright.Messages;
import com.wdev91.eclipse.copyright.model.Copyright;
import com.wdev91.eclipse.copyright.viewers.CopyrightContentProvider;
import com.wdev91.eclipse.copyright.viewers.CopyrightLabelProvider;
import com.wdev91.eclipse.copyright.viewers.CopyrightsInput;
import com.wdev91.eclipse.copyright.viewers.CopyrightsComparator;

public class CopyrightPreferencePage extends PreferencePage
    implements IWorkbenchPreferencePage {
  private static final int LIST_LINES_NUMBER = 10;

  protected Text ownerText;
  protected ListViewer copyrightsList;
  protected Text headerText;
  protected Text licenseFile;
  protected Text licenseText;
  protected Button addButton;
  protected Button modifyButton;
  protected Button deleteButton;
  protected TabFolder tab;

  protected CopyrightsInput input;
  protected Copyright currentSelection = null;

  public CopyrightPreferencePage() {
    super();
    noDefaultAndApplyButton();
  }

  /**
   * Adds a new Copyright. An input dialog is opened to get the copyright label.
   */
  protected void addCopyright() {
    InputDialog dialog = new InputDialog(this.getShell(),
                                         Messages.CopyrightPreferencePage_inputTitle,
                                         Messages.CopyrightPreferencePage_inputLabel,
                                         null,
                                         null);
    if ( dialog.open() == InputDialog.OK ) {
      Copyright c = new Copyright(dialog.getValue());
      c.setHeaderText(Constants.EMPTY_STRING);
      c.setLicenseFilename(Constants.EMPTY_STRING);
      c.setLicenseText(Constants.EMPTY_STRING);
      if ( input.addCopyright(c) ) {
        copyrightsList.add(c);
        copyrightsList.setSelection(new StructuredSelection(c));
        tab.setSelection(0);
        headerText.setFocus();
      }
    }
  }

  @Override
  protected Control createContents(Composite parent) {
    Font font = parent.getFont();
    FontData[] fontData = font.getFontData();
    GridData data;
    int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);

    input = new CopyrightsInput(false);

    Composite top = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(3, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    top.setLayout(layout);
    top.setFont(font);

    Label l1 = new Label(top, SWT.NONE);
    l1.setText(Messages.CopyrightPreferencePage_labelOwner);
    ownerText = new Text(top, SWT.BORDER);
    data = new GridData();
    data.horizontalSpan = 2;
    data.widthHint = 200;
    ownerText.setLayoutData(data);

    Label l2 = new Label(top, SWT.NONE);
    l2.setText(Messages.CopyrightPreferencePage_labelLicenses);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 3;
    l2.setLayoutData(data);
    l2.setFont(font);

    List list = new List(top, SWT.BORDER);
    data = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
    data.heightHint = (fontData.length > 0 ? fontData[0].getHeight() : 10) * LIST_LINES_NUMBER;
    data.horizontalSpan = 2;
    data.verticalSpan = 3;
    list.setLayoutData(data);
    list.setFont(font);
    copyrightsList = new ListViewer(list);
    copyrightsList.setContentProvider(new CopyrightContentProvider());
    copyrightsList.setLabelProvider(new CopyrightLabelProvider());
    copyrightsList.setComparator(new CopyrightsComparator());
    copyrightsList.setInput(input);
    copyrightsList.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        updateContent();
      }
    });

    addButton = new Button(top, SWT.PUSH);
    addButton.setText(Messages.CopyrightPreferencePage_buttonAdd);
    Dialog.applyDialogFont(addButton);
    data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    data.widthHint = Math.max(widthHint, addButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
    addButton.setLayoutData(data);

    modifyButton = new Button(top, SWT.PUSH);
    modifyButton.setText(Messages.CopyrightPreferencePage_buttonModify);
    data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    data.widthHint = Math.max(widthHint, modifyButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
    modifyButton.setLayoutData(data);

    deleteButton = new Button(top, SWT.PUSH);
    deleteButton.setText(Messages.CopyrightPreferencePage_buttonDelete);
    data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
    data.widthHint = Math.max(widthHint, deleteButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
    deleteButton.setLayoutData(data);

    tab = new TabFolder(top, SWT.TOP);
    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 3;
    tab.setLayoutData(data);

    TabItem headerTab = new TabItem(tab, SWT.NONE);
    headerTab.setText(Messages.CopyrightPreferencePage_labelHeader);
    headerText = new Text(tab, SWT.BORDER | SWT.MULTI | SWT.WRAP
                          | SWT.H_SCROLL | SWT.V_SCROLL);
    headerTab.setControl(headerText);

    TabItem licenseTab = new TabItem(tab, SWT.NONE);
    licenseTab.setText(Messages.CopyrightPreferencePage_labelLicenseFile);
    Composite licenseTabContent = new Composite(tab, SWT.NONE);
    licenseTabContent.setLayout(new GridLayout(2, false));
    Label l3 = new Label(licenseTabContent, SWT.NONE);
    l3.setText(Messages.CopyrightPreferencePage_labelFilename);
    licenseFile = new Text(licenseTabContent, SWT.BORDER);
    licenseFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    Label l4 = new Label(licenseTabContent, SWT.NONE);
    l4.setText(Messages.CopyrightPreferencePage_labelFilecontent);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    l4.setLayoutData(data);
    licenseText = new Text(licenseTabContent, SWT.BORDER | SWT.MULTI | SWT.WRAP
                           | SWT.H_SCROLL | SWT.V_SCROLL);
    data = new GridData(GridData.FILL_BOTH);
    data.horizontalSpan = 2;
    licenseText.setLayoutData(data);
    licenseTab.setControl(licenseTabContent);

    SelectionListener listener = new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
      }

      public void widgetSelected(SelectionEvent e) {
        if ( e.widget == addButton ) {
          addCopyright();
        } else if ( e.widget == modifyButton ) {
          updateCopyright();
        } else if ( e.widget == deleteButton ) {
          deleteCopyright();
        }
      }
    };
    addButton.addSelectionListener(listener);
    modifyButton.addSelectionListener(listener);
    deleteButton.addSelectionListener(listener);

    ownerText.setText(getPreferenceStore().getString(Constants.PREFERENCES_OWNER));
    updateContent();
    return top;
  }

  /**
   * Deletes the currently selected Copyright.
   */
  protected void deleteCopyright() {
    Copyright c = getSelection();
    if ( c != null
         && MessageDialog.openQuestion(this.getShell(),
                Messages.CopyrightPreferencePage_titleDelete,
                NLS.bind(Messages.CopyrightPreferencePage_msgConfirmDelete, c.getLabel())) ) {
      if ( input.deleteCopyright(c) ) {
        copyrightsList.remove(c);
        updateContent();
      }
    }
  }

  /**
   * Returns the current Copyright object selected in the list.
   * 
   * @return Selected Copyright, or null if none.
   */
  protected Copyright getSelection() {
    StructuredSelection selection = (StructuredSelection) copyrightsList.getSelection();
    return (Copyright) selection.getFirstElement();
  }

  /*
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  public void init(IWorkbench workbench) {
    setPreferenceStore(Activator.getDefault().getPreferenceStore());
  }

  @Override
  public boolean performOk() {
    getPreferenceStore().setValue(Constants.PREFERENCES_OWNER, ownerText.getText().trim());
    updateContent();
    input.save();
    return true;
  }

  /**
   * Updates the label of the currently selected Copyright.
   */
  protected void updateCopyright() {
    Copyright c = getSelection();
    if ( c != null ) {
      InputDialog dialog = new InputDialog(this.getShell(),
                                           Messages.CopyrightPreferencePage_titleModify,
                                           Messages.CopyrightPreferencePage_inputLabel,
                                           c.getLabel(),
                                           null);
      if ( dialog.open() == InputDialog.OK ) {
        c.setLabel(dialog.getValue());
        copyrightsList.refresh(c);
      }
    }
  }

  /**
   * Update the Text widgets content and the enabled status of buttons and Text
   * after the change of the current selection in the copyrights list.
   */
  private void updateContent() {
    Copyright c = getSelection();
    if ( c != null ) {
      if ( currentSelection != null ) {
        currentSelection.setHeaderText(headerText.getText());
        currentSelection.setLicenseFilename(licenseFile.getText());
        currentSelection.setLicenseText(licenseText.getText());
      }
      currentSelection = c;
      headerText.setText(currentSelection.getHeaderText());
      licenseFile.setText(currentSelection.getLicenseFilename());
      licenseText.setText(currentSelection.getLicenseText());
    } else {
      headerText.setText(Constants.EMPTY_STRING);
    }
    modifyButton.setEnabled(c != null);
    deleteButton.setEnabled(c != null);
    headerText.setEnabled(c != null);
    licenseFile.setEnabled(c != null);
    licenseText.setEnabled(c != null);
  }
}
