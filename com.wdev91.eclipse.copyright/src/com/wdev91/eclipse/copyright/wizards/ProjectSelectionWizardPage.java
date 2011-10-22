/*******************************************************************************
 * Copyright (c) 2008-2011 Eric Wuillai.
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchContentProvider;

import com.wdev91.eclipse.copyright.Messages;
import com.wdev91.eclipse.copyright.model.CopyrightSettings;

/**
 * Wizard page for the selection of projects on which to apply the copyright.
 */
public class ProjectSelectionWizardPage extends WizardPage {
  public static final String DEFAULT_PAGE_NAME = "projectSelectionPage"; //$NON-NLS-1$

  protected CheckboxTableViewer viewer;
  protected Button override;
  protected Button overrideText;

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
    viewer.setLabelProvider(new ProjectLabelProvider());
    viewer.setInput(ResourcesPlugin.getWorkspace());
    viewer.addCheckStateListener(new ICheckStateListener() {
      public void checkStateChanged(CheckStateChangedEvent event) {
        validatePage();
      }
    });
    viewer.setCheckedElements(settings.getProjects());

    override = new Button(top, SWT.CHECK);
    override.setText(Messages.ProjectSelectionWizardPage_checkboxOverrideSettings);

    overrideText = new Button(top, SWT.CHECK);
    overrideText.setText(Messages.ProjectSelectionWizardPage_checkboxOverrideFormatsOnly);
    overrideText.setEnabled(false);
    data = new GridData();
    data.horizontalIndent = 15;
    overrideText.setLayoutData(data);

    Listener listener = new Listener() {
      public void handleEvent(Event event) {
        if ( event.widget == override ) {
          overrideText.setEnabled(override.getSelection());
        }
        validatePage();
      }
    };
    override.addListener(SWT.Selection, listener);
    overrideText.addListener(SWT.Selection, listener);

    PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ApplyCopyrightWizard.CONTEXT_ID);
    setPageComplete(settings.getProjects().length > 0);
    setControl(top);
  }

  /**
   * Returns the override selection, coded as an integer:
   *  0: no override of projects copyright settings
   *  1: override header content only. Projects header formats definitions are preserved.
   *  2: override header content and formats definitions.
   * 
   * @return override selection code
   */
  protected int getOverrideSelection() {
    return override.getSelection()
           ? (overrideText.getSelection()
              ? CopyrightSettings.OVERRIDE_TEXT
              : CopyrightSettings.OVERRIDE_ALL)
           : CopyrightSettings.OVERRIDE_NONE;
  }

  /**
   * Returns an array of all the selected projects, on which the copyright will
   * be applied.
   * 
   * @return array of projects
   */
  protected IProject[] getSelectedProjects() {
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
    settings.setOverride(getOverrideSelection());
  }
}
