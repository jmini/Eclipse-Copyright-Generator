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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.wdev91.eclipse.copyright.Constants;
import com.wdev91.eclipse.copyright.Messages;
import com.wdev91.eclipse.copyright.model.HeaderFormat;

public class FormatsPanel extends Composite {
  private static final int TREE_LINES_NUMBER = 15;
  private static final String LENGTH_LABEL_PREFIX = "l:"; //$NON-NLS-1$

  private TreeViewer contentTypesViewer;
  private Button excludedButton;
  private Text firstLineText;
  private Text linePrefixText;
  private Text lastLineText;
  private Text postBlankLinesText;
  private Button lineFormatButton;
  private Button preserveFirstLineButton;
  private Text firstLinePatternText;
  private Button clearButton;

  private Map<String, HeaderFormat> headerFormats = new HashMap<String, HeaderFormat>();
  private String currentId = null;

  public FormatsPanel(Composite parent, int style) {
    super(parent, style);
    createContent();
  }

  private void changeSelection() {
    IContentType contentType = (IContentType) ((IStructuredSelection) contentTypesViewer.getSelection()).getFirstElement();
    if ( contentType != null ) {
      currentId = contentType.getId();
      HeaderFormat format = headerFormats.get(currentId);
      if ( format != null ) {
      	if ( ! format.isExcluded() ) {
          firstLineText.setText(format.getBeginLine());
          linePrefixText.setText(format.getLinePrefix());
          lastLineText.setText(format.getEndLine());
          postBlankLinesText.setText(Constants.EMPTY_STRING + format.getPostBlankLines());
          lineFormatButton.setSelection(format.isLineCommentFormat());
          preserveFirstLineButton.setSelection(format.isPreserveFirstLine());
          if ( format.getFirstLinePattern() != null ) {
          	firstLinePatternText.setText(format.getFirstLinePattern());
          } else {
          	firstLinePatternText.setText(Constants.EMPTY_STRING);
          }
      	} else {
          clearFields();
      	}
      	excludedButton.setSelection(format.isExcluded());
      	setEnabled(true);
      } else {
        clearFields();
      }
    }
  }

  private void clearFields() {
  	excludedButton.setSelection(false);
    firstLineText.setText(Constants.EMPTY_STRING);
    linePrefixText.setText(Constants.EMPTY_STRING);
    lastLineText.setText(Constants.EMPTY_STRING);
    postBlankLinesText.setText("0"); //$NON-NLS-1$
    lineFormatButton.setSelection(false);
    preserveFirstLineButton.setSelection(false);
    firstLinePatternText.setText(Constants.EMPTY_STRING);
    setEnabled(clearButton.isEnabled());
  }

  private int convertHorizontalDLUsToPixels(int dlus) {
    GC gc = new GC(this);
    gc.setFont(JFaceResources.getDialogFont());
    FontMetrics fontMetrics = gc.getFontMetrics();
    gc.dispose();

    if ( fontMetrics == null ) {
      return 0;
    }
    return Dialog.convertHorizontalDLUsToPixels(fontMetrics, dlus);
  }

  protected void createContent() {
    FontData[] fontData = this.getFont().getFontData();

    GridLayout layout = new GridLayout(4, false);
    layout.marginHeight = 5;
    layout.marginWidth = 5;
    this.setLayout(layout);

    contentTypesViewer = new TreeViewer(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
    contentTypesViewer.getControl().setFont(this.getFont());
    contentTypesViewer.setContentProvider(new ContentTypesContentProvider(true));
    contentTypesViewer.setLabelProvider(new LabelProvider() {
      @Override
			public String getText(Object element) {
        IContentType contentType = (IContentType) element;
        return contentType.getName();
      }
    });
    contentTypesViewer.setComparator(new ViewerComparator());
    contentTypesViewer.setInput(Platform.getContentTypeManager());
    GridData data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 4;
    data.heightHint = (fontData.length > 0 ? fontData[0].getHeight() : 10) * TREE_LINES_NUMBER;
    contentTypesViewer.getControl().setLayoutData(data);

    excludedButton = new Button(this, SWT.CHECK);
    excludedButton.setText(Messages.FormatsPanel_labelExcludeType);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 4;
    excludedButton.setLayoutData(data);

    new Label(this, SWT.NONE).setText(Messages.HeadersPreferencePage_labelFirstLine);
    firstLineText = new Text(this, SWT.BORDER);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    firstLineText.setLayoutData(data);
    final Label length1 = new Label(this, SWT.NONE);
    length1.setText(LENGTH_LABEL_PREFIX);
    data = new GridData();
    data.widthHint = 30;
    length1.setLayoutData(data);

    new Label(this, SWT.NONE).setText(Messages.HeadersPreferencePage_labelLinePrefix);
    linePrefixText = new Text(this, SWT.BORDER);
    data = new GridData();
    data.horizontalSpan = 2;
    data.widthHint = 50;
    linePrefixText.setLayoutData(data);
    final Label length2 = new Label(this, SWT.NONE);
    length2.setText(LENGTH_LABEL_PREFIX);
    data = new GridData();
    data.widthHint = 30;
    length2.setLayoutData(data);

    new Label(this, SWT.NONE).setText(Messages.HeadersPreferencePage_labelLastLine);
    lastLineText = new Text(this, SWT.BORDER);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    lastLineText.setLayoutData(data);
    final Label length3 = new Label(this, SWT.NONE);
    length3.setText(LENGTH_LABEL_PREFIX);
    data = new GridData();
    data.widthHint = 30;
    length3.setLayoutData(data);

    Label bll = new Label(this, SWT.NONE);
    bll.setText(Messages.HeadersPreferencePage_labelBlankLines);
    data = new GridData();
    data.horizontalSpan = 2;
    bll.setLayoutData(data);
    postBlankLinesText = new Text(this, SWT.BORDER);
    data = new GridData();
    data.horizontalSpan = 2;
    data.widthHint = 20;
    postBlankLinesText.setLayoutData(data);

    lineFormatButton = new Button(this, SWT.CHECK);
    lineFormatButton.setText(Messages.HeadersPreferencePage_checkLineFormat);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 4;
    lineFormatButton.setLayoutData(data);

    preserveFirstLineButton = new Button(this, SWT.CHECK);
    preserveFirstLineButton.setText(Messages.HeadersPreferencePage_checkPreserveFirstLine);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 4;
    preserveFirstLineButton.setLayoutData(data);

    new Label(this, SWT.NONE).setText(Messages.FormatsPanel_labelFirstLinePattern);
    firstLinePatternText = new Text(this, SWT.BORDER);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 2;
    firstLinePatternText.setLayoutData(data);
    firstLinePatternText.setEnabled(false);

    clearButton = new Button(this, SWT.PUSH);
    clearButton.setText(Messages.HeadersPreferencePage_buttonClear);
    data = new GridData(GridData.FILL_HORIZONTAL);
    data.horizontalSpan = 4;
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
    excludedButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEnabled(true);
			}
    });
    preserveFirstLineButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				firstLinePatternText.setEnabled(preserveFirstLineButton.getSelection());
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
  }

  public Collection<HeaderFormat> getFormats() {
    saveCurrentFormat();
    return headerFormats.values();
  }

  private void saveCurrentFormat() {
    if ( currentId == null ) return;

    boolean ex = excludedButton.getSelection();
    String fl = firstLineText.getText();
    String lp = linePrefixText.getText();
    String ll = lastLineText.getText();
    String bl = postBlankLinesText.getText().trim();
    boolean lf = lineFormatButton.getSelection();
    boolean pf = preserveFirstLineButton.getSelection();
    String flp = firstLinePatternText.getText().trim();
    if ( ! ex && ! lf && ! pf
    		 && fl.trim().length() + lp.trim().length() + ll.trim().length() == 0 ) {
      headerFormats.remove(currentId);
    } else {
      HeaderFormat format = headerFormats.get(currentId);
      if ( format == null ) {
        format = new HeaderFormat(currentId);
        headerFormats.put(currentId, format);
      }
    	format.setExcluded(ex);
      if ( ! ex ) {
        format.setBeginLine(fl);
        format.setLinePrefix(lp);
        format.setEndLine(ll);
        format.setPostBlankLines(bl.length() > 0 ? Math.abs(Integer.parseInt(bl)) : 0);
        format.setLineCommentFormat(lf);
        format.setPreserveFirstLine(pf);
        format.setFirstLinePattern(flp.length() > 0 ? flp : null);
      }
    }
  }

  @Override
  public void setEnabled(boolean enabled) {
  	boolean excluded = excludedButton.getSelection();
    contentTypesViewer.getControl().setEnabled(enabled);
    excludedButton.setEnabled(enabled);
    firstLineText.setEnabled(enabled && ! excluded);
    linePrefixText.setEnabled(enabled && ! excluded);
    lastLineText.setEnabled(enabled && ! excluded);
    postBlankLinesText.setEnabled(enabled && ! excluded);
    lineFormatButton.setEnabled(enabled && ! excluded);
    preserveFirstLineButton.setEnabled(enabled && ! excluded);
    firstLinePatternText.setEnabled(enabled && ! excluded
    																&& preserveFirstLineButton.getSelection());
    clearButton.setEnabled(enabled);
  }

  public void setFormats(Collection<HeaderFormat> formats) {
    headerFormats.clear();
    for (HeaderFormat f : formats) {
      headerFormats.put(f.getContentId(), f);
    }
    changeSelection();
  }
}
