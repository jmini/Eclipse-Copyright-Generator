/*******************************************************************************
 * Copyright (c) 2008 Eric Wuillai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Eric Wuillai - initial API and implementation
 ******************************************************************************/
package com.wdev91.eclipse.copyright.preferences;

import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.wdev91.eclipse.copyright.Constants;
import com.wdev91.eclipse.copyright.Messages;
import com.wdev91.eclipse.copyright.model.CopyrightManager;
import com.wdev91.eclipse.copyright.model.HeaderFormat;

public class HeadersPreferencePage extends PreferencePage implements
    IWorkbenchPreferencePage {
  private static final int TREE_LINES_NUMBER = 20;
  private static final String LENGTH_LABEL_PREFIX = "l:"; //$NON-NLS-1$

  private TreeViewer contentTypesViewer;
  private Text firstLineText;
  private Text linePrefixText;
  private Text lastLineText;
  private Button lineFormatButton;
  private Button preserveFirstLineButton;

  private Map<String, HeaderFormat> headerFormats;
  private String currentId = null;

  public HeadersPreferencePage() {
    super();
    noDefaultAndApplyButton();
  }

  private void changeSelection() {
    IContentType contentType = (IContentType) ((IStructuredSelection) contentTypesViewer.getSelection()).getFirstElement();
    if ( contentType != null ) {
      currentId = contentType.getId();
      HeaderFormat format = headerFormats.get(currentId);
      if ( format != null ) {
        firstLineText.setText(format.getBeginLine());
        linePrefixText.setText(format.getLinePrefix());
        lastLineText.setText(format.getEndLine());
        lineFormatButton.setSelection(format.isLineCommentFormat());
        preserveFirstLineButton.setSelection(format.isPreserveFirstLine());
      } else {
        clearFields();
      }
    }
  }

  private void clearFields() {
    firstLineText.setText(Constants.EMPTY_STRING);
    linePrefixText.setText(Constants.EMPTY_STRING);
    lastLineText.setText(Constants.EMPTY_STRING);
    lineFormatButton.setSelection(false);
    preserveFirstLineButton.setSelection(false);
  }

  @Override
  protected Control createContents(Composite parent) {
    Font font = parent.getFont();
    FontData[] fontData = font.getFontData();

    Composite top = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(3, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    top.setLayout(layout);
    top.setFont(font);

    contentTypesViewer = new TreeViewer(top, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    contentTypesViewer.getControl().setFont(top.getFont());
    contentTypesViewer.setContentProvider(new ContentTypesContentProvider(true));
    contentTypesViewer.setLabelProvider(new LabelProvider() {
      public String getText(Object element) {
        IContentType contentType = (IContentType) element;
        return contentType.getName();
      }
    });
    contentTypesViewer.setComparator(new ViewerComparator());
    contentTypesViewer.setInput(Platform.getContentTypeManager());
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 3;
    data.heightHint = (fontData.length > 0 ? fontData[0].getHeight() : 10) * TREE_LINES_NUMBER;
    contentTypesViewer.getControl().setLayoutData(data);

    new Label(top, SWT.NONE).setText(Messages.HeadersPreferencePage_labelFirstLine);
    firstLineText = new Text(top, SWT.BORDER);
    firstLineText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Label length1 = new Label(top, SWT.NONE);
    length1.setText(LENGTH_LABEL_PREFIX);
    data = new GridData();
    data.widthHint = 30;
    length1.setLayoutData(data);

    new Label(top, SWT.NONE).setText(Messages.HeadersPreferencePage_labelLinePrefix);
    linePrefixText = new Text(top, SWT.BORDER);
    final Label length2 = new Label(top, SWT.NONE);
    length2.setText(LENGTH_LABEL_PREFIX);
    data = new GridData();
    data.widthHint = 30;
    length2.setLayoutData(data);

    new Label(top, SWT.NONE).setText(Messages.HeadersPreferencePage_labelLastLine);
    lastLineText = new Text(top, SWT.BORDER);
    lastLineText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    final Label length3 = new Label(top, SWT.NONE);
    length3.setText(LENGTH_LABEL_PREFIX);
    data = new GridData();
    data.widthHint = 30;
    length3.setLayoutData(data);

    lineFormatButton = new Button(top, SWT.CHECK);
    lineFormatButton.setText(Messages.HeadersPreferencePage_checkLineFormat);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 3;
    lineFormatButton.setLayoutData(data);

    preserveFirstLineButton = new Button(top, SWT.CHECK);
    preserveFirstLineButton.setText(Messages.HeadersPreferencePage_checkPreserveFirstLine);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 3;
    preserveFirstLineButton.setLayoutData(data);

    Button clearButton = new Button(top, SWT.PUSH);
    clearButton.setText(Messages.HeadersPreferencePage_buttonClear);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 3;
    data.horizontalAlignment = GridData.END;
    data.widthHint = Math.max(convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH),
                              clearButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
    clearButton.setLayoutData(data);

    contentTypesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        saveCurrentFormat();
        changeSelection();
      }
    });
    clearButton.addSelectionListener(new SelectionListener() {
      public void widgetDefaultSelected(SelectionEvent e) {
      }

      public void widgetSelected(SelectionEvent e) {
        clearFields();
      }
    });
    ModifyListener listener = new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        if ( e.widget == firstLineText ) {
          length1.setText(LENGTH_LABEL_PREFIX + firstLineText.getText().length());
        } else if ( e.widget == linePrefixText ) {
          length2.setText(LENGTH_LABEL_PREFIX + linePrefixText.getText().length());
        } else if ( e.widget == lastLineText ) {
          length3.setText(LENGTH_LABEL_PREFIX + lastLineText.getText().length());
        }
      }
    };
    firstLineText.addModifyListener(listener);
    linePrefixText.addModifyListener(listener);
    lastLineText.addModifyListener(listener);

    return top;
  }

  public void init(IWorkbench workbench) {
    headerFormats = CopyrightManager.getAllHeadersFormats();
  }

  @Override
  public boolean performOk() {
    saveCurrentFormat();
    for (HeaderFormat format : headerFormats.values()) {
      if ( format.getBeginLine().trim().length() == 0
           || format.getEndLine().trim().length() == 0 ) {
        MessageDialog.openError(getShell(), Messages.HeadersPreferencePage_errorTitle,
                                NLS.bind(Messages.HeadersPreferencePage_errorInvalidHeaderFormat,
                                    Platform.getContentTypeManager()
                                            .getContentType(format.getContentId())
                                            .getName()));
        return false;
      }
    }
    CopyrightManager.save(headerFormats);
    return true;
  }

  private void saveCurrentFormat() {
    if ( currentId == null ) return;

    String fl = firstLineText.getText();
    String lp = linePrefixText.getText();
    String ll = lastLineText.getText();
    boolean lf = lineFormatButton.getSelection();
    boolean pf = preserveFirstLineButton.getSelection();
    if ( fl.trim().length() + lp.trim().length() + ll.trim().length() == 0 && ! lf && ! pf ) {
      headerFormats.remove(currentId);
    } else {
      HeaderFormat format = headerFormats.get(currentId);
      if ( format == null ) {
        format = new HeaderFormat(currentId);
        headerFormats.put(currentId, format);
      }
      format.setBeginLine(fl);
      format.setLinePrefix(lp);
      format.setEndLine(ll);
      format.setLineCommentFormat(lf);
      format.setPreserveFirstLine(pf);
    }
  }
}
