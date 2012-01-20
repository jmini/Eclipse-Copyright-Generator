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
package com.wdev91.eclipse.copyright.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ContentTypesContentProvider implements ITreeContentProvider {
  private IContentTypeManager manager;
  private boolean textOnly;
  private IContentType textContentType;

  public ContentTypesContentProvider(boolean textOnly) {
    this.textOnly = textOnly;
  }

  public static boolean equals(Object left, Object right) {
    return left == null
           ? right == null
           : ((right != null) && left.equals(right));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  public Object[] getChildren(Object parentElement) {
    List<IContentType> elements = new ArrayList<IContentType>();
    IContentType baseType = (IContentType) parentElement;
    IContentType[] contentTypes = manager.getAllContentTypes();
    for (int i = 0; i < contentTypes.length; i++) {
      IContentType type = contentTypes[i];
      if ( equals(type.getBaseType(), baseType)
          && (! textOnly || type.isKindOf(textContentType)) ) {
        elements.add(type);
      }
    }
    return elements.toArray();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent(Object element) {
    IContentType contentType = (IContentType) element;
    return contentType.getBaseType();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren(Object element) {
    return getChildren(element).length > 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements(Object inputElement) {
    return getChildren(null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
   *      java.lang.Object, java.lang.Object)
   */
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    manager = (IContentTypeManager) newInput;
    if ( manager != null ) {
      textContentType = manager.getContentType(IContentTypeManager.CT_TEXT);
    }
  }
}
