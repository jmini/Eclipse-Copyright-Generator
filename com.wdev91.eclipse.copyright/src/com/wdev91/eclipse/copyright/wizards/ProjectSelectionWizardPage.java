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
package com.wdev91.eclipse.copyright.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.wdev91.eclipse.copyright.Messages;
import com.wdev91.eclipse.copyright.model.CopyrightSettings;

public class ProjectSelectionWizardPage extends WizardPage {
  public static final String DEFAULT_PAGE_NAME = "projectSelectionPage"; //$NON-NLS-1$

  protected CheckboxTableViewer viewer;
  protected CopyrightSettings settings;

  ProjectSelectionWizardPage() {
    super(DEFAULT_PAGE_NAME);
    setTitle(Messages.ProjectSelectionWizardPage_title);
    setDescription(Messages.ProjectSelectionWizardPage_description);
  }

  public void createControl(Composite parent) {
    Font font = parent.getFont();

    Composite top = new Composite(parent, SWT.NONE);
    top.setLayout(new GridLayout());
    top.setLayoutData(new GridData(GridData.FILL_BOTH));
    top.setFont(font);

    viewer = CheckboxTableViewer.newCheckList(top, SWT.BORDER);
    GridData data = new GridData(GridData.FILL_BOTH);
    viewer.getControl().setLayoutData(data);
    viewer.getControl().setFont(font);
    viewer.setContentProvider(new WorkbenchContentProvider() {
      @Override
      public Object[] getChildren(Object element) {
        if ( ! (element instanceof IWorkspace) ) {
          return new Object[0];
        }
        List<IProject> projects = new ArrayList<IProject>();
        for (IProject project : ((IWorkspace) element).getRoot().getProjects()) {
          if ( project.isOpen() ) {
            projects.add(project);
          }
        }
        return projects.toArray();
      }
    });
    viewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
    viewer.setInput(ResourcesPlugin.getWorkspace());
    viewer.addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        validatePage();
      }
    });
    viewer.setCheckedElements(settings.getProjects());

    setPageComplete(settings.getProjects().length > 0);
    setControl(top);
  }

  public IProject[] getSelectedProjects() {
    Object[] objs = viewer.getCheckedElements();
    IProject[] projects = new IProject[objs.length];
    for (int i = 0; i < objs.length; i++) {
      projects[i] = (IProject) objs[i];
    }
    return projects;
  }

  public void init(CopyrightSettings settings) {
    this.settings = settings;
  }

  protected void validatePage() {
    setPageComplete(viewer.getCheckedElements().length > 0);
    settings.setProjects(getSelectedProjects());
  }
}
