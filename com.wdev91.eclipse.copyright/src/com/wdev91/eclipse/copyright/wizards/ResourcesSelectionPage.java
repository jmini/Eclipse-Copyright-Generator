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
package com.wdev91.eclipse.copyright.wizards;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;

import com.wdev91.eclipse.copyright.Messages;
import com.wdev91.eclipse.copyright.model.CopyrightSelectionItem;
import com.wdev91.eclipse.copyright.model.CopyrightSettings;

public class ResourcesSelectionPage extends WizardPage {
  public static final String DEFAULT_PAGE_NAME = "resourcesSelectionPage"; //$NON-NLS-1$

  protected CheckboxTreeViewer viewer;

  ResourcesSelectionPage() {
    super(DEFAULT_PAGE_NAME);
    setTitle(Messages.ResourcesSelectionPage_title);
    setDescription(Messages.ResourcesSelectionPage_description);
  }

  public void createControl(Composite parent) {
    Font font = parent.getFont();

    Composite top = new Composite(parent, SWT.NONE);
    top.setLayout(new GridLayout(1, false));
    top.setFont(font);

    viewer = new CheckboxTreeViewer(top, SWT.BORDER);
    viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
    viewer.setContentProvider(new SelectionContentProvider());
    viewer.setLabelProvider(new SelectionLabelProvider());
    viewer.addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        setSubtreeChecked((CopyrightSelectionItem) event.getElement(), false);
        viewer.setGrayed(event.getElement(), false);
        viewer.setSubtreeChecked(event.getElement(), event.getChecked());
        updateParentState(((CopyrightSelectionItem) event.getElement()).getParent());
        validatePage();
      }
    });

    setPageComplete(false);
    setControl(top);
  }

  public void getSelection(CopyrightSettings settings) {
    ArrayList<IFile> selection = new ArrayList<IFile>();
    for (Object o : viewer.getCheckedElements()) {
      CopyrightSelectionItem item = (CopyrightSelectionItem) o;
      if ( item.getResource() instanceof IFile ) {
        selection.add((IFile) item.getResource());
      }
    }
    settings.setFiles(selection.toArray(new IFile[selection.size()]));
  }

  private void setChecked(TreeItem[] items) {
  	for (TreeItem item : items) {
    	item.setChecked(true);
    	setChecked(item.getItems());
    }
  }

  public void setSelection(CopyrightSelectionItem[] selection) {
    viewer.setInput(new CopyrightSelectionInput(selection));
    viewer.getTree().setRedraw(false);
    viewer.expandAll();
    setChecked(viewer.getTree().getItems());
    viewer.collapseAll();
    viewer.getTree().setRedraw(true);
    validatePage();
  }

  private void setSubtreeChecked(CopyrightSelectionItem element, boolean grayed) {
    CopyrightSelectionItem[] children = element.getChildren();
    if ( children != null ) {
      for (CopyrightSelectionItem child : children) {
        viewer.setGrayChecked(child, grayed);
        setSubtreeChecked(child, grayed);
      }
    }
  }

  private void updateParentState(CopyrightSelectionItem parent) {
    if ( parent == null ) return;

    CopyrightSelectionItem[] children = parent.getChildren();
    int checked = 0;
    int grayed = 0;
    for (CopyrightSelectionItem child : children) {
      if ( viewer.getChecked(child) ) checked++;
      if ( viewer.getGrayed(child) ) grayed++;
    }
    viewer.setChecked(parent, checked + grayed > 0);
    viewer.setGrayed(parent, grayed > 0 || (checked < children.length && checked > 0));

    updateParentState(parent.getParent());
  }

  protected void validatePage() {
    setPageComplete(viewer.getCheckedElements().length > 0);
  }
}
