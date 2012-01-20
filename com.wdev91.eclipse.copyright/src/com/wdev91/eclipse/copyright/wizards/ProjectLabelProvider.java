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
package com.wdev91.eclipse.copyright.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.wdev91.eclipse.copyright.model.CopyrightManager;
import com.wdev91.eclipse.copyright.model.ProjectPreferences;

class ProjectLabelProvider implements ILabelProvider {
  private static final String COPYRIGHT_LABEL = "  (C)"; //$NON-NLS-1$

  private ILabelProvider workbenchProvider;

  ProjectLabelProvider() {
    workbenchProvider = WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider();
  }

  public Image getImage(Object element) {
    return workbenchProvider.getImage(element);
  }

  public String getText(Object element) {
    String text = workbenchProvider.getText(element);
    if ( CopyrightManager.getProjectPreferences((IProject) element) != ProjectPreferences.NO_PREFS ) {
      return text + COPYRIGHT_LABEL;
    } else {
      return text;
    }
  }

  public void addListener(ILabelProviderListener listener) {
    workbenchProvider.addListener(listener);
  }

  public void dispose() {
    workbenchProvider.dispose();
  }

  public boolean isLabelProperty(Object element, String property) {
    return workbenchProvider.isLabelProperty(element, property);
  }

  public void removeListener(ILabelProviderListener listener) {
    workbenchProvider.removeListener(listener);
  }
}
