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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.wdev91.eclipse.copyright.Messages;
import com.wdev91.eclipse.copyright.model.CopyrightManager;
import com.wdev91.eclipse.copyright.model.CopyrightSettings;
import com.wdev91.eclipse.copyright.model.ProjectPreferences;
import com.wdev91.eclipse.copyright.wizards.ApplyCopyrightWizard;

/**
 * Apply copyright... command.
 * Allow to apply a copyright on selected resources from a popup menu. Mainly
 * concern Eclipse navigator and package explorer.
 */
public class ApplyCopyrightOnSelectionHandler extends AbstractHandler {
  public static final String COMMAND_ID = "com.wdev91.eclipse.copyright.ApplyCopyrightCommand"; //$NON-NLS-1$

  private void addFile(IResource res, List<IFile> resources) {
    if ( res instanceof IFile ) {
      if ( ! resources.contains(res) ) {
        resources.add((IFile) res);
      }
    } else if ( res instanceof IFolder ) {
      try {
        for (IResource member : ((IFolder) res).members(IFolder.EXCLUDE_DERIVED)) {
          addFile(member, resources);
        }
      } catch (CoreException e) {}
    }
  }

  public Object execute(ExecutionEvent event) throws ExecutionException {
    // Creates list of selected files
    List<IFile> resources = new ArrayList<IFile>();
    IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getActiveMenuSelection(event);
    for (Object sel : selection.toArray()) {
      if ( sel instanceof IFile || sel instanceof IFolder ) {
        addFile((IResource) sel, resources);
      } else {
        Object ao = null;
        if ( sel instanceof IAdaptable ) {
          ao = ((IAdaptable) sel).getAdapter(IFile.class);
          if ( ao == null ) {
            ao = ((IAdaptable) sel).getAdapter(IFolder.class);
          }
        }
        if ( ao == null ) {
          ao = findJavaResource(sel);
        }
        if ( ao != null ) {
          addFile((IResource) ao, resources);
        }
      }
    }

    // List of projects containing the selected files, with analyze if wizard is needed
    List<IProject> projects = new ArrayList<IProject>();
    boolean wizard = false;
    for (IFile f : resources) {
      IProject p = f.getProject();
      if ( ! projects.contains(p) ) {
        projects.add(p);
        ProjectPreferences prefs;
        if ( (prefs = CopyrightManager.getProjectPreferences(p)) == null
        		|| prefs.getHeaderText() == null )
          wizard = true;
      }
    }

    // Apply the copyrights
    Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
    if ( wizard ) {
      ApplyCopyrightWizard.openWizard(shell, projects, resources);
    } else {
      if ( MessageDialog.openConfirm(shell, Messages.ApplyCopyrightOnSelectionHandler_messageTitle,
    		  NLS.bind(Messages.ApplyCopyrightOnSelectionHandler_confirmMessage,
    				  resources.size())) ) {
        CopyrightSettings settings = new CopyrightSettings();
        settings.setFiles(resources.toArray(new IFile[] {}));
        CopyrightManager.applyCopyrightJob(settings);
      }
    }

    return null;
  }

  private IResource findJavaResource(Object obj) {
    if ( ! obj.getClass().getPackage().getName().startsWith("org.eclipse.jdt") ) //$NON-NLS-1$
      return null;

    try {
      Method m = obj.getClass().getMethod("getResource"); //$NON-NLS-1$
      Object res = m.invoke(obj);
      if ( res instanceof IFile || res instanceof IFolder ) {
        return (IResource) res;
      }
    } catch (Exception e) {
      // Method not found
    }
    return null;
  }
}
