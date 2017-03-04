/*******************************************************************************
 * Copyright (c) 2008-2012 Eric Wuillai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Eric Wuillai - initial API and implementation
 ******************************************************************************/
package com.wdev91.eclipse.copyright.actions;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.wdev91.eclipse.copyright.Activator;
import com.wdev91.eclipse.copyright.wizards.ApplyCopyrightWizard;

/**
 * Opens the wizard to check and apply a copyright header in selected resource
 * files of a given project.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class ApplyCopyrightAction implements IWorkbenchWindowActionDelegate {
  public static final String ACTION_ID = Activator.PLUGIN_ID + ".ApplyCopyrightAction"; //$NON-NLS-1$

  private IWorkbenchWindow window;
  private IStructuredSelection selection;

  /**
   * The constructor.
   */
  public ApplyCopyrightAction() {}

  /**
   * Disposes any system resources we previously allocated.
   * 
   * @see IWorkbenchWindowActionDelegate#dispose
   */
  public void dispose() {
    selection = null;
    window = null;
  }

  public Object getAdapter(Object element, Class<?> adapterType) {
    if ( adapterType.isInstance(element) ) {
      return element;
    }
    if (element instanceof IAdaptable) {
      Object adapted = ((IAdaptable) element).getAdapter(adapterType);
      if ( adapterType.isInstance(adapted) ) {
        return adapted;
      }
    }
    Object adapted = Platform.getAdapterManager().getAdapter(element, adapterType);
    if ( adapterType.isInstance(adapted) ) {
      return adapted;
    }
    return null;
  }

  protected IProject[] getSelectedProjects() {
    ArrayList<IProject> projects = new ArrayList<IProject>();

    for (Object obj : getSelection().toArray()) {
      if ( obj instanceof IResource ) {
        projects.add(((IResource) obj).getProject());
      } else if ( obj instanceof ResourceMapping ) {
        for (IProject project : ((ResourceMapping) obj).getProjects()) {
          projects.add(project);
        }
      } else if ( obj != null ) {
        Object adapted = getAdapter(obj, IResource.class);
        if ( adapted instanceof IResource ) {
          projects.add(((IResource) adapted).getProject());
        } else {
          adapted = getAdapter(obj, ResourceMapping.class);
          if ( adapted instanceof ResourceMapping ) {
            for (IProject project : ((ResourceMapping) adapted).getProjects()) {
              projects.add(project);
            }
          }
        }
      }
    }
    return projects.toArray(new IProject[projects.size()]);
  }

  protected IStructuredSelection getSelection() {
    if ( selection == null ) {
      selection = StructuredSelection.EMPTY;
    }
    return selection;
  }

  /**
   * Initialisation of the action.
   * Caches window object in order to be able to provide parent shell for the
   * wizard dialog.
   * 
   * @see IWorkbenchWindowActionDelegate#init
   */
  public void init(IWorkbenchWindow window) {
    this.window = window;
  }

  /**
   * Action execution.
   * Opens the wizard dialog.
   * 
   * @see IWorkbenchWindowActionDelegate#run
   */
  public void run(IAction action) {
    ApplyCopyrightWizard.openWizard(window.getShell(), getSelectedProjects());
  }

  /**
   * Selection in the workbench has been changed.
   * 
   * @see IWorkbenchWindowActionDelegate#selectionChanged
   */
  public void selectionChanged(IAction action, ISelection selection) {
    if ( selection instanceof IStructuredSelection ) {
	  this.selection = (IStructuredSelection) selection;
    }
  }
}
