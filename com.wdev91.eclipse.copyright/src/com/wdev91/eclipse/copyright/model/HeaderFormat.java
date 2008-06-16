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
package com.wdev91.eclipse.copyright.model;

import org.eclipse.core.runtime.content.IContentTypeManager;

public class HeaderFormat {
  public static final String CT_JAVA = "org.eclipse.jdt.core.javaSource"; //$NON-NLS-1$
  public static final String CT_XML = "org.eclipse.core.runtime.xml"; //$NON-NLS-1$

  public static final HeaderFormat TEXT_HEADER;
  public static final HeaderFormat JAVA_HEADER;
  public static final HeaderFormat XML_HEADER;

  protected String contentId;
  protected boolean lineCommentFormat = true;
  protected String beginLine;
  protected String endLine;
  protected String linePrefix = ""; //$NON-NLS-1$
  protected int postBlankLines = 0;
  protected boolean preserveFirstLine = false;

  static {
    TEXT_HEADER = new HeaderFormat(
        IContentTypeManager.CT_TEXT,
        "#-------------------------------------------------------------------------------", //$NON-NLS-1$
        "# ", //$NON-NLS-1$
        "#-------------------------------------------------------------------------------", //$NON-NLS-1$
        0, true, false);

    JAVA_HEADER = new HeaderFormat(
        CT_JAVA,
        "/*******************************************************************************", //$NON-NLS-1$
        " * ", //$NON-NLS-1$
        " ******************************************************************************/", //$NON-NLS-1$
        0, false, false);

    XML_HEADER = new HeaderFormat(
        CT_XML,
        "<!------------------------------------------------------------------------------", //$NON-NLS-1$
        "  ", //$NON-NLS-1$
        "------------------------------------------------------------------------------->", //$NON-NLS-1$
        0, false, false);
  }

  public HeaderFormat(String contentId) {
    this.contentId = contentId;
  }

  private HeaderFormat(String contentId, String beginLine, String linePrefix,
      String endLine, int postBlankLines, boolean lineCommentFormat,
      boolean preserveFirstLine) {
    this.contentId = contentId;
    this.beginLine = beginLine;
    this.linePrefix = linePrefix;
    this.endLine = endLine;
    this.postBlankLines = postBlankLines;
    this.lineCommentFormat = lineCommentFormat;
    this.preserveFirstLine = preserveFirstLine;
  }

  @Override
  protected Object clone() {
    return new HeaderFormat(this.contentId, this.beginLine, this.linePrefix,
                            this.endLine, this.postBlankLines,
                            this.lineCommentFormat, this.preserveFirstLine);
  }

  public boolean isLineCommentFormat() {
    return lineCommentFormat;
  }

  public String getBeginLine() {
    return beginLine;
  }

  public String getContentId() {
    return contentId;
  }

  public String getEndLine() {
    return endLine;
  }

  public String getLinePrefix() {
    return linePrefix;
  }

  public int getPostBlankLines() {
    return postBlankLines;
  }

  public boolean isPreserveFirstLine() {
    return preserveFirstLine;
  }

  public void setLineCommentFormat(boolean lineCommentFormat) {
    this.lineCommentFormat = lineCommentFormat;
  }

  public void setBeginLine(String beginLine) {
    this.beginLine = beginLine;
  }

  public void setEndLine(String endLine) {
    this.endLine = endLine;
  }

  public void setLinePrefix(String linePrefix) {
    this.linePrefix = linePrefix;
  }

  public void setPostBlankLines(int postBlankLines) {
    this.postBlankLines = postBlankLines;
  }

  public void setPreserveFirstLine(boolean preserveFirstLine) {
    this.preserveFirstLine = preserveFirstLine;
  }
}
