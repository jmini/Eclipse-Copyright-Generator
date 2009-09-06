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
package com.wdev91.eclipse.copyright.model;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.content.IContentTypeManager;

import com.wdev91.eclipse.copyright.Constants;

public class HeaderFormat {
  public static final String CT_JAVA = "org.eclipse.jdt.core.javaSource"; //$NON-NLS-1$
  public static final String CT_XML = "org.eclipse.core.runtime.xml"; //$NON-NLS-1$

  public static final HeaderFormat TEXT_HEADER;
  public static final HeaderFormat JAVA_HEADER;
  public static final HeaderFormat XML_HEADER;

  protected String contentId;
  protected boolean excluded;
  protected boolean lineCommentFormat = true;
  protected String beginLine;
  protected String endLine;
  protected String linePrefix = Constants.EMPTY_STRING;
  protected int postBlankLines = 0;
  protected boolean preserveFirstLine = false;
  protected String firstLinePattern;

  protected Pattern pattern = null;

  static {
    TEXT_HEADER = new HeaderFormat(
        IContentTypeManager.CT_TEXT, false,
        "#-------------------------------------------------------------------------------", //$NON-NLS-1$
        "# ", //$NON-NLS-1$
        "#-------------------------------------------------------------------------------", //$NON-NLS-1$
        0, true, false, null);

    JAVA_HEADER = new HeaderFormat(
        CT_JAVA, false,
        "/*******************************************************************************", //$NON-NLS-1$
        " * ", //$NON-NLS-1$
        " ******************************************************************************/", //$NON-NLS-1$
        0, false, false, null);

    XML_HEADER = new HeaderFormat(
        CT_XML, false,
        "<!------------------------------------------------------------------------------", //$NON-NLS-1$
        "  ", //$NON-NLS-1$
        "------------------------------------------------------------------------------->", //$NON-NLS-1$
        0, false, true, "<\\?xml version=.*\\?>"); //$NON-NLS-1$
  }

  public HeaderFormat(String contentId) {
    this.contentId = contentId;
  }

  private HeaderFormat(String contentId, boolean excluded, String beginLine,
  		String linePrefix, String endLine, int postBlankLines,
  		boolean lineCommentFormat, boolean preserveFirstLine,
  		String firstLinePattern) {
    this.contentId = contentId;
    this.excluded = excluded;
    this.beginLine = beginLine;
    this.linePrefix = linePrefix;
    this.endLine = endLine;
    this.postBlankLines = postBlankLines;
    this.lineCommentFormat = lineCommentFormat;
    this.preserveFirstLine = preserveFirstLine;
    this.firstLinePattern = firstLinePattern;
  }

  @Override
  protected Object clone() {
    return new HeaderFormat(this.contentId, this.excluded, this.beginLine,
    												this.linePrefix, this.endLine, this.postBlankLines,
                            this.lineCommentFormat, this.preserveFirstLine,
                            this.firstLinePattern);
  }

  static HeaderFormat createExcluded(String contentId) {
  	HeaderFormat format = new HeaderFormat(contentId);
  	format.setExcluded(true);
  	return format;
  }

  @Override
	public boolean equals(Object obj) {
		return (obj instanceof HeaderFormat)
					 ? ((HeaderFormat) obj).getContentId().equals(this.contentId)
					 : false;
	}

	public boolean isExcluded() {
		return excluded;
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

  public String getFirstLinePattern() {
		return firstLinePattern;
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

  public void setExcluded(boolean excluded) {
		this.excluded = excluded;
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

  public void setFirstLinePattern(String firstLinePattern) {
		this.firstLinePattern = firstLinePattern;
		pattern = null;
	}

	public void setLinePrefix(String linePrefix) {
    this.linePrefix = linePrefix;
  }

  public void setPostBlankLines(int postBlankLines) {
    this.postBlankLines = postBlankLines;
  }

  public void setPreserveFirstLine(boolean preserveFirstLine) {
    this.preserveFirstLine = preserveFirstLine;
		pattern = null;
  }

  public boolean skipFirstLine(String line) {
  	if ( preserveFirstLine ) {
  		if ( firstLinePattern != null ) {
  			if ( pattern == null ) {
  				pattern = Pattern.compile(firstLinePattern);
  			}
  			return pattern.matcher(line).matches();
  		}
 			return true;
  	}
  	return false;
  }

  @Override
	public String toString() {
		return this.getContentId();
	}
}
