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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.wdev91.eclipse.copyright.model.CopyrightSelectionItem;

public class SelectionLabelProvider extends LabelProvider {
  private WorkbenchLabelProvider wlp = new WorkbenchLabelProvider();

  @Override
  public Image getImage(Object element) {
    return wlp.getImage(((CopyrightSelectionItem) element).getResource());
  }

  @Override
  public String getText(Object element) {
    return wlp.getText(((CopyrightSelectionItem) element).getResource());
  }
}
