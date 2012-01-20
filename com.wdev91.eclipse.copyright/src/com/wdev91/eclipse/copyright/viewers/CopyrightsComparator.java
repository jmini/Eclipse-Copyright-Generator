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
package com.wdev91.eclipse.copyright.viewers;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.wdev91.eclipse.copyright.model.CopyrightManager;

public class CopyrightsComparator extends ViewerComparator {
  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {
    return e1 == CopyrightManager.CUSTOM ? -1 : super.compare(viewer, e1, e2);
  }
}
