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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.wdev91.eclipse.copyright.model.CopyrightSelectionItem;

public class SelectionContentProvider implements ITreeContentProvider {

  public Object[] getChildren(Object parentElement) {
    if ( parentElement instanceof CopyrightSelectionInput ) {
      return ((CopyrightSelectionInput) parentElement).getRootSelection();
    } else {
      CopyrightSelectionItem[] children = ((CopyrightSelectionItem) parentElement).getChildren();
      return children != null ? children : new Object[] {};
    }
  }

  public Object getParent(Object element) {
    if ( element instanceof CopyrightSelectionItem ) {
      return ((CopyrightSelectionItem) element).getParent();
    }
    return null;
  }

  public boolean hasChildren(Object element) {
    return getChildren(element).length > 0;
  }

  public Object[] getElements(Object inputElement) {
    return getChildren(inputElement);
  }

  public void dispose() {
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }
}
