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
package com.wdev91.eclipse.copyright.preferences;

import java.util.Collection;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.wdev91.eclipse.copyright.Messages;
import com.wdev91.eclipse.copyright.model.CopyrightManager;
import com.wdev91.eclipse.copyright.model.HeaderFormat;

public class HeadersPreferencePage extends PreferencePage implements
    IWorkbenchPreferencePage {
  private FormatsPanel formats;

  public HeadersPreferencePage() {
    super();
    noDefaultAndApplyButton();
  }

  @Override
  protected Control createContents(Composite parent) {
    formats = new FormatsPanel(parent, SWT.NONE);
    formats.setFormats(CopyrightManager.getHeadersFormats());

    return formats;
  }

  public void init(IWorkbench workbench) {
  }

  @Override
  public boolean performOk() {
    Collection<HeaderFormat> headerFormats = formats.getFormats();
    for (HeaderFormat format : headerFormats) {
      if ( ! format.isExcluded()
      		 && ( format.getBeginLine().trim().length() == 0
                || format.getEndLine().trim().length() == 0) ) {
        MessageDialog.openError(getShell(), Messages.HeadersPreferencePage_errorTitle,
                                NLS.bind(Messages.HeadersPreferencePage_errorInvalidHeaderFormat,
                                    Platform.getContentTypeManager()
                                            .getContentType(format.getContentId())
                                            .getName()));
        return false;
      }
    }
    CopyrightManager.saveFormats(headerFormats);
    return true;
  }
}
