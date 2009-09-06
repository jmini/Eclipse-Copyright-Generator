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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.wdev91.eclipse.copyright.Activator;
import com.wdev91.eclipse.copyright.Messages;
import com.wdev91.eclipse.copyright.model.CopyrightException;
import com.wdev91.eclipse.copyright.model.CopyrightManager;
import com.wdev91.eclipse.copyright.model.CopyrightSettings;

public class ApplyCopyrightWizard extends Wizard {
	public static final String CONTEXT_ID = Activator.PLUGIN_ID + ".wizard"; //$NON-NLS-1$

	protected ProjectSelectionWizardPage projectsPage;
  protected CopyrightSettingsPage settingsPage;
  protected ResourcesSelectionPage selectionPage;
  protected CopyrightSettings settings;

  public ApplyCopyrightWizard() {
    setWindowTitle(Messages.ApplyCopyrightWizard_title);
  }

  @Override
  public void addPages() {
    projectsPage = new ProjectSelectionWizardPage();
    addPage(projectsPage);

    settingsPage = new CopyrightSettingsPage();
    addPage(settingsPage);

    selectionPage = new ResourcesSelectionPage();
    addPage(selectionPage);

    IPageChangingListener listener = new IPageChangingListener() {
      public void handlePageChanging(PageChangingEvent event) {
        if ( event.getTargetPage() == selectionPage && settings.isChanged() ) {
          computeSelectionWithProgress();
        }
      }
    };
    ((WizardDialog) getContainer()).addPageChangingListener(listener);

    projectsPage.init(settings);
    settingsPage.init(settings);
  }

  private void computeSelectionWithProgress() {
    IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
    try {
      progressService.runInUI(progressService,
          new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) {
              monitor.beginTask(Messages.ApplyCopyrightWizard_selectionTaskMessage,
                                IProgressMonitor.UNKNOWN);
              try {
								selectionPage.setSelection(CopyrightManager.selectResources(settings, monitor));
								settings.setChanged(false);
	              monitor.done();
							} catch (CopyrightException e) {
	              monitor.done();
					      MessageDialog.openError(getShell(), Messages.ApplyCopyrightWizard_error,
                    										e.getMessage());
							}
            }
          },
          null);
    } catch (Exception e) {
    }
  }

  public void init(IProject[] projects) {
    settings = new CopyrightSettings();
    settings.setProjects(projects);
  }

  public static void openWizard(Shell shell, IProject[] projects) {
    ApplyCopyrightWizard wizard = new ApplyCopyrightWizard();
    wizard.init(projects);
    WizardDialog dialog = new WizardDialog(shell, wizard);
    dialog.open();
  }

  @Override
  public boolean performFinish() {
    selectionPage.getSelection(settings);
    CopyrightManager.applyCopyrightJob(settings);
    return true;
  }
}
