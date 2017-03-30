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
package com.wdev91.eclipse.copyright.controls;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;

public class CheckboxFilteredTree extends FilteredTree {

  public CheckboxFilteredTree(Composite parent, boolean useNewLook) {
    super(parent, useNewLook);
  }

  public CheckboxFilteredTree(Composite parent, int treeStyle,
		  PatternFilter filter, boolean useNewLook) {
    super(parent, treeStyle, filter, useNewLook);
  }

  @Override
  protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
    return new CheckboxTreeViewer(parent, style);
  }

  @Override
  public CheckboxTreeViewer getViewer() {
    return (CheckboxTreeViewer) super.getViewer();
  }
}
